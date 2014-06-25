/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2004-2005, University of Nottingham All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the University of Nottingham nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
 * Created by: Jan Humble (University of Nottingham) 
 * Contributors: Jan Humble (University of Nottingham)
 *  
 */

package equip.ect.apps.editor.grapheditor;

import java.awt.Component;
import java.awt.Point;

import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.Connectable;
import equip.ect.apps.editor.CurvedLine;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.Link;
import equip.ect.apps.editor.RenderableLink;

/**
 * @author humble
 * 
 */
public class GraphEditorLink extends Link
{
	public GraphEditorLink(final Component canvas, final Point startPoint, final Point endPoint,
			final Connectable source, final Connectable target)
	{
		super(canvas, startPoint, endPoint, source, target, new CurvedLine(startPoint, endPoint), null);
	}

	public GraphEditorLink(final Component canvas, final Point startPoint, final Point endPoint,
			final Connectable source, final Connectable target, final RenderableLink linkView,
			final PropertyLinkRequest link)
	{
		super(canvas, startPoint, endPoint, source, target, linkView, link);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ect.apps.editor.Link#cleanUp()
	 */
	@Override
	public void cleanUp()
	{
		Info.message(this, "Cleaning up link");

		if (source != null)
		{
			((GraphComponentProperty) source).getParent().removeOutputLink(this);
			((GraphComponentProperty) source).removeOutputLink(this);
		}
		if (target != null)
		{
			((GraphComponentProperty) target).getParent().removeInputLink(this);
			((GraphComponentProperty) target).removeInputLink(this);
		}

	}

	@Override
	public Object clone(final Component canvas)
	{
		return new GraphEditorLink(canvas, ((RenderableLink) view).getStartPoint(),
				((RenderableLink) view).getEndPoint(), source, target, (RenderableLink) view, linkReq);
	}

	@Override
	public boolean isInside(final int x, final int y)
	{
		final double THRESH = 5.0;
		return ((CurvedLine) view).intersects(x - THRESH, y - THRESH, THRESH * 2, THRESH * 2);
	}
}
