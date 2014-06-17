/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Sussex
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Sussex
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

Created by: Ted Phelps (University of Sussex)
Contributors:
  Ted Phelps (University of Sussex)

 */
package equip.ect.components.gps;

/*
 * GPS receiver component
 * $RCSfile: GPS.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:28 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: GPS.java,v $
 * Revision 1.2  2012/04/03 12:27:28  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.1  2005/05/03 11:54:38  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.4  2005/04/28 15:59:15  cmg
 * add BSD license boilerplates
 *
 * Revision 1.3  2004/10/19 11:30:04  phelps
 * Now handles GPGGA sentences properly
 *
 * Revision 1.2  2004/10/08 15:58:44  phelps
 * Lexical analyzer now appears to work.  Removed the comm port
 * initialization code as I'm told that it's now done centrally.
 *
 * Revision 1.1  2004/10/08 13:47:59  phelps
 * Initial: sufficient to receive data, but not to interpret it.
 */

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

import equip.ect.ContainerManager;
import equip.ect.Persistable;
import equip.ect.PersistenceManager;

public class GPS implements Serializable, SerialPortEventListener, Persistable
{
	public static final String PERSISTENCE_FILE_TAG = "GPS";
	public static final String STATUS_STOPPED = "stopped";
	public static final String STATUS_RUNNING = "running";

	/* Parser states */
	private static final int STATE_START = 0;
	private static final int STATE_PAYLOAD = 1;
	private static final int STATE_CSUM1 = 2;
	private static final int STATE_CSUM2 = 3;
	private static final int STATE_CR = 4;
	private static final int STATE_LF = 5;
	private static final int STATE_ERROR = 6;

	private String status = STATUS_STOPPED;
	private CommPortIdentifier id;
	private SerialPort port;
	private int state;
	private int csum, xsum;
	private StringBuffer buffer = new StringBuffer(128);
	private Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	private Double longitude, latitude;
	private Integer satellitesInView;
	private Float hdop, vdop, altitude;

	private File persistFile;
	private PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public GPS()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public Float getAltitude()
	{
		return altitude;
	}

	public Float getHDOP()
	{
		return hdop;
	}

	public boolean getIsRunning()
	{
		return port != null;
	}

