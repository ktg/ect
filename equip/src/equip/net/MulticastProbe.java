/*
<COPYRIGHT>

Copyright (c) 2003-2005, University of Nottingham
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
package equip.net;

import java.io.*;
import java.net.*;

/** a simple multicast network probe to help with mobile networking tests.
 * regularly sends a multicast packet a group, which it also listens to.
 */
public class MulticastProbe {
    /** multicast socket to send and receive on */
    MulticastSocket socket;

    /** default group */
    public static InetAddress DEFAULT_GROUP = null;
    static {
	try {
	    DEFAULT_GROUP = InetAddress.getByName("225.1.2.4");
	} catch(UnknownHostException e) {
	    System.err.println("ERROR: MulticastProbe could not initialise DEFAULT_GROUP: "+e);
	}
    }
    /** default port */
    public static final int DEFAULT_PORT = 6234;

    /** default interval, ms */
    public static final int INTERVAL_MS = 1000;

    /** cons on given multicast group - runs immediately */
    public MulticastProbe(final InetAddress group, final int port) 
	throws IOException, SecurityException {
	socket = new MulticastSocket(port);
	socket.joinGroup(group);
	socket.setTimeToLive(1);
	// send thread
	new Thread() {
	    public void run() {
		try {
		    while(true) {
			byte [] data = new byte[8];
			long now = System.currentTimeMillis();
			int i;
			for(i=0; i<8; i++) {
			    data[i] = (byte)(0xff & now);
			    now = now >> 8;
			}
			DatagramPacket packet = new DatagramPacket(data, data.length,
								   group, port);
			try {
			    socket.send(packet);
			} catch (Exception e) {
			    System.out.println("["+System.currentTimeMillis()+"] MulticastProbe errrorInSend "+e);
			}
			Thread.sleep(INTERVAL_MS);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in MulticastProbe sending thread: "+e);
		    e.printStackTrace(System.err);
		}
		System.err.println("MulticastProbe sending thread exited");
	    }}.start();
	// receive thread
	new Thread() {
	    public void run() {
		try {
		    while(true) {
			byte [] data = new byte[8];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
			    socket.receive(packet);
			} catch (Exception e) {
			    System.out.println("["+System.currentTimeMillis()+"] MulticastProbe errrorInReceive "+e);
			    Thread.sleep(INTERVAL_MS/2);
			    continue;
			}
			long then = 0;
			int i;
			for (i=7; i>=0; i--) {
			    then = then << 8;
			    then = then | (0xff & ((long)data[i]));
			}
			System.out.println("["+System.currentTimeMillis()+"] MulticastProbe receivedPacket from "+
					   packet.getAddress().getHostAddress()+":"+
					   packet.getPort()+" sentAt "+then);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in MulticastProbe receiving thread: "+e);
		    e.printStackTrace(System.err);
		}
		System.err.println("MulticastProbe receiving thread exited");
	    }}.start();
	// receive thread
    }

    /** test main: usage java equip.net.MulticastProbe */
    public static void main(String [] args) {
	try {
	    new MulticastProbe(DEFAULT_GROUP, DEFAULT_PORT);
	} catch (Exception e) {
	    System.err.println("ERROR creating MulticastProbe: "+e);
	    e.printStackTrace(System.err);
	}
    }
}
// EOF
