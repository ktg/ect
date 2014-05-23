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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.Capability;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;

public class PropertyLinkBrowser extends BasicPanel implements MouseListener, DataspaceConfigurationListener
{

	private final PropertyLinkTableModel linkTableModel;

	private final JTable linkTable;

	private List<PropertyLinkRequest> links;

	public PropertyLinkBrowser()
	{
		super("Property Link Browser");
		links = DataspaceMonitor.getMonitor().getPropertyLinks();
		if (links == null)
		{
			links = new ArrayList<PropertyLinkRequest>();
		}
		linkTableModel = new PropertyLinkTableModel(links);
		linkTable = new JTable(linkTableModel);
		final JScrollPane scrollPane = new JScrollPane(linkTable);
		scrollPane.setViewportView(linkTable);
		scrollPane.setColumnHeaderView(linkTable.getTableHeader());
		add(scrollPane);
		linkTable.addMouseListener(this);
	}

	@Override
	public void capabilityAdded(final Capability cap)
	{
	}

	@Override
	public void capabilityDeleted(final Capability cap)
	{
	}

	@Override
	public void capabilityUpdated(final Capability cap)
	{
	}

	@Override
	public void componentRequestAdded(final ComponentRequest compReq)
	{
	}

	@Override
	public void componentRequestDeleted(final ComponentRequest compReq)
	{
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(final MouseEvent e)
	{
	}

	@Override
	public void mouseExited(final MouseEvent e)
	{
	}

	@Override
	public void mousePressed(final MouseEvent e)
	{
		handle(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		handle(e);
	}

	@Override
	public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
	{
		links.add(linkReq);
		linkTableModel.fireTableDataChanged();
	}

	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
		links.remove(linkReq);
		linkTableModel.fireTableDataChanged();
	}

	@Override
	public void propertyLinkRequestUpdated(final PropertyLinkRequest linkReq)
	{

	}

	protected void handle(final MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			final JPopupMenu popup = new JPopupMenu();
			popup.add(new AbstractAction("Refresh")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					// refreshLinkTable();
				}
			});
			popup.add(new AbstractAction("Delete selected links")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					removeLink();
				}
			});
			popup.show(linkTable, e.getX(), e.getY());
		}
	}

	private void removeLink()
	{
		System.out.println("Remove link...");
		final int rows[] = linkTable.getSelectedRows();
		setPreferredSize(new Dimension(400, 300));

		for (final int row : rows)
		{
			final GUID linkId = (links.get(row)).getID();
			System.out.println("-> delete link " + linkId);
			try
			{
				DataspaceMonitor.getMonitor().getDataspace().delete(linkId);
			}
			catch (final DataspaceInactiveException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}

class PropertyLinkTableModel extends AbstractTableModel
{

	private List<PropertyLinkRequest> links;

	private final String[] headers = { "From Component", "Property", "To Component", "Property" };

	PropertyLinkTableModel(final List<PropertyLinkRequest> links)
	{
		this.links = links;
	}

	@Override
	public int getColumnCount()
	{
		return headers.length;
	}

	@Override
	public String getColumnName(final int column)
	{
		return headers[column];
	}

	@Override
	public int getRowCount()
	{
		return links.size();
	}

	@Override
	public Object getValueAt(final int row, final int column)
	{
		final PropertyLinkRequest linkReq = (PropertyLinkRequest) links.get(row);
		if (linkReq == null) { return ""; }

		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();

		switch (column)
		{
			case 0:
				return DataspaceUtils.getDisplayString(	dataspace,
														RDFStatement.GUIDToUrl(linkReq.getSourceComponentID()));
			case 1:
				return DataspaceUtils.getDisplayString(dataspace, RDFStatement.GUIDToUrl(linkReq.getSourcePropID()));
			case 2:
				return DataspaceUtils.getDisplayString(dataspace, RDFStatement.GUIDToUrl(linkReq.getDestComponentID()));
			case 3:
				return DataspaceUtils.getDisplayString(	dataspace,
														RDFStatement.GUIDToUrl(linkReq.getDestinationPropID()));
		}

		return "";
	}
}
