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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.httpconfigtextvalue;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;

import equip.ect.components.text.TextBeanInfo;
import equip.ect.http.SimpleFormHttpServer;
import equip.ect.http.SimpleFormModel;
import equip.ect.http.SimpleFormProvider;

public class HttpConfigText implements Serializable, SimpleFormProvider
{

	public static void main(final String[] args)
	{
		try
		{
			new HttpConfigText();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Property
	private String text = "";

	/**
	 * config url property
	 */
	protected String configUrl;
	/**
	 * configuration model
	 */
	SimpleFormModel configModel = new SimpleFormModel();

	/**
	 * server
	 */
	SimpleFormHttpServer configServer;

	/**
	 * cons
	 */
	public HttpConfigText() throws IOException
	{
		// text value
		configModel.addProperty("text", "the single text value", text);
		try
		{
			configServer = new SimpleFormHttpServer("Text component", this);
			configUrl = configServer.getBaseURL();
		}
		catch (final IOException e)
		{
			System.err.println("ERROR creating text component form server: " + e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * config url getter
	 */
	public String getConfigUrl()
	{
		return configUrl;
	}

	/**
	 * get {@link SimpleFormModel} to show - for config
	 */
	@Override
	public SimpleFormModel getModel()
	{
		return configModel;
	}

	public String getText()
	{
		return text;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * set {@link SimpleFormModel}.
	 * 
	 * @return Confirmed/filtered model (else null)
	 */
	@Override
	public SimpleFormModel setModel(final SimpleFormModel model)
	{
		final String newtext = (String) model.getValue("text");
		System.out.println("Text set by setModel (" + newtext + ")");
		setText(newtext);
		return model;
	}

	/**
	 * setter
	 */
	public void setText(final String newtext)
	{
		final String oldtext = text;
		if (oldtext == newtext) { return; // prevent any looping
		}
		this.text = newtext;
		// update model
		configModel.setValue("text", newtext);

		propertyChangeListeners.firePropertyChange(TextBeanInfo.TEXT_PROPERTY_NAME, oldtext, newtext);
	}

	/**
	 * stop
	 */
	public void stop()
	{
		System.err.println("Stop http config text component...");
		try
		{
			configServer.terminate();
		}
		catch (final Exception e)
		{
			System.err.println("Error stopping server: " + e);
		}
		System.err.println("Stopped");
	}

}
