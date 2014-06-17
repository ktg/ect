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

package equip.ect.components.simplemediaviewer;

import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class SelectionHistory extends JComboBox
{

	class HistoryCellRenderer extends JLabel implements ListCellRenderer
	{

		HistoryCellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, final int index,
				final boolean isSelected, final boolean cellHasFocus)
		{

			// System.out.println("value= " + value.getClass().getName());
			if (value instanceof HistoryItem)
			{
				final HistoryItem hi = (HistoryItem) value;

				setText(hi.item.toString());
				// System.out.println("Rendering=" + hi.item.toString());
				// setToolTipText("Created on: " +
				// dateFormat.format(hi.timestamp));
				setBackground(isSelected ? Color.blue : Color.white);
				setForeground(isSelected ? Color.white : Color.black);

			}

			return this;
		}

	}

	class HistoryModel extends DefaultComboBoxModel
	{

		@Override
		public Object getSelectedItem()
		{
			final Object sel = super.getSelectedItem();
			if (sel instanceof HistoryItem) { return ((HistoryItem) sel).item; }
			return sel;
		}

	}

	// private Vector history;

	private final int historySize;

	private final HistoryModel historyModel;

	private int currentIndex;

	private static DateFormat dateFormat = DateFormat.getDateInstance();

	/*
	 * public void addToHistory(HistoryItem historyItem) { histor if (history.size() == 1) {
	 * currentIndex = 0; } Collections.sort(history, new Comparator() { public int compare(Object
	 * item1, Object item2) { return ((HistoryItem) item1).timestamp .compareTo(((HistoryItem)
	 * item2).timestamp); } }); }
	 */

	public SelectionHistory(final int historySize)
	{
		// this.history = new Vector();
		this.historyModel = new HistoryModel();
		this.setModel(historyModel);
		this.historySize = historySize;
		this.setRenderer(new HistoryCellRenderer());
		this.setEditable(true);
	}

	public synchronized HistoryItem addToHistory(final Object item)
	{
		final HistoryItem hi = new HistoryItem(item);

		final DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
		// remove the item if already in history
		for (int index = 0; index < model.getSize(); index++)
		{
			final HistoryItem currentItem = (HistoryItem) model.getElementAt(index);
			if (currentItem.item.equals(item))
			{
				model.removeElementAt(index);
				break;
			}
		}

		if (model.getSize() + 1 > historySize)
		{
			model.removeElementAt(model.getSize() - 1);
		}
		model.insertElementAt(hi, 0);
		setSelectedItem(hi);
		return hi;
	}

	public void back()
	{
		if (currentIndex < historyModel.getSize() - 1)
		{
			currentIndex++;
		}
	}

	public void forward()
	{
		if (currentIndex > 0)
		{
			currentIndex--;
		}
	}

	public HistoryItem getCurrentItem()
	{
		if (historyModel.getSize() == 0) { return null; }
		return (HistoryItem) historyModel.getElementAt(currentIndex);
	}

}

class HistoryItem
{

	final Object item;

	Date timestamp;

	HistoryItem(final Date timestamp, final Object item)
	{
		this.item = item;
		this.timestamp = timestamp;
	}

	HistoryItem(final Object item)
	{
		this(Calendar.getInstance().getTime(), item);
	}

}
