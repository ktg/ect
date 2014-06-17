package equip.ect.components.imageframework;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

public abstract class ImageFilter extends ImageSink
{
	protected BufferedImage image;

	@Override
	public void imageUpdated(final BufferedImage image)
	{
		this.image = image;
		if (image != null)
		{
			if (getImageOp() == null)
			{
				outputImage.setImage(image);
			}
			else
			{
				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						outputImage.setImage(getImageOp().filter(image, null));

					}
				}).start();

			}
		}
		else
		{
			outputImage.setImage(null);
		}
	}

	protected abstract BufferedImageOp getImageOp();
}
