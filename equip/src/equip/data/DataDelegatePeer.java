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
// DataDelegatePeer.java
// Chris Greenhalgh, 6th april 2001

package equip.data;

import equip.runtime.*;
import equip.net.*;
import java.util.Vector;
import java.util.Hashtable;

/** Internal implementation class; works with  {@link DataDelegate} 
 * to handle a dataspace replica's association with another dataspace
 * replica. For example, a client will have an instance of this class
 * to deal with its relationship to its server, and a server will have
 * one instance of this class for each current client. */
public class DataDelegatePeer extends DataCallback {
    // DataDelegate we are associated with
    protected DataDelegate dataDelegate = null;
    // the peer's id
    protected GUID peerId = null, responsible = null;
    // server flag, affects event/item forwarding
    boolean serverFlag;
    // policy info
    protected PeerPolicy policy = null;
    // the session that we manage on behalf of the peer.
    // consumes the initial patterns (in PeerConnectEvent).
    protected DataSession session = null;
    // outbound events...

    public class OutboundEvent {
	Event event;
	int sequenceNo;
	// policy-related info (e.g. priority, reliability)
	// ....
    	// connectionsap object_id for cancelObject
	int object_id;
    }

    protected Vector outboundEvents = new Vector();
    // next sequence number
    protected int nextOutboundSequenceNo;
    // for lock/notify on outboundEvents...
    protected java.lang.Object outboundEventLock;
    // items for which the peer has had an event but not a subsequent delete
    protected Hashtable notifiedItemIds = new Hashtable();
    // last event notified to us
    protected Event lastNotifiedEvent = null;

    boolean debug = false;
    boolean debugDuplicate = true;

  //------------------------------------------------------------
  /* public API */
  // created as a result of a SetPeer event - next action is to call
  // handleEvent
    public DataDelegatePeer(DataDelegate dataDelegate, 
			    GUID peerId, boolean serverFlag) {
	this.peerId = peerId;
	this.serverFlag = serverFlag;
	this.dataDelegate = dataDelegate;
	responsible = dataDelegate.getPeerId();
	outboundEventLock =  new java.lang.Object();
	nextOutboundSequenceNo = 0;
	System.err.println("Creating DataDelegatePeer");
    }
    public void shutdown() {
	System.err.println("Deactivating DataDelegatePeer");
	// make sure that we are unregistered
	if (session!=null)
	    dataDelegate.deleteSession(session);
	session = null;
    }
    public boolean getServerFlag() {
	return serverFlag;
    }
    public GUID getPeerId() {
	return peerId;
    }
    public GUID getResponsible() {
	return dataDelegate.getPeerId();
    }

