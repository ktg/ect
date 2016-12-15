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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.Connectable;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.Link;
import equip.ect.apps.editor.LinkGroup;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.interactive.InteractiveCanvasItem;

/**
 * The basic component representation for the GraphEditorCanvas. These contain
 * GraphComponentProperties as subcomponents.
 *
 * @author Jan Humble
 */

public class GraphComponent extends BeanCanvasItem implements Connectable
{
	private class MyLinkWatcher implements DataspaceEventListener
	{
		@Override
		public void dataspaceEvent(final DataspaceEvent de)
		{
			final Runnable task = new Runnable()
			{
				@Override
				public void run()
				{
					// System.err.println("Link event: " + de);
					final equip.data.ItemData newItem = de.getAddItem();
					if (newItem instanceof equip.data.TupleImpl && canvas instanceof GraphEditorCanvas)
					{
						final GraphEditorCanvas gec = (GraphEditorCanvas) canvas;
						final PropertyLinkRequest req = new PropertyLinkRequest((equip.data.TupleImpl) newItem);
						Info.message(this, "Link from " + req.getSourcePropID() + " to " + req.getDestinationPropID());
						final String linkid = req.getID().toString();
						final List<BeanCanvasItem> links = gec.getBeanInstances(linkid);
						if (links != null && links.size() > 0)
						{
							System.err.println("Link " + linkid + " already exists");
							return;
						}

						final String fromid = req.getSourceComponentID().toString();
						//final String toid = req.getDestComponentID().toString();
						GraphComponentProperty fromprop = null, toprop = null;
						if (fromid.equals(beanid))
						{
							fromprop = getGraphComponentProperty(req.getSourcePropID().toString());
						}
						else
						{
							toprop = getGraphComponentProperty(req.getDestinationPropID().toString());
						}

						final ComponentProperty prop = new ComponentProperty(
								(fromid.equals(beanid) ? req.getDestinationPropID() : req.getSourcePropID()));
						final List<GraphComponentProperty> props = gec.getGraphComponentProperties(prop);
						if (props != null && props.size() > 0)
						{
							if (fromid.equals(beanid))
							{
								toprop = props.get(0);
							}
							else
							{
								fromprop = props.get(0);
							}
						}

						if (fromid.equals(beanid))
						{
							if (fromprop == null)
							{
								// probably a dynamic property
								List<PropertyLinkRequest> lr = pendingFromLinks.get(req.getSourcePropID().toString());
								if (lr == null)
								{
									lr = new ArrayList<>();
									pendingFromLinks.put(req.getSourcePropID().toString(), lr);
								}
								lr.add(req);
							}
						}
						else
						{
							if (toprop == null)
							{
								List<PropertyLinkRequest> lr = pendingToLinks.get(req.getDestinationPropID().toString());
								if (lr == null)
								{
									lr = new ArrayList<>();
									pendingToLinks.put(req.getDestinationPropID().toString(), lr);
								}
								lr.add(req);
							}
						}

						if (toprop != null && fromprop != null)
						{
							Info.message(this, "Found both ends of link!");
							gec.connect(fromprop, toprop, req);
							fromprop.getParent().revalidate();
							toprop.getParent().revalidate();
						}
					}
				}
			};
			javax.swing.SwingUtilities.invokeLater(task);
		}
	}
	// links from me - template
	public static boolean showAllLinks = true;
	private final Drawer drawer;
	private transient final Map<String, GraphComponentProperty> graphCompProps = new HashMap<>();
	private transient final List<GraphComponentProperty> renderableProps = new ArrayList<>();
	private final Map<String, List<PropertyLinkRequest>> pendingToLinks = new HashMap<>();
	private final Map<String, List<PropertyLinkRequest>> pendingFromLinks = new HashMap<>();
	private Map<GraphComponent, LinkGroup> outLinkGroups, inLinkGroups;
	private String hostID;
	private equip.data.DataSession linkFromSession;
	private equip.data.DataSession linkToSession;
	private boolean watchingLinks = false;

	GraphComponent(final Component canvas, final String beanid, final String title, final String hostID)
	{
		super(canvas, new GraphComponentView(canvas, title, null), beanid, title);

		System.out.println("new graph component created");

		this.hostID = hostID;
		final Map<String, ComponentProperty> props = DataspaceMonitor.getMonitor().getComponentProperties().get(beanid);
		if (props != null)
		{
			props.values().forEach(this::addGraphComponentProperty);
		}
		((GraphComponentView) view).setGraphComponentProperties(renderableProps);
		this.drawer = new Drawer(this, Drawer.Type.UP, Drawer.State.OPEN);
		((GraphComponentView) view).setDrawer(drawer);
	}

