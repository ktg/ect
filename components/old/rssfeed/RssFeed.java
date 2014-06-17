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

Created by: Tom Hart (University of Nottingham)
Contributors:
  Tom Hart (University of Nottingham)

 */
/*
 * RssFeed, $RCSfile: RssFeed.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */
package equip.ect.components.rssfeed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Vector;

import equip.ect.http.XmlServer;

/**
 * Class implementing an RSS Feed. Creates a XmlServer which hosts the RSS Feed which meets the RSS
 * 2.0 standard. Only contains selected RSS elements and is somewhat tempremental
 */
// Data is currently unreachable with most RSS readers, but can be viewed
// in the browser. If anyone wants to fix this please do..
public class RssFeed implements Serializable, PropertyChangeListener
{

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private XmlServer xmlServer;
	private String url, title, link, desc, content;
	private Vector items = new Vector();

	/**
	 * Create new RssFeed object on local port 2121, with null data values
	 */
	public RssFeed()
	{
		try
		{
			xmlServer = new XmlServer(content, 2121);
		}
		catch (final Exception e)
		{
		}
		final RssItem item = new RssItem("item1");
		item.addPropertyChangeListener(this);
		items.add(item);
		setTitle("");
		setLink("");
		setDesc("");
		refreshFeed();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Get the current &lt;description&gt; element value of the feed
	 * 
	 * @return the current description
	 */
	public String getDesc()
	{
		return this.desc;
	}

	/**
	 * Get a list of {link RssItem# RssItems} associated with this feed.
	 * 
	 * @return Array of type {link RssItem# RssItems}
	 */
	public RssItem[] getItems()
	{
		return (RssItem[]) this.items.toArray(new RssItem[this.items.size()]);
	}

	/**
	 * Get the current &lt;link&gt; element value of the feed
	 * 
	 * @return The current link.
	 */
	public String getLink()
	{
		return this.link;
	}

	/**
	 * Get the current &lt;title&gt; element value of the feed
	 * 
	 * @return The current title.
	 */
	public String getTitle()
	{
		return this.title;
	}

	/**
	 * Get the current URL where this feed can be viewed (will be in the form
	 * http://&lt;localhost&gt;:&lt;port&gt;).
	 * 
	 * @return The URL as a string
	 */
	public String getURL()
	{
		return this.xmlServer.getBaseURL();
	}

	/**
	 * change to item
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent pce)
	{
		System.out.println("Property change...");
		refreshFeed();
	}

	/**
	 * Refreshes the data in the Feed. This will need to be called before feed viewer will see any
	 * changes occur.
	 */
	public void refreshFeed()
	{
		content = "";
		content += "	<title>" + getTitle() + "</title>\n";
		content += "	<link>" + getLink() + "</link>\n";
		content += "	<description>" + getDesc() + "</description>\n";
		content += addItemsToContent();
		System.out.println(content);
		content = wrap(content);
		xmlServer.setContent(content);
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Set the &lt;description&gt; element of this feed
	 * 
	 * @param string
	 *            The description of the feed.
	 */
	public void setDesc(final String value)
	{
		propertyChangeListeners.firePropertyChange("desc", this.desc, value);
		this.desc = value;
		this.refreshFeed();
	}

	/**
	 * Set the &lt;link&gt; element of this feed
	 * 
	 * @param string
	 *            The link of the feed.
	 */
	public void setLink(final String value)
	{
		propertyChangeListeners.firePropertyChange("link", this.link, value);
		this.link = value;
		this.refreshFeed();
	}

	/**
	 * Set the &lt;title&gt; element of this feed
	 * 
	 * @param string
	 *            The title of the feed.
	 */
	public void setTitle(final String value)
	{
		propertyChangeListeners.firePropertyChange("title", this.title, value);
		this.title = value;
		this.refreshFeed();
	}

	/**
	 * Cleanup any resources and destroy the XmlServer
	 */
	public void stop()
	{
		xmlServer.terminate();
	}

	/**
	 * Adds data (correctly nested) from any content items this RssFeed has
	 */
	private String addItemsToContent()
	{
		String its = "";
		for (int i = 0; i < items.size(); i++)
		{
			its += ((RssItem) items.get(i)).toXML();
		}
		return its;
	}

	/**
	 * Wraps RSS header/footer around the variable data
	 */
	private String wrap(String in)
	{
		in = "<?xml version=\"1.0\"?>\n<rss version=\"2.0\">\n	<channel>\n" + in;
		in = in + "	</channel>\n</rss>\n";
		return in;
	}
}
