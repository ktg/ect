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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.functionfactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.DecimalFormat;

import equip.data.DictionaryImpl;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;

public class FunctionComponent implements Serializable, DynamicProperties
{
	protected String persistentChild;
	protected DictionaryImpl input = DictionaryUtilities.constructDictionary(0.0, null);

	protected DictionaryImpl output;

	protected String functionName;
	protected String message;

	protected double outputValue;
	protected String outputUnit;
	protected String outputText;

	protected Function f;

	protected boolean parametersAreModifiable;

	protected DynamicPropertiesSupport dynsup;

	final static String DEFAULT_DECIMAL_PLACES = "0";

	String decimalPlaces = DEFAULT_DECIMAL_PLACES;

	final static int DEFAULT_DECIMAL_PLACES_INT = 0;

	int decimalPlacesInt = DEFAULT_DECIMAL_PLACES_INT;

	final static String DEFAULT_PATTERN = "#,##0";

	DecimalFormat df = new DecimalFormat(DEFAULT_PATTERN);

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public FunctionComponent(final int id, final Function f)
	{

		persistentChild = new Integer(id).toString();
		this.f = f;

		this.parametersAreModifiable = f.parametersAreModifiable();

		setFunctionName(f.getDisplayName());

		final String[] propertyNames = f.getPropertyNames();
		final double[] propertyValues = f.getPropertyValues();

		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);

		// if function defines some custome properties
		// (aka parameters)
		// then add dynamic properties for each of these to the components

		if ((propertyNames != null) && (propertyValues != null))
		{
			for (int i = 0; i < propertyNames.length; i++)
			{
				dynsup.addProperty(propertyNames[i], Double.class, new Double(propertyValues[i]));
			}
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	// pattern to format number as having
	// no decimal places

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		if (parametersAreModifiable)
		{
			final String[] propertyNames = f.getPropertyNames();
			final double[] propertyValues = f.getPropertyValues();

			if ((propertyNames != null) && (propertyValues != null))
			{
				dynsup.dynSetProperty(name, value);
				for (int i = 0; i < propertyNames.length; i++)
				{

					if (propertyNames[i].equals(name))
					{

						propertyValues[i] = ((Double) value).doubleValue();
						break;
					}
				}
			}

			f.setPropertyValues(propertyValues);

			calcOutput();
		}
	}

	public String getAttention()
	{
		return message;
	}

	public String getDecimalPlaces()
	{
		return decimalPlaces;
	}

	public String getDescription()
	{
		return getPersistentChild() + ":" + retrieveFunction().getDisplayName();
	}

	public String getFunctionName()
	{
		return functionName;
	}

	public DictionaryImpl getInput()
	{
		return input;
	}

	public DictionaryImpl getOutput()
	{
		return output;
	}

	public String getOutputText()
	{
		return outputText;
	}

	public String getOutputUnit()
	{
		return outputUnit;
	}

	public double getOutputValue()
	{
		return outputValue;
	}

	/**
	 * persistentChild property for persistence of sub-components (must be String type)
	 */
	public String getPersistentChild()
	{
		return persistentChild;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public Function retrieveFunction()
	{
		return f;
	}

	public void setDecimalPlaces(final String newValue)
	{
		final String oldValue = getDecimalPlaces();

		try
		{
			final int newValueInt = ((new Integer(newValue)).intValue());

			if (newValueInt >= 0)
			{
				this.decimalPlaces = newValue;
				this.decimalPlacesInt = newValueInt;
				this.df.setMinimumFractionDigits(newValueInt);
				this.df.setMaximumFractionDigits(newValueInt);
			}
			else
			{
				setAttention("Must be a number greater than or equal to 0");
				this.decimalPlaces = DEFAULT_DECIMAL_PLACES;
				this.decimalPlacesInt = DEFAULT_DECIMAL_PLACES_INT;
				df.applyPattern(DEFAULT_PATTERN);
			}
		}
		catch (final NumberFormatException e)
		{
			setAttention("Must be a number greater than or equal to 0");
			this.decimalPlaces = DEFAULT_DECIMAL_PLACES;
			this.decimalPlacesInt = DEFAULT_DECIMAL_PLACES_INT;
			df.applyPattern(DEFAULT_PATTERN);
		}

		if (!(this.decimalPlaces.equals(oldValue)))
		{
			propertyChangeListeners.firePropertyChange("decimalPlaces", oldValue, this.decimalPlaces);
		}

		calcOutput();
	}

	public void setInput(final DictionaryImpl input)
	{
		final DictionaryImpl oldInput = this.input;
		this.input = input;

		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);

		calcOutput();
	}

