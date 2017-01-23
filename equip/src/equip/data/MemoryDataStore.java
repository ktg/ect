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
package equip.data;

import java.util.*;

/**
 * Core implementation of {@link IDataStore} interface which
 * can hold any data items in memory (only).
 * <p>
 * Factors out state maintainance code previously included in
 * {@link DataDelegate} before the introduction of the {@link IDataStore}
 * internal interface.
 *
 * @author Chris Greenhalgh, 2003-10-27
 */
public class MemoryDataStore implements IDataStore
{
	final Map<GUID, ItemBinding> idMap = new HashMap<>();
	private final Map<GUID, List<ItemBinding>> parentMultimap = new HashMap<>();
	private final List<LeasedItem> leasedItems = new ArrayList<>(); // LeasedItem, in order of expire
	private static boolean debug = true;

	static class LeasedItem
	{
		LeasedItem(Lease itemLease, GUID id)
		{
			this.itemLease = itemLease;
			this.id = id;
		}

		Lease itemLease;
		GUID id;
	}

	// helper
	private void addParent(ItemBinding binding)
	{
		if (binding.item == null ||
				!(binding.item instanceof TreeNode))
		{
			return;
		}
		TreeNode node = (TreeNode) binding.item;
		if (node.parent == null)
		{
			node.parent = new GUIDImpl();
			node.parent.setNull();
		}
		synchronized (parentMultimap)
		{
			List<ItemBinding> val = parentMultimap.computeIfAbsent(node.parent, k -> new ArrayList<>());
			val.add(binding);
		}
	}

	void removeParent(ItemBinding binding)
	{
		if (binding.item == null ||
				!(binding.item instanceof TreeNode))
		{
			return;
		}
		TreeNode node = (TreeNode) binding.item;
		Vector list = (Vector) parentMultimap.get(node.parent);
		if (list == null)
		{
			return;
		}
		int i;
		boolean found = false;
		for (i = 0; i < list.size(); i++)
		{
			ItemBinding b = (ItemBinding) list.elementAt(i);
			if (b.item.id.equals(node.id))
			{
				list.removeElementAt(i);
				found = true;
				break;
			}
		}
		if (!found)
		{
			System.err.println("ERROR: DataDelegate::removeParent could " +
					"not find parent entry in map");
		}
	}

	/**
	 * default constructor.
	 */
	public MemoryDataStore()
	{
		// see field initialisers
	}

	/**
	 * request that store handle the Add event, adding to itself
	 * accordingly.
	 * <p>
	 * Note: also has to handle lease updates.
	 *
	 * @param add The {@link AddEvent} to be handled.
	 * @return true iff this store has handled the add event; false if declined.
	 * (normally as indicated by a call to {@link IDataStore#checkAdd}
	 */
	public synchronized boolean handleAdd(AddEvent add)
	{
		GUID id = add.binding.item.id;
		ItemBinding oldBinding = getItemBinding(add.binding.item.id);

		LeasedItem leasedItem = null;
		if (oldBinding != null)
		{
			if (oldBinding.info.itemLease == null &&
					add.binding.info.itemLease == null)
			{
				// no lease on old or new - its an error
				System.err.println("ERROR: MemoryDataStore::handleAdd add for " +
						"existing item (" + id + ") - ignored");
				return true;
			}
			// new or old had lease -> replace binding (which put will)
			// change parent?
			removeParent(oldBinding);

			// find in leasedItems and move if required
			int i;
			for (i = 0; i < leasedItems.size(); i++)
			{
				leasedItem = leasedItems.get(i);
				if (leasedItem.id.equals(oldBinding.item.id))
				{
					break;
				}
			}
			if (i < leasedItems.size())
			{
				// found
				leasedItems.remove(i);
			}
			// drop through
		}

		// add to item map
		idMap.put(id, add.binding);

		// add to parent map
		addParent(add.binding);

		if (add.binding.info.itemLease != null)
		{
			if (add.binding.info.itemLease.expireTime == null)
			{
				System.err.println("ERROR: add item " + add.binding.item.id +
						" has lease with null expireTime");
			}
			else
			{
				// add to leasedItems
				if (leasedItem != null)
				{
					leasedItem.itemLease = add.binding.info.itemLease;
				}
				else
				{
					leasedItem =
							new LeasedItem(add.binding.info.itemLease,
									add.binding.item.id);
				}
				int i;
				for (i = 0; i < leasedItems.size(); i++)
				{
					LeasedItem li = leasedItems.get(i);
					if (leasedItem.itemLease.expireTime.sec <
							li.itemLease.expireTime.sec ||
							(leasedItem.itemLease.expireTime.sec ==
									li.itemLease.expireTime.sec &&
									leasedItem.itemLease.expireTime.usec <
											li.itemLease.expireTime.usec))
					{
						break;
					}
				}
				leasedItems.add(i, leasedItem);
				// kick timer(s) if required??
			}
		}
		return true;
	}

