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
// DataDelegate.java
// Chris Greenhalgh 28/03/01

package equip.data;

import equip.config.ConfigManager;
import equip.net.ServerURL;
import equip.net.SimpleMoniker;
import equip.runtime.SingletonManager;
import equip.runtime.Time;
import equip.runtime.TimeImpl;
import equip.runtime.ValueBase;

import java.util.*;

/**
 * Main internal implementation of the {@link DataProxy} API; used by
 * delegation in {@link DataProxyImpl} and {@link Server}.
 * <p>
 * Now supports internal API {@link IDataStore} for pluggable storage
 * modules, currently {@link MemoryDataStore} (which is always the
 * default), {link FileBackedMemoryDataStore} and
 * {@link equip.data.sql.CustomTableDataStore}.  These
 * are configured at present for server dataspaces, only, based on the
 * canonical equip URL of the dataspace (with ':', '=' and ' '
 * replaced with '_').  {@link DataManager} reads the standard config
 * file equip.eqconf, which is the normal home for configuration of
 * data space. For example, a dataspace server with the equip url
 * "equip://128.243.22.12:9123/" would be configured to (also) use
 * a {@link equip.data.sql.CustomTableDataStore} as follows:
 * <pre>
 * equip_//128.243.22.12_9123/.dataStore1Name: FooTable
 * equip_//128.243.22.12_9123/.dataStore1Class: equip.data.sql.CustomTableDataStore
 * </pre>
 * <p>
 * The data store itself would be configured as described in {@link
 * equip.data.sql.CustomTableDataStore} (the store's name is
 * "FooTable" in this case).
 */
public class DataDelegate
{
	private List<Event> pendingEvents = new ArrayList<>();
	/*package*/ List<DataSessionImpl> sessions = new ArrayList<>();
	private GUIDFactory guidFactory = null;
	private GUID responsible = null; // this delegates unique 'responsible' id
	private Map<GUID, DataDelegatePeer> peerMap = new Hashtable<>(); // GUID -> DataDelegatePeer
	private static boolean debug = false;
	private static boolean debugCheck = false;
	// push events through the queue
	Thread queuePushThread;
	int queuePushSignal[] = new int[1];
	java.lang.Object queuePushLock = new java.lang.Object();
	private GUID statusId;
	private DataProxy proxy = null;
	/**
	 * List of IDataStores, to store stuff in
	 */
	private List<IDataStore> stores = new ArrayList<>();

	// busy?
	int busyCount = 0;

	void beginBusy()
	{
		synchronized (this)
		{
			busyCount++;
		}
	}

	void endBusy()
	{
		boolean tryPending = false;
		synchronized (this)
		{
			busyCount--;
			if (busyCount <= 1)
			{
				try
				{
					this.notifyAll();
				}
				catch (Exception e)
				{
				}
			}
			if (busyCount == 0)
			{
				tryPending = true;
			}
		} // sync(this)
		if (tryPending)
		{
			doPendingEvents();
		}
	}

