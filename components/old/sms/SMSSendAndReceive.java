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

 */
package equip.ect.components.sms;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

public class SMSSendAndReceive implements Runnable
{

	private InputStream is = null;
	private OutputStream os = null;

	private boolean stop = false;
	private Object stopLock = new Object();

	private Vector listeners = new Vector();
	private Object listenersLock = new Object();

	private Vector sendQueue = new Vector();
	private Object sendQueueLock = new Object();

	private Vector receivedMessages = new Vector();
	private Object receivedMessagesLock = new Vector();

	public static final String DEFAULT_PORT = "COM1";
	public static final int DEFAULT_BAUD_RATE = 115200;
	public static final int DEFAULT_IO_DELAY_MS = 100;

	public static final String ERROR_CODE = "ERROR";
	public static final String OK_CODE = "OK";

	protected String[] initATCommands = { "ATE0\r", /* disable local echo */
	"AT+CMGF=1\r", /* enable text mode */
	"AT+CNMI=1,1,0,0,1\r" /* enable message indication */
	};

	public static final String AT_SMS_SEND = "AT+CMGS=";
	public static final String AT_SMS_RECEIVE = "AT+CMGR=";
	public static final String AT_SMS_DELETE = "AT+CMGD=";
	public static final String AT_MESSAGE_RECEIVED = "+CMTI";

	private SerialPort port = null;
	private String portName = DEFAULT_PORT;
	private int baud = DEFAULT_BAUD_RATE;
	private int delay = DEFAULT_IO_DELAY_MS;
	private int initialDelete = 5;

	public SMSSendAndReceive()
	{
		this(DEFAULT_PORT, DEFAULT_BAUD_RATE, DEFAULT_IO_DELAY_MS);
	}

	public SMSSendAndReceive(final String portName, final int baud, final int delay)
	{
		this.portName = portName;
		this.baud = baud;
		this.delay = delay;
		initSMS();
	}

	public void addCommand(final String command)
	{
		if (command != null)
		{
			synchronized (sendQueueLock)
			{
				sendQueue.add(command);
			}
		}
	}

	public void addSMSListener(final SMSListener listener)
	{
		if (listener != null)
		{
			synchronized (listenersLock)
			{
				if (!listeners.contains(listener))
				{
					listeners.add(listener);
				}
			}
		}
	}

	public void addSMSMessage(final String number, final String message)
	{
		synchronized (sendQueueLock)
		{
			sendQueue.add(new SMSMessage(number, message));
		}
	}

	public int getDelay()
	{
		return delay;
	}

	public SMSMessage[] getPendingMessages()
	{
		final Vector pending = new Vector();
		Object object = null;
		synchronized (sendQueueLock)
		{
			for (int i = 0; i < sendQueue.size(); i++)
			{
				object = sendQueue.elementAt(i);
				if (object instanceof SMSMessage)
				{
					pending.add(object);
				}
			}
			return (SMSMessage[]) pending.toArray(new SMSMessage[0]);
		}
	}

	public SMSMessage[] getReceivedMessages()
	{
		synchronized (receivedMessagesLock)
		{
			return (SMSMessage[]) receivedMessages.toArray(new SMSMessage[0]);
		}
	}

	public void removeSMSListener(final SMSListener listener)
	{
		if (listener != null)
		{
			synchronized (listenersLock)
			{
				listeners.remove(listener);
			}
		}
	}

