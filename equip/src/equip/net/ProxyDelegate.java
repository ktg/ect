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
/*

  File:    ProxyDelete.java
  $Id: ProxyDelegate.java,v 1.1.1.1 2005/03/08 16:17:23 cgreenhalgh Exp $
  Created:  15/03/2001
  Author:  Chris Greenhalgh

  Purpose:
  proxy helper class 

  To do:

  Change History:
  $Log: ProxyDelegate.java,v $
  Revision 1.1.1.1  2005/03/08 16:17:23  cgreenhalgh
  From Nottingham CVS

  Revision 1.7  2005/03/08 14:37:30  cmg
  added BSD license

  Revision 1.6  2003/10/14 09:41:21  cmg
  updated eqidl-generated files with more javadoc comments

  Revision 1.10  2003/06/20 12:40:28  cmg
  more comments

  Revision 1.9  2002/07/15 14:39:14  cmg
  initial revision

  Revision 1.8  2002/02/21 14:27:57  cmg
  no linger (causes real problems on personal java and ipaq)

  Revision 1.7  2001/09/20 14:33:12  cmg
  added UDP stuff; added Sap stuff (tcp); added equipm URL form & simple rpc support

  Revision 1.6  2001/06/08 09:49:27  cmg
  use java buffered io streams (performance)

  Revision 1.5  2001/05/17 08:25:50  cmg
  reduce buffering again

  Revision 1.4  2001/05/01 08:05:34  cmg
  irix fixes, esp. eqidl update

  Revision 1.3  2001/04/23 08:51:16  cmg
  reduce buffering

  Revision 1.2  2001/04/09 10:06:54  cmg
  added deactivate callback on failed proxy

  Revision 1.1.1.1  2001/04/01 08:40:57  Administrator
  initial - going to chi

  Revision 1.1.1.1  2001/03/27 12:22:35  cmg
  Initial import to CVS - in progress

*/

package equip.net;

import equip.runtime.*;
import java.net.*;
import java.io.*;

/** Generic implementation of {@link ServiceProxy}, for use by
 * delegation from IDL'd subclasses of {@link ServiceProxy}.
 *
 * The main entry point is {@link #init}, which passes received
 * messages to a callback interface implementing 
 * {@link ProxyDelegate.Handler}. 
 */ 
public class ProxyDelegate {
    /** callback/strategy function for {@link ProxyDelegate} */
    public static interface Handler { 
	public void handleProxyMessage(ProxyDelegate delegate,
				       ValueBase object,
				       java.lang.Object closure);
    }

    /** empty constructor - see {@link #init} */
    public ProxyDelegate() {
    }

    private DeactivateCallback deactivateCallback = null;
    private equip.runtime.ValueBase deactivateClosure = null;
    private ConnectionSap connection = null;
    // multicast UDP stuff
    private MulticastSocket mc_socket = null;
    private int session_id;
    private int message_id;
    private int mc_group;
    private short mc_port;
    // end multicast UDP stuff
    
    private ServiceProxy proxy = null;
    private Handler handler = null;
    private java.lang.Object closure = null;

    /** true = ok; handler!=null => create new thread for replies */
    public boolean init(ServiceProxy proxy, Handler handler,
			java.lang.Object closure) {
	deactivate();
	this.proxy = proxy;
	this.handler = handler;
	this.closure = closure;
	return true;
    }

    public ConnectionSap getConnection() {
	return connection;
    }
    
