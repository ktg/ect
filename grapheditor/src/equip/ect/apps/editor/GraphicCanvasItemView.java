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
 * GraphicCanvasItem, $RCSfile: GraphicCanvasItemView.java,v $
 * 
 * $Revision: 1.4 $ $Date: 2012/04/03 12:27:26 $
 * 
 * $Author: chaoticgalen $ Original Author: Jan Humble Copyright (c) 2001, Swedish
 * Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import equip.ect.apps.editor.interactive.InteractiveCanvasItemView;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class GraphicCanvasItemView extends InteractiveCanvasItemView
{

	protected transient Image image;

	protected transient Image imageBuffer = null;

	public GraphicCanvasItemView(final Component canvas, final Image image, final int width, final int height)
	{
		super(canvas);
		setSize(width, height);
		this.image = image;
	}

	@Override
	public Rectangle getClip()
	{
		return new Rectangle(posX, posY, width, height);
	}

	public Image getIconView()
	{
		if (imageBuffer == null)
		{
			imageBuffer = createImageBuffer();
		}
		return imageBuffer;
	}

	public Image getIconView(final int iconWidth, final int iconHeight)
	{
		return createImageBuffer(iconWidth, iconHeight);
	}

	public Image getImage()
	{
		return image;
	}

	@Override
	public void paintActive(final Graphics2D g)
	{
	}

	@Override
	public void paintNormal(final Graphics2D g)
	{
		if (imageBuffer == null)
		{
			imageBuffer = createImageBuffer();
		}
		g.drawImage(imageBuffer, posX, posY, canvas);
	}

	@Override
	public void paintSelected(final Graphics2D g)
	{
		paintNormal(g);
	}

	@Override
	public void paintShadowed(final Graphics2D g)
	{
	}

	@Override
	public void paintUnavailable(final Graphics2D g)
	{
	}

	public void setImage(final Image image)
	{
		this.image = image;
		imageBuffer = createImageBuffer();
	}

	public void setImage(final String imageFile)
	{
		setImage(MediaFactory.createImage(imageFile, canvas));
	}

	protected Image createImageBuffer()
	{
		return this.image;
	}

	protected Image createImageBuffer(final int bufferWidth, final int bufferHeight)
	{
		if(image == null)
		{
			return null;
		}
			final Image scaled = createImageBuffer().getScaledInstance(bufferWidth, bufferHeight, Image.SCALE_SMOOTH);
	
			canvas.checkImage(scaled, canvas);
			return scaled;
	}
}
