package equip.ect.components.functionfactory.functions;

import java.io.Serializable;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import equip.data.DictionaryImpl;
import equip.ect.components.functionfactory.Function;

public abstract class AbstractFunction implements Function, Serializable
{
	private String[] propertyNames;
	private double[] propertyValues;

	@Override
	public abstract DictionaryImpl calculate(DictionaryImpl arg1);

	@Override
	public abstract String getDisplayName();

	@Override
	public String getFunctionFormula()
	{
		return null;
	}

	@Override
	public Icon getIcon()
	{

		final String fqn = this.getClass().getName();

		final int pos = fqn.lastIndexOf('.');

		final String className = fqn.substring(pos + 1);

		final String location = "/ect/components/functionfactory/functions/icons/" + className + ".gif";

		final URL fileURL = this.getClass().getResource(location);

		return (new ImageIcon(fileURL));
	}

	@Override
	public String[] getPropertyNames()
	{
		return propertyNames;
	}

	@Override
	public double[] getPropertyValues()
	{
		return propertyValues;
	}

	@Override
	public abstract double[] getSampleRange();

	@Override
	public boolean parametersAreModifiable()
	{
		return true;
	}

	@Override
	public void setPropertyNames(final String[] propertyNames)
	{
		this.propertyNames = propertyNames;
	}

	@Override
	public void setPropertyValues(final double[] propertyValues)
	{
		this.propertyValues = propertyValues;
	}

	@Override
	public String validateInput(final DictionaryImpl arg)
	{
		return null;
	}

	@Override
	public String validateProperties(final double[] newProperties)
	{
		return null;
	}
}
