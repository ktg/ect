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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)
  Stefan Rennick Egglestone
 */
package equip.ect.components.sms;

import equip.data.BooleanBoxImpl;
import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;
import org.gnu.stealthp.rsslib.RSSHandler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A component which can be used to send and receive SMS <h3>Description</h3> This component can be
 * used to send and receive SMS messages. To do this, it must be used on a computer which is
 * attached to a GSM device that can be fully controlled using AT commands (AT commands represent a
 * standard for controlling GSM devices from computers). It has been fully tested with an MTC45
 * GSM/GPRS modem (which uses a Siemens MC45 GPRS module), and is likely to work with any GSM modem.
 * It may also work when connected to a mobile phone (see your phone instructions for details of how
 * to connect your phone to a computer). However, although AT commands can be used to send SMS
 * through many mobile phones, it is common for AT commands which support the receiving of SMS not
 * to work properly. Hence, if you wish to receive SMS, the use of a GSM modem is recommended. <H3>
 * Installation</H3> With whatever device you wish to use, you have to attach it to your computer in
 * such a way as to make it visible as a COM port. GSM modems commonly connect directly to a
 * computer's serial port, and require no driver installation (since the serial port automatically
 * appears as a COM port). Newer modems may connect via USB, and require a driver to create a
 * virtual COM port representing the device. Mobile phones may also connect via USB and require
 * driver installation. Check your manual for more details. <h3>Configuration</h3> To configure the
 * component, first set the following properties to values which are correct for your device and
 * driver set-up (you may need to look these up in your manual)
 * <ul>
 * <li><i>configBaudRate</i> (rate of serial communication with device)
 * <li><i>configPort</i> (COM port which can be used to communicate with device)
 * </ul>
 * <P>
 * A typical baud rate for communicating with the MC45 GSM modem is <tt>19200</tt> baud.
 * </p>
 * <P>
 * Then set the <i>configured</i> property to <tt>true</tt>. If the component successfully connects
 * to the device using the settings that you have provided, then property <i>connected</i> will be
 * set to <tt>true</tt>. If not, then you will be given an error message (through property
 * <i>attention</i>) indicating the error.
 * </p>
 * <P>
 * Note that only one device can use a serial port at any one time, so you can only run one
 * component per GSM device. To disconnect from the GSM device, set <i>configured</t> to
 * <tt>false</tt>.
 * <h3>Usage</h3>
 * <P>
 * To send a message, enter the phone number you wish to send to into property <i>sendTo</i>, and
 * then enter the message you wish to send into property <i>sendMessage</i>. Depending upon the
 * properties of the GSM device you are using, there may be limits on the length of message that you
 * send (your message may be truncated if it is too long for your device to handle).
 * </p>
 * <P>
 * The most-recent message received will always be displayed through property
 * <i>receivedMessage</i>, and will be archived to a list of received messages on property
 * <i>messages</i>. Note that a message may be deleted from your GSM device by the component as soon
 * as it has received it, to save storage space on the SIM card being used by the device.
 * </p>
 * <H3>Technical Details</H3> The component uses the Java comm api to interact with a GSM device, by
 * sending AT commands over a comm connection.
 *
 * @classification Hardware/Input & Output
 * @defaultInputProperty sendMessage
 * @defaultOutputProperty receivedMessage
 * @technology SMS
 */
public class SMS implements Serializable, SMSListener
{

	// need to initialise the serial driver
	// to be used by this component. This means
	// that the javax.comm.properites file
	// (whose only job is to provide the details
	// of this driver) is not required by the component.

	public static final String SEND_TO_PROPERTY_NAME = "sendTo";
	public static final String SEND_MESSAGE_PROPERTY_NAME = "sendMessage";
	public static final String RECEIVED_FROM_PROPERTY_NAME = "receivedFrom";
	public static final String RECEIVED_MESSAGE_PROPERTY_NAME = "receivedMessage";
	public static final String RECEIVED_TIME_PROPERTY_NAME = "receivedTime";
	public static final String MESSAGES_PROPERTY_NAME = "messages";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	private static final int MAX_TEXT_LENGTH = 160;
	String port = "COM1";
	int rate = 19200;
	String attention = "";
	boolean configured = false;
	boolean connected = false;
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// Properties
	private String sendTo = null;
	private DictionaryImpl receivedMessage = null;
	private Vector messages = new Vector();
	private Thread smsThread = null;
	private SMSSendAndReceive sms = new SMSSendAndReceive();

