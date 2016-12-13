package equip.ect.components.dataprocessing;

import com.fazecast.jSerialComm.SerialPort;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.Serializable;

import equip.ect.Category;
import equip.ect.ECTComponent;

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

	public boolean getRunning()
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
		propertyChangeListeners.firePropertyChange("port", oldPort, port);
	}

	public void setRunning(final boolean running)
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
		new Thread(() -> {
			try
			{
				final SerialPort serialPort = SerialPort.getCommPort(port);
				serialPort.setBaudRate(57600);
				serialPort.setParity(SerialPort.NO_PARITY);
				serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
				serialPort.setNumDataBits(8);

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
			catch (Exception e)
			{
				e.printStackTrace();
				setError("Error: " + e.getMessage());
				setRunning(false);
			}
		}).start();
	}
}
