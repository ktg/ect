package equip.ect.components.transition;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;

/**
 * Transitions between two values over a duration
 * 
 * @classification Behaviour/Timing
 * @defaultOutputValue result
 * @author ktg
 */
@ECTComponent
@Category("Data/Processing")
public class Transition implements Runnable, Serializable, PropertyChangeListener
{
	public static void main(final String args[])
	{
		final Transition transition = new Transition();
		transition.setRunning(true);
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private double ax = 0, bx = 0, cx = 0, ay = 0, by = 0, cy = 0;
	private boolean running = false;
	private double progress = 0;
	private double start = 0;
	private double end = 10;
	private double result = 0;
	private double range;
	private long duration = 1000;

	private double outFreq = 30;

	private long sleep = 33;

	private double x1 = 0.1;
	private double x2 = 0.25;
	private double y1 = 0.1;
	private double y2 = 0.9;

	private long startTime;

	public Transition()
	{
		updateRange();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public long getDuration()
	{
		return duration;
	}

	public double getEnd()
	{
		return end;
	}

	public double getOutFreq()
	{
		return outFreq;
	}

	public double getResult()
	{
		return result;
	}

	public boolean getRunning()
	{
		return running;
	}

	public double getStart()
	{
		return start;
	}

	public double getX1()
	{
		return x1;
	}

	public double getX2()
	{
		return x2;
	}

	public double getY1()
	{
		return y1;
	}

	public double getY2()
	{
		return y2;
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
		setProgress(0);
		startTime = new Date().getTime();
		while (running)
		{
			updateProgress();
			try
			{
				Thread.sleep(sleep);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		setProgress(1);
	}

	public double getProgress()
	{
		return progress;
	}

	public void setProgress(double progress)
	{
		if(this.progress != progress)
		{
			double oldTransition = this.progress;
			this.progress = progress;
			propertyChangeListeners.firePropertyChange("progress", oldTransition, progress);

			setResult((progress * range) + start);
		}
	}

	private void setResult(double result)
	{
		if(this.result != result)
		{
			final double oldOut = this.result;
			this.result = result;
			propertyChangeListeners.firePropertyChange("result", oldOut, result);
		}
	}

	private void updateProgress()
	{
		if(running && startTime > 0)
		{
			final double time = new Date().getTime() - startTime;
			if (time > duration)
			{
				setRunning(false);
			}
			else
			{
				setProgress(solve(time / duration));
			}
		}
	}

	public void setDuration(final long duration)
	{
		final long oldDuration = this.duration;
		this.duration = duration;
		propertyChangeListeners.firePropertyChange("duration", oldDuration, duration);
	}

	public void setEnd(final double end)
	{
		final double oldEnd = this.end;
		this.end = end;
		propertyChangeListeners.firePropertyChange("end", oldEnd, end);
		updateRange();
	}

	public void setOutFreq(final double outFreq)
	{
		final double oldFreq = this.outFreq;
		this.outFreq = outFreq;
		propertyChangeListeners.firePropertyChange("outFreq", oldFreq, outFreq);
		updateDelta();
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

	public void setStart(final double start)
	{
		final double oldStart = this.start;
		this.start = start;
		propertyChangeListeners.firePropertyChange("start", oldStart, start);
		updateRange();
	}

	public void setX1(final double x1)
	{
		final double oldX1 = this.x1;
		this.x1 = x1;
		propertyChangeListeners.firePropertyChange("x1", oldX1, x1);
	}

	public void setX2(final double x2)
	{
		final double oldX2 = this.x2;
		this.x2 = x2;
		propertyChangeListeners.firePropertyChange("x2", oldX2, x2);
	}

	public void setY1(final double y1)
	{
		final double oldY1 = this.y1;
		this.y1 = y1;
		propertyChangeListeners.firePropertyChange("y1", oldY1, y1);
	}

	public void setY2(final double y2)
	{
		final double oldY2 = this.y2;
		this.y2 = y2;
		propertyChangeListeners.firePropertyChange("y2", oldY2, y2);
	}

	public void stop()
	{
		running = false;
	}

	private double fabs(final double n)
	{
		if (n >= 0)
		{
			return n;
		}
		else
		{
			return 0 - n;
		}
	}

	private double sampleCurveDerivativeX(final double t)
	{
		return (3.0 * ax * t + 2.0 * bx) * t + cx;
	}

	private double sampleCurveX(final double t)
	{
		return ((ax * t + bx) * t + cx) * t;
	}

	private double sampleCurveY(final double t)
	{
		return ((ay * t + by) * t + cy) * t;
	}

	private double solve(final double t)
	{
		cx = 3.0 * x1;
		bx = 3.0 * (x2 - x1) - cx;
		ax = 1.0 - cx - bx;
		cy = 3.0 * y1;
		by = 3.0 * (y2 - y1) - cy;
		ay = 1.0 - cy - by;
		return solve(t, solveEpsilon());
	}

	private double solve(final double x, final double epsilon)
	{
		return sampleCurveY(solveCurveX(x, epsilon));
	}

	// Given an x value, find a parametric value it came from.
	private double solveCurveX(final double x, final double epsilon)
	{
		double t0, t1, t2, x2, d2, i;
		// First try a few iterations of Newton's method -- normally very fast.
		for (t2 = x, i = 0; i < 8; i++)
		{
			x2 = sampleCurveX(t2) - x;
			if (fabs(x2) < epsilon) { return t2; }
			d2 = sampleCurveDerivativeX(t2);
			if (fabs(d2) < 1e-6)
			{
				break;
			}
			t2 = t2 - x2 / d2;
		}
		// Fall back to the bisection method for reliability.
		t0 = 0.0;
		t1 = 1.0;
		t2 = x;
		if (t2 < t0) { return t0; }
		if (t2 > t1) { return t1; }
		while (t0 < t1)
		{
			x2 = sampleCurveX(t2);
			if (fabs(x2 - x) < epsilon) { return t2; }
			if (x > x2)
			{
				t0 = t2;
			}
			else
			{
				t1 = t2;
			}
			t2 = (t1 - t0) * .5 + t0;
		}
		return t2; // Failure.
	}

	private double solveEpsilon()
	{
		return 1.0 / (200.0 * duration);
	}

	private void updateDelta()
	{
		sleep = (long) (1000 / outFreq);
	}

	private void updateRange()
	{
		range = end - start;
		setResult((progress * range) + start);
	}
}