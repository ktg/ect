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
/* ConnectionSapJcp.java
   Jim Purbrick
   10/8/2002 
*/
package equip.net;

import equip.runtime.*;

import java.net.*;
import java.io.*;
import java.util.*;

/** JCP packet flag values */
class JcpFlag
{
	static final int FLAG_NONE = 0x0; // Keep alives have no flags or data.
	static final int FLAG_SYN = 0x1;
	static final int FLAG_ACK = 0x2;
	static final int FLAG_START = 0x4; // Start of object.
	static final int FLAG_END = 0x8; // End of object.
	static final int FLAG_RST = 0x10;
	static final int FLAG_FIN = 0x20;
	static final int FLAG_RELIABLE = 0x40; // JcpFragment is reliable.
        static final int FLAG_SACK = 0x80; // SACK present
}

/** JCP packet header data structure */
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
  public int getSack() {
    return getInt(28);
  }
  public void setSack(int ack) {
    setInt(28, ack);
  }
  public static int getSize() 
  {
	return 32;
  }
}

/** JCP message fragment */
class JcpFragment
{

/**
* JcpFragment
* @param m
*/
public
JcpFragment(Mbuf  m)
{mbuf = m; objectID = 0; priority = -1;}

/**
* printHeader
*/
public
void printHeader()
{
    if (ConnectionSapJcp.debug || ConnectionSapJcp.debug2) {
	System.out.print("Packet: ");
	JcpPacketHeader  header = getHeader();
	if((header.getFlags() & JcpFlag.FLAG_SYN) != 0) System.out.print("SYN ");
	if((header.getFlags() & JcpFlag.FLAG_ACK) != 0) System.out.print("ACK ");
	if((header.getFlags() & JcpFlag.FLAG_START) != 0) System.out.print("START ");
	if((header.getFlags() & JcpFlag.FLAG_END) != 0) System.out.print("END ");
	if((header.getFlags() & JcpFlag.FLAG_RELIABLE) != 0) System.out.print("REL ");
	if((header.getFlags() & JcpFlag.FLAG_FIN) != 0) System.out.print("FIN ");
	if((header.getFlags() & JcpFlag.FLAG_RST) != 0) System.out.print("RST ");
	System.out.print("seq=" + String.valueOf(header.getRSeq()) + String.valueOf('.') + String.valueOf(header.getSeq()) + ' ');
	System.out.print(" ack=" + String.valueOf(header.getAck()) + ' ');
	System.out.print(" sWind=" + String.valueOf(header.getSWind()) + ' ');
	System.out.print(" length=" + String.valueOf(mbuf.getUsed()) + ' ');
	System.out.print(" sAddr=" + String.valueOf(header.getSAddr()) + ' ');
	System.out.println(" sPort=" + String.valueOf(header.getSPort()));
    }
}

/**
* printData
*/
public
void printData()
{
	//...
}

/**
* getHeader
* @return JcpPacketHeader *
*/
public
JcpPacketHeader   getHeader()
{return new JcpPacketHeader(mbuf.getBuf());}

/**
* getData
* @return Mbuf *
*/
public
Mbuf   getData()
{return mbuf;}

/**
* getDataLength
* @return int
*/
public
int getDataLength()
{return mbuf.getUsed() - JcpPacketHeader.getSize();}

/**
* setObjectID
* @param o
*/
public
void setObjectID(int o)
{objectID = o;}

/**
* getObjectID
* @return int
*/
public
int getObjectID()
{return objectID;}

/**
* setPriority
* @param p
*/
public
void setPriority(int p)
{priority = p;}

/**
* getPriority
* @return int
*/
public
int getPriority()
{return priority;}
Mbuf mbuf;
int objectID;
int priority;
boolean treatAsUnsent=false; // slow start after retransmit behaviour
}

/** JCP Timer class.
 * 
 * This code has been generated using C2J++
 * C2J++ is based on Chris Laffra's C2J (laffra@watson.ibm.com)
 * Read general disclaimer distributed with C2J++ before using this code
 * For information about C2J++, send mail to Ilya_Tilevich@ibi.com
 */

class Timer
{

/**
* Timer
*/
public
Timer()
{
	set = false;
}

/**
* setTimeout
* @param t
*/
public
void setTimeout(int t)
{
	timeout = t;
}

/**
* getTimeout
* @return int
*/
public
long getTimeout()
{return timeout;}

/**
* getExpires
* @return int
*/
public
long getExpires()
{return startTime+timeout;}

/**
* setTimer
*/
public
void setTimer()
{
	Date d = new Date();
	startTime = d.getTime();
	set = true;
}

/**
* isSet
* @return PRBool
*/
public
boolean isSet()
{return set;}

/**
* cancelTimer
*/
public
void cancelTimer()
{
	set = false;
}

/**
* tick
* @return PRBool
*/
public
boolean tick()
{
	Date d = new Date();
	if(set && ((d.getTime() - startTime) > timeout))
	{
		set = false;
		return true;
	}
	else return false;
}
/**
 * bringForward
 * If set, ensure that it will expire/trigger within this time
 */
public
void bringForward(long maxRemaining) {
    if (set) {
	long now = new Date().getTime();
	if (timeout-(now-startTime) > maxRemaining)
	    startTime = now-(timeout-maxRemaining);
    }
}

boolean set;
long startTime;
long timeout;
}

/*
 * JCP class RoundTripTimer.
 * 
 * This code has been generated using C2J++
 * C2J++ is based on Chris Laffra's C2J (laffra@watson.ibm.com)
 * Read general disclaimer distributed with C2J++ before using this code
 * For information about C2J++, send mail to Ilya_Tilevich@ibi.com
 */

class RoundTripTimer
{

/**
* RoundTripTimer
*/
public
RoundTripTimer()
{
	smoothedRoundTripTime = 0;
	roundTripTimeVariation = 0;
	startTime = 0;
	sequenceNumber = 0;
}

/**
* startTimer
* @param i
*/
public
void startTimer(int i)
{
	Date d = new Date();
	startTime = d.getTime();
	sequenceNumber = i;
}

/**
* stopTimer
* @return int
*/
public
int stopTimer()
{
	
	sequenceNumber = 0;

	Date d = new Date();
	int roundTripTime = (int) (d.getTime() - startTime);

	
	if(smoothedRoundTripTime == 0)
	{
		
		smoothedRoundTripTime = roundTripTime;
		roundTripTimeVariation = roundTripTime / 2;
	}
	else
	{
		
		float beta = 0.25f;
		float alpha = 0.125f;

		
		roundTripTimeVariation = (int) ((1 - beta) * roundTripTimeVariation + beta * Math.abs(smoothedRoundTripTime - roundTripTime));
		smoothedRoundTripTime = (int) ((1 - alpha) * smoothedRoundTripTime + alpha * roundTripTime);
	}

	
	return roundTripTime;
}

/**
* cancelTimer
*/
public
void cancelTimer()
{
	sequenceNumber = 0;
}

/**
* isSet
* @return PRBool
*/
public
boolean isSet()
{
	return sequenceNumber != 0;
}

/**
* getAverage
* @return int
*/
public
int getAverage()
{
	return smoothedRoundTripTime;
}

/**
* getVariation
* @return int
*/
public
int getVariation()
{
	return roundTripTimeVariation;
}

/**
* getSequenceNumber
* @return int
*/
public
int getSequenceNumber()
{return sequenceNumber;}
long startTime;
int sequenceNumber;
int smoothedRoundTripTime;
int roundTripTimeVariation;
}

/**
 * JCP class implementing {@link ConnectionSap} for JCP.
 * 
 * This code has been generated using C2J++
 * C2J++ is based on Chris Laffra's C2J (laffra@watson.ibm.com)
 * Read general disclaimer distributed with C2J++ before using this code
 * For information about C2J++, send mail to Ilya_Tilevich@ibi.com
 */

