/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (Swedish Institute of Computer Science AB)
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.camera;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;
import java.net.URL;

import equip.ect.ContainerManagerHelper;

/**
 * Captures individual frames from a camera. <H3>Description</H3>
 * <P>
 * When triggered, this component captures images using a camera, and publishes these images using
 * an embedded web-server. For each captured image which has been published to the web-server, the
 * component publishes the URL at which the image can be found.
 * </P>
 * <P>
 * The component is compatible with any camera that can be registered with the Java Media Framework
 * (JMF). This includes both web-cams and camera which may be sampled by some capture devices.
 * </P>
 * <H3>Installation</H3> The camera component is dependent upon the installation of the Java Media
 * Framework (JMF) which is downloadable from http://java.sun.com . Before using the component, you
 * should
 * <ul>
 * <li>install JMF - if using Windows, this should make JMF available as an option on your Start
 * menu
 * <li>open the JMF registry, make sure your camera is registered, and note the JMF id it has been
 * allocated (in Windows, the JMF registry is accessable using the
 * <tt>Java Media Framework -> JMF Registry</tt> start menu option
 * 
 * </ul>
 * <H3>Configuration</H3> Create an instance of the camera component, and specify the JMF id of your
 * camera using the <i>configCaptureDevice</i> property (though the default value in this property
 * may be sufficient) <H3>Usage</H3> Every time a new value is supplied to the
 * <i>triggerImageCapture</i> property, then your camera should take a picture. The URL of this
 * picture will then appear on the <i>imageLocation</i> property. If available, you may be able to
 * use the <B>SimpleMediaViewer</B> component to view this image.
 * 
 * @displayName Camera
 * @classification Media/Video/Capture
 * @preferred
 * @technology JMF video processing
 */

public class Camera implements Serializable
{

	private transient File httpDirectory = ContainerManagerHelper.getHttpDirectory();

	private transient CameraPictureGrabber picGrabber = null;

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Property
	private String url = null;
	private Object captureState = null;
	private String captureDevice = CameraPictureGrabber.DEFAULT_CAPTURE_DEVICE;

	public Camera()
	{

		try
		{
			picGrabber = new CameraPictureGrabber(captureDevice);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR creating default grabber " + captureDevice + ": " + e);
			captureDevice = "";
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public void captureImage()
	{
		System.out.println("called capture image");
		try
		{
			final File image = picGrabber.grabAndDumpToFile(httpDirectory);
			final URL url = ContainerManagerHelper.uploadToHttp(image);
			if (url != null)
			{
				setImageLocation(url.toExternalForm());

			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public synchronized String getConfigCaptureDevice()
	{
		return captureDevice;
	}

	public String getImageLocation()
	{
		return url;
	}

	public Object getTriggerImageCapture()
	{
		return null;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public synchronized void setConfigCaptureDevice(final String device)
	{
		if (captureDevice.equals(device)) { return; }
		final String oldValue = captureDevice;
		this.captureDevice = device;
		if (picGrabber != null)
		{
			System.out.println("Camera component stopping old capture device");
			try
			{
				picGrabber.stop();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR stopping old capture device: " + e);
			}
			picGrabber = null;
		}
		try
		{
			picGrabber = new CameraPictureGrabber(captureDevice);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR creating grabber " + captureDevice + ": " + e);
			captureDevice = "";
		}
		propertyChangeListeners.firePropertyChange("captureDevice", oldValue, captureDevice);
	}

	public synchronized void setImageLocation(final String url)
	{

		final String oldURL = this.url;
		this.url = url;

		propertyChangeListeners.firePropertyChange("imageLocation", oldURL, this.url);
	}

	public void setTriggerImageCapture(final Object ob)
	{
		if (ob != null)
		{
			captureImage();
		}
	}

	public void stop()
	{
		picGrabber.stop();
	}

}
