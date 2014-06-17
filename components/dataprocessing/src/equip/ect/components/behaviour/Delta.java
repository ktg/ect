package equip.ect.components.behaviour;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

@ECTComponent
@Category("data/processing")
public class Delta implements Serializable
{
	private int endOffset = 0;
	private int offset = 0;

	private int length = 1000;
	private double[] array = new double[this.length];

	private final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public Delta()
	{
		this.array[0] = 0.0D;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public double getDelta()
	{
		return this.array[this.offset] - this.array[this.endOffset];
	}

	public int getLength()
	{
		return this.length;
	}

	public double getValue()
	{
		return this.array[this.offset];
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

		propertyChangeListeners.firePropertyChange("value", this.array[this.offset], 0);
		propertyChangeListeners.firePropertyChange("delta", getDelta(), 0);
		offset = 0;
		endOffset = 0;

		this.length = length;
		this.array = new double[length];
		propertyChangeListeners.firePropertyChange("length", oldLength, length);

		this.array[0] = 0.0D;
	}

	public void setValue(final double value)
	{
		final double oldValue = getValue();
		final double oldDelta = getDelta();

		if ((this.offset == 0) && (this.endOffset == 0))
		{
			this.endOffset = (this.length - 1);
			this.array[this.endOffset] = value;
		}
		else
		{
			this.offset += 1;
			this.offset %= this.length;
			if (this.offset == this.endOffset)
			{
				this.endOffset += 1;
				this.endOffset %= this.length;
			}
		}

		this.array[this.offset] = value;

		this.propertyChangeListeners.firePropertyChange("delta", oldDelta, getDelta());
		this.propertyChangeListeners.firePropertyChange("value", oldValue, value);
	}
}