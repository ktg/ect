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
/*
 * RenderableLink, $RCSfile: RenderableLink.java,v $
 *
 * $Revision: 1.6 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * 
 */

package equip.ect.apps.editor;

import equip.ect.apps.editor.interactive.InteractiveCanvasItemView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

public abstract class RenderableLink extends InteractiveCanvasItemView
{
	protected final Point startPoint, endPoint;

	protected Color color;

	protected GeneralPath path;

	protected Stroke stroke;

	protected boolean connected;

	public RenderableLink(final Point startPoint, final Point endPoint)
	{
		this(startPoint, endPoint, new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));

	}

	public RenderableLink(final Point startPoint, final Point endPoint, final Stroke stroke)
	{
		this.startPoint = startPoint;
		this.endPoint = endPoint;
		this.color = Color.black;
		setStroke(stroke);
	}

	public abstract void drawLink(Graphics2D g);

	@Override
	public Rectangle getBounds()
	{
		if (path != null) { return path.getBounds(); }
		return null;
	}

	public final Point getEndPoint()
	{
		return this.endPoint;
	}

	public final Point getStartPoint()
	{
		return this.startPoint;
	}

	@Override
	public void paintActive(final Graphics2D g)
	{
		paintNormal(g);
	}

	@Override
	public void paintNormal(final Graphics2D g)
	{
		setColor(Color.black);
		drawLink(g);
	}

	@Override
	public void paintSelected(final Graphics2D g)
	{
		setColor(Color.red);
		drawLink(g);
	}

	@Override
	public void paintShadowed(final Graphics2D g)
	{
		paintNormal(g);
	}

	@Override
	public void paintUnavailable(final Graphics2D g)
	{
		paintNormal(g);
	}

	public void paintUnselected(final Graphics2D g)
	{
		paintNormal(g);
	}

	public void setColor(final Color color)
	{
		this.color = color;
	}

	public void setConnected(final boolean conn)
	{
		this.connected = conn;
	}

	public void setEndPoint(final int x, final int y)
	{
		endPoint.setLocation(x, y);
		calculate();
	}

	public void setEndPoint(final Point endP)
	{
		setEndPoint((int) endP.getX(), (int) endP.getY());
	}

	public void setStartPoint(final int x, final int y)
	{
		startPoint.setLocation(x, y);
		calculate();
	}

	public void setStartPoint(final Point startP)
	{
		setStartPoint((int) startP.getX(), (int) startP.getY());
	}

	public void setStroke(final Stroke stroke)
	{
		this.stroke = stroke;
	}

	@Override
	public String toString()
	{
		return "Link startP=" + startPoint + " endP=" + endPoint;
	}

	protected abstract void calculate();

}
