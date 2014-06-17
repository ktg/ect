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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.dictionary;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;
import equip.runtime.ValueBase;
import org.gnu.stealthp.rsslib.RSSHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Extracts information from messages. <h3>Description</h3> SmallDisplayStringExtractor can be used
 * to extract two lines of text from complex messages (eg those produced by EmailReceiver or
 * RSSClient). Such lines of text might then be suitable for placement on a small display (ie one
 * not capable of displaying the full contents of a complex message). Examples of
 * small-display-related components include PhidgetLCD. <h3>Configuration</h3>
 * <p/>
 * Some components (at present, EmailReceiver and RSSClient) embed information into messages that
 * they produce specifying the order in which fields in these messages should be displayed, on small
 * displays, and on large displays. For example, EmailReceiver might specify that, on a small
 * display, the <i>from</i> and the <i>subject</i> fields of the any message should be displayed
 * before any others.
 * </P>
 * <p/>
 * If SmallDisplayStringExtractor is used with such messages, then no configuration is required. The
 * relevant fields will be extracted from any message placed on property <i>message</i>, and will
 * then be placed on properties <i>lineOneText</i> and <i>lineTwoText</i>. Users can override this
 * process by setting <i>acceptKeyOrderingFromMessage</i> to false, and by specifying the names of
 * fields that should be extracted through properties <i>lineOneKey</i> and <i>lineTwoKey</i>. If a
 * message is supplied which does not define key ordering information, then these properties can
 * also be used to specify which fields should be extracted from the message.
 * </P>
 *
 * @classification Data/Dictionary
 * @defaultInputProperty message
 * @defaultOutputProperty lineOneText
 * @preferred
 */
public class SmallDisplayStringExtractor implements Serializable
{
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	DictionaryImpl message;
	String lineOneKey = "value";
	String lineTwoKey = null;
	String lineOneText = null;
	String lineTwoText = null;
	boolean acceptKeyOrderingFromMessage = true;

	public SmallDisplayStringExtractor()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized boolean getAcceptKeyOrderingFromMessage()
	{
		return acceptKeyOrderingFromMessage;
	}

	public synchronized void setAcceptKeyOrderingFromMessage(final boolean newValue)
	{
		final boolean oldValue = this.acceptKeyOrderingFromMessage;
		this.acceptKeyOrderingFromMessage = newValue;

		propertyChangeListeners.firePropertyChange("acceptKeyOrderingFromMessage", oldValue, newValue);
	}

	public synchronized String getLineOneKey()
	{
		return lineOneKey;
	}

	public synchronized void setLineOneKey(final String newValue)
	{
		final String oldValue = this.lineOneKey;
		this.lineOneKey = newValue;

		propertyChangeListeners.firePropertyChange("lineOneKey", oldValue, newValue);

	}

	public synchronized String getLineOneText()
	{
		return lineOneText;
	}

	protected synchronized void setLineOneText(final String newValue)
	{
		final String oldValue = this.lineOneText;
		this.lineOneText = newValue;

		propertyChangeListeners.firePropertyChange("lineOneText", oldValue, newValue);
	}

	public synchronized String getLineTwoKey()
	{
		return lineTwoKey;
	}

	public synchronized void setLineTwoKey(final String newValue)
	{
		final String oldValue = this.lineTwoKey;
		this.lineTwoKey = newValue;

		propertyChangeListeners.firePropertyChange("lineTwoKey", oldValue, newValue);
	}

	public synchronized String getLineTwoText()
	{
		return lineTwoText;
	}

	protected synchronized void setLineTwoText(final String newValue)
	{
		final String oldValue = this.lineTwoText;
		this.lineTwoText = newValue;

		propertyChangeListeners.firePropertyChange("lineTwoText", oldValue, newValue);
	}

	public synchronized DictionaryImpl getMessage()
	{
		return message;
	}