public class ConnectionSapJcp extends ConnectionSap implements Runnable
{
public static boolean debug=false;
public static boolean debug2=false;
public static boolean debugCon=false;
public static boolean debugRetrans=false;
public static boolean debugRate=false;

public static double receiveLossRate=0.0;
public static double sendLossRate=0.0;
Timer retransmissionTimer;
Timer delayedAckTimer;
Timer timeoutTimer;
Timer keepAliveTimer;
RoundTripTimer roundTripTimer;
int duplicateAcks;
boolean fastRetransmit;
int slowStartThreshold;
float connectionStrength;
static final int STATE_LISTEN = 0;
static final int STATE_CLOSED = 1;
static final int STATE_SYN_SENT = 2;
static final int STATE_SYN_RCVD = 3;
static final int STATE_ESTABLISHED = 4;
static final int STATE_FIN_WAIT_1 = 5;
static final int STATE_FIN_WAIT_2 = 6;
static final int STATE_CLOSE_WAIT = 7;
static final int STATE_CLOSING = 8;
static final int STATE_LAST_ACK = 9;
static final int STATE_TIME_WAIT = 10;
DatagramSocket   socket;
//int session_id;
//int message_id;
Vector unsentFragments;
Vector unackedFragments;
Vector receivedFragments;
Vector completedObjectFragments;
Thread receiveThread;
int receiveNextSeq;
int receiveLastRSeq;
int maxReceiveWindow;
int sendLastAck;
int sendNextSeq;
int highestSeq;
int recover; // rfc2582 NewReno
boolean firstPartialAck;
int sendLastRSeq;
int sendWindow;
int sendWindow1;
int sendWindow2;
int maxUnsentFragments;
float conjestionWindow;
int state;
int maxFragmentSize;
int minRetransmissionTimeout;
int maxRetransmissionTimeout;
int connectionStrengthHistorySize;
int advertisedReceiveWindow;
/**
 * printState
 */
public 
void printState(int state) {
    switch(state) {
    case STATE_LISTEN:
	System.out.print("LISTEN");
	break;
    case STATE_CLOSED:
	System.out.print("CLOSED");
	break;
    case STATE_SYN_SENT:
	System.out.print("SYN_SENT");
	break;
    case STATE_SYN_RCVD:
	System.out.print("SYN_RCVD");
	break;
    case STATE_ESTABLISHED:
	System.out.print("EST");
	break;
    case STATE_FIN_WAIT_1:
	System.out.print("FIN_WAIT_1");
	break;
    case STATE_FIN_WAIT_2:
	System.out.print("FIN_WAIT_2");
	break;
    case STATE_CLOSE_WAIT:
	System.out.print("CLOSE_WAIT");
	break;
    case STATE_CLOSING:
	System.out.print("CLOSING");
	break;
    case STATE_LAST_ACK:
	System.out.print("LAST_ACK");
	break;
    case STATE_TIME_WAIT:
	System.out.print("TIME_WAIT");
	break;
    default:
	System.out.print("unknown("+state+")");
    }
}

/**
 * printState
 */
void printState(String where) {
    printState(where,debug2);
}
public 
void printState(String where, boolean debug2) {
    if (!debug2)
	return;
    synchronized(System.out) {
	long now=new Date().getTime();
	System.out.print("STATE("+where+"): time="+now+
			 " thread="+Thread.currentThread()+
			 " conn="+localAddress+"."+localPort+"/"+
			 remoteAddress+"."+remotePort+" state=");
	printState(state);
	System.out.print(" rtt="+roundTripTimer.getAverage()+"\n");
	System.out.print("send: fastRetransmit="+fastRetransmit+
			 " slowStartThresh="+slowStartThreshold+
			 " unsent="+unsentFragments.size()+
			 " unacked="+unackedFragments.size()+
			 " lastAck="+sendLastAck+" nextSeq="+sendNextSeq+
			 " lastRSeq="+sendLastRSeq+" sendWindow="+sendWindow+
			 " sendWindow1="+sendWindow1+" sendWindow2="+sendWindow2+
			 " maxUnsent="+maxUnsentFragments+
			 " conjestionWindow="+((int)conjestionWindow)+"\n");
	System.out.print("recv: duplicateAcks="+duplicateAcks+
			 " received="+receivedFragments.size()+
			 " completed="+completedObjectFragments.size()+
			 " nextSeq="+receiveNextSeq+
			 " lastRSeq="+receiveLastRSeq+
			 " maxWindow="+maxReceiveWindow+"\n");
	System.out.print("timers:"+
			 " retransmission="+retransmissionTimer.isSet()+
			 (!retransmissionTimer.isSet() ? "" : 
			  "("+(retransmissionTimer.getExpires()-now)+"ms)")+
			 " delayedAck="+delayedAckTimer.isSet()+
			 (!delayedAckTimer.isSet() ? "" : 
			  "("+(delayedAckTimer.getExpires()-now)+"ms)")+
			 " timeout="+timeoutTimer.isSet()+
			 (!timeoutTimer.isSet() ? "" : 
			  "("+(timeoutTimer.getExpires()-now)+"ms)")+
			 " keepAlive="+keepAliveTimer.isSet()+
			 (!keepAliveTimer.isSet() ? "" : 
			  "("+(keepAliveTimer.getExpires()-now)+"ms)")+"\n");
    }
}
void printRateState(String where) {
    synchronized(System.out) {
	long now=new Date().getTime();
	System.out.print(now+" unsent="+unsentFragments.size()+
			 " unacked="+(sendNextSeq-sendLastAck)+
			 " sendWindow="+sendWindow+
			 " cong.Window="+((int)conjestionWindow)+
			 " "+where+"\n");
	System.out.flush();
    }
}

/**
* ConnectionSapJcp
* @param remoteAddress
* @param remotePort
* @param socket
*/
public
ConnectionSapJcp(int remoteAddress, short remotePort, DatagramSocket  socket)
{
	this.remoteAddress = remoteAddress;
	this.remotePort = remotePort;
	this.socket = socket;
	init();

	state = STATE_SYN_SENT;
	syn();

	startReceiveThread();	
}

/**
* init
*/
private
void init()
{
	// Create socket.
	if(socket == null)
	{
		try
		{
			socket = new DatagramSocket();
		}
		catch(SocketException s)
		{
			System.err.println("ConnectionSapJcp::ConnectionSapJcp: Failed to created DatagramSocket " + String.valueOf(s) + '\n');
			socket = null;
			status = StatusValues.STATUS_ERROR;
			return;
		}
	}

	try
	{
		byte[] ip = InetAddress.getLocalHost().getAddress();
		localAddress = 
			((((int)ip[0]) & 0xff) << 24) |
			((((int)ip[1]) & 0xff) << 16) |
			((((int)ip[2]) & 0xff) << 8) |
			((((int)ip[3]) & 0xff));
		localPort = (short) socket.getLocalPort();
	}
	catch(UnknownHostException e)
	{
		System.err.println("ERROR: ConnectionSapJcp.init " + e);
		status = StatusValues.STATUS_ERROR;
	}

	/** @c2j++ Replacement from cerr << "ConnectionSapJcp::init: local " << localAddress << " " << localPort << " remote " << remoteAddress << " " << remotePort << endl; */
	if (debug)
	    System.out.println("ConnectionSapJcp::init: local " + String.valueOf(localAddress) + " " + String.valueOf(localPort) + " remote " + String.valueOf(remoteAddress) + " " + String.valueOf(remotePort) + '\n');

	unsentFragments = new Vector();
	unackedFragments = new Vector();
	receivedFragments = new Vector();
	completedObjectFragments = new Vector();

	// Select ISS based on current time. Clock cycle period should be greater than
	// JcpFragment lifetime in network.
	Date d = new Date();
	int initialSeq = (int) d.getTime();

	receiveNextSeq = 0;
	receiveLastRSeq = 0;
	sendNextSeq = initialSeq;
	sendLastRSeq = 0;
	sendLastAck = initialSeq;
	highestSeq = initialSeq;
	sendWindow = 120;
	sendWindow1 = 0;
	sendWindow2 = 0;
	conjestionWindow = 1;
	slowStartThreshold = 100;

	// Initial retransmission time of 3 seconds,
	// as specified in RFC 2988.
	retransmissionTimer = new Timer();
	int timeout = 3000;
	timeout = ((timeout)>(minRetransmissionTimeout)?(timeout):(minRetransmissionTimeout));
	timeout = ((timeout)<(maxRetransmissionTimeout)?(timeout):(maxRetransmissionTimeout));
	retransmissionTimer.setTimeout(timeout);
    
	// RFC 2581 4.2
	// An ACK SHOULD be generated for at least 
	// every second full-sized segment, and MUST be generated within
    // 500 ms of the arrival of the first unacknowledged packet.
	delayedAckTimer = new Timer();
	delayedAckTimer.setTimeout(500);
	
	keepAliveTimer = new Timer();
	keepAliveTimer.setTimeout(keepAliveTimeMs);
	keepAliveTimer.setTimer();

	timeoutTimer = new Timer();
	timeoutTimer.setTimeout(connectionTimeoutMs);
	timeoutTimer.setTimer();

	fastRetransmit = false;
	connectionStrength = 1;

	// more like normal (max is shorter) -
	// note that retransmission timeout kills TCP throughput
	// since it causes a slow start
	minRetransmissionTimeout = 1000;
	maxRetransmissionTimeout = 10000;
	connectionStrengthHistorySize = 10;
	maxFragmentSize = 1024; 
	// smaller, to allow DataDeletePeer to do more queue management?!
	// also not sure if unacked unrel packets are counting quite right - cmg
	// larger again for low bw links?!
	maxReceiveWindow = 60 /*30*/ /*!!120!!*/;
	maxUnsentFragments = 60 /*30*/ /*!!120!!*/;
	
	try
	{
	        setRecvBufSize(maxFragmentSize * 2 * maxReceiveWindow);
	        setSendBufSize(maxFragmentSize * 2 * maxUnsentFragments);

		socket.setSoTimeout(100);
	}
	catch(SocketException e)
	{
		System.err.println("ERROR: ConnectionSapJcp.init " + e);
		status = StatusValues.STATUS_ERROR;
	}

	roundTripTimer = new RoundTripTimer();
}

/**
* startReceiveThread
*/
private
void startReceiveThread()
{
	receiveThread = new Thread(this);
	receiveThread.start();
}

/**
* ConnectionSapJcp
* @param remoteAddress
* @param remotePort
*/
public
ConnectionSapJcp(int remoteAddress, short remotePort)
{
    if (debug)
	System.out.println("ConnectionSapJcp::ConnectionSapJcp\n");

	
	this.remoteAddress = remoteAddress;
	this.remotePort = remotePort;

	
	init();

	
	state = STATE_SYN_SENT;
	syn();

	startReceiveThread();
}

/**
* ConnectionSapJcp
* @param packet
*/
public
ConnectionSapJcp(DatagramPacket  packet)
{
	init();

	
	state = STATE_LISTEN;
	receivePacket(packet);

	startReceiveThread(); 
}

/**
* getProtocol
* @return SapProtocol
*/
public
int getProtocol()
{
  return SapProtocol.SAP_PR_UDP;
}

/**
* waitUntilConnected
* @return int
*/
public
int waitUntilConnected()
{
  
  return StatusValues.STATUS_ERROR;
}

/**
* close
* @param flushFlag
*/
public
void close(boolean flushFlag)
{	
	if(flushFlag = true)
	{
		abort();
	}
	else
	{
	  switch(state) 
	  {
	  case STATE_SYN_SENT:
		break;
	  case STATE_SYN_RCVD:
		fin();
		state = STATE_FIN_WAIT_1;
		break;
	  case STATE_ESTABLISHED:
		fin();
		state = STATE_FIN_WAIT_1;
		break;
	  case STATE_CLOSE_WAIT:
		fin();
		state = STATE_FIN_WAIT_1;
		break;
	  }
	}
}

/**
* readObject
* @param object
* @return int
*/
public
ValueBase readObject() throws java.io.IOException
{
	ValueBase object;

	int oldReceiveWindow = 0;
	synchronized(this)
	{

	if(state == STATE_CLOSED ||
	   state == STATE_CLOSING ||
	   state == STATE_LAST_ACK || 
	   state == STATE_TIME_WAIT ||
	   (state == STATE_CLOSE_WAIT && completedObjectFragments.size() == 0))
	{
 	        if (debug2)
		    printState("readObject/fail-closed");

		throw new IOException("ConnectionSapJcp closing(1)");
	}

	// Block till data.
	while(completedObjectFragments.size() == 0)
	{
		try
		{
 		        printState("readObject/no-completed");
			wait();
		}
		catch(InterruptedException e) {
		}

		if(state == STATE_CLOSED ||
		   state == STATE_CLOSING ||
		   state == STATE_LAST_ACK || 
		   state == STATE_TIME_WAIT ||
		   (state == STATE_CLOSE_WAIT && completedObjectFragments.size() == 0))
		    {
			if (debug2)
			    printState("readObject/fail-closed2");
			throw new IOException("ConnectionSapJcp closing(2)");
		    }
	}

	oldReceiveWindow = getReceiveWindow();

	// Build Mbuf chain.
	Mbuf  first =  ((JcpFragment) completedObjectFragments.elementAt(0)).getData();
	Mbuf  current = null;

	int index;
	for(index = 0; index < completedObjectFragments.size(); index++)
	{
		JcpFragment  f = (JcpFragment) completedObjectFragments.elementAt(index);

		// Chain Mbuf.
		if(current != null) current.setNext(f.getData());
		current = f.getData();
		
		// Save end flag.
		boolean endFlag = (f.getHeader().getFlags() & JcpFlag.FLAG_END) != 0;

		
		completedObjectFragments.removeElementAt(index);
		index--;
		
		// If end of object, break.
		if(endFlag) break;
	}

	// Read object. 
	MbufInputStream memoryIn = new MbufInputStream();
	memoryIn.reset(first);
	equip.runtime.ObjectInputStream objectIn = new equip.runtime.ObjectInputStream(memoryIn);

	try
	{
		object = objectIn.readObject();
	}
	catch(Exception e)
	{
		System.err.print("WARNING: ConnectionSapJcp::readObject failed in readObject\n");
		abort();
		status = StatusValues.STATUS_ERROR;
		throw new IOException("ConnectionSapJcp: nested error: "+e);
	}

	if (debug2)
	    printState("readObject/done-ok");

	// if receiveWindow has now opened up from nothing then let the other end know sharpish
	// - cmg - currently 0.5 s delay
	if (debug) {
	  System.out.flush();
	  System.err.println("oldReceiveWindow="+oldReceiveWindow);
	}
	if (oldReceiveWindow==0) {
	  // the problem is that the receive thread normally does acks, so thread
	  // safety is an extra issue.
	  if (debug) 
	    System.err.println("note: zeroReceiveWindow");
	  
	  ackNow();
	}

	} // Synchronized block.

	return object;
}

/**
* getUnsentObjects
* @param object_id
* @return int
*/
public
synchronized
int getUnsentObjects()
{
  // Delete fragments from unsent list.
  int index;
  int last_object_id = -1;
  int unsentObjects = 0;
  for(index = 0; index < unsentFragments.size(); index++)
  {
	JcpFragment  f = (JcpFragment) unsentFragments.elementAt(index);
	if(f.getObjectID() != last_object_id)
	{
	    unsentObjects++;
	    last_object_id = f.getObjectID();
	}
  }
  return unsentObjects;
}


/**
* cancelObject
* @param object_id
* @return int
*/
public
synchronized
int cancelObject(int object_id)
{
  // Delete fragments from unsent list.

  int index;
  // if already (part) in unacked list then leave it alone
  for(index = 0; index < unackedFragments.size(); index++)
  {
	JcpFragment  f = (JcpFragment) unackedFragments.elementAt(index);
	if(f.getObjectID() == object_id)
	{
	        if (debug)
		    System.out.println("WARNING: cancelObject for unacked fragment "+object_id+" ignored");
		return StatusValues.STATUS_OK;  
	}
  }

  for(index = 0; index < unsentFragments.size(); index++)
  {
	JcpFragment  f = (JcpFragment) unsentFragments.elementAt(index);
	if(f.getObjectID() == object_id)
	{
	        if (debug)
		    System.out.println("cancelObject removes unsent fragment "+object_id);
		unsentFragments.removeElementAt(index);
		index--;
	}
  }

  // may wake up writeObject
  if( /*fragmentsSent &&*/  (unsentFragments.size() + unackedFragments.size()) < maxUnsentFragments) {
      notifyAll();
  }

  return StatusValues.STATUS_OK;  
}

public final int UNSENT = 1;
public final int UNACKED = 2;
public final int UNKNOWN = 3;

/**
* getObjectStatus
* @param object_id
* @return int, UNSENT,UNACKED,UNKNOWN
*/
public
synchronized
int getObjectStatus(int object_id)
{
  // check from unsent list.
  int index;
  for(index = 0; index < unsentFragments.size(); index++)
  {
	JcpFragment  f = (JcpFragment) unsentFragments.elementAt(index);
	if(f.getObjectID() == object_id)
	{
	    return UNSENT;
	}
  }

  // check from unacked list.
  for(index = 0; index < unackedFragments.size(); index++)
  {
	JcpFragment  f = (JcpFragment) unackedFragments.elementAt(index);
	if(f.getObjectID() == object_id)
	{
	    return UNACKED;
	}
  }

  return UNKNOWN;
}

public int writeObjectLocked(ValueBase  object, boolean reliableFlag, int[] object_id, int priority)
{
	System.err.println("ERROR: ConnectionSapJcp.writeObjectLocked called");
	return StatusValues.STATUS_ERROR;
}

public int writeObject(ValueBase  object, boolean reliableFlag, int[] object_id)
{
	return writeObject(object, reliableFlag, object_id, 0);
}

/**
* writeObject
* @param object
* @param reliableFlag
* @param object_id
* @param priority
* @return int
*/
public
int writeObject(ValueBase  object, boolean reliableFlag, int[] object_id, int priority)
{
  if (object_id!=null && object_id.length>0)
	    object_id[0] = nextObjectId;
	nextObjectId++;

  if(state == STATE_CLOSED ||
	 state == STATE_FIN_WAIT_1 ||
	 state == STATE_FIN_WAIT_2 ||
	 state == STATE_CLOSING ||
	 state == STATE_LAST_ACK ||
	 state == STATE_TIME_WAIT)
  {
          if (debug2)
	      printState("writeObject/fail-closed");
	  return StatusValues.STATUS_ERROR;
  }

  // Write object into object output stream attached to memory.
  // MbufOutputStream creates a list of buffers. Set size and
  // header of buffers so one buffer can be used per JcpFragment.
  // Avoids copying serialized object data.
  MbufOutputStream memoryOut = new MbufOutputStream(maxFragmentSize, JcpPacketHeader.getSize());
  try
  {
	equip.runtime.ObjectOutputStream objectOut = new equip.runtime.ObjectOutputStream(memoryOut);
	objectOut.writeObject(object);
  }
  catch(IOException e)
  {
	System.err.print("WARNING: ConnectionSapJcp::writeObject to memory buffer failed in writeObject\n");
    close();
    status = StatusValues.STATUS_ERROR;
  }
  
  // Get memory buffers.
  Mbuf  memoryBufferList = memoryOut.takeMbuf();

  // Get exclusive access to send queue.
  synchronized(this) {

  // Block write if too many fragments queued, unless
  // object has top priority. This means that queue can
  // potentially grow beyond maximum size, but that
  // highest priority object is always written.
  if((unsentFragments.size() > 0) && 
	 (priority <= ((JcpFragment) unsentFragments.elementAt(0)).getPriority()))
  {
	  while(unsentFragments.size() + unackedFragments.size() >= maxUnsentFragments)
	  {
		try
		{
 		        printState("writeObject/unsent>max");
			wait();
		}
		catch(InterruptedException e) {
		}
	  }
  }

  // Find position in queue to insert new fragments.
  // Currently just queue all fragments for object at point in unsent queue
  // where priority drops below given priority.
  int insertPoint;
  for(insertPoint = 0; insertPoint < unsentFragments.size(); insertPoint++)
  {
	  if(((JcpFragment) unsentFragments.elementAt(insertPoint)).getPriority() < priority) break;
  }

  // Loop through buffers writing headers and queueing.
  Mbuf  current;
  for(current = memoryBufferList; current != null; current = current.getNext())
  {
	// If last buffer for this object, flag it.
	int flags = JcpFlag.FLAG_ACK;
	if(current == memoryBufferList) flags |= JcpFlag.FLAG_START;
	if(current.getNext() == null) flags |= JcpFlag.FLAG_END;
	if(reliableFlag) flags |= JcpFlag.FLAG_RELIABLE;

	// Make JcpFragment.
	JcpFragment  f = makeFragment(current, flags);
	f.setObjectID(object_id[0]);
	f.setPriority(priority);

	// Queue.
	unsentFragments.insertElementAt(f, insertPoint++);
  }
  
  // Release exclusive access to send queue.
  } // synchronized;

  // Try to send fragments if connection is sufficiently
  // established, otherwise just leave fragments on queue until
  // handshaking is finished.
  if(state == STATE_CLOSE_WAIT ||
	state == STATE_ESTABLISHED)
  {
	  output();
  }

  
  if (debug2) {
      printState("writeObject/done-ok");
  }

  return StatusValues.STATUS_OK;
}

/**
* setSendBufSize
* @param size
*/
public
void setSendBufSize(int size)
{
	super.setSendBufSize(size);
	maxUnsentFragments = size / maxFragmentSize;

	try
	{
	    java.lang.reflect.Method m = socket.getClass().getMethod
		("setSendBufferSize",
		 new Class [] { Integer.TYPE });
	    m.invoke(socket,new java.lang.Object [] 
		{ new Integer(size) });
	    m = socket.getClass().getMethod
		("getSendBufferSize",
		 new Class [] {});
	    Integer val = (Integer)m.invoke(socket,new java.lang.Object [] {});
	    if (val.intValue() < size) {
		System.err.println("WARNING: ConnectionSapJcp send buffer size only "+
				   val.intValue()+"/"+size);
		super.setSendBufSize(size);
		maxReceiveWindow = size / (2*maxFragmentSize);
	    }
	}
	catch(Exception e)
	{
		System.err.println("Warning: ConnectionSapJcp.setSendBufSize " + e);
	}
}

/**
* setRecvBufSize
* @param size
*/
public
void setRecvBufSize(int size)
{
	super.setRecvBufSize(size);
	maxReceiveWindow = size / (2*maxFragmentSize);

	try
	{
	    java.lang.reflect.Method m = socket.getClass().getMethod
		("setReceiveBufferSize",
		 new Class [] { Integer.TYPE });
	    m.invoke(socket,new java.lang.Object [] 
		{ new Integer(size) });
	    m = socket.getClass().getMethod
		("getReceiveBufferSize",
		 new Class [] {});
	    Integer val = (Integer)m.invoke(socket,new java.lang.Object [] {});
	    if (val.intValue() < size) {
		System.err.println("WARNING: ConnectionSapJcp recv buffer size only "+
				   val.intValue()+"/"+size);
		super.setRecvBufSize(size);
		maxReceiveWindow = size / (2*maxFragmentSize);
	    }
	}
	catch(Exception e)
	{
		System.err.println("Warning: ConnectionSapJcp.setRecvBufSize " + e);
	}
}

/**
* setKeepAliveTime
* @param timeMs
*/
public
void setKeepAliveTime(int timeMs)
{
	super.setKeepAliveTime(timeMs);
	keepAliveTimer.setTimeout(timeMs);
}

/**
* setConnectionTimeout
* @param timeoutMs
*/
public
void setConnectionTimeout(int timeoutMs)
{
	super.setConnectionTimeout(timeoutMs);
	timeoutTimer.setTimeout(timeoutMs);
}

/**
* resetConnectionTimeoutFlag
* @return int
*/
public
int resetConnectionTimeoutFlag()
{
	timeoutTimer.setTimer();
	return StatusValues.STATUS_OK;
}

/**
* setMaximumFragmentSize
* @param size
*/
public
void setMaximumFragmentSize(int size)
{
	maxFragmentSize = size;

	setSendBufSize(sendBufSize);
	setRecvBufSize(recvBufSize);
}

/**
* setMinimumRetransmissionTimeout
* @param timeoutMs
*/
public
void setMinimumRetransmissionTimeout(int timeoutMs)
{
	minRetransmissionTimeout = timeoutMs;
}

/**
* setMaximumRetransmissionTimeout
* @param timeoutMs
*/
public
void setMaximumRetransmissionTimeout(int timeoutMs)
{
	maxRetransmissionTimeout = timeoutMs;
}

/**
* setConnectionStrengthHistorySize
* @param size
*/
public
void setConnectionStrengthHistorySize(int size)
{
	connectionStrengthHistorySize = size;
}

/**
* setDelayedAckTimeout
* @param timeoutMs
*/
public
void setDelayedAckTimeout(int timeoutMs)
{
	delayedAckTimer.setTimeout(timeoutMs);
}

/**
* setTimerGrainularity
* @param timeoutMs
*/
public
void setTimerGrainularity(int timeoutMs)
{	
	try
	{
		socket.setSoTimeout(timeoutMs);
	}
	catch(SocketException e)
	{
		System.err.println("ERROR: ConnectionSapJcp.setTimerGrainularity " + e);
	}
}

/**
* getConnectionStrength
* @return float
*/
public
float getConnectionStrength()
{
	return connectionStrength;
}

/**
* seqLessThan
* @param a
* @param b
* @return PRBool
*/
protected
boolean seqLessThan(int a, int b)
{return ((int) (a - b)) < 0;}

/**
* seqLessThanEqual
* @param a
* @param b
* @return PRBool
*/
protected
boolean seqLessThanEqual(int a, int b)
{return ((int) (a - b)) <= 0;}

/**
* seqGreaterThan
* @param a
* @param b
* @return PRBool
*/
protected
boolean seqGreaterThan(int a, int b)
{return ((int) (a - b)) > 0;}

/**
* seqGreaterThanEqual
* @param a
* @param b
* @return PRBool
*/
protected
boolean seqGreaterThanEqual(int a, int b)
{return ((int) (a - b)) >= 0;}

/**
* receiveThreadFunction
*/
public
void run()
{
	if (debug)
	    System.out.println("ConnectionSapJcp::receiveThreadFunction\n");
	
	// raise thread priority
	int pri = Thread.currentThread().getPriority();
	Thread.currentThread().setPriority(pri+1);

	while((socket != null) && (status == StatusValues.STATUS_OK))
	{
		// The packet we receive into.
		byte[] buf = new byte[maxFragmentSize];
		DatagramPacket packet = new DatagramPacket(buf, maxFragmentSize);	

		try
		{
		        printState("run/receive");

			// Read a packet.
			socket.receive(packet);

			/* *** artificial loss ***  */
			   if(receiveLossRate>0 &&
			      Math.random() > (1-receiveLossRate))
			{
				System.out.println("Dropping packet on receive");
				throw new IOException();
			}

			// Something has been read, restart timeout timer.
			timeoutTimer.setTimer();
			receivePacket(packet);
		}
		catch(InterruptedIOException e)
		{
			// Normal...
		}
		catch(IOException e2)
		{
		    if (debug) System.out.println("ConnectionSapJcp.run read failed\n");
			//break;
		}

		if(retransmissionTimer.tick()) retransmit();
		if(delayedAckTimer.tick())
		{
		    if (debug)
			System.out.println("ConnectionSapJcp.run Delayed ack timeout\n");
			ackNow();
		}
		if(timeoutTimer.tick())
		{
		    if (debug)
			System.out.println("ConnectionSapJcp.run: User timeout\n");
			abort();
		}
		if(keepAliveTimer.tick())
		{
		    if (debug)
			System.out.println("ConnectionSapJcp.run: Send keep alive\n");
 		        JcpFragment   f = makeFragment(null, JcpFlag.FLAG_ACK /* !!!! */);
			outputFragment(f);
			keepAliveTimer.setTimer();
		}
	}
	if (debug)
	    System.out.println("ConnectionSapJcp::receiveThreadFunction exiting\n");
}

/**
* makeFragment
* @param data
* @param flags
* @return JcpFragment*
*/
protected
JcpFragment  makeFragment(Mbuf  data, int flags)
{
	// If NULL data, make header only JcpFragment.	
	if(data == null)
	{
		data = new Mbuf(JcpPacketHeader.getSize(), JcpPacketHeader.getSize());
		data.setUsed(JcpPacketHeader.getSize());
	}

	// Build JcpFragment.		
	JcpFragment  f = new JcpFragment(data);

	// Build header.
	f.getHeader().setSAddr(localAddress);
	f.getHeader().setSPort(localPort);
	f.getHeader().setFlags(flags);

	// Set sequence numbers. These are rewritten for
	// fragments queued for sending.
	f.getHeader().setSeq(sendNextSeq);
	f.getHeader().setRSeq(sendLastRSeq);
	
	return f;
}

/**
* output
* @return PRBool
*/
protected
boolean output()
{
	boolean fragmentsSent = false;

	// Get exclusive access to unsent and unacked fragments.
	synchronized(this) 
	{
	
	    // cmg - check for treatAsUnsent in unackedFragments,
	    // indicating that we are slow starting following a retransmission
	    // timeout
	    for (int i=0; i<unackedFragments.size(); i++) {
		JcpFragment currentFragment = (JcpFragment) unackedFragments.elementAt(i);
		if (!currentFragment.treatAsUnsent)
		    continue;
		// send allowed? fragment is within sending window
		if (currentFragment.getHeader().getSeq() - sendLastAck <
		    ((sendWindow)<(conjestionWindow)?(sendWindow):(conjestionWindow))) {
		    
		    if (debugRetrans) {
			//System.out.println("ConnectionSapJcp::output: retransmit on post-timeout slow start");
			printState("RESEND-SLOWSTART("+(currentFragment.getHeader().getSeq())+")", debugRetrans);
		    }
		    currentFragment.treatAsUnsent = false;
		    // Send JcpFragment.
		    outputFragment(currentFragment);

		    if (debugRate) 
			printRateState("output-retransmit("+(currentFragment.getHeader().getSeq())+")");

		    // Clear ack flag.
		    delayedAckTimer.cancelTimer();
		    fragmentsSent = true;	
		}
	    }

	// Send packets while current sequence number is less than window.
	while(unsentFragments.size() > 0)
	{
		JcpFragment currentFragment = (JcpFragment) unsentFragments.elementAt(0);

		// If current sequence number is greater than window stop.
		if(! sendAllowed(currentFragment)) {
		    if (debugRate) 
			printRateState("output-not-allowed("+(currentFragment.getHeader().getSeq())+")");
		    break;
		} else {
		    if (debugRate) 
			printRateState("output-send("+(currentFragment.getHeader().getSeq())+")");
		}

		// See if we need to calculate round trip time.
		if(!roundTripTimer.isSet())
		{
			roundTripTimer.startTimer(currentFragment.getHeader().getSeq());

			if (debug)
			    System.out.println("ConnectionSapJcp::outputFragment: Measuring round trip of " + String.valueOf(currentFragment.getHeader().getSeq()) + '\n');
		}

		// Increment sequence number if not pure ACK.
		// Pure ACKs are not queued for sending, so no test is needed.
		// NOTE: This is correct. A JcpFragment following a pure
		// ack should have the same sequence number as the ack.
		currentFragment.getHeader().setSeq(sendNextSeq++);
			
		// Increment reliable sequence number if reliable.
		// NOTE: This JcpFragment gets incremented sequence number.
		// following unreliable fragments will share this RSeq value.
		if((currentFragment.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0)
		{
			sendLastRSeq++;
		}
		currentFragment.getHeader().setRSeq(sendLastRSeq);

		// Send JcpFragment.
		outputFragment(currentFragment);

		// Update connection strength.
		connectionStrength = (1 + (connectionStrength * (connectionStrengthHistorySize - 1))) / connectionStrengthHistorySize;

		if (debug)
		    System.out.println("ConnectionSapJcp::outputFragment: Connection Strength: " + String.valueOf(connectionStrength) + '\n');

		// Update next sequence number.
		sendNextSeq = currentFragment.getHeader().getSeq() + 1;

		unsentFragments.removeElement(currentFragment);

		if((currentFragment.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0)
		{
			// Reliable JcpFragment, add to unacked queue for retransmission.
 		        currentFragment.treatAsUnsent = false;
			unackedFragments.addElement(currentFragment);
			sendLastRSeq = currentFragment.getHeader().getRSeq();
		}
		
		// Clear ack flag.
		delayedAckTimer.cancelTimer();
		fragmentsSent = true;	
	}

	if( /*fragmentsSent &&*/  (unsentFragments.size() + unackedFragments.size()) < maxUnsentFragments)
	{
		notifyAll();
	}
	
	} // synchronized;

	return fragmentsSent;
}

/**
* outputFragment
* @param f
*/
protected
void outputFragment(JcpFragment  f)
{
	// Fill in acknowledge and window fields. These may have
	// changed since the JcpFragment was created

	// Tell peer which packet is expected next.
	f.getHeader().setAck(receiveNextSeq);

	// Tell peer which if any packets we have received with a higher sequence number
	if (receivedFragments.size()==0)
	    f.getHeader().setFlags(f.getHeader().getFlags() & (~JcpFlag.FLAG_SACK)); 
	else {
	    JcpFragment fsack= (JcpFragment)receivedFragments.elementAt(0);
	    f.getHeader().setSack(fsack.getHeader().getSeq());
	    f.getHeader().setFlags(f.getHeader().getFlags() | JcpFlag.FLAG_SACK); 
	}

	// Tell peer how many packets we can receive.
	// - keep track of what we think they should know
	f.getHeader().setSWind(advertisedReceiveWindow=getReceiveWindow());
		
	// check/update highestSeq - highest seq no sent - rfc2582 NewReno
	if (seqGreaterThan(f.getHeader().getSeq(), highestSeq))
	    highestSeq = f.getHeader().getSeq();

	if (debug) {
	    System.out.println("ConnectionSapJcp::outputFragment\n");
	    f.printHeader();
	}
	if (debug) {
	    System.out.print("sendNext " + String.valueOf(sendNextSeq) + " lastAck " + String.valueOf(sendLastAck));
	    System.out.print(" conjestionWindow " + String.valueOf(conjestionWindow) + " sendWindow ");
	    System.out.println(String.valueOf(sendWindow));
	}

	// RFC 2988 5.1 Every time a packet containing data is sent 
	// (including a retransmission), if the timer is not running,
	// start it running so that it will expire after RTO seconds
	// (for the current value of RTO).
	// NOTE: Want to retransmit SYNs and FINs too. Just don't
	// retransmit pure ACKs (not that they should be queued).
	// NOTE: Only set timer if JcpFragment is reliable.
	if(((f.getHeader().getFlags() & ~JcpFlag.FLAG_SACK)!= JcpFlag.FLAG_ACK || (f.getDataLength() > 0)) 
		&& (!retransmissionTimer.isSet()) && (f.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0)
	{
		retransmissionTimer.setTimer();
	}

	// Reset keep alive timer. Only need to send keep alive fragments
	// when data is not being sent.
	keepAliveTimer.setTimer();
	
	try
	{
	InetAddress addr = InetAddress.getByName(""+((remoteAddress >> 24) & 0xff)+
					       "."+((remoteAddress >> 16) & 0xff) +
					       "."+((remoteAddress >> 8) & 0xff) +
					       "."+((remoteAddress) & 0xff));

	// Send packet (avoid sign extension problem for tall default ports)
	DatagramPacket packet = new DatagramPacket(f.getData().getBuf(), f.getData().getUsed(), addr, ((int)remotePort & 0xffff));
	printState("outputFragment/send");
	if (debug2) {
	    synchronized(System.out) {
		System.out.print("SEND: ");
		f.printHeader();
	    }
	}
	/* *** artificial loss ***  */
	if(sendLossRate>0 &&
	   Math.random() > (1-sendLossRate)) {
		System.out.println("Dropping packet on send");
	}
	else {
	    socket.send(packet);
	    // limit send rate?!
	    try { 
		Thread.sleep(1);
	    } catch(Exception e) {}
	}
	}
	catch(UnknownHostException e) {
	}
	catch(NoRouteToHostException e) { /* ignore -happens when temporarily out of wavelan range */ }
	catch(IOException e2)
	{
		System.err.println("ERROR: ConnectionSapJcp.outputFragment " + e2);
		abort();
	}
}

/**
* receivePacket
* @param packet
* now package scope for ServerSapUdp dup SYN hack
*/
void receivePacket(DatagramPacket  packet)
{
	// Build JcpFragment.
	Mbuf m = new Mbuf();
	m.setData(packet.getData(), packet.getLength());
	m.setHeaderSize(JcpPacketHeader.getSize());
	JcpFragment  rec = new JcpFragment(m);

	if (debug) {
	    System.out.println("ConnectionSapJcp::receivePacket\n");
	    rec.printHeader();
	}
	if (debug2) {
	    synchronized(System.out) {
		System.out.print("RECV: ");
		rec.printHeader();
	    }
	}

	if (debug)
	    System.out.println("receiveLastRSeq " + String.valueOf(receiveLastRSeq) + " receiveNextSeq " + String.valueOf(receiveNextSeq) + " receiveWindow " + String.valueOf(getReceiveWindow()) + '\n');
	
	// Process JcpFragment.
	processFragment(rec);
		
	// Call output, which may be able to send more data.
	output();
}

/**
* processFragment
* @param f
*/
protected
void processFragment(JcpFragment  f)
{
	int status;

	// Copy header, as data processing sometimes deletes JcpFragment.
	JcpPacketHeader header =  (f.getHeader());

	switch(state)
	{
	case STATE_LISTEN:
		
		if((f.getHeader().getFlags() & JcpFlag.FLAG_ACK) != 0)
		{
			// Send reset.
			reset();
		}
		else if((f.getHeader().getFlags() & JcpFlag.FLAG_SYN) != 0)
		{
		    	if (debug)
			    System.out.print("ConnectionSapJcp::processFragment: STATE_LISTEN: SYN recieved, moving to STATE_SYN_RCVD\n");

			state = STATE_SYN_RCVD;
			remoteAddress = f.getHeader().getSAddr();
			remotePort = f.getHeader().getSPort();
			receiveNextSeq = f.getHeader().getSeq() + 1;
			receiveLastRSeq = f.getHeader().getRSeq();
			sendWindow = f.getHeader().getSWind();
			
			synack();
		}
		break;
	case STATE_SYN_SENT:
		if((f.getHeader().getFlags() & JcpFlag.FLAG_ACK) != 0)
		{
			if(seqGreaterThan(f.getHeader().getAck(),sendNextSeq))
			{
				// Unacceptable ACK -> send reset.
				reset();
			}
			else if((f.getHeader().getFlags() & JcpFlag.FLAG_RST) != 0)
			{
				// Acceptable or no ACK and SYN.

			    if (debug)
				System.out.println(String.valueOf("ConnectionSapJcp) + String.valueOf(processFragment: Connection reset\n"));
				state = STATE_CLOSED;
				status = StatusValues.STATUS_ERROR;
			}
		}
		if((f.getHeader().getFlags() & JcpFlag.FLAG_SYN) != 0)
		{
			receiveNextSeq = f.getHeader().getSeq() + 1;
			receiveLastRSeq = f.getHeader().getRSeq();
			
			sendWindow = f.getHeader().getSWind();
			conjestionWindow = 1;
	
			// HACK: Set remote address/port from received SYN. 
			// Allows SYN to be sent from port other than listener
			// to implement socket style semantics.
			remoteAddress = f.getHeader().getSAddr();
			remotePort = f.getHeader().getSPort();

			if((f.getHeader().getFlags() & JcpFlag.FLAG_ACK) != 0)
			{
				// Synack received.	
			    if (debug)
				System.out.print("ConnectionSapJcp::processFragment: STATE_SYN_SENT: SYNACK recieved, moving to STATE_ESTABLISHED\n");	
				
				boolean synAcked;

				// Get exclusive access to unackedFragments
				synchronized(this) 
				{

					JcpFragment  unackedsyn = (JcpFragment) unackedFragments.elementAt(0);
					
					// Check it's ackonologing our syn packet, which should
					// be the only unacknowledged packet.
					synAcked = f.getHeader().getAck() == unackedsyn.getHeader().getSeq() + 1;
					if(synAcked)
					{
						sendLastAck = f.getHeader().getAck();

						state = STATE_ESTABLISHED;

						// Remove our syn from the unacknowledged queue.
						unackedFragments.removeElementAt(0);
					}

				} // synchronized;

				
				if(synAcked)
				{	
					// Cancel retransmission of syn packet.
					retransmissionTimer.cancelTimer();

					// Acknowledge the synack.
					ackNow();
				}
			}
			else
			{
				
			    if (debug)
				System.out.print("ConnectionSapJcp::processFragment: STATE_SYN_SENT: SYN recieved, moving to STATE_SYN_RCVD\n");
				state = STATE_SYN_RCVD;

				// Set ACK flag on queued syn and resend. Don't create a new SYNACK as it will
				// consume more sequence space and result in 2 SYNs that need to be acked, the queued SYN
				// and the new SYNACK.
				JcpFragment  unackedsyn = (JcpFragment) unackedFragments.elementAt(0);
				unackedsyn.getHeader().setFlags(unackedsyn.getHeader().getFlags() | JcpFlag.FLAG_ACK);
				outputFragment(unackedsyn);
			}
		}
		break;
	case STATE_SYN_RCVD:
		if((f.getHeader().getFlags() & JcpFlag.FLAG_ACK) != 0) 
		{
		  if(seqLessThan(sendLastAck, f.getHeader().getAck()) && seqLessThanEqual(f.getHeader().getAck(), sendNextSeq))
		  {
		      if (debug)
			System.out.print("ConnectionSapJcp::processFragment: STATE_SYN_RCVD: ACK recieved, moving to STATE_ESTABLISHED\n");
			state = STATE_ESTABLISHED;
		  }

		  // If this is simultaneous open, ACK could be SYNACK. Remove SYN flag, so connection doesn't reset.
		  //f.getHeader().setFlags(f.getHeader().getFlags() & ~JcpFlag.FLAG_SYN);
		  receiveFragment(f);
		  break;
		}
	case STATE_CLOSE_WAIT:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		break;
	case STATE_ESTABLISHED:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		if((header.getFlags() & JcpFlag.FLAG_FIN) != 0)
		{
		    if (debug)
			System.out.print("ConnectionSapJcp::processFragment: STATE_ESTABLISHED: FIN recieved, moving to STATE_CLOSE_WAIT\n");
			ackNow();
			state = STATE_CLOSE_WAIT;
		}
		break;
	case STATE_FIN_WAIT_1:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		if((header.getFlags() & JcpFlag.FLAG_FIN) != 0) 
		{
			// RFC 793 step 8
			// If our FIN has been ACKed (perhaps in this segment), then
			// enter TIME-WAIT, start the time-wait timer, turn off the other
			// timers; otherwise enter the CLOSING state.

			if(((header.getFlags() & JcpFlag.FLAG_ACK) != 0) && (header.getAck() == sendNextSeq)) 
			{
			    if (debug)
				System.out.print("ConnectionSapJcp::processFragment: STATE_FIN_WAIT_1: FIN ACKed, moving to STATE_TIME_WAIT\n");
				ackNow();
				state = STATE_TIME_WAIT;
				
				retransmissionTimer.cancelTimer();
			} 
			else 
			{
			    if (debug)
				System.out.print("ConnectionSapJcp::processFragment: STATE_FIN_WAIT_1: received FIN, moving to STATE_CLOSING\n");
				ackNow();
				state = STATE_CLOSING;
			}
		}
		else if(((header.getFlags() & JcpFlag.FLAG_ACK) != 0) && (header.getAck() == sendNextSeq)) 
		{
			// RFC 793 step 5
			// If our FIN is now acknowledged then enter FIN-WAIT-2 and continue
			// processing in that state.
		    if (debug)
			System.out.print("ConnectionSapJcp::processFragment: STATE_FIN_WAIT_1: FIN_ACKed, moving to STATE_FIN_WAIT_2\n");
			state = STATE_FIN_WAIT_2;
		}
		break;
	case STATE_FIN_WAIT_2:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		if((header.getFlags() & JcpFlag.FLAG_FIN) != 0) 
		{
			// RFC 793 step 6. URG processing. TODO...

			// RFC 793 step 8.
			// Enter the TIME-WAIT state.  Start the time-wait timer, turn
			// off the other timers.
		    if (debug)
			System.out.print("ConnectionSapJcp::processFragment: STATE_FIN_WAIT_2: received FIN, moving to STATE_TIME_WAIT\n");
			state = STATE_TIME_WAIT;
			
			retransmissionTimer.cancelTimer();

			ackNow(); 
		}
		break;
	case STATE_CLOSING:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		if((header.getFlags() & JcpFlag.FLAG_ACK) != 0 && header.getAck() == sendNextSeq) 
		{
			// RFC 793 Step 5 If the ACK acknowledges our FIN then enter the TIME-WAIT state,
			// otherwise ignore the segment.
		    if (debug)
			System.out.print("ConnectionSapJcp::processFragment: STATE_CLOSING: FIN acked, moving to STATE_TIME_WAIT\n");
		  state = STATE_TIME_WAIT;
		  ackNow();
		}
		break;
	case STATE_LAST_ACK:
		if(receiveFragment(f) != StatusValues.STATUS_OK) break;
		if((header.getFlags() & JcpFlag.FLAG_ACK) != 0 && header.getAck() == sendNextSeq) 
		{
		  // If our FIN is now acknowledged,
		  // delete the TCB, enter the CLOSED state, and return.
		  state = STATE_CLOSED; 

		  // Can't delete TCP, set state to closed.
		}
		break;
	case STATE_TIME_WAIT:

		// The only thing that can arrive in this state is a
		// retransmission of the remote FIN.  Acknowledge it, and restart
		// the 2 MSL timeout.

		break;
	}
}

/**
* receiveFragment
* @param f
* @return int
* Performs common processing for segment arrivals in SYN-RECEIVED STATE, 
* ESTABLISHED STATE, FIN-WAIT-1 STATE,IN-WAIT-2 STATE, CLOSE-WAIT STATE
* CLOSING STATE, LAST-ACK STATE and TIME-WAIT STATE as described in RFC 793
*/
protected
int receiveFragment(JcpFragment  f)
{
	// Check acceptability of JcpFragment.
	// RFC 793 Step 1: Test sequence number for acceptability.
	boolean acceptable = false;
	if(getReceiveWindow() > 0)
	{
		if(!(seqLessThanEqual(receiveNextSeq, f.getHeader().getSeq()) && 
			 seqLessThanEqual(f.getHeader().getSeq(), receiveNextSeq + getReceiveWindow())))
		{
		    if (debug) {
			System.out.print("ConnectionSapJcp::receiveFragment: Unacceptable JcpFragment: receiveNext ");
			System.out.print(String.valueOf(receiveNextSeq) + " receiveWindow " + String.valueOf(getReceiveWindow()));
			System.out.println(" JcpFragment " + String.valueOf(f.getHeader().getSeq()) + '\n');
		    }

			ackNow();
			return StatusValues.STATUS_ERROR;
		}
	}
	else
	{
		if(!((f.getDataLength() == 0) && (f.getHeader().getSeq() == receiveNextSeq)))
		{
		    if (debug) {
			System.out.print("ConnectionSapJcp::receiveFragment: Unacceptable JcpFragment: receiveNext ");
			System.out.print(String.valueOf(receiveNextSeq));
			System.out.println(" JcpFragment " + String.valueOf(f.getHeader().getSeq()));
		    }
			ackNow();
			return StatusValues.STATUS_ERROR;
		}
	}

	// RFC 793 Step 2: Check resets.
	if((f.getHeader().getFlags() & JcpFlag.FLAG_RST) != 0)
	{
	    if (debug)
		System.out.println("ConnectionSapJcp::receiveFragment: Received RST fragment in state " + state);
		state = STATE_CLOSED;
		status = StatusValues.STATUS_ERROR;
		return StatusValues.STATUS_ERROR;
	}

	// RFC 793 third check security and precedence. TODO...

    // RFC 793 fourth, check the SYN bit.
	if((f.getHeader().getFlags() & JcpFlag.FLAG_SYN) != 0)
	{
	    if (debug)
		System.out.println("ConnectionSapJcp::receiveFragment: Received SYN fragment in state " + state);
		reset();
		state = STATE_CLOSED;
		status = StatusValues.STATUS_ERROR;
		return StatusValues.STATUS_ERROR;
	}	

	// RFC 793 fifth check the ACK field.

	// Handle acknowledgements.
	if((f.getHeader().getFlags() & JcpFlag.FLAG_ACK) != 0)
	{		
	        synchronized(this) {
		  
		// Update window if JcpFragment has a new sequence number,
		// has the same sequence number but acknowledges new data
		// or acknowledges the same data but increases the window.
		// - tentative move out from new data block - cmg, since seem not
		// to recover sending window in high-throughput tests
		if(seqLessThan(sendWindow1, f.getHeader().getSeq()) ||
		   (sendWindow1 == f.getHeader().getSeq() &&
		    seqLessThan(sendWindow2, f.getHeader().getAck()) ||
		    (sendWindow2 == f.getHeader().getAck() && 
		     f.getHeader().getSWind() > sendWindow)))	{
		  
		  sendWindow = f.getHeader().getSWind();
		  sendWindow1 = f.getHeader().getSeq();
		  sendWindow2 = f.getHeader().getAck();
		}

		// Monitor duplicate acknowledgements to perform fast retransmit/recovery.
		// If peer receives out of order fragments it will send acks with the same
		// ack number. If we recieve 3 acks with the same ack number, we assume
		// a JcpFragment has been lost and retransmit without waiting for retransmit
		// timeout.
		if(sendLastAck == f.getHeader().getAck()) 
		{


		  ++duplicateAcks;
		  if (debugCon)
		      System.out.println("ConnectionSapJcp::receiveFragment: duplicate acks " + String.valueOf(duplicateAcks) + '\n');

		  if(duplicateAcks >= 3 && unackedFragments.size() > 0) 
		  {
		      if (debugCon)
			System.out.println("ConnectionSapJcp::receiveFragment: fast retransmit duplicate acks " + String.valueOf(duplicateAcks) + '\n');

			if(! fastRetransmit) 
			{
			    if (debugRetrans) {
				//System.out.println("ConnectionSapJcp::receiveFragment: fast retransmit triggered by duplicate acks");
				printState("BEGIN-FAST-RETRANS("+((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")", debugRetrans);
			    }

			  JcpFragment retrans = (JcpFragment) unackedFragments.elementAt(0);
			  if (retrans.getHeader().getSeq()!=f.getHeader().getAck()) {
			      System.err.println("ERROR: fast retransmit sends wrong packet: ack="+
						 f.getHeader().getAck()+" unacked="+
						 retrans.getHeader().getSeq());
			  }
			  retransmitFragment((JcpFragment) unackedFragments.elementAt(0));

			  // Set slowStartThreshold to max (FlightSize / 2, 2) RFC 2581 1999			  
			  slowStartThreshold = (((sendNextSeq - sendLastAck) / 2)>(2)?((sendNextSeq - sendLastAck) / 2):(2));

			  // NewReno rfc2582
			  recover = highestSeq;
			  firstPartialAck = true;

			  conjestionWindow = slowStartThreshold + 3;
			  fastRetransmit = true;

			  if (debugRate) 
			      printRateState("fast-retransmit("+
					     ((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")");

			  // try resetting the retransmission timer here too - cmg
			  //ornot; retransmissionTimer.setTimer();
			} 
			else 
			{	
			        // doing fast retransmit
				// cmg - dangerous non-standard bonus resend(s)
				if ((duplicateAcks % 3)==0) {
				    // resend once for each 3 acks?!
				    JcpFragment retrans = (JcpFragment) unackedFragments.elementAt(0);
				    if (retrans.getHeader().getSeq()!=f.getHeader().getAck()) {
					System.err.println("ERROR: fast retransmit (cmg extra) sends wrong packet: ack="+
							   f.getHeader().getAck()+" unacked="+
							   retrans.getHeader().getSeq());
				    }
				    retransmitFragment((JcpFragment) unackedFragments.elementAt(0));
				    if (debugRate) 
					printRateState("fast-retransmit-cmg("+
						       ((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")");

				} else
				    conjestionWindow++;

			}
		  }
		}
		else 

		if(seqLessThan(sendLastAck, f.getHeader().getAck()) &&
				seqLessThanEqual(f.getHeader().getAck(), sendNextSeq))
		{
		        // The ACK acknowledges new data.

			// Reset the "IN Fast Retransmit" flag, since we are no longer
			// in fast retransmit. Also reset the congestion window to the
			// slow start threshold.

		        boolean partialAck = false;
			if(fastRetransmit && 
			   seqLessThanEqual(f.getHeader().getAck(),recover) ||
			   ((f.getHeader().getFlags() & JcpFlag.FLAG_SACK)!=0)) {

			    // plus hacky use of pseudo-SACK - keep going with partial acks
			    // above recover, up to SACK

				// Partial ACK
				// RFC2582 NewReno
 			        partialAck = true;

			} else if (fastRetransmit) {
				// ACKs all outstanding packets at time of duplicate ACK

				fastRetransmit = false;
				conjestionWindow = slowStartThreshold;
				duplicateAcks = 0;
				if (debugRate) 
				    printRateState("end-fast-retransmit");
			} else {
			        // normal
			        duplicateAcks = 0;
			}
			

			try
			{
				// Reset the retransmission time-out.
				// RFC 2988 5.3
				int timeout = roundTripTimer.getAverage() + ((socket.getSoTimeout())>(4 * roundTripTimer.getVariation())?(socket.getSoTimeout()):(4 * roundTripTimer.getVariation()));

				// RFC 2988 2.4
				// Whenever RTO is computed, if it is less than 1 second then the
				// RTO SHOULD be rounded up to 1 second.
				timeout = ((timeout)>(minRetransmissionTimeout)?(timeout):(minRetransmissionTimeout));
				timeout = ((timeout)<(maxRetransmissionTimeout)?(timeout):(maxRetransmissionTimeout));

				if (!partialAck || firstPartialAck) {
				    // NewReno - only reset retransmission timer on first partial ack
				    retransmissionTimer.setTimeout(timeout);
				    retransmissionTimer.setTimer();
				    firstPartialAck = false;
				}
			}
			catch(SocketException e)
			{
				System.err.println("ERROR: ConnectionSapJcp.receiveFragment " + e);
			}
			
			// Update the congestion control variables (conjestionWindow and slowStartThreshold)
			if(state != STATE_SYN_RCVD) 
			{
			        if (partialAck) {
				    // Deflate the
				    // congestion window by the amount of new data acknowledged, then
				    // add back one MSS
				    conjestionWindow -= (f.getHeader().getAck()-sendLastAck);
				    if (conjestionWindow<1)
					conjestionWindow = 1;
				    conjestionWindow++;
				    if (debugCon)
					System.out.println("ConnectionSapJcp::receiveFragment: partialAck: conjestion window " + String.valueOf(conjestionWindow) + '\n');
				}
				else if(conjestionWindow < slowStartThreshold) 
				{
					// Slow start -> increase conjestion window with each ack.
					// This creates an exponential increase with time as the 
					// larger conjestion window results in greater ack frequency
					// and so greater opening frequency.
					conjestionWindow++;
					if (debugCon)
					    System.out.println("ConnectionSapJcp::receiveFragment: slow start: conjestion window " + String.valueOf(conjestionWindow) + '\n');
				}
				else 
				{
					// Conjestion avoidance -> increase conjestion window.
					// This creates an additive increase with time as the
					// greater the window and so ack frequency, the less
					// the window is increased with each ack.
					conjestionWindow += (1 / conjestionWindow);
					if (debugCon)
					    System.out.println("ConnectionSapJcp::receiveFragment: conjestion avoidance: conjestion window " + String.valueOf(conjestionWindow) + '\n');
				}
			}

			// Reset the fast retransmit variables.
			sendLastAck = f.getHeader().getAck();

			// Remove acknowledged packets from unacknowledged queue.
			while((unackedFragments.size() > 0) && 
					seqLessThan(((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq(), f.getHeader().getAck()))
			{
				unackedFragments.removeElementAt(0);
			}

			// Cancel retransmission timer if no unacknowledged fragments are outstanding.
			// RFC 2988 5.2
			if(unackedFragments.size() == 0) retransmissionTimer.cancelTimer();
			
			// NewReno rfc2582 - resend next packet
			if (partialAck && unackedFragments.size()>0) {
			    if (debugRetrans) {
				//System.out.println("ConnectionSapJcp::receiveFragment: Retransmitting JcpFragment partialAck");
				printState("PARTIAL-ACK-RETRANS("+((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")",debugRetrans);
			    }

			    retransmitFragment((JcpFragment) unackedFragments.elementAt(0));

			    if (debugRate) 
				printRateState("partial-ack-retransmit("+
					       ((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")"+
					       " ack="+f.getHeader().getAck()+" recover="+recover+" sack="+
					       ((f.getHeader().getFlags() & JcpFlag.FLAG_SACK)!=0 ? 
						""+f.getHeader().getSack() : "NO"));
			}

			// Do round trip calculations if test JcpFragment has been acknowledged.
			// Calculations from RFC 2988	
			if(roundTripTimer.isSet() && seqLessThan(roundTripTimer.getSequenceNumber(), f.getHeader().getAck()))
			{
				try
				{
					int roundTripTime = roundTripTimer.stopTimer();
					if (debug)
					    System.out.println("round trip time estimate " + String.valueOf(roundTripTime) + '\n');

					// Constants from RFC 2988.
					int timeout = roundTripTimer.getAverage() + ((socket.getSoTimeout())>(4 * roundTripTimer.getVariation())?(socket.getSoTimeout()):(4 * roundTripTimer.getVariation()));

					// RFC 2988 2.4
					// Whenever RTO is computed, if it is less than 1 second then the
					// RTO SHOULD be rounded up to 1 second.
					timeout = ((timeout)>(minRetransmissionTimeout)?(timeout):(minRetransmissionTimeout));
					timeout = ((timeout)<(maxRetransmissionTimeout)?(timeout):(maxRetransmissionTimeout));

					retransmissionTimer.setTimeout(timeout);
					if (debug)
					    System.out.println("retransmission timeout " + String.valueOf(retransmissionTimer.getTimeout()) + '\n');
				}
				catch(SocketException e)
				{
					System.err.println("ERROR: ConnectionSapJcp.receiveFragment " + e);
				}
			}
		}
		else if(seqGreaterThan(f.getHeader().getAck(), sendNextSeq))
		{
			// RFC 793 If the ACK acks
	        // something not yet sent (SEG.ACK > SND.NXT) then send an ACK,
			// drop the segment, and return.
		    if (debugCon)
			System.out.println("ConnectionSapJcp::receiveFragment: ACK received for unsent JcpFragment: sendNext " + String.valueOf(sendNextSeq) + " ack " + String.valueOf(f.getHeader().getAck()) + '\n');
			ackNow();
			return StatusValues.STATUS_ERROR;
		} else {
		    if (debugCon)
		      System.out.println("ConnectionSapJcp::receiveFragment: unhandled ack " + f.getHeader().getAck()+" vs "+sendNextSeq);
		}

		
		} // synchronized;
	} 
	
	// Handle data fragments.
	if(f.getData().getUsed() > JcpPacketHeader.getSize())
	{
		// Process JcpFragment if it is within receive window.
		if(seqGreaterThanEqual(f.getHeader().getSeq(), receiveNextSeq) && 
			seqLessThan(f.getHeader().getSeq(), receiveNextSeq + getReceiveWindow()))
		{
			
			addReceivedFragment(f);
			if (debug)
			    System.out.println("Adding fragment " + receivedFragments.size());

			// Try to extract objects from received queue.
			// List is traversed and sequence numbers compared to last stored sequence
			// numbers. Once last stored sequencer numbers are reached, newIf a gap in reliable sequence numbers is found, traversal stops. 
			// If a gap in normal sequence numbers is found, traversal continues, but 
			// unreliable fragments before gap are discarded. If unbroken spans of fragments 
			// contain complete objects, they are moved to completedObjectFragments list. 
			// When traversal stops, the JcpFragment following the highest encountered JcpFragment 
			// is requested with an ack. This is either a missing reliable JcpFragment, or the 
			// JcpFragment following the highest received unreliable JcpFragment.
			// NOTE: Some of this work could be done in readObject.
			// NOTE: Some of this work could be done in readObject.
			int index, objectStart = 0, objectEnd = 0;
			boolean inObject = false;
			boolean reliable = false; // In reliable or unreliable span.
			Vector objectStartPoints = new Vector(); // Stack of object starts for nested objects.

			for(index = 0; index < receivedFragments.size(); index++)
			{
				JcpFragment  rFrag = (JcpFragment) receivedFragments.elementAt(index);
				if (debug) {
				    System.out.print("ConnectionSapJcp::receiveFragment: " + String.valueOf(rFrag.getHeader().getRSeq()) + String.valueOf('.') + String.valueOf(rFrag.getHeader().getSeq()));
				    if((rFrag.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0) System.out.print(" RELIABLE");
				    if((rFrag.getHeader().getFlags() & JcpFlag.FLAG_START) != 0) System.out.print(" START");
				    if((rFrag.getHeader().getFlags() & JcpFlag.FLAG_END) != 0) System.out.print(" END");
				    System.out.println();
				}

				// Check sequence numbers.

				// If reliable sequence number is next reliable and the JcpFragment
				// is not reliable there is a missing reliable. If reliable sequence 
				// number is greater than next reliable there is a missing reliable.
				// If reliable sequence number is equal to last reliable and
				// sequence number is not equal to next sequence number there is
				// a missing unreliable. If reliable sequence number is less than
				// last reliable sequence nuber JcpFragment is an old reliable JcpFragment
				// waiting for further fragments to complete the object.

				if(((rFrag.getHeader().getRSeq() == (receiveLastRSeq + 1)) && 
					!((rFrag.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0)) ||
					seqGreaterThan(rFrag.getHeader().getRSeq(), (receiveLastRSeq + 1)))
				{
					// Gap in reliable sequence numbers found,
					// need to wait till gap is filled.

					// Ack now to signal gap.
					ackNow();
					return StatusValues.STATUS_OK;
				}
				else if((rFrag.getHeader().getRSeq() == receiveLastRSeq) && 
						(rFrag.getHeader().getSeq() != receiveNextSeq) &&
						!((rFrag.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0))
				{
					// Reliable sequence number equal to last JcpFragment, gap in sequence number.
				    if (debug)
					System.out.println("ConnectionSapJcp::receiveFragment: Gap in sequence numbers, expected " + String.valueOf(receiveNextSeq) + " found " + String.valueOf(rFrag.getHeader().getSeq()) + '\n');
					
					if(index != 0)
					{
						int unreliableIndex;
						unreliableIndex = index;
						unreliableIndex--;
						while(unreliableIndex != 0)
						{
							JcpFragment  unreliable = (JcpFragment) receivedFragments.elementAt(unreliableIndex);
							
							// Stop if we've reached a reliable JcpFragment.	
							if((unreliable.getHeader().getFlags() & JcpFlag.FLAG_RELIABLE) != 0) break;

							// Old, unreliable JcpFragment before gap: delete.
							if (debug)
							    System.out.println("ConnectionSapJcp::receiveFragment: Discarding JcpFragment " + String.valueOf(unreliable.getHeader().getRSeq()) + String.valueOf('.') + String.valueOf(unreliable.getHeader().getSeq()));
							receivedFragments.removeElementAt(unreliableIndex);
							unreliableIndex--;
							index--;
						}
					}

					// Clear in object flag.
					inObject = false;

					// Empty stack of object start points.
					if (debug)
					    System.out.println("ConnectionSapJcp::receiveFragment: Gap in sequence numbers when " + String.valueOf(objectStartPoints.size()) + " objects deep\n");
					objectStartPoints.removeAllElements();
				}

				// Update sequence numbers if the last furthest traversal has been passed.
				if(seqGreaterThanEqual(rFrag.getHeader().getSeq(), receiveNextSeq))
				{
					receiveNextSeq = rFrag.getHeader().getSeq() + 1;
					receiveLastRSeq = rFrag.getHeader().getRSeq();
				}

				if((rFrag.getHeader().getFlags() & JcpFlag.FLAG_START) != 0)
				{
					// If already in object, push old object start onto
					// stack and process nested object.
					if(inObject)
					{
						objectStartPoints.insertElementAt(new Integer(objectStart), 0);
						if (debug)
						    System.out.println("ConnectionSapJcp::receiveFragment: Processing nested object, depth " + String.valueOf(objectStartPoints.size()));
					}
					else
					{
						inObject = true;
					}
					objectStart = index;	
				}
				if(((rFrag.getHeader().getFlags() & JcpFlag.FLAG_END) != 0) && inObject)
				{
					objectEnd = index;

					// Complete object found, extract it.
					buildObject(objectStart, objectEnd);
					index -= ((objectEnd - objectStart) + 1);

					// If nested object is being processed, restore
					// old object start point.
					if(objectStartPoints.size() > 0)
					{
						objectStart = ((Integer) objectStartPoints.elementAt(0)).intValue();
						objectStartPoints.removeElementAt(0);
					}
					else
					{
						inObject = false;
					}
				}
				else if(!inObject)
				{
					// Old unreliable JcpFragment outside object: delete.
				    if (debug)
					System.out.println("ConnectionSapJcp::receiveFragment: Discarding JcpFragment " + String.valueOf(rFrag.getHeader().getRSeq()) + String.valueOf('.') + String.valueOf(rFrag.getHeader().getSeq()) + '\n');
					receivedFragments.removeElementAt(index);
					index--;
				}
			}

			// Ack received fragments.
			ack();

		} // Within window.

	}  // Data JcpFragment.
	else 
	{
	    // no data - could be an ack or a keepalive. 
	    // TCP can ignore it, but if the window was full of unreliable 
	    // packets then the sender may be stalled awaiting an ACK.
	    // So, if the sequence number indicates that we have only
	    // missed unreliable packets then we will update as if
	    // they had been received, 
	    // so that the sender will see a packet (e.g. keep alive)
	    // with the next ack number in (new functionality, also).
	    //  Ideally it would be nice to bring forward the next
	    // keepAlive packet, but that requires a Timer API change...
	    // - cmg.
	    if(seqGreaterThanEqual(f.getHeader().getSeq(), receiveNextSeq) && 
	       seqLessThan(f.getHeader().getSeq(), receiveNextSeq + getReceiveWindow())) {
		// in window
		if ((f.getHeader().getRSeq() == receiveLastRSeq) &&
		    seqGreaterThan(f.getHeader().getSeq(), receiveNextSeq)) {
		    if (debug) 
			System.out.println("ConnectionSapJcp::receiveFragment: synthesing ACK for missed unreliable fragments - from seq "+receiveNextSeq+" through "+f.getHeader().getSeq());

		    // next = fragment sequence number.
			// NOTE: f is a pure ACK or keepalive, so next data fragment will have the
			// same sequence number as ACKs to not consume sequence space.
		    receiveNextSeq = f.getHeader().getSeq();

			// Bring forward keep alive timer so keep alive with new next
			// sequence number is sent to sender ASAP to restart 
			// potentially stalled connection
		    keepAliveTimer.bringForward(100);
		}
	    }
	}
	    
	return StatusValues.STATUS_OK;
}

/**
* syn
*/
protected
void syn()
{
	if (debug)
	    System.out.print("ConnectionSapJcp::syn: SYN to " + String.valueOf(remoteAddress) + " " + String.valueOf(remotePort) + "\n");

	JcpFragment  f = makeFragment(null, JcpFlag.FLAG_SYN | JcpFlag.FLAG_RELIABLE);
	unsentFragments.addElement(f);
	output();
}

/**
* synack
*/
protected
void synack()
{
	if (debug)
	    System.out.print("ConnectionSapJcp::synack: SYNACK to " + String.valueOf(remoteAddress) + " " + String.valueOf(remotePort) + "\n");
	
	JcpFragment  f = makeFragment(null, JcpFlag.FLAG_SYN | JcpFlag.FLAG_ACK | JcpFlag.FLAG_RELIABLE);

	unsentFragments.addElement(f);
	
	output();
}

/**
* fin
*/
protected
void fin()
{
	if (debug)
	    System.out.print("ConnectionSapJcp::fin: FIN to " + String.valueOf(remoteAddress) + " " + String.valueOf(remotePort) + "\n");
	
	JcpFragment  f = makeFragment(null, JcpFlag.FLAG_FIN | JcpFlag.FLAG_RELIABLE);
	
	unsentFragments.addElement(f);
	
	output();
}

/**
* ack
*/
protected
void ack()
{

	if(true)
	{
		// Ack already delayed,
		// force one.
		ackNow();
	}
	else
	{
	    if (debug)
		System.out.println("ConnectionSapJcp::ack: Delaying ack\n");

		// No outstanding ack,
		// wait.
		delayedAckTimer.setTimer();
	}
}

/**
* ackNow
*/
protected
void ackNow()
{
	// Try to output data with ack.
	if(! output())
	{
		// If nothing output, send ack.
		JcpFragment  f = makeFragment(null, JcpFlag.FLAG_ACK);
		outputFragment(f);

		// Reset delayed ack flag.
		delayedAckTimer.cancelTimer();
	}
}

/**
* sendAllowed
* @param f
* @return PRBool
*/
protected
boolean sendAllowed(JcpFragment  f)
{

	return (sendNextSeq - sendLastAck) < ((sendWindow)<(conjestionWindow)?(sendWindow):(conjestionWindow));
}

/**
* retransmit
*/
protected
void retransmit()
{
	
        if (debugRetrans) {
	    //System.out.println("ConnectionSapJcp::retransmit: Retransmitting JcpFragment timeout " + String.valueOf(retransmissionTimer.getTimeout()));
	    printState("RETRANSMIT("+((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")", debugRetrans);
        }

	synchronized(this) {
    
	    // RFC 2988 5.4 Retransmit the earliest segment that has not been acknowledged
	    // by the TCP receiver.
	    retransmitFragment((JcpFragment) unackedFragments.elementAt(0));
	    
	    // cmg - mark other unacked fragments treatAsUnsent to allow slow start on them
	    for (int i=1; i<unackedFragments.size(); i++) {
		JcpFragment frag = (JcpFragment) unackedFragments.elementAt(i);
		frag.treatAsUnsent = true;
	    }


	// RFC 2988 5.5 The host MUST set RTO <- RTO * 2 ("back off the timer").	
	retransmissionTimer.setTimeout((int) ((retransmissionTimer.getTimeout() *2 ) < (maxRetransmissionTimeout)?(retransmissionTimer.getTimeout() * 2):(maxRetransmissionTimeout)));

	// RFC 2988 (5.6) Start the retransmission timer, such that it expires after RTO
    // seconds (for the value of RTO after the doubling operation
    // outlined in 5.5).    
	retransmissionTimer.setTimer();

	// Reduce congestion window and slowStartThreshold.
	int effectiveWindow = (int) ((conjestionWindow)<(sendWindow)?(conjestionWindow):(sendWindow)); 
	slowStartThreshold = ((effectiveWindow / 2)>(2)?(effectiveWindow / 2):(2));
	// set to one - that's what the rfc says - exp. decrease
	// only works in fast retransmit - cmg
	conjestionWindow = 1;
	fastRetransmit = false;

	// now, really I want to start systematically resending packets, as a kind of slow start,
	// but (a) they are already in the unacked rather than the unsent queue, and (b)
	// the receiver may already have them , so that the ACK sequence number may jump up
	// when we get to the missing bit...
	
	if (debugRate) 
	    printRateState("retransmit-timeout("+
			   ((JcpFragment) unackedFragments.elementAt(0)).getHeader().getSeq()+")");

	} // synchronized;

	if (debugCon)
	    System.out.println("ConnectionSapJcp::retransmit: reset conjestion window to "+conjestionWindow);
}

/**
* sendDelayedAck

protected


/**
* retransmitFragment
* @param f
* Retransmits JcpFragment as a result of fast retransmit or retransmission timeout.
* Updates connection strength measurement, retransmits and cancels round trip
* timer.
*/
protected void retransmitFragment(JcpFragment  f)
{
	
	connectionStrength = (connectionStrength * (connectionStrengthHistorySize - 1)) / connectionStrengthHistorySize;
	if (debug)
	    System.out.println("ConnectionSapJcp::outputFragment: Connection Strength: " + String.valueOf(connectionStrength) + '\n');

	outputFragment(f);

    roundTripTimer.cancelTimer();
}

/**
* addReceivedFragment
* @param f
*/
protected
void addReceivedFragment(JcpFragment  f)
{	
	int index;
	for(index = 0; index < receivedFragments.size(); index++)
	{
		JcpFragment  outOfOrderFragment = (JcpFragment) receivedFragments.elementAt(index);

		if(f.getHeader().getSeq() < outOfOrderFragment.getHeader().getSeq())
		{
			receivedFragments.insertElementAt(f, index);
			break;
		}
	}
	if(index == receivedFragments.size())
	{		
		receivedFragments.addElement(f);
	}
}

/**
* buildObject
* @param objectStart
* @param objectEnd
*/
protected
void buildObject(int objectStart, int objectEnd)
{
	synchronized(this) {

	while(objectStart <= objectEnd)
	{
		JcpFragment f = (JcpFragment) receivedFragments.elementAt(objectStart);
		completedObjectFragments.addElement(f);
		receivedFragments.removeElement(f);
		objectEnd--;
	}
	
	// Notify blocking readers.
	notifyAll();

	} // synchronized;

	// give blocking readers a chance to run so that they can open up the window
	// for us
	try {
	  Thread.sleep(0);
	} catch (Exception e) {}
}

/**
* abort
*/
protected
void abort()
{
	if(state == STATE_SYN_RCVD || state == STATE_ESTABLISHED ||
	   state == STATE_FIN_WAIT_1 || state == STATE_FIN_WAIT_2 || state == STATE_CLOSE_WAIT)
	{
	  // Send reset.
      JcpFragment  f = makeFragment(null, JcpFlag.FLAG_RST);
      // anticipate closed state before calling output or a send failure may
      // trigger a recursive call to abort which will loop
      state = STATE_CLOSED;
	  outputFragment(f);
	}
	
	closed();
}

/**
* reset
* Send reset dependent on state, then close.
*/
protected
void reset()
{
	JcpFragment  rst = makeFragment(null, JcpFlag.FLAG_RST | JcpFlag.FLAG_RELIABLE);
	outputFragment(rst);
}

/**
* closed
* Clean up after abort or close.
*/
protected
void closed()
{
	state = STATE_CLOSED;
	status = StatusValues.STATUS_ERROR;

	// Stop timers.
	retransmissionTimer.cancelTimer();
	delayedAckTimer.cancelTimer();
	keepAliveTimer.cancelTimer();
	timeoutTimer.cancelTimer();
	
	// Delete fragments.

	// Get exclusive access to lists.
	synchronized(this) {
	
	// Release exclusive access to lists.
	unackedFragments.removeAllElements();
	unsentFragments.removeAllElements();
	receivedFragments.removeAllElements();
	completedObjectFragments.removeAllElements();

	} // synchronized;


	// Close socket.
	if(socket != null)
	{
		socket.close();
		socket = null;
	}

	// Notify reading threads which should return. 
	synchronized(this) {
	    notifyAll();
	}
}

/**
* getReceiveWindow
* @return PRUint32
*/
protected int getReceiveWindow()
{
	return ((0)>(maxReceiveWindow - receivedFragments.size() - completedObjectFragments.size())?(0):(maxReceiveWindow - receivedFragments.size() - completedObjectFragments.size()));
}
}