	@Override
	public void cleanUp()
	{
		Info.message("Cleaning up");
		if (graphCompProps != null)
		{
			renderableProps.forEach(GraphComponentProperty::cleanUp);
		}
		watchLinks(false);
		super.cleanUp();
	}

	@Override
	public Object clone(final Component canvas)
	{
		return new GraphComponent(canvas, beanid, name, hostID);
	}

	public final Drawer getDrawer()
	{
		return drawer;
	}

	public final Map<String, GraphComponentProperty> getGraphComponentProperties()
	{
		return graphCompProps;
	}

	@Override
	public Point getInAnchorPoint()
	{
		final int headerHeight = ((GraphComponentView) view).getHeaderHeight();
		return new Point(posX, (int) (posY + 0.5 * headerHeight));
	}

	@Override
	public Point getOutAnchorPoint()
	{
		final int headerHeight = ((GraphComponentView) view).getHeaderHeight();
		return new Point(posX + width, (int) (posY + 0.5 * headerHeight));
	}

	public void removeInputLink(final Link link)
	{
		if (inLinkGroups == null)
		{
			return;
		}

		final GraphComponentProperty source = (GraphComponentProperty) link.getSource();
		if (source != null)
		{
			final LinkGroup linkGroup = getInLinkGroup(source.getParent());
			if (linkGroup == null)
			{
				return;
			}

			linkGroup.removeLink(link);
			if (linkGroup.nrLinks() < 1)
			{
				inLinkGroups.remove(linkGroup.getSource());
			}
			if (inLinkGroups.size() < 1)
			{
				inLinkGroups = null;
			}
		}
	}

	public void removeOutputLink(final Link link)
	{
		synchronized (link)
		{
			if (outLinkGroups == null)
			{
				return;
			}

			final GraphComponentProperty target = (GraphComponentProperty) link.getTarget();
			if (target != null)
			{
				final LinkGroup linkGroup = getOutLinkGroup(target.getParent());
				if (linkGroup == null)
				{
					return;
				}
				//final Link removedLink =
				linkGroup.removeLink(link);

				if (linkGroup.nrLinks() < 1)
				{
					outLinkGroups.remove(linkGroup.getTarget());
				}
				if (outLinkGroups.size() < 1)
				{
					outLinkGroups = null;
				}
			}
		}
	}

	@Override
	public void repaint()
	{
		if (graphCompProps != null)
		{
			for (GraphComponentProperty current : renderableProps)
			{
				List<Link> links = current.getOutputLinks();
				if (links != null)
				{
					links.forEach(InteractiveCanvasItem::repaint);
				}

				links = current.getInputLinks();
				if (links != null)
				{
					links.forEach(InteractiveCanvasItem::repaint);
				}
			}

			if (inLinkGroups != null)
			{
				inLinkGroups.values().forEach(InteractiveCanvasItem::repaint);
			}

			if (outLinkGroups != null)
			{
				outLinkGroups.values().forEach(InteractiveCanvasItem::repaint);
			}
		}
		super.repaint();
	}

	public void revalidate()
	{
		switch (drawer.getDrawerState())
		{
			case OPEN:
				openDrawer();
				break;
			case COMPACT:
				compactDrawer();
				break;
			case CLOSED:
				closeDrawer();
				break;
		}
	}

	public final void setDrawerState(final Drawer.State drawerState)
	{
		drawer.setDrawerState(drawerState);
		switch (drawerState)
		{
			case OPEN:
				openDrawer();
				break;
			case COMPACT:
				compactDrawer();
				break;
			case CLOSED:
				closeDrawer();
				break;
		}
	}

	@Override
	public void setName(final String name)
	{
		System.out.println("Set Name " + name);
		super.setName(name);
		((GraphComponentView) view).setName(name);
	}

	@Override
	public void setSelected(boolean selected)
	{
		if (!selected)
		{
			renderableProps.stream().filter(InteractiveCanvasItem::isSelected).forEach(gcp -> gcp.setSelected(false));
		}
		super.setSelected(selected);
	}

