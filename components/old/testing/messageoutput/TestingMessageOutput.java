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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)
  Chris Greenhalgh (University of Nottingham)
 */

package equip.ect.components.testing.messageoutput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import equip.data.DictionaryImpl;
import equip.data.StringBox;
import equip.ect.components.testing.common.TestingBaseClass;
import equip.runtime.ValueBase;

/**
 * Component has a read-only gui which displays details of a supplied array of dictionaries
 * 
 * @author Stefan Rennick Egglestone
 */
public class TestingMessageOutput extends TestingBaseClass
{
	private DictionaryImpl[] messages;

	private JTabbedPane tabbedPane;
	private JTextField textField;

	private static String NO_MESSAGE = "No messages currently being displayed";

	public TestingMessageOutput()
	{
		super();

		getContentPane().setLayout(new BorderLayout());

		textField = new JTextField(NO_MESSAGE);

		textField.setEnabled(false);
		textField.setDisabledTextColor(Color.BLACK);

		tabbedPane = new JTabbedPane();

		getContentPane().add(textField, BorderLayout.NORTH);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		setSize(new Dimension(400, 300));

		this.setVisible(true);
	}

	public synchronized DictionaryImpl[] getMessages()
	{
		return messages;
	}

	public synchronized void setMessages(final DictionaryImpl[] messages)
	{
		final DictionaryImpl[] oldMessages = this.messages;
		this.messages = messages;

		propertyChangeListeners.firePropertyChange("messages", oldMessages, this.messages);

		if (messages == null)
		{
			runSwing(new Runnable()
			{
				@Override
				public void run()
				{
					if (!stopped)
					{
						textField.setText(NO_MESSAGE);
						tabbedPane.removeAll();
						return;
					}
				}
			});
		}
		else
		{
			runSwing(new Runnable()
			{
				@Override
				public void run()
				{
					if (!stopped)
					{
						tabbedPane.removeAll();

						textField.setText(messages.length + " message have been received");

						for (int i = 0; i < messages.length; i++)
						{
							final String title = "Message " + i;

							final JPanel messagePanel = new JPanel();
							messagePanel.setLayout(new BorderLayout());

							final JTabbedPane fieldPane = new JTabbedPane();

							messagePanel.add(fieldPane, BorderLayout.CENTER);

							final DictionaryImpl di = messages[i];

							if (di != null)
							{
								final Hashtable hash = di.getHashtable();

								if (hash.containsKey("_largeDisplayKeys"))
								{
									// if this key exists, then dictionary
									// has defined the ordering in which
									// it would like its keys displayed

									final StringBox keysBox = (StringBox) (hash.get("_largeDisplayKeys"));
									final String keys = keysBox.value;

									final String[] bits = keys.split(",");

									if (bits.length != 0)
									{
										for (final String bit : bits)
										{
											addTabToPane(bit, hash, fieldPane);
										}
									}
								}
								else
								{
									// if no ordering defined, then display
									// in any order

									final Enumeration keys = hash.keys();

									while (keys.hasMoreElements())
									{
										// for each dictionary entry, create a tab in a
										// pane which lists the fields in the dictionary entry

										final String key = (String) (keys.nextElement());

										addTabToPane(key, hash, fieldPane);
									}
								}

								tabbedPane.add(title, fieldPane);

								validate();
							}
						}
					}
				}
			});
		}
	}

	void addTabToPane(final String key, final Hashtable hash, final JTabbedPane tbp)
	{
		final ValueBase vb = (ValueBase) (hash.get(key));

		if (vb != null)
		{
			if (vb instanceof StringBox)
			{
				final JPanel fieldPanel = new JPanel();
				fieldPanel.setLayout(new BorderLayout());

				final JTextArea fieldArea = new JTextArea(((StringBox) vb).value);
				fieldArea.setEnabled(false);
				fieldArea.setDisabledTextColor(Color.BLACK);

				fieldPanel.add(fieldArea, BorderLayout.CENTER);

				tbp.add(key, new JScrollPane(fieldPanel));
			}
			else
			{
				// do nothing
			}
		}
	}
}