    // handle peer event incoming.
    // first event should be PeerConnectEvent.
    public void handleEvent(Event event) {
	// fix source
	if (event!=null && event.metadata!=null && peerId!=null) {
	    event.metadata.source = peerId;
	}
	// peer event?
	PeerEvent peer;
	if (event instanceof PeerEvent) {
	    peer = (PeerEvent)event;
	    // PeerConnectEvent?
	    if (event instanceof PeerConnectEvent) {
		PeerConnectEvent connect = (PeerConnectEvent)event;
		// handle peer update
		if (peerId!=null &&
		    !peerId.equals(connect.peerId)) {
		    System.err.println("Warning: PeerConnectEvent changes "
				       +"peerId from "+peerId+" to "
				       +connect.peerId);
		} 
		peerId = connect.peerId;

		if (serverFlag) 
		{
			// truncate leases from previous connections - we should see duplicate adds soon!
			dataDelegate.truncateExpireTimes(peerId);
		}

		// handle policy update
		// .... ?
		policy = connect.policy;

		// new session?
		if (session==null) {
		    session = dataDelegate.createSession(this, null);
		}

		// merge patterns - turn it into a PeerAddPatternsEvent?
		handleAddPatterns(connect.patterns);
		return;
	    }
    
	    if (session==null) {
		System.err.println("ERROR: received peer event " + 
				   event.getClass().getName()
				   + " while not connected - ignored");
		return;
	    }
	    if (peer.peerId!=null &&
		!peerId.equals(peer.peerId)) {
		System.err.println("ERROR: received peer event for wrong "
				   +"peer id: "+peer.peerId+" vs our id " 
				   +peerId+" - ignored");
		return;
	    } 
    
	    // PeerDisconnectEvent?
	    if (event instanceof PeerDisconnectEvent) {
		PeerDisconnectEvent disconnect = 
		    (PeerDisconnectEvent)event;
		// depending on policy we should either retain the session
		// and queue outbound notifications or we should remove the
		// session.
		//   depending on policy we should garbage collect for item
		// that the other end is responsible for, e.g. by a remove
		// agent event or similar.

		// deleteFlag?....
		if (disconnect.deleteFlag) {
		    // remove session, ignoring notifications
		    if (session!=null) {
			dataDelegate.deleteSession(session);
			session = null;
		    }

		    // clear outbound event list
		    outboundEvents.removeAllElements();

		    // GC for peer
		    RemoveResponsible remove = new RemoveResponsibleImpl();
		    remove.initMetadata2(peerId, false, true, 0);
		    if (serverFlag) {
		      remove.responsible = peerId;
		      remove.inverseFlag = false;
		    } else {
		      // on client leave only us
		      remove.responsible = responsible;
		      remove.inverseFlag = true;
		    }
		    dataDelegate.addEvent(remove);

		    // What about peer's peers??
		    // ....
		}
		return;
	    } else if (event instanceof PeerAckEvent) {
		PeerAckEvent ack = (PeerAckEvent)event;
		if(ack.data!=null && ack.data.ack) {
		    //System.err.println("received acking PeerAckEvent");
		    // back from server - queue for local handling
		    dataDelegate.queueEvent(event); 
		    return;
		}
		if (ack.data!=null) {
		    // ack it
		    ack.data.ack = true;
		    // local events 
		    dataDelegate.waitForEvents(true);
		    //System.err.println("returning acked PeerAckEvent");
		    outputEvent(ack);
		    return;
		} else {
		    System.err.println("Warning: broken (no data) PeerAckEvent");
		    return;
		}
	    }
		
	    System.err.println("ERROR: received unknown peer event (type " 
		 + event.getClass().getName() + " - ignored");
	    return;
	}

	// add/delete pattern? -> PeerAddPatterns/PeerDeletePatterns
	if (event instanceof AddEvent) {
	    AddEvent add = (AddEvent)(event);
	    if (add.binding.item instanceof EventPattern) {
		EventPattern pattern = (EventPattern)(add.binding.item);
		//cerr << "DataDelegatePeer::handleEvent: add pattern\n";
		// force pattern itself to local scope - mutating incoming event
		pattern.local = true;
		handleAddPattern(pattern);
	    }
	} else {
	    if (event instanceof DeleteEvent) {
		DeleteEvent del = (DeleteEvent)(event);
		GUID id = del.id;
		ItemBinding binding = dataDelegate.getItemBinding(id);
		if (binding!=null && 
		    (binding.item instanceof EventPattern)) {
		    EventPattern pattern = (EventPattern)(binding.item);
		    if (session==null) {
			System.err.println("ERROR: PeerDeletePatternsEvent "+
					   "received w. no session");
		    } else {
			//cerr << "DataDelegatePeer::handleEvent: delete pattern\n";
			session.deletePattern(id);
		    }
		}
	    }
	} 

	// other event, assumed normal.
	// Ideally we want the input thread to run for a bit, filling up the queue
	// before we push it through; that means we need to know when there are 
	// no more messages, or push pending.
	//dataDelegate.addEvent(event);
	dataDelegate.queueEvent(event); //??? requires DataDelegate push thread
    }

    // add an event to the outbound queue and signal the output thread.
    // normally called by notify.
    // Meta-data??
    public void outputEvent(Event event) {
      synchronized (outboundEventLock) {
	OutboundEvent oevent = new OutboundEvent();
	if (event!=null) {
	    // make sure that sourceId is set - this should be the same for all
	    // parallel uses of this event
	    if (event.metadata==null) {
		System.err.println("Warning: DataDelegatePeer::outputEvent had no metadata (class "
		     + event.getClass().getName() + "");
		event.metadata = new EventMetadataImpl();
		event.metadata.init2(dataDelegate.getPeerId(), false, true, 0);
	    }
	    if (event.metadata.source==null) {
		GUID sourceId = dataDelegate.getPeerId();
		if (sourceId==null) {
		    System.err.println("Warning: DataDelegatePeer::outputEvent cannot get source id");
		}
		event.metadata.source = sourceId;
	    }
	}
	// is there an unreliable update event for the same item already in the
	// event queue? if so, we will dump it.
	//  NB don't dump the first event as another process may already be
	// handling it.
	if (event!=null && (event instanceof UpdateEvent)) {
	    UpdateEvent update1 = (UpdateEvent)(event);
	    if (update1!=null &&
		update1.item!=null &&
		update1.item.id!=null) {
		int iter;
		iter = outboundEvents.size();
		if (iter!=0)
		    iter--;
		while(!(iter==0)) {
		    OutboundEvent oevent2 = 
			(OutboundEvent)outboundEvents.elementAt(iter);
		    if (oevent2.event!=null &&
			(oevent2.event instanceof UpdateEvent)) {
			UpdateEvent update = (UpdateEvent)(oevent2.event);
			if (update.metadata!=null &&
			    update.item!=null &&
			    update.item.id!=null &&
			    update1.item.id.equals(update.item.id)) {
			    if (!update.metadata.reliable) {
				if (debug) 
				    System.err.println("Squash queued unreliable event for "
						       + update1.item.getClass().getName() + " " 
						       + update1.item.id);
				outboundEvents.removeElementAt(iter);
				break;
			    } else {
				if (debug)
				    System.err.println("Could not squash reliable update event for "
						       + update1.item.getClass().getName() + " " 
						       + update1.item.id);
			    }
			} // update & matches
		    } 
		    iter--;
		} // while
		if (cancelCallback!=null)
		    cancelCallback.checkCancelObject(update1);
	    }
	}
	oevent.event = event;
	oevent.sequenceNo = nextOutboundSequenceNo++;
	outboundEvents.addElement(oevent);
	outboundEventLock.notify();
      }
      /*?? here, or rely on new yield in readObject
	try {
	  // yield
	  Thread.currentThread().sleep(0);
      } catch (Exception e) {}
      */
    }

