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
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.apps.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.state.EditorID;

public class BeanGraphPanel extends InteractiveCanvas implements DropTargetListener, ComponentListener,
		ComponentPropertyUpdateListener, ComponentMetadataListener, DataspaceConfigurationListener
{

	public static List<BeanCanvasItem> getBeanInstances(final String beanid, final Collection<InteractiveCanvasItem> beans)
	{
		List<BeanCanvasItem> results = null;
		if (beans != null)
		{
			synchronized (beans)
			{
				for(final InteractiveCanvasItem obj: beans)
				{
					if (obj instanceof BeanCanvasItem)
					{
						final BeanCanvasItem item = (BeanCanvasItem) obj;
						if (beanid.equals(item.getBeanID()))
						{
							if (results == null)
							{
								results = new ArrayList<BeanCanvasItem>();
							}
							results.add(item);
						}
					}
				}
			}
		}
		return results;
	}

	private int dragging = 0;

	private boolean dynamicCanvas = true;

	/**
	 * enables this component to be a dropTarget
	 */
	private DropTarget dropTarget = null;

	protected TrashCanvasItem trash;

	public static boolean animatePropertyUpdate = true;

	protected transient List<BeanCanvasItem> unattachableItems = new ArrayList<BeanCanvasItem>();

	protected transient List<BeanCanvasItem> attachableItems = new ArrayList<BeanCanvasItem>();

	private List<EditorEventListener> editorEventListeners;

	private List<ItemMovementEventListener> itemMovementEventListeners;

	public BeanGraphPanel(final String title)
	{
		super(title, Color.white, InteractiveCanvas.MULTIPLE_SELECTION);
		// create the trash icon

		// initiate drag and drop
		dropTarget = new DropTarget(this, this);

		setTransferHandler(new BeanTransferHandler());
		/*
		 * MouseMotionListener ml = new MouseMotionAdapter() { public void mouseDragged(MouseEvent
		 * e) {
		 * 
		 * int x = e.getX(); int y = e.getY(); JComponent c = (JComponent)e.getSource();
		 * TransferHandler th = c.getTransferHandler(); th.exportAsDrag(c, e, TransferHandler.COPY);
		 * addMouseMotionListener(ml); } };
		 */

		if (dynamicCanvas)
		{
			final MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter()
			{
				@Override
				public void mouseDragged(final MouseEvent e)
				{
					final int x = e.getX();
					final int y = e.getY();
					final Rectangle r = new Rectangle(x, y, 1, 1);
					final JComponent source = (JComponent) e.getSource();
					source.scrollRectToVisible(r);
					int width = (int) source.getPreferredSize().getWidth();
					int height = (int) source.getPreferredSize().getHeight();
					boolean resize = false;
					if (x > width)
					{
						width = x;
						resize = true;
					}
					if (y > height)
					{
						height = y;
						resize = true;
					}

					if (resize)
					{
						source.setPreferredSize(new Dimension(width, height));
						source.revalidate();
						// TODO Should also repaint scroll parent,
						// but this might be too much
					}
				}
			};
			addMouseMotionListener(doScrollRectToVisible);
		}
		DataspaceMonitor.getMonitor().addComponentListener(this);
		DataspaceMonitor.getMonitor().addComponentPropertyListener(this);

		DataspaceMonitor.getMonitor().addComponentMetadataListener(this);

		final List<Capability> caps = DataspaceMonitor.getMonitor().getCapabilities();
		if (caps != null)
		{
			for(final Capability cap: caps)
			{
				capabilityAdded(cap);

			}
		}
		DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);
		// showTrash(true);
	}

	/*
	 * public void mouseExited(MouseEvent me) { if (dragging > 0 && inSelection) {
	 * //System.out.println("Mouse Exited In Selection, triggering drag"); JComponent c =
	 * (JComponent) me.getSource(); TransferHandler th = c.getTransferHandler(); th.exportAsDrag(c,
	 * me, TransferHandler.COPY); } else { //System.out.println("Mouse Exited"); } }
	 */

	public void addEditorEventListener(final EditorEventListener listener)
	{

		if (editorEventListeners == null)
		{
			editorEventListeners = new ArrayList<EditorEventListener>();
		}

		editorEventListeners.add(listener);
	}

	public void addItemMovementEventListener(final ItemMovementEventListener listener)
	{
		if (itemMovementEventListeners == null)
		{
			itemMovementEventListeners = new ArrayList<ItemMovementEventListener>();
		}

		itemMovementEventListeners.add(listener);
	}

	public void animateActiveItems(final List<? extends BeanCanvasItem> items)
	{
		new TimerPaint(items, 500).start();
		AudioManager.getAudioManager().playSoundResource("update");
	}

	@Override
	public void capabilityAdded(final Capability cap)
	{
	}

	@Override
	public void capabilityDeleted(final Capability cap)
	{
	}

	@Override
	public void capabilityUpdated(final Capability cap)
	{
	}

	@Override
	public void componentAdvertAdded(final ComponentAdvert compAdvert)
	{
	}

	@Override
	public void componentAdvertUpdated(ComponentAdvert compAd)
	{
		
	}

	@Override
	public void componentAdvertDeleted(final ComponentAdvert compAdvert)
	{
		final String beanid = compAdvert.getComponentID().toString();
		removeBeans(beanid, false);
	}

	@Override
	public void componentMetadataAdded(final Object metadata)
	{
		// TODO Auto-generated method stub
		componentMetadataChanged(metadata);
	}

	public void componentMetadataChanged(final Object metadata)
	{
		// to be extended
	}

	@Override
	public void componentMetadataDeleted(final Object metadata)
	{
		// TODO Auto-generated method stub
		componentMetadataChanged(metadata);
	}

	@Override
	public void componentMetadataUpdated(final Object metadata)
	{
		// TODO Auto-generated method stub
		componentMetadataChanged(metadata);
	}

	/*
	 * public BeanCanvasItem setBean(GUID beanID, int x, int y) { return null; }
	 */

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
	}

	/*
	 * public void removeSelectedItems() { Vector selectedItems = getSelectedItems(); if
	 * (selectedItems != null) { synchronized(selectedItems) { for (Enumeration e =
	 * selectedItems.elements(); e.hasMoreElements();) { Object obj = e.nextElement();
	 * 
	 * if (obj instanceof BeanCanvasItem) { BeanCanvasItem item = (BeanCanvasItem) obj;
	 * System.out.println("Removing item " + item.getName() + selectedItems.size());
	 * item.setSelected(false); removeItem(item); } }
	 * 
	 * selectedItems.clear(); selectedItems = null; } } }
	 */

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
	}

	@Override
	public void componentPropertyUpdated(final ComponentProperty compProp)
	{
		if (animatePropertyUpdate)
		{
			final List<BeanCanvasItem> instances = getBeanInstances(compProp.getComponentID().toString());
			animateActiveItems(instances);
		}
	}

	@Override
	public void componentRequestAdded(final ComponentRequest compReq)
	{
	}

	@Override
	public void componentRequestDeleted(final ComponentRequest compReq)
	{
	}

	@Override
	public void doOnMouseDragged(final MouseEvent me)
	{

		if (selectedItems != null)
		{
			// start dragging sound
			if (dragging == 0)
			{
				dragging = 1;
				// AudioManager.getAudioManager().playSoundResource("drag"+dragging);
			}
		}

	}

	@Override
	public void doOnMouseReleased(final int x, final int y)
	{
		// stop dragging sound
		if (selectedItems != null && dragging != 0)
		{
			AudioManager.getAudioManager().stopSoundResource("drag" + dragging, true);
			dragging = 0;
		}

		if (trash != null && trash.isInside(x, y))
		{
			unselectItem(trash);
			if (selectedItems != null)
			{
				removeItems(new ArrayList<InteractiveCanvasItem>(selectedItems), true);
			}
			AudioManager.getAudioManager().playSoundResource("trash");
		}

		validateViewportSize();

		super.doOnMouseReleased(x, y);
	}

	@Override
	public void dragEnter(final DropTargetDragEvent event)
	{
		// debug messages for diagnostics
		// System.out.println("dragEnter");
		/*
		 * if (event.getSource() == this.dropTarget) { System.out.println("rejecting");
		 * event.getDropTargetContext().rejectDrop(); return; }
		 */
		// System.out.println("Accepting drag " + event.getSource());
		event.acceptDrag(DnDConstants.ACTION_MOVE);

	}

	@Override
	public void dragExit(final DropTargetEvent event)
	{
		// System.out.println("drag exit");
	}

	@Override
	public void dragOver(final DropTargetDragEvent event)
	{
		// System.out.println("drag over");
	}

	/**
	 * a drop has occurred
	 * 
	 */
	@Override
	public void drop(final DropTargetDropEvent event)
	{
		dragging = 0;
		// System.out.println("source =" + event.getSource());
		/*
		 * if (event.getDropTargetContext().getComponent() == this) { event.rejectDrop(); return; }
		 */
		unselectAll();

		try
		{
			final Transferable transferable = event.getTransferable();
			final Point location = event.getLocation();
			final int x = (int) location.getX();
			final int y = (int) location.getY();

			if (transferable.isDataFlavorSupported(BeanCanvasItemTransferableSupport.beanCanvasItemDataFlavor))
			{
				final BeanCanvasItem template = (BeanCanvasItem) transferable
						.getTransferData(BeanCanvasItemTransferableSupport.beanCanvasItemDataFlavor);
				/**
				 * We now create a copy from the original template. NOTE: The drag mechanism creates
				 * already a copy of the original item from the source component. So creating
				 * another copy here is perhaps unecessary, but logical in design since we don't
				 * want a copy of the original template but a new instance based on the template
				 * deemed for purposes of the target drop component. The copy made by the drag
				 * mechanism will just get trash collected.
				 */
				final BeanCanvasItem piece = setBeanFromTemplate(template, x, y);
				selectItem(piece);
			} /*
			 * else if (transferable. isDataFlavorSupported(JigsawPieceTransferableSupport.
			 * beanGroupDataFlavor)) { BeanGroupSupport bcm = (BeanGroupSupport) transferable.
			 * getTransferData(JigsawPieceTransferableSupport. beanGroupDataFlavor); int offset =
			 * 100; JigsawPiece prev = null; unselectAll(); for (ListIterator it =
			 * bcm.getItemListIterator(); it.hasNext();) { JigsawPiece template = (JigsawPiece)
			 * it.next(); JigsawPiece current = setBeanFromTemplate(template, x, y); offset =
			 * current.getOffset(); if (prev != null) {
			 * current.attachViaAttractor(current.getInAttractor(), prev.getOutAttractor());
			 * current.getInAttractor().attach(prev.getOutAttractor(), true); } selectItem(current);
			 * current.setIsAttached(true); prev = current; x += (100 - offset); } }
			 */
			else
			{
				event.rejectDrop();
			}
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
			System.err.println("Exception" + exception.getMessage());
			event.rejectDrop();
		}
		catch (final UnsupportedFlavorException ufException)
		{
			ufException.printStackTrace();
			System.err.println("Exception" + ufException.getMessage());
			event.rejectDrop();
		}
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent event)
	{
		// System.out.println("drop action changed");
	}

	public BeanCanvasItem getBeanInstance(final EditorID editorID)
	{
		return getBeanInstance(editorID, getItems());
	}

	public BeanCanvasItem getBeanInstance(final EditorID editorID, final Collection<InteractiveCanvasItem> beans)
	{
		if (beans != null)
		{
			synchronized (beans)
			{
				for (InteractiveCanvasItem obj: beans)
				{
					if (obj instanceof BeanCanvasItem)
					{
						final BeanCanvasItem item = (BeanCanvasItem) obj;
						if (editorID.equals(item.getID())) { return item; }
					}
				}
			}
		}

		return null;
	}

	public List<BeanCanvasItem> getBeanInstances(final String beanid)
	{
		return getBeanInstances(beanid, getItems());
	}

	@Override
	public void mousePressed(final MouseEvent me)
	{
		switch (me.getModifiers())
		{
			case InputEvent.BUTTON1_MASK: // left mouse button

		}
		super.mousePressed(me);
		final InteractiveCanvasItem selectedItem = selectedItems != null && selectedItems.size() > 0 ? (InteractiveCanvasItem) selectedItems
				.get(0) : null;
		if (selectedItem != null && selectedItem instanceof BeanCanvasItem)
		{
			filterValidAttachable((BeanCanvasItem) selectedItem, items);
		}
	}

	@Override
	public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
	{

	}

	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
		removeBeans(linkReq.getID().toString());
	}

	@Override
	public void propertyLinkRequestUpdated(final PropertyLinkRequest linkReq)
	{

	}

	public void removeBeans(final String beanid)
	{
		removeBeans(beanid, true);
	}

	public void removeBeans(final String beanid, final boolean cleanUp)
	{
		final List<BeanCanvasItem> removals = getBeanInstances(beanid);
		System.out.println("Removing " + beanid);
		removeItems(removals, cleanUp);
		/*
		 * if (removals != null) { for (Enumeration e = removals.elements(); e.hasMoreElements();) {
		 * BeanCanvasItem piece = (BeanCanvasItem) e.nextElement(); removeItem(piece, cleanUp); } }
		 */
	}

	public void removeEditorEventListener(final EditorEventListener listener)
	{
		if (editorEventListeners == null) { return; }

		editorEventListeners.remove(listener);
		if (editorEventListeners.size() < 1)
		{
			editorEventListeners = null;
		}
	}

	public void removeItemMovementEventListener(final ItemMovementEventListener listener)
	{
		if (itemMovementEventListeners == null) { return; }

		itemMovementEventListeners.remove(listener);
		if (itemMovementEventListeners.size() < 1)
		{
			itemMovementEventListeners = null;
		}
	}

	public void removeSelectedItems()
	{
		removeItems(new ArrayList<InteractiveCanvasItem>(getSelectedItems()));
	}

	public BeanCanvasItem setBeanFromTemplate(final BeanCanvasItem template, final int x, final int y)
	{
		final BeanCanvasItem newBean = createFromTemplate(template);
		newBean.setPosition(x, y);

		addItem(newBean);

		/*
		 * BeanManager.getManager().addBeanPropertyListener(newBean.getBeanID(), this);
		 */
		return newBean;
	}

	public void showTrash(final boolean showTrash)
	{
		if (showTrash)
		{
			if (trash == null)
			{
				this.trash = new TrashCanvasItem(this);
				addItem(trash);
				trash.repaint();
			}
		}
		else
		{
			if (trash != null)
			{
				removeItem(trash);
				trash = null;
			}
		}
	}

	public void validateViewportSize()
	{
		final Rectangle bounds = getBounds();
		setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));
		revalidate();
	}

	protected BeanCanvasItem createFromTemplate(final BeanCanvasItem template)
	{
		final BeanCanvasItem newBean = (BeanCanvasItem) template.clone(this);
		newBean.setTargetCanvas(this);
		// newBean.setSize(100, 100);
		return newBean;
	}

	@Override
	protected void doOnDoubleClick(final MouseEvent me)
	{
		//final InteractiveCanvasItem selectedItem = getItem(me.getX(), me.getY(), BeanCanvasItem.class);
		//final BeanCanvasItem item = (BeanCanvasItem) selectedItem;
		/*
		 * BeanAccessDialog bad = new BeanAccessDialog(((BeanCanvasItem)selectedItem).getBeanID());
		 * bad.pack(); bad.setVisible(true);
		 */
	}

	@Override
	protected void doOnTranslate(final int newPosX, final int newPosY)
	{
		// check if inside trash
		if (trash != null)
		{
			if (trash.isInside(newPosX, newPosY))
			{
				if (trash.getSelectStatus() == InteractiveCanvasItem.UNSELECTED)
				{
					trash.setSelected(InteractiveCanvasItem.SELECTED);
				}
			}
			else if (trash.getSelectStatus() == InteractiveCanvasItem.SELECTED)
			{
				trash.setSelected(InteractiveCanvasItem.UNSELECTED);
			}
		}
	}

	/**
	 * Filters out all possible pieces to attach to
	 */
	protected void filterValidAttachable(final BeanCanvasItem pp, final List<InteractiveCanvasItem> allBeanCanvasItems)
	{
		List<BeanCanvasItem> possible = null;
		for (final InteractiveCanvasItem obj: allBeanCanvasItems)
		{
			if (obj instanceof BeanCanvasItem)
			{
				final BeanCanvasItem foreignBit = (BeanCanvasItem) obj;
				if (foreignBit == pp)
				{ // ignore self check
					continue;
				}

				if (pp.isAttachable(foreignBit) || foreignBit.isAttachable(pp))
				{
					if (possible == null)
					{
						possible = new ArrayList<BeanCanvasItem>();
					}
					attachableItems.add(foreignBit);
				}
				else
				{
					unattachableItems.add(foreignBit);
					foreignBit.setAvailable(InteractiveCanvasItem.UNAVAILABLE);
				}
			}
		}
	}

	protected final void fireEditorEvent(final EditorEvent event, final String callback)
	{

		if (editorEventListeners == null) { return; }
		final java.lang.reflect.Method method;
		try
		{
			method = EditorEventListener.class.getMethod(callback, new Class[] { EditorEvent.class });
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return;
		}
		new Thread()
		{
			@Override
			public void run()
			{
				for(EditorEventListener listener: editorEventListeners)
				{
					try
					{
						method.invoke(listener, new Object[] { event });
					}
					catch (final Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	protected final void fireItemMovementEvent(final ItemMovementEvent event, final String callback)
	{

		if (itemMovementEventListeners == null) { return; }
		final java.lang.reflect.Method method;
		try
		{
			method = ItemMovementEventListener.class.getMethod(callback, new Class[] { ItemMovementEvent.class });
		}
		catch (final Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		new Thread()
		{
			@Override
			public void run()
			{
				for(ItemMovementEventListener listener: itemMovementEventListeners)
				{
					try
					{
						method.invoke(listener, new Object[] { event });
					}
					catch (final Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	protected void restoreItemDefaultStates()
	{
		// Restore items marked unavailable to default
		if (unattachableItems != null)
		{
			for (final Iterator<BeanCanvasItem> it = unattachableItems.iterator(); it.hasNext();)
			{
				it.next().setAvailable(InteractiveCanvasItem.AVAILABLE);
			}
		}
	}
}
