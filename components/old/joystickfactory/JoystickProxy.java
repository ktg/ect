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

/** proxy for a single connected joystick */
public class JoystickProxy implements Serializable
{
	/**
	 * resolution
	 */
	protected float resolution = 0.01f;
	/**
	 * id
	 */
	protected int id;
	/**
	 * status
	 */
	protected String status = "new";

	/**
	 * input value
	 */
	protected float x = 0.5f;
	/**
	 * input value
	 */
	protected float y = 0.5f;

	/**
	 * input value
	 */
	protected float z;
	/**
	 * buttons value
	 */
	protected int buttons;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * constructor
	 */
	public JoystickProxy(final int id)
	{
		this.id = id;
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
	public synchronized boolean getButton0()
	{
		return ((buttons >> 0) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton1()
	{
		return ((buttons >> 1) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton2()
	{
		return ((buttons >> 2) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton3()
	{
		return ((buttons >> 3) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton4()
	{
		return ((buttons >> 4) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton5()
	{
		return ((buttons >> 5) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton6()
	{
		return ((buttons >> 6) & 0x01) != 0;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getButton7()
	{
		return ((buttons >> 7) & 0x01) != 0;
	}

	/**
	 * persistent child
	 */
	public String getPersistentChild()
	{
		return "joystick" + id;
	}

	/**
	 * get status
	 */
	public synchronized String getStatus()
	{
		return status;
	}

	/**
	 * input getter
	 */
	public synchronized float getX()
	{
		return x;
	}

	/**
	 * input getter
	 */
	public synchronized float getY()
	{
		return y;
	}

	/**
	 * input getter
	 */
	public synchronized float getZ()
	{
		return z;
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
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
	}

	/**
	 * set status
	 */
	synchronized void setStatus(final String s)
	{
		if (status.equals(s)) { return; }
		final String old = status;
		status = s;
		propertyChangeListeners.firePropertyChange("status", old, s);
	}

	/**
	 * update - package scope
	 */
	synchronized void update(final float x, final float y, final float z, final int buttons)
	{
		setX(x);
		setY(y);
		setZ(z);
		setButtons(buttons);
	}

	/**
	 * fix resolution
	 */
	protected float fixResolution(float in)
	{
		in = resolution * ((int) (in / resolution));
		if (in < 0)
		{
			in = 0;
		}
		if (in > 1)
		{
			in = 1;
		}
		return in;
	}

	/**
	 * input setter
	 */
	protected synchronized void setButtons(final int in)
	{
		final int old = buttons;
		buttons = in;
		for (int i = 0; i < 8; i++)
		{
			final boolean old0 = ((old >> i) & 0x01) != 0;
			final boolean new0 = ((in >> i) & 0x01) != 0;
			if (old0 != new0)
			{
				propertyChangeListeners.firePropertyChange("button" + i, old0, new0);
			}
		}
	}

	/**
	 * input setter
	 */
	protected synchronized void setX(float in)
	{
		in = fixResolution(in);
		// could suppress no-change setting
		if (in == x) { return; }
		final float old = x;
		x = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("x", new Float(old), new Float(in));
	}

	/**
	 * input setter
	 */
	protected synchronized void setY(float in)
	{
		in = fixResolution(in);
		// could suppress no-change setting
		if (in == y) { return; }
		final float old = y;
		y = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("y", new Float(old), new Float(in));
	}

	/**
	 * input setter
	 */
	protected synchronized void setZ(float in)
	{
		in = fixResolution(in);
		// could suppress no-change setting
		if (in == z) { return; }
		final float old = z;
		z = in;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("z", new Float(old), new Float(in));
	}
}
