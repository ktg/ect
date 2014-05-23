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
/* ServerSapTcp.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

import equip.runtime.*;

import java.net.*;

/** Concrete implemented of {@link ServerSap} using {@link
 * java.net.ServerSocket}. */
public class ServerSapTcp extends ServerSap {
    
    /** Open on any port */
    public ServerSapTcp() {
	init((short)0);
    }

    /** Open on specific port */
    public ServerSapTcp(short port) {
	init(port);
    }
    protected void finalize() {
	close();
    }
    protected void init(short port) {
	try {
	    socket = new ServerSocket(((int)port) & 0xffff);
	} catch (Exception e) {
	    System.err.println
		("ERROR: ServerSapTcp::init failed for port " +port);
	    socket = null;
	    status = StatusValues.STATUS_ERROR;
	    return;
	} 
	this.port = (short)socket.getLocalPort();
	byte [] ip = ServerSap.rewriteLocalAddress(socket.getInetAddress()).getAddress();
	this.address = 
	    ((((int)ip[0]) & 0xff) << 24) |
	    ((((int)ip[1]) & 0xff) << 16) |
	    ((((int)ip[2]) & 0xff) << 8) |
	    ((((int)ip[3]) & 0xff));
	status = StatusValues.STATUS_OK;
    }

    public int getProtocol() {
	return SapProtocol.SAP_PR_TCP;
    }

    public ConnectionSap accept() {
	if (socket==null) {
	    System.err.println
		("WARNING: ServerSapTcp::accept called on failed or closed socket");
	    return null;
	}
	Socket client = null;
	try {
	    client = socket.accept();
	} catch (Exception e) {
	    System.err.println
		("WARNIG: ServerSapTcp::accept received NULL from ThreadedSocket::accept");
	    return null;
	}
	byte [] ip = client.getInetAddress().getAddress();
	int remoteAddress = ((((int)ip[0]) & 0xff) << 24) |
	    ((((int)ip[1]) & 0xff) << 16) |
	    ((((int)ip[2]) & 0xff) << 8) |
	    ((((int)ip[3]) & 0xff));
	short remotePort = (short)client.getPort();
	ConnectionSapTcp conn = new ConnectionSapTcp(remoteAddress,
						     remotePort,
						     client);
	return conn;
    }
    public void close() {
	if (socket!=null) {
	    try {
		socket.close();
	    } catch (Exception e) {}
	    socket = null;
	}
    }
    protected ServerSocket socket = null;
};
