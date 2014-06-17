package equip.ect.components.mobiledevice.simplebluetoothserver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import com.sun.media.Connector;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class SimpleBluetoothServer implements Serializable
{

	class ConnectionHandler implements Runnable
	{

		private StreamConnection conn;

		private boolean connected;

		ConnectionHandler(final StreamConnection newConn)
		{
			this.conn = newConn;
		}

		@Override
		public void run()
		{

			DataInputStream din = null;
			DataOutputStream dout = null;
			try
			{
				din = new DataInputStream(conn.openInputStream());
				dout = new DataOutputStream(conn.openOutputStream());

				while (connected)
				{
					String cmd = "";
					char c;
					while (((c = din.readChar()) > 0) && (c != '\n'))
					{
						cmd = cmd + c;
					}
					System.out.println("Received " + cmd);
					if (cmd.equals("<GET>"))
					{
						System.out.println("Sending =>" + message);
						dout.writeUTF(message + '\n');
					}
				}

			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				try
				{
					din.close();
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try
				{
					dout.close();
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try
				{
					conn.close();
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		public void start()
		{
			connected = true;
			new Thread(this).start();
		}

		public void stop()
		{
			connected = false;
		}
	}

	public static void main(final String args[])
	{
		final SimpleBluetoothServer echoserver = new SimpleBluetoothServer();
	}

	public final UUID uuid = new UUID( // the uid
			// of the
			// service,
			// it has to
			// be
			// unique,
			"27012f0c68af4fbf8dbe6bbaf7aa432a", false); // it can be generated

	// randomly
	public final String name = "BT Server"; // the name

	// of the
	// service
	public final String url = "btspp://localhost:" + uuid // the
			// service
			// url
			+ ";name=" + name + ";authenticate=false;encrypt=false;";

	LocalDevice local = null;

	StreamConnectionNotifier server = null;

	private String message = "Wow what a great way\n to be, ingenious";

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SimpleBluetoothServer()
	{
		try
		{
			System.out.println("Setting device to be discoverable...");
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);
			System.out.println("Start advertising service...");
			server = (StreamConnectionNotifier) Connector.open(url);
			System.out.println("Waiting for incoming connection...");
			new Thread()
			{

				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							final StreamConnection conn = server.acceptAndOpen();
							System.out.println("Client Connected...");
							new ConnectionHandler(conn).start();
						}
						catch (final IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		catch (final Exception e)
		{
			System.out.println("Exception Occured: " + e.toString());
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getMessage()
	{
		return message;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setMessage(final String message)
	{
		final String old = this.message;
		this.message = message;
		propertyChangeListeners.firePropertyChange("message", old, this.message);
	}

}