	/**
	 * check if store would like to/be prepared to handle this
	 * {@link AddEvent}.
	 *
	 * @param add The {@link AddEvent} to be handled.
	 * @return true iff this store would be happy/able to handle it.
	 */
	public boolean checkAdd(AddEvent add)
	{
		// any
		return true;
	}

	/**
	 * check if store is maintaining state for the given {@link GUID}.
	 *
	 * @param id The GUID of the data item in question.
	 * @return true iff the store currently has state for this item.
	 */
	public boolean holdsGUID(GUID id)
	{
		return getItemBinding(id) != null;
	}

	/**
	 * request that store handles the given update, which it must
	 * iff it is currently maintaining state for it.
	 * <p>
	 * Note: implies currently no migration of item state between
	 * store(s).
	 *
	 * @param update the {@link UpdateEvent} to be handled.
	 * @return true iff the update has been handled (and necessarily
	 * the item's state is maintained by this store).
	 */
	public synchronized boolean handleUpdate(UpdateEvent update)
	{
		ItemBinding oldBinding = idMap.get(update.item.id);
		if (oldBinding == null)
		{
			// not handled
			return false;
		}

		// change parent?
		removeParent(oldBinding);
		// update
		ItemBinding binding = new ItemBindingImpl();
		binding.item = update.item;
		binding.info = oldBinding.info;

		idMap.put(update.item.id, binding);

		addParent(binding);

		return true;
	}

	/**
	 * request that store handles the given delete, which it must
	 * iff it is currently maintaining state for the corresponding
	 * data item.
	 *
	 * @param del the {@link UpdateEvent} to be handled.
	 * @return true iff the delete has been handled (and necessarily
	 * the item's state was maintained by this store).
	 */
	public synchronized boolean handleDelete(DeleteEvent del)
	{
		// delete
		ItemBinding binding = (ItemBinding) idMap.get(del.id);
		if (binding == null)
		{
			// not known/handled
			return false;
		}

		// leased?
		if (binding.info.itemLease != null)
		{
			int i;
			boolean found = false;
			for (i = 0; i < leasedItems.size(); i++)
			{
				LeasedItem li = leasedItems.get(i);
				if (li.id.equals(del.id))
				{
					leasedItems.remove(i);
					found = true;
					break;
				}
			}
			if (!found)
			{
				// debug
				if (debug)
				{
					System.err.println("Note: MemoryDataStore::handleDelete delete " +
							"could not find leased item " + del.id +
							" in leasedItems");
				}
			}
		}

		// owner?
		// ....

		// remove from parent map
		removeParent(binding);

		// remove
		idMap.remove(del.id);

		return true;
	}

	/**
	 * get GUIDs of data items in this store which are process bound
	 * to the given responsible ID as per the RemoveResponsible event
	 * (or not, according to inverse flag).
	 *
	 * @param remove the {@link RemoveResponsible} event.
	 * @return an Enumeration of the GUIDs of locally maintained
	 * data items that should now be deleted.
	 */
	public synchronized Iterable<ItemBinding> getRemoveResponsibleGUIDs(final RemoveResponsible remove)
	{
		return () -> new Iterator<ItemBinding>()
		{
			Iterator<ItemBinding> set = idMap.values().iterator();
			ItemBinding next = null;

			@Override
			public boolean hasNext()
			{
				if (next != null)
				{
					return true;
				}

				while (set.hasNext())
				{
					next = set.next();
					if (next != null && next.info != null &&
							(remove.inverseFlag || next.info.processBound) &&
							next.info.responsible != null &&
							((!remove.inverseFlag &&
									next.info.responsible.equals
											(remove.responsible)) ||
									(remove.inverseFlag &&
											(!next.info.responsible.equals
													(remove.responsible)))) &&
							next.item != null && next.item.id != null)
					{
						return true;
					}
				}
				next = null;
				return false;
			}

			@Override
			public ItemBinding next()
			{
				ItemBinding rval = next;
				next = null;
				return rval;
			}
		};
	}

