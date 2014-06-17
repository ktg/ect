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

  Created by: Alastair Hampshire (University of Nottingham)
  Contributors:
  Alastair Hampshire (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.email;

import equip.data.BooleanBoxImpl;
import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;
import org.gnu.stealthp.rsslib.RSSHandler;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * A simple component which collects the latest email from a mail server. <H3>Configuration</H3>
 * <p/>
 * To configure the EmailReceiver, you will need the following items of information:
 * </P>
 * <ul>
 * <li>address of machine hosting your email
 * <li>your user name on this machine
 * <li>your password on this machine
 * <li>the protocol that should be used to communicate with your email server
 * </ul>
 * <p/>
 * Enter these items of information into, respectively, the ECT properties labelled
 * <i>configHost</i>, <i>configUsername</i>, <i>configPassword</i> and <i>configProtocol</i> (you
 * will not have to modify the protocol if you are using IMAP). Optionally, you may also change
 * <i>configFolderName</i> and <i>configPort</i>.
 * </P>
 * <p/>
 * Now change the <i>configured</i> property to value <tt>true</tt>, and the component should
 * connect to your email account. If this is the case, then the <i>connected</i> property should
 * change to <tt>true</tt>, and the <i>messageCount</i> property should change to an indication of
 * the number of mails in the selected folder.
 * </P>
 * <p/>
 * To disconnect, set <i>configured</t> to <tt>false</tt>.
 * </P>
 * <H3>Usage</H3>
 * <p/>
 * When new email messages are received, they will be placed onto the <i>messages</i> property. If
 * multiple emails are received at once, then an array of emails may be placed onto this property.
 * Emails might be processed by other ECT components eg see ArrayPlayer,
 * SmallDisplayStringExtractor, TestingMessageOutput...
 * </P>
 *
 * @classification Networked Services/Email
 * @preferred
 * @technology Email
 * @defaultOutputProperty messages
 */
public class EmailReceiver implements Serializable, MessageCountListener
{

	private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
	private static final int DESCRIPTION_TRUNCATE_LENGTH = 160;
	/**
	 * Refresh period in seconds. I.e. checks for new mail every x seconds.
	 */

	private final int DEFAULT_REFRESH_PERIOD = 10;
	private String refreshPeriod = DEFAULT_REFRESH_PERIOD + "";
	private int refreshPeriodInt = DEFAULT_REFRESH_PERIOD;
	Thread messageCountThread = null;
	Thread connectionThread;
	private Folder folder;
	private Store store;
	private int msgId = 0;
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// Properties
	private String protocol = "imap";
	private String host = null;
	private String port = "-1";
	private String folderName = "inbox";
	private String username = null;
	private String password = null;
	private String attention = null;
	private boolean configured = false;
	private DictionaryImpl messages[];
	private boolean connected = false;
	private String searchSubject = "";
	private String searchFrom = "";
	private int messageCount = -1;

	public EmailReceiver()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	protected synchronized void setAttention(final String newAttention)
	{
		final String oldAttention = this.attention;
		this.attention = newAttention;

		propertyChangeListeners.firePropertyChange("attention", oldAttention, newAttention);
	}

	public synchronized String getConfigFolderName()
	{
		return folderName;

	}

	public synchronized void setConfigFolderName(final String newFolderName)
	{
		final String oldFolderName = folderName;

		this.folderName = newFolderName;

		propertyChangeListeners.firePropertyChange("configFolderName", oldFolderName, newFolderName);
	}

	public synchronized String getConfigHost()
	{
		return host;
	}

	public synchronized void setConfigHost(final String newHost)
	{
		final String oldHost = host;
		this.host = newHost;

		propertyChangeListeners.firePropertyChange("configHost", oldHost, newHost);
	}

	public synchronized String getConfigPassword()
	{
		return password;
	}

	public synchronized void setConfigPassword(final String newPassword)
	{
		final String oldPassword = password;

		this.password = newPassword;

		propertyChangeListeners.firePropertyChange("configPassword", oldPassword, newPassword);
	}

	public synchronized String getConfigPort()
	{
		return port;
	}

	public synchronized void setConfigPort(final String newPort)
	{
		// need to check that port is a number!

		try
		{
			final int portInt = new Integer(newPort).intValue();

			if (portInt <= 0 && portInt != -1)
			{
				setAttention("Must be above 0");
				return;
			}
		}
		catch (final NumberFormatException nfe)
		{
			setAttention("Must be a number");
			return;
		}

		final String oldPort = this.port;
		this.port = newPort;

		propertyChangeListeners.firePropertyChange("configPort", oldPort, newPort);
	}

	public synchronized String getConfigProtocol()
	{
		return protocol;
	}

	public synchronized void setConfigProtocol(final String newProtocol)
	{
		final String oldProtocol = protocol;

		this.protocol = newProtocol;

		propertyChangeListeners.firePropertyChange("configProtocol", oldProtocol, newProtocol);
	}

	public synchronized boolean getConfigured()
	{
		return configured;
	}

	public synchronized void setConfigured(boolean newValue)
	{
		// whilst the value of configured is true, the user
		// is indicating that they want the component to
		// try and connect to a specified email server

		// if they change it to false, then the component
		// will try and disocnnect if it is connected

		final boolean oldValue = this.configured;

		if ((newValue == true) && (oldValue == false))
		{

			try
			{
				tryToConnect();
			}
			catch (final ConfigurationException e)
			{
				setAttention("Property " + e.getMessage() + " is not configured properly");
				newValue = false;
			}
		}

		if ((newValue == false) && (oldValue == true))
		{
			tryToDisconnect();
		}

		this.configured = newValue;
		propertyChangeListeners.firePropertyChange("configured", oldValue, newValue);
	}

	public synchronized String getConfigUsername()
	{
		return username;
	}

	public synchronized void setConfigUsername(final String newUsername)
	{
		final String oldUsername = this.username;

		this.username = newUsername;

		propertyChangeListeners.firePropertyChange("configUsername", oldUsername, newUsername);
	}

	public synchronized boolean getConnected()
	{
		return connected;
	}

	protected synchronized void setConnected(final boolean newValue)
	{
		final boolean oldValue = this.connected;
		this.connected = newValue;

		propertyChangeListeners.firePropertyChange("connected", oldValue, newValue);
	}

	public synchronized int getMessageCount()
	{
		return messageCount;
	}

	private void setMessageCount(final int newCount)
	{
		final int oldCount = this.messageCount;
		this.messageCount = newCount;

		propertyChangeListeners.firePropertyChange("messageCount", oldCount, newCount);
	}

	public synchronized DictionaryImpl[] getMessages()
	{
		return messages;
	}

	protected synchronized void setMessages(final DictionaryImpl[] newMessage)
	{
		final DictionaryImpl[] oldMessage = this.messages;
		this.messages = newMessage;

		propertyChangeListeners.firePropertyChange("messages", oldMessage, newMessage);
	}

	public synchronized String getRefreshPeriod()
	{
		return refreshPeriod;
	}

	public synchronized void setRefreshPeriod(final String newRefreshPeriod)
	{
		final String oldRefreshPeriod = this.refreshPeriod;

		// need to check if supplied refresh period is a valid integer

		boolean foundException = false;

		try
		{
			refreshPeriodInt = new Integer(newRefreshPeriod).intValue();
		}
		catch (final NumberFormatException e)
		{
			foundException = true;
		}

		if (foundException == false)
		{
			if (refreshPeriodInt > 0)
			{
				this.refreshPeriod = newRefreshPeriod;
			}
			else
			{
				setAttention("refreshPeriod must be greater than 0");
				this.refreshPeriod = DEFAULT_REFRESH_PERIOD + "";
				refreshPeriodInt = DEFAULT_REFRESH_PERIOD;
			}
		}
		else
		{
			setAttention("refreshPeriod must be a valid number.");
			this.refreshPeriod = DEFAULT_REFRESH_PERIOD + "";
			refreshPeriodInt = DEFAULT_REFRESH_PERIOD;
		}

		propertyChangeListeners.firePropertyChange("refreshPeriod", oldRefreshPeriod, this.refreshPeriod);

		// wake up any sleeping thread and check
		// in case we've gone from a long waiting period to a short waiting period
		if (messageCountThread != null)
		{
			messageCountThread.interrupt();
		}
	}

	@Override
	public void messagesAdded(final MessageCountEvent e)
	{
		final Message[] messages = e.getMessages();
		processMessages(messages);
	}

	@Override
	public void messagesRemoved(final MessageCountEvent e)
	{
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void stop()
	{
		tryToDisconnect();
	}

	public String stringifyAddresses(final Address[] address)
	{
		String addStr = "";
		for (final Address addres : address)
		{
			if (addStr.equals(""))
			{
				addStr += addres.toString();
			}
			else
			{
				addStr += ", " + addres.toString();
			}
		}
		return addStr;
	}

	void processMessages(final Message[] messages)
	{
		final Vector dictionaries = new Vector();

		for (int i = 0; i < messages.length; i++)
		{
			try
			{
				// stored in the data structure used to represent
				// the email message
				// indicates which order the fields from the email
				// should be displayed on a screen only capable
				// of displaying limited information from the
				// email, and on a screen capable of displaying
				// all the information in the email

				final Address[] from = messages[i].getFrom();
				final String fromStr = stringifyAddresses(from);

				final String subject = messages[i].getSubject();

				final Object content = messages[i].getContent();
				final String contentType = messages[i].getContentType();
				String contentStr = null;
				if (contentType.startsWith("TEXT/PLAIN") || contentType.startsWith("TEXT/HTML"))
				{
					if (content instanceof String)
					{
						contentStr = (String) content;
					}
				}
				else
				{
					// setAttention("Content Type: " + contentType);
					contentStr = "Message contents of type " + contentType + " cannot be stored";
				}

				String ccStr = null;
				String bccStr = null;
				String toStr = null;

				final Address[] to = messages[i].getRecipients(Message.RecipientType.TO);
				if (to != null)
				{
					toStr = stringifyAddresses(to);
				}

				final Address[] cc = messages[i].getRecipients(Message.RecipientType.CC);

				if (cc != null)
				{
					ccStr = stringifyAddresses(cc);
				}
				final Address[] bcc = messages[i].getRecipients(Message.RecipientType.BCC);
				if (bcc != null)
				{
					bccStr = stringifyAddresses(bcc);
				}

				final String _smallDisplayKeys = "from,subject";
				final String _largeDisplayKeys = "to,from,subject,cc,bcc,contentType,messageBody";

				final boolean containsRSS = true;

				final String rssTitle = "New email from " + fromStr;

				String rssDescription = "";

				if ((subject == null) || (subject.trim().length() == 0))
				{
					rssDescription = "<no subject>";
				}
				else
				{
					rssDescription = "subject: " + subject;
				}

				/*
				 * if(contentStr != null) { rssDescription = rssDescription + "    message: " +
				 * contentStr; }
				 */

				if (rssDescription.length() > DESCRIPTION_TRUNCATE_LENGTH)
				{
					rssDescription = rssDescription.substring(0, DESCRIPTION_TRUNCATE_LENGTH) + "...";
				}

				final Date currentDate = new Date();
				final String receivedDate = sdf.format(currentDate);

				// build the message as a dictionary
				final DictionaryImpl dict = new DictionaryImpl();
				dict.put("from", new StringBoxImpl(fromStr));
				dict.put("contentType", new StringBoxImpl(contentType));
				dict.put("messageBody", new StringBoxImpl(contentStr));
				dict.put("subject", new StringBoxImpl(subject));
				dict.put("to", new StringBoxImpl(toStr));
				dict.put("cc", new StringBoxImpl(ccStr));
				dict.put("bcc", new StringBoxImpl(bccStr));

				dict.put("_smallDisplayKeys", new StringBoxImpl(_smallDisplayKeys));
				dict.put("_largeDisplayKeys", new StringBoxImpl(_largeDisplayKeys));

				dict.put("_containsRSS", new BooleanBoxImpl(containsRSS));
				dict.put("_rss." + RSSHandler.TITLE_TAG, new StringBoxImpl(rssTitle));
				dict.put("_rss." + RSSHandler.DESCRIPTION_TAG, new StringBoxImpl(rssDescription));
				dict.put("_rss." + RSSHandler.PUB_DATE_TAG, new StringBoxImpl(receivedDate));

				dictionaries.add(dict);
			}
			catch (final MessagingException e)
			{
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}

		final DictionaryImpl[] messageDict = (DictionaryImpl[]) (dictionaries.toArray(new DictionaryImpl[dictionaries
				.size()]));

		setMessages(messageDict);
	}

	void startConnectionThread()
	{
		connectionThread = new Thread(new ConnectionRunnable());
		connectionThread.start();
	}

	void startMessageCountThread()
	{

		messageCountThread = new Thread(new MessageCountRunnable());
		messageCountThread.start();
	}

	void stopConnectionThread()
	{
		if ((connectionThread != null) && (connectionThread.isAlive()))
		{
			connectionThread.interrupt();
			try
			{
				// wait before the thread has died before returning
				connectionThread.join();
			}
			catch (final InterruptedException e)
			{
			}
		}
	}

	void stopMessageCountThread()
	{
		if (messageCountThread != null && messageCountThread.isAlive())
		{
			messageCountThread.interrupt();

			try
			{
				// wait until thread dies before returning
				messageCountThread.join();
			}
			catch (final InterruptedException e)
			{
			}
		}
	}

	void tryToConnect() throws ConfigurationException
	{
		if (host == null)
		{
			throw (new ConfigurationException("host"));
		}

		if (username == null)
		{
			throw (new ConfigurationException("username"));
		}

		if (password == null)
		{
			throw (new ConfigurationException("password"));
		}

		if (folderName == null)
		{
			throw (new ConfigurationException("folderName"));
		}

		startConnectionThread();
	}

	private void tryToDisconnect()
	{
		stopConnectionThread();
		stopMessageCountThread();

		if (store != null && store.isConnected())
		{
			try
			{
				store.close();
				setAttention("Not connected");
				setConnected(false);

				folder.removeMessageCountListener(this);

				setMessages(null);
			}
			catch (final javax.mail.MessagingException me)
			{
				setAttention("Failed to close connection: " + me.getMessage());
				setConnected(true);
			}
		}
	}

	class ConfigurationException extends Exception
	{
		ConfigurationException(final String fieldName)
		{
			super(fieldName);
		}
	}

	class ConnectionException extends Exception
	{
		ConnectionException()
		{
			super();
		}
	}

	class ConnectionRunnable implements Runnable
	{
		@Override
		public void run()
		{
			boolean shouldLoop = true;

			// try to connect
			int portInt = 0;

			if (port == null)
			{
				portInt = -1;
			}
			else
			{
				portInt = new Integer(port).intValue();
			}

			final Session session = Session.getDefaultInstance(System.getProperties());
			final URLName urlName = new URLName(protocol, host, portInt, "", username, password);

			while (shouldLoop)
			{
				setAttention("Trying to connect");

				try
				{
					try
					{
						store = session.getStore(urlName);
						store.connect(host, username, password);
						folder = store.getFolder(folderName);

						shouldLoop = false;

						setAttention("Successfully connected");
						setConnected(true);

						if (folder.exists())
						{
							folder.open(Folder.READ_ONLY);
							folder.addMessageCountListener(EmailReceiver.this);

							// start a thread that monitors the message
							// count. By doing this, a periodic action
							// is being undertaken which is essential
							// in keeping the connectoin alive
							startMessageCountThread();

						}
					}

					catch (final javax.mail.NoSuchProviderException nspe)
					{
						nspe.printStackTrace();
						setConnected(false);
						setAttention("Failed to connect. Will retry");
						Thread.sleep(refreshPeriodInt * 1000);
					}
					catch (final javax.mail.MessagingException me)
					{
						me.printStackTrace();
						setAttention("Failed to connect. Will retry");
						setConnected(false);
						Thread.sleep(refreshPeriodInt * 1000);
					}
				}
				catch (final InterruptedException ie)
				{
					setConnected(false);
					shouldLoop = false;
				}
			}
		}
	}

	class MessageCountRunnable implements Runnable
	{

		// polls message count property
		// this triggers the mail folder to forward any events
		// about new messages, which are then intercepted by the component

		@Override
		public void run()
		{
			boolean shouldLoop = true;

			try
			{
				while (shouldLoop)
				{
					try
					{
						final int messageCount = folder.getMessageCount();
						setMessageCount(messageCount);
						Thread.sleep(refreshPeriodInt * 1000);
					}

					catch (final MessagingException me)
					{
						// something has gone wrong with the messaging service
						// start a thread to periodically try to reconnect

						shouldLoop = false;
						startConnectionThread();
					}
				}
			}
			catch (final InterruptedException ie)
			{
			}
		}
	}
}
