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

Created by: Alastair Hampshire (University of Nottingham)
Contributors:
  Alastair Hampshire (University of Nottingham)
  Andy Law (produced the original processing component upon which this component is based)

 */
package equip.ect.components.goldsmiths;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 * @displayName Postcode Engine
 * @classification user/goldsmiths
 * @preferred
 */
public class PostcodeEngine implements Serializable, Runnable
{
	class Postcode
	{
		public String code;
		public int easting;
		public int northing;
		public int eastingCentre = 0;
		public int northingCentre = 0;
		public double angle = 0;
		public double dist = 0;

		public Postcode(final String code, final int northing, final int easting)
		{
			this.code = code;
			this.easting = easting;
			this.northing = northing;
		}

		public int getX()
		{
			return easting - eastingCentre;
		}

		public int getY()
		{
			return northing - northingCentre;
		}

		public void update(final int northingCentre, final int eastingCentre)
		{
			this.eastingCentre = eastingCentre;
			this.northingCentre = northingCentre;

			angle = Math.PI + Math.atan2(getY(), getX());
			dist = Math.sqrt((getX() * getX()) + (getY() * getY()));
		}
	}

	class PostcodeComp implements Comparator<Postcode>
	{
		@Override
		public int compare(final Postcode postcodeA, final Postcode postcodeB)
		{
			if (postcodeA.dist < postcodeB.dist)
			{
				return -1;
			}
			else if (postcodeA.dist == postcodeB.dist)
			{
				return 0;
			}
			return 1;
		}
	}

	List<Postcode> postcodes = new ArrayList<Postcode>();

	Thread updater = null;

	private boolean running = true;
	protected long delay = 1000;

	protected String attention = "";
	protected String filename = "";
	protected double oneUnitOfSpeedEqualsXUnitsOFDistance = 10;

	protected int numberOfPostcodes = 10;
	protected int eastingCentre = -1;

	protected int northingCentre = -1;
	protected float angleTolerance = (float) Math.PI / 4;
	protected float angle;

	protected float speed;
	protected String[] nearestPostcodes;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public PostcodeEngine()
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

	public synchronized void calculateNearestPostcodes()
	{
		final List<Postcode> tempPostcodes = new ArrayList<Postcode>();

		for (Postcode postcode: postcodes)
		{
			// int yPos = postcode.getY();
			// int xPos = postcode.getX();

			// System.out.println(postcode.angle);
			// System.out.println(postcode.dist);

			if ((postcode.angle <= angle + angleTolerance) && (postcode.angle >= angle - angleTolerance))
			{
				// float dist = (float) Math.sqrt(yPos * yPos + xPos * xPos);
				// System.out.println("dist: " + dist);
				if (speed * oneUnitOfSpeedEqualsXUnitsOFDistance > postcode.dist)
				{
					tempPostcodes.add(postcode);
				}
			}
		}

		Collections.sort(tempPostcodes, new PostcodeComp());
		nearestPostcodes = new String[numberOfPostcodes];
		if (tempPostcodes.size() < numberOfPostcodes)
		{
			nearestPostcodes = (String[]) tempPostcodes.toArray(new String[tempPostcodes.size()]);
		}
		else
		{
			for (int q = 0; q < numberOfPostcodes; q++)
			{
				nearestPostcodes[q] = tempPostcodes.get(q * (tempPostcodes.size() / numberOfPostcodes));
			}
		}

		/*
		 * nearestPostcodes = new String[tempPostcodes.size()]; for (int g=0;
		 * g<tempPostcodes.size(); g++) { //System.out.println("got code: " + (String)
		 * tempPostcodes.elementAt(g)); nearestPostcodes[g] = (String) tempPostcodes.elementAt(g); }
		 */

		propertyChangeListeners.firePropertyChange("nearestPostcodes", "", nearestPostcodes);
	}

	public synchronized Float getAngle()
	{
		return new Float(angle);
	}

