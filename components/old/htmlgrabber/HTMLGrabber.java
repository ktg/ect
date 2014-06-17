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

package equip.ect.components.htmlgrabber;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Grabs the text definining a web-page from a specified URL <H3>Usage</H3> Provide a URL to
 * download from to the <i>address</i> property of the component. The component will then download
 * the HTML defining the web-page and place it onto the <i>text</i> property. Please note that if
 * this HTML is longer than 65,535 characters then only the first 65,535 characters will be returned
 * 
 * @classification Media/download
 */

public class HTMLGrabber implements Serializable
{
	private static final int LENGTH_LIMIT = 65536;

	protected String address;

	protected String attention;

	protected String text;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public HTMLGrabber()
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

	public String getText()
	{
		return text;
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

		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				downloadHTML(newValue);
			}
		})).start();
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

		final BufferedReader br = new BufferedReader(new InputStreamReader(is));

		final StringBuffer htmlText = new StringBuffer();

		String lastLine = null;

		try
		{
			while ((lastLine = br.readLine()) != null)
			{
				htmlText.append(lastLine);

				if (htmlText.length() > LENGTH_LIMIT)
				{
					break;
				}
			}

			if (htmlText.length() > LENGTH_LIMIT)
			{
				setText(htmlText.substring(0, LENGTH_LIMIT));
			}
			else
			{
				setText(htmlText.toString());
			}

			setAttention("Successfully downloaded from supplied address");
		}
		catch (final IOException e)
		{
			signalError("Error whilst reading from supplied address");
			return;
		}
	}

	void signalError(final String errorString)
	{
		setAttention(errorString);
		setText(null);
	}

	protected void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}

	protected void setText(final String newValue)
	{
		final String oldValue = this.text;
		this.text = newValue;

		propertyChangeListeners.firePropertyChange("text", oldValue, newValue);
	}
}
