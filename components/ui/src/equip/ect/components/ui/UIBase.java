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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.ui;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Test input/output base class, with name property, property change support, stop.
 * @author Chris Greenhalgh
 */
public abstract class UIBase extends JFrame implements Serializable
{
	/**
	 * utility method to run on swing thread (later)
	 */
	public static void runSwing(final Runnable r)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		}
		else
		{
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * name property
	 */
	protected String name = "unnamed";

	/**
	 * stopped
	 */
	protected boolean stopped = false;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * main cons, no args. Sub-class cons should call pack and show.
	 */
	public UIBase()
	{
		super("unnamed");
		// only allow stop from within framework
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// make GUI and show - done in subclass
	}

	/**
	 * Property Change Listeners - required for all beans with active properties
	 */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * name getter
	 */
	@Override
	public synchronized String getName()
	{
		return name;
	}

	/**
	 * Property Change Listeners - required for all beans with active properties
	 */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * name of GUI component, shown as Window title
	 */
	@Override
	public synchronized void setName(final String name)
	{
		if (name == this.name || (this.name != null && name != null && this.name.equals(name)))
		{
			// same
			return;
		}
		final Object old = this.name;
		this.name = name;
		// update gui title
		// may not be swing thread
		runSwing(new Runnable()
		{
			@Override
			public void run()
			{
				setTitle(name);
			}
		});
		// fire change event
		propertyChangeListeners.firePropertyChange("name", old, name);
	}

	/**
	 * stop - called by container
	 */
	public synchronized void stop()
	{
		runSwing(new Runnable()
		{
			@Override
			public void run()
			{
				setVisible(false);
				dispose();
			}
		});
		// stop subsequent actions on slider
		stopped = true;
	}
}
