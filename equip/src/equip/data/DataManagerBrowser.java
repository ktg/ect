/*
<COPYRIGHT>

Copyright (c) 2003-2005, University of Nottingham
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

import equip.runtime.SingletonManager;
import equip.runtime.ValueBase;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Browser for {@link DataManager} to monitor active dataspaces in process.
 */
public class DataManagerBrowser
{

	DataManager manager;
	AbstractTableModel dataModel;
	DataManager.DataspaceInfo[] dsinfos;
	Thread updateThread;

	boolean seenServerFlag;
	int seenCount;
	boolean clientConnectedFlag;

	GUIDFactory guidFactory = (GUIDFactory) SingletonManager.get("equip.data.GUIDFactoryImpl");
	MyDataCallback callback = new MyDataCallback();

	public DataManagerBrowser(DataManager manager)
	{
		System.err.println("New DataManagerBrowser");
		this.manager = manager;
		makeGui();
	}

	void makeGui()
	{
		JFrame frame = new JFrame("DataManagerBrowser");

		dataModel = new AbstractTableModel()
		{
			public int getColumnCount()
			{
				return 4;
			}

			public String getColumnName(int col)
			{
				if (col == 0)
				{
					return "Dataspace URL";
				}
				if (col == 1)
				{
					return "UseCount";
				}
				if (col == 2)
				{
					return "Type";
				}
				if (col == 3)
				{
					return "Active";
				}
				if (col == 4)
				{
					return "BackgroundActivate";
				}
				return "?";
			}

			public int getRowCount()
			{
				return dsinfos == null ? 0 : dsinfos.length;
			}

			public java.lang.Object getValueAt(int row, int col)
			{
				if (col == 0)
				{
					return dsinfos[row].name;
				}
				else if (col == 1)
				{
					return "" + dsinfos[row].refCount;
				}
				else if (col == 2)
				{
					if (dsinfos[row].type == DataManager.Type.CLIENT)
					{
						return "client";
					}
					else if (dsinfos[row].type == DataManager.Type.SERVER)
					{
						return "server";
					}
					else if (dsinfos[row].type == DataManager.Type.PEER)
					{
						return "peer";
					}
					else
					{
						return "?" + dsinfos[row].type;
					}
				}
				else if (col == 3)
				{
					// check in dataspace for DataspaceStatusItem w. serverFlag
					DataspaceStatusItem template = new DataspaceStatusItemImpl();
					seenServerFlag = false;
					seenCount = 0;
					clientConnectedFlag = false;

					DataSession session = dsinfos[row].dataspace.createSession(callback, null);
					EventPattern pattern = new EventPatternImpl();
					pattern.id = guidFactory.getUnique();
					pattern.initAsSimpleCopyCollect(template, true);
					session.addPattern(pattern);
					dsinfos[row].dataspace.deleteSession(session);

					if (dsinfos[row].type == DataManager.Type.CLIENT)
					{
						return seenServerFlag ? "conn(server)" :
								((seenCount > 1 || clientConnectedFlag) ? "conn(client)" : "disconn");
					}
					else if (dsinfos[row].type == DataManager.Type.SERVER)
					{
						return "server (" + (seenCount - 1) + " clients)";
					}
					else if (dsinfos[row].type == DataManager.Type.PEER)
					{
						return "peer (" + (seenCount - 1) + " peers)";
					}
					else
					{
						return "?";
					}
				}
				else if (col == 4)
				{
					return "?"; // ....
				}
				else
				{
					return "?";
				}
			}
		};
		JTable table = new JTable(dataModel);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				//Ignore extra messages.
				if (e.getValueIsAdjusting())
				{
					return;
				}

				ListSelectionModel lsm =
						(ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
				{
					//no rows are selected
				}
				else
				{
					int selectedRow = lsm.getMinSelectionIndex();
					System.err.println("Open in-process browser on DS " + dsinfos[selectedRow].name + "...");
					Browser2.openBrowser(dsinfos[selectedRow].name, dsinfos[selectedRow].dataspace);
				}
			}
		});


		JScrollPane scrollpane = new JScrollPane(table);
		JButton refresh = new JButton("Force refresh");
		refresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (updateThread != null)
				{
					updateThread.interrupt();
				}
			}
		});
		frame.getContentPane().add(refresh, BorderLayout.SOUTH);
		frame.getContentPane().add(scrollpane, BorderLayout.CENTER);
		frame.getContentPane().setSize(400, 400);
		frame.pack();
		frame.setVisible(true);

		Runnable task = new Runnable()
		{
			public void run()
			{
				try
				{
					synchronized (manager)
					{
						while (true)
						{
							try
							{
								final DataManager.DataspaceInfo[] infos = manager.getDataspaceInfos();
								SwingUtilities.invokeLater(new Runnable()
								{
									public void run()
									{
										updateInfos(infos);
									}
								});
								manager.wait(5000);
							}
							catch (InterruptedException e)
							{
								System.err.println("DataManagerBrowser update thread interrupted");
							}
						}
					}//synchronized
				}
				catch (Exception e)
				{
					System.err.println("Error in DataManagerBrowser update thread: " + e);
					e.printStackTrace(System.err);
				}
			}
		};
		updateThread = new Thread(task);
		updateThread.start();
	}

	void updateInfos(DataManager.DataspaceInfo[] infos)
	{
		dsinfos = infos;
		dataModel.fireTableDataChanged();
	}

	class MyDataCallback extends DataCallback
	{
		public void notify(Event event, EventPattern pattern,
		                   boolean patternDeleted,
		                   DataSession session,
		                   ValueBase closure)
		{
			if (!(event instanceof AddEvent))
			{
				return;
			}
			AddEvent add = (AddEvent) event;
			if (add.binding == null ||
					add.binding.item == null ||
					!(add.binding.item instanceof DataspaceStatusItem))
			{
				return;
			}
			DataspaceStatusItem dsitem = (DataspaceStatusItem) add.binding.item;
			if (dsitem.data == null)
			{
				return;
			}
			seenCount++;
			if (dsitem.data.serverFlag)
			{
				seenServerFlag = true;
			}
			if (dsitem.data.clientConnectedFlag)
			{
				clientConnectedFlag = true;
			}
		}
	}
	// ....
}
