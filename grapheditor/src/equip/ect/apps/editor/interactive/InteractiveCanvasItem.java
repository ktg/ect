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

package equip.ect.apps.editor.interactive;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.Serializable;
import java.util.Calendar;

public abstract class InteractiveCanvasItem implements Serializable
{
	public static final int NORMAL_DRAW_PRIORITY = 1;
	protected int drawPriority = NORMAL_DRAW_PRIORITY;

	protected boolean visible = true;
	protected boolean keepVisible = false;
	protected int posX, posY, width, height, lastPosX, lastPosY, lastWidth, lastHeight;
	protected transient Rectangle lastClip;
	protected InteractiveCanvasItemView view;
	protected long startIdleTime; // idle time in millisecs
	protected transient Component canvas;
	private boolean available;
	private boolean selected = false;
	private boolean active = false;

	protected InteractiveCanvasItem(final Component canvas, final InteractiveCanvasItemView view)
	{
		this.canvas = canvas;
		this.view = view;
		if (view != null)
		{
			this.width = view.getWidth();
			this.height = view.getHeight();
			this.lastClip = view.getClip();
		}
		this.selected = false;
		this.active = false;
		this.available = true;
		this.startIdleTime = -1;
	}

	public abstract String getID();

	public void cleanUp()
	{
	}

	public Rectangle getBounds()
	{
		if (view != null)
		{
			return view.getBounds();
		}
		return null;
	}

	public final Point getCenter()
	{
		return view.getCenter();
	}

	/**
	 * Returns the clip to repaint.
	 */
	public Rectangle getClip()
	{
		final Rectangle viewClip = view.getClip();
		if (lastClip == null)
		{
			return viewClip;
		}

		final int clipX = Math.min(viewClip.x, lastClip.x) - 5;
		final int clipY = Math.min(viewClip.y, lastClip.y) - 5;
		final int maxWidth = Math.max(viewClip.width, lastClip.width);
		final int maxHeight = Math.max(viewClip.height, lastClip.height);
		final int clipWidth = Math.max(viewClip.x, lastClip.x) + maxWidth - clipX + 10;
		final int clipHeight = Math.max(viewClip.y, lastClip.y) + maxHeight - clipY + 10;
		final Rectangle clip = new Rectangle(clipX, clipY, clipWidth, clipHeight);
		lastClip = viewClip;
		return clip;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(final int height)
	{
		this.height = height;
		view.setHeight(height);
	}

	public Point getPosition()
	{
		return new Point(posX, posY);
	}

	public final boolean isSelected()
	{
		return selected;
	}

	public void setSelected(final boolean selected)
	{
		this.selected = selected;
		itemStateChanged();
	}

	public Component getTargetCanvas()
	{
		return canvas;
	}

	public void setTargetCanvas(final Component canvas)
	{
		this.canvas = canvas;
		if (view != null)
		{
			view.setTargetCanvas(canvas);
		}
	}

	public InteractiveCanvasItemView getView()
	{
		return this.view;
	}

	public void setView(final InteractiveCanvasItemView view)
	{
		this.view = view;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(final int width)
	{
		this.width = width;
		view.setWidth(width);
	}

	public boolean isInside(final int x, final int y)
	{
		return x >= posX && x <= (posX + width) && y >= posY && y <= (posY + height);
	}

	public boolean isInside(final int x1, final int y1, final int x2, final int y2)
	{
		return (posX >= x1 && (posX + width) <= x2 && posY >= y1 && (posY + height) <= y2);
	}

	public boolean isSelectable(final int x, final int y)
	{
		return isInside(x, y);
	}

	public final boolean isVisible()
	{
		return this.visible;
	}

	public final void setVisible(final boolean visible)
	{
		this.visible = visible;
	}

	public final boolean keepVisible()
	{
		return this.keepVisible;
	}

	public final void paintComponent(final Graphics2D g)
	{
		final Shape currentClip = g.getClip();
		g.setClip(view.getClip());

		if (selected)
		{
			view.paintSelected(g);
		}
		else if (active)
		{
			view.paintActive(g);
		}
		else
		{
			view.paintNormal(g);
		}

		if (!available)
		{
			view.paintUnavailable(g);
		}

		g.setClip(currentClip);
	}

	public void repaint()
	{
		if (canvas != null)
		{
			final Rectangle clip = getClip();
			canvas.repaint(-1, clip.x, clip.y, clip.width, clip.height);
		}
	}

	public final void setActive(final boolean active)
	{
		this.active = active;
		itemStateChanged();
	}

	public final void setAvailable(final boolean available)
	{
		this.available = available;
		itemStateChanged();
	}

	public final void setKeepVisible(final boolean keepVisible)
	{
		this.keepVisible = keepVisible;
	}

	public void setPosition(final int x, final int y)
	{
		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.posX = x;
		this.posY = y;
		view.setPosition(x, y);
	}

	public void setSize(final int width, final int height)
	{
		this.width = width;
		this.height = height;
		view.setSize(width, height);
	}

	public void translatePosition(final int dx, final int dy)
	{
		setPosition(posX + dx, posY + dy);
	}

	/**
	 * Initiates the idle time counter.
	 */
	final void flagStartIdleTime()
	{
		startIdleTime = Calendar.getInstance().getTimeInMillis();
	}

	protected void doOnTranslate()
	{
		repaint();
	}

	/**
	 * It is up to each implemented subclass to define it's own idle time. Returns the idle time for
	 * this item in milliseconds. Returns -1 if this item is currently not idle. Idle time default
	 * beahviour is idle while unselected.
	 */
	protected long idleTime()
	{
		if (!selected)
		{
			return Calendar.getInstance().getTimeInMillis()
					- startIdleTime;
		}
		return -1;
	}

	/**
	 * Paints this item immediately, otherwise events will happen sooner than the repaint() call.
	 */
	protected void itemStateChanged()
	{
		repaint();
		flagStartIdleTime();
	}

}
