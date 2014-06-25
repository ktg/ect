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

import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.dataspace.ComponentListener;
import equip.ect.apps.editor.dataspace.ComponentMetadataListener;
import equip.ect.apps.editor.dataspace.ComponentPropertyUpdateListener;
import equip.ect.apps.editor.dataspace.DataspaceConfigurationListener;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.interactive.InteractiveCanvas;
import equip.ect.apps.editor.interactive.InteractiveCanvasItem;

import javax.swing.JComponent;
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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeanGraphPanel extends InteractiveCanvas implements DropTargetListener, ComponentListener,
		ComponentPropertyUpdateListener, ComponentMetadataListener, DataspaceConfigurationListener
{

	private static final boolean dynamicCanvas = true;
	public static boolean animatePropertyUpdate = true;

	public static List<BeanCanvasItem> getBeanInstances(final String beanid, final Collection<InteractiveCanvasItem> beans)
	{
		List<BeanCanvasItem> results = null;
		if (beans != null)
		{
			synchronized (beans)
			{
				for (final InteractiveCanvasItem obj : beans)
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

	protected transient List<BeanCanvasItem> unattachableItems = new ArrayList<BeanCanvasItem>();
	protected transient List<BeanCanvasItem> attachableItems = new ArrayList<BeanCanvasItem>();
	private int dragging = 0;

	private static final boolean singleInstance = true;
	/**
	 * enables this component to be a dropTarget
	 */
	private DropTarget dropTarget = null;

	public BeanGraphPanel(final String title, final SelectionModel selectionModel)
	{
		super(title, Color.white, selectionModel);
		// create the trash icon

		// initiate drag and drop
		dropTarget = new DropTarget(this, this);

		setTransferHandler(new ComponentGUIDTransferHandler());
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

		final Collection<Capability> caps = DataspaceMonitor.getMonitor().getCapabilities();
		if (caps != null)
		{
			for (final Capability cap : caps)
			{
				capabilityAdded(cap);

			}
		}
		DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);
	}

	public void animateActiveItems(final List<? extends BeanCanvasItem> items)
	{
		new TimerPaint(items, 500).start();
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
	public void selectionChanged(Collection<String> selection)
	{
		for(InteractiveCanvasItem item: items)
		{
			boolean selected = false;
			if(item instanceof BeanCanvasItem)
			{
				String id = ((BeanCanvasItem) item).getBeanID();
				if(id != null && selection.contains(id))
				{
					selected = true;
				}
			}

			item.setSelected(selected);
		}
		super.selectionChanged(selection);
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
		componentMetadataChanged(metadata);
	}

	public void componentMetadataChanged(final Object metadata)
	{
		// to be extended
	}

	@Override
	public void componentMetadataDeleted(final Object metadata)
	{
		componentMetadataChanged(metadata);
	}

	@Override
	public void componentMetadataUpdated(final Object metadata)
	{
		componentMetadataChanged(metadata);
	}

	/*
	 * public BeanCanvasItem setBean(GUID beanID, int x, int y) { return null; }
	 */

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
	}

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
		if (dragging == 0)
		{
			dragging = 1;
		}
	}

	@Override
	public void doOnMouseReleased(final int x, final int y)
	{
		// stop dragging sound
		if (dragging != 0)
		{
			dragging = 0;
		}

		validateViewportSize();

		super.doOnMouseReleased(x, y);
	}

	@Override
	public void dragEnter(final DropTargetDragEvent event)
	{
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	@Override
	public void dragExit(final DropTargetEvent event)
	{
	}

	@Override
	public void dragOver(final DropTargetDragEvent event)
	{
	}

	/**
	 * a drop has occurred
	 */
	@Override
	public void drop(final DropTargetDropEvent event)
	{
		dragging = 0;

		try
		{
			final Transferable transferable = event.getTransferable();
			final Point location = event.getLocation();
			final int x = (int) location.getX();
			final int y = (int) location.getY();

			if (transferable.isDataFlavorSupported(ComponentGUIDTransferableSupport.componentGUIDDataFlavor))
			{
				final String componentGUID = (String) transferable.getTransferData(ComponentGUIDTransferableSupport.componentGUIDDataFlavor);

				if(singleInstance)
				{
					List<BeanCanvasItem> items = getBeanInstances(componentGUID);
					if(items != null && items.size() > 0)
					{
						event.rejectDrop();
						return;
					}
				}
				final BeanCanvasItem item = createItem(componentGUID, x, y);
				if (item == null)
				{
					event.rejectDrop();
					return;
				}

				selectionModel.set(item.getBeanID());
				event.acceptDrop(event.getDropAction());
				event.dropComplete(true);
			}
			else
			{
				event.rejectDrop();
			}
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
			event.rejectDrop();
		}
		catch (final UnsupportedFlavorException ufException)
		{
			ufException.printStackTrace();
			event.rejectDrop();
		}
	}

	public BeanCanvasItem createItem(String guid, int x, int y)
	{
		final BeanCanvasItem item = createFromGUID(guid);
		if(item != null)
		{
			item.setPosition(x, y);
			addItem(item);
		}
		return item;
	}

	protected BeanCanvasItem createFromGUID(String guid)
	{
		return null;
	}


	@Override
	public void dropActionChanged(final DropTargetDragEvent event)
	{
	}

	public List<BeanCanvasItem> getBeanInstances(final String beanid)
	{
		return getBeanInstances(beanid, getItems());
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
	}

	/**
	 * Filters out all possible pieces to attach to
	 */
	protected void filterValidAttachable(final BeanCanvasItem pp, final List<InteractiveCanvasItem> allBeanCanvasItems)
	{
		List<BeanCanvasItem> possible = null;
		for (final InteractiveCanvasItem obj : allBeanCanvasItems)
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
					foreignBit.setAvailable(false);
				}
			}
		}
	}
}
