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

package equip.ect.components.goldsmiths;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class LootRipper implements Serializable
{

	protected String address;

	protected String attention;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public LootRipper()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getAddress()
	{
		return address;
	}

	public String getAttention()
	{
		return attention;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setAddress(final String newValue)
	{
		final String oldValue = this.address;
		this.address = newValue;

		propertyChangeListeners.firePropertyChange("address", oldValue, newValue);

		if (newValue != null)
		{
			(new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					downloadHTML(newValue);
				}
			})).start();
		}
	}

	void downloadHTML(final String urlName)
	{
		setAttention("beginning download");

		URL url = null;

		try
		{
			url = new URL(urlName);
		}
		catch (final MalformedURLException e)
		{
			signalError("Error with supplied address");
			return;
		}

		InputStream is = null;

		try
		{
			is = url.openStream();
		}
		catch (final IOException e)
		{
			signalError("Error connecting to supplied address");
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		final StringBuffer htmlText = new StringBuffer();
		String lastLine = null;

		try
		{
			while ((lastLine = br.readLine()) != null)
			{
				htmlText.append(lastLine);
			}

			setAttention("Successfully downloaded from supplied address");

			// close reader to free up memory
			br.close();
			br = null;

			final DOMParser parser = new DOMParser();
			final InputSource source = new InputSource(new StringReader(htmlText.toString()));

			parser.parse(source);
			final HTMLDocument doc = (HTMLDocument) (parser.getDocument());

			ripAdvert(doc);
		}
		catch (final IOException e)
		{
			signalError("Error whilst reading from supplied address");
			return;
		}
		catch (final SAXException e)
		{
			signalError("Error in parsing advert");
			return;
		}

	}

	protected abstract void clearOutputs();

	protected boolean isValidAttribute(final String attributeName, final String[] validAttributes)
	{

		for (final String validAttribute : validAttributes)
		{
			if (validAttribute.equals(attributeName)) { return true; }
		}

		return false;
	}

	protected abstract void ripAdvert(HTMLDocument doc);

	protected void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}

	protected void signalError(final String errorString)
	{
		setAttention(errorString);
		clearOutputs();
	}
}
