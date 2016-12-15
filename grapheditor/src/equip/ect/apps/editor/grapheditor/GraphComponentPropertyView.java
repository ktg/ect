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

import java.awt.*;
import java.awt.geom.Rectangle2D;

import equip.ect.apps.editor.interactive.InteractiveCanvasItemView;

class GraphComponentPropertyView extends InteractiveCanvasItemView
{
	static boolean renderPropValue = true;
	private static final Color normalColor = Color.white;
	private static final Color selectedColor = Color.green.darker();
	private static final Color valueColor = Color.blue.darker();
	private static final Font propertyFont = new Font("Arial", Font.PLAIN, 10);
	private String name, value;

	GraphComponentPropertyView(final String name, final String value)
	{
		setName(name);
		setValue(value);
	}

	void paint(final Graphics2D g)
	{
		// g.setClip(posX, posY, width, height);
		g.setFont(propertyFont);
		final FontMetrics metrics = g.getFontMetrics();
		Rectangle2D r2d = metrics.getStringBounds(name, g);
		final double nameWidth = r2d.getWidth();
		g.fillRect(posX + 1,posY + 1, width - 2, height - 2);
		//g.fillRoundRect(posX, posY, width, height, 10, 10);
		g.setColor(Color.black);
		//g.drawRect(posX, posY, width - 1, height - 1);
		g.drawString(name, posX + 5, (int)(posY + r2d.getHeight()) - 1);
		if (renderPropValue)
		{
			double dotWidth = metrics.getStringBounds("...", g).getWidth();
			String valueString = value;
			r2d = metrics.getStringBounds(valueString, g);
			double valueWidth = r2d.getWidth();
			boolean dots = false;
			while(nameWidth + valueWidth + 15 > width && valueString.length() > 0)
			{
				valueString = valueString.substring(0, valueString.length() - 1);
				r2d = metrics.getStringBounds(valueString, g);
				valueWidth = r2d.getWidth() + dotWidth;
				dots = true;
			}

			if(dots)
			{
				valueString = valueString + "...";
			}
			g.setColor(valueColor);
			g.drawString(valueString, (int)(posX + width - valueWidth - 5), (int)(posY + r2d.getHeight()) - 1);
		}
	}

	@Override
	public void paintActive(final Graphics2D g)
	{
		g.setColor(GraphEditorResources.PROPERTY_ACTIVE_COLOR);
		paint(g);
	}

	@Override
	public void paintNormal(final Graphics2D g)
	{
		g.setColor(normalColor);
		paint(g);
	}

	@Override
	public void paintSelected(final Graphics2D g)
	{
		g.setColor(selectedColor);
		paint(g);
	}

	@Override
	public void paintShadowed(final Graphics2D g)
	{
	}

	@Override
	public void paintUnavailable(final Graphics2D g)
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