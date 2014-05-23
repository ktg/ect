/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
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
package equip.discovery;

import java.net.*;
import equip.net.ServerSap;

/** some helpers for multicast stuff
 */
public class MulticastUtils 
{
	/** open on any port
	 */
	public static MulticastSocket newMulticastSocket() throws java.io.IOException
	{
		// try to set network interface for sending according to 
		// equip.net.ServerSap.rewriteLocalAddress
		MulticastSocket socket = new MulticastSocket();
		InetAddress addr = null;
		NetworkInterface nif = null;
		try 
		{
			socket.setNetworkInterface(nif=NetworkInterface.getByInetAddress(addr=ServerSap.rewriteLocalAddress(InetAddress.getLocalHost())));
		} 
		catch (Exception e) 
		{
			System.err.println("Warning: error setting network interface for multicasting to "+(nif==null ? "null" : nif.toString())+
				" ("+(addr==null ? "null": addr.toString())+")");
		}
		return socket;
	}
	public static MulticastSocket newMulticastSocket(int port) throws java.io.IOException 
	{
		//?? SocketAddress saddr = new InetSocketAddress
		MulticastSocket socket = new MulticastSocket(port);
		InetAddress addr = null;
		try 
		{
			socket.setInterface(addr=ServerSap.rewriteLocalAddress(InetAddress.getLocalHost()));
		} catch (Exception e) {
			System.err.println("Warning: error setting network interface for receiving to "+(addr==null ? "null" : addr.toString()));
		}
		return socket;
	}
}