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
package equip.ect.components.ipergobjectproxy.part;


import org.iperg.platform.core.IpEvent;
import org.iperg.platform.networking.IpIoStream;
import org.iperg.platform.networking.IpNetworkEvent;
import org.iperg.platform.synchronisation.IpGameObject;
import org.iperg.platform.synchronisation.IpVetoException;

/**
 * potential part GameObject base class which accepts SetProperty <name> <value> events and tries to
 * set its own corresponding property.
 */
public class SetPropertyObject extends IpGameObject
{
	/**
	 * set property event type
	 */
	public static String SET_PROPERTY = "SetProperty";
	/**
	 * callback
	 */
	protected SetPropertyObjectCallback callback;

	/**
	 * public cons
	 */
	public SetPropertyObject()
	{
	}

	/**
	 * cons with callback
	 */
	public SetPropertyObject(final SetPropertyObjectCallback callback)
	{
		this.callback = callback;
	}

	/**
	 * check set property; throw IpVetoEvent if not; default allows all
	 */
	public void checkSetProperty(final String name, final String value) throws IpVetoException
	{
		if (callback != null)
		{
			callback.checkSetProperty(name, value);
		}
	}

	/**
	 * handle event
	 */
	public void handleEvent(final IpEvent event)
	{
		if (event.getType().equals(SET_PROPERTY))
		{
			try
			{
				final IpNetworkEvent ev = (IpNetworkEvent) event;
				final String name = ev.getPayload().readString();
				final String value = ev.getPayload().readString();
				System.out.println("SetPropertyObject: set property " + name + " = " + value);
				// exists??
				// check
				checkSetProperty(name, value);
				setProperty(name, value);
				if (callback != null)
				{
					callback.notifySetProperty(name, value);
				}
			}
			catch (final IpVetoException e)
			{
				System.out.println("Set property vetoed: " + e);
			}
			catch (final Exception e)
			{
				System.out.println("ERROR handling set property event: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * set callback
	 */
	public void setCallback(final SetPropertyObjectCallback callback)
	{
		this.callback = callback;
	}

	/**
	 * request property set
	 */
	public void setMasterProperty(final String name, final String value)
	{
		try
		{
			final IpIoStream s = new IpIoStream();
			s.writeString(name);
			s.writeString(value);
			IpNetworkEvent e;
			// Event destination is this object.
			e = new IpNetworkEvent(SET_PROPERTY, this.getId(), s);
			// Send event to master (may be this instance)
			e.send();
		}
		catch (final Exception ex)
		{
			System.out.println("ERROR setting master property: " + ex);
			ex.printStackTrace();
		}
	}
}
