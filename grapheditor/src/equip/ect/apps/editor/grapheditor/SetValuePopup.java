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
  Chris Greenhalgh (University of Nottingham)
  Jan Humble (University of Nottingham)

 */
package equip.ect.apps.editor.grapheditor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import equip.data.beans.DataspaceBean;
import equip.ect.ComponentProperty;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.dataspace.DataspaceUtils;

/**
 * popup dialog allowing (attempt) to set a property value by making a temporary pseudo-property,
 * and temporarily linking it to the property to be set.
 *
 * @author Chris Greenhalgh, Jan Humble
 */
class SetValuePopup extends JDialog
{
	private class PropertyInfoPanel extends JPanel
	{

		PropertyInfoPanel(final ComponentProperty targetProperty)
		{
			super(new BorderLayout());
			final JPanel leftPanel = new JPanel(new GridLayout(3, 1));
			final JPanel rightPanel = new JPanel(new GridLayout(3, 1));
			leftPanel.add(new JLabel("name: "));
			rightPanel.add(new JLabel(targetProperty.getPropertyName()));
			leftPanel.add(new JLabel("class: "));
			rightPanel.add(new JLabel(targetProperty.getPropertyClass()));

			leftPanel.add(new JLabel("writable: "));
			rightPanel.add(new JLabel(String.valueOf(!targetProperty.isReadonly())));
			add(BorderLayout.WEST, leftPanel);
			add(BorderLayout.CENTER, rightPanel);
		}

	}

	/**
	 * text size
	 */
	private static final int TEXT_ROWS = 10;
	/**
	 * text size
	 */
	private static final int TEXT_COLUMNS = 80;
	/**
	 * text area
	 */
	private JTextArea text;
	/**
	 * guid
	 */
	private ComponentProperty targetProperty;
	/**
	 * dataspace
	 */
	protected DataspaceBean dataspace;

	/**
	 * cons - property GUID and initial value
	 */

	private boolean ignoreEscChar = false;

	SetValuePopup(final Frame owner, final DataspaceBean dataspace, final ComponentProperty targetProperty)
	{
		super(owner, "Property: " + targetProperty.getPropertyName());
		this.targetProperty = targetProperty;
		this.dataspace = dataspace;
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		text = new JTextArea(TEXT_ROWS, TEXT_COLUMNS);
		panel.add(BorderLayout.NORTH, new PropertyInfoPanel(targetProperty));
		panel.add(new JScrollPane(text), BorderLayout.CENTER);
		final String val = DataspaceUtils.getPropValueAsString(targetProperty, dataspace);
		text.setText(val);
		if (val.length() > 0)
		{
			text.select(0, val.length());
		}
		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout());
		if (targetProperty.isReadonly())
		{
			text.setEditable(false);
		}
		else
		{
			buttons.add(new JButton(new AbstractAction("Set")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					setValue(text.getText());
					setVisible(false);
					// dispose();
				}
			}));
		}
		buttons.add(new JButton(new AbstractAction("Cancel")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setVisible(false);
				// dispose();
			}
		}));
		final JPanel bottomPanel = new JPanel(new BorderLayout());

		final JCheckBox ignoreEscCharCB = new JCheckBox("Ignore escape characters", ignoreEscChar);
		ignoreEscCharCB.addActionListener(arg0 -> ignoreEscChar = ignoreEscCharCB.isSelected());
		bottomPanel.add(BorderLayout.NORTH, ignoreEscCharCB);
		bottomPanel.add(BorderLayout.SOUTH, buttons);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		getContentPane().add(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		System.out.println("Dialog visible");
	}

	protected void setValue(final String val)
	{
		String newVal = val;
		if (this.ignoreEscChar)
		{
			newVal = val.replaceAll("\\\\", "\\\\\\\\");
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		DataspaceMonitor.getMonitor().setProperty(targetProperty, newVal);
		setCursor(Cursor.getDefaultCursor());
	}
}
