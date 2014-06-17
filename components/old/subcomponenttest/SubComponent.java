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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.subcomponenttest;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SubComponent extends JFrame implements Serializable
{

	public static void main(final String[] args)
	{
		new SubComponent(null, 1);
	}

	private String title = "SubComponent";
	private JTextArea textArea = new JTextArea();
	private Dimension defaultSize = new Dimension(300, 100);
	private JButton update = new JButton("update");

	private ComponentFactory parent;

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Property
	private String text = null;

	/**
	 * persistent child property value - must be String
	 */
	protected String persistentChild;

	public SubComponent(final ComponentFactory parent, final int id)
	{
		this.parent = parent;
		// persistent child ID - unique for children of our parent
		persistentChild = new Integer(id).toString();
		setTitle(title + " " + id);
		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		contentPane.add(update, BorderLayout.EAST);
		update.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setText(textArea.getText());
			}
		});
		setSize(defaultSize);
		setVisible(true);
	}

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * persistentChild property for persistence of sub-components (must be String type)
	 */
	public String getPersistentChild()
	{
		return persistentChild;
	}

	public String getText()
	{
		return text;
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setText(final String newtext)
	{
		final String oldtext = text;
		if (oldtext == newtext) { return; // prevent any looping
		}
		this.text = newtext;
		propertyChangeListeners.firePropertyChange("text", oldtext, newtext);
		textArea.setText(text);
	}

	public void stop()
	{
		dispose();
		parent = null;
	}

}
