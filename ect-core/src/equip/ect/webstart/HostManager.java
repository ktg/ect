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

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  James Mathrick (University of Nottingham)

 */
package equip.ect.webstart;

/**
 * Host manager, takes over from {@link equip.ect.webstart.Boot} for a started {@link Installation}. Perhaps
 * "InstallationManager" would be a more appropriate name. Currently largely unimplemented.
 */
public class HostManager
{
	/**
	 * the installation
	 */
	Installation initiator = null;
	/**
	 * GUI
	 */
	HostManagerFrame frame;

	/**
	 * normal cons. creates {@link HostManagerFrame} GUI.
	 */
	public HostManager(final Installation i)
	{
		this.initiator = i;
		// TODO ... startup the HM gui
		frame = new HostManagerFrame(this);
	}

	/**
	 * add container to known list
	 */
	public void addContainer(final Container c)
	{
		// TODO
		frame.addContainer(c);
	}

	/**
	 * notification that container crashed - typically not swing thread
	 */
	public void containerCrashed(final Container c)
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Container crashed: " + c);
		}
		// TODO ... update gui
		frame.updateContainerStatus(c);
	}

	/**
	 * restart container
	 */
	public boolean restartContainer(final Container c)
	{
		if (c.isRunning())
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Attempt to restart container " + c + " foiled. Container already running!");
			}
			return false;
		}
		final boolean itWorked = c.restart();
		if (itWorked)
		{
			// TODO ... update gui
			return true;
		}
		return false;
	}

	/**
	 * set status
	 */
	public void setStatus(final String s)
	{
		frame.setStatus(s);
	}

	/**
	 * start container
	 */
	public boolean startContainer(final Container c)
	{
		if (c.isRunning())
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Attempt to start already running container " + c);
			}
			return false;
		}
		final boolean itWorked = c.start(this);
		if (itWorked)
		{
			// TODO .. update gui
			frame.updateContainerStatus(c);
			return true;
		}
		return false;
	}

	/**
	 * stop container
	 */
	public boolean stopContainer(final Container c)
	{
		if (!c.isRunning())
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Attempt to stop a non-running container " + c);
			}
			return false;
		}
		c.stop();
		if (!c.isRunning())
		{
			// TODO .. update gui
			frame.updateContainerStatus(c);
			return true;
		}
		return false;
	}
}
