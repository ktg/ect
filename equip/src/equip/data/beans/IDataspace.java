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
/* Chris Greenhalgh
 * 26 Sept 2002
 */

package equip.data.beans;
 
import equip.data.*;
import equip.runtime.ValueBase;

/** main interface of dataspace client JavaBean
 */ 
public interface IDataspace {

    /*------------------------------------------------------------*
     * item and event publishing operations                       * 
     *------------------------------------------------------------*/

    /** allocated a new GUID for a data item to be published 
     */
    public GUID allocateId(); 

    /** add a globally visible item to the dataspace
     */
    public void add(ItemData item) 
	throws DataspaceInactiveException;

	/** add a (potentially) persistent item to the dataspace,
	 * i.e. not process bound, and normally with a Lease (although null
	 * is permitted)
	 */
	public void addPersistent(ItemData item, Lease lease) 
		throws DataspaceInactiveException;

	/** add a local-only item to the dataspace
     */
    public void addLocal(ItemData item) 
	throws DataspaceInactiveException;

    /** update an item, globally visible, reliable
     */
    public void update(ItemData item) 
	throws DataspaceInactiveException;

    /** update an item, globally visible, optionally reliable
     */
    public void update(ItemData item, boolean reliableFlag) 
	throws DataspaceInactiveException;

    /** update an item, local-only, reliable
     */
    public void updateLocal(ItemData item) 
	throws DataspaceInactiveException;

    /** update an item, local-only, optionally reliable
     */
    public void updateLocal(ItemData item, boolean reliableFlag) 
	throws DataspaceInactiveException;

    /** delete a globally visible item from the dataspace
     */
    public void delete(GUID id) 
	throws DataspaceInactiveException;

    /** delete a local-only item from the dataspace
     */
    public void deleteLocal(GUID id) 
	throws DataspaceInactiveException;

    /** emit an event directly
     */
    public void addEvent(Event event) 
	throws DataspaceInactiveException;

    /*------------------------------------------------------------*
     * query/lookup operations                                    * 
     *------------------------------------------------------------*/

    /** get current value for an item by id - local only.
     */
    public ItemData getItem(GUID id) 
	throws DataspaceInactiveException;

    /** copy collect - local only.
     *
     * returns all known items (local or replicated) matching the 
     * template item.
     */
    public ItemData[] copyCollect(ItemData template) 
	throws DataspaceInactiveException;

    /** copy collect - local only - callback form.
     *
     * returns all known items (local or replicated) matching the 
     * template item. Calls listener once for each item
     */
    public void copyCollect(ItemData template,
			    DataspaceEventListener listener) 
	throws DataspaceInactiveException;

    /** add a item monitor.
     *
     * listener is called with matching events.
     *
     * returned reference is only need to removing the monitor.
     * Its actually a DataSession.
     */
    public DataSession addDataspaceEventListener
	(ItemData template, boolean localFlag, 
	 DataspaceEventListener listener) 
	throws DataspaceInactiveException;

    /** add an event monitor.
     *
     * listener is called with matching events.
     *
     * returned reference is only need to removing the monitor.
     * Its actually a DataSession.
     */
    public DataSession addDataspaceEventListener
	(equip.data.Event template, boolean localFlag, 
	 DataspaceEventListener listener) 
	throws DataspaceInactiveException;

    /** remove a previously added event monitor 
     */
    public void removeDataspaceEventListener
	(equip.data.DataSession session) 
	throws DataspaceInactiveException;

}
/* EOF */

