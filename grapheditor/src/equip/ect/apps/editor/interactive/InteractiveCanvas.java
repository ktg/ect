/*
 <COPYRIGHT>

 Copyright (c) 2002-2006, Swedish Institute of Computer Science AB
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
 Stefan Rennick Egglestone (Nottingham University)
 */

package equip.ect.apps.editor.interactive;

import equip.ect.apps.editor.EditorResources;
import equip.ect.apps.editor.SelectionModel;

import javax.swing.JDesktopPane;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The InteractiveCanvas provides base functionality for working areas. It is intended to be as
 * generic as possible. You should not implement behaviours that are specific to applications, for
 * that create or redefine the methods in subclasses.
 */
public class InteractiveCanvas extends JDesktopPane implements MouseListener, MouseMotionListener, SelectionModel.SelectionListener
{
	protected static final int NORMAL_MODE = 0;
	private static final Stroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, new float[]{2.1f}, 0);
	protected final SelectionModel selectionModel;
	protected final List<InteractiveCanvasItem> items = new ArrayList<>();
	protected int mode;
	private MarkerType markerType;
	private boolean inSelection;
	protected int xPos;
	protected int yPos;
	private int xPosPrev;
	private int yPosPrev;
	private int xPosAnchor;
	private int yPosAnchor;
	private int[] marker;

	public InteractiveCanvas(final String name, final Color backgroundColor, final SelectionModel selectionModel)
	{
		setName(name);
		setBackground(backgroundColor);
		setSize(200, 200);
		addMouseListener(this);
		addMouseMotionListener(this);
		markerType = MarkerType.NO_MARKER;
		mode = NORMAL_MODE;
		this.selectionModel = selectionModel;
		selectionModel.add(this);
	}

	public void addItem(final InteractiveCanvasItem item)
	{
		addItem(item, true);
	}

	public void addItems(final Iterable<? extends InteractiveCanvasItem> newItems)
	{
		for (InteractiveCanvasItem item : newItems)
		{
			addItem(item, false);
		}
		sortItemsByDrawPriority();
	}

	@Override
	public Rectangle getBounds()
	{
		final Rectangle bounds = getBounds(null);
		double rightmost = bounds.getX() + bounds.getWidth();
		double downmost = bounds.getY() + bounds.getHeight();
		for (InteractiveCanvasItem ici : items)
		{
			final Rectangle b = ici.getBounds();
			final double crightmost = b.getX() + b.getWidth();
			final double cdownmost = b.getY() + b.getHeight();
			if (crightmost > rightmost)
			{
				rightmost = crightmost;
			}
			if (cdownmost > downmost)
			{
				downmost = cdownmost;
			}
		}
		return new Rectangle(0, 0, (int) rightmost, (int) downmost);
	}

	public final List<InteractiveCanvasItem> getItems()
	{
		return items;
	}

	@Override
	public void mouseClicked(final MouseEvent me)
	{
	}

	@Override
	public void mouseDragged(final MouseEvent me)
	{
		doOnMouseDragged(me);

		xPosPrev = xPos;
		yPosPrev = yPos;
		xPos = me.getX();
		yPos = me.getY();
		if (mode == NORMAL_MODE)
		{
			switch (me.getModifiers())
			{
				case InputEvent.BUTTON1_MASK: // left mouse button
					if (inSelection)
					{

						// System.out.println("translate intercan");
						translateItems(xPos, yPos, xPosPrev, yPosPrev);
						doOnTranslate(xPos, yPos);
					}
					else
					{
						markerType = MarkerType.BOX_MARKER;
						final int[] prevMarker = new int[]{marker[0], marker[1], marker[2], marker[3]};
						calculateMarker();
						final int clipOrigoX = Math.min(marker[0], prevMarker[0]);
						final int clipOrigoY = Math.min(marker[1], prevMarker[1]);
						final int clipSizeX = Math.max(marker[0] + marker[2], prevMarker[0] + prevMarker[2])
								- clipOrigoX + 1;
						final int clipSizeY = Math.max(marker[1] + marker[3], prevMarker[1] + prevMarker[3])
								- clipOrigoY + 1;
						insideMarker();
						repaint(-1, clipOrigoX, clipOrigoY, clipSizeX, clipSizeY);
					}
			}
		}
	}

	@Override
	public void mouseEntered(final MouseEvent me)
	{
	}

	@Override
	public void mouseExited(final MouseEvent me)
	{
	}

	@Override
	public void mouseMoved(final MouseEvent me)
	{
	}

	@Override
	public void mousePressed(final MouseEvent me)
	{
		xPosPrev = xPos = me.getX();
		yPosPrev = yPos = me.getY();
		xPosAnchor = xPos;
		yPosAnchor = yPos;

		switch (me.getButton())
		{
			case MouseEvent.BUTTON1: // left mouse button
				final InteractiveCanvasItem selectedItem = getItem(xPos, yPos);
				if (selectedItem != null)
				{
					if (!selectedItem.isSelected())
					{
						if (me.isShiftDown())
						{
							selectionModel.add(selectedItem.getID());
						}
						else
						{
							selectionModel.set(selectedItem.getID());
						}

						setTopDrawPriority(selectedItem);
					}
					markerType = MarkerType.SINGLE_MARKER;
					inSelection = true;
				}
				else
				{
					inSelection = false;
					calculateMarker();
					selectionModel.clear();
				}
				break;
			case InputEvent.BUTTON2_MASK:
				// System.out.println("Button 2");
				break;
			case InputEvent.BUTTON3_MASK: // right mouse button
				// System.out.println("Button 3");

				break;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me)
	{
		switch (me.getModifiers())
		{
			case InputEvent.BUTTON1_MASK: // left mouse button
				doOnMouseReleased(xPos, yPos);
				break;
		}
	}

	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		paintBackground(g2);
		drawSelector(g2);
		drawItems(g2);
	}

	public void removeItem(final InteractiveCanvasItem item)
	{
		removeItem(item, true);
	}

	public void removeItem(final InteractiveCanvasItem item, final boolean cleanUp)
	{
		if (item != null)
		{
			synchronized (items)
			{
				if (item.isSelected())
				{
					selectionModel.remove(item.getID());
				}
				if (cleanUp)
				{
					item.cleanUp();
				}
				items.remove(item);
				item.repaint();
			}
		}
	}

	public void removeItems(final Iterable<? extends InteractiveCanvasItem> items)
	{
		removeItems(items, true);
	}

	public void removeItems(final Iterable<? extends InteractiveCanvasItem> items, final boolean cleanUp)
	{
		if (items != null)
		{
			synchronized (items)
			{
				for (final InteractiveCanvasItem item : items)
				{
					System.out.println("removing item: " + item.getClass().getName() + " cleanUp=" + cleanUp);
					removeItem(item, cleanUp);
				}
			}
		}
	}

	@Override
	public void selectionChanged(Collection<String> selection)
	{

	}

	public void startDaemon()
	{
		new InteractiveCanvasDaemon(this);
	}

	protected void doOnMouseDragged(final MouseEvent me)
	{
	}

	protected void doOnMouseReleased(final int newPosX, final int newPosY)
	{
		markerType = MarkerType.NO_MARKER;
		repaint();
	}

	protected InteractiveCanvasItem getItem(final int x, final int y)
	{
		return getItem(x, y, InteractiveCanvasItem.class);
	}

	protected InteractiveCanvasItem getItem(final int x, final int y, final Class<?> matchClass)
	{
		// We need to traverse in the opposite direction since
		// last elements precede in focus.
		for (InteractiveCanvasItem ici : items)
		{
			if (matchClass.isAssignableFrom(ici.getClass()) && ici.isSelectable(x, y))
			{
				return ici;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <T> List<T> getItems(final Class<T> matchClass)
	{
		List<T> results = new ArrayList<>();
		// We need to traverse in the opposite direction since
		// last elements precede in focus.
		for (final InteractiveCanvasItem ici : items)
		{
			if (matchClass.isAssignableFrom(ici.getClass()))
			{
				results.add((T) ici);
			}
		}

		return results;
	}

	private void addItem(final InteractiveCanvasItem item, final boolean sort)
	{
		if (!items.contains(item))
		{
			items.add(item);
			item.setTargetCanvas(this);
			if (sort)
			{
				sortItemsByDrawPriority();
			}
			item.repaint();
		}
	}

	private void calculateMarker()
	{
		if (marker == null)
		{
			marker = new int[4];
		}
		marker[0] = (xPos < xPosAnchor) ? xPos : xPosAnchor;
		marker[1] = (yPos < yPosAnchor) ? yPos : yPosAnchor;
		marker[2] = Math.abs(xPosAnchor - xPos);
		marker[3] = Math.abs(yPosAnchor - yPos);
	}

	/**
	 * Convenience class for processing upon translation of items.
	 */
	private void doOnTranslate(final int newPosX, final int newPosY)
	{

	}

	private void drawItems(final Graphics2D g)
	{
		synchronized (items)
		{
			for (InteractiveCanvasItem item : items)
			{
				item.paintComponent(g);
			}
		}
	}

	private void drawSelector(final Graphics2D g)
	{
		if (markerType == MarkerType.BOX_MARKER)
		{
			final Stroke currentStroke = g.getStroke();
			final Color currentColor = g.getColor();
			g.setColor(Color.black);
			g.setStroke(DASHED_STROKE);
			g.drawRect(marker[0], marker[1], marker[2], marker[3]);
			g.setColor(currentColor);
			g.setStroke(currentStroke);
		}
	}

	private void insideMarker()
	{
		for (InteractiveCanvasItem item : items)
		{
			if (item.isInside(marker[0], marker[1], marker[0] + marker[2], marker[1] + marker[3]))
			{
				if (!item.isSelected())
				{
					// TODO selectItem(item, false);
				}
			}
			else
			{
				if (item.isSelected())
				{
					// TODO unselectItem(item);
				}
			}
		}
	}

	private void paintBackground(final Graphics2D g)
	{
		g.setBackground(EditorResources.BACKGROUND_COLOR);
	}

	/**
	 * This sets the object to be drawn last, and over the rest.
	 */
	private void setTopDrawPriority(final InteractiveCanvasItem item)
	{
		if (items.remove(item))
		{
			items.add(item);
		}
	}

	private void sortItemsByDrawPriority()
	{
		items.sort((item1, item2) ->
		{
			final int drawPrior1 = item1.drawPriority;
			final int drawPrior2 = item2.drawPriority;
			if (drawPrior1 == drawPrior2)
			{
				return 0;
			}
			else if (drawPrior1 > drawPrior2)
			{
				return 1;
			}
			else
			{
				return -1;
			}
		});
	}

	private void translateItems(final int newPosX, final int newPosY, final int oldPosX, final int oldPosY)
	{
		final int dx = (newPosX - oldPosX);
		final int dy = (newPosY - oldPosY);
		for (InteractiveCanvasItem item : items)
		{
			if (item.isSelected())
			{
				item.translatePosition(dx, dy);

				item.doOnTranslate();
			}
		}
	}

	private enum MarkerType {
		SINGLE_MARKER,
		BOX_MARKER,
		NO_MARKER
	}
}