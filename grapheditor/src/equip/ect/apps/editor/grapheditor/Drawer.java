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

import java.awt.Point;

import equip.ect.apps.editor.interactive.InteractiveCanvasItem;

/**
 * The Drawer is designed to accomodate subcomponents, and includes features to hide or bring these
 * into view. The actual rendering and default pull down mechanism is proposed through the
 * DrawerView.
 *
 * @author Jan Humble
 */

public class Drawer extends InteractiveCanvasItem
{
	public enum State
	{
		OPEN, CLOSED, COMPACT
	}

	public enum Type
	{
		NONE, UP, DOWN, BOTH
	}
	private State drawerState;
	private State previousState;
	private Type type;

	Drawer(final GraphComponent parent, final Type type, final State initialState)
	{
		super(parent.getTargetCanvas(), null);
		this.type = type;
		this.drawerState = initialState;

		setView(new DrawerView(this));
		setSize(50, 10);
	}

	public String getID() { return null; }

	public final State getDrawerState()
	{
		return this.drawerState;
	}

	public final void setDrawerState(final State drawerState)
	{
		this.previousState = this.drawerState;
		this.drawerState = drawerState;
		switch (drawerState)
		{
			case OPEN:
				setType(Type.UP);
				break;
			case COMPACT:
				setType(Type.BOTH);
				break;
			case CLOSED:
				setType(Type.DOWN);
				break;
			default:
				setType(Type.NONE);
		}
	}

	final Type getType()
	{
		return type;
	}

	private void setType(final Type type)
	{
		this.type = type;
	}

	Type getAction(final int x, final int y)
	{
		if (isInside(x, y))
		{
			if (type == Type.BOTH)
			{
				final Point c = getCenter();
				return (x < c.x ? Type.UP : Type.DOWN);
			}
			else
			{
				return type;
			}
		}
		else
		{
			return Type.NONE;
		}
	}

	final State getPreviousDrawerState()
	{
		return this.previousState;
	}
}