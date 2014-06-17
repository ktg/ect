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
  Stefan Rennick Egglestone (University of Nottingham)
*/
/* ConnectionSap.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

import equip.runtime.*;

import java.net.*;

/** Abstract base class for a connection service access point (i.e. a
 * network connection).
 * 
 * A connection sap is specifically connected to another such in another
 * process. <P>
 *
 * <b>NB</b> returns without having called start() on keepAliveThread -
 * must be released to allow it to run).
 */
public abstract class ConnectionSap implements Runnable {
    // (NB returns without having called start() on keepAliveThread -
    //  - must be released to allow it to run)
    protected ConnectionSap() {
	localAddress = 0;
	remoteAddress = 0;
	localPort = 0;
	remotePort = 0;
	status = StatusValues.STATUS_OK;
	connectedFlag = false;
	sendBufSize = SapConstants.DEFAULT_BUF_SIZE;
	recvBufSize = SapConstants.DEFAULT_BUF_SIZE;
	recvTimeoutMs = SapConstants.DEFAULT_RECV_TIMEOUT_MS;
	keepAliveTimeMs = SapConstants.DEFAULT_KEEP_ALIVE_TIME_MS;
	slowResponseTimeMs = SapConstants.DEFAULT_SLOW_RESPONSE_TIME_MS;
	slowResponseFlag = false;
	lastNewSendTimeMs = lastResponseTimeMs = 0;
	connectionTimeoutMs = SapConstants.DEFAULT_CONNECTION_TIMEOUT_MS;
	probingIntervalMs = SapConstants.DEFAULT_PROBING_INTERVAL_MS;
	
	nextObjectId = 1;
	lock = new java.lang.Object();
	//PR_Lock(lock);
	
	Time now = new TimeImpl();
	now.getCurrentTime();
	
	// will overflow, but we just want deltas
	lastSendTimeMs = now.sec*1000+(now.usec/1000);
	lastNewSendTimeMs = lastResponseTimeMs = 0;
	
	keepAliveThreadData = new KeepAliveThreadData();
	keepAliveThreadData.self = this;
	keepAliveThreadData.lock = lock;
	keepAliveThread = new Thread(this); 
    }
	    
    public short getLocalPort() { return localPort; }
    public int getLocalAddress() { return localAddress; }
    public short getRemotePort() { return remotePort; }
    public int getRemoteAddress() { return remoteAddress; }
    public abstract int getProtocol();
    public int getStatus() { return status; }
    public boolean getConnectedFlag() { return connectedFlag; }
    public boolean getSlowResponseFlag() { return slowResponseFlag; }
    public boolean getConnectionTimeoutFlag() { return connectionTimeoutFlag; }
    public float getConnectionStrength() { return 1.0f; }
    
    // 'normal' api for two-ways communication
    public abstract int waitUntilConnected();
	
    public void close() {
	close(true);
    }
    public abstract void close(boolean flushFlag);

    public int writeObject(ValueBase object) 
	throws java.io.IOException {
	int [] object_id = new int [1];
	return writeObject(object, true, object_id, 0);
    }
	   
    public int writeObject(ValueBase object,
			      boolean reliableFlag,
			      int [] object_id, int priority)
	throws java.io.IOException {
	synchronized(lock) {
	    return writeObjectLocked(object, reliableFlag, object_id, priority);
	}
    }
    public abstract ValueBase readObject()
	throws java.io.IOException;

    // if previously written object becomes out of date it
    // may be possible to cancel it in flight (e.g. UDP) using
    // the id returned from writeObject.
    public abstract int cancelObject(int object_id);

    // socket communication properties
    
    // - send buffer size (max sender window)
    public void setSendBufSize(int size) {
	sendBufSize = size;
    }
    public int getSendBufSize() { return sendBufSize; }
    // - receive buffer size (max receiver window)
    public void setRecvBufSize(int size) {
	recvBufSize = size;
    }
    public int getRecvBufSize() { return recvBufSize; }
    // - timeout for blocking read (not an indication of failure -
    //   may just mean data has not been received)
    public void setRecvTimeout(int timeoutMs) {
	recvTimeoutMs = timeoutMs;
    }
    public int getRecvTimeout() { return recvTimeoutMs; }
    // - keep alive send interval
    public void setKeepAliveTime(int timeMs) {
	keepAliveTimeMs = timeMs;
    }
    public int getKeepAliveTime() { return keepAliveTimeMs; }
    // - worry about connection timeout interval (after 
    //   expected ack)
    public void setSlowResponseTime(int timeMs) {
	slowResponseTimeMs = timeMs;
    }
    public int getSlowResponseTime() { return slowResponseTimeMs; }
    
    // - connection timeout interval (no keep alives...),
    //   reported to readObject (and getStatus) as ERROR.
    public void setConnectionTimeout(int timeMs) {
	connectionTimeoutMs = timeMs;
    }
    public int getConnectionTimeout() { return connectionTimeoutMs; }
    // force carry on, STATUS_OK = ok
    public int resetConnectionTimeoutFlag() {
	// not possible by default
	return StatusValues.STATUS_ERROR;
    }