    /* ServiceProxy methods */
    public boolean activate(DeactivateCallback callback, 
			    ValueBase closure) {
	deactivate();
	if (proxy==null) {
	    System.err.println("ERROR: ProxyDelegate::activate called "+
			       "with null proxy");
	    return false;
	}
	Moniker moniker = proxy.serviceMoniker;
	if (moniker==null) {
	    System.err.println("ERROR: ProxyDelegate::activate called "+
			       "with null moniker");
	    return false;
	}
	SimpleMoniker resolved = moniker.resolve();
	if (resolved==null) {
	    System.err.println("ERROR: ProxyDelegate::activate could "+
			       "not resolve moniker (class "+
			       moniker.getClass().getName()+")");
	    return false;
	}
	
	if ((resolved instanceof SimpleTCPMoniker)) {
	    SimpleTCPMoniker tcp = (SimpleTCPMoniker)resolved;
	    if (tcp.encoding!=Encoding.EQENCODE_EQ_OBJECT_STREAM) {
		System.err.println("ERROR: ProxyDelegate::activate resolved "+
				   "to unsupported tcp encoding: "+
				   tcp.encoding);
		return false;
	    }

	    connection = new ConnectionSapTcp(tcp.addr, tcp.port);
	    if (connection.getStatus()!=StatusValues.STATUS_OK) {
		System.err.println("ERROR: ProxyDelegate::activate failed "+
				   "to connect to server");
		connection = null;
		return false;
	    }
	    
	    System.err.println("ProxyDelegate::activate() ok");
	}
	else if ((resolved instanceof SimpleUDPMoniker))
	{
		SimpleUDPMoniker udp = (SimpleUDPMoniker) resolved;
		if (udp.encoding!=Encoding.EQENCODE_EQ_OBJECT_PACKET) {
		System.err.println("ERROR: ProxyDelegate::activate resolved "+
				   "to unsupported tcp encoding: "+
				   udp.encoding);
		return false;
	    }

		connection = new ConnectionSapJcp(udp.addr, udp.port);
	    if (connection.getStatus()!=StatusValues.STATUS_OK) {
		System.err.println("ERROR: ProxyDelegate::activate failed "+
				   "to connect to server");
		connection = null;
		return false;
		}

		System.err.println("ProxyDelegate::activate() ok");
	}
	else if ((resolved instanceof MulticastUDPMoniker)) 
	{
	    MulticastUDPMoniker mc = (MulticastUDPMoniker)resolved;
	    if (mc.encoding!=Encoding.EQENCODE_EQ_OBJECT_PACKET) {
		System.err.println("ERROR: ProxyDelegate::activate resolved "+
				   "to unsupported multicast udp encoding: "+
				   mc.encoding);

		return false;
	    }
	    mc_group = mc.addr;
	    mc_port = mc.port;
	    try {
		mc_socket = new MulticastSocket();
	    } catch (Exception e) {
		System.err.println("ERROR: ProxyDelegate::activate failed to "+
				   "create multicast socket: "+e);
		mc_socket = null;
		return false;
	    }
    
	    message_id = 1;
	    session_id = 1; // first & only use of port
	    System.err.println("ProxyDelegate::activate() [multicast] ok");
	} else {
	    System.err.println("ERROR: ProxyDelegate::activate resolved "+
			       "to unsupported moniker class "+
			       moniker.getClass().getName());
	    return false;
	}

	deactivateCallback = callback;
	deactivateClosure = closure;
	
	// client thread... 
	if (handler!=null) 
	    System.err.println("ERROR: ProxyDelegate::activate does not "+
			       "yet support handler thread");
	
	return true;
    }
    public void deactivate() {
	if (connection!=null) {
	    connection.close(false);
	    connection = null;
	}
	if (mc_socket!=null) {
	    try {
		mc_socket.close();
	    } catch (Exception e) {}
	    mc_socket = null;
	}
	deactivateCallback = null;
	deactivateClosure = null;
    }
    /* to signal an async. deactivation (network failure) */
    public void notifyDeactivate() {
	if (deactivateCallback!=null)
	    deactivateCallback.notifyDeactivate(proxy, 
						deactivateClosure);
	deactivate();
    }

