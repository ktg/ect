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
package equip.ect.components.ipergobjectproxy;

import equip.ect.SimpleDynamicComponent;
import equip.ect.components.ipergobjectproxy.part.SetPropertyObject;
import org.iperg.platform.core.IpEvent;
import org.iperg.platform.core.IpEventHandler;
import org.iperg.platform.core.IpIdentifier;
import org.iperg.platform.core.IpUrl;
import org.iperg.platform.networking.IpLocalProcess;
import org.iperg.platform.networking.IpProcessEvent;
import org.iperg.platform.networking.IpRemoteProcess;
import org.iperg.platform.synchronisation.IpGameObject;
import org.iperg.platform.synchronisation.IpObjectPattern;
import org.iperg.platform.synchronisation.IpPropertyEvent;
import org.iperg.platform.synchronisation.IpVetoException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * An experimental factory component to interface to and replicate game objects from the IPerG PART
 * platform.
 *
 * @classification Experimental/IPerG
 */
public class IPerGObjectProxyFactory implements Serializable, IpEventHandler
{
	/**
	 * PARTobject search timeout (10s)
	 */
	protected static int SEARCH_TIMEOUT_MS = 10000;
	/**
	 * persistent child property name
	 */
	protected static String PERSISTENT_CHILD = "persistentChild";
	/**
	 * part subscription interval, ms (1 second)
	 */
	protected static int SUBSCRIPTION_INTERVAL_MS = 1000;
	/**
	 * part connect timeout ms (10 seconds)
	 */
	protected static int CONNECT_TIMEOUT_MS = 10000;
	/**
	 * part connected RemoteProcesses
	 */
	protected Vector m_clients = new Vector();
	/**
	 * urls to listen tcp port to listen for part connections
	 */
	protected String[] configPartListenUrls = new String[0];
	/**
	 * Part connections to establish
	 */
	protected String[] configPartConnectionUrls = new String[0];
	/**
	 * output value
	 */
	protected String status;
	/**
	 * dynamic components
	 */
	protected Vector subcomponents = new Vector();
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public IPerGObjectProxyFactory()
	{
		init();
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
	 * getter
	 */
	public synchronized String[] getConfigPartConnectionUrls()
	{
		return configPartConnectionUrls;
	}

	/**
	 * Part connections to establish.
	 *
	 * @preferred
	 */
	public void setConfigPartConnectionUrls(final String[] value)
	{
		String[] old = null;
		synchronized (this)
		{
			// TODO remove old connections
			// ....

			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.configPartConnectionUrls;
			this.configPartConnectionUrls = value;

			// try to listen
			for (int i = 0; configPartConnectionUrls != null && i < configPartConnectionUrls.length; i++)
			{
				try
				{
					// Listen to incoming connections on port 1000. Does not
					// block. Whenever a connection is accepted, our eventCb
					// will be called with a IpProcessEvent argument.
					final IpUrl url = new IpUrl(configPartConnectionUrls[i]);
					final IpRemoteProcess proc = IpLocalProcess.getInstance().connect(url, CONNECT_TIMEOUT_MS);
					System.out.println("Connected to address " + url);

					searchForObjects(proc);
				}
				catch (final Exception e)
				{
					System.out.println("Exception trying to connect to " + configPartConnectionUrls[i] + ": " + e);
					e.printStackTrace();
				}
			}
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configPartConnectionUrls", old, value);
	}

	/**
	 * getter
	 */
	public synchronized String[] getConfigPartListenUrls()
	{
		return configPartListenUrls;
	}

	/**
	 * Part connections to establish.
	 *
	 * @preferred
	 */
	public void setConfigPartListenUrls(final String[] value)
	{
		String[] old = null;
		synchronized (this)
		{
			// TODO remove old listens
			// ....

			// could suppress no-change setting
			// if (input==this.input || (input!=null && this.input!=null &&
			// input.equals(this.input)) return;
			old = this.configPartListenUrls;
			this.configPartListenUrls = value;

			// try to listen
			for (int i = 0; configPartListenUrls != null && i < configPartListenUrls.length; i++)
			{
				try
				{
					// Listen to incoming connections on port 1000. Does not
					// block. Whenever a connection is accepted, our eventCb
					// will be called with a IpProcessEvent argument.
					final IpUrl url = new IpUrl(configPartListenUrls[i]);
					IpLocalProcess.getInstance().listen(url);
					System.out.println("Listening to address " + url);
				}
				catch (final Exception e)
				{
					System.out.println("Exception trying to listen to " + configPartListenUrls[i] + ": " + e);
					e.printStackTrace();
				}
			}
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configPartListenUrls", old, value);
	}

	/**
	 * get subcomponents
	 */
	public synchronized SimpleDynamicComponent[] getObjectProxies()
	{
		return (SimpleDynamicComponent[]) subcomponents.toArray(new SimpleDynamicComponent[subcomponents.size()]);
	}

	/**
	 * general component status.
	 */
	public synchronized String getStatus()
	{
		return status;
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setStatus(final String output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldOutput = this.status;
		this.status = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("status", oldOutput, output);
	}

	/**
	 * PART event handler
	 */
	public void handleEvent(final IpEvent event)
	{
		if (event.getType().equals(IpProcessEvent.CONNECTION_ACCEPTED))
		{
			final IpRemoteProcess proc = ((IpProcessEvent) event).getProcess();
			System.out.println("A new client has connected! " + proc.getId());
			m_clients.addElement(proc);
			searchForObjects(proc);
		}
		else if (event.getType().equals(IpProcessEvent.CONNECTION_TERMINATED))
		{
			final IpProcessEvent e = (IpProcessEvent) event;
			System.out.println("Client is no longer connected! " + e.getConnection().getStatus());
			final IpRemoteProcess proc = ((IpProcessEvent) event).getProcess();
			m_clients.removeElement(proc);
			// TODO tidy up
		}
		else if (event.getType().equals(IpPropertyEvent.EVENT_TYPE))
		{
			final IpPropertyEvent propEvent = (IpPropertyEvent) event;
			System.out.println("Property '" + propEvent.getPropertyName() + "' was updated, value is '"
					+ propEvent.getObject().getProperty(propEvent.getPropertyName()) + "'");
			handlePropertyUpdate(propEvent.getObject().getId(), propEvent.getPropertyName(), propEvent.getObject()
					.getProperty(propEvent.getPropertyName()));
		}
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
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
	}

	/**
	 * add subcomponent
	 */
	protected void addSubcomponent(final SimpleDynamicComponent c)
	{
		SimpleDynamicComponent[] old = null;
		SimpleDynamicComponent[] value = null;
		synchronized (this)
		{
			old = getObjectProxies();
			subcomponents.addElement(c);
			value = getObjectProxies();
		}
		propertyChangeListeners.firePropertyChange("objectProxies", old, value);
	}

	/**
	 * handle newly discovered game object
	 */
	protected void handleNewGameObject(final IpGameObject object)
	{
		System.out.println("New game object " + object.getId() + ", class " + object.getClass().getName());

		// add new proxy
		final ObjectProxy proxy = new ObjectProxy(object.getId().toString());

		final Enumeration pe = object.getAllPropertyNames();
		while (pe.hasMoreElements())
		{
			final String pname = (String) pe.nextElement();
			final String value = object.getProperty(pname);
			System.out.println("  " + pname + " = " + value + " (last updated "
					+ (System.currentTimeMillis() - object.getMostRecentUpdateTime(pname)) + " ms ago)");

			proxy.addPropertySilent(pname, String.class, value);
		}
		try
		{
			object.subscribe(SUBSCRIPTION_INTERVAL_MS);
		}
		catch (final Exception e)
		{
			System.out.println("ERROR subscribing to game object " + object + ": " + e);
			e.printStackTrace(System.out);
		}

		addSubcomponent(proxy);
	}

	/**
	 * handle PART property update
	 */
	protected void handlePropertyUpdate(final IpIdentifier id, final String name, final String value)
	{
		ObjectProxy proxy = null;
		final String ids = id.toString();
		synchronized (this)
		{
			final Enumeration e = subcomponents.elements();
			while (proxy == null && e.hasMoreElements())
			{
				final ObjectProxy c = (ObjectProxy) e.nextElement();
				try
				{
					if (c.dynGetProperty(PERSISTENT_CHILD).equals(ids))
					{
						proxy = c;
					}
				}
				catch (final ect.NoSuchPropertyException e2)
				{
					System.out.println("Warning: proxy " + c + " has no " + PERSISTENT_CHILD + " Property");
				}
			}
		}
		if (proxy == null)
		{
			System.out.println("Could not find proxy for " + ids);
		}
		else
		{
			try
			{
				// suppress re-set
				proxy.dynSetPropertySilent(name, value);
			}
			catch (final ect.NoSuchPropertyException e2)
			{
				System.out.println("Warning: proxy for " + ids + " has no " + name + " property (now " + value + ")");
			}
		}

	}

	/**
	 * part initialise
	 */
	protected void init()
	{
		System.out.println("IPerGObjectProxyFactory init");
		// non-standard extension
		// IpManager.setDebugLevel(10);
		IpLocalProcess.getInstance().setEventHandler(this);
	}

	/**
	 * PART search for objects
	 */
	protected void searchForObjects(final IpRemoteProcess proc)
	{
		final IpObjectPattern pattern = new IpObjectPattern();
		pattern.setClass("org.iperg.platform.synchronisation.IpGameObject", IpObjectPattern.EXTENDS);
		// Try to find instances of the TestObject class
		// unfortunately blocking :-(
		try
		{
			System.out.println("Search for game object in process " + proc);
			final IpGameObject[] objects = IpGameObject.searchObjects(proc.getId(), pattern, 0, SEARCH_TIMEOUT_MS);
			System.out.println("- found " + objects.length + " game objects");
			for (int i = 0; i < objects.length; i++)
			{
				handleNewGameObject(objects[i]);
			}
		}
		catch (final Exception e)
		{
			System.out.println("Error searching for game objects in process " + proc + ": " + e);
			e.printStackTrace(System.out);
		}
	}

	/**
	 * set from ECT side
	 */
	protected boolean setGameObjectProperty(final String id, final String name, final String value)
			throws IpVetoException
	{
		System.out.println("Try to set game object " + id + " property " + name + " to " + value);

		final IpIdentifier ident = IpIdentifier.fromString(id);
		if (ident == null)
		{
			System.out.println("setGameObjectProperty: id invalid: " + id);
			return false;
		}
		final IpGameObject object = IpGameObject.getObject(ident);
		if (object == null)
		{
			System.out.println("setGameObjectProperty: object unknown: " + id);
			return false;
		}

		if (object instanceof SetPropertyObject)
		{
			((SetPropertyObject) object).setMasterProperty(name, value);
			// not set yet
			return false;
		}
		else
		{
			object.setProperty(name, value);
			// is set now
			return true;
		}
	}

	/**
	 * subcomponent class
	 */
	class ObjectProxy extends SimpleDynamicComponent
	{
		/**
		 * id
		 */
		String ids;
		/**
		 * silent
		 */
		int silent = 0;

		/**
		 * noarg cons
		 */
		ObjectProxy(final String ids)
		{
			super();
			this.ids = ids;
			addProperty(PERSISTENT_CHILD, String.class, ids);
			// addPropertyChangeListener(this);
		}

		/**
		 * override set property
		 */
		@Override
		public void dynSetProperty(final String name, final Object value) throws ect.NoSuchPropertyException
		{
			System.out
					.println("ObjectProxy property " + name + " set (" + (silent > 0 ? "silent" : "not silent") + ")");
			if (silent == 0)
			{
				try
				{
					final String svalue = (String) ect.Coerce.toClass(value, String.class);
					if (setGameObjectProperty(ids, name, svalue))
					{
						// is set now
						super.dynSetProperty(name, svalue);
					}
				}
				catch (final Exception e)
				{
					System.out.println("Unable to set object " + ids + " property " + name + " to " + value + ": " + e);
				}
			}
			else
			{
				super.dynSetProperty(name, value);
			}
		}

		/**
		 * internal set property
		 */
		void addPropertySilent(final String name, final Class clazz, final Object value)
		{
			synchronized (this)
			{
				silent++;
			}
			addProperty(name, clazz, value);
			synchronized (this)
			{
				silent--;
			}
		}

		/**
		 * internal set property
		 */
		void dynSetPropertySilent(final String name, final Object value) throws ect.NoSuchPropertyException
		{
			synchronized (this)
			{
				silent++;
			}
			dynSetProperty(name, value);
			synchronized (this)
			{
				silent--;
			}
		}

		/**
		 * get id
		 */
		String getId()
		{
			return ids;
		}
	}
}
