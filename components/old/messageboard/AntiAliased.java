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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class AntiAliased implements Serializable
{

	public static class Label extends JLabel implements Serializable
	{

		public Label(final Font font)
		{
			this(font, null, null, SwingConstants.LEADING);
		}

		public Label(final Font font, final String text, final Icon icon, final int orientation)
		{
			super(text, icon, orientation);
			setWidgetOptions(this, false);
			setFont(font);
		}

		public Label(final Font font, final String text, final int orientation)
		{
			this(font, text, null, orientation);
		}

		@Override
		public void paintComponent(final Graphics g)
		{
			setRenderingHints(g);
			super.paintComponent(g);
		}
	}

	public static class TextField extends JTextField implements Serializable
	{
		public TextField(final Font font, final boolean opaque)
		{
			setWidgetOptions(this, opaque);
			setFont(font);
		}

		@Override
		public void paintComponent(final Graphics g)
		{
			setRenderingHints(g);
			super.paintComponent(g);
		}
	}

	public static class TextPane extends JTextPane implements Serializable
	{

		public TextPane(final Font font, final boolean opaque)
		{
			setWidgetOptions(this, opaque);
			setFont(font);
		}

		@Override
		public void paintComponent(final Graphics g)
		{
			setRenderingHints(g);
			super.paintComponent(g);
		}
	}

	public static final Color DEFAULT_FOREGROUND_COLOR = Color.darkGray;

	public static final Border DEFAULT_BORDER = new EtchedBorder();

	public static final Color DEFAULT_BACKGROUND_COLOR = new Color(208, 206, 255);

	public static void setRenderingHints(final Graphics g)
	{
		final Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	public static void setWidgetOptions(final JComponent component, final boolean opaque)
	{
		component.setOpaque(opaque);
		component.setForeground(DEFAULT_FOREGROUND_COLOR);
		component.setBackground(DEFAULT_BACKGROUND_COLOR);
		component.setBorder(opaque ? DEFAULT_BORDER : null);
	}
}
