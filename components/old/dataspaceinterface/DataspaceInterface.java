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
package equip.ect.components.dataspaceinterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import equip.data.DataSession;
import equip.data.DictionaryImpl;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.ect.Coerce;
import equip.ect.ContainerManager;
import equip.ect.IActiveComponent;

/**
 * A bean which interfaces directly to the installation dataspace, allowing values to be copied in
 * and out. <H3>Description</H3> Each ECT installation contains an EQUIP dataspace for data
 * distribution and coordination. This component allows values (in particular Dictionary values) to
 * be shared via this dataspace. Published values are retrieved from the dataspace by pattern
 * matching the "templates" values. Multiple instances of this component running in the same
 * installation will have access to the same shared values. <H3>Installation</H3> No specific
 * installation requirements - this software-only component interfaces to the installation's
 * internal EQUIP dataspace. <H3>Configuration</H3> The value of the "configchannelname" property is
 * included in the values and templates shared in the, so that only values with the same
 * "configchannelname" will be matched, i.e. values published by components set to the same
 * "channel". <H3>Usage</H3> The (array of Dictionaries) value(s) presented to the "publish"
 * property will be published in the dataspace as data objects, tagged by the current
 * "configchannelname". Note that this property will merge inputs from multiple incoming links into
 * a single array. The "templates" (array of Dictionaries) value will query the dataspace for all
 * matching values, which are reported by the "matches" property. As values are published/removed
 * from the dataspace the "matches" property will be updated accordingly. <H3>Technical Details</H3>
 * Each published value is placed into the dataspace as a 3-element Tuple, whose first element is
 * "DataspaceInterfaceValue:1.0", second element is the channel name (string box), and third element
 * is the Dictionary. Each change of publish value results in any/all values being re-published.
 * There are no dataspace updates performed. The value merging in "publish" relies on the
 * ect.IActiveComponent interface facilities (see component documentation).
 * 
 * @preferred
 * @classification Data/Dataspace
 * @defaultInputProperty publish
 * @defaultOutputProperty matches
 */
public class DataspaceInterface implements Serializable, IActiveComponent, DataspaceEventListener
{
	/**
	 * dataspace
	 */
	protected DataspaceBean dataspace;
	/**
	 * publishing GUIDs
	 */
	protected Vector guids = new Vector();
	/**
	 * debug
	 */
	static boolean debug = false;
	/**
	 * current links to publish and their most recent values
	 */
	protected Hashtable publishRequests = new Hashtable();
	/**
	 * default channel name
	 */
	public static final String DEFAULT_CHANNELNAME = "default";
	/**
	 * configchannelname value
	 */
	protected String configchannelname = DEFAULT_CHANNELNAME;
	/**
	 * value(s) to be published
	 */
	protected DictionaryImpl publish[] = new DictionaryImpl[0];
	/**
	 * type name
	 */
	public static final String TUPLE_TYPE_NAME = "DataspaceInterfaceValue:1.0";
	/**
	 * template value(s) to be monitored
	 */
	protected DictionaryImpl templates[] = new DictionaryImpl[0];
	/**
	 * DataSessions, one per template
	 */
	protected DataSession templateSessions[];
	/**
	 * template data items
	 */
	protected TupleImpl templateTuples[];

	/**
	 * flag to delay notification of matches change
	 */
	protected boolean delayMatchNotification = false;
	/**
	 * old value of matches
	 */
	protected DictionaryImpl matches[] = new DictionaryImpl[0];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DataspaceInterface()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/** notify of event (cf DataCallbackPost) - DataspaceEventListener */
	@Override
	public void dataspaceEvent(final DataspaceEvent event)
	{
		if (!delayMatchNotification)
		{
			updateMatches();
		}
	}

	/**
	 * The name of a virtual channel on which to pub/sub values.
	 */
	public synchronized String getConfigchannelname()
	{
		return configchannelname;
	}

	/**
	 * matched value(s) returned from dataspace
	 */
	public synchronized DictionaryImpl[] getMatches()
	{
		return matches;
	}

