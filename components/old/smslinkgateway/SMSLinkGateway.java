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

Created by: Adrian Friday (Lancaster University)
Contributors:
  Adrian Friday (Lancaster University)

 */
package equip.ect.components.smslinkgateway;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;

/**
 * 
 * Bridges to SMSLink Gateway
 * 
 * @author Adrian Friday
 * @date 29/07/2004
 */

public class SMSLinkGateway extends Thread implements Serializable, PropertyChangeListener
{
	// Property Change

	private class MessageToSend
	{
		private final int MAXINPUTLINE = 80;

		public String receiverNumber, senderNumber, messageToSend;

		public MessageToSend(final String receiver, final String sender, final String message)
		{
			receiverNumber = receiver;
			senderNumber = sender;
			messageToSend = message;
		}

		public void Send(final PrintWriter out, final BufferedReader in, final boolean first, final boolean last)
		{
			System.out.println("SMSLinkGateway: Sending \"" + messageToSend + "\" to " + receiverNumber);

			// Send the SMS

			if (first)
			{
				WaitForPrompt(in);
			}

			SendString(out, "set user=\"" + senderNumber + "\"");

			WaitForPrompt(in);

			SendString(out, "set dest=\"" + receiverNumber + "\"");

			WaitForPrompt(in);

			SendString(out, "set msg=\"" + messageToSend + "\"");

			WaitForPrompt(in);

			SendString(out, "send");

			WaitForPrompt(in);

			if (last)
			{
				SendString(out, "quit");
			}
		}

		public void SendString(final PrintWriter out, final String stringToWrite)
		{
			out.print(stringToWrite + "\n");
			System.out.println(stringToWrite);
			out.flush();
		}

		private void WaitForPrompt(final BufferedReader in)
		{
			// Wait for 'SMS>' prompt

			final char[] inputString = new char[MAXINPUTLINE];

			try
			{
				while (true)
				{
					// Bytes to read from terminal

					final int bytesRead = in.read(inputString, 0, MAXINPUTLINE);

					if (bytesRead <= 0)
					{
						break;
					}

					final String inputAsString = new String(inputString, 0, bytesRead).trim();

					if (inputAsString.indexOf("SMS>") > -1)
					{
						System.out.print(inputAsString + " ");
						break;
					}

					System.out.println(inputAsString);
				}
			}
			catch (final IOException e)
			{
				System.out.println(e.toString());
			}
		}
	}

	// Properties to expose

	private class OutgoingQueueManager implements Runnable
	{
		private Vector outboundQueue = new Vector();

		private volatile boolean running = true;

		public OutgoingQueueManager()
		{
			new Thread(this).start();
		}

		public void Add(final MessageToSend msg)
		{
			outboundQueue.add(msg);
		}

		@Override
		public void run()
		{
			System.out.println("SMSLinkGateway: Dispatcher thread ready...");

			while (running)
			{
				if (!outboundQueue.isEmpty())
				{
					// Contact server and connect to SMSLink

					Socket smsLinkSocket;
					PrintWriter out;
					BufferedReader in;

					System.out
							.println("SMSLinkGateway: Contacting server " + smsLinkServer + " on port " + smsLinkPort);

					try
					{
						smsLinkSocket = new Socket(InetAddress.getByName(smsLinkServer), Integer.parseInt(smsLinkPort));
						out = new PrintWriter(smsLinkSocket.getOutputStream());
						in = new BufferedReader(new InputStreamReader(smsLinkSocket.getInputStream()));

					}
					catch (final Exception e)
					{
						System.out.println("SMSLinkGateway: connection failed " + e.toString() + " so, retrying");

						continue;
					}

					boolean firstMessage = true;

					do
					{
						// Got a message, let's send it!

						((MessageToSend) outboundQueue.firstElement()).Send(out, in, firstMessage,
																			outboundQueue.size() == 1);

						// Remove it from the queue

						outboundQueue.remove(0);

						firstMessage = false;
					}
					while (!outboundQueue.isEmpty());

					try
					{
						in.close();
						out.close();
						smsLinkSocket.close();
					}
					catch (final IOException e)
					{
						System.out.println("Can't close connection " + e.toString());
					}
				}

				try
				{
					Thread.sleep(5);
				}
				catch (final Exception e)
				{
					// Terminate thread

					running = false;
				}
			}

			System.out.println("SMSLinkGateway: Dispatcher thread, stopped.");
		}

		public void stop()
		{
			running = false;
		}
	}

	/**
	 * main method
	 */

	public static void main(final String args[])
	{
		final SMSLinkGateway gateway = new SMSLinkGateway();
	}

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private String senderNumber, receiverNumber, messageToSend, smsLinkServer = "148.88.153.221", smsLinkPort = "6701";

	private OutgoingQueueManager outputHandler;

	// Property Change Listeners

	public SMSLinkGateway()
	{
		addPropertyChangeListener(this);
		outputHandler = new OutgoingQueueManager();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	// Signal a property change event

	public String getMessageToSend()
	{
		return messageToSend;
	}

	// Set methods

	public String getReceiverNumber()
	{
		return receiverNumber;
	}

	public String getSenderNumber()
	{
		return senderNumber;
	}

	public String getSmsLinkPort()
	{
		return smsLinkPort;
	}

	public String getSmsLinkServer()
	{
		return smsLinkServer;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
	}

	// Get Methods

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setMessageToSend(final String newMessage)
	{
		if (newMessage != messageToSend)
		{
			propertyChangeListeners.firePropertyChange("messageToSend", messageToSend, newMessage);
			messageToSend = newMessage;

			// Add to outbound queue

			outputHandler.Add(new MessageToSend(receiverNumber, senderNumber, messageToSend));
		}
	}

	public void setReceiverNumber(final String newNumber)
	{
		if (newNumber != receiverNumber)
		{
			propertyChangeListeners.firePropertyChange("receiverNumber", receiverNumber, newNumber);
			receiverNumber = newNumber;
		}
	}

	public void setSenderNumber(final String newNumber)
	{
		if (newNumber != senderNumber)
		{
			propertyChangeListeners.firePropertyChange("senderNumber", senderNumber, newNumber);
			senderNumber = newNumber;
		}
	}

	public void setSmsLinkPort(final String newPort)
	{
		if (newPort != smsLinkPort)
		{
			propertyChangeListeners.firePropertyChange("smsLinkPort", smsLinkPort, newPort);
			smsLinkPort = newPort;
		}
	}

	// Methods to deal with SMS sending

	public void setSmsLinkServer(final String newServer)
	{
		if (newServer != smsLinkServer)
		{
			propertyChangeListeners.firePropertyChange("smsLinkServer", smsLinkServer, newServer);
			smsLinkServer = newServer;
		}
	};

	@Override
	protected void finalize()
	{
		System.out.println("SMSLinkGateway: finalize() stopping thread");
		outputHandler.stop();
	}
}
