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
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.apps.editor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.dataspace.DataspaceUtils;

/**
 * dialog for viewing/setting RDFStatement-specified names of things
 */
public class NameDialog extends JDialog
{
	class ActiveCellRenderer extends JLabel implements ListCellRenderer<String>
	{
		public ActiveCellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final String value, final int index,
				final boolean isSelected, final boolean cellHasFocus)
		{
			final String activeName = ((ActiveListModel) (list.getModel())).getActiveName();

			String stringValue = value;

			if (stringValue.equals(activeName))
			{
				stringValue = stringValue + " (active)";
			}

			setText(stringValue);

			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());

			}

			return this;
		}
	}

	class ActiveListModel extends DefaultListModel<String>
	{
		String activeName = null;

		synchronized String getActiveName()
		{
			return activeName;
		}

		synchronized void setActiveName(final String activeName)
		{
			this.activeName = activeName;
		}
	}

	/**
	 * dataspace
	 */
	protected DataspaceBean dataspace;

	/**
	 * component id
	 */
	protected GUID id;

	/**
	 * list
	 */
	protected ActiveListModel list;

	/**
	 * jlist
	 */
	protected JList<String> jlist;

	/**
	 * text field
	 */
	protected JTextField name;

	/**
	 * add action
	 */
	protected AbstractAction addAction;

	/**
	 * delete action
	 */
	protected AbstractAction deleteAction;

	/**
	 * update action
	 */
	protected AbstractAction updateAction;

	protected AbstractAction activeAction;

	/**
	 * cons
	 */
	public NameDialog(final JFrame owner, final DataspaceBean dataspace, final GUID id)
	{
		super(owner, "Names of " + id);
		this.dataspace = dataspace;
		this.id = id;
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		list = new ActiveListModel();
		refresh();

		jlist = new JList<String>(list);
		jlist.setCellRenderer(new ActiveCellRenderer());
		jlist.setVisibleRowCount(8);
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(jlist));
		jlist.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(final ListSelectionEvent e)
			{
				final String val = jlist.getSelectedValue();
				if (val != null)
				{
					name.setText(val);
				}
				enableActions();
			}
		});

		name = new JTextField();
		panel.add(name);
		name.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent e)
			{
				enableActions();
			}

			@Override
			public void insertUpdate(final DocumentEvent e)
			{
				enableActions();
			}

			@Override
			public void removeUpdate(final DocumentEvent e)
			{
				enableActions();
			}

		});

		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		buttons.add(new JButton(addAction = new AbstractAction("Refresh")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				refresh();
				enableActions();
			}
		}));
		buttons.add(new JButton(addAction = new AbstractAction("Add")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				addName(name.getText());
				refresh();
				enableActions();
			}
		}));
		addAction.setEnabled(false);
		buttons.add(new JButton(updateAction = new AbstractAction("Update")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				updateName(jlist.getSelectedValue(), name.getText());
				refresh();
				enableActions();
			}
		}));
		updateAction.setEnabled(false);
		buttons.add(new JButton(deleteAction = new AbstractAction("Delete")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String activeName = DataspaceUtils
						.getActiveRDFName(NameDialog.this.dataspace, NameDialog.this.id);

				if ((activeName != null) && (activeName.equals(name.getText())))
				{
					deleteActiveName(name.getText());
				}

				deleteName(name.getText());
				refresh();
				enableActions();
			}
		}));
		deleteAction.setEnabled(false);

		buttons.add(new JButton(new AbstractAction("Close")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setVisible(false);
			}
		}));

		buttons.add(new JButton(activeAction = new AbstractAction("Set active")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{

				// see if another name is currently
				// active

				final String activeName = DataspaceUtils
						.getActiveRDFName(NameDialog.this.dataspace, NameDialog.this.id);

				if (activeName == null)
				{
					addActiveName(name.getText());
				}
				else
				{
					updateActiveName(activeName, name.getText());
				}

				refresh();
				enableActions();
			}
		}));

		activeAction.setEnabled(false);

		panel.add(buttons);
		this.getContentPane().add(panel);
		this.pack();
		this.setVisible(true);
		// System.out.println("NameDialog visible");
	}

	protected void addActiveName(final String activeName)
	{
		addRDF(RDFStatement.ECT_ACTIVE_TITLE, activeName);
	}

	/**
	 * add name
	 */
	protected void addName(final String name)
	{
		addRDF(RDFStatement.DC_TITLE, name);
	}

	protected void addRDF(final String rdfPredicate, final String rdfObject)
	{
		try
		{
			final RDFStatement rdf = new RDFStatement(dataspace.allocateId(), RDFStatement.GUIDToUrl(id), rdfPredicate,
					rdfObject);

			rdf.addtoDataSpacePersistent(dataspace, null);// lease
		}
		catch (final Exception e)
		{
			System.err.println("ERROR adding name to dataspace: " + e);
			e.printStackTrace(System.err);
		}
	}

	protected void deleteActiveName(final String activeName)
	{
		deleteRDF(RDFStatement.ECT_ACTIVE_TITLE, activeName);
	}

	/**
	 * delete name
	 */
	protected void deleteName(final String name)
	{

		deleteRDF(RDFStatement.DC_TITLE, name);
	}

	protected void deleteRDF(final String rdfPredicate, final String rdfObject)
	{
		try
		{
			final RDFStatement template = new RDFStatement(null, RDFStatement.GUIDToUrl(id), rdfPredicate, rdfObject);
			final RDFStatement names[] = template.copyCollectAsRDFStatement(dataspace);
			if (names.length == 0)
			{
				System.err.println("Unable to find name " + name + " in dataspace (may have just changed?)");
				return;
			}

			dataspace.delete(names[0].getID());
		}
		catch (final Exception e)
		{
			System.err.println("ERROR deleting name from dataspace: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * enable actions
	 */
	protected void enableActions()
	{
		final String selected = jlist.getSelectedValue();
		deleteAction.setEnabled(selected != null);

		if (selected != null)
		{
			final String activeName = ((ActiveListModel) (jlist.getModel())).getActiveName();

			if (activeName == null)
			{
				activeAction.setEnabled(true);
			}
			else
			{
				if (activeName.equals(selected))
				{
					activeAction.setEnabled(false);
				}
				else
				{
					activeAction.setEnabled(true);
				}
			}
		}
		else
		{
			activeAction.setEnabled(false);
		}

		final String text = name.getText();
		if (text.length() == 0)
		{
			addAction.setEnabled(false);
			updateAction.setEnabled(false);
		}
		else
		{
			boolean novel = true;
			for (int i = 0; i < list.size(); i++)
			{
				final String s = list.elementAt(i);
				if (s.equals(text))
				{
					novel = false;
					break;
				}
			}

			addAction.setEnabled(novel);
			updateAction.setEnabled(novel && selected != null);
		}
	}

	/**
	 * refresh list
	 */
	protected void refresh()
	{
		try
		{

			final String activeName = DataspaceUtils.getActiveRDFName(dataspace, id);

			list.setActiveName(activeName);

			list.removeAllElements();
			final RDFStatement template = new RDFStatement(null, RDFStatement.GUIDToUrl(id), RDFStatement.DC_TITLE,
					null);
			final RDFStatement names[] = template.copyCollectAsRDFStatement(dataspace);
			for (final RDFStatement name2 : names)
			{
				final String name = name2.getObject();
				int j;
				for (j = 0; j < list.size(); j++)
				{
					final String el = list.elementAt(j);
					if (el.compareTo(name) > 0)
					{
						break;
					}
				}
				list.insertElementAt(name, j);
			}
		}
		catch (final Exception e)
		{
			System.err.println("NameDialog error in refresh: " + e);
			e.printStackTrace(System.err);
		}
	}

	protected void updateActiveName(final String from, final String to)
	{
		updateRDF(RDFStatement.ECT_ACTIVE_TITLE, from, to);
	}

	/**
	 * update name
	 */
	protected void updateName(final String from, final String to)
	{
		updateRDF(RDFStatement.DC_TITLE, from, to);
	}

	protected void updateRDF(final String rdfPredicate, final String objectFrom, final String objectTo)
	{
		try
		{
			final RDFStatement template = new RDFStatement(null, RDFStatement.GUIDToUrl(id), rdfPredicate, objectFrom);
			final RDFStatement names[] = template.copyCollectAsRDFStatement(dataspace);
			if (names.length == 0)
			{
				System.err.println("Unable to find name " + objectFrom + " in dataspace (may have just changed?)");
				return;
			}

			final RDFStatement rdf = new RDFStatement(names[0].getID(), RDFStatement.GUIDToUrl(id), rdfPredicate,
					objectTo);
			rdf.updateinDataSpace(dataspace);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR updating name in dataspace: " + e);
			e.printStackTrace(System.err);
		}
	}
}
