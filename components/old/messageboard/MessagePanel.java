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
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class MessagePanel extends Panel.Background implements Serializable
{

	private Font font = new Font("Century Gothic", Font.PLAIN, 25);
	private AntiAliased.Label attachment = new AntiAliased.Label(font);
	private AntiAliased.Label userIcon = new AntiAliased.Label(font);
	private AntiAliased.TextPane message = new AntiAliased.TextPane(font, false);
	private AntiAliased.Label date = new AntiAliased.Label(font);
	private AntiAliased.Label from = new AntiAliased.Label(font);

	public MessagePanel(final JButton reply, final JButton back)
	{
		super("/brushed_metal.jpg", new Insets(5, 5, 5, 5));
		setLayout(new BorderLayout(10, 10));
		// place attachment
		final JScrollPane scroll = new JScrollPane(attachment);
		scroll.setOpaque(false);
		scroll.setBorder(new EtchedBorder());
		add(BorderLayout.CENTER, scroll);
		// place message and icon
		final Panel.Transparent topPanel = new Panel.Transparent(new BorderLayout());
		topPanel.add(BorderLayout.WEST, userIcon);
		topPanel.add(BorderLayout.CENTER, message);
		add(BorderLayout.NORTH, topPanel);
		// place from, date and buttons
		final Panel.Transparent bottomPanel = new Panel.Transparent(new BorderLayout());
		bottomPanel.add(date);
		bottomPanel.add(BorderLayout.WEST, from);
		date.setVerticalTextPosition(SwingConstants.BOTTOM);
		date.setHorizontalTextPosition(SwingConstants.CENTER);
		date.setHorizontalAlignment(SwingConstants.CENTER);
		// reply and back buttons
		final Panel.Transparent buttons = new Panel.Transparent(new GridLayout(1, 2));
		buttons.add(reply);
		buttons.add(back);
		bottomPanel.add(BorderLayout.EAST, buttons);
		add(BorderLayout.SOUTH, bottomPanel);
	}

	public void displayMessage(final CellData data)
	{
		message.setText(data.getMessage());
		if (data.getAttachment() != null)
		{
			attachment.setIcon(new ImageIcon(data.getAttachment()));
		}
		if (data.getUserIcon() != null)
		{
			userIcon.setIcon(new ImageIcon(data.getUserIcon()));
		}
		else
		{
			userIcon.setIcon(null);
		}
		date.setText(data.getDate().toString());
		from.setText(data.getDisplayString());
	}
}
