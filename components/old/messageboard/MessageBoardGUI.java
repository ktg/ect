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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

public class MessageBoardGUI implements ActionListener, Serializable
{

	static
	{
		setLookAndFeel();
	}

	public static JButton createButton(final String imgPath, final ActionListener listener)
	{
		final JButton button = new JButton(createImageIcon(imgPath));
		final String vals[] = imgPath.split(".png");
		if (vals != null && vals.length > 0)
		{
			button.setPressedIcon(createImageIcon(vals[0] + "_select.png"));
		}
		button.setFocusPainted(false);
		button.setContentAreaFilled(false);
		button.setOpaque(false);
		button.setBorder(null);
		button.addActionListener(listener);
		return button;
	}

	public static ImageIcon createImageIcon(final String path)
	{
		final URL image = MessageBoardGUI.class.getResource(path);
		if (image != null)
		{
			return new ImageIcon(image);
		}
		else
		{
			return new ImageIcon();
		}
	}

	public static void setLookAndFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final Exception e)
		{
			System.out.println("unable to set look and feel\n" + e);
		}
	}

	// fonts
	private Font font = new Font("Century Gothic", Font.BOLD, 18);
	private Font large = new Font("Century Gothic", Font.PLAIN, 25);
	// buttons
	private JButton note = createButton("/note.png", this);
	private JButton cancel = createButton("/cancel.png", this);
	private JButton send = createButton("/send.png", this);
	private JButton back = createButton("/back.png", this);
	private JButton reply = createButton("/reply.png", this);
	private JButton adrBook = createButton("/addressbook.png", this);
	// text entry
	private AntiAliased.TextField recipient = new AntiAliased.TextField(font, true);
	private AntiAliased.TextPane message = new AntiAliased.TextPane(font, true);
	private String alertURL = "file:/" + extractResource("alert.png", "/alert.png").getAbsolutePath();
	// date/time and counter display
	private final AntiAliased.Label date = new AntiAliased.Label(large, "", JLabel.LEFT);
	private int msgCounter = 0;
	private int unreadCounter = 0;
	private AntiAliased.Label counter = new AntiAliased.Label(large, "", JLabel.RIGHT);
	private Object counterLock = new Object();
	// incoming message list
	private MessageList list = new MessageList();
	private Object listLock = new Object();
	private JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	// panels to view individual messages and address book
	private MessagePanel msgPanel = new MessagePanel(reply, back);
	private AddressBook addressBookPanel = new AddressBook(this);
	// main window/panel
	private Panel.Background background = new Panel.Background("/board.jpg", new Insets(30, 45, 50, 45));
	private JFrame owner = new JFrame();
	private JWindow window = new JWindow(owner);

	private Object panelLock = new Object();

	private boolean running = true;

	// reference to message board component
	private MessageBoard messageBoard = null;

	public MessageBoardGUI(final MessageBoard messageBoard)
	{
		this(messageBoard, null);
	}

	public MessageBoardGUI(final MessageBoard messageBoard, final PersistedState state)
	{
		this.messageBoard = messageBoard;
		reload(state);
		split.setDividerLocation(1000);
		// window settings
		background.setLayout(new BorderLayout(5, 5));
		window.setContentPane(background);
		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		window.setSize(size.width, size.height);
		// add list to scrollpane
		final JScrollPane listScroll = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		listScroll.setBorder(new EtchedBorder());
		// capture double clicks on list and display message content
		addListListeners();
		// message entry pane
		final Panel.Background entryPane = new Panel.Background("/brushed_metal.jpg", new Insets(5, 5, 5, 5));
		entryPane.setLayout(new BorderLayout(5, 5));
		final Panel.Transparent recipientPane = new Panel.Transparent(new BorderLayout(5, 5));
		final JLabel recipientLabel = new JLabel(MessageBoardGUI.createImageIcon("/recipient.png"));
		recipientPane.add(BorderLayout.WEST, recipientLabel);
		recipientPane.add(BorderLayout.CENTER, recipient);
		recipientPane.add(BorderLayout.EAST, adrBook);
		entryPane.add(BorderLayout.NORTH, recipientPane);
		final JScrollPane txtScroll = new JScrollPane(message);
		txtScroll.setBorder(new EtchedBorder());
		final Panel.Transparent msgBodyPane = new Panel.Transparent(new BorderLayout(5, 5));
		final Panel.Transparent buttonPane = new Panel.Transparent(new GridLayout(3, 1));
		buttonPane.add(send);
		buttonPane.add(cancel);
		buttonPane.add(note);
		msgBodyPane.add(BorderLayout.CENTER, txtScroll);
		msgBodyPane.add(BorderLayout.EAST, buttonPane);
		entryPane.add(BorderLayout.CENTER, msgBodyPane);
		// add to split pane then main content pane
		split.add(listScroll);
		split.add(entryPane);
		background.add(split);
		// now top details pane
		final Panel.Transparent detailsPane = new Panel.Transparent(new GridLayout(1, 2));
		detailsPane.add(BorderLayout.NORTH, date);
		detailsPane.add(BorderLayout.NORTH, counter);
		background.add(BorderLayout.NORTH, detailsPane);
		startDateTimer();
	}

	@Override
	public void actionPerformed(final ActionEvent ae)
	{
		final Object src = ae.getSource();
		if (src == cancel)
		{
			clearMessage();
		}
		else if (src == send)
		{
			if (messageBoard != null)
			{
				messageBoard.setDataOut(recipient.getText().trim(), message.getText().trim(), null);
			}
			clearMessage();
		}
		else if (src == note)
		{
			add(message.getText(), alertURL, null, "NOTE", "", 0);
			clearMessage();
		}
		else if (src == back)
		{
			read();
			displayListPane();
		}
		else if (src == reply)
		{
			final CellData data = read();
			if (data != null)
			{
				recipient.setText(data.getNumber());
				message.setText("");
				if (data.getReplies() != -1)
				{
					data.setReplies(data.getReplies() + 1);
				}
			}
			displayListPane();
		}
		else if (src == adrBook)
		{
			displayAddressBook();
		}
	}

	public void add(final String key, final String message, final String attachment)
	{
		CellData data = (CellData) addressBookPanel.getAddressBook().get(key);
		if (data == null)
		{
			data = new CellData(message, null, null, key, key);
		}
		else
		{
			data = new CellData(message, data.getUserIcon(), null, data.getName(), data.getNumber());
		}
		if (attachment != null)
		{
			try
			{
				data.setAttachment(new URL(attachment));
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		incrementAllCounters();
		synchronized (listLock)
		{
			list.addMessage(data);
		}
	}

	public void add(final String message, final String icon, final String attachment, final String from,
			final String number, final int replies)
	{
		incrementAllCounters();
		synchronized (listLock)
		{
			list.addMessage(message, icon, attachment, from, number, replies);
		}
	}

	public void clearMessage()
	{
		recipient.setText("");
		message.setText("");
	}

	public void decrementAllCounters()
	{
		synchronized (counterLock)
		{
			msgCounter--;
			unreadCounter--;
			updateCounter();
		}
	}

	public void decrementMessageCounter()
	{
		synchronized (counterLock)
		{
			msgCounter--;
			updateCounter();
		}
	}

	public void decrementUnreadCounter()
	{
		synchronized (counterLock)
		{
			unreadCounter--;
			updateCounter();
		}
	}

	public void displayAddressBook()
	{
		synchronized (panelLock)
		{
			background.remove(split);
			background.add(addressBookPanel);
			window.repaint();
		}
	}

	public void displayListPane()
	{
		synchronized (panelLock)
		{
			counter.setText(msgCounter + " Messages / " + unreadCounter + " Unread");
			background.remove(msgPanel);
			background.add(split);
			window.repaint();
		}
	}

	public void displayMessagePane()
	{
		synchronized (panelLock)
		{
			background.remove(split);
			background.add(msgPanel);
			window.repaint();
		}
	}

	public synchronized void dispose()
	{
		owner.dispose();
		window.dispose();
		running = false;
	}

	public File extractResource(final String name, final String path)
	{
		final File file = new File(name);
		if (file.exists()) { return file; }
		final InputStream is = MessageBoardGUI.class.getResourceAsStream(path);
		if (is != null)
		{
			try
			{
				final FileOutputStream fos = new FileOutputStream(file);
				int actual = 0;
				final byte buffer[] = new byte[8192];
				while ((actual = is.read(buffer)) > -1)
				{
					fos.write(buffer, 0, actual);
				}
				fos.flush();
				fos.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		return file;
	}

	public AntiAliased.TextField getRecipient()
	{
		return recipient;
	}

	public void incrementAllCounters()
	{
		synchronized (counterLock)
		{
			msgCounter++;
			unreadCounter++;
			updateCounter();
		}
	}

	public void incrementMessageCounter()
	{
		synchronized (counterLock)
		{
			msgCounter++;
			updateCounter();
		}
	}

	public void incrementUnreadCounter()
	{
		synchronized (counterLock)
		{
			unreadCounter++;
			updateCounter();
		}
	}

	public PersistedState persistState()
	{
		synchronized (listLock)
		{
			return new PersistedState(addressBookPanel.getAddressBook(), addressBookPanel.getList().getModel(),
					list.getModel(), new Integer(unreadCounter), new Integer(msgCounter));
		}
	}

	public void setVisible(final boolean visible)
	{
		owner.setVisible(visible);
		window.setVisible(visible);
	}

	public void startDateTimer()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				while (running)
				{
					date.setText(new Date().toString());
					try
					{
						Thread.sleep(1);
					}
					catch (final Exception e)
					{
					}
				}
			}
		}.start();
	}

	public void updateCounter()
	{
		counter.setText(msgCounter + " Messages / " + unreadCounter + " Unread");
	}

	protected void addListListeners()
	{
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					final int index = list.locationToIndex(e.getPoint());
					final CellData data = list.get(index);
					if (data != null)
					{
						synchronized (panelLock)
						{
							msgPanel.displayMessage(data);
							displayMessagePane();
						}
					}

				}
			}
		});
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
						if (!data.hasRead())
						{
							decrementAllCounters();
						}
						else
						{
							decrementMessageCounter();
						}
					}
				}
			}
		});
	}

	protected void addMainPanel()
	{
		background.add(split);
		background.updateUI();
	}

	protected Object lockPanel()
	{
		return panelLock;
	}

	protected CellData read()
	{
		synchronized (listLock)
		{
			final CellData data = (CellData) list.getSelectedValue();
			if (data != null)
			{
				if (!data.hasRead())
				{
					decrementUnreadCounter();
				}
				data.setRead(true);
			}
			return data;
		}
	}

	protected void reload(final PersistedState state)
	{
		if (state == null) { return; }
		if (state.addressBookMap != null)
		{
			addressBookPanel.setAddressBook(state.addressBookMap);
		}
		if (state.addressBookList != null)
		{
			addressBookPanel.getList().setModel(state.addressBookList);
		}
		if (state.messageList != null)
		{
			list.setModel(state.messageList);
		}
		if (state.unreadCount != null)
		{
			unreadCounter = state.unreadCount.intValue();
		}
		if (state.messageCount != null)
		{
			msgCounter = state.messageCount.intValue();
		}
		updateCounter();
	}

}
