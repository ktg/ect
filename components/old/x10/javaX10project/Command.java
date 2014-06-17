/*
 * Copyright 2002-2003, Wade Wassenberg  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package equip.ect.components.x10.javaX10project;

import java.io.Serializable;

import equip.ect.components.x10.X10_Constants;

/**
 * Command represents an x10 command. This class encapsulates the house code, unit code, function,
 * and optionally the level value for the command.<BR>
 * <BR>
 * 
 * An instance of this class may be constructed and posted to a Controller to have the command
 * executed.<BR>
 * <BR>
 * 
 * Also, a UnitEvent contains the command that generated the event.
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 * @see x10.Controller
 * @see x10.UnitEvent
 */

public class Command implements Serializable, /* TEMP */X10_Constants
{

	/**
	 * getAddress returns a String-based x10 address as a [HouseCode][UnitCode] two-character
	 * String, based on the specified address in x10 "CM11A" protocol format.
	 * 
	 * @param address
	 *            the x10 address as a "CM11A" protocol value.
	 * @return String - the x10 addres as a two-character [Housecode][Unitcode] address (ie "G5").
	 * 
	 */

	public static String getAddress(final byte address)
	{
		final byte house = ((byte) ((address >> 4) & 0x0000000F));
		final byte unit = ((byte) (address & 0x0000000F));
		int unitCode = -1;
		int houseCode = -1;
		for (int i = 0; i < ADDRESS_CODES.length; i++)
		{
			if (ADDRESS_CODES[i] == house)
			{
				houseCode = i;
			}
			if (ADDRESS_CODES[i] == unit)
			{
				unitCode = i;
			}
			if ((houseCode > -1) && (unitCode > -1)) { return (((char) ('A' + houseCode)) + "" + (unitCode + 1)); }
		}
		return (null);
	}

	/**
	 * getFunction returns the function as a constant (specified above), based on the specified
	 * function in x10 "CM11A" protocol format.
	 * 
	 * @param function
	 *            the x10 function as a "CM11A" protocol value.
	 * @return byte - the x10 function as a constant (see above).
	 * 
	 */

	public static byte getFunction(final byte function)
	{
		return ((byte) (function & 0x0000000F));
	}

	/**
	 * isValid checks the specified address to see if it is valid. A valid address is one whose
	 * housecode is between 'A' and 'P' inclusive, and whose unitcode is between '1' and '16'
	 * inclusive. To be valid, the address String must be of the format [HouseCode][UnitCode] (ie.
	 * "P16").
	 * 
	 * @param address
	 *            a housecode and unitcode as a two-character String
	 * @return boolean - true if the address is a valid x10 address, false otherwise
	 * 
	 */

	public static boolean isValid(final String address)
	{
		final byte housecode = (byte) (Character.toUpperCase(address.charAt(0)) - 'A');
		byte unitcode = 0;
		try
		{
			unitcode = (byte) Integer.parseInt(address.substring(1));
		}
		catch (final NumberFormatException nfe)
		{
			return (false);
		}
		return ((housecode <= 15) && (unitcode <= 16) && (housecode >= 0) && (unitcode >= 1));
	}

	public static String shortToBinaryString(final short sh)
	{
		int power;
		final StringBuffer buff = new StringBuffer();
		for (int bit = 15; bit >= 0; bit--)
		{

			power = ((bit > 0) ? (2 << (bit - 1)) : 1);
			buff.append((power == (sh & power)) ? '1' : '0');
		}
		return buff.toString();
	}

	private short address;

	private short function;

	private long timestamp;

	private boolean executing = false;

	private static final byte[] ADDRESS_CODES = { ((byte) 0x06), ((byte) 0x0E), ((byte) 0x02), ((byte) 0x0A),
													((byte) 0x01), ((byte) 0x09), ((byte) 0x05), ((byte) 0x0D),
													((byte) 0x07), ((byte) 0x0F), ((byte) 0x03), ((byte) 0x0B),
													((byte) 0x00), ((byte) 0x08), ((byte) 0x04), ((byte) 0x0C) };

	/**
	 * ALL_UNITS_OFF byte - the x10 function "All Units Off". This function turns all x10 units off.
	 * 
	 * @serial
	 */
	public static final byte ALL_UNITS_OFF = ((byte) 0x00);

	/**
	 * ALL_LIGHTS_ON byte - the x10 function "All Lights On". This function turns all x10 light
	 * modules on.
	 * 
	 * @serial
	 */

	public static final byte ALL_LIGHTS_ON = ((byte) 0x01);

	/**
	 * ON byte - the x10 function "On". This function turns the specified module on.
	 * 
	 * @serial
	 */

	public static final byte ON = ((byte) 0x02);

	/**
	 * OFF byte - the x10 function "Off". This function turns the specified module off.
	 * 
	 * @serial
	 */

