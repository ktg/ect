package equip.ect.components.dataprocessing;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import equip.ect.Category;
import equip.ect.ECTComponent;
import jssc.SerialPort;

/**
 * Reads ints from serial port
 *
 * @author ktg
 * @classification Data/Processing
 * @defaultOutputValue out
 */
@ECTComponent
@Category("Data/Processing")
public class SerialReader implements Serializable
{
	private String port = "COM1";
	private float value = 0;
	private String error = "";
	private int baudRate = SerialPort.BAUDRATE_9600;
	private boolean running = false;
	private SerialPort serialPort;
	private StringBuilder message = new StringBuilder();

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

	public float getValue()
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

	public int getBaudRate()
	{
		return baudRate;
	}

	public void setBaudRate(int baudRate)
	{
		int oldBaud = this.baudRate;
		this.baudRate = baudRate;
		propertyChangeListeners.firePropertyChange("baudRate", oldBaud, baudRate);
		if (running)
		{
			setRunning(false);
			setRunning(true);
		}
	}

	public void setPort(String port)
	{
		setRunning(false);
		String oldPort = this.port;
		this.port = port;
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
		else
		{
			disconnect();
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
		new Thread(() -> {
			try
			{
				serialPort = new SerialPort(port);
				serialPort.openPort();
				serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
				serialPort.addEventListener(event -> {
					System.out.println();
					try
					{
						if (event.isRXCHAR() && event.getEventValue() > 0)
						{
							final byte[] buffer = serialPort.readBytes();
							for (byte b : buffer)
							{
								if ((b == '\r' || b == '\n') && message.length() > 0)
								{
									String input = message.toString();
									message.setLength(0);
									float oldValue = value;
									float value = Float.parseFloat(input);
									propertyChangeListeners.firePropertyChange("value", oldValue, value);
								}
								else
								{
									message.append((char) b);
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						setError("Error: " + e.getMessage());
					}
				}, SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				setError("Error: " + e.getMessage());
				setRunning(false);
			}
		}).start();
	}

	private void disconnect()
	{
		try
		{
			if (serialPort != null)
			{
				serialPort.removeEventListener();
				if (serialPort.isOpened())
				{
					serialPort.closePort();
				}
				serialPort = null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			setError("Error: " + e.getMessage());
		}
	}
}
