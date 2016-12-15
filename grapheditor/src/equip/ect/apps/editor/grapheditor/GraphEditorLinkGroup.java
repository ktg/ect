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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Point;
import java.util.List;

import equip.ect.apps.editor.Connectable;
import equip.ect.apps.editor.CurvedLine;
import equip.ect.apps.editor.interactive.InteractiveCanvas;
import equip.ect.apps.editor.Link;
import equip.ect.apps.editor.LinkGroup;
import equip.ect.apps.editor.RenderableLink;

class GraphEditorLinkGroup extends LinkGroup
{

	GraphEditorLinkGroup(final Component canvas, final Point startPoint, final Point endPoint,
			final Connectable source, final Connectable target)
	{
		this(canvas, startPoint, endPoint, source, target, null);

	}

	GraphEditorLinkGroup(final Component canvas, final Point startPoint, final Point endPoint,
			final Connectable source, final Connectable target, final List<Link> links)
	{
		super(canvas, startPoint, endPoint, source, target, new CurvedLine(startPoint, endPoint), links);
		((RenderableLink) view).setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
	}

	void compact()
	{
		// System.out.println("Compacting");
		((GraphEditorCanvas) canvas).removeItems(getLinks(), false);
		((InteractiveCanvas) canvas).addItem(this);
	}

	void expand()
	{
		((GraphEditorCanvas) canvas).removeItem(this, false);
		// we need to add the links to the canvas again,
		// however they need to be added to the properties as
		// well as they

		/*
		 * Collection links = getLinks();
		 * 
		 * for (Iterator it = links.iterator(); it.hasNext();) { GraphEditorLink link =
		 * (GraphEditorLink) it.next(); GraphComponentProperty source = (GraphComponentProperty)
		 * link .getSource(); if (source != null) { source.addOutputLink(link); }
		 * GraphComponentProperty target = (GraphComponentProperty) link .getTarget(); if (target !=
		 * null) { target.addInputLink(link); } }
		 */
		((InteractiveCanvas) canvas).addItems(getLinks());

	}

	@Override
	public boolean isInside(final int x, final int y)
	{
		final double THRESH = 5.0;
		return ((CurvedLine) view).intersects(x - THRESH, y - THRESH, THRESH * 2, THRESH * 2);
	}
}
