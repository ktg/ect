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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

/**
 * Registers mouse and key events.
 * 
 * <H3>Description</H3> A straightforward component to register mouse and key events from a dummy
 * frame. This is basically intended to monitor GUI interactions.
 * 
 * <H3>Usage</H3> Run the component and a dummy frame should be created to monitor all mouse and key
 * events.
 * 
 * <H3>Technical Details</H3> Currently uses a dummy window to listen in to the events.
 * 
 * @classification Media/GUI
 * 
 * @author humble
 * 
 */
public class EventListener implements MouseInputListener, KeyListener, Serializable
{

	private JFrame dumbFrame;

	private int mouseX, mouseY;

	private String key;

	private boolean mouseButton1, mouseButton2, mouseButton3;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public EventListener()
	{
		dumbFrame = new JFrame("EVENT LISTENER");
		dumbFrame.setSize(800, 800);
		dumbFrame.setVisible(true);
		dumbFrame.addKeyListener(this);
		dumbFrame.addMouseListener(this);
		dumbFrame.addMouseMotionListener(this);
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

	public synchronized boolean getMouseButton2()
	{
		return this.mouseButton2;
	}

	public synchronized boolean getMouseButton3()
	{
		return this.mouseButton2;
	}

	public synchronized int getMouseX()
	{
		return this.mouseX;
	}

	public synchronized int getMouseY()
	{
		return this.mouseY;
	}

	@Override
	public void keyPressed(final KeyEvent e)
	{
		setKeyPressed(KeyEvent.getKeyText(e.getKeyCode()));
	}

	@Override
	public void keyReleased(final KeyEvent e)
	{
	}

	@Override
	public void keyTyped(final KeyEvent e)
	{
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(final MouseEvent e)
	{
		setMouseX(e.getX());
		setMouseY(e.getY());
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
	}

	@Override
	public void mouseMoved(final MouseEvent e)
	{
		setMouseX(e.getX());
		setMouseY(e.getY());
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		switch (e.getButton())
		{
			case MouseEvent.BUTTON1:
				setMouseButton1(true);
				break;
			case MouseEvent.BUTTON2:
				setMouseButton2(true);
				break;
			case MouseEvent.BUTTON3:
				setMouseButton3(true);
				break;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		switch (e.getButton())
		{
			case MouseEvent.BUTTON1:
				setMouseButton1(false);
				break;
			case MouseEvent.BUTTON2:
				setMouseButton2(false);
				break;
			case MouseEvent.BUTTON3:
				setMouseButton3(false);
				break;
		}
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
	}

	public synchronized void setMouseButton1(final boolean mouseButton1)
	{
		final boolean oldMouseButton = this.mouseButton1;
		this.mouseButton1 = mouseButton1;
		propertyChangeListeners.firePropertyChange("mouseButton1", oldMouseButton, this.mouseButton1);
	}

	public synchronized void setMouseButton2(final boolean mouseButton2)
	{
		final boolean oldMouseButton = this.mouseButton2;
		this.mouseButton2 = mouseButton2;
		propertyChangeListeners.firePropertyChange("mouseButton2", oldMouseButton, this.mouseButton2);
	}

	public synchronized void setMouseButton3(final boolean mouseButton3)
	{
		final boolean oldMouseButton = this.mouseButton3;
		this.mouseButton3 = mouseButton3;
		propertyChangeListeners.firePropertyChange("mouseButton3", oldMouseButton, this.mouseButton3);
	}

	public synchronized void setMouseX(final int mouseX)
	{
		final int oldMouseX = this.mouseX;
		this.mouseX = mouseX;
		propertyChangeListeners.firePropertyChange("mouseX", oldMouseX, this.mouseX);
	}

	public synchronized void setMouseY(final int mouseY)
	{
		final int oldMouseY = this.mouseY;
		this.mouseY = mouseY;
		propertyChangeListeners.firePropertyChange("mouseY", oldMouseY, this.mouseY);
	}

	public synchronized void stop()
	{

	}

}
