package equip.ect.components.fileio;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

@ECTComponent
@Category("Input")
public class BluetoothLEInput implements Serializable
{
	private transient final PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private String address = "";
	private String characteristic = "0xe";
	private boolean running = false;
	private int value;
	private transient Thread thread;

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		String oldAddress = this.address;
		this.address = address;
		propertyChangeListeners.firePropertyChange("address", oldAddress, address);

	}

	public String getCharacteristic()
	{
		return characteristic;
	}

	public void setCharacteristic(String characteristic)
	{
		String oldCharacteristic = this.characteristic;
		this.characteristic = characteristic;
		propertyChangeListeners.firePropertyChange("characteristic", oldCharacteristic, characteristic);
	}

	public int getValue()
	{
		return value;
	}

	private void setValue(int value)
	{
		int oldValue = this.value;
		this.value = value;
		propertyChangeListeners.firePropertyChange("value", oldValue, value);
	}

	public boolean isRunning()
	{
		return running;
	}

	public void setRunning(boolean running)
	{
		boolean oldRunning = this.running;
		this.running = running;
		if(running != oldRunning)
		{
			propertyChangeListeners.firePropertyChange("running", oldRunning, running);

			if(running)
			{
				thread = new Thread(this::run);
				thread.start();
			}
		}
	}

	private Process createProcess() throws IOException
	{
		final ProcessBuilder builder = new ProcessBuilder("gatttool", "-b", address, "-t", "random", "--char-write-req", "-a", characteristic, "-n", "0100", "--listen");
		return builder.start();
	}

	private Integer getValue(String line)
	{
		try
		{
			String[] valueLine = line.split("value: ");
			if (valueLine.length > 1)
			{
				String[] values = valueLine[1].split(" ");
				String result = "";
				for (String value : values)
				{
					result = result + (char) Integer.parseInt(value, 16);
				}

				return Integer.parseInt(result);
			}
		}
		catch (Exception e)
		{
			return null;
		}
		return null;
	}

	private synchronized void run()
	{
		boolean error = false;
		while (running)
		{
			try
			{
				if (error)
				{
					wait(1000);
				}

				while (running)
				{
					Process process = null;
					try
					{
						String line;
						process = createProcess();
						final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
						while (running && (line = reader.readLine()) != null)
						{
							final Integer value = getValue(line);
							if (value != null)
							{
								// TODO Hack here to ignore erroneous values
								if(value >= 100  && value <= 1024)
								{
									setValue(value);
								}
							}
						}
					}
					catch (Throwable e)
					{
						System.err.println(e.getMessage());
					}
					if(process != null) {
						process.destroy();
					}
				}
			}
			catch (Throwable e)
			{
				System.err.println(e.getMessage());
				error = true;
			}
		}
	}
}