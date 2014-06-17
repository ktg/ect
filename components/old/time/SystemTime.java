/*
<COPYRIGHT>

Copyright (c) 2004-2005, Lancaster University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Lancaster University
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

Created by: Craig Morrall (Lancaster University)
Contributors:
  Craig Morrall (Lancaster University)

 */
package equip.ect.components.time;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Calendar;

/**
 * class for getting the system time - might be useful for setting the clock on sytems eg. one
 * clock, for vcr, tv, stero, etc.<br>
 * <H3>Summary</H3> Class for getting the system time<br>
 * Might be useful for setting the clock on sytems eg. one clock, for vcr, tv, stero, etc.<br>
 * <H3>Usage</H3> The system time component displays the current time as defined by the computer on
 * which the component is running.<br>
 * The following properties are available for use:<br>
 * - hours : The hour of the day as per 12 hour clock<br>
 * - hoursInDay : The hour of the day as per 24 hour clock<br>
 * - minutes : The minutes of the current hour<br>
 * - seconds : The seconds of te current minute<br>
 * - time : The current time in the form hh:mm:ss where hh specifies time as per a 12 hour clock<br>
 * 
 * @classification Behaviour/Timing
 * @preferred
 * @author Craig Morrall
 * @date 03/06/2004
 */
public class SystemTime extends Thread implements Serializable, PropertyChangeListener
{
	/**
	 * main method
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(final String[] args)
	{
		systemTime = new SystemTime();
	}// end of main method

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private static SystemTime systemTime; // static instance of ourselves
	// properties
	private int seconds;
	private int minutes;
	private int hours;
	private int hoursInDay;

	private String time;

	/**
	 * constructor method
	 */
	public SystemTime()
	{
		addPropertyChangeListener(this); // add us to property listener
		// set start times
		final Calendar calendar = Calendar.getInstance();
		seconds = calendar.get(Calendar.SECOND);
		minutes = calendar.get(Calendar.MINUTE);
		hours = calendar.get(Calendar.HOUR);
		hoursInDay = calendar.get(Calendar.HOUR_OF_DAY);
		this.start(); // start the clock ticking
	}// end of constructor method

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * method to get number of hours
	 */
	public int getHours()
	{
		return hours;
	}// end of method getHours

	/**
	 * method to get get number of hours in day
	 */
	public int getHoursInDay()
	{
		return hoursInDay;
	}// end of method getHoursInDay

	/**
	 * method to get number of minutes
	 */
	public int getMinutes()
	{
		return minutes;
	}// end of method getMinutes

	// get methods

	/**
	 * method to get number of seconds
	 */
	public int getSeconds()
	{
		return seconds;
	}// end of method getSeconds

	/**
	 * method to get the time
	 */
	public String getTime()
	{
		return time;
	}// end of method getTime

	// signal a property change event
	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	// clock ticking
	@Override
	public void run()
	{
		while (true)
		{
			// set old times
			final int oldSeconds = seconds;
			final int oldMinutes = minutes;
			final int oldHours = hours;
			final int oldHoursInDay = hoursInDay;
			final String oldTime = time;

			// set new times
			final Calendar calendar = Calendar.getInstance();
			seconds = calendar.get(Calendar.SECOND);
			minutes = calendar.get(Calendar.MINUTE);
			hours = calendar.get(Calendar.HOUR);
			hoursInDay = calendar.get(Calendar.HOUR_OF_DAY);

			time = createTime(seconds, minutes, hours);

			// only update properties that need altering
			if (seconds != oldSeconds)
			{
				propertyChangeListeners.firePropertyChange("seconds", oldSeconds, seconds);
			}
			if (minutes != oldMinutes)
			{
				propertyChangeListeners.firePropertyChange("minutes", oldMinutes, minutes);
			}
			if (hours != oldHours)
			{
				propertyChangeListeners.firePropertyChange("hours", oldHours, hours);
			}
			if (hoursInDay != hoursInDay)
			{
				propertyChangeListeners.firePropertyChange("hoursInDay", oldHoursInDay, hoursInDay);
			}
			if (!time.equals(oldTime))
			{
				propertyChangeListeners.firePropertyChange("time", oldTime, time);
			}

			try
			{
				sleep(990); // sleep for just under a second to allow data to be processed
			}// end of try statement
			catch (final InterruptedException e)
			{
			}// end of catch statement
		}// end of while loop
	}// end of method run

	/**
	 * method to create a time string
	 * 
	 * @param seconds
	 *            the number of seconds
	 * @param minutes
	 *            the number of minutes
	 * @param hours
	 *            the number of hours
	 */
	private String createTime(final int seconds, final int minutes, final int hours)
	{
		String theTime = new String();
		if (hours < 10)
		{
			theTime += "0";
		}
		theTime += Integer.toString(hours);
		theTime += ":";
		if (minutes < 10)
		{
			theTime += "0";
		}
		theTime += Integer.toString(minutes);
		theTime += ":";
		if (seconds < 10)
		{
			theTime += "0";
		}
		theTime += Integer.toString(seconds);

		return theTime;
	}// end of method createTime

}// end of class SystemTime
