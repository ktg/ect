/*
 <COPYRIGHT>

 Copyright (c) 2005-2006, University of Nottingham
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
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.apps.editor.grapheditor;

import java.awt.Point;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import equip.data.GUID;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.AppsResources;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.BeanChoicePanel;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.DataspaceMonitor;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.Link;
import equip.ect.apps.editor.state.EditorID;
import equip.ect.apps.editor.state.EditorItemState;
import equip.ect.apps.editor.state.EditorState;
import equip.ect.apps.editor.state.EditorStateManager;

/**
 * @author humble
 * 
 */
public class GraphEditorStateManager extends EditorStateManager
{

	public static GraphEditorState loadState(final File file)
	{
		final GraphEditorState state = new GraphEditorState();
		if (state.loadFromXML(file)) { return state; }
		return null;
	}

	static void loadComponentPropertyStates(final GraphComponent graphComponent, final PropertyItemState[] propStates,
			final EditorState state)
	{
		if (propStates == null) { return; }
		for (final PropertyItemState propState : propStates)
		{
			final GraphComponentProperty gcp = graphComponent.getGraphComponentProperty(propState.dataspaceID);
			if (gcp != null)
			{
				gcp.setKeepVisible(propState.keepVisible);

			}
			else
			{
				state.addPendingItem(propState.dataspaceID, propState);
			}
		}

	}

	private final GraphEditor editor;

	/**
	 * @param graphEditor
	 */
	public GraphEditorStateManager(final GraphEditor graphEditor)
	{
		super();
		editor = graphEditor;
		// TODO Auto-generated constructor stub
	}

	public String getDefaultFileName(final BeanGraphPanel canvas)
	{
		final String canvasName = canvas.getName();
		return canvasName + ".xml";
	}

	@Override
	public void restoreState(final BeanGraphPanel canvas, final EditorState state)
	{
		for (final EditorItemState itemstate : state.itemStateMaps.values())
		{
			final GraphEditorItemState obj = (GraphEditorItemState) itemstate;
			final String id = obj.dataspaceID;
			final BeanCanvasItem itemTemplate = BeanChoicePanel.getTemplates().get(id);
			if (itemTemplate != null)
			{
				final Point pos = obj.position;
				final GraphComponent item = (GraphComponent) canvas.setBeanFromTemplate(itemTemplate, pos.x, pos.y);
				item.setID(obj.editorID);
				state.activeItemMaps.put(obj.editorID, item);
				loadComponentPropertyStates(item, obj.propStates, state);

			}
			else
			{
				state.addPendingItem(id, obj);
			}
		}

		for(EditorItemState itemState: state.linkStates)
		{
			final GraphEditorLinkState linkState = (GraphEditorLinkState)itemState;
			((GraphEditorState) state).restoreLink(canvas, linkState);
		}
		canvas.validateViewportSize();
		canvas.repaint();
		// TODO should not monitor constantly
		// need to fix this
		state.monitorCanvas(canvas);
	}

	/**
	 * mapping is old component/property guid as String -> new component/property GUID as GUID
	 */
	public void restoreStateWithMapping(final BeanGraphPanel canvas, final EditorState state, final Map<String, GUID> mapping)
	{
		final Map<String, String> idmapping = new HashMap<String, String>();
		for(String old: mapping.keySet())
		{
			final GUID now = (GUID) mapping.get(old);
			System.out.println("Restore state with mapping " + old + " -> " + now);
			idmapping.put(old, now.toString());
		}

		GraphEditorState.mapIds(state, idmapping);

		restoreState(canvas, state);
	}

	public void saveState(final BeanGraphPanel canvas)
	{
		final String defaultDirectory = System.getProperty(AppsResources.DEFAULT_DIR_PROPERTY_NAME);

		File file;

		if (defaultDirectory == null)
		{
			file = new File(defaultDirectory, getDefaultFileName(canvas));
		}
		else
		{
			file = new File(".", getDefaultFileName(canvas));
		}

		saveState(canvas, file);
	}

	public void saveState(final BeanGraphPanel canvas, final File stateFile)
	{
		EditorState editorState = (EditorState) editorStates.get(canvas.getName());
		if (editorState == null)
		{
			editorState = new GraphEditorState(canvas);
			editorStates.put(canvas.getName(), editorState);
		}
		Info.message(this, "Saving editor state for canvas '" + canvas.getName() + "' to file: " + stateFile.getName());
		// build the state from the editor
		((GraphEditorState) editorState).buildState(canvas);
		editorState.saveToFile(stateFile);
	}

}

