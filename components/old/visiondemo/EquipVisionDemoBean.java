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
package equip.ect.components.visiondemo;

/*
 * wrapper for EquipVisionDemo class as bean
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class EquipVisionDemoBean implements PropertyChangeListener, Serializable
{

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Properties
	private String foregroundUrl = "http://www.cs.nott.ac.uk/~smx/skin.jpg";
	private String backgroundUrl = "http://www.cs.nott.ac.uk/~smx/background.jpg";
	private boolean needsTraining = true;

	int medianRadius = 2;
	int minBlobSize = 100;
	private int numBlobs;
	public double blob0X, blob0Y, blob0Size;

	private String image = null;
	private EquipVisionDemo proc;

	/**
     *
     *
     */
	public EquipVisionDemoBean()
	{
		proc = new EquipVisionDemo();
		// initial/default values
		proc.setMedianRadius(medianRadius);
		proc.setMinBlobSize(minBlobSize);

	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getBackgroundUrl()
	{
		return backgroundUrl;
	}

	public double getBlob0Size()
	{
		return blob0Size;
	}

	public double getBlob0X()
	{
		return blob0X;
	}

	public double getBlob0Y()
	{
		return blob0Y;
	}

	public String getForegroundUrl()
	{
		return foregroundUrl;
	}

	public String getImage()
	{
		return image;
	}

	public int getMedianRadius()
	{
		return medianRadius;
	}

	public int getMinBlobSize()
	{
		return minBlobSize;
	}

	public int getNumBlobs()
	{
		return numBlobs;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{

	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setBackgroundUrl(final String url)
	{
		final String oldUrl = foregroundUrl;
		backgroundUrl = url;
		needsTraining = true;
		propertyChangeListeners.firePropertyChange("backgroundUrl", oldUrl, url);
	}

	public void setForegroundUrl(final String url)
	{
		final String oldUrl = foregroundUrl;
		foregroundUrl = url;
		needsTraining = true;
		propertyChangeListeners.firePropertyChange("foregroundUrl", oldUrl, url);
	}

	/* the main event */
	public synchronized void setImage(final String url)
	{
		final String oldVal = image;
		image = url;
		propertyChangeListeners.firePropertyChange("image", oldVal, url);

		if (needsTraining)
		{
			System.err.println("Training vision on " + foregroundUrl + " and " + backgroundUrl);
			try
			{
				proc.trainValues(foregroundUrl, backgroundUrl);
			}
			catch (final Exception e)
			{
				System.err.println("Training error: " + e);
			}
			needsTraining = false;
		}
		try
		{
			proc.imageUpdate(url);
		}
		catch (final Exception e)
		{
			System.err.println("Image update error: " + e);
			return;
		}
		// set outputs
		final int oldNumBlobs = numBlobs;
		numBlobs = proc.getNumBlobs();
		final double oldBlob0X = blob0X;
		final double oldBlob0Y = blob0Y;
		final double oldBlob0Size = blob0Size;
		if (numBlobs > 0)
		{
			blob0X = proc.getBlobX(0);
			blob0Y = proc.getBlobY(0);
			blob0Size = proc.getBlobSize(0);
		}

		propertyChangeListeners.firePropertyChange("numBlobs", new Double(oldNumBlobs), new Double(numBlobs));
		propertyChangeListeners.firePropertyChange("blob0X", new Double(oldBlob0X), new Double(blob0X));
		propertyChangeListeners.firePropertyChange("blob0Y", new Double(oldBlob0Y), new Double(blob0Y));
		propertyChangeListeners.firePropertyChange("blob0Size", new Double(oldBlob0Size), new Double(blob0Size));

		System.err.println("Blobs: " + numBlobs + ", first at " + blob0X + ", " + blob0Y + " size " + blob0Size);
	}

	public void setMedianRadius(final int val)
	{
		final int oldVal = medianRadius;
		medianRadius = val;
		proc.setMedianRadius(medianRadius);
		propertyChangeListeners.firePropertyChange("medianRadius", new Integer(oldVal), new Integer(val));
	}

	public void setMinBlobSize(final int val)
	{
		final int oldVal = minBlobSize;
		minBlobSize = val;
		proc.setMinBlobSize(minBlobSize);
		propertyChangeListeners.firePropertyChange("minBlobSize", new Integer(oldVal), new Integer(val));
	}

}
