/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.audioframework.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * a single frame of audio samples
 */
public class Frame implements Serializable
{
	/**
	 * sample rate, Hz(? as per JMF, anyway)
	 */
	public double sampleRate;
	/**
	 * number of channels
	 */
	public int channels;
	/**
	 * data
	 */
	public Object data;
	/**
	 * short to float conversion multiplier
	 */
	public static float SHORT_TO_FLOAT = (1.0f / 32768);
	/**
	 * approx to sin
	 */
	protected static float sin[];
	protected static final int SIN_STEPS = 1000;
	static
	{
		sin = new float[SIN_STEPS];
		for (int i = 0; i < SIN_STEPS; i++)
		{
			sin[i] = (float) Math.sin(Math.PI * 2 * i / SIN_STEPS);
		}
	}

	/**
	 * cons; does NOT clone data. Currently best-supported types: short[], float[], 1 or 2 channels.
	 */
	public Frame(final double sampleRate, final int channels, final Object data)
	{
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.data = data;
	}

	/**
	 * copy - does NOT copy array
	 */
	public Frame copy()
	{
		final Frame f = new Frame(sampleRate, channels, data);
		return f;
	}

	/**
	 * copy data
	 */
	public Object copyData()
	{
		final Object d = java.lang.reflect.Array.newInstance(	data.getClass().getComponentType(),
																java.lang.reflect.Array.getLength(data));
		System.arraycopy(data, 0, d, 0, java.lang.reflect.Array.getLength(data));
		return d;
	}

