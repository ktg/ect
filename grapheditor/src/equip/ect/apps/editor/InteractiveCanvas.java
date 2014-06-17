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

package equip.ect.apps.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JDesktopPane;

/**
 * The InteractiveCanvas provides base functionality for working areas. It is intended to be as
 * generic as possible. You should not implement behaviours that are specific to applications, for
 * that create or redefine the methods in subclasses.
 */
public class InteractiveCanvas extends JDesktopPane implements MouseListener, MouseMotionListener
{
	public static final int SINGLE_SELECTION = 0;
	public static final int MULTIPLE_SELECTION = 1;
	public static final int NORMAL_MODE = 0;
	private static final Stroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, new float[] { 2.1f }, 0);

	protected final int SINGLE_MARKER = 0;
	protected final int BOX_MARKER = 1;
	protected final int NO_MARKER = 2;
	protected int markerType;
	protected boolean inSelection;
	protected int mode;
	protected int xPos, yPos, xPosPrev, yPosPrev, xPosAnchor, yPosAnchor;
	protected List<InteractiveCanvasItem> items;
	protected List<InteractiveCanvasItem> selectedItems = null;
	private int[] marker;
	private int selectionType;

	public InteractiveCanvas(final Color backgroundColor, final int selectionType)
	{
		this("Untitled", backgroundColor, selectionType);
	}

	public InteractiveCanvas(final String name, final Color backgroundColor, final int selectionType)
	{
		setName(name);
		setBackground(backgroundColor);
		setSize(200, 200);
		addMouseListener(this);
		addMouseMotionListener(this);
		markerType = NO_MARKER;
		mode = NORMAL_MODE;
		setSelectionType(selectionType);
	}

	public void addItem(final InteractiveCanvasItem item)
	{
		addItem(item, true);
	}

	public void addItem(final InteractiveCanvasItem item, final boolean sort)
	{
		if (items == null)
		{
			items = new ArrayList<InteractiveCanvasItem>();
		}
		if (!items.contains(item))
		{
			items.add(item);
			item.setTargetCanvas(this);
			if (sort)
			{
				sortItemsByDrawPriority();
			}
		}
	}

	public void addItem(final InteractiveCanvasItem item, final int x, final int y)
	{
		item.setPosition(x, y);
		addItem(item);
		item.repaint();
	}

	public void addItems(final Iterable<? extends InteractiveCanvasItem> newItems)
	{
		for (InteractiveCanvasItem item: newItems)
		{
			addItem(item, false);
		}
		sortItemsByDrawPriority();
	}

	public TexturePaint createImageTexture(final String filename, final boolean withinPackage)
	{
		final Image img = MediaFactory.createImage(filename, this, withinPackage);

		if (img == null) { return null; }

		final int iw = img.getWidth(this);
		final int ih = img.getHeight(this);
		if (iw < 0 || ih < 0) { return null; }
		final BufferedImage bi = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
		final Graphics2D tG2 = bi.createGraphics();
		tG2.drawImage(img, 0, 0, this);
		final Rectangle r = new Rectangle(0, 0, iw, ih);
		return new TexturePaint(bi, r);
	}

	@Override
	public Rectangle getBounds()
	{
		final Rectangle bounds = getBounds(null);
		double rightmost = bounds.getX() + bounds.getWidth();
		double downmost = bounds.getY() + bounds.getHeight();
		if (items != null)
		{
			for(InteractiveCanvasItem ici: items)
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
		}
		return new Rectangle(0, 0, (int) rightmost, (int) downmost);
	}

	public int[] getCenterCoordinates()
	{
		final int[] cent = new int[2];
		cent[0] = (int) (0.5 * this.getWidth());
		cent[1] = (int) (0.5 * this.getHeight());
		return cent;
	}

	public InteractiveCanvasItem getItem(final int x, final int y)
	{
		return getItem(x, y, InteractiveCanvasItem.class);
	}

	public InteractiveCanvasItem getItem(final int x, final int y, final Class<?> matchClass)
	{
		if (items != null)
		{
			// We need to traverse in the opposite direction since
			// last elements precede in focus.
			for (InteractiveCanvasItem ici: items)
			{
				if (matchClass.isAssignableFrom(ici.getClass()) && ici.isSelectable(x, y)) { return ici; }
			}
		}
		return null;
	}

	public final List<InteractiveCanvasItem> getItems()
	{
		return items;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getItems(final Class<T> matchClass)
	{
		List<T> results = null;
		if (items != null && items.size() > 0)
		{
			// We need to traverse in the opposite direction since
			// last elements precede in focus.
			for (final InteractiveCanvasItem ici: items)
			{
				if (matchClass.isAssignableFrom(ici.getClass()))
				{
					if (results == null)
					{
						results = new ArrayList<T>();
					}
					results.add((T) ici);
				}
			}
		}
		return results;
	}

	public List<InteractiveCanvasItem> getSelectedItems()
	{
		return selectedItems;
	}

	@Override
	public void mouseClicked(final MouseEvent me)
	{
		/*
		 * handled in mousePressed to be compatible with plasma screen
		 * System.out.println("CLICKED"); if (me.getClickCount() == 2) { doOnDoubleClick(me); }
		 */
	}

	@Override
	public void mouseDragged(final MouseEvent me)
	{

		// System.out.println("mouse dragged");
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
					else if (selectionType == MULTIPLE_SELECTION)
					{
						markerType = BOX_MARKER;
						final int[] prevMarker = new int[] { marker[0], marker[1], marker[2], marker[3] };
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
		/**
		 * System.out.println("Moving"); xPosPrev = xPos; yPosPrev = yPos; xPos = me.getX(); yPos =
		 * me.getY();
		 */
	}

	@Override
	public void mousePressed(final MouseEvent me)
	{

		final int modifier = me.getModifiers();

		xPosPrev = xPos = me.getX();
		yPosPrev = yPos = me.getY();
		xPosAnchor = xPos;
		yPosAnchor = yPos;

		switch (modifier)
		{
			case InputEvent.BUTTON1_MASK: // left mouse button
				if (selectionType == SINGLE_SELECTION)
				{
					unselectAll();
				}

				final InteractiveCanvasItem selectedItem = getItem(xPos, yPos);
				if (selectedItem != null)
				{
					switch (selectedItem.getSelectStatus())
					{
						case InteractiveCanvasItem.UNSELECTED:
							selectItem(selectedItem);
							setTopDrawPriority(selectedItem);
							break;
					}
					markerType = SINGLE_MARKER;
					inSelection = true;
				}
				else
				{
					inSelection = false;
					calculateMarker();
					unselectAll();
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

	public void paintBackground(final Graphics g)
	{
		final Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(EditorResources.BACKGROUND_COLOR);
	}

	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		paintBackground(g);
		drawSelector(g);
		drawItems(g);

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
				if (isSelected(item))
				{
					selectedItems.remove(item);
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
				for (final InteractiveCanvasItem item: items)
				{
					System.out.println("removing item: " + item.getClass().getName() + " cleanUp=" + cleanUp);
					removeItem(item, cleanUp);
				}
			}
		}
	}

	/**
	 * Saves the current state of the canvas to a file. E g size and position of items, and so on.
	 */
	public void saveGlobalState(final File parentDirectory, final String fileName)
	{

		// Create a directory if none exists
		if (!parentDirectory.isDirectory())
		{
			// creates even all parent dirs if necessary
			if (!parentDirectory.mkdirs())
			{
				System.err.println("Could not allocate settings dir: " + parentDirectory);
				return;
			}
		}

		// Hopefully we have valid directories ...
		final File dataFile = new File(parentDirectory, fileName);
		try
		{
			final FileOutputStream fos = new FileOutputStream(dataFile);
			final BufferedOutputStream bos = new BufferedOutputStream(fos);
			final ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(items);
			oos.close();
			bos.close();
			fos.close();
		}
		catch (final FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void setAvailable(final List<InteractiveCanvasItem> icItems, final int status)
	{
		for (final InteractiveCanvasItem item: icItems)
		{
			item.setAvailable(status);
		}
	}

	/**
	 * This sets the object to be drawn first, and hence under the rest.
	 */
	public void setBottomDrawPriority(final InteractiveCanvasItem item)
	{
		if (items.remove(item))
		{
			items.add(0, item);
		}
	}

	public void setSelectionType(final int type)
	{
		this.selectionType = type;
	}

	/**
	 * This sets the object to be drawn last, and over the rest.
	 */
	public void setTopDrawPriority(final InteractiveCanvasItem item)
	{
		if (items.remove(item))
		{
			items.add(item);
		}
	}

	public void sortItemsByDrawPriority()
	{
		Collections.sort(this.items, new Comparator<InteractiveCanvasItem>()
		{
			@Override
			public int compare(final InteractiveCanvasItem item1, final InteractiveCanvasItem item2)
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
			}

		});
	}

	public void startDaemon()
	{
		new InteractiveCanvasDaemon(this);
	}

	final void calculateMarker()
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

	boolean isSelected(final InteractiveCanvasItem item)
	{
		if (selectedItems != null && selectedItems.contains(item) && item.getSelected()) { return true; }
		return false;
	}

	protected void doOnDoubleClick(final MouseEvent me)
	{
	}

	protected void doOnMouseDragged(final MouseEvent me)
	{
	}

	protected void doOnMouseReleased(final int newPosX, final int newPosY)
	{

		// System.out.println("should be here");

		markerType = NO_MARKER;
		repaint();

	}

	/**
	 * Convenience class for processing upon translation of items.
	 */
	protected void doOnTranslate(final int newPosX, final int newPosY)
	{

	}

	protected void drawItems(final Graphics g)
	{
		if (items != null)
		{
			synchronized (items)
			{
				for(InteractiveCanvasItem item: items)
				{
					item.paintComponent(g);
				}
			}
		}
	}

	protected void drawSelector(final Graphics g)
	{

		if (markerType != NO_MARKER)
		{
			final Graphics2D g2 = (Graphics2D) g;
			switch (markerType)
			{
				case SINGLE_MARKER:
					break;
				case BOX_MARKER:
					final Stroke currentStroke = g2.getStroke();
					final Color currentColor = g.getColor();
					g2.setColor(Color.black);
					g2.setStroke(DASHED_STROKE);
					g2.drawRect(marker[0], marker[1], marker[2], marker[3]);
					g2.setColor(currentColor);
					g2.setStroke(currentStroke);
					break;
			}
		}
	}

	protected void insideMarker()
	{
		if (items == null) { return; }

		for(InteractiveCanvasItem item: items)
		{
			if (item.isInside(marker[0], marker[1], marker[0] + marker[2], marker[1] + marker[3]))
			{
				if (item.getSelectStatus() == InteractiveCanvasItem.UNSELECTED)
				{
					selectItem(item);
				}
			}
			else
			{
				if (item.getSelectStatus() == InteractiveCanvasItem.SELECTED)
				{
					unselectItem(item);
				}
			}
		}
	}

	/*
	 * public void setItems(Vector items) { this.items = items; }
	 */

	protected void selectItem(final InteractiveCanvasItem item)
	{
		if (selectedItems == null)
		{
			selectedItems = new ArrayList<InteractiveCanvasItem>();
		}

		item.setSelected(true);

		if (!(selectedItems.contains(item)))
		{
			selectedItems.add(item);
		}
	}

	protected void setMode(final int mode)
	{
		this.mode = mode;
	}

	protected void translateItems(final int newPosX, final int newPosY, final int oldPosX, final int oldPosY)
	{
		if (selectedItems != null)
		{
			final int dx = (newPosX - oldPosX);
			final int dy = (newPosY - oldPosY);
			for(InteractiveCanvasItem item: selectedItems)
			{
				item.translatePosition(dx, dy);

				item.doOnTranslate();
			}
		}
	}

	protected void unselectAll()
	{
		if (selectedItems != null)
		{
			synchronized (selectedItems)
			{
				for(InteractiveCanvasItem item: items)
				{
					item.setSelected(false);
				}
				selectedItems = null;
			}
		}
		this.inSelection = false;
	}

	protected void unselectItem(final InteractiveCanvasItem item)
	{
		if (selectedItems != null)
		{
			selectedItems.remove(item);
			if (selectedItems.size() < 1)
			{
				selectedItems = null;
			}
			item.setSelected(false);
		}
	}
}