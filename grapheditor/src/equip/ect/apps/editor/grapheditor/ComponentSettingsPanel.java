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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.apps.editor.grapheditor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import equip.data.GUID;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.dataspace.ComponentMetadataListener;
import equip.ect.apps.editor.dataspace.ComponentPropertyListener;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.dataspace.DataspaceUtils;

public class ComponentSettingsPanel extends JPanel implements ComponentPropertyListener, ComponentMetadataListener
{
	private class PropertyValueSetAction extends AbstractAction
	{
		final ComponentProperty prop;

		final JTextField tf;

		PropertyValueSetAction(final ComponentProperty prop, final JTextField tf)
		{
			this.prop = prop;
			this.tf = tf;
		}

		@Override
		public void actionPerformed(final ActionEvent ae)
		{
			final String value = tf.getText();
			DataspaceMonitor.getMonitor().setProperty(prop, value);
		}
	}

	private Map<String, JTextField> propFields = new HashMap<String, JTextField>();

	private final ComponentAdvert compAdv;

	private JTextField tf;

	private final String NONE_SET = "<none set>";

	public ComponentSettingsPanel(final GraphComponent graphComponent)
	{
		super(new BorderLayout());

		DataspaceMonitor.getMonitor().addComponentMetadataListener(this);

		// System.out.println("called settings constructor");
		this.compAdv = graphComponent.getComponentAdvert();
		final String beanid = compAdv.getID().toString();
		final Map<String, ComponentProperty> propsH = DataspaceMonitor.getMonitor().getComponentProperties().get(beanid);
		final ComponentProperty[] props = propsH.values().toArray(new ComponentProperty[ propsH.size()]);
		final JPanel topPanel = new JPanel(new BorderLayout());
		JPanel p = new JPanel();

		topPanel.add(BorderLayout.NORTH, p);
		p = new JPanel(new GridLayout(4, 1));
		p.add(new JLabel("Name: " + DataspaceUtils.getCurrentName(compAdv), SwingConstants.CENTER));		
		p.add(new JLabel("Equip ID: " + compAdv.getID().toString(), SwingConstants.CENTER));
		p.add(new JLabel("Host ID: " + compAdv.getHostID().toString(), SwingConstants.CENTER));

		p.add(new JLabel("Capability name: " + DataspaceUtils.getCapabilityDisplayName(compAdv.getCapabilityID()), SwingConstants.CENTER));

		topPanel.add(BorderLayout.SOUTH, p);
		add(BorderLayout.NORTH, topPanel);

		if (props != null)
		{
			Arrays.sort(props, (cp1, cp2) -> cp1.getPropertyName().compareTo(cp2.getPropertyName()));

			final JPanel propNamesPanel = new JPanel(new GridLayout(props.length, 1));
			// JPanel propsPanel = new JPanel(new GridLayout(props.size(), 2));
			final JPanel propFieldsPanel = new JPanel(new GridLayout(props.length, 1));
			for (final ComponentProperty cp : props)
			{
				p = new JPanel();
				propNamesPanel.add(new JLabel(cp.getPropertyName()));
				final JTextField propField = new JTextField(DataspaceUtils.getPropValueAsString(cp), 20);
				p.add(propField);
				final JButton b = new JButton("set");

				b.setFont(b.getFont().deriveFont(10.0f));
				if (cp.isReadonly())
				{
					b.setEnabled(false);
					propField.setEditable(false);
				}
				else
				{
					b.addActionListener(new PropertyValueSetAction(cp, propField));
				}
				p.add(b);

				final GraphComponentProperty gcp = graphComponent.getGraphComponentProperty(cp.getID().toString());
				final JCheckBox cb = new JCheckBox("Keep visible", gcp.keepVisible());
				cb.addActionListener(ae -> gcp.setKeepVisible(cb.isSelected()));
				p.add(cb);

				propFieldsPanel.add(p);
				propFields.put(cp.getID().toString(), propField);
			}
			final JPanel propsPanel = new JPanel(new BorderLayout());
			propsPanel.add(BorderLayout.WEST, propNamesPanel);
			propsPanel.add(BorderLayout.EAST, propFieldsPanel);
			add(BorderLayout.CENTER, new JScrollPane(propsPanel));
		}
		if (CompoundComponentEditor.isCompoundComponent(compAdv))
		{
			// nasty hack to see if it seems to be a compound component
			final JPanel bp = new JPanel();
			bp.setLayout(new FlowLayout());
			bp.add(new JButton(new AbstractAction("Delete compound component")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					CompoundComponentEditor.deleteCompoundComponent(compAdv.getID());
				}
			}));
			add(BorderLayout.SOUTH, bp);
		}

		// System.out.println("end of constructor");
	}

	@Override
	public void componentMetadataAdded(final Object ob)
	{
		componentMetadataChanged(ob);
	}

	@Override
	public void componentMetadataDeleted(final Object ob)
	{
		componentMetadataChanged(ob);
	}

	@Override
	public void componentMetadataUpdated(final Object ob)
	{
		componentMetadataChanged(ob);
	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
	}

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
	}

	public void componentPropertyUpdated(final ComponentProperty compProp)
	{
		final JTextField tf = propFields.get(compProp.getID().toString());
		if (tf != null)
		{
			tf.setText(DataspaceUtils.getPropValueAsString(compProp));
		}
	}

	protected void componentMetadataChanged(final Object metadata)
	{
		if (metadata instanceof RDFStatement)
		{
			final RDFStatement rdf = (RDFStatement) metadata;
			if (rdf.getPredicate().equals(RDFStatement.ECT_ACTIVE_TITLE))
			{
				processActiveNameChange(rdf);
			}
		}
	}

	protected void processActiveNameChange(final RDFStatement rdf)
	{
		// System.out.println("process name change");

		final GUID beanid = RDFStatement.urlToGUID(rdf.getSubject());

		final ComponentAdvert compAdv = DataspaceMonitor.getMonitor().getComponentAdvert(beanid.toString());

		if (compAdv != null)
		{
			// get the current name

			String activeName = DataspaceUtils.getActiveRDFName(DataspaceMonitor.getMonitor().getDataspace(),
																compAdv.getComponentID());

			if (activeName == null)
			{
				activeName = NONE_SET;
			}

			tf.setText(activeName);
		}
	}
}
