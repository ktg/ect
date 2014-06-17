package equip.ect.components.functionfactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import equip.ect.NoSuchPropertyException;

public class FunctionComponentModificationFrame extends JFrame implements ActionListener, PropertyChangeListener
{
	private FunctionComponent fc;
	private FunctionPreviewPanel fpp;
	private Function f;

	private static final Dimension DEFAULT_DIMENSION = new Dimension(600, 400);
	private static final String UPDATE_TEXT = "Update component";
	private static final String CLOSE_TEXT = "Close";

	private String[] propertyNames;
	private double[] currentPropertyValues;

	private Hashtable hashFromPropertyNameToTextField = new Hashtable();

	public FunctionComponentModificationFrame(final FunctionComponent fc)
	{
		setDefaultCloseOperation(HIDE_ON_CLOSE);

		this.fc = fc;
		fc.addPropertyChangeListener(this);

		getContentPane().setLayout(new BorderLayout());

		// first, produce an inital rendering of the
		// function represented by this component

		f = fc.retrieveFunction();

		fpp = new FunctionPreviewPanel();
		fpp.renderFunction(f);

		getContentPane().add(fpp, BorderLayout.CENTER);

		// now add text boxes that contain current
		// component parameters, and which allow the modification
		// of these parameters

		propertyNames = f.getPropertyNames();
		currentPropertyValues = f.getPropertyValues();

		final JPanel propertyPanel = new JPanel();

		final GridBagLayout gbl = new GridBagLayout();

		final GridBagConstraints c1 = new GridBagConstraints();
		c1.weightx = 1.0;
		c1.ipady = 5;
		c1.fill = GridBagConstraints.NONE;

		final GridBagConstraints c2 = new GridBagConstraints();
		c2.ipady = 5;
		c2.weightx = 4.0;
		c2.gridwidth = GridBagConstraints.REMAINDER;
		c2.fill = GridBagConstraints.HORIZONTAL;

		propertyPanel.setLayout(gbl);

		final String formula = f.getFunctionFormula();

		final boolean parametersAreModifiable = f.parametersAreModifiable();

		if (formula != null)
		{
			final JLabel formulaLable = new JLabel("formula");
			final JTextField formulaField = new JTextField(formula);

			formulaField.setEnabled(false);
			formulaField.setDisabledTextColor(Color.BLACK);

			gbl.setConstraints(formulaLable, c1);
			gbl.setConstraints(formulaField, c2);

			propertyPanel.add(formulaLable);
			propertyPanel.add(formulaField);
		}

		// add fields gathering parameter values specified
		// by function

		if ((propertyNames != null) && (currentPropertyValues != null))
		{
			for (int i = 0; i < propertyNames.length; i++)
			{
				final JLabel lineLabel = new JLabel(propertyNames[i]);
				final JTextField lineField = new JTextField(currentPropertyValues[i] + "");

				if (!parametersAreModifiable)
				{
					lineField.setEnabled(false);
					lineField.setDisabledTextColor(Color.BLACK);
				}

				gbl.setConstraints(lineLabel, c1);
				gbl.setConstraints(lineField, c2);

				propertyPanel.add(lineLabel);
				propertyPanel.add(lineField);

				hashFromPropertyNameToTextField.put(propertyNames[i], lineField);
			}
		}

		final JPanel buttonPanel = new JPanel();

		if ((propertyNames != null) && (propertyNames.length != 0) && (parametersAreModifiable))
		{
			// only add an update button if there are some properties,
			// and if they are modifiable - otherwise there will be no
			// changes to the component to update

			final JButton updateButton = new JButton(UPDATE_TEXT);
			updateButton.addActionListener(this);
			buttonPanel.add(updateButton);
		}

		final JButton closeButton = new JButton(CLOSE_TEXT);
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);

		final GridBagConstraints c3 = new GridBagConstraints();
		c3.gridwidth = GridBagConstraints.REMAINDER;
		c3.fill = GridBagConstraints.NONE;
		c3.ipady = 10;

		gbl.setConstraints(buttonPanel, c3);
		propertyPanel.add(buttonPanel);

		getContentPane().add(propertyPanel, BorderLayout.SOUTH);

		setTitle("Modify component parameters: " + fc.getDescription());
		setSize(DEFAULT_DIMENSION);

		setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		final String actionCommand = e.getActionCommand();

		if (actionCommand.equals(CLOSE_TEXT))
		{
			dispose();
			return;
		}

		if (actionCommand.equals(UPDATE_TEXT))
		{
			// update component properties and
			// re-render example plot

			processUpdate();
		}
	}

	public void disposeYourself()
	{
		// called to force frame to close any resources that it is using

		fc.removePropertyChangeListener(this);
		dispose();
	}

	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
		// called when a property on the component changes

		final String propertyName = e.getPropertyName();
		final Object value = e.getNewValue();

		boolean functionPropertyChanged = false;

		// work out what property has been modified, and if it
		// affects function

		for (int i = 0; i < propertyNames.length; i++)
		{
			if (propertyName.equals(propertyNames[i]))
			{
				functionPropertyChanged = true;
				final JTextField propertyField = (JTextField) (hashFromPropertyNameToTextField.get(propertyName));

				final double doubleValue = ((Double) value).doubleValue();

				propertyField.setText(doubleValue + "");
				currentPropertyValues[i] = doubleValue;
				break;
			}
		}

		if (functionPropertyChanged == true)
		{
			fpp.renderFunction(f);
		}
	}

	private synchronized void processUpdate()
	{
		try
		{
			String validateError = null;

			final double[] newValues = new double[propertyNames.length];
			boolean newValueFound = false;

			// first, get all the values specified in properties

			for (int i = 0; i < propertyNames.length; i++)
			{
				final JTextField field = (JTextField) (hashFromPropertyNameToTextField.get(propertyNames[i]));

				final String stringValue = field.getText().trim();

				final double value = (new Double(stringValue)).doubleValue();

				newValues[i] = value;

				if (newValues[i] == currentPropertyValues[i])
				{
					newValueFound = true;
				}
			}

			if (newValueFound)
			{
				if ((validateError = f.validateProperties(newValues)) != null)
				{
					JOptionPane.showMessageDialog(this, validateError, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				for (int i = 0; i < newValues.length; i++)
				{
					// if property value has changed

					if (newValues[i] != currentPropertyValues[i])
					{
						fc.dynSetProperty(propertyNames[i], new Double(newValues[i]));
					}
				}

				currentPropertyValues = newValues;

				// fpp.renderFunction(f);
			}
		}
		catch (final NumberFormatException f)
		{
			JOptionPane
					.showMessageDialog(	this,
										"You have entered a non-numeric value into one of the function property specifiers.",
										"Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (final NoSuchPropertyException f)
		{
			// should never happen!
		}
	}
}
