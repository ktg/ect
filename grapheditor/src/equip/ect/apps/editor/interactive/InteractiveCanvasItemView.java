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
 * InteractiveCanvasItemView, $RCSfile: InteractiveCanvasItemView.java,v $
 * 
 * $Revision: 1.3 $ $Date: 2012/04/03 12:27:26 $
 * 
 * $Author: chaoticgalen $ Original Author: Jan Humble
 */

package equip.ect.apps.editor.interactive;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;

public abstract class InteractiveCanvasItemView implements Serializable
{

	protected transient Component canvas;

	protected int posX, posY, width, height, lastPosX, lastPosY, lastWidth, lastHeight;

	public InteractiveCanvasItemView()
	{
		this(null);
	}

	public InteractiveCanvasItemView(final Component canvas)
	{
		this.canvas = canvas;
	}

	public Rectangle getBounds()
	{
		return new Rectangle(posX, posY, width, height);
	}

	public final Point getCenter()
	{
		return new Point(posX + (int) (width * 0.5), posY + (int) (height * 0.5));
	}

	public Rectangle getClip()
	{
		return new Rectangle(posX, posY, width + 1, height + 1);
	}

	public int getHeight()
	{
		return height;
	}

	public int getPosX()
	{
		return posX;
	}

	public int getPosY()
	{
		return posY;
	}

	public int getWidth()
	{
		return width;
	}

	public abstract void paintActive(Graphics2D g);

	public abstract void paintNormal(Graphics2D g);

	public abstract void paintSelected(Graphics2D g);

	public abstract void paintUnavailable(Graphics2D g);

	public void setHeight(final int height)
	{
		this.height = height;
	}

	public void setPosition(final int x, final int y)
	{
		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.posX = x;
		this.posY = y;
	}

	public void setSize(final int width, final int height)
	{
		this.width = width;
		this.height = height;
	}

	public void setTargetCanvas(final Component canvas)
	{
		this.canvas = canvas;
	}

	public void setWidth(final int width)
	{
		this.width = width;
	}

	public void translatePosition(final int dx, final int dy)
	{
		setPosition(posX + dx, posY + dy);
	}
}
