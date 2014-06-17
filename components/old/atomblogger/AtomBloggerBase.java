package equip.ect.components.atomblogger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class AtomBloggerBase implements Serializable
{
	// Business End
	AtomBloggerLogic abl = new AtomBloggerLogic();

	String atomPostEndpoint = "https://www.blogger.com/atom/7117113";
	String username = "curioushome";
	String password = "ambient";
	String attention = "";

	transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public AtomBloggerBase()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public synchronized String getAtomPostEndpoint()
	{
		return atomPostEndpoint;
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	public synchronized String getPassword()
	{
		return password;
	}

	public synchronized String getUsername()
	{
		return username;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public synchronized void setAtomPostEndpoint(final String newAtomPostEndpoint)
	{
		final String oldAtomPostEndpoint = this.atomPostEndpoint;
		this.atomPostEndpoint = newAtomPostEndpoint;

		propertyChangeListeners.firePropertyChange("atomPostEndpoint", oldAtomPostEndpoint, newAtomPostEndpoint);
	}

	public synchronized void setPassword(final String newPassword)
	{
		final String oldPassword = this.password;
		this.password = newPassword;

		setAttention(null);

		propertyChangeListeners.firePropertyChange("password", oldPassword, newPassword);
	}

	public synchronized void setUsername(final String newUsername)
	{
		final String oldUsername = this.username;
		this.username = newUsername;

		setAttention(null);

		propertyChangeListeners.firePropertyChange("username", oldUsername, newUsername);
	}

	public synchronized void stop()
	{
		abl = null;
	}

	void uploadEntry(final String topic, final String content)
	{
		setAttention("Trying to upload entry");

		if (atomPostEndpoint == null)
		{
			setAttention("Must provide an endpoint");
			return;
		}
		if (username == null)
		{
			setAttention("Must provide a username");
			return;
		}
		if (password == null)
		{
			setAttention("Must provide a password");
			return;
		}
		if (topic == null)
		{
			setAttention("Must provide a topic");
			return;
		}
		if (content == null)
		{
			setAttention("Must provide some content");
			return;
		}

		URL endpointURL;

		try
		{
			endpointURL = new URL(atomPostEndpoint);
		}
		catch (final MalformedURLException e)
		{
			setAttention("Not a valid endpoint");
			return;
		}

		try
		{
			abl.doPost(endpointURL, username, password, topic, content);
			setAttention("Successfully uploaded new entry");

			System.out.println("new entry uploaded");
			System.out.println("topic: " + topic);
			System.out.println("content: " + content);
		}
		catch (final UploadException e)
		{
			setAttention("Failed to upload." + e.errorCode + ":" + e.errorMessage);
		}
		catch (final IOException e)
		{
			setAttention("Failed to upload." + e.getMessage());
		}
		catch (final Exception e)
		{
			setAttention("Failed to upload for unspecified reason");
		}
	}

	protected synchronized void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}
}
