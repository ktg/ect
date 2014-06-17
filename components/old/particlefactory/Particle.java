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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.particlefactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import edu.teco.particle.ClId;
import edu.teco.particle.ClPacket;

public abstract class Particle implements Serializable
{

	public static String generateParticleId(final ClId id)
	{
		if (id != null)
		{
			return id.toString();
		}
		else
		{
			return "NONE";
		}
	}

	private ClId id = null;

	protected transient PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public Particle(final ClPacket packet)
	{
		update(packet);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		listeners.addPropertyChangeListener(l);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj != null && obj instanceof Particle)
		{
			final ClId id1 = getId();
			final ClId id2 = ((Particle) obj).getId();
			if (id1 != null) { return (id1.equals(id2)); }
		}
		return false;
	}

	public String getParticleId()
	{
		return generateParticleId(id);
	}

	public String getPersistentChild()
	{
		return getParticleId();
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		listeners.removePropertyChangeListener(l);
	}

	public void setId(final ClId id)
	{
		listeners.firePropertyChange("particleId", generateParticleId(this.id), generateParticleId(id));
		this.id = id;
	}

	public abstract void update(ClPacket packet);

	protected ClId getId()
	{
		return id;
	}
}