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
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.teco.particle.ClPacket;
import edu.teco.particle.ParticleSocket;

public class ParticleFactory implements Serializable
{

	private Map particles = Collections.synchronizedMap(new HashMap());
	private boolean stop = false;
	private int sleep = 10;

	// Property Change
	private transient PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	public ParticleFactory()
	{
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					startReceive();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		listeners.addPropertyChangeListener(l);
	}

	public Particle[] getChildren()
	{
		return (Particle[]) particles.values().toArray(new Particle[0]);
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		listeners.removePropertyChangeListener(l);
	}

	public void startReceive() throws SocketException
	{
		final ParticleSocket socket = new ParticleSocket();
		while (!stop)
		{
			try
			{
				final ClPacket packet = new ClPacket();
				socket.receive(packet);
				if (packet.isValid())
				{
					Particle particle = (Particle) particles.get(packet.getSenderId());
					if (particle != null)
					{
						// particle already exists so just update
						particle.update(packet);
					}
					else
					{
						if (packet.getType(ParticleBreakout.MYS) != null)
						{
							particle = new ParticleBreakout(packet);
						}
						else
						{
							particle = new ParticleSensor(packet);
						}
						final Particle oldVal[] = getChildren();
						particles.put(packet.getSenderId(), particle);
						final Particle newVal[] = getChildren();
						listeners.firePropertyChange("children", oldVal, newVal);
					}
					Thread.sleep(sleep);
				}
			}
			catch (final Exception e)
			{
				System.out.println("ParticleFactory: Error " + "processing CL Packet");
			}
		}
	}

	public synchronized void stop()
	{
		stop = true;
	}
}