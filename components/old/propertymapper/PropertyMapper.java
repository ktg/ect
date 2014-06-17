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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */

package equip.ect.components.propertymapper;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Maps a key to a value.
 * 
 * <H3>Description</H3> <B>PropertyMapper</B> maps a string input to a string output. One may define
 * mappings in the form ... key1=value1 key2=value2 ... You may also specify a properties file in
 * the same format. This will be loaded as the mapping to use.
 * 
 * <H3>Configuration</H3> To configure the <B>PropertyMapper</B> load a properties file or input the
 * key/value mappings in the mappings property.
 * 
 * <H3>Usage</H3> Set the 'input' property. This will try to match that input to any of the keys in
 * the map set and output the corresponding value. Returns 'null' as output if no mapping is present
 * for that input.
 * 
 * @defaultInputProperty input
 * @defaultOutputProperty output
 * @classification Behaviour/Simple Mapping
 * 
 * @author humble
 * 
 */
public class PropertyMapper implements Serializable
{

	public static String propertiesToString(final Properties props)
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			props.store(baos, null);
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toString();
	}

	private String mappingsDescription;

	private Properties mappings;

	private String input, output;

	private String propsFile;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public PropertyMapper()
	{
		this.mappings = new Properties();

	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public void fireMappingsDescriptionUpdated(final String mappingDesc)
	{
		final String old = this.mappingsDescription;
		this.mappingsDescription = mappingDesc;
		propertyChangeListeners.firePropertyChange("mappingsDescription", old, mappingsDescription);
	}

	public void fireMappingsUpdated(final Properties mappings)
	{
		final Hashtable old = this.mappings;
		this.mappings = mappings;
		propertyChangeListeners.firePropertyChange("mappings", old, mappings);
	}

	public String getInput()
	{
		return input;
	}

	public Hashtable getMappings()
	{
		return mappings;
	}

	public String getMappingsDescription()
	{
		return this.mappingsDescription;
	}

	public String getOutput()
	{
		return output;
	}

	public String getPropsFile()
	{
		return propsFile;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setInput(final String input)
	{
		final String old = this.input;
		this.input = input;
		propertyChangeListeners.firePropertyChange("input", old, input);
		setOutput(mappings.getProperty(input));
	}

	public void setMappingsDescription(final String mappingDesc)
	{

		final ByteArrayInputStream bais = new ByteArrayInputStream(mappingDesc.getBytes());
		try
		{
			final Properties newMaps = new Properties();
			newMaps.load(bais);
			fireMappingsDescriptionUpdated(mappingDesc);
			fireMappingsUpdated(newMaps);
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setOutput(final String output)
	{
		final String old = this.output;
		this.output = output;
		propertyChangeListeners.firePropertyChange("output", old, output);
	}

	public void setPropsFile(final String propsFile)
	{
		final String old = this.propsFile;
		this.propsFile = propsFile;
		try
		{
			final FileInputStream fis = new FileInputStream(propsFile);
			propertyChangeListeners.firePropertyChange("propsFile", old, propsFile);
			mappings.load(fis);
			fis.close();
			fireMappingsDescriptionUpdated(propertiesToString(this.mappings));
		}
		catch (final FileNotFoundException e)
		{
			System.out.println("PropertyMapper: " + e.getMessage());
		}
		catch (final IOException e)
		{
			System.out.println("PropertyMapper: " + e.getMessage());
		}
	}
}