	public synchronized void setMessage(final DictionaryImpl newValue)
	{
		final DictionaryImpl oldValue = this.message;
		this.message = newValue;
		propertyChangeListeners.firePropertyChange("message", oldValue, newValue);

		updateOutputStrings();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	void setLineOneFromKey(final String key, final Hashtable hash, final boolean displayFieldName)
	{
		if (hash.containsKey(key))
		{
			final ValueBase vb = (ValueBase) (hash.get(key));

			if (vb instanceof StringBoxImpl)
			{
				final String text = ((StringBoxImpl) vb).value;

				if (displayFieldName)
				{
					setLineOneText(key + ":" + text);
				}
				else
				{
					setLineOneText(text);
				}
			}
			else
			{
				setLineOneText(null);
			}
		}
		else
		{
			setLineOneText(null);
		}
	}

	void setLineTwoFromKey(final String key, final Hashtable hash, final boolean displayFieldName)
	{
		if (hash.containsKey(key))
		{
			final ValueBase vb = (ValueBase) (hash.get(key));

			if (vb instanceof StringBoxImpl)
			{
				final String text = ((StringBoxImpl) vb).value;

				if (displayFieldName)
				{
					setLineTwoText(key + ":" + text);
				}
				else
				{
					setLineTwoText(text);
				}
			}
			else
			{
				setLineTwoText(null);
			}
		}
		else
		{
			setLineTwoText(null);
		}
	}

	void setTextFromRSSFields(final Hashtable hash)
	{
		System.out.println("set text from rss fields");

		if (hash.containsKey("_rss." + RSSHandler.TITLE_TAG))
		{
			// final argument (ie false) indicates
			// that the component shouldn't display the name
			// of the field

			System.out.println("found an rss title");

			setLineOneFromKey("_rss." + RSSHandler.TITLE_TAG, hash, false);
		}
		else
		{
			System.out.println("no rss title");
			setLineOneText(null);
		}

		if (hash.containsKey("_rss." + RSSHandler.DESCRIPTION_TAG))
		{
			setLineTwoFromKey("_rss." + RSSHandler.DESCRIPTION_TAG, hash, false);
		}
		else
		{
			setLineTwoText(null);
		}
	}

	void setTextFromSmallDisplayKeys(final String keys, final Hashtable hash)
	{
		final String[] bits = keys.split(",");

		if (bits.length == 0)
		{
			setLineOneText(null);
			setLineTwoText(null);
		}
		else
		{
			if (bits.length == 1)
			{
				setLineOneFromKey(bits[0], hash, true);
				setLineTwoText(null);
			}
			else
			{
				setLineOneFromKey(bits[0], hash, true);
				setLineTwoFromKey(bits[1], hash, true);
			}
		}
	}

	void setTextFromUserKeys(final Hashtable hash)
	{
		if ((lineOneKey != null) && (hash.containsKey(lineOneKey)))
		{
			setLineOneFromKey(lineOneKey, hash, true);
		}
		else
		{
			setLineOneText(null);
		}

		if ((lineTwoKey != null) && (hash.containsKey(lineTwoKey)))
		{
			setLineTwoFromKey(lineTwoKey, hash, true);
		}
		else
		{
			setLineTwoText(null);
		}
	}

	void updateOutputStrings()
	{
		if (message != null)
		{
			final Hashtable hash = message.getHashtable();

			if (acceptKeyOrderingFromMessage == true)
			{
				// check message to see if it actually
				// defines a key ordering

				// note there are two ways of doing this
				// the message might contain definitions of rss fields
				// for this message, in which case the component
				// will display the rss title and rss description on
				// lines one and tow
				// or the message might contain a definition
				// of the ordering in which keys should be displayed

				// note the rss method currently takes precedence

				if (hash.containsKey("_containsRSS"))
				{
					System.out.println("calling set text from rss fields");
					setTextFromRSSFields(hash);
				}
				else
				{
					if (hash.containsKey("_smallDisplayKeys"))
					{
						System.out.println("calling set text from small display keys");

						final StringBoxImpl sb = (StringBoxImpl) (hash.get("_smallDisplayKeys"));
						final String keys = sb.value;

						setTextFromSmallDisplayKeys(keys, hash);
					}
					else
					{
						System.out.println("set text from user keys");

						setTextFromUserKeys(hash);
					}
				}
			}
			else
			{
				setTextFromUserKeys(hash);
			}
		}
		else
		{
			setLineOneText(null);
			setLineTwoText(null);
		}
	}
}
