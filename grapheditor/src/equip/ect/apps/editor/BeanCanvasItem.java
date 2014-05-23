/*
 <COPYRIGHT>

 Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

 Created by: Jan Humble (Swedish Institute of Computer Science AB)
 Contributors:
 Jan Humble (Swedish Institute of Computer Science AB)

 */

package equip.ect.apps.editor;

import java.awt.Component;
import java.awt.Image;
import java.util.Calendar;

import equip.ect.apps.editor.state.EditorID;

/**
 * The class representing graphical icons for the bean canvas. Notice that origo for each icon is in
 * the center of the clipping area!
 */
public abstract class BeanCanvasItem extends InteractiveCanvasItem implements Cloneable
{

	protected boolean isAttached;

	// private final transient ComponentAdvert comp;
	protected String beanid = "";

	// private Vector properties;
	protected String name;

	protected transient Image imageBuffer;

	private EditorID editorID;

	public BeanCanvasItem(final Component canvas, final InteractiveCanvasItemView view)
	{
		super(canvas, view);
	}

	public BeanCanvasItem(final Component canvas, final InteractiveCanvasItemView view, final String beanid,
			final String name)
	{
		super(canvas, view);
		this.beanid = beanid;
		this.name = name;
		if (editorID == null)
		{
			this.editorID = EditorID.createNew();
		}
	}

	@Override
	public Object clone()
	{
		return clone(canvas);
	}

	public abstract Object clone(Component targetCanvas);

	public String getBeanID()
	{
		return beanid;
	}

	public final Image getIconView()
	{
		if (imageBuffer == null)
		{
			imageBuffer = createIconView();
		}
		return imageBuffer;
	}

	public final EditorID getID()
	{
		return this.editorID;
	}

	public final String getName()
	{
		return name;
	}

	public void setAttached(final boolean attached)
	{
		this.isAttached = attached;
	}

	public final void setID(final EditorID id)
	{
		this.editorID = id;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	protected abstract Image createIconView();

	protected void forceIconViewRepaint()
	{
		imageBuffer = createIconView();
	}

	/**
	 * It is up to each implemented subclass to define it's own idle time. Returns the idle time for
	 * this item in milliseconds. Returns -1 if this item is currently not idle.
	 */
	@Override
	protected long idleTime()
	{
		if (getSelectStatus() == InteractiveCanvasItem.SELECTED || isAttached) { return -1; }
		return Calendar.getInstance().getTimeInMillis() - startIdleTime;
	}

	protected boolean isAttachable(final BeanCanvasItem foreign)
	{
		return true;
	}

}
