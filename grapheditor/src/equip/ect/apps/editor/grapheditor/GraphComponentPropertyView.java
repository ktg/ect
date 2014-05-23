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

package equip.ect.apps.editor.grapheditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import equip.ect.apps.editor.InteractiveCanvasItemView;

public class GraphComponentPropertyView extends InteractiveCanvasItemView
{

	private Color normalColor = Color.gray.brighter();

	private Color selectedColor = Color.green.darker();

	private Font propertyFont = new Font("Helvetica", Font.PLAIN, 10);

	private String name, value;

	public static boolean renderPropValue = true;

	GraphComponentPropertyView(final String name, final String value)
	{
		setName(name);
		setValue(value);
	}

	public void paint(final Graphics g)
	{
		// g.setClip(posX, posY, width, height);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setFont(propertyFont);
		final FontMetrics metrics = g2.getFontMetrics();
		Rectangle2D r2d = metrics.getStringBounds(name, g2);
		g2.fillRoundRect(posX, posY, width, height, 10, 10);
		g2.setColor(Color.black);
		g2.drawRoundRect(posX, posY, width - 1, height - 1, 10, 10);
		g2.drawString(name, (int) (posX + 0.5 * (width - r2d.getWidth())), // center
						posY + (int) r2d.getHeight());
		if (renderPropValue)
		{
			r2d = metrics.getStringBounds(value, g2);
			g.setColor(Color.blue);
			final double valueWidth = r2d.getWidth();
			g2.drawString(value, (int) (posX + 0.5 * (width - valueWidth)), // center
							posY + 2 * (int) r2d.getHeight());

		}
	}

	@Override
	public void paintActive(final Graphics g)
	{
		g.setColor(GraphEditorResources.PROPERTY_ACTIVE_COLOR);
		paint(g);
	}

	@Override
	public void paintNormal(final Graphics g)
	{
		g.setColor(normalColor);
		paint(g);
	}

	@Override
	public void paintSelected(final Graphics g)
	{
		g.setColor(selectedColor);
		paint(g);
	}

	@Override
	public void paintShadowed(final Graphics g)
	{
	}

	@Override
	public void paintUnavailable(final Graphics g)
	{
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setValue(final String value)
	{
		this.value = (value != null ? value : "null");
	}

}
