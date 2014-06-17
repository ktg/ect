package equip.ect.components.functionfactory.functions;

import equip.data.DictionaryImpl;
import equip.ect.components.functionfactory.DictionaryUtilities;

public class Linear extends AbstractFunction
{
	public Linear()
	{
		final String[] propertyNames = new String[2];

		propertyNames[0] = "a";
		propertyNames[1] = "b";

		setPropertyNames(propertyNames);

		final double[] propertyValues = new double[2];

		propertyValues[0] = 1.0;
		propertyValues[1] = 0.0;

		setPropertyValues(propertyValues);
	}

	@Override
	public DictionaryImpl calculate(final DictionaryImpl arg1)
	{
		final double value = DictionaryUtilities.getValue(arg1);
		final String unit = DictionaryUtilities.getUnit(arg1);

		final double[] propertyValues = getPropertyValues();

		// propertyValues[0] is a, propertyValues[1] is b
		// in formula y = ax + b

		final double ax = propertyValues[0] * value;
		final double toReturn = ax + propertyValues[1];

		return (DictionaryUtilities.constructDictionary(toReturn, unit));
	}

	@Override
	public String getDisplayName()
	{
		return "Linear function";
	}

	@Override
	public String getFunctionFormula()
	{
		return "y = ax + b";
	}

	@Override
	public double[] getSampleRange()
	{
		final double[] toReturn = { 0.0, 1.0 };
		return toReturn;
	}
}
