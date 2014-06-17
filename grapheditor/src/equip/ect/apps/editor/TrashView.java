package equip.ect.apps.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class TrashView extends GraphicCanvasItemView
{

	public TrashView(final Component canvas)
	{
		super(canvas, MediaFactory.createImage(EditorResources.TRASH_ICON, canvas), 32, 32);
		this.imageBuffer = createImageBuffer(this.width, this.height);
	}

	@Override
	public void paintSelected(final Graphics g)
	{
		paintNormal(g);
		g.setColor(new Color(255, 255, 255, 100));
		g.fillOval(posX, posY, width, height);
	}

}