    // get next outbout event from the outbound queue.
    // returns a pointer to its own copy, left in queue.
    // call removeOutboundEvent to remove it.
    // Blocks, but may be woken by wakeOutbound (returning null).
    public OutboundEvent getNextOutboundEvent() {
      synchronized (outboundEventLock) {
	while (outboundEvents.size()==0)
	    try {
		outboundEventLock.wait();
	    } catch (Exception e) {}
	// check for a null event bypassing the queue
	OutboundEvent oevent = (OutboundEvent)
	    outboundEvents.elementAt(outboundEvents.size()-1);
	if (oevent.event==null) {
	    outboundEvents.removeElementAt(outboundEvents.size()-1);
	    return null;
	}
	// take the next main event off the front
	oevent = (OutboundEvent)outboundEvents.elementAt(0);
    
	return oevent;
      }
    }
    
    // wake getNextOutboundEvent w. null
    public void wakeOutbound() {
	outputEvent(null);
    }
    // removes the specified event (if present) from the outbound
    // events queue; normally caused by incoming acknowledgement
    // event (which does not yet exist :-).
    public void removeOutboundEvent(int sequenceNo) {
      synchronized (outboundEventLock) {
	int iter;
	for (iter=0; iter<outboundEvents.size(); iter++) {
	    OutboundEvent oevent = 
		(OutboundEvent)outboundEvents.elementAt(iter);
	    if (oevent.sequenceNo==sequenceNo) {
		outboundEvents.removeElementAt(iter);
		return;
	    }
	}
	System.err.println("Warning: DataDelegatePeer::removeOutboundEvent "
			   +"for unknown event (" + sequenceNo + ")");
      }
    }

