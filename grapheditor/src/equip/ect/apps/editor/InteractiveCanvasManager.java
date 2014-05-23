/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */

package equip.ect.apps.editor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author humble
 * 
 */
public class InteractiveCanvasManager
{
	protected final Map<String, InteractiveCanvas> canvases;

	protected InteractiveCanvas activeCanvas = null;

	public InteractiveCanvasManager()
	{
		this((InteractiveCanvas) null);
	}

	public InteractiveCanvasManager(final InteractiveCanvas activeCanvas)
	{
		this.canvases = new HashMap<String, InteractiveCanvas>();

		if (activeCanvas != null)
		{
			addCanvas(activeCanvas);
		}
	}

	public String addCanvas(final InteractiveCanvas canvas)
	{
		return addCanvas(canvas.getName(), canvas);
	}

	public String addCanvas(final String name, final InteractiveCanvas canvas)
	{
		if (canvas == null)
		{
			Info.message(this, "Warning => empty canvas");
			return null;
		}

		String finalName = name;
		if (canvases.containsKey(name))
		{
			finalName = createNewNameFromClash(name);
		}
		canvas.setName(finalName);
		canvases.put(finalName, canvas);
		if (canvases.size() == 1)
		{
			setActiveCanvas(canvas);
		}
		return finalName;
	}

	public InteractiveCanvas getActiveCanvas()
	{
		return this.activeCanvas;
	}

	public InteractiveCanvas getCanvas(final String canvasName)
	{
		return canvases.get(canvasName);
	}

	public InteractiveCanvas[] getCanvases()
	{
		final int size = canvases.size();
		final InteractiveCanvas[] allCanvases = new InteractiveCanvas[size];
		return canvases.values().toArray(allCanvases);
	}

	public void removeAllCanvases()
	{
		canvases.clear();
		setActiveCanvas(null);
	}

	public void removeCanvas(final InteractiveCanvas canvas)
	{
		canvases.remove(canvas.getName());
	}

	/**
	 * @param newName
	 * @param canvas
	 * @return the accepted name of the canvas in the manager. NULL if canvas could not be renamed.
	 */
	public String renameCanvas(final String newName, final InteractiveCanvas canvas)
	{
		synchronized (canvas)
		{
			final String oldName = canvas.getName();
			if (newName.equals(oldName)) { return oldName; }
			final InteractiveCanvas removed = canvases.remove(oldName);
			if (removed != null) { return addCanvas(newName, removed); }
			return null;
		}
	}

	public void setActiveCanvas(final InteractiveCanvas canvas)
	{
		this.activeCanvas = canvas;
	}

	protected String createNewNameFromClash(final String oldName)
	{
		return oldName.trim() + "_2";
	}
}