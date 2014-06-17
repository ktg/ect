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

package equip.ect.components.reminder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * <b>Reminder</b> Bean outputs a reminder message at the specified date.
 * 
 * 
 * <H3>Description</H3> Bean component for scheduling a 'reminder message' to be fired at a user
 * specified date and time in the future.
 * <p>
 * Supports a GUI, but functions with or without it active.
 * 
 * <H3>Usage</H3>
 * Set the date and time through the properties or gui.<BR>
 * Set the output message to be fired at the specified time.
 * 
 * @classification Experimental/Local GUI
 * 
 * @see ReminderGUI
 * @see ReminderBeanInfo
 * 
 * @author Chris allsopp <cpa02u@cs.nott.ac.uk>
 */
public final class Reminder implements Serializable, Runnable
{

	// >>>>>>>>>>>>>>> Bean Properties <<<<<<<<<<<<<<<<<<< //

	public class FireReminderTask extends TimerTask implements Serializable
	{

		/**
		 * Code executed by the scheduler when the system time matches the reminder time.
		 */
		@Override
		public void run()
		{
			setOutputMessage(getReminderMessage());
			logger.debug("Reminder Triggered ==> " + getOutputMessage());
			if (gui != null)
			{
				gui.createPopupReminder();
			}

		}
	}

	/**
	 * This component is intended to be used as part of the Equip Component Toolkit.
	 */
	public static void main(final String[] args)
	{
		final Reminder rem_logic = new Reminder();
	}

	/** The GUI associated with this particular instance of the Reminder. */
	private ReminderGUI gui;

	// null or

	// the last fired reminder message)

	private String rem_message = null; // Reminder Message

	private String output_message = rem_message; // Output field (will equal

	// >>>>>>>>>>>> End of Bean Properties <<<<<<<<<<<<<< //

	// Simple Datatype Fields which make up values of the reminder_calendar.
	private int rem_seconds, rem_minutes, rem_hours, rem_dayOfMonth, rem_month, rem_year;

	// Simple Datatype Fields which make up values of the system calendar.
	private int sys_seconds, // read only for now
			sys_minutes, // read only for now
			sys_hours, // read only for now
			sys_dayOfMonth, // read only for now
			sys_month, // read only for now
			sys_year; // read only for now

	/** Time period that the system clock properties are updated. */
	public final static int ONE_SECOND = 1000;

	/** Keeps track of the time that the 'reminder' message is triggered. */
	private GregorianCalendar reminder_calendar = new GregorianCalendar(TimeZone.getDefault());

	/** Keeps track of the system time. */
	private GregorianCalendar system_calendar = new GregorianCalendar(TimeZone.getDefault());

	/**
	 * Specifies the format that time and date information is displayed in the diagnostics box,
	 * debug output and the pop-up reminder messages.
	 * <p>
	 * Example output is as follows <code>23:19:58 on Friday, July 16 2004<\code>
	 */
	private final static String timeAndDateFormat = "HH:mm:ss 'on' EEEE, MMMM dd yyyy";

	/**
	 * Used to schedule the time and date when the reminder message should be fired. This class uses
	 * the {@link java.util.Timer} class and not the class of the same name provided in the <javax
	 * swing package.
	 */
	private Timer scheduler = new Timer();

	/** Property Change Delegate. */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * log4j logger, used to print to console (System.out) by default.
	 * 
	 * @see <a href="http://logging.apache.org/log4j/docs/">Log4j Documentation</a>
	 */
	private static Logger logger = Logger.getLogger(Reminder.class);

	static
	{
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}

	/**
	 * Task added to the scheduler. Is re-instantiated on each call of the reschedule() method.
	 */
	private FireReminderTask task = new FireReminderTask();

	private Thread thread;

	/* ------------------------ Getters and Setters ------------------------- */

