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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.util.process;

import java.io.InputStream;
import java.io.OutputStream;

public class WrappedProcess extends Process
{

	private int pid = -1;
	private int termTimeout = 2000;

	public WrappedProcess(final int pid)
	{
		this.pid = pid;
	}

	@Override
	public void destroy()
	{
		ProcessUtils.terminateProcess(pid, termTimeout);
	}

	@Override
	public int exitValue()
	{
		return ProcessUtils.exitCode(pid);
	}

	@Override
	public InputStream getErrorStream()
	{
		return null;
	}

	@Override
	public InputStream getInputStream()
	{
		return null;
	}

	@Override
	public OutputStream getOutputStream()
	{
		return null;
	}

	public int getProcessID()
	{
		return pid;
	}

	public int getTerminateTimeout()
	{
		return termTimeout;
	}

	public void setTerminateTimeout(final int timeout)
	{
		this.termTimeout = timeout;
	}

	@Override
	public int waitFor() throws InterruptedException
	{
		ProcessUtils.waitFor(pid, -1);
		return exitValue();
	}

}
