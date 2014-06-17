package equip.ect.components.functionfactory;

import javax.swing.Icon;

import equip.data.DictionaryImpl;

public interface Function
{
	public DictionaryImpl calculate(DictionaryImpl arg1);

	public String getDisplayName();

	/**
	 * <P>
	 * Implementing method should return string representing function eg for linear function might
	 * return <tt>y = ax + b</tt>
	 * </P>
	 * <P>
	 * This function is displayed to user to help them understand what parameters are available for
	 * them to supply
	 * </P>
	 */

	public String getFunctionFormula();

	public Icon getIcon();

	public String[] getPropertyNames();

	public double[] getPropertyValues();

	public double[] getSampleRange();

	// should return true if a user is allowed to modify
	// parameters values specified by a function, false otherwise
	public boolean parametersAreModifiable();

	public void setPropertyNames(String[] propertyNames);

	public void setPropertyValues(double[] propertyValues);

	public String validateInput(DictionaryImpl arg1);

	public String validateProperties(double[] newProperties);
}
