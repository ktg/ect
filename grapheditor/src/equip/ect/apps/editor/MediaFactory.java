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
 * MediaFactory, $RCSfile: MediaFactory.java,v $
 *
 * $Revision: 1.5 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * Copyright (c) 2002, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Utilities for media loading and conversion between formats. NOTE: The TransferableMediaObject
 * references should not be here, and perhaps this class belongs outside of distcomp.
 */

public abstract class MediaFactory
{

	public static Image addAlphaChannel(final Image image, final Component observer)
	{
		final BufferedImage alphaImage = new BufferedImage(image.getWidth(observer), image.getHeight(observer),
				BufferedImage.TYPE_INT_ARGB);
		alphaImage.createGraphics().drawImage(image, 0, 0, observer);
		return alphaImage;
	}

	/**
	 * Creates a TransferableMultiMedia (ex animation) from all images reciding in a directory.
	 */
	public static TransferableMultiMedia createFromAnimation(final File directory)
	{
		if (!directory.isDirectory())
		{
			System.out.println("Could not create animation: '" + directory + "' not a directory");
			return null;
		}

		int delay = 200;
		int[] indexes = null;

		final FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				if (name.endsWith(".jpg") || name.endsWith(".gif"))
				{
					return true;
				}
				return false;
			}
		};

		final FilenameFilter propFilter = new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				if (name.endsWith(".prop"))
				{
					return true;
				}
				return false;
			}
		};

		final File[] imageFiles = directory.listFiles(filter);
		final String[] names = new String[imageFiles.length];
		for (int i = 0; i < imageFiles.length; i++)
		{
			names[i] = imageFiles[i].getPath();
		}

		final TransferableMultiMedia tmm = new TransferableMultiMedia(names);

		final File[] propFiles = directory.listFiles(propFilter);
		if (propFiles != null && propFiles.length > 0)
		{
			final Properties props = loadBeanProperty(propFiles[0]);
			try
			{
				delay = Integer.parseInt(props.getProperty("delay"));
				tmm.setDelay(delay);
			}
			catch (final NumberFormatException nfe)
			{
				System.out.println(nfe.getMessage());
			}
			final String array = props.getProperty("indexes");
			if (array != null)
			{
				indexes = parseIntArray(array, ",; ");
				tmm.setPlaybackIndexes(indexes);
			}
		}

		return tmm;

	}

	public static TransferableMediaObject createFromAudio(final byte[] audioData)
	{
		TransferableMediaObject media = null;
		media = new TransferableMediaObject(TransferableMediaObject.AUDIO, audioData);
		return media;
	}

	public static TransferableMediaObject createFromAudio(final String filename)
	{
		TransferableMediaObject media = null;
		media = new TransferableMediaObject(TransferableMediaObject.AUDIO, getBytesFromFile(filename));
		return media;
	}

	public static TransferableMediaObject createFromImage(final String filename)
	{
		TransferableMediaObject media = null;
		media = new TransferableMediaObject(TransferableMediaObject.IMAGE, getBytesFromFile(filename));
		return media;
	}

	public static TransferableMediaObject createFromText(final String text)
	{
		TransferableMediaObject media = null;
		media = new TransferableMediaObject(TransferableMediaObject.TEXT, text.getBytes());
		return media;
	}

	public static TransferableMediaObject createFromTextFile(final String filename)
	{
		TransferableMediaObject media = null;
		media = new TransferableMediaObject(TransferableMediaObject.TEXT, getBytesFromFile(filename));
		return media;
	}

	public static Image createImage(final File file, final Component component)
	{
		if (file.exists())
		{
			return createImage(file.getAbsolutePath(), component);
		}
		return null;
	}

	public static Image createImage(final String filename)
	{
		return (new ImageIcon(filename)).getImage();
		// return Toolkit.getDefaultToolkit().createImage(filename);
	}

	public static Image createImage(final String filename, final Component component)
	{
		return createImage(filename, component, true);
	}

	public static Image createImage(final String filename, final Component component, final boolean withinPackage)
	{
		if (filename == null)
		{
			return null;
		}

		java.net.URL url = null;
		if (withinPackage)
		{
			url = component.getClass().getResource(filename);
		}
		else
		{
			try
			{
				url = new java.net.URL("file:///" + filename);
			}
			catch (final MalformedURLException e)
			{
				Info.message(component, e.getMessage());
			}
		}
		if (url != null)
		{
			final Image image = Toolkit.getDefaultToolkit().getImage(url);

			try
			{
				final MediaTracker tracker = new MediaTracker(component);

				tracker.addImage(image, 0);
				tracker.waitForID(0);

			}
			catch (final Exception e)
			{
				Info.message(e.getMessage());
			}

			return image;
		}
		return null;
	}

	public static Image createImage(final String filename, final Component component, final int width, final int height)
	{
		final Image image = createImage(filename, component);
		if (image != null)
		{
			final Image scaled = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
			component.checkImage(scaled, component);
			return scaled;
		}
		else
		{
			return null;
		}
	}

	public static Image createImage(final TransferableMediaObject media)
	{
		final byte[] data = media.getData();
		if (data != null)
		{
			final Image image = Toolkit.getDefaultToolkit().createImage(data);
			return image;
		}
		else
		{
			System.out.println("Empty Data");
			return null;
		}
	}

	public static Image createImage(final TransferableMediaObject media, final Component component)
	{
		final MediaTracker tracker = new MediaTracker(component);
		final Image image = createImage(media);
		tracker.addImage(image, 0);
		try
		{

			tracker.waitForID(0);

		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		return image;

	}

	public static ImageIcon createImageFromData(final byte[] data)
	{
		ImageIcon icon = null;
		if (data != null && data.length > 0)
		{
			icon = new ImageIcon(data);
		}
		return icon;
	}

	public static Image[] createImages(final String[] files)
	{
		final int nrImages = files.length;
		final Image[] images = new Image[nrImages];
		for (int i = 0; i < nrImages; i++)
		{
			images[i] = createImage(files[i]);
		}
		return images;
	}

	public static Image[] createImages(final String[] files, final Component component)
	{
		final int nrImages = files.length;
		final Image[] images = new Image[nrImages];
		for (int i = 0; i < nrImages; i++)
		{
			images[i] = createImage(files[i], component);
		}
		return images;
	}

	public static Image[] createImages(final String[] files, final Component component, final int width,
	                                   final int height)
	{
		final int nrImages = files.length;
		final Image[] images = new Image[nrImages];
		for (int i = 0; i < nrImages; i++)
		{
			images[i] = createImage(files[i], component, width, height);
		}
		return images;
	}

	public static Image[] createImages(final TransferableMultiMedia media)
	{

		final int nrImages = media.getDataCount();
		final Image[] images = new Image[nrImages];
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final byte[][] allData = media.getMultiData();
		for (int i = 0; i < nrImages; i++)
		{
			if (allData[i] != null)
			{
				images[i] = tk.createImage(allData[i]);
			}
			else
			{
				images[i] = null;
			}
		}
		return images;
	}

	public static String createText(final TransferableMediaObject media)
	{
		final String text = new String(media.getData());
		return text;

	}

	/**
	 * Decodes a JPEG coded BufferedImage into a JPEG coded byte array.
	 */
	public static BufferedImage decodeImage(final String filename)
	{
		try
		{
			final FileInputStream fis = new FileInputStream(filename);
			final BufferedImage image = ImageIO.read(fis);
			fis.close();
			return image;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Encodes an BufferedImage into a JPEG coded byte array.
	 */
	public static byte[] encodeImage(final BufferedImage image)
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(image, "jpeg", baos);
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
		return baos.toByteArray();

	}

	public static byte[] getBytesFromFile(final String filename)
	{
		byte[] bytes = null;
		try
		{
			final FileInputStream fis = new FileInputStream(filename);
			final int tot = fis.available();
			bytes = new byte[tot];
			fis.read(bytes);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return bytes;
	}

	public static ImageIcon getImageIcon(final String filename, final Component component)
	{
		final java.net.URL url = component.getClass().getResource(filename);
		if (url != null)
		{
			return new ImageIcon(url);
		}
		return null;
	}

	public static Properties loadBeanProperty(final File file)
	{
		final Properties props = new Properties();
		try
		{
			final FileInputStream fis = new FileInputStream(file);
			props.load(fis);
		}
		catch (final FileNotFoundException fnfe)
		{
			System.out.println("Error loading from properties file '" + file.getPath() + "' : " + fnfe.getMessage());
		}
		catch (final IOException ioe)
		{
			System.out.println("Error loading from properties file '" + file.getPath() + "' : " + ioe.getMessage());
		}

		return props;
	}

	/**
	 * Parses a string list representing an int array. The delim gives the delimeter characters
	 * separating the ints.
	 */
	public static int[] parseIntArray(final String intList, final String delim)
	{
		final StringTokenizer st = new StringTokenizer(intList, delim);
		final int[] ints = new int[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens())
		{
			ints[i++] = Integer.parseInt(st.nextToken());
		}
		return ints;
	}

	public static void playAudio(final TransferableMediaObject media)
	{
		final byte[] soundData = media.getData();
		if (soundData != null)
		{
			AudioInputStream audioInputStream = null;
			try
			{
				audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(soundData));
			}
			catch (final UnsupportedAudioFileException uafe)
			{
				System.out.println(uafe.getMessage());
				return;
			}
			catch (final IOException ioe)
			{
				System.out.println(ioe.getMessage());
				return;
			}

			AudioFormat streamFormat = null;
			streamFormat = audioInputStream.getFormat();
			final DataLine.Info info = new DataLine.Info(Clip.class, streamFormat);

			if (!AudioSystem.isLineSupported(info))
			{
				System.out.println("An out line matching " + info + " is not found.");
				return;
			}
			// get and open the source data line for playback.
			final Clip outline;
			try
			{
				outline = (Clip) AudioSystem.getLine(info);
				final LineListener listener = new LineListener()
				{
					@Override
					public void update(final LineEvent event)
					{
						System.out.println("Got an event on this " + event.getType());
						if (event.getType().equals(LineEvent.Type.STOP))
						{
							outline.close();
						}
					}

				};
				outline.addLineListener(listener);
				outline.open(audioInputStream);
			}
			catch (final LineUnavailableException ex)
			{
				System.out.println("Unable to open the out line: " + ex);
				return;
			}
			catch (final IOException ex)
			{
				System.out.println("Unable to open the out line: " + ex);
				return;
			}

			if (!outline.isControlSupported(FloatControl.Type.MASTER_GAIN))
			{
				System.out.println("Gain Control is not supported on output line");
			}
			else
			{
				final FloatControl gainControl = (FloatControl) outline.getControl(FloatControl.Type.MASTER_GAIN);
				System.out.println("Gain was " + gainControl.getValue() + "(" + gainControl.getMaximum() + ")");
				gainControl.setValue(gainControl.getMaximum());
			}

			// play back the captured audio data

			outline.start();
		}
	}

}
