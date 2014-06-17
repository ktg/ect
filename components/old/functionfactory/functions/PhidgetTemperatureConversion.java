package equip.ect.components.functionfactory.functions;

import equip.data.DictionaryImpl;
import equip.ect.components.functionfactory.DictionaryUtilities;

public class PhidgetTemperatureConversion extends AbstractFunction
{
	public PhidgetTemperatureConversion()
	{
		// first element xmin, second element xmax

		final double[] propertyValues = new double[2];
		propertyValues[0] = 0.04;
		propertyValues[1] = 0.7;

		setPropertyValues(propertyValues);

		final String[] propertyNames = new String[2];

		propertyNames[0] = "min value";
		propertyNames[1] = "max value";

		setPropertyNames(propertyNames);
	}

	@Override
	public DictionaryImpl calculate(final DictionaryImpl arg1)
	{
		// calculate temp in centigrade from phidget
		// sensor reading
		// ignores unit - but output has unit labelled c

		final double value = DictionaryUtilities.getValue(arg1);

		final double newValue = value * 250.0;

		final double temp = newValue - 50.0;

		return (DictionaryUtilities.constructDictionary(temp, "C"));
	}

	@Override
	public String getDisplayName()
	{
		return "Phidget temperature conversion function";
	}

	@Override
	public String getFunctionFormula()
	{
		return "y = 250x-50";
	}

	@Override
	public double[] getSampleRange()
	{
		final double[] toReturn = { 0.04, 0.7 };
		return toReturn;
	}

	@Override
	public boolean parametersAreModifiable()
	{
		// only parameters defined by function are xmin and
		// xmax, but these are fixed by sensor, so user
		// should not be able to change them

		return false;
	}

	@Override
	public String validateInput(final DictionaryImpl di)
	{
		// need to check that input is between min value and max value

		final double[] propertyValues = getPropertyValues();
		final double value = DictionaryUtilities.getValue(di);

		if ((propertyValues[0] <= value) && (propertyValues[1] >= value))
		{
			return null;
		}
		else
		{
			final String errorMessage = "Input value must be between min value and max value";
			return errorMessage;
		}
	}
}
