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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.ect.Coerce;
import equip.ect.ContainerManager;
import equip.ect.IActiveComponent;

/**
 * simplegui display, i.e. device. C.f. MIDlet Display
 */
public class SimpleGuiDisplay implements Serializable, IActiveComponent
{
	public boolean debug = true;
	/**
	 * frame
	 */
	JFrame frame;
	/**
	 * panel
	 */
	JPanel panel;
	/**
	 * option menu
	 */
	JMenu optionMenu;
	/**
	 * application menu
	 */
	JMenu applicationMenu;
	/**
	 * last displayable
	 */
	SimpleGuiDisplayable lastDisplayable;
	/**
	 * last displayable name
	 */
	String lastDisplayableName;
	/**
	 * current requests to merge and their most recent values
	 */
	protected Hashtable valueRequests = new Hashtable();

	/**
	 * displayable input
	 */
	protected SimpleGuiDisplayable displayables[];
	/**
	 * test attribute
	 */
	String test;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * bean cons
	 */
	public SimpleGuiDisplay()
	{
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					panel = new JPanel();
					panel.setPreferredSize(new Dimension(400, 600));
					frame = new JFrame("SimpleGuiDisplay");
					frame.getContentPane().add(panel);
					final JMenuBar menuBar = new JMenuBar();
					optionMenu = new JMenu("Options");
					menuBar.add(optionMenu);
					applicationMenu = new JMenu("Applications");
					menuBar.add(applicationMenu);
					frame.setJMenuBar(menuBar);
					frame.pack();
					frame.show();
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
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
	 * test getter
	 */
	public synchronized SimpleGuiDisplayable[] getDisplayables()
	{
		return displayables;
	}

	/**
	 * test getter
	 */
	public synchronized String getTest()
	{
		return test;
	}

	/**
	 * initialise - IActiveComponent
	 */
	@Override
	public void initialise(final ContainerManager cmgr, final DataspaceBean dataspace)
	{
		// noop
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToAdded(final String propertyName, final GUID requestId)
	{
		// noop
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToDeleted(final String propertyName, final GUID requestId)
	{
		// noop
	}

	/**
	 * IActiveComponent - unused - attempt to set a property due to a link add / source update
	 */
	@Override
	public boolean linkToUpdated(final String propertyName, final GUID requestId, final Object value)
	{
		try
		{
			if (debug)
			{
				System.out.println("SimpleGuiDisplay: linkToUpdated(" + propertyName + ", " + requestId + ", ("
						+ (value != null ? value.getClass().getName() : "null") + ")"
						+ Coerce.toClass(value, String.class) + ")");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in DictionaryArrayMerge.linkToUpdated debug output: " + e);
		}
		if (!propertyName.equals("displayables")) { return false; }
		synchronized (this)
		{
			if (value == null)
			{
				// includes delete
				valueRequests.remove(requestId);
			}
			else
			{
				// merge to publish?!
				valueRequests.put(requestId, value);
			}
		}
		mergeValue();
		return true;
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
	 * displayable setter
	 */
	public void setDisplayables(final SimpleGuiDisplayable displayables[])
	{
		SimpleGuiDisplayable old[] = null;
		synchronized (this)
		{
			old = this.displayables;
			this.displayables = displayables;

			try
			{
				SwingUtilities.invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						swingUpdateDisplayables();
					}
				});
			}
			catch (final Exception e)
			{
				System.err.println("ERROR: " + e);
				e.printStackTrace(System.err);
			}

		}
		propertyChangeListeners.firePropertyChange("displayables", old, displayables);
	}

	/**
	 * test setter
	 */
	public void setTest(final String test)
	{
		String old = null;
		synchronized (this)
		{
			old = this.test;
			this.test = test;
		}
		propertyChangeListeners.firePropertyChange("test", old, test);
	}

	/**
	 * stop
	 */
	@Override
	public synchronized void stop()
	{
		frame.dispose();
	}

	/**
	 * merge values
	 */
	protected void mergeValue()
	{
		final Vector ps = new Vector();
		synchronized (this)
		{
			/*
			 * clear if nothing if (valueRequests.size()==0) // leave it alone return;
			 */
			final Enumeration ve = valueRequests.elements();
			while (ve.hasMoreElements())
			{
				final SimpleGuiDisplayable[] d = (SimpleGuiDisplayable[]) ve.nextElement();
				for (final SimpleGuiDisplayable element : d)
				{
					if (element != null)
					{
						ps.addElement(element);
					}
				}
			}
		}
		final SimpleGuiDisplayable ds[] = (SimpleGuiDisplayable[]) ps.toArray(new SimpleGuiDisplayable[ps.size()]);
		setDisplayables(ds);
	}

	/**
	 * select applicatin
	 */
	protected void swingSelectApplication(final SimpleGuiDisplayable displayable, final String name)
	{
		System.out.println("Select application " + name);
		lastDisplayable = displayable;
		lastDisplayableName = name;
		swingUpdateDisplayables();
	}

	/**
	 * select option
	 */
	protected void swingSelectOption(final SimpleGuiDisplayable disp, final String option)
	{
		System.out.println("Select option " + option);
		disp.setSelectedOption(option);
	}

	/**
	 * update displayables - swing thread method
	 */
	protected void swingUpdateDisplayables()
	{
		final SimpleGuiDisplayable displayables[] = this.displayables;
		applicationMenu.removeAll();
		final ButtonGroup group = new ButtonGroup();
		int besti = -1;
		// best
		for (int i = 0; besti < 0 && displayables != null && i < displayables.length; i++)
		{
			if (displayables[i] == lastDisplayable)
			{
				besti = i;
			}
		}
		for (int i = 0; besti < 0 && lastDisplayableName != null && displayables != null && i < displayables.length; i++)
		{
			if (lastDisplayableName.equals(displayables[i].getNavigationName()))
			{
				besti = i;
			}
		}
		if (besti < 0 && displayables != null && displayables.length > 0)
		{
			besti = 0;
		}
		for (int i = 0; displayables != null && i < displayables.length; i++)
		{
			if (displayables[i] == null)
			{
				continue;
			}
			final int fi = i;
			final String name = displayables[i].getNavigationName();
			final JRadioButtonMenuItem app = new JRadioButtonMenuItem(new AbstractAction(name)
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					swingSelectApplication(displayables[fi], name);
				}
			});
			if (i == besti)
			{
				app.setSelected(true);
			}
			group.add(app);
			applicationMenu.add(app);
		}

		applicationMenu.validate();
		panel.removeAll();
		final SimpleGuiDisplayable displayable = (besti >= 0) ? displayables[besti] : null;
		final JComponent comp = displayable != null ? displayable.getJComponent() : null;
		if (comp != null)
		{
			panel.add(comp);
		}
		optionMenu.removeAll();
		final String[] options = displayable != null ? displayable.getOptions() : null;
		if (options != null)
		{
			System.out.println("Options:");
			for (int i = 0; i < options.length; i++)
			{
				final int fi = i;
				System.out.println(" [" + i + "]: " + options[i]);
				optionMenu.add(new AbstractAction(options[i])
				{
					@Override
					public void actionPerformed(final ActionEvent ae)
					{
						swingSelectOption(displayable, options[fi]);
					}
				});
			}
		}
		optionMenu.validate();
		panel.validate();
		panel.repaint();
		lastDisplayable = displayable;
		lastDisplayableName = displayable != null ? displayable.getNavigationName() : null;
	}
}
