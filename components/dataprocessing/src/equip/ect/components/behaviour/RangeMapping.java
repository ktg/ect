package equip.ect.components.behaviour;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.io.Serializable;

@ECTComponent
@Category("data/processing")
public class RangeMapping extends AbstractMinMax implements Serializable
{
	private double inMin = -1;
	private double inMax = 1;

	private double input;

	public RangeMapping()
	{
	}

	public double getInput()
	{
		return input;
	}

	public double getInputMax()
	{
		return inMax;
	}

	public double getInputMin()
	{
		return inMin;
	}

	public void setInput(final double newInput)
	{
		if (newInput != input)
		{
			final double oldInput = input;
			input = newInput;
			propertyChangeListeners.firePropertyChange("input", oldInput, newInput);

			recalcOutput();
		}
	}

	public void setInputMax(final double newInMax)
	{
		if (newInMax != inMax)
		{
			final double oldInMax = inMax;
			inMax = newInMax;
			propertyChangeListeners.firePropertyChange("inputMax", oldInMax, newInMax);
			recalcOutput();
		}
	}

	public void setInputMin(final double newInMin)
	{
		if (newInMin != inMin)
		{
			final double oldInMin = inMin;
			inMin = newInMin;
			propertyChangeListeners.firePropertyChange("inputMin", oldInMin, newInMin);
			recalcOutput();
		}
	}
}