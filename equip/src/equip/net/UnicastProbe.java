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

/** a simple unicast network probe to help with mobile networking tests.
 * regularly sends to a unicast address, and also listens.
 */
public class UnicastProbe {
    /** multicast socket to send and receive on */
    DatagramSocket socket;

    /** default port */
    public static final int DEFAULT_PORT = 6234;

    /** default interval, ms */
    public static final int INTERVAL_MS = 1000;

    /** cons on given multicast group - runs immediately */
    public UnicastProbe(final InetAddress peer, final int localport, final int peerport) 
	throws IOException, SecurityException {
	socket = new DatagramSocket(localport);
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
								   peer, peerport);
			try {
			    socket.send(packet);
			} catch (Exception e) {
			    System.out.println("["+System.currentTimeMillis()+"] UnicastProbe errrorInSend "+e);
			}
			Thread.sleep(INTERVAL_MS);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in UnicastProbe sending thread: "+e);
		    e.printStackTrace(System.err);
		}
		System.err.println("UnicastProbe sending thread exited");
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
			    System.out.println("["+System.currentTimeMillis()+"] UnicastProbe errrorInReceive "+e);
			    Thread.sleep(INTERVAL_MS/2);
			    continue;
			}
			long then = 0;
			int i;
			for (i=7; i>=0; i--) {
			    then = then << 8;
			    then = then | (0xff & ((long)data[i]));
			}
			System.out.println("["+System.currentTimeMillis()+"] UnicastProbe receivedPacket from "+
					   packet.getAddress().getHostAddress()+":"+
					   packet.getPort()+" sentAt "+then);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in UnicastProbe receiving thread: "+e);
		    e.printStackTrace(System.err);
		}
		System.err.println("UnicastProbe receiving thread exited");
	    }}.start();
	// receive thread
    }

    /** test main: usage java equip.net.UnicastProbe &lt;peerip&gt; &lt;localport&gt; &lt;peerport&gt; */
    public static void main(String [] args) {
	if (args.length!=3) {
	    System.err.println("Usage:  java equip.net.UnicastProbe <peerip> <localport> <peerport>");
	    System.exit(-1);
	}
	try {
	    new UnicastProbe(InetAddress.getByName(args[0]), new Integer(args[1]).intValue(),
			     new Integer(args[2]).intValue());
	} catch (Exception e) {
	    System.err.println("ERROR creating UnicastProbe: "+e);
	    e.printStackTrace(System.err);
	}
    }
}
// EOF