    protected int eventRepeatCount = 0;
    //------------------------------------------------------------
    /* internal/implementation */
    // notification from DataDelegate destined for our peer.
    // normally results in called outputEvent (once).
    public void notify(Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
	// same event delivered for multiple patterns?
	if (lastNotifiedEvent==event) 
	{
	    eventRepeatCount ++;
	}
	else 
	{
	    lastNotifiedEvent = event;
	    eventRepeatCount = 0;
	}
	// pattern deleted??
	// ....
	// don't send our own events/items...
	if (event.metadata==null) {
	    System.err.println("WARNING: event could not be source filtered: no metadata (class "
		 + event.getClass().getName() + ")");
	}
	else {
	    if (event.metadata.source==null)
		System.err.println("WARNING: event could not be source filtered: no source (class "
		     + event.getClass().getName() + ")");
	    else if (peerId==null)
		System.err.println("WARNING: event could not be source filtered: no peer id (class "
		     + event.getClass().getName() + ")");
	    else if (event.metadata.source.equals(peerId)) 
		// ignore
		return;
	    else if (!serverFlag &&
		     !event.metadata.source.equals(responsible)) {
		// ignore
		return;
	    }
	    if (event.metadata.local)
		// ignore
		return;
	}

	// add?
	if (event instanceof AddEvent) 
	{
	    AddEvent add = (AddEvent)event;
	    if (add.binding==null ||
		add.binding.item==null ||
		add.binding.item.id==null) 
	    {
		System.err.println("ERROR: DataDelegatePeer.notify with ill-formed AddEvent - ignored");
		return;
	    }
	    if (add.binding.info!=null && 
		add.binding.info.itemLease!=null) 
	    {
		// it is leased, so we better send anyway
		// and we can't really count it, because deletes 
		// may just be local timeouts that we won't see
		// ? on the other hand perhaps we could count pseudo-adds or suppress them, anyway.
		// cases:
		//   item exists before pattern: pseudo-add on pattern add
		//   pattern exists before item: real add (dup suppressed) on item add
		//   add is a lease maintain vs add is a new item

		// we need to suppress propagation of deletes on leased items caused by withdrawal of 
		// patterns (rather than actual delete), but ideally would like to propagate delete
		// on withdrawal of last matching pattern...
		int [] value = (int[])notifiedItemIds.get(add.binding.item.id);
		// if read add then these must be pattern matches for a real add/update and repeat count
		//   will reflect (eventually) the no. of patterns
		// if eventRepeatCount==0 && add is pseudo then caused by pattern add; increase count
		if (value==null) 
		{
		    value = new int[1];
		    notifiedItemIds.put(add.binding.item.id, value);
		}
		if (add.kind!=null && add.kind.data==ItemEventKind.EQDATA_KIND_PRESENT)
		    value[0]++;
		else
		    value[0] = eventRepeatCount+1;
		if (value[0]>1) 
		{
		    if (debugDuplicate) 
			System.err.println("DataDelegatePeer: suppress duplicate add for leased item "+
			    add.binding.item.id);
		    return;
		}
	    } 
	    else 
	    {
		// count adds
		int [] value = (int[])notifiedItemIds.get(add.binding.item.id);
		if (value!=null) 
		{
		    // already done
		    if (debugDuplicate) 
		    {
			System.err.println("DataDelegatePeer: suppress duplicate add for item "+
			    add.binding.item.id);
		    }
		    value[0]++;
		    return;
		} 
		value = new int[1];
		value[0] = 1;
		notifiedItemIds.put(add.binding.item.id, value);
	    }
	} 
	else if (event instanceof DeleteEvent) 
	{
	    DeleteEvent del = (DeleteEvent)event;
	    if (del.id==null) 
	    {
		System.err.println("ERROR: DataDelegatePeer.notify with ill-formed DeleteEvent - ignored");
		return;
	    }
	    // if it is (most recently) leased, no need to touch value
	    int [] value = (int[])notifiedItemIds.get(del.id);
	    if (value==null) 
	    {
		// might have been leased
		ItemBinding binding = dataDelegate.getItemBinding(del.id);
		if(binding!=null && binding.info!=null &&
		    binding.info.itemLease!=null) 
		    // ok
		    ;
		else 
		{
		    System.err.println("Note: DataDelegatePeer: DeleteEvent for unknown item: "+
			del.id+" - forwarded assuming owned by far end");
		    //return;
		}
	    } 
	    else 
	    {
		value[0]--;
		if (value[0]>0) 
		{
		    if (debugDuplicate) 
		    {
			System.err.println("DataDelegatePeer: suppress duplicate delete for item "+
			    del.id);
		    }
		    return;
		}
		notifiedItemIds.remove(del.id);
	    }
	}
	else 
	{
	    if (eventRepeatCount>0) 
	    {
		if (debugDuplicate) 
		    System.err.println("DataDelegatePeer: suppress duplicate update event "+
			event+" ("+event.getClass().getName()+")");
		return;
	    }
	}
	outputEvent(event);
    }

    // status feedback - from driver
    public void setStatus(boolean serverFlag,
			  boolean clientConnectedFlag,
			  boolean clientSlowFlag) {
      if (!this.serverFlag && dataDelegate!=null)
	dataDelegate.setStatus(this.serverFlag, clientConnectedFlag,
			       clientSlowFlag);
    }

    // handleEvent helpers
    // merge new patterns
    protected void handleAddPatterns(EventPattern [] patterns) {
	int ci;
	for (ci=0; ci<(patterns).length; ci++) {
	    handleAddPattern(patterns[ci]);
	}
    }
    protected void handleAddPattern(EventPattern pattern) {
	int si;
	// try to avoid duplicates; not quite
	synchronized (session) {
	    for (si=0; si<session.patterns.length; si++)
		if (session.patterns[si].id!=null &&
		    pattern.id!=null &&
		    session.patterns[si].id.equals(pattern.id))
		    // got it
		    break;
	    if (si<session.patterns.length)
		// got it
		return;
	}
	// add it
	session.addPattern(pattern);
    }

  protected void finalize() {
    cleanup();
  }
  public void cleanup() {
    System.err.println("DataDelegatePeer.cleanup...\n");

    // make sure that we are unregistered
    if (session!=null)
      dataDelegate.deleteSession(session);

    // clear outbound event list
    outboundEvents.removeAllElements();

  }
  DataDelegatePeerCancel cancelCallback;
  public void setCancelCallback(DataDelegatePeerCancel cb) {
      cancelCallback = cb;
  }
  /** return number of outbound events not passed to connection */
  public int getNumOutboundEvents() {
      return outboundEvents.size();
  }
}
