package equip.ect.components.sineWave;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Produces a sine wave
 * 
 * @classification Behaviour/Timing
 * @defaultOutputValue out
 * @author ktg
 */
@ECTComponent
@Category("Behaviour/Timing")
public class AsymmetricalWave implements Runnable, Serializable, PropertyChangeListener
{
	public static void main(final String args[])
	{
		final AsymmetricalWave sineWave = new AsymmetricalWave();
		sineWave.setDownDuration(1);
		sineWave.setOutFreq(5);
		sineWave.setRunning(true);
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private double out;
	private double max = 1;
	private double min = -1;
	private double downDuration = 1;
	private double upDuration = 2;
	private double outFreq = 30;

	private double delta = 1 / 30;

	private double range = 1;

	private long sleep = 33;

	private boolean running = false;

	public AsymmetricalWave()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public double getDownDuration()
	{
		return downDuration;
	}

	public double getUpDuration() { return upDuration; }

	public void setUpDuration(double upDuration)
	{
		final double oldFreq = this.upDuration;
		this.upDuration = upDuration;
		updateDelta();
		propertyChangeListeners.firePropertyChange("upDuration", oldFreq, upDuration);
	}

	public double getMax()
	{
		return max;
	}

	public double getMin()
	{
		return min;
	}

	public double getOut()
	{
		return out;
	}

	public double getOutFreq()
	{
		return outFreq;
	}

	public boolean getRunning()
	{
		return running;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	@Override
	public void run()
	{
		double time = 0;
		while (running)
		{
			time += delta;

			final double oldOut = out;

			final double duration = downDuration + upDuration;
			time %= duration;

			double y;
			if(time < downDuration)
			{
				y = Math.cos(time / (downDuration / Math.PI));
			}
			else
			{
				y = Math.cos(time / (upDuration / Math.PI) + Math.PI * (1 - ((downDuration / Math.PI) / (upDuration / Math.PI))));
			}
			out = ((y + 1) * range) + min;

			System.out.println(out);

			propertyChangeListeners.firePropertyChange("out", oldOut, out);

			try
			{
				Thread.sleep(sleep);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setDownDuration(final double downDuration)
	{
		final double oldFreq = this.downDuration;
		this.downDuration = downDuration;
		updateDelta();
		propertyChangeListeners.firePropertyChange("downDuration", oldFreq, downDuration);
	}

	public void setMax(final double max)
	{
		final double oldMax = this.max;
		this.max = max;
		updateRange();
		propertyChangeListeners.firePropertyChange("max", oldMax, max);
	}

	public void setMin(final double min)
	{
		final double oldMin = this.min;
		this.min = min;
		updateRange();
		propertyChangeListeners.firePropertyChange("min", oldMin, min);
	}

	public void setOutFreq(final double outFreq)
	{
		final double oldFreq = this.outFreq;
		if (oldFreq != outFreq)
		{
			this.outFreq = outFreq;
			sleep = (long) (1000 / outFreq);

			updateDelta();
			propertyChangeListeners.firePropertyChange("outFreq", oldFreq, outFreq);
		}
	}

	public void setRunning(final boolean running)
	{
		if (this.running != running)
		{
			this.running = running;
			propertyChangeListeners.firePropertyChange("running", !running, running);
			if (running)
			{
				new Thread(this).start();
			}
		}
	}

	public void stop()
	{
		running = false;
	}

	private void updateDelta()
	{
		delta = 1 / outFreq;
	}

	private void updateRange()
	{
		range = (max - min) / 2;
	}
}