	@Override
	public void setPosition(final int x, final int y)
	{
		super.setPosition(x, y);
		final int headerHeight = ((GraphComponentView) view).getHeaderHeight();
		final int headerWidth = ((GraphComponentView) view).getHeaderWidth();

		int yp = posY + headerHeight - 1;
		if (graphCompProps != null)
		{
			for (GraphComponentProperty gcp : renderableProps)
			{
				if (gcp.isVisible())
				{
					gcp.setPosition(posX, yp);
					yp += gcp.getHeight() - 1;
				}
			}
		}
		final int ddx = (int) (0.5 * (headerWidth - drawer.getWidth()));
		drawer.setPosition(posX + ddx, posY + height - drawer.getHeight());

		if (outLinkGroups != null)
		{
			for (LinkGroup group : outLinkGroups.values())
			{
				group.setStartPoint(getOutAnchorPoint());
			}
		}

		if (inLinkGroups != null)
		{
			for (LinkGroup group : inLinkGroups.values())
			{
				group.setEndPoint(getInAnchorPoint());
			}
		}
	}

	@Override
	public void setTargetCanvas(final Component canvas)
	{
		super.setTargetCanvas(canvas);
		calculateSize();
	}

	public void update()
	{
		calculateSize();
	}

	public void watchLinks(final boolean watch)
	{
		Info.message(this, "Watch links? " + watch);
		if (watch && showAllLinks && !watchingLinks)
		{
			final ComponentAdvert compAdv = DataspaceMonitor.getMonitor().getComponentAdverts().get(this.beanid);
			final PropertyLinkRequest fromRequest = new PropertyLinkRequest((equip.data.GUID) null);
			fromRequest.setSourceComponentID(compAdv.getComponentID());
			equip.data.ItemData linkFromTemplate = fromRequest.tuple;
			final PropertyLinkRequest toRequest = new PropertyLinkRequest((equip.data.GUID) null);
			toRequest.setDestComponentID(compAdv.getComponentID());
			equip.data.ItemData linkToTemplate = toRequest.tuple;
			final DataspaceEventListener linkWatcher = new MyLinkWatcher();
			try
			{
				linkFromSession = DataspaceMonitor.getMonitor().getDataspace()
						.addDataspaceEventListener(linkFromTemplate, /* local */
								true, linkWatcher);
				linkToSession = DataspaceMonitor.getMonitor().getDataspace().addDataspaceEventListener(linkToTemplate, /* local */
						true, linkWatcher);
			}
			catch (final DataspaceInactiveException e)
			{
				// Do nothing?
			}
			watchingLinks = true;
		}
		else if (!watch && watchingLinks)
		{
			try
			{
				if (linkFromSession != null)
				{
					DataspaceMonitor.getMonitor().getDataspace().removeDataspaceEventListener(linkFromSession);
					linkFromSession = null;
				}
				if (linkToSession != null)
				{
					DataspaceMonitor.getMonitor().getDataspace().removeDataspaceEventListener(linkToSession);
					linkToSession = null;
				}
			}
			catch (final DataspaceInactiveException e)
			{
				// Do nothing?
			}
			watchingLinks = false;
		}
	}

	void addGraphComponentProperty(final ComponentProperty compProp)
	{
		final GraphComponentProperty gcp = new GraphComponentProperty(this, compProp);
		graphCompProps.put(compProp.getID().toString(), gcp);
		renderableProps.add(gcp);
		Collections.sort(renderableProps);
		height += gcp.getHeight();

		// check for pending links
		List<PropertyLinkRequest> pendingReqs = pendingFromLinks.get(compProp.getID().toString());
		if (pendingReqs != null)
		{
			for (PropertyLinkRequest req : pendingReqs)
			{
				final List<GraphComponentProperty> props = ((GraphEditorCanvas) canvas).getGraphComponentProperties(req
						.getDestinationPropID().toString());
				if (props != null && props.size() > 0)
				{
					final GraphComponentProperty toProp = props.get(0);
					((GraphEditorCanvas) canvas).connect(gcp, toProp, req);
					gcp.getParent().revalidate();
				}
			}
			if (pendingReqs.size() < 1)
			{
				pendingFromLinks.remove(compProp.getID().toString());
			}
		}

		pendingReqs = pendingToLinks.get(compProp.getID().toString());
		if (pendingReqs != null)
		{
			for (PropertyLinkRequest req : pendingReqs)
			{
				final List<GraphComponentProperty> props = ((GraphEditorCanvas) canvas).getGraphComponentProperties(req.getSourcePropID()
						.toString());
				if (props != null && props.size() > 0)
				{
					final GraphComponentProperty fromProp = props.get(0);
					((GraphEditorCanvas) canvas).connect(fromProp, gcp, req);
				}
			}
			if (pendingReqs.size() < 1)
			{
				pendingToLinks.remove(compProp.getID().toString());
			}
		}

	}

