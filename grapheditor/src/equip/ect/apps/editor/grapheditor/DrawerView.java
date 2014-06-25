package equip.ect.apps.editor.grapheditor;

import equip.ect.apps.editor.interactive.InteractiveCanvasItemView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

class DrawerView extends InteractiveCanvasItemView
{
	private static final Color BACKGROUND_COLOR = new Color(175, 175, 175);
	private final Drawer drawer;

	DrawerView(final Drawer drawer)
	{
		this.drawer = drawer;
	}

	@Override
	public void paintActive(final Graphics2D g)
	{
	}

	public void paintDrawer(final Graphics2D g)
	{
		g.fillRoundRect(posX, posY - 20, width, height + 20, 10, 10);
		g.setColor(Color.black);
		g.drawRoundRect(posX, posY - 20, width, height + 19, 10, 10);
		final Point center = getCenter();
		final int arrowWidth = 5;
		final int arrowHeight = 7;
		switch (drawer.getType())
		{
			case Drawer.UP:
				drawUpArrow(g, center.x, posY + height - 2, arrowWidth, arrowHeight);
				break;
			case Drawer.DOWN:
				drawDownArrow(g, center.x, posY + height - 2, arrowWidth, arrowHeight);
				break;
			case Drawer.BOTH:
				drawUpArrow(g, center.x - 10, posY + height - 2, arrowWidth, arrowHeight);
				drawDownArrow(g, center.x + 10, posY + height - 2, arrowWidth, arrowHeight);
				break;
		}
	}

	@Override
	public void paintNormal(final Graphics2D g)
	{
		g.setColor(BACKGROUND_COLOR);
		paintDrawer(g);
	}

	@Override
	public void paintSelected(final Graphics2D g)
	{
		g.setColor(Color.blue);
		paintNormal(g);
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

	private void drawDownArrow(final Graphics2D g, final int x, final int y, final int arrowWidth, final int arrowHeight)
	{
		g.fillPolygon(new int[]{x - arrowWidth, x, x + arrowWidth},
				new int[]{y - arrowHeight, y, y - arrowHeight}, 3);
	}

	private void drawUpArrow(final Graphics2D g, final int x, final int y, final int arrowWidth, final int arrowHeight)
	{
		g.fillPolygon(new int[]{x - arrowWidth, x, x + arrowWidth}, new int[]{y, y - arrowHeight, y}, 3);
	}
}