	/**
	 * Creates the Reminder component using the current time and date settings as the calendar for
	 * the starting reminder and system time property values.
	 */
	public Reminder()
	{

		// Tell the Reminder Tool which GUI it should listen to whilst the GUI
		// is open
		this.gui = new ReminderGUI(this);

		// set system time calendar and properties
		this.system_calendar = new GregorianCalendar(TimeZone.getDefault());
		this.sys_seconds = system_calendar.get(Calendar.SECOND);
		this.sys_minutes = system_calendar.get(Calendar.MINUTE);
		this.sys_hours = system_calendar.get(Calendar.HOUR);
		this.sys_dayOfMonth = system_calendar.get(Calendar.DAY_OF_MONTH);
		this.sys_month = system_calendar.get(Calendar.MONTH);
		this.sys_year = system_calendar.get(Calendar.YEAR);

		// set reminder calendar and reminder properties
		this.reminder_calendar = new GregorianCalendar(TimeZone.getDefault());
		this.rem_seconds = reminder_calendar.get(Calendar.SECOND);
		this.rem_minutes = reminder_calendar.get(Calendar.MINUTE);
		this.rem_hours = reminder_calendar.get(Calendar.HOUR);
		this.rem_dayOfMonth = reminder_calendar.get(Calendar.DAY_OF_MONTH);
		this.rem_month = reminder_calendar.get(Calendar.MONTH);
		this.rem_year = reminder_calendar.get(Calendar.YEAR);

		this.gui.setVisible(true);
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Returns the last message that has been output.
	 * 
	 * @return the last output message.
	 */
	public String getOutputMessage()
	{
		return output_message;
	}

	/**
	 * Returns the reminder date/time information.
	 * 
	 * @return the reminder date/time information as a GregorianCalendar instance.
	 */
	public GregorianCalendar getReminderCalendar()
	{
		return reminder_calendar;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Returns the day of the month that the reminder will be fired
	 * 
	 * @return the day of the month that the reminder will be fired.
	 */
	public int getReminderDayOfMonth()
	{
		return rem_dayOfMonth;
	}

	/**
	 * Returns the <B>hours field</B> of the time the reminder will be fired, <B>not</B> the number
	 * of hours until the reminder will be fired.
	 * 
	 * @return the hours field of the time that the reminder will be fired.
	 */
	public int getReminderHours()
	{
		return rem_hours;
	}

	/**
	 * Returns the current reminder message that has been set.
	 * 
	 * @return the current reminder message that has been set.
	 */
	public String getReminderMessage()
	{
		return rem_message;
	}

	/**
	 * Returns the <B>minutes field</B> of the time the reminder will be fired, <B>not</B> the
	 * number of minutes until the reminder will be fired.
	 * 
	 * @return the minutes field of the time that the reminder will be fired.
	 */
	public int getReminderMinutes()
	{
		return rem_minutes;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Returns the month that the reminder will be fired where 0 is January and 11 is December.
	 * 
	 * @return the month that the reminder will be fired.
	 */
	public int getReminderMonth()
	{
		return rem_month;
	}

	/**
	 * Returns the <B>seconds field</B> of the time the reminder will be fired, <B>not</B> the
	 * number of seconds until the reminder will be fired.
	 * 
	 * @return the seconds field of the time that the reminder will be fired.
	 */
	public int getReminderSeconds()
	{
		return rem_seconds;
	}

	/**
	 * Returns the year that the reminder will be fired.
	 * 
	 * @return the month that the reminder will be fired.
	 */
	public int getReminderYear()
	{
		return rem_year;
	}

	/**
	 * Returns the system date/time information.
	 * 
	 * @return the system date/time information as a GregorianCalendar instance.
	 */
	public GregorianCalendar getSystemCalendar()
	{
		return system_calendar;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Returns the day of the month of the current system time.
	 * 
	 * @return the day of the month of the current system time.
	 */
	public int getSystemDayOfMonth()
	{
		return sys_dayOfMonth;
	}

	/**
	 * Returns the <B>hours field</B> of the current system time, <B>not</B> the system time in
	 * hours alone.
	 * 
	 * @return the hours field of the current system time.
	 */
	public int getSystemHours()
	{
		return sys_hours;
	}

	/**
	 * Returns the <B>minutes field</B> of the current system time, <B>not</B> the system time in
	 * minutes alone.
	 * 
	 * @return the minutes field of the current system time.
	 */
	public int getSystemMinutes()
	{
		return sys_minutes;
	}

	/**
	 * Returns the month of the current system time where 0 is January and 11 is December.
	 * 
	 * @return the month of the current system time.
	 */
	public int getSystemMonth()
	{
		return sys_month;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Returns the <B>seconds field</B> of the current system time, <B>not</B> the system time in
	 * seconds alone.
	 * 
	 * @return the seconds field of the current system time.
	 */
	public int getSystemSeconds()
	{
		return sys_seconds;
	}

	/**
	 * Returns the year of the current system time.
	 * 
	 * @return the year of the current system time.
	 */
	public int getSystemYear()
	{
		return sys_year;
	}

	/**
	 * Checks whether a reminder date is valid given the current system time.
	 * 
	 * @param proposedReminderDate
	 *            The date that is to be validated as a <code> 
	 * 								GregorianCalendar</code> representation.
	 * 
	 * @return <code>True</code> if the <code>proposedReminderDate</code> is valid. <br>
	 *         <code>False</code> if the <code>proposedReminderDate</code> is in the past.
	 */
	public boolean isValidReminderDate(final GregorianCalendar proposedReminderDate)
	{

		// updates System Time with current system time
		updateSystemTime(new GregorianCalendar(TimeZone.getDefault()));
		return (proposedReminderDate.getTime().after(this.getSystemCalendar().getTime()));
	}

	/**
	 * Checks whether a change to a reminder field value will invalidate the reminder date of not.
	 * 
	 * @param calendarFieldChanged
	 *            a <code>Calendar</code> field constant for the field that is required to be
	 *            changed.
	 * 
	 * @param newValue
	 *            The calendar's 'field' new value.
	 * 
	 * @return <code>True</code> if the the field change is valid. <br>
	 *         <code>False</code> if the field change invalidates the reminder time.
	 */
	public boolean isValidReminderDateChange(final int calendarFieldChanged, final int newValue)
	{

		final GregorianCalendar proposedReminderTime = new GregorianCalendar(TimeZone.getDefault());
		proposedReminderTime.setTime(reminder_calendar.getTime());
		proposedReminderTime.set(calendarFieldChanged, newValue);
		return isValidReminderDate(proposedReminderTime);

	}

	// ----------------------------------------------------------------------------//

	/**
	 * Formats the reminder date/time property values of the Reminder into a human readable String.
	 * 
	 * @return String representation of the current reminder date/time as specified by the
	 *         <code>dateAndTimeFormat</code> field.
	 */
	public String reminder_propertyValues()
	{

		final SimpleDateFormat sdf = new SimpleDateFormat(timeAndDateFormat);
		return sdf.format(this.getReminderCalendar().getTime()).toString();
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Thread for updating the following system's time properties every second.
	 * <p>
	 * <ul>
	 * <li>sys_seconds</li>
	 * <li>sys_minutes</li>
	 * <li>sys_hours</li>
	 * <li>sys_dayOfMonth</li>
	 * <li>sys_month</li>
	 * <li>sys_year</li>
	 * <li>system_calendar</li>
	 * </ul>
	 * 
	 * @see #ONE_SECOND
	 */
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				// Only working with second precision so computational
				// time not important
				Thread.sleep(ONE_SECOND);
				// updates System Time with current system time
				updateSystemTime(new GregorianCalendar(TimeZone.getDefault()));
			}
			catch (final InterruptedException ie)
			{
				logger.debug("Stopping Thread " + ie.getMessage());
				return;
			}
		}
	}

	/**
	 * Sets the ReminderToolGUI that this Reminder object is tied to.
	 * <p>
	 * Setting to <code>null</code> tells this Reminder that the GUI has been closed down.
	 * 
	 * @param gui
	 *            The ReminderGUI that this Reminder instance will be tied to.
	 */
	public void setGUI(final ReminderGUI gui)
	{
		this.gui = gui;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the reminder_calendar date values and all associated time and date property values.
	 * <p>
	 * This methods updates any of the following property values that have different values to that
	 * of the new calendar value by calling their corresponding setter methods:
	 * <p>
	 * <ul>
	 * <li>rem_seconds</li>
	 * <li>rem_minutes</li>
	 * <li>rem_hours</li>
	 * <li>rem_dayOfMonth</li>
	 * <li>rem_month</li>
	 * <li>rem_year</li>
	 * </ul>
	 * 
	 * @param cal
	 *            The new date and time values that the reminder calendar should be set to.
	 *            <p>
	 *            Any erroneous calendar fields will be ignored.
	 * 
	 * @see #setReminderSeconds(int)
	 * @see #setReminderMinutes(int)
	 * @see #setReminderHours(int)
	 * @see #setReminderDayOfMonth(int)
	 * @see #setReminderMonth(int)
	 * @see #setReminderYear(int)
	 */
	public void setReminderCalendar(final GregorianCalendar cal)
	{

		int field; // temp store for calendar values (i.e. the method argument)

		// ensures that only the setters are called if the field values have
		// changed

		field = cal.get(Calendar.HOUR_OF_DAY);
		if (field != rem_hours)
		{
			this.setReminderHours(field);
		}

		field = cal.get(Calendar.MINUTE);
		if (field != rem_minutes)
		{
			this.setReminderMinutes(field);
		}

		field = cal.get(Calendar.SECOND);
		if (field != rem_seconds)
		{
			this.setReminderSeconds(field);
		}

		field = cal.get(Calendar.YEAR);
		if (field != rem_year)
		{
			this.setReminderYear(field);
		}

		field = cal.get(Calendar.MONTH);
		if (field != rem_month)
		{
			this.setReminderMonth(field);
		}

		field = cal.get(Calendar.DAY_OF_MONTH);
		if (field != rem_dayOfMonth)
		{
			this.setReminderDayOfMonth(field);
		}

	}

	/**
	 * Sets the day of the month that the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.DAY_OF_MONTH field.
	 * <p>
	 * Reschedules the reminder message to fire at the new date providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's dateSelector value (used to set the reminder date) is
	 * also updated with the new value.
	 * 
	 * @param rem_newDayOfMonth
	 *            The new value for the day of the month field. <br>
	 *            Does not allow integer values outside of 0-31, 0-30, 0-29 (leap year) or 0-28 to
	 *            be entered depending on the month property. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderDayOfMonth(final int rem_newDayOfMonth)
	{

		// logger.debug("Setting Day Of Month");

		int numDaysInMonth;

		switch (rem_month)
		{

			case Calendar.FEBRUARY:
				if (reminder_calendar.isLeapYear(getReminderYear()))
				{
					numDaysInMonth = 29;
				}
				else
				{
					numDaysInMonth = 28;
				}

			case Calendar.JANUARY:
			case Calendar.MARCH:
			case Calendar.MAY:
			case Calendar.JULY:
			case Calendar.AUGUST:
			case Calendar.OCTOBER:
			case Calendar.DECEMBER:
				numDaysInMonth = 31;

			default: // all other months have 30 days
				numDaysInMonth = 30;
		}

		if (rem_newDayOfMonth < 0 || rem_newDayOfMonth > numDaysInMonth) { return; // ignore
		}

		if (!isValidReminderDateChange(Calendar.DAY_OF_MONTH, rem_newDayOfMonth)) { return; }

		propertyChangeListeners.firePropertyChange("rem_dayOfMonth", rem_dayOfMonth, rem_newDayOfMonth);
		rem_dayOfMonth = rem_newDayOfMonth;
		reminder_calendar.set(Calendar.DAY_OF_MONTH, rem_dayOfMonth);
		if (gui != null)
		{
			gui.setDateSelector(reminder_calendar.getTime());
		}
		reschedule();
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the hours field (24hour time) for when the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.HOUR_OF_DAY field.
	 * <p>
	 * Reschedules the reminder message to fire at the new time providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's spinner value (used to set reminder time) is also
	 * updated with the new value.
	 * 
	 * @param rem_newhours
	 *            The new value for the hours field. <br>
	 *            Must be in the range 0-23. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderHours(final int rem_newhours)
	{

		// logger.debug("Setting Reminder Hours");

		if (rem_newhours > 23 || rem_newhours < 0) { return; }

		if (!isValidReminderDateChange(Calendar.HOUR_OF_DAY, rem_newhours)) { return; }

		propertyChangeListeners.firePropertyChange("rem_hours", rem_hours, rem_newhours);
		rem_hours = rem_newhours;
		reminder_calendar.set(Calendar.HOUR_OF_DAY, rem_hours);
		if (gui != null)
		{
			gui.setSpinner(reminder_calendar.getTime());
		}
		reschedule();
	}

	/**
	 * Sets the reminder message string that will be fired.
	 * 
	 * If the GUI is still open then the GUI's msgBox (used to set the reminder message) is set to
	 * the new reminder message value.
	 * 
	 * @param rem_newmsg
	 *            The new text string for the the reminder.
	 */
	public void setReminderMessage(final String rem_newmsg)
	{

		propertyChangeListeners.firePropertyChange("rem_message", rem_message, rem_newmsg);
		rem_message = rem_newmsg;
		if (gui != null)
		{
			gui.setMsgboxText(rem_message);
		}
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the minutes field for when the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.MINUTES field.
	 * <p>
	 * Reschedules the reminder message to fire at the new time providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's spinner value (used to set reminder time) is also
	 * updated with the new value.
	 * 
	 * @param rem_newmins
	 *            The new value for the minutes field. <br>
	 *            Must be in the range 0-59. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderMinutes(final int rem_newmins)
	{

		// logger.debug("Setting Reminder Minutes");

		if (rem_newmins > 59 || rem_newmins < 0) { return; }

		if (!isValidReminderDateChange(Calendar.MINUTE, rem_newmins)) { return; }

		propertyChangeListeners.firePropertyChange("rem_minutes", rem_minutes, rem_newmins);
		rem_minutes = rem_newmins;
		reminder_calendar.set(Calendar.MINUTE, rem_minutes);
		if (gui != null)
		{
			gui.setSpinner(reminder_calendar.getTime());
		}
		reschedule();

	}

	/**
	 * Sets the month that the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.MONTH field.
	 * <p>
	 * Reschedules the reminder message to fire at the new date providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's dateSelector value (used to set reminder time) is
	 * also updated with the new value.
	 * 
	 * @param rem_newmonth
	 *            The new value for the the month field. <br>
	 *            Does not allow integer values outside of 0-11, (Java numbers months from 0
	 *            (January) through to 11 (December)to be entered. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderMonth(final int rem_newmonth)
	{

		// logger.debug("Setting Reminder Month");

		if (rem_newmonth < Calendar.JANUARY || rem_newmonth > Calendar.DECEMBER) { return; }

		if (!isValidReminderDateChange(Calendar.MONTH, rem_newmonth)) { return; }

		propertyChangeListeners.firePropertyChange("rem_month", rem_month, rem_newmonth);
		rem_month = rem_newmonth;
		reminder_calendar.set(Calendar.MONTH, rem_month);
		if (gui != null)
		{
			gui.setDateSelector(reminder_calendar.getTime());
		}
		reschedule();
	}

	/**
	 * Sets the seconds field for when the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.SECONDS field.
	 * <p>
	 * Reschedules the reminder message to fire at the new time providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's spinner value (used to set reminder time) is also
	 * updated with the new value.
	 * 
	 * @param rem_newsecs
	 *            The new value for the seconds field. <br>
	 *            Must be in the range 0-59. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderSeconds(final int rem_newsecs)
	{

		// logger.debug("Setting Reminder Seconds");

		if (rem_newsecs > 59 || rem_newsecs < 0) { return; }

		if (!isValidReminderDateChange(Calendar.SECOND, rem_newsecs)) { return; }

		propertyChangeListeners.firePropertyChange("rem_seconds", rem_seconds, rem_newsecs);
		rem_seconds = rem_newsecs;
		reminder_calendar.set(Calendar.SECOND, rem_seconds);
		if (gui != null)
		{
			gui.setSpinner(reminder_calendar.getTime());
		}
		reschedule();
	}

	/**
	 * Sets the year that the reminder message will be fired.
	 * <p>
	 * Also updates the reminder_calendar.YEAR field.
	 * <p>
	 * Reschedules the reminder message to fire at the new date providing the reminder date/time is
	 * not before the current system date/time.
	 * <p>
	 * If the GUI is still open then the GUI's dateSelector value (used to set reminder time) is
	 * also updated with the new value.
	 * 
	 * @param rem_newyear
	 *            The new value for the the year field. <br>
	 *            The max year a reminder can be set is currently 2037. <br>
	 *            Any erroneous input will be ignored.
	 */
	public void setReminderYear(final int rem_newyear)
	{

		// logger.debug("Setting Reminder Year");

		if (rem_newyear > 2037) { return; }

		if (!isValidReminderDateChange(Calendar.YEAR, rem_newyear)) { return; }

		propertyChangeListeners.firePropertyChange("rem_year", rem_year, rem_newyear);
		rem_year = rem_newyear;
		reminder_calendar.set(Calendar.YEAR, rem_year);
		if (gui != null)
		{
			gui.setDateSelector(reminder_calendar.getTime());
		}
		reschedule();
	}

	// ----------------------------------------------------------------------------//

	/** Interrupts the thread and sets it to null - 'Cleans up' on close. */
	public void stop()
	{
		logger.debug("Cleaning up Reminder Tool and Exiting");
		thread.interrupt();
		thread = null;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Formats the system date/time property values of the Reminder into a human readable String.
	 * 
	 * @return String representation of the current system date/time as specified by the
	 *         <code>dateAndTimeFormat</code> field.
	 */
	public String system_propertyValues()
	{

		final SimpleDateFormat sdf = new SimpleDateFormat(timeAndDateFormat);
		return sdf.format(this.getSystemCalendar().getTime()).toString();
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Reschedules the reminder message to be fired at the current reminder_calendar date and time.
	 */
	private void reschedule()
	{
		logger.debug("Rescheduling Reminder for " + reminder_calendar.getTime());
		scheduler.cancel();
		scheduler = new Timer();
		task = new FireReminderTask();
		scheduler.schedule(task, reminder_calendar.getTime());
	}

	/**
	 * Sets the output message string.
	 * 
	 * @param new_output_message
	 *            The new text string for the the output message property. <br>
	 *            Should be the same text as the most recently fired reminder message.
	 */
	private void setOutputMessage(final String new_output_message)
	{
		propertyChangeListeners.firePropertyChange("output_message", output_message, new_output_message);
		output_message = new_output_message;
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the system time's 'day of the month' field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newDayOfMonth
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemDayOfMonth(final int sys_newDayOfMonth)
	{

		propertyChangeListeners.firePropertyChange("sys_dayOfMonth", sys_dayOfMonth, sys_newDayOfMonth);
		sys_dayOfMonth = sys_newDayOfMonth;
		system_calendar.set(Calendar.DAY_OF_MONTH, sys_dayOfMonth);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}

	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the system time's hours field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newhours
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemHours(final int sys_newhours)
	{

		propertyChangeListeners.firePropertyChange("sys_hours", sys_hours, sys_newhours);
		sys_hours = sys_newhours;
		system_calendar.set(Calendar.HOUR_OF_DAY, sys_hours);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the system time's minutes field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newmins
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemMinutes(final int sys_newmins)
	{

		propertyChangeListeners.firePropertyChange("sys_minutes", sys_minutes, sys_newmins);
		sys_minutes = sys_newmins;
		system_calendar.set(Calendar.MINUTE, sys_minutes);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}
	}

	/**
	 * Sets the system time's month field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newmonth
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemMonth(final int sys_newmonth)
	{

		propertyChangeListeners.firePropertyChange("sys_month", sys_month, sys_newmonth);
		sys_month = sys_newmonth;
		system_calendar.set(Calendar.MONTH, sys_month);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the system time's seconds field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newsecs
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemSeconds(final int sys_newsecs)
	{

		propertyChangeListeners.firePropertyChange("sys_seconds", sys_seconds, sys_newsecs);
		sys_seconds = sys_newsecs;
		system_calendar.set(Calendar.SECOND, sys_seconds);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the system time's year field.
	 * <p>
	 * If the GUI is still open then the GUI's system time label at the bottom of the window is also
	 * updated with the new value.
	 * 
	 * @param sys_newyear
	 *            No error checking of the parameter value will be performed. <br>
	 *            It is assumed that valid values will be passed in by any code which invokes this
	 *            method within the private boundaries of this class.
	 */
	private void setSystemYear(final int sys_newyear)
	{

		propertyChangeListeners.firePropertyChange("sys_year", sys_year, sys_newyear);
		sys_year = sys_newyear;
		system_calendar.set(Calendar.YEAR, sys_year);
		if (gui != null)
		{
			gui.setSystemClockText("System Time: " + system_propertyValues());
		}
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Updates system_calendar and property values with the current system time.
	 * <p>
	 * The following fields are updated on calling this method.
	 * <p>
	 * <ul>
	 * <li>sys_seconds</li>
	 * <li>sys_minutes</li>
	 * <li>sys_hours</li>
	 * <li>sys_dayOfMonth</li>
	 * <li>sys_month</li>
	 * <li>sys_year</li>
	 * <li>system_calendar</li>
	 * </ul>
	 */
	private void updateSystemTime(final GregorianCalendar cal)
	{

		int field; // temp store for calendar values (i.e. the method argument)

		// ensures that only the setters are called if the field values have
		// changed

		field = cal.get(Calendar.HOUR_OF_DAY);
		if (field != sys_hours)
		{
			this.setSystemHours(field);
		}

		field = cal.get(Calendar.MINUTE);
		if (field != sys_minutes)
		{
			this.setSystemMinutes(field);
		}

		field = cal.get(Calendar.SECOND);
		if (field != sys_seconds)
		{
			this.setSystemSeconds(field);
		}

		field = cal.get(Calendar.YEAR);
		if (field != sys_year)
		{
			this.setSystemYear(field);
		}

		field = cal.get(Calendar.MONTH);
		if (field != sys_month)
		{
			this.setSystemMonth(field);
		}

		field = cal.get(Calendar.DAY_OF_MONTH);
		if (field != sys_dayOfMonth)
		{
			this.setSystemDayOfMonth(field);
		}
	}

} // end class
