/*
 <COPYRIGHT>

 Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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
package equip.ect.components.fileio;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import equip.ect.Category;
import equip.ect.Coerce;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.ECTComponent;
import equip.ect.NoSuchPropertyException;

/**
 * Writes or concatenates string inputs to a file.
 * <p>
 * <H3>Description</H3> <B>FileWriter</B> is designed to easily write any string value to a file.
 * You can specify to append or overwrite to the file. You may also specify a delimiter to write
 * between inputs. Note that 'newline' delimeters such as "\n" are currently invisible.<BR>
 * Delimeters are any sequence of characters that separate one text element from another. They can
 * be single characters, as well as special characters.<BR>
 * Special characters include:<BR>
 * <B>\n</B> newline<BR>
 * <B>\r</B> carriage return<BR>
 * <B>\t</B> tab<BR>
 * etc ...
 * <p>
 * <H3>Usage</H3> Set the 'file' to the desired output file (full file path).<BR>
 * Set 'append' to true, to concatenate to the file, otherwise the specified file will be written
 * over.<BR>
 * Set 'delimeter' to use. This is any string to be written between each input. Note that 'newline'
 * delimeters such as "\n" are currently invisible.<BR>
 * Set 'input' to any string value. This will be written to the file.<BR>
 * <p>
 * <H3>Technical Details</H3> Uses the java.io.FileWriter
 *
 * @author humble
 */
@ECTComponent
@Category("File")
public class TimeWriter implements Serializable, PropertyChangeListener, DynamicProperties
{
	private enum State
	{
		CLOSED, OPENED, WRITING, FAILED
	}

	private static String getParameterNamesString(final String[] outputs)
	{
		final StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < outputs.length; i++)
		{
			if (i != 0)
			{
				buffer.append(' ');
			}
			buffer.append(outputs[i]);
		}

