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
package equip.data.benchmark;
import java.util.Date;
import java.util.Vector;
import java.net.*;

public class UDPDelay {
    static boolean debug = false;
    static boolean dump = true;

    public static void main(String [] args) {
	try {
	    int proxyPort = new Integer(args[0]).intValue();
	    int serverPort = new Integer(args[2]).intValue();
	    InetAddress serverHost = InetAddress.getByName(args[1]);
	    int delayMs = new Integer(args[3]).intValue();
	    double lossRate = 0.01*new Double(args[4]).doubleValue();

	    new UDPDelay(proxyPort, serverHost, serverPort, 
			 delayMs, lossRate);

	} catch (Exception e) {
	    System.err.println("Usage: java equip.data.benchmark.UDPDelay "+
			       "<proxyport> <serverhost> <serverport> "+
			       "<delay-ms> <loss%>");
	    System.exit(-1);
	}
    }
    DatagramSocket proxySocket;
    DatagramSocket forwardSocket;
    public static class Packet {
	DatagramPacket datagram;
	long forwardTime;
	Packet(DatagramPacket datagram, int delayMs) {
	    this.datagram = datagram;
	    forwardTime = (new Date().getTime())+delayMs;
	}
    }
    int serverPort;
    InetAddress serverHost;
    static final int DATAGRAM_SIZE = 1500;
    static final int CLIENT = 0;
    static final int SERVER = 1;

