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
@Category("Data/Waves")
public class SineWave implements Runnable, Serializable, PropertyChangeListener
{
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private double out;
	private double max = 1;
	private double min = -1;
	private double freq = 1;
	private double outFreq = 30;

	private double range = 1;

	private double delta = Math.PI / 15;

	private long sleep = 33;

	private boolean running = false;

	public SineWave()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public double getFreq()
	{
		return freq;
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
		double value = 0;
		while (running)
		{
			value += delta;

			final double oldOut = out;

			out = ((Math.sin(value) + 1) * range) + min;

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

	public void setFreq(final double freq)
	{
		final double oldFreq = this.freq;
		this.freq = freq;
		updateDelta();
		propertyChangeListeners.firePropertyChange("freq", oldFreq, freq);
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
		delta = 2 * Math.PI * freq / outFreq;
	}

	private void updateRange()
	{
		range = (max - min) / 2;
	}
}