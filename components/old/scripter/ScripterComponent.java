/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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
package equip.ect.components.scripter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.NoSuchPropertyException;
import equip.ect.SimpleDynamicComponent;
import equip.ect.apps.scripter.Script;
import equip.ect.apps.scripter.Scripter;

/**
 * Scripted component based on BeanShell and Scripter application.
 * 
 * <H3>Description</H3> A scriping component, which requires properties to be declared and typed.
 * Script format is the same as the Scripter application (which it makes use of).
 * 
 * <H3>Installation</H3> No specific requirements (depends on BeanShell interpreter).
 * 
 * <H3>Configuration</H3> See Usage.
 * 
 * <H3>Usage</H3> Set configScript with the script. Script is divided into sections:
 * <ul>
 * <li>"inputs:" - input properties, e.g. "Comp.Prop as boolean in1".</li>
 * <li>"outputs:" - output properties, e.g. "Comp.Prop as boolean out1".</li>
 * <li>"state:" - exposed state (writable for persistence recovery), e.g. "int aValue".</li>
 * <li>"status:" - exposed status, readonly, e.g. "String info".</li>
 * <li>"on VARS:" - a script to execute when the named variables are set, e.g.
 * "on in1: out1=(!in1)".</li>
 * </ul>
 * Note that the "Comp.Prop" parts of inputs and outputs are only used by the Scripter application,
 * not by this component (in the application they what the properties should be linked to).
 * 
 * <H3>Technical Details</H3> Scripting uses BeanShell interpreter. In component form link creation
 * is not attempted - this should be done in the GraphEditor.
 * 
 * @displayName Scripter Component
 * @classification Behaviour/Scripting
 * @technology BeanShell
 * @preferred
 * @defaultInputProperty configScript
 * @defaultOutputProperty status
 */
public class ScripterComponent implements Serializable, DynamicProperties
{
	/**
	 * property change support
	 */
	protected PropertyChangeSupport props;
	/**
	 * scripter
	 */
	protected Scripter scripter;
	/**
	 * dyn prop
	 */
	protected SimpleDynamicComponent component;
	/**
	 * status
	 */
	protected String status;
	/**
	 * config script
	 */
	protected String configScript = "";
	static String DEFAULT_SCRIPT = "inputs:\n  A.B as boolean in1\noutputs:\n  C.D as boolean out1\nstate:\nstatus:\non in1:\n  out1 = in1\n";

	/**
	 * cons
	 */
	public ScripterComponent()
	{
		scripter = new Scripter();
		component = (SimpleDynamicComponent) scripter.getComponent();
		props = new PropertyChangeSupport(this);
		component.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(final PropertyChangeEvent event)
			{
				System.out.println("ScripterComponent property change: " + event.getPropertyName() + " "
						+ event.getOldValue() + " -> " + event.getNewValue());
				props.firePropertyChange(event.getPropertyName(), event.getOldValue(), event.getNewValue());
			}
		});
		intSetConfigScript(DEFAULT_SCRIPT);
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		props.addPropertyChangeListener(l);
	}

	/**
	 * get all properties (DynamicProperties)
	 */
	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		final DynamicPropertyDescriptor[] props = component.dynGetProperties();
		System.out.println("Scripter.dynGetProperties: " + props.length);
		return props;
	}

	/**
	 * get one property by name (DynamicProperties)
	 */
	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		System.out.println("Scripter.dynGetProperty(" + name + ")");
		return component.dynGetProperty(name);
	}

	/**
	 * get one property by name (DynamicProperties)
	 */
	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		component.dynSetProperty(name, value);
	}

	/**
	 * config script
	 */
	public String getConfigScript()
	{
		return configScript;
	}

	/**
	 * status
	 */
	public String getStatus()
	{
		return status;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		props.removePropertyChangeListener(l);
	}

	/**
	 * config script
	 */
	public void setConfigScript(final String s)
	{
		final String old = configScript;
		intSetConfigScript(s);
		props.firePropertyChange("configScript", old, s);
	}

	/**
	 * stop
	 */
	public void stop()
	{
	}

	/**
	 * config script
	 */
	protected void intSetConfigScript(final String s)
	{
		configScript = s;
		try
		{
			final Script script = Scripter.parse(configScript);
			scripter.setScript(script);
			setStatus("Set script OK");
		}
		catch (final Exception e)
		{
			System.err.println("ERROR setting script:" + e);
			e.printStackTrace(System.err);
			setStatus("Error: " + e);
		}
	}

	/**
	 * status
	 */
	protected void setStatus(final String status)
	{
		final String old = this.status;
		this.status = status;
		props.firePropertyChange("status", old, status);
	}
}
