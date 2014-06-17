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

import java.util.List;

import edu.teco.particle.AclTuple;
import edu.teco.particle.ClPacket;

public class ParticleSensor extends Particle
{

	public static final int LIGHT_TSL2550_IR = 22;
	public static final int LIGHT_TSL2550_VISIBLE = 23;
	public static final int LIGHT_TSL250 = 25;
	public static final int LIGHT_TSL260 = 26;

	public static final int AUDIO_VOLUME = 1;

	public static final String SFC = "SFC";
	public static final String STE = "STE";
	public static final String SLI = "SLI";
	public static final String SAU = "SAU";
	public static final String SVC = "SVC";
	public static final String SGX = "SGX";
	public static final String SGY = "SGY";

	private int sfcForce = -1;
	private int steTempCelsius = -1;
	private int sliLightVisible = -1;
	private int sliLightIR = -1;
	private int sauAudioVolume = -1;
	private int svcVoltageMV = -1;
	private int sgxGravityX = -1;
	private int sgyGravityY = -1;

	public ParticleSensor(final ClPacket packet)
	{
		super(packet);
	}

	public int getSauAudioVolume()
	{
		return sauAudioVolume;
	}

	public int getSfcForce()
	{
		return sfcForce;
	}

	public int getSgxGravityX()
	{
		return sgxGravityX;
	}

	public int getSgyGravityY()
	{
		return sgyGravityY;
	}

	public int getSliLightIR()
	{
		return sliLightIR;
	}

	public int getSliLightVisible()
	{
		return sliLightVisible;
	}

	public int getSteTempCelsius()
	{
		return steTempCelsius;
	}

	public int getSvcVoltageMV()
	{
		return svcVoltageMV;
	}

	public void setSauAudioVolume(final int sauAudioVolume)
	{
		listeners.firePropertyChange("sauAudioVolume", this.sauAudioVolume, sauAudioVolume);
		this.sauAudioVolume = sauAudioVolume;
	}

	public void setSfcForce(final int sfcForce)
	{
		listeners.firePropertyChange("sfcForce", this.sfcForce, sfcForce);
		this.sfcForce = sfcForce;
	}

	public void setSgxGravityX(final int sgxGravityX)
	{
		listeners.firePropertyChange("sgxGravityX", this.sgxGravityX, sgxGravityX);
		this.sgxGravityX = sgxGravityX;
	}

	public void setSgyGravityY(final int sgyGravityY)
	{
		listeners.firePropertyChange("sgyGravityY", this.sgyGravityY, sgyGravityY);
		this.sgyGravityY = sgyGravityY;
	}

	public void setSliLightIR(final int sliLightIR)
	{
		listeners.firePropertyChange("sliLightIR", this.sliLightIR, sliLightIR);
		this.sliLightIR = sliLightIR;
	}

	public void setSliLightVisible(final int sliLightVisible)
	{
		listeners.firePropertyChange("sliLightVisible", this.sliLightVisible, sliLightVisible);
		this.sliLightVisible = sliLightVisible;
	}

	public void setSteTempCelsius(final int steTempCelsius)
	{
		listeners.firePropertyChange("steTempCelsius", this.steTempCelsius, steTempCelsius);
		this.steTempCelsius = steTempCelsius;
	}

	public void setSvcVoltageMV(final int svcVoltageMV)
	{
		listeners.firePropertyChange("svcVoltageMV", this.svcVoltageMV, svcVoltageMV);
		this.svcVoltageMV = svcVoltageMV;
	}

	@Override
	public void update(final ClPacket packet)
	{
		if (packet != null)
		{
			setId(packet.getSenderId());
			setSfcForce(parseForce(packet));
			setSteTempCelsius(parseTemp(packet));
			setSvcVoltageMV(parseVoltage(packet));
			setSauAudioVolume(parseAudio(packet));
			setSgxGravityX(parseGravityX(packet));
			setSgyGravityY(parseGravityY(packet));
			setSliLightIR(parseLight(packet, LIGHT_TSL2550_IR));
			setSliLightVisible(parseLight(packet, LIGHT_TSL2550_VISIBLE));
		}
	}

	protected int parseAudio(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(SAU);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				if (data[0] == AUDIO_VOLUME) { return data[1]; }
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseForce(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(SFC);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				return data[0];
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseGravityX(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(SGX);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				return ((data[0] * 256) + data[1]);
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseGravityY(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(SGY);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				return ((data[0] * 256) + data[1]);
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseLight(final ClPacket packet, final int type)
	{
		try
		{
			final List list = packet.getAllOfType(SLI);
			if (list != null)
			{
				AclTuple tuple = null;
				byte data[] = null;
				for (int i = 0; i < list.size(); i++)
				{
					tuple = (AclTuple) list.get(i);
					data = tuple.getData();
					if (data[0] == type) { return ((data[1] * 256) + data[2]); }
				}
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseTemp(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(STE);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				return data[0];
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}

	protected int parseVoltage(final ClPacket packet)
	{
		try
		{
			final AclTuple tuple = packet.getType(SVC);
			if (tuple != null)
			{
				final byte data[] = tuple.getData();
				return ((data[0] * 256) + data[1]);
			}
		}
		catch (final Exception e)
		{
		}
		return -1;
	}
}