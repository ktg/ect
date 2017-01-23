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

/** The interface to a data item store, used internally by
 * a {@link equip.data.DataDelegate} to implement state storage
 * within a dataspace replica.
 *
 * @author Chris Greenhalgh, 2003-10-27
 */
public interface IDataStore {
    /** request that store handle the Add event, adding to itself
     * accordingly.
     *
     * Note: also has to handle lease updates.
     *
     * @param add The {@link AddEvent} to be handled.
     * @return true iff this store has handled the add event; false if declined.
     *         (normally as indicated by a call to {@link IDataStore#checkAdd}
     */
    boolean handleAdd(AddEvent add);

    /** check if store would like to/be prepared to handle this
     * {@link AddEvent}.
     *
     * @param add The {@link AddEvent} to be handled.
     * @return true iff this store would be happy/able to handle it.
     */
    boolean checkAdd(AddEvent add);
    
    /** check if store is maintaining state for the given {@link GUID}.
     *
     * @param id The GUID of the data item in question.
     * @return true iff the store currently has state for this item.
     */
    boolean holdsGUID(GUID id);
	    
    /** request that store handles the given update, which it must 
     * iff it is currently maintaining state for it.
     *
     * Note: implies currently no migration of item state between
     * store(s).
     *
     * @param upd the {@link UpdateEvent} to be handled.
     * @return true iff the update has been handled (and necessarily
     *         the item's state is maintained by this store).
     */
    boolean handleUpdate(UpdateEvent upd);

    /** request that store handles the given delete, which it must 
     * iff it is currently maintaining state for the corresponding 
     * data item.
     *
     * @param del the {@link DeleteEvent} to be handled.
     * @return true iff the delete has been handled (and necessarily
     *         the item's state was maintained by this store).
     */
    boolean handleDelete(DeleteEvent del);

    /** get GUIDs of data items in this store which are process bound
     * to the given responsible ID as per the RemoveResponsible event
     * (or not, according to inverse flag).
     *
     * @param remove the {@link RemoveResponsible} event.
     * @return an Enumeration of the GUIDs of locally maintained
     *         data items that should now be deleted.
     */
    Iterable<ItemBinding> getRemoveResponsibleGUIDs(RemoveResponsible remove);

    /** get the ItemBinding for the given id iff it is maintained by
     * this store, else null.
     *
     * @param id the id of the data item being requested.
     * @return the {@link ItemBinding} for that item, else null iff
     *         unknown to this store.
     */
    ItemBinding getItemBinding(GUID id);

    /** get the ItemBinding maintained by this store which should be 
     * considered when pattern matching the associated itemTemplates
     * for an add/delete while present pattern.
     *
     * @param itemTemplates array of template data items, else 
     *        null or zero length list for a wild-card (any item).
     * @return Enumeration of ItemBindings that should be considered
     *         (guaranteed to be a superset of possible matches).
     */
    Iterable<ItemBinding>  getCandidateItemBindings(ItemData [] itemTemplates);

    /** returns lowest (soonest, or furthest in past) expire time of
     * any leased item in this store.
     *
     * @return lowest (soonest, or furthest in past) expire time, else null (no leased items).
     */
    equip.runtime.Time getFirstExpireTime();

    /** returns GUIDs of all leased items expiring at or before time 
     * 'now'.
     *
     * @param now The current time of the expiration clock.
     * @return Enumeration of {@link GUID}s of now expiring data items
     *         which the call might now reasonably issue delete events
     *         for).
     */
     Iterable<GUID> getExpiredGUIDs(equip.runtime.Time now);

	/** reduce lease on all leased items with given responsible id to
	 * expire at 'expire time'.
	 *
	 * @param responsible required item responsible id
	 * @param expire  the new expire time for matched items
	 * @return none.
	 */
	void truncateExpireTimes(GUID responsible, equip.runtime.Time expire);

	/** terminate - tidy up.
     */
    void terminate();

    /** Flush any pending persistent records
     */
	void flush();
}
//EOF




