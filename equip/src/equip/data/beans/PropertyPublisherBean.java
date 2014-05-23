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
 * 25 Sept 2002
 * $Id: PropertyPublisherBean.java,v 1.1.1.1 2005/03/08 16:17:22 cgreenhalgh Exp $
 */
package equip.data.beans;

import java.beans.*;
import java.beans.beancontext.*;

import java.lang.reflect.*;

import equip.runtime.*;
import equip.data.*;

/** generic property publisher.
 * 
 * monitors a bean property and publishes it as a Tuple in a dataspace.
 *
 * PropertyPublisherBean publishes a data item of class
 * equip.data.Tuple with two fields. The first is a
 * equip.data.StringBox with the nominal class name of the tuple,
 * and the second is the property value itself (if a subclass of 
 * equip.runtime.ValueBase) or an equip.data.SerializedObject (if the
 * property value is serializable). The name of the item (field of
 * equip.data.ItemData) is also set by configuration to identify 
 * individual data items. The GUID is arbitrary and allocated on 
 * creation.
 *
 * to be active we require:
 * - a dataspace to publish to (reference to equip.data.beans.DataspaceBean)
 * - a source bean reference (java.lang.Object)
 * - name of property to publish (String)
 * - name to use for data item (String)
 * - nominal tuple class name (String)
 */
