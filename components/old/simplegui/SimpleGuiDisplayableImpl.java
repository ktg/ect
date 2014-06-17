/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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
package equip.ect.components.simplegui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JComponent;

/**
 * helper implementation of {@link SimpleGuiDisplayable}.
 */
public class SimpleGuiDisplayableImpl implements SimpleGuiDisplayable
{
	/**
	 * component
	 */
	protected JComponent component;
	/**
	 * name
	 */
	protected String name;
	/**
	 * options - ie choices from menu for next thing
	 */
	protected String[] options = new String[0];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * cons
	 */
	public SimpleGuiDisplayableImpl(final JComponent component, final String navigationName)
	{
		this(component, navigationName, new String[0]);
	}

	/**
	 * cons
	 */
	public SimpleGuiDisplayableImpl(final JComponent component, final String navigationName, final String[] options)
	{
		this.component = component;
		this.name = navigationName;
		this.options = options;
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * the appearance
	 */
	@Override
	public JComponent getJComponent()
	{
		return component;
	}

	/**
	 * user navigation name
	 */
	@Override
	public String getNavigationName()
	{
		return name;
	}

	/**
	 * options - ie choices from menu for next thing
	 */
	@Override
	public synchronized String[] getOptions()
	{
		return options;
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * options setter - throws change event
	 */
	public void setOptions(final String[] options)
	{
		String[] old = null;
		synchronized (this)
		{
			old = this.options;
			this.options = options;
		}
		propertyChangeListeners.firePropertyChange(OPTIONS, old, options);
	}

	/**
	 * selection option - callback
	 */
	@Override
	public void setSelectedOption(final String option)
	{
		// pseudo property change event (no actual property)
		propertyChangeListeners.firePropertyChange(SELECTED_OPTION, null, option);
	}
}