	public static final byte OFF = ((byte) 0x03);
	/**
	 * DIM byte - the x10 function "Dim". This function dims the specified module to the specified
	 * level.
	 * 
	 * @serial
	 */

	public static final byte DIM = ((byte) 0x04);

	/**
	 * BRIGHT byte - the x10 function "Bright". This function brightens the specified module to the
	 * specified level.
	 * 
	 * @serial
	 */

	public static final byte BRIGHT = ((byte) 0x05);
	/**
	 * ALL_LIGHTS_OFF byte - the x10 function "All Lights Off". This function turns all x10 light
	 * modules off.
	 * 
	 * @serial
	 */

	public static final byte ALL_LIGHTS_OFF = ((byte) 0x06);
	private static final byte ADDRESS = ((byte) 0x04);

	private static final byte FUNCTION = ((byte) 0x06);

	private String addressString;

	private byte functionByte;

	private int level;

	/**
	 * Command constructs a Command object with the specified address and function. This constructor
	 * is typically only called by a Controller. It is recommended that instances of this class be
	 * constructed with one of the String-parameter-based constructors.
	 * 
	 * @param address
	 *            an x10 module's housecode and unitcode in "CM11A" protocol format.
	 * @param function
	 *            an x10 function in "CM11A" protocol format.
	 * 
	 */

	public Command(final byte address, final byte function)
	{
		this(getAddress(address), getFunction(function));

	}

	/**
	 * Command constructs a Command object with the specified address, function, and level. This
	 * constructor is typically only called by a Controller. It is recommended that instances of
	 * this class be constructed with one of the String-parameter-based constructors.
	 * 
	 * @param address
	 *            an x10 module's housecode and unitcode in "CM11A" protocol format.
	 * @param function
	 *            an x10 function in "CM11A" protocol format.
	 * @param level
	 *            an x10 module level in "CM11A" protocol format.
	 * 
	 */

	public Command(final byte address, final byte function, final byte level)
	{
		int aLevel = ((int) (((level & 0x000000FF) / 210.0) * 100.0));
		if (aLevel > 100)
		{
			aLevel = 100;
		}
		else if (aLevel < 0)
		{
			aLevel = 0;
		}
		setAddress(getAddress(address));
		setFunction(getFunction(function), aLevel);

	}

	/**
	 * Command constructs a Command object with the specified address and function. The address must
	 * be specified as a two-character String of the formmat [HouseCode][UnitCode] (ie. "A1"). The
	 * function must be one of the constant functions that are described above.
	 * 
	 * @param address
	 *            the housecode and unitcode as a two-character String
	 * @param function
	 *            a valid function constant (listed above).
	 * @exception IllegalArgumentException
	 *                thrown if either parameter is invalid. An address is illegal if the housecode
	 *                is greater than 'P' or if the unitcode is greater than 16. The function is
	 *                illegal if it is not one of the functions listed as a constant (listed above).
	 * 
	 */

	public Command(final String address, final byte function) throws IllegalArgumentException
	{
		setAddress(address);
		setFunction(function);
	}

	/**
	 * Command constructs a Command object with the specified address, function, and level. The
	 * address must be specified as a two-character String of the formmat [HouseCode][UnitCode] (ie.
	 * "A1"). The function must be one of the constant functions that are described above. The level
	 * is a percentage and must be between 0 and 100 inclusive.
	 * 
	 * @param address
	 *            the housecode and unitcode as a two-character String
	 * @param function
	 *            a valid function constant (listed above).
	 * @param level
	 *            the percentage by which the level should change.
	 * @exception IllegalArgumentException
	 *                thrown if either parameter is invalid. An address is illegal if the housecode
	 *                is greater than 'P' or if the unitcode is greater than 16. The function is
	 *                illegal if it is not one of the functions listed as a constant (listed above).
	 * 
	 */

	public Command(final String address, final byte function, final int level) throws IllegalArgumentException
	{
		setAddress(address);
		setFunction(function, level);
	}

	// equality is determined by a command having the same address and function type
	// as another command
	// that is all DIM commands for module X10 are equal (the 'level' is irrelevant)
	@Override
	public boolean equals(final Object obj)
	{
		Command cmd;
		if (obj instanceof Command)
		{
			cmd = (Command) obj;
			return (this.getFunctionByte() == cmd.getFunctionByte() && this.getAddress() == cmd.getAddress());

		}
		else
		{
			return false;
		}
	}

	/**
	 * getAddress returns the x10 address in ready-to-send "CM11A" protocol format.
	 * 
	 * @return short - the x10 address in "CM11A" protocol format.
	 * 
	 */

	public short getAddress()
	{
		return (address);
	}

	public String getAddressString()
	{
		return addressString;
	}

	/**
	 * getFunction returns the x10 function in ready-to-send "CM11A" protocol format.
	 * 
	 * @return short - the x10 function in "CM11A" protocol format.
	 * 
	 */

