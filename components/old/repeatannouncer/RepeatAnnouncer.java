/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Southampton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Southampton
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

Created by: Mark Thompson (University of Southampton)
Contributors:
  Mark Thompson (University of Southampton)

 */
package equip.ect.components.repeatannouncer;

// vim: expandtab sw=4 ts=4 sts=4:

/*
 * Repeat Announcer, $RCSfile: RepeatAnnouncer.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 */

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

/**
 * The <b>RepeatAnnouncer</b> bean publishes a user defined message and an instance counter
 * periodically to the dataspace.
 * <p>
 * This Java Bean is part of a learning exercise for the Curious Home project.
 * <p>
 * The component has the following properties that are exported to Equip:
 * <ul>
 * <li><b>interval</b> - interval between updates, in seconds (int)</li>
 * <li><b>message</b> - message to be emitted (java.lang.String)</li>
 * </ul>
 * 
 * @author Mark Thompson &lt;mkt@ecs.soton.ac.uk&gt;
 */
public class RepeatAnnouncer extends JFrame implements Serializable
{

	// Logging
	private static Logger logger = Logger.getLogger(RepeatAnnouncer.class);

	/**
	 * This component is intended to be used as part of the Equip Component Toolkit.
	 */
	public static void main(final String[] args)
	{
		logger.debug("main method invoked!? " + args);
		final RepeatAnnouncer ra = new RepeatAnnouncer();
	}

	// Business End
	private RepeatAnnouncerLogic ral;

	// GUI components
	private final static String guiTitle = "RepeatAnnouncer";
	private JButton guiUpdateMessageButton;
	private JButton guiUpdateIntervalButton;
	private JTextField guiMessageField;

	private JTextField guiIntervalField;

	/** Property Change Delegate */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	/** Properties */
	private String message = "";

	private int interval = 0;

	/**
	 * Constructor - initialises graphical representation of the component. Also creates timed task
	 * that emits messages to dataspace at regular intervals
	 */
	public RepeatAnnouncer()
	{
		logger.debug("Repeat Announcer constructing GUI");
		setTitle(guiTitle);
		final Container contentPane = this.getContentPane();
		contentPane.setLayout(new GridLayout(0, 3));

		contentPane.add(new JLabel("Message:"));
		guiMessageField = new JTextField(message, 32);
		contentPane.add(guiMessageField);
		contentPane.add(guiUpdateMessageButton = new JButton("Update"));

		contentPane.add(new JLabel("Interval:"));
		guiIntervalField = new JTextField("" + interval, 5);
		contentPane.add(guiIntervalField);
		contentPane.add(guiUpdateIntervalButton = new JButton("Update"));

		guiUpdateMessageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent _)
			{
				logger.debug("Message Button Clicked");
				setMessage(guiMessageField.getText());
			}
		});

		guiUpdateIntervalButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent _)
			{
				logger.debug("Interval Button Clicked");
				// check it's an int, make it 0 otherwise :-)
				int i = 0;
				try
				{
					i = Integer.parseInt(guiIntervalField.getText());
				}
				catch (final NumberFormatException __)
				{
					guiIntervalField.setText("0");
				}

				setInterval(i);
			}
		});

		setSize(new Dimension(300, 100));
		setVisible(true);

		ral = new RepeatAnnouncerLogic(this);
	}

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * Gets current value of bound update interval property
	 * 
	 * @return Current value of update interval property
	 * @see #setInterval
	 */
	public int getInterval()
	{
		logger.debug("getInterval()");
		return interval;
	}

	/**
	 * Gets current value of bound message property
	 * 
	 * @return Current value of message property
	 * @see #setMessage
	 */
	public String getMessage()
	{
		logger.debug("getMessage()");
		return message;
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Sets bound property interval to a new value
	 * 
	 * @param newInterval
	 *            new interval between updates (seconds)
	 * @see #getInterval
	 */
	public void setInterval(final int newInterval)
	{
		logger.debug("setInterval(" + newInterval + ")");
		// prevent any looping and redundant PropertyChangeEvent firing
		if (interval == newInterval) { return; }
		final int oldInterval = this.interval;
		this.interval = newInterval;

		// tell anyone who cares - including our business logic!
		propertyChangeListeners.firePropertyChange("interval", new Integer(oldInterval), new Integer(newInterval));
		// reflect the change in the gui in case the change was equip-sourced
		guiIntervalField.setText(Integer.toString(interval));
	}

	/**
	 * Sets bound property Message to a new value
	 * 
	 * @param newMessage
	 *            new message to emit
	 * @see #getMessage
	 */
	public void setMessage(final String newMessage)
	{
		logger.debug("setMessage(" + newMessage + ")");
		// prevent any looping and redundant PropertyChangeEvent firing
		if (message == newMessage) { return; }
		final String oldMessage = this.message;
		this.message = newMessage;

		// tell anyone who cares - including our business logic!
		propertyChangeListeners.firePropertyChange("message", oldMessage, newMessage);
		// reflect the change in the gui in case the change was equip-sourced
		guiMessageField.setText(message);
	}

}