class GraphEditorItemState extends EditorItemState
{

	public static EditorItemState parseXMLElement(final Element element)
	{
		final GraphEditorItemState state = new GraphEditorItemState();
		final String className = element.getAttribute("componentClass");
		try
		{
			state.componentClass = Class.forName(className);
		}
		catch (final ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}
		state.canvasName = element.getAttribute("canvasName");
		state.dataspaceID = element.getAttribute("dataspaceID");
		state.editorID = EditorID.parseID(element.getAttribute("editorID"));
		final int posX = Integer.parseInt(element.getAttribute("posX"));
		final int posY = Integer.parseInt(element.getAttribute("posY"));
		state.position = new Point(posX, posY);
		state.state = Integer.parseInt(element.getAttribute("state"));

		// Parse property states

		final NodeList nl = element.getElementsByTagName("Properties");

		if (nl != null)
		{
			final Element propNode = (Element) nl.item(0);
			printNode(propNode, 0);
			if (propNode != null)
			{
				final int nrProps = Integer.parseInt(propNode.getAttribute("nrProperties"));
				final NodeList propNodes = propNode.getElementsByTagName("ComponentPropertyItem");
				final PropertyItemState[] propStates = new PropertyItemState[nrProps];
				for (int i = 0; i < nrProps; i++)
				{
					// System.out.println("NODE=>" + propNodes.item(i));
					propStates[i] = (PropertyItemState) PropertyItemState.parseXMLElement((Element) propNodes.item(i));
				}
				state.propStates = propStates;
			}
		}
		return state;
	}

	private static void printNode(final Node node, final int level)
	{
		System.out.println("NODE (level=" + level + ") =>" + node.getClass().getName());
		if (node.hasChildNodes())
		{
			final NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++)
			{
				printNode(nl.item(i), level + 1);
			}
		}
	}

	String dataspaceID;

	Point position;

	int state;

	PropertyItemState[] propStates;

	@Override
	public void mapIds(final Map<String, String> mapping)
	{
		// if (mapping.containsKey(editorID))
		// editorID = (String)mapping.get(editorID);
		if (mapping.containsKey(dataspaceID))
		{
			dataspaceID = (String) mapping.get(dataspaceID);
		}
	}

	@Override
	public Element toXMLElement(final Document doc)
	{
		final Element itemElement = doc.createElement("GraphEditorItem");
		itemElement.setAttribute("canvasName", canvasName);
		itemElement.setAttribute("componentClass", componentClass.getName());
		itemElement.setAttribute("dataspaceID", dataspaceID.toString());
		itemElement.setAttribute("editorID", editorID.toString());
		itemElement.setAttribute("posX", Integer.toString(position.x));
		itemElement.setAttribute("posY", Integer.toString(position.y));
		itemElement.setAttribute("state", Integer.toString(state));

		if (propStates != null)
		{
			final Element propNode = doc.createElement("Properties");
			propNode.setAttribute("nrProperties", Integer.toString(propStates.length));
			itemElement.appendChild(propNode);
			for (final PropertyItemState propState : propStates)
			{
				propNode.appendChild(propState.toXMLElement(doc));
			}
		}

		return itemElement;
	}
}

class GraphEditorLinkState extends EditorItemState
{

	public static EditorItemState parseXMLElement(final Element element)
	{
		final GraphEditorLinkState state = new GraphEditorLinkState();
		final String className = element.getAttribute("componentClass");
		try
		{
			state.componentClass = Class.forName(className);
		}
		catch (final ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
		}

		state.canvasName = element.getAttribute("canvasName");
		state.linkID = element.getAttribute("dataspaceID");

		state.sourceID = element.getAttribute("sourceID");
		state.sourceParentID = element.getAttribute("sourceParentID");
		state.sourceEditorID = element.getAttribute("sourceEditorID");

		state.targetID = element.getAttribute("targetID");
		state.targetParentID = element.getAttribute("targetParentID");
		state.targetEditorID = element.getAttribute("targetEditorID");

		return state;
	}

	String linkID;

	// Source info
	String sourceID;

	String sourceParentID;

	String sourceEditorID;

	transient GraphComponentProperty liveSource = null;

	// Target Info
	String targetID;

	String targetParentID;

	String targetEditorID;

	transient GraphComponentProperty liveTarget = null;

