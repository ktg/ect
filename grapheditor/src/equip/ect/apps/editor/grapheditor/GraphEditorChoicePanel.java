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
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.apps.editor.grapheditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import equip.data.GUID;
import equip.data.StringBox;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.BeanCellRenderer;
import equip.ect.apps.editor.BeanChoicePanel;
import equip.ect.apps.editor.BeanListModel;
import equip.ect.apps.editor.ComponentMetadataStore;
import equip.ect.apps.editor.DataspaceMonitor;
import equip.ect.apps.editor.DataspaceUtils;
import equip.ect.apps.editor.DocsDialog;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.InteractiveCanvas;

public class GraphEditorChoicePanel extends BeanChoicePanel
{

	public static class TooltipBeanCellRenderer extends BeanCellRenderer
	{

		// This is the only method defined by ListCellRenderer.
		// We just reconfigure the JLabel each time we're called.
		@Override
		public Component getListCellRendererComponent(final JList list, final Object value, // value
				// to
				// display
				final int index, // cell index
				final boolean isSelected, final boolean cellHasFocus)
		{

			if (value instanceof GraphComponent)
			{
				final GraphComponent gc = (GraphComponent) value;
				// component
				final ComponentAdvert ad = gc.getComponentAdvert();
				final java.lang.Object val = ad.getAttributeValue(BeanDescriptorHelper.SHORT_DESCRIPTION);
				String text = null;
				if (val instanceof StringBox)
				{
					text = ((StringBox) val).value;
				}
				setToolTipText(text);
			}
			else
			{
				System.out.println("List value = " + value);
				setToolTipText(null);
			}

			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	class ComponentPopupMenu extends JPopupMenu
	{

		//private final BeanCanvasItem component;

		ComponentPopupMenu(final BeanCanvasItem component)
		{
			super("Component Menu");
			//this.component = component;
			add(new AbstractAction("View Documentation")
			{

				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					final ComponentAdvert ca = DataspaceMonitor.getMonitor().getComponentAdvert(component.getBeanID());
					if (ca != null)
					{
						final Capability cap = DataspaceMonitor.getMonitor().getComponentCapability(ca);
						if (cap != null)
						{
							new DocsDialog(null, cap);
						}
					}
				}

			});
			add(new AbstractAction("Delete request")
			{

				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					final ComponentAdvert ca = DataspaceMonitor.getMonitor().getComponentAdvert(component.getBeanID());
					if (ca != null)
					{
						deleteComponentRequest(ca.getComponentRequestID());
					}
				}

			});

		}

		private void deleteComponentRequest(final GUID id)
		{
			try
			{
				System.err.println("Delete ComponentRequest " + id);
				DataspaceMonitor.getMonitor().getDataspace().delete(id);
			}
			catch (final DataspaceInactiveException e)
			{
				System.err.println("deleteComponentRequest: " + e);
			}
		}
	}

	// private Hashtable pendingMetadata = new Hashtable();
	private ComponentMetadataStore cms = new ComponentMetadataStore();

	private Map<String, GraphComponent> pendingCapabilities = new HashMap<String, GraphComponent>();

	public GraphEditorChoicePanel()
	{
		super();
		beanList.setCellRenderer(new TooltipBeanCellRenderer());
		// beanList.add(new ComponentPopupMenu());
		beanList.addMouseListener(new MouseAdapter()
		{

			private GraphComponent previousSelection;

			@Override
			public void mousePressed(final MouseEvent me)
			{
				if (me.getButton() == MouseEvent.BUTTON3)
				{
					final BeanCanvasItem component = getListElement(me);
					if (component != null)
					{
						final JPopupMenu pm = new ComponentPopupMenu(component);
						pm.show(GraphEditorChoicePanel.this, me.getX(), me.getY());
					}
				}
				else
				{
					super.mousePressed(me);
					if (me.getButton() == MouseEvent.BUTTON1)
					{
						final GraphComponent gc = (GraphComponent) getListElement(me);
						if (previousSelection != null && previousSelection != gc)
						{
							previousSelection.setHighlighted(false);
							final InteractiveCanvas[] canvases = GraphEditor.getInstance().getCanvases();
							for (final InteractiveCanvas canvase : canvases)
							{
								((GraphEditorCanvas) canvase).setHighlightComponents(	previousSelection.getBeanID(),
																						false);
							}
						}
						if (gc != null)
						{
							gc.setHighlighted(true);
							final InteractiveCanvas[] canvases = GraphEditor.getInstance().getCanvases();
							for (final InteractiveCanvas canvase : canvases)
							{
								((GraphEditorCanvas) canvase).setHighlightComponents(gc.getBeanID(), true);
							}
							previousSelection = gc;
						}
					}
				}
			}

			private BeanCanvasItem getListElement(final MouseEvent me)
			{
				final int index = beanList.locationToIndex(me.getPoint());
				/*
				 * the index does not ensure the point is within a particulat component but returns
				 * the closest one. We need to ensure this.
				 */
				if (beanList.getCellBounds(index, index).contains(me.getPoint()))
				{
					final BeanCanvasItem component = (BeanCanvasItem) beanList.getModel().getElementAt(index);
					return component;
				}
				return null;
			}
		});
	}

