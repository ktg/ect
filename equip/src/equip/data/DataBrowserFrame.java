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
package equip.data;

import equip.runtime.ValueBase;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

public class DataBrowserFrame extends JFrame
{
	Vector<ItemData> itemList = new Vector<ItemData>();
	JPanel contentPane;
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane1 = new JScrollPane();
	JPanel jPanel1 = new JPanel();
	JButton quit = new JButton();
	JList<ItemData> jList1 = new JList<ItemData>();

	/**
	 * Construct the frame
	 */
	EquipConnector ec;

	public DataBrowserFrame(EquipConnector equipcon)
	{
		ec = equipcon;

		DataSession session = ec.dataservice.createSession(new EventHandler(), null);
		EventPattern pattern = new EventPatternImpl();
		pattern.id = ec.idFactory.getUnique();
		equip.data.ItemData item = new equip.data.ItemDataImpl();
		pattern.initAsSimpleItemMonitor(item, false);

		session.addPattern(pattern);

		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Component initialization
	 */
	private void jbInit() throws Exception
	{
		//setIconImage(Toolkit.getDefaultToolkit().createImage(DataBrowserFrame.class.getResource("[Your Icon]")));
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setSize(new Dimension(400, 300));
		this.setTitle("Equip Beans Browser");
		quit.setText("Quit");
		quit.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});

		// The main list stuff
		// jList1.setCellRenderer(new BeanCellRender());

		//specialised mousehandler for doubleclicks
		jList1.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					int index =
							jList1.locationToIndex(e.getPoint());
					jList1.setSelectedIndex(index);
					Object item = jList1.getSelectedValue();

					if (item != null)
					{
						ItemFrame dataframe = new ItemFrame(item);
						dataframe.setTitle("Item: " + item);
						dataframe.pack();
						dataframe.setVisible(true);
					}
				}
			}
		});

		contentPane.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jList1, null);
		contentPane.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(quit, null);
	}

	/**
	 * Overridden so we can exit when window is closed
	 */
	protected void processWindowEvent(WindowEvent e)
	{
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			System.exit(0);
		}
	}

	// ======== Inner Class to act as a Generic Event Handler ======================
	class EventHandler extends DataCallback
	{
		public void notify(equip.data.Event event, EventPattern pattern,
		                   boolean patternDeleted,
		                   DataSession session,
		                   ValueBase closure)
		{
			System.out.println("++ main notify..." + event);

			if (event instanceof AddEvent)
			{
				AddEvent add = (AddEvent) event;
				if (add.binding.item == null)
				{
					return; // Should not occur
				}
				itemList.add(add.binding.item); // Add to the listing
				jList1.setListData(itemList);
				//jList1.repaint(); // redraw the list
			}

			if (event instanceof DeleteEvent)
			{
				DeleteEvent del = (DeleteEvent) event;

				if (ec.dataservice.getItem(del.id) == null)
				{
					return; // Should not occur
				}
				itemList.remove(ec.dataservice.getItem(del.id)); // Remove to the listing
				jList1.setListData(itemList);
				//jList1.repaint(); // redraw the list
			}

		}
	}
}