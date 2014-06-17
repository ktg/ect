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
 * RssItem, $RCSfile: RssItem.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Tom Hart
 */
package equip.ect.components.rssfeed;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Class for creating &lt;item&gt;s for use in XML RSS documents
 */

public class RssItem implements Serializable
{

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private int numElmts = 6;
	private int TITLE = 0, DESC = 1, LINK = 2, AUTHOR = 3, PUBDATE = 4, CATEGORY = 5;

	private String[] tags = { "title", "description", "link", "author", "pubDate", "category" };

	private String[] elements;

	private String persistentChild = "";

	SimpleDateFormat rfc822format = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");

	/**
	 * Create new RssItem with blank data values
	 */
	public RssItem(final String persistentChild)
	{
		this.persistentChild = persistentChild;
		elements = new String[numElmts];
		for (int i = 0; i < numElmts; i++)
		{
			elements[i] = "";
		}
	}

	/**
	 * Create new RssItem
	 * 
	 * @param title
	 *            title element of new feed
	 * @param desc
	 *            description element
	 * @param link
	 *            link element
	 * @param author
	 *            author element
	 * @param pubDate
	 *            pubDate element
	 * @param category
	 *            category element
	 */
	public RssItem(final String persistentChild, final String title, final String desc, final String link,
			final String author, final String pubDate, final String category)
	{
		this.persistentChild = persistentChild;
		elements = new String[numElmts];
		elements[TITLE] = title;
		elements[DESC] = desc;
		elements[LINK] = link;
		elements[AUTHOR] = author;
		elements[PUBDATE] = pubDate;
		elements[CATEGORY] = category;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Get the current &lt;author&gt; element value of the feed
	 * 
	 * @return the current author
	 */
	public String getAuthor()
	{
		return this.elements[AUTHOR];
	}

	/**
	 * Get the current &lt;category&gt; element value of the feed
	 * 
	 * @return the current category
	 */
	public String getCategory()
	{
		return this.elements[CATEGORY];
	}

	/**
	 * Get the current &lt;description&gt; element value of the feed
	 * 
	 * @return the current description
	 */
	public String getDesc()
	{
		return this.elements[DESC];
	}

	/**
	 * Get the current &lt;link&gt; element value of the feed
	 * 
	 * @return the current link
	 */
	public String getLink()
	{
		return this.elements[LINK];
	}

	/**
	 * persistentChild property for persistence of sub-components (must be String type)
	 */
	public String getPersistentChild()
	{
		return persistentChild;
	}

	/**
	 * Get the current &lt;pubDate&gt; element value of the feed
	 * 
	 * @return the current pubDate
	 */
	public String getPubDate()
	{
		return this.elements[PUBDATE];
	}

	/**
	 * Get the current &lt;title&gt; element value of the feed
	 * 
	 * @return the current title
	 */
	public String getTitle()
	{
		return this.elements[TITLE];
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Set the &lt;author&gt; element of this feed
	 * 
	 * @param string
	 *            The author of the feed.
	 */
	public void setAuthor(final String value)
	{
		final String old = this.elements[AUTHOR];
		this.elements[AUTHOR] = value;
		propertyChangeListeners.firePropertyChange("author", old, value);
	}

	/**
	 * Set the &lt;category&gt; element of this feed
	 * 
	 * @param string
	 *            The category of the feed.
	 */
	public void setCategory(final String value)
	{
		final String old = this.elements[CATEGORY];
		this.elements[CATEGORY] = value;
		propertyChangeListeners.firePropertyChange("category", old, value);
	}

	/**
	 * Set the &lt;description&gt; element of this feed
	 * 
	 * @param string
	 *            The description of the feed.
	 */
	public void setDesc(final String value)
	{
		final String old = this.elements[DESC];
		this.elements[DESC] = value;
		propertyChangeListeners.firePropertyChange("desc", old, value);
		// update pub date
		setPubDate();
	}

	/**
	 * Set the &lt;link&gt; element of this feed
	 * 
	 * @param string
	 *            The link of the feed.
	 */
	public void setLink(final String value)
	{
		final String old = this.elements[LINK];
		this.elements[LINK] = value;
		propertyChangeListeners.firePropertyChange("link", old, value);
	}

	/**
	 * set default pub date (now)
	 */
	public void setPubDate()
	{
		setPubDate(rfc822format.format(new java.util.Date()));
	}

	/**
	 * Set the &lt;pubDate&gt; element of this feed
	 * 
	 * @param string
	 *            The pubDate of the feed.
	 */
	public void setPubDate(final String value)
	{
		final String old = this.elements[PUBDATE];
		this.elements[PUBDATE] = value;
		propertyChangeListeners.firePropertyChange("pubDate", old, value);
	}

	/**
	 * Set the &lt;title&gt; element of this feed
	 * 
	 * @param string
	 *            The title of the feed.
	 */
	public void setTitle(final String value)
	{
		final String old = this.elements[TITLE];
		this.elements[TITLE] = value;
		propertyChangeListeners.firePropertyChange("title", old, value);
	}

	/**
	 * Adds the correct tag around each data fragment to produce XML
	 */
	public String toXML()
	{
		String s = "	<item>\n";
		for (int i = 0; i < elements.length; i++)
		{
			// if element has any content add it, else skip
			if ((!elements[i].equals("")) && elements[i] != null)
			{
				s += "		<" + tags[i] + ">" + elements[i] + "</" + tags[i] + ">\n";
			}
		}
		s += "	</item>\n";
		// if item doesn't actually have any contents return nothing
		if (s.equals("	<item>\n	</item>\n"))
		{
			return "";
		}
		else
		{
			return s;
		}
	}
}
