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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */
package equip.ect.components.eventinterface;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * <B>EventRobot</B> manifests key and mouse events.
 * 
 * <H3>Description</H3> <B>EventRobot</B> moves the mouse position to the given coordinates in the
 * screen, and triggers key pressed events from the given keyPressed property value.
 * 
 * <H3>Usage</H3> Set the mouseButton to true to generate a mousePressed event.<BR>
 * Set the mouseButton to false to generate a mouseReleased event.<BR>
 * Set the mouseButtonClick to generate clicked event. A click event is handled as a mousePressed
 * followed by mouseRelease.<BR>
 * Set the keyPressed to generate a keyPressed event.
 * 
 * <H3>Technical Details</H3> Makes use of the Java.awt.Robot to mimick mouse and key events.
 * 
 * @classification Media/GUI
 * 
 * @author humble
 */
public class EventRobot implements Serializable
{

	private Robot robot = null;

	private int mouseX, mouseY;

	private String key;

	private boolean mouseButton1;

	private boolean mouseButton2;

	private boolean mouseButton3;

	private boolean mouseButton1Click;

	private boolean mouseButton3Click;

	private boolean mouseButton2Click;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public EventRobot()
	{
		try
		{
			this.robot = new Robot();
		}
		catch (final AWTException ae)
		{
			System.out.println("Warning: Robot could not be instantiated");
			ae.printStackTrace();

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

	public synchronized String getKeyPressed()
	{
		return this.key;
	}

	public synchronized boolean getMouseButton1()
	{
		return this.mouseButton1;
	}

	public synchronized boolean getMouseButton1Click()
	{
		return this.mouseButton1Click;
	}

	public synchronized boolean getMouseButton2()
	{
		return this.mouseButton2;
	}

	public synchronized boolean getMouseButton2Click()
	{
		return this.mouseButton2Click;
	}

	public synchronized boolean getMouseButton3()
	{
		return this.mouseButton3;
	}

	public synchronized boolean getMouseButton3Click()
	{
		return this.mouseButton3Click;
	}

	public synchronized int getMouseX()
	{
		return this.mouseX;
	}

	public synchronized int getMouseY()
	{
		return this.mouseY;
	}

	/**
	 * Property Change Listeners
	 */

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setKeyPressed(final String key)
	{
		final String oldKey = this.key;
		this.key = key;
		propertyChangeListeners.firePropertyChange("key", oldKey, this.key);
		if (robot != null)
		{
			robot.keyPress(KeyEvent.VK_A);
		}
	}

	public synchronized void setMouseButton1(final boolean mouseButton1)
	{
		final boolean oldMouseButton = this.mouseButton1;
		this.mouseButton1 = mouseButton1;
		setMouseButtonState(InputEvent.BUTTON1_MASK, mouseButton1);
		propertyChangeListeners.firePropertyChange("mouseButton1", oldMouseButton, this.mouseButton1);
	}

	public synchronized void setMouseButton1Click(final boolean buttonClick)
	{
		if (buttonClick)
		{
			this.mouseButton1Click = true;
			propertyChangeListeners.firePropertyChange("mouseButton1Click", false, true);
			setMouseButton1(true);
			setMouseButton1(false);
			this.mouseButton1Click = false;
			propertyChangeListeners.firePropertyChange("mouseButton1Click", true, false);
		}
	}

	public synchronized void setMouseButton2(final boolean mouseButton2)
	{
		final boolean oldMouseButton = this.mouseButton2;
		this.mouseButton2 = mouseButton2;
		setMouseButtonState(InputEvent.BUTTON2_MASK, mouseButton2);
		propertyChangeListeners.firePropertyChange("mouseButton2", oldMouseButton, this.mouseButton2);
	}

	public synchronized void setMouseButton2Click(final boolean buttonClick)
	{
		if (buttonClick)
		{
			this.mouseButton2Click = true;
			propertyChangeListeners.firePropertyChange("mouseButton2Click", false, true);
			setMouseButton2(true);
			setMouseButton2(false);
			this.mouseButton2Click = false;
			propertyChangeListeners.firePropertyChange("mouseButton2Click", true, false);
		}
	}

	public synchronized void setMouseButton3(final boolean mouseButton3)
	{
		final boolean oldMouseButton = this.mouseButton3;
		this.mouseButton3 = mouseButton3;
		setMouseButtonState(InputEvent.BUTTON3_MASK, mouseButton3);
		propertyChangeListeners.firePropertyChange("mouseButton3", oldMouseButton, this.mouseButton3);
	}

	public synchronized void setMouseButton3Click(final boolean buttonClick)
	{
		if (buttonClick)
		{
			this.mouseButton3Click = true;
			propertyChangeListeners.firePropertyChange("mouseButton3Click", false, true);
			setMouseButton3(true);
			setMouseButton3(false);
			this.mouseButton3Click = false;
			propertyChangeListeners.firePropertyChange("mouseButton3Click", true, false);
		}
	}

	public synchronized void setMouseX(final int mouseX)
	{
		final int oldMouseX = this.mouseX;
		this.mouseX = mouseX;
		propertyChangeListeners.firePropertyChange("mouseX", oldMouseX, this.mouseX);
		if (robot != null)
		{
			robot.mouseMove(mouseX, mouseY);
		}
	}

	public synchronized void setMouseY(final int mouseY)
	{
		final int oldMouseY = this.mouseY;
		this.mouseY = mouseY;
		propertyChangeListeners.firePropertyChange("mouseY", oldMouseY, this.mouseY);
		if (robot != null)
		{
			robot.mouseMove(mouseX, mouseY);
		}
	}

	public synchronized void stop()
	{

	}

	private void setMouseButtonState(final int buttonMask, final boolean buttonState)
	{
		if (robot != null)
		{
			if (buttonState)
			{
				robot.mousePress(buttonMask);
			}
			else
			{
				robot.mouseRelease(buttonMask);
			}
		}
	}
}
