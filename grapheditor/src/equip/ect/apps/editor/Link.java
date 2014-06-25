/*
 <COPYRIGHT>

 Copyright (c) 2004-2005, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */

package equip.ect.apps.editor;

import equip.ect.PropertyLinkRequest;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

public abstract class Link extends BeanCanvasItem
{
	protected Connectable source, target;

	protected PropertyLinkRequest linkReq;
	protected boolean connected = false;

	public Link(final Component canvas, final Point startPoint, final Point endPoint, final Connectable source,
	            final Connectable target, final RenderableLink linkView)
	{
		this(canvas, startPoint, endPoint, source, target, linkView, null);
	}

	public Link(final Component canvas, final Point startPoint, final Point endPoint, final Connectable source,
	            final Connectable target, final RenderableLink linkView, final PropertyLinkRequest link)
	{
		super(canvas, linkView, link != null ? link.getID().toString() : null, "Link");
		this.source = source;
		this.target = target;
	}

	@Override
	public abstract void cleanUp();

	@Override
	public Object clone(final Component targetCanvas)
	{
		/*
		 * return new Link(canvas, ((RenderableLink) view).getStartPoint(), ((RenderableLink)
		 * view).getEndPoint(), source, target, (RenderableLink) view, linkReq);
		 */
		return null;
	}

	public final Point getEndPoint()
	{
		return ((RenderableLink) view).getEndPoint();
	}

	public void setEndPoint(final Point endP)
	{
		setEndPoint((int) endP.getX(), (int) endP.getY());
	}

	public PropertyLinkRequest getLinkRequest()
	{
		return this.linkReq;
	}

	public void setLinkRequest(final PropertyLinkRequest linkReq)
	{
		this.linkReq = linkReq;
		this.name = "Link";
		if (linkReq != null)
		{
			this.beanid = linkReq.getID().toString();
		}
	}

	public Connectable getSource()
	{
		return source;
	}

	public void setSource(final Connectable source)
	{
		this.source = source;
	}

	/*
	 * { if (linkReq != null) { try {
	 * DataspaceMonitor.getMonitor().getDataspace().delete(linkReq.getID()); } catch
	 * (DataspaceInactiveException ex) { Info.message("Warning: Error removing link from
	 * dataspace"); Info.message(ex.getMessage()); } }
	 *
	 * if (source != null) { ((GraphComponentProperty) source).getParent().removeOutputLink(this);
	 * ((GraphComponentProperty) source).removeOutputLink(this); } if (target != null) {
	 * ((GraphComponentProperty) target).getParent().removeInputLink(this);
	 * ((GraphComponentProperty) target).removeInputLink(this); } }
	 */
	public final Point getStartPoint()
	{
		return ((RenderableLink) view).getStartPoint();
	}

	public void setStartPoint(final Point startP)
	{
		setStartPoint((int) startP.getX(), (int) startP.getY());
	}

	public Connectable getTarget()
	{
		return target;
	}

	public void setTarget(final Connectable target)
	{
		this.target = target;
	}

	@Override
	public boolean isInside(final int x, final int y)
	{
		return ((RenderableLink) view).getBounds().contains(x, y);
	}

	public void setConnected(final boolean conn)
	{
		this.connected = conn;
		((RenderableLink) view).setConnected(conn);
	}

	public void setEndPoint(final int x, final int y)
	{
		((RenderableLink) view).setEndPoint(x, y);
		final Rectangle bounds = view.getBounds();

		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.lastWidth = this.width;
		this.lastHeight = this.height;

		posX = bounds.x;
		posY = bounds.y;
		width = bounds.width;
		height = bounds.height;
	}

	public void setStartPoint(final int x, final int y)
	{
		((RenderableLink) view).setStartPoint(x, y);
		final Rectangle bounds = view.getBounds();
		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.lastWidth = this.width;
		this.lastHeight = this.height;
		posX = bounds.x;
		posY = bounds.y;
		width = bounds.width;
		height = bounds.height;
	}

	public void updateLinkBounds()
	{
		if (view != null && view instanceof RenderableLink)
		{
			((RenderableLink) view).calculate();
		}
	}

	/**
	 * Set idle time to -1 so as to prevent daemons from killing it.
	 */
	@Override
	protected long idleTime()
	{
		return -1;
	}

}
