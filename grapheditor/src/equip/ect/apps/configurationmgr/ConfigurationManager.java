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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.apps.configurationmgr;

import equip.data.*;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.ect.*;
import equip.ect.apps.AppsResources;
import equip.ect.apps.editor.dataspace.DataspaceUtils;
import equip.ect.util.DirectoryEventListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration manager tool for ECT that allows persistent dataspace state to be saved and
 * restored, allowing complete arrangements to be saved and recovered. For now, it watches a single
 * directory, each configuration being an EQUIP binary serialisation of the persistent contents of
 * the dataspace, i.e. ComponentRequests and PropertyLinkRequests. Some code theft from ExportedGUI.
 * Todo:
 * <ul>
 * <li>do not set readonly properties (nb change to mapper to mark them)
 * </ul>
 */
public class ConfigurationManager extends JPanel implements DirectoryEventListener, DataspaceEventListener,
		ListSelectionListener
{
	private static final String ATcapabilityref = "capabilityref";
	private static final String ELproperties = "properties";
	private static final String ELproperty = "property";
	private static final String ELcomponentrequests = "componentrequests";
	private static final String ELcomponentrequest = "componentrequest";
	private static final String ELclass = "class";
	private static final String ELlinks = "links";
	private static final String ELlink = "link";
	private static final String ATsrcpropref = "srcpropref";
	private static final String ATsrccompref = "srccompref";
	private static final String ATsrcpropname = "srcpropname";
	private static final String ATdstpropref = "dstpropref";
	private static final String ATdstcompref = "dstcompref";
	private static final String ATdstpropname = "dstpropname";
	private static final String ELvalue = "value";
	private static final String ATisnull = "isnull";
	private static final String FILE_SUFFIX = ".ect";
	private static final String ELcomponents = "components";
	private static final String ELcomponent = "component";
	private static final String ELcapabilities = "capabilities";
	private static final String ELcapability = "capability";
	private static final String ELname = "name";
	private static final String ATid = "id";
	private final DefaultListModel<File> listModel = new DefaultListModel<>();
	/**
	 * dataspace bean
	 */
	public DataspaceBean dataspace;
	private boolean createCancelled;
	private JFrame restoreFrame;
	private JLabel restoreStatus;
	private JProgressBar restoreProgress;
	private JTextArea restoreLogText;
	private JButton restoreDoneButton;
	/**
	 * all request/rdf templates
	 */
	private CompInfo templates[];

	/**
	 * cons for internal use by GraphEditor etc
	 */
	public ConfigurationManager(final DataspaceBean dataspace)
	{
		this.dataspace = dataspace;

		ComponentRequest compReqTemplate = new ComponentRequest((GUID) null);
		PropertyLinkRequest linkReqTemplate = new PropertyLinkRequest((GUID) null);
		RDFStatement rdfTemplate = new RDFStatement((GUID) null);
		templates = new CompInfo[]{compReqTemplate, linkReqTemplate, rdfTemplate};
	}


	private static void invokeAndWait(final Runnable r) throws InterruptedException,
			java.lang.reflect.InvocationTargetException
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		}
		else
		{
			SwingUtilities.invokeAndWait(r);
		}
	}

	public File chooseOpenFile(final Component parent)
	{
		final String defaultDirectory = System.getProperty(AppsResources.DEFAULT_DIR_PROPERTY_NAME);
		JFileChooser chooser;
		if (defaultDirectory == null)
		{
			chooser = new JFileChooser(".");
		}
		else
		{
			chooser = new JFileChooser(defaultDirectory);
		}

		final ExtensionFileFilter filter = new ExtensionFileFilter("ect", "ECT configuration files");
		chooser.setFileFilter(filter);
		final int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
			return chooser.getSelectedFile();
		}
		return null;
	}

	public File chooseSaveFile(final Component parent)
	{
		final String defaultDirectory = System.getProperty(AppsResources.DEFAULT_DIR_PROPERTY_NAME);
		JFileChooser chooser;
		if (defaultDirectory == null)
		{
			chooser = new JFileChooser(".");
		}
		else
		{
			chooser = new JFileChooser(defaultDirectory);
		}

		final ExtensionFileFilter filter = new ExtensionFileFilter("ect", "ECT configuration files");
		chooser.setFileFilter(filter);
		final int returnVal = chooser.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File selected = chooser.getSelectedFile();
			if (!selected.getName().endsWith(".ect"))
			{
				selected = new File(selected.getParent(), selected.getName() + ".ect");
			}
			System.out.println("You chose to save this file: " + selected.getName());
			return selected;
		}
		return null;
	}

	/**
	 * clear configuration ie delete all persistent PropertyLinkRequests and ComponentRequests
	 */
	public boolean clearConfiguration()
	{
		try
		{
			for (final CompInfo template : templates)
			{
				final ItemData[] cret = dataspace.copyCollect(template.tuple);
				System.out.println("Delete " + cret.length + " " + template.getType());
				for (final ItemData element : cret)
				{
					// only persistent?!
					dataspace.delete(element.id);
				}
			}
			return true;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR clear current configiration: " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * notify of event (cf DataCallbackPost)
	 */
	@Override
	public void dataspaceEvent(final DataspaceEvent event)
	{
		// no op - just to force replication
	}

	/**
	 * DirectoryEventListener implementation
	 */
	@Override
	public void fileAdd(final File file)
	{
		// TODO
	}

	/**
	 * DirectoryEventListener implementation
	 */
	@Override
	public void fileDeleted(final File file)
	{
		SwingUtilities.invokeLater(() -> listModel.removeElement(file));
	}

	/**
	 * DirectoryEventListener implementation
	 */
	@Override
	public void fileModified(final File file)
	{
		// TODO
	}

	/**
	 * DirectoryEventListener implementation
	 */
	@Override
	public void filesAdded(final List<File> files)
	{
		for (final File file : files)
		{
			if (!file.getName().endsWith(FILE_SUFFIX))
			{
				System.out.println("Ignore file " + file);
				return;
			}
			SwingUtilities.invokeLater(() ->
			{
				synchronized (listModel)
				{
					// in lexical order
					for (int i = 0; i < listModel.getSize(); i++)
					{
						final File f = listModel.getElementAt(i);
						if (f.getName().compareTo(file.getName()) > 0)
						{
							listModel.insertElementAt(file, i);
							return;
						}
					}
					listModel.addElement(file);
				}
			});
		}
	}

	/**
	 * load an XML configuration - swing thread
	 */
	public boolean restoreConfiguration(final File f)
	{
		new Thread(() -> doRestoreConfiguration(f)).start();
		return true;
	}


	/**
	 * list selection listener
	 */
	@Override
	public void valueChanged(final ListSelectionEvent e)
	{
	}

	private void createRestoreGui()
	{
		// load frame - should be preserved between calls
		restoreFrame = new JFrame("Restore configuration");
		restoreFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		restoreFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				System.out.println("Cancel restore?");
				// frame.setVisible(false);
				// ignore?!
			}
		});
		restoreFrame.getContentPane().setLayout(new BorderLayout());
		// restoreFrame.getContentPane().add(restoreStatus, BorderLayout.SOUTH);

		final JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(500, 600));
		restoreStatus = new JLabel();
		p.add(restoreStatus);
		// SpringLayout pl = new SpringLayout();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		p.add(new JLabel("This action progess"));
		restoreProgress = new JProgressBar(0, 1000);
		p.add(restoreProgress);

		restoreLogText = new JTextArea();
		restoreLogText.setEditable(false);
		p.add(new JLabel("Log output"));
		final JScrollPane rsp = new JScrollPane(restoreLogText);
		p.add(rsp);
		restoreDoneButton = new JButton(new AbstractAction("Done")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Done");
				restoreFrame.setVisible(false);
			}
		});
		p.add(restoreDoneButton);
		restoreDoneButton.setEnabled(false);

		// wrap it up
		restoreFrame.getContentPane().add(p, BorderLayout.CENTER);

		restoreFrame.pack();
		// frame.setSize(500,600);
	}

	/**
	 * load an XML configuration - NOT swing thread
	 */
	private boolean doRestoreConfiguration(final File f)
	{
		try
		{
			invokeAndWait(() ->
			{
				if (restoreFrame == null)
				{
					createRestoreGui();
				}
				restoreDoneButton.setEnabled(false);
				restoreProgress.setValue(0);
				restoreFrame.setVisible(true);
				restoreLogText.setText("Recreating configuration from file " + f + "\n");
				restoreStatus.setText("Loading document...");
			});

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// factory.setValidating(true);
			// factory.setNamespaceAware(true);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(f);
			System.out.println("Read document OK");
			final Element root = document.getDocumentElement();

			restoreSetStatus("Getting top-level elements...");

			Element capabilities = getNamedChildElement(root, ELcapabilities);

			final Map<String, GUID> requestComponents = new HashMap<>();
			final Map<GUID, GUID> idMapping = new HashMap<>();

			for (final Element capability : getNamedChildElements(capabilities, ELcapability))
			{
				final GUID oldID = DataspaceUtils.stringToGUID(capability.getAttribute(ATid));
				ItemData item = dataspace.getItem(oldID);
				if (item == null)
				{
					final String className = getElementText(getNamedChildElement(capability, ELclass));
					System.out.println("Capability not recognised, searching for class " + className);
					final String name = getElementText(getNamedChildElement(capability, ELname));

					final Capability template = new Capability((GUID) null);
					template.setCapablityClass(className);

					final ItemData[] items = dataspace.copyCollect(template.tuple);
					if (items == null || items.length == 0)
					{
						restoreLog("WARNING: capability " + name + " (" + oldID + ") cannot be found\n");
						continue;
					}
					else
					{
						System.out.println("Capabilities found " + items.length + ": " + items[0].id + " = " + oldID);
						item = items[0];
					}
				}

				idMapping.put(oldID, item.id);
			}

			restoreLog("\n--- Recreating Components ---\n\n");

			// recreate component requests
			final Element requestRoot = getNamedChildElement(root, ELcomponentrequests);
			for (final Element componentRequest : getNamedChildElements(requestRoot, ELcomponentrequest))
			{
				DataSession session = null;
				try
				{
					final GUID requestid = DataspaceUtils.stringToGUID(componentRequest.getAttribute(ATid));

					if (dataspace.getItem(requestid) != null)
					{
						restoreLog("WARNING: cannot restore request " + requestid + ": already present\n");
						continue;
					}

					System.out.println("Looking for " + DataspaceUtils.stringToGUID(componentRequest.getAttribute(ATcapabilityref)));
					final GUID capid = idMapping.get(DataspaceUtils.stringToGUID(componentRequest.getAttribute(ATcapabilityref)));
					final ItemData cap = dataspace.getItem(capid);
					if (cap == null)
					{
						restoreLog("WARNING: cannot restore request " + requestid + ": capability " + capid
								+ " not found\n");
						continue;
					}

					final Capability capability = new Capability((TupleImpl) cap);

					restoreSetStatus("Recreate request " + requestid);

					final ComponentRequest request = new ComponentRequest(requestid);
					request.setRequestID(requestid.toString());
					request.setHostID(DataspaceUtils.stringToGUID(capability.getHostID()));
					request.setContainerID(capability.getContainerID());
					request.setCapabilityID(capid);

					dataspace.addPersistent(request.tuple, null);
					// System.out.println("Restored component request "+requestid);

					// created??!
					final ComponentAdvert template = new ComponentAdvert((GUID) null);
					template.setComponentRequestID(requestid);
					final GUID[] found = new GUID[1];
					found[0] = null;
					restoreSetProgress(0);

					// local only will do
					session = dataspace.addDataspaceEventListener(template.tuple, true, new DataspaceEventListener()
					{
						/** notify of event (cf DataCallbackPost) */
						@Override
						public void dataspaceEvent(final DataspaceEvent event)
						{
							if (event.getAddItem() != null)
							{
								System.out.println("Found component");
								synchronized (found)
								{
									found[0] = event.getAddItem().id;
									found.notify();
								}
							}
						}
					});

					final int MAX = 300;
					for (int count = 0; found[0] == null && !createCancelled && count < MAX; count++)
					{
						Thread.sleep(100);
						restoreSetProgress(count * 1.0f / MAX);
					}

					dataspace.removeDataspaceEventListener(session);
					session = null;
					if (found[0] == null)
					{
						// restoreLog("WARNING: request "+requestid+" had no effect\n");
						// dataspace.delete(requestid);
						// requestid = null;
						restoreLog("FAILED to create new component from capability " + capid + "\n");
					}
					else
					{
						// restoreLog("created component from capability "+capabilityid+"\n");
						// System.out.println("Succeeded: "+found[0]);
						GUID created = found[0];
						requestComponents.put(requestid.toString(), created);
						restoreLog("Created New " + capability.getCapabilityName() + " component from capability "
								+ capid + "\n");
					}
				}
				catch (final Exception e)
				{
					restoreLog("ERROR adding component request: " + e + "\n");
					e.printStackTrace(System.err);
					if (session != null)
					{
						try
						{
							dataspace.removeDataspaceEventListener(session);
						}
						catch (final Exception e2)
						{
							// Do nothing?
						}
					}
				}
			}
			restoreSetProgress(1);

			restoreLog("\n--- Setting Properties ---\n\n");

			Element components = getNamedChildElement(root, ELcomponents);
			final Element[] componentList = getNamedChildElements(components, ELcomponent);
			// re-set property
			for (int iconfig = 0; iconfig < 4; iconfig++)
			{
				// configX, configured, X
				for (final Element component : componentList)
				{
					final GUID componentID = requestComponents.get(component.getAttribute("componentrequestref"));
					final String componentName = getElementText(getNamedChildElement(component, ELname));
					if (iconfig == 0)
					{
						idMapping.put(DataspaceUtils.stringToGUID(component.getAttribute("id")), componentID);

						setComponentName(componentID, componentName);
					}

					final Element propertyRoot = getNamedChildElement(component, ELproperties);
					final Element[] properties = getNamedChildElements(propertyRoot, ELproperty);
					for (final Element property : properties)
					{
						int stage = 2;
						final String propertyName = getElementText(getNamedChildElement(property, ELname));

						if (propertyName.equals("configured"))
						{
							stage = 1;
						}
						else if (propertyName.startsWith("config"))
						{
							stage = 0;
						}
						else if (Boolean.parseBoolean(property.getAttribute("dynamic")))
						{
							stage = 3;
						}
						if (stage != iconfig)
						{
							// not this stage
							continue;
						}

						final Element valueElement = getNamedChildElement(property, ELvalue);
						String value = null;
						if (!valueElement.hasAttribute(ATisnull)
								|| !Boolean.parseBoolean(valueElement.getAttribute(ATisnull)))
						{
							value = getElementText(valueElement);
						}

						restoreLog("Setting Property " + propertyName + " on " + componentName + " to " + value + "\n");

						GUID oldID = null;
						try
						{
							oldID = DataspaceUtils.stringToGUID(property.getAttribute(ATid));
							ItemData item = dataspace.getItem(oldID);
							if (item == null)
							{

								final ComponentProperty template = new ComponentProperty((GUID) null);
								template.setComponentID(componentID);
								template.setPropertyName(propertyName);

								final ItemData[] items = dataspace.copyCollect(template.tuple);
								if (items == null || items.length == 0)
								{
									restoreLog("WARNING: property " + propertyName + " (" + oldID
											+ ") cannot be set because it cannot be found\n");
									continue;
								}
								else
								{
									item = items[0];
								}
							}

							idMapping.put(oldID, item.id);

							final ComponentProperty prop = new ComponentProperty((TupleImpl) item);
							if (prop.isReadonly())
							{
								System.out.println("Ignore readonly property " + propertyName + " (" + oldID + ")");
								continue;
							}
							// no GUI?!
							doSetProperty2(prop.getID(), value, iconfig);
						}
						catch (final Exception e)
						{
							System.err.println("ERROR setting property " + propertyName + " (" + oldID + "): " + e);
							e.printStackTrace(System.err);
						}
					}// for (property)
				}// for (component)
			}// for iconfig

			restoreSetStatus("Recreate links...");
			// recreate property link requests
			for (final Element link : getNamedChildElements(getNamedChildElement(root, ELlinks), ELlink))
			{
				GUID id = null;
				try
				{
					id = DataspaceUtils.stringToGUID(link.getAttribute(ATid));
					if (dataspace.getItem(id) != null)
					{
						restoreLog("WARNING: cannot restore link " + id + ": already present\n");
						continue;
					}

					if (!idExists(idMapping, link.getAttribute(ATsrcpropref)))
					{
						restoreLog("WARNING: cannot restore link " + id + ": Source Property " + link.getAttribute(ATsrcpropname) + " cannot be found\n");
						continue;
					}

					if (!idExists(idMapping, link.getAttribute(ATsrccompref)))
					{
						restoreLog("WARNING: cannot restore link " + id + ": Source Component cannot be found\n");
						continue;
					}

					if (!idExists(idMapping, link.getAttribute(ATdstpropref)))
					{
						restoreLog("WARNING: cannot restore link " + id + ": Destination Property " + link.getAttribute(ATdstpropname) + " cannot be found\n");
						continue;
					}

					if (!idExists(idMapping, link.getAttribute(ATdstcompref)))
					{
						restoreLog("WARNING: cannot restore link " + id + ": Destination Component cannot be found\n");
						continue;
					}

					final PropertyLinkRequest l = new PropertyLinkRequest(id);
					l.setSourcePropertyName(link.getAttribute(ATsrcpropname));
					l.setDestinationPropertyName(link.getAttribute(ATdstpropname));
					l.setSourcePropID(idMapping.get(DataspaceUtils.stringToGUID(link.getAttribute(ATsrcpropref))));
					l.setSourceComponentID(idMapping.get(DataspaceUtils.stringToGUID(link.getAttribute(ATsrccompref))));

					l.setDestinationPropID(idMapping.get(DataspaceUtils.stringToGUID(link.getAttribute(ATdstpropref))));
					l.setDestComponentID(idMapping.get(DataspaceUtils.stringToGUID(link.getAttribute(ATdstcompref))));

					dataspace.addPersistent(l.tuple, null);
					restoreLog("Recreated link " + id + " " + link.getAttribute(ATsrcpropname) + " -> "
							+ link.getAttribute(ATdstpropname) + "\n");
				}
				catch (final Exception e)
				{
					System.err.println("ERROR recreating link " + id + ": " + e);
					e.printStackTrace(System.err);
				}
			}// for (link)

			restoreSetStatus("Done");

			invokeAndWait(() -> restoreDoneButton.setEnabled(true));
			return true;
		}
		catch (final Exception e)
		{
			restoreLog("ERROR reading " + f + ": " + e + "\n");
			try
			{
				invokeAndWait(() -> restoreDoneButton.setEnabled(true));
			}
			catch (final Exception e2)
			{
				System.err.println("ERROR: " + e2);
				e2.printStackTrace(System.err);
			}

			System.err.println("ERROR reading " + f + ": " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * load frame, properties panel, set property - swing thread, no gui
	 */
	private void doSetProperty2(final GUID pid, final String newvalue, final int stage) throws Exception
	{
		final ItemData propitem = dataspace.getItem(pid);
		final ComponentProperty targetProperty = new ComponentProperty((TupleImpl) propitem);

		// do config..., configured, other
		if (!((stage == 1 && targetProperty.getPropertyName().equals("configured"))
				|| (stage == 2 && !targetProperty.getPropertyName().startsWith("config")) || (stage == 0
				&& !targetProperty.getPropertyName().equals("configured") && targetProperty.getPropertyName()
				.startsWith("config"))))
		{
			// not at this stage
			return;
		}

		final GUID myPropertyID = dataspace.allocateId();
		final GUID myComponentID = dataspace.allocateId();
		final ComponentProperty prop = new ComponentProperty(myPropertyID);
		prop.setPropertyName("configurationmanager");
		prop.setPropertyClass(String.class);
		prop.setPropertyValue(newvalue);
		prop.setComponentID(myComponentID);
		final GUID myLinkID = dataspace.allocateId();
		final PropertyLinkRequest link = new PropertyLinkRequest(myLinkID);
		link.setSourcePropertyName("configurationmanager");
		link.setSourcePropID(myPropertyID);
		link.setSourceComponentID(myComponentID);
		link.setDestinationPropertyName(targetProperty.getPropertyName());
		link.setDestinationPropID(targetProperty.getID());
		link.setDestComponentID(targetProperty.getComponentID());

		final ComponentProperty template = new ComponentProperty(pid);
		final String value[] = new String[1];
		final int updates[] = new int[1];
		// local only
		final DataSession session = dataspace.addDataspaceEventListener(template.tuple, false,
				new DataspaceEventListener()
				{
					/**
					 * notify of event (cf
					 * DataCallbackPost)
					 */
					@Override
					public void dataspaceEvent(
							final DataspaceEvent event)
					{
						try
						{
							if (event.getAddItem() != null)
							{
								final ComponentProperty p = new ComponentProperty(
										(TupleImpl) event
												.getAddItem());
								synchronized (value)
								{
									value[0] = Coerce.toClass(p.getPropertyValue(),
											String.class);
									System.out
											.println("Add target property: "
													+ value[0]);
									value.notify();
								}
							}
							else if (event.getUpdateItem() != null)
							{
								final ComponentProperty p = new ComponentProperty(
										(TupleImpl) event
												.getUpdateItem());
								synchronized (value)
								{
									value[0] = Coerce.toClass(p.getPropertyValue(),
											String.class);
									System.out
											.println("Update target property: "
													+ value[0]);
									value.notify();
									updates[0]++;
								}
							}
						}
						catch (final Exception e)
						{
							System.err
									.println("ERROR handling target property event: "
											+ e);
							e.printStackTrace(System.err);
						}
					}
				});

		// already set?
		synchronized (value)
		{
			if (value[0] != null && value[0].equals(newvalue))
			{
				System.out.println("Already equal to intended value: " + newvalue);
				return;
			}
		}

		prop.addtoDataSpace(dataspace);
		link.addtoDataSpace(dataspace);

		restoreSetProgress(0);
		final long start = System.currentTimeMillis();
		final int MAX = 30000;
		boolean done = false;
		synchronized (value)
		{
			while (true)
			{
				final long now = System.currentTimeMillis();
				final long elapsed = now - start;

				if (value[0] != null && value[0].equals(newvalue))
				{
					System.out.println("Matches value: " + newvalue);
					done = true;
					break;
				}
				if (elapsed >= MAX)
				{
					break;
				}

				final int prog = (int) (elapsed * 1000 / MAX);
				restoreSetProgress(prog);

				value.wait(1000);
			}
		}
		if (!done)
		{
			restoreLog("FAILED to set property " + pid + " to " + newvalue + "; "
					+ (value[0] != null ? value[0] + " instead" : "") + "\n");
		}

		dataspace.delete(myLinkID);
		dataspace.delete(myPropertyID);
		dataspace.removeDataspaceEventListener(session);

		restoreSetProgress(1);
	}

	/**
	 * get text of all Text nodes under an Element
	 */
	private String getElementText(final Element el)
	{
		if (el == null)
		{
			return null;
		}
		final StringBuilder builder = new StringBuilder();
		final NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.TEXT_NODE)
			{
				builder.append(children.item(i).getNodeValue());
			}
		}
		return builder.toString();
	}

	/**
	 * get named element node
	 */
	private Element getNamedChildElement(final Element el, final String name)
	{
		if (el == null)
		{
			return null;
		}
		final NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals(name))
			{
				return (Element) children
						.item(i);
			}
		}
		return null;
	}

	/**
	 * get array of so-named child elements
	 */
	private Element[] getNamedChildElements(final Element el, final String name)
	{
		if (el == null)
		{
			return null;
		}
		final List<Element> res = new ArrayList<>();
		final NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals(name))
			{
				res.add((Element) children.item(i));
			}
		}
		return res.toArray(new Element[res.size()]);
	}

	private boolean idExists(final Map<GUID, GUID> mapping, final String id)
	{
		return mapping.containsKey(DataspaceUtils.stringToGUID(id));
	}

	private void restoreLog(final String msg)
	{
		try
		{
			invokeAndWait(() ->
			{
				try
				{
					System.out.print("LOG: " + msg);
					restoreLogText.getDocument().insertString(restoreLogText.getDocument().getLength(), msg, null);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR logging " + msg + ": " + e);
					e.printStackTrace(System.err);
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	private void restoreSetProgress(final float v)
	{
		try
		{
			SwingUtilities.invokeLater(() -> restoreProgress.setValue((int) (1000 * v)));
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	private void restoreSetStatus(final String s)
	{
		try
		{
			invokeAndWait(() -> restoreStatus.setText(s));
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	private void setComponentName(final GUID component, final String name)
	{
		System.out.println("Set component name to " + name);
		if (name == null)
		{
			return;
		}
		try
		{
			final ItemData data = dataspace.getItem(component);
			if (data == null)
			{
				System.out.println("Warning: Component Not Found");
				return;
			}
			final ComponentAdvert comp = new ComponentAdvert((TupleImpl) data);

			final StringBox value = new StringBoxImpl(name);
			comp.setAttribute(BeanDescriptorHelper.DISPLAY_NAME, value);
			comp.updateinDataSpace(dataspace);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}
