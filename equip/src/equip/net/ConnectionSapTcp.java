/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

*/
/* ConnectionSapTcp.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

import equip.runtime.*;
import equip.config.*;

import java.net.*;
import java.io.*;

/** TCP-based implementation of {@link ConnectionSap}. 
 *
 * Also supports compression; see {@link equip.net.tcpcompress}. */
public class ConnectionSapTcp extends ConnectionSap {
    // ServerSapTcp
    ConnectionSapTcp(int remoteAddress, short remotePort,
		     Socket socket) {
	this.socket = socket;
	try {
	    socket.setTcpNoDelay(true);
	} catch (SocketException e) {
	    System.err.println("WARNING: unable to setTcpNoDelay: "+e);
	}
	oout = null;
	oin = null;
	init(remoteAddress, remotePort, false);
    }
    /** connect using default StreamFilter (if any) */
    public ConnectionSapTcp(int remoteAddress, short remotePort) {
	connect(remoteAddress, remotePort);
	if (socket!=null)
	    init(remoteAddress, remotePort, true);
    }
    /** connect using non-default StreamFilter */
    public ConnectionSapTcp(int remoteAddress, short remotePort,
			    String streamFilterClassName, 
			    String streamFilterArg) {
	connect(remoteAddress, remotePort);
	if (socket!=null)
	    init(remoteAddress, remotePort, true,
		 streamFilterClassName, streamFilterArg);
    }
    protected void connect(int remoteAddress, short remotePort) {
	oout = null;
	oin = null;
	try {
	    InetAddress addr = InetAddress.getByName
		(""+((remoteAddress >> 24) & 0xff)+
		 "."+((remoteAddress >> 16) & 0xff)+
		 "."+((remoteAddress >> 8) & 0xff)+
		 "."+((remoteAddress) & 0xff));
	    int port = ((int)remotePort)&0xffff;
	    System.err.println("Connect (TCP) to "+addr+":"+port+"...");
	    socket = new Socket(addr, port);
	    try {
		socket.setTcpNoDelay(true);
	    } catch (SocketException e) {
		System.err.println("WARNING: unable to setTcpNoDelay: "+e);
	    }
	} catch (Exception e) {
	    System.err.println
		("WARNING: ConnectionSapTcp::ConnectionSapTcp failed to connect: "+e);
	    socket = null;
	    status = StatusValues.STATUS_ERROR;
	    // release ConnectionSap keepAliveThread
	    keepAliveThread.start();
	    return;
	}
    }
    public static final String DEFAULT_TCP_FILTER_CONFIGNAME = 
	"DefaultTcpFilter";
    public static final String DEFAULT_TCP_FILTER_ARG_CONFIGNAME =
	"DefaultTcpFilterArg";
    public static final String DEFAULT_TCP_FILTER = null;
    public static final String DEFAULT_TCP_FILTER_ARG = null;
    public static final int TCP_FILTER_MAGIC = 0x8476;

