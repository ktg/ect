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

Created by: James Mathrick (University of Nottingham)
Contributors:
  James Mathrick (University of Nottingham)

 */
package equip.ect.components.webdigester;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

public class XMLMunger implements PropertyChangeListener, Serializable
{

	public static void main(final String[] args)
	{
		final XMLMunger foo = new XMLMunger();
		foo.setTarget("news");
		System.out.println(foo.getInfo());
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// Properties
	private String config = null;
	private String target = null;

	private String info = null;

	/**
     *
     *
     */
	public XMLMunger()
	{
		try
		{
			this.setConfig("news#http://news.bbc.co.uk/rss/newsonline_uk_edition/front_page/rss091.xml#title");
			this.setTarget("");
			this.setInfo("");

		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}

	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * @return
	 */
	public String getConfig()
	{
		return this.config;
	}

	/**
	 * @return
	 */
	public String getInfo()
	{
		return this.info;
	}

	/**
	 * @return
	 */
	public String getTarget()
	{
		return this.target;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{

	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * @param inText
	 */
	public void setConfig(final String c)
	{
		final String oldConfig = this.config;
		this.config = new String(c);
		propertyChangeListeners.firePropertyChange("config", oldConfig, this.config);
	}

	/**
     *
     *
     */
	public void setInfo(final String i)
	{
		final String oldInfo = this.info;
		this.info = new String(i);
		propertyChangeListeners.firePropertyChange("info", oldInfo, this.info);
	}

	/**
     *
     *
     */
	public void setTarget(final String t)
	{
		final String c = configHasTarget(t);
		if (!c.equals(""))
		{
			final String oldTarget = this.target;
			this.target = new String(t);
			propertyChangeListeners.firePropertyChange("target", oldTarget, this.target);
			setInfo(extractInfo(c));
		}
	}

	/**
	 * stop/kill
	 */
	public void stop()
	{
	}

	/**
	 * returns the xmlAddress:targetTag string if config present, "" if not
	 */
	private String configHasTarget(final String target)
	{
		final String[] configs = this.config.split(" ");
		for (final String config2 : configs)
		{
			final int index = config2.indexOf('#');
			if (index > -1) { return config2.substring(index + 1); }
		}
		return "";
	}

	/**
	 * returns the xmlAddress:targetTag string if config present, error msg if not
	 */
	private String extractInfo(final String config)
	{
		final int marker = config.indexOf('#');
		final String xmlAddress = config.substring(0, marker);
		final String tag = config.substring(marker + 1);
		URLConnection conn = null;

		try
		{
			conn = new URL(xmlAddress).openConnection();
		}
		catch (final Exception e)
		{
			return e.getMessage();
		}
		String data = "";
		try
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (true)
			{
				String line;
				/*
				 * while (!in.ready()) { try { Thread.sleep(10); } catch (InterruptedException e) {
				 * } }
				 */
				line = in.readLine();
				if (line == null) { return data; }
				if (line.lastIndexOf(tag) > -1)
				{
					data += line.substring(	line.lastIndexOf("<" + tag + ">") + 2 + tag.length(),
											line.lastIndexOf("</" + tag))
							+ "...";
				}
			}
		}
		catch (final IOException e)
		{
			return e.getMessage();
		}
	}
}
