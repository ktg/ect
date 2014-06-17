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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.JLabel;

import equip.data.DictionaryImpl;
import equip.ect.Coerce;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

/**
 * simplegui Screen base class/common implementation. C.f. MIDlet Screen. Can have multiple active
 * sessions, each of which has a corresponding Displayable.
 */
public class SimpleGuiScreen implements Serializable, DynamicProperties
{
	/**
	 * screen name (test?!)
	 */
	protected String configName;
	/**
	 * dynamic properties support
	 */
	protected DynamicPropertiesSupport dynsup;
	public static final String OPTION_PREFIX = "option";
	/**
	 * options
	 */
	protected String[] options = new String[0];
	/**
	 * interrupts
	 */
	public static final String INTERRUPT_PREFIX = "interrupt";
	public static final String INTERRUPTED_PREFIX = "interrupted";
	/**
	 * interrupts
	 */
	protected String[] interrupts = new String[0];
	/**
	 * sessions, String (?) -> DictionaryImpl
	 */
	protected Hashtable sessions = new Hashtable();
	/**
	 * sessionName (String) -> SimpleGuiDisplayableImpl
	 */
	protected Hashtable sessionDisplayables = new Hashtable();

	/**
	 * session key
	 */
	public static final String SESSION_KEY = "value";
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * bean cons
	 */
	public SimpleGuiScreen()
	{
		// make displayable
		// displayable = new SimpleGuiDisplayableImpl(label, "default message");
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptors}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (name.startsWith(INTERRUPT_PREFIX) && !name.startsWith(INTERRUPTED_PREFIX))
		{
			handleInterrupt(INTERRUPTED_PREFIX + name.substring(INTERRUPT_PREFIX.length()), (DictionaryImpl) value);
			return;
		}
		// force change?!
		// if (value!=null)
		// dynsup.dynSetProperty(name, null);
		dynsup.dynSetProperty(name, value);
	}

	/**
	 * interrupts get
	 */
	public synchronized String[] getConfigInterrupts()
	{
		return interrupts;
	}

	/**
	 * get screen name
	 */
	public String getConfigName()
	{
		return configName;
	}

	/**
	 * options get
	 */
	public synchronized String[] getConfigOptions()
	{
		return options;
	}

	/**
	 * displayable getter
	 */
	public synchronized SimpleGuiDisplayable[] getDisplayables()
	{
		return (SimpleGuiDisplayable[]) sessionDisplayables.values().toArray(new SimpleGuiDisplayable[0]);
	}

	/**
	 * get current sessions - readonly
	 */
	public synchronized DictionaryImpl[] getSessions()
	{
		return (DictionaryImpl[]) sessions.values().toArray(new DictionaryImpl[0]);
	}

	/**
	 * get start session - null (pseudo-event)
	 */
	public DictionaryImpl getStartSession()
	{
		return null;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * interrupts set
	 */
	public void setConfigInterrupts(final String[] interrupts)
	{
		String[] old = null;
		synchronized (this)
		{
			old = this.interrupts;
			this.interrupts = interrupts;
			// update
			for (int i = 0; old != null && i < old.length; i++)
			{
				boolean found = false;
				for (int j = 0; !found && interrupts != null && j < interrupts.length; j++)
				{
					if (old[i].equals(interrupts[j]))
					{
						found = true;
					}
				}
				if (!found)
				{
					try
					{
						dynsup.removeProperty(INTERRUPT_PREFIX + old[i]);
						dynsup.removeProperty(INTERRUPTED_PREFIX + old[i]);
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}
			}
			for (int j = 0; interrupts != null && j < interrupts.length; j++)
			{
				boolean found = false;
				for (int i = 0; !found && old != null && i < old.length; i++)
				{
					if (old[i].equals(interrupts[j]))
					{
						found = true;
					}
				}
				if (!found)
				{
					dynsup.addProperty(INTERRUPT_PREFIX + interrupts[j], DictionaryImpl.class, null, false);
					dynsup.addProperty(INTERRUPTED_PREFIX + interrupts[j], DictionaryImpl.class, null, true);
				}
			}
		}
		propertyChangeListeners.firePropertyChange("configInterrupts", old, interrupts);
	}

	/**
	 * set screen name (test?!)
	 */
	public void setConfigName(final String name)
	{
		String old = null;
		synchronized (this)
		{
			old = this.configName;
			this.configName = name;
		}
		propertyChangeListeners.firePropertyChange("configName", old, name);
	}

	/**
	 * options set
	 */
	public void setConfigOptions(final String[] options)
	{
		String[] old = null;
		synchronized (this)
		{
			old = this.options;
			this.options = options;
			// update
			for (int i = 0; old != null && i < old.length; i++)
			{
				boolean found = false;
				for (int j = 0; !found && options != null && j < options.length; j++)
				{
					if (old[i].equals(options[j]))
					{
						found = true;
					}
				}
				if (!found)
				{
					try
					{
						dynsup.removeProperty(OPTION_PREFIX + old[i]);
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}
			}
			for (int j = 0; options != null && j < options.length; j++)
			{
				boolean found = false;
				for (int i = 0; !found && old != null && i < old.length; i++)
				{
					if (old[i].equals(options[j]))
					{
						found = true;
					}
				}
				if (!found)
				{
					dynsup.addProperty(OPTION_PREFIX + options[j], DictionaryImpl.class, null, true);
				}
			}
		}
		propertyChangeListeners.firePropertyChange("configOptions", old, options);
	}

	/**
	 * start a session
	 */
	public void setStartSession(final DictionaryImpl session)
	{
		if (session == null)
		{
			System.err.println("WARNING: setStartSession: Null session ignored\n");
			return;
		}
		try
		{
			final String sessionName = (String) Coerce.toClass(session.get(SESSION_KEY), String.class);
			if (sessionName == null || sessionName.length() == 0)
			{
				System.err.println("WARNING: setStartSession: unnamed session ignored\n");
				return;
			}
			System.out.println("Start session " + sessionName + " - " + Coerce.toClass(session, String.class));
			// update sessions
			final DictionaryImpl[] old = getSessions();
			final SimpleGuiDisplayable[] oldDisplayables = getDisplayables();
			synchronized (this)
			{
				sessions.put(sessionName, session);
				// displayables
				final SimpleGuiDisplayableImpl disp = new SimpleGuiDisplayableImpl(new JLabel("screen '" + configName
						+ "' session " + sessionName), sessionName, getConfigOptions());
				disp.addPropertyChangeListener(new PropertyChangeListener()
				{
					@Override
					public void propertyChange(final PropertyChangeEvent pce)
					{
						if (pce.getPropertyName().equals(SimpleGuiDisplayable.SELECTED_OPTION))
						{
							handleInterrupt(OPTION_PREFIX + ((String) pce.getNewValue()), sessionName);
						}
					}
				});
				sessionDisplayables.put(sessionName, disp);
			}
			propertyChangeListeners.firePropertyChange("sessions", old, getSessions());
			propertyChangeListeners.firePropertyChange("displayables", oldDisplayables, getDisplayables());
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * stop
	 */
	public void stop()
	{
	}

	/**
	 * handle interrupt
	 */
	protected void handleInterrupt(final String interrupt, final DictionaryImpl value)
	{
		if (value == null)
		{
			System.err.println("WARNING: handleInterrupt(" + interrupt + "): Null session ignored\n");
			return;
		}
		try
		{
			final String sessionName = (String) Coerce.toClass(value.get(SESSION_KEY), String.class);
			handleInterrupt(interrupt, sessionName);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * handle interrupt
	 */
	protected void handleInterrupt(final String interrupt, final String sessionName)
	{
		try
		{
			if (sessionName == null || sessionName.length() == 0)
			{
				System.err.println("WARNING: handleInterrupt(" + interrupt + "): unnamed session ignored\n");
				return;
			}
			DictionaryImpl[] old = null;
			DictionaryImpl session = null;
			SimpleGuiDisplayable[] oldDisplayables = null;
			synchronized (this)
			{
				// update sessions
				old = getSessions();
				oldDisplayables = getDisplayables();

				session = (DictionaryImpl) sessions.remove(sessionName);
				if (session == null)
				{
					System.err.println("WARNING: handleInterrupt(" + interrupt + ") for unknown session " + sessionName
							+ " - ignored");
					return;
				}
				System.out.println("Interrupt (" + interrupt + ") session " + sessionName);
				sessionDisplayables.remove(sessionName);
			}

			propertyChangeListeners.firePropertyChange("sessions", old, getSessions());
			propertyChangeListeners.firePropertyChange("displayables", oldDisplayables, getDisplayables());
			// fire interruptedX
			dynsup.dynSetProperty(interrupt, session, true);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}
}
