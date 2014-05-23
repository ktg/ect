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
 * $Id: InputPanelBean.java,v 1.1.1.1 2005/03/08 16:17:22 cgreenhalgh Exp $
 */
package equip.data.beans;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import java.beans.*;
import java.beans.beancontext.*;

import equip.runtime.*;
import equip.data.*;

/** panel reporting last mouse click position as a property.
 * test class for equip.data.beans. 
 */
public class InputPanelBean 
    extends JPanel
    implements BeanContextChild, PropertyChangeListener {

    /** no-arg constructor - required */
    public InputPanelBean() {
	/** mouse click updates value */
	this.addMouseListener(new MouseAdapter() {
		/** mouse input - click */
		public void mouseClicked(MouseEvent e) {
		    updateValue(e.getX(), e.getY());
		}
	    });
    }

    /** last position - use equip types for cross-languageness */
    private equip.data.IntArrayBox mValue = 
	new equip.data.IntArrayBoxImpl(new int[] { 0,0 });
    
    /** property change support */
    private java.beans.PropertyChangeSupport mPropertyChange = 
	new java.beans.PropertyChangeSupport(this);

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

    /** vetoable change support */
    private java.beans.VetoableChangeSupport mVetoableChange = 
	new java.beans.VetoableChangeSupport(this);

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

    /** bean context */
    protected BeanContext mBeanContext;

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
	    // remove old designTime property listener
	    beanContext.removePropertyChangeListener("designTime", this);
	    // set
	    mBeanContext = beanContext;
	    // set new designTime property listener
	    beanContext.addPropertyChangeListener("designTime", this);
	    // initial value
	    setDesignTime(beanContext.isDesignTime());
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

    /** design time? defaults to true*/
    protected boolean mDesignTime = true;

    /** design time setter (internal) */
    protected void setDesignTime(boolean designTime) {
	mDesignTime = designTime;
	System.err.println("InputPanelBean: designTime = "+designTime);
    }

    /** property change listener */
    public void propertyChange(PropertyChangeEvent evt) {
	if (evt.getPropertyName().equals("designTime")) {
	    if (!(evt.getNewValue() instanceof java.lang.Boolean)) {
		System.err.println("Unexpected type for designTime value: "+
				   evt.getNewValue().getClass().getName());
		return;
	    }
	    java.lang.Boolean val = (java.lang.Boolean)evt.getNewValue();
	    setDesignTime(val.booleanValue());
	}
    }

    /** value property getter.
     * read only - no external setter
     */
    public equip.data.IntArrayBox getValue() {
	return mValue;
    }

    /** internal value update */
    private void updateValue(int x, int y) {
	equip.data.IntArrayBox oldValue = null, newValue = null;
	synchronized(this) {
	    // keep old value for event
	    oldValue = mValue;
	    // replace with new value
	    newValue =  
		new equip.data.IntArrayBoxImpl(new int[] { x, y });
	    // veto...?
	    // set
	    mValue = newValue;
	}
	try {
	    // fire!
	    mPropertyChange.firePropertyChange("value",
					       oldValue,
					       newValue);
	} catch (Exception e) {
	    System.err.println("ERROR firing value change: "+e);
	    e.printStackTrace(System.err);
	}
	repaint();
    }

    /** paint - show last value */
    public void paint(Graphics g) {
	super.paint(g);
	int x = mValue.value[0];
	int y = mValue.value[1];
	g.drawLine(x-10,y-10,x+10,y+10);
	g.drawLine(x+10,y-10,x-10,y+10);
    }
}
/* EOF */
