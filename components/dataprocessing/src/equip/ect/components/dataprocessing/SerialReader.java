package equip.ect.components.dataprocessing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.Serializable;

import equip.ect.Category;
import equip.ect.ECTComponent;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * Reads ints from serial port
 *
 * @classification Data/Processing
 * @defaultOutputValue out
 * @author ktg
 */
@ECTComponent
@Category("Data/Processing")
public class SerialReader implements Serializable
{
	private String port = "COM1";
	private int value = 0;
	private String error = "";
	private boolean running = false;

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SerialReader()
	{

	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public String getPort()
	{
		return port;
	}

	public int getValue()
	{
		return value;
	}

	public boolean isRunning()
	{
		return running;
	}

	public String getError()
	{
		return error;
	}

	public void setPort(String port)
	{
		String oldPort = this.port;
		this.port = port;
		System.out.println("Change port " + oldPort + " -> " + port);
		propertyChangeListeners.firePropertyChange("port", oldPort, port);
	}

	public void setRunning(boolean running)
	{
		boolean oldRunning = this.running;
		this.running = running;
		propertyChangeListeners.firePropertyChange("running", oldRunning, running);
		if (running)
		{
			connect();
		}
	}

	private void setError(String error)
	{
		String oldError = this.error;
		this.error = error;
		propertyChangeListeners.firePropertyChange("error", oldError, error);
	}

	private void connect()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					final CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
					if (portIdentifier.isCurrentlyOwned())
					{
						setError("Error: Port is currently in use");
						setRunning(false);
					}
					else
					{
						final CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
						if (commPort instanceof SerialPort)
						{
							final SerialPort serialPort = (SerialPort) commPort;
							serialPort.setSerialPortParams(57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

							final InputStream in = serialPort.getInputStream();
							final DataInputStream dataInputStream = new DataInputStream(in);

							setError("");
							while (running)
							{
								int oldValue = value;
								value = dataInputStream.readInt();
								propertyChangeListeners.firePropertyChange("value", oldValue, value);
							}
						}
						else {
							setError("Error: Not a serial port");
							setRunning(false);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					setError("Error: " + e.getMessage());
					setRunning(false);
				}
			}
		}).start();
	}
}