    int reversePort[] = new int[2];
    InetAddress reverseHost[] = new InetAddress[2];
    public class ForwardDelayed {
	DatagramSocket from;
	DatagramSocket to;
	Vector packets = new Vector();
	int delayMs;
	ForwardDelayed(DatagramSocket from, DatagramSocket to,
		       int delayMs, double lossRate) {
	    this.from = from;
	    this.to = to;
	    this.delayMs = delayMs;
	    new Receiver();
	    new Forwarder();
	    System.err.println("ForwardDelayed...");
	}
	class Receiver extends Thread {
	    Receiver() {
		start();
	    }
	    public void run() {
		try {
		    while (true) {
			DatagramPacket pkt = 
			    new DatagramPacket(new byte[DATAGRAM_SIZE],
					       DATAGRAM_SIZE);
			from.receive(pkt);
			if (debug) {
			    System.out.println("Received "+
					       pkt.getLength()+"B from "+
					       pkt.getAddress()+":"+
					       pkt.getPort()+" on port "+
					       from.getLocalPort()+" - queue");
			}
			int i = (from==proxySocket) ? CLIENT : SERVER;
			if (reversePort[i]==0) {
			    reversePort[i] = pkt.getPort();
			    reverseHost[i] = pkt.getAddress();
			    System.err.println("Setting "+
					       (i==CLIENT ? "client" : "server")+
					       " reverse address to "+
					       reverseHost[i]+":"+reversePort[i]);
			} else if (reversePort[i]!=pkt.getPort() ||
				   !reverseHost[i].equals(pkt.getAddress())) {
			    reversePort[i] = pkt.getPort();
			    reverseHost[i] = pkt.getAddress();
			    System.err.println("*Changing "+
					       (i==CLIENT ? "client" : "server")+
					       " reverse address to "+
					       reverseHost[i]+":"+reversePort[i]);
			}
			if (from==proxySocket && dump) 
			    dump(pkt.getAddress(), pkt.getPort(),
				 reverseHost[1-i], reversePort[1-i],
				 pkt);

			synchronized(packets) {
			    packets.addElement(new Packet(pkt, delayMs));
			    packets.notify();
			}
		    }
		} catch (Exception e) {
		    System.err.println("Error in Receiver: "+e);
		    e.printStackTrace(System.err);
		    System.exit(-1);
		}
	    }
	}
	class Forwarder extends Thread {
	    Forwarder() {
		start();
	    }
	    public void run() {
		try {
		    while (true) {
			Packet next = null;
			synchronized(packets) {
			    while(packets.size()==0) {
			    try {
				    packets.wait();
				} catch(Exception e) {}
			    }
			    next = (Packet)packets.elementAt(0);
			    packets.removeElementAt(0);
			}
			while (true) {
			    long now = new Date().getTime();
			    long delay = next.forwardTime-now;
			    if (delay>0)
				try {
				    Thread.sleep(delay);
				} catch (Exception e) {}
			    else
				break;
			} 
			byte [] data = next.datagram.getData();
			int len = next.datagram.getLength();
			DatagramPacket pkt = null;
			// rewrite SPort & SAddr
			JcpPacketHeader hdr = new JcpPacketHeader(data);
			hdr.setSPort((short)(to.getLocalPort()));
			if (to==forwardSocket) {
			    pkt = new DatagramPacket(data, len,
						     reverseHost[SERVER],
						     reversePort[SERVER]);
			}
			else if (reversePort[CLIENT]==0) {
			    System.err.println("Warning: reverse path packet "+
					       "discarded - client unknown");
			} else {
			    pkt = new DatagramPacket(data, len,
						     reverseHost[CLIENT],
						     reversePort[CLIENT]);
			}
			if (debug) {
			    System.out.println("Forward "+
					       next.datagram.getLength()+"B from "+
					       next.datagram.getAddress()+":"+
					       next.datagram.getPort()+" to "+
					       pkt.getAddress()+":"+
					       pkt.getPort()+" on port "+
					       to.getLocalPort());
			}
			if (to==proxySocket && dump) 
			    dump(next.datagram.getAddress(),
				 next.datagram.getPort(),
				 pkt.getAddress(), pkt.getPort(), pkt);
			to.send(pkt);
		    }
		} catch (Exception e) {
		    System.err.println("Error in Receiver: "+e);
		    e.printStackTrace(System.err);
		    System.exit(-1);
		}
	    }
	}
    }
    public UDPDelay(int proxyPort, InetAddress serverHost,
		    int serverPort, int delayMs, double lossRate) {
	try {
	    this.serverHost = serverHost;
	    this.serverPort = serverPort;
	    reversePort[SERVER] = serverPort;
	    reverseHost[SERVER] = serverHost;
	    proxySocket = new DatagramSocket(proxyPort);
	    forwardSocket = new DatagramSocket();
	    
	    new ForwardDelayed(proxySocket, forwardSocket, delayMs, lossRate);
	    new ForwardDelayed(forwardSocket, proxySocket, delayMs, lossRate);
	} catch (Exception e) {
	    System.err.println("Error in UDPDelay: "+e);
	    e.printStackTrace(System.err);
	    System.exit(-1);
	}
    }
    void dump(InetAddress from, int fromPort, 
	      InetAddress to, int toPort,
	      DatagramPacket pkt) {
	JcpPacketHeader hdr = new JcpPacketHeader(pkt.getData());
	System.out.println(new Date().getTime()+" "+
			   from.getHostAddress()+":"+fromPort+" -> "+
			   to.getHostAddress()+":"+toPort+" "+pkt.getLength()+
			   " B");
	System.out.println("- rseq="+hdr.getRSeq()+" seq="+hdr.getSeq()+
			   " ack="+hdr.getAck()+" swind="+hdr.getSWind()+
			   " flags="+hdr.getFlags());
    }
class JcpPacketHeader
{
  private byte [] b;
  public JcpPacketHeader(byte [] b) {
    this.b = b;
  }
  public byte getMagic(int i) {
    return b[i];
  }
  public void setMagic(int i, byte bb) {
    b[i] = bb;
  }
  public byte getVersion() {
    return b[3];
  }
  public void setVersion(byte bb) {
    b[3] = bb;
  }
  protected int getInt(int i) {
    return 
      ((((int)b[i+0]) & 0xff) << 24) |
      ((((int)b[i+1]) & 0xff) << 16) |
      ((((int)b[i+2]) & 0xff) << 8) |
      ((((int)b[i+3]) & 0xff));
  }
  protected void setInt(int i, int addr) {
    b[i+0] = (byte)((addr >> 24) & 0xff);
    b[i+1] = (byte)((addr >> 16) & 0xff);
    b[i+2] = (byte)((addr >> 8) & 0xff);
    b[i+3] = (byte)((addr) & 0xff);
  }
  protected void setInt(int i, byte [] bb) {
    b[i+0] = bb[0];
    b[i+1] = bb[1];
    b[i+2] = bb[2];
    b[i+3] = bb[3];
  }
  protected short getShort(int i) {
      return (short)(
		     ((((short)b[i+0]) & 0xff) << 8) |
		     ((((short)b[i+1]) & 0xff)));
  }
  protected void setShort(int i, short addr) {
    b[i+0] = (byte)((addr >> 8) & 0xff);
    b[i+1] = (byte)((addr) & 0xff);
  }

  public int getRSeq() {
    return getInt(0);
  }
  public void setRSeq(int rSeq) {
    setInt(0, rSeq);
  }
  public int getSeq() {
    return getInt(4);
  }
  public void setSeq(int seq) {
    setInt(4, seq);
  }
  public int getAck() {
    return getInt(8);
  }
  public void setAck(int ack) {
    setInt(8, ack);
  }
  public int getSWind() {
    return getInt(12);
  }
  public void setSWind(int sWind) {
    setInt(12, sWind);
  }
  public short getSPort() {
    return getShort(16);
  }
  public void setSPort(short port) {
    setShort(16, port);
  }
  public int getSAddr() {
    return getInt(20);
  }
  public void setSAddr(int addr) {
    setInt(20, addr);
  }
  public int getFlags() {
    return getInt(24);
  }
  public void setFlags(int flags) {
    setInt(24, flags);
  }
  public int getSize() 
  {
	return 28;
  }
}
}