	public short getFunction()
	{
		return (function);
	}

	/**
	 * getFunctionByte returns the function of this Command.
	 * 
	 * @return byte - the function constant of this Command (as listed above).
	 * 
	 */

	public byte getFunctionByte()
	{
		return (functionByte);
	}

	/**
	 * getHouseCode returns the housecode as a character. The returned housecode is always uppercase
	 * and between 'A' and 'P' inclusively.
	 * 
	 * @return char the housecode of the module affected by this command
	 * 
	 */

	public char getHouseCode()
	{
		return (Character.toUpperCase(addressString.charAt(0)));
	}

	/**
	 * getLevel returns the level associated with this Command. This level is always positive and
	 * between 0 and 100 inclusive. The level is modified by weather the function is a Dim or
	 * Bright.
	 * 
	 * @return int - the percentage level change.
	 * 
	 */

	public int getLevel()
	{
		return (level);
	}

	public long getTimestamp()
	{
		return this.timestamp;
	}

	/**
	 * getUnitCode returns the unitcode as an integer. The returned unitcode is always between 1 and
	 * 16 inclusive.
	 * 
	 * @return int the unitcode of the module affected by this command
	 * 
	 */

	public int getUnitCode()
	{
		return (Integer.parseInt(addressString.substring(1)));
	}

	public boolean isExecuting()
	{
		return this.executing;
	}

	/**
	 * setAddress sets the housecode and unitcode of this command based on the specified address.
	 * 
	 * @param address
	 *            a housecode and unitcode as a two-character String
	 * @exception IllegalArgumentException
	 *                thrown if the address is invalid.
	 * @see #isValid
	 * 
	 */

	public void setAddress(final String address) throws IllegalArgumentException
	{
		final byte housecode = (byte) (Character.toUpperCase(address.charAt(0)) - 'A');
		byte unitcode = 0;
		try
		{
			unitcode = (byte) (Integer.parseInt(address.substring(1)) - 1);
		}
		catch (final NumberFormatException nfe)
		{
			throw new IllegalArgumentException("No such device: " + address);
		}
		if ((housecode > 15) || (unitcode > 15) || (housecode < 0) || (unitcode < 0)) { throw new IllegalArgumentException(
				"No such device: " + address); }
		this.address = (short) ((ADDRESS << 8) | (ADDRESS_CODES[housecode] << 4) | (ADDRESS_CODES[unitcode]));
		this.addressString = address;
	}

	public void setExecuting(final boolean executing)
	{
		this.executing = executing;
	}

	/**
	 * setFunction sets the function of this command.
	 * 
	 * @param func
	 *            a valid function constant (listed above).
	 * @exception IllegalArgumentException
	 *                thrown if the specified function is not one of the constant functions listed
	 *                above.
	 * 
	 */

	public void setFunction(final byte func) throws IllegalArgumentException
	{
		function = ((short) (0x000000F0 & address));
		function = ((short) ((FUNCTION << 8) | (function) | (func)));
		this.functionByte = func;
	}

	/**
	 * setFunction sets the function and level of this command.
	 * 
	 * @param func
	 *            a valid function constant (listed above).
	 * @param level
	 *            the percentage by which the level should change.
	 * @exception IllegalArgumentException
	 *                if the specified function is not one of the constant functions listed above.
	 * 
	 */

	public void setFunction(final byte func, final int level) throws IllegalArgumentException
	{
		final byte lvalue = ((byte) ((level * 22) / 100));
		if ((lvalue > 22) || (lvalue < 0)) { throw new IllegalArgumentException("Invalid dim percent: " + level); }
		setFunction(func);
		function = ((short) (function | (lvalue << 11)));
		// System.out.println("Sending " + lvalue + " dims [" + shortToBinaryString(function) +
		// "] to module (" + level + "%)");
		this.functionByte = func;
		this.level = level;
	}

	@Override
	public String toString()
	{

		String name;

		switch (this.getFunctionByte())
		{
			case Command.ALL_LIGHTS_ON:
				name = (ALL_LIGHTS_ON_CMD);
				break;
			case Command.ALL_UNITS_OFF:
				name = (ALL_UNITS_OFF_CMD);
				break;
			case Command.ALL_LIGHTS_OFF:
				name = (ALL_LIGHTS_OFF_CMD);
				break;
			case Command.BRIGHT:
				name = (BRIGHT_CMD);
				break;
			case Command.DIM:
				name = (DIM_CMD);
				break;
			case Command.ON:
				name = (ON_CMD);
				break;
			case Command.OFF:
				name = (OFF_CMD);
				break;
			default:
				name = ("Unknown command name!");
		}
		return ("CMD(" + name + ":" + addressString + ":" + level + ":"
				+ ((this.isExecuting()) ? "executing" : "queued") + ")");
	}
}