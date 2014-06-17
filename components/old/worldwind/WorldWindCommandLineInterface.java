/*
 <COPYRIGHT>

 Copyright (c) 2006, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */
package equip.ect.components.worldwind;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * @author humble
 * 
 */
public class WorldWindCommandLineInterface implements Serializable
{

	private File httpDirectory;

	private Hashtable settings = new Hashtable();

	// private double longitud, latitude, range;

	private boolean update = false;

	private static final String WORLDWIND_PREFIX = "worldwind://";

	static double stringToDouble(final String doubleString)
	{
		return Double.parseDouble(doubleString);
	}

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public WorldWindCommandLineInterface()
	{

		settings.put("longitude", "-72.0");
		settings.put("latitude", "22.0");
		settings.put("range", "1000000");
		settings.put("tilt", "51.57");
		settings.put("heading", "0.0");
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public Hashtable getSettings()
	{
		return this.settings;
	}

	public boolean getUpdate()
	{
		return this.update;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setSettings(final Hashtable newSettings)
	{
		final Hashtable old = this.settings;
		this.settings = newSettings;
		propertyChangeListeners.firePropertyChange("settings", old, settings);
	}

	public synchronized void setUpdate(final boolean update)
	{
		final boolean old = this.update;
		this.update = update;
		if (update)
		{
			final String argument = this.mapToWorldWindCommandContent();

			callRuntimeWorldWindClient(argument);
			propertyChangeListeners.firePropertyChange("update", old, update);
			setUpdate(false);
		}
		else
		{
			propertyChangeListeners.firePropertyChange("update", old, update);
		}

	}

	protected String mapToWorldWindCommandContent()
	{
		final double longitude = stringToDouble((String) settings.get("longitude"));
		final double latitude = stringToDouble((String) settings.get("latitude"));
		final double range = stringToDouble((String) settings.get("range"));
		final double tilt = stringToDouble((String) settings.get("tilt"));
		final double heading = stringToDouble((String) settings.get("heading"));

		final String command = WORLDWIND_PREFIX + "goto/world=Earth" + "&latitude=" + latitude + "&longitude="
				+ longitude + "&dir=" + heading + "&alt=" + range + "&tilt=" + tilt;
		return command;
	}

	private void callRuntimeWorldWindClient(final String argument)
	{

		try
		{
			// a bit of a hack
			final String command = "explorer " + "\"" + argument + "\"";
			System.out.println("*** calling process=>" + command);

			final Process proc = Runtime.getRuntime().exec(command);

			System.out.println("*** done calling process");
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
