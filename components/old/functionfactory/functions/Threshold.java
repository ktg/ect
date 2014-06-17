package equip.ect.components.functionfactory.functions;

import equip.data.DictionaryImpl;
import equip.ect.components.functionfactory.DictionaryUtilities;

public class Threshold extends AbstractFunction
{
	public Threshold()
	{
		final double[] propertyValues = new double[3];
		propertyValues[0] = 0.5;
		propertyValues[1] = 1.0;
		propertyValues[2] = 0.0;

		setPropertyValues(propertyValues);

		final String[] propertyNames = new String[3];

		propertyNames[0] = "threshold";
		propertyNames[1] = "yMax";
		propertyNames[2] = "yMin";

		setPropertyNames(propertyNames);
	}

	@Override
	public DictionaryImpl calculate(final DictionaryImpl arg1)
	{
		final double value = DictionaryUtilities.getValue(arg1);
		final String unit = DictionaryUtilities.getUnit(arg1);

		final double[] propertyValues = getPropertyValues();

		// propertyValues[0] is threshold, 1 is ymax, 2 is ymin
		if (value >= propertyValues[0])
		{
			return (DictionaryUtilities.constructDictionary(propertyValues[1], unit));
		}
		else
		{
			return (DictionaryUtilities.constructDictionary(propertyValues[2], unit));
		}
	}

	@Override
	public String getDisplayName()
	{
		return "Threshold function";
	}

	@Override
	public String getFunctionFormula()
	{
		final String formula = "y = yMax if x >= threshold, yMin otherwise";
		return formula;
	}

	@Override
	public double[] getSampleRange()
	{
		final double[] toReturn = { 0.0, 1.0 };
		return toReturn;
	}
}
