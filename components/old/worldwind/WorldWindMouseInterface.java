package equip.ect.components.worldwind;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class WorldWindMouseInterface implements Serializable
{
	JFrame frame;
	Robot r;

	Dimension size = new Dimension(1024, 768);

	int rotateLeft = 0;

	int rotateRight = 0;

	int rotateUp = 0;
	int rotateDown = 0;
	int zoomIn = 0;
	int zoomOut = 0;
	boolean controllable = false;
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public WorldWindMouseInterface()
	{
		try
		{
			r = new Robot();

		}
		catch (final AWTException e)
		{
			setControllable(false);
		}

		setControllable(true);

		/*
		 * 
		 * frame = new AWT1UpFrame();
		 * 
		 * try { Thread.sleep(5000); }
		 * 
		 * catch(InterruptedException e) { }
		 */

		frame = new WorldWindFrame(size);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// frame.setVisible(true);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public boolean getControllable()
	{
		return controllable;
	}

	public int getRotateDown()
	{
		return rotateDown;
	}

	public int getRotateLeft()
	{
		return rotateLeft;
	}

	public int getRotateRight()
	{
		return rotateRight;
	}

	public int getRotateUp()
	{
		return rotateUp;
	}

	public int getZoomIn()
	{
		return zoomIn;
	}

	public int getZoomOut()
	{
		return zoomIn;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setRotateDown(final int newValue)
	{
		if (!controllable) { return; }

		try
		{

			final int oldValue = this.rotateDown;
			this.rotateDown = newValue;

			propertyChangeListeners.firePropertyChange("rotateDown", oldValue, newValue);

			final Point p = centreMouse();
			r.mousePress(InputEvent.BUTTON1_MASK);
			final Point newPoint = new Point(p.x, p.y + newValue);
			r.mouseMove(newPoint.x, newPoint.y);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		catch (final AWTException e)
		{
			setControllable(false);
		}
	}

	public void setRotateLeft(final int newValue)
	{
		if (!controllable) { return; }

		try
		{

			final int oldValue = this.rotateLeft;
			this.rotateLeft = newValue;

			propertyChangeListeners.firePropertyChange("rotateLeft", oldValue, newValue);

			final Point p = centreMouse();
			r.mousePress(InputEvent.BUTTON1_MASK);
			final Point newPoint = new Point(p.x - newValue, p.y);
			r.mouseMove(newPoint.x, newPoint.y);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		catch (final AWTException e)
		{
			setControllable(false);
		}
	}

	public void setRotateRight(final int newValue)
	{
		if (!controllable) { return; }

		try
		{
			final int oldValue = this.rotateRight;
			this.rotateRight = newValue;

			propertyChangeListeners.firePropertyChange("rotateRight", oldValue, newValue);

			final Point p = centreMouse();
			r.mousePress(InputEvent.BUTTON1_MASK);
			final Point newPoint = new Point(p.x + newValue, p.y);
			r.mouseMove(newPoint.x, newPoint.y);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		catch (final AWTException e)
		{
			setControllable(false);
		}
	}

	public void setRotateUp(final int newValue)
	{
		if (!controllable) { return; }

		try
		{

			final int oldValue = this.rotateUp;
			this.rotateUp = newValue;

			propertyChangeListeners.firePropertyChange("rotateUp", oldValue, newValue);

			final Point p = centreMouse();
			r.mousePress(InputEvent.BUTTON1_MASK);
			final Point newPoint = new Point(p.x, p.y - newValue);
			r.mouseMove(newPoint.x, newPoint.y);
			r.mouseRelease(InputEvent.BUTTON1_MASK);
		}
		catch (final AWTException e)
		{
			setControllable(false);

		}
	}

	public void setZoomIn(final int newValue)
	{
		if (!controllable) { return; }

		try
		{
			final int oldValue = this.zoomIn;
			this.zoomIn = newValue;

			propertyChangeListeners.firePropertyChange("zoomIn", oldValue, newValue);

			centreMouse();
			Thread.sleep(100);

			for (int i = 0; i < newValue; i++)
			{
				r.mouseWheel(-1);
			}
		}
		catch (final AWTException e)
		{
			setControllable(false);
		}
		catch (final InterruptedException e)
		{
		}
	}

	public void setZoomOut(final int newValue)
	{
		if (!controllable) { return; }

		try
		{
			final int oldValue = this.zoomOut;
			this.zoomOut = newValue;

			propertyChangeListeners.firePropertyChange("zoomOut", oldValue, newValue);

			centreMouse();
			Thread.sleep(100);

			for (int i = 0; i < newValue; i++)
			{
				r.mouseWheel(1);
			}

		}
		catch (final AWTException e)
		{
			setControllable(false);
		}
		catch (final InterruptedException e)
		{

		}
	}

	public void stop()
	{
		frame.dispose();
	}

	Point centreMouse() throws AWTException
	{
		// puts the mouse pointer in the centre of the window
		// and makes sure that the window is to the front

		final Point topLeft = frame.getLocation();

		final int halfWidth = size.width / 2;
		final int halfHeight = size.height / 2;

		final Point centre = new Point(topLeft.x + halfWidth, topLeft.y + halfHeight);

		frame.toFront();

		r.mouseMove(centre.x, centre.y);

		return centre;
	}

	protected void setControllable(final boolean newValue)
	{
		final boolean oldValue = this.controllable;
		this.controllable = newValue;

		propertyChangeListeners.firePropertyChange("controllable", oldValue, newValue);
	}
}