	@Override
	public void componentAdvertAdded(final ComponentAdvert compAdvert)
	{
		super.componentAdvertAdded(compAdvert);

		final GUID guid = compAdvert.getComponentID();

		cms.replayMetadata(guid, this);

		/*
		 * 
		 * Object metadata = pendingMetadata.remove(compAdvert.getComponentID() .toString()); if
		 * (metadata != null) { componentMetadataChanged(metadata); }
		 */
	}

	@Override
	public void componentAdvertUpdated(final ComponentAdvert compAdvert)
	{
		final String beanid = compAdvert.getComponentID().toString();
		final GraphComponent item = (GraphComponent) templates.get(beanid);
		final String currentName = DataspaceUtils.getCurrentName(compAdvert);
		if (!item.getName().equals(currentName))
		{
			synchronized (item)
			{
				item.setName(currentName);
				item.update();
				repaint();
			}
		}
	}

	
	@Override
	public void componentAdvertDeleted(final ComponentAdvert compAdvert)
	{
		final String beanid = compAdvert.getComponentID().toString();
		final BeanCanvasItem removal = (BeanCanvasItem) templates.get(beanid);
		((BeanListModel) beanList.getModel()).removeElement(removal);
		// remove from editor as well
		templates.remove(beanid);
	}

	@Override
	public void componentMetadataAdded(final Object metadata)
	{

		componentMetadataChanged(metadata, ComponentMetadataStore.META_DATA_ADDED);

	}

	public void componentMetadataChanged(final Object metadata, final String eventType)
	{
		if (metadata instanceof RDFStatement)
		{
			final RDFStatement rdf = (RDFStatement) metadata;
			if (rdf.getPredicate().equals(RDFStatement.ECT_ACTIVE_TITLE))
			{
				processActiveNameChange(rdf, eventType);
			}
		}
	}

	@Override
	public void componentMetadataDeleted(final Object metadata)
	{
		componentMetadataChanged(metadata, ComponentMetadataStore.META_DATA_DELETED);
	}

	@Override
	public void componentMetadataUpdated(final Object metadata)
	{
		componentMetadataChanged(metadata, ComponentMetadataStore.META_DATA_UPDATED);

	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
		final String beanid = compProp.getComponentID().toString();
		final GraphComponent gc = (GraphComponent) templates.get(beanid);
		if (gc != null)
		{
			synchronized (gc)
			{
				gc.addGraphComponentProperty(compProp);
			}
		}
	}

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
		final String beanid = compProp.getComponentID().toString();
		final GraphComponent gc = (GraphComponent) templates.get(beanid);
		if (gc != null)
		{
			synchronized (gc)
			{
				gc.removeGraphComponentProperty(compProp);
				gc.cleanUp();
			}
		}
	}

	@Override
	public void componentPropertyUpdated(final ComponentProperty compProp)
	{
		final String beanid = compProp.getComponentID().toString();
		final GraphComponent gc = (GraphComponent) templates.get(beanid);
		if (gc != null)
		{
			synchronized (gc)
			{
				final GraphComponentProperty gcp = gc.getGraphComponentProperty(compProp.getID().toString());
				if (gcp != null)
				{
					gcp.updateValue(compProp);
				}
			}
		}
	}

	@Override
	public BeanCanvasItem createComponentTemplate(final ComponentAdvert compAdv)
	{

		final String currentName = DataspaceUtils.getCurrentName(compAdv);

		final String beanID = compAdv.getID().toString();
		final String hostName = DataspaceUtils.getHostID(compAdv, DataspaceMonitor.getMonitor().getDataspace());

		final GraphComponent newComp = new GraphComponent(this, beanID, currentName, hostName);

		if (hostName == null)
		{
			if (compAdv.getCapabilityID() != null)
			{
				pendingCapabilities.put(beanID, newComp);
			}
			else
			{
				Info.message(this, "Warning => ComponentAdvert without capability ID!");
			}
		}

		return newComp;
	}

	protected void processActiveNameChange(final RDFStatement rdf, final String eventType)
	{
		// metadata relating to the name of the component
		// has changed, so update its name in the graph editor choice panel

		final GUID beanid = RDFStatement.urlToGUID(rdf.getSubject());

		final ComponentAdvert compAdv = DataspaceMonitor.getMonitor().getComponentAdvert(beanid.toString());

		if (compAdv != null)
		{
			// get the current name

			final String currentName = DataspaceUtils.getCurrentName(	compAdv);

			// now locate the component and set its name

			final GraphComponent gc = (GraphComponent) templates.get(beanid.toString());
			if (gc != null)
			{
				if (!gc.getName().equals(currentName))
				{
					synchronized (gc)
					{
						gc.setName(currentName);
						gc.update();
						repaint();
					}
				}
			}
			else
			{
				// if graph component has not yet
				// been added, save metadata until it has
				cms.addItem(beanid, eventType, rdf);
				// pendingMetadata.put(beanid, rdf);
			}
		}
		else
		{
			// if component advert has not yet been added,
			// save metadata until it has
			cms.addItem(beanid, eventType, rdf);
		}
	}
}
