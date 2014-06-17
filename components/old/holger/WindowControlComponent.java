/*
<COPYRIGHT>

Copyright (c) 2006, University of Nottingham
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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.holger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class WindowControlComponent implements Serializable
{
	protected int frameOneIndex = 0;
	protected int frameTwoIndex = 1;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public WindowControlComponent()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized int getFrameOneIndex()
	{
		return frameOneIndex;
	}

	public synchronized int getFrameTwoIndex()
	{
		return frameTwoIndex;
	}

	public synchronized Object getTriggerSwapFrames()
	{
		return null;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setFrameOneIndex(final int newValue)
	{
		final int oldValue = this.frameOneIndex;
		this.frameOneIndex = newValue;

		propertyChangeListeners.firePropertyChange("frameOneIndex", oldValue, newValue);
	}

	public synchronized void setFrameTwoIndex(final int newValue)
	{
		final int oldValue = this.frameTwoIndex;
		this.frameTwoIndex = newValue;

		propertyChangeListeners.firePropertyChange("frameTwoIndex", oldValue, newValue);
	}

	public synchronized void setTriggerSwapFrames(final Object trigger)
	{
		final int tempIndexOne = getFrameOneIndex();
		final int tempIndexTwo = getFrameTwoIndex();

		setFrameOneIndex(tempIndexTwo);
		setFrameTwoIndex(tempIndexOne);
	}

	public synchronized void stop()
	{
	}
}
