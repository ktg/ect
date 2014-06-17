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
package equip.ect.components.textscroller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A simple-as-possible bean with one in and one out. A useful starting point for coding.
 * 
 * @preferred
 */
public class TextScroller implements Serializable
{
	class Scroller implements Runnable
	{
		private String outString = "";
		private int scrollPos = 0;
		private boolean shouldContinue = true;

		private long internalScrollDelay;

		private static final int gapWidth = 20;

		Scroller(final String outString)
		{
			String toAdd = "";

			for (int i = 0; i < gapWidth; i++)
			{
				toAdd = toAdd + " ";
			}

			this.outString = outString + toAdd;

			// scroll delay might change. This thread will only
			// update internalScrollDelay from this property when
			// it is safe to do so

			this.internalScrollDelay = getScrollDelay();
		}

		@Override
		public void run()
		{
			while (shouldContinue == true)
			{
				try
				{
					final String firstPart = outString.substring(scrollPos);
					final String lastPart = outString.substring(0, scrollPos);

					final String tempDisplay = firstPart + lastPart;

					setOutput(tempDisplay);

					scrollPos++;

					if (scrollPos > outString.length())
					{
						scrollPos = 1;
					}

					Thread.sleep(internalScrollDelay);

					this.internalScrollDelay = getScrollDelay();
				}
				catch (final InterruptedException e)
				{
				}
			}

			setOutput("");
		}

		void stopSafely()
		{
			shouldContinue = false;
		}
	}

	protected Scroller scroller;
	protected String input;

	protected String output;

	// delay in milliseconds between shifting string left
	protected long scrollDelay = 100;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public TextScroller()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized String getInput()
	{
		return input;
	}

	public synchronized String getOutput()
	{
		return output;
	}

	public synchronized long getScrollDelay()
	{
		return scrollDelay;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setInput(final String input)
	{
		// could suppress no-change setting
		// if (input==this.input || (input!=null && this.input!=null && input.equals(this.input))
		// return;
		final String oldInput = this.input;
		this.input = input;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input", oldInput, this.input);

		if (scroller != null)
		{
			scroller.stopSafely();
		}

		scroller = new Scroller(input);

		final Thread scrollThread = new Thread(scroller);
		scrollThread.start();
	}

	public synchronized void setScrollDelay(final long scrollDelay)
	{
		final long oldDelay = this.scrollDelay;
		this.scrollDelay = scrollDelay;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("scrolldelay", new Long(oldDelay), new Long(this.scrollDelay));

	}

	public synchronized void stop()
	{
	}

	protected synchronized void setOutput(final String output)
	{
		// could suppress no-change setting
		// if (output==this.output || (output!=null && this.output!=null &&
		// output.equals(this.output)) return;
		final String oldOutput = this.output;
		this.output = output;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("output", oldOutput, this.output);
	}
}
