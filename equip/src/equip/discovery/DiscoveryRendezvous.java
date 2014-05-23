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
import java.io.*;
import java.util.Vector;
import java.util.Iterator;

/** unicast reflector (with some filtering) for non-multicast
 * discovery.
 * @author Chris Greenhalgh
 */
public class DiscoveryRendezvous 
{
    /** main - [port]
     */
    public static void main(String [] args) 
    {
	int port = DISCOVERY_PORT.value;
	if (args.length>0) 
	{
	    try 
	    {
		port = new Integer(args[0]).intValue();
	    } 
	    catch (NumberFormatException e) 
	    {
		System.err.println("Usage: DiscoveryRendezvous [port]");
		System.exit(-1);
	    }
	}
	new DiscoveryRendezvous(port);
    }
    /** cons - port
     */
    public DiscoveryRendezvous(int port) 
    {
	System.out.println("DiscoveryRendezvous running on port "+port);
	try 
	{
	    socket = new DatagramSocket(port);
	    new Thread(new Runnable() 
	    {
		public void run() 
		{
		    listenFn();
		}
	    }).start();

	    new Thread(new Runnable() 
	    {
		public void run() 
		{
		    expireFn();
		}
	    }).start();
	}
	catch (Exception e) 
	{
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
    }
    /** socket
     */
    protected DatagramSocket socket;
    /** client info
     */
    protected class PeerInfo 
    {
	/** address
	 */
	InetAddress address;
	/** port
	 */
	int port;
	/** request
	 */
	DiscoveryRequestImpl request;
	/** annoucement
	 */
	ServerAnnouncementImpl announcement;
	/** last receive time
	 */
	long lastReceiveTime;
	/** cons
	 */
	PeerInfo(InetAddress addr, int port, DiscoveryRequestImpl request, ServerAnnouncementImpl announcement) 
	{
	    this.address = addr;
	    this.port = port;
	    this.request = request;
	    this.announcement = announcement;
	    this.lastReceiveTime = System.currentTimeMillis();
	}
    }
    /** peers
     */
    protected Vector peers = new Vector();
  
    /** listen and respond
     */
    protected void listenFn() 
    {
	try 
	{
	    System.out.println("Listening on equipd://"+InetAddress.getLocalHost().getHostAddress()+":"+socket.getLocalPort());
	    while(true) 
	    {
		byte announceData [] = new byte[1024];
		DatagramPacket announcepkt = 
		    new DatagramPacket(announceData,
		    announceData.length);
		try 
		{
		    // get a packet 
		    socket.receive(announcepkt);
		} 
		catch (Exception e) 
		{
		    System.err.println("ERROR: in DiscoveryRendezvoud receive: "+
			e+" (give up!)");
		    return;
		}
		try 
		{
		    ByteArrayInputStream bins = 
			new ByteArrayInputStream(announcepkt.getData(), 0, 
			announcepkt.getLength());
		    equip.runtime.ObjectInputStream oins = 
			new equip.runtime.ObjectInputStream(bins);
		    long version = oins.readInt();
		    if (version!=DISCOVERY_VERSION.value) 
		    {
			System.err.println
			    ("Warning: DiscoveryRendezvous failed to "+
			    "get a valid discovery object "+
			    "from "+announcepkt.getAddress()+"/"+
			    announcepkt.getPort()+" ("+
			    announcepkt.getLength()+" bytes), "+
			    "version "+version+" (expecting "+
			    DISCOVERY_VERSION.value+")");
			continue;
		    }
		    Object msg = oins.readObject();
		    System.out.println("Received a "+msg.getClass().getName());
		    ServerAnnouncementImpl announcement = null;
		    DiscoveryRequestImpl request = null;
		    if (msg instanceof ServerAnnouncementImpl)
			announcement = (ServerAnnouncementImpl)msg;
		    else if (msg instanceof DiscoveryRequestImpl) 
			request = (DiscoveryRequestImpl)msg;
		    else
			// don't know what to do with this!
			continue;
		    synchronized(peers) 
		    {
			boolean found = false;
			Iterator ipeer = peers.iterator();
			PeerInfo peer = null;
			while(ipeer.hasNext() && !found) 
			{
			    peer =(PeerInfo)ipeer.next();
			    if (peer.address.equals(announcepkt.getAddress()) &&
				peer.port==announcepkt.getPort()) 
				found = true;
			}
			if (!found) 
			{
			    // new
			    peer = new PeerInfo(announcepkt.getAddress(), 
				announcepkt.getPort(), request, announcement);
			    peers.addElement(peer);
			    System.out.println("New "+(request!=null ? "client": "server"));
			    // could do immediate response
			    // ....
			} 
			else 
			{
			    peer.lastReceiveTime = System.currentTimeMillis();
			    System.out.println("known");
			    if (announcement!=null)
    			       peer.announcement = announcement;
			    if (request!=null)
				peer.request = request;
			}
			if (announcement!=null) 
			{
			    // send announcement to all matching requests
			    ipeer = peers.iterator();
			    while(ipeer.hasNext()) 
			    {
				peer =(PeerInfo)ipeer.next();
				if (peer.request==null)
				    continue;
				boolean matches = false;
				int si;
				for (si=0; !matches && announcement.infos!=null && si<announcement.infos.length; si++) 
				{
				    // match?
				    if (DiscoveryServerAgentImpl.
					matchServiceTypes(peer.request.serviceTypes,
					announcement.infos[si].serviceTypes) &&
					DiscoveryServerAgentImpl.
					matchServiceTypes(peer.request.groups,
					announcement.infos[si].groups)) 
					matches = true;
				}
				// send!
				try 
				{
				    // return to specific address and port - hopefully better for NAT
				    DatagramPacket p = 
					new DatagramPacket(announcepkt.getData(),
					announcepkt.getLength(),
					peer.address, 
					((int)peer.port)&0xffff);
				    // adjust for overflow error - ianm
				    socket.send(p);
				}
				catch (Exception e) 
				{
				    System.err.println("ERROR forwarding announcement to "+peer.address+"/"+peer.port+": "+e);
				}
			    }
			}
		    }
		} 
		catch (Exception e) 
		{
		    System.err.println("Warning: DiscoveryRendezvous failed to "+
			"get a valid discovery object "+
			"from "+announcepkt.getAddress()+"/"+
			announcepkt.getPort()+" ("+
			announcepkt.getLength()+" bytes)");
		    continue;
		}

	    }
	} 
	catch (Exception e) 
	{
	    System.err.println("ERROR in listenFn: "+e);
	    e.printStackTrace(System.err);
	}
    }
    /** expire peers
     */
    protected void expireFn() 
    {
	while(true) 
	{
	    synchronized(peers) 
	    {
		long now = System.currentTimeMillis();
		long expiredTime = now-ANNOUNCEMENT_EXPIRE_COUNT.value*ANNOUNCEMENT_INTERVAL_S.value*1000;
		for (int ip=0; ip<peers.size(); ip++) 
		{
		    PeerInfo peer = (PeerInfo)peers.elementAt(ip);
		    if (peer.lastReceiveTime < expiredTime) 
		    {
			System.out.println("Expire "+peer.address+"/"+peer.port+" "+(peer.request!=null ? "client" : "server"));
			peers.removeElementAt(ip);
			ip--;
		    }
		}
	    }
	    try 
	    {
		Thread.sleep(1000);
	    } 
	    catch (InterruptedException e) 
	    {
		System.err.println("expireFn interrupted");
	    }
	}
    }
}
