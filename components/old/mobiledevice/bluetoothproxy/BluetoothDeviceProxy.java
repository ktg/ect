/*
 <COPYRIGHT>

 Copyright (c) 2006, University of Nottingham
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
 Ben Hui (www.benhui.net)
 */
package equip.ect.components.mobiledevice.bluetoothproxy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import com.sun.media.Connector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;

/**
 * @author humble
 * 
 */
public class BluetoothDeviceProxy implements Serializable
{

	class InputListener extends Thread
	{

		private ServiceRecord serviceRecord;

		InputListener(final ServiceRecord r)
		{
			this.serviceRecord = r;
		}

		@Override
		public void run()
		{
			final String url = serviceRecord.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
			log("listening to url:" + url);

			// obtain connection and stream to this service

			try
			{
				con = getConnection(url);
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (in == null)
			{
				try
				{
					// in = new BufferedInputStream(con.openDataInputStream());
					in = con.openDataInputStream();
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			listening = true;
			while (listening)
			{
				if (con != null && in != null)
				{
					try
					{
						log("Waiting here to listening ...");
						// int size = in.available();
						final String input = ((DataInputStream) in).readUTF();
						if (input != null)
						{
							System.out.println("READ: " + input);
							setOutput(input);
						}
						/*
						 * int size = 0; byte[] incoming = new byte[1000]; int c; while ((c =
						 * in.read()) > -1) { incoming[size] = (byte) c; size++; } if (size > 0) {
						 * String input = new String(incoming); System.out.println("READ: " +
						 * input); setOutput(input); }
						 */

					}
					catch (final IOException e)
					{
						log("ERROR reading input stream!");
						e.printStackTrace();
					}

					// yield a bit
					try
					{
						sleep(200);
					}
					catch (final InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else
				{
					stopListening(false);
				}
			}
			setListening(false);
		}

		public void stopListening(final boolean closeConnection)
		{
			listening = false;
			if (in != null)
			{
				try
				{
					in.close();
					in = null;
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (closeConnection)
			{

				if (con != null)
				{
					try
					{
						con.close();
						con = null;
					}
					catch (final IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

	}

	class Listener implements DiscoveryListener
	{

		public void deviceDiscovered(final RemoteDevice remoteDevice, final DeviceClass deviceClass)
		{

			devices.addElement(remoteDevice);
			deviceClasses.addElement(deviceClass);
		}

		public void inquiryCompleted(final int complete)
		{
			log("device discovery is completed with return code:" + complete);
			log("" + devices.size() + " devices are discovered");

			deviceReturnCode = complete;

			if (devices.size() == 0)
			{
				System.out.println("No Bluetooth device found");
				// remotedeviceui.showui();

			}
			else
			{

			}
			// we cannot callback in this thread because this is a Bluetooth
			// subsystem thread. we do not want to block it.
			final Thread t = new Thread(new Worker(ID_DEVICE_COMPLETED));
			t.start();
		}

		public void servicesDiscovered(final int transId, final ServiceRecord[] records)
		{
			// note: we do not use transId because we only have one search at a
			// time
			log("Remote Bluetooth services is discovered:");
			for (int i = 0; i < records.length; i++)
			{
				final ServiceRecord record = records[i];
				Util.printServiceRecord(record);
				services.addElement(record);
			}
		}

		public void serviceSearchCompleted(final int transId, final int complete)
		{
			// note: we do not use transId because we only have one search at a
			// time
			log("service discovery completed with return code:" + complete);
			log("" + services.size() + " services are discovered");

			serviceReturnCode = complete;

			// we cannot callback in this thread because this is a Bluetooth
			// subsystem thread. we do not want to block it.
			final Thread t = new Thread(new Worker(ID_SERVICE_COMPLETED));
			t.start();

		}

	} // Listener

	/**
	 * Worker thread that invoke callback CommandListener upon Bluetooth event occurs.
	 */
	class Worker implements Runnable
	{
		int cmd = 0;

		public Worker(final int cmd)
		{
			this.cmd = cmd;
		}

		@Override
		public void run()
		{
			switch (cmd)
			{
				case ID_SERVICE_COMPLETED:
					setMessage("FINISHED SERVICE SEARCH");
					final String[] discServ = new String[services.size()];
					int i = 0;
					for (final Iterator it = services.iterator(); it.hasNext(); i++)
					{
						final ServiceRecord sr = (ServiceRecord) it.next();
						discServ[i] = sr.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
					}
					setDiscoveredServices(discServ);
					// callback.commandAction(COMPLETED, remotedeviceui);

					break;
				case ID_DEVICE_COMPLETED:
					// callback.commandAction(COMPLETED, remotedeviceui);
					// device inquiry finished, now perform a service search
					setMessage("FINISHED DEVICE SEARCH");

					final String[] deviceNames = new String[devices.size()];
					i = 0;
					for (final Iterator it = devices.iterator(); it.hasNext(); i++)
					{
						final RemoteDevice remoteDevice = (RemoteDevice) it.next();
						final String deviceName = "UNKNOWN_NAME";
						try
						{
							log("A remote Bluetooth device is discovered:");

							// Util.printRemoteDevice(remoteDevice, null);
							// deviceName = remoteDevice.getFriendlyName(false);
						}
						catch (final Exception e)
						{
							log("Warning! Error getting info for device");
						}
						finally
						{
							deviceNames[i] = remoteDevice.getBluetoothAddress();
						}
					}
					setDiscoveredDevices(deviceNames);

					final RemoteDevice rd = getDeviceByAddress(deviceAddress);
					if (rd != null)
					{
						remoteDevice = rd;
						startServiceInquiry(remoteDevice);
					}
					if (search)
					{
						setSearch(false);
					}
					break;
				case ID_DEVICE_SELECTED:
					// callback.commandAction(SELECTED, remotedeviceui);

					break;
				default:
					break;

			}
		}
	}

	Vector devices = new Vector();

	Vector deviceClasses = new Vector();

	// public static int selectedService = -1;

	Vector services = new Vector();

	private RemoteDevice remoteDevice;

	private ServiceRecord remoteService;

	// discovery mode in device inquiry
	int discoveryMode;

	// list of UUID to match during service discovery
	UUID[] serviceUUIDs = null;

	// Bluetooth return code from device inquiry operation
	// see DiscoveryListener
	int deviceReturnCode;

	// Bluetooth return code from service discovery operation
	// see DiscoveryListener
	int serviceReturnCode;

	private LocalDevice device;

	private DiscoveryAgent agent;

	// SPP_Server specific service UUID
	// note: this UUID must be a string of 32 char
	// do not use the 0x???? constructor because it won't
	// work. not sure if it is a N6600 bug or not
	public final static UUID uuid = new UUID("102030405060708090A0B0C0D0E0F010", false);

	private String input, output;

	private String deviceAddress = "";

	private String serviceAddress = "";

	private String[] discoveredDevices = null;

	private String[] discoveredServices = null;

	private String message = "OK";

	private boolean search = false;

	private String dataURL;

	private boolean listening = false;

	private InputStream in = null;

	private OutputStream out = null;

	private StreamConnection con = null;

	private final static int ID_SERVICE_COMPLETED = 1;

	private final static int ID_DEVICE_COMPLETED = 2;

	private final static int ID_DEVICE_SELECTED = 3;

	/**
	 * Utility function to write log message.
	 * 
	 * @param s
	 *            String
	 */
	public static void log(final String s)
	{
		System.out.println(s);
	}

	public static void main(final String[] args)
	{
		new BluetoothDeviceProxy();

	}

	private static byte[] getFileData(final String dataURL)
	{
		try
		{
			BufferedInputStream is = null;
			if (dataURL.startsWith("http://"))
			{
				final URL url = new URL(dataURL);
				is = new BufferedInputStream(url.openStream());
			}
			else
			{

				is = new BufferedInputStream(new FileInputStream(dataURL));
			}
			final Vector byteData = new Vector();
			int d;
			while ((d = is.read()) > -1)
			{
				byteData.add(new Byte((byte) d));
			}
			final byte[] data = new byte[byteData.size()];
			int i = 0;
			for (final Iterator it = byteData.iterator(); it.hasNext(); i++)
			{
				data[i] = ((Byte) it.next()).byteValue();
			}

			return data;
		}
		catch (final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private InputListener inputListener;

	/**
     * 
     */
	public BluetoothDeviceProxy()
	{

		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			@Override
			public void run()
			{
				closeConnection();
			}

		});
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized void closeConnection()
	{
		log("Closing connection ...");
		if (in != null)
		{
			try
			{
				in.close();
				in = null;
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (out != null)
		{
			try
			{
				out.close();
				out = null;
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (con != null)
		{
			try
			{
				con.close();
				con = null;
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getDataURL()
	{
		return this.dataURL;
	}

	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceAddress()
	{
		return deviceAddress;
	}

	public RemoteDevice getDeviceByAddress(final String address)
	{

		for (final Enumeration e = devices.elements(); e.hasMoreElements();)
		{
			final RemoteDevice rd = (RemoteDevice) e.nextElement();
			if (rd.getBluetoothAddress().equals(address)) { return rd; }
		}
		return null;
	}

	public RemoteDevice getDeviceByName(final String name)
	{

		for (final Enumeration e = devices.elements(); e.hasMoreElements();)
		{
			final RemoteDevice rd = (RemoteDevice) e.nextElement();
			String fname;
			try
			{
				fname = rd.getFriendlyName(false);

				if (fname.equalsIgnoreCase(name)) {

				return rd; }
			}
			catch (final Exception e1)
			{
				log("IOException getting friendly name");
			}

		}
		return null;
	}

	/**
	 * Return the Bluetooth result code from device inquiry. This is the result code obtained in
	 * DiscoveryListener.inquiryCompleted(). Your application cal call this method after a COMPLETED
	 * callback event is received.
	 * 
	 * @return int
	 */
	public int getDeviceDiscoveryReturnCode()
	{
		return deviceReturnCode;
	}

	public String[] getDiscoveredDevices()
	{
		return this.discoveredDevices;
	}

	/**
	 * Get all discovered services from selected remote device. Your application call this method
	 * after your app receive COMPLETED callback event. This will return all services that match
	 * your UUIDs in startInquiry().
	 * 
	 * @return ServiceRecord[]
	 */
	public ServiceRecord[] getDiscoveredServices()
	{
		final ServiceRecord[] r = new ServiceRecord[services.size()];
		services.copyInto(r);
		return r;
	}

	/**
	 * Get the first discovered service from selected remote device. Your application call this
	 * method after your app receives COMPLETED callback event. This will return the first service
	 * that match your UUIDs in startInquiry().
	 * 
	 * @return ServiceRecord null if no service discovered
	 */
	public ServiceRecord getFirstDiscoveredService()
	{
		if (services.size() > 0)
		{
			return (ServiceRecord) services.elementAt(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return Returns the input.
	 */
	public String getInput()
	{
		return input;
	}

	public String getMessage()
	{
		return this.message;
	}

	/**
	 * @return Returns the output.
	 */
	public String getOutput()
	{

		return output;
	}

	public boolean getSearch()
	{
		return this.search;
	}

	/**
	 * @return the deviceAddress
	 */
	public String getServiceAddress()
	{
		return serviceAddress;
	}

	public ServiceRecord getServiceByDeviceAddress(final String deviceAddress)
	{

		for (final Enumeration e = services.elements(); e.hasMoreElements();)
		{
			final ServiceRecord sr = (ServiceRecord) e.nextElement();

			if (sr.getHostDevice().getBluetoothAddress().equals(deviceAddress)) { return sr; }

		}
		return null;
	}

	/**
	 * Return the Bluetooth result code from service discovery. This is the result code obtained in
	 * DiscoveryListener.serviceSearchCompleted(). Your application cal call this method after a
	 * COMPLETED callback event is received.
	 * 
	 * @return int
	 */
	public int getServiceDiscoveryReturnCode()
	{
		return serviceReturnCode;
	}

	public boolean isListening()
	{
		return this.listening;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized boolean sendSPPData(final String address, final byte[] data)
	{
		try
		{
			log("connected to server. now writing '" + dataURL + "'");
			final StreamConnection con = getConnection(address);
			out = con.openDataOutputStream();

			// write data into serial stream
			out.write(data);
			out.flush();
			log("write and flush ok");

			// this wait is artificial, the purpose to do wait until the
			// server side really receive the message before we close the
			// connection
			// in theory, this is not necessary, but when I use the Rococo
			// simulator,
			// sometimes the connection dropped on the server side when I
			// close
			// it here
			// it may be a bug in Rococo simulator.
			Thread.sleep(1000);

			// finish, close output stream
			out.close();
			out = null;
			// now the server should have echo back the string, so we read
			// it
			// using input stream
			// DataInputStream in = con.openDataInputStream();
			// String s = in.readUTF();

			// log("read in data '" + s + "'");

			if (!listening)
			{
				closeConnection();
			}
			return true;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return false;
		}

	}

	public boolean sendSPPData(final String serviceAddress, final String dataURL)
	{
		final byte[] data = getFileData(dataURL);
		if (data != null)
		{
			return sendSPPData(serviceAddress, data);
		}
		else
		{
			log("Warning, data for '" + dataURL + "' could not be loaded");
			setMessage("ERROR loading data");
			return false;
		}
	}

	/**
	 * Send a message to server using Serial Port Profile. Connect to incoming service record, send
	 * a text, and read in a text. This method illustrate how to send and receive data using serial
	 * port profile. Device and service discovery is part of Serial Port client but it is done by
	 * Bluelet component. See SPP_MIDlet for usage of Bluelet.
	 * 
	 * @param msg
	 */
	public boolean sendSPPMessage(final String serviceAddress, final String msg)
	{
		log("send_SPP2");

		try
		{
			// obtain connection and stream to this service
			con = getConnection(serviceAddress);
			if (con != null)
			{
				log("connected to server. now writing '" + msg + "'");
				out = con.openDataOutputStream();
				// write data into serial stream
				((DataOutputStream) out).writeUTF(msg);
				out.flush();
				log("write and flush ok");

				// this wait is artificial, the purpose to do wait until the
				// server side really receive the message before we close the
				// connection
				// in theory, this is not necessary, but when I use the Rococo
				// simulator,
				// sometimes the connection dropped on the server side when I
				// close
				// it here
				// it may be a bug in Rococo simulator.
				Thread.sleep(1000);

				// finish, close output stream
				out.close();
				out = null;
				// now the server should have echo back the string, so we read
				// it
				// using input stream
				// DataInputStream in = con.openDataInputStream();
				// String s = in.readUTF();

				// log("read in data '" + s + "'");
				// in.close();
				if (!listening)
				{
					closeConnection();
				}
			}
			return true;
		}
		catch (final Exception e)
		{
			return false;
		}

	}

	public void setDataURL(final String dataURL)
	{
		final String old = this.dataURL;
		if (serviceAddress != null)
		{
			this.dataURL = dataURL;
			if (sendSPPData(serviceAddress, dataURL))
			{
				setMessage("DATA SUCCEEDED");
			}
			else
			{
				setMessage("DATA FAILED");
			}
			propertyChangeListeners.firePropertyChange("dataURL", old, dataURL);

		}
		else
		{
			setMessage("SERVICE UNAVAILABLE");
		}
	}

	/**
	 * @param deviceName
	 *            The deviceName to set.
	 */
	public void setDeviceAddress(final String deviceAddress)
	{
		final String old = this.deviceAddress;
		this.deviceAddress = deviceAddress;
		propertyChangeListeners.firePropertyChange("deviceAddress", old, deviceAddress);
	}

	/**
	 * @param discoveredDevices
	 *            the discoveredDevices to set
	 */
	public void setDiscoveredDevices(String[] discoveredDevices)
	{
		final String[] prevDiscDevices = this.discoveredDevices;
		discoveredDevices = discoveredDevices;
		propertyChangeListeners.firePropertyChange("discoveredDevices", prevDiscDevices, discoveredDevices);
	}

	public void setDiscoveredServices(final String[] discoveredServices)
	{
		final String[] prevDiscServ = this.discoveredServices;
		this.discoveredServices = discoveredServices;
		propertyChangeListeners.firePropertyChange("discoveredServices", prevDiscServ, discoveredServices);
	}

	/**
	 * @param input
	 *            The input to set.
	 */
	public void setInput(final String input)
	{
		final String old = this.input;
		if (serviceAddress != null)
		{
			this.input = input;
			if (sendSPPMessage(serviceAddress, input))
			{
				setMessage("MESSAGE SUCCEEDED");
			}
			else
			{
				setMessage("MESSAGE FAILED");
			}
			propertyChangeListeners.firePropertyChange("input", old, input);

		}
		else
		{
			setMessage("SERVICE UNAVAILABLE");
		}
	}

	public void setListening(final boolean listening)
	{
		final boolean old = this.listening;

		if (listening)
		{
			if (remoteService == null)
			{
				remoteService = getFirstDiscoveredService();
			}
			if (remoteService != null)
			{
				inputListener = new InputListener(remoteService);
				this.listening = listening;
				inputListener.start();
			}
		}
		else
		{
			if (inputListener != null)
			{
				inputListener.stopListening(false);
				inputListener = null;
			}
			this.listening = listening;
		}
		propertyChangeListeners.firePropertyChange("listening", old, this.listening);
	}

	/**
	 * @param output
	 *            The output to set.
	 */
	public void setOutput(final String output)
	{
		final String old = this.output;
		this.output = output;
		propertyChangeListeners.firePropertyChange("output", old, output);
	}

	public void setSearch(final boolean search)
	{
		final boolean alreadySearching = this.search;
		this.search = search;

		propertyChangeListeners.firePropertyChange("search", alreadySearching, search);
		if (search && !alreadySearching)
		{
			startDeviceInquiry();
		}

	}

	public void setServiceAddress(String serviceAddress)
	{
		final String old = this.serviceAddress;

		if (serviceAddress.startsWith("btspp://"))
		{
			this.serviceAddress = serviceAddress;
		}
		else
		{
			serviceAddress = serviceAddress.replaceAll(":", "");
			this.serviceAddress = "btspp://" + serviceAddress + ":2;authenticate=true;encrypt=false";
		}
		// this.remoteDevice = LocalDevice.getLocalDevice().getBluetoothPeer().
		propertyChangeListeners.firePropertyChange("serviceAddress", old, this.serviceAddress);
	}

	public void startDeviceInquiry()
	{
		startInquiry(DiscoveryAgent.GIAC, new UUID[] { uuid });
	}

	/**
	 * Start device inquiry. Your application call this method to start inquiry.
	 * 
	 * @param mode
	 *            int one of DiscoveryAgent.GIAC or DiscoveryAgent.LIAC
	 * @param serviceUUIDs
	 *            UUID[]
	 */
	public void startInquiry(final int mode, final UUID[] serviceUUIDs)
	{
		try
		{
			setMessage("SEARCHING DEVICES");
			this.discoveryMode = mode;
			this.serviceUUIDs = serviceUUIDs;

			// clear previous values first
			devices.removeAllElements();
			deviceClasses.removeAllElements();
			services.removeAllElements();
			//
			// initialize the JABWT stack
			device = LocalDevice.getLocalDevice(); // obtain reference to
			// singleton
			device.setDiscoverable(DiscoveryAgent.GIAC); // set Discover Mode
			agent = device.getDiscoveryAgent(); // obtain reference to singleton

			final boolean result = agent.startInquiry(mode, new Listener());

			// update screen with "Please Wait" message
			// remotedeviceui.setMsg("[Please Wait...]");

		}
		catch (final BluetoothStateException e)
		{
			e.printStackTrace();
		}

	}

	public void startServiceInquiry(final RemoteDevice remoteDevice)
	{
		setMessage("SEARCHING SERVICES");
		services.removeAllElements();
		try
		{
			agent.searchServices(null, serviceUUIDs, remoteDevice, new Listener());

		}
		catch (final BluetoothStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected synchronized StreamConnection getConnection(final ServiceRecord r) throws IOException
	{
		// obtain the URL reference to this service on remote device
		// note: must use AUTHENTICATE_NOENCRYPT in order to use BlueCove
		// connection
		final String url = r.getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
		log("url:" + url);
		// obtain connection and stream to this service
		return getConnection(url);
	}

	protected synchronized StreamConnection getConnection(final String url) throws IOException
	{
		if (url == null || !url.startsWith("btspp://")) { return null; }
		final StreamConnection con = (StreamConnection) Connector.open(url);
		return con;
	}

	protected void setMessage(final String message)
	{
		final String old = this.message;
		this.message = message;
		propertyChangeListeners.firePropertyChange("message", old, message);

	}

}
