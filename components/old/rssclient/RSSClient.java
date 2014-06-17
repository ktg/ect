/*
 <COPYRIGHT>

 Copyright (c) 2005, University of Nottingham
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
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.components.rssclient;

import org.gnu.stealthp.rsslib.RSSChannel;
import org.gnu.stealthp.rsslib.RSSException;
import org.gnu.stealthp.rsslib.RSSHandler;
import org.gnu.stealthp.rsslib.RSSItem;
import org.gnu.stealthp.rsslib.RSSParser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Simple client and viewer for monitoring RSS feeds.
 * <p/>
 * <H3>Description</H3> Allows you to subscribe to an RSS feed. Provides a simple window for
 * displaying the feed as a browsable HTML page.
 * <p/>
 * The results from an RSS feed are returned in a Hashtable array representation, with each
 * hashtable containing the attribute/value elements of the channel.
 * <h3>Configuration</h3>
 * Although this component requires no configuration as such, if your network requires you to access
 * the web through a web-cache/proxy, then the component must be used in a java environment where
 * web-cache/proxy access has been configured properly (eg jvm caching properties set).
 * <H3>Usage</H3>
 * Once you have located an RSS feed that you wish to access in this component, copy the URL of the
 * feed into either
 * <ul>
 * <li>the <i>url</i> property of the component
 * <li>the <i>url</i> field in the GUI launched by the component (you need to click <i>go</i> after
 * doing this)
 * </ul>
 * The component will then fetch any available news items from this feed, and will
 * <ul>
 * <li>place them on the <i>messages</i> property of the component
 * <li>make them available through the GUI
 * </ul>
 * Clicking on a link in the GUI rendering of a news item will cause the URL of this link to appear
 * on the <i>browserURL</i> property of the component.
 * <H3>Technical Details</H3> This currently requires library rsslib4j (rsslib4j.sourceforge.net)
 *
 * @author humble
 * @defaultInputProperty url
 * @defaultOutputProperty messages
 * @classification Networked Services/RSS
 * @technology RSS
 */
public class RSSClient extends JFrame
{

	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	private String url = "http://rss.cnn.com/rss/cnn_topstories.rss";
	private boolean refresh;
	private JEditorPane rssPane;
	private JTextField urlField;
	private RSSHandler rssHandler;
	private JTabbedPane tabbedPane;
	private JButton goB;
	private String browserURL = null;
	private Hashtable[] messages;