	/**
	 * value(s) to be published.
	 */
	public synchronized DictionaryImpl[] getPublish()
	{
		return publish;
	}

	/**
	 * template value(s) to be monitored.
	 */
	public synchronized DictionaryImpl[] getTemplates()
	{
		return templates;
	}

	/**
	 * initialise - IActiveComponent
	 */
	@Override
	public void initialise(final ContainerManager cmgr, final DataspaceBean dataspace)
	{
		if (debug || true)
		{
			System.out.println("DataspaceInterface component initialised; dataspace = " + dataspace.getDataspaceUrl());
		}
		this.dataspace = dataspace;
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToAdded(final String propertyName, final GUID requestId)
	{
		if (debug)
		{
			System.out.println("DataspaceInterface: linkToAdded(" + propertyName + ", " + requestId + ")");
		}
	}

	/**
	 * IActiveComponent - unused - property link request added to this component
	 */
	@Override
	public void linkToDeleted(final String propertyName, final GUID requestId)
	{
		if (debug)
		{
			System.out.println("DataspaceInterface: linkToDeleted(" + propertyName + ", " + requestId + ")");
		}
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
				System.out.println("DataspaceInterface: linkToUpdated(" + propertyName + ", " + requestId + ", ("
						+ (value != null ? value.getClass().getName() : "null") + ")"
						+ Coerce.toClass(value, String.class) + ")");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in DataspaceInterface.linkToUpdated debug output: " + e);
		}
		if (!propertyName.equals("publish")) { return false; }
		synchronized (this)
		{
			if (value == null)
			{
				// includes delete
				publishRequests.remove(requestId);
			}
			else
			{
				// merge to publish?!
				publishRequests.put(requestId, value);
			}
		}
		mergePublish();
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
	 * The name of a virtual channel on which to pub/sub values.
	 * 
	 * @preferred
	 */
	public void setConfigchannelname(final String configchannelname)
	{
		String oldconfigchannelname = null;
		synchronized (this)
		{
			if (publish != null && publish.length > 0)
			{
				System.err.println("ERROR: configchannelname should be set before publish");
				return;
			}
			// could suppress no-change setting
			// if (configchannelname==this.configchannelname || (configchannelname!=null &&
			// this.configchannelname!=null && configchannelname.equals(this.configchannelname))
			// return;
			oldconfigchannelname = this.configchannelname;
			this.configchannelname = configchannelname;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configchannelname", oldconfigchannelname, configchannelname);
	}

	/**
	 * value(s) to be published.
	 * 
	 * @preferred
	 */
	public void setPublish(final DictionaryImpl[] publish)
	{
		try
		{
			System.out.println("Set publish to " + publish + " (" + publish.length + " elements, "
					+ Coerce.toClass(publish, String.class) + ")");
		}
		catch (final Exception e)
		{
		}
		DictionaryImpl[] old = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.publish;
			this.publish = publish;
			// do publish
			for (int i = 0; i < publish.length; i++)
			{
				if (guids.size() <= i)
				{
					guids.addElement(dataspace.allocateId());
				}
				final GUID id = (GUID) guids.elementAt(i);
				final TupleImpl tuple = new TupleImpl(new StringBoxImpl(TUPLE_TYPE_NAME), new StringBoxImpl(
						configchannelname), publish[i]);
				tuple.id = id;
				try
				{
					if (i < old.length)
					{
						System.out.println("Withdraw " + id + " before re-publish");
						dataspace.delete(id);
					}
					System.out.println("Publish " + publish[i] + " as " + id + " (add)");
					dataspace.add(tuple);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR updating/publishing item " + publish[i] + " as " + id + ": " + e);
					e.printStackTrace(System.err);
				}
			}
			for (int i = publish.length; old != null && i < old.length && i < guids.size(); i++)
			{
				final GUID id = (GUID) guids.elementAt(i);
				try
				{
					System.out.println("Unpublish " + id);
					dataspace.delete(id);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR updating/publishing item " + publish[i] + " as " + id + ": " + e);
					e.printStackTrace(System.err);
				}
			}
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("publish", old, publish);
	}

	/**
	 * template value(s) to be monitored.
	 * 
	 * @preferred
	 */
	public void setTemplates(DictionaryImpl[] templates)
	{
		try
		{
			System.out.println("Set templates to " + templates + " (" + templates.length + " elements, "
					+ Coerce.toClass(templates, String.class) + ")");
		}
		catch (final Exception e)
		{
		}
		DictionaryImpl[] old = null;
		synchronized (this)
		{
			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.templates;
			this.templates = templates;

			delayMatchNotification = true;

			// remove old sessions
			for (int i = 0; templateSessions != null && i < templateSessions.length; i++)
			{
				try
				{
					dataspace.removeDataspaceEventListener(templateSessions[i]);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR unregistering listener " + i + ": " + e);
					e.printStackTrace(System.err);
				}
			}
			templateSessions = null;

			if (templates == null)
			{
				templates = new DictionaryImpl[0];
			}
			// add new sessions
			templateSessions = new DataSession[templates.length];
			templateTuples = new TupleImpl[templates.length];
			for (int i = 0; i < templates.length; i++)
			{
				templateTuples[i] = new TupleImpl(new StringBoxImpl(TUPLE_TYPE_NAME), new StringBoxImpl(
						configchannelname), templates[i]);
				try
				{
					templateSessions[i] = dataspace.addDataspaceEventListener(templateTuples[i], false, this);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR registering listener " + i + " for " + templates[i] + ": " + e);
					e.printStackTrace(System.err);
				}
			}

			delayMatchNotification = false;
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("templates", old, templates);
		updateMatches();
	}

	/**
	 * stop - IActiveComponent - release all resources.
	 */
	@Override
	public synchronized void stop()
	{
		for (int i = 0; i < publish.length && i < guids.size(); i++)
		{
			final GUID id = (GUID) guids.elementAt(i);
			try
			{
				dataspace.delete(id);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR updating/publishing item " + publish[i] + " as " + id + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		// remove old sessions
		for (int i = 0; templateSessions != null && i < templateSessions.length; i++)
		{
			try
			{
				dataspace.removeDataspaceEventListener(templateSessions[i]);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR unregistering listener " + i + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		templateSessions = null;
	}

	/**
	 * matched value(s) returned from dataspace
	 */
	public void updateMatches()
	{
		DictionaryImpl matches[] = null;
		DictionaryImpl old[] = null;
		synchronized (this)
		{
			old = this.matches;

			// get current matches
			final Vector mv = new Vector();
			for (int i = 0; templates != null && i < templates.length; i++)
			{
				try
				{
					final ItemData items[] = dataspace.copyCollect(templateTuples[i]);
					for (final ItemData item : items)
					{
						final TupleImpl t = (TupleImpl) item;
						mv.addElement(t.fields[2]);
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR doing collect for template " + i + ", " + templateTuples[i] + ": " + e);
					e.printStackTrace(System.err);
				}
			}
			matches = (DictionaryImpl[]) mv.toArray(new DictionaryImpl[mv.size()]);
			this.matches = matches;
		}
		propertyChangeListeners.firePropertyChange("matches", old, matches);
	}

	/**
	 * merge values to publish
	 */
	protected void mergePublish()
	{
		final Vector ps = new Vector();
		synchronized (this)
		{
			if (publishRequests.size() == 0)
			{
				// leave it alone
				return;
			}
			final Enumeration ve = publishRequests.elements();
			while (ve.hasMoreElements())
			{
				final DictionaryImpl[] d = (DictionaryImpl[]) ve.nextElement();
				for (final DictionaryImpl element : d)
				{
					if (element != null)
					{
						ps.addElement(element);
					}
				}
			}
		}
		final DictionaryImpl ds[] = (DictionaryImpl[]) ps.toArray(new DictionaryImpl[ps.size()]);
		setPublish(ds);
	}
}
