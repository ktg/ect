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
package equip.ect.components.dynamicbsh;

/*
 * FILE_TITLE, $RCSfile: DynamicBeanShell.java,v $
 *
 * $Revision: 1.9 $
 * $Date: 2013/08/06 12:15:13 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 */

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;
import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * BeanShell scripting component with run-time specified inputs and outputs.
 * 
 * <h3>Description</h3> BeanShell is a small, free, embeddable Java source interpreter with object
 * scripting language features, written in Java. BeanShell dynamically executes standard Java syntax
 * and extends it with common scripting conveniences such as loose types, commands, and method
 * closures like those in Perl and JavaScript. The component can create dynamic properties to act as
 * inputs and outputs to the script.
 * 
 * <h3>Usage</h3> The script for this component is set through the script property. The script gets
 * run after changes to any of the properties, including the dynamic ones. The inputNamesString
 * property is a list of inputs for the script, separated by spaces. Each input will be added as a
 * dynamic property to the component, and can be referenced by name within the script.
 * outputNamesString is exactly the same, but for outputs from the script. Each time the script is
 * run, the dynamic output properties are updated according to any changes made to them within the
 * script. The result property is the result of the last expression evaluated by the script.
 * 
 * @author Chris Greenhalgh, Ted Phelps
 * @see <a href="http://www.beanshell.org/">BeanShell Website</a>
 * @displayName Dynamic BeanShell
 * @classification Behaviour/Scripting
 * @technology BeanShell
 */
@ECTComponent
@Category("Scripting")
public class DynamicBeanShell implements Serializable, PropertyChangeListener, DynamicProperties
{
	private Interpreter interpreter = new Interpreter();
	private final Pattern pattern = Pattern.compile("^[A-Za-z_][0-9A-Za-z_]*$");
	private BeanShellParameter[] inputs = new BeanShellParameter[0];
	private BeanShellParameter[] outputs = new BeanShellParameter[0];
	private String script = "// Insert script here\n";
	private Object result = null;
	private final transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	/**
	 * dynamic properties support
	 */
	private final DynamicPropertiesSupport dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
	/**
	 * thread running invoke
	 */
	private Thread invokeThread = null;
	private String errorMessage = "";

	public DynamicBeanShell()
	{
		// Compile a regular expression to match valid input and output names
		addPropertyChangeListener(this);
	}

	private static synchronized String[] getInputNames(final BeanShellParameter[] inputs)
	{
		String[] names;

		names = new String[inputs.length];
		for (int i = 0; i < inputs.length; i++)
		{
			names[i] = inputs[i].getName();
		}

		return names;
	}

	private static String[] getOutputNames(final BeanShellParameter[] outputs)
	{
		final String[] names = new String[outputs.length];
		for (int i = 0; i < outputs.length; i++)
		{
			names[i] = outputs[i].getName();
		}

		return names;
	}

	private static String getParameterNamesString(final BeanShellParameter[] outputs)
	{
		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < outputs.length; i++)
		{
			if (i != 0)
			{
				buffer.append(' ');
			}
			buffer.append(outputs[i].getName());
		}

