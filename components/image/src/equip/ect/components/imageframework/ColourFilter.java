package equip.ect.components.imageframework;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ColourFilter extends AbstractFilter
{
	private Color colour;
	private float mix = 0;

	public ColourFilter(final Color mixColor)
	{
		this.colour = mixColor;
	}

	@Override
	public BufferedImage filter(final BufferedImage src, final BufferedImage dst)
	{
		if (mix == 0) { return src; }
		BufferedImage result = dst;
		if (result == null)
		{
			result = createCompatibleDestImage(src, null);
		}

		final int width = src.getWidth();
		final int height = src.getHeight();

		final int[] pixels = new int[width * height];
		getRGB(src, 0, 0, width, height, pixels);
		mixColor(pixels);
		setRGB(result, 0, 0, width, height, pixels);
		return result;
	}

	public Color getColour()
	{
		return colour;
	}

	public float getMixValue()
	{
		return mix;
	}

	public void setColour(final Color colour)
	{
		this.colour = colour;
	}

	public void setMixValue(final float mixValue)
	{
		if (mixValue < 0.0f)
		{
			this.mix = 0.0f;
			return;
		}
		else if (mixValue > 1.0f)
		{
			this.mix = 1.0f;
			return;
		}
		this.mix = mixValue;
	}

	private void mixColor(final int[] inPixels)
	{
		final int mix_r = colour.getRed();
		final int mix_b = colour.getBlue();
		final int mix_g = colour.getGreen();

		for (int i = 0; i < inPixels.length; i++)
		{
			final int argb = inPixels[i];

			final int a = argb & 0xFF000000;
			int r = (argb >> 16) & 0xFF;
			int g = (argb >> 8) & 0xFF;
			int b = (argb) & 0xFF;
			r = (int) (r * (1.0f - mix) + mix_r * mix);
			g = (int) (g * (1.0f - mix) + mix_g * mix);
			b = (int) (b * (1.0f - mix) + mix_b * mix);

			inPixels[i] = a << 24 | r << 16 | g << 8 | b;
		}
	}
}