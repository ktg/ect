package equip.ect.components.behaviour;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractMinMax
{
	private double outMin = -1;
	private double outMax = 1;

	private double output = 0;

	private boolean reverseOutput = false;

	protected final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public abstract double getInput();

	public abstract double getInputMax();

	public abstract double getInputMin();
	
	public abstract void setInput(double newInput);

	public double getOutput()
	{
		return output;
	}

	public double getOutputMax()
	{
		return outMax;
	}

	public double getOutputMin()
	{
		return outMin;
	}

	public boolean getReverseOutput()
	{
		return reverseOutput;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setOutputMax(final double newOutMax)
	{
		if (newOutMax != outMax)
		{
			final double oldOutMax = outMax;
			outMax = newOutMax;
			propertyChangeListeners.firePropertyChange("outputMax", oldOutMax, newOutMax);
			recalcOutput();
		}
	}

	public void setOutputMin(final double newOutMin)
	{
		if (newOutMin != outMin)
		{
			final double oldOutMin = outMin;
			outMin = newOutMin;
			propertyChangeListeners.firePropertyChange("outputMin", oldOutMin, newOutMin);
			recalcOutput();
		}
	}

	public void setReverseOutput(final boolean reverseOutput)
	{
		final boolean oldReverse = this.reverseOutput;
		this.reverseOutput = reverseOutput;

		propertyChangeListeners.firePropertyChange("reverseOutput", oldReverse, reverseOutput);
		recalcOutput();
	}

	protected void recalcOutput()
	{
		final double oldOutput = output;
		double newOutput = 0;
		System.out.println("Recalc Output: " + getInputMin() +  "," + getInput() + "," + getInputMax());
		if (getInputMin() != getInputMax())
		{
			if (reverseOutput)
			{
				newOutput = (getInputMax() - getInput()) / (getInputMax() - getInputMin());
			}
			else
			{
				newOutput = (getInput() - getInputMin()) / (getInputMax() - getInputMin());
			}
			
			newOutput = newOutput * (this.outMax - this.outMin) + this.outMin;
		}

		if (newOutput != output)
		{
			output = newOutput;

			propertyChangeListeners.firePropertyChange("output", oldOutput, newOutput);
		}
	}
}
