/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2004-2005, University of Nottingham All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  - Neither the name of the University of Nottingham nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * </COPYRIGHT>
 * 
 * Created by: Jan Humble (University of Nottingham) Contributors: Jan Humble
 * (University of Nottingham)
 *  
 */

package equip.ect.apps.editor.grapheditor;

import equip.ect.apps.editor.InteractiveCanvasItemView;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * The default view (or rendering strategy) for a GraphComponent.
 */
public class GraphComponentView extends InteractiveCanvasItemView
{

	private Drawer drawer;

	private String name;

	private int headerWidth, headerHeight;

	private Font hostIDFont = new Font("Helvetica", Font.PLAIN, 10);

	private static Font headerFont = new Font("Arial", Font.PLAIN, 13);

	private transient List<GraphComponentProperty> graphCompProps;

	private String hostID;

	private boolean highlight = false;
	private static Color HIGHLIGHT_COLOR = new Color(50, 100, 255);

	public static boolean renderHostID = false;

	public GraphComponentView(final Component canvas, final String name, final String hostID,
	                          final List<GraphComponentProperty> renderableProps)
	{
		super(canvas);
		this.name = name;
		this.hostID = (hostID != null) ? hostID : "unknown host";
		this.graphCompProps = renderableProps;
	}

	public void drawHeader(final Graphics2D g2, final boolean collapsed)
	{
		g2.setFont(headerFont);
		FontMetrics fontMetrics = g2.getFontMetrics();
		Rectangle2D r2d = fontMetrics.getStringBounds(name, g2);
		g2.setColor(highlight ? HIGHLIGHT_COLOR : Color.gray);

		int height = headerHeight;
		if (!collapsed && (drawer.getDrawerState() == Drawer.OPEN || drawer.getDrawerState() == Drawer.COMPACT))
		{
			height += 10;
		}

		g2.fillRoundRect(posX, posY, headerWidth, height, 10, 10);
		g2.setColor(Color.black);
		g2.drawRoundRect(posX, posY, headerWidth - 1, height - 1, 10, 10);
		g2.setColor(Color.white);
		g2.drawString(name, (int) (posX + 0.5 * (headerWidth - r2d.getWidth())), // center
				posY + 1 + (int) r2d.getHeight());
		if (renderHostID)
		{
			g2.setFont(hostIDFont);
			fontMetrics = g2.getFontMetrics();
			r2d = fontMetrics.getStringBounds(hostID, g2);
			g2.setColor(Color.green);
			final double hostWidth = r2d.getWidth();
			g2.drawString(hostID, (int) (posX + 0.5 * (headerWidth - hostWidth)), // center
					posY + 2 * (int) r2d.getHeight());

		}
	}

	public void drawProps(final Graphics2D g)
	{
		if (graphCompProps != null)
		{
			Rectangle2D rect = null;
			for (GraphComponentProperty gcp : graphCompProps)
			{
				if(gcp.isVisible())
				{
					if (rect == null)
					{
						rect = gcp.getBounds();
					}
					else
					{
						rect = rect.createUnion(gcp.getBounds());
					}
				}
			}

			if(rect != null)
			{
				g.setColor(Color.white);
				g.fillRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth() - 1, (int)rect.getHeight() - 1);
				g.setColor(Color.black);
				g.drawRect((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth() - 1, (int)rect.getHeight() - 1);
			}

			for (GraphComponentProperty gcp : graphCompProps)
			{
				if (gcp.isVisible())
				{
					gcp.paintComponent(g);
				}
			}
		}
	}

	public final int getHeaderHeight()
	{
		return this.headerHeight;
	}

	public final int getHeaderWidth()
	{
		return this.headerWidth;
	}

	@Override
	public void paintActive(final Graphics g)
	{
		g.setColor(Color.red);
		g.fillRect(posX, posY, width, height);
	}

