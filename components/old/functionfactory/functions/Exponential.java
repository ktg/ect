package equip.ect.components.functionfactory.functions;

import equip.data.DictionaryImpl;
import equip.ect.components.functionfactory.DictionaryUtilities;

public class Exponential extends AbstractFunction
{
	public Exponential()
	{
		// function is y = a * exp(bx)
		// a is stored in propertyValues[0],
		// b is stored in propertyValues[1]

		final double[] propertyValues = new double[2];
		propertyValues[0] = 1.0;
		propertyValues[1] = 1.0;

		setPropertyValues(propertyValues);

		final String[] propertyNames = new String[2];

		propertyNames[0] = "a";
		propertyNames[1] = "b";

		setPropertyNames(propertyNames);

	}

	@Override
	public DictionaryImpl calculate(final DictionaryImpl arg1)
	{
		final double value = DictionaryUtilities.getValue(arg1);
		final String unit = DictionaryUtilities.getUnit(arg1);

		final double[] propertyValues = getPropertyValues();

		// propertyValues[0] is a, 1 is b
		// in formula y = a * exp(bx)

		final double bx = propertyValues[1] * value;

		final double expbx = Math.exp(bx);

		final double toReturn = expbx * propertyValues[0];

		return (DictionaryUtilities.constructDictionary(toReturn, unit));
	}

	@Override
	public String getDisplayName()
	{
		return "Exponential function";
	}

	@Override
	public String getFunctionFormula()
	{
		final String formula = "y = a * exp(bx)";

		return formula;
	}

	@Override
	public double[] getSampleRange()
	{
		final double[] toReturn = { 0.0, 1.0 };
		return toReturn;
	}
}
