package equip.ect.components.sineWave;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Date;

@ECTComponent
@Category("Data/Waves")
public class FourierTransform implements Serializable, PropertyChangeListener
{
	private final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	private final double constant = -2 * Math.PI;
	private int bufferLength = 128;
	private double frequency;
	private Entry[] values;
	private double[] realArray;
	private double[] imagArray;
	private double[] freqArray;
	private int valuesStart;
	private int nu;
	private long count;
	private double value;
	private double radice;
	private int calculationFreq = 2;

	public FourierTransform()
	{
		setBufferLength(bufferLength);
	}

	/**
	 * The reference bitreverse function.
	 */
	private static int bitreverseReference(final int j, final int nu)
	{
		int j2;
		int j1 = j;
		int k = 0;
		for (int i = 1; i <= nu; i++)
		{
			j2 = j1 / 2;
			k = 2 * k + j1 - 2 * j2;
			j1 = j2;
		}
		return k;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public int getBufferLength()
	{
		return bufferLength;
	}

	public void setBufferLength(final int bufferLength)
	{
		final int oldLength = this.bufferLength;
		this.bufferLength = bufferLength;

		values = new Entry[bufferLength];

		realArray = new double[bufferLength];
		imagArray = new double[bufferLength];

		freqArray = new double[bufferLength / 2];

		radice = 1 / Math.sqrt(bufferLength);

		final double ld = Math.log(bufferLength) / Math.log(2.0);

		nu = (int) ld;

		propertyChangeListeners.firePropertyChange("bufferLength", oldLength, bufferLength);
	}

	public int getCalculationFreq()
	{
		return calculationFreq;
	}

	public void setCalculationFreq(final int value)
	{
		final int oldFreq = calculationFreq;
		calculationFreq = value;

		propertyChangeListeners.firePropertyChange("calculationFreq", oldFreq, calculationFreq);
	}

	public double getFrequency()
	{
		return frequency;
	}

	public double getValue()
	{
		return value;
	}

	public void setValue(final double value)
	{
		final double oldValue = this.value;
		this.value = value;

		values[valuesStart] = new Entry();
		values[valuesStart].value = value;
		values[valuesStart].time = new Date().getTime();

		valuesStart = getOffset(valuesStart + 1);

		count++;

		if (count > bufferLength && (count % calculationFreq) == 0)
		{
			fft();
		}

		propertyChangeListeners.firePropertyChange("value", oldValue, value);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	void setValue(final double value, final long timestamp)
	{
		final double oldValue = this.value;
		this.value = value;

		values[valuesStart] = new Entry();
		values[valuesStart].value = value;
		values[valuesStart].time = timestamp;

		valuesStart = getOffset(valuesStart + 1);

		count++;

		if (count > bufferLength && (count % calculationFreq) == 0)
		{
			fft();
		}

		propertyChangeListeners.firePropertyChange("value", oldValue, value);
	}

	private void fft()
	{
		int n2 = bufferLength / 2;
		int nu1 = nu - 1;
		double tReal, tImag, p, arg, c, s;

		// I don't want to overwrite the input arrays, so here I copy them. This
		// choice adds \Theta(2n) to the complexity.
		// long latest = values[getOffset(valuesStart - 1)].time;
		for (int i = 0; i < bufferLength; i++)
		{
			realArray[i] = values[getOffset(i + valuesStart)].value;
			imagArray[i] = 0;
			// latest - values[getOffset(i + valuesStart)].value;
		}

		// First phase - calculation
		int k = 0;
		for (int l = 1; l <= nu; l++)
		{
			while (k < bufferLength)
			{
				for (int i = 1; i <= n2; i++)
				{
					p = bitreverseReference(k >> nu1, nu);
					// direct FFT or inverse FFT
					arg = constant * p / bufferLength;
					c = Math.cos(arg);
					s = Math.sin(arg);
					tReal = realArray[k + n2] * c + imagArray[k + n2] * s;
					tImag = imagArray[k + n2] * c - realArray[k + n2] * s;
					realArray[k + n2] = realArray[k] - tReal;
					imagArray[k + n2] = imagArray[k] - tImag;
					realArray[k] += tReal;
					imagArray[k] += tImag;
					k++;
				}
				k += n2;
			}
			k = 0;
			nu1--;
			n2 /= 2;
		}

		// Second phase - recombination
		k = 0;
		int r;
		while (k < bufferLength)
		{
			r = bitreverseReference(k, nu);
			if (r > k)
			{
				tReal = realArray[k];
				tImag = imagArray[k];
				realArray[k] = realArray[r];
				imagArray[k] = imagArray[r];
				realArray[r] = tReal;
				imagArray[r] = tImag;
			}
			k++;
		}

		// Here I have to mix xReal and xImag to have an array (yes, it should
		// be possible to do this stuff in the earlier parts of the code, but
		// it's here to readibility).
		for (int i = 0; i < freqArray.length; i++)
		{
			// I used Stephen Wolfram's Mathematica as a reference so I'm going
			// to normalize the output while I'm copying the elements.
			realArray[i] *= radice;
			imagArray[i] *= radice;
			freqArray[i] = Math.sqrt((realArray[i] * realArray[i]) + (imagArray[i] * imagArray[i]));
			// freqArray[i] = Math.abs(realArray[i2] * radice);
			// freqArray[i + 1] = Math.abs(imagArray[i2] * radice);
		}

		double max = 0;
		int maxBin = 0;
		for (int i = 1; i < freqArray.length - 1; i++)
		{
			if (freqArray[i] > max)
			{
				max = freqArray[i];
				maxBin = i;
			}
		}

		System.out.println(freqArray[0] + ", " + realArray[0] + ", " + imagArray[0] + ", " + radice);

		if (maxBin != 0)
		{
			final long timespan = values[getOffset(valuesStart - 1)].time - values[valuesStart].time;
			final double oldFreq = frequency;

			final double y1 = freqArray[getOffset(maxBin - 1)];
			final double y2 = freqArray[maxBin];
			final double y3 = freqArray[getOffset(maxBin + 1)];
			final double d = (y3 - y1) / (y1 + y2 + y3);
			frequency = (maxBin + d) * 1000 / timespan;

			propertyChangeListeners.firePropertyChange("frequency", oldFreq, frequency);
		}
	}

	private int getOffset(final int offset)
	{
		return ((bufferLength + offset) % bufferLength);
	}

	private static class Entry
	{
		private double value;
		private long time;
	}
}