	@Override
	public void paintNormal(final Graphics g)
	{
		// paint the drawer first, since we need to hide the top part.
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);
		drawer.paintComponent(g);
		drawHeader(g2, false);
		if (drawer.getDrawerState() == Drawer.OPEN || drawer.getDrawerState() == Drawer.COMPACT)
		{
			drawProps(g2);
		}
	}

	@Override
	public void paintSelected(final Graphics g)
	{
		paintNormal(g);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.red);
		final Stroke current = g2.getStroke();
		g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
		g2.drawRoundRect(posX, posY, width, height, 10, 10);
		g2.setStroke(current);
	}

	@Override
	public void paintShadowed(final Graphics g)
	{
		paintNormal(g);
	}

	@Override
	public void paintUnavailable(final Graphics g)
	{
		paintNormal(g);
	}

	public void setDrawer(final Drawer drawer)
	{
		this.drawer = drawer;
	}

	public void setGraphComponentProperties(final List<GraphComponentProperty> graphCompProps)
	{
		this.graphCompProps = graphCompProps;
	}

	public void setHighlighted(final boolean highlight)
	{
		this.highlight = highlight;

	}

	public void setHostID(final String hostID)
	{
		this.hostID = hostID;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * Calculate the size for component, adjusting to font metrics. Different sizes for different
	 * fonts and targets canvases.
	 */
	void calculateSize()
	{
		this.lastWidth = width;
		this.lastHeight = height;

		final int insetX = 10;
		final int insetY = 5;
		final Graphics g = canvas.getGraphics();
		g.setFont(headerFont);
		final FontMetrics fontMetrics = g.getFontMetrics();
		Rectangle2D r2d = fontMetrics.getStringBounds(name, g);
		headerWidth = (int) r2d.getWidth() + insetX;
		headerHeight = (int) r2d.getHeight() + insetY;
		this.height = headerHeight;
		this.width = headerWidth;
		//final int visibleCount = 0;
		int maxWidth = Math.max(headerWidth, drawer.getWidth());
		if (graphCompProps != null)
		{
			for (GraphComponentProperty gcp : graphCompProps)
			{
				r2d = fontMetrics.getStringBounds(gcp.getName(), g);
				final int currentWidth = (int) r2d.getWidth() + insetX;
				if (currentWidth > maxWidth)
				{
					maxWidth = currentWidth;
				}
				/*
				 * if (drawer.getDrawerState() == Drawer.OPEN) { gcp.setVisible(true);
				 * visibleCount++; } else if (drawer.getDrawerState() == Drawer.COMPACT) { if
				 * (gcp.keepVisible() || gcp.isLinked()) { gcp.setVisible(true); visibleCount++; }
				 * else { gcp.setVisible(false); } } else { gcp.setVisible(false); }
				 */
			}

			/*
			 * if (drawer.getDrawerState() == Drawer.COMPACT) { if (drawer.getPreviousDrawerState()
			 * == Drawer.CLOSED) { if (visibleCount == graphCompProps.size()) {
			 * drawer.setDrawerState(Drawer.OPEN); } else if (visibleCount < 1) {
			 * drawer.setDrawerState(Drawer.OPEN); } } else if (drawer.getPreviousDrawerState() ==
			 * Drawer.OPEN) { if (visibleCount < 1) { drawer.setDrawerState(Drawer.CLOSED); } else
			 * if (visibleCount == graphCompProps.size()) { drawer.setDrawerState(Drawer.CLOSED); }
			 * } }
			 */

			this.width = this.headerWidth = maxWidth;
			int y = posY + headerHeight - 1;
			for (GraphComponentProperty gcp : graphCompProps)
			{
				if (drawer.getDrawerState() == Drawer.OPEN)
				{
					gcp.setVisible(true);
				}
				else if (drawer.getDrawerState() == Drawer.COMPACT)
				{
					if (gcp.keepVisible() || gcp.isLinked())
					{
						gcp.setVisible(true);
					}
					else
					{
						gcp.setVisible(false);
					}
				}
				else if (drawer.getDrawerState() == Drawer.CLOSED)
				{
					gcp.setVisible(false);
				}

				if (gcp.isVisible())
				{
					gcp.setWidth(maxWidth);
					gcp.setHeight(headerHeight - 5);
					gcp.setPosition(posX, y);
					y += gcp.getHeight() - 1;
					height += gcp.getHeight() - 1;
				}
			}
		}
		height += drawer.getHeight();
		final int ddx = (int) (0.5 * (headerWidth - drawer.getWidth()));
		drawer.setPosition(posX + ddx, posY + height - drawer.getHeight());
	}

}
