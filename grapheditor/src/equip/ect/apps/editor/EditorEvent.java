/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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
package equip.ect.apps.editor;

import java.util.EventObject;

/**
 * @author humble
 * 
 */
public class EditorEvent extends EventObject
{

	public static final int ITEM_ADDED = 0;
	public static final int ITEM_REMOVED = 1;
	public static final int LINK_ADDED = 2;
	public static final int LINK_REMOVED = 3;

	public static final int ITEM_MOVED = 5;

	protected static String typeToString(final int type)
	{
		switch (type)
		{
			case ITEM_ADDED:
				return "ITEM_ADDED";
			case ITEM_REMOVED:
				return "ITEM_REMOVED";
			case LINK_ADDED:
				return "LINK_ADDED";
			case LINK_REMOVED:
				return "LINK_REMOVED";
			default:
				return "UNKNOWN_EDITOR_EVENT_TYPE";
		}
	}

	protected final int type;

	protected final Object context;

	/**
	 * @param source
	 */
	public EditorEvent(final Object source, final int type, final Object context)
	{
		super(source);
		this.type = type;
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	public final Object getContext()
	{
		return this.context;
	}

	public final int getType()
	{
		return this.type;
	}

	public final String getTypeString()
	{
		return (typeToString(getType()));
	}

}
