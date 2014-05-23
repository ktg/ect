/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * TexturedPanel, $RCSfile: TexturedPanel.java,v $
 * 
 * $Revision: 1.2 $ $Date: 2012/04/03 12:27:26 $
 * 
 * $Author: chaoticgalen $ Original Author: Jan Humble Copyright (c) 2001, Swedish
 * Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;

import javax.swing.JPanel;

// import java.util.*;

public class TexturedPanel extends JPanel
{

	private Image texture = null;

	private int textureWidth, textureHeight;

	public TexturedPanel()
	{
		this(new BorderLayout(), null);
	}

	public TexturedPanel(final LayoutManager layout)
	{
		this(layout, null);
	}

	public TexturedPanel(final LayoutManager layout, final String textureFile)
	{
		super(layout);
		// setOpaque(false);
		setTexture(textureFile);
	}

	public Image loadTexture(final String textureFile)
	{
		/*
		 * Should probably use the icon interface, to be in accordance with matte borders from
		 * swing. ImageIcon textureLoader = new ImageIcon(textureFile); But until then ...
		 */
		return MediaFactory.createImage(textureFile, this);
	}

	@Override
	public void paintComponent(final Graphics g)
	{
		//final Graphics2D g2 = (Graphics2D) g;

		final int width = getWidth();
		final int height = getHeight();

		if (texture != null)
		{
			for (int y = 0; y < height; y += textureHeight)
			{
				for (int x = 0; x < width; x += textureWidth)
				{
					g.drawImage(texture, x, y, this);
				}
			}
		}
		super.paintComponent(g);

	}

	public void setImage(final Image image)
	{
		setTexture(image);
	}

	public void setTexture(final Image image)
	{
		if(image != null)
		{
			textureWidth = image.getWidth(this);
			textureHeight = image.getHeight(this);
			texture = image;
		}
	}

	public void setTexture(final String textureFile)
	{
		if (textureFile != null)
		{
			setTexture(loadTexture(textureFile));
		}
		else
		{
			setBackground(EditorResources.BACKGROUND_COLOR);
		}

	}
}
