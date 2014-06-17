package equip.ect.components.imageframework;

public abstract class ImageSink extends ImageSource implements ImageUpdateListener
{
	protected ImageUpdateHandler inputImage;

	@Override
	public ImageUpdateHandler getImage()
	{
		return outputImage;
	}

	public void setImage(final ImageUpdateHandler updateHandler)
	{
		if (inputImage != null)
		{
			inputImage.removeImageUpdateListener(this);
		}
		inputImage = updateHandler;
		if (inputImage != null)
		{
			inputImage.addImageUpdateListener(this);
		}
	}
}
