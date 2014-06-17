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

Created by: Jan Humble (University of Nottingham)
Contributors:
  Jan Humble (University of Nottingham)

 */
/*
 * InfraRedControl, $RCSfile: InfraRedControl.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original author: Jan Humble 
 */

package equip.ect.components.infrared;

import se.sics.commx.DeviceEvent;
import se.sics.commx.SerialDeviceHandler;
import se.sics.commx.SerialDeviceListener;

import javax.comm.SerialPortEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class InfraRedControl extends SerialDeviceHandler implements Serializable
{

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	private String status = null;
	private String read = null;
	private Properties inMappings;
	private Properties outMappings;
	private JTextField receivedTF;

	/**
	 * Default constructor.<br>
	 */
	public InfraRedControl()
	{
		super("config/infrared.properties", SerialDeviceListener.class);
		// Now set the component up
		try
		{
			synchronized (InfraRedControl.class)
			{
				final String drivername = "com.sun.comm.Win32Driver";
				try
				{
					final javax.comm.CommDriver driver = (javax.comm.CommDriver) Class.forName(drivername)
							.newInstance();
					driver.initialize();
				}
				catch (final Exception e)
				{
					System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
				}

				if (!openConnection())
				{
					System.out.println("Could not open connection to " + "Infrared device!");
					// System.exit(0);
					status = "Could not open connection";
				}
				else
				{
					status = "OK";
				}
				System.out.println("Adding device Listener");
			}
		}
		catch (final Exception e)
		{
			System.out.println("Could not communicate with Motes Receiver: " + e);
			status = "ERROR: " + e;
			// System.exit(0);
		}
		initGUI();
	}

	/**
	 * The main method of this application.
	 * <p/>
	 * Create one instance.
	 */

	public static void main(final String[] args)
	{
		final InfraRedControl irc = new InfraRedControl();
	}

	static byte[] hexToByteArray(String hex) throws Exception
	{
		hex = hex.trim();
		final int hexLength = hex.length();
		if ((hexLength % 2) != 0)
		{
			throw new Exception("Warning: hex string odd sized <size=" + hexLength + ">!");
		}
		final byte[] data = new byte[hexLength / 2];
		for (int i = 0, j = 0; i < hexLength; i += 2, j++)
		{
			data[j] = Byte.parseByte(hex.substring(i, i + 2), 16);
		}
		return data;
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getRead()
	{
		return this.read;
	}

	public void setRead(final String read)
	{
		final String lastRead = this.read;
		this.read = read;
		propertyChangeListeners.firePropertyChange("read", lastRead, read);
	}

	public String getStatus()
	{
		return status;
	}

	public void handleEvent(final DeviceEvent event)
	{
		switch (event.getType())
		{
		}
	}

	public void handleReadBuffer(final String data)
	{
		// System.out.println("READ IR: " + data);
		receivedTF.setText(data);
		setRead(data);

	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void sendHexSignal(final String hexSignal)
	{
		if (outputStream != null)
		{
			try
			{
				final byte[] data = hexToByteArray(hexSignal);
				outputStream.write(data);
				outputStream.flush();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void serialEvent(final SerialPortEvent e)
	{
		// Create a StringBuffer and int to receive input data.
		final StringBuffer inputBuffer = new StringBuffer();
		int newData = 0;

		// Determine type of event.
		switch (e.getEventType())
		{
			// ntRead data until -1 is returned. If \r is received substitute
			// \n for correct newline handling.
			case SerialPortEvent.DATA_AVAILABLE:
				// message("Getting event");
				while (newData != -1)
				{
					try
					{
						newData = inputStream.read();
						if (newData == -1)
						{
							break;
						}
						if ('\r' == (char) newData)
						{
							// inputBuffer.append('\n');
						}
						else
						{
							inputBuffer.append(Integer.toHexString(newData & 0xff).toUpperCase());
						}
					}
					catch (final IOException ex)
					{
						message(ex.getMessage());
						return;
					}
				}
				handleReadBuffer(inputBuffer.toString());

				break;
			// If break event append BREAK RECEIVED message.
			case SerialPortEvent.BI:
				message("\n--- BREAK RECEIVED ---\n");
				break;
			case SerialPortEvent.CTS:
				handleClearToSend();
				break;
		}
	}

	public void setInMapping(final String key, final String signal)
	{
		inMappings.setProperty(key, signal);
	}

	/**
	 * stop
	 */
	public void stop()
	{
		try
		{
			synchronized (InfraRedControl.class)
			{
				closeConnection();
				System.err.println("Infrared closed device");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR closing smartit device handler: " + e);
			e.printStackTrace(System.err);
		}
		// setStatus("Stopped");
	}

	void initGUI()
	{
		final JFrame main = new JFrame("IR Control");

		final JPanel p = new JPanel(new BorderLayout());
		final JList mappingsList = new JList();

		p.add(BorderLayout.CENTER, new JScrollPane(mappingsList));
		JButton b = new JButton("Add");
		final JPanel sendP = new JPanel();
		sendP.add(new JLabel("HEX TO SEND:"));
		final JTextField hexField = new JTextField(30);
		sendP.add(hexField);
		b = new JButton("send");
		b.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String hex = hexField.getText().trim();
				if (hex.length() > 1)
				{
					sendHexSignal(hex);
				}
			}
		});

		sendP.add(b);
		final JPanel controlsP = new JPanel(new GridLayout(3, 1));
		final JPanel receiveP = new JPanel();
		receiveP.add(new JLabel("Received:"));
		receiveP.add(receivedTF = new JTextField(30));
		final JPanel mapP = new JPanel();
		mapP.add(new JLabel("Map to"));
		final JTextField mapTF = new JTextField(20);
		mapP.add(mapTF);
		b = new JButton("map");
		b.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{

			}
		});
		mapP.add(b);
		controlsP.add(receiveP);
		controlsP.add(mapP);
		controlsP.add(sendP);
		p.add(BorderLayout.SOUTH, controlsP);
		main.getContentPane().add(p);
		main.pack();
		main.setVisible(true);
	}
}
