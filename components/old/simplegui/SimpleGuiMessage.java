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
package equip.ect.components.simplegui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * simplegui displayable message. C.f. MIDlet ?
 */
public class SimpleGuiMessage implements Serializable
{
	/**
	 * label
	 */
	JLabel label;
	/**
	 * displayable attribute
	 */
	protected SimpleGuiDisplayable displayable;
	/**
	 * message test
	 */
	protected String text = "message";
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * bean cons
	 */
	public SimpleGuiMessage()
	{
		// make GUI
		label = new JLabel(text);
		// make displayable
		displayable = new SimpleGuiDisplayableImpl(label, "default message");
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * displayable getter
	 */
	public SimpleGuiDisplayable getDisplayable()
	{
		return displayable;
	}

	/**
	 * text getter
	 */
	public synchronized String getText()
	{
		return text;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * test setter
	 */
	public void setText(final String text)
	{
		String old = null;
		synchronized (this)
		{
			old = this.text;
			this.text = text;
		}
		propertyChangeListeners.firePropertyChange("text", old, text);
		// update displayable
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					label.setText(text);
					System.out.println("Message text set to " + text);
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * stop
	 */
	public void stop()
	{
	}
}