	static
	{
		final String drivername = "com.sun.comm.Win32Driver";
		try
		{
			final javax.comm.CommDriver driver = (javax.comm.CommDriver) Class.forName(drivername).newInstance();
			driver.initialize();
		}
		catch (final Exception e)
		{
			System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
		}
	}

	public SMS()
	{
		sms.addSMSListener(this);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public String getAttention()
	{
		return attention;
	}

	public void setAttention(final String newAttention)
	{
		final String oldAttention = attention;
		attention = newAttention;

		propertyChangeListeners.firePropertyChange("attention", oldAttention, newAttention);
	}

	public int getConfigBaudRate()
	{
		return rate;
	}

	public void setConfigBaudRate(final int newRate)
	{
		final int oldRate = rate;
		rate = newRate;

		propertyChangeListeners.firePropertyChange("configBaudRate", oldRate, newRate);
	}

	public String getConfigPort()
	{
		return port;
	}

	public void setConfigPort(final String newPort)
	{
		final String oldPort = port;
		port = newPort;

		propertyChangeListeners.firePropertyChange("configPort", oldPort, newPort);
	}

	public boolean getConfigured()
	{
		return configured;
	}

	public void setConfigured(final boolean newValue)
	{
		if ((configured == false) && (newValue == true))
		{

			// try and connect the serial port

			if (sms.initPort(port, rate))
			{
				configured = true;

				propertyChangeListeners.firePropertyChange("configured", false, true);

				setAttention("Connected to device");
				setConnected(true);
			}
			else
			{
				setAttention("Failed to connect to device");
				setConnected(false);
			}
		}
		else
		{
			if ((configured == true) && (newValue == false))
			{
				// user wants to close down
				// existing connectoin

				sms.stop();

				// now wait for event
				// indicating that it has closed

				setAttention("Waiting for port to close");
			}
		}
	}

	public boolean getConnected()
	{
		return connected;
	}

	protected void setConnected(final boolean newValue)
	{
		final boolean oldValue = connected;
		connected = newValue;

		propertyChangeListeners.firePropertyChange("connected", oldValue, newValue);
	}

	public DictionaryImpl[] getMessages()
	{
		final DictionaryImpl[] toReturn = (DictionaryImpl[]) (messages.toArray(new DictionaryImpl[messages.size()]));

		return toReturn;
	}

	private void setMessages(final DictionaryImpl[] newMessages)
	{
		final DictionaryImpl[] oldValue = getMessages();

		messages.removeAllElements();

		if (newMessages != null)
		{
			for (final DictionaryImpl newMessage : newMessages)
			{
				messages.add(newMessage);
			}
		}

		final DictionaryImpl[] newValue = getMessages();

		propertyChangeListeners.firePropertyChange(SMS.MESSAGES_PROPERTY_NAME, oldValue, newValue);
	}

	public DictionaryImpl getReceivedMessage()
	{
		return receivedMessage;
	}

	private void setReceivedMessage(final DictionaryImpl newMessage)
	{
		final DictionaryImpl oldMessage = receivedMessage;
		receivedMessage = newMessage;

		propertyChangeListeners.firePropertyChange(SMS.RECEIVED_MESSAGE_PROPERTY_NAME, oldMessage, newMessage);

	}

	public DictionaryImpl getSendMessage()
	{
		return null;
	}

	public void setSendMessage(final DictionaryImpl sendMessage)
	{
		if ((sendMessage != null) && (connected == true))
		{
			System.out.println("should send message");

			final Hashtable hash = sendMessage.getHashtable();

			if (hash.containsKey("_containsRSS"))
			{
				final StringBoxImpl title = (StringBoxImpl) (hash.get("_rss.title"));
				final StringBoxImpl description = (StringBoxImpl) (hash.get("_rss.description"));

				final String toSend = title.value + "..." + description.value;
				sendSMS(toSend);
			}
			else
			{
				if (hash.containsKey("value"))
				{
					final Object value = hash.get("value");

					if (value instanceof StringBoxImpl)
					{
						sendSMS(((StringBoxImpl) value).value);
						return;
					}
					if (value instanceof String)
					{
						sendSMS((String) value);
					}
				}
			}
		}
	}

	public String getSendTo()
	{
		return sendTo;
	}

	public void setSendTo(final String sendTo)
	{
		propertyChangeListeners.firePropertyChange(SMS.SEND_TO_PROPERTY_NAME, this.sendTo, sendTo);
		this.sendTo = sendTo;
	}

	public Object getTriggerClearMessages()
	{
		return null;
	}

	public void setTriggerClearMessages(final Object trigger)
	{
		setMessages(null);
	}

	public Object getTriggerClearReceivedMessage()
	{
		return null;
	}

	public void setTriggerClearReceivedMessage(final Object trigger)
	{
		setReceivedMessage(null);
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void sendSMS(String sendMessage)
	{
		System.out.println("actual send sms");

		final String to = getSendTo();

		if ((to == null) || (to.trim().equals("")))
		{
			setAttention("You must provide a phone number in the sendTo property");
		}
		else
		{
			setAttention("Trying to send message");

			if (sendMessage.length() > MAX_TEXT_LENGTH)
			{
				sendMessage = sendMessage.substring(0, 160);
			}

			try
			{
				sms.addSMSMessage(to, sendMessage);
			}
			catch (final Exception e)
			{
				setAttention("Exception when sending message");
				System.out.println("WARNING: error sending sms " + e);
			}
		}
	}

	@Override
	public void serialPortClosed(final boolean success)
	{
		if (success == true)
		{
			setAttention("Successfully closed port");
			setConnected(false);

			configured = false;

			propertyChangeListeners.firePropertyChange("configured", "true", "false");

			sms = new SMSSendAndReceive();
			sms.addSMSListener(this);
		}
		else
		{
			setAttention("Failed to close port");
		}

	}

	@Override
	public void serialPortInitialised()
	{

		smsThread = new Thread(sms);
		smsThread.start();
	}

	@Override
	public void smsReceived(final SMSMessage sms, final String[] responses)
	{
		setAttention("New message received");

		try
		{
			final DictionaryImpl newMessage = new DictionaryImpl();

			newMessage.put("from", new StringBoxImpl(sms.getNumber()));
			newMessage.put("message", new StringBoxImpl(sms.getMessage()));
			newMessage.put("time", new StringBoxImpl(sms.getTimestamp()));

			newMessage.put("_smallDisplayKeys", new StringBoxImpl("from,message"));
			newMessage.put("_largeDisplayKeys", new StringBoxImpl("from,message,time"));
			newMessage.put("_containsRSS", new BooleanBoxImpl(true));

			final Date currentDate = new Date();
			final String currentDateString = sdf.format(currentDate);

			final String title = "New SMS from " + sms.getNumber();
			final String description = sms.getMessage();

			newMessage.put("_rss." + RSSHandler.TITLE_TAG, new StringBoxImpl(title));
			newMessage.put("_rss." + RSSHandler.DESCRIPTION_TAG, new StringBoxImpl(description));
			newMessage.put("_rss." + RSSHandler.PUB_DATE_TAG, new StringBoxImpl(currentDateString));

			setReceivedMessage(newMessage);
			addMessage(newMessage);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void smsSent(final SMSMessage sms, final String[] responses)
	{
		setAttention("Message sent successfully");
	}

	@Override
	public void smsSentFailed(final SMSMessage sms, final String[] responses)
	{
		setAttention("Failed to send message");
	}

	public synchronized void stop()
	{
		sms.stop();
	}

	private void addMessage(final DictionaryImpl newMessage)
	{
		final DictionaryImpl[] oldValue = getMessages();

		messages.add(newMessage);

		final DictionaryImpl[] newValue = getMessages();

		propertyChangeListeners.firePropertyChange(SMS.MESSAGES_PROPERTY_NAME, oldValue, newValue);
	}

}