    /* send an object, receive a reply object */
    public ValueBase doSimpleRpc(ValueBase request) {
	if (mc_socket!=null)
	    return doSimpleRpcMulticast(request);

	if (connection==null || connection.getStatus()!=StatusValues.STATUS_OK) { 
	    System.err.println("ERROR: ProxyDelegate::doSimpleRpc called "+
			       "with bad socket or streams");
	    notifyDeactivate();
	    return null;
	}
	try {
	    connection.writeObject(request);
	} catch(Exception e) {
	    System.err.println("ERROR: ProxyDelegate::doSimpleRpc failed "+
			       "on object write: "+e);
	    notifyDeactivate();
	    return null;
	}
	ValueBase result = null;
	try {
	    result = connection.readObject();
	} catch (Exception e) {
	    System.err.println("ERROR: ProxyDelegate::doSimpleRpc failed "+
			       "on object read: "+e);
	    e.printStackTrace(System.err);
	    notifyDeactivate();
	    return null;
	}
	if (result==null) {
	    System.err.println("Warning: ProxyDelegate::doSimpleRpc "+
			       "returned null");
	    notifyDeactivate();
	    return null;
	}
	return result;
    }
    /* send an object, receive a reply object */
    private ValueBase doSimpleRpcMulticast(ValueBase request) {
	if (mc_socket==null) {
	    System.err.println("ERROR: ProxyDelegate::doSimpleRpcMulticast called with bad socket");
	    notifyDeactivate();
	    return null;
	}
  
	// serialise request object into Mbuf(s)
	// - this is intended just for trader lookup so we'll just go with a 
	// single large Mbuf for now! (HACK HACK HACK...)
	// - for now we'll work with a header of:
	// UDPPacketHeader
	MbufOutputStream mout = 
	    new MbufOutputStream(65535, UDPPacketHeader.getSize());
	try {
	    equip.runtime.ObjectOutputStream oout = 
		new equip.runtime.ObjectOutputStream(mout);
	    oout.writeObject(request);
	    oout.flush();
	} catch (Exception e) {
	    System.err.println("ERROR: ProxyDelegate::doSimpleRpcMulticast failed on object write");
	    notifyDeactivate();
	    return null;
	}

	Mbuf request_mbuf = mout.takeMbuf();
	if (request_mbuf==null) {
	    System.err.println("INTERNAL ERROR: ProxyDelegate::doSimpleRpcMulticast "
			       + "writeObject failed to return an Mbuf");
	    return null;
	}

	// send request
	int request_i = 0;
	int timeout_ms = 250;
	// 5 tries
	int MAX_REQUEST_I = 5;
	while (request_i < MAX_REQUEST_I) {

	    System.err.println("- send multicast request...");

	    Mbuf mbuf = request_mbuf;
	    Mbuf next = null;
	    int src_frag = 0;
    
	    while (mbuf!=null) {
		try {
		    // fill in header
		    byte [] buf = mbuf.getBuf();
		    UDPPacketHeader header = new UDPPacketHeader(buf);
		    header.setMagic(0, UDPPacketHeader.magic_0);
		    header.setMagic(1, UDPPacketHeader.magic_1);
		    header.setMagic(2, UDPPacketHeader.magic_2);
		    header.setVersion(UDPPacketHeader.version);
		    header.setSrcAddr(mc_socket.getLocalAddress().getAddress());
		    header.setSrcPort((short)mc_socket.getLocalPort());
		    header.setRes0((short)0);
		    header.setSrcSid(session_id);
		    header.setSrcMid(message_id);
		    header.setSrcFrag(src_frag++);
		    
		    next = mbuf.getNext();
		    if (next==null)
			// msb set to mark end of message
			header.setSrcFrag(header.getSrcFrag() | 0x80000000);
		    
		    // new packet - fixes som eold bugs
		    DatagramPacket packet = new DatagramPacket
			(buf, mbuf.getUsed(),
			 InetAddress.getByName(""+((mc_group >> 24) & 0xff)+
					       "."+((mc_group >> 16) & 0xff)+
					       "."+((mc_group >> 8) & 0xff)+
					       "."+((mc_group) & 0xff)),
			 ((int)mc_port) & 0xffff);
		    
		    mc_socket.send(packet,(byte)1);
		} catch (Exception e) {
		    System.err.println("ERROR: sending packet in "+
				       "ProxyDelegate::doSimpleRpcMulticast:"+
				       e);
		}
		
		mbuf = next;
	    }
	    
	    try {
		// wait - some period of time - for a response
		mc_socket.setSoTimeout(timeout_ms);
		
		// if we repeat...
		timeout_ms *= 2;
		request_i ++;
		
		Mbuf rmbuf = new Mbuf(65535, UDPPacketHeader.getSize());
		DatagramPacket response_packet =
		    new DatagramPacket(rmbuf.getBuf(), 65535);
	    
		mc_socket.receive(response_packet);
		rmbuf.setUsed(response_packet.getLength());

		// response
		System.err.println("- received response packet...");
		
		byte [] rbuf = rmbuf.getBuf();
		UDPPacketHeader rheader = new UDPPacketHeader(rbuf);
		if (rheader.getMagic(0) != UDPPacketHeader.magic_0 ||
		    rheader.getMagic(1) != UDPPacketHeader.magic_1 ||
		    rheader.getMagic(2) != UDPPacketHeader.magic_2) {
		    System.err.println
			("ERROR: received packet magic number did not match ("
			 + rheader.getMagic(0) + "," 
			 + rheader.getMagic(1) + "," 
			 + rheader.getMagic(2) + " vs " 
			 + UDPPacketHeader.magic_0 + ","
			 + UDPPacketHeader.magic_1 + ","
			 + UDPPacketHeader.magic_2 + ")");
		    break;
		}
		if (rheader.getVersion() != UDPPacketHeader.version) {
		    System.err.println
			("ERROR: received packet version number did not match ("
			 + rheader.getVersion() + " vs " 
			 + UDPPacketHeader.version + ")");
		    break;
		}
		if (rheader.getSrcFrag() != 0x80000000) {
		    System.err.println
			("Sorry: received packet that was fragmented - not yet supported");
		    break;
		}
		
		MbufInputStream mbin = new MbufInputStream();
		mbin.reset(rmbuf);
		equip.runtime.ObjectInputStream oin =
		    new equip.runtime.ObjectInputStream(mbin);
		
		ValueBase result;
		result = oin.readObject();
		
		if (result==null) {
		    System.err.println
			("Warning: ProxyDelegate::doSimpleRpcMulticast returned null");
		    return null;
		}
		System.err.println("- OK");
		return result;
	    } catch (InterruptedIOException e) {
		System.err.println("- timeout (may retry)");
	    } catch (Exception e) {
		System.err.println("- failed: "+e);
		e.printStackTrace(System.err);
		break;
	    }
	}
	System.err.println
	    ("ERROR: ProxyDelegate::doSimpleRpcMulticast failed");
	notifyDeactivate();
	return null;
    }
}
