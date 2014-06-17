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

 Created by: Shahram Izadi (University of Nottingham)
 Contributors:
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)
 Shahram Izadi (University of Nottingham)

 */
package equip.ect;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import equip.data.GUID;
import equip.data.GUIDImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.util.URLUtils;

public class PersistenceManager implements XMLConstants
{

	class ObjectInputStreamLoader extends ObjectInputStream
	{

		ClassLoader loader = null;

		ObjectInputStreamLoader(final InputStream is, ClassLoader loader) throws IOException
		{
			super(is);
			if (loader == null)
			{
				loader = Thread.currentThread().getContextClassLoader();
			}
			else
			{
				this.loader = loader;
			}
		}

		@Override
		protected Class<?> resolveClass(final ObjectStreamClass osc) throws IOException, ClassNotFoundException
		{
			if (osc != null)
			{
				final String className = osc.getName();
				if (loader != null && className != null)
				{
					try
					{
						final Class<?> cls = loader.loadClass(className);
						if (cls != null) { return cls; }
					}
					catch (final ClassNotFoundException e)
					{
						throw new ClassNotFoundException("urlclassloader could not find " + className);
					}
				}
			}
			return super.resolveClass(osc);
		}
	}

	// currently only one persistence file per host
	public static final File PERSISTENCE_FILE = new File("persist.xml");

	public static final String TEMP_SUFFIX = ".temp";

	public static final String BACKUP_SUFFIX = ".backup";

	protected static final String NULL_VALUE_MONIKER = "[Lnull";

	public static PersistenceManager getPersistenceManager()
	{
		synchronized (instantiateLock)
		{
			if (instance == null)
			{
				instance = new PersistenceManager();
			}
			return instance;
		}
	}

	//public File COMPONENT_PERSISTENCE_DIRECTORY = ContainerManagerHelper.createDirectory("persistence");

	private static PersistenceManager instance = null;

	private static Object instantiateLock = new Object();

	/**
	 * hash map of all known components GUID -> DOM Node
	 */
	//private Map<GUID, Node> allKnownComponents = new HashMap<GUID, Node>();

	// ///////////////////////////
	// persistence
	//Document doc = null;

	private PersistenceManager()
	{
	}

	public void appendComponentProperties(final Document doc, final Element parent, final GUID compId,
			final DataspaceBean dataSpaceBean)
	{
		if (parent == null || doc == null || dataSpaceBean == null) { return; }
		final Element propertiesElement = doc.createElement(COMPONENT_PROPERTIES_TAG);
		parent.appendChild(propertiesElement);
		final CompInfo[] compProps = retrieveComponentProperties(dataSpaceBean, compId);
		if (compProps != null)
		{
			Element propElement = null;
			for (final CompInfo compProp : compProps)
			{
				try
				{
					if (compProp.getType().equals(ComponentProperty.TYPE))
					{
						final ComponentProperty prop = new ComponentProperty((TupleImpl) compProp.tuple);
						propElement = createXMLElement(COMPONENT_PROPERTY_TAG, null, null, null, doc, prop.getID());
						if (propElement != null)
						{
							propElement.setAttribute(PROPERTY_NAME_ATTRIBUTE, prop.getPropertyName());
							try
							{
								String propValue = prop.getPropertyValueAsString();
								if (propValue == null)
								{
									propValue = NULL_VALUE_MONIKER;
								}
								propElement.setAttribute(PROPERTY_VALUE_ATTRIBUTE, propValue);
							}
							catch (final ConnectionPointTypeException e)
							{
								System.err.println("PersistenceManager: Unable to get property value: " + e);
							}
							propElement.setAttribute(PROPERTY_CLASS_ATTRIBUTE, prop.getPropertyClass());
							propertiesElement.appendChild(propElement);
						}
					}
				}
				catch (final Exception e)
				{
					System.out.println("error generating component properties " + "element for xml document " + e);
					e.printStackTrace();
				}
			}
		}
	}

	// ///////////////////////////
	// dataspace copy collect helpers

