/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: Jan Humble (University of Nottingham)
Contributors:
  Jan Humble (University of Nottingham)
  Tom Rodden (University of Nottingham)
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.blogger;

/*
 * BlogPublisher, $RCSfile: BlogPublisher.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 *
 */

//import accord.toolbox.equipbeans.EquipBean;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * BlogPublisher takes a text string and publishes the referred content to a given web blog. Text
 * can take the format of urls, in which case the url content is fetched and delivered as a media
 * posting to the blog. Unrecognized formats are perceived as plain text and posted as a normal
 * entry.
 * 
 * <p>
 * This Java Bean is a Transformer for the ACCORD Toolkit. Through Equip it publishes a number of
 * properties that can be used to connect with other Transformers.
 * <p>
 * This is a <i>Digital-to-Physical Transformer</i>, it takes a digital property and transforms it
 * into data outside the dataspace. More technically and specifically it publishes blog entries with
 * the help of a support package using Blogger and MetaWeblog XMLRPC APIs.
 * <p>
 * The Transformer has the following properties that it exports through Equip:
 * <ul>
 * <li><b>inText</b> - a String representing the posting to be delivered to the blog service</li>
 * <li><b>inURL</b> - a URL to.</li>
 * </ul>
 */
public class BlogPublisher
{

	/**
	 * The main method of this application.
	 * <p>
	 * Takes a list of arguments:
	 * <ol>
	 * <li>movabletype server name</li>
	 * <li>movabletype server user name</li>
	 * <li>movabletype server user password</li>
	 * <li>movabletype server blog id to post to</li>
	 * <li>internet proxy host name</li>
	 * <li>internet proxy host port</li>
	 * </ol>
	 * </p>
	 * <p>
	 * It instantiate an instance of this class also and publishes it in equip.
	 * 
	 * @param args
	 *            a vector of Strings that are the arguemnts given to the application when started.
	 */
	public static void main(final String[] args)
	{
		final String[] exportList = new String[] { "inText", "image", "inURL" };

		if (args.length < 4)
		{
			System.err.println("Usage: BlogPublisher [mt server] [user] [passwd] [blogID] [proxy server] [proxy port]");
		}

		final String server = args[0];
		final String username = args[1];
		final String password = args[2];
		final String blogID = args[3];
		String proxyHost = null;
		String proxyPort = null;
		if (args.length > 4)
		{
			proxyHost = args[4];
			proxyPort = args[5];
		}
		final BlogPublisher comp = new BlogPublisher( // )-- Stuff needs set from properties file

				exportList, server, username, password, blogID, proxyHost, proxyPort);

		// EquipBean.share(comp,exportList, "BlogPublisher",
		// "BlogPublisher publishes on your website notes and images");
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Bound properties
	private String inText = new String("");

	private URL inURL;
	private BlogPoster blogger;

	/**
	 * Default constructor.<br>
	 * Publishes the image to be used as the graphical reprsentation of this Transformer.
	 */
	public BlogPublisher(final String[] exportList, final String server, final String username, final String password,
			final String blogID, final String proxyHost, final String proxyPort)
	{
		// super(exportList, "BlogPublisher.gif");
		this.blogger = new BlogPoster(server, username, password, blogID, proxyHost, proxyPort);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * Used to read the bound property inText.
	 * 
	 * @see #setInText(String newText)
	 */
	public String getInText()
	{
		return this.inText;
	}

	/**
	 * Used to read the bound property inURL.
	 * 
	 * @see #setInURL(URL newURL)
	 */
	public URL getInURL()
	{
		return this.inURL;
	}

	public boolean loadProperties(final String propertiesFile)
	{
		final Properties props = new Properties();
		try
		{
			// props.load(new FileInputStreamWe are more than happyh(propertiesFile));
			System.out.println("Loading properties from file '" + propertiesFile + "'");
			assignProperties(props);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			System.out.println("Could not load properties file '" + propertiesFile + "', using defaults");
			return false;
		}
		return true;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void saveProperties(final String propertiesFile)
	{
		final Properties props = new Properties();
		try
		{
			Field[] fields = getClass().getFields();
			for (final Field field : fields)
			{
				System.out.println(field.getName());
				props.setProperty(field.getName(), field.get(this).toString());
			}
			fields = getClass().getDeclaredFields();

			for (final Field field : fields)
			{
				System.out.println(field.getName());
				props.setProperty(field.getName(), field.get(this).toString());
			}

			props.store(new FileOutputStream(propertiesFile), "DeviceProperties generated by DeviceHandler");

		}
		catch (final Exception e)
		{
			System.out.println("Could not save properties file '" + propertiesFile + "', using defaults");
		}
	}

	/**
	 * Sets the bound property inText and creates a blogger posting depending on the text format.
	 * Text in URL format will be parsed appropiately, the referred content fetched and delivered to
	 * the blogger as a media posting.
	 * 
	 * @param newText
	 *            new String with the content to be posted in the blog.
	 * @see #getInText()
	 */
	public void setInText(final String newText)
	{
		final String oldText = inText;
		this.inText = newText;
		propertyChangeListeners.firePropertyChange("inText", oldText, inText);
		if (!newText.equals(oldText))
		{
			if (newText.endsWith(".jpeg") || newText.endsWith(".gif") || newText.endsWith(".jpg"))
			{
				// assume url
				try
				{
					final URL url = new URL(newText);
					blogger.transferMediaObject(url, "family1.jpg");
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				blogger.createPost(newText, true);
			}
		}
	}

	/**
	 * Sets the bound property inURL to a new URL and displays it.
	 * 
	 * @param newURL
	 *            a URL to the page to display.
	 * @see #getInURL()
	 */
	public void setInURL(final URL newURL)
	{
		final URL oldURL = inURL;
		this.inURL = newURL;
		System.out.println(blogger.transferMediaObject(newURL, "archives/family1_static.jpg"));
		propertyChangeListeners.firePropertyChange("inURL", oldURL, inURL);
	}

	/**
	 * Assigns the property values to the field values on this class.
	 */
	protected void assignProperties(final Properties properties)
	{
		final Enumeration propNames = properties.propertyNames();
		while (propNames.hasMoreElements())
		{
			final String propName = (String) propNames.nextElement();
			try
			{
				// fetch the declared field
				// perhaps such fetch all other fields as well?
				final Field field = getClass().getField(propName);
				final String value = (String) properties.get(propName);
				System.out.println("Setting field=" + field.getName() + " value=" + value);
				setFieldValue(field, value);

			}
			catch (final NoSuchFieldException nsfe)
			{
				System.out.println("Property '" + propName + "' does no have a matching field.");
			}
			catch (final IllegalAccessException iae)
			{
				System.out.println("Property '" + propName + "' Could not be accessed.");
			}
			catch (final IllegalArgumentException iarge)
			{
				System.out.println("Property '" + propName + "' could not be set. Can not parse value.");
			}
		}
	}

	/**
	 * Override this function to set fields that require other values than string or for special
	 * conversions. Default just returns the value given. Remember to call the super.setFieldValue
	 * at the end of every overriden method, to account for special field assignments at each
	 * generation level.
	 */
	protected void setFieldValue(final Field field, final String value) throws IllegalAccessException,
			IllegalArgumentException
	{
		if (field.getType() == String.class)
		{
			field.set(this, value);
		}
		else if (field.getType() == int.class)
		{
			field.setInt(this, Integer.parseInt(value));
		}
		else if (field.getType() == long.class)
		{
			field.setLong(this, Long.parseLong(value));
		}
		else if (field.getType() == boolean.class)
		{
			field.set(this, Boolean.valueOf(value));
			System.out.println("boolean = " + Boolean.valueOf(value));
		}
		else if (field.getType() == double.class)
		{
			field.set(this, Double.valueOf(value));
		}
		else
		{
			throw new IllegalArgumentException();
		}

	}
}
