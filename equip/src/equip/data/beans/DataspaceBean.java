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

import java.beans.*;
import java.beans.beancontext.*;

/** dataspace client JavaBean
 */ 
public class DataspaceBean implements IDataspace, BeanContextChild {

    /*============================================================
     * constructor(s)
     */

    /** no arg bean constructor */
    public DataspaceBean() {
    }

	/** over existing ds bean constructor */
	public DataspaceBean(DataProxy ds) throws DataspaceInactiveException
	{
		this.dataspace = ds;
		this.activeFlag = true;
		init();
	}

	/*============================================================
     * BeanContextChild support
     */

    /** property change support */
    private java.beans.PropertyChangeSupport mPropertyChange = 
	new java.beans.PropertyChangeSupport(this);

    /** vetoable change support */
    private java.beans.VetoableChangeSupport mVetoableChange = 
	new java.beans.VetoableChangeSupport(this);

    /** bean context */
    protected BeanContext mBeanContext;

    /*============================================================
     * BeanContextChild operations
     */

    /** property change api */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	mPropertyChange.addPropertyChangeListener(listener);
    }
    /** property change api */
    public void addPropertyChangeListener(String propertyName, 
					  PropertyChangeListener listener) {
	mPropertyChange.addPropertyChangeListener(propertyName, listener);
    }
    /** property change api */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	mPropertyChange.removePropertyChangeListener(listener);
    }
    /** property change api */
    public void removePropertyChangeListener(String propertyName, 
					     PropertyChangeListener listener) {
	mPropertyChange.removePropertyChangeListener(propertyName, listener);
    }
    /** vetoable change api */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
	mVetoableChange.addVetoableChangeListener(listener);
    }
    /** vetoable change api */
    public void addVetoableChangeListener(String vetoableName, 
					  VetoableChangeListener listener) {
	mVetoableChange.addVetoableChangeListener(vetoableName, listener);
    }
    /** vetoable change api */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
	mVetoableChange.removeVetoableChangeListener(listener);
    }
    /** vetoable change api */
    public void removeVetoableChangeListener(String vetoableName, 
					     VetoableChangeListener listener) {
	mVetoableChange.removeVetoableChangeListener(vetoableName, listener);
    }

    /** bean context getter */
    public BeanContext getBeanContext() {
	return mBeanContext;
    }

    /** bean setter with veto and property events */
    public void setBeanContext(BeanContext beanContext) {
	System.err.println("InputPanelBean.setBeanContext");
	BeanContext oldValue = null;
	synchronized (this) {
	    oldValue = mBeanContext;
	    try {
		mVetoableChange.fireVetoableChange("beanContext",
						   oldValue, beanContext);
	    } catch (PropertyVetoException e) {
		System.err.println("InputPanelBean.setBeanContext vetoed: "+e);
		return;
	    } catch (Exception e) {
		System.err.println("InputPanelBean.setBeanContext veto "+
				   "error exception: "+e);
		e.printStackTrace(System.err);
	    }
	    // set
	    mBeanContext = beanContext;
	}
	// property change
	try {
	    // fire!
	    mPropertyChange.firePropertyChange("beanContext",
					       oldValue,
					       beanContext);
	} catch (Exception e) {
	    System.err.println("ERROR firing beanContext change: "+e);
	    e.printStackTrace(System.err);
	}
    }

    /*============================================================
     * IDataspace support
     */

    /** dataspace URL */
    protected String dataspaceUrl = null;

    /** actual dataspace */
    protected DataProxy dataspace = null;

    /** active flag */
    protected boolean activeFlag = false;
    
    /** GUID factory */
    protected GUIDFactory guids = new GUIDFactoryImpl();

	/** connected?
	 */
	protected boolean connectedFlag = false;
	/** connected session
	 */
	protected DataSession connectedMonitorSession;

	/** async - call BEFORE setDataspaceUrl!
	 */
	public void setRetryConnect(boolean r) 
	{
		retryConnect = r;
	}
	/** async - default no
	 */
	protected boolean retryConnect = false;

	/** dataspace Url setter */
	public void setDataspaceUrl(String dataspaceUrl)  
		throws DataspaceInactiveException 
	{
		setDataspaceUrl(dataspaceUrl, true);
	}
	/** dataspace Url setter */
	public void setDataspaceUrl(String dataspaceUrl, boolean useDataManager)  
		throws DataspaceInactiveException 
	{ 
		String oldValue = null;
		synchronized (this) 
		{
			oldValue = this.dataspaceUrl;
			if (oldValue!=null && dataspaceUrl.equals(oldValue))
				// unchanged
				return;
			if (activeFlag) 
			{
				System.err.println("ERROR: cannot change DataspaceBean url "+
					"(from "+this.dataspaceUrl+" to "+
					dataspaceUrl+")");
				return;
			}
			this.dataspaceUrl = dataspaceUrl;
			System.err.println("Join dataspace "+dataspaceUrl);
	    
			if (useDataManager) 
				dataspace = DataManager.getInstance().getDataspace
					(dataspaceUrl, DataManager.DATASPACE_CLIENT, true, retryConnect);
			else 
			{
				// client
				equip.net.ServerURL surl = new equip.net.ServerURL(dataspaceUrl);
				if (surl.getURL()==null) 
				{
					System.err.println("ERROR: setDataspaceUrl for ill-formed url: "
						+ dataspaceUrl);
					return;
				}
				equip.net.Moniker moniker = surl.getMoniker();

				dataspace = new DataProxyImpl(null);
				dataspace.serviceMoniker = moniker;
				//System.err.println("- Activate...");
				if (retryConnect)
					dataspace.activateAsync();
				else if (!dataspace.activate(null, null)) 
				{
					System.err.println("WARNING: activate failed for dataspace " + dataspaceUrl);
				} 
			}

			if (dataspace==null)
			{
				System.err.println("ERROR: could not join dataspace: "+
					dataspaceUrl);
				this.dataspaceUrl = null;
				throw new DataspaceInactiveException
					("could not join dataspace: "+dataspaceUrl);
			}
			init();
		}
		// property change
		try 
		{
			// fire!
			mPropertyChange.firePropertyChange("dataspaceUrl",
				oldValue,
				dataspaceUrl);
		} 
		catch (Exception e) 
		{
			System.err.println("ERROR firing dataspaceUrl change: "+e);
			e.printStackTrace(System.err);
		}
		setActive(true);
	}
	protected void init() throws DataspaceInactiveException
	{
		synchronized(this) 
		{
			DataspaceStatusItem template = new DataspaceStatusItemImpl();
			template.data = new DataspaceStatusImpl();
			template.data.serverFlag = false;
			template.data.clientConnectedFlag = true;
			connectedMonitorSession = addDataspaceEventListener
				(template, false, new DataspaceEventListener() 
			{
				public void dataspaceEvent(DataspaceEvent event) 
				{
					if (event.getEvent() instanceof AddEvent)
						setConnected(true);
					else if (event.getEvent() instanceof DeleteEvent)
						setConnected(false);
				}
			}  );
		}
	}

    /** dataspace Url getter */
	public String getDataspaceUrl() 
	{
		return dataspaceUrl;
	}
   
    /** active getter */
    public boolean isActive() {
	return activeFlag; 
    }

    /** internal active setter */
    protected void setActive(boolean activeFlag) {
	boolean oldValue = false;
	synchronized(this) {
	    oldValue = this.activeFlag;
	    this.activeFlag = activeFlag;
	}
	// property change
	try {
	    // fire!
	    mPropertyChange.firePropertyChange("active",
					       oldValue,
					       activeFlag);
	} catch (Exception e) {
	    System.err.println("ERROR firing active change: "+e);
	    e.printStackTrace(System.err);
	}
    }
	/** active getter
	 */
	public boolean isConnected() 
	{
		return connectedFlag;
	}
	/** internal setter 
	 */
	protected void setConnected(boolean connectedFlag) 
	{
		boolean oldValue = false;
		System.err.println("DataspaceBean "+dataspaceUrl+" is now "+
			(connectedFlag ? "connected" : "unconnected"));
		synchronized(this) 
		{
			oldValue = this.connectedFlag;
			this.connectedFlag = connectedFlag;
		}
		// property change
		try 
		{
			// fire!
			mPropertyChange.firePropertyChange("connected",
				oldValue,
				connectedFlag);
		} 
		catch (Exception e) 
		{
			System.err.println("ERROR firing connected change: "+e);
			e.printStackTrace(System.err);
		}
	}
    /*============================================================
     * IDataspace operations
     */

    /*------------------------------------------------------------*
     * item and event publishing operations                       * 
     *------------------------------------------------------------*/

    /** allocated a new GUID for a data item to be published 
     */
    public GUID allocateId() 
	{
	return guids.getUnique();
    }

    /** add a globally visible item to the dataspace
     */
    public void add(ItemData item) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("add()");
	//   item,locked,processBound,local,lease
	dataspace.addItem(item, LockType.LOCK_HARD, true, false, null);
    }

	/** default (long long) lease - 10 years :-)
	 */
	public static final int DEFAULT_LEASE_TIME_S = (60*60*24*365*10);
	/** add a (potentially) persistent item to the dataspace,
	 * i.e. not process bound, and normally with a Lease (although null
	 * is permitted)
	 */
	public void addPersistent(ItemData item, Lease lease) 
		throws DataspaceInactiveException
	{
		if (dataspace==null) 
			throw new DataspaceInactiveException("addPersistent()");
		if (lease==null) 
		{
			System.err.println("WARNING: DataspaceBean.addPersistent using default lease ("+
				DEFAULT_LEASE_TIME_S+" s) for item "+item.id);
			lease = new equip.data.LeaseImpl();
			lease.initFromTimeToLive(DEFAULT_LEASE_TIME_S);
		}
		//   item,locked,processBound,local,lease
		dataspace.addItem(item, LockType.LOCK_HARD, false, false, lease);
	}

	/** add a local-only item to the dataspace
     */
    public void addLocal(ItemData item) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("addLocal()");
	//   item,locked,processBound,local,lease
	dataspace.addItem(item, LockType.LOCK_HARD, true, true, null);
    }

    /** update an item, globally visible, reliable
     */
    public void update(ItemData item) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("update()");
	//  (args: item.local,reliable)
	dataspace.updateItem(item, false, true);
    }

    /** update an item, globally visible, optionally reliable
     */
    public void update(ItemData item, boolean reliableFlag) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("update()");
	//  (args: item.local,reliable)
	dataspace.updateItem(item, false, reliableFlag);
    }

    /** update an item, local-only, reliable
     */
    public void updateLocal(ItemData item) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("updateLocal()");
	//  (args: item.local,reliable)
	dataspace.updateItem(item, true, true);
    }

    /** update an item, local-only, optionally reliable
     */
    public void updateLocal(ItemData item, boolean reliableFlag) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("updateLocal()");
	//  (args: item.local,reliable)
	dataspace.updateItem(item, true, reliableFlag);
    }

    /** delete a globally visible item from the dataspace
     */
    public void delete(GUID id) 
	throws DataspaceInactiveException  {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("delete()");
	dataspace.deleteItem(id, false);
    }

    /** delete a local-only item from the dataspace
     */
	public void deleteLocal(GUID id)  
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("delete()");
	dataspace.deleteItem(id, true);
    }

    /** emit an event directly
     */
    public void addEvent(Event event)  
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("addEvent()");
	dataspace.addEvent(event);
    }

    /*------------------------------------------------------------*
     * query/lookup operations                                    * 
     *------------------------------------------------------------*/

    /** get current value for an item by id - local only.
     */
    public ItemData getItem(GUID id) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("copyCollect()");
	return dataspace.getItem(id);
    }

    /** copy collect - local only.
     *
     * returns all known items (local or replicated) matching the 
     * template item.
     */
    public ItemData[] copyCollect(ItemData template) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("copyCollect()");
	final java.lang.Object source = this;
	final java.util.Vector result = new java.util.Vector();
	DataSession session = dataspace.createSession
	    (new DataCallbackPostImpl() {
		    public void notifyPost(equip.data.Event event,
					   EventPattern pattern,
					   boolean patternDeleted,
					   DataSession session,
					   DataProxy dataspace, 
					   ItemData oldValue, 
					   ItemBinding oldBinding, 
					   ValueBase closure) {
			AddEvent add = (AddEvent)event;
			result.addElement(add.binding.item);
		    }}, null);
	EventPattern pattern = new EventPatternImpl();
	pattern.id = allocateId();
	pattern.initAsSimpleCopyCollect(template, true);
	
	session.addPattern(pattern);

	dataspace.deleteSession(session);

	return (ItemData[])result.toArray(new ItemData[0]);
    }


    /** copy collect - local only - callback form.
     *
     * returns all known items (local or replicated) matching the 
     * template item. Calls listener once for each item
     */
    public void copyCollect(ItemData template,
			    final DataspaceEventListener listener) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("copyCollect()");
	final java.lang.Object source = this;
	DataSession session = dataspace.createSession
	    (new DataCallbackPostImpl() {
		    public void notifyPost(equip.data.Event event,
					   EventPattern pattern,
					   boolean patternDeleted,
					   DataSession session,
					   DataProxy dataspace, 
					   ItemData oldValue, 
					   ItemBinding oldBinding, 
					   ValueBase closure) {
			listener.dataspaceEvent
			    (new DataspaceEvent(source,
						event, pattern,
						patternDeleted,
						session, dataspace,
						oldValue, oldBinding));
		    }}, null);
	EventPattern pattern = new EventPatternImpl();
	pattern.id = allocateId();
	pattern.initAsSimpleCopyCollect(template, true);
	
	session.addPattern(pattern);

	dataspace.deleteSession(session);
    }

    /** add a item monitor.
     *
     * listener is called with matching events.
     *
     * returned reference is only need to removing the monitor.
     * Its actually a DataSession.
     */
    public DataSession addDataspaceEventListener
	(ItemData template, boolean localFlag, 
	 final DataspaceEventListener listener) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("addDataspaceEventListener()");
	final java.lang.Object source = this;
	DataSession session = dataspace.createSession
	    (new DataCallbackPostImpl() {
		    public void notifyPost(equip.data.Event event,
					   EventPattern pattern,
					   boolean patternDeleted,
					   DataSession session,
					   DataProxy dataspace, 
					   ItemData oldValue, 
					   ItemBinding oldBinding, 
					   ValueBase closure) {
			listener.dataspaceEvent
			    (new DataspaceEvent(source,
						event, pattern,
						patternDeleted,
						session, dataspace,
						oldValue, oldBinding));
		    }}, null);
	EventPattern pattern = new EventPatternImpl();
	pattern.id = allocateId();
	pattern.initAsSimpleItemMonitor(template, localFlag);
	
	session.addPattern(pattern);
	
	return session;
    }

    /** add an event monitor.
     *
     * listener is called with matching events.
     *
     * returned reference is only need to removing the monitor.
     * Its actually a DataSession.
     */
    public DataSession addDataspaceEventListener
	(equip.data.Event template, boolean localFlag, 
	 final DataspaceEventListener listener) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("addDataspaceEventListener()");
	final java.lang.Object source = this;
	DataSession session = dataspace.createSession
	    (new DataCallbackPostImpl() {
		    public void notifyPost(equip.data.Event event,
					   EventPattern pattern,
					   boolean patternDeleted,
					   DataSession session,
					   DataProxy dataspace, 
					   ItemData oldValue, 
					   ItemBinding oldBinding, 
					   ValueBase closure) {
			listener.dataspaceEvent
			    (new DataspaceEvent(source,
						event, pattern,
						patternDeleted,
						session, dataspace,
						oldValue, oldBinding));
		    }}, null);
	EventPattern pattern = new EventPatternImpl();
	pattern.id = allocateId();
	pattern.initAsSimpleEventMonitor(template, localFlag);
	
	session.addPattern(pattern);
	
	return session;
    }

    /** remove a previously added event monitor 
     */
    public void removeDataspaceEventListener
	(equip.data.DataSession session) 
	throws DataspaceInactiveException {
	if (dataspace==null) 
	    throw new DataspaceInactiveException("removeDataspaceEventListener()");
	dataspace.deleteSession(session);
    }

    /** return internal DataProxy */
    public DataProxy getDataProxy() {
	return dataspace;
    }
}
/* EOF */

