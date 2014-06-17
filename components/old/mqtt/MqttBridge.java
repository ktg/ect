/*
<COPYRIGHT>

Copyright (c) 2006-2008, University of Nottingham
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
  Jan Humble (University of Nottingham)

 */
package equip.ect.components.mqtt;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Bridge to sensor data published via IBM MQTT protocol from a Broker. <H3>Summary</H3> Bridge to
 * sensor data published via IBM MQTT protocol from a Broker. Developed to connect to the
 * instrumented chemistry labs at Southampton University for the Semantic Media project. <H3>
 * Description</H3> Connects to configured IBM MQ-series Broker using MQTT client libraries and
 * subscribes to specified topic(s). Creates subcomponents for observed topics and publishes values
 * via those subcomponents. <H3>Installation</H3> Requires only IBM IA92 Java MQTT client libraries,
 * included. <H3>Configuration</H3> Set configServerUrl to (e.g.) "tcp://realtime.ngi.ibm.com:1883".
 * Set configTopics to (e.g.) "/SmartLab/Mimic/SHG/Env/#". Set configured to true. <H3>Usage</H3>
 * See above. <H3>Technical Details</H3> See Semantic Media project.
 * 
 * @displayName MQTT Bridge
 * @classification Hardware/Input
 * @preferred
 */
public class MqttBridge implements Serializable
{
	/**
	 * connection callback handler
	 */
	class Handler implements MqttSimpleCallback
	{
		XMLDataParser parser = new XMLDataParser();

		Handler() throws Exception
		{
		}

		public void connectionLost() throws java.lang.Exception
		{
			setStatus("Connection lost");
		}

		public void publishArrived(final java.lang.String thisTopicName, final byte[] thisPayload, final int QoS,
				final boolean retained) throws java.lang.Exception
		{
			try
			{
				final String msg = new String(thisPayload);
				final HashMap<String, String> map = parser.parseData(msg);
				final String data = map.get("data");
				final String value = map.get("value");
				final String time = map.get("time");

				setStatus("Received topic=" + thisTopicName + ": " + data + "=" + value + " at " + time);

				if (data != null)
				{
					final MqttTopic topic = getTopic(thisTopicName);
					topic.update(map);
				}
				// ....
			}
			catch (final Exception e)
			{
				setStatus("Error handling received topic=" + thisTopicName + ", " + thisPayload.length + " bytes: " + e);
			}
		}

	}

	/**
	 * config value
	 */
	protected String configTopics;
	/**
	 * config value
	 */
	protected boolean configured;
	/**
	 * config value
	 */
	protected String configServerUrl;
	/**
	 * static output value
	 */
	protected String status;
	static final String CLIENT_ID = "ect-mqtt-bridge";
	/**
	 * at most once QOS
	 */
	static final int QOS = 0;
	/**
	 * topics
	 */
	protected HashMap<String, MqttTopic> topics = new HashMap<String, MqttTopic>();
	/**
	 * client
	 */
	protected MqttClient client;
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public MqttBridge()
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

	/**
	 * input getter
	 */
	public synchronized String getConfigServerUrl()
	{
		return configServerUrl;
	}

	/**
	 * input getter
	 */
	public synchronized String getConfigTopics()
	{
		return configTopics;
	}

	/**
	 * input getter
	 */
	public synchronized boolean getConfigured()
	{
		return configured;
	}

	/**
	 * Status output getter
	 */
	public synchronized String getStatus()
	{
		return status;
	}

	/**
	 * topic as property getter
	 */
	public MqttTopic[] getTopics()
	{
		return topics.values().toArray(new MqttTopic[topics.size()]);
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
	 * Server URL, e.g. "tcp://realtime.ngi.ibm.com:1883"
	 * 
	 * @preferred
	 */
	public synchronized void setConfigServerUrl(final String configServerUrl)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldConfigServerUrl = this.configServerUrl;
		this.configServerUrl = configServerUrl;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configServerUrl", oldConfigServerUrl, this.configServerUrl);
	}

	/**
	 * Subscription topic(s), e.g. "/SmartLab/Mimic/SHG/Env/#"
	 * 
	 * @preferred
	 */
	public synchronized void setConfigTopics(final String configTopics)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldConfigTopics = this.configTopics;
		this.configTopics = configTopics;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configTopics", oldConfigTopics, this.configTopics);
	}

	/**
	 * Done configuration - run.
	 * 
	 * @preferred
	 */
	public synchronized void setConfigured(final boolean configured)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final boolean oldConfigured = this.configured;
		this.configured = configured;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("configured", oldConfigured, this.configured);
		if (configured)
		{
			stop();
			start();
		}
		else
		{
			stop();
			setStatus("Stopped");
		}
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		if (client != null)
		{
			try
			{
				client.disconnect();
			}
			catch (final Exception e)
			{
				System.err.println("Error stopping MqttBridge: " + e);
			}
			client = null;
		}
	}

	/** get/make topic */
	protected MqttTopic getTopic(final String topicName)
	{
		MqttTopic topic = topics.get(topicName);
		if (topic != null) { return topic; }
		// new topic
		topic = new MqttTopic(topicName);
		final MqttTopic oldvalue[] = getTopics();
		topics.put(topicName, topic);
		setStatus("Adding topic " + topicName);
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("topics", oldvalue, getTopics());
		return topic;
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setStatus(final String status)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldstatus = this.status;
		this.status = status;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("status", oldstatus, this.status);
	}

	/**
	 * start
	 */
	protected void start()
	{
		setStatus("Starting...");

		try
		{
			setStatus("Connect to " + configServerUrl);
			final MqttClient client = new MqttClient(configServerUrl);
			client.registerSimpleHandler(new Handler());
			// client id, clean start, keepalive
			client.connect(CLIENT_ID, true, (short) 60);
			setStatus("Subscribe to " + configTopics);
			client.subscribe(new String[] { configTopics }, new int[] { QOS });
			setStatus("Running...");
		}
		catch (final Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace(System.err);
			setStatus("Error: configServerUrl=" + configServerUrl + ", configTopics=" + configTopics + ": " + e);
		}
	}
}