    // - probing interval (when fully backed off sending)
    public void setProbingInterval(int timeMs) {
	probingIntervalMs = timeMs;
    }
    public int getProbingInterval() { return probingIntervalMs; }

    protected int localAddress, remoteAddress;
    protected short localPort, remotePort;
    protected int status;
    protected boolean connectedFlag;
    protected int sendBufSize, recvBufSize;
    protected int recvTimeoutMs;
    protected int keepAliveTimeMs;
    protected int slowResponseTimeMs;
    protected boolean slowResponseFlag;
    protected int lastNewSendTimeMs;
    protected int lastResponseTimeMs;
    protected int connectionTimeoutMs;
    protected boolean connectionTimeoutFlag;
    protected int probingIntervalMs;

    // internal implementatin -lock held
    protected abstract int writeObjectLocked(ValueBase object,
						boolean reliableFlag,
						int [] object_id, int priority)
	throws java.io.IOException;

    protected  int nextObjectId;
    // keep alive timer and keepalive/sending thread
    protected java.lang.Object lock;
    protected Thread keepAliveThread;
    public void run() { //keepAliveThreadFnS(void *arg);
	KeepAliveThreadData data = keepAliveThreadData;
	boolean runFlag;
	synchronized(data.lock) {
	    runFlag = (data.self!=null);
	}
	if (runFlag)
	    data.self.keepAliveThreadFn(data);
	else {
	    System.err.println
		("WARNING: ConnectionSap::keepAliveThreadFnS never had chance to run");
	}
	if (DEBUG_TCP) 
	    System.err.println
		("Debug: ConnectionSap::keepAliveThreadFnS terminated");
	data.lock = null;
    }
    /** Internal class holding connection keep-alive data */
    protected static class KeepAliveThreadData {
	ConnectionSap self;
	java.lang.Object lock;
    }

	protected KeepAliveThreadData keepAliveThreadData;
    protected void keepAliveThreadFn(KeepAliveThreadData data) {
	KeepAlive keepAlive = new KeepAliveImpl();

	while (true) {
	    System.gc();
	    // check we are alive; get last send time
	    if (DEBUG_TCP)
		System.err.println
		    ("Debug: ConnectionSap::keepAliveThreadFn get lock...");
	    int waitMs = 0;
	    synchronized(data.lock) {
		if (DEBUG_TCP)
		    System.err.println
			("Debug: ConnectionSap::keepAliveThreadFn ...get lock OK");
		if (data.self==null) {
		    // dead 
		    return;
		}
		int lastSendTimeMs = data.self.lastSendTimeMs;
		int keepAliveTimeMs = data.self.keepAliveTimeMs;

		// time to next keep alive?
		Time now = new TimeImpl();
		now.getCurrentTime();
		int nowMs = now.sec*1000+(now.usec/1000);
		
		// stef: for some reason, the class that this
		// reference points to wasn't getting garbage
		// collected (at least under jdk1.4.2 and 1.5)
		// Setting it to null, and explicitly garbage
		// collecting at the end of the loop,
		// seems to solve this problem
		now = null;
	     
		waitMs = (lastSendTimeMs+keepAliveTimeMs)-nowMs;
		if (DEBUG_TCP)
		    System.err.println
			("Debug: ConnectionSap::keepAliveThreadFn waitMs = "
			 + waitMs + " (now = " + nowMs 
			 + ", lastSendTimeMs = " + lastSendTimeMs 
			 + ", keepAliveTimeMs = " + keepAliveTimeMs
			 + ")");
		    if (data.self.getStatus()!=StatusValues.STATUS_OK) {
		    if (DEBUG_TCP)
			System.err.println
			    ("Debug: ConnectionSap::keepAliveThreadFn force wait - failed conn");
		    waitMs = 5000;
		}
		if (waitMs <= 0) {
		    // send
		    int [] id= new int [1];
		    if (DEBUG_TCP)
			System.err.println
			    ("Debug: ConnectionSap::keepAliveThreadFn send keepAlive");
		    try {
			data.self.writeObjectLocked(keepAlive, false, id, 0);
			id = null;
		    } catch (Exception e) {
			System.err.println("Keep alive write object failed: "+e);
		    }
		}
	    } // synchronized
	    if (waitMs > 0) {
		// wait
		// unlocked
		if (DEBUG_TCP)
		    System.err.println
			("Debug: ConnectionSap::keepAliveThreadFn sleep " + waitMs);
		try {
		    Thread.sleep(waitMs);
		} catch (Exception e) {}
		if (DEBUG_TCP)
		    System.err.println
			("Debug: ConnectionSap::keepAliveThreadFn wake up...");
	    }
	}	
    }
    protected int lastSendTimeMs;
    
    // check/update receive timers
    protected void noteReceivedObject(ValueBase object) {
	Time now = new TimeImpl();
	now.getCurrentTime();
	int nowMs = now.sec*1000+(now.usec/1000);
  
	lastResponseTimeMs = nowMs;
    }
    protected boolean DEBUG_TCP = false;
}