public class PropertyPublisherBean extends BeanContextChildSupport
    implements PropertyChangeListener {

    /** no-arg constructor - required */
    public PropertyPublisherBean() {
    }
    
    /** debug */
    public boolean debug = false;
    public void setDebug(boolean d) { debug = d; }
    public boolean getDebug() { return debug; }

    /** last value */
    protected java.lang.Object mValue = null;
    
    /** source as object */
    protected java.lang.Object mSource = null;

    /** source property name */
    protected String mPropertyName = null;

    /** dataspace */
    protected DataspaceBean mDataspace = null;
    
    /** item name */
    protected String mItemName = null;

    /** item class name */
    protected String mItemClassName = null;

    /** active/connected */
    protected boolean mIsConnected = false;

    /** dataspace item id */
    protected GUID mItemId = null;

    /** fire property changed */
    public void firePropertyChange(String propertyName,
				    java.lang.Object oldValue,
				    java.lang.Object newValue) {
	try {
	    pcSupport.firePropertyChange(propertyName, oldValue, newValue);
	} catch (Exception e) {
	    System.err.println("ERROR: PropertyPublisherBean "+
			       "firePropertyChange ("+propertyName+
			       "): "+e);
	    e.printStackTrace(System.err);
	}
    }

    /** source setter - initialisation time only */
    public void setSource(java.lang.Object source) {
	java.lang.Object oldValue = mSource;
	synchronized (this) {
	    disconnectSource();
	    mSource = source;
	    connectSource();
	}
	firePropertyChange("source", oldValue, source);
    }
    /** source getter */
    public java.lang.Object getSource() {
	return mSource;
    }

    /** property name setter - initialisation time only */
    public void setPropertyName(String propertyName) {
	String oldValue = mPropertyName;
	synchronized (this) {
	    disconnectSource();
	    mPropertyName = propertyName;
	    connectSource();
	}
	firePropertyChange("propertyName", oldValue, propertyName);
    }
    /** property name getter */
    public String getPropertyName() {
	return mPropertyName;
    }

    /** item name setter - initialisation time only */
    public void setItemName(String itemName) {
	String oldValue = mItemName;
	synchronized (this) {
	    disconnectSource();
	    mItemName = itemName;
	    connectSource();
	}
	firePropertyChange("itemName", oldValue, itemName);
    }
    /** item name getter */
    public String getItemName() {
	return mItemName;
    }

    /** item class name setter - initialisation time only */
    public void setItemClassName(String itemClassName) {
	String oldValue = mItemClassName;
	synchronized (this) {
	    disconnectSource();
	    mItemClassName = itemClassName;
	    connectSource();
	}
	firePropertyChange("itemClassName", oldValue, itemClassName);
    }
    /** item name getter */
    public String getItemClassName() {
	return mItemClassName;
    }

    /** dataspace setter - initialisation time only */
    public void setDataspace(DataspaceBean dataspace) {
	DataspaceBean oldValue = mDataspace;
	synchronized (this) {
	    disconnectSource();
	    mDataspace = dataspace;
	    connectSource();
	}
	firePropertyChange("dataspace", oldValue, dataspace);
    }
    /** source getter */
    public DataspaceBean getDataspace() {
	return mDataspace;
    }

    /** property getter Method */
    private Method mGetPropertyMethod = null;

    /** undo plumbing to set source */
    private synchronized void disconnectSource() {
	if (!mIsConnected)
	    return;
	// remove dataitem
	try {
	    if (mItemId!=null) {
		mDataspace.delete(mItemId);
		if(debug)
		    System.err.println("item deleted");
	    }
	} catch(Exception e) {
	    System.err.println("Unable to delete old item on disconnect: "+e);
	    e.printStackTrace(System.err);
	}
	mItemId = null;
	mIsConnected = false;
	// remove property listener
	boolean done;
	done = false;
	try {
	    Method m = mSource.getClass().
		getMethod("removePropertyChangeListener",
			  new Class[] {java.lang.String.class,
				       java.beans.PropertyChangeListener.class});
	    m.invoke(mSource,new java.lang.Object[] {mPropertyName,this});
	    done = true;
	} catch (Exception e) {
	    try {
		// try unnamed
		Method m = mSource.getClass().
		    getMethod("removePropertyChangeListener",
			      new Class[] {java.beans.PropertyChangeListener.class});
		m.invoke(mSource,new java.lang.Object[] {this});
	    } catch (Exception e2) {
		System.err.println("ERROR: removePropertyChangeListener "+
				   "failed: "+e);
		e.printStackTrace(System.err);
	    }
	}
    }
    /** do plumbing to set source */
    private synchronized void connectSource() {
	if (mSource==null || mPropertyName==null || mDataspace==null ||
	    mItemName==null || mItemClassName==null)
	    // not configured
	    return;
	// get and cache Method for get<property>/is<property>
	mGetPropertyMethod = null;
	// capitalise
	StringBuffer capPropertyNameBuf = new StringBuffer(mPropertyName);
	if (capPropertyNameBuf.length()>0) 
	    capPropertyNameBuf.setCharAt(0, Character.toUpperCase
					 (capPropertyNameBuf.charAt(0)));
	try {
	    mGetPropertyMethod = 
		mSource.getClass().getMethod("get"+capPropertyNameBuf, 
				  new Class[0]);
	} catch (Exception e) {
	    System.err.println("Warning: unable to get "+
			       "get"+capPropertyNameBuf+" Method from source");
	}
	if (mGetPropertyMethod==null) {
	    // try is<Prop>
	    try {
		mGetPropertyMethod = 
		    mSource.getClass().getMethod("is"+capPropertyNameBuf, 
				      new Class[0]);
	    } catch (Exception e) {
		System.err.println("Warning: unable to get "+
				   "get"+capPropertyNameBuf+" Method from source");
		return;
	    }
	}
	// add property listener
	try {
	    Method m = mSource.getClass().
		getMethod("addPropertyChangeListener",
			  new Class[] {java.lang.String.class,
				       java.beans.PropertyChangeListener.class});
	    m.invoke(mSource,new java.lang.Object[] {mPropertyName,this});
	} catch (Exception e) {
	    try {
		Method m = mSource.getClass().
		    getMethod("addPropertyChangeListener",
			      new Class[] {java.beans.PropertyChangeListener.class});
		m.invoke(mSource,new java.lang.Object[] {this});
	    } catch (Exception e2) {
		System.err.println("Warning: unable to add property listener "+
				   "for "+mPropertyName+": "+e);
		e.printStackTrace(System.err);
		return;
	    }
	}

	mIsConnected = true;
	mItemId = null;

	// initial value
	try {
	    java.lang.Object value = mGetPropertyMethod.invoke
		(mSource,new java.lang.Object[0]);
	    handlePropertyChange(value);
	} catch (Exception e) {
	    System.err.println("ERROR: OutputPanelBean.connectSource: "+
			       e);
	    e.printStackTrace(System.err);
	}
    }	    

    /** property change event */
    public void propertyChange(PropertyChangeEvent evt) {
	if (!mIsConnected) {
	    System.err.println("Warning: OutputPanelBean received "+
			       "property change when not connected");
	    return;
	}
	if (evt.getSource()!=mSource) {
	    System.err.println("Warning: OutputPanelBean received "+
			       "property change from unknown source");
	    return;
	}
	if (!evt.getPropertyName().equals(mPropertyName)) {
	    System.err.println("Note: OutputPanelBean received "+
			       "property change for unknown property: "+
			       evt.getPropertyName());
	    return;
	}
	
	handlePropertyChange(evt.getNewValue());
    }

    /** handle property change */
    private void handlePropertyChange(java.lang.Object newValue) {
	if (newValue==null) {
	    System.err.println("handlePropertyChange value was null");
	    return;
	}
	synchronized(this) {
	    mValue = newValue;
	    ItemData item = null;
	    try {
		item = encodePropertyValue(mValue);
	    } catch (Exception e) {
		System.err.println("ERROR: unable to encode property: "+e);
		e.printStackTrace(System.err);
		return;
	    }
	    if (mItemId==null) {
		try {
		    item.id = mItemId = mDataspace.allocateId();
		    mDataspace.add(item);
		    if (debug)
			System.err.println("item added");
		} catch (Exception e) {
		    System.err.println("ERROR: unable to publish value: "+e);
		    e.printStackTrace(System.err);
		    mItemId = null;
		}
	    } else {
		try {
		    item.id = mItemId;
		    mDataspace.update(item);
		    if (debug)
			System.err.println("item updated");
		} catch (Exception e) {
		    System.err.println("ERROR: unable to update value: "+e);
		    e.printStackTrace(System.err);
		}
	    }		
	}
    }

    /** overridable type conversion of property value */
    protected ItemData encodePropertyValue(java.lang.Object value)
	throws java.io.IOException {
	if (value instanceof equip.runtime.ValueBase) {
	    // natively serializable
	    ItemData item = new TupleImpl(new StringBoxImpl(mItemClassName),
					  (equip.runtime.ValueBase)value);
	    item.name = mItemName;
	    return item;
	}
	// default to tuple with serialised value
	ItemData item = new TupleImpl(new StringBoxImpl(mItemClassName),
				      new SerializedObjectImpl
					  ((java.io.Serializable)value));
	item.name = mItemName;
	return item;
    }
}
/* EOF */
