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
/* ServerSap.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

import equip.runtime.*;
import java.net.InetAddress;
import equip.config.ConfigManagerImpl;

/** The abstract class/interface for a Server SAP (Service Access
 * Point), e.g. a TCP ServerSocket. */
public abstract class ServerSap {
    /** Default constructor, only accessible to subclass */
    protected ServerSap() {}

    /** Get server socket UDP/TCP port number */
    public short getPort() {
	return port;
    }

    /** Get server IP address, currently IPv4 as 32bit int */
    public int getAddress() {
	return address;
    }
    
    /** What protocol (from {@link SapProtocol}) */
    public abstract int getProtocol();

    /** Current status; cached in field {@link #status} */
    public int getStatus() {
	return status;
    }
  
    /** Accept an incoming connection, normally blocking */
    public abstract ConnectionSap accept();

    /** Close the server socket (no more connections) */
    public abstract void close();

    /** Socket IPv4 host address */
    protected int address = 0;

    /** Socket UDP/TCP port */
    protected short port = 0;
    
    /** Socket status cache */
    protected int status = StatusValues.STATUS_ERROR;
	/** chance to replace local socket ip address with something 
	 * else, e.g. to deal with multi-home issues
	 */
	static public InetAddress rewriteLocalAddress(InetAddress addr) 
	{
		try 
		{
			ConfigManagerImpl config = (ConfigManagerImpl)SingletonManager.get(equip.config.ConfigManagerImpl.class.getName());
			String localhost = null;
			localhost = config.getStringValue("localhost", localhost);
			localhost = config.getStringValue("localhost."+addr.getHostAddress(), localhost);
			localhost = config.getStringValue("localhost."+InetAddress.getLocalHost().getHostName(), localhost);
			if (localhost!=null)
				return InetAddress.getByName(localhost);
		} 
		catch (Exception e) 
		{
			System.err.println("ERROR rewriting local address: "+e);
		}
		return addr;
	}
};

