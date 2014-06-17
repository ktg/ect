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

 Created by: Chris Allsop (University of Nottingham)
 Contributors:
 Chris Greenhalgh (University of Nottingham)
 Chris Allsop (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.components.x10;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import equip.ect.components.x10.javaX10project.CM11ASerialController;
import equip.ect.components.x10.javaX10project.Command;

/**
 * Interface to serial-connected X10 controller. <H3>Description</H3> The X10_OneWay component can
 * be used to connect to an X10 controller connected via a COM port. After connecting, it can be
 * used to create components which can communicate with any appliance or lamp modules which the
 * controller can communicate with. Lamp modules can be used to set the lighting level of a lamp
 * plugged into a module (from 0 lighting level to max lighting level) and appliance modules can be
 * used to turn appliances on and off. <H3>Installation</H3> Before using this component, you should
 * <ul>
 * <li>plug your x10 controller and lamp and appliance modules into AC power sockets (these must be
 * on the same ring main)
 * <li>assign your lamp and appliance modules unique ids by turning the dials on the front of the
 * modules
 * <li>plug any required lamps and appliances into the modules
 * <li>connect your x10 controller to a spare serial port on your computer (alternatively, you can
 * use a serial->usb converter and connect to a usb port, but you will have to install a software
 * driver for the converter)
 * </ul>
 * <H3>Configuration</H3>
 * <P>
 * Create an instance of the X10_OneWay component, and then set the <i>configPort</i> property to
 * the name of the COM port to which your controller is connected (eg this might be COM1).
 * </p>
 * <p>
 * If you are using an X10 controller connected by USB, the device will appear as a USB-to-serial
 * adapter. To find the COM port given to the convertor, from the 'Window Control Panel' select
 * 'System'. In the 'System Properties' dialog which appears, select the 'Hardware' tab. From this
 * tab press the 'Device Manager' button. From the tree list which appears, expand the 'Ports (COM &
 * LPT)' branch. The COM port address of you controller should be listed in brackets after
 * 'USB-to-serial'. Note that the above is also true if you are connecting a serial X10 module to
 * your PC via a USB-to-serial convertor.
 * </P>
 * <P>
 * Now set the <i>lampModule</i> and <i>applianceModule</i> properties to comma-seperated lists of
 * the unique ids of your lamp and appliance modules. For example, if you had two lamp modules with
 * ids <tt>A1</tt> and <tt>A2</tt>, and an appliance module with id <tt>B1</tt>, you would set
 * property <i>lampModule</i> to <tt>A1,A2</tt> and property <i>applianceModule</i> to <tt>B1</tt>.
 * This should cause one component per module to be created, which you can use to control the
 * module.
 * </P>
 * <H3>Usage</H3> <h4>Lamp module components</h4> Set property <i>on</i> to <tt>true</tt> to turn
 * your lamp all the way on, to <tt>false</tt> to turn it all the way off, or set a lighting level
 * using property <tt>level</tt> in the range 0-1.0. <h4>Appliance module components</h4> Set
 * property <i>on</i> to <tt>true</tt> to turn your appliance on, or to <tt>false</tt> to turn it
 * off. <h4>Other details</h4> Note that there is often a delay in between changing a property on a
 * module comonent and the lamp or appliance module being notified of this change.
 * 
 * @displayName X10_OneWay
 * @classification Hardware/Output
 * @preferred
 * @technology X10
 */

public class X10_OneWay implements Serializable, /* , PropertyChangeListener, */
X10_Constants
{
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]+"); // \s = whitespace

	// character

	public static void parseAddress(final String address) throws BadAddressException
	{
		if (address.length() < 2) { throw new BadAddressException(address); }

		int unitcode;
		final char housecode = address.charAt(0);

		// Invalid if first character is not a letter or is not in the correct
		// range
		if (!Character.isLetter(housecode) || (!(housecode >= MIN_HOUSE_CODE && housecode <= MAX_HOUSE_CODE))) { throw new BadAddressException(
				housecode, address); }

		try
		{
			// Invalid if remaining characters do not form a number or the
			// number
			// is not in the correct range
			unitcode = Integer.parseInt(address.substring(1));
			if (!(unitcode >= MIN_UNIT_CODE && unitcode <= MAX_UNIT_CODE)) { throw new BadAddressException(unitcode,
					address); }

		}
		catch (final NumberFormatException nfe)
		{
			throw new BadAddressException(address);
		}
	}

	/** The port that the X10_Controller is plugged into. */
	private String port;
	/** Name of this bean. */
	private String name;

	/**
	 * The <b>actual</b> controller object, either an instance of CM11ASerialController or
	 * CM17ASerialController.
	 */
	private CM11ASerialController x10Controller;

	/**
	 * Space seperated string of lamp module addresses.
	 * <p>
	 * Each lamp module that you wish to communicate with via the controller must have it's address
	 * &gtHOME&lt &gtUNIT&lt e.g. "A1" as part of this string. Each address must be seperated by one
	 * or more whitespaces.
	 */
	private String lampModuleAddresses = "";

	/**
	 * Space seperated string of appliance module addresses.
	 * <p>
	 * Each appliance module that you wish to communicate with via the controller must have it's
	 * address &gtHOME&lt &gtUNIT&lt e.g. "A1" as part of this string. Each address must be
	 * seperated by one or more whitespaces.
	 */
	private String applianceModuleAddresses = "";

	/**
	 * Status of the controller, will report information on connection status and/or exceptions.
	 */
	private String status;

	/**
	 * Lamp module subcomponents, generated by parsing the lampModuleAddresses string
	 * <p>
	 * 
	 * @see #lampModuleAddresses
	 */
	private X10_LampModule[] lampModules;

	/** *************** END OF EXPOSABLE PROPERTIES **************** */

	/**
	 * Appliance module subcomponents, generated by parsing the applianceModuleAddresses string
	 * <p>
	 * 
	 * @see #applianceModuleAddresses
	 */
	private X10_ApplianceModule[] applianceModules;
	/**
	 * log4j logger, used to print to console (System.out) by default.
	 * 
	 * @see <a href="http://logging.apache.org/log4j/docs/">Log4j Documentation</a>
	 */
	private static transient Logger logger = Logger.getLogger(X10_OneWay.class);

	static
	{
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}
	/** Property Change delegate. */
	private final transient PropertyChangeSupport propertyChangeListeners;

	private static final String NO_PORT_MSG = "Please specify port on which controller is attached";

	/**
	 * HashMap to keep track of module addresses that this bean is controlling.
	 */
	final Map<String, X10_Module> registeredAddresses = new HashMap<String, X10_Module>();

	public X10_OneWay()
	{
		name = DEFAULT_NAME;
		lampModules = new X10_LampModule[0];
		applianceModules = new X10_ApplianceModule[0];
		propertyChangeListeners = new PropertyChangeSupport(this);
		status = NO_PORT_MSG;

		final String drivernames[] = new String[] { "com.sun.comm.Win32Driver" };
		for (final String drivername : drivernames)
		{
			try
			{
				final java.lang.Object driver = Class.forName(drivername).newInstance();
				final java.lang.reflect.Method init = driver.getClass().getMethod("initialize", new Class[0]);
				init.invoke(driver, new Object[0]);
				// ((javax.comm.Driver)driver).initialize();
				System.out.println("Initialised javax.comm driver " + drivername + " OK");
				break;
			}
			catch (final Exception e)
			{
				System.out.println("ERROR initialising javax.comm driver " + drivername + ": " + e.getMessage());
			}
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public String getApplianceModuleAddresses()
	{
		return applianceModuleAddresses;
	}

	public X10_ApplianceModule[] getApplianceModules()
	{
		return applianceModules;
	}

	public String getConfigPort()
	{
		return port;
	}

	public String getLampModuleAddresses()
	{
		return lampModuleAddresses;
	}

	public X10_LampModule[] getLampModules()
	{
		return lampModules;
	}

	public String getName()
	{
		return name;
	}

	public String getStatus()
	{
		return status;
	}

	public CM11ASerialController getX10Controller()
	{
		return x10Controller;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public synchronized void setApplianceModuleAddresses(final String addressStr)
	{
		final String oldAddresses = applianceModuleAddresses;
		applianceModuleAddresses = addressStr;
		propertyChangeListeners.firePropertyChange("applianceModuleAddresses", oldAddresses, applianceModuleAddresses);

		if (x10Controller != null)
		{
			updateApplianceModules();
		}
	}

	public synchronized void setConfigPort(final String newPort)
	{
		try
		{
			final CM11ASerialController oldX10Controller = x10Controller;
			final CM11ASerialController newX10Controller = new CM11ASerialController(newPort);

			setStatus("Successfully listening on " + newPort);

			propertyChangeListeners.firePropertyChange("configPort", port, newPort);
			port = newPort;

			propertyChangeListeners.firePropertyChange("x10Controller", oldX10Controller, newX10Controller);

			// gracefully shut down controller on old port
			stop();

			x10Controller = newX10Controller;
			updateApplianceModules();
			updateLampModules();

			// IOException thrown if we give the Controller a bad port
		}
		catch (final IOException ioe)
		{
			setStatus(ioe.getMessage());
		}
	}

	public synchronized void setLampModuleAddresses(final String addressStr)
	{
		final String oldAddresses = lampModuleAddresses;
		lampModuleAddresses = addressStr;
		propertyChangeListeners.firePropertyChange("lampModuleAddresses", oldAddresses, lampModuleAddresses);

		if (x10Controller != null)
		{
			updateLampModules();
		}
	}

	public synchronized void setName(final String newName)
	{
		if (!newName.equals(name))
		{
			propertyChangeListeners.firePropertyChange("name", name, newName);
			name = newName;
		}
	}

	public void stop()
	{
		if (x10Controller != null)
		{
			x10Controller.shutdownNow();
		}
	}

	/** Sets the Status Message. */
	protected synchronized void setStatus(final String msg)
	{
		if (msg != status)
		{
			propertyChangeListeners.firePropertyChange("status", status, msg);
			status = msg;
		}
	}

	private synchronized void updateApplianceModules()
	{
		final String addresses = applianceModuleAddresses.trim().toUpperCase();
		final String[] strsTemp = ((addresses.equals("") ? new String[0] : WHITESPACE.split(addresses)));

		final HashSet<String> strs = new HashSet<String>(Arrays.asList(strsTemp));

		final StringBuffer invalidAddresses = new StringBuffer();
		final HashSet<String> newAddresses = new HashSet<String>();
		for (final String address : strs)
		{
			try
			{
				// first check the address is meaningful
				parseAddress(address); // throws BadAddressException

				// secondly check that no other *type* of module is registered
				// with this address
				final X10_Module module = registeredAddresses.get(address);
				if (module != null && !(module instanceof X10_ApplianceModule)) { throw new BadAddressException(module,
						address); }

				newAddresses.add(address);

			}
			catch (final BadAddressException bae)
			{
				logger.warn(bae.getMessage());
				invalidAddresses.append(bae.getInvalidAddress() + " ");
			}
		}

		// check which old modules we can reuse
		final X10_Module[] oldModules = applianceModules;
		final List<X10_Module> modules = new ArrayList<X10_Module>();
		for (final X10_Module module : oldModules)
		{
			final String address = module.getAddress();
			if (newAddresses.contains(address))
			{
				modules.add(module);
				// remove the address, since we dont have to create a module for
				// it
				newAddresses.remove(address);
			}
			else
			{
				if (module.isOn())
				{
					module.setOn(false);
				}
				registeredAddresses.remove(module.getAddress());
			}
		}

		for (final String address : newAddresses)
		{
			final X10_Module module = new X10_ApplianceModule(this, address);
			modules.add(module);
			registeredAddresses.put(module.getAddress(), module);
			// After registering this address we must sent an X10 command to ensure our
			// new module is off in reality. Ideally this should be done in the
			// constructor however, generating the command in the constructor doesn't work
			// because an exception is thrown since the module is not 'registered' at
			// construction time) - confused ?!? :)
			x10Controller.addCommand(new Command(module.getAddress(), Command.OFF));
		}

		applianceModules = new X10_ApplianceModule[modules.size()];
		modules.toArray(applianceModules);
		propertyChangeListeners.firePropertyChange("applianceModules", oldModules, applianceModules);

		if (invalidAddresses.length() > 0)
		{
			setStatus("Couldn't create the following appliance modules: " + invalidAddresses.toString().trim());
		}
		else
		{
			setStatus("OK");
		}
	}

	private synchronized void updateLampModules()
	{
		final String addresses = lampModuleAddresses.trim().toUpperCase();
		final String[] strsTemp = ((addresses.equals("") ? new String[0] : WHITESPACE.split(addresses)));

		final HashSet<String> strs = new HashSet<String>(Arrays.asList(strsTemp));

		final StringBuffer invalidAddresses = new StringBuffer();
		final HashSet<String> newAddresses = new HashSet<String>();
		for (final String address : strs)
		{
			try
			{
				// first check the address is meaningful
				parseAddress(address); // throws BadAddressException

				// secondly check that no other *type* of module is registered
				// with this address
				final X10_Module module = registeredAddresses.get(address);

				if (module != null && !(module instanceof X10_LampModule)) { throw new BadAddressException(module,
						address); }
				newAddresses.add(address);

			}
			catch (final BadAddressException bae)
			{
				logger.warn(bae.getMessage());
				invalidAddresses.append(bae.getInvalidAddress() + " ");
			}
		}

		// check which old modules we can reuse
		final X10_Module[] oldModules = lampModules;
		final List<X10_Module> modules = new ArrayList<X10_Module>();
		for (final X10_Module module : oldModules)
		{
			final String address = module.getAddress();
			if (newAddresses.contains(address))
			{
				// Re-use existing module, don't create
				modules.add(module);
				newAddresses.remove(address);
			}
			else
			{
				// Remove existing module
				if (module.isOn())
				{
					module.setOn(false);
				}
				registeredAddresses.remove(address);
			}
		}

		for (final String address : newAddresses)
		{
			final X10_Module module = new X10_LampModule(this, address);
			modules.add(module);
			registeredAddresses.put(module.getAddress(), module);
			// After registering this address we must sent an X10 command to ensure our
			// new module is off in reality. Ideally this should be done in the
			// constructor however, generating the command in the constructor doesn't work
			// because an exception is thrown since the module is not 'registered' at
			// construction time) - confused ?!? :)
			x10Controller.addCommand(new Command(module.getAddress(), Command.OFF));
		}

		lampModules = new X10_LampModule[modules.size()];
		modules.toArray(lampModules);
		propertyChangeListeners.firePropertyChange("lampModules", oldModules, lampModules);

		if (invalidAddresses.length() > 0)
		{
			setStatus("Couldn't create the following lamp modules: " + invalidAddresses.toString().trim());
		}
		else
		{
			setStatus("OK");
		}
	}
}