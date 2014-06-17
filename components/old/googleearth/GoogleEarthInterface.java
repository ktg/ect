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
package equip.ect.components.googleearth;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

import equip.ect.ContainerManagerHelper;

public class GoogleEarthInterface implements Serializable
{

	static double stringToDouble(final String doubleString)
	{
		return Double.parseDouble(doubleString);
	}

	private String DEFAULT_KML_FILENAME = "google_earth.kml";

	private File httpDirectory;

	// private double longitud, latitude, range;

	private Hashtable settings = new Hashtable();

	private boolean update = false;

	private String googleEarthClient = "C:\\Program Files\\Google\\Google Earth\\GoogleEarth.exe";

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public GoogleEarthInterface()
	{

		settings.put("longitude", "-69.90");
		settings.put("latitude", "18.5");
		settings.put("range", "305.8880792294568");
		settings.put("tilt", "46.72425699662645");
		settings.put("heading", "49.06133439171233");
		this.httpDirectory = ContainerManagerHelper.getHttpDirectory();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getGoogleEarthClient()
	{
		return googleEarthClient;
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

	public void setGoogleEarthClient(final String googleClient)
	{
		final String old = this.googleEarthClient;
		this.googleEarthClient = googleClient;
		propertyChangeListeners.firePropertyChange("googleEarthClient", old, googleEarthClient);
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
			final String kmlContent = mapToKMLContent();
			final File kmlFile = writeKMLFile(kmlContent);
			callRuntimeGoogleEarthClient(kmlFile);
			propertyChangeListeners.firePropertyChange("update", old, update);
			setUpdate(false);
		}
		else
		{
			propertyChangeListeners.firePropertyChange("update", old, update);
		}

	}

	protected String mapToKMLContent()
	{
		final double longitude = stringToDouble((String) settings.get("longitude"));
		final double latitude = stringToDouble((String) settings.get("latitude"));
		final double range = stringToDouble((String) settings.get("range"));
		final double tilt = stringToDouble((String) settings.get("tilt"));
		final double heading = stringToDouble((String) settings.get("heading"));

		final String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<kml xmlns=\"http://earth.google.com/kml/2.0\">" + "<Placemark>"
				+ "<description>Tethered to the ground by a customizable tail</description>"
				+ "<name>Tethethed placemark</name>" + "<LookAt>" + "<longitude>"
				+ longitude
				+ "</longitude>"
				+ "<latitude>"
				+ latitude
				+ "</latitude>"
				+ "<range>"
				+ range
				+ "</range>"
				+ "<tilt>"
				+ tilt
				+ "</tilt>"
				+ "<heading>"
				+ heading
				+ "</heading>"
				+ "</LookAt>"
				+ "<visibility>0</visibility>"
				+ "<Style>"
				+ "<IconStyle>"
				+ "<Icon>"
				+ "<href>root://icons/palette-3.png</href>"
				+ "<x>96</x>"
				+ "<y>160</y>"
				+ "<w>32</w>"
				+ "<h>32</h>"
				+ "</Icon>"
				+ "</IconStyle>"
				+ "</Style>"
				+ "<Point>"
				+ "<extrude>1</extrude>"
				+ "<altitudeMode>relativeToGround</altitudeMode>"
				+ "<coordinates>-122.0856204541786,37.42244015321688,50</coordinates>"
				+ "</Point>"
				+ "</Placemark>"
				+ "</kml>";
		return kml;
	}

	protected File writeKMLFile(final String kmlContent)
	{
		final File file = ContainerManagerHelper.createLocalFile(DEFAULT_KML_FILENAME, httpDirectory);

		try
		{
			final FileWriter fos = new FileWriter(file);
			fos.write(kmlContent);
			fos.flush();
			fos.close();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file;
	}

	private void callRuntimeGoogleEarthClient(final File kmlFile)
	{

		try
		{
			final String command = googleEarthClient + " " + kmlFile.getCanonicalPath();

			System.out.println("*** calling process=>" + command);

			final Process proc = Runtime.getRuntime().exec(command);
			// BufferedOutputStream bis = new
			// BufferedOutputStream(proc.getOutputStream());
			// byte[] bytes = mapToKMLContent().getBytes();
			// bis.write(bytes, 0, bytes.length);
			// bis.flush();
			// bis.close();
			System.out.println("*** done calling process");
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