	public synchronized Float getAngleTolerance()
	{
		return new Float(angleTolerance);
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	public synchronized Long getDelay()
	{
		return new Long(delay);
	}

	public synchronized Integer getEastingCentre()
	{
		return new Integer(eastingCentre);
	}

	public String getFilename()
	{
		return filename;
	}

	public synchronized String[] getNearestPostcodes()
	{
		return nearestPostcodes;
	}

	public synchronized Integer getNorthingCentre()
	{
		return new Integer(northingCentre);
	}

	public synchronized Integer getNumberOfPostcodes()
	{
		return new Integer(numberOfPostcodes);
	}

	public synchronized Double getOneUnitOfSpeedEqualsXUnitsOFDistance()
	{
		return new Double(oneUnitOfSpeedEqualsXUnitsOFDistance);
	}

	public synchronized Float getSpeed()
	{
		return new Float(speed);
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	@Override
	public void run()
	{
		double oldAngle = -1;
		double oldSpeed = -1;

		while (running)
		{
			if (northingCentre != -1 && eastingCentre != -1)
			{
				if (oldAngle != angle || oldSpeed != speed)
				{

					final List<Postcode> tempPostcodes = new ArrayList<Postcode>();

					for (int i = 0; i < postcodes.size(); i++)
					{
						final Postcode postcode = (Postcode) postcodes.get(i);
						// int yPos = postcode.getY();
						// int xPos = postcode.getX();

						// System.out.println(postcode.angle);
						// System.out.println(postcode.dist);

						if ((postcode.angle <= angle + angleTolerance) && (postcode.angle >= angle - angleTolerance))
						{
							// float dist = (float) Math.sqrt(yPos * yPos + xPos * xPos);
							// System.out.println("dist: " + dist);
							if (speed * oneUnitOfSpeedEqualsXUnitsOFDistance > postcode.dist)
							{
								tempPostcodes.add(postcode);
							}
							else
							{
								// the postcode list is ordered, so we can break now
								break;
							}
						}
					}

					// Collections.sort(tempPostcodes, new PostcodeComp());
					nearestPostcodes = new String[numberOfPostcodes];
					if (tempPostcodes.size() < numberOfPostcodes)
					{
						nearestPostcodes = (String[]) tempPostcodes.toArray(new String[tempPostcodes.size()]);
					}
					else
					{
						for (int q = 0; q < numberOfPostcodes; q++)
						{
							nearestPostcodes[q] = (String) tempPostcodes.get(q
									* (tempPostcodes.size() / numberOfPostcodes));
						}
					}

					oldSpeed = speed;
					oldAngle = angle;

					propertyChangeListeners.firePropertyChange("nearestPostcodes", "", nearestPostcodes);
				}

				try
				{
					Thread.sleep(delay);
				}
				catch (final InterruptedException ie)
				{
				}
			}
		}
	}

	public synchronized void setAngle(final Float newAngle)
	{
		final Float oldAngle = new Float(this.angle);
		this.angle = newAngle.floatValue();
		propertyChangeListeners.firePropertyChange("angle", oldAngle, newAngle);

		// calculateNearestPostcodes();
	}

	public synchronized void setAngleTolerance(final Float newAngleTolerance)
	{
		final Float oldAngleTolerance = new Float(this.angleTolerance);
		this.angleTolerance = newAngleTolerance.floatValue();
		propertyChangeListeners.firePropertyChange("angleTolerance", oldAngleTolerance, newAngleTolerance);

		// calculateNearestPostcodes();
	}

	public synchronized void setDelay(final Long num)
	{
		final Long old = new Long(this.delay);
		this.delay = num.longValue();
		propertyChangeListeners.firePropertyChange("delay", old, num);

		// calculateNearestPostcodes();
	}

	public synchronized void setEastingCentre(final Integer newEastingCentre)
	{
		final Integer old = new Integer(this.eastingCentre);
		this.eastingCentre = newEastingCentre.intValue();
		propertyChangeListeners.firePropertyChange("eastingCentre", old, newEastingCentre);

		updatePostcodes();
		// calculateNearestPostcodes();
	}

	public synchronized void setFilename(final String newFilename)
	{
		final String old = this.filename;
		this.filename = newFilename;
		propertyChangeListeners.firePropertyChange("filename", old, "" + this.filename);

		final String readableDataText = null;// reset readableDataText
		postcodes = new Vector();
		try
		{// create buffer bufferIn to read file readableData.txt
			final BufferedReader bufferIn = new BufferedReader(new InputStreamReader(new FileInputStream(new File(
					filename))));
			String readableDataLine;
			while ((readableDataLine = bufferIn.readLine()) != null)// if bufferIn has a line
			{
				// find the begining of the grid ref
				// format coming in = "SP4 9,41504,14620"
				final int startReading = readableDataLine.indexOf(",", 0);
				final String postCodeExtract = readableDataLine.substring(0, startReading);
				// String northingExtract = readableDataLine.substring(startReading+7,
				// readableDataLine.length());
				// String eastingExtract = readableDataLine.substring(startReading+1,
				// startReading+6);
				final int eastingExtract = Integer.parseInt(readableDataLine.substring(	startReading + 1,
																						startReading + 6));
				final int northingExtract = Integer.parseInt(readableDataLine.substring(startReading + 7,
																						readableDataLine.length()));
				// println
				// ("postCode_"+postCodeExtract+"_easting_"+eastingExtract+"_northing_"+northingExtract);

				// create a postCodeObject
				// startObject(postCodeExtract, eastingExtract, northingExtract);
				postcodes.add(new Postcode(postCodeExtract, northingExtract, eastingExtract));

			}
			bufferIn.close();// if bufferIn has run out of lines close it

			attention = "data file sucessfully loaded";
			propertyChangeListeners.firePropertyChange("attention", "", attention);

			System.out.println("Buffer Closed");
			// configured = true;

			updatePostcodes();

			if (updater == null)
			{
				updater = new Thread(this);
				updater.start();
			}
		}
		catch (final IOException e)
		{
			System.out.println("Bugger: I've gone wrong: " + e.getMessage());
			attention = "Problem loading data file: " + e.getMessage();
			propertyChangeListeners.firePropertyChange("attention", "", attention);
		}
	}

	public synchronized void setNorthingCentre(final Integer newNorthingCentre)
	{
		final Integer old = new Integer(this.northingCentre);
		this.northingCentre = newNorthingCentre.intValue();
		propertyChangeListeners.firePropertyChange("northingCentre", old, new Integer(this.northingCentre));

		updatePostcodes();
		// calculateNearestPostcodes();
	}

	public synchronized void setNumberOfPostcodes(final Integer num)
	{
		final Integer old = new Integer(this.numberOfPostcodes);
		this.numberOfPostcodes = num.intValue();
		propertyChangeListeners.firePropertyChange("numberOfPostcodes", old, num);

		// calculateNearestPostcodes();
	}

	public synchronized void setOneUnitOfSpeedEqualsXUnitsOFDistance(final Double num)
	{
		final Double old = new Double(this.oneUnitOfSpeedEqualsXUnitsOFDistance);
		this.oneUnitOfSpeedEqualsXUnitsOFDistance = num.doubleValue();
		propertyChangeListeners.firePropertyChange("oneUnitOfSpeedEqualsXUnitsOFDistance", old, num);

		// calculateNearestPostcodes();
	}

	public synchronized void setSpeed(final Float newSpeed)
	{
		final Float oldSpeed = new Float(this.speed);
		this.speed = newSpeed.floatValue();
		propertyChangeListeners.firePropertyChange("speed", oldSpeed, newSpeed);

		// calculateNearestPostcodes();
	}

	public synchronized void stop()
	{
	}

	public synchronized void updatePostcodes()
	{
		if (northingCentre != -1 && eastingCentre != -1)
		{
			for (int g = 0; g < postcodes.size(); g++)
			{
				((Postcode) postcodes.elementAt(g)).update(northingCentre, eastingCentre);
			}
		}

		Collections.sort(postcodes, new PostcodeComp());
	}
}
