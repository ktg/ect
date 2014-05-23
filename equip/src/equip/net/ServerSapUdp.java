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
  Ian MacColl (University of Glasgow)

*/
/* ServerSapTcp.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

import equip.runtime.*;

import java.net.*;

/** Concrete Implementation of {@link ServerSap} using 
 * JCP (Jim's Communication Protocol) */ 
public class ServerSapUdp extends ServerSap 
{
    /** Open on any port */
    public ServerSapUdp() {
	init((short)0);
    }

    /** Open on specified port */
    public ServerSapUdp(short port) {
	init(port);
    }
    protected void finalize() {
	close();
    }
    protected void init(short port) 
	{

	try
	{
		socket = new DatagramSocket(((int)port) & 0xffff);
		socket.setSoTimeout(0);
	}
	catch(Exception e)
	{
	    System.err.println("ERROR: ServerSapUdp::init failed for port " +
	    	(((int)port) & 0xffff) + " (" + port + ")");
	    socket = null;
	    status = StatusValues.STATUS_ERROR;
	    return;
	} 
  
	this.port = (short)socket.getLocalPort();
	byte [] ip = ServerSap.rewriteLocalAddress(socket.getLocalAddress()).getAddress();
	this.address = 
	    ((((int)ip[0]) & 0xff) << 24) |
	    ((((int)ip[1]) & 0xff) << 16) |
	    ((((int)ip[2]) & 0xff) << 8) |
	    ((((int)ip[3]) & 0xff));
	status = StatusValues.STATUS_OK;
    }

    public int getProtocol() 
	{
		return SapProtocol.SAP_PR_UDP;
    }

    // partial hack - won't deal with overlapping connects
    private InetAddress lastSourceAddress;
    private int lastSourcePort;
    private ConnectionSapJcp lastConnection = null;

    public ConnectionSap accept() 
	{

		System.out.println("ServerSapUdp::accept: waiting for connnection...");

		// Read packet to this port.
		byte[] buf = new byte[JcpPacketHeader.getSize()];
		DatagramPacket packet = new DatagramPacket(buf, JcpPacketHeader.getSize());

		try
		{
		    while (true) {
			socket.receive(packet);
			if (lastConnection!=null &&
			    packet.getAddress().equals(lastSourceAddress) &&
			    packet.getPort()==lastSourcePort) {
			    System.err.println("Note: ServerSapUdp suspects duplicate SYN - forwading to connection");
			    lastConnection.receivePacket(packet);
			}  else
			    break;
		    }
		    lastSourceAddress = packet.getAddress();
		    lastSourcePort = packet.getPort();
		    lastConnection=new ConnectionSapJcp(packet);
		    return lastConnection;
		}
		catch(Exception e)
		{
			System.err.println("WARNING: ServerSapUdp::accept called on failed or closed socket");
			return null;
		}
	}

    public void close() 
	{
		if (socket!=null) 
		{
			try {
			socket.close();
			} catch (Exception e) {}
			socket = null;
		}
    }
    protected DatagramSocket socket = null;
};
