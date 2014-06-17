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
package equip.ect.components.send_form;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * send form component displays a form for sending messages
 * 
 * @author Craig Morrall
 * @date 03/06/2004
 */
public class SendForm extends JFrame implements Serializable, PropertyChangeListener, ActionListener
{
	/**
	 * main method
	 */
	public static void main(final String args[])
	{
		final SendForm sendForm = new SendForm();
	}// end of main method

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// form components
	private JTextField senderTextField, recieverTextField, subjectTextField, mailServerTextField;
	private JTextArea messageTextArea;

	private JButton send;

	// properties
	private String sender, reciever, subject, message, mailServer;

	public SendForm()
	{
		super();
		setTitle("Message Sender");
		setSize(400, 300);

		// create top form components
		senderTextField = new JTextField();
		recieverTextField = new JTextField();
		subjectTextField = new JTextField();
		mailServerTextField = new JTextField();

		// create top panel
		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(4, 2));
		topPanel.add(new JLabel("To: "));
		topPanel.add(recieverTextField);
		topPanel.add(new JLabel("From: "));
		topPanel.add(senderTextField);
		topPanel.add(new JLabel("Subject: "));
		topPanel.add(subjectTextField);
		topPanel.add(new JLabel("Mail Server: "));
		topPanel.add(mailServerTextField);

		// create middle form components
		messageTextArea = new JTextArea(1, 1);
		messageTextArea.setLineWrap(true);
		final JScrollPane messageScrollPane = new JScrollPane(messageTextArea);

		// create middle panel
		final JPanel middlePanel = new JPanel(new GridLayout(2, 1));
		middlePanel.add(new JLabel("Message: "));
		middlePanel.add(messageScrollPane);

		// create bottom form components
		send = new JButton("Send");
		send.addActionListener(this);

		// create bottom panel
		final JPanel bottomPanel = new JPanel();
		bottomPanel.add(send);

		// crate panel to house all other panels
		final JPanel mainPanel = new JPanel(new GridLayout(3, 1));
		mainPanel.add(topPanel);
		mainPanel.add(middlePanel);
		mainPanel.add(bottomPanel);

		getContentPane().add(mainPanel);
		setVisible(true);
		addPropertyChangeListener(this); // add us to property listener
	}// end of method SendForm

	/**
	 * method to handle button presses
	 */
	@Override
	public void actionPerformed(final ActionEvent event)
	{
		if (event.getSource() == send)
		{
			final String getSenderStr = senderTextField.getText().trim();
			final String getRecieverStr = recieverTextField.getText().trim();
			final String getSubjectStr = subjectTextField.getText().trim();
			final String getMailServerStr = mailServerTextField.getText().trim();
			final String getMessageStr = messageTextArea.getText().trim();
			// fire property change events for fields that have changed
			if (sender != getSenderStr)
			{
				propertyChangeListeners.firePropertyChange("sender", sender, getSenderStr);
				sender = getSenderStr;
			}// end of if statement
			if (reciever != getRecieverStr)
			{
				propertyChangeListeners.firePropertyChange("reciever", reciever, getRecieverStr);
				reciever = getRecieverStr;
			}// end of if statement
			if (subject != getSubjectStr)
			{
				propertyChangeListeners.firePropertyChange("subject", subject, getSubjectStr);
				subject = getSubjectStr;
			}// end of if statement
			if (mailServer != getMailServerStr)
			{
				propertyChangeListeners.firePropertyChange("mailServer", mailServer, getMailServerStr);
				mailServer = getMailServerStr;
			}// end of if statement

			propertyChangeListeners.firePropertyChange("message", null, getMessageStr);
			message = getMessageStr;
		}// end of if statement
	}// end of actionPerformed method

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String getMailServer()
	{
		return mailServer;
	}// end of method getMailServer

	public String getMessage()
	{
		return message;
	}// end of method getMessage

	public String getReciever()
	{
		return reciever;
	}// end of method getReciever

	// set methods

	public String getSender()
	{
		return sender;
	}// end of method getSender

	public String getSubject()
	{
		return subject;
	}// end of method getSubject

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

	public void setMailServer(final String mailServer)
	{
		this.mailServer = mailServer;
	}// end of method setMailServer

	// get methods

	public void setMessage(final String message)
	{
		this.message = message;
	}// end of method setMessage

	public void setReciever(final String reciever)
	{
		this.reciever = reciever;
	}// end of method setReciever

	public void setSender(final String sender)
	{
		this.sender = sender;
	}// end of method setSender

	public void setSubject(final String subject)
	{
		this.subject = subject;
	}// end of method setSubject

	/**
	 * stop/kill
	 */
	public void stop()
	{
		this.dispose();
	}// end of method stop

}// end of class SendForm
