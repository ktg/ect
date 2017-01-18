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
 * CurvedLine, $RCSfile: CurvedLine.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * 
 */

package equip.ect.apps.editor;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

public class CurvedLine extends RenderableLink
{

	private final Point[] controlPoints;

	private static int defaultControlOffsetX = 40;
	private static int defaultControlOffsetY = 0;

	public CurvedLine(final Point startPoint, final Point endPoint)
	{
		this(startPoint, endPoint,
				new Point(startPoint.x + defaultControlOffsetX, startPoint.y + defaultControlOffsetY), new Point(
						endPoint.x - defaultControlOffsetX, endPoint.y - defaultControlOffsetY));
	}

	private CurvedLine(final Point startPoint, final Point endPoint, final Point controlPoint1, final Point controlPoint2)
	{
		super(startPoint, endPoint);
		controlPoints = new Point[2];
		controlPoints[0] = controlPoint1;
		controlPoints[1] = controlPoint2;
		calculate();
	}

	@Override
	public void drawLink(final Graphics2D g)
	{
		g.setColor(color);
		final Stroke currentStroke = g.getStroke();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(stroke);
		g.draw(path);
		// draw arrow heads
		// start
		g.fillPolygon(new int[] { startPoint.x + 5, startPoint.x, startPoint.x }, new int[] { startPoint.y,
																								startPoint.y + 5,
																								startPoint.y - 5 }, 3);
		// end
		g.fillPolygon(new int[] { endPoint.x, endPoint.x - 5, endPoint.x - 5 }, new int[] { endPoint.y,
																							endPoint.y + 5,
																							endPoint.y - 5 }, 3);
		g.setStroke(currentStroke);
	}

	@Override
	public Rectangle getClip()
	{
		final Rectangle clip = path.getBounds();
		clip.grow(0, 5); // allow space for arrow heads
		return clip;
	}

	public boolean intersects(final double x, final double y, final double width, final double height)
	{
		return path.intersects(x, y, width, height);
	}

	@Override
	public void setEndPoint(final int x, final int y)
	{
		controlPoints[1].setLocation(x - defaultControlOffsetX, y - defaultControlOffsetY);
		super.setEndPoint(x, y);
	}

	@Override
	public void setStartPoint(final int x, final int y)
	{
		controlPoints[0].setLocation(x + defaultControlOffsetX, y + defaultControlOffsetY);
		super.setStartPoint(x, y);
	}

	@Override
	protected void calculate()
	{
		if (path == null)
		{
			path = new GeneralPath(Path2D.WIND_EVEN_ODD);
		}
		path.reset();
		path.moveTo(startPoint.x, startPoint.y);
		path.lineTo(startPoint.x + 10, startPoint.y);
		path.curveTo(	controlPoints[0].x, controlPoints[0].y, controlPoints[1].x, controlPoints[1].y,
						endPoint.x - 10,
						endPoint.y);
		path.lineTo(endPoint.x, endPoint.y);
	}
}