		return buffer.toString();
	}

	private Pattern pattern = Pattern.compile("^[A-Za-z_][0-9A-Za-z_]*$");

	private String file = "";

	private boolean newFile;
	private boolean logAll;

	private State state = State.CLOSED;

	private transient java.io.FileWriter fileWriter;
	private transient BufferedWriter writer;

	private String[] statuses = new String[0];

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private final DynamicPropertiesSupport dynamicProperties = new DynamicPropertiesSupport(propertyChangeListeners);

	private String dateFormat = "yyyy-MM-dd hh:mm:ss.SSS";

	private SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

	private SimpleDateFormat fileFormatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	public TimeWriter()
	{
		addPropertyChangeListener(this);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	/**
	 * get all properties' {@link DynamicPropertyDescriptor}
	 */
	@Override
	public DynamicPropertyDescriptor[] getDynamicProperties()
	{
		return dynamicProperties.getDynamicProperties();
	}

	/**
	 * get one property by name
	 */
	@Override
	public Object getDynamicProperty(final String name) throws NoSuchPropertyException
	{
		return dynamicProperties.getDynamicProperty(name);
	}

	/**
	 * get one property by name
	 */
	@Override
	public void setDynamicProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynamicProperties.setDynamicProperty(name, value);
	}

	public String getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * @return Returns the file.
	 */
	public String getFile()
	{
		return file;
	}

	public boolean getNewFile()
	{
		return newFile;
	}

	/**
	 * @return Returns the write state.
	 */
	public String getState()
	{
		return state.name();
	}

	public String getStatusNames()
	{
		return getParameterNamesString(statuses);
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event)
	{
		final String name = event.getPropertyName();
		if (name.equals("state") || name.equals("file") || name.equals("statusNames") || name.equals("dateFormat"))
		{
			return;
		}
		if (logAll)
		{
			writeTimestamp();
		}
		else
		{
			try
			{
				writeTimestamp(name + "," + Coerce.toClass(event.getNewValue(), String.class));
			}
			catch (final Exception e)
			{
				writeTimestamp(name + "," + event.getNewValue());
			}
		}
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setDateFormat(final String newFormat)
	{
		final String oldFormat = dateFormat;

		dateFormat = newFormat;
		formatter = new SimpleDateFormat(dateFormat);

		propertyChangeListeners.firePropertyChange("dateFormat", oldFormat, dateFormat);
	}

	public void setFile(final String outFile)
	{
		final String old = file;
		file = outFile;

		if (writer != null)
		{
			try
			{
				writer.flush();
				writer.close();
				writer = null;
				setState(State.CLOSED);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				setState(State.FAILED);
			}
		}

		try
		{
			final File file = new File(outFile);
			file.createNewFile();

			fileWriter = new java.io.FileWriter(file);
			writer = new BufferedWriter(fileWriter);
			setState(State.OPENED);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			setState(State.FAILED);
		}

		propertyChangeListeners.firePropertyChange("file", old, outFile);
	}

	public void setNewFile(final boolean value)
	{
		newFile = value;
		if (value)
		{
			String ext = "";
			final int extIndex = file.lastIndexOf('.');
			String content = file;
			if (extIndex > -1)
			{
				ext = file.substring(extIndex);
				content = content.substring(0, extIndex);
				System.out.println(content);
			}

			final int dateIndex = content.lastIndexOf('-');
			if (dateIndex > -1)
			{
				final String date = content.substring(dateIndex);
				try
				{
					fileFormatter.parse(date);
					content = content.substring(0, dateIndex);
					System.out.println(content);
				}
				catch (final ParseException e)
				{
					e.printStackTrace();
				}
			}

			setFile(content + "-" + fileFormatter.format(new Date()) + ext);
			if(logAll)
			{
				try
				{
					setState(State.WRITING);
					writer.append("TIME");
					for (String parameter : statuses)
					{
						writer.append(",");
						writer.append(parameter);
					}
					writer.newLine();
					writer.flush();
					setState(State.OPENED);
				}
				catch (final Exception e)
				{
					e.printStackTrace();
					setState(State.FAILED);
				}
			}
		}
	}

	public void setStatusNames(final String string)
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

		setStatuses(names);
	}

	public boolean getLogAll()
	{
		return logAll;
	}

	public void setLogAll(boolean logAll)
	{
		final boolean old = this.logAll;
		this.logAll = logAll;
		propertyChangeListeners.firePropertyChange("logAll", old, logAll);
	}

	private void setState(final State state)
	{
		final State old = this.state;
		this.state = state;
		propertyChangeListeners.firePropertyChange("state", old.name(), state.name());
	}

	private void setStatuses(final String[] names)
	{
		final Map<String, Integer> indicesByName = new HashMap<>();
		String[] oldStatuses, newStatuses;
		boolean namesAreLegal;
		boolean outputsWereChanged;
		Integer indexObject;
		int index;

		namesAreLegal = true;
		for (int i = 0; i < names.length; i++)
		{
			// Make sure each name is a valid Java identifier
			if (!pattern.matcher(names[i]).matches())
			{
				System.err.println("TimeWriter: error: invalid name: " + names[i]);
				namesAreLegal = false;
				continue;
			}

			// Look for duplicate
			if (indicesByName.get(names[i]) != null)
			{
				System.err.println("TimeWriter: error: duplicate name: " + names[i]);
				namesAreLegal = false;
				continue;
			}

			// Record the name
			indicesByName.put(names[i], i);
		}

		// Bail out if any of the names were invalid
		if (!namesAreLegal)
		{
			return;
		}

		newStatuses = new String[names.length];

		// Update the list of names
		outputsWereChanged = false;
		synchronized (this)
		{
			oldStatuses = statuses;

			// Reuse the old outputs that appear in the new list and discard those that don't
			for (int i = 0; i < oldStatuses.length; i++)
			{
				indexObject = indicesByName.get(oldStatuses[i]);
				if (indexObject == null)
				{
					// remove oldStatuses[i]....
					try
					{
						dynamicProperties.removeProperty(oldStatuses[i]);
					}
					catch (final NoSuchPropertyException e)
					{
						System.err.println("ERROR removing dyn property " + oldStatuses[i] + ": " + e);
						e.printStackTrace(System.err);
					}
					outputsWereChanged = true;
				}
				else
				{
					index = indexObject;
					newStatuses[index] = oldStatuses[i];
					if (index != i)
					{
						outputsWereChanged = true;
					}
				}
			}

			// Create new outputs for the rest
			for (int i = 0; i < newStatuses.length; i++)
			{
				if (newStatuses[i] == null)
				{
					newStatuses[i] = names[i];
					outputsWereChanged = true;
					dynamicProperties.addProperty(newStatuses[i], Object.class, null);
				}
			}

			// Bail if nothing has actually changed
			if (!outputsWereChanged)
			{
				return;
			}

			// Get the new list of output names
			statuses = newStatuses.clone();
		}

		propertyChangeListeners.firePropertyChange("statusNames", getParameterNamesString(oldStatuses),
				getParameterNamesString(newStatuses));
	}

	private void writeHeader() {

	}

	private void writeTimestamp()
	{
		if (writer != null && state == State.OPENED)
		{
			try
			{
				setState(State.WRITING);
				writer.append(formatter.format(new Date()));
				for (String parameter : statuses)
				{
					writer.append(",");
					try
					{
						writer.append(Coerce.toClass(dynamicProperties.getDynamicProperty(parameter), String.class));
					}
					catch (Exception e)
					{
						writer.append(dynamicProperties.getDynamicProperty(parameter).toString());
					}
				}
				writer.newLine();
				writer.flush();
				setState(State.OPENED);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				setState(State.FAILED);
			}
		}
	}

	/**
	 * @param newInput The input to set.
	 */
	private void writeTimestamp(final String newInput)
	{
		if (writer != null && state == State.OPENED)
		{
			try
			{
				setState(State.WRITING);
				writer.append(formatter.format(new Date()));
				writer.append(",");
				writer.append(newInput);
				writer.newLine();
				writer.flush();
				setState(State.OPENED);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				setState(State.FAILED);
			}
		}
	}
}