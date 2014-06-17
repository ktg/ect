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
package equip.ect.components.visionframework.common;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;

/**
 * a single frame of video
 */
public class Frame implements Serializable
{
	/**
	 * type
	 */
	public int type;
	/**
	 * type - RGB bytes (a) for 1 element per pixel, packed into int (b) for 3 elements per pixel,
	 * in consecutive elements
	 */
	public static final int TYPE_RGB = 1;
	/**
	 * default red shift when packed as bytes in int
	 */
	public static final int RED_SHIFT = 16;
	/**
	 * default blue shift
	 */
	public static final int BLUE_SHIFT = 8;
	/**
	 * default green shift
	 */
	public static final int GREEN_SHIFT = 0;
	/**
	 * type - single value, e.g. float, boolean
	 */
	public static final int TYPE_1D = 2;
	/**
	 * type - YUV for 3 elements per pixel (float), in consecutive elements
	 */
	public static final int TYPE_YUV = 3;
	/**
	 * elements per pixel
	 */
	public int elementsPerPixel;
	/**
	 * width
	 */
	public int width;
	/**
	 * height
	 */
	public int height;
	/**
	 * offset
	 */
	public int offset;
	/**
	 * pixelstep, i.e. elements between pixels; may be negative
	 */
	public int pixelstep;
	/**
	 * linestride, i.e. elements between same pixel on consecutive lines; may be negative
	 */
	public int linestride;
	/**
	 * data
	 */
	public Object data;
	/**
	 * RGB to float conversion multiplier
	 */
	public static float RGB_TO_FLOAT_FACTOR = (1.0f / 255.0f);
	/**
	 * float to RGB conversion multiplier
	 */
	public static float FLOAT_TO_RGB_FACTOR = (255.999f);