	@Override
	public void mapIds(final Map<String, String> mapping)
	{
		if (mapping.containsKey(linkID))
		{
			linkID = mapping.get(linkID);
		}
		if (mapping.containsKey(sourceID))
		{
			sourceID = mapping.get(sourceID);
		}
		// if (mapping.containsKey(sourceEditorID))
		// sourceEditorID = (String)mapping.get(sourceEditorID);
		if (mapping.containsKey(sourceParentID))
		{
			sourceParentID = mapping.get(sourceParentID);
		}
		if (mapping.containsKey(targetID))
		{
			targetID = mapping.get(targetID);
		}
		// if (mapping.containsKey(targetEditorID))
		// targetEditorID = (String)mapping.get(targetEditorID);
		if (mapping.containsKey(targetParentID))
		{
			targetParentID = mapping.get(targetParentID);
		}
	}

	@Override
	public Element toXMLElement(final Document doc)
	{
		final Element itemElement = doc.createElement("GraphEditorLink");
		itemElement.setAttribute("canvasName", canvasName);

		itemElement.setAttribute("componentClass", componentClass.getName());
		itemElement.setAttribute("dataspaceID", linkID);

		itemElement.setAttribute("sourceID", sourceID);
		itemElement.setAttribute("sourceParentID", sourceParentID);
		itemElement.setAttribute("sourceEditorID", sourceEditorID);

		itemElement.setAttribute("targetID", targetID);
		itemElement.setAttribute("targetParentID", targetParentID);
		itemElement.setAttribute("targetEditorID", targetEditorID);
		return itemElement;
	}
}

class GraphEditorState extends EditorState
{

	// map ids
	public static void mapIds(final EditorState editorState, final Map<String, String> mapping)
	{
		// each item
		for(EditorID key: editorState.itemStateMaps.keySet())
		{
			final GraphEditorItemState state = (GraphEditorItemState) editorState.itemStateMaps.get(key);
			state.mapIds(mapping);
		}
		for(EditorItemState state: editorState.linkStates)
		{
			state.mapIds(mapping);
		}
	}

	public GraphEditorState()
	{
		this((BeanGraphPanel) null);
	}

	public GraphEditorState(final BeanGraphPanel canvas)
	{
		super(canvas);
	}

