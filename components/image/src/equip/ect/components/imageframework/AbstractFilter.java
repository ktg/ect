/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package equip.ect.components.imageframework;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
 */
public abstract class AbstractFilter implements BufferedImageOp
{

	@Override
	public BufferedImage createCompatibleDestImage(final BufferedImage src, final ColorModel dstCM)
	{
		ColorModel colourModel = dstCM;
		if (colourModel == null)
		{
			colourModel = src.getColorModel();
		}
		return new BufferedImage(colourModel, colourModel.createCompatibleWritableRaster(	src.getWidth(),
																							src.getHeight()),
				colourModel.isAlphaPremultiplied(), null);
	}

	@Override
	public Rectangle2D getBounds2D(final BufferedImage src)
	{
		return new Rectangle(0, 0, src.getWidth(), src.getHeight());
	}

	@Override
	public Point2D getPoint2D(final Point2D srcPt, final Point2D dstPt)
	{
		Point2D result = dstPt;
		if (result == null)
		{
			result = new Point2D.Double();
		}
		result.setLocation(srcPt.getX(), srcPt.getY());
		return result;
	}

	@Override
	public RenderingHints getRenderingHints()
	{
		return null;
	}

	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the
	 * performance penalty of BufferedImage.getRGB unmanaging the image.
	 */
	public int[] getRGB(final BufferedImage image, final int x, final int y, final int width, final int height,
			final int[] pixels)
	{
		final int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB) { return (int[]) image
				.getRaster().getDataElements(x, y, width, height, pixels); }
		return image.getRGB(x, y, width, height, pixels, 0, width);
	}

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
	 */
	public void setRGB(final BufferedImage image, final int x, final int y, final int width, final int height,
			final int[] pixels)
	{
		final int type = image.getType();
		if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
		{
			image.getRaster().setDataElements(x, y, width, height, pixels);
		}
		else
		{
			image.setRGB(x, y, width, height, pixels, 0, width);
		}
	}
}