	@Override
	public void run()
	{
		Object object = null;
		String command = null;
		SMSMessage message = null;
		String data[] = null;
		while (!stopped())
		{
			try
			{
				synchronized (sendQueueLock)
				{
					if (is.available() < 1)
					{
						if (sendQueue.size() > 0)
						{
							// send next command
							object = sendQueue.elementAt(0);
							if (object instanceof String)
							{
								command = (String) object;
								if (sendData(command))
								{
									processResponse(command, readData(true, true));
									sendQueue.remove(0);
								}
							}
							else if (object instanceof SMSMessage)
							{
								message = (SMSMessage) object;
								if (sendSMS(message))
								{
									processResponse(message, readData(true, true));
									sendQueue.remove(0);
								}
							}
						}
					}
				}
				data = readData(false, false);
				if (data != null && data.length == 1)
				{
					processIncoming(data[0]);
				}
				Thread.sleep(delay);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			is.close();
			os.close();
			port.close();

			// serial port has closed successfully

			fireSerialPortClosed(true);

		}
		catch (final Exception e)
		{

			// serial port has failed to close successfully
			fireSerialPortClosed(false);

			e.printStackTrace();
		}
	}

	public void setDelay(final int delay)
	{
		this.delay = delay;
	}

	public void stop()
	{
		synchronized (stopLock)
		{
			stop = true;
		}
	}

	public boolean stopped()
	{
		synchronized (stopLock)
		{
			return stop;
		}
	}

	boolean initPort(final String portName, final int baud)
	{
		enumPorts(portName);
		if (port != null)
		{
			try
			{
				// System.out.println("start of set serial params");

				port.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				is = port.getInputStream();
				os = port.getOutputStream();

				// System.out.println("end of set serial params");

				fireSerialPortInitialised();
				return true;
			}
			catch (final Exception e)
			{
				System.err.println("Error initialising port: " + portName + " " + baud);
				e.printStackTrace();
			}
		}

		return false;
	}

	protected void enumPorts(final String portName)
	{
		try
		{
			CommPortIdentifier portId = null;
			final Enumeration portList = CommPortIdentifier.getPortIdentifiers();

			// System.out.println("Searching through ports");
			while (portList.hasMoreElements())
			{

				portId = (CommPortIdentifier) portList.nextElement();

				// System.out.println("found " + portId.getName());

				if (portId.getPortType() == 1 && portId.getName().equalsIgnoreCase(portName))
				{
					port = (SerialPort) portId.open("ECT:SMS", 100);
					return;
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void fireSerialPortClosed(final boolean success)
	{
		synchronized (listenersLock)
		{
			for (int i = 0; i < listeners.size(); i++)
			{
				((SMSListener) listeners.elementAt(i)).serialPortClosed(success);
			}
		}
	}

	protected void fireSerialPortInitialised()
	{
		synchronized (listenersLock)
		{

			// System.out.println("start of init");

			for (int i = 0; i < listeners.size(); i++)
			{

				System.out.println("firing init");

				((SMSListener) listeners.elementAt(i)).serialPortInitialised();
			}
		}
	}

	protected void fireSMSReceived(final SMSMessage sms, final String[] responses)
	{
		if (sms != null)
		{
			synchronized (receivedMessagesLock)
			{
				if (!receivedMessages.contains(sms))
				{
					receivedMessages.add(sms);
				}
			}
			synchronized (listenersLock)
			{
				for (int i = 0; i < listeners.size(); i++)
				{
					((SMSListener) listeners.elementAt(i)).smsReceived(sms, responses);
				}
			}
		}
	}

	protected void fireSMSSent(final SMSMessage sms, final String[] responses)
	{
		synchronized (listenersLock)
		{
			for (int i = 0; i < listeners.size(); i++)
			{
				((SMSListener) listeners.elementAt(i)).smsSent(sms, responses);
			}
		}
	}

	protected void fireSMSSentFailed(final SMSMessage sms, final String[] responses)
	{
		synchronized (listenersLock)
		{
			for (int i = 0; i < listeners.size(); i++)
			{
				((SMSListener) listeners.elementAt(i)).smsSentFailed(sms, responses);
			}
		}
	}

	protected void initSMS()
	{
		for (final String initATCommand : initATCommands)
		{
			addCommand(initATCommand);
		}
		// free up some memory in the sim/phone in case full
		for (int i = 0; i < initialDelete; i++)
		{
			addCommand(AT_SMS_DELETE + (i + 1) + "\r");
		}
	}

	protected void processIncoming(final String data)
	{
		try
		{
			if (data != null)
			{
				if (data.toLowerCase().startsWith(AT_MESSAGE_RECEIVED.toLowerCase()))
				{
					// incoming message
					final int strIndex = data.indexOf(',');
					final String s = data.substring(strIndex + 1, data.length());
					final int msgIndex = Integer.parseInt(s);
					// read it
					addCommand(AT_SMS_RECEIVE + msgIndex + "\r");
					// then delete it
					addCommand(AT_SMS_DELETE + msgIndex + "\r");
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Error parsing incoming AT response");
		}
	}

	protected void processResponse(final SMSMessage sms, final String[] responses)
	{
		if (responses != null)
		{
			for (final String response : responses)
			{
				if (response.equalsIgnoreCase(OK_CODE))
				{
					fireSMSSent(sms, responses);
					return;
				}
			}
		}
		// fire sent failed...listening app can choose to resend
		fireSMSSentFailed(sms, responses);
	}

	protected void processResponse(final String command, final String[] responses)
	{
		if (responses != null)
		{
			final String commandLower = command.toLowerCase();
			if (commandLower.startsWith(AT_SMS_RECEIVE.toLowerCase()) && responses.length > 1)
			{
				final StringTokenizer tokens = new StringTokenizer(responses[0], ",");
				if (tokens.countTokens() > 1)
				{
					tokens.nextToken();
					fireSMSReceived(new SMSMessage(tokens.nextToken(), responses[1]), responses);
				}
			}
		}
	}

	protected String[] readData(final boolean block, final boolean waitForStatus)
	{
		try
		{
			final Vector responses = new Vector();
			String response = null;
			final int result = block ? -1 : 0;
			StringBuffer buffer = new StringBuffer();
			while (is.available() > result)
			{
				final int nxtChar = is.read();
				System.out.print((char) nxtChar);
				if (nxtChar == 13 || nxtChar == 10)
				{
					if (buffer.length() > 0)
					{
						response = buffer.toString();
						responses.add(response);
						if (waitForStatus)
						{
							if (response.toLowerCase().indexOf(ERROR_CODE.toLowerCase()) < 0
									&& !response.equalsIgnoreCase(OK_CODE))
							{
								buffer = new StringBuffer();
								continue;
							}
						}
						return (String[]) responses.toArray(new String[0]);
					}
					buffer = new StringBuffer();
					continue;
				}
				buffer.append((char) nxtChar);
				Thread.sleep(delay);
			}
		}
		catch (final Exception e)
		{
			System.out.println("Error reading AT response\n+e");
		}
		return null;
	}

	protected boolean sendData(final String command)
	{
		try
		{
			final byte b[] = command.getBytes();
			for (final byte element : b)
			{
				os.write(element);
				os.flush();
				System.out.print((char) element);
				Thread.sleep(delay);
			}
			System.out.println();
			return true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	protected boolean sendSMS(final SMSMessage sms)
	{
		try
		{
			if (sendData(AT_SMS_SEND + "\"" + sms.getNumber() + "\"\r"))
			{
				is.read();
				if (sendData(sms.getMessage()))
				{
					if (sendData("\u001A"))
					{ // \z char
						return true;
					}
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	protected void showGUI()
	{
		final JFrame frame = new JFrame("SMS Settings");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final JPanel pane = new JPanel(new GridLayout(4, 1, 10, 10));
		final JPanel comPane = new JPanel(new BorderLayout());
		final JPanel baudPane = new JPanel(new BorderLayout());
		final JPanel delayPane = new JPanel(new BorderLayout());
		comPane.setBorder(new TitledBorder("Port"));
		baudPane.setBorder(new TitledBorder("Baud"));
		delayPane.setBorder(new TitledBorder("IO Delay (ms)"));
		final JTextField comText = new JTextField(portName);
		final JTextField baudText = new JTextField("" + baud);
		final JTextField delayText = new JTextField("" + delay);
		comPane.add(comText);
		baudPane.add(baudText);
		delayPane.add(delayText);
		final JButton connectButton = new JButton("Connect");
		pane.add(comPane);
		pane.add(baudPane);
		pane.add(delayPane);
		pane.add(connectButton);
		connectButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				try
				{
					portName = comText.getText().trim();
					baud = Integer.parseInt(baudText.getText().trim());
					delay = Integer.parseInt(delayText.getText().trim());
					if (!initPort(portName, baud))
					{
						JOptionPane.showMessageDialog(null, "Failed to initialise port " + portName + " " + baud);
						return;
					}
					frame.dispose();
				}
				catch (final Exception e)
				{
					JOptionPane.showMessageDialog(null, "Error opening port " + e);
				}
			}
		});
		frame.getContentPane().add(pane);
		frame.pack();
		frame.setVisible(true);
	}
}
