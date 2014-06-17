package equip.ect.components.webbrowser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class SwingBrowserComponent implements Serializable
{
	// one static shared frame
	SwingBrowserFrame frame;

	boolean displayContent = false;
	String content = "";

	static final String DEFAULT_DOCUMENT_LOCATION = "c:/inScape/ect/resources/arauthoring/documents/default.html";

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SwingBrowserComponent()
	{
		// the single, shared frame used by all instances of this
		// components

		frame = SwingBrowserFrame.getFrameReference();
		frame.registerComponent(this);
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getContent()
	{
		return content;
	}

	public boolean getDisplayContent()
	{
		return displayContent;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setContent(final String newValue)
	{
		final String oldValue = content;
		content = newValue;

		propertyChangeListeners.firePropertyChange("content", oldValue, newValue);

		frame.submitContentChange(this, content);
	}

	public void setDisplayContent(final boolean newValue)
	{
		final boolean oldValue = displayContent;
		displayContent = newValue;

		propertyChangeListeners.firePropertyChange("displayContent", oldValue, newValue);
		frame.submitVisibilityChange(this, displayContent);

	}

	public void stop()
	{
		frame.deRegisterComponent(this);
		frame = null;
	}
}
