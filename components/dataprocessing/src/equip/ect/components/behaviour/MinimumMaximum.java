package equip.ect.components.behaviour;

import java.io.Serializable;

import equip.ect.Category;
import equip.ect.ECTComponent;
import equip.ect.components.dataprocessing.CircularList;

@ECTComponent
@Category("data/processing")
public class MinimumMaximum extends AbstractMinMax implements Serializable
{
	private int inputMaxOffset = 0;
	private int inputMinOffset = 0;

	private final CircularList buffer = new CircularList(1000);

	private double input = 0;

	public MinimumMaximum()
	{
	}

	@Override
	public double getInput()
	{
		return input;
	}

	@Override
	public double getInputMax()
	{
		return buffer.get(inputMaxOffset);
	}

	@Override
	public double getInputMin()
	{
		return buffer.get(inputMinOffset);
	}

	public int getLength()
	{
		return buffer.maxSize();
	}

	@Override
	public void setInput(final double newInput)
	{
		final double oldValue = getInput();
		if (buffer.size() == 0)
		{
			buffer.add(newInput);
			propertyChangeListeners.firePropertyChange("inputMax", oldValue, newInput);
			propertyChangeListeners.firePropertyChange("inputMin", oldValue, newInput);
			propertyChangeListeners.firePropertyChange("input", oldValue, newInput);
		}
		else
		{
			int offset = buffer.getOffset();
			buffer.add(newInput);

			final double inputMin = getInputMin();
			if (newInput <= inputMin)
			{
				inputMinOffset = offset;
				propertyChangeListeners.firePropertyChange("inputMin", inputMin, newInput);
			}
			else if (offset == inputMinOffset)
			{
				double min = newInput;
				int minOffset = offset;
				for (int count = 1; count < buffer.size(); count++)
				{
					final int index = (count + offset) % buffer.size();
					if (buffer.get(index) > min)
					{
						continue;
					}
					min = buffer.get(index);
					minOffset = index;
				}

				inputMinOffset = minOffset;
				propertyChangeListeners.firePropertyChange("inputMin", inputMin, min);
			}

			final double inputMax = getInputMax();
			if (newInput >= inputMax)
			{
				inputMaxOffset = offset;
				propertyChangeListeners.firePropertyChange("inputMax", inputMax, newInput);
			}
			else if (offset == inputMaxOffset)
			{
				double max = newInput;
				int maxOffset = offset;
				for (int count = 1; count < buffer.size(); count++)
				{
					final int index = (count + offset) % buffer.size();
					if (buffer.get(index) < max)
					{
						continue;
					}
					max = buffer.get(index);
					maxOffset = index;
				}

				inputMaxOffset = maxOffset;
				propertyChangeListeners.firePropertyChange("inputMax", inputMax, max);
			}

			input = newInput;
			propertyChangeListeners.firePropertyChange("input", oldValue, newInput);
		}

		recalcOutput();
	}

	public void setLength(final int newLength)
	{
		final int oldLength = buffer.maxSize();
		if (oldLength == newLength) { return; }
		if (newLength <= 0) { return; }

		propertyChangeListeners.firePropertyChange("input", getInput(), 0);
		propertyChangeListeners.firePropertyChange("output", getOutput(), 0);
		propertyChangeListeners.firePropertyChange("inputMax", getInputMax(), 0);
		propertyChangeListeners.firePropertyChange("inputMin", getInputMin(), 0);
		inputMaxOffset = 0;
		inputMinOffset = 0;

		buffer.setMaxSize(newLength);
		propertyChangeListeners.firePropertyChange("length", oldLength, newLength);
	}
}