    protected void init(int remoteAddress, short remotePort,
			boolean initiatorFlag) {
	if (initiatorFlag) {
	    // defaults
	    ConfigManager config = (ConfigManager)
		SingletonManager.get(equip.config.ConfigManagerImpl.
				     class.getName());
	    String streamFilterClassName = 
		config.getStringValue(DEFAULT_TCP_FILTER_CONFIGNAME,
				      DEFAULT_TCP_FILTER);
	    String streamFilterArg = 
		config.getStringValue(DEFAULT_TCP_FILTER_ARG_CONFIGNAME,
				      DEFAULT_TCP_FILTER_ARG);
	    init(remoteAddress, remotePort, initiatorFlag, 
		 streamFilterClassName, streamFilterArg);
	} else
	    init(remoteAddress, remotePort, initiatorFlag, null, null);
    }
    protected void init(int remoteAddress, short remotePort,
			boolean initiatorFlag,
			String streamFilterClassName, 
			String streamFilterArg) {
	this.remoteAddress = remoteAddress;
	this.localAddress = localAddress;
	
	setSendBufSize(sendBufSize);
	setRecvBufSize(recvBufSize);
	
	connectedFlag = true;
	status = StatusValues.STATUS_OK;
	slowResponseFlag = false;
	connectionTimeoutFlag = false;
	slowResponseTimeMs = SapConstants.DEFAULT_SLOW_RESPONSE_TIME_MS_TCP;
	
	// must do out first!
	try {
	    // NB buffering is REALLY important here
	    // - larger than default (512B) buffer
	    BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream(),10000);
	    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

	    // negotiate optional stream filter(s)
	    // initiator takes the initiative!
	    if (initiatorFlag) {
		if (streamFilterClassName==null) {
		    // old style - just slap the ObjectOutputStream on it
		    oout = new equip.runtime.ObjectOutputStream(bos);
		    oout.flush();
		    oin = new equip.runtime.ObjectInputStream(bis);
		} else {
		    IOStreamFilterFactory fact = null;
		    try {
			// check we can load it ourselves
			fact = (IOStreamFilterFactory)SingletonManager.
			    get(streamFilterClassName);
		    } catch (Exception e) {
			System.err.println("ERROR: could not load stream "+
					   "filter class: "+
					   streamFilterClassName);
			throw e;
		    }
		    // request this
		    writeShort(bos, TCP_FILTER_MAGIC);
		    // 'm'andatory - no negotiation
		    String req = "m"+streamFilterClassName+";"+
			streamFilterArg;
		    System.err.println("ConnectionSapTcp requesting: "+req);
		    writeRawString(bos, req);
		    bos.flush();
		    
		    wrapStreams(socket, bos, bis, fact, streamFilterArg);
		}
	    } else {
		// non-initiator - see what was requested...
		bis.mark(2);
		int magic = readShort(bis);
		if (magic != TCP_FILTER_MAGIC) {
		    // try the old way
		    bis.reset();
		    oout = new equip.runtime.ObjectOutputStream(bos);
		    oout.flush();
		    oin = new equip.runtime.ObjectInputStream(bis);
		} else {
		    // read the request
		    String req = readRawString(bis);
		    System.err.println("ConnectionSapTcp requested: "+req);

		    char cmd = req.charAt(0);
		    int pos = req.indexOf(';');
		    streamFilterClassName = req.substring(1,pos);
		    streamFilterArg = req.substring(pos+1);
		    if (DEBUG_TCP)
		    System.err.println("ConnectionSapTcp filter class="+
				       streamFilterClassName+", arg="+
				       streamFilterArg);
		    IOStreamFilterFactory fact = null;
		    try {
			// check we can load it ourselves
			fact = (IOStreamFilterFactory)SingletonManager.
			    get(streamFilterClassName);
		    } catch (Exception e) {
			System.err.println("ERROR: could not load stream "+
					   "filter class: "+
					   streamFilterClassName);
			// more polite error?!
			throw e;
		    }

		    wrapStreams(socket, bos, bis, fact, streamFilterArg);
		}
	    }
	} catch (Exception e) {
	    System.err.println("ConnectionSapTcp get streams failed: "+e);
	    close(false);
	    keepAliveThread.start();
	    return;
	}
	// release ConnectionSap keepAliveThread
	keepAliveThread.start();
    }	
    protected void writeRawString(OutputStream os, String string)
	throws IOException {
	byte [] b = string.getBytes();
	writeShort(os, b.length);
	os.write(b);
    }
    protected void writeShort(OutputStream os, int val)  throws IOException {
	os.write((val >> 8) & 0xff);
	os.write(val & 0xff);
    }
    protected int readShort(InputStream is) throws IOException {
	int val = ((is.read() << 8) & 0xff00) |
	    (is.read() & 0xff);
	return val;
    }
    protected String readRawString(InputStream is) throws IOException {
	int len = readShort(is);
	if(len>0) {
	    byte [] b = new byte[len];
	    if (is.read(b) < len) 
		throw new IOException("EOF in readRawString");
	    return new String(b);
	}
	return "";
    }
    protected void wrapStreams(Socket socket, OutputStream bos,
			       InputStream bis, IOStreamFilterFactory fact,
			       String streamFilterArg) throws IOException {
	if(DEBUG_TCP)
	    System.err.println("ConnectionSapTcp - wrapping output stream...");
	// do it ourselves
	oout = new equip.runtime.ObjectOutputStream
	    (fact.createOutputFilter(socket, bos, 
				     streamFilterArg));
	oout.flush();
	if(DEBUG_TCP)
	    System.err.println("ConnectionSapTcp - wrapping input stream...");
	oin = new equip.runtime.ObjectInputStream
	    (fact.createInputFilter(socket, bis, 
				    streamFilterArg));
	if(DEBUG_TCP)
	    System.err.println("ConnectionSapTcp - wrapped streams ok");
    }
    protected void finalize() {
	close(false);
    }
    public int getProtocol() {
	return SapProtocol.SAP_PR_TCP;
    }

    public int waitUntilConnected() {
	// creation waits until connected
	return status;
    }
    public void close(boolean flushFlag) {
	if (!flushFlag && socket!=null) {
	    // in case this close is meant to unblock reads/writes
	    try {
		socket.setSoLinger(false, 30);
		socket.close();
	    } catch(Exception e) {}
	}
	// make sure keepAliveThread is going to stop
		if (keepAliveThreadData!=null && keepAliveThreadData.lock!=null) 
		{
			synchronized(keepAliveThreadData.lock) 
			{
				keepAliveThreadData.self = null;
			}
		}
	// stop thread?? - could be dangerous - don't bother if this is crashing the JVM!
	if (keepAliveThread!=null)
	    try {
		if (keepAliveThread.isAlive())
		    ;//keepAliveThread.stop();
	    } catch (Exception e) {
		System.err.println("ERROR stopping ConnectionSapTcp keep-alive thread: "+e);
	    }
	synchronized(lock) {
	    if (socket!=null) {
		try {
		    socket.setSoLinger(flushFlag, 30);		
		    socket.close();
		} catch (Exception e) {}
		socket = null;
	    }
	    if (oout!=null) {
		oout = null;
	    }
	    if (oin!=null) {
		oin = null;
	    }
	}// lock
    }	
    public ValueBase readObject() throws java.io.IOException {
	ValueBase object;

	// not locked - writes and close can occur in parallel!
	if (socket==null || status!=StatusValues.STATUS_OK || oin==null) {
	    status = StatusValues.STATUS_ERROR;
	    throw new IOException("ConnectionSapTcp.readObject on closed or failed socket");
	}

	KeepAlive keepAlive = null;
	// connection failure timeout - we must be getting keep alives!
	socket.setSoTimeout(connectionTimeoutMs);
	do {
	    try {
		object = oin.readObject();
	    } catch (IOException e) {
		System.err.println
		    ("WARNING: ConnectionSapTcp::readObject failed in readObject: "+e);
		close();
		status = StatusValues.STATUS_ERROR;
		throw e;
	    } catch (Exception e) {
		System.err.println
		    ("WARNING: ConnectionSapTcp::readObject failed in readObject: "+e);
		close();
		status = StatusValues.STATUS_ERROR;
		throw new IOException("ConnectionSapTcp::readObject failed in readObject: "+e);
	    }
	
	    noteReceivedObject(object);
	    
	    if (object instanceof KeepAlive) {
		if (DEBUG_TCP)
		    System.err.println("Discarding received KeepAlive");
	    }
	} while ((object instanceof KeepAlive));
	
	return object;
    }

    public int cancelObject(int object_id) {
	// not supported by tcp
	return StatusValues.STATUS_ERROR;
    }

    // - send buffer size (max sender window)
    public void setSendBufSize(int size) {
	super.setSendBufSize(size);
	if (socket!=null)
	    try {
		java.lang.reflect.Method m = socket.getClass().getMethod
		    ("setSendBufferSize",
		     new Class [] { Integer.TYPE });
		m.invoke(socket,new java.lang.Object [] { new Integer(size) });
	    } catch (Exception e) {
		System.err.println("socket.setSendBufferSize failed: "+e);
	    }
    }
	
    // - receive buffer size (max receiver window)
    public void setRecvBufSize(int size) {
	super.setRecvBufSize(size);
	if (socket!=null)
	    try {
		java.lang.reflect.Method m = socket.getClass().getMethod
		    ("setReceiveBufferSize",
		     new Class [] { Integer.TYPE });
		m.invoke(socket,new java.lang.Object [] { new Integer(size) });
	    } catch (Exception e) {
		System.err.println("socket.setReceiveBufferSize failed: "+e);
	    }
    }
	
    // - timeout for blocking read (not an indication of failure -
    //   may just mean data has not been received)
    public void setRecvTimeout(int timeoutMs) {
	super.setRecvTimeout(timeoutMs);
	if (socket!=null)
	    try {
		socket.setSoTimeout(timeoutMs);
	    } catch (Exception e) {
		System.err.println("setRecvTimeout failed: "+e);
	    }
    }

    protected Socket socket;
    protected equip.runtime.ObjectOutputStream oout;
    protected equip.runtime.ObjectInputStream oin;
    protected int writeObjectLocked(ValueBase object,
				       boolean reliableFlag,
				       int [] object_id, int priority) 
	throws java.io.IOException {
	if (socket==null || status!=StatusValues.STATUS_OK || oout==null) {
	    status = StatusValues.STATUS_ERROR;
	    throw new IOException("ConnectionSapTcp.writeObjectLocked when failed or closed");
	    //return StatusValues.STATUS_ERROR;
	}
	
	if (object_id!=null && object_id.length>0)
	    object_id[0] = nextObjectId;
	nextObjectId++;
  
	// update time 
	Time now = new TimeImpl();
	now.getCurrentTime();
	int nowMs = now.sec*1000+(now.usec/1000);
	lastSendTimeMs = lastNewSendTimeMs = nowMs;
	
	try {
	    oout.writeObject(object);
	    oout.flush();
	} catch (IOException e) {
	    System.err.println
		("WARNING: ConnectionSapTcp::writeObject failed in writeObject: "+e);
	    close();
	    status = StatusValues.STATUS_ERROR;
	    throw e;
	} catch (Exception e) {
	    System.err.println
		("WARNING: ConnectionSapTcp::writeObject failed in writeObject: "+e);
	    close();
	    status = StatusValues.STATUS_ERROR;
	    throw new IOException("ConnectionSapTcp::writeObject failed in writeObject: "+e);
	}
	return status;
    }
protected static final boolean DEBUG_TCP = false;
}
