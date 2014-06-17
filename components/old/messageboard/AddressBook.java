/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.messageboard;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

public class AddressBook extends Panel.Background implements Serializable, ActionListener
{

	private Font font = new Font("Century Gothic", Font.BOLD, 18);
	private Font large = new Font("Century Gothic", Font.PLAIN, 25);
	private JButton selectIcon = MessageBoardGUI.createButton("/choose.png", this);
	private JButton addEntry = MessageBoardGUI.createButton("/add.png", this);
	private JButton selectEntry = MessageBoardGUI.createButton("/select.png", this);
	private JButton back = MessageBoardGUI.createButton("/back.png", this);
	private AntiAliased.TextField number = new AntiAliased.TextField(font, true);
	private AntiAliased.TextField name = new AntiAliased.TextField(font, true);
	private AntiAliased.TextField icon = new AntiAliased.TextField(font, true);
	private MessageList list = new MessageList();
	private Map addressBook = Collections.synchronizedMap(new HashMap());
	private MessageBoardGUI messageBoardGUI = null;

	public AddressBook(final MessageBoardGUI messageBoardGUI)
	{
		super("/brushed_metal.jpg", new Insets(5, 5, 5, 5));
		this.messageBoardGUI = messageBoardGUI;
		// create renderer for address book
		final MessageListRenderer renderer = new MessageListRenderer(false);
		renderer.setCellHeight(70);
		list.setCellRenderer(renderer);
		list.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(final KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{
					final CellData data = (CellData) list.getSelectedValue();
					if (data != null)
					{
						list.removeMessage(data);
					}
				}
			}
		});
		// add address book list to scrollpane
		final JScrollPane listScroll = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScroll.setBorder(new EtchedBorder());
		// panel for phone number / id entry
		final Panel.Transparent numberPane = new Panel.Transparent();
		numberPane.add(BorderLayout.WEST, new JLabel(MessageBoardGUI.createImageIcon("/number.png")));
		numberPane.add(number);
		// panel for name entry
		final Panel.Transparent namePane = new Panel.Transparent();
		namePane.add(BorderLayout.WEST, new JLabel(MessageBoardGUI.createImageIcon("/name.png")));
		namePane.add(name);
		// panel for associated icon file entry
		final Panel.Transparent iconPane = new Panel.Transparent();
		iconPane.add(BorderLayout.WEST, new JLabel(MessageBoardGUI.createImageIcon("/icon-file.png")));
		iconPane.add(icon);
		iconPane.add(BorderLayout.EAST, selectIcon);
		// create and bottom set of buttons
		final Panel.Transparent buttonPane = new Panel.Transparent(new GridLayout(1, 3, 10, 10));
		buttonPane.add(addEntry);
		buttonPane.add(selectEntry);
		buttonPane.add(back);
		final Panel.Transparent bottomPane = new Panel.Transparent(new Insets(5, 5, 5, 5), new GridLayout(4, 1, 10, 10));
		bottomPane.add(numberPane);
		bottomPane.add(namePane);
		bottomPane.add(iconPane);
		bottomPane.add(buttonPane);
		// place on main content pane
		add(listScroll);
		add(BorderLayout.SOUTH, bottomPane);
	}

	@Override
	public void actionPerformed(final ActionEvent ae)
	{
		final Object src = ae.getSource();
		// add new entry to address book
		if (src == addEntry)
		{
			try
			{
				final String numberStr = number.getText();
				final String nameStr = name.getText();
				final CellData data = new CellData(nameStr + "\n" + numberStr, new URL(icon.getText()), null, nameStr,
						numberStr);
				addressBook.put(numberStr, data);
				list.addMessage(data);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		// select an icon file to associate with entry
		else if (src == selectIcon)
		{
			final JFileChooser chooser = new JFileChooser();
			final int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				final File file = chooser.getSelectedFile();
				if (file != null)
				{
					icon.setText("file:/" + file.getAbsolutePath());
				}
				else
				{
					icon.setText("");
				}
			}
		}
		// select the number from the address book
		else if (src == selectEntry)
		{
			if (messageBoardGUI != null)
			{
				final CellData data = (CellData) list.getSelectedValue();
				if (data != null)
				{
					messageBoardGUI.getRecipient().setText(data.getNumber());
				}
				goBack();
			}
		}
		// return to main gui
		else if (src == back)
		{
			goBack();
		}
	}

	public Map getAddressBook()
	{
		return addressBook;
	}

	public AntiAliased.TextField getIconField()
	{
		return icon;
	}

	// //////////////
	// setters and getters

	public MessageList getList()
	{
		return list;
	}

	public AntiAliased.TextField getNameField()
	{
		return name;
	}

	public AntiAliased.TextField getNumberField()
	{
		return number;
	}

	public void setAddressBook(final Map addressBook)
	{
		this.addressBook = addressBook;
	}

	// list model must contain CellData objects only
	public void updateList(final ListModel model)
	{
		list.setModel(model);
		list.repaint();
	}

	protected void goBack()
	{
		if (messageBoardGUI != null)
		{
			synchronized (messageBoardGUI.lockPanel())
			{
				getParent().remove(this);
				messageBoardGUI.addMainPanel();
			}
		}
	}
}
