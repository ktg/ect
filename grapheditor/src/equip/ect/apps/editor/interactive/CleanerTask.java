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
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * InteractiveCanvasDaemon, $RCSfile: CleanerTask.java,v $
 *
 * $Revision: 1.3 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * Copyright (c) 2001, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor.interactive;

import java.util.List;
import java.util.TimerTask;

/**
 * The cleaner task to remove idle items in the canvas.
 */
public class CleanerTask extends TimerTask
{

	public static final long DEFAULT_TIME_UNTIL_KILL = 1000 * 60 * 2; // 2 min

	public static long timeUntilKill;

	private InteractiveCanvas canvas;

	CleanerTask(final InteractiveCanvas canvas)
	{
		this(canvas, DEFAULT_TIME_UNTIL_KILL);
	}

	CleanerTask(final InteractiveCanvas canvas, final long timeUntilKill)
	{
		this.canvas = canvas;
		CleanerTask.timeUntilKill = timeUntilKill;
	}

	public long getTimeToKill()
	{
		return CleanerTask.timeUntilKill;
	}

	@Override
	public void run()
	{

		final List<InteractiveCanvasItem> items = canvas.getItems();
		//final long currentTime = Calendar.getInstance().getTimeInMillis();
		for(InteractiveCanvasItem item: items)
		{
			// System.out.println("Doing task " + item.idleTime());
			final long idleTime = item.idleTime();
			if (idleTime > -1 && idleTime > timeUntilKill)
			{
				canvas.removeItem(item);
				// canvas.repaint();
			}
		}
	}

	public void setTimeToKill(final long ms)
	{
		CleanerTask.timeUntilKill = ms;
	}
}