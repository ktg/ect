/*
 <COPYRIGHT>

 Copyright (c) 2005, University of Nottingham
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

import equip.ect.apps.editor.interactive.InteractiveCanvasItem;

import java.awt.*;

/**
 * The Drawer is designed to accomodate subcomponents, and includes features to hide or bring these
 * into view. The actual rendering and default pull down mechanism is proposed through the
 * DrawerView.
 *
 * @author Jan Humble
 */

public class Drawer extends InteractiveCanvasItem
{
	public final static int OPEN = 1;
	public final static int CLOSED = 2;
	public final static int COMPACT = 3;

	private int drawerState, previousState;

	public final static int NONE = -1;
	public final static int UP = 0;
	public final static int DOWN = 1;
	public final static int BOTH = 2;

	private int type;

	Drawer(final GraphComponent parent, final int type, final int initialState)
	{
		super(parent.getTargetCanvas(), null);
		this.type = type;
		this.drawerState = initialState;

		setView(new DrawerView(this));
		setSize(50, 10);
	}

	public int getAction(final int x, final int y)
	{
		if (isInside(x, y))
		{
			if (type == BOTH)
			{
				final Point c = getCenter();
				return (x < c.x ? UP : DOWN);
			}
			else
			{
				return type;
			}
		}
		else
		{
			return NONE;
		}
	}

	public String getID() { return null; }

	public final int getDrawerState()
	{
		return this.drawerState;
	}

	public final int getPreviousDrawerState()
	{
		return this.previousState;
	}

	public final int getType()
	{
		return type;
	}

	public final void setDrawerState(final int drawerState)
	{
		this.previousState = this.drawerState;
		this.drawerState = drawerState;
		switch (drawerState)
		{
			case OPEN:
				setType(UP);
				break;
			case COMPACT:
				setType(BOTH);
				break;
			case CLOSED:
				setType(DOWN);
				break;
			default:
				setType(NONE);
		}
	}

	public void setType(final int type)
	{
		this.type = type;
	}
}