	/**
	 * get the ItemBinding for the given id iff it is maintained by
	 * this store, else null.
	 *
	 * @param id the id of the data item being requested.
	 * @return the {@link ItemBinding} for that item, else null iff
	 * unknown to this store.
	 */
	public synchronized ItemBinding getItemBinding(GUID id)
	{
		synchronized (idMap)
		{
			ItemBinding b = (ItemBinding) idMap.get(id);
			return b;
		}
	}

	/**
	 * get the ItemBinding maintained by this store which should be
	 * considered when pattern matching the associated itemTemplates
	 * for an add/delete while present pattern.
	 *
	 * @param itemTemplates array of template data items, else
	 *                      null or zero length list for a wild-card (any item).
	 * @return Enumeration of ItemBindings that should be considered
	 * (guaranteed to be a superset of possible matches).
	 */
	public synchronized Iterable<ItemBinding> getCandidateItemBindings(ItemData[] itemTemplates)
	{
		return idMap.values();
	}

	/**
	 * returns lowest (soonest, or furthest in past) expire time of
	 * any leased item in this store.
	 *
	 * @return lowest (soonest, or furthest in past) expire time, else null.
	 */
	public synchronized equip.runtime.Time getFirstExpireTime()
	{
		if (leasedItems.size() == 0)
		{
			return null;
		}
		LeasedItem li = leasedItems.get(0);
		return li.itemLease.expireTime;
	}

	/**
	 * returns GUIDs of all leased items expiring at or before time
	 * 'now'.
	 *
	 * @param now The current time of the expiration clock.
	 * @return Enumeration of {@link GUID}s of now expiring data items
	 * which the call might now reasonably issue delete events
	 * for).
	 */
	public synchronized Iterable<GUID> getExpiredGUIDs(equip.runtime.Time now)
	{
		List<GUID> expired = new ArrayList<>();
		int i;
		for (i = 0; i < leasedItems.size(); i++)
		{
			LeasedItem li = leasedItems.get(i);
			if (now.sec > li.itemLease.expireTime.sec ||
					(now.sec == li.itemLease.expireTime.sec &&
							now.usec >= li.itemLease.expireTime.usec))
			{
				if (debug)
				{
					System.out.println("getExpiredGUIDs returns " + li.id);
				}
				expired.add(li.id);
			}
			else
			{
				break;
			}
		}
		return expired;
	}

	/**
	 * reduce lease on all leased items with given responsible id to
	 * expire at 'expire time'.
	 *
	 * @param responsible required item responsible id
	 * @param expire      the new expire time for matched items
	 * @return none.
	 */
	public void truncateExpireTimes(GUID responsible, equip.runtime.Time expire)
	{
		int i;
		for (i = 0; i < leasedItems.size(); i++)
		{
			LeasedItem li = (LeasedItem) leasedItems.get(i);
			ItemBinding binding = getItemBinding(li.id);
			if (binding == null)
			{
				System.err.println("ERROR: truncateExpireTimes cannot find leased item " + li.id);
				continue;
			}
			if (binding != null &&
					binding.info != null &&
					binding.info.responsible != null &&
					binding.info.responsible.equals(responsible) &&
					binding.info.itemLease != null &&
					binding.info.itemLease.expireTime != null &&
					binding.info.itemLease.expireTime.sec > expire.sec ||
					(binding.info.itemLease.expireTime.sec == expire.sec &&
							binding.info.itemLease.expireTime.usec > expire.usec))
			{
				if (debug)
				{
					System.err.println("MemoryDataStore truncates lease on item " + li.id);
				}
				binding.info.itemLease.expireTime = li.itemLease.expireTime = expire;
				// re-order? - can only move forward
				int j;
				for (j = 0; j < i; j++)
				{
					LeasedItem lj = (LeasedItem) leasedItems.get(j);
					if (li.itemLease.expireTime.sec <
							lj.itemLease.expireTime.sec ||
							(li.itemLease.expireTime.sec ==
									lj.itemLease.expireTime.sec &&
									li.itemLease.expireTime.usec <
											lj.itemLease.expireTime.usec))
					{
						break;
					}
				}
				leasedItems.remove(i);
				leasedItems.add(j, li);
			}
		}
	}

	/**
	 * terminate - tidy up.
	 */
	public synchronized void terminate()
	{
	}

	/**
	 * Flush any pending persistent records
	 */
	public void flush()
	{
	}
}
    
