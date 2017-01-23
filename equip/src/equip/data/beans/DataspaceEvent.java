/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

*/
/* Chris Greenhalgh
 * 26 Sept 2002
 */

package equip.data.beans;

import equip.data.*;

/**
 * representation of an event in the dataspace as returned to
 * client, e.g. using DataCallbackPost.
 * <p>
 * inherits java.lang.Object source;
 */
public class DataspaceEvent extends java.util.EventObject
{
	/**
	 * native dataspace event
	 */
	protected equip.data.Event event;

	/**
	 * native event pattern
	 */
	protected equip.data.EventPattern pattern;

	/**
	 * was the associated pattern deleted as a result?
	 */
	protected boolean patternDeleted;

	/**
	 * native DataSession
	 */
	protected equip.data.DataSession session;

	/**
	 * native dataspace
	 */
	protected equip.data.DataProxy dataspace;

	/**
	 * old value of item, if any
	 */
	protected equip.data.ItemData oldValue;

	/**
	 * old binding of item, if any
	 */
	private equip.data.ItemBinding oldBinding;

	/**
	 * source-only constructor
	 */
	public DataspaceEvent(java.lang.Object source)
	{
		super(source);
	}

	/**
	 * full constructor
	 */
	public DataspaceEvent(java.lang.Object source,
	                      equip.data.Event event,
	                      equip.data.EventPattern pattern,
	                      boolean patternDeleted,
	                      equip.data.DataSession session,
	                      equip.data.DataProxy dataspace,
	                      equip.data.ItemData oldValue,
	                      equip.data.ItemBinding oldBinding)
	{
		super(source);
		this.event = event;
		this.pattern = pattern;
		this.patternDeleted = patternDeleted;
		this.session = session;
		this.dataspace = dataspace;
		this.oldValue = oldValue;
		this.oldBinding = oldBinding;
	}

	/**
	 * getter
	 */
	public equip.data.Event getEvent()
	{
		return event;
	}

	/**
	 * getter
	 */
	public equip.data.EventPattern getEventPattern()
	{
		return pattern;
	}

	/**
	 * getter
	 */
	public boolean getPatternDeleted()
	{
		return patternDeleted;
	}

	/**
	 * getter
	 */
	public equip.data.DataSession getSession()
	{
		return session;
	}

	/**
	 * getter
	 */
	public equip.data.DataProxy getDataspace()
	{
		return dataspace;
	}

	/**
	 * getter
	 */
	public equip.data.ItemData getOldValue()
	{
		return oldValue;
	}

	/**
	 * getter
	 */
	public equip.data.ItemBinding getOldBinding()
	{
		return oldBinding;
	}

	/**
	 * convenience getter for item if add event (else null)
	 */
	public equip.data.ItemData getAddItem()
	{
		if (event != null && (event instanceof AddEvent))
		{
			AddEvent add = (AddEvent) event;
			if (add.binding != null)
			{
				return add.binding.item;
			}
		}
		return null;
	}

	/**
	 * convenience getter for item if update event (else null)
	 */
	public equip.data.ItemData getUpdateItem()
	{
		if (event != null && (event instanceof UpdateEvent))
		{
			UpdateEvent upd = (UpdateEvent) event;
			return upd.item;
		}
		return null;
	}

	/**
	 * convenience getter for id if delete event (else null)
	 */
	public equip.data.GUID getDeleteId()
	{
		if (event != null && (event instanceof DeleteEvent))
		{
			DeleteEvent del = (DeleteEvent) event;
			return del.id;
		}
		return null;
	}
}
/* EOF */

      
