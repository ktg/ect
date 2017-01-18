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

 Created by: Chris Greenhalgh (University of Nottingham)
 Contributors:

 */
package equip.ect.apps.editor.grapheditor;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import equip.data.GUID;
import equip.data.ItemData;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ConnectionPointTypeException;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;

/**
 * compound component editor - test/demo
 */
class CompoundComponentEditor extends JFrame
{
	static void createCompoundComponent(final String name)
	{
		System.out.println("Create compound component \"" + name + "\"");
		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
		final GUID id = dataspace.allocateId();
		final ComponentAdvert ad = new ComponentAdvert(id);
		ad.setComponentName(name);
		ad.setComponentID(id);
		ad.setHostID(ComponentAdvert.COMPOUND_COMPONENT_HOST_ID);
		// host, container, capability
		try
		{
			dataspace.addPersistent(ad.tuple, null);
		}
		catch (final DataspaceInactiveException e)
		{
		}
	}

	private static void createCompoundComponentProperty(final GUID compId, final String name, final ComponentProperty refProp)
	{
		System.out.println("Create compound component property \"" + name + "\" -> " + refProp.getID());
		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
		final GUID id = dataspace.allocateId();
		final ComponentProperty prop = new ComponentProperty(id);
		prop.setPropertyName(name);
		prop.setConnectionPointType(ComponentProperty.CONNECTION_POINT_PROPERTY_REFERENCE);
		prop.setPropertyClass(refProp.getPropertyClass());
		prop.setComponentID(compId);
		try
		{
			prop.setPropertyReference(refProp.getID());
		}
		catch (final ConnectionPointTypeException e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
			return;
		}
		prop.setReadonly(refProp.isReadonly());
		try
		{
			dataspace.addPersistent(prop.tuple, null);
		}
		catch (final DataspaceInactiveException e)
		{
		}
	}

	static void deleteCompoundComponent(final GUID id)
	{
		System.out.println("Delete compound component " + id + "...");
		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
		try
		{
			dataspace.delete(id);

			final ComponentProperty prop = new ComponentProperty((GUID) null);
			prop.setComponentID(id);
			final ItemData[] props = dataspace.copyCollect(prop.tuple);
			for (final ItemData prop2 : props)
			{
				System.err.println("- delete compound component property " + prop2.id);
				dataspace.delete(prop2.id);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR deleting compound component " + id + ": " + e);
			e.printStackTrace(System.err);
		}
	}

	static void deleteProperty(final GUID id)
	{
		System.out.println("Delete compound component property " + id + "...");
		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
		try
		{
			dataspace.delete(id);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR deleting compound component property " + id + ": " + e);
			e.printStackTrace(System.err);
		}
	}

	static void handleAddProperty(final ComponentProperty prop)
	{
		// list of compound components
		final ComponentAdvert ad = new ComponentAdvert((GUID) null);
		ad.setHostID(ComponentAdvert.COMPOUND_COMPONENT_HOST_ID);
		final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
		try
		{
			final ItemData items[] = dataspace.copyCollect(ad.tuple);
			if (items.length == 0)
			{
				JOptionPane.showMessageDialog(GraphEditor.instance, "No compound components found");
				return;
			}
			final Vector<String> names = new Vector<>();
			for (final ItemData item : items)
			{
				final ComponentAdvert a = new ComponentAdvert((TupleImpl) item);
				names.add(a.getComponentName() + " " + a.getID().toString());
			}

			final JDialog d = new JDialog(GraphEditor.instance, "Add property to compount component", true);
			final JPanel p = new JPanel();
			p.setLayout(new GridLayout(5, 1));
			p.add(new JLabel("Compound Component:"));
			final JComboBox<String> choice = new JComboBox<>(names);
			choice.setEditable(false);
			p.add(choice);
			p.add(new JLabel("Property Name:"));
			final JTextField name = new JTextField(20);
			name.setText(prop.getPropertyName());
			p.add(name);
			final JPanel bp = new JPanel();
			bp.setLayout(new FlowLayout());
			bp.add(new JButton(new AbstractAction("OK")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					final String n = name.getText();
					final int ix = choice.getSelectedIndex();

					createCompoundComponentProperty(items[ix].id, n, prop);
					d.setVisible(false);
				}
			}));
			bp.add(new JButton(new AbstractAction("Cancel")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					d.setVisible(false);
				}
			}));
			p.add(bp);
			d.setContentPane(p);
			d.pack();
			d.setVisible(true);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR adding property: " + e);
			e.printStackTrace(System.err);
		}
	}

	static boolean isCompoundComponent(final ComponentAdvert ad)
	{
		return ad.getHostID().equals(ComponentAdvert.COMPOUND_COMPONENT_HOST_ID);
	}
}