	/**
	 * get absolute
	 */
	public Frame getAbs() throws UnsupportedFormatException
	{
		if (data instanceof short[])
		{
			final short fromdata[] = (short[]) data;
			final short newdata[] = new short[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				newdata[i] = (short) Math.abs(fromdata[i]);
			}
			return new Frame(sampleRate, channels, newdata);
		}
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			final float newdata[] = new float[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				newdata[i] = Math.abs(fromdata[i]);
			}
			return new Frame(sampleRate, channels, newdata);
		}
		throw new UnsupportedFormatException("getAbs " + this);
	}

	/**
	 * get convolution with various frequencies (in Hz)
	 */
	public Frame getFrequencyConvolution(final float frequencies[]) throws UnsupportedFormatException
	{
		final Frame f = this.getInFormat(1, Float.TYPE);
		final float fromdata[] = (float[]) f.data;
		final float outputs[] = new float[frequencies.length];
		for (int fi = 0; fi < frequencies.length; fi++)
		{
			if (frequencies[fi] == 0)
			{
				// total
				float t = 0;
				for (final float element : fromdata)
				{
					t = t + element * element;
				}
				outputs[fi] = t / fromdata.length;
			}
			else
			{
				float st = 0, ct = 0;
				for (int i = 0; i < fromdata.length; i++)
				{
					final float angle = (float) (i * frequencies[fi] / sampleRate);
					st = st + sin(angle) * fromdata[i];
					ct = ct + cos(angle) * fromdata[i];
				}
				st = st / fromdata.length;
				ct = ct / fromdata.length;
				outputs[fi] = (st * st + ct * ct);
			}
		}
		return new Frame(0, 1, outputs);
	}

	/**
	 * get in alternative format; may return this if format matches; otherwise will be have new data
	 * (typically packed) array. Currently supports packed RGB (in int) -> float, packed RGB ->
	 * boolean, float -> boolean.
	 */
	public Frame getInFormat(final int toChannels, final Class dataClass) throws UnsupportedFormatException
	{
		if (channels == this.channels && dataClass.equals(data.getClass().getComponentType())) { return this; }

		final int datasize = java.lang.reflect.Array.getLength(this.data) * toChannels / this.channels;
		final Object ffdata = java.lang.reflect.Array.newInstance(dataClass, datasize);
		final Frame f = new Frame(sampleRate, toChannels, ffdata);

		if (f.data instanceof float[])
		{
			final float todata[] = (float[]) f.data;
			if (this.data.getClass().getComponentType().equals(Float.TYPE))
			{
				final float fromdata[] = (float[]) this.data;
				// ave
				for (int i = 0; i < todata.length; i = i + f.channels)
				{
					float t = 0;
					for (int j = 0, p = i * this.channels / f.channels; j < this.channels; j++)
					{
						t = t + fromdata[p + j];
					}
					t = t / this.channels;
					// then copy
					for (int c = 0; c < f.channels; c++)
					{
						todata[i + c] = (short) t;
					}
				}
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Short.TYPE))
			{
				final short fromdata[] = (short[]) this.data;
				// convert type
				final float fdata[] = new float[fromdata.length];
				final Frame ff = new Frame(sampleRate, channels, fdata);
				for (int i = 0; i < fromdata.length; i++)
				{
					fdata[i] = SHORT_TO_FLOAT * fromdata[i];
				}
				// channel adjustment (if any)
				return ff.getInFormat(toChannels, dataClass);
			}
		}
		throw new UnsupportedFormatException("to " + f + " from " + this);
	}

	/**
	 * get scaled
	 */
	public Frame getScaled(final float scale) throws UnsupportedFormatException
	{
		if (data instanceof short[])
		{
			final short fromdata[] = (short[]) data;
			final short newdata[] = new short[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				final int v = (int) (fromdata[i] * scale);
				if (v < -32767)
				{
					newdata[i] = -32767;
				}
				else if (v > 32767)
				{
					newdata[i] = 32767;
				}
				else
				{
					newdata[i] = (short) v;
				}
			}
			return new Frame(sampleRate, channels, newdata);
		}
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			final float newdata[] = new float[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				newdata[i] = fromdata[i] * scale;
			}
			return new Frame(sampleRate, channels, newdata);
		}
		throw new UnsupportedFormatException("getScaled " + this);
	}

	/**
	 * get square
	 */
	public Frame getSquare() throws UnsupportedFormatException
	{
		if (data instanceof short[])
		{
			final short fromdata[] = (short[]) data;
			final short newdata[] = new short[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				newdata[i] = (short) (fromdata[i] * fromdata[i] / 32768);
			}
			return new Frame(sampleRate, channels, newdata);
		}
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			final float newdata[] = new float[fromdata.length];
			for (int i = 0; i < newdata.length; i++)
			{
				newdata[i] = fromdata[i] * fromdata[i];
			}
			return new Frame(sampleRate, channels, newdata);
		}
		throw new UnsupportedFormatException("getSquare " + this);
	}

	/**
	 * get sub-sampled; new data array (just averages at the moment)
	 */
	public Frame getSubsampled(final int step) throws UnsupportedFormatException
	{
		if (data instanceof short[])
		{
			final short fromdata[] = (short[]) data;
			final short newdata[] = new short[fromdata.length / step];
			for (int i = 0; i < newdata.length; i = i + channels)
			{
				for (int c = 0; c < channels; c++)
				{
					int j;
					int p;
					int t = 0;
					for (j = 0, p = step * i; j < step * channels; j = j + channels)
					{
						t = t + fromdata[p + j + c];
					}
					newdata[i + c] = (short) (t / step);
				}
			}
			return new Frame(sampleRate / step, channels, newdata);
		}
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			final float newdata[] = new float[fromdata.length / step];
			for (int i = 0; i < newdata.length; i = i + channels)
			{
				for (int c = 0; c < channels; c++)
				{
					int j;
					int p;
					float t = 0;
					for (j = 0, p = step * i; j < step * channels; j = j + channels)
					{
						t = t + fromdata[p + j + c];
					}
					newdata[i + c] = t / step;
				}
			}
			return new Frame(sampleRate / step, channels, newdata);
		}
		throw new UnsupportedFormatException("getSubsampled " + this);
	}

	/**
	 * get as buffered image of waveform
	 */
	public BufferedImage getWaveformBufferedImage() throws UnsupportedFormatException
	{
		final Frame f = this.getInFormat(1, Float.TYPE);

		final int w = 800;
		final int h = 600;
		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final Graphics g = image.getGraphics();

		final float val[] = (float[]) f.data;
		int lastx = 0;
		int lasty = 0;
		for (int i = 0; i < val.length; i++)
		{
			final int x = i * w / val.length;
			final int y = (int) (0.5f * h * (1 - val[i]));
			if (i > 0)
			{
				g.drawLine(lastx, lasty, x, y);
			}
			else
			{
				g.drawLine(x, y, x, y);
			}
			lastx = x;
			lasty = y;
			if ((i & 3) == 0)
			{
				g.drawLine(x, h / 2, x, h / 2);
			}
		}

		g.drawString(this.toString(), 10, 20);
		g.drawString("samples: " + val.length, 10, 30);
		for (int i = 0; i < val.length && 40 + 10 * i < h; i++)
		{
			g.drawString(" v[" + i + "]=" + val[i], 10, 40 + 10 * i);
		}
		return image;
	}

	/**
	 * window, hamming = 0.02
	 */
	public Frame getWindowed(final float alpha) throws UnsupportedFormatException
	{
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			final float todata[] = new float[fromdata.length];
			for (int i = 0; i < fromdata.length; i = i + channels)
			{
				final float factor = 1 - (1 - alpha) * 0.5f * (1 + cos(i * 1.0f / fromdata.length));
				for (int c = 0; c < channels; c++)
				{
					todata[i + c] = factor * fromdata[i + c];
				}
			}
			return new Frame(sampleRate, channels, todata);
		}
		throw new UnsupportedFormatException("getWindowed " + this);
	}

	/**
	 * window and max length, hamming = 0.02
	 */
	public Frame getWindowed(final float alpha, final float maxLenSecs) throws UnsupportedFormatException
	{
		final int maxLenSamples = (int) (maxLenSecs * sampleRate);
		if (data instanceof float[])
		{
			final float fromdata[] = (float[]) data;
			if (maxLenSamples >= fromdata.length)
			{
				// already not as long as max
				return getWindowed(alpha);
			}

			final float todata[] = new float[maxLenSamples * channels];
			for (int i = 0; i < todata.length; i = i + channels)
			{
				final float factor = 1 - (1 - alpha) * 0.5f * (1 + cos(i * 1.0f / todata.length));
				for (int c = 0; c < channels; c++)
				{
					todata[i + c] = factor * fromdata[i + c];
				}
			}
			return new Frame(sampleRate, channels, todata);
		}
		throw new UnsupportedFormatException("getWindowed " + this);
	}

	/**
	 * to string
	 */
	@Override
	public String toString()
	{
		final StringBuffer b = new StringBuffer();
		b.append("AudioFormat[sampleRate=");
		b.append(new Double(sampleRate).toString());
		b.append(", channels=");
		b.append(new Integer(channels).toString());
		b.append(", class=");
		b.append(data.getClass().getComponentType().getName());
		b.append(", datasize=");
		b.append(new Integer(java.lang.reflect.Array.getLength(data)).toString());
		b.append("]");
		return b.toString();
	}

	/**
	 * approx to cos, angle in revolutions
	 */
	protected float cos(float angle)
	{
		angle = angle + 0.25f;
		final int i = (int) (SIN_STEPS * (angle - Math.floor(angle)));
		if (i >= SIN_STEPS) { return sin[SIN_STEPS - 1]; }
		return sin[i];
	}

	/**
	 * approx to sin, angle in revolutions
	 */
	protected float sin(final float angle)
	{
		final int i = (int) (SIN_STEPS * (angle - Math.floor(angle)));
		if (i >= SIN_STEPS) { return sin[SIN_STEPS - 1]; }
		return sin[i];
	}
}