	// pseudo-internal - called by addEvent; false=fail
	public boolean validateEvent(Event event)
	{
		if (event instanceof AddEvent)
		{
			AddEvent add = (AddEvent) (event);
			if (add.binding == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent add with missing binding - "
						+ "ignored");
				return false;
			}
			if (add.binding.item == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent add with missing item - "
						+ "ignored");
				return false;
			}
			if (add.binding.info == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent add with missing info - "
						+ "ignored");
				return false;
			}
			if (add.binding.item.id == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent add with missing item id "
						+ "(class " + add.binding.item.getClass().getName()
						+ ") - ignored");
				return false;
			}
			if (add.kind != null &&
					add.kind.data == ItemEventKind.EQDATA_KIND_LEASE_RENEW)
			// must really be NORMAL
			{
				add.kind.data = ItemEventKind.EQDATA_KIND_NORMAL;
			}
		}
		// delete?
		if (event instanceof DeleteEvent)
		{
			DeleteEvent del = (DeleteEvent) (event);
			if (del.id == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent del with missing id - "
						+ "ignored");
				return false;
			}
		}
		// update?
		if (event instanceof UpdateEvent)
		{
			UpdateEvent update = (UpdateEvent) (event);
			if (update.item == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent update with missing item - "
						+ "ignored");
				return false;
			}
			if (update.item.id == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent update with missing item id "
						+ "(class " + update.item.getClass().getName() + ") - ignored");
				return false;
			}
		}
		return true;
	}

	// pseudo-internal - called by addEvent/processPending immediately before enactment; 0=fail
	private boolean validateEventFinal(Event event)
	{
		// update?
		if (event instanceof UpdateEvent)
		{
			UpdateEvent update = (UpdateEvent) (event);
			if (update.item == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent update with missing item - "
						+ "ignored");
				return false;
			}
			if (update.item.id == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent update with missing item id "
						+ "(class " + update.item.getClass().getName() + ") - ignored");
				return false;
			}
			ItemData oldValue = getItem(update.item.id);
			if (oldValue == null)
			{
				System.err.println("ERROR: DataDelegate::validateEvent update for unknown item (id "
						+ update.item.id + ", class " + update.item.getClass().getName()
						+ ") - ignored");
				return false;
			}
		}
		return true;
	}

	/**
	 * (always) Queued add of a new event into the dataspace.
	 *
	 * @param event The event to published.
	 */
	public void queueEvent(equip.data.Event event)
	{
		if (event == null)
		{
			System.err.println("ERROR: DataDelegate::addEvent received null event");
			return;
		}
		// responsible...
		fixResponsible(event);
		if (!validateEvent(event))
		{
			System.err.println("ERROR: DataDelegate::addEvent rejected invalid event (class "
					+ event.getClass().getName() + ")");
			return;
		}
		synchronized (this)
		{
			intQueueEvent(event);
		}
	}

	/* DataProxy API */
	public void addEvent(Event event)
	{
		if (event == null)
		{
			System.err.println("ERROR: DataDelegate::addEvent received null event");
			return;
		}
		// responsible...
		fixResponsible(event);
		if (!validateEvent(event))
		{
			System.err.println("ERROR: DataDelegate::addEvent rejected invalid event (class "
					+ event.getClass().getName() + ")");
			return;
		}
		// if we are already adding doing something then we should just queue it
		boolean kickPending = false;
		synchronized (this)
		{
			// = atomic check & beginBusy
			if (busyCount != 0 || pendingEvents.size() != 0)
			{
				// busy or events already in the queue
				// re-entrant monitor in java (unlike NSPR)
				intQueueEvent(event);
				if (busyCount == 0)
				// kick it just in case
				{
					kickPending = true;
				}
				else
				{
					return;
				}
			}
			else
			{
				busyCount++;
			}
		} // synchronized(this);
		if (kickPending)
		{
			// now out of lock
			doPendingEvents();
			return;
		}
		if (!validateEventFinal(event))
		{
			System.err.println("ERROR: DataDelegate::addEvent rejected invalid event (class "
					+ event.getClass().getName() + ") - final check");
		}
		else
		{
			// post callbacks
			List<PostCallbackInfo> postCallbackList = new ArrayList<>();

			checkLeaseAdd(event);
			// callbacks?
			checkEvent(event, postCallbackList);
			// do the work
			addEventInternal(event);

			callPostCallbacks(postCallbackList);
			try
			{
				// yield
				Thread.sleep(0);
			}
			catch (Exception e)
			{
			}
		}
		endBusy();
	}

	public ItemBinding getItemBinding(GUID id)
	{
		for (IDataStore store : stores)
		{
			ItemBinding rval = store.getItemBinding(id);
			if (rval != null)
			{
				return rval;
			}
		}
		return null;
	}

	public ItemData getItem(GUID id)
	{
		ItemBinding binding = getItemBinding(id);
		if (binding == null)
		{
			return null;
		}
		return binding.item;
	}
	// event synthesis routines are not included

	public DataSession createSession(DataCallback callback,
	                                 ValueBase closure)
	{
		DataSessionImpl session = new DataSessionImpl();
		session.callback = callback;
		session.closure = closure;

		beginBusy();

		if (!session.delegateAdd(this))
		{
			System.err.println("ERROR: DataDelegate::addSession failed");
			endBusy();
			return null;
		}
		synchronized (this)
		{
			// add to list...
			sessions.add(session);
			if (debugCheck)
			{
				System.err.println("DataSession.createSession " +
						(sessions.size() - 1));
			}
		}

		// no need to check, etc. as it cannot have any patterns yet

		endBusy();

		return session;
	}

	synchronized void deleteSession(DataSession sessionin)
	{
		beginBusy();
		// better hope this is DataSessionImpl
		DataSessionImpl session = (DataSessionImpl) sessionin;
		// make sure all of its patterns are removed
		// (be aware that we could be in checkEvent)
		int i;
		synchronized (session)
		{
			for (i = 0; i < session.addingPatterns.length; i++)
			{
				if (!session.addingPatterns[i].deleteOnCheck)
				{
					System.err.println("Note: DataDelegate::deleteSession for currently adding "
							+ "pattern - commuted to deleteOnCheck");
					session.addingPatterns[i].deleteOnCheck = true;
				}
			}
		}
		while (true)
		{
			GUID id = null;
			synchronized (session)
			{
				if (session.patterns.length > 0)
				{
					id = session.patterns[0].id;
				}
			}
			if (id == null)
			{
				break;
			}
			if (debugCheck)
			{
				System.err.println("Note: DataDelegate::deleteSession removing a pattern");
			}
			// deletePattern (.checkPattern) should do the worrying about
			// checkEvent in progress
			session.deletePattern(id);
			synchronized (session)
			{
				if (session.patterns.length > 0 &&
						id.equals(session.patterns[0].id))
				{
					System.err.println("INTERNAL ERROR: DataDelegate::deleteSession deletePattern "
							+ "did not appear to work - remaining patterns cannot be "
							+ "removed correctly");
					break;
				}
			} // sync(session)
		} // while true

		if (!session.delegateRemove(this))
		{
			System.err.println("ERROR: DataDelegate::removeSession failed");
			endBusy();
			return;
		}
		for (i = 0; i < sessions.size(); i++)
		{
			DataSession si = sessions.get(i);
			if (session == si)
			{
				sessions.remove(i);
				if (debugCheck)
				{
					System.err.println("DataSession.deleteSession " + i);
				}

				// compensate if checkEvent is in progress
				if (checkEventEvent != null)
				{
					if (checkEventSession > i)
					// list is now shorter
					{
						checkEventSession--;
					}
					else if (checkEventSession == i)
					{
						// was this - make it start the next one (will do ++ as next op)
						checkEventPattern = -1;
					}
				}

				// no longer need to check as we have removed its patterns

				endBusy();
				return;
			}
		}
		System.err.println("ERROR: DataDelegate::deleteSession could not find session");
		endBusy();
	}


	// status feedback
	void setStatus(boolean serverFlag,
	               boolean clientConnectedFlag,
	               boolean clientSlowFlag)
	{
		boolean createFlag = false;
		synchronized (this)
		{
			if (statusId == null)
			{
				statusId = guidFactory.getUnique();
				createFlag = true;
			}
		}

		System.err.println
				("NOTE: DataDelegate::setStatus: "
						+ (serverFlag ? "SERVER" : "CLIENT") + ", "
						+ (clientConnectedFlag ? "CONNECTED" : "UNCONNECTED") + ", "
						+ (clientSlowFlag ? "SLOW" : "OK"));

		// server -> get unique id -> check for custom config...
		if (serverFlag && proxy != null &&
				proxy instanceof Server)
		{
			Server server = (Server) proxy;
			SimpleMoniker moniker = server.getMoniker();
			ServerURL surl = new ServerURL(moniker);
			String realurl = surl.getURL();
			StringBuilder escurl = new StringBuilder(realurl);
			int i;
			for (i = 0; i < escurl.length(); i++)
			{
				if (escurl.charAt(i) == ':' ||
						escurl.charAt(i) == '=' ||
						escurl.charAt(i) == ' ')
				{
					escurl.setCharAt(i, '_');
				}
			}
			String url = escurl.toString();
			System.err.println("Server started as " + realurl + ": checking config \"" + url + "\"...");
			ConfigManager config = (ConfigManager) SingletonManager.get(equip.config.ConfigManagerImpl.class.getName());
			for (i = 1; ; i++)
			{
				String customStateId = config.getStringValue(url + ".dataStore" + i + "Name", null);
				if (customStateId != null)
				{
					String customStateClass = config.getStringValue(url + ".dataStore" + i + "Class", null);
					if (customStateClass == null)
					{
						System.err.println("ERROR: " + url + ".dataStore" + i + "Class should be specified for " +
								url + ".dataStore" + i + "Name = " + customStateId);
					}
					else
					{
						try
						{
							Class<IDataStore> clazz = (Class<IDataStore>) Class.forName(customStateClass);
							IDataStore store = clazz.getConstructor(new Class[]{String.class, GUID.class}).
									newInstance(customStateId, responsible);
							stores.add(i - 1, store);
							System.err.println("Added store " + customStateClass + " " + customStateId);
						}
						catch (Exception e)
						{
							System.err.println("ERROR Adding store " + customStateClass + " " + customStateId);
							e.printStackTrace(System.err);
						}
					}
				}
				else
				{
					break;
				}
			}
		}

		DataspaceStatus status = new DataspaceStatusImpl();
		status.serverFlag = serverFlag;
		status.clientConnectedFlag = clientConnectedFlag;
		status.clientSlowFlag = clientSlowFlag;
		DataspaceStatusItem item = new DataspaceStatusItemImpl();
		item.id = statusId;
		item.data = status;
		if (createFlag)
		{
			AddEvent event = new AddEventImpl();
			// NB local
			event.initFromItem(item, responsible, LockType.LOCK_HARD, true, true, null);
			addEvent(event);
		}
		else
		{
			UpdateEvent event = new UpdateEventImpl();
			// local and unreliable
			event.initFromItem2(item, responsible, true, false, 0);
			addEvent(event);
		}
	}

	// internal API - called from DataSessionImpl
	void checkPattern(DataSession session, EventPattern pattern,
	                  boolean addPatternFlag)
	{
		beginBusy();
		checkPattern(session, pattern, addPatternFlag, false);
		endBusy();
	}

	// internal operations
	private void fixResponsible(Event event)
	{
		if (event == null)
		{
			return;
		}
		if (event.metadata == null)
		{
			System.err.println("Warning: DataDelegate::addEvent had no metadata");
			event.metadata = new EventMetadataImpl();
			event.metadata.init2(responsible, false, true, 0);
		}
		if (event.metadata.source == null)
		{
			GUID sourceId = responsible;
			if (sourceId == null)
			{
				System.err.println("Warning: DataDelegate::addEvent cannot get source id");
			}
			event.metadata.source = sourceId;
		}
		if (!(event instanceof AddEvent))
		{
			return;
		}
		// scope of event
		AddEvent add = (AddEvent) event;
		if (add != null && add.binding != null &&
				add.binding.info != null &&
				add.binding.info.responsible == null)
		{
			add.binding.info.responsible = responsible;
		}
	}

	private synchronized void addEventInternal(Event event)
	{

		// can only proceed if we are not (otherwise) busy (i.e. busyCount==1)
		while (busyCount > 1)
		{
			try
			{
				this.wait();
			}
			catch (Exception e)
			{
			}
		}

		if (event instanceof AddEvent)
		{
			// add?
			AddEvent add = (AddEvent) event;
			if (add.binding == null ||
					add.binding.item == null ||
					add.binding.info == null ||
					add.binding.item.id == null)
			{
				System.err.println("ERROR: DataDelegate::addEvent add " +
						"with missing info - ignored");
				return;
			}

			Iterator<IDataStore> storei = stores.iterator();
			// try stores in order
			boolean handled = false;
			while (!handled && storei.hasNext())
			{
				IDataStore store = (storei.next());
				if (store.checkAdd(add))
				{
					handled = store.handleAdd(add);
				}
			}
			if (!handled)
			{
				System.err.println("ERROR: DataDelegate::addEvent could not find store to handle add event");
				return;
			}

			// notify?!
			if (add.binding.item instanceof ActiveTreeNode)
			{
				ActiveTreeNode active = (ActiveTreeNode) add.binding.item;
				active.notifyDataspaceAdd(proxy);
			}

			if (debug)
			{
				System.err.println("- added item " +
						add.binding.item +
						" " + add.binding.item.id);
			}
			return;
		}
		if (event instanceof DeleteEvent)
		{
			// delete?
			DeleteEvent del = (DeleteEvent) event;

			ItemBinding binding = getItemBinding(del.id);

			Iterator<IDataStore> storei = stores.iterator();
			// try stores in order
			boolean handled = false;
			while (!handled && storei.hasNext())
			{
				IDataStore store = (storei.next());
				if (store.handleDelete(del))
				{
					handled = true;
				}
			}
			if (!handled)
			{
				System.err.println("ERROR: DataDelegate::addEvent could not find " +
						"store to handle delete event for " + del.id);
				return;
			}

			// notify?!
			if (binding.item instanceof ActiveTreeNode)
			{
				ActiveTreeNode active = (ActiveTreeNode) binding.item;
				active.notifyDataspaceDelete(proxy);
			}

			if (debug)
			{
				System.err.println("- deleted item " + del.id);
			}
			return;
		}
		if (event instanceof UpdateEvent)
		{
			// update?
			UpdateEvent update = (UpdateEvent) event;
			if (update.item == null ||
					update.item.id == null)
			{
				System.err.println("ERROR: DataDelegate::addEvent update " +
						"with missing info - ignored");
				return;
			}

			ItemBinding oldBinding = getItemBinding(update.item.id);

			Iterator<IDataStore> storei = stores.iterator();
			// try stores in order
			boolean handled = false;
			while (!handled && storei.hasNext())
			{
				IDataStore store = (storei.next());
				if (store.handleUpdate(update))
				{
					handled = true;
				}
			}
			if (!handled)
			{
				System.err.println("ERROR: DataDelegate::addEvent could not find " +
						"store to handle update event for " + update.item.id);
				return;
			}

			ItemBinding binding = getItemBinding(update.item.id);

			// notify?!
			if (binding.item instanceof ActiveTreeNode)
			{
				ActiveTreeNode active = (ActiveTreeNode) binding.item;
				active.notifyDataspaceUpdate(proxy, oldBinding.item);
			}
			if (oldBinding.item instanceof ActiveTreeNode)
			{
				ActiveTreeNode active = (ActiveTreeNode) oldBinding.item;
				active.notifyDataspaceReplaced(proxy);
			}

			if (debug)
			{
				System.err.println("- updated item " + update.item.id);
			}
			return;
		}
		if (event instanceof RemoveResponsible)
		{
			// remove agent - trigger GC?
			RemoveResponsible remove = (RemoveResponsible) (event);
			if (remove.responsible == null)
			{
				System.err.println("ERROR: DataDelegate::addEvent remove responsible "
						+ "with missing info - ignored");
				return;
			}
			System.err.println("Note: remove responsible for " +
					(remove.inverseFlag ? "ALL EXCEPT " : " ") +
					remove.responsible + "");
			// remove from pending queue
			int eviter, evnext;
			for (eviter = 0;
			     eviter < pendingEvents.size(); eviter++)
			{
				Event eventpend = pendingEvents.get(eviter);
				if (eventpend instanceof AddEvent)
				{
					AddEvent a = (AddEvent) (eventpend);
					if (a.binding != null &&
							a.binding.info != null &&
							(remove.inverseFlag || a.binding.info.processBound) &&
							a.binding.info.responsible != null &&
							((!remove.inverseFlag &&
									a.binding.info.responsible.equals
											(remove.responsible)) ||
									(remove.inverseFlag &&
											(!a.binding.info.responsible.equals
													(remove.responsible)))))
					{
						// bang
						pendingEvents.remove(eviter);
						eviter--;
						continue;
					}
				}
				GUID id = null;
				if (event instanceof DeleteEvent)
				{
					DeleteEvent d = (DeleteEvent) (event);
					if (d.id != null)
					{
						id = d.id;
					}
				}
				else if (event instanceof UpdateEvent)
				{
					UpdateEvent u = (UpdateEvent) (event);
					if (u.item != null &&
							u.item.id != null)
					{
						id = u.item.id;
					}
				}
				else if (event instanceof MutateEvent)
				{
					MutateEvent m = (MutateEvent) (event);
					if (m.id != null)
					{
						id = m.id;
					}
				}
				if (id != null)
				{
					ItemBinding binding = getItemBinding(id);
					if (binding != null &&
							binding.info != null &&
							(remove.inverseFlag || binding.info.processBound) &&
							binding.info.responsible != null &&
							((!remove.inverseFlag &&
									binding.info.responsible.equals
											(remove.responsible)) ||
									(remove.inverseFlag &&
											(!binding.info.responsible.equals
													(remove.responsible)))))
					{
						// bang
						pendingEvents.remove(eviter);
						eviter--;
						continue;
					}
				}
			} // eviter

			// try stores in order
			for (IDataStore store : stores)
			{
				// only returns items to be deleted/matched
				Iterable<ItemBinding> set = store.getRemoveResponsibleGUIDs(remove);
				for(ItemBinding binding: set)
				{
					queueDeleteHaveLock(binding.item.id);
				}
			}
			return;
		}
		if (debug)
		{
			System.err.println("- ignored event of type " + event);
		}
	}

	/**
	 * 'soon'
	 */
	public static int TRUNCATE_EXPIRE_TIME_S = 30;

	/**
	 * reduce lease on all leased items with given responsible id to
	 * expire 'soon' (long enough for replication)
	 *
	 * @param responsible required item responsible id
	 * @return none.
	 */
	public void truncateExpireTimes(GUID responsible)
	{
		equip.runtime.Time expire = new equip.runtime.TimeImpl();
		expire.getCurrentTime();
		expire.sec += TRUNCATE_EXPIRE_TIME_S;
		System.err.println("truncateExpireTimes for " + responsible + " to " + expire);
		for (IDataStore store : stores)
		{
			store.truncateExpireTimes(responsible, expire);
		}
	}

	// event in flight (checkEvent) & current pattern check
	Event checkEventEvent = null;
	int checkEventSession, checkEventPattern;

	private void checkEvent(Event event, List<PostCallbackInfo> postCallbackList)
	{
		if (debugCheck)
		{
			System.err.println("DataDelegate.checkEvent "
					+ event.getClass().getName());
		}
		// event item?
		ItemData item = null;
		ItemData item2 = null;
		GUID id = null;
		if (event instanceof AddEvent)
		{
			AddEvent add = (AddEvent) event;
			if (add.binding != null &&
					add.binding.item != null)
			{
				item = add.binding.item;
				// add for lease
				ItemBinding oldItemBinding = getItemBinding(item.id);
				if (oldItemBinding != null)
				{
					// after
					item2 = item;
					// before
					item = oldItemBinding.item;
				}
			}
		}
		else if (event instanceof DeleteEvent)
		{
			DeleteEvent del = (DeleteEvent) event;
			id = del.id;
			item = getItem(id);
			if (item == null)
			{
				System.err.println("DataDelegate::checkEvent ignoring delete for non-existant "
						+ "item " + del.id);
				return;
			}
		}
		else if (event instanceof UpdateEvent)
		{
			UpdateEvent update = (UpdateEvent) event;
			// old value vs new value ?!!
			if (update.item != null)
			{
				id = update.item.id;
			}
			// subsequent value
			item2 = update.item;
			// ....
		}
		else if (event instanceof MutateEvent)
		{
			MutateEvent mutate = (MutateEvent) event;
			id = mutate.id;
		}
		if (item == null && id != null)
		{
			item = getItem(id);
		}

		beginBusy();
		// only this routine changes this, and only one can be in progress
		checkEventEvent = event;
		// check all sessions against this event
		// Think carefully about what happens if the check notifies
		// and the notify add/removes sessions/patterns...
		//
		// If a pattern is added then it will be checked (prior to the
		// enactment of this event (e.g. add/delete/update)
		// The desired semantics is that...

		// make use of checkEventSession & checkEventPattern atomic w.r.t
		// add/remove session/pattern to ensure correct iteration/compensation.

		checkEventSession = 0;
		checkEventPattern = 0;
		boolean endPattern = false, endSession = false;
		while (!endSession)
		{
			endPattern = false;
			checkEventPattern = 0;
			while (!endPattern)
			{
				DataSession session = null;
				EventPattern pattern = null;
				synchronized (this)
				{
					if (checkEventSession >= sessions.size())
					{
						endPattern = endSession = true;
						break;
					}
					// check all patterns in session
					session = sessions.get(checkEventSession);

					if (checkEventPattern >= session.patterns.length)
					{
						endPattern = true;
						break;
					}

					pattern = session.patterns[checkEventPattern];
				}
				if (debugCheck)
				{
					System.err.println("- check against session "
							+ checkEventSession + "/" +
							sessions.size() + " pattern "
							+ checkEventPattern + "/" +
							session.patterns.length);
				}
				checkEvent(event, item, item2, pattern, session, postCallbackList);

				synchronized (this)
				{
					if (checkEventSession >= sessions.size())
					{
						endSession = true;
						// lost our last session
						break;
					}
					checkEventPattern++;
				}
			} // while(!endPattern)
			synchronized (this)
			{
				checkEventSession++;
			}
		} // while(!endSession)

		checkEventEvent = null;
		endBusy();
	}

	private void callPostCallbacks(List<PostCallbackInfo> postCallbackList)
	{
		// post callbacks
		while (postCallbackList.size() > 0)
		{
			PostCallbackInfo cb = postCallbackList.get(0);
			postCallbackList.remove(0);

			if (debugCheck)
			{
				System.err.println("- Call post notify");
			}

			cb.call();
		}
	}

	private void
	checkPattern(DataSession session, EventPattern pattern,
	             boolean addPatternFlag, boolean doingSessionFlag)
	{
		beginBusy();

		// ensure that pattern has an id
		if (pattern.id == null)
		{
			System.err.println("Warning: pattern had no id - initialising");
			pattern.id = guidFactory.getUnique();
		}

		// add/remove the pattern itself...
		// delete on check, done and local patterns are not propagated.
		// deleteOnMatch patterns need more work, so we make them local for now.
		// (need to migrate responsibility if it has failed locally)
		// ** for now we avoid publishing local events at allbecause we do not
		// know if this is a remote pattern being reused by a data delegate peer.
		// (in which case it will already have been added).
		//  Extra hack - removePattern during add now sets deleteOnCheck,
		// so we will publish deletes even for deleteOnCheck. However
		// this delete will fail on real deleteOnCheck patterns. Ho hum.
		boolean publishPattern =
				(!pattern.deleteOnCheck || !addPatternFlag) && !pattern.local;
		boolean localPattern = pattern.deleteOnCheck || pattern.matched
				|| pattern.local || pattern.deleteOnMatch;

		if (debugCheck)
		{
			System.err.println("DataDelegate::checkPattern: publish? " +
					publishPattern + ", local? " + localPattern +
					", add? " + addPatternFlag);
		}

		if (publishPattern && addPatternFlag)
		{
			AddEvent add = new AddEventImpl();
			add.initFromItem(pattern, responsible, LockType.LOCK_HARD,
					true, localPattern, null);
			if (debug)
			{
				System.err.println("Add pattern item");
			}
			addEvent(add);
		}
		else if (publishPattern)
		{
			// ensure that pattern has an id
			if (pattern.id == null)
			{
				System.err.println("ERROR: removed pattern had no id");
			}
			else
			{
				DeleteEvent del = new DeleteEventImpl();
				del.initFromID(pattern.id, responsible, localPattern);
				if (debug)
				{
					System.err.println("Delete pattern item");
				}
				addEvent(del);
			}
		}

		// if add then fake add events, else fake delete events
		AddEvent add = null;
		DeleteEvent del = null;
		Event event = null;
		if (addPatternFlag)
		{
			// first, is this necessary? i.e. are there any AddEvents with
			// wildcard or PRESENT kinds
			// or a pure wildcard event?
			int i;
			for (i = 0; i < pattern.eventTemplates.length; i++)
			{
				if (pattern.eventTemplates[i].getClass().getName().
						equals("equip.data.EventImpl"))
				{
					Event ev = (equip.data.EventImpl) (pattern.eventTemplates[i]);
					if (ev.metadata == null)
					{
						System.err.println("Wildcard pattern generates pseudo-adds");
						break;
					}
				}
				if (!(pattern.eventTemplates[i] instanceof AddEvent))
				{
					continue;
				}
				AddEvent check = (AddEvent) (pattern.eventTemplates[i]);
				if (check != null &&
						(check.kind == null ||
								check.kind.data == ItemEventKind.EQDATA_KIND_PRESENT))
				// ok
				{
					break;
				}
			}
			if (i >= pattern.eventTemplates.length)
			{
				// no
				endBusy();
				return;
			}
		}
		else
		{
			// first, is this necessary? i.e. are there any AddEvents with
			// wildcard or PRESENT kinds
			// or a pure wildcard event?
			int i;
			for (i = 0; i < pattern.eventTemplates.length; i++)
			{
				if (pattern.eventTemplates[i].getClass().getName().
						equals("equip.data.EventImpl"))
				{
					Event ev = (equip.data.EventImpl) (pattern.eventTemplates[i]);
					if (ev.metadata == null)
					{
						System.err.println("Wildcard pattern generates pseudo-adds");
						break;
					}
				}
				if (!(pattern.eventTemplates[i] instanceof DeleteEvent))
				{
					continue;
				}
				DeleteEvent check = (DeleteEvent) (pattern.eventTemplates[i]);
				if (check != null &&
						(check.kind == null ||
								check.kind.data == ItemEventKind.EQDATA_KIND_PRESENT))
				// ok
				{
					break;
				}
			}
			if (i >= pattern.eventTemplates.length)
			{
				// no
				endBusy();
				return;
			}
		}

		// iterate database
		// Hmm. Maybe this should be top-down for Node types?!
		ItemEventKindBox kind = new ItemEventKindBoxImpl();
		kind.data = ItemEventKind.EQDATA_KIND_PRESENT;
		EventMetadata metadata = new EventMetadataImpl();
		metadata.init2(null, false, true, 0);
		metadata.source = responsible;

		for (IDataStore store : stores)
		{
			Iterable<ItemBinding> set = store.getCandidateItemBindings(pattern.itemTemplates);
			if (set == null)
			{
				continue;
			}
			for(ItemBinding b: set)
			{
				// fix up pseudo event
				if (addPatternFlag)
				{
					if (add == null)
					{
						add = new AddEventImpl();
						add.kind = kind;
						add.metadata = metadata;
						event = add;
					}
					add.binding = b;
				}
				else
				{
					if (del == null)
					{
						del = new DeleteEventImpl();
						del.kind = kind;
						del.metadata = metadata;
						event = del;
					}
					del.id = b.item.id;
				}
				event.metadata.source = b.info.responsible;
				if (checkEvent(event, b.item, null, pattern, session))
				{
					// matched - ensure pseudo-events are regenerated
					add = null;
					del = null;
				}
				if (pattern.deleteOnMatch && pattern.matched)
				// done enough
				{
					break;
				}
			}
		}
		endBusy();
	}

	/*package*/ boolean checkEvent(Event event,
	                               EventPattern pattern,
	                               DataSession session)
	{
		// event item?
		ItemData item = null, item2 = null;
		GUID id = null;
		if (event instanceof AddEvent)
		{
			AddEvent add = (AddEvent) event;
			if (add.binding != null &&
					add.binding.item != null)
			{
				item = add.binding.item;
			}
		}
		else if (event instanceof DeleteEvent)
		{
			DeleteEvent del = (DeleteEvent) event;
			id = del.id;
			item = getItem(id);
			if (item == null)
			{
				System.err.println("DataDelegate::checkEvent ignoring delete for non-existant "
						+ "item " + del.id);
				return false;
			}
		}
		else if (event instanceof UpdateEvent)
		{
			UpdateEvent update = (UpdateEvent) event;
			// old value vs new value ?!!
			if (update.item != null)
			{
				id = update.item.id;
			}
			// subsequent value
			item2 = update.item;
			// ....
		}
		else if (event instanceof MutateEvent)
		{
			MutateEvent mutate = (MutateEvent) event;
			id = mutate.id;
		}
		if (item == null && id != null)
		{
			item = getItem(id);
		}

		return checkEvent(event, item, item2, pattern, session);
	}

	boolean checkEvent(Event event, ItemData item,
	                   EventPattern pattern,
	                   DataSession session)
	{
		return checkEvent(event, item, null, pattern, session, null);
	}

	private boolean checkEvent(Event event, ItemData item,
	                           ItemData itemPostUpdate,
	                           EventPattern pattern,
	                           DataSession session)
	{
		return checkEvent(event, item, itemPostUpdate, pattern, session,
				null);
	}

	/**
	 * nested class for post callback
	 */
	static class PostCallbackInfo
	{
		DataCallbackPost target;
		Event event;
		EventPattern pattern;
		boolean patternDeleted;
		DataSession session;
		DataProxy dataspace;
		ItemData oldValue;
		ItemBinding oldBinding;
		equip.runtime.ValueBase closure;

		PostCallbackInfo(DataCallbackPost target,
		                 Event event, EventPattern pattern,
		                 boolean patternDeleted,
		                 DataSession session,
		                 DataProxy dataspace,
		                 ItemData oldValue,
		                 ItemBinding oldBinding,
		                 equip.runtime.ValueBase closure)
		{
			this.target = target;
			this.event = event;
			this.pattern = pattern;
			this.patternDeleted = patternDeleted;
			this.session = session;
			this.dataspace = dataspace;
			this.oldValue = oldValue;
			this.oldBinding = oldBinding;
			this.closure = closure;
		}

		void call()
		{
			if (target != null)
			{
				try
				{
					target.notifyPost(event, pattern, patternDeleted,
							session, dataspace, oldValue,
							oldBinding, closure);
				}
				catch (Exception e)
				{
					System.err.println("ERROR in event notify callback: " + e);
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private boolean checkEvent(Event event, ItemData item,
	                           ItemData itemPostUpdate,
	                           EventPattern pattern,
	                           DataSession session,
	                           List<PostCallbackInfo> postCallbackList)
	{
		boolean notified = false;
		// check a single event
		int i;
		if (pattern.matched)
		// no need to repeat
		{
			return notified;
		}
		boolean matchesBeforeUpdate = false;
		for (i = 0; i < pattern.eventTemplates.length; i++)
		{
			if (!pattern.eventTemplates[i].matches(event))
			// no match
			{
				continue;
			}
			if (item != null)
			{
				int ii;
				for (ii = 0; pattern.itemTemplates != null &&
						ii < pattern.itemTemplates.length; ii++)
				{
					if (pattern.itemTemplates[ii].matches(item))
					// match
					{
						break;
					}
				}
				if (ii > 0 && ii >= pattern.itemTemplates.length)
				// no match
				{
					continue;
				}
			}
			// match
			break;
		}
		if (i >= pattern.eventTemplates.length && i > 0)
		{
			// no match
			if (itemPostUpdate == null)
			{
				return notified;
			}
			// update - may match after
			matchesBeforeUpdate = false;
		}
		else
		{
			matchesBeforeUpdate = true;
		}

		boolean matchesAfterUpdate = true;
		if (itemPostUpdate != null)
		{
			// update - may match after update
			for (i = 0; i < pattern.eventTemplates.length; i++)
			{
				if (!pattern.eventTemplates[i].matches(event))
				// no match
				{
					continue;
				}
				int ii;
				for (ii = 0; pattern.itemTemplates != null &&
						ii < pattern.itemTemplates.length; ii++)
				{
					if (pattern.itemTemplates[ii].matches(itemPostUpdate))
					// match
					{
						break;
					}
				}
				if (ii > 0 && ii >= pattern.itemTemplates.length)
				// no match
				{
					continue;
				}
				// match
				break;
			}
			if (i >= pattern.eventTemplates.length && i > 0)
			{
				// no match
				if (!matchesBeforeUpdate)
				{
					return notified;
				}
				// update - did match before
				matchesAfterUpdate = false;
			}
			else
			{
				matchesAfterUpdate = true;
			}

			// turn into pseudo-add or pseudo-delete?
			if (matchesAfterUpdate && !matchesBeforeUpdate && !(event instanceof AddEvent))
			{
				// fake add
				AddEvent add = new AddEventImpl();
				add.kind = new ItemEventKindBoxImpl();
				add.kind.data = ItemEventKind.EQDATA_KIND_PRESENT;
				if (event instanceof UpdateEvent)
				{
					add.metadata = ((UpdateEvent) event).metadata;
				}
				else
				{
					add.metadata = new EventMetadataImpl();
					add.metadata.init2(null, false, true, 0);
					add.metadata.source = responsible;
				}
				add.binding = new ItemBindingImpl();
				add.binding.item = itemPostUpdate;
				ItemBinding oldBinding = getItemBinding(item.id);
				add.binding.info = oldBinding.info;
				event = add;
			}
			else if (matchesBeforeUpdate && !matchesAfterUpdate && !(event instanceof DeleteEvent))
			{
				// fake delete
				DeleteEvent del = new DeleteEventImpl();
				del.kind = new ItemEventKindBoxImpl();
				del.kind.data = ItemEventKind.EQDATA_KIND_PRESENT;
				if (event instanceof UpdateEvent)
				{
					del.metadata = ((UpdateEvent) event).metadata;
				}
				else
				{
					del.metadata = new EventMetadataImpl();
					del.metadata.init2(null, false, true, 0);
					del.metadata.source = responsible;
				}
				del.id = item.id;
				event = del;
			}
			// else must match both -> update (already is)
		}

		// deleteItemOnMatch would succeed?
		if (pattern.deleteItemOnMatch)
		{
			// add event?
			if (event instanceof AddEvent)
			{
				AddEvent add = (AddEvent) event;
				// Either single real add event, or one of a possible
				// sequence of pseudo add-present events.
				//
				// In the first case the add must be completed, and a
				// delete queued behind it, or must be discarded
				// subsequent to this. (If a delete is queued then not
				// subsequent deleteItemOnMatch pattern must be allowed
				// to succeed.) (If event is discarded then should
				// subsequent non-deleteItemOnMatch patterns match it?)
				//
				// In the second case this must be an addPattern or
				// addSession context, and the delete event could be
				// generated and given to addEvent now. (NB provided
				// that the item iterator in checkPattern is fixed up.)
				// (What about other patterns in an addSession? do they
				// see the item?)
				//
				// Either way, it would be odd if a pattern saw the
				// generated delete but not the add! All patterns will see the
				// delete if it is normal; therefore all patterns should see
				// the add.
				//
				// Implies that checkEvent - and this - must know when
				// a real AddEvent has satisfied a deleteItemOnMatch.
				// Similarly, checkSession - and this - must know which
				// item(s) in the database have satisfied a
				// deleteItemOnMatch.
				// Only other context of use is checkPattern.
				//
				// So... add deleting 'flag' in ItemBindingInfo (which is
				// not normally specific for matching).

				synchronized (this)
				{
					if (add.binding == null ||
							add.binding.info == null ||
							add.binding.item == null ||
							add.binding.item.id == null ||
							add.binding.info.deleting)
					{
						// already deleting or cannot be marked - ignore this event
						return notified;
					}

					// would delete succeed?
					// ....

					// mark event/item
					add.binding.info.deleting = true;
				} // sync (this)
				// add to list
				queueDelete(add.binding.item.id);
			}
		}
		boolean patternDeleted = false;
		synchronized (this)
		{
			if (pattern.deleteOnMatch)
			{
				if (pattern.matched)
				{
					return notified;
				}
				pattern.matched = true;
				patternDeleted = true;
			}
		}
		if (session.callback != null)
		{
			if (session.callback instanceof DataCallbackPost)
			{
				ItemData ov = null;
				ItemBinding ob = null;
				GUID id = null;
				if (event instanceof DeleteEvent)
				{
					id = ((DeleteEvent) event).id;
				}
				else if (event instanceof UpdateEvent)
				{
					id = ((UpdateEvent) event).item.id;
				}
				else if (event instanceof AddEvent)
				// in case leased
				{
					id = ((AddEvent) event).binding.item.id;
				}
				if (id != null)
				{
					ob = getItemBinding(id);
					if (ob != null)
					{
						ov = ob.item;
					}
				}
				if (postCallbackList != null)
				{
					postCallbackList.add
							(new PostCallbackInfo((DataCallbackPost) (session.callback),
									event, pattern,
									patternDeleted, session,
									proxy, ov, ob, session.closure));
					if (debugCheck)
					{
						System.err.println("-- checkEvent delay (post) notify");
					}
				}
				else
				{
					try
					{
						((DataCallbackPost) (session.callback))
								.notifyPost(event, pattern, patternDeleted,
										session,
										proxy, ov, ob,
										session.closure);
					}
					catch (Exception e)
					{
						System.err.println("ERROR in event notify callback: " + e);
						e.printStackTrace(System.err);
					}
					if (debugCheck)
					{
						System.err.println("-- checkEvent immediate (post) notified");
					}
				}
			}
			else
			{
				try
				{
					session.callback.notify(event, pattern, patternDeleted,
							session, session.closure);
				}
				catch (Exception e)
				{
					System.err.println("ERROR in event notify callback: " + e);
					e.printStackTrace(System.err);
				}
				if (debugCheck)
				{
					System.err.println("-- checkEvent notified");
				}
			}
			notified = true;
		}
		else
		{
			if (debugCheck)
			{
				System.err.println("-- checkEvent succeeded but no callback");
			}
		}
		return notified;
	}

	public void queueDelete(GUID id)
	{
		DeleteEvent del = new DeleteEventImpl();
		del.initFromID(id, responsible, false);
		fixResponsible(del);
		intQueueEvent(del);
	}

	protected void intQueueEvent(Event event)
	{
		synchronized (this)
		{
			queueEventHaveLock(event);
		}
	}

	public void queueDeleteHaveLock(GUID id)
	{
		DeleteEvent del = new DeleteEventImpl();
		del.initFromID(id, responsible, false);
		fixResponsible(del);
		queueEventHaveLock(del);
	}

	public void queueEventHaveLock(Event event)
	{
		// - see outboundEvents
		// is there an unreliable update event for the same item already in the
		// event queue? if so, we will dump it.
		if (event instanceof UpdateEvent)
		{
			UpdateEvent update1 = (UpdateEvent) (event);
			if (update1.item != null &&
					update1.item.id != null)
			{
				int iter;
				iter = pendingEvents.size();
				if (iter != 0)
				{
					do
					{
						iter--;
						Event eventpend = pendingEvents.get(iter);
						if (eventpend != null &&
								(eventpend instanceof UpdateEvent))
						{
							UpdateEvent update = (UpdateEvent) eventpend;
							if (update.metadata != null &&
									update.item != null &&
									update.item.id != null &&
									update1.item.id.equals(update.item.id))
							{
								if (!update.metadata.reliable)
								{
									if (debug)
									{
										System.err.println("DataDelegate::addEvent: "
												+ "Squash queued unreliable event for "
												+ update1.item.getClass().getName() + " "
												+ update1.item.id);
									}
									pendingEvents.remove(iter);
									break;
								}
								else
								{
									if (debug)
									{
										System.err.println("DataDelegate::addEvent: "
												+ "Could not squash reliable update event for "
												+ update1.item.getClass().getName() + " "
												+ update1.item.id);
									}
								}
							}
						}
					}
					while (!(iter == 0));
				} // not empty
			} // update
		}
		pendingEvents.add(event);
	}

	private class QueuePushThread extends Thread
	{
		public void run()
		{
			int[] myQueuePushSignal = queuePushSignal;
			java.lang.Object myQueuePushLock = queuePushLock;

			System.err.println("DataDelegate::queuePushThreadFn started");
			while (true)
			{
				synchronized (myQueuePushLock)
				{

					if (queuePushSignal[0] != 0)
					// done
					{
						break;
					}

					checkExpiredLeases();

					// push pending events
					doPendingEvents();
				}
				// 50 Hz max networked
				try
				{
					Thread.sleep(20);
				}
				catch (Exception e)
				{
				}
			} // while
			System.err.println("DataDelegate::queuePushThreadFn exiting");
		}
	}

	void terminate()
	{
		queuePushSignal[0] = 1;
		for (IDataStore store : stores)
		{
			store.terminate();
		}
	}

	private void checkExpiredLeases()
	{
		synchronized (this)
		{
			// check expired leases
			Time now = new TimeImpl();
			now.getCurrentTime();
			ItemEventKindBox kind = null;
			EventMetadata metadata = null;

			for (IDataStore store : stores)
			{
				Iterable<GUID> expired = store.getExpiredGUIDs(now);
				if (expired == null)
				{
					continue;
				}

				for(GUID id: expired)
				{
					// synthesis local delete
					DeleteEvent del = new DeleteEventImpl();
					if (kind == null)
					{
						kind = new ItemEventKindBoxImpl();
						kind.data = ItemEventKind.EQDATA_KIND_LEASE_EXPIRE;
						metadata = new EventMetadataImpl();
						// local reliable
						metadata.init2(null, true, true, 0);
						metadata.source = responsible;
					}
					del.kind = kind;
					del.metadata = metadata;
					del.id = id;

					System.err.println("Lease expired on item " + del.id);
					// FIXME check before actually adding that there isn't already
					// a AddEvent/lease renewal in the pending event queue
					addEvent(del);
				}
			}
		}
	}

	private void checkLeaseAdd(Event event)
	{
		if (event instanceof AddEvent)
		{
			AddEvent add = (AddEvent) event;
			// already present?
			if (add.kind == null ||
					add.kind.data != ItemEventKind.EQDATA_KIND_NORMAL)
			{
				return;
			}
			if (add.binding != null && add.binding.info != null &&
					add.binding.info.itemLease != null &&
					add.binding.item != null &&
					add.binding.item.id != null)
			{
				// leased
				ItemBinding binding = getItemBinding(add.binding.item.id);
				if (binding != null)
				{
					// already present
					add.kind.data = ItemEventKind.EQDATA_KIND_LEASE_RENEW;
				}
			}
		}
	}

	// process deleteIds
	private void doPendingEvents()
	{
		int iter;
		while (true)
		{
			Event event = null;
			synchronized (this)
			{
				iter = 0;
				if (iter == pendingEvents.size() ||
						busyCount != 0)
				{
					break;
				}
				event = pendingEvents.get(iter);
				pendingEvents.remove(iter);

				// from addEvent(event);
				busyCount++;
			}

			if (!validateEventFinal(event))
			{
				System.err.println("ERROR: DataDelegate::addEvent rejected invalid event (class "
						+ event.getClass().getName() + ") - final check");
			}
			else
			{
				// post callbacks
				Vector<PostCallbackInfo> postCallbackList = new Vector<PostCallbackInfo>();

				checkLeaseAdd(event);
				// callbacks?
				checkEvent(event, postCallbackList);
				// do the work
				addEventInternal(event);

				callPostCallbacks(postCallbackList);
				try
				{
					// yield
					Thread.sleep(0);
				}
				catch (Exception e)
				{
				}
			}

			synchronized (this)
			{
				busyCount--;
				if (busyCount <= 1)
				{
					try
					{
						this.notifyAll();
					}
					catch (Exception e)
					{
					}
				}
			} // sync(this)
		}
	}

	/* peer management */
	// get 'responsible' id for this
	GUID getPeerId()
	{
		return responsible;
	}

	// return null if unknown
	synchronized DataDelegatePeer findPeer(GUID peerId)
	{
		return peerMap.get(peerId);
	}

	// (if doesn't exist)
	synchronized DataDelegatePeer createPeer(GUID peerId, boolean serverFlag)
	{
		DataDelegatePeer peer = findPeer(peerId);
		if (peer == null)
		{
			peer = new DataDelegatePeer(this, peerId, serverFlag);
			peerMap.put(peerId, peer);
		}
		return peer;
	}

	// remove/delete; TRUE = found ok
	boolean deletePeer(GUID peerId)
	{
		DataDelegatePeer peer = null;
		synchronized (this)
		{
			peer = peerMap.get(peerId);
			if (peer == null)
			{
				return false;
			}
			peerMap.remove(peerId);
		}
		// unlocked... may try to reaquire lock, etc.
		if (peer != null)
		{
			peer.shutdown();
		}
		return true;
	}

	public DataDelegate()
	{
		queuePushThread = new QueuePushThread();
		queuePushThread.start();
		guidFactory = new GUIDFactoryImpl();
		responsible = guidFactory.getUnique();
		// default store
		stores.add(new MemoryDataStore());
		System.err.println("DataDelegate::DataDelegate created with id " +
				responsible);
	}

	public void setProxy(DataProxy proxy)
	{
		// unique ID?
		// -> custom DataStores?
		// ....
		this.proxy = proxy;
	}

	/// only  useful if called early in lifecycle
	void setResponsible(GUID id)
	{
		responsible = id;
	}

	// finalise queuePushThread??
	/// wait for all pending events
	public void waitForEvents(boolean local)
	{
		// local
		// wait until not busy?!
		synchronized (this)
		{
			while (busyCount > 0)
			{
				try
				{
					//System.err.println("waitForEvents...");
					wait();
				}
				catch (InterruptedException e)
				{
				}
			}
			//System.err.println("waitForEvents OK");
		}
		// ...?
		if (!local)
		{
			// ....?
		}
		return;
	}
}

