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
@Category("Maths/Waves")
public class SkewedWave implements Runnable, Serializable, PropertyChangeListener
{
	public static void main(final String args[])
	{
		final SkewedWave sineWave = new SkewedWave();
		sineWave.setFreq(0.1);
		sineWave.setOutFreq(5);
		sineWave.setRunning(true);
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private double curve = 0.05;
	private double value = 0;
	private double out;
	private double max = 1;
	private double min = -1;
	private double freq = 1;
	private double outFreq = 30;
	private double mix = 0.5;
	private boolean invert = true;

	private double range = 1;

	private double delta = Math.PI / 15;

	private long sleep = 33;

	private boolean running = false;

	public SkewedWave()
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

	public double getMix()
	{
		return mix;
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

	private double tri(double x)
	{
		return 1 - 2 * Math.acos((1 - curve) * Math.sin(x))/ Math.PI;
	}

	public double sqr(double x)
	{
		return 2 * Math.atan(Math.cos(x) / curve)/ Math.PI;
	}

	public double getCurve()
	{
		return curve;
	}

	public void setCurve(final double curve)
	{
		final double oldCurve = this.curve;
		this.curve = curve;
		propertyChangeListeners.firePropertyChange("curve", oldCurve, curve);
	}

	@Override
	public void run()
	{
		value = 0;
		double triMax = 0;
		double sawMax = 0;
		while (running)
		{
			value += delta;

			final double oldOut = out;

			double sine = Math.sin(value);
			double tri = tri(value);
			double saw = (tri(value / 2) * sqr(value / 2));

			double combined = (sine * mix) + (saw * (1-mix));

			triMax = Math.max(tri, triMax);
			sawMax = Math.max(saw, sawMax);

			if(invert)
			{
				combined = combined * -1;
			}

			out = ((combined + 1) * range) + min;

			System.out.println(sine + ", " + saw + ", " + out + ", " + triMax);

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

	public boolean getInvert()
	{
		return invert;
	}

	public void setFreq(final double freq)
	{
		final double oldFreq = this.freq;
		this.freq = freq;
		updateDelta();
		propertyChangeListeners.firePropertyChange("freq", oldFreq, freq);
	}

	public void setInvert(final boolean invert)
	{
		if(this.invert != invert)
		{
			this.invert = invert;
			propertyChangeListeners.firePropertyChange("invert", !invert, invert);
		}
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

	public void setMix(final double mix)
	{
		final double oldMix = this.mix;
		this.mix = mix;
		propertyChangeListeners.firePropertyChange("mix", oldMix, mix);
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