	LinkGroup addInputLink(final Link link)
	{
		if (inLinkGroups == null)
		{
			inLinkGroups = new HashMap<>();
		}

		final GraphComponent source = ((GraphComponentProperty) link.getSource()).getParent();
		final GraphComponent target = ((GraphComponentProperty) link.getTarget()).getParent();

		// get all the links that have the same source
		LinkGroup linkGroup = getInLinkGroup(source);
		if (linkGroup == null)
		{
			linkGroup = source.getOutLinkGroup(this);
		}

		if (linkGroup == null)
		{
			// create new one
			linkGroup = new GraphEditorLinkGroup(canvas, source.getOutAnchorPoint(), target.getInAnchorPoint(), source,
					target);
		}
		linkGroup.addLink(link);
		inLinkGroups.put(source, linkGroup);
		return linkGroup;
	}

	LinkGroup addOutputLink(final Link link)
	{
		if (outLinkGroups == null)
		{
			outLinkGroups = new HashMap<>();
		}

		final GraphComponent source = ((GraphComponentProperty) link.getSource()).getParent();
		final GraphComponent target = ((GraphComponentProperty) link.getTarget()).getParent();

		// get all the links that have the same target
		LinkGroup linkGroup = getOutLinkGroup(target);
		if (linkGroup == null)
		{
			linkGroup = target.getInLinkGroup(this);
		}

		if (linkGroup == null)
		{
			// create new one
			linkGroup = new GraphEditorLinkGroup(canvas, source.getOutAnchorPoint(), target.getInAnchorPoint(), source,
					target, null);
		}
		linkGroup.addLink(link);
		outLinkGroups.put(target, linkGroup);
		return linkGroup;
	}

	ComponentAdvert getComponentAdvert()
	{
		return DataspaceMonitor.getMonitor().getComponentAdverts().get(beanid);
	}

	GraphComponentProperty getGraphComponentProperty(final int x, final int y)
	{
		if (drawer.getDrawerState() != Drawer.State.CLOSED)
		{
			if (graphCompProps != null)
			{
				for (GraphComponentProperty current : graphCompProps.values())
				{
					if (current.isVisible() && current.isInside(x, y))
					{
						return current;
					}
				}
			}
		}
		return null;
	}

	GraphComponentProperty getGraphComponentProperty(final String beanid)
	{
		if (graphCompProps != null)
		{
			return graphCompProps.get(beanid);
		}
		return null;
	}

	Map<GraphComponent, LinkGroup> getInLinkGroups()
	{
		return inLinkGroups;
	}

	Map<GraphComponent, LinkGroup> getOutLinkGroups()
	{
		return outLinkGroups;
	}

	void removeGraphComponentProperty(final ComponentProperty compProp)
	{
		final GraphComponentProperty gcp = graphCompProps.remove(compProp.getID().toString());
		renderableProps.remove(gcp);
		if (gcp != null)
		{
			height -= gcp.getHeight();
		}
		pendingToLinks.remove(compProp.getID().toString());
		pendingFromLinks.remove(compProp.getID().toString());

		if (canvas instanceof GraphEditorCanvas)
		{
			List<Link> links = gcp.getOutputLinks();
			if (links != null)
			{
				((GraphEditorCanvas) canvas).removeItems(new ArrayList<>(links));
			}
			links = gcp.getInputLinks();
			if (links != null)
			{
				((GraphEditorCanvas) canvas).removeItems(new ArrayList<>(links));
			}
		}
	}

	/**
	 * Calculate the size for component, adjusting to font metrics. Different sizes for different
	 * fonts and targets canvases.
	 */
	void calculateSize()
	{
		this.lastWidth = width;
		this.lastHeight = height;
		((GraphComponentView) view).calculateSize();
		this.width = view.getWidth();
		this.height = view.getHeight();
	}

