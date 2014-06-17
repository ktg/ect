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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.webstart;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import equip.data.DataCallbackPostImpl;
import equip.data.DataProxy;
import equip.data.DataSession;
import equip.data.DataspaceStatusItem;
import equip.data.DataspaceStatusItemImpl;
import equip.data.EventPattern;
import equip.data.EventPatternImpl;
import equip.data.ItemBinding;
import equip.data.ItemData;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.runtime.ValueBase;

/**
 * JComponent showing if dataspace is currently connected
 */
public class DataspaceConnectedPanel extends JLabel implements DataspaceEventListener
{
	/**
	 * connected
	 */
	protected boolean connected = false;

	/**
	 * cons
	 */
	public DataspaceConnectedPanel(final DataProxy dataspace)
	{
		super("Dataspace Not Connected");
		final DataspaceStatusItem template = new DataspaceStatusItemImpl();
		// try
		{
			final DataSession session = dataspace.createSession(new DataCallbackPostImpl()
			{
				@Override
				public void notifyPost(final equip.data.Event event, final EventPattern pattern,
						final boolean patternDeleted, final DataSession session, final DataProxy dataspace,
						final ItemData oldValue, final ItemBinding oldBinding, final ValueBase closure)
				{
					dataspaceEvent(new DataspaceEvent(DataspaceConnectedPanel.this, event, pattern, patternDeleted,
							session, dataspace, oldValue, oldBinding));
				}
			}, null);
			final EventPattern pattern = new EventPatternImpl();
			// pattern.id = allocateId();
			pattern.initAsSimpleItemMonitor(template, false/* localFlag */);

			session.addPattern(pattern);

			// dataspace.addDataspaceEventListener(template, true, this);//local
		}
		/*
		 * catch (DataspaceInactiveException e) {
		 * System.err.println("ERROR setting up DataspaceConnectedPanel: "+e); }
		 */
	}

	/**
	 * notify
	 */
	@Override
	public void dataspaceEvent(final DataspaceEvent e)
	{
		DataspaceStatusItem item = (DataspaceStatusItem) e.getAddItem();
		if (item == null)
		{
			item = (DataspaceStatusItem) e.getUpdateItem();
		}
		System.out.println("Dataspace status change: " + e);
		if (item != null && item.data != null)
		{
			System.out.println("  clientconn=" + item.data.clientConnectedFlag + " server=" + item.data.serverFlag);
		}
		final boolean nowConnected = (item != null && item.data != null) && item.data.clientConnectedFlag;
		if (nowConnected == connected) { return; }
		connected = nowConnected;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				setText(connected ? "Dataspace connected" : "Dataspace not connected");
			}
		});
	}
}
