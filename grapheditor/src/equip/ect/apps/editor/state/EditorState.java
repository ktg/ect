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

 */

package equip.ect.apps.editor.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.ComponentListener;
import equip.ect.apps.editor.ComponentPropertyListener;
import equip.ect.apps.editor.DataspaceConfigurationListener;
import equip.ect.apps.editor.DataspaceMonitor;
import equip.ect.apps.editor.Info;

public abstract class EditorState implements ComponentListener, ComponentPropertyListener,
		DataspaceConfigurationListener
{

	public Map<EditorID, EditorItemState> itemStateMaps;

	public Map<EditorID, BeanCanvasItem> activeItemMaps;

	public List<EditorItemState> linkStates;

	protected Map<String, List<Object>> pendingItems;
	protected Map pendingLinks;

	protected BeanGraphPanel canvas;

	public EditorState()
	{
		this((BeanGraphPanel) null);
	}

	public EditorState(final BeanGraphPanel canvas)
	{
		this.canvas = canvas;
		this.activeItemMaps = new HashMap<EditorID, BeanCanvasItem>();
		this.itemStateMaps = new HashMap<EditorID, EditorItemState>();
		this.linkStates = new ArrayList<EditorItemState>();
	}

	public void addPendingItem(final String itemid, final Object context)
	{

		if (pendingItems == null)
		{
			pendingItems = new HashMap<String, List<Object>>();
		}
		List<Object> contexts = pendingItems.get(itemid);
		if (contexts == null)
		{
			contexts = new ArrayList<Object>();
		}

		if (!contexts.contains(context))
		{
			// System.out.println("ADDING PENDING ITEM=" +
			// context.getClass().getName());
			contexts.add(context);
		}
		pendingItems.put(itemid, contexts);
	}

	@Override
	public void capabilityAdded(final Capability cap)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void capabilityDeleted(final Capability cap)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void capabilityUpdated(final Capability cap)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void componentAdvertAdded(final ComponentAdvert compAd)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void componentAdvertDeleted(final ComponentAdvert compAd)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * public Vector getPendingItemContexts(String itemID) { if (pendingItems != null) { return
	 * (Vector) pendingItems.get(itemID); } return null; }
	 */

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
		// TODO Auto-generated method stub

	}

	public void componentPropertyUpdated(final ComponentProperty compProp)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void componentRequestAdded(final ComponentRequest compReq)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void componentRequestDeleted(final ComponentRequest compReq)
	{
		// TODO Auto-generated method stub

	}

	public List<Object> getPendingItemContexts(final String itemid)
	{
		if (pendingItems == null) { return null; }
		final List<Object> contexts = pendingItems.get(itemid);
		return (contexts != null ? new ArrayList<Object>(contexts) : null);
	}

	public boolean loadFromXML(final File file)
	{
		try
		{
			final FileInputStream fis = new FileInputStream(file);
			return loadFromXML(fis);
		}
		catch (final FileNotFoundException fnfe)
		{
			Info.message(this, fnfe.getMessage());

		}
		return false;
	}

	public final boolean loadFromXML(final InputStream inputStream)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			final Document doc = db.parse(new InputSource(inputStream));
			// Document
			return loadFromXML(doc);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public final boolean loadFromXML(final Reader characterStream)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			final Document doc = db.parse(new InputSource(characterStream));
			return loadFromXML(doc);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public final boolean loadFromXML(final String xmlString)
	{
		final StringReader sr = new StringReader(xmlString);
		return loadFromXML(sr);
	}

	public void monitorCanvas(final BeanGraphPanel canvas)
	{
		this.canvas = canvas;
		DataspaceMonitor.getMonitor().addComponentListener(this);
		DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);
		DataspaceMonitor.getMonitor().addComponentPropertyListener(this);
	}

	@Override
	public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void propertyLinkRequestUpdated(final PropertyLinkRequest linkReq)
	{
		// TODO Auto-generated method stub

	}

	public void removePendingItem(final String itemid)
	{
		if (pendingItems != null)
		{
			// System.out.println("REMOVING PENDING ITEM");
			pendingItems.remove(itemid);
			if (pendingItems.size() < 1)
			{
				pendingItems = null;
			}
		}
	}

	public void removePendingItemContext(final String itemid, final Object context)
	{
		final List<Object> contexts = getPendingItemContexts(itemid);
		if (contexts != null)
		{
			contexts.remove(context);
			// System.out.println("REMOVING PENDING ITEM CONTEXT");
			if (contexts.size() < 1)
			{
				removePendingItem(itemid);
			}
		}
	}

	public void saveToFile(final File file)
	{
		try
		{
			final Document doc = toXML();

	        //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            //create string from xml tree
            StreamResult result = new StreamResult(file);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	protected abstract boolean loadFromXML(Document doc);

	protected abstract Document toXML();
}