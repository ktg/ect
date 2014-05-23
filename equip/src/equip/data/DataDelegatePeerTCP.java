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
// DataDelegatePeerTCP.java
// Chris Greenhalgh 6th April 2001

package equip.data;

import java.net.*;
import equip.runtime.*;
import equip.net.*;
import java.util.Vector;
import equip.config.ConfigManager;
import equip.config.ConfigManagerImpl;

/** Internal implementation class; works with {@link DataDelegatePeer}
    and {@link DataDelegate} to handle the direct networking aspects
    of a single dataspace peer. 'TCP' is actually now a misnomer,
    since it now uses the {@link equip.net.ConnectionSap} abstraction
    to support both TCP and JCP. */
public class DataDelegatePeerTCP implements Runnable, DataDelegatePeerCancel 
{
    java.lang.Object lock;
    DataDelegatePeer peer;
    ConnectionSap connection;
    Thread inputThread, outputThread;
    boolean persistFlag;
    DeactivateCallback deactivateCallback;
    ValueBase deactivateClosure;
    boolean deactivatedFlag;
    // wait for connect/fail
    java.lang.Object connectLock;
    boolean connectedFlag;
    boolean failedFlag;
    // debug
    boolean replicateAll = true; // ****

    public DataDelegatePeerTCP(DataDelegatePeer peer) {
	lock = new java.lang.Object();
	this.peer = peer;
	connection = null;
	inputThread = null;
	outputThread = null;
	connectLock = new java.lang.Object();
	connectedFlag = failedFlag = false;
	peer.setCancelCallback(this);
    }
    
    public DataDelegatePeer getPeer() {
	return peer;
    }

    // wait for connect/fail (returned peer connect msg); true = ok
    public boolean waitForConnect() {
	boolean okFlag;
	System.err.println("DataDelegatePeerTCP.waitForConnect lock...");
	synchronized(connectLock) {
	    while (!connectedFlag && !failedFlag)
		try {
		    System.err.println
			("DataDelegatePeerTCP.waitForConnect wait...");
		    connectLock.wait();
		} catch (Exception e) {}
	    okFlag = !failedFlag;
	}
	System.err.println
	    ("DataDelegatePeerTCP.waitForConnect done ("+okFlag+")");
	return okFlag;
    }  

