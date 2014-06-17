package equip.ect.components.uploadtohttp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import equip.ect.ContainerManagerHelper;

// uses container's internal web-server
// to make text available over http

public class UploadToHTTP implements Serializable
{
	File httpDir;
	File currentFile;

	String text;

	String fileName;
	String location;
	String attention;
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public UploadToHTTP()
	{
		httpDir = ContainerManagerHelper.getHttpDirectory();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getAttention()
	{
		return attention;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getLocation()
	{
		return location;
	}

	public String getText()
	{
		return text;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setAttention(final String newInput)
	{
		final String oldInput = this.attention;
		this.attention = newInput;

		propertyChangeListeners.firePropertyChange("attention", oldInput, newInput);
	}

	public void setFileName(final String newInput)
	{
		if ((fileName == null) || (!(newInput.equals(fileName))))
		{
			final String oldInput = this.fileName;
			this.fileName = newInput;

			// first, tidy up any file that we have already opened

			if ((currentFile != null) && (currentFile.exists()))
			{
				currentFile.delete();
			}

			// now try and create new file to write to

			currentFile = new File(httpDir.getAbsolutePath() + "/" + fileName);

			// empty out any contents of this file if it already
			// exists (eg a file with the same name might
			// have coincidentally been put there by a user)

			if (currentFile.exists())
			{
				currentFile.delete();
			}

			try
			{
				currentFile.createNewFile();

				final URL url = ContainerManagerHelper.uploadToHttp(currentFile);
				setLocation(url.toString());
				setAttention("File created successfully");
			}
			catch (final IOException e)
			{
				setAttention("Unable to create file with this name");
				this.fileName = null;
				setLocation(null);
				currentFile = null;
			}

			propertyChangeListeners.firePropertyChange("fileName", oldInput, this.fileName);

			updateFile();
		}
	}

	public void setText(final String newInput)
	{
		final String oldInput = this.text;
		this.text = newInput;

		propertyChangeListeners.firePropertyChange("text", oldInput, newInput);

		updateFile();
	}

	void updateFile()
	{
		if ((currentFile != null) && (text != null))
		{

			try
			{
				final FileWriter fw = new FileWriter(currentFile);
				final BufferedWriter bw = new BufferedWriter(fw);

				bw.write(text);
				bw.flush();

				bw.close();

				setAttention("Written text to file successfully");

			}
			catch (final IOException e)
			{
				setAttention("Unable to open file for writing");
			}
		}
	}

	protected void setLocation(final String newInput)
	{
		final String oldInput = this.location;
		this.location = newInput;

		propertyChangeListeners.firePropertyChange("location", oldInput, newInput);
	}
}
