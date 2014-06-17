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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.Serializable;

import javax.swing.JPanel;

public class Panel implements Serializable
{

	public static class Background extends Transparent implements Serializable
	{
		private Image image = null;

		public Background(final String imagePath)
		{
			this(imagePath, DEFAULT_INSETS);
		}

		public Background(final String imagePath, final Insets insets)
		{
			super(insets);
			image = MessageBoardGUI.createImageIcon(imagePath).getImage();
		}

		@Override
		public void paintComponent(final Graphics g)
		{
			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		}

	}

	public static class Transparent extends JPanel implements Serializable
	{
		private Insets insets = null;

		public Transparent()
		{
			this(DEFAULT_INSETS, new BorderLayout());
		}

		public Transparent(final Insets insets)
		{
			this(insets, new BorderLayout());
		}

		public Transparent(final Insets insets, final LayoutManager layout)
		{
			super(layout);
			this.insets = insets;
			setOpaque(false);
		}

		public Transparent(final LayoutManager layout)
		{
			this(DEFAULT_INSETS, layout);
		}

		@Override
		public Insets getInsets()
		{
			return insets;
		}

		public void setInsets(final Insets insets)
		{
			this.insets = insets;
		}
	}

	public static final Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);
}
