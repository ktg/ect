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

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Writes or concatenates string inputs to a file.
 * 
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
 * 
 * <H3>Usage</H3> Set the 'outFile' to the desired output file (full file path).<BR>
 * Set 'append' to true, to concatenate to the file, otherwise the specified file will be written
 * over.<BR>
 * Set 'delimeter' to use. This is any string to be written between each input. Note that 'newline'
 * delimeters such as "\n" are currently invisible.<BR>
 * Set 'input' to any string value. This will be written to the file.<BR>
 * 
 * <H3>Technical Details</H3> Uses the java.io.FileWriter
 * 
 * @classification Local Services
 * 
 * @author humble
 * 
 */
@ECTComponent
@Category("File")
public class FileWriter implements Serializable
{

	public static final String CLOSED_STATE = "CLOSED";

	public static final String OPENED_STATE = "OPENED";

	public static final String WRITING_STATE = "WRITING";

	public static final String FAILED_STATE = "FAILED";

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// TODO Auto-generated method stub

	}

	private String outFile = "./file_writer_output.txt";

	private boolean append = true;

	private boolean open = false;

	private String input = new String();

	private String state = CLOSED_STATE;

	private transient java.io.FileWriter fileWriter = null;

	private String delimiter = "\n"; // newline default

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public String getDelimiter()
	{
		return this.delimiter;
	}

	/**
	 * @return Returns the input.
	 */
	public String getInput()
	{
		return input;
	}

	/**
	 * @return Returns the outFile.
	 */
	public String getOutFile()
	{
		return outFile;
	}

	/**
	 * @return Returns the write state.
	 */
	public String getState()
	{
		return state;
	}

	/**
	 * @return Returns the append.
	 */
	public boolean isAppend()
	{
		return append;
	}

	/**
	 * @return Returns the openFile.
	 */
	public boolean isOpen()
	{
		return open;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * @param append
	 *            The append to set.
	 */
	public void setAppend(final boolean append)
	{
		final boolean old = this.append;
		this.append = append;
		propertyChangeListeners.firePropertyChange("append", old, append);
	}

	public void setDelimiter(final String delimiter)
	{
		final String old = this.delimiter;
		this.delimiter = delimiter;
		propertyChangeListeners.firePropertyChange("delimeter", old, delimiter);
	}

	/**
	 * @param newInput
	 *            The input to set.
	 */
	public void setInput(final String newInput)
	{
		final String old = this.input + "s!"; // just anything else to trigger
		// updates.
		if (append)
		{
			this.input = newInput;
			if (fileWriter != null && getState() == OPENED_STATE)
			{
				try
				{
					setState(WRITING_STATE);
					// should use FileWriter.append
					// but it's only available in 1.5
					fileWriter.write(this.input + delimiter);
					fileWriter.flush();
					setState(OPENED_STATE);
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{ // no appending, just dump all
			this.input = newInput;
			if (fileWriter != null && getState() == OPENED_STATE)
			{
				try
				{
					setState(WRITING_STATE);
					fileWriter.write(input);
					fileWriter.flush();
					setState(OPENED_STATE);
				}
				catch (final IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		propertyChangeListeners.firePropertyChange("input", old, input);
	}

	/**
	 * @param openFile
	 *            The openFile to set.
	 */
	public synchronized void setOpen(final boolean openFile)
	{
		final boolean old = this.open;
		this.open = openFile;
		if (openFile)
		{
			if (fileWriter == null && outFile != null)
			{
				try
				{
					final File file = new File(outFile);
					fileWriter = new java.io.FileWriter(file, append);
					setState(OPENED_STATE);
					propertyChangeListeners.firePropertyChange("open", false, true);
				}
				catch (final IOException e)
				{
					e.printStackTrace();
					setState(FAILED_STATE);
					propertyChangeListeners.firePropertyChange("open", true, false);
				}
			}
		}
		else
		{
			if (fileWriter != null)
			{
				try
				{
					fileWriter.flush();
					fileWriter.close();
					fileWriter = null;
					setState(CLOSED_STATE);
					propertyChangeListeners.firePropertyChange("open", true, false);
				}
				catch (final IOException e)
				{

					e.printStackTrace();
					setState(FAILED_STATE);
					propertyChangeListeners.firePropertyChange("open", false, true);
				}

			}
		}

	}

	/**
	 * @param outFile
	 *            The outFile to set.
	 */
	public void setOutFile(final String outFile)
	{
		final String old = this.outFile;
		this.outFile = outFile;
		propertyChangeListeners.firePropertyChange("outFile", old, outFile);
	}

	protected void setState(final String state)
	{
		final String old = this.state;
		this.state = state;
		propertyChangeListeners.firePropertyChange("state", old, state);
	}

}
