/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package equip.ect.components.imageframework;

import java.awt.image.BufferedImage;

public class BoxBlurFilter extends AbstractFilter
{

	public static void blur(final int[] in, final int[] out, final int width, final int height, final int radius)
	{
		final int widthMinus1 = width - 1;
		final int tableSize = 2 * radius + 1;
		final int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
		{
			divide[i] = i / tableSize;
		}

		int inIndex = 0;

		for (int y = 0; y < height; y++)
		{
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -radius; i <= radius; i++)
			{
				final int rgb = in[inIndex + ImageMath.clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++)
			{
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

				int i1 = x + radius + 1;
				if (i1 > widthMinus1)
				{
					i1 = widthMinus1;
				}
				int i2 = x - radius;
				if (i2 < 0)
				{
					i2 = 0;
				}
				final int rgb1 = in[inIndex + i1];
				final int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	private int hRadius;
	private int vRadius;

	private int iterations = 1;

	@Override
	public BufferedImage filter(final BufferedImage src, final BufferedImage dst)
	{
		final int width = src.getWidth();
		final int height = src.getHeight();

		BufferedImage result = dst;
		if (result == null)
		{
			result = createCompatibleDestImage(src, null);
		}

		final int[] inPixels = new int[width * height];
		final int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);

		for (int i = 0; i < iterations; i++)
		{
			blur(inPixels, outPixels, width, height, hRadius);
			blur(outPixels, inPixels, height, width, vRadius);
		}

		setRGB(result, 0, 0, width, height, inPixels);
		return result;
	}

	public int getHRadius()
	{
		return hRadius;
	}

	public int getIterations()
	{
		return iterations;
	}

	public int getRadius()
	{
		return hRadius;
	}

	public int getVRadius()
	{
		return vRadius;
	}

	public void setHRadius(final int hRadius)
	{
		this.hRadius = hRadius;
	}

	public void setIterations(final int iterations)
	{
		this.iterations = iterations;
	}

	public void setRadius(final int radius)
	{
		this.hRadius = this.vRadius = radius;
	}

	public void setVRadius(final int vRadius)
	{
		this.vRadius = vRadius;
	}

	@Override
	public String toString()
	{
		return "Blur/Box Blur...";
	}
}
