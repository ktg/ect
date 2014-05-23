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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)

 */
package equip.ect.apps.editor.grapheditor;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import equip.data.GUID;
import equip.data.StringBox;
import equip.data.StringBoxImpl;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.AudioManager;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.DataspaceMonitor;
import equip.ect.apps.editor.DataspaceUtils;
import equip.ect.apps.editor.DocsDialog;
import equip.ect.apps.editor.EditorResources;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.InteractiveCanvasItem;
import equip.ect.apps.editor.Link;
import equip.ect.apps.editor.LinkGroup;

/**
 * The GraphEditorCanvas is the main editing component in which component properties are linked
 * together.
 * 
 * @author humble
 * 
 */
public class GraphEditorCanvas extends BeanGraphPanel
{

	/** ************** INNER CLASSES ******************************* */
	class CanvasPopupMenu extends JPopupMenu
	{

		CanvasPopupMenu()
		{
			super("Canvas Menu");

			add(new AbstractAction("Refresh")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					GraphEditorCanvas.this.repaint();
				}
			});
		}
	}

	class ComponentPopupMenu extends JPopupMenu
	{

		//private final InteractiveCanvasItem component;

		ComponentPopupMenu(final InteractiveCanvasItem component)
		{
			super("Graph Component");
			//this.component = component;

			add(new AbstractAction("Edit Name...")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					if (component instanceof GraphComponent)
					{
						GraphComponent graphComp = (GraphComponent) component;
						ComponentAdvert advert = graphComp.getComponentAdvert();
						String name = JOptionPane.showInputDialog("Edit Name:", DataspaceUtils.getCurrentName(advert));
						if(name != null)
						{
							try
							{
								StringBox value = new StringBoxImpl(name);
								advert.setAttribute(BeanDescriptorHelper.DISPLAY_NAME, value);
								advert.updateinDataSpace(DataspaceMonitor.getMonitor().getDataspace());
								
								graphComp.setName(name);
								graphComp.update();		
								graphComp.repaint();
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}				
			});
			
			add(new AbstractAction("Settings...")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					if (component instanceof GraphComponent)
					{
						showComponentSettings(((GraphComponent) component), getX(), getY());
					}
				}
			});

			add(new AbstractAction("View Documentation")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					if (component instanceof GraphComponent)
					{
						final ComponentAdvert ca = DataspaceMonitor.getMonitor()
								.getComponentAdvert(((GraphComponent) component).getBeanID());
						if (ca != null)
						{
							final Capability cap = DataspaceMonitor.getMonitor().getComponentCapability(ca);
							if (cap != null)
							{
								new DocsDialog(null, cap);
							}
						}
					}
				}
			});

			add(new AbstractAction("Remove from canvas")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					if (component instanceof GraphComponent)
					{
						removeItem(component, true);
					}
				}
			});

		}
	}

	class LinkPopupMenu extends JPopupMenu
	{

		//private final Link link;

		LinkPopupMenu(final Link link)
		{
			super("Link");
			//this.link = link;

			add(new AbstractAction("Delete")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					if (link instanceof GraphEditorLink)
					{
						final PropertyLinkRequest linkReq = link.getLinkRequest();
						if (linkReq != null)
						{
							try
							{
								DataspaceMonitor.getMonitor().getDataspace().delete(linkReq.getID());
							}
							catch (final DataspaceInactiveException ex)
							{
								Info.message("Warning: Error removing link from dataspace");
								Info.message(ex.getMessage());
							}
						}
					}
					else if (link instanceof GraphEditorLinkGroup)
					{
						for(Link linkItem: ((GraphEditorLinkGroup) link).getLinks())
						{
							final PropertyLinkRequest linkReq = linkItem.getLinkRequest();
							if (linkReq != null)
							{
								try
								{
									DataspaceMonitor.getMonitor().getDataspace().delete(linkReq.getID());
								}
								catch (final DataspaceInactiveException ex)
								{
									Info.message("Warning: Error removing link from dataspace");
									Info.message(ex.getMessage());
								}
							}
						}
					}
					removeItem(link);

				}
			});

			add(new AbstractAction("Remove from canvas")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					removeItem(link, true);
				}
			});
		}
	}

	class PropertyPopupMenu extends JPopupMenu
	{

		private final GraphComponentProperty prop;

		PropertyPopupMenu(final GraphComponentProperty gcp)
		{
			super(gcp.getName());
			this.prop = gcp;

			//final ComponentProperty cp = gcp.getComponentProperty();

			add(new AbstractAction("Examine value ...")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					new SetValuePopup(null, DataspaceMonitor.getMonitor().getDataspace(), gcp.getComponentProperty());
				}
			});

			addSeparator();

			add(new AbstractAction("Delete inputs")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					removeItems(prop.getInputLinks());
					prop.removeAllInputLinks();
					prop.setSelected(false);
				}
			});
			add(new AbstractAction("Delete outputs")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					removeItems(prop.getOutputLinks());
					prop.removeAllOutputLinks();
					prop.setSelected(false);
				}
			});

			add(new AbstractAction("Delete All")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					removeItems(prop.getInputLinks());
					removeItems(prop.getOutputLinks());
					prop.removeAllLinks();
					prop.setSelected(false);
				}
			});
			addSeparator();

			final JCheckBox cb = new JCheckBox("Keep visible", prop.keepVisible());
			cb.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					prop.setKeepVisible(cb.isSelected());
				}
			});

			add(cb);
			addSeparator();
			{
				if (CompoundComponentEditor.isCompoundComponent(prop.getParent().getComponentAdvert()))
				{
					add(new AbstractAction("Delete property (composite component)")
					{
						@Override
						public void actionPerformed(final ActionEvent ae)
						{
							CompoundComponentEditor.deleteProperty(prop.getComponentProperty().getID());
						}
					});
				}
				else
				{
					add(new AbstractAction("Add to a compound component...")
					{
						@Override
						public void actionPerformed(final ActionEvent ae)
						{
							CompoundComponentEditor.handleAddProperty(prop.getComponentProperty());
						}
					});
				}
			}

		}
	}

	public final static int LINK_MODE = 1;

	public final static int LINK_DRAG_MODE = 2;

	public final static int DRAWER_MODE = 3;

	public final static int MENU_MODE = 4;

	private Link currentLink;

	private GraphComponentProperty currentAnchor, currentTarget;

	public static boolean allowComponentSelfConnect = false;

	private Map<String, Component> componentDialogs = new HashMap<String, Component>();

	public GraphEditorCanvas(final String title)
	{
		super(title, EditorResources.BACKGROUND_TEXTURE);

		final AudioManager am = AudioManager.getAudioManager();
		am.addResource("drag1", EditorResources.DRAG_SOUND1);
		am.addResource("drag2", EditorResources.DRAG_SOUND2);
		am.addResource("drag3", EditorResources.DRAG_SOUND3);
		am.addResource("trash", EditorResources.TRASH_SOUND);
		am.addResource("update", EditorResources.UPDATE_SOUND);
		showTrash(true);
		// need to call this to enable tooltips
		this.setToolTipText("");
	}

	@Override
	public void componentMetadataChanged(final Object metadata)
	{
		// System.out.println("Metadata changed graph editor canvas");

		if (metadata instanceof RDFStatement)
		{
			final RDFStatement rdf = (RDFStatement) metadata;
			if (rdf.getPredicate().equals(RDFStatement.ECT_ACTIVE_TITLE))
			{
				processActiveNameChange(rdf);
			}
		}
	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
		final String beanid = compProp.getComponentID().toString();
		final List<BeanCanvasItem> comps = getBeanInstances(beanid);

		if (comps != null)
		{
			synchronized (comps)
			{

				for(BeanCanvasItem item: comps)
				{
					final GraphComponent gc = (GraphComponent) item;
					gc.addGraphComponentProperty(compProp);

					gc.update();
					gc.repaint();

				}
			}
		}
	}

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
		final String beanid = compProp.getComponentID().toString();
		final List<BeanCanvasItem> comps = getBeanInstances(beanid);
		if (comps != null)
		{
			for(BeanCanvasItem item: comps)
			{
				final GraphComponent gc = (GraphComponent) item;
				gc.removeGraphComponentProperty(compProp);
				gc.update();
				gc.repaint();
			}
		}
	}

	@Override
	public void componentPropertyUpdated(final ComponentProperty compProp)
	{
		if (animatePropertyUpdate)
		{
			final List<GraphComponentProperty> graphProps = getGraphComponentProperties(compProp);
			if (graphProps != null)
			{
				synchronized (graphProps)
				{
					for(GraphComponentProperty gcp: graphProps)
					{
						gcp.componentPropertyUpdated(compProp);
						gcp.repaint();
					}
				}
				animateActiveItems(graphProps);
			}
		}
	}

	public List<GraphComponentProperty> getGraphComponentProperties(final ComponentProperty compProp)
	{
		if(compProp == null || compProp.getID() == null)
		{
			return null;
		}
		return getGraphComponentProperties(compProp.getID().toString());
	}

	public List<GraphComponentProperty> getGraphComponentProperties(final String compPropID)
	{
		List<GraphComponentProperty> results = null;
		if (items != null)
		{
			for(InteractiveCanvasItem item: items)
			{
				if (item instanceof GraphComponent)
				{
					final GraphComponentProperty gcp = ((GraphComponent) item)
							.getGraphComponentProperties().get(compPropID);
					if (gcp != null)
					{
						if (results == null)
						{
							results = new ArrayList<GraphComponentProperty>();
						}
						results.add(gcp);
					}
				}
			}
		}
		return results;
	}

	/**
	 * custom tooltip
	 */
	@Override
	public String getToolTipText(final MouseEvent event)
	{
		final int xPos = event.getX();
		final int yPos = event.getY();
		final InteractiveCanvasItem item = getItem(xPos, yPos);
		// System.out.println("Get tooltip for "+event+" at
		// "+event.getX()+","+event.getY()+" ("+selectedItem+")...");
		if (item instanceof GraphComponent)
		{
			final GraphComponent gc = (GraphComponent) item;
			final GraphComponentProperty gcp = gc.getGraphComponentProperty(xPos, yPos);
			if (gcp == null)
			{
				// component
				final ComponentAdvert ad = gc.getComponentAdvert();
				final java.lang.Object val = ad.getAttributeValue(BeanDescriptorHelper.SHORT_DESCRIPTION);
				String text = null;
				if (val instanceof StringBox)
				{
					text = ((StringBox) val).value;
				}
				return text;
			}
			else
			{
				/*
				 * ComponentProperty p = gcp.getComponentProperty(); java.lang.Object val = p
				 * .getAttributeValue(ect.BeanDescriptorHelper.SHORT_DESCRIPTION); String text
				 * = null; if (val instanceof StringBox) text = ((StringBox) val).value;
				 * 
				 * return text;
				 */
				return gcp.getDisplayValue();
			}
		}
		return getToolTipText();
	}

	@Override
	public void mouseDragged(final MouseEvent me)
	{
		super.mouseDragged(me);
		final Point end = new Point(xPos, yPos);
		switch (me.getModifiers())
		{
			case InputEvent.BUTTON1_MASK: // left
				switch (mode)
				{

					case LINK_MODE:
						mode = LINK_DRAG_MODE;
						final Point start = currentAnchor.getOutAnchorPoint();
						currentLink = new GraphEditorLink(this, start, end, currentAnchor, null);
						addItem(currentLink);
						currentLink.repaint();
						break;

					case LINK_DRAG_MODE:
						currentLink.setEndPoint(end);
						final GraphComponentProperty gcp = getGraphComponentProperty(xPos, yPos);
						if (gcp != null)
						{
							if (currentTarget != null)
							{
								if (gcp != currentTarget)
								{
									currentTarget.setSelected(false);
									currentTarget = null;
									if (checkIfCanConnect(currentAnchor, gcp))
									{ // no
										// self
										// links
										currentTarget = gcp;
										currentTarget.setSelected(true);
									}
								}
							}
							else if (gcp != currentAnchor && checkIfCanConnect(currentAnchor, gcp))
							{
								currentTarget = gcp;
								currentTarget.setSelected(true);
							}
						}
						else
						{
							if (currentTarget != null)
							{
								currentTarget.setSelected(false);
								currentTarget = null;
							}
						}

						currentLink.repaint(); // optimize
						break;
				}
				break;

			case InputEvent.BUTTON2_MASK:
				break;

			case InputEvent.BUTTON3_MASK:

				break;
		}
	}

	@Override
	public void mousePressed(final MouseEvent me)
	{
		xPos = me.getX();
		yPos = me.getY();
		switch (me.getModifiers())
		{
			case InputEvent.BUTTON1_MASK: // left
				switch (mode)
				{
					case NORMAL_MODE:
						final GraphComponent gc = getGraphComponent(xPos, yPos);
						if (gc != null)
						{
							final GraphComponentProperty gcp = gc.getGraphComponentProperty(xPos, yPos);
							if (gcp != null)
							{
								currentAnchor = gcp;
								gcp.setSelected(true);
								mode = LINK_MODE;
							}
							else
							{
								final int drawerAction = gc.getDrawer().getAction(xPos, yPos);
								if (drawerAction != Drawer.NONE)
								{
									gc.handleDrawerAction(drawerAction);
									mode = DRAWER_MODE;
								}
								else
								{
									super.mousePressed(me);
								}
							}
						}
						else
						{ // no property selected
							super.mousePressed(me);
						}
						break;

					case LINK_MODE:
						final GraphComponentProperty gcp = getGraphComponentProperty(xPos, yPos);
						if (gcp != null)
						{
							gcp.setSelected(true);
							currentTarget = gcp;
						}
						else if (currentTarget != null)
						{
							currentTarget.setSelected(false);
							currentTarget = null;
						}
						break;
				}

				break;
			case InputEvent.BUTTON2_MASK: // middle
				break;

			case InputEvent.BUTTON3_MASK: // right
				if (mode == LINK_MODE)
				{
					if (currentAnchor != null)
					{
						currentAnchor.setSelected(false);
						currentAnchor = null;
					}
				}
				mode = MENU_MODE;
				break;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent me)
	{
		xPos = me.getX();
		yPos = me.getY();
		switch (me.getModifiers())
		{

			case InputEvent.BUTTON1_MASK:
				switch (mode)
				{
					case LINK_MODE:
						if (currentAnchor != null)
						{
							// Check if within another component
							final GraphComponentProperty gcp = getGraphComponentProperty(xPos, yPos);
							if (gcp != null)
							{ // some property selected
								if (currentTarget != null)
								{ // target already
									// selected on mouse
									// pressed
									if (currentTarget == gcp)
									{ // same as when clicked
										if (checkIfCanConnect(currentAnchor, gcp))
										{
											final Point start = currentAnchor.getOutAnchorPoint();
											final Point end = gcp.getInAnchorPoint();
											final Link link = new GraphEditorLink(this, start, end, currentAnchor, gcp);
											addItem(link);
											link.repaint();
											connect(currentAnchor, gcp, link);
											currentAnchor.setSelected(false);
											// currentAnchor = null;
											// mode = NORMAL_MODE;
										}
									}
									currentTarget.setSelected(false);
									currentTarget = null;

									currentAnchor.setSelected(false);
									currentAnchor = null;
									mode = NORMAL_MODE;
								}
							}
							else
							{ // no new selection so reset
								currentAnchor.setSelected(false);
								currentAnchor = null;
								mode = NORMAL_MODE;
							}
						}
						break;
					case LINK_DRAG_MODE:
						if (currentAnchor != null)
						{
							if (currentLink != null)
							{
								// Check if within another component
								final GraphComponentProperty gcp = getGraphComponentProperty(xPos, yPos);
								if (gcp != null)
								{
									if (checkIfCanConnect(currentAnchor, gcp))
									{
										currentLink.setEndPoint(gcp.getInAnchorPoint());
										currentTarget = gcp;
										if (connect(currentAnchor, currentTarget, currentLink))
										{
											currentTarget.setSelected(false);
											currentLink = null;
										}
										currentTarget = null;
									}
									else
									{
										removeItem(currentLink);
										currentLink = null;
									}
								}
								else
								{ // nothing to attach to, so remove
									removeItem(currentLink);
									currentLink = null;
								}
							}
							currentAnchor.setSelected(false);
							currentAnchor = null;
						}

						mode = NORMAL_MODE;
						repaint();
						break;

					case DRAWER_MODE:
						mode = NORMAL_MODE;
						repaint();
						break;
					default:
						super.mouseReleased(me);
				}
				break;

			case InputEvent.BUTTON2_MASK:
				mode = NORMAL_MODE;
				super.mouseReleased(me);
				break;

			case InputEvent.BUTTON3_MASK:

				if (mode == MENU_MODE)
				{
					final BeanCanvasItem item = (BeanCanvasItem) getItem(xPos, yPos, BeanCanvasItem.class);
					if (item instanceof Link)
					{
						final Link link = (Link) item;
						link.setSelected(InteractiveCanvasItem.SELECTED);
						final LinkPopupMenu popup = new LinkPopupMenu(link);
						popup.show(this, xPos, yPos);
					}
					else if (item instanceof GraphComponent)
					{
						final GraphComponent gc = (GraphComponent) item;
						final GraphComponentProperty gcp = gc.getGraphComponentProperty(xPos, yPos);
						// check first if the selection is a property
						if (gcp != null)
						{
							// gcp.setSelected(true);
							final PropertyPopupMenu popup = new PropertyPopupMenu(gcp);
							popup.show(this, xPos, yPos);

						}
						else
						{
							// if not then just show the common settings for
							// component
							final ComponentPopupMenu popup = new ComponentPopupMenu(gc);
							popup.show(this, xPos, yPos);
						}
					}
					else
					{
						final CanvasPopupMenu popup = new CanvasPopupMenu();
						popup.show(this, xPos, yPos);
					}
				}
				else
				{
					super.mouseReleased(me);
				}
				mode = NORMAL_MODE;

				break;
		}
	}

	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
		System.out.println("Proerty link request deleted");
		final String beanid = linkReq.getID().toString();

		final List<LinkGroup> allLinkGroups = getItems(LinkGroup.class);
		if (allLinkGroups != null)
		{
			for(LinkGroup linkGroup: allLinkGroups)
			{
				final Link link = linkGroup.getLink(beanid);
				if (link != null)
				{
					// link request now non existent,
					// so remove to avoid double cleanup
					link.setLinkRequest(null);
					link.cleanUp();
					linkGroup.removeLink(link);
					if (linkGroup.nrLinks() < 1)
					{ // just one item, remove
						// System.out.println("Removing the link group");
						removeItem(linkGroup, false); // this should call the
						// links cleanUp as well
					}
				}
			}
		}
		removeBeans(beanid, true);
	}

	@Override
	public void removeItem(final InteractiveCanvasItem item, final boolean cleanUp)
	{
		if (item instanceof GraphComponent)
		{
			final GraphComponent gc = (GraphComponent) item;
			// Links are canvas items, so we need to remove these first

			final Map<String, GraphComponentProperty> props = gc.getGraphComponentProperties();
			final Map<GraphComponent, LinkGroup> outLinkGroups = gc.getOutLinkGroups();

			if (outLinkGroups != null)
			{
				removeItems(new ArrayList<LinkGroup>(outLinkGroups.values()), false);
			}

			final Map<GraphComponent, LinkGroup> inLinkGroups = gc.getInLinkGroups();
			if (inLinkGroups != null)
			{
				removeItems(new ArrayList<LinkGroup>(inLinkGroups.values()), false);
			}

			synchronized (props)
			{
				if (props != null)
				{
					for(GraphComponentProperty gcp: props.values())
					{
						List<Link> links = gcp.getInputLinks();
						if (links != null)
						{
							removeItems(new ArrayList<Link>(links), cleanUp);
						}
						links = gcp.getOutputLinks();
						if (links != null)
						{
							removeItems(new ArrayList<Link>(links), cleanUp);
						}
					}
				}
			}

			gc.watchLinks(false);

			final JFrame f = (JFrame) componentDialogs.remove(gc.getBeanID());
			if (f != null)
			{
				f.setVisible(false);
			}
		}
		else if (item instanceof GraphEditorLinkGroup)
		{
			/*
			 * GraphEditorLinkGroup linkGroup = (GraphEditorLinkGroup) item; GraphComponent source =
			 * (GraphComponent) linkGroup.getSource(); GraphComponent target = (GraphComponent)
			 * linkGroup.getTarget(); if (source != null) { //source.removeOutputLinkGroup(target);
			 * } if (target != null) { //target.removeInputLinkGroup(source); }
			 */

		}
		else if (item instanceof GraphEditorLink)
		{
			/*
			 * GraphEditorLink link = (GraphEditorLink) item; GraphComponentProperty source =
			 * (GraphComponentProperty) link .getSource(); if (source != null) {
			 * source.removeOutputLink(link); } GraphComponentProperty target =
			 * (GraphComponentProperty) link .getTarget(); if (target != null) {
			 * target.removeInputLink(link); }
			 */
		}
		// now just remove this item
		super.removeItem(item, cleanUp);
	}

	public void setHighlightComponents(final String beanID, final boolean highlight)
	{
		final List<BeanCanvasItem> comps = getBeanInstances(beanID);
		if (comps != null)
		{
			synchronized (comps)
			{
				for(BeanCanvasItem item: comps)
				{
					final GraphComponent gc = (GraphComponent) item;
					gc.setHighlighted(highlight);
					gc.repaint();

				}
			}
		}
	}

	public void showComponentSettings(final GraphComponent gc, final int x, final int y)
	{
		final ComponentAdvert compAdv = gc.getComponentAdvert();
		JFrame ff = (JFrame) componentDialogs.get(compAdv.getID().toString());
		if (ff == null)
		{
			final JFrame f = new JFrame("Component Settings");
			final ComponentSettingsPanel p = new ComponentSettingsPanel(gc);
			f.getContentPane().add(p);
			componentDialogs.put(compAdv.getID().toString(), f);

			f.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(final WindowEvent we)
				{
					DataspaceMonitor.getMonitor().removeComponentPropertyListener(p);
					componentDialogs.remove(compAdv.getID().toString());
					f.setVisible(false);
				}
			});
			ff = f;
		}
		// frame already exists, get the settings panel
		final Component c = ff.getContentPane().getComponent(0);
		if (c instanceof ComponentSettingsPanel)
		{
			DataspaceMonitor.getMonitor().addComponentPropertyListener((ComponentSettingsPanel) c);
		}

		ff.setLocationRelativeTo(this);
		// ff.setResizable(false);
		ff.pack();
		ff.setVisible(true);
	}

	/**
	 * Rules for allowing connections
	 */
	boolean checkIfCanConnect(final GraphComponentProperty source, final GraphComponentProperty target)
	{
		if (source == target)
		{ // no self connect
			return false;
		}

		// check if connect within same component allowed
		if (!allowComponentSelfConnect && source.getParent().getBeanID().equals(target.getParent().getBeanID())) { return false; }

		// check whether target is read-only
		if (target.getComponentProperty().isReadonly()) { return false; }
		return true;
	}

	boolean connect(final GraphComponentProperty source, final GraphComponentProperty target, final Link link)
	{
		return connect(source, target, link, true);
	}

	boolean connect(final GraphComponentProperty source, final GraphComponentProperty target, final Link link,
			final boolean publish)
	{
		synchronized (link)
		{
			if (publish)
			{
				final PropertyLinkRequest plr = DataspaceMonitor.getMonitor()
						.publishLink(source.getComponentProperty(), target.getComponentProperty());
				if (plr != null)
				{
					link.setLinkRequest(plr);
				}
				else
				{
					return false;
				}
			}

			final GraphComponent parentSource = source.getParent();
			final GraphComponent parentTarget = target.getParent();
			parentSource.setAttached(true);
			parentTarget.setAttached(true);

			link.setSource(source);
			link.setTarget(target);

			source.addOutputLink(link);
			target.addInputLink(link);

			parentTarget.addInputLink(link);
			parentSource.addOutputLink(link);

			return true;
		}
	}

	boolean connect(final GraphComponentProperty source, final GraphComponentProperty target,
			final PropertyLinkRequest req)
	{

		final Point start = source.getOutAnchorPoint();
		final Point end = target.getInAnchorPoint();
		final Link link = new GraphEditorLink(this, start, end, source, target);
		link.setLinkRequest(req);
		// addItem(link);
		return connect(source, target, link, false);

	}

	GraphComponent getGraphComponent(final int x, final int y)
	{
		final GraphComponent gc = (GraphComponent) getItem(x, y, GraphComponent.class);
		return gc;
	}

	GraphComponentProperty getGraphComponentProperty(final int x, final int y)
	{
		final GraphComponent gc = getGraphComponent(x, y);
		if (gc != null) { return gc.getGraphComponentProperty(x, y); }
		return null;
	}

	// override to show all links
	@Override
	protected BeanCanvasItem createFromTemplate(final BeanCanvasItem template)
	{
		final BeanCanvasItem newBean = super.createFromTemplate(template);
		if (newBean instanceof GraphComponent)
		{
			((GraphComponent) newBean).watchLinks(true);
		}
		return newBean;
	}

	protected void processActiveNameChange(final RDFStatement rdf)
	{
		final GUID beanGUID = RDFStatement.urlToGUID(rdf.getSubject());
		final List<BeanCanvasItem> comps = getBeanInstances(beanGUID.toString());

		if (comps != null)
		{
			synchronized (comps)
			{
				final DataspaceMonitor monitor = DataspaceMonitor.getMonitor();

				final String currentName = DataspaceUtils.getCurrentName(monitor
						.getComponentAdvert(beanGUID.toString()));

				for(BeanCanvasItem item: comps)
				{
					GraphComponent gc = (GraphComponent)item;
					if (!gc.getName().equals(currentName))
					{
						gc.setName(currentName);
						gc.update();
						gc.repaint();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#capabilityAdded(ect.Capability)
	 */
	/*
	 * public void capabilityAdded(Capability cap) { ComponentAdvert temp = new
	 * ComponentAdvert((GUID) null); temp.setCapabilityID(cap.getID()); try { ComponentAdvert[]
	 * results = temp.copyCollectAsComponentAdvert(DataspaceMonitor.getMonitor() .getDataspace());
	 * if (results != null) { for (int i = 0; i < results.length; i++) { ComponentAdvert ca =
	 * results[i]; String beanID = ca.getComponentID().toString(); Vector gcs =
	 * getBeanInstances(beanID); if (gcs == null) { continue; } String hostName = cap.getHostID();
	 * for (Enumeration e = gcs.elements(); e.hasMoreElements(); ) { GraphComponent gc =
	 * (GraphComponent) e.nextElement(); gc.setHostName(hostName); gc.repaint(); } } } } catch
	 * (DataspaceInactiveException e) { // TODO Auto-generated catch block e.printStackTrace(); } }
	 */

}
