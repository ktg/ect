package equip.ect.components.sineWave;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

@ECTComponent
public class ValueTrigger implements Serializable
{
	private boolean running = false;
	private double value;

	private boolean triggered = false;

	private final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public boolean getRunning()
	{
		return running;
	}

	public boolean getTriggered()
	{
		return triggered;
	}

	public double getValue()
	{
		return value;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setRunning(final boolean value)
	{
		final boolean oldValue = running;

		if (oldValue != value)
		{
			running = value;
			propertyChangeListeners.firePropertyChange("running", oldValue, value);

			if (!running)
			{
				setTriggered(false);
			}
		}
	}

	public void setValue(final double value)
	{
		final double oldValue = this.value;

		if (oldValue != value)
		{
			this.value = value;
			propertyChangeListeners.firePropertyChange("value", oldValue, value);

			if (running)
			{
				setTriggered(true);
			}
		}
	}

	private void setTriggered(final boolean value)
	{
		final boolean oldValue = triggered;

		if (oldValue != value)
		{
			triggered = value;
			propertyChangeListeners.firePropertyChange("triggered", oldValue, value);
		}
	}
}
