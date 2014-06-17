package equip.ect.components.functionfactory;

import equip.data.DictionaryImpl;
import equip.data.DoubleBoxImpl;
import equip.data.FloatBoxImpl;
import equip.data.StringBoxImpl;
import equip.runtime.ValueBase;

public class DictionaryUtilities
{
	public static DictionaryImpl constructDictionary(final double value, final String unit)
	{
		final DictionaryImpl dl = new DictionaryImpl();
		dl.put("value", new DoubleBoxImpl(value));

		if (unit != null)
		{
			dl.put("unit", new StringBoxImpl(unit));
		}

		return dl;
	}

	public static DictionaryImpl constructErrorValue()
	{
		final DictionaryImpl toReturn = new DictionaryImpl();
		final DoubleBoxImpl dbl = new DoubleBoxImpl(0.0);
		toReturn.put("value", dbl);
		return toReturn;
	}

	public static String getUnit(final DictionaryImpl arg1)
	{
		final ValueBase vb = arg1.get("unit");

		if (vb == null)
		{
			return null;
		}
		else
		{
			return (((StringBoxImpl) vb).value);
		}
	}

	public static double getValue(final DictionaryImpl arg1)
	{
		final ValueBase vb = arg1.get("value");

		if (vb instanceof StringBoxImpl)
		{
			final String value = ((StringBoxImpl) vb).value;
			return ((new Double(value)).doubleValue());
		}

		if (vb instanceof DoubleBoxImpl)
		{
			final DoubleBoxImpl dbi = (DoubleBoxImpl) (arg1.get("value"));
			return (dbi.value);
		}

		if (vb instanceof FloatBoxImpl)
		{
			final FloatBoxImpl fbi = (FloatBoxImpl) (arg1.get("value"));
			return (fbi.value);
		}

		// should never get to here
		return 0.0;
	}

	public static boolean validateDictionary(final DictionaryImpl arg1)
	{
		// returns true if this is a valid dictionary that
		// can be used by a function, false if not

		final ValueBase value = arg1.get("value");

		if (value == null)
		{
			System.out.println("no value");
			return false;
		}

		if ((value instanceof StringBoxImpl) || (value instanceof DoubleBoxImpl) || (value instanceof FloatBoxImpl))
		{
			if (value instanceof StringBoxImpl)
			{
				final String stringValue = ((StringBoxImpl) value).value;

				try
				{
					// si this string encoding a real
					// double?
					new Double(stringValue);
				}
				catch (final NumberFormatException e)
				{
					System.out.println("not a number");
					e.printStackTrace();
					return false;
				}
			}

			final ValueBase unit = arg1.get("unit");

			if (unit != null)
			{
				if (!(unit instanceof StringBoxImpl))
				{
					System.out.println("not valid unit");
					return false;
				}
			}

			return true;
		}
		else
		{
			System.out.println(value.getClass().getName());
			return false;
		}
	}

}
