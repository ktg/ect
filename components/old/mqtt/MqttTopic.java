/*
<COPYRIGHT>

Copyright (c) 2008, University of Nottingham
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

 */
package equip.ect.components.mqtt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;

/**
 * A single MQTT topic - subcomponent of MqttBridge.
 */
public class MqttTopic implements Serializable
{
	/**
	 * topic -constant
	 */
	protected String topic;
	/**
	 * persistentChild identifier = topic -constant
	 */
	protected String persistentChild;
	/**
	 * value
	 */
	protected String value;
	/**
	 * date
	 */
	protected String date;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public MqttTopic(final String topic)
	{
		this.topic = topic;
		this.persistentChild = topic;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * date output getter
	 */
	public synchronized String getDate()
	{
		return date;
	}

	/**
	 * getter
	 */
	public String getPersistentChild()
	{
		return persistentChild;
	}

	/**
	 * getter
	 */
	public String getTopic()
	{
		return topic;
	}

	/**
	 * value output getter
	 */
	public synchronized String getValue()
	{
		return value;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * internal call from bridge
	 */
	void update(final HashMap<String, String> message)
	{
		setValue(message.get("value"));
		setDate(message.get("unixtime"));
	}

	/**
	 * date internal setter
	 */
	protected synchronized void setDate(final String date)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String olddate = this.date;
		this.date = date;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("date", olddate, this.date);
	}

	/**
	 * value internal setter
	 */
	protected synchronized void setValue(final String value)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldvalue = this.value;
		this.value = value;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("value", oldvalue, this.value);
	}
}