	/**
	 * convert, true-> 1, false->0
	 */
	public static void convertBooleanToFloat(final Frame from, final Frame to)
	{
		final boolean b[] = (boolean[]) from.data;
		final float v[] = (float[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{
				v[ti] = b[fi] ? 1.0f : 0.0f;
			}
		}
	}

	/**
	 * convert - default > 0.5
	 */
	public static void convertFloatToBoolean(final Frame from, final Frame to)
	{
		convertFloatToBoolean(from, to, 0.5f, true);
	}

	/**
	 * convert - threshold test
	 */
	public static void convertFloatToBoolean(final Frame from, final Frame to, final float threshold,
			final boolean aboveFlag)
	{
		final float v[] = (float[]) from.data;
		final boolean b[] = (boolean[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{
				b[ti] = (v[fi] > threshold) ? (aboveFlag) : (!aboveFlag);
			}
		}
	}

	/**
	 * convert; 1 -> white; greyscale.
	 */
	public static void convertFloatToPackedRGB(final Frame from, final Frame to)
	{
		final float v[] = (float[]) from.data;
		final int rgb[] = (int[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{

				// Y
				int r, g, b;
				r = g = b = (v[fi] < 0) ? 0 : (v[fi] > 1 ? 0xff : (int) (FLOAT_TO_RGB_FACTOR * v[fi]));
				rgb[ti] = (r << RED_SHIFT) | (g << GREEN_SHIFT) | (b << BLUE_SHIFT);
			}
		}
	}

	public static void convertFloatToYUV(final Frame from, final Frame to)
	{
		final float v[] = (float[]) from.data;
		final float yuv[] = (float[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{
				yuv[ti] = v[fi];
				yuv[ti + 1] = yuv[ti + 2] = 0f;
			}
		}
	}

	/**
	 * convert - intensity
	 */
	public static void convertPackedRGBToFloat(final Frame from, final Frame to)
	{
		final int rgb[] = (int[]) from.data;
		final float v[] = (float[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{

				// Y
				int r, g, b;
				r = (rgb[fi] >> RED_SHIFT) & 0xff;
				g = (rgb[fi] >> GREEN_SHIFT) & 0xff;
				b = (rgb[fi] >> BLUE_SHIFT) & 0xff;

				v[ti] = RGB_TO_FLOAT_FACTOR * (0.299f * r + 0.587f * g + 0.114f * b);
			}
		}
	}

	public static void convertPackedRGBToYUV(final Frame from, final Frame to)
	{
		final int rgb[] = (int[]) from.data;
		final float yuv[] = (float[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{

				// Y
				int r, g, b;
				r = (rgb[fi] >> RED_SHIFT) & 0xff;
				g = (rgb[fi] >> GREEN_SHIFT) & 0xff;
				b = (rgb[fi] >> BLUE_SHIFT) & 0xff;

				yuv[ti] = RGB_TO_FLOAT_FACTOR * (0.299f * r + 0.587f * g + 0.114f * b);
				yuv[ti + 1] = (0.565f * (RGB_TO_FLOAT_FACTOR * b - yuv[ti])); // U
				yuv[ti + 2] = (0.713f * (RGB_TO_FLOAT_FACTOR * r - yuv[ti])); // V
			}
		}
	}

	public static void convertYUVToFloat(final Frame from, final Frame to)
	{
		final float yuv[] = (float[]) from.data;
		final float v[] = (float[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{
				// V only
				v[ti] = yuv[fi];
			}
		}
	}

	public static void convertYUVToPackedRGB(final Frame from, final Frame to)
	{
		final float yuv[] = (float[]) from.data;
		final int rgb[] = (int[]) to.data;
		int s, t;
		int fir, tir;
		for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
				+ to.linestride)
		{
			int fi, ti;
			for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
			{

				// Y
				int r, g, b;
				r = (int) (FLOAT_TO_RGB_FACTOR * (yuv[fi]/* Y */+ 1.403f * yuv[fi + 2]/* V' */)) & 0xff;
				g = (int) (FLOAT_TO_RGB_FACTOR * (yuv[fi]/* Y */- 0.344f * yuv[fi + 1]/* U' */- 0.714f * yuv[fi + 2]/*
																												 * V
																												 * '
																												 */)) & 0xff;
				b = (int) (FLOAT_TO_RGB_FACTOR * (yuv[fi]/* Y */+ 1.770f * yuv[fi + 1]/* U' */)) & 0xff;
				rgb[ti] = (r << RED_SHIFT) | (g << GREEN_SHIFT) | (b << BLUE_SHIFT);
			}
		}
	}

	/**
	 * cons; does NOT clone data. Currently best-supported types: packed RGB (type RGB, 1 element
	 * per pixel, int[]), float 1D (type 1D, (1 el per pixel), float[]), boolean 1D (type 1D, (1 el
	 * per pixel), boolean[]), YUV (type YUV, 3 el per pixel, float[]).
	 */
	public Frame(final int type, final int elementsPerPixel, final int width, final int height, final int offset,
			final int pixelstep, final int linestride, final Object data)
	{
		this.type = type;
		this.elementsPerPixel = elementsPerPixel;
		if (type == TYPE_1D)
		{
			// forced
			this.elementsPerPixel = 1;
		}
		this.height = height;
		this.width = width;
		this.offset = offset;
		this.pixelstep = pixelstep;
		this.linestride = linestride;
		this.data = data;
	}

	/**
	 * copy - does NOT copy array
	 */
	public Frame copy()
	{
		final Frame f = new Frame(type, elementsPerPixel, width, height, offset, pixelstep, linestride, data);
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
	 * gate this by boolean image pixels
	 */
	public Frame gate(final Frame from) throws UnsupportedFormatException
	{
		if (!(from.data instanceof boolean[]) || from.type != TYPE_1D) { throw new UnsupportedFormatException(
				"Cannot gate by " + from); }
		final Frame to = this.copy();
		to.data = java.lang.reflect.Array.newInstance(	data.getClass().getComponentType(),
														java.lang.reflect.Array.getLength(data));
		if (data instanceof int[])
		{

			int s, t;
			final boolean gateval[] = (boolean[]) from.data;
			final int val[] = (int[]) data;
			final int tval[] = (int[]) to.data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					if (gateval[fi])
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = val[i + c];
						}
					}
					else
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = 0;
						}
					}
				}
			}
		}
		else if (data instanceof float[])
		{

			int s, t;
			final boolean gateval[] = (boolean[]) from.data;
			final float val[] = (float[]) data;
			final float tval[] = (float[]) to.data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					if (gateval[fi])
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = val[i + c];
						}
					}
					else
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = 0;
						}
					}
				}
			}
		}
		else if (data instanceof boolean[])
		{

			int s, t;
			final boolean gateval[] = (boolean[]) from.data;
			final boolean val[] = (boolean[]) data;
			final boolean tval[] = (boolean[]) to.data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					if (gateval[fi])
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = val[i + c];
						}
					}
					else
					{
						for (int c = 0; c < elementsPerPixel; c++)
						{
							tval[ti + c] = false;
						}
					}
				}
			}
		}
		else
		{
			throw new UnsupportedFormatException("gate on " + this);
		}
		return to;
	}

	/**
	 * get as buffered image
	 */
	public BufferedImage getBufferedImage() throws UnsupportedFormatException
	{
		final Frame f = this.getInFormat(TYPE_RGB, 1, Integer.TYPE).getPacked();
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final WritableRaster raster = image.getRaster();
		raster.setDataElements(0, 0, width, height, f.data);
		// image.setRGB(0, 0, width, height, (int[])(f.data), f.offset, f.linestride);
		return image;
	}

	/**
	 * get square of 'distance' between images, in this image's colour space, with RGB/YUV/Y scale
	 * factors given.
	 * 
	 * @return distance image (1D float)
	 */
	public Frame getDistance2(final Frame otherin, final float element1Scale, final float element2Scale,
			final float element3Scale) throws UnsupportedFormatException
	{
		final Frame from = otherin.getInFormat(type, elementsPerPixel, data.getClass().getComponentType());
		final int w = (width < from.width) ? width : from.width;
		final int h = (height < from.height) ? height : from.height;
		final int datasize = w * h;
		final float rdata[] = new float[datasize];
		final Frame to = new Frame(TYPE_1D, 1, w, h, 0, 1, w, rdata);
		if (type == TYPE_RGB && elementsPerPixel == 1 && data instanceof int[])
		{
			// packed RGB
			int s, t;
			final int frgb[] = (int[]) from.data;
			final int rgb[] = (int[]) data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					int r, g, b;
					r = (rgb[i] >> RED_SHIFT) & 0xff;
					g = (rgb[i] >> GREEN_SHIFT) & 0xff;
					b = (rgb[i] >> BLUE_SHIFT) & 0xff;
					int fr, fg, fb;
					fr = (frgb[fi] >> RED_SHIFT) & 0xff;
					fg = (frgb[fi] >> GREEN_SHIFT) & 0xff;
					fb = (frgb[fi] >> BLUE_SHIFT) & 0xff;
					rdata[ti] = RGB_TO_FLOAT_FACTOR
							* RGB_TO_FLOAT_FACTOR
							* (element1Scale * (r - fr) * (r - fr) + element2Scale * (g - fg) * (g - fg) + element3Scale
									* (b - fb) * (g - fg));
				}
			}
		}
		else if (type == TYPE_1D && data instanceof float[])
		{
			// float 1D
			int s, t;
			final float fval[] = (float[]) from.data;
			final float val[] = (float[]) data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = element1Scale * (val[i] - fval[fi]) * (val[i] - fval[fi]);
				}
			}
		}
		else if (type == TYPE_YUV && elementsPerPixel == 3 && data instanceof float[])
		{
			// float 3D
			int s, t;
			final float fval[] = (float[]) from.data;
			final float val[] = (float[]) data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = element1Scale * (val[i] - fval[fi]) * (val[i] - fval[fi]) + element2Scale
							* (val[i + 1] - fval[fi + 1]) * (val[i + 1] - fval[fi + 1]) + element2Scale
							* (val[i + 2] - fval[fi + 2]) * (val[i + 2] - fval[fi + 2]);
				}
			}
		}
		else if (type == TYPE_1D && data instanceof boolean[])
		{
			// boolean
			int s, t;
			final boolean fval[] = (boolean[]) from.data;
			final boolean val[] = (boolean[]) data;
			int fir, tir, ir;
			for (t = 0, fir = from.offset, tir = to.offset, ir = offset; t < to.height; t++, fir = fir
					+ from.linestride, tir = tir + to.linestride, ir = ir + linestride)
			{
				int fi, ti, i;
				for (s = 0, fi = fir, ti = tir, i = ir; s < to.width; s++, fi = fi + from.pixelstep, ti = ti
						+ to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = (val[i] == fval[i]) ? 0 : element1Scale;
				}
			}
		}
		else
		{
			throw new UnsupportedFormatException("getDistance2 not supported on " + this.toString());
		}
		return to;
	}

	/**
	 * get square of 'distance' between images, in this image's colour space, with RGB/YUV/Y scale
	 * factors given.
	 * 
	 * @return distance image (1D float)
	 */
	public Frame getDistance2FromConstant(final float element1Value, final float element2Value,
			final float element3Value, final float element1Scale, final float element2Scale, final float element3Scale)
			throws UnsupportedFormatException
	{
		final int datasize = width * height;
		final float rdata[] = new float[datasize];
		final Frame to = new Frame(TYPE_1D, 1, width, height, 0, 1, width, rdata);
		if (type == TYPE_RGB && elementsPerPixel == 1 && data instanceof int[])
		{
			// packed RGB
			int fr, fg, fb;
			fr = (int) (FLOAT_TO_RGB_FACTOR * element1Value);
			fg = (int) (FLOAT_TO_RGB_FACTOR * element2Value);
			fb = (int) (FLOAT_TO_RGB_FACTOR * element3Value);
			int s, t;
			final int rgb[] = (int[]) data;
			int tir, ir;
			for (t = 0, tir = to.offset, ir = offset; t < to.height; t++, tir = tir + to.linestride, ir = ir
					+ linestride)
			{
				int ti, i;
				for (s = 0, ti = tir, i = ir; s < to.width; s++, ti = ti + to.pixelstep, i = i + pixelstep)
				{
					int r, g, b;
					r = (rgb[i] >> RED_SHIFT) & 0xff;
					g = (rgb[i] >> GREEN_SHIFT) & 0xff;
					b = (rgb[i] >> BLUE_SHIFT) & 0xff;
					rdata[ti] = RGB_TO_FLOAT_FACTOR
							* RGB_TO_FLOAT_FACTOR
							* (element1Scale * (r - fr) * (r - fr) + element2Scale * (g - fg) * (g - fg) + element3Scale
									* (b - fb) * (g - fg));
				}
			}
		}
		else if (type == TYPE_1D && data instanceof float[])
		{
			// float 1D
			int s, t;
			final float val[] = (float[]) data;
			int tir, ir;
			for (t = 0, tir = to.offset, ir = offset; t < to.height; t++, tir = tir + to.linestride, ir = ir
					+ linestride)
			{
				int ti, i;
				for (s = 0, ti = tir, i = ir; s < to.width; s++, ti = ti + to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = element1Scale * (val[i] - element1Value) * (val[i] - element1Value);
				}
			}
		}
		else if (type == TYPE_YUV && elementsPerPixel == 3 && data instanceof float[])
		{
			// float 3D
			int s, t;
			final float val[] = (float[]) data;
			int tir, ir;
			for (t = 0, tir = to.offset, ir = offset; t < to.height; t++, tir = tir + to.linestride, ir = ir
					+ linestride)
			{
				int ti, i;
				for (s = 0, ti = tir, i = ir; s < to.width; s++, ti = ti + to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = element1Scale * (val[i] - element1Value) * (val[i] - element1Value) + element2Scale
							* (val[i + 1] - element2Value) * (val[i + 1] - element2Value) + element2Scale
							* (val[i + 2] - element3Value) * (val[i + 2] - element3Value);
				}
			}
		}
		else if (type == TYPE_1D && data instanceof boolean[])
		{
			// boolean
			int s, t;
			final boolean fval = (element1Value > 0.5f) ? true : false;
			final boolean val[] = (boolean[]) data;
			int tir, ir;
			for (t = 0, tir = to.offset, ir = offset; t < to.height; t++, tir = tir + to.linestride, ir = ir
					+ linestride)
			{
				int ti, i;
				for (s = 0, ti = tir, i = ir; s < to.width; s++, ti = ti + to.pixelstep, i = i + pixelstep)
				{
					rdata[ti] = (val[i] == fval) ? 0 : element1Scale;
				}
			}
		}
		else
		{
			throw new UnsupportedFormatException("getDistance2 not supported on " + this.toString());
		}
		return to;
	}

	/**
	 * get float pixel sum
	 */
	public float getFloatSum() throws UnsupportedFormatException
	{
		final Frame f = this.getInFormat(Frame.TYPE_1D, 1, Float.TYPE);
		float sum = 0;
		final float val[] = (float[]) f.data;
		int s, t;
		int fir;
		for (t = 0, fir = f.offset; t < f.height; t++, fir = fir + f.linestride)
		{
			int fi;
			for (s = 0, fi = fir; s < f.width; s++, fi = fi + f.pixelstep)
			{
				sum = sum + val[fi];
			}
		}
		return sum;
	}

	/**
	 * get as buffered image of histogram
	 */
	public BufferedImage getHistogramBufferedImage() throws UnsupportedFormatException
	{
		final int w = 800;
		final int h = 600;
		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		// min & max
		float min = 0;
		float max = 0;
		if (data instanceof float[])
		{
			// float 1D
			int s, t;
			final float val[] = (float[]) data;
			int ir;
			for (t = 0, ir = offset; t < height; t++, ir = ir + linestride)
			{
				int i;
				for (s = 0, i = ir; s < width; s++, i = i + pixelstep)
				{
					for (int c = 0; c < elementsPerPixel && c < 3; c++)
					{
						if ((s == 0 && t == 0) || min > val[i + c])
						{
							min = val[i + c];
						}
						if ((s == 0 && t == 0) || max < val[i + c])
						{
							max = val[i + c];
						}
					}
				}
			}
		}
		else
		{
			throw new UnsupportedFormatException("getHistogramImage for " + this.toString());
		}

		System.out.println("min = " + min);
		// if (min>0)
		// min = 0;
		System.out.println("max = " + max);
		// if (max<1)
		// max = 1;
		if (max == min)
		{
			max = 1;
		}

		final int count[][] = new int[w][3];

		int zeroCount = 0;
		final float sum[] = new float[3];
		final float sum2[] = new float[3];
		int num = 0;
		if (data instanceof float[])
		{
			// float 1D
			int s, t;
			final float val[] = (float[]) data;
			int ir;
			for (t = 0, ir = offset; t < height; t++, ir = ir + linestride)
			{
				int i;
				for (s = 0, i = ir; s < width; s++, i = i + pixelstep)
				{
					if (val[i + 0] == 0
							&& (elementsPerPixel < 2 || (val[i + 1] == 0 && (elementsPerPixel < 3 || val[i + 2] == 0))))
					{
						zeroCount++;
					}
					else
					{
						num++;
						for (int c = 0; c < elementsPerPixel && c < 3; c++)
						{
							final int bin = (int) ((w - 0.001f) * (val[i + c] - min) / (max - min));
							count[bin][c]++;
							sum[c] = sum[c] + val[i + c];
							sum2[c] = sum2[c] + val[i + c] * val[i + c];
						}
					}
				}
			}
		}

		// max count
		/*
		 * int zeroBin = (int)((w-0.001f)*(0-min)/(max-min)); int zeroCount [] = new int[3]; if
		 * (zeroBin>=0 && zeroBin<w) { for (int c=0; c<elementsPerPixel && c<3; c++) { zeroCount[c]
		 * = count[zeroBin][c]; count[zeroBin][c] = 0; } }
		 */
		int maxCount = 0;
		for (int bin = 0; bin < w; bin++)
		{
			for (int c = 0; c < elementsPerPixel && c < 3; c++)
			{
				if (maxCount < count[bin][c])
				{
					maxCount = count[bin][c];
				}
			}
		}
		System.out.println("maxCount=" + maxCount);

		// draw
		final int ys[] = new int[3];
		int color;
		for (int bin = 0; bin < w; bin++)
		{
			for (int c = 0; c < elementsPerPixel && c < 3; c++)
			{
				ys[c] = (int) ((h - 0.001f) * count[bin][c] / maxCount);
			}

			for (int y = 0; y < h; y++)
			{
				color = ((ys[0] >= y) ? (0xff << RED_SHIFT) : 0) | ((ys[1] >= y) ? (0xff << GREEN_SHIFT) : 0)
						| ((ys[2] >= y) ? (0xff << BLUE_SHIFT) : 0);

				image.setRGB(bin, h - 1 - y, color);
			}
		}
		final Graphics g = image.getGraphics();
		g.drawString(this.toString(), 10, 20);
		g.drawString("min " + min, 10, 40);
		g.drawString("max " + max, 10, 60);
		g.drawString("maxCount " + maxCount, 10, 80);
		g.drawString("zeroCount " + zeroCount, 10, 100);
		final float mean[] = new float[] { sum[0] / num, sum[1] / num, sum[2] / num };
		g.drawString("means " + (mean[0]) + "," + mean[1] + "," + mean[2], 10, 120);
		g.drawString(	"SDs " + Math.sqrt((sum2[0] - mean[0] * mean[0]) / num) + ","
								+ Math.sqrt((sum2[1] - mean[1] * mean[1]) / num) + ","
								+ Math.sqrt((sum2[2] - mean[2] * mean[2]) / num), 10, 140);

		return image;
	}

	/**
	 * get in alternative format; may return this if format matches; otherwise will be have new data
	 * (typically packed) array. Currently supports packed RGB (in int) -> float, packed RGB ->
	 * boolean, float -> boolean.
	 */
	public Frame getInFormat(final int ftype, final int felementsPerPixel, final Class dataClass)
			throws UnsupportedFormatException
	{
		if (ftype == this.type && felementsPerPixel == this.elementsPerPixel
				&& dataClass.equals(data.getClass().getComponentType())) { return this; }

		// special case of YUV float -> float = change pixel step
		if (ftype == TYPE_1D && felementsPerPixel == 1 && type == TYPE_YUV && elementsPerPixel == 3
				&& dataClass.equals(data.getClass().getComponentType()))
		{
			final Frame f = new Frame(ftype, felementsPerPixel, width, height, 0, elementsPerPixel, linestride, data);
			return f;
		}

		final int datasize = width * height * felementsPerPixel;
		final Object fdata = java.lang.reflect.Array.newInstance(dataClass, datasize);
		final Frame f = new Frame(ftype, felementsPerPixel, width, height, 0, felementsPerPixel, felementsPerPixel
				* width, fdata);

		if (f.data instanceof boolean[] && f.type == TYPE_1D)
		{
			if (this.data.getClass().getComponentType().equals(Integer.TYPE) && type == TYPE_RGB
					&& elementsPerPixel == 1)
			{
				final Frame ff = new Frame(ftype, felementsPerPixel, width, height, 0, felementsPerPixel,
						felementsPerPixel * width, new float[datasize]);
				convertPackedRGBToFloat(this, ff);
				convertFloatToBoolean(ff, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_YUV
					&& elementsPerPixel == 3)
			{
				final Frame ff = new Frame(ftype, felementsPerPixel, width, height, 0, felementsPerPixel,
						felementsPerPixel * width, new float[datasize]);
				convertYUVToFloat(this, ff);
				convertFloatToBoolean(ff, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_1D)
			{
				convertFloatToBoolean(this, f);
				return f;
			}

		}
		else if (f.data instanceof float[] && f.type == TYPE_1D)
		{
			if (this.data.getClass().getComponentType().equals(Integer.TYPE) && type == TYPE_RGB
					&& elementsPerPixel == 1)
			{
				convertPackedRGBToFloat(this, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_YUV
					&& elementsPerPixel == 3)
			{
				convertYUVToFloat(this, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Boolean.TYPE) && type == TYPE_1D)
			{
				convertBooleanToFloat(this, f);
				return f;
			}
		}
		else if (f.data instanceof int[] && f.type == TYPE_RGB && f.elementsPerPixel == 1)
		{
			if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_YUV && elementsPerPixel == 3)
			{
				convertYUVToPackedRGB(this, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Boolean.TYPE) && type == TYPE_1D)
			{
				final Frame ff = new Frame(ftype, felementsPerPixel, width, height, 0, felementsPerPixel,
						felementsPerPixel * width, new float[datasize]);
				convertBooleanToFloat(this, ff);
				convertFloatToPackedRGB(ff, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_1D)
			{
				convertFloatToPackedRGB(this, f);
				return f;
			}
		}
		else if (f.data instanceof float[] && f.type == TYPE_YUV && f.elementsPerPixel == 3)
		{
			if (this.data.getClass().getComponentType().equals(Integer.TYPE) && type == TYPE_RGB
					&& elementsPerPixel == 1)
			{
				convertPackedRGBToYUV(this, f);
				return f;
			}
			if (this.data.getClass().getComponentType().equals(Boolean.TYPE) && type == TYPE_1D)
			{
				final Frame ff = new Frame(ftype, felementsPerPixel, width, height, 0, felementsPerPixel,
						felementsPerPixel * width, new float[datasize]);
				convertBooleanToFloat(this, ff);
				convertFloatToYUV(ff, f);
				return f;
			}
			else if (this.data.getClass().getComponentType().equals(Float.TYPE) && type == TYPE_1D)
			{
				convertFloatToYUV(this, f);
				return f;
			}
		}
		else
		{
			throw new UnsupportedFormatException("to " + f + " from " + this);
		}
		return f;
	}

	/**
	 * get in packed format, i.e. pixelStep = elementsPerPixel; may return this if already OK
	 */
	public Frame getPacked() throws UnsupportedFormatException
	{
		if (elementsPerPixel == pixelstep) { return this; }
		final int datasize = width * height * elementsPerPixel;
		final Object fdata = java.lang.reflect.Array.newInstance(data.getClass().getComponentType(), datasize);
		final Frame f = new Frame(type, elementsPerPixel, width, height, 0, elementsPerPixel, width, fdata);
		if (data instanceof boolean[])
		{
			final Frame from = this;
			final Frame to = f;
			final boolean[] fb = (boolean[]) from.data;
			final boolean[] tb = (boolean[]) to.data;
			int s, t;
			int fir, tir;
			for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
					+ to.linestride)
			{
				int fi, ti;
				for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
				{
					int ei;
					for (ei = 0; ei < elementsPerPixel; ei++)
					{
						tb[ti + ei] = fb[fi + ei];
					}
				}
			}
		}
		else if (data instanceof float[])
		{
			final Frame from = this;
			final Frame to = f;
			final float[] fb = (float[]) from.data;
			final float[] tb = (float[]) to.data;
			int s, t;
			int fir, tir;
			for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
					+ to.linestride)
			{
				int fi, ti;
				for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
				{
					int ei;
					for (ei = 0; ei < elementsPerPixel; ei++)
					{
						tb[ti + ei] = fb[fi + ei];
					}
				}
			}
		}
		else if (data instanceof int[])
		{
			final Frame from = this;
			final Frame to = f;
			final int[] fb = (int[]) from.data;
			final int[] tb = (int[]) to.data;
			int s, t;
			int fir, tir;
			for (t = 0, fir = from.offset, tir = to.offset; t < from.height; t++, fir = fir + from.linestride, tir = tir
					+ to.linestride)
			{
				int fi, ti;
				for (s = 0, fi = fir, ti = tir; s < from.width; s++, fi = fi + from.pixelstep, ti = ti + to.pixelstep)
				{
					int ei;
					for (ei = 0; ei < elementsPerPixel; ei++)
					{
						tb[ti + ei] = fb[fi + ei];
					}
				}
			}
		}
		else
		{
			throw new UnsupportedFormatException("Pack " + this);
		}
		return f;
	}

	/**
	 * get subregion; refers to same data array
	 */
	public Frame getRegion(int x, int y, int w, int h)
	{
		if (x > width)
		{
			x = width;
		}
		if (x + w > width)
		{
			w = width - x;
		}
		if (y > height)
		{
			y = height;
		}
		if (y + h > height)
		{
			h = height - y;
		}
		final Frame f = new Frame(type, elementsPerPixel, w, h, offset + pixelstep * x + linestride * y, pixelstep,
				linestride, data);
		return f;
	}

	/**
	 * get sub-sampled; refers to same data array
	 */
	public Frame getSubsampled(final int xstep, final int ystep)
	{
		final Frame f = new Frame(type, elementsPerPixel, width / xstep, height / ystep, offset, pixelstep * xstep,
				linestride * ystep, data);
		return f;
	}

	/**
	 * get true pixel count
	 */
	public int getTrueCount() throws UnsupportedFormatException
	{
		final Frame f = this.getInFormat(Frame.TYPE_1D, 1, Boolean.TYPE);
		int count = 0;
		final boolean b[] = (boolean[]) f.data;
		int s, t;
		int fir;
		for (t = 0, fir = f.offset; t < f.height; t++, fir = fir + f.linestride)
		{
			int fi;
			for (s = 0, fi = fir; s < f.width; s++, fi = fi + f.pixelstep)
			{
				if (b[fi])
				{
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * square blur - only for float[] at the mo
	 */
	public Frame meanFilter(final int radius) throws UnsupportedFormatException
	{
		Frame from = this;
		if (type == TYPE_RGB && elementsPerPixel == 1)
		{
			from = this.getInFormat(TYPE_YUV, 3, Float.TYPE);
		}
		else if (!(data instanceof float[]))
		{
			from = this.getInFormat(this.type, this.elementsPerPixel, Float.TYPE);
		}

		final int size = (radius * 2 + 1) * (radius * 2 + 1);
		// float
		final int datasize = (width - 2 * radius) * (height - 2 * radius) * from.elementsPerPixel;
		final float bout[] = new float[datasize];
		final Frame to = new Frame(from.type, from.elementsPerPixel, width - 2 * radius, height - 2 * radius, 0,
				from.elementsPerPixel, from.elementsPerPixel * (width - 2 * radius), bout);
		// boolean
		int s, t;
		final float tval[] = (float[]) to.data;
		final float val[] = (float[]) from.data;
		int tir, fir;
		for (t = 0, tir = to.offset, fir = from.offset; t < to.height; t++, tir = tir + to.linestride, fir = fir
				+ from.linestride)
		{
			int ti, fi;
			for (s = 0, ti = tir, fi = fir; s < to.width; s++, ti = ti + to.pixelstep, fi = fi + from.pixelstep)
			{
				for (int c = 0; c < from.elementsPerPixel; c++)
				{
					float sum = 0;
					int fiir = fi;
					for (int dy = (-radius); dy <= radius; dy++, fiir = fiir + from.linestride)
					{
						int fii = fiir;
						for (int dx = (-radius); dx <= radius; dx++, fii = fii + from.pixelstep)
						{
							sum = sum + val[fii + c];
						}
					}
					val[ti + c] = sum / size;
				}
			}
		}
		return to;
	}

	/**
	 * median filter - only defined on float[1] and boolean[1] at the mo; default fraction 0.5
	 */
	public Frame medianFilter(final int radius) throws UnsupportedFormatException
	{
		return medianFilter(radius, 0.5f);
	}

	/**
	 * median filter - only defined on boolean[1] at the mo
	 */
	public Frame medianFilter(final int radius, final float fraction) throws UnsupportedFormatException
	{
		final Frame from = this.getInFormat(TYPE_1D, 1, Boolean.TYPE);

		final int minCount = (int) ((radius * 2 + 1) * (radius * 2 + 1) * fraction);
		// boolean
		final int datasize = (width - 2 * radius) * (height - 2 * radius);
		final boolean bout[] = new boolean[datasize];
		final Frame to = new Frame(TYPE_1D, 1, width - 2 * radius, height - 2 * radius, 0, 1, width - 2 * radius, bout);
		// boolean
		int s, t;
		final boolean tval[] = (boolean[]) to.data;
		final boolean val[] = (boolean[]) from.data;
		int tir, fir;
		for (t = 0, tir = to.offset, fir = from.offset; t < to.height; t++, tir = tir + to.linestride, fir = fir
				+ from.linestride)
		{
			int ti, fi;
			for (s = 0, ti = tir, fi = fir; s < to.width; s++, ti = ti + to.pixelstep, fi = fi + from.pixelstep)
			{
				int count = 0;
				int fiir = fi;
				for (int dy = (-radius); dy <= radius; dy++, fiir = fiir + from.linestride)
				{
					int fii = fiir;
					for (int dx = (-radius); dx <= radius; dx++, fii = fii + from.pixelstep)
					{
						if (val[fii])
						{
							count++;
						}
					}
				}
				tval[ti] = (count > minCount);
			}
		}
		return to;
	}

	/**
	 * to string
	 */
	@Override
	public String toString()
	{
		final StringBuffer b = new StringBuffer();
		b.append("Format[type=");
		b.append(type == TYPE_1D ? "1D" : (type == TYPE_RGB ? "RGB" : (type == TYPE_YUV ? "YUV" : new Integer(type)
				.toString())));
		b.append(", elementsPerPixel=");
		b.append(new Integer(elementsPerPixel).toString());
		b.append(", size=");
		b.append(new Integer(width).toString());
		b.append("x");
		b.append(new Integer(height).toString());
		b.append(", offset=");
		b.append(new Integer(offset).toString());
		b.append(", pixelstep=");
		b.append(new Integer(pixelstep).toString());
		b.append(", linestride=");
		b.append(new Integer(linestride).toString());
		b.append(", class=");
		b.append(data.getClass().getComponentType().getName());
		b.append(", datasize=");
		b.append(new Integer(java.lang.reflect.Array.getLength(data)).toString());
		b.append("]");
		return b.toString();
	}
}
