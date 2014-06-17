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

 Created by: Chris Greenhalgh (University of Nottingham)
 Contributors:
 Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.runprogram;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A bean to run a native application.
 * 
 * <H3>Description</H3> A simple bean to run native commands on the host computer.
 * 
 * <H3>Usage</H3> Enter the command required and set property 'run' to process the command.
 * 
 * <H3>Technical Details</H3> Uses the standard Runtime.exec() call.
 * 
 * @classification Local Services
 * 
 * @author Chris Greenhalgh (University of Nottingham)
 */
public class RunProgram implements Serializable
{
	/**
	 * output class
	 */
	protected class ConsoleReaderThread extends Thread
	{
		java.io.InputStream is;

		Process p;

		ConsoleReaderThread(final java.io.InputStream is, final Process p)
		{
			this.is = is;
			this.p = p;
		}

		@Override
		public void run()
		{
			try
			{
				final java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
				while (true)
				{
					final String in = br.readLine();
					if (in == null)
					{
						// eof
						break;
					}
					synchronized (RunProgram.this)
					{
						if (process == p)
						{
							System.out.println("Output " + process + ": " + in);
							setConsole(in);
						}
						else
						{
							System.err.println("Warning: output from old process " + process + ": " + in);
						}
					}
				}
				System.err.println("Clean end of ConsoleReaderThread");
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in ConsoleReaderThread: " + e);
				e.printStackTrace(System.err);
				setConsole("READER ERROR: " + e);
			}
		}
	}

	/**
	 * input value
	 */
	protected String command;

	/**
	 * input value
	 */
	protected boolean run;

	/**
	 * output value
	 */
	protected String console;

	/**
	 * output value
	 */
	protected boolean running;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * process
	 */
	protected Process process;

	static final int CLOSE_DELAY_MS = 1000;

	static final int STOP_DELAY_MS = 5000;

	/**
	 * no-args constructor (required)
	 */
	public RunProgram()
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

	/**
	 * input getter
	 */
	public synchronized String getCommand()
	{
		return command;
	}

	/**
	 * output getter
	 */
	public synchronized String getConsole()
	{
		return console;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getRun()
	{
		return run;
	}

	/**
	 * output getter
	 */
	public synchronized boolean getRunning()
	{
		return running;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * input setter
	 */
	public synchronized void setCommand(final String c)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null &&
		// input.equals(this.input)) return;
		final boolean wasRun = getRun();
		setRun(false);
		final String oldCommand = this.command;
		this.command = c;
		setRun(wasRun);
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("command", oldCommand, this.command);
	}

	/**
	 * input setter
	 */
	public synchronized void setRun(final boolean r)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null &&
		// input.equals(this.input)) return;
		if (this.running)
		{
			stopProcess();
		}
		final boolean oldRun = this.run;
		this.run = r;
		if (this.run)
		{
			startProcess();
		}
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("run", oldRun, this.run);
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		// make sure it is not running
		setRun(false);
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setConsole(final String output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldOutput = this.console;
		this.console = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("console", oldOutput, this.console);
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setRunning(final boolean output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final boolean oldOutput = this.running;
		this.running = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("running", oldOutput, this.running);
	}

	/**
	 * actually start/run etc
	 */
	protected synchronized void startProcess()
	{
		if (command == null || command.equals("")) { return; }
		try
		{
			process = Runtime.getRuntime().exec(command);
			System.out.println("Started " + process + ": " + command);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR running " + command + ": " + e);
			e.printStackTrace(System.err);
			setConsole("EXEC ERROR: " + e);
			process = null;
			return;
		}
		setRunning(true);
		final Process p = process;
		try
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						final int status = p.waitFor();
						System.out.println("Ended " + process + ": " + status);
						synchronized (RunProgram.this)
						{
							if (process == p)
							{
								setRunning(false);
								setConsole("EXIT status " + status);
								RunProgram.this.notifyAll();
							}
							else
							{
								System.err.println("Warning: RunProgram waitFor thread exiting with running == false");
							}
						}
					}
					catch (final InterruptedException e)
					{
						System.err.println("RunProcess waitFor thread interrupted");
					}
				}
			}).start();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR tracking program " + command + ": " + e);
			e.printStackTrace(System.err);
			setConsole("TRACK ERROR: " + e);
			stopProcess();
			return;
		}
		try
		{
			new ConsoleReaderThread(p.getInputStream(), p).start();
			new ConsoleReaderThread(p.getErrorStream(), p).start();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR getting output from program " + command + ": " + e);
			e.printStackTrace(System.err);
			setConsole("OUTPUT ERROR: " + e);
		}
	}

	/**
	 * stop
	 */
	protected synchronized void stopProcess()
	{
		if (!running) { return; }
		if (process != null)
		{
			// try to stop by closing input
			try
			{
				process.getOutputStream().close();
				wait(CLOSE_DELAY_MS);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR closing process input: " + e);
				e.printStackTrace(System.err);
				try
				{
					wait(1);
				}
				catch (final InterruptedException ee)
				{
				}
			}
			if (!running) { return; }
			// try to stop
			try
			{
				process.destroy();
				wait(STOP_DELAY_MS);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR closing process input: " + e);
				e.printStackTrace(System.err);
				try
				{
					wait(1);
				}
				catch (final InterruptedException ee)
				{
				}
			}
			if (!running) { return; }
			try
			{
				final int status = process.exitValue();
				// stopped anyway!
				setRunning(false);
				process = null;
				System.err.println("Warning: RunProgram had apparently stopped anyway");
				return;
			}
			catch (final IllegalThreadStateException e)
			{
				System.err.println("ERROR: unable to destroy process");
				setConsole("ERROR: unable to destroy process");
				setRunning(false);
				process = null;
			}
		}
	}
}
