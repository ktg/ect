package equip.ect.components.sineWave;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;

@ECTComponent
@Category("Data/Waves")
public class FrequencyAnalyser implements Serializable
{
	private int offset = 0;

	private double currentMid = 0;

	private long lastZeroTime = 0;

	private double frequency = 0;
	private int length = 128;

	private double[] array = new double[length];

	private boolean filled = false;

	private boolean empty = true;

	private final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public FrequencyAnalyser()
	{
		array[0] = 0.0D;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public double getFrequency()
	{
		return frequency;
	}

	public int getLength()
	{
		return length;
	}

	public double getValue()
	{
		return array[offset];
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setLength(final int length)
	{
		final int oldLength = this.length;
		if (oldLength == length) { return; }
		if (length <= 0) { return; }

		propertyChangeListeners.firePropertyChange("value", array[offset], 0);
		offset = 0;
		empty = true;
		filled = false;

		this.length = length;
		array = new double[length];
		propertyChangeListeners.firePropertyChange("length", oldLength, length);
	}

	public void setValue(final double value)
	{
		setValue(value, new Date().getTime());
	}

	private double getCurrentMid()
	{
		return currentMid;
	}

	private void setValue(final double value, final long timestamp)
	{
		final double oldValue = getValue();
		if (empty)
		{
			array[offset] = value;
			propertyChangeListeners.firePropertyChange("value", oldValue, value);
			empty = false;
		}
		else
		{
			offset += 1;
			if (offset >= length)
			{
				filled = true;
				offset = 0;
			}

			array[offset] = value;
			propertyChangeListeners.firePropertyChange("value", oldValue, value);

			double total = 0;
			for (int index = 0; index < length; index++)
			{
				if (index > offset && !filled)
				{
					break;
				}
				total += array[index];
			}

			if (filled)
			{
				currentMid = total / length;
			}
			else
			{
				currentMid = total / (offset + 1);
			}

			if (lastZeroTime == 0)
			{
				lastZeroTime = timestamp;
			}
			else if (oldValue < currentMid && value > currentMid)
			{
				final long time = timestamp;
				final long wavelength = time - lastZeroTime;
				final double oldFreq = frequency;
				frequency = 1000.0 / wavelength;

				propertyChangeListeners.firePropertyChange("frequency", oldFreq, frequency);

				lastZeroTime = time;

				System.out.println("Crossed " + currentMid + " with " + value + ". Freq = " + frequency
						+ ", wavelength = " + wavelength + "ms");
			}
		}
	}
}