	public void buildState(final BeanGraphPanel canvas)
	{
		synchronized (canvas)
		{
			final Map<GraphComponent, EditorID> itemMapsRev = new HashMap<GraphComponent, EditorID>();
			itemStateMaps = new HashMap<EditorID, EditorItemState>();
			final List<GraphComponent> items = canvas.getItems(GraphComponent.class);
			if (items != null)
			{
				for(GraphComponent item: items)
				{
					final GraphEditorItemState itemState = new GraphEditorItemState();
					itemState.componentClass = item.getClass();
					itemState.editorID = item.getID();
					itemState.itemName = item.getName();
					itemState.dataspaceID = item.getBeanID();
					itemState.position = item.getPosition();
					itemState.state = item.getDrawer().getDrawerState();
					itemStateMaps.put(itemState.editorID, itemState);
					itemMapsRev.put(item, itemState.editorID);

					final Map<String,GraphComponentProperty> graphCompProps = item.getGraphComponentProperties();
					if (graphCompProps != null)
					{
						final PropertyItemState[] propStates = new PropertyItemState[graphCompProps.size()];
						int pi = 0;
						for (final GraphComponentProperty current: graphCompProps.values())
						{
							propStates[pi] = new PropertyItemState();
							propStates[pi].dataspaceID = current.getBeanID();
							propStates[pi].keepVisible = current.keepVisible();
							propStates[pi].parentEditorID = item.getID();
							propStates[pi].itemName = current.getComponentProperty().getPropertyName();
							pi++;
						}
						itemState.propStates = propStates;

					}
				}

				for (GraphComponent gc: items)
				{
					final Map<String, GraphComponentProperty> graphCompProps = gc.getGraphComponentProperties();
					if (graphCompProps == null)
					{
						continue;
					}
					for(GraphComponentProperty current: graphCompProps.values())
					{
						final List<Link> links = current.getOutputLinks();
						if (links != null)
						{
							for (final Link link: links)
							{
								final GraphComponentProperty source = (GraphComponentProperty) link.getSource();
								final GraphComponentProperty target = (GraphComponentProperty) link.getTarget();
								final GraphEditorLinkState linkState = new GraphEditorLinkState();
								linkState.componentClass = link.getClass();
								linkState.linkID = link.getBeanID();
								linkState.sourceID = source.getBeanID();
								linkState.sourceParentID = source.getParent().getBeanID();
								linkState.sourceEditorID = itemMapsRev.get(source.getParent()).toString();
								linkState.targetID = target.getBeanID();
								linkState.targetParentID = target.getParent().getBeanID();
								linkState.targetEditorID = itemMapsRev.get(target.getParent()).toString();
								linkStates.add(linkState);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void componentAdvertAdded(final ComponentAdvert compAdvert)
	{
		final String id = compAdvert.getComponentID().toString();
		final List<Object> contexts = getPendingItemContexts(id);
		if (contexts != null)
		{
			for (Object context: contexts)
			{
				if (context instanceof GraphEditorItemState)
				{
					final GraphEditorItemState itemState = (GraphEditorItemState) context;
					final BeanCanvasItem itemTemplate = BeanChoicePanel.getTemplates().get(itemState.dataspaceID);
					if (itemTemplate != null)
					{
						final Point pos = itemState.position;
						final BeanCanvasItem item = canvas.setBeanFromTemplate(itemTemplate, pos.x, pos.y);
						item.setID(itemState.editorID);
						GraphEditorStateManager.loadComponentPropertyStates((GraphComponent) item,
																			itemState.propStates, this);
						activeItemMaps.put(itemState.editorID, item);
						removePendingItemContext(id, itemState);
						item.repaint();
					}
				}
			}

			for (Object context: contexts)
			{
				if (context instanceof GraphEditorLinkState)
				{
					restoreLink(canvas, (GraphEditorLinkState) context);
				}
			}
		}
	}

	@Override
	public void componentAdvertDeleted(final ComponentAdvert compAdvert)
	{
		removePendingItem(compAdvert.getID().toString());
	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
		final String id = compProp.getID().toString();
		final List<Object> contexts = getPendingItemContexts(id);
		if (contexts != null)
		{
			for (final Object context: contexts)
			{
				if (context instanceof GraphEditorLinkState)
				{
					final GraphEditorLinkState state = (GraphEditorLinkState) context;
					restoreLink(canvas, state);

					// } else if (context instanceof PropertyItemState) {
					// System.out.println("*** PEnding");
					// PropertyItemState propState = (PropertyItemState) context;
					// GraphComponent gc = (GraphComponent)
					// canvas.getBeanInstance(propState.parentEditorID);
					// if (gc != null) {
					// System.out.println("*** But no cigarr :-(");
					// GraphComponentProperty gcp =
					// gc.getGraphComponentProperty(propState.dataspaceID);
					// if (gcp != null) {
					// gcp.setKeepVisible(propState.keepVisible);
					// removePendingItem(id);
					// }
					// }
				}
			}
		}
	}

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
		removePendingItem(compProp.getID().toString());
	}

	@Override
	public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
	{
		final List<Object> contexts = getPendingItemContexts(linkReq.getID().toString());
		if (contexts != null)
		{
			for (final Object context: contexts)
			{
				if (context instanceof GraphEditorLinkState)
				{
					final GraphEditorLinkState linkState = (GraphEditorLinkState) context;
					restoreLink(canvas, linkState);
				}
			}
		}
	}

	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
		removePendingItem(linkReq.getID().toString());
	}

	@Override
	public Document toXML()
	{
		try
		{
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			
			final Document doc = db.newDocument();
			final Element top = doc.createElement("GraphEditorState");
			doc.appendChild(top);
			top.setAttribute("canvasName", canvas.getName());
			Element itemsElement = doc.createElement("GraphEditorComponents");
			top.appendChild(itemsElement);
			if (itemStateMaps != null)
			{
				for(EditorItemState state: itemStateMaps.values())
				{
					itemsElement.appendChild(state.toXMLElement(doc));
				}
			}
	
			itemsElement = doc.createElement("GraphEditorLinks");
			top.appendChild(itemsElement);
			if (linkStates != null)
			{
				for(EditorItemState state: itemStateMaps.values())
				{
					itemsElement.appendChild(state.toXMLElement(doc));
				}
			}
			return doc;
		}
		catch(Exception e)
		{
			return null;
		}		
	}

	boolean restoreLink(final BeanGraphPanel canvas, final GraphEditorLinkState linkState)
	{
		// System.out.println("Restoring LINK");
		GraphComponentProperty source = null, target = null;

		// get the source component
		final GraphComponent sourceComp = (GraphComponent) activeItemMaps.get(new Integer(linkState.sourceEditorID));
		if (sourceComp != null)
		{
			source = sourceComp.getGraphComponentProperty(linkState.sourceID);
			if (source != null)
			{
				linkState.liveSource = source;
			}
			else
			{
				addPendingItem(linkState.sourceID, linkState);
			}
		}
		else
		{
			addPendingItem(linkState.sourceParentID, linkState);
		}

		// get the target component
		final GraphComponent targetComp = (GraphComponent) activeItemMaps.get(new Integer(linkState.targetEditorID));
		if (targetComp != null)
		{
			target = targetComp.getGraphComponentProperty(linkState.targetID);
			if (target != null)
			{
				linkState.liveTarget = target;
			}
			else
			{
				addPendingItem(linkState.targetID, linkState);
			}
		}
		else
		{
			addPendingItem(linkState.targetParentID, linkState);
		}

		// create the link
		if (source != null && target != null)
		{
			final GraphEditorCanvas graphPanel = (GraphEditorCanvas) canvas;
			final Link link = new GraphEditorLink(graphPanel, linkState.liveSource, linkState.liveTarget);
			final PropertyLinkRequest plr = DataspaceMonitor.getMonitor().getPropertyLink(linkState.linkID);
			if (plr != null)
			{
				link.setLinkRequest(plr);
				graphPanel.connect(linkState.liveSource, linkState.liveTarget, link, false);
				graphPanel.addItem(link);
				removePendingItem(linkState.linkID);
				graphPanel.repaint();
				return true;
			}
			else
			{
				addPendingItem(linkState.linkID, linkState);
			}
		}
		else
		{
			addPendingItem(linkState.linkID, linkState);
		}
		return false;
	}

	@Override
	protected boolean loadFromXML(final Document doc)
	{
		final NodeList topNodes = doc.getElementsByTagName("GraphEditorState");
		final Node firstTopNode = topNodes.item(0);
		// EditorState editorState = new GraphEditorState(canvas);
		if (firstTopNode instanceof Element)
		{
			final String canvasName = ((Element) firstTopNode).getAttribute("canvasName");

		}

		final NodeList componentNodes = doc.getElementsByTagName("GraphEditorComponents");
		for (int i = 0; i < componentNodes.getLength(); i++)
		{ // this should
			// only be one,
			// but just in
			// case
			final Node componentNode = componentNodes.item(i);
			final NodeList graphComponents = componentNode.getChildNodes();
			for (int ni = 0; ni < graphComponents.getLength(); ni++)
			{
				final Node node = graphComponents.item(ni);
				if (node instanceof Element)
				{
					final Element graphComponent = (Element) graphComponents.item(ni);
					final GraphEditorItemState state = (GraphEditorItemState) GraphEditorItemState
							.parseXMLElement(graphComponent);
					itemStateMaps.put(state.editorID, state);
				}
			}
		}

		final NodeList linkNodes = doc.getElementsByTagName("GraphEditorLinks");
		for (int i = 0; i < linkNodes.getLength(); i++)
		{ // this should only
			// be one, but just
			// in case
			final Node linkNode = linkNodes.item(i);
			final NodeList linkComponents = linkNode.getChildNodes();
			for (int ni = 0; ni < linkComponents.getLength(); ni++)
			{
				final Node node = linkComponents.item(ni);
				if (node instanceof Element)
				{
					final Element linkComponent = (Element) linkComponents.item(ni);
					final GraphEditorLinkState state = (GraphEditorLinkState) GraphEditorLinkState
							.parseXMLElement(linkComponent);
					linkStates.add(state);
				}
			}
		}
		return true;
	}

	@Override
	public void componentAdvertUpdated(ComponentAdvert compAd)
	{
		// TODO Auto-generated method stub
		
	}

}

class PropertyItemState extends EditorItemState
{

	public static EditorItemState parseXMLElement(final Element element)
	{
		final PropertyItemState state = new PropertyItemState();
		state.dataspaceID = element.getAttribute("dataspaceID");
		state.keepVisible = Boolean.valueOf(element.getAttribute("keepVisible")).booleanValue();
		state.parentEditorID = EditorID.parseID(element.getAttribute("parentEditorID"));
		state.itemName = element.getAttribute("itemName");
		return state;
	}

	String dataspaceID;
	EditorID parentEditorID;

	boolean keepVisible;

	@Override
	public void mapIds(final Map<String, String> mapping)
	{

		if (mapping.containsKey(dataspaceID))
		{
			dataspaceID = (String) mapping.get(dataspaceID);
		}

	}

	@Override
	public Element toXMLElement(final Document doc)
	{
		final Element itemElement = doc.createElement("ComponentPropertyItem");
		itemElement.setAttribute("itemName", itemName);
		itemElement.setAttribute("dataspaceID", dataspaceID.toString());
		itemElement.setAttribute("keepVisible", Boolean.toString(keepVisible));
		itemElement.setAttribute("parentEditorID", parentEditorID.toString());

		return itemElement;
	}
}