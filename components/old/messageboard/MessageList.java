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

import java.awt.Color;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class MessageList extends JList implements Serializable
{

	private DefaultListModel model = new DefaultListModel();
	private MessageListRenderer renderer = new MessageListRenderer(true);
	private Object listLock = new Object();

	public MessageList()
	{
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setForeground(Color.darkGray);
		setSelectionBackground(new Color(35, 28, 53));
		setSelectionForeground(Color.white);
		setCellRenderer(renderer);
		setAutoscrolls(true);
		setBorder(null);
	}

	public void addMessage(final CellData data)
	{
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (listLock)
					{
						model.addElement(data);
						setSelectedValue(data, true);
						repaint();
					}
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public void addMessage(final String message, final String icon, final String attachment, final String from,
			final String number, final int replies)
	{
		try
		{
			final URL iconURL = new URL(icon);
			URL attachmentURL = null;
			if (attachment != null)
			{
				attachmentURL = new URL(attachment);
			}
			final CellData data = new CellData(message, iconURL, attachmentURL, from, number);
			data.setReplies(replies);
			addMessage(data);
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}

	}

	public CellData get(final int index)
	{
		if (index > -1 && index < model.getSize()) { return (CellData) model.get(index); }
		return null;
	}

	public DefaultListModel getDefaultModel()
	{
		return model;
	}

	public int getLength()
	{
		return model.getSize();
	}

	public void removeIndex(final int index)
	{
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (listLock)
					{
						model.remove(index);
						repaint();
					}
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public void removeMessage(final CellData data)
	{
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (listLock)
					{
						try
						{
							model.removeElement(data);
							repaint();
						}
						catch (final Exception e)
						{
							System.out.println("Failed to remove element " + "from message list");
						}
					}
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setModel(final ListModel model)
	{
		if (model != null && model instanceof DefaultListModel)
		{
			this.model = (DefaultListModel) model;
			super.setModel(model);
		}
	}
}
