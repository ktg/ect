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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class MessageListRenderer extends Panel.Transparent implements ListCellRenderer, Serializable
{

	// default alternating background colors for cells
	private Color defaultColors[] = { new Color(217, 207, 245), new Color(173, 190, 195), new Color(204, 212, 189),
										new Color(220, 195, 171) };
	// cache thumbnails
	private Map cachedThumbnails = Collections.synchronizedMap(new HashMap());
	// cell dimensions
	private int cellHeight = 125;
	private int border = 5;
	// fonts
	private Font messageFont = new Font("Century Gothic", Font.PLAIN, 22);
	private Font detailsFont = new Font("Century Gothic", Font.BOLD, 16);
	// icons
	private JLabel reply = new JLabel(MessageBoardGUI.createImageIcon("/reply-icon.png"));
	private JLabel read = new JLabel(MessageBoardGUI.createImageIcon("/read.png"));
	private JLabel unread = new JLabel(MessageBoardGUI.createImageIcon("/unread.png"));
	// whether list includes detail view
	private boolean includeDetails = true;

	public MessageListRenderer(final boolean includeDetails)
	{
		super(new BorderLayout(10, 10));
		this.includeDetails = includeDetails;
		setOpaque(true);
		setInsets(new Insets(border, border, border, border));
		setPreferredSize(new Dimension(0, cellHeight));
	}

	public JPanel createDetailsPane(final JLabel image, final CellData data, final Color foreground)
	{
		final Panel.Transparent main = new Panel.Transparent(new BorderLayout(border, border));
		final GridLayout layout = new GridLayout(3, 1);
		final Panel.Transparent text = new Panel.Transparent(layout);
		AntiAliased.Label label = null;
		text.add(label = new AntiAliased.Label(detailsFont, data.getDisplayString(), SwingConstants.RIGHT));
		label.setForeground(foreground);
		text.add(label = new AntiAliased.Label(detailsFont, data.getDateAsString(), SwingConstants.RIGHT));
		label.setForeground(foreground);
		text.add(label = new AntiAliased.Label(detailsFont, data.getTimeAsString(), SwingConstants.RIGHT));
		label.setForeground(foreground);
		final Panel.Transparent icons = new Panel.Transparent(new GridLayout(2, 1));
		icons.add(data.hasRead() ? read : unread);

		final int replies = data.getReplies();
		if (replies > -1)
		{
			layout.setRows(layout.getRows() + 1);
			text.add(label = new AntiAliased.Label(detailsFont, replies + (replies == 1 ? " reply" : " replies"),
					SwingConstants.RIGHT));
			label.setForeground(foreground);
			if (replies > 0)
			{
				icons.add(reply);
			}
		}
		main.add(BorderLayout.CENTER, text);
		if (image != null)
		{
			main.add(BorderLayout.WEST, image);
		}
		main.add(BorderLayout.EAST, icons);
		return main;
	}

	public int getCellHeight()
	{
		return cellHeight;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus)
	{
		removeAll();
		final int iconHeight = cellHeight - (border * 2);
		Color foreground = null, background = null;
		// set foreground/background color of cell
		if (isSelected)
		{
			setBackground(background = list.getSelectionBackground());
			setForeground(foreground = list.getSelectionForeground());
		}
		else
		{
			setBackground(background = defaultColors[index % defaultColors.length]);
			setForeground(foreground = list.getForeground());
		}
		// render cell data
		if (value != null && value instanceof CellData)
		{
			final CellData data = (CellData) value;
			if (data.getUserIcon() != null)
			{
				// render user icon
				add(BorderLayout.WEST, new JLabel(scaleImage(data.getUserIcon(), iconHeight)));
			}
			// render message body
			final AntiAliased.TextPane message = new AntiAliased.TextPane(messageFont, false);
			message.setText(data.getMessage());
			message.setForeground(foreground);
			add(BorderLayout.CENTER, message);
			// render additional message details
			if (includeDetails)
			{
				JLabel attachment = null;
				if (data.getAttachment() != null)
				{
					attachment = new JLabel(scaleImage(data.getAttachment(), iconHeight));
				}
				add(BorderLayout.EAST, createDetailsPane(attachment, data, foreground));
			}
		}
		return this;
	}

	public void setCellHeight(final int cellHeight)
	{
		this.cellHeight = cellHeight;
		setPreferredSize(new Dimension(0, cellHeight));
	}

	protected ImageIcon scaleImage(final URL imageUrl, final int maxBounds)
	{
		ImageIcon image = (ImageIcon) cachedThumbnails.get(imageUrl);
		if (image != null) { return image; }
		image = new ImageIcon(imageUrl);
		final int h = image.getIconHeight();
		final int w = image.getIconWidth();
		double newW, newH;
		if (w > h)
		{
			newW = maxBounds;
			newH = (((double) maxBounds / w)) * h;
		}
		else
		{
			newH = maxBounds;
			newW = (((double) maxBounds / h)) * w;
		}
		final ImageIcon thumbnail = new ImageIcon(image.getImage().getScaledInstance((int) newW, (int) newH,
																						Image.SCALE_SMOOTH));
		cachedThumbnails.put(imageUrl, thumbnail);
		return thumbnail;
	}
}
