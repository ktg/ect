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
package equip.ect.components.digital_clock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Displays a 24 hour digital clock
 * 
 * @author Craig Morrall
 * @date 03/06/2004
 */
public class DigitalClock extends JFrame implements Serializable, PropertyChangeListener
{
	/**
	 * main method
	 * 
	 * @param args
	 *            - the command line arguments
	 */
	public static void main(final String args[])
	{
		final DigitalClock digDisplay = new DigitalClock();
	}// end of main method

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private JLabel timeLabel;
	// roperties
	private int seconds;
	private int minutes;

	private int hourInDay;

	/**
	 * constructor method sets up the display
	 */
	public DigitalClock()
	{
		timeLabel = new JLabel("00:00:00");
		this.getContentPane().add(timeLabel);
		setSize(100, 50);
		setTitle("Digital Clock");
		setVisible(true);
		addPropertyChangeListener(this); // add us to property listener
	}// end of constructor method

	// set methods

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public int getHourInDay()
	{
		return hourInDay;
	}// end of method getHours

	public int getMinutes()
	{
		return minutes;
	}// end of methos setMinutes

	// get methods

	public int getSeconds()
	{
		return seconds;
	}// end of method getSeconds

	// signal a property change event
	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setHourInDay(final int hourInDay)
	{
		if (hourInDay != this.hourInDay)
		{
			propertyChangeListeners.firePropertyChange("hourInDay", this.hourInDay, hourInDay);
			setTime(seconds, minutes, hourInDay);
			this.hourInDay = hourInDay;
		}// end of if statement
	}// end of method setHours

	public void setMinutes(final int minutes)
	{
		if (minutes != this.minutes)
		{
			propertyChangeListeners.firePropertyChange("minutes", this.minutes, minutes);
			setTime(seconds, minutes, hourInDay);
			this.minutes = minutes;
		}// end of if statement
	}// end of method setMinutes

	public void setSeconds(final int seconds)
	{
		if (seconds != this.seconds)
		{
			propertyChangeListeners.firePropertyChange("seconds", this.seconds, seconds);
			setTime(seconds, minutes, hourInDay);
			this.seconds = seconds;
		}// end of if statement
	}// end of method setSeconds

	/**
	 * stop/kill
	 */
	public void stop()
	{
		this.dispose();
	}

	/**
	 * method to constuct the Jlabel with the correct time
	 */
	private void setTime(final int seconds, final int minutes, final int hourInDay)
	{
		String time = new String();
		if (hourInDay < 10)
		{
			time = "0";
		}
		time += Integer.toString(hourInDay);
		time += ":";
		if (minutes < 10)
		{
			time += "0";
		}
		time += minutes;
		time += ":";
		if (seconds < 10)
		{
			time += "0";
		}
		time += seconds;
		timeLabel.setText(time);
	}// end of method setTime
}
