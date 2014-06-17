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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.holger;

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class WindowComponent implements Serializable, ComponentListener

{
	class InvalidDisplayNumberException extends Exception
	{
		InvalidDisplayNumberException()
		{
			super();
		}
	}

	class MoveableJFrame extends JFrame
	{
		GraphicsConfiguration gc;
		JTextField textField;

		public MoveableJFrame(final GraphicsConfiguration gc, final Point location)
		{
			super(gc);
			this.gc = gc;

			final Canvas c = new Canvas(gc);
			final Rectangle gcBounds = gc.getBounds();
			final int xoffs = gcBounds.x;
			final int yoffs = gcBounds.y;
			getContentPane().add(c);
			setLocation(xoffs + location.x, yoffs + location.y);

			textField = new JTextField(20);
			getContentPane().add(textField);

			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		}

		public synchronized Point getRelativeLocation()
		{
			final Rectangle gcBounds = gc.getBounds();
			final int xoffs = gcBounds.x;
			final int yoffs = gcBounds.y;

			final Point p = getLocation();
			p.x = p.x - xoffs;
			p.y = p.y - yoffs;

			return p;
		}

		public synchronized String getText()
		{
			return (textField.getText());
		}

		public synchronized void setRelativeLocation(final int x, final int y)
		{
			final Rectangle gcBounds = gc.getBounds();
			final int xoffs = gcBounds.x;
			final int yoffs = gcBounds.y;

			super.setLocation(x + xoffs, y + yoffs);
		}

		public synchronized void setRelativeLocation(final Point p)
		{
			final Rectangle gcBounds = gc.getBounds();
			final int xoffs = gcBounds.x;
			final int yoffs = gcBounds.y;

			super.setLocation(p.x + xoffs, p.y + yoffs);
		}

		public synchronized void setText(final String text)
		{
			textField.setText(text);
		}
	}

	MoveableJFrame frame = null;

	GraphicsDevice[] gs;
	int xPosition;

	int yPosition;

	String attention;

	int displayNumber = 0;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public WindowComponent()
	{
		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gs = ge.getScreenDevices();

		try
		{
			displayFrame(0, new Point(0, 0));
			displayNumber = 0;
		}

		catch (final InvalidDisplayNumberException e)
		{
			// shouldn't happen!
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	@Override
	public void componentHidden(final ComponentEvent e)
	{
		System.out.println("component hidden");
	}

	@Override
	public void componentMoved(final ComponentEvent e)
	{
		final int oldX = getXPosition();
		final int oldY = getYPosition();

		final Point newPosition = frame.getRelativeLocation();

		xPosition = newPosition.x;
		yPosition = newPosition.y;

		propertyChangeListeners.firePropertyChange("xPosition", oldX, xPosition);

		propertyChangeListeners.firePropertyChange("yPosition", oldY, yPosition);
	}

	@Override
	public void componentResized(final ComponentEvent e)
	{
	}

	@Override
	public void componentShown(final ComponentEvent e)
	{
	}

	public String getAttention()
	{
		return attention;
	}

	public int getDisplayNumber()
	{
		return this.displayNumber;
	}

	public String getText()
	{
		return (frame.getText());
	}

	public String getTitle()
	{
		return (frame.getTitle());
	}

	public Object getTriggerMoveLeft()
	{
		return null;
	}

	public Object getTriggerMoveRight()
	{
		return null;
	}

	public int getXPosition()
	{
		return this.xPosition;
	}

	public int getYPosition()
	{
		return this.yPosition;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}

	public void setDisplayNumber(final int newValue)
	{
		final int oldValue = this.displayNumber;

		try
		{
			moveFrameLocation(newValue);
			displayNumber = newValue;
			propertyChangeListeners.firePropertyChange("displayNumber", oldValue, newValue);
		}
		catch (final InvalidDisplayNumberException e)
		{
			setAttention("Device with this number does not exist!");
			try
			{
				moveFrameLocation(0);
				displayNumber = 0;
				propertyChangeListeners.firePropertyChange("displayNumber", oldValue, 0);
			}
			catch (final InvalidDisplayNumberException f)
			{
				// as long as you have one display device, should never happen!
				e.printStackTrace();
			}
		}
	}

	public void setText(final String newValue)
	{
		final String oldValue = frame.getText();
		frame.setText(newValue);

		propertyChangeListeners.firePropertyChange("text", oldValue, newValue);

	}

	public void setTitle(final String newValue)
	{
		final String oldValue = frame.getTitle();
		frame.setTitle(newValue);

		propertyChangeListeners.firePropertyChange("title", oldValue, newValue);
	}

	public void setTriggerMoveLeft(final Object value)
	{
		if (value != null)
		{
			// find the current display device, and see if any devices
			// are to the right of it.

			final GraphicsDevice gd = gs[displayNumber];
			final int xPos = gd.getDefaultConfiguration().getBounds().x;

			for (int i = 0; i < gs.length; i++)
			{
				if (i != displayNumber)
				{
					final int newXPos = gs[i].getDefaultConfiguration().getBounds().x;

					if (newXPos < xPos)
					{
						setDisplayNumber(i);
						break;
					}
				}
			}
		}
	}

	public void setTriggerMoveRight(final Object value)
	{
		if (value != null)
		{
			// find the current display device, and see if any devices
			// are to the right of it.

			final GraphicsDevice gd = gs[displayNumber];
			final int xPos = gd.getDefaultConfiguration().getBounds().x;

			for (int i = 0; i < gs.length; i++)
			{
				if (i != displayNumber)
				{
					final int newXPos = gs[i].getDefaultConfiguration().getBounds().x;

					if (newXPos > xPos)
					{
						setDisplayNumber(i);
						break;
					}
				}
			}
		}
	}

	public void setXPosition(final int newValue)
	{
		final int oldValue = this.xPosition;
		this.xPosition = newValue;

		final Point currentPos = frame.getRelativeLocation();
		frame.setRelativeLocation(newValue, currentPos.y);

		propertyChangeListeners.firePropertyChange("xPosition", oldValue, newValue);
	}

	public void setYPosition(final int newValue)
	{
		final int oldValue = this.yPosition;
		this.yPosition = newValue;

		final Point currentPos = frame.getRelativeLocation();
		frame.setRelativeLocation(currentPos.x, newValue);

		propertyChangeListeners.firePropertyChange("yPosition", oldValue, newValue);
	}

	public synchronized void stop()
	{
		frame.dispose();
	}

	MoveableJFrame displayFrame(final int displayIndex, final Point location) throws InvalidDisplayNumberException
	{
		if ((displayIndex < 0) || (displayIndex >= gs.length)) { throw (new InvalidDisplayNumberException()); }
		final GraphicsConfiguration gc = gs[displayIndex].getDefaultConfiguration();

		frame = new MoveableJFrame(gc, location);

		frame.pack();
		frame.setVisible(true);

		frame.addComponentListener(this);

		return frame;
	}

	void moveFrameLocation(final int newValue) throws InvalidDisplayNumberException
	{
		final Point relativeLocation = frame.getRelativeLocation();
		final String tempText = frame.getText();
		final String tempTitle = frame.getTitle();

		frame.dispose();

		displayFrame(newValue, relativeLocation);
		frame.setText(tempText);
		frame.setTitle(tempTitle);
	}
}
