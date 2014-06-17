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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.messageboard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import equip.ect.ContainerManager;
import equip.ect.ContainerManagerHelper;
import equip.ect.Persistable;
import equip.ect.PersistenceManager;

public class MessageBoard implements Serializable, Persistable
{

	private String dataIn = null;
	private String dataOut = null;
	private PersistenceManager manager = PersistenceManager.getPersistenceManager();
	private File persistFile = new File(manager.COMPONENT_PERSISTENCE_DIRECTORY, "MessageBoard");
	private MessageBoardGUI gui = null;
	private transient PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public MessageBoard() throws Exception
	{
		final PersistedState state = load();
		gui = new MessageBoardGUI(this, state);
		gui.setVisible(true);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		listeners.addPropertyChangeListener(listener);
	}

	public String getDataIn()
	{
		return dataIn;
	}

	public String getDataOut()
	{
		return dataOut;
	}

	public synchronized PersistedState load()
	{
		try
		{
			final ClassLoader loader = ContainerManagerHelper.getInstance().getClassLoader();
			final Object obj = manager.loadObject(persistFile, loader);
			if (obj != null && obj instanceof PersistedState) { return ((PersistedState) obj); }
		}
		catch (final Exception e)
		{
			System.out.println(e);
		}
		return null;
	}

	@Override
	public synchronized void load(final File persistFile, final ContainerManager containerManager) throws IOException
	{
	}

	@Override
	public synchronized File persist(final ContainerManager containerManager) throws IOException
	{
		try
		{
			final PersistedState state = gui.persistState();
			manager.persistObject(persistFile, state);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return persistFile;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		listeners.removePropertyChangeListener(listener);
	}

	public void setDataIn(final String dataIn)
	{
		listeners.firePropertyChange(MessageBoardBeanInfo.DATA_IN_PROPERTY_NAME, this.dataIn, dataIn);
		this.dataIn = dataIn;
		if (dataIn != null)
		{
			try
			{
				final String vals[] = dataIn.split("\",");
				for (int i = 0; i < vals.length; i++)
				{
					vals[i] = vals[i].replace('\"', ' ').trim();
				}
				if (vals.length > 2)
				{
					gui.add(vals[0], vals[1], vals[2]);
				}
				else if (vals.length > 1)
				{
					gui.add(vals[0], vals[1], null);
				}
			}
			catch (final Exception e)
			{
				System.out.println("error parsing incoming" + " data for message board:\n" + e);
			}
		}
	}

	public synchronized void stop()
	{
		gui.dispose();
	}

	protected void setDataOut(final String dataOut)
	{
		listeners.firePropertyChange(MessageBoardBeanInfo.DATA_OUT_PROPERTY_NAME, this.dataOut, dataOut);
		this.dataOut = dataOut;
	}

	protected void setDataOut(final String key, final String msg, final String attach)
	{
		setDataOut("\"" + key + "\", \"" + msg + "\"" + (attach != null ? ", \"" + attach + "\"" : ""));
	}
}
