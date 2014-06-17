/*
<COPYRIGHT>

Copyright (c) 2004-2005, Goldsmiths College, University of London
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

Created by: Andy Law (Goldsmiths College)
Contributors:
  Andy Law (Goldsmiths College)
  Stefan Rennick Egglestone (University of Nottingham)

 */

package equip.ect.components.template;

//Required for persistance
//import equip.ect.ContainerManager;
//import equip.ect.Persistable;
//import equip.ect.PersistenceManager;

//Import all the libraries we will use
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.Serializable;

/**
 * This template is intended to support the quick building of simple I/O components. <h3>Description
 * </h3>
 * <P>
 * The component handles property changes and centralises component calculations. Here is how it
 * functions:
 * </P>
 * <ul>
 * <li>The component generates property change events when any of its properties are changed by a
 * user
 * <li>When any property change occurs, the runScript method is called to perform any necessary
 * computation
 * <li>This then delegates to the runSubscript01 method, which is the one that should be modified to
 * implement any functionality that you want
 * </ul>
 * 
 * @displayName Alternate template component
 * @classification Tutorials/Writing a Component
 * @preferred
 */

public class SimpleTemplate implements Serializable
{
	// input names
	public static final String VALUE_IN_01_PROPERTY_NAME = "input01";
	public static final String VALUE_IN_02_PROPERTY_NAME = "input02";
	public static final String VALUE_IN_03_PROPERTY_NAME = "input03";

	// output names
	public static final String VALUE_OUT_01_PROPERTY_NAME = "output01";

	public static void main(final String[] args)
	{
		new SimpleTemplate();
	}

	// Establish Property Change Support
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	// Set persistance
	private File persistFile = null;
	// Set the I/O Properties
	// Inputs
	private String input01 = "input01 default";
	private String input02 = "";
	private String input03 = "input03 default";

	// Outputs
	private String output01 = "";

	// class constructor - necessary to include this
	public SimpleTemplate()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public String getInput01()
	{
		return input01;
	}

	public String getInput02()
	{
		return input02;
	}

	public String getInput03()
	{
		return input03;
	}

	public String getOutput01()
	{
		return output01;
	}

	// Set up the generic Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	// Set up the standard Property Change Listeners for the Inputs and Outputs
	// When a Property Change occurs any Listeners are informed of the old and the new values
	// The main script is also called to calculate the effects of the change
	public void setInput01(final String putIn)
	{
		final String oldPutIn = this.input01;
		this.input01 = putIn;
		propertyChangeListeners.firePropertyChange(VALUE_IN_01_PROPERTY_NAME, oldPutIn, putIn);
		runScript(VALUE_IN_01_PROPERTY_NAME);
	}

	public void setInput02(final String putIn)
	{
		final String oldPutIn = this.input02;
		this.input02 = putIn;
		propertyChangeListeners.firePropertyChange(VALUE_IN_02_PROPERTY_NAME, oldPutIn, putIn);
		runScript(VALUE_IN_02_PROPERTY_NAME);
	}

	public void setInput03(final String putIn)
	{
		final String oldPutIn = this.input03;
		this.input03 = putIn;
		propertyChangeListeners.firePropertyChange(VALUE_IN_03_PROPERTY_NAME, oldPutIn, putIn);
		runScript(VALUE_IN_03_PROPERTY_NAME);
	}

	public void setOutput01(final String putIn)
	{
		final String oldPutIn = this.output01;
		this.output01 = putIn;
		propertyChangeListeners.firePropertyChange(VALUE_OUT_01_PROPERTY_NAME, oldPutIn, putIn);
		runScript(VALUE_OUT_01_PROPERTY_NAME);
	}

	// The main script checks to see what property has changed and calls the appropriate sub script
	// to report a specific output change
	protected void runScript(final String propertyChanged)
	{
		if (propertyChanged.equals(VALUE_IN_01_PROPERTY_NAME))
		{
			setOutput01(subScript01(this.input01));
		}
		else if (propertyChanged.equals(VALUE_IN_02_PROPERTY_NAME))
		{
			setOutput01(subScript01(this.input02));
		}
		else if (propertyChanged.equals(VALUE_IN_03_PROPERTY_NAME))
		{
			setOutput01(subScript01(this.input03));
		}
		else
		{
			// nothing happened;
		}
	}

	// Use this to implement a Persistable interface
	/*
	 * public synchronized File persist(ContainerManager containerManager) throws IOException { if
	 * (persistFile == null) { persistFile = File.createTempFile("SimpleTemplate", "",
	 * PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY); } try {
	 * PersistenceManager.getPersistenceManager().persistObject(persistFile, this); } catch
	 * (IOException e) { e.printStackTrace(); } return persistFile; }
	 */

	// The sub script calculates what changes have occurred due to a Property Change
	protected String subScript01(final String subScriptIn)
	{
		// Set up the temporary variables
		// Inputs
		final String tempInput01 = input01;
		final String tempInput02 = input02;
		final String tempInput03 = input03;
		// Outputs
		String tempOutput01 = output01;

		// Perform the calculation
		tempOutput01 = tempInput03.replaceAll(tempInput02, tempInput01);

		// Report the result
		return tempOutput01;
	}

}
