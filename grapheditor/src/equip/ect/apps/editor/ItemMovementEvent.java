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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone
 Jan Humble (University of Nottingham)

 */
package equip.ect.apps.editor;

import java.util.EventObject;

/**
 * An event representing the movement of a set of items
 */
public class ItemMovementEvent extends EventObject
{

	public static final int ITEM_MOVE_INITIATED = 0;
	public static final int ITEM_MOVE_CONTINUED = 1;

	protected static String typeToString(final int type)
	{
		switch (type)
		{
			case ITEM_MOVE_INITIATED:
				return "ITEM_MOVE_INITIATED";
			case ITEM_MOVE_CONTINUED:
				return "ITEM_MOVE_CONTINUED";
			default:
				return "UNKNOWN_EDITOR_EVENT_TYPE";
		}
	}

	protected int type;

	protected Object context;

	/**
	 * @param source
	 */
	public ItemMovementEvent(final Object source, final int type, final Object context)
	{
		super(source);
		this.type = type;
		this.context = context;
	}

	public Object getContext()
	{
		return this.context;
	}

	public int getType()
	{
		return this.type;
	}

	public String getTypeString()
	{
		return (typeToString(getType()));
	}

	public void setContext(final Object context)
	{
		this.context = context;
	}
}
