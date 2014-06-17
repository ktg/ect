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

import edu.teco.particle.AclTuple;
import edu.teco.particle.ClPacket;

public class ParticleBreakout extends Particle
{
	// my sensor type - indicates sensor
	// values from breakout board
	public static final String MYS = "MYS";
	public String mysSensorValues = null;

	public ParticleBreakout(final ClPacket packet)
	{
		super(packet);
	}

	public String getMysSensorValues()
	{
		return mysSensorValues;
	}

	public void setMysSensorValues(final String mysSensorValues)
	{
		listeners.firePropertyChange("mysSensorValues", this.mysSensorValues, mysSensorValues);
		this.mysSensorValues = mysSensorValues;
	}

	@Override
	public void update(final ClPacket packet)
	{
		if (packet != null)
		{
			setId(packet.getSenderId());
			setMysSensorValues(parseSensors(packet));
		}
	}

	protected String parseSensors(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(MYS);
			if (tuple != null) { return tuple.getDataAsString(); }
		}
		catch (final Exception e)
		{
		}
		return null;
	}
}