		return buffer.toString();
	}

	/** Convert basetypes into a string that bsh will interpret properly */
	private static String stringify(final Object value)
	{
		// For strings, we escape the double-quotes and backslashes
		if (value instanceof String)
		{
			StringBuilder buffer = new StringBuilder();
			String string = (String) value;
			buffer.append('"');
			for (int i = 0; i < string.length(); i++)
			{
				char ch = string.charAt(i);
				if (ch == '\n')
				{
					buffer.append('\\');
					buffer.append('n');
				}
				else if (ch == '"')
				{
					buffer.append('\\');
					buffer.append('"');
				}
				else if (ch == '\\')
				{
					buffer.append('\\');
					buffer.append('\\');
				}
				else
				{
					buffer.append(ch);
				}
			}
			buffer.append('"');

			return buffer.toString();
		}
		else
		{
			return String.valueOf(value);
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptor}
	 */
	@Override
	public DynamicPropertyDescriptor[] dynamicProperties()
	{
		return dynsup.dynamicProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object getDynamicProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.getDynamicProperty(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public void setDynamicProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.setDynamicProperty(name, value);
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	private void setErrorMessage(final String newValue)
	{
		final String oldValue = errorMessage;
		errorMessage = newValue;

		if(oldValue != newValue && (oldValue == null || !oldValue.equals(newValue)))
		{
			propertyChangeListeners.firePropertyChange("errorMessage", oldValue, newValue);
		}
	}

	public String getInputNamesString()
	{
		BeanShellParameter[] inputs;

		synchronized (this)
		{
			inputs = this.inputs;
		}

		return getParameterNamesString(inputs);
	}

	/**
	 * A list of inputs for the script, separated by spaces. Each input will be added as a property
	 * to the component, and can be referenced by name within the script.
	 */
	public void setInputNamesString(final String string)
	{
		String[] names;

		// Split the string into an array
		if ("".equals(string))
		{
			names = new String[0];
		}
		else
		{
			names = string.split("[ \t\r\n]+");
		}

		setInputNames(names);
	}

	public String getOutputNamesString()
	{
		BeanShellParameter[] outputs;

		synchronized (this)
		{
			outputs = this.outputs;
		}

		return getParameterNamesString(outputs);
	}

	/**
	 * A list of outputs for the script, separated by spaces. Each output will be added as a
	 * property to the component, and can be referenced by name within the script.
	 */
	public void setOutputNamesString(final String string)
	{
		String[] names;

		// Split the string into an array
		if ("".equals(string))
		{
			names = new String[0];
		}
		else
		{
			names = string.split("[ \t\r\n]+");
		}

		setOutputNames(names);
	}

	/**
	 * The result of the last expression evaluated by the script.
	 */
	public Object getResult()
	{
		return result;
	}

	private void setResult(final Object newResult)
	{
		Object oldResult;

		synchronized (this)
		{
			oldResult = result;

			// Bail if the result is unchanged.
			if (oldResult == newResult) { return; }

			result = newResult;
		}

		// Update the listeners
		propertyChangeListeners.firePropertyChange("result", oldResult, newResult);
	}

	public String getScript()
	{
		return script;
	}

	/**
	 * Beanshell script
	 */
	public void setScript(final String script)
	{
		String old;

		// Avoid loops
		if (this.script == script) { return; }

		// Record the new script
		old = this.script;
		this.script = script;

		propertyChangeListeners.firePropertyChange("script", old, script);

		// Try to invoke it
		invoke();
	}

	public synchronized void invoke()
	{
		setErrorMessage(null);

		invokeThread = Thread.currentThread();

		// Invoke the script to compute a new output value
		try
		{
			/* Set the value of each of the inputs */
			for (BeanShellParameter input : inputs)
			{
				try
				{
					Object value = dynsup.getDynamicProperty(input.getName());
					try
					{
						if (value != null && value.getClass().getName().endsWith("BoxImpl"))
						{
							value = Coerce.toClass(value, Serializable.class);
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR coercing " + value + " to Serializable: " + e);
						e.printStackTrace(System.err);
					}
					input.setValue(value);
				}
				catch (final NoSuchPropertyException e)
				{
					System.err.println("ERROR getting property " + input.getName() + ": " + e);
				}
				interpreter.set(input.getName(), input.getValue());
			}
			for (BeanShellParameter output : outputs)
			{
				try
				{
					Object value = dynsup.getDynamicProperty(output.getName());
					try
					{
						if (value != null && value.getClass().getName().endsWith("BoxImpl"))
						{
							value = Coerce.toClass(value, Serializable.class);
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR coercing " + value + " to Serializable: " + e);
						e.printStackTrace(System.err);
					}
					output.setValue(dynsup.getDynamicProperty(output.getName()));
				}
				catch (final NoSuchPropertyException e)
				{
					System.err.println("ERROR getting property " + output.getName() + ": " + e);
				}
				interpreter.set(output.getName(), output.getValue());
			}

			/* Invoke the script */
			// interpreter.setErr(System.out);

			try
			{
				Object newResult = interpreter.eval(script);

				/* Get the ouputs */
				synchronized (this)
				{
					setResult(newResult);
					for (BeanShellParameter output : outputs)
					{
						output.setValue(interpreter.get(output.getName()));
						try
						{
							dynsup.setDynamicProperty(output.getName(), output.getValue());
						}
						catch (final NoSuchPropertyException e)
						{
							System.err.println("ERROR setting output property " + output.getName() + ": " + e);
						}
					}
				}
			}
			catch (final EvalError e)
			{
				setErrorMessage(e.toString());
			}

			/* Unset the inputs to aid garbage collection */
			for (BeanShellParameter input : inputs)
			{
				interpreter.unset(input.getName());
			}
			for (BeanShellParameter output : outputs)
			{
				interpreter.unset(output.getName());
			}
		}
		catch (final EvalError exception)
		{
			System.err.println("EvalError: " + exception);
			synchronized (this)
			{
				setResult(null);
				for (BeanShellParameter output : outputs)
				{
					output.setValue(null);
					try
					{
						dynsup.setDynamicProperty(output.getName(), output.getValue());
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR setting output property " + output.getName() + ": " + e);
					}
				}
			}
		}
		finally
		{
			invokeThread = null;
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		final String name = event.getPropertyName();
		if (name.equals("script") || name.equals("result") || name.equals("inputNamesString")
				|| name.equals("outputNamesString") || name.equals("errorMessage")) { return; }
		if (Thread.currentThread() == invokeThread)
		{
			// doing invoke
			return;
		}
		System.out.println("Invoke script on change of " + name);
		// dyn property changed?!
		invoke();
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void stop()
	{
		synchronized (this)
		{
			inputs = null;
			outputs = null;
		}
	}

	protected BeanShellParameter[] getInputs()
	{
		return inputs.clone();
	}

	protected String[] getOutputNames()
	{
		BeanShellParameter[] outputs;

		synchronized (this)
		{
			outputs = this.outputs;
		}

		return getOutputNames(outputs);
	}

	/* PropertyChangeListener interface */

	/*
	 * This is a dodgy hack to work around a race condition in the re-instantiation code. We should
	 * really just be using setOutputNames().
	 */
	public void setOutputNames(final String[] names)
	{
		final Map<String, Integer> indicesByName = new HashMap<>();
		BeanShellParameter[] oldOutputs, newOutputs;
		boolean namesAreLegal = true;
		boolean outputsWereChanged;
		Integer indexObject;
		int index;

		for (int i = 0; i < names.length; i++)
		{
			// Make sure each name is a valid Java identifier
			if (!pattern.matcher(names[i]).matches())
			{
				System.err.println("BeanShell: error: invalid output name: " + DynamicBeanShell.stringify(names[i]));
				namesAreLegal = false;
				continue;
			}

			// Look for duplicate
			if (indicesByName.get(names[i]) != null)
			{
				System.err.println("BeanShell: error: duplicate output name: " + DynamicBeanShell.stringify(names[i]));
				namesAreLegal = false;
				continue;
			}

			// Record the name
			indicesByName.put(names[i], i);
		}

		// Bail out if any of the names were invalid
		if (!namesAreLegal) { return; }

		newOutputs = new BeanShellParameter[names.length];

		// Update the list of names
		outputsWereChanged = false;
		synchronized (this)
		{
			oldOutputs = outputs;

			// Reuse the old outputs that appear in the new list and discard those that don't
			for (int i = 0; i < oldOutputs.length; i++)
			{
				indexObject = indicesByName.get(oldOutputs[i].getName());
				if (indexObject == null)
				{
					// remove oldOutputs[i]....
					try
					{
						dynsup.removeProperty(oldOutputs[i].getName());
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR removing dyn property " + oldOutputs[i].getName() + ": " + e);
						e.printStackTrace(System.err);
					}
					outputsWereChanged = true;
				}
				else
				{
					index = indexObject;
					newOutputs[index] = oldOutputs[i];
					if (index != i)
					{
						outputsWereChanged = true;
					}
				}
			}

			// Create new outputs for the rest
			for (int i = 0; i < newOutputs.length; i++)
			{
				if (newOutputs[i] == null)
				{
					newOutputs[i] = new BeanShellParameter(names[i]);
					outputsWereChanged = true;
					dynsup.addProperty(newOutputs[i].getName(), Object.class, null);
				}
			}

			// Bail if nothing has actually changed
			if (!outputsWereChanged) { return; }

			// Get the new list of output names
			outputs = newOutputs.clone();
		}

		propertyChangeListeners.firePropertyChange(	"outputNamesString", getParameterNamesString(oldOutputs),
		                                           	getParameterNamesString(newOutputs));
	}

	protected BeanShellParameter[] getOutputs()
	{
		return outputs.clone();
	}

	private void setInputNames(final String[] names)
	{
		final Map<String, Integer> indicesByName = new HashMap<>();
		BeanShellParameter[] oldInputs, newInputs;
		boolean namesAreLegal;
		boolean inputsWereChanged;
		Integer indexObject;
		int index;

		namesAreLegal = true;
		for (int i = 0; i < names.length; i++)
		{
			// Make sure each name is a valid Java identifier
			if (!pattern.matcher(names[i]).matches())
			{
				System.err.println("BeanShell: error: invalid input name: " + DynamicBeanShell.stringify(names[i]));
				namesAreLegal = false;
				continue;
			}

			// Look for duplicates
			if (indicesByName.get(names[i]) != null)
			{
				System.err.println("BeanShell: error: duplicate input name: " + DynamicBeanShell.stringify(names[i]));
				namesAreLegal = false;
				continue;
			}

			// Record the name
			indicesByName.put(names[i], i);
		}

		// Bail out if any of the names were invalid
		if (!namesAreLegal) { return; }

		newInputs = new BeanShellParameter[names.length];

		// Update the list of names
		inputsWereChanged = false;
		synchronized (this)
		{
			oldInputs = inputs;

			// Reuse any old inputs that appear in the new list and discard those that don't
			for (int i = 0; i < oldInputs.length; i++)
			{
				indexObject = indicesByName.get(oldInputs[i].getName());
				if (indexObject == null)
				{
					// remove oldInputs[i]....
					try
					{
						dynsup.removeProperty(oldInputs[i].getName());
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR removing dyn property " + oldInputs[i].getName() + ": " + e);
						e.printStackTrace(System.err);
					}
					inputsWereChanged = true;
				}
				else
				{
					index = indexObject;
					newInputs[index] = oldInputs[i];
					if (index != i)
					{
						inputsWereChanged = true;
					}
				}
			}

			// Create new ones for the rest
			for (int i = 0; i < newInputs.length; i++)
			{
				if (newInputs[i] == null)
				{
					newInputs[i] = new BeanShellParameter(names[i]);
					dynsup.addProperty(newInputs[i].getName(), Object.class, null);
					inputsWereChanged = true;
				}
			}

			// Bail if nothing has actually changed
			if (!inputsWereChanged) { return; }

			// Get the new list of input names
			inputs = newInputs.clone();
		}

		propertyChangeListeners.firePropertyChange(	"inputNamesString", getParameterNamesString(oldInputs),
		                                           	getParameterNamesString(newInputs));
	}
}