    // intended to be run on input thread of socket.
    // inputThread==null if not joinable.
    public void run(ConnectionSap connection, 
		    Thread inputThread,
		    boolean persistFlag,
		    DeactivateCallback callback,
		    ValueBase closure) {
	synchronized (lock) {
	    if (outputThread!=null) {
		System.err.println("Warning: DataDelegatePeerTCP::run called while still active "
				   +" - deactivating old session");
		deactivate();
	    }
	    
	    // in case left over...
	    if (inputThread!=null && 
		Thread.currentThread()!=inputThread) {
		System.err.println("DataDelegatePeerTCP::run waiting for old input thread...");
		try {
		    inputThread.join();
		} catch (Exception e) {
		    System.err.println("DataDelegatePeerTCP.handleDeactivate "
				       +"join input thread failed: "+e);
		}
		inputThread = null;
		System.err.println("OK");
	    }
	    
	    if (connection==null) {
		System.err.println("ERROR: DataDelegatePeerTCP::run called on failed/no socket");
		return;
	    }
	    
	    deactivateCallback = callback;
	    deactivateClosure = closure;
	    this.connection = connection;
	    this.persistFlag = persistFlag;
	    this.inputThread = inputThread;
	    deactivatedFlag = false;
	    
	    outputThread = new Thread(this);
	    outputThread.start();
	} /* synchronized(lock) */

	// slow???
	peer.setStatus(false,false,false);

 	// connect
	PeerConnectEvent connect = new PeerConnectEventImpl();
	GUID peerId = peer.getResponsible();
	if (peerId==null) {
	    System.err.println("Warning: DataDelegatePeerTCP::run sending "
			       +"null peer id in connect");
	}
	if (peerId.time_s < 1000) {
	    // HACK HACK for persistent dataspace peer -> long timeout
	    System.err.println("NOTE: increasing connection timeout for "+
			       "presumed persistent connection (local)");
	    connection.setConnectionTimeout(SapConstants.DEFAULT_LONG_CONNECTION_TIMEOUT_MS);
	}
	connect.initMetadata2(peerId, false, true, 0);
	connect.peerId = peerId;
	connect.policy = new PeerPolicyImpl();
	connect.policy.persistFlag = persistFlag;
	
	if (replicateAll) {
	    if (peer.getServerFlag()) {
		System.err.println("DataDelegatePeerTCP sending wildcard "+
				   "pattern - we are the server");
		// on the server will ask to replicate everything for now
		connect.patterns = new EventPattern [1];
		connect.patterns[0] = new EventPatternImpl();
		Event anyEvent = new EventImpl();
		// local pattern
		connect.patterns[0].initAsSimpleEventMonitor(anyEvent, true);
	    } else
		connect.patterns = new EventPattern [0];
	}

	peer.outputEvent(connect);

	// bootstrap config
	DataManager.getInstance();
	ConfigManager config = (ConfigManager)SingletonManager.get(ConfigManagerImpl.class.getName());
	String secret = System.getProperty("DataspaceSecret", config.getStringValue("DataspaceSecret", null));
		// client must receive a challenge
	boolean requiresChallenge = (secret!=null) && !peer.getServerFlag();
	boolean requiresResponse = requiresChallenge;
	String challenge = null;

	// run input
	// - lower priority
	int pri = Thread.currentThread().getPriority();
	System.err.println("DataDelegatePeerTCP lowering priority of "+
			   "input thread from "+pri+" to "+(pri-1));
	Thread.currentThread().setPriority(pri-1);
	boolean firstFlag = true;
		while(true) 
		{
			ValueBase obj=null;
			try 
			{
				obj = connection.readObject();
			} 
			catch (Exception e) 
			{
				System.err.println("read object exception: "+e);
				e.printStackTrace(System.err);
			}
			if (obj==null) 
			{
				System.err.println("Warning - data peer object read failed - "
					+"assume connection closed");
				break;
			}
			Event event = null;
			// treat this at this "layer"
			if (requiresChallenge) 
			{
				if (!(obj instanceof PeerChallengeEvent)) 
				{
					System.err.println("ERROR: DataDelegateTCP did not receive "
						+"PeerChallengeEvent first ("+obj.getClass().getName() 
						+") - rejecting");
					break;
				}
				PeerChallengeEvent response = (PeerChallengeEvent)obj;
				if (response.challengeType!=ChallengeType.CHALLENGE_SIMPLE_CHALLENGE) 
				{
					System.err.println("ERROR: received unexpected challenge type "+response.challengeType+" - rejecting session");
					break;
				}
				if (response.challenge!=null) 
				{
					PeerChallengeEvent pchallenge = new PeerChallengeEventImpl();
					pchallenge.challengeType = ChallengeType.CHALLENGE_SIMPLE_CHALLENGE_RESPONSE;
					pchallenge.challenge = challenge = Challenge.makeChallenge();
					pchallenge.response = Challenge.makeResponse(secret, response.challenge);
					try 
					{
						connection.writeObject(pchallenge);
					} 
					catch (Exception e) 
					{
						System.err.println("ERROR writing response to connection: "+e+" - assume connection closed");
						break;
					}
					requiresChallenge = false;
					requiresResponse = true;
				}
			}
			else if (requiresResponse) 
			{
				if (!(obj instanceof PeerChallengeEvent)) 
				{
					System.err.println("ERROR: DataDelegateTCP did not receive "
						+"PeerChallengeEvent second ("+obj.getClass().getName() 
						+") - rejecting");
					break;
				}
				PeerChallengeEvent response = (PeerChallengeEvent)obj;
				if (response.challengeType!=ChallengeType.CHALLENGE_SIMPLE_RESPONSE) 
				{
					System.err.println("ERROR: received unexpected challenge response type "+
						response.challengeType+" - rejecting session");
					break;
				}
				if (!Challenge.acceptResponse(secret, challenge, response.response)) 
				{
					System.err.println("ERROR: server failed challenge - rejecting");
					PeerChallengeEvent reject = new PeerChallengeEventImpl();
					reject.challengeType = ChallengeType.CHALLENGE_FAILURE;
					try 
					{
						connection.writeObject(reject);
					} 
					catch (Exception e) {}
					break;
				}
				requiresResponse = false;
			}
			else if (obj instanceof PeerChallengeEvent) 
			{
				PeerChallengeEvent pchallenge = (PeerChallengeEvent)obj;
				System.err.println("ERROR: client received unexpected challenge event (type "+
					pchallenge.challengeType+") - giving up");
				PeerChallengeEvent reject = new PeerChallengeEventImpl();
				reject.challengeType = ChallengeType.CHALLENGE_FAILURE;
				try 
				{
					connection.writeObject(reject);
				} 
				catch (Exception e) {}
				break;
			}
			else if (!(obj instanceof Event)) 
			{
				System.err.println("ERROR: data peer received non-event object (class "
					+ obj.getClass().getName() + ") - ignored");
			} 
			else 
			{
				event = (Event)obj;
				peer.handleEvent(event);
				try 
				{
					// yield
					Thread.sleep(0);
				} 
				catch (Exception e) {}
			}

	    if (firstFlag) {
		firstFlag = false;
		// first message
		notifyConnect(true);
		// only say connected on receipt!
		peer.setStatus(false,true,false);
	    }
	}
	System.err.println("DataDelegatePeerTCP restoring priority of "+
			   "terminating input thread to "+pri);
	Thread.currentThread().setPriority(pri);

	// slow???
	peer.setStatus(false,false,false);

	synchronized (lock) {	    // term
	    if (deactivatedFlag) {
		// term
		return;
	    }
	    deactivatedFlag = true;
	}

	// create disconnect
	handleDeactivate(true);
    }


