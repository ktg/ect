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
  Chris Allsop (University of Nottingham)

 */

package equip.ect.components.x10;

import equip.ect.components.x10.javaX10project.Command;
import equip.ect.components.x10.javaX10project.CommandPair;

/**
 * Any X10_Module that is variable (for example can be dimmed or brightened to a variable level)
 * should extend this abstract class.
 */
public abstract class X10_DimmableModule extends X10_Module
{

	public static double dimPercentageToLevel(final int percentage)
	{
		// assuming current module brightness is OFF or at FULL BRIGHTNESS (at 100%)
		return 1.0f - ((double) percentage / MAX_PERCENTAGE);
	}

	public static int levelToDimPercentage(final double level)
	{
		return (int) ((1.0f - level) * 100);
	}

	public static int parsePercentage(final String str) throws NumberFormatException
	{
		final int level = Integer.parseInt(str);
		if (level < MIN_PERCENTAGE || level > MAX_PERCENTAGE) { throw new NumberFormatException(level
				+ " is an invalid percentile, must be in rang [0-100]"); }
		return level;
	}

	public static double roundToNearestLevel(final double decimal)
	{
		return (double) ((int) (decimal * NUM_BRIGHTNESS_LEVELS)) / NUM_BRIGHTNESS_LEVELS;
	}

	/**
	 * The light level of an X10 module will be represented as a floating point value (percentage)
	 * to comply with more recent ect components such as testing slider etc...
	 */
	protected double level;

	public static final String LEVEL_PROPERTY = "level";

	public X10_DimmableModule(final X10_OneWay parent, final String address)
	{
		super(parent, address);
		this.level = 0.0f;
	}

	public double getLevel()
	{
		return this.level;
	}

	@Override
	public abstract String getPersistentChild();

	public synchronized void setLevel(double newLevel)
	{

		// Decimal value of the level should be rounded down to a valid a
		// multiple of 1/NUM_BRIGHTNESS_LEVELS.
		newLevel = roundToNearestLevel(newLevel);

		if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL || newLevel == this.level) { return; }

		// We now need to dim the module by the appropriate percentage.
		// A level of 1.0 means dim by 0% and a level of 0.0 means dim by 100%
		final Command dim = new Command(this.address, Command.DIM, levelToDimPercentage(newLevel));

		// First turn the module off. Any dim command will then first turn the module
		// on to full (100%) brightness before dimming commences.
		final Command turnOff = new Command(this.address, Command.OFF);
		parent.getX10Controller().addCommandPair(new CommandPair(turnOff, dim));

		if (!this.isOn())
		{
			propertyChangeListeners.firePropertyChange(ON_PROPERTY, this.isOn(), true);
			this.on = true;
		}

		propertyChangeListeners.firePropertyChange(LEVEL_PROPERTY, new Double(this.level), new Double(newLevel));
		this.level = newLevel;
	}

	@Override
	public synchronized void setOn(final boolean flag)
	{
		super.setOn(flag);
		final double oldlevel = this.level;
		this.level = ((flag) ? 1.0f : 0.0f);
		// when a module is requested by x10 to turn on and *IT ISN'T ALREADY ON*,
		// it defaults to 100% brightness search google for 'x10 nova effect'
		propertyChangeListeners.firePropertyChange(LEVEL_PROPERTY, new Double(oldlevel), new Double(this.level));
	}

}
