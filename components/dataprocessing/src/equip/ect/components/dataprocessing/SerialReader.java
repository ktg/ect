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
	private int value = 0;
	private String error = "";
	private boolean running = false;
	private SerialPort serialPort;

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
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					serialPort = new SerialPort(port);
					serialPort.openPort();
					serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, 1, 0);
					serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
					serialPort.addEventListener(event -> {
						try
						{
							if (event.isRXCHAR() && event.getEventValue() > 0)
							{
								byte buffer[] = serialPort.readBytes();
								String test = new String(buffer);
								System.out.println(test);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
							setError("Error: " + e.getMessage());
							setRunning(false);
						}
					});
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

	private void disconnect()
	{
		try
		{
			if (serialPort != null)
			{
				serialPort.removeEventListener();
				if(serialPort.isOpened())
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