	public Double getLatitude()
	{
		return latitude;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public synchronized String getPort()
	{
		return (id == null) ? null : id.getName();
	}

	public Integer getSatellitesInView()
	{
		return satellitesInView;
	}

	public String getStatus()
	{
		return status;
	}

	@Override
	public synchronized void load(final File persistFile, final ContainerManager manager) throws IOException
	{
		ObjectInputStream in;
		int major, minor;
		String port;
		boolean isRunning;

		System.out.println("GPS: info: loading state from " + persistFile);

		/* Bail if we lack state */
		if (persistFile == null) { return; }

		/* Remember the state file so that we don't create a new one each time */
		this.persistFile = persistFile;

		in = new ObjectInputStream(new FileInputStream(persistFile));
		try
		{
			if (!PERSISTENCE_FILE_TAG.equals(in.readObject()))
			{
				System.err.println("GPS: error: corrupt state file: " + persistFile);
				in.close();
				return;
			}

			/* Get the file version number */
			major = in.readInt();
			minor = in.readInt();

			/* Bail if the major version is too new */
			if (major > 0)
			{
				System.err.println("GPS: warning: persistence file too new: " + major + "." + minor + "; ignoring");
				in.close();
				return;
			}

			/* Warn if the minor version number is too new */
			if (minor > 0)
			{
				System.err.println("GPS: warning: persistence file too new: " + major + "." + minor
						+ "; some data may be lost");
			}

			/*
			 * A change in minor version number indicates that additional fields have been added to
			 * the end, but we should be able to cope with just reading what we understand
			 */

			port = (String) in.readObject();
			isRunning = (in.readInt() == 0 ? false : true);
		}
		catch (final ClassNotFoundException e)
		{
			System.err.println("GPS: error: unable to read state information:");
			e.printStackTrace();
			in.close();
			return;
		}

		in.close();

		/* Debugging */
		System.out.println("GPS: info: initializing from " + persistFile + ":");
		System.out.println("GPS: info:   port=" + port);
		System.out.println("GPS: info:   isRunning=" + isRunning);

		/* Record the state */
		setPort(port);
		if (isRunning)
		{
			setIsRunning(true);
		}
	}

	/* Persistable interface */
	@Override
	public synchronized File persist(final ContainerManager manager) throws IOException
	{
		ObjectOutputStream out;

		/* Make sure we have a file in which to store our state */
		if (persistFile == null)
		{
			persistFile = File
					.createTempFile("GPS", ".dat",
									PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY);
		}

		out = new ObjectOutputStream(new FileOutputStream(persistFile));
		/* Output file format and major.minor version numbers */
		out.writeObject(PERSISTENCE_FILE_TAG);
		out.writeInt(0);
		out.writeInt(1);

		/* Write the port and running status */
		out.writeObject(getPort());
		out.writeInt(getIsRunning() ? 1 : 0);
		out.close();

		return persistFile;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	@Override
	public void serialEvent(final SerialPortEvent event)
	{
		/*
		 * The NMEA maximum sentence length is 82 bytes, so make sure we have more than enough room
		 */
		final byte[] bytes = new byte[128];

		final int type = event.getEventType();
		int length;
		/*
		 * String typeName;
		 * 
		 * if (type == SerialPortEvent.BI) { typeName = "BI"; } else if (type == SerialPortEvent.CD)
		 * { typeName = "CD"; } else if (type == SerialPortEvent.CTS) { typeName = "CTS"; } else if
		 * (type == SerialPortEvent.DATA_AVAILABLE) { typeName = "DATA_AVAILABLE"; } else if (type
		 * == SerialPortEvent.DSR) { typeName = "DSR"; } else if (type == SerialPortEvent.FE) {
		 * typeName = "FE"; } else if (type == SerialPortEvent.OE) { typeName = "OE"; } else if
		 * (type == SerialPortEvent.OUTPUT_BUFFER_EMPTY) { typeName = "OUTPUT_BUFFER_EMPTY"; } else
		 * if (type == SerialPortEvent.PE) { typeName = "PE"; } else if (type == SerialPortEvent.RI)
		 * { typeName = "RI"; } else { typeName = "<unknown>"; }
		 * 
		 * System.out.println("GPS: info: serial event: " + typeName);
		 */
		if (type == SerialPortEvent.BI)
		{
			state = STATE_START;
			buffer.setLength(0);
		}
		else if (type == SerialPortEvent.DATA_AVAILABLE)
		{
			/* Read some bytes and interpret them */
			try
			{
				length = port.getInputStream().read(bytes, 0, 128);
				digest(bytes, length);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setIsRunning(final boolean isRunning)
	{
		/* Discard redundant updates */
		if ((port != null) == isRunning) { return; }

		if (port != null)
		{
			/* Close the port */
			port.removeEventListener();
			port.close();
			port = null;

			/* Clear our state */
			state = STATE_START;
			buffer.setLength(0);

			setStatus(STATUS_STOPPED);
		}
		else
		{
			/* Bail if the port identifier isn't set */
			if (id == null)
			{
				System.err.println("GPS: error: serial port not specified");
				return;
			}

			try
			{
				/* Open the serial port */
				port = (SerialPort) id.open("ECT:GPS", 100);

				/* Configure it to the appropriate settings for a GPS receiver */
				port.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				/* Listen for changes to the port */
				port.addEventListener(this);
				port.notifyOnDataAvailable(true);
				port.notifyOnBreakInterrupt(true);
			}
			catch (final TooManyListenersException e)
			{
				port.close();
				port = null;
				e.printStackTrace(System.err);
				return;
			}
			catch (final UnsupportedCommOperationException e)
			{
				port.close();
				port = null;
				e.printStackTrace(System.err);
				return;
			}
			catch (final PortInUseException e)
			{
				System.err.println(e);
				return;
			}
		}

		propertyChangeListeners.firePropertyChange(GPSBeanInfo.IS_RUNNING_PROPERTY_NAME, !isRunning, isRunning);
	}

	public synchronized void setPort(final String port)
	{
		String oldPort;
		CommPortIdentifier id;

		/* Get the old port name */
		oldPort = (this.id == null) ? null : this.id.getName();

		/* Discard redundant updates */
		if (oldPort == null ? port == null : oldPort.equals(port)) { return; }

		/* Make sure we can find the given port and that it's a serial port */
		try
		{
			id = CommPortIdentifier.getPortIdentifier(port);
			if (id.getPortType() != CommPortIdentifier.PORT_SERIAL)
			{
				System.err.println("GPS: error: " + id.getName() + " is not a serial port");
			}
			else
			{
				this.id = id;
				propertyChangeListeners.firePropertyChange(GPSBeanInfo.PORT_PROPERTY_NAME, oldPort, port);
			}
		}
		catch (final NoSuchPortException e)
		{
			System.out.println(e);
		}
	}

	public synchronized void stop()
	{
		if (port != null)
		{
			/* Clear our state */
			state = STATE_START;
			buffer.setLength(0);

			/* Close the port */
			port.removeEventListener();
			port.close();
			port = null;
			setStatus(STATUS_STOPPED);
			System.out.println("GPS: Closed port " + port.getName());
		}
	}

	private void digest(final byte[] bytes, final int count)
	{
		int i, digit;
		char ch;
		final String[] payload;
		int eop_offset;

		for (i = 0; i < count; i++)
		{
			/*
			 * Discard bytes with the high byte set as these are NMEA binary-format data, which we
			 * don't understand, and which can apparently be interleaved with ASCII-format data.
			 */
			if (bytes[i] < 0 || bytes[i] > 0x7f)
			{
				continue;
			}

			/*
			 * Coerce the byte to a Unicode character. Since we're certain that the byte is between
			 * 0 and 127, we can safely assume that we're in the happy world of ASCII
			 */
			ch = (char) bytes[i];

			switch (state)
			{
				case STATE_START:
					/* Looking for the '$' that marks the beginning of a sentence */
					if (ch == '$')
					{
						csum = 0;
						eop_offset = -1;
						state = STATE_PAYLOAD;
					}

					break;

				case STATE_PAYLOAD:
					/* Scanning the payload */
					if (ch == '*')
					{
						/* A '*' precedes the checksum */
						eop_offset = i;
						state = STATE_CSUM1;
					}
					else if (ch == '\r')
					{
						/* CR-LF indicates the end of a sentence. Checksum is absent! */
						state = STATE_LF;
					}
					else if (bytes[i] < 32)
					{
						/*
						 * Non-printing characters other than the CR-LF sentence terminator are not
						 * permitted
						 */
						state = STATE_ERROR;
					}
					else
					{
						/* Update the checksum */
						csum ^= bytes[i];

						/* And record the byte */
						buffer.append(ch);
					}

					break;

				case STATE_CSUM1:
					/* The first character of the two-byte checksum */
					digit = Character.digit(ch, 16);
					if (digit < 0)
					{
						state = STATE_ERROR;
					}
					else
					{
						xsum = digit << 4;
						state = STATE_CSUM2;
					}

					break;

				case STATE_CSUM2:
					/* The second character of the two-byte checksum */
					digit = Character.digit(ch, 16);
					if (digit < 0)
					{
						state = STATE_ERROR;
					}
					else
					{
						xsum |= digit;

						/* Verify the checksum */
						if (csum != xsum)
						{
							System.err.println("GPS: warning: checksum mismatch: expected "
									+ Integer.toString(xsum, 16) + " but calculated " + Integer.toString(csum, 16));
							state = STATE_ERROR;
						}
						else
						{
							state = STATE_CR;
						}
					}

					break;

				case STATE_CR:
					/* We're expecting a CR */
					if (ch == '\r')
					{
						state = STATE_LF;
					}
					else
					{
						state = STATE_ERROR;
					}

					break;

				case STATE_LF:
					/* We've seen a CR */
					if (ch == '\n')
					{
						/*
						 * An LF indicates the end of a sentence. Split the string using commas as
						 * separators
						 */
						interpret(buffer.toString().split(",", -1));

						/* Prepare for the next sentence */
						buffer.setLength(0);
						state = STATE_START;
					}
					else
					{
						/* Otherwise we've got a bad sentence. */
						state = STATE_ERROR;
					}

					break;

				case STATE_ERROR:
					/* Ignore everything until we see a LF */
					if (ch == '\n')
					{
						state = STATE_START;
					}

					break;

				default:
					System.err.println("GPS: internal error: invalid state: " + state);
					return;
			}
		}
	}

	private void interpret(final String args[])
	{
		final int i;
		long now, time;
		Double lon, lat;
		Float altitude, hdop;
		final Float vdop;
		Integer fixQuality, satCount;

		now = System.currentTimeMillis();

		try
		{
			if ("GPGGA".equals(args[0]))
			{
				// System.out.println("Global Positioning System Fix Data");
				time = parseTime(now, args[1]);
				// System.out.println("  Timestamp: " + new Date(time) + " (" + time + "/\"" +
				// args[1] + "\")");
				lon = parseLoc(args[2], args[3]);
				// System.out.println("  Longitude: " + lon + " (\"" + args[2] + "\" \"" + args[3] +
				// "\")");
				lat = parseLoc(args[4], args[5]);
				// System.out.println("  Latitude: " + lat + " (\"" + args[4] + "\" \"" + args[5] +
				// "\")");
				fixQuality = (args[6].length() == 0) ? null : Integer.valueOf(args[6]);
				// System.out.println("  Quality of Fix: " + fixQuality + " (\"" + args[6] + "\")");
				satCount = (args[7].length() == 0) ? null : Integer.valueOf(args[7]);
				// System.out.println("  Satellites in view: " + satCount + " (\"" + args[7] +
				// "\")");
				hdop = (args[8].length() == 0) ? null : Float.valueOf(args[8]);
				// System.out.println("  HDOP: " + hdop + " (\"" + args[8] + "\")");
				altitude = (args[9].length() == 0) ? null : Float.valueOf(args[9]);
				// System.out.println("  Altitude: " + altitude + " (\"" + args[9] + "\" \"" +
				// args[10] + "\")");

				/*
				 * for (i = 11; i < args.length; i++) { System.out.println("  " + i + ": \"" +
				 * args[i] + "\""); }
				 */

				setLongitude(lon);
				setLatitude(lat);
				setSatellitesInView(satCount);
				setHDOP(hdop);
				setAltitude(altitude);
			}
			else if ("GPGSA".equals(args[0]))
			{
				System.out.println("GPS DOP and Active Satellites");
			}
			else
			{
				System.out.println(args[0]);
			}
			System.out.println("---");
		}
		catch (final NumberFormatException e)
		{
			System.err.println(e);
		}
	}

	/*
	 * Parse a GPS location of the form DDMMSS.sss, and return the number of degrees as a decimal
	 * number, treating north and east as positive, south and west as negative.
	 */
	private Double parseLoc(final String loc, final String dir) throws NumberFormatException
	{
		float num, min;
		int deg, sign;

		/* If either the location or direction is an empty string then return null */
		if (loc.length() == 0 || dir.length() == 0) { return null; }

		/* Convert the location to a number */
		num = Float.parseFloat(loc);

		/* Make sure the direction looks sane */
		switch (dir.charAt(0))
		{
			case 'N':
			case 'n':
			case 'E':
			case 'e':
				sign = 1;
				break;

			case 'S':
			case 's':
			case 'W':
			case 'w':
				sign = -1;
				break;

			default:
				throw new NumberFormatException();
		}

		/* Extract the degrees, minutes and seconds */
		deg = (int) num / 100;
		min = num - (deg * 100);

		return new Double(sign * ((min / 60) + deg));
	}

	/*
	 * Parse a GPS timestamp of the form HHMMSS.sss, and return a time within 12 hours of the
	 * reference time. Both now and the result should be the number of milliseconds from the start
	 * of the epoch.
	 */
	private long parseTime(final long now, final String string) throws NumberFormatException
	{
		float num;
		long time;
		int value;
		final float subsec;

		/* Convert the time into a float */
		num = Float.parseFloat(string);

		/* Set the calendar's time to now */
		calendar.setTimeInMillis(now);

		/* And then overwrite the hours, minutes and seconds */
		value = (int) num / 10000;
		calendar.set(Calendar.HOUR_OF_DAY, value);
		value = ((int) num / 100) % 100;
		calendar.set(Calendar.MINUTE, value);
		value = (int) num % 100;
		calendar.set(Calendar.SECOND, value);

		/*
		 * If we differ from the reference time by more then 12 hours then move to an adjacent day
		 */
		time = calendar.getTimeInMillis();
		if (time - now > 12 * 60 * 60 * 1000)
		{
			calendar.add(Calendar.DATE, -1);
			time = calendar.getTimeInMillis();
		}
		else if (now - time > 12 * 60 * 60 * 1000)
		{
			calendar.add(Calendar.DATE, 1);
			time = calendar.getTimeInMillis();
		}

		/* Add on the subsecond part */
		if (string.length() > 6)
		{
			time += (int) (num * 1000) % 1000;
		}

		return time;
	}

	private synchronized void setAltitude(final Float altitude)
	{
		final Float oldAltitude = this.altitude;

		/* Discard redundant updates */
		if (oldAltitude == null ? altitude == null : oldAltitude.equals(altitude)) { return; }

		this.altitude = altitude;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.ALTITUDE_PROPERTY_NAME, oldAltitude, altitude);
	}

	private synchronized void setHDOP(final Float hdop)
	{
		final Float oldHDOP = this.hdop;

		/* Discard redundant updates */
		if (oldHDOP == null ? hdop == null : oldHDOP.equals(hdop)) { return; }

		this.hdop = hdop;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.HDOP_PROPERTY_NAME, oldHDOP, hdop);
	}

	private synchronized void setLatitude(final Double latitude)
	{
		final Double oldLatitude = this.latitude;

		/* Discard redundant updates */
		if (oldLatitude == null ? latitude == null : oldLatitude.equals(latitude)) { return; }

		this.latitude = latitude;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.LATITUDE_PROPERTY_NAME, oldLatitude, latitude);
	}

	private synchronized void setLongitude(final Double longitude)
	{
		final Double oldLongitude = this.longitude;

		/* Discard redundant updates */
		if (oldLongitude == null ? longitude == null : oldLongitude.equals(longitude)) { return; }

		this.longitude = longitude;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.LONGITUDE_PROPERTY_NAME, oldLongitude, longitude);
	}

	private synchronized void setSatellitesInView(final Integer count)
	{
		final Integer oldCount = this.satellitesInView;

		/* Discard redundant updates */
		if (oldCount == null ? count == null : oldCount.equals(count)) { return; }

		this.satellitesInView = count;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.SATELLITES_PROPERTY_NAME, oldCount, count);
	}

	private synchronized void setStatus(final String status)
	{
		final String oldStatus = this.status;

		/* Discard redundant updates */
		if (oldStatus.equals(status)) { return; }

		this.status = status;
		propertyChangeListeners.firePropertyChange(GPSBeanInfo.STATUS_PROPERTY_NAME, oldStatus, status);
	}
}
