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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Tom Rodden (University of Nottingham)
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)

 */
package equip.ect.components.chat;

/*
 * Created on 19-Jan-2004
 *
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */

/**
 * @author imt, jch
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import equip.ect.ContainerManager;
import equip.ect.Persistable;
import equip.ect.PersistenceManager;

public class Chat extends JFrame implements Serializable, PropertyChangeListener, Persistable
{

	/**
	 * @param inText
	 */
	private class messageActionListener implements ActionListener
	{

		@Override
		public void actionPerformed(final ActionEvent e)
		{
			setOutText("<" + name + "> " + message.getText());
			message.setText("");
		}

	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{

		final Chat frame = new Chat();

	}

	// GUI
	JPanel panel;
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane1 = new JScrollPane();

	JTextField message = new JTextField();

	JTextArea history = new JTextArea();
	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// Properties
	private String inText = null;

	private String outText = null;
	private String name = "unknown";

	private File persistFile = null;

	/**
     *
     *
     */
	public Chat()
	{
		try
		{
			makeGUI();
			this.setTitle("Chat");
			this.addPropertyChangeListener(this);

			this.pack();
			this.setSize(200, 300);
			this.setVisible(true);
			// this.setInText("in-empty");
			// this.setOutText("out-empty");

		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}

	}

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * @return
	 */
	public String getInText()
	{
		return inText;
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public String getOutText()
	{
		return outText;
	}

	@Override
	public synchronized void load(final File persistFile, final ContainerManager containerManager) throws IOException
	{

		if (persistFile != null)
		{
			final FileInputStream fis = new FileInputStream(persistFile);
			final byte[] buffer = new byte[8192];
			int actual = 0;
			while ((actual = fis.read(buffer)) != -1)
			{
				final int count = actual;
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						history.append(new String(buffer, 0, count));
						history.setCaretPosition(history.getDocument().getLength());
					}
				});
			}
			fis.close();
			this.persistFile = persistFile;
		}
	}

	@Override
	public synchronized File persist(final ContainerManager containerManager) throws IOException
	{
		if (persistFile == null)
		{
			persistFile = File
					.createTempFile("Chat", ".txt",
									PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY);
		}
		final FileOutputStream fos = new FileOutputStream(persistFile);
		try
		{
			final String txt = history.getText();
			fos.write(txt.getBytes());
			fos.flush();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		fos.close();
		return persistFile;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{

		if (e.getPropertyName().equalsIgnoreCase("outText"))
		{
			history.append((String) e.getNewValue());
			history.append("\n");
		}
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * @param inText
	 */
	public void setInText(final String inText)
	{

		final String oldInText = this.inText;
		this.inText = inText;
		history.append(inText);
		history.append("\n");

		propertyChangeListeners.firePropertyChange("inText", oldInText, inText);

	}

	@Override
	public void setName(final String name)
	{
		final String oldName = this.name;
		this.name = new String(name);

		propertyChangeListeners.firePropertyChange("name", oldName, name);
	}

	/**
     *
     *
     */
	public void setOutText(final String outText)
	{
		final String oldText = this.outText;
		this.outText = new String(outText);

		propertyChangeListeners.firePropertyChange("outText", oldText, outText);
	}

	// // implementation of Persistable interface

	/**
	 * stop/kill
	 */
	public void stop()
	{
		this.dispose();
	}

	/**
     *
     *
     */
	private void makeGUI()
	{
		panel = new JPanel();

		panel.setLayout(borderLayout1);
		getContentPane().add(panel);

		final JPanel nameP = new JPanel(new BorderLayout());
		nameP.add(BorderLayout.WEST, new JLabel("Name:"));
		final JTextField nameTF = new JTextField();
		nameP.add(BorderLayout.CENTER, nameTF);
		nameTF.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setName(nameTF.getText());
			}
		});
		panel.add(BorderLayout.NORTH, nameP);
		message.addActionListener(new messageActionListener());
		panel.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.setMinimumSize(new Dimension(300, 300));

		history.setEditable(false);

		jScrollPane1.getViewport().add(history, null);
		panel.add(message, BorderLayout.SOUTH);
		panel.setSize(200, 300);
	}
}