    // change persistence flag
    public void setPersist(boolean persistFlag) {
	this.persistFlag = persistFlag;
	System.err.println("WARNING: DataDelegatePeerTCP::setPersist not fully supported");
	// ....
    }

    // if you need to kill it off
    public void deactivate() {
	synchronized(lock) {
	    
	    if (outputThread==null) {
		System.err.println("ERROR: DataDelegatePeerTCP::deactivate "
				   +"called when inactive");
		return;
	    }
	    if (deactivatedFlag) {
		System.err.println("ERROR: DataDelegatePeerTCP::deactivate "
				   +"called when already deactivating");
		return;
	    }
	    deactivatedFlag = true;
	    
	    // this should free input and output threads from blocking read/write
	    try {
		connection.close();
	    } catch (Exception e) {
		System.err.println("DataDelegatePeerTCP socket close exception: "
				   +e);
	    }
	}

	handleDeactivate(false);
    }

    // notify connect
    private void notifyConnect(boolean okFlag) {
	synchronized (connectLock) {
	    connectedFlag = okFlag;
	    failedFlag = !okFlag;
	    connectLock.notify();
	}
    }
    // common to deactivate and connection failure
    private void handleDeactivate(boolean notifyFlag) {
	notifyConnect(false);

	// this should free output thread from blocking on outbound event
	peer.wakeOutbound();

	// sort out input thread
	if (inputThread!=null) {
	    if (Thread.currentThread()==inputThread) {
		System.err.println("Note: DataDelegatePeerTCP::handleDeactivate defering "
				   +"input thread join til next activate");
	    } else {
		System.err.println("DataDelegatePeerTCP: join input thread...");
		try {
		    inputThread.join();
		} catch (Exception e) {
		    System.err.println("DataDelegatePeerTCP.handleDeactivate "
				       +"join old input thread failed: "+e);
		}
		inputThread = null;
		System.err.println("OK");
	    }
	}
	// sort out output thread
	System.err.println("DataDelegatePeerTCP: join output thread...");
	try {
	    outputThread.join();
	} catch (Exception e) {
	    System.err.println("DataDelegatePeerTCP.handleDeactivate "
			       +"join output thread failed: "+e);
	}
	outputThread = null;
	System.err.println("OK");
  
	synchronized (lock) {
	    
	    connection = null;

	    // generate PeerDisconnectEvent on local peer of main database
	    GUID peerId = peer.getPeerId();
	    PeerDisconnectEvent disconnect = new PeerDisconnectEventImpl();
	    disconnect.initMetadata2(peerId, false, true, 0);
	    disconnect.peerId = peerId;
	    disconnect.deleteFlag = !persistFlag;
	    peer.handleEvent(disconnect);
	    disconnect = null;

	    // GC?
	    if (notifyFlag && deactivateCallback!=null)
		deactivateCallback.notifyDeactivate(null,
						    deactivateClosure);
	    deactivateCallback = null;
	    deactivateClosure = null;
	}
    }
    /* implementation */
    public void run() {
	int pri = Thread.currentThread().getPriority();
	System.err.println("DataDelegatePeerTCP raising priority of "+
			   "output thread from "+pri+" to "+(pri+1));
	Thread.currentThread().setPriority(pri+1);
	while(true) {
	    DataDelegatePeer.OutboundEvent oevent = 
		peer.getNextOutboundEvent();
	    if (oevent==null) {
		System.err.println("DataDelegatePeerTCP::outputThread terminating on null event");
		return;
	    }
	    try {
		boolean reliableFlag = true;
		int priority = 0;
		if (oevent!=null && oevent.event!=null && oevent.event.metadata!=null)
		{
		  reliableFlag = oevent.event.metadata.reliable;
		  priority = oevent.event.metadata.priority;
		}
		int [] object_id = new int [1];
		connection.writeObject(oevent.event, reliableFlag, object_id, priority);
		oevent.object_id = object_id[0];
		
		if (!reliableFlag) {
		    addSentEvent(oevent);
		}

	    } catch (Exception e) {
		System.err.println("DataDelegatePeerTCP::outputThread "+
				   "write object exception (terminate): "+e);
		e.printStackTrace(System.err);
		return;
	    }
	    peer.removeOutboundEvent(oevent.sequenceNo);
	}
    }
  protected void finalize() {
    cleanup();
  } 
  public void cleanup() {
  }
  Vector sentEvents = new Vector();
  static final int maxSentEvents = 100;
  void addSentEvent(DataDelegatePeer.OutboundEvent oe) {
      synchronized(sentEvents) {
	  sentEvents.addElement(oe);
	  if (sentEvents.size() > maxSentEvents) 
	      sentEvents.removeElementAt(0);
      }
  }
  /** this should be called from DataDelegatePeer.outputEvent
   * when an UpdateEvent is added, to allow us to check
   * whether this causes a cancel on a written object.
   */
  public void checkCancelObject(UpdateEvent update1) {
      // like outputEvent
      synchronized(sentEvents) {
	  int iter;
	  iter = sentEvents.size();
	  if (iter!=0)
	      iter--;
	  while(!(iter==0)) {
	      DataDelegatePeer.OutboundEvent oevent2 = 
		  (DataDelegatePeer.OutboundEvent)sentEvents.elementAt(iter);
	      if (oevent2.event!=null &&
		  (oevent2.event instanceof UpdateEvent)) {
		  UpdateEvent update = (UpdateEvent)(oevent2.event);
		  if (update.metadata!=null &&
		      update.item!=null &&
		      update.item.id!=null &&
		      update1.item.id.equals(update.item.id)) {
		      if (!update.metadata.reliable) {
			  //System.err.println("Squash sent unreliable event for "
			  //+ update1.item.getClass().getName() + " " 
			  //+ update1.item.id);
			  sentEvents.removeElementAt(iter);
			  // cancel
			  connection.cancelObject(oevent2.object_id);
			  break;
		      } 
		  } // update & matches
	      } 
	      iter--;
	  } // while
      } // synchronized()
  }
}