	public void appendExportedCapabilities(final Document doc, final Element parent, final CompInfo[] caps,
			final Map<GUID, Class<?>> mappings)
	{

		if (mappings == null || caps == null || parent == null || doc == null) { return; }

		Element capElement = null;
		for (final CompInfo cap2 : caps)
		{
			try
			{
				if (cap2.getType().equals(Capability.TYPE))
				{
					final Capability cap = new Capability((TupleImpl) cap2.tuple);
					capElement = createXMLElement(	CAPABILITY_TAG, cap.getCapabilityName(),
													classLoadedFrom(mappings.get(cap.getID())), null, doc, cap.getID());
					if (capElement != null)
					{
						parent.appendChild(capElement);
					}
				}
			}
			catch (final Exception e)
			{
				System.out.println("error generating capability " + "element for xml document " + e);
			}
		}
	}

	public void appendRunningComponents(final Document doc, final Element parent, final CompInfo[] adverts,
			final Map<GUID, Class<?>> capMappings, final Map<GUID, Serializable> compMappings, final DataspaceBean dataSpaceBean,
			final ContainerManager containerManager)
	{

		if (capMappings == null || compMappings == null || adverts == null || parent == null || doc == null
				|| dataSpaceBean == null) { return; }

		Element componentElement = null;
		Element requestElement = null;
		Object obj = null;
		File compPersistFile = null;
		final Map<GUID,Node> allKnownComponents = new HashMap<GUID, Node>();
		for (final CompInfo advert2 : adverts)
		{
			try
			{
				if (advert2.getType().equals(ComponentAdvert.TYPE))
				{
					final ComponentAdvert advert = new ComponentAdvert((TupleImpl) advert2.tuple);
					obj = compMappings.get(advert.getComponentRequestID());
					if (obj != null && obj instanceof Persistable)
					{
						try
						{
							compPersistFile = ((Persistable) obj).persist(containerManager);
							containerManager.updateStartupDataPersistFile(	advert.getComponentRequestID(),
																			compPersistFile);

						}
						catch (final IOException e)
						{
							System.out.println("error occured while persisting " + "component state for :"
									+ advert.getComponentName());
						}
					}
					componentElement = createXMLElement(COMPONENT_TAG, advert.getComponentName(),
														classLoadedFrom(capMappings.get(advert.getCapabilityID())),
														compPersistFile, doc, advert.getComponentID());
					if (componentElement != null)
					{
						// parent.appendChild(componentElement);
						requestElement = createXMLElement(	COMPONENT_REQUEST_TAG, null, null, null, doc,
															advert.getComponentRequestID());
						if (requestElement != null)
						{
							componentElement.appendChild(requestElement);
						}
						appendComponentProperties(doc, componentElement, advert.getComponentID(), dataSpaceBean);
						// update all known components
						//allKnownComponents.remove(advert.getComponentID());
						allKnownComponents.put(advert.getComponentID(), componentElement);
					}
				}
			}
			catch (final Exception e)
			{
				System.out.println("error generating component " + "element for xml document " + e);
			}
		}
		// write all known components
		for(Node el: allKnownComponents.values())
		{
			parent.appendChild(el);
		}
	}

	// retrieve the url (of jar or dir) where class is loaded from
	public URL classLoadedFrom(final Object obj)
	{
		URL url = null;
		if (obj != null)
		{
			try
			{
				if (obj instanceof Class)
				{
					url = ((Class<?>) obj).getProtectionDomain().getCodeSource().getLocation();
				}
				else
				{
					url = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
				}
			}
			catch (final Exception e)
			{
				System.out.println("error retrieving url " + "where class loaded from");
			}
		}
		return url;
	}

