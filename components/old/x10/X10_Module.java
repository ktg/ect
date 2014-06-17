/*
 <COPYRIGHT>

 Copyright (c) 2004-2005, University of Nottingham
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

 Created by: Chris Allsop (University of Nottingham)
 Contributors:
 Chris Allsop (University of Nottingham)

 */

package equip.ect.components.x10;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import equip.ect.components.x10.javaX10project.Command;

// The only way a module's data can ever be inconsistent with the true state
// of the module is if you change its address or unplug / plug it in whilst
// the module is registered to an X10_OneWay controller (bean).

public abstract class X10_Module implements Serializable, Comparable<X10_Module>, X10_Constants
{
	protected X10_OneWay parent;
	protected String name;
	protected String address; // the housecode and unitcode appended together
	protected boolean on;
	protected transient PropertyChangeSupport propertyChangeListeners;

	public static final String ADDRESS_PROPERTY = "address";

	public static final String HOUSE_CODE_PROPERTY = "housecode";

	public static final String UNIT_CODE_PROPERTY = "unitcode";

	public static final String ON_PROPERTY = "on";

	public X10_Module(final X10_OneWay parent, final String address)
	{

		this.propertyChangeListeners = new PropertyChangeSupport(this);
		// address should have already been parsed by this modules parent controller
		this.address = address;
		this.parent = parent;
		this.name = DEFAULT_NAME;
		this.on = false;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	@Override
	public int compareTo(final X10_Module module)
	{
		return address.compareTo(module.getAddress());
	}

	public String getAddress()
	{
		return address;
	}

	public String getName()
	{
		return this.name;
	}

	public abstract String getPersistentChild();

	public boolean isOn()
	{
		return on;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public synchronized void setOn(final boolean flag)
	{
		if (flag != isOn())
		{
			parent.getX10Controller().addCommand(new Command(this.address, ((flag) ? Command.ON : Command.OFF)));

			propertyChangeListeners.firePropertyChange(ON_PROPERTY, this.on, flag);
			this.on = flag;
		}
	}

	public void stop()
	{
		parent = null;
	}

	@Override
	public String toString()
	{
		return "Name: " + this.name + ", Address: " + this.address + ", Status: " + ((this.on) ? "On" : "Off");
	}

}
