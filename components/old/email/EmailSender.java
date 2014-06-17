/*
<COPYRIGHT>

Copyright (c) 2004-2005, Lancaster University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Lancaster University
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

Created by: Craig Morrall (Lancaster University)
Contributors:
  Craig Morrall (Lancaster University)
  Alastair Hampshire (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.components.email;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * @classification Networked Services/Email
 * @technology Email
 * @preferred
 * @author Craig Morrall, Alastair Hampshire, Stefan Rennick Egglestone
 * @date 03/06/2004
 */
public class EmailSender extends Thread implements Serializable
{
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// properties
	private String sender;
	private String receiver;
	private String mailServer;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

	private String attention;

	public EmailSender()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getAttention()
	{
		return attention;
	}

	public String getMailServer()
	{
		return mailServer;
	}

	public DictionaryImpl getMessage()
	{
		return null;
	}

	public String getReceiver()
	{
		return receiver;
	}

	public String getSender()
	{
		return sender;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setMailServer(final String newValue)
	{
		final String oldValue = this.mailServer;
		this.mailServer = newValue;

		propertyChangeListeners.firePropertyChange("mailServer", oldValue, newValue);
	}

	public void setMessage(final DictionaryImpl message)
	{
		if (message != null)
		{
			sendMessage(message);
		}
	}

	public void setReceiver(final String newValue)
	{
		final String oldValue = this.receiver;
		this.receiver = newValue;

		propertyChangeListeners.firePropertyChange("receiver", oldValue, newValue);
	}

	public void setSender(final String newValue)
	{
		final String oldValue = this.sender;
		this.sender = newValue;

		propertyChangeListeners.firePropertyChange("sender", oldValue, newValue);
	}

	protected void sendMessage(final DictionaryImpl message)
	{
		if ((mailServer == null) || (sender == null) || (receiver == null))
		{
			setAttention("Failed to send message - you must supply a mail server, send and receiver");
			return;
		}

		setAttention("Trying to send message");

		String subjectString = "";
		String messageString = "";

		final Hashtable hash = message.getHashtable();

		if (hash.containsKey("_containsRSS"))
		{
			// get the title and description fields and use these
			// as the subject and message of this email

			final StringBoxImpl title = (StringBoxImpl) (hash.get("_rss.title"));
			subjectString = title.value;

			final StringBoxImpl desc = (StringBoxImpl) (hash.get("_rss.description"));
			messageString = desc.value;
		}
		else
		{
			subjectString = "<no subject>";

			if (hash.containsKey("value"))
			{
				final Object value = hash.get("value");

				if (value instanceof StringBoxImpl)
				{
					messageString = ((StringBoxImpl) value).value;
				}
				else
				{
					if (value instanceof String)
					{
						messageString = (String) value;
					}
					else
					{
						messageString = "<no message>";
					}
				}
			}
			else
			{
				messageString = "<no message>";
			}
		}

		InternetAddress toAddress = null;
		InternetAddress fromAddress = null;

		try
		{
			toAddress = new InternetAddress(receiver);
		}
		catch (final AddressException e)
		{
			setAttention("Badly formatted address in receiver property");
			return;
		}
		try
		{
			fromAddress = new InternetAddress(sender);
		}
		catch (final AddressException e)
		{
			setAttention("Badly formatted address in sender property");
			return;
		}

		try
		{
			final Properties props = new Properties();
			props.put("mail.smtp.host", mailServer);

			final Session mailsession = Session.getDefaultInstance(props, null);
			final Message msg = new MimeMessage(mailsession);
			msg.addRecipient(Message.RecipientType.TO, toAddress);

			msg.setSentDate(new Date());
			msg.setSubject(subjectString);
			msg.setFrom(fromAddress);
			msg.setText(messageString);
			Transport.send(msg);

			setAttention("Successfully sent message");
		}
		catch (final MessagingException e)
		{
			setAttention("Failed to send message");
		}
	}

	// methods to deal with email sending

	protected void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}
}
