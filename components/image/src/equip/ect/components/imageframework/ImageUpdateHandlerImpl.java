package equip.ect.components.imageframework;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageUpdateHandlerImpl implements ImageUpdateHandler
{
	private BufferedImage image;
	private List<ImageUpdateListener> listeners = new ArrayList<ImageUpdateListener>();

	@Override
	public void addImageUpdateListener(final ImageUpdateListener listener)
	{
		listeners.add(listener);
		listener.imageUpdated(image);
	}

	public BufferedImage getImage()
	{
		return image;
	}

	@Override
	public void removeImageUpdateListener(final ImageUpdateListener listener)
	{
		listeners.remove(listener);
		// listener.imageUpdated(null);
	}

	public void setImage(final BufferedImage image)
	{
		this.image = image;
		fireImageUpdateEvent(image);
	}

	@Override
	public String toString()
	{
		return "";
	}

	private void fireImageUpdateEvent(final BufferedImage image)
	{
		for (final ImageUpdateListener listener : listeners)
		{
			listener.imageUpdated(image);
		}
	}
}
