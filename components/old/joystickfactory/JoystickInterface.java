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
package equip.ect.components.joystickfactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Automatic factory for JoystickProxy components, using Windows Joystick API.
 * 
 * <H3>Description</H3>
 * 
 * <H3>Configuration</H3>
 * 
 * <H3>Usage</H3>
 * 
 * <H3>Technical Details</H3>
 * 
 * @technology Joystick interfacing
 * @classification Hardware/Input
 */
public class JoystickInterface implements Serializable
{
	/**
	 * map int to float x/y/z
	 */
	protected static float int2float(final int in)
	{
		return in / 65535.0f;
	}

	/**
	 * joystick internal components
	 */
	protected JoystickProxy joysticks[] = new JoystickProxy[0];

	/**
	 * status
	 */
	protected String status = "new";
	/**
	 * stopped
	 */
	protected boolean stopped = false;

	/**
	 * poll interval
	 */
	protected int pollIntervalMs = 50;
	/**
	 * poll info
	 */
	protected boolean oks[];
	/**
	 * poll info
	 */
	protected int xs[];
	/**
	 * poll info
	 */
	protected int ys[];
	/**
	 * poll info
	 */
	protected int zs[];
	/**
	 * poll info
	 */
	protected int buttons[];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public JoystickInterface()
	{
		try
		{
			final int num = JoystickNative.getNum();
			joysticks = new JoystickProxy[num];
			for (int i = 0; i < num; i++)
			{
				joysticks[i] = new JoystickProxy(i);
			}
			oks = new boolean[num];
			xs = new int[num];
			ys = new int[num];
			zs = new int[num];
			buttons = new int[num];
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					threadFn();
				}
			}).start();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR initialising JoystickInterface: " + e);
			e.printStackTrace(System.err);
			setStatus("Exception initialising: " + e);
			joysticks = new JoystickProxy[0];
		}
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
	 * joysticks getter
	 */
	public synchronized JoystickProxy[] getJoysticks()
	{
		int n = 0;
		for (final boolean ok : oks)
		{
			if (ok)
			{
				n++;
			}
		}
		final JoystickProxy rval[] = new JoystickProxy[n];
		for (int i = 0, j = 0; i < oks.length; i++)
		{
			if (oks[i])
			{
				rval[j] = joysticks[i];
				j++;
			}
		}
		return rval;
	}

	/**
	 * input getter
	 */
	public synchronized int getPollIntervalMs()
	{
		return pollIntervalMs;
	}

	/**
	 * get status
	 */
	public synchronized String getStatus()
	{
		return status;
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
	public synchronized void setPollIntervalMs(final int input)
	{
		// could suppress no-change setting
		if (input == pollIntervalMs || input <= 0) { return; }
		final int old = pollIntervalMs;
		this.pollIntervalMs = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("pollIntervalMs", old, input);
		// behaviour... (doesn't have to be done like this)
		notifyAll();
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		stopped = true;
		notifyAll();
	}

	/**
	 * poll
	 */
	protected synchronized void poll()
	{
		final JoystickProxy oldJoysticks[] = getJoysticks();
		final boolean oldoks[] = oks;
		oks = new boolean[oldoks.length];

		int i;
		for (i = 0; i < oks.length; i++)
		{
			oks[i] = true;
		}

		boolean changed = false;
		JoystickNative.poll(oks, xs, ys, zs, buttons);
		for (i = 0; i < joysticks.length; i++)
		{
			if (oks[i])
			{
				joysticks[i].setStatus("OK");
				// System.out.println("joystick "+i+": "+xs[i]+", "+ys[i]+", "+zs[i]+", "+buttons[i]);
				joysticks[i].update(int2float(xs[i]), int2float(ys[i]), int2float(zs[i]), buttons[i]);
			}
			else
			{
				joysticks[i].setStatus("not attached");
				joysticks[i].update(0.5f, 0.5f, 0.0f, 0);
			}
			if (oks[i] != oldoks[i])
			{
				changed = true;
			}
		}
		if (changed)
		{
			final JoystickProxy newJoysticks[] = getJoysticks();
			propertyChangeListeners.firePropertyChange("joysticks", oldJoysticks, newJoysticks);
		}
	}

	/**
	 * set status
	 */
	protected synchronized void setStatus(final String s)
	{
		final String old = status;
		status = s;
		propertyChangeListeners.firePropertyChange("status", old, s);
	}

	/**
	 * thread fn
	 */
	protected synchronized void threadFn()
	{
		try
		{
			while (!stopped)
			{
				wait(pollIntervalMs);
				poll();
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR in JoystickInterface threadFn: " + e);
			e.printStackTrace(System.err);
		}
	}
}
