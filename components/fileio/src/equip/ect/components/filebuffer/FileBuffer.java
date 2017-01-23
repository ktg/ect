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

 Created by: Stefan Rennick Egglestone(University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.filebuffer;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A buffer
 * 
 * @classification Data/Numeric
 * @defaultOutputProperty output
 * @defaultInputProperty input
 * @author ktg
 */
@ECTComponent
@Category("File")
public class FileBuffer implements Serializable
{
	private double output = 0.0;
	private double input = 0.0;

	private boolean running = false;

	private double outputFreq = 30;
	private int sleepTime = 33;
	private boolean reset = false;

	private ObjectOutputStream os;

	private File file;

	/**
	 * Property Change support
	 */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public FileBuffer()
	{
		createFile();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public boolean getClearBuffer()
	{
		return reset;
	}

	public synchronized double getInput()
	{
		return input;
	}

	public synchronized double getOutput()
	{
		return output;
	}

	public double getOutputFreq()
	{
		return outputFreq;
	}

	public boolean getRunning()
	{
		return running;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setClearBuffer(final boolean reset)
	{
		final boolean oldReset = this.reset;
		this.reset = reset;

		propertyChangeListeners.firePropertyChange("clearBuffer", oldReset, reset);
		if (reset)
		{
			createFile();
		}
	}

	public synchronized void setInput(final double input)
	{
		final double oldValue = this.input;
		this.input = input;

		try
		{
			os.writeDouble(input);
			os.flush();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		propertyChangeListeners.firePropertyChange("input", oldValue, input);
	}

	public void setOutputFreq(final double outputFreq)
	{
		final double oldFreq = this.outputFreq;
		this.outputFreq = outputFreq;

		if (oldFreq != outputFreq)
		{
			sleepTime = (int) (1000 / outputFreq);

			propertyChangeListeners.firePropertyChange("outputFreq", oldFreq, outputFreq);
		}
	}

	public void setRunning(final boolean newRunning)
	{
		final boolean oldRunning = this.running;
		this.running = newRunning;

		if (oldRunning != running)
		{
			if (running)
			{
				new Thread(() ->
				{
					try
					{
						final ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
						createFile();
						while (running)
						{
							final double oldOutput = output;

							output = is.readDouble();
							System.out.println(output);

							propertyChangeListeners.firePropertyChange("output", oldOutput, output);

							try
							{
								Thread.sleep(sleepTime);
							}
							catch (final Exception e)
							{

							}
						}
					}
					catch (final EOFException e)
					{
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}

					setRunning(false);

				}).start();
			}

			propertyChangeListeners.firePropertyChange("running", oldRunning, running);
		}
	}

	public void stop()
	{
		running = false;
	}

	private void createFile()
	{
		try
		{
			file = File.createTempFile("buffer", "");
			System.out.println(file.getCanonicalPath());
			os = new ObjectOutputStream(new FileOutputStream(file));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
}