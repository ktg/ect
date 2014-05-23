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
 * TexturedJList, $RCSfile: TexturedJList.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * Copyright (c) 2002, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Textured version of JList using standard TexturePaint swing method.
 */

public class TexturedJList extends JList
{

	private TexturePaint texturePaint = null;

	public TexturedJList()
	{
		this(null);
	}

	public TexturedJList(final ListModel dataModel, final String textureFile)
	{
		this(textureFile);
		this.setModel(dataModel);
	}

	public TexturedJList(final String textureFile)
	{
		super();
		if (textureFile != null)
		{
			texturePaint = getImageTexture(textureFile);
		}
		setBackground(EditorResources.BACKGROUND_COLOR);
	}

	public TexturePaint getImageTexture(final String filename)
	{
		final Image img = MediaFactory.createImage(filename, this);
		final int iw = img.getWidth(this);
		final int ih = img.getHeight(this);
		if (img == null || iw < 0 || ih < 0) { return null; }
		final BufferedImage bi = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
		final Graphics2D tG2 = bi.createGraphics();
		tG2.drawImage(img, 0, 0, this);
		final Rectangle r = new Rectangle(0, 0, iw, ih);
		return new TexturePaint(bi, r);
	}

	/**
	 * Render the background with the specified texture context. Calls super.paintComponent at the
	 * end.
	 */
	@Override
	public void paintComponent(final Graphics g)
	{
		final Graphics2D g2 = (Graphics2D) g;
		if (texturePaint != null)
		{
			final int w = getSize().width;
			final int h = getSize().height;
			g2.setPaint(texturePaint);
			g2.fill(new Rectangle(0, 0, w, h));
		}
		super.paintComponent(g);
	}

}
