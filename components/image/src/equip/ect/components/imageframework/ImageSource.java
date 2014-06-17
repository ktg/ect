package equip.ect.components.imageframework;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ImageSource
{
	protected final ImageUpdateHandlerImpl outputImage = new ImageUpdateHandlerImpl();
	protected final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public ImageUpdateHandler getImage()
	{
		return outputImage;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}
}
