/*
 <COPYRIGHT>

 Copyright (c) 2002-2005, University of Nottingham
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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.fileio;

import equip.ect.Category;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kevin Glover
 */
@ECTComponent
@Category("File")
public class BiotraceFileReader implements Serializable, DynamicProperties
{
	/**
	 * dynamic properties support
	 */
	private transient final PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	private final DynamicPropertiesSupport dynamicProperties = new DynamicPropertiesSupport(propertyChangeListeners);
	private int refresh = 200;
	private String filename = "";
	private String status = "";
	private String channels = "";
	private boolean running = false;
	private Set<Integer> channelIDs = new HashSet<>();
	private final Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
			while (running)
			{
				try
				{
					if (filename == null || filename.equals(""))
					{
						setStatus("No file");
						synchronized (runnable)
						{
							runnable.wait();
						}
					}
					else
					{
						try
						{
							final FileInputStream is = new FileInputStream(filename);
							try
							{
								final byte[] bytes = new byte[8];
								int channel = 1;
								while (true)
								{
									if (is.read(bytes) == -1)
									{
										setStatus("OK");
										break;
									}

									if (channelIDs.isEmpty() || channelIDs.contains(channel))
									{

										final String channelName = String.format("channel %03d", channel);
										final double channelValue = readDouble(bytes);
										dynamicProperties.addProperty(channelName, Double.class, channelValue);
									}
									channel++;
								}
							}
							catch (final EOFException eof)
							{
								setStatus("OK");
							}
							catch (final IOException e)
							{
								setStatus("ERROR: " + e.getMessage());
								e.printStackTrace();
							}
						}
						catch (final FileNotFoundException e)
						{
							setStatus("ERROR: File Missing");
						}

						synchronized (runnable)
						{
							runnable.wait(refresh);
						}
					}
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			setRunning(false);
		}
	};

	public BiotraceFileReader()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * get all properties' {@link equip.ect.DynamicPropertyDescriptor}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynamicProperties.dynGetProperties();
	}

	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynamicProperties.dynGetProperty(name);
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynamicProperties.dynSetProperty(name, value);
	}

	public String getChannels()
	{
		return channels;
	}

	public void setChannels(final String channels)
	{
		final String oldChannels = this.channels;
		this.channels = channels;

		final String[] channelIDStrings = channels.trim().split(",");
		channelIDs.clear();
		for (final String channelIDString : channelIDStrings)
		{
			Integer channelID = new Integer(channelIDString);
			channelIDs.add(channelID);
			String channelName = String.format("channel %03d", channelID);
			try
			{
				dynamicProperties.dynGetProperty(channelName);
			}
			catch (NoSuchPropertyException e)
			{
				dynamicProperties.addProperty(channelName, Double.class, 0);
			}
		}

		final DynamicPropertyDescriptor[] descriptors = dynamicProperties.dynGetProperties();
		for (final DynamicPropertyDescriptor descriptor : descriptors)
		{
			final String channelName = descriptor.getName();
			final String channelIDString = channelName.substring(channelName.length() - 3);
			final Integer channelID = new Integer(channelIDString);
			if (!channelIDs.contains(channelID))
			{
				try
				{
					dynamicProperties.removeProperty(channelName);
				}
				catch (final NoSuchPropertyException e)
				{
					e.printStackTrace();
				}
			}
		}

		propertyChangeListeners.firePropertyChange("channels", oldChannels, channels);
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(final String filename)
	{
		final String oldFilename = this.filename;
		this.filename = filename;

		System.err.println("Update filename " + filename);

		propertyChangeListeners.firePropertyChange("filename", oldFilename, filename);

		synchronized (runnable)
		{
			runnable.notifyAll();
		}
	}

	public int getRefresh()
	{
		return refresh;
	}

	public void setRefresh(final int refresh)
	{
		final int oldRefresh = this.refresh;
		this.refresh = refresh;

		propertyChangeListeners.firePropertyChange("refresh", oldRefresh, refresh);
	}

	public boolean getRunning()
	{
		return running;
	}

	public void setRunning(final boolean running)
	{
		if (this.running != running)
		{
			final boolean oldRunning = this.running;

			this.running = running;
			if (running)
			{
				final Thread thread = new Thread(runnable);
				thread.start();
			}
			else
			{
				synchronized (runnable)
				{
					runnable.notifyAll();
				}
			}

			propertyChangeListeners.firePropertyChange("running", oldRunning, running);
		}
	}

	public String getStatus()
	{
		return status;
	}

	private void setStatus(final String status)
	{
		final String oldStatus = this.status;
		this.status = status;

		propertyChangeListeners.firePropertyChange("status", oldStatus, status);
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void stop()
	{
		running = false;
	}

	/**
	 * Read double from bytes in little-endian format
	 */
	private double readDouble(final byte[] bytes)
	{
		// get the 8 unsigned raw bytes, accumulate to a long and then
		// convert the 64-bit pattern to a double.
		long accum = 0;
		int i = 0;
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8)
		{
			// must cast to long or the shift would be done modulo 32
			accum |= ((long) (bytes[i] & 0xff)) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}


}
