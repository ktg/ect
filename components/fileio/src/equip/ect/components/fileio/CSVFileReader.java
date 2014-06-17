/*
 <COPYRIGHT>

 Copyright (c) 2002-2005, University of Nottingham
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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.fileio;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Reads from a CSV (ie comma-seperated) file <h3>Description</h3> The CSVFileReader is capable of
 * reading from a comma-seperated file, as might be exported by Excel for example. It can parse
 * files whose first line is a comma-seperated list of column headers, or those which do not define
 * column headers. <h3>Configuration</h3> Set property <i>configFileName</i> to the full path of
 * your file. It is probably best to use forward slash characters in this rather than backslash
 * characters eg <tt>d:/data/datafile.csv</tt> rather than <tt>d:\data\datafile.csv</tt>. Then set
 * the <i>configured</i> property to <tt>true</tt>. If the file exists, it will be loaded into the
 * component, and an indication of the number of rows and columns of data it contains will be given
 * through the <i>rowCount</i> and <i>columnCount</i> properties. <h3>Usage</h3> Once you have
 * configured the component with a file, then there are several ways to access the data it contains:
 * <ul>
 * <li>access the data directly through the <i>data</i> property
 * <li>use the <i>currentRowIndex</i> or <i>currentColumnIndex</i> properties to select a particular
 * row or column. Data from this row or column will then appear on either property <i>currentRow</i>
 * or <i>currentColumn</i>
 * </ul>
 * 
 * @classification Local Services
 * @author Stefan Rennick Egglestone
 * @displayName CSVFileReader
 */
@ECTComponent
@Category("File")
public class CSVFileReader implements Serializable
{
	String fileName = null;
	int currentColumn = -1;
	int currentRow = -1;
	int rowCount = -1;
	int columnCount = -1;
	String[][] data = null;

	String[][] fullData = null;
	String[][] partialData = null;

	String message;
	boolean configured = false;

	boolean ignoreFirstLine = false;

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public CSVFileReader()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public int getColumnCount()
	{
		return columnCount;
	}

	public synchronized String getConfigFileName()
	{
		return fileName;
	}

	public boolean getConfigured()
	{
		return configured;
	}

	public synchronized String[] getCurrentColumn()
	{
		if ((currentColumn == -1) || (currentColumn >= columnCount) || (data == null))
		{
			return null;
		}
		else
		{
			final String[] toReturn = new String[data.length];

			for (int i = 0; i < data.length; i++)
			{
				toReturn[i] = data[i][currentColumn];

			}
			return toReturn;
		}
	}

	public synchronized int getCurrentColumnIndex()
	{
		return currentColumn;
	}

	public synchronized String[] getCurrentRow()
	{
		if ((currentRow == -1) || (currentRow >= rowCount) || (data == null))
		{
			return null;
		}
		else
		{
			return data[currentRow];
		}
	}

	public synchronized int getCurrentRowIndex()
	{
		return currentRow;
	}

	public synchronized String[][] getData()
	{
		return data;
	}

	public synchronized boolean getIgnoreFirstLine()
	{
		return ignoreFirstLine;
	}

	public String getMessage()
	{
		return message;
	}

