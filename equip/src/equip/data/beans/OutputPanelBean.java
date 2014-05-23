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
 * $Id: OutputPanelBean.java,v 1.1.1.1 2005/03/08 16:17:22 cgreenhalgh Exp $
 */
package equip.data.beans;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.beans.*;
import java.beans.beancontext.*;

import java.lang.reflect.*;

import equip.runtime.*;
import equip.data.*;

/** generic property publisher
 */
public class OutputPanelBean extends JPanel 
    implements PropertyChangeListener {

    /** no-arg constructor - required */
    public OutputPanelBean() {
    }

    /** last value - using equip types for cross-languageness */
    private equip.data.IntArrayBox mValue = null;
    
    /** source as object */
    private java.lang.Object mSource = null;

    /** source property name */
    private String mPropertyName = null;

    /** source setter - initialisation time only */
    public void setSource(java.lang.Object source) {
	synchronized (this) {
	    disconnectSource();
	    mSource = source;
	    connectSource();
	}
    }
    /** source getter */
    public java.lang.Object getSource() {
	return mSource;
    }

    /** source setter - initialisation time only */
    public void setPropertyName(String propertyName) {
	synchronized (this) {
	    disconnectSource();
	    mPropertyName = propertyName;
	    connectSource();
	}
    }
    /** source getter */
    public String getPropertyName() {
	return mPropertyName;
    }

    /** addPropertyChangeListener(PropertyChangeListener) 
     * Method in source */
    private Method mAddPropertyListenerMethod = null;
    /** addPropertyChangeListener(String, 
     *	PropertyChangeListener) Method in source */
    private Method mAddNamedPropertyListenerMethod = null;
    /** removePropertyChangeListener(PropertyChangeListener) 
     * Method in source */
    private Method mRemovePropertyListenerMethod = null;
    /** removePropertyChangeListener(String, 
     *	PropertyChangeListener) Method in source */
    private Method mRemoveNamedPropertyListenerMethod = null;
    /** property getter Method */
    private Method mGetPropertyMethod = null;

    /** connected ok? */
    private boolean mIsConnected = false;

    /** undo plumbing to set source */
    private synchronized void disconnectSource() {
	if (!mIsConnected)
	    return;
	if (mRemoveNamedPropertyListenerMethod!=null) {
	    try {
		mRemoveNamedPropertyListenerMethod.invoke
		    (mSource,new java.lang.Object[] {mPropertyName,this});
	    } catch (Exception e) {
		System.err.println("ERROR: OutputPanelBean.disconnectSource: "+
				   e);
		e.printStackTrace(System.err);
	    }
	} else if (mRemovePropertyListenerMethod!=null) {
	    try {
		mRemovePropertyListenerMethod.invoke
		    (mSource,new java.lang.Object[] {this});
	    } catch (Exception e) {
		System.err.println("ERROR: OutputPanelBean.disconnectSource: "+
				   e);
		e.printStackTrace(System.err);
	    }
	}
	mIsConnected = false;
    }
    /** do plumbing to set source */
    private synchronized void connectSource() {
	if (mSource==null || mPropertyName==null)
	    // not configured
	    return;
	// get and cache Methods for addPropertyChangeListener,
	// removePropertyChangeListener and get<property>/is<property>
	mAddNamedPropertyListenerMethod = null;
	try {
	    mAddNamedPropertyListenerMethod = 
		mSource.getClass().getMethod("addPropertyChangeListener",
				  new Class[] {java.lang.String.class,
					       java.beans.PropertyChangeListener.class});
	} catch (Exception e) {
	    System.err.println("Warning: unable to get (named) "+
			       "addPropertyChangeListener Method from source");
	}
	mAddPropertyListenerMethod = null;
	try {
	    mAddPropertyListenerMethod = 
		mSource.getClass().getMethod("addPropertyChangeListener",
				  new Class[] {java.beans.PropertyChangeListener.class});
	} catch (Exception e) {
	    System.err.println("Warning: unable to get "+
			       "addPropertyChangeListener Method from source");
	}
	mRemoveNamedPropertyListenerMethod = null;
	try {
	    mRemoveNamedPropertyListenerMethod = 
		mSource.getClass().getMethod("removePropertyChangeListener",
				  new Class[] {java.lang.String.class,
					       java.beans.PropertyChangeListener.class});
	} catch (Exception e) {
	    System.err.println("Warning: unable to get (named) "+
			       "removePropertyChangeListener Method from source");
	}
	mRemovePropertyListenerMethod = null;
	try {
	    mRemovePropertyListenerMethod = 
		mSource.getClass().getMethod("removePropertyChangeListener",
				  new Class[] {java.beans.PropertyChangeListener.class});
	} catch (Exception e) {
	    System.err.println("Warning: unable to get "+
			       "removePropertyChangeListener Method from source");
	}
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
	    }
	}

	// ok?
	if (mGetPropertyMethod==null ||
	    (mRemovePropertyListenerMethod==null &&
	     mRemoveNamedPropertyListenerMethod==null) ||
	    (mAddPropertyListenerMethod==null &&
	     mAddNamedPropertyListenerMethod==null)) {
	    System.err.println("Cannot connect - insufficient methods");
	    return;
	}

	mIsConnected = true;

	// add property listener
	if (mAddNamedPropertyListenerMethod!=null) {
	    try {
		mAddNamedPropertyListenerMethod.invoke
		    (mSource,new java.lang.Object[] {mPropertyName,this});
	    } catch (Exception e) {
		System.err.println("ERROR: OutputPanelBean.connectSource: "+
				   e);
		e.printStackTrace(System.err);
	    }
	} else if (mAddPropertyListenerMethod!=null) {
	    try {
		mAddPropertyListenerMethod.invoke
		    (mSource,new java.lang.Object[] {this});
	    } catch (Exception e) {
		System.err.println("ERROR: OutputPanelBean.connectSource: "+
				   e);
		e.printStackTrace(System.err);
	    }
	}

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
	if (!(newValue instanceof equip.data.IntArrayBox)) {
	    System.err.println("handlePropertyChange value has wrong class: "+
			       newValue.getClass().getName());
	    return;
	}
	setValue((equip.data.IntArrayBox)newValue);
    }

    /** handle property change */
    public void setValue(equip.data.IntArrayBox newValue) {
	if (newValue==null) {
	    System.err.println("handlePropertyChange value was null");
	    return;
	}
	synchronized(this) {
	    mValue = newValue;
	    repaint();
	}
    }
    /** getter - for monitoring */
    public equip.data.IntArrayBox getValue() {
	return mValue;
    }

    /** paint - show last value */
    public void paint(Graphics g) {
	super.paint(g);
	if (mValue!=null) {
	    int x = mValue.value[0];
	    int y = mValue.value[1];
	    g.drawLine(x-10,y-10,x+10,y+10);
	    g.drawLine(x+10,y-10,x-10,y+10);
	}
    }
}
/* EOF */