	public RSSClient()
	{
		super("RSS Client");

		final JPanel mainPanel = new JPanel(new BorderLayout());

		rssPane = new JEditorPane();
		rssPane.setEditable(false);

		final HyperlinkListener hll = createHyperLinkListener();
		rssPane.addHyperlinkListener(hll);
		tabbedPane = new JTabbedPane();
		tabbedPane.add("RSS Feed", new JScrollPane(rssPane));
		mainPanel.add(BorderLayout.CENTER, tabbedPane);

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.WEST, new JLabel("url: "));
		urlField = new JTextField(url);
		topPanel.add(BorderLayout.CENTER, urlField);
		goB = new JButton("Go");
		goB.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final String newURL = urlField.getText().trim();
				setUrl(newURL);

			}
		});
		topPanel.add(BorderLayout.EAST, goB);
		mainPanel.add(BorderLayout.NORTH, topPanel);
		getContentPane().add(mainPanel);
		setSize(500, 400);
		setVisible(true);

	}

	public static void main(final String[] args)
	{
		final RSSClient rssDisplay = new RSSClient();
		rssDisplay.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	static void filterEntry(final Hashtable dict, final String entry, final String value)
	{
		if (value != null)
		{
			dict.put(entry, value);
		}
	}

	static Hashtable[] rssChannelToDictArray(final RSSChannel rssChannel)
	{
		final LinkedList lst = rssChannel.getItems();
		final Hashtable[] dictArray = new Hashtable[lst.size()];
		for (int i = 0; i < lst.size(); i++)
		{
			final RSSItem itm = (RSSItem) lst.get(i);
			dictArray[i] = rssItemToHashtable(itm);
		}
		return dictArray;
	}

	static String rssChannelToHTML(final RSSChannel rssChannel)
	{

		final LinkedList lst = rssChannel.getItems();
		System.out.println("RSSClient: number of items in channel: " + lst.size());
		final StringBuffer buffer = new StringBuffer("<HTML>\n<BODY>\n");
		buffer.append("\n<H2>" + rssChannel.getTitle() + "</H2>");
		buffer.append("\n" + rssChannel.getDescription());
		buffer.append("\n<HR>");
		for (int i = 0; i < lst.size(); i++)
		{
			final RSSItem itm = (RSSItem) lst.get(i);
			buffer.append(rssItemToHTML(itm));
		}
		buffer.append("\n</BODY>\n</HTML>");
		return buffer.toString();

	}

	static Hashtable rssItemToHashtable(final RSSItem rssItem)
	{
		final Hashtable dict = new Hashtable();

		dict.put("_containsRSS", new Boolean(true));

		filterEntry(dict, "_rss." + RSSHandler.AUTHOR_TAG, rssItem.getAuthor());

		filterEntry(dict, "_rss." + RSSHandler.COMMENTS_TAG, rssItem.getComments());
		filterEntry(dict, "_rss." + RSSHandler.DESCRIPTION_TAG, rssItem.getDescription());
		filterEntry(dict, "_rss." + RSSHandler.LINK_TAG, rssItem.getLink());
		// filterEntry(dict, "_rss." + RSSHandler.PUB_DATE_TAG,
		// rssItem.getPubDate());
		filterEntry(dict, "_rss." + RSSHandler.TITLE_TAG, rssItem.getTitle());

		final Date receivedDate = new Date();
		final String dateString = sdf.format(receivedDate);

		// tag the rss item with the date it was received
		// into the ect system
		dict.put("_rss." + RSSHandler.PUB_DATE_TAG, dateString);

		// now construct keys consisting of key names,
		// to assist displays in working out how
		// to render this message

		final String smallDisplayKeys = "_rss.title,_rss.link,_rss.description,_rss.publicationDate,_rss.about,_rss.comments,_rss.author,_rss.date";
		final String largeDisplayKeys = "_rss.title,_rss.publicationDate,_rss.author,_rss.description,_rss.link,_rss.about,_rss.comments,_rss.date";

		dict.put("_smallDisplayKeys", smallDisplayKeys);
		dict.put("_largeDisplayKeys", largeDisplayKeys);

		return dict;
	}

	static String rssItemToHTML(final RSSItem item)
	{
		final StringBuffer sb = new StringBuffer();
		// sb.append("\n<P>");
		sb.append("\n<H3>" + item.getTitle() + "</H3>");
		sb.append("\n<DATE>" + item.getDate() + "</DATE><BR>");
		sb.append("\n<A HREF=\"" + item.getLink() + "\">" + item.getDescription() + "</A>");
		// sb.append("\n</P>");
		return sb.toString();
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public HyperlinkListener createHyperLinkListener()
	{

		return new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					setBrowserURL(e.getURL());

				}
			}

		};
	}

	public String getBrowserURL()
	{
		return this.browserURL;
	}

	protected void setBrowserURL(final String url)
	{
		try
		{
			final URL webURL = new URL(url);
			setBrowserURL(webURL);
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param url The url to set.
	 */

	protected void setBrowserURL(final URL webURL)
	{
		final String old = this.browserURL;
		this.browserURL = webURL.toExternalForm();

		propertyChangeListeners.firePropertyChange("browserURL", old, browserURL);

	}

	public Hashtable[] getMessages()
	{
		return this.messages;
	}

	public RSSHandler getRSSFeed()
	{

		rssHandler = new RSSHandler();
		URL rssURL = null;

		try
		{
			rssURL = new URL(url);

		}
		catch (final MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		try
		{
			// RSSParser parser = new RSSParser();
			// parser.setHandler(rssHandler);

			// parser.setXmlResource(rssURL);

			// System.out.println("SAX Prop: " +
			// System.getProperty("javax.xml.parsers.SAXParserFactory"));
			// System.setProperty("javax.xml.parsers.SAXParserFactory",
			// "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
			// System.out.println("SAX: " +
			// SAXParserFactory.newInstance().getClass().getName());
			RSSParser.parseXmlFile(rssURL, rssHandler, false);
			// parser.parse();
			// parser.free();
			return rssHandler;
		}
		catch (final RSSException e)
		{

			System.out.println("RSSDisplay: " + e.getMessage());
		}

		return null;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * @param url The url to set.
	 */
	public void setUrl(final String url)
	{

		final String old = this.url;
		this.url = url;
		urlField.setText(url);
		propertyChangeListeners.firePropertyChange("url", old, url);
		setRefresh(true);
	}

	/**
	 * @return Returns the refresh.
	 */
	public boolean isRefresh()
	{
		return refresh;
	}

	/**
	 * @param refresh The refresh to set.
	 */
	public void setRefresh(final boolean refresh)
	{
		final boolean old = this.refresh;
		this.refresh = refresh;
		if (refresh)
		{
			goB.setEnabled(false);
			getRSSFeed();
			if (rssHandler != null)
			{
				final RSSChannel ch = rssHandler.getRSSChannel();
				if (ch != null)
				{
					setRSSChannel(ch);
				}
			}
			goB.setEnabled(true);
		}
		propertyChangeListeners.firePropertyChange("refresh", old, refresh);
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void stop()
	{
		this.dispose();
	}

	private void setRSSChannel(final RSSChannel ch)
	{
		final Hashtable[] old = this.messages;
		this.messages = rssChannelToDictArray(ch);
		propertyChangeListeners.firePropertyChange("messages", old, messages);

		rssPane.setContentType("text/html");
		rssPane.setText(rssChannelToHTML(ch));
		rssPane.setCaretPosition(0);
	}
}