	public int getRowCount()
	{
		return rowCount;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public synchronized void setConfigFileName(final String newName)
	{
		initializeVariables();

		final String oldName = this.fileName;
		this.fileName = newName;

		propertyChangeListeners.firePropertyChange("configFileName", oldName, newName);

		loadFile();
	}

	public synchronized void setCurrentColumnIndex(final int newIndex)
	{

		final String[] oldColumn = getCurrentColumn();

		final int oldIndex = this.currentColumn;
		this.currentColumn = newIndex;

		propertyChangeListeners.firePropertyChange("currentColumnIndex", oldIndex, newIndex);

		// setting the current column index will cause the current column
		// property to change

		final String[] newColumn = getCurrentColumn();

		propertyChangeListeners.firePropertyChange("currentColumn", oldColumn, newColumn);

		if (currentColumn >= columnCount)
		{
			setMessage("Current column has been set too high. There are only " + columnCount + " columns in the file");
		}
		else
		{
			setMessage(null);
		}
	}

	public synchronized void setCurrentRowIndex(final int newIndex)
	{
		final String[] oldRow = getCurrentRow();

		final int oldIndex = this.currentRow;
		this.currentRow = newIndex;

		propertyChangeListeners.firePropertyChange("currentRowIndex", oldIndex, newIndex);

		final String[] newRow = getCurrentRow();

		propertyChangeListeners.firePropertyChange("currentRow", oldRow, newRow);

		if (currentRow >= rowCount)
		{
			setMessage("Current row has been set too high. There are only " + rowCount + " rows in the file");
		}
		else
		{
			setMessage(null);
		}
	}

	public synchronized void setData(final String[][] newData)
	{
		final String[][] oldData = this.data;
		this.data = newData;

		propertyChangeListeners.firePropertyChange("data", oldData, newData);

		if (data == null)
		{
			setColumnCount(-1);
			setRowCount(-1);
		}
		else
		{
			setRowCount(data.length);

			if (data.length == 0)
			{
				setColumnCount(0);
			}
			else
			{
				setColumnCount(data[0].length);
			}
		}
	}

	public synchronized void setIgnoreFirstLine(final boolean newValue)
	{
		final String[] oldCurrentRow = getCurrentRow();
		final String[] oldCurrentColumn = getCurrentColumn();

		final boolean oldValue = this.ignoreFirstLine;
		this.ignoreFirstLine = newValue;

		propertyChangeListeners.firePropertyChange("ignoreFirstLine", oldValue, newValue);
		if (newValue)
		{
			setData(partialData);
		}
		else
		{
			setData(fullData);
		}

		// will change both current row data and current column data,
		// so trigger these to be updated

		final String[] newCurrentRow = getCurrentRow();
		final String[] newCurrentColumn = getCurrentColumn();

		propertyChangeListeners.firePropertyChange("currentColumn", oldCurrentColumn, newCurrentColumn);

		propertyChangeListeners.firePropertyChange("currentRow", oldCurrentRow, newCurrentRow);

	}

	void initializeVariables()
	{
		fullData = null;
		partialData = null;
		setData(null);
		setCurrentColumnIndex(-1);
		setCurrentRowIndex(-1);
		setConfigured(false);
	}

	void loadFile()
	{
		try
		{
			final FileReader fr = new FileReader(fileName);
			final BufferedReader br = new BufferedReader(fr);

			final List<String> currentLines = new ArrayList<String>();

			String lastLine;
			while ((lastLine = br.readLine()) != null)
			{
				currentLines.add(lastLine);
			}

			br.close();

			// first, work out how many entries per line
			// in file

			int tempColumnCount = 0;

			if (currentLines.size() > 0)
			{
				// if there are lines in the file, then
				// the column count is then number of entries
				// in the first line

				final String currentLine = currentLines.get(0);

				final String[] bits = processLine(currentLine);
				tempColumnCount = bits.length;
			}

			fullData = new String[currentLines.size()][];

			for (int i = 0; i < currentLines.size(); i++)
			{
				final String currentLine = currentLines.get(i);
				fullData[i] = processLine(currentLine);

				if (fullData[i].length != tempColumnCount)
				{
					setMessage("Wrong number of entries in row " + i);

					initializeVariables();
					return;
				}
			}

			// having loaded in all of the lines of data, then
			// prepare a version which does not have a top line

			// this is because some csv files use the top line
			// for column headings

			if (fullData.length > 0)
			{
				partialData = new String[(fullData.length) - 1][];
				System.arraycopy(fullData, 1, partialData, 0, fullData.length - 1);

			}
			else
			{
				partialData = new String[0][];
			}

			if (getIgnoreFirstLine())
			{
				setData(partialData);
			}
			else
			{
				setData(fullData);
			}
			setMessage("File loaded successfully");
			setConfigured(true);

		}
		catch (final FileNotFoundException e)
		{
			setMessage("File not found");
			initializeVariables();
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			setMessage("Error in opening file");
			initializeVariables();
			e.printStackTrace();
		}
	}

	String[] processLine(String line)
	{
		// adding starting and finishing , to line - then all the sections
		// within the line will be in between these, which simplified
		// the loop design

		line = "," + line + ",";
		int lastCommaPos = 0;
		boolean speechMarksOpen = false;

		final List<String> chunks = new ArrayList<String>();

		for (int i = 1; i < line.length(); i++)
		{
			final char c = line.charAt(i);

			switch (c)
			{
				case ',':

					if (speechMarksOpen)
					{
						// do nothing, as commas
						// within speech marks do not
						// indicate start of new chunk
					}
					else
					{
						if (i == (lastCommaPos + 1))
						{
							// chunk has no characters
							// in it

							chunks.add("");
							lastCommaPos = i;
						}
						else
						{
							final String chunk = line.substring(lastCommaPos + 1, i);
							chunks.add(chunk);
							lastCommaPos = i;
						}
					}

					break;

				case '\"':

					speechMarksOpen = !speechMarksOpen;
					break;

			}
		}

		return chunks.toArray(new String[chunks.size()]);
	}

	protected void setColumnCount(final int newCount)
	{
		final int oldCount = this.columnCount;
		this.columnCount = newCount;

		propertyChangeListeners.firePropertyChange("columnCount", oldCount, newCount);

	}

	protected void setConfigured(final boolean newValue)
	{
		final boolean oldValue = this.configured;
		this.configured = newValue;

		propertyChangeListeners.firePropertyChange("configured", oldValue, newValue);
	}

	protected void setMessage(final String newMessage)
	{
		final String oldMessage = this.message;
		this.message = newMessage;

		propertyChangeListeners.firePropertyChange("message", oldMessage, newMessage);
	}

	protected void setRowCount(final int newCount)
	{
		final int oldCount = this.rowCount;
		this.rowCount = newCount;

		propertyChangeListeners.firePropertyChange("rowCount", oldCount, newCount);

	}

}
