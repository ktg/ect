/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Olov Stahl (SICS)

 */
package equip.ect.components.ipergobjectproxy.part.test.j2me;

import equip.ect.components.ipergobjectproxy.part.SetPropertyObjectCallback;
import equip.ect.components.ipergobjectproxy.part.TestSetPropertyObject;
import equip.ect.components.x10.javaX10project.Command;
import org.iperg.platform.core.IpEvent;
import org.iperg.platform.core.IpEventHandler;
import org.iperg.platform.core.IpUrl;
import org.iperg.platform.networking.IpLocalProcess;
import org.iperg.platform.synchronisation.IpVetoException;

/**
 * Testing phone app for ECT IpergObjectProxy.
 **/
public class ECTObjectServerTest extends MIDlet implements IpEventHandler, CommandListener, SetPropertyObjectCallback
{
	/**
	 * the single MIDP display
	 */
	private Display m_display; // Reference to Display object
	/**
	 * the text box in the single display
	 */
	private TextBox m_tbMain; // A Textbox to display a message
	/**
	 * the exit command
	 */
	private Command m_cmdExit; // A Command to exit the MIDlet
	/**
	 * the refresh command
	 */
	private Command m_cmdRefresh;
	/**
	 * the connect command
	 */
	private Command m_cmdConnect;

	/**
	 * server url;
	 */
	String CONNECT_URL = "btspp://008098648aec:1";

	/**
	 * a single test object
	 */
	TestSetPropertyObject obj;
	/**
	 * count
	 */
	int count = 0;

	/**
	 * noarg cons - don't do anything :-)
	 */
	public ECTObjectServerTest()
	{
		m_display = null;
		m_tbMain = null;
		m_cmdExit = null;

		// Command to the exit the MIDlet
		m_cmdExit = new Command("Exit", Command.EXIT, 1);
		m_cmdRefresh = new Command("Refresh", Command.SCREEN, 1);
		m_cmdConnect = new Command("Connect", Command.SCREEN, 1);

		// Create a textbox component
		m_tbMain = new TextBox("TextBox", "IPERG rules!", 150, 0);
		// Add the command onto the textbox
		m_tbMain.addCommand(m_cmdExit);
		m_tbMain.addCommand(m_cmdRefresh);
		m_tbMain.addCommand(m_cmdConnect);
		// Listen for events
		m_tbMain.setCommandListener(this);
	}

	/**
	 * check set property; throw IpVetoEvent if not; default allows all (SetPropertyObjectCallback)
	 */
	@Override
	public void checkSetProperty(final String name, final String value) throws IpVetoException
	{
	}

	public void commandAction(final Command cmd, final Displayable disp)
	{
		if (cmd == m_cmdExit)
		{
			destroyApp(false);
			notifyDestroyed();
		}
		else if (cmd == m_cmdRefresh)
		{
			if (obj != null)
			{
				try
				{
					count++;
					obj.setProperty(TestSetPropertyObject.INPUT, "input: " + count);
					setStatus("Object updated, " + TestSetPropertyObject.INPUT + "="
							+ obj.getProperty(TestSetPropertyObject.INPUT) + ", " + TestSetPropertyObject.OUTPUT + "="
							+ obj.getProperty(TestSetPropertyObject.OUTPUT));
				}
				catch (final Exception e)
				{
					setStatus("ERROR updating object: " + e);
				}
			}
		}
		else if (cmd == m_cmdConnect)
		{
			try
			{
				final IpUrl url = new IpUrl(CONNECT_URL);
				setStatus("Connecting to " + url + "...");
				IpLocalProcess.getInstance().connect(url, 15000);
				setStatus("Connected to " + url);
			}
			catch (final Exception e)
			{
				setStatus("Error connecting to " + CONNECT_URL + ": " + e);
			}
		}
	}

	/**
	 * bye bye
	 */
	public void destroyApp(final boolean unconditional)
	{
		IpLocalProcess.getInstance().notifyExitApp();
	}

	/**
	 * process event handler
	 */
	public void handleEvent(final IpEvent event)
	{
		// noop
	}

	/**
	 * property set (SetPropertyObjectCallback)
	 */
	@Override
	public void notifySetProperty(final String name, final String value)
	{
		setStatus("Property " + name + " set (remotely) to " + value);
	}

	/**
	 * Pauses midlet, standard method.
	 */
	public void pauseApp()
	{
		IpLocalProcess.getInstance().notifyPauseApp();
	}

	public void startApp()
	{
		setStatus("startApp");

		if (m_display == null)
		{
			m_display = Display.getDisplay(this);
			m_display.setCurrent(m_tbMain);

			IpLocalProcess.getInstance().setEventHandler(this);

			try
			{
				// Listen to incoming connections on port 1000. Does not
				// block. Whenever a connection is accepted, our eventCb
				// will be called with a IpProcessEvent argument.
				IpUrl url;
				// url = new IpUrl("tcp://:1000");
				// IpLocalProcess.getInstance().listen(url);
				// System.out.println("Listening to address " + url);
				// Also listen for Bluetooth connections
				url = new IpUrl("btspp://localhost:1");
				// System.out.println("Listening to address " + url);
				IpLocalProcess.getInstance().listen(url);
				setStatus("Listen to " + url.toString());
			}
			catch (final Exception e)
			{
				setStatus("Exception in listen: " + e.getClass().getName());
				// System.exit(-1);
			}

			obj = new TestSetPropertyObject();
			obj.setCallback(this);
		}
	}

	/**
	 * show status
	 */
	void setStatus(final String s)
	{
		m_tbMain.setString(s);
	}
}