	public void setPersistentChild(final String persistentChild)
	{
		this.persistentChild = persistentChild;
	}

	public void stop()
	{
	}

	protected void calcOutput()
	{
		System.out.println("called calc output");

		final DictionaryImpl input = getInput();

		// first, check that is correctly formed input

		if (!(DictionaryUtilities.validateDictionary(input)))
		{
			System.out.println("not correctly formed dictionary");

			createOutputValues(DictionaryUtilities.constructErrorValue());
			setAttention("Not correctly formed input");
			return;
		}

		// now check that it is acceptable to the function

		final String errorMessage = f.validateInput(input);

		if (errorMessage != null)
		{
			createOutputValues(DictionaryUtilities.constructErrorValue());
			setAttention(errorMessage);
			return;
		}

		final DictionaryImpl outDict = f.calculate(input);
		createOutputValues(outDict);
	}

	protected void setAttention(final String message)
	{
		final String oldMessage = this.message;
		this.message = message;

		propertyChangeListeners.firePropertyChange("message", oldMessage, this.message);
	}

	protected void setFunctionName(final String functionName)
	{
		final String oldFunctionName = this.functionName;
		this.functionName = functionName;

		propertyChangeListeners.firePropertyChange("functionName", oldFunctionName, functionName);
	}

	protected void setOutput(final DictionaryImpl output)
	{
		final DictionaryImpl oldOutput = this.output;
		this.output = output;

		propertyChangeListeners.firePropertyChange("output", oldOutput, this.output);
	}

	protected void setOutputText(final String outputText)
	{
		final String oldText = this.outputText;
		this.outputText = outputText;

		propertyChangeListeners.firePropertyChange("outputText", oldText, this.outputText);
	}

	// property change stuff

	protected void setOutputUnit(final String outputUnit)
	{
		final String oldUnit = this.outputUnit;
		this.outputUnit = outputUnit;

		propertyChangeListeners.firePropertyChange("outputUnit", oldUnit, this.outputUnit);
	}

	protected void setOutputValue(final double outputValue)
	{
		final double oldValue = this.outputValue;
		this.outputValue = outputValue;

		propertyChangeListeners.firePropertyChange("outputValue", new Double(oldValue), new Double(this.outputValue));
	}

	private void createOutputValues(final DictionaryImpl outDict)
	{
		final String outputUnit = DictionaryUtilities.getUnit(outDict);
		final double outputValue = DictionaryUtilities.getValue(outDict);

		// now round the outputValue to the number of decimal
		// places specified by the user. Two operations
		// 1. do the rounding

		final int multiplier = (int) (Math.pow(10, decimalPlacesInt));
		final double multValue = multiplier * outputValue;
		final double rounded = Math.round(multValue);
		final double newValue = rounded / multiplier;

		final DictionaryImpl newDict = DictionaryUtilities.constructDictionary(newValue, outputUnit);

		setOutput(newDict);
		setOutputUnit(outputUnit);
		setOutputValue(newValue);

		final String newText = df.format(newValue);
		String outText = newText + " ";

		if (outputUnit != null)
		{
			outText = outText + outputUnit;
		}

		setOutputText(outText);
	}
}
