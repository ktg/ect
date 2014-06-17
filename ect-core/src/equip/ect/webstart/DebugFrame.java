/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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
 Chris Greenhalgh (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.webstart;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A simple text window to show console/debug output from {@link equip.ect.webstart.Boot}, {@link Installation}s,
 * {@link java.awt.Container}s, etc.
 */
public class DebugFrame extends JFrame
{

	private final JTextComponent textArea;

	protected int bufferSize = 1000;

	private boolean useBufferConstraints = false;

	public final static int LARGE_BUFFER_SIZE = 100000;

	public DebugFrame(final String title, final int width, final int height)
	{
		final Container content = getContentPane();
		setTitle(title);
		setSize(width, height);
		// this.setBufferSize(1000);
		textArea = new JTextArea();
		textArea.setEditable(false);
		((JTextArea) textArea).setLineWrap(true);
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.CENTER, new JScrollPane(textArea));
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		final JTextField searchField = new JTextField();
		// searchField.setPreferredSize(new Dimension(100, 20));
		searchField.setMaximumSize(new Dimension(100, 20));
		final ActionListener searchAction = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final String regexString = searchField.getText();
				final String text = textArea.getText();
				final int found = text.indexOf(regexString);
				if (found >= 0)
				{
					// textArea.setSelectedTextColor(Color.red);
					// textArea.select(found, found + regexString.length());
					// textArea.selectAll();
					try
					{
						final Rectangle viewR = textArea.modelToView(found);
						textArea.scrollRectToVisible(viewR);
					}
					catch (final BadLocationException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		};
		searchField.addActionListener(searchAction);
		final JButton searchButton = new JButton();
		// searchButton.setIcon(MediaFactory.getImageIcon(EditorResources.SEARCH_ICON,
		// this));
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setMaximumSize(new Dimension(20, 20));
		searchButton.setEnabled(false);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			@Override
			public void insertUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			@Override
			public void removeUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			void cancelHighlight()
			{

			}

			private void checkStatus()
			{
				// Always remove highlight information as it is now
				// no longer in synch with the regex in the text
				// field
				cancelHighlight();
				// Check whether the search button should be enabled
				if (!searchField.getText().equals(""))
				{
					searchButton.setEnabled(true);
				}
				else
				{
					searchButton.setEnabled(false);
				}
			}

		});

		final JLabel searchLabel = new JLabel();
		searchLabel.setText(" Search ");
		searchLabel.setLabelFor(searchField);

		searchButton.addActionListener(searchAction);

		final JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.add(searchLabel);
		toolbar.add(searchField);
		// toolbar.add(searchButton);

		content.add(BorderLayout.NORTH, toolbar);

	}

	public void enableBufferConstraint(final boolean enableBufferC)
	{
		this.useBufferConstraints = enableBufferC;
	}

	public JTextComponent getTextArea()
	{
		return textArea;
	}

	public void out(final String str)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{

				final Document doc = textArea.getDocument();
				((JTextArea) textArea).append(str);

				if (useBufferConstraints)
				{

					final int size = doc.getLength();

					if (size > bufferSize)
					{
						try
						{
							// System.out.println("remove");
							doc.remove(0, size - bufferSize);
						}
						catch (final BadLocationException e)
						{
							// should never happen
							e.printStackTrace();
						}
					}

				}

				textArea.setCaretPosition(doc.getLength());
			}
		});
	}

	public void processInputStream(final InputStream is) throws IOException
	{
		if (is != null)
		{
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						final BufferedReader br = new BufferedReader(new InputStreamReader(is));
						String line = null;
						while (true)
						{
							line = br.readLine();
							if (line != null)
							{
								out(line + "\r\n");
							}
							try
							{
								Thread.sleep(10);
							}
							catch (final Exception e)
							{
							}
						}
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public void setBufferSize(final int size)
	{

		System.out.println("set buffer size " + size);
		this.bufferSize = size;
		enableBufferConstraint(true);
	}
}
