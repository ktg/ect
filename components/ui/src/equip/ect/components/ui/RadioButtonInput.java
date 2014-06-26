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
package equip.ect.components.ui;

import equip.ect.Category;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

/**
 * Test input which is a toggle button generating true/false.
 *
 * @author Chris Greenhalgh
 */
@ECTComponent
@Category("UI")
public class RadioButtonInput extends UIBase implements DynamicProperties
{
	private String buttons = "";
	private String selected = "";

	protected final DynamicPropertiesSupport dynsup;
	private Map<String, JRadioButton> radioButtons = new HashMap<String, JRadioButton>();

	/**
	 * main cons, no args.
	 */
	public RadioButtonInput()
	{
		super();

		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);

		// make GUI and show
		final Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());

		setButtons("Select1 Select2");

		frame.setVisible(true);
	}

	public String getButtons()
	{
		return buttons;
	}

	public void setButtons(String buttons)
	{
		if (this.buttons.equals(buttons))
		{
			// no change
			return;
		}
		final String oldButtons = this.buttons;
		this.buttons = buttons;

		frame.getContentPane().removeAll();
		radioButtons.clear();
		final ButtonGroup group = new ButtonGroup();
		for (DynamicPropertyDescriptor property : dynsup.dynGetProperties())
		{
			try
			{
				dynsup.removeProperty(property.getName());
			}
			catch (NoSuchPropertyException e)
			{
				e.printStackTrace();
			}
		}

		JPanel radioPanel = new JPanel(new GridLayout(0, 1));
		for (String buttonName : buttons.split(" "))
		{
			final JRadioButton radioButton = new JRadioButton(buttonName);
			radioButton.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					try
					{
						System.out.println(radioButton.getText() + " = " + radioButton.isSelected());
						dynSetProperty(radioButton.getText(), radioButton.isSelected());
					}
					catch (NoSuchPropertyException e1)
					{
						e1.printStackTrace();
					}
				}
			});
			radioPanel.add(radioButton);
			group.add(radioButton);
			radioButtons.put(buttonName, radioButton);

			dynsup.addProperty(buttonName, Boolean.class, false);

		}
		frame.add(radioPanel, BorderLayout.CENTER);
		frame.pack();

		// fire change event
		propertyChangeListeners.firePropertyChange("buttons", oldButtons, buttons);
	}

	private void selectionChanged()
	{
		String oldSelected = selected;
		for (JRadioButton button : radioButtons.values())
		{
			System.out.println(button.getText() + " = " + button.isSelected());
			if (button.isSelected())
			{
				System.out.println("Selected = " + button.getText());
				if (!button.getText().equals(selected))
				{
					selected = button.getText();
					propertyChangeListeners.firePropertyChange("selected", oldSelected, selected);
				}
				return;
			}
		}
		System.out.println("None Selected");
		selected = "";
		propertyChangeListeners.firePropertyChange("selected", oldSelected, selected);
	}

	public void setSelected(String selected)
	{
		if (this.selected.equals(selected))
		{
			JRadioButton button = radioButtons.get(selected);
			if (button != null)
			{
				button.setEnabled(true);
			}
		}
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptor}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	public String getSelected()
	{
		return selected;
	}

	/**
	 * get one property by name
	 */
	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);
		if (radioButtons.containsKey(name))
		{
			JRadioButton button = radioButtons.get(name);
			button.setSelected((Boolean) value);
			selectionChanged();
		}
	}
}
