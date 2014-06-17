package equip.ect.components.webbrowser;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.LogManager;

import org.xml.sax.SAXException;

/**
 * A component that can be used to display a web-page, using the Cobra pure-java web-browser. Very
 * much hard-coded for inscape.
 */

public class CobraBrowser implements Serializable
{
	class URLRunnable implements Runnable
	{
		CobraFrame frame;
		String url;

		URLRunnable(final String url, final CobraFrame frame)
		{
			this.frame = frame;
			this.url = url;
		}

		@Override
		public void run()
		{
			try
			{
				if (url == null)
				{
					frame.processURL(null);
				}
				else
				{
					frame.processURL(url);
				}
			}
			catch (final MalformedURLException e)
			{
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			catch (final SAXException e)
			{
				e.printStackTrace();
			}
		}
	}

	boolean displayContent = false;

	String content = "";

	CobraFrame frame;

	static final String DEFAULT_LOGGER_URL = "http://www.mrl.nott.ac.uk/~sre/configuration/cobra/logging.properties";

	String configLogger;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public CobraBrowser()
	{
		setConfigLogger(DEFAULT_LOGGER_URL);
		frame = CobraFrame.getSingleFrame();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getConfigLogger()
	{
		return configLogger;
	}

	public String getContent()
	{
		return content;
	}

	public boolean getDisplayContent()
	{
		return displayContent;
	}

	public synchronized void reDisplay()
	{

		// work out what to put on the back of the html panel
		// possiblities are:
		// 1. nothing
		// 2. a home-page (whose URL is specified in the "backdrop" property
		// 3. an arbitrary web-page (whose URL is specified in the "content" property
		// note using Cobra to display web-pages, which has some limitations in
		// terms of the content it can display

		// always use a thread - as web-pages can take some time to download

		if (displayContent == true)
		{
			if ((content == null) || (content.equals("")))
			{
				startURLRunnable(null);
				return;
			}
			else
			{
				startURLRunnable(content);
				return;
			}
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setConfigLogger(final String newValue)
	{
		final String oldValue = configLogger;
		configLogger = newValue;

		propertyChangeListeners.firePropertyChange("configLogger", oldValue, newValue);

		// now use this URL to try and load new configuration
		// for the loggers used by cobra

		// note cobra does a LOT of logging. So this
		// property might be used to turn this off for performance
		// reasons

		reConfigureLogger(newValue);
	}

	public void setContent(final String newValue)
	{
		final String oldValue = content;
		content = newValue;

		propertyChangeListeners.firePropertyChange("content", oldValue, newValue);
		reDisplay();
	}

	public void setDisplayContent(final boolean newValue)
	{
		final boolean oldValue = displayContent;
		displayContent = newValue;

		propertyChangeListeners.firePropertyChange("displayContent", oldValue, newValue);

		reDisplay();
	}

	void reConfigureLogger(final String urlString)
	{
		try
		{
			final URL url = new URL(urlString);
			final InputStream is = url.openStream();

			LogManager.getLogManager().readConfiguration(is);

		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

	}

	void startURLRunnable(final String url)
	{
		(new Thread(new URLRunnable(url, frame))).start();
	}
}