	public CompInfo[] copyCollect(final DataspaceBean dataSpaceBean, final CompInfo template)
	{

		if (template != null && dataSpaceBean != null)
		{
			try
			{
				return template.copyCollect(dataSpaceBean);
			}
			catch (final DataspaceInactiveException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public Element createGUIDElement(final Document doc, final GUID guid)
	{
		Element element = null;
		if (doc != null && guid != null && !guid.isNull())
		{

			element = doc.createElement(GUID_TAG);
			element.setAttribute(HOST_ID_ATTRIBUTE, "" + guid.host_id);
			element.setAttribute(ITEM_ID_ATTRIBUTE, "" + guid.item_id);
			element.setAttribute(PROC_ID_ATTRIBUTE, "" + guid.proc_id);
			element.setAttribute(TIME_S_ATTRIBUTE, "" + guid.time_s);
		}
		return element;
	}

	public Element createXMLElement(final String tag, final String name, final URL url, final File compPersistFile,
			final Document doc, final GUID guid)
	{
		Element element = null;
		if (tag != null && doc != null)
		{
			final Element guidElement = createGUIDElement(doc, guid);
			if (guidElement != null)
			{
				element = doc.createElement(tag);
				if (name != null)
				{
					element.setAttribute(NAME_ATTRIBUTE, name);
				}
				element.appendChild(guidElement);
				if (url != null)
				{
					element.setAttribute(URL_ATTRIBUTE, URLUtils.encode(url.toExternalForm()));
				}
				if (compPersistFile != null)
				{
					try
					{
						element.setAttribute(PERSIST_FILE_ATTRIBUTE, compPersistFile.getCanonicalPath());
					}
					catch (final java.io.IOException e)
					{
						System.err.println("ERROR getting canonical path for persistence file: " + compPersistFile);
						element.setAttribute(PERSIST_FILE_ATTRIBUTE, compPersistFile.getAbsolutePath());
					}
				}
			}
		}
		return element;
	}

	// xml helpers
	public String extractAttribute(final Node node, final String attribute)
	{
		if (node != null)
		{
			final NamedNodeMap attrs = node.getAttributes();
			final Attr attr = (Attr) attrs.getNamedItem(attribute);
			if (attr != null) { return attr.getValue(); }
		}
		return null;
	}

	public Node extractNode(final Node parent, final String element)
	{
		if (parent != null) { return extractNode(parent.getChildNodes(), element); }
		return null;
	}

	public Node extractNode(final NodeList children, final String element)
	{
		if (children != null)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				final Node childNode = children.item(i);
				if (childNode != null && childNode.getNodeName().equals(element)) { return childNode; }
			}
		}
		return null;
	}

	public File getValidPersistFile(final File file)
	{
		if (file != null)
		{
			if (isValidPersistFile(file)) { return file; }
			// try and recover backup instead
			final File backup = new File(file.getAbsolutePath() + BACKUP_SUFFIX);
			if (isValidPersistFile(backup)) { return backup; }
			// finally try the temp file
			final File temp = new File(file.getAbsolutePath() + TEMP_SUFFIX);
			if (isValidPersistFile(temp)) { return temp; }
		}
		return null;
	}

	public boolean isValidPersistFile(final File file)
	{
		try
		{
			final InputStream fin = new BufferedInputStream(new FileInputStream(file));
			try
			{
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				// factory.setValidating(true);
				// factory.setNamespaceAware(true);
				final DocumentBuilder builder = factory.newDocumentBuilder();
				builder.parse(fin);
				fin.close();
				return true;
			}
			catch (final Exception e)
			{
				e.printStackTrace();
				fin.close();
				return false;
			}
		}
		catch (final Exception e)
		{
			System.out.println(e.getMessage());
			return false;
		}
	}

	// ///////////////////////////
	// recovery

	public Object loadObject(final File file, final ClassLoader loader) throws IOException, ClassNotFoundException
	{
		if (file != null)
		{
			final FileInputStream fis = new FileInputStream(file);
			final ObjectInputStreamLoader ois = new ObjectInputStreamLoader(fis, loader);
			final Object obj = ois.readObject();
			ois.close();
			fis.close();
			return obj;
		}
		return null;
	}

	public ComponentStartupData parseComponent(final Node compNode)
	{

		if (compNode == null || !compNode.getNodeName().equals(COMPONENT_TAG)) { return null; }
		final GUID compId = parseGUID(compNode);
		if (compId == null) { return null; }
		final String name = extractAttribute(compNode, NAME_ATTRIBUTE);
		final String url = extractAttribute(compNode, URL_ATTRIBUTE);
		final String persistFile = extractAttribute(compNode, PERSIST_FILE_ATTRIBUTE);
		final Node compReqNode = extractNode(compNode, COMPONENT_REQUEST_TAG);
		GUID compReqId = null;
		if (compReqNode != null)
		{
			compReqId = parseGUID(compReqNode);
		}
		final Node propsNode = extractNode(compNode, COMPONENT_PROPERTIES_TAG);
		final ComponentProperty[] props = parseComponentProperties(propsNode, compId);
		return new ComponentStartupData(name, url, compId, compReqId, props, persistFile);
	}

	public ComponentProperty[] parseComponentProperties(final Node node, final GUID componentId)
	{
		if (node != null)
		{
			final NodeList children = node.getChildNodes();
			if (children != null)
			{
				ComponentProperty property = null;
				final List<ComponentProperty> properties = new ArrayList<ComponentProperty>();
				for (int i = 0; i < children.getLength(); i++)
				{
					final Node childNode = children.item(i);
					property = parseComponentProperty(childNode, componentId);
					if (property != null)
					{
						properties.add(property);
					}
				}
				return properties.toArray(new ComponentProperty[0]);
			}
		}
		return null;
	}

	// guid parsing

	// todo - currently does not parse value of component property
	public ComponentProperty parseComponentProperty(final Node node, final GUID componentId)
	{
		if (node != null && node.getNodeName().equals(COMPONENT_PROPERTY_TAG))
		{
			final GUID propGUID = parseGUID(node);
			if (propGUID != null)
			{
				final ComponentProperty compProp = new ComponentProperty(propGUID);
				compProp.setPropertyName(extractAttribute(node, PROPERTY_NAME_ATTRIBUTE));
				compProp.setPropertyClass(extractAttribute(node, PROPERTY_CLASS_ATTRIBUTE));
				// value as string only?!
				try
				{

					final String propAttValue = extractAttribute(node, PROPERTY_VALUE_ATTRIBUTE);
					if (propAttValue.equals(NULL_VALUE_MONIKER))
					{
						compProp.setPropertyValue(null);
					}
					else
					{
						compProp.setPropertyValue(propAttValue);
					}
				}
				catch (final Exception e)
				{
					// ignore
				}
				return compProp;
			}
		}
		return null;
	}

	public GUID parseGUID(final Node node)
	{
		if (node != null) { return parseGUID(node.getChildNodes()); }
		return null;
	}

	public GUID parseGUID(final NodeList nodes)
	{
		final Node node = extractNode(nodes, GUID_TAG);
		if (node != null)
		{
			final NamedNodeMap attrs = node.getAttributes();
			final Attr hostIdAttr = (Attr) attrs.getNamedItem(HOST_ID_ATTRIBUTE);
			final Attr itemIdAttr = (Attr) attrs.getNamedItem(ITEM_ID_ATTRIBUTE);
			final Attr procIdAttr = (Attr) attrs.getNamedItem(PROC_ID_ATTRIBUTE);
			final Attr timeSAttr = (Attr) attrs.getNamedItem(TIME_S_ATTRIBUTE);

			try
			{
				final GUID guid = new GUIDImpl();
				guid.host_id = Integer.parseInt(hostIdAttr.getValue());
				guid.item_id = Integer.parseInt(itemIdAttr.getValue());
				guid.proc_id = Integer.parseInt(procIdAttr.getValue());
				guid.time_s = Integer.parseInt(timeSAttr.getValue());
				return guid;
			}
			catch (final Exception e)
			{
				System.out.println("Error parsing GUID from XML file: " + e);
			}
		}
		return null;
	}

	public Map<GUID, ComponentStartupData> parseRunningComponents(final Node node)
	{

		final Map<GUID, ComponentStartupData> map = new HashMap<GUID, ComponentStartupData>();
		final NodeList children = node.getChildNodes();
		if (children != null)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				final Node childNode = children.item(i);
				final ComponentStartupData data = parseComponent(childNode);
				if (data != null && data.getRequestGUID() != null)
				{
					map.put(data.getRequestGUID(), data);
				}
				if (data != null && data.getComponentGUID() != null)
				{
					// fudge for sub-components (and others) - put it in under
					// its own GUID (aswell)
					map.put(data.getComponentGUID(), data);
				}
				if (data != null && data.getComponentGUID() != null)
				{
					//allKnownComponents.put(data.getComponentGUID(), childNode);
				}
			}
		}
		return map;
	}

	public synchronized void persistContainer(final DataspaceBean dataSpaceBean,
			final ContainerManager containerManager, final File persistFile) throws IOException
	{
		try
		{

		if (containerManager == null || persistFile == null || containerManager.id == null) { return; }

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.setNamespaceAware(true);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		
		// persist info regarding container
		final Element containerElement = createXMLElement(CONTAINER_TAG, null, null, null, doc, containerManager.id);
		if (containerElement == null) { return; }
		doc.appendChild(containerElement);

		// persist info regarding exported capabilities
		final CompInfo[] caps = retrieveExportedCapabilities(dataSpaceBean, containerManager.id);
		final Element exportedElements = doc.createElement(EXPORTED_CAPABILITIES_TAG);
		containerElement.appendChild(exportedElements);

		appendExportedCapabilities(doc, exportedElements, caps, containerManager.capabilityClasses);

		// persist info regarding running components
		final CompInfo[] adverts = retrieveRunningComponents(dataSpaceBean, containerManager.id);
		final Element runningElements = doc.createElement(RUNNING_COMPONENTS_TAG);
		containerElement.appendChild(runningElements);

		appendRunningComponents(doc, runningElements, adverts, containerManager.capabilityClasses,
								containerManager.comlauncher.launchedComponents, dataSpaceBean, containerManager);


			System.out.println("Saving Persist Doc");
			// Use a Transformer for output
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			final DOMSource source = new DOMSource(doc);
			final OutputStream fout = new BufferedOutputStream(new FileOutputStream(persistFile));
			final StreamResult result = new StreamResult(fout);
			transformer.transform(source, result);
			fout.close();
			System.out.println("Saved");
		}
		catch (final Exception e)
		{
			throw new IOException("Error Saving Persist File", e);
		}
	}

	public void persistObject(final File file, final Object object) throws IOException
	{
		if (object != null && file != null)
		{
			final FileOutputStream fos = new FileOutputStream(file);
			final ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.flush();
			oos.close();
			fos.close();
		}
	}

	// recover all container state based upon specified xml file
	public synchronized ContainerManager recoverContainer(final ContainerManagerHelper managerHelper,
			final File persistFile, final String hostName, final String dir) throws IOException, SAXException,
			javax.xml.parsers.ParserConfigurationException
	{

		if (persistFile == null) { return null; }
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(true);
		// factory.setNamespaceAware(true);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(persistFile);
		final Node node = extractNode(doc.getChildNodes(), CONTAINER_TAG);
		final GUID containerGUID = parseGUID(node);
		// first parse capabilities and export
		final Map<GUID, Class<?>> exportedCaps = recoverExportedCapabilities(	extractNode(node, EXPORTED_CAPABILITIES_TAG),
																	managerHelper, hostName, containerGUID);
		// now parse info regarding any running components
		final Map<GUID, ComponentStartupData> parsedIDs = parseRunningComponents(extractNode(node, RUNNING_COMPONENTS_TAG));
		return new ContainerManager(managerHelper.getDataSpaceBean(), dir, hostName, containerGUID, exportedCaps,
				parsedIDs);
	}

	public Map<GUID, Class<?>> recoverExportedCapabilities(final Node node, final ContainerManagerHelper helper,
			final String hostname, final GUID containerGUID)
	{

		if (helper == null || node == null) { return null; }

		final CapabilityExporter exporter = new CapabilityExporter();
		final Map<GUID, Class<?>> map = new HashMap<GUID, Class<?>>();
		final NodeList children = node.getChildNodes();
		if (children != null)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				final Node childNode = children.item(i);
				if (childNode != null && childNode.getNodeName().equals(CAPABILITY_TAG))
				{
					final GUID capGUID = parseGUID(childNode);
					final String url = extractAttribute(childNode, URL_ATTRIBUTE);
					final String name = extractAttribute(childNode, NAME_ATTRIBUTE);
					try
					{
						final BeanJarContent beanContent = new BeanJarContent();
						// workaround for windows file: paths starting //server/... which are illegal with
						// new File since authority is non-null.
						final List<Class<?>> classes = helper.loadFromJarFile(url.startsWith("file:") ? new File(URLUtils
																			.decode(url.substring(5))) : new File(
																			new URI(url)), beanContent);
						Introspector.setBeanInfoSearchPath(new String[]{"."});
						Class<?> cls = null;
						for(Class<?> clss: classes)
						{
							// get name the same way exporter does
							try
							{
								final BeanInfo beanInfo = Introspector.getBeanInfo(clss, clss.getSuperclass());
								final BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
								if (beanDescriptor.getName().equals(name))
								{
									cls = clss;
								}
							}
							catch (final Exception e)
							{
								System.err.println("ERROR checking bean class name (" + clss + "): " + e);
								e.printStackTrace();
							}
						}
						if (cls != null)
						{
							final Capability cap = exporter.exportCapability(	cls, capGUID, containerGUID, hostname,
																				helper.getDataSpaceBean(), beanContent
																						.getBeanContent(cls.getName()));
							if (cap != null)
							{
								map.put(cap.getID(), cls);
							}
						}
					}
					catch (final Exception e)
					{
						e.printStackTrace();
						System.out.println("error exporting capability " + e);
					}
				}
			}
		}
		return map;
	}

	public CompInfo[] retrieveComponentProperties(final DataspaceBean dataSpaceBean, final GUID componentId)
	{

		if (componentId != null && !componentId.isNull())
		{

			final ComponentProperty template = new ComponentProperty((GUID) null);
			template.setComponentID(componentId);
			return copyCollect(dataSpaceBean, template);
		}
		return null;
	}

	public CompInfo[] retrieveExportedCapabilities(final DataspaceBean dataSpaceBean, final GUID containerId)
	{

		if (containerId != null && !containerId.isNull())
		{

			final Capability template = new Capability((GUID) null);
			template.setContainerID(containerId);

			return copyCollect(dataSpaceBean, template);
		}
		return null;
	}

	public CompInfo[] retrieveRunningComponents(final DataspaceBean dataSpaceBean, final GUID containerId)
	{

		if (containerId != null && !containerId.isNull())
		{

			final ComponentAdvert template = new ComponentAdvert((GUID) null);
			template.setContainerID(containerId);

			return copyCollect(dataSpaceBean, template);
		}
		return null;
	}

	public void startPersistence(final File persistFile, final DataspaceBean dataSpaceBean,
			final ContainerManager containerManager, final int frequency)
	{
		while (true)
		{
			try
			{

				final File persistTemp = new File(persistFile.getCanonicalPath() + TEMP_SUFFIX);
				if (persistTemp.exists())
				{
					if (!persistTemp.delete())
					{
						System.err.println("ERROR: unable to delete old temp persist file " + persistTemp);
						Thread.sleep(100);
						continue;
					}
				}
				// do persistence
				persistContainer(dataSpaceBean, containerManager, persistTemp);

				if (!isValidPersistFile(persistTemp))
				{
					System.err.println("ERROR: generating persist file");
					continue;
				}
				if (persistFile.exists())
				{
					final File backup = new File(persistFile.getCanonicalPath() + BACKUP_SUFFIX);
					if (backup.exists())
					{
						if (!backup.delete())
						{
							System.err.println("WARNING: unable to delete old backup persist file " + backup);
						}
					}
					if (!persistFile.renameTo(backup))
					{
						System.err.println("WARNING: unable to backup persist file " + persistFile + " to " + backup);
					}
				}
				if (!persistTemp.renameTo(new File(persistFile.getName())))
				{
					System.err
							.println("ERROR: unable to rename new persist file " + persistTemp + " to " + persistFile);
					System.err.println("- temp file " + (persistTemp.exists() ? "exists" : "doesn't exist") + " and "
							+ (persistTemp.canRead() ? "is readable" : "is not readable") + "; persist file "
							+ (persistFile.exists() ? "exists" : "does not exist"));
					Thread.sleep(100);
					continue;
				}
				Thread.sleep(frequency);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR persisting container: " + e);
				e.printStackTrace();
			}
		}
	}
}
