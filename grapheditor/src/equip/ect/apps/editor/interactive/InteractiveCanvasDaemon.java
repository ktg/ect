/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Shahram Izadi (University of Nottingham)
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * InteractiveCanvasDaemon, $RCSfile: InteractiveCanvasDaemon.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * Copyright (c) 2001, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor.interactive;

import java.util.Timer;

/**
 * The background daemon for the InteractiveCanvas, which should take care of tasks in the
 * background. Useful for maintance of the system, clean ups, state checks, auto saves, etc.
 * Currently based on the java.util.Timer, running as daemon, which partially means tha it should
 * only run while the main application runs and not extend its lifetime.
 */

class InteractiveCanvasDaemon extends Timer
{
	private final static long DEFAULT_DELAY = 60000; // 60 secs
	private final static long DEFAULT_PERIOD = 60000; // 60 secs
	private InteractiveCanvas canvas;

	InteractiveCanvasDaemon(final InteractiveCanvas canvas)
	{
		this(canvas, DEFAULT_DELAY, DEFAULT_PERIOD);
	}

	private InteractiveCanvasDaemon(final InteractiveCanvas canvas, final long delay, final long period)
	{
		// Instantiate as daemon
		super(true);
		this.canvas = canvas;
		// remove auto-cleanup requested by RCA
		// schedule(new CleanerTask(canvas), delay, period);
	}
}