	void handleDrawerAction(final Drawer.Type action)
	{
		switch (action)
		{
			case UP:
				if (drawer.getDrawerState() == Drawer.State.OPEN)
				{
					if (canDrawerCompact())
					{
						setDrawerState(Drawer.State.COMPACT);
					}
					else
					{
						setDrawerState(Drawer.State.CLOSED);
					}
				}
				else
				{ // other option is drawer is compact
					setDrawerState(Drawer.State.CLOSED);
				}
				break;
			case DOWN:
				if (drawer.getDrawerState() == Drawer.State.CLOSED)
				{
					if (canDrawerCompact())
					{
						// System.out.println("drawer can compact!!!");
						setDrawerState(Drawer.State.COMPACT);
					}
					else
					{
						setDrawerState(Drawer.State.OPEN);
					}
				}
				else
				{
					setDrawerState(Drawer.State.OPEN);
				}
				break;
		}

	}

	protected boolean canDrawerCompact()
	{
		int visibleCount = 0;
		for (GraphComponentProperty gcp : graphCompProps.values())
		{
			if (gcp.keepVisible() || gcp.isLinked())
			{
				visibleCount++;
			}
		}

		return !(visibleCount == 0 || visibleCount == graphCompProps.size());
	}

	protected void compactLinkGroups()
	{
		if (inLinkGroups != null)
		{
			for (LinkGroup group : inLinkGroups.values())
			{
				final GraphEditorLinkGroup linkGroup = (GraphEditorLinkGroup) group;
				final Drawer.State sourceDrawerState = ((GraphComponent) linkGroup.getSource()).getDrawer().getDrawerState();
				final Drawer.State targetDrawerState = ((GraphComponent) linkGroup.getTarget()).getDrawer().getDrawerState();
				if (sourceDrawerState == Drawer.State.CLOSED || targetDrawerState == Drawer.State.CLOSED)
				{
					linkGroup.compact();
				}
			}
		}
		if (outLinkGroups != null)
		{
			for (LinkGroup group : outLinkGroups.values())
			{
				final GraphEditorLinkGroup linkGroup = (GraphEditorLinkGroup) group;
				final Drawer.State targetDrawerState = ((GraphComponent) linkGroup.getTarget()).getDrawer().getDrawerState();
				final Drawer.State sourceDrawerState = ((GraphComponent) linkGroup.getSource()).getDrawer().getDrawerState();
				if (targetDrawerState == Drawer.State.CLOSED || sourceDrawerState == Drawer.State.CLOSED)
				{
					linkGroup.compact();
				}
			}
		}
	}

	protected void expandLinkGroups()
	{
		if (inLinkGroups != null)
		{
			for (LinkGroup group : inLinkGroups.values())
			{
				final GraphEditorLinkGroup linkGroup = (GraphEditorLinkGroup) group;
				final Drawer.State drawerState = ((GraphComponent) linkGroup.getSource()).getDrawer().getDrawerState();
				if (drawerState == Drawer.State.OPEN || drawerState == Drawer.State.COMPACT)
				{
					linkGroup.expand();
				}
			}
		}
		if (outLinkGroups != null)
		{
			for (LinkGroup group : outLinkGroups.values())
			{
				final GraphEditorLinkGroup linkGroup = (GraphEditorLinkGroup) group;
				final Drawer.State drawerState = ((GraphComponent) linkGroup.getTarget()).getDrawer().getDrawerState();
				if (drawerState == Drawer.State.OPEN || drawerState == Drawer.State.COMPACT)
				{
					linkGroup.expand();
				}
			}
		}
	}

	private void closeDrawer()
	{
		compactLinkGroups();
		calculateSize();
		repaint();
	}

	private void compactDrawer()
	{
		if (drawer.getPreviousDrawerState() == Drawer.State.CLOSED)
		{
			expandLinkGroups();
		}
		else
		{
			compactLinkGroups();
		}
		calculateSize();
		repaint();
	}

	/**
	 * Returns the LinkGroup where the given component is a source.
	 */
	private LinkGroup getInLinkGroup(final Connectable source)
	{
		if (inLinkGroups != null)
		{
			return inLinkGroups.get(source);
		}
		return null;
	}

	/**
	 * Returns the LinkGroup where the given component is a target.
	 */
	private LinkGroup getOutLinkGroup(final Connectable target)
	{
		if (outLinkGroups != null)
		{
			return outLinkGroups.get(target);
		}
		return null;
	}

	private void openDrawer()
	{
		expandLinkGroups();
		calculateSize();
		repaint();
	}
}
