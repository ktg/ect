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

package equip.ect.components.trigger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * @displayName BooleanToTriggerConverter
 * @classification Behaviour/Timing
 */
public class TriggerConverter implements Serializable
{
	boolean booleanTrigger = false;
	Object objectTrigger = null;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public TriggerConverter()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public boolean getBooleanTrigger()
	{
		return booleanTrigger;
	}

	public Object getObjectTrigger()
	{
		return objectTrigger;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setBooleanTrigger(final boolean newValue)
	{
		final boolean oldValue = this.booleanTrigger;
		this.booleanTrigger = newValue;

		propertyChangeListeners.firePropertyChange("booleanTrigger", oldValue, newValue);

		if ((oldValue == false) && (newValue == true))
		{
			setObjectTrigger(new Double(Math.random()));
		}
	}

	public void setObjectTrigger(final Object newValue)
	{
		final Object oldValue = this.objectTrigger;
		this.objectTrigger = newValue;

		propertyChangeListeners.firePropertyChange("objectTrigger", oldValue, this.objectTrigger);
	}

	public void stop()
	{
	}
}
