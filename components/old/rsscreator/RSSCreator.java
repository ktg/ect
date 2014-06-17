/*
 <COPYRIGHT>

 Copyright (c) 2006, University of Nottingham
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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.rsscreator;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;
import org.gnu.stealthp.rsslib.RSSHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class RSSCreator implements Serializable
{
	protected static final String INITIAL_XML = "<?xml version=\"1.0\"";
	protected static final String INITIAL_RSS = "<rss version=\"2.0\">";
	protected static final String TERMINATOR = "\r\n";

	protected static final String TABLE_WIDTH = "80%";

	protected static final int REFRESH_PERIOD = 10;

	protected static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

	protected static final String RSS_KEY_PREFIX = "_rss.";

	protected static final String[] itemKeys = {"_rss.title", "_rss.pubDate", "_rss.description"};
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	String title;
	String description;
	String language = "en-gb";
	String outputRSS;
	String outputHTML;
	String cssLocation;
	String link;
	DictionaryImpl[] messages;
	String locationOfRSS;
	String locationOfReversedRSS;
	String locationOfHTML;
	String locationOfReversedHTML;
	String fileName;
	String encoding = "UTF-8";
	String attention;

	public RSSCreator()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getAttention()
	{
		return attention;
	}

	protected void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);

	}

	public String getCssLocation()
	{
		return cssLocation;
	}

	public void setCssLocation(final String newValue)
	{
		final String oldValue = this.cssLocation;
		this.cssLocation = newValue;

		propertyChangeListeners.firePropertyChange("cssLocation", oldValue, newValue);

		updateOutput();
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String newValue)
	{
		final String oldValue = this.description;
		this.description = newValue;

		propertyChangeListeners.firePropertyChange("description", oldValue, newValue);

		updateOutput();
	}

	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(final String newValue)
	{
		final String oldValue = this.encoding;
		this.encoding = newValue;

		propertyChangeListeners.firePropertyChange("encoding", oldValue, newValue);

		updateOutput();
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(final String newValue)
	{

		final String oldValue = this.fileName;
		this.fileName = newValue;

		propertyChangeListeners.firePropertyChange("fileName", oldValue, newValue);

		updateOutput();
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(final String newValue)
	{
		final String oldValue = this.language;
		this.language = newValue;

		propertyChangeListeners.firePropertyChange("language", oldValue, newValue);

		updateOutput();
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(final String newValue)
	{
		final String oldValue = this.link;
		this.link = newValue;

		propertyChangeListeners.firePropertyChange("link", oldValue, newValue);

		updateOutput();
	}

	public String getLocationOfHTML()
	{
		return locationOfHTML;
	}

	protected void setLocationOfHTML(final String newValue)
	{
		final String oldValue = this.locationOfHTML;
		this.locationOfHTML = newValue;

		propertyChangeListeners.firePropertyChange("locationOfHTML", oldValue, newValue);
	}

	public String getLocationOfReversedHTML()
	{
		return locationOfReversedHTML;
	}

	protected void setLocationOfReversedHTML(final String newValue)
	{
		final String oldValue = this.locationOfReversedHTML;
		this.locationOfReversedHTML = newValue;

		propertyChangeListeners.firePropertyChange("locationOfReversedHTML", oldValue, newValue);
	}

	public String getLocationOfReversedRSS()
	{
		return locationOfReversedRSS;
	}

	protected void setLocationOfReversedRSS(final String newValue)
	{
		final String oldValue = this.locationOfReversedRSS;
		this.locationOfReversedRSS = newValue;

		propertyChangeListeners.firePropertyChange("locationOfReversedRSS", oldValue, newValue);
	}

	public String getLocationOfRSS()
	{
		return locationOfRSS;
	}

	protected void setLocationOfRSS(final String newValue)
	{
		final String oldValue = this.locationOfRSS;
		this.locationOfRSS = newValue;

		propertyChangeListeners.firePropertyChange("locationOfRSS", oldValue, newValue);
	}

	public DictionaryImpl[] getMessages()
	{
		return messages;
	}

	public void setMessages(final DictionaryImpl[] newValue)
	{
		final DictionaryImpl[] oldValue = this.messages;
		this.messages = newValue;

		propertyChangeListeners.firePropertyChange("messages", oldValue, newValue);

		updateOutput();
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(final String title)
	{
		final String oldTitle = this.title;
		this.title = title;

		propertyChangeListeners.firePropertyChange("title", oldTitle, this.title);

		updateOutput();
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	protected void appendKey(final String key, final Hashtable hash, final StringBuffer sb)
	{
		final String rssKey = key.substring(RSS_KEY_PREFIX.length());

		sb.append("<" + rssKey + ">");

		final String rssText = ((StringBoxImpl) (hash.get(key))).value;
		sb.append(escape(rssText));
		sb.append("</" + rssKey + ">" + TERMINATOR);
	}

	protected void appendMessageAsHTML(final Hashtable hash, final StringBuffer sb)
	{
		final Enumeration keys = hash.keys();

		sb.append("<tr bgcolor=\"white\"><TD>" + TERMINATOR);

		final StringBoxImpl title = (StringBoxImpl) (hash.get("_rss." + RSSHandler.TITLE_TAG));
		final StringBoxImpl pubDate = (StringBoxImpl) (hash.get("_rss." + RSSHandler.PUB_DATE_TAG));
		final StringBoxImpl description = (StringBoxImpl) (hash.get("_rss." + RSSHandler.DESCRIPTION_TAG));

		sb.append("<font size=\"4\">" + escape(title.value) + "</font>");
		sb.append("<font size=\"3\"><i>  " + escape(pubDate.value) + "</i></font>");
		sb.append("<BR><font size=\"4\">" + escape(description.value) + "</font>");
	}

	protected void appendMessageAsRSS(final Hashtable hash, final StringBuffer sb, final String guid)
	{
		final Enumeration keys = hash.keys();

		sb.append("<item>" + TERMINATOR);
		sb.append("<guid isPermaLink=\"false\">" + guid + "</guid>" + TERMINATOR);

		for (final String itemKey : itemKeys)
		{
			if (hash.containsKey(itemKey))
			{
				appendKey(itemKey, hash, sb);
			}
		}

		sb.append("</item>" + TERMINATOR);
	}

	protected String constructHTMLString(final boolean reverseMessages)
	{
		final StringBuffer htmlSB = new StringBuffer();

		if (language == null)
		{
			htmlSB.append("<html>" + TERMINATOR);
		}
		else
		{
			htmlSB.append("<html lang=\"" + language + "\">" + TERMINATOR);
		}
		htmlSB.append("<head>" + TERMINATOR);

		if (encoding == null)
		{
			htmlSB.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/> ");
		}
		else
		{
			htmlSB.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding + "\"/> ");
		}

		htmlSB.append(TERMINATOR);

		htmlSB.append("<meta http-equiv=\"refresh\" content=\"" + REFRESH_PERIOD + "\">");
		htmlSB.append(TERMINATOR);
		if (title != null)
		{
			htmlSB.append("<title>" + escape(title) + "</title>" + TERMINATOR);
		}

		htmlSB.append("</head>" + TERMINATOR);

		htmlSB.append("<table width=\"" + TABLE_WIDTH + "\" bgcolor=\"#eeeeee\" border>" + TERMINATOR);

		htmlSB.append("<tr><th>");

		if (title != null)
		{
			htmlSB.append("<Font size=\"6\">" + escape(title) + "</font><BR>");
		}

		final String dateStamp = getDateStamp();

		htmlSB.append("<font size=\"4\"><i>" + dateStamp + "</i></font>" + TERMINATOR);

		// now iterate through any supplied messages, and if they
		// define rss fields, output them into this file

		if (messages != null)
		{
			if (reverseMessages == false)
			{
				for (final DictionaryImpl message : messages)
				{
					if (message != null)
					{
						processMessageHTML(htmlSB, message);
					}
				}
			}
			else
			{
				for (int i = (messages.length - 1); i >= 0; i--)
				{
					final DictionaryImpl message = messages[i];

					if (message != null)
					{
						processMessageHTML(htmlSB, message);
					}
				}
			}
		}

		htmlSB.append("</table>" + TERMINATOR);
		htmlSB.append("</body>" + TERMINATOR);
		htmlSB.append("</html>");

		final String toOutput = htmlSB.toString();
		return toOutput;
	}

	protected String constructXMLString(final boolean reverseMessages)
	{
		final StringBuffer xmlSB = new StringBuffer();

		xmlSB.append(INITIAL_XML);

		if (encoding == null)
		{
			// encoding defaults to utf-8 if none is specified
			xmlSB.append(" encoding=\"utf-8\"?>");
		}
		else
		{
			xmlSB.append(" encoding=\"" + encoding + "\"?>");
		}

		xmlSB.append(TERMINATOR);

		// specifies the location of a css stylesheet that is used to format
		// the document

		if ((cssLocation != null) && (cssLocation.trim().length() != 0))
		{
			xmlSB.append("<?xml-stylesheet title=\"CSS_formatting\" type=\"text/css\" href=\"" + cssLocation + "\"?>"
					+ TERMINATOR);
		}

		xmlSB.append(INITIAL_RSS + TERMINATOR);

		xmlSB.append("<channel>" + TERMINATOR);

		if (title != null)
		{
			xmlSB.append("<title>" + escape(title) + "</title>" + TERMINATOR);
		}

		if (link != null)
		{
			xmlSB.append("<link>" + escape(link) + "</link>" + TERMINATOR);
		}

		if (description != null)
		{
			xmlSB.append("<description>" + escape(description) + "</description>" + TERMINATOR);
		}

		final String dateStamp = getDateStamp();

		xmlSB.append("<lastBuildDate>" + dateStamp + "</lastBuildDate>" + TERMINATOR);

		if (language != null)
		{
			xmlSB.append("<language>" + escape(language) + "</language>" + TERMINATOR);
		}

		// now iterate through any supplied messages, and if they
		// define rss fields, output them into this file

		if (messages != null)
		{
			if (reverseMessages == false)
			{

				for (int i = 0; i < messages.length; i++)
				{
					final DictionaryImpl message = messages[i];

					if (message != null)
					{
						final String guidString = dateStamp + "-" + i;
						processMessageRSS(xmlSB, message, guidString);
					}
				}
			}
			else
			{
				for (int i = (messages.length - 1); i >= 0; i--)
				{

					final DictionaryImpl message = messages[i];

					if (message != null)
					{
						final String guidString = dateStamp + "-" + i;
						processMessageRSS(xmlSB, message, guidString);
					}
				}
			}
		}

		xmlSB.append("</channel>" + TERMINATOR);
		xmlSB.append("</rss>");

		final String toOutput = xmlSB.toString();
		return toOutput;
	}

	protected String escape(final String xmlString)
	{
		// escapes any characters in this string that are necessary
		// so that valid xml is produced

		final StringBuffer sb = new StringBuffer(xmlString);

		int pos = 0;

		while (pos < sb.length())
		{
			final char c = sb.charAt(pos);

			switch (c)
			{
				case '<':

					sb.replace(pos, pos + 1, "&lt;");
					pos = pos + 4;
					break;

				case '>':
					sb.replace(pos, pos + 1, "&gt;");
					pos = pos + 4;
					break;

				case '&':
					sb.replace(pos, pos + 1, "&amp;");
					pos = pos + 5;
					break;

				default:
					pos = pos + 1;
			}
		}

		return sb.toString();
	}

	protected String getDateStamp()
	{
		final Date date = new Date();
		final String dateString = sdf.format(date);
		return (escape(dateString));
	}

	protected abstract File getFile(String fileName);

	/*
	 * 
	 * protected void setOutputHTML(String newValue) { String oldValue = this.outputHTML;
	 * this.outputHTML = newValue;
	 * 
	 * propertyChangeListeners.firePropertyChange("outputHTML", oldValue, newValue); }
	 * 
	 * public String getOutputHTML() { return outputHTML; }
	 */

	/*
	 * 
	 * protected void setOutputRSS(String newValue) { String oldValue = this.outputRSS;
	 * this.outputRSS = newValue;
	 * 
	 * propertyChangeListeners.firePropertyChange("outputRSS", oldValue, newValue); }
	 * 
	 * public String getOutputRSS() { return outputRSS; }
	 */

	protected abstract String getFileLocation(File file) throws IOException;

	protected void outputToFile(final File outputFile, final String outputString) throws UnsupportedEncodingException,
			IOException

	{
		if (outputFile.exists())
		{
			outputFile.delete();
		}

		final FileOutputStream baos = new FileOutputStream(outputFile);
		OutputStreamWriter osw = null;

		if (encoding == null)
		{
			osw = new OutputStreamWriter(baos, "UTF-8");
		}
		else
		{
			osw = new OutputStreamWriter(baos, encoding);
		}

		final BufferedWriter bw = new BufferedWriter(osw);
		bw.write(outputString);
		bw.flush();
		bw.close();
	}

	protected void processMessageHTML(final StringBuffer sb, final DictionaryImpl message)
	{
		final Hashtable hash = message.getHashtable();

		if (hash.containsKey("_containsRSS"))
		{
			appendMessageAsHTML(hash, sb);
		}
		else
		{
			// heuristic - if there is a value
			// field in the message, then use this as the title

			if (hash.containsKey("value"))
			{
				final Object valueField = hash.get("value");

				if (valueField instanceof String)
				{
					hash.put("_rss." + RSSHandler.TITLE_TAG, new StringBoxImpl((String) valueField));
					appendMessageAsHTML(hash, sb);

				}
				else
				{
					if (valueField instanceof StringBoxImpl)
					{

						hash.put("_rss." + RSSHandler.TITLE_TAG, (StringBoxImpl) valueField);
						appendMessageAsHTML(hash, sb);
					}
				}
			}
		}
	}

	protected void processMessageRSS(final StringBuffer sb, final DictionaryImpl message, final String guidString)
	{
		final Hashtable hash = message.getHashtable();

		if (hash.containsKey("_containsRSS"))
		{
			appendMessageAsRSS(hash, sb, guidString);
		}
		else
		{
			// heuristic - if there is a value
			// field in the message, then use this as the title

			if (hash.containsKey("value"))
			{
				final Object valueField = hash.get("value");

				if (valueField instanceof String)
				{
					hash.put("_rss." + RSSHandler.TITLE_TAG, new StringBoxImpl((String) valueField));
					appendMessageAsRSS(hash, sb, guidString);

				}
				else
				{
					if (valueField instanceof StringBoxImpl)
					{

						hash.put("_rss." + RSSHandler.TITLE_TAG, (StringBoxImpl) valueField);
						appendMessageAsRSS(hash, sb, guidString);
					}
				}
			}
		}
	}

	protected void updateOutput()
	{
		// pass a boolean indicating whether the order of messages
		// in the constructed strings should be reversed

		final String xmlString = constructXMLString(false);
		final String htmlString = constructHTMLString(false);

		final String reversedXMLString = constructXMLString(true);
		final String reversedHTMLString = constructHTMLString(true);

		/*
		 * setOutputRSS(xmlString); setOutputHTML(htmlString);
		 */

		// now see if a user has
		// provided a file name
		// and output text to this, using the encoding
		// they have supplied

		if ((fileName != null) && (fileName.trim().length() > 0))
		{

			if ((encoding != null) && (encoding.trim().length() > 0))
			{
				try
				{
					// normal rss...

					final File rssFile = getFile(fileName + ".xml");
					outputToFile(rssFile, xmlString);

					final String rssLocation = getFileLocation(rssFile);

					setLocationOfRSS(rssLocation);
					setAttention("RSS file uploaded successfully");

					// ******************

					// reversed message order

					final File reversedRSSFile = getFile(fileName + "_reversed.xml");
					outputToFile(reversedRSSFile, reversedXMLString);

					final String reversedRSSLocation = getFileLocation(reversedRSSFile);

					setLocationOfReversedRSS(reversedRSSLocation);
					setAttention("Reversed RSS file uploaded successfully");

					// *******************

					// normal html

					final File htmlFile = getFile(fileName + ".html");
					outputToFile(htmlFile, htmlString);

					final String htmlLocation = getFileLocation(htmlFile);

					setLocationOfHTML(htmlLocation);
					setAttention("HTML file uploaded successfully");

					// *******************

					// reversed message order

					final File reversedHTMLFile = getFile(fileName + "_reversed.html");
					outputToFile(reversedHTMLFile, reversedHTMLString);

					final String reversedHTMLLocation = getFileLocation(reversedHTMLFile);

					setLocationOfReversedHTML(reversedHTMLLocation);
					setAttention("Reversed HTML file uploaded successfully");

				}
				catch (final UnsupportedEncodingException e)
				{
					setAttention("Cannot recognize the file encoding you have specified (try UTF-8 if you are not sure)");
				}
				catch (final IOException e)
				{
					setAttention("Error in writing to file");
				}
			}
			else
			{
				setAttention("Not writing to file as you have not specified a file encoding (default is UTF-8)");
			}
		}
		else
		{
			setAttention("Not writing to file as you have not supplied a file name");
		}
	}
}
