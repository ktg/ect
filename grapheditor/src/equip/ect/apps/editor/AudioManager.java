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
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * AudioManager, $RCSfile: AudioManager.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/05/22 14:10:18 $
 * $Author: chaoticgalen $
 * Original Author: Jan Humble, Karl-Petter kesson
 * Copyright (c) 2002, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * The AudioManager class is designed to hold references to media resources. Resources are given a
 * name for callback. They can take the form of filenames for streaming or caching.
 */

public class AudioManager implements LineListener
{

	public static boolean audioOn = false;

	/**
	 * The way to access the AudioManager. Only one instance allowed.
	 */
	public static synchronized AudioManager getAudioManager()
	{
		if (instance == null)
		{
			instance = new AudioManager();
		}
		return instance;
	}

	private Map<String, Object> resources;

	private Map<String, Clip> audioLines;

	static AudioManager instance;

	public final static String propFileName = "audio.resources";

	/**
	 * Toggle audio status on/off.
	 */
	public static void audioOn(final boolean audioOn)
	{
		AudioManager.audioOn = audioOn;
	}

	/**
	 * Universal constructor. Useful for adding common resources.
	 */
	private AudioManager()
	{
		resources = new HashMap<String, Object>();
		audioLines = new HashMap<String, Clip>();
		loadResourcesFromFile(propFileName);
	}

	/**
	 * Adds a resource to the repository.
	 */
	public void addResource(final String resourceName, final Object resource)
	{
		resources.put(resourceName, resource);
	}

	/**
	 * Returns a specific media resource.
	 */
	public Object getResource(final String resourceName)
	{
		return resources.get(resourceName);
	}

	public void loadResourcesFromFile(final String fileName)
	{
		try
		{
			final FileInputStream fis = new FileInputStream(fileName);
			final Properties props = new Properties();
			props.load(fis);
			for(String name: props.stringPropertyNames())
			{
				final String value = props.getProperty(name);
				addResource(name, value);
			}
		}
		catch (final FileNotFoundException fnfe)
		{
			System.out.println("AudioManager: Warning Audio resource file not found.");
			return;
		}
		catch (final IOException ioe)
		{
			System.out.println("AudioManager: Error while reading from audio resource file.");
			return;
		}
	}

	/**
	 * Plays a sound file from a valid resource name.
	 */
	public void playSoundResource(final String resourceName)
	{
		if (audioOn)
		{
			final Object resource = getResource(resourceName);
			if (resource != null)
			{
				if (resource instanceof String)
				{
					final Clip outline = getAudioLineFromFile((String) resource);
					if (outline != null)
					{
						// System.out.println("AudioManager: Playing resource: "
						// + resource);
						outline.start();
						audioLines.put(resourceName, outline);
					}
					else
					{
						System.out.println("AudioManager: Warning, no audioline for resource: " + resource);
					}
				}
			}
			else
			{
				System.out.println("AudioManager: Warning, resource not found: " + resourceName);
			}
		}
	}

	public void stopSoundResource(final String resourceName)
	{
		stopSoundResource(resourceName, true);
	}

	/**
	 * Stops a sound file from a valid resource name. Set terminate to true if the line should be
	 * closed, e g if no resumed playback is wanted.
	 */
	public void stopSoundResource(final String resourceName, final boolean terminate)
	{
		if (audioOn)
		{
			//final Object resource = getResource(resourceName);
			final Clip outline = audioLines.get(resourceName);
			if (outline != null)
			{
				outline.stop();
				if (terminate)
				{
					outline.close();
					audioLines.remove(resourceName);
				}
			}
		}
	}

	@Override
	public void update(final LineEvent event)
	{
		// System.out.println("Got an event on this " + event.getType());
		if (event.getType().equals(LineEvent.Type.STOP))
		{
			final Line outline = event.getLine();
			outline.close();
			// should clean up the audio line table at this point
			cleanAudioLineTable();
		}
	}

	private void cleanAudioLineTable()
	{
		synchronized (audioLines)
		{
			for(String resource: audioLines.keySet())
			{
				final Clip line = audioLines.get(resource);
				if (!line.isOpen())
				{
					audioLines.remove(resource);
				}
			}
		}
	}

	/**
	 * Internal implementation for streaming audio from a file.
	 */
	private Clip getAudioLineFromFile(final String filename)
	{
		AudioInputStream audioInputStream = null;
		try
		{
			// File soundFile = new File(filename);
			final java.net.URL url = getClass().getResource(filename);
			if (url != null)
			{
				audioInputStream = AudioSystem.getAudioInputStream(url);
			}
		}
		catch (final UnsupportedAudioFileException uafe)
		{
			System.out.println(uafe.getMessage());
			return null;
		}
		catch (final IOException ioe)
		{
			System.out.println(ioe.getMessage());
			return null;
		}

		if (audioInputStream == null)
		{
			Info.message("AudioManager: Warning resource not found '" + filename + "'");
			return null;
		}
		AudioFormat streamFormat = null;
		streamFormat = audioInputStream.getFormat();
		final DataLine.Info info = new DataLine.Info(Clip.class, streamFormat);

		if (!AudioSystem.isLineSupported(info))
		{
			System.out.println("AudioManager: An out line matching " + info + " is not found.");
			return null;
		}

		// get and open the source data line for playback.
		final Clip outline;
		try
		{
			outline = (Clip) AudioSystem.getLine(info);
			outline.addLineListener(this);
			outline.open(audioInputStream);
		}
		catch (final LineUnavailableException ex)
		{
			System.err.println("AudioManager: Unable to open the out line: " + ex);
			return null;
		}
		catch (final IOException ex)
		{
			System.err.println("AudioManager: Unable to open the out line: " + ex);
			return null;
		}

		if (!outline.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			System.out.println("AudioManager: Gain Control is not supported on output line");
		}
		else
		{
			final FloatControl gainControl = (FloatControl) outline.getControl(FloatControl.Type.MASTER_GAIN);
			/*
			 * System.out.println("Gain was " +gainControl.getValue() +"("+gainControl.getMaximum()
			 * +")");
			 */
			gainControl.setValue(gainControl.getMaximum());
		}

		// play back the captured audio data

		return outline;
	}

}
