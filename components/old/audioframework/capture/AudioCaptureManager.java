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
package equip.ect.components.audioframework.capture;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.MediaLocator;
import javax.media.format.AudioFormat;

/**
 * Factory for AudioCapture components (part of Media/Audio/Analysis framework; uses JMF). <H3>
 * Description</H3> A factory for AudioCaptureDevice components, which allows audio to be captured
 * from JMF-supported audio devices for use with the associated analysis components.
 * 
 * <H3>Installation</H3> Requires Java Media Framework (JMF) to be installed, and capture devices
 * registered with the JMF registry. Currently developed with version 2.1.1e.
 * 
 * <H3>Configuration</H3> No explicit configuration, but see installation notes.
 * 
 * <H3>Usage</H3> Create a single instance on a machine on which audio is to be captured and
 * analysed. AudioCaptureDevice sub-components will be dynamically generated for all registered JMF
 * audio capture devices. These can then be linked to the other components in the audio framework
 * for analysis.
 * 
 * <H3>Technical Details</H3> Based on JMF. Using javax.sound would probably now be better and more
 * portable. Structure based on video framework classes.
 * 
 * @classification Media/Audio/Analysis
 * @technology JMF audio processing
 */
public class AudioCaptureManager implements Serializable
{

	/**
	 * Standalone test main
	 */
	public static void main(final String[] args)
	{
		final AudioCaptureManager mgr = new AudioCaptureManager();
	}

	/**
	 * Property Change
	 */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	/**
	 * Vector of AudioCaptureDevice
	 */
	protected Vector children = new Vector();

	/**
	 * status
	 */
	protected String status = "new";

	/**
	 * Default constructor.<br>
	 */
	public AudioCaptureManager()
	{

		try
		{
			// capture
			final Vector devs = CaptureDeviceManager.getDeviceList(/* null */new AudioFormat("LINEAR", 44100, 16, 2));
			int i;
			MediaLocator locator = null;
			for (i = 0; i < devs.size(); i++)
			{
				final CaptureDeviceInfo info = (CaptureDeviceInfo) devs.elementAt(i);
				System.out.println("name - " + info.getName());
				locator = info.getLocator();
				System.out.println("locator - " + info.getLocator());
				getChild(locator);
			}

			status = "OK";
		}
		catch (final Exception e)
		{
			System.out.println("ERROR initialising AudioCaptureManager: " + e);
			e.printStackTrace(System.err);
			status = "ERROR: " + e;
			// System.exit(0);
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * children getter
	 */
	public AudioCaptureDevice[] getChildren()
	{
		synchronized (this)
		{
			return (AudioCaptureDevice[]) children.toArray(new AudioCaptureDevice[children.size()]);
		}
	}

	/**
	 * status getter
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * stop
	 */
	public synchronized void stop()
	{
		for (int i = 0; i < children.size(); i++)
		{
			final AudioCaptureDevice child = (AudioCaptureDevice) children.elementAt(i);
			try
			{
				child.stop();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR stopping capture child " + child + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		final Object oldValue = getChildren();
		children.removeAllElements();
		propertyChangeListeners.firePropertyChange("children", oldValue, getChildren());
	}

	/**
	 * get/make child for JMF locator
	 */
	protected AudioCaptureDevice getChild(final MediaLocator locator)
	{
		AudioCaptureDevice oldValue[] = null;
		AudioCaptureDevice newValue[] = null;
		AudioCaptureDevice newChild = null;
		boolean changed = false;
		synchronized (this)
		{
			oldValue = getChildren();
			int i;
			for (i = 0; i < oldValue.length; i++)
			{
				if (oldValue[i].getLocator().equals(locator)) { return oldValue[i]; }
			}

			try
			{
				System.out.println("Create AudioCaptureDevice " + locator);
				newChild = new AudioCaptureDevice(locator);
				children.addElement(newChild);
				newValue = getChildren();
				changed = true;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating AudioCaptureDevice " + locator + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		if (changed)
		{
			propertyChangeListeners.firePropertyChange("children", oldValue, newValue);
		}
		return newChild;
	}
}
