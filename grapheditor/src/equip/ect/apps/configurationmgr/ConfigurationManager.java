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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import equip.data.BooleanBox;
import equip.data.DataSession;
import equip.data.GUID;
import equip.data.ItemBinding;
import equip.data.ItemData;
import equip.data.StringBox;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.Coerce;
import equip.ect.CompInfo;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.ConnectionPointTypeException;
import equip.ect.ContainerManagerHelper;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;
import equip.ect.apps.AppsResources;
import equip.ect.apps.editor.dataspace.DataspaceUtils;
import equip.ect.util.DirectoryEventListener;
import equip.ect.util.DirectoryMonitor;
import equip.runtime.ValueBase;

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
	/**
	 * my table - uneditable
	 */
	public static class MyDefaultTableModel extends DefaultTableModel
	{
		public MyDefaultTableModel(final String[] arg1, final int arg2)
		{
			super(arg1, arg2);
		}

		@Override
		public boolean isCellEditable(final int row, final int column)
		{
			return false;
		}
	}

	class CellRenderer extends JLabel implements ListCellRenderer<File>
	{
		public CellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final File value, final int index,
		                                              final boolean isSelected, final boolean cellHasFocus)
		{
			setText(value.getName());
			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return this;
		}
	}

	private JList<File> compList = new JList<File>();
	private DefaultListModel<File> listModel = new DefaultListModel<File>();
	// private Dimension defaultSize = new Dimension(225, 400);
	/**
	 * directory
	 */
	protected File directory;
	/**
	 * property link request template
	 */
	protected PropertyLinkRequest linkReqTemplate;
	/**
	 * rdf statement template
	 */
	protected RDFStatement rdfTemplate;
	/**
	 * component request template
	 */
	protected ComponentRequest compReqTemplate;
	/**
	 * all request/rdf templates
	 */
	protected CompInfo templates[];
	/**
	 * component template
	 */
	protected ComponentAdvert compAdTemplate;

	/**
	 * component property template
	 */
	protected ComponentProperty compPropTemplate;

	/**
	 * capability template
	 */
	protected Capability capTemplate;

	/**
	 * dataspace bean
	 */
	public DataspaceBean dataspace;

	/**
	 * configuration suffix
	 */
	public static final String FILE_SUFFIX = ".ect";

	/**
	 * save as file chooser
	 */
	protected JFileChooser saveAsFileChooser;

	/**
	 * actions only possible with selection
	 */
	protected AbstractAction ifSelected[];
	/**
	 * XML constants
	 */
	public static final String ELectconfiguration = "ectconfiguration";

	public static final String ELcomponents = "components";

	public static final String ELcomponent = "component";

	public static final String ELcapabilities = "capabilities";

	public static final String ELcapability = "capability";

	public static final String ELname = "name";

	public static final String ATid = "id";

	public static final String ATcapabilityref = "capabilityref";
	public static final String ATcomponentrequestref = "componentrequestref";
	public static final String ELhost = "host";
	public static final String ELcontainer = "container";
	public static final String ELproperties = "properties";
	public static final String ELproperty = "property";
	public static final String ELcomponentrequests = "componentrequests";
	public static final String ELcomponentrequest = "componentrequest";
	public static final String ELclass = "class";
	public static final String ELlinks = "links";
	public static final String ELlink = "link";
	public static final String ATpersistent = "persistent";
	public static final String ATsrcpropref = "srcpropref";
	public static final String ATsrccompref = "srccompref";
	public static final String ATsrcpropname = "srcpropname";
	public static final String ATdstpropref = "dstpropref";
	public static final String ATdstcompref = "dstcompref";
	public static final String ATdstpropname = "dstpropname";
	public static final String ELvalue = "value";
	public static final String ATreadonly = "readonly";
	public static final String ELrdfstatements = "rdfstatements";
	public static final String ELrdfstatement = "rdfstatement";
	public static final String ATrdfstatementref = "rdfstatementref";
	public static final String ELsubject = "subject";
	public static final String ELpredicate = "predicate";
	public static final String ELobject = "object";
	public static final String ATisnull = "isnull";
	public static final String VALtrue = "true";
	public static final String VALfalse = "false";
	/**
	 * GUID pattern
	 */
	protected static java.util.regex.Pattern guidPattern;
	/**
	 * component action constants
	 */
	public static final String ACTION_IGNORE = "Ignore";
	public static final String ACTION_USE_ORIGINAL = "Use original component";
	public static final String ACTION_USE_EXISTING_SAME_CLASS = "Use existing component (same class)";
	public static final String ACTION_USE_EXISTING_ANY_CLASS = "Use existing component (any class)";
	public static final String ACTION_CREATE_NEW_DYNAMIC_SAME_CLASS = "Create new component (same class)";
	public static final String ACTION_CREATE_NEW_DYNAMIC_ANY_CLASS = "Create new component (any class)";
	public static final String ACTION_CREATE_NEW_SUBCOMPONENT = "Create new component (subcomponent)";

	public static final String ACTION_CREATE_NEW_BY_HAND = "Create new component (manually)";
	/**
	 * properties action constants
	 */
	public static final String ACTION_LEAVE = "Leave current values";
	public static final String ACTION_SET_MATCHING = "Set all matching properties (ex local interfaces)";
	public static final String ACTION_SET_SELECTED = "Set all selected properties";
	/**
	 * links action constants
	 */
	public static final String ACTION_OMIT = "Omit link";
	public static final String ACTION_CREATE_EXACT = "Create link exactly as before";
	public static final String ACTION_CREATE_ALTERNATIVE = "Create alternative link";
	public static final String CONFIG_PATH = "./config/";

	public static void invokeAndWait(final Runnable r) throws InterruptedException,
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

	/**
	 * app main. Usage: [ dataspaceUrl [ configurationDir ] ]. Default dataspace url
	 * "equip://:9123". Default configuration dir "."
	 */
	public static void main(final String args[]) throws java.io.IOException, DataspaceInactiveException
	{
		ConfigurationManager gui;
		if (args.length == 1)
		{
			gui = new ConfigurationManager(args[0], ".");
		}
		else if (args.length == 2)
		{
			gui = new ConfigurationManager(args[0], args[1]);
		}
		else
		{
			gui = new ConfigurationManager("equip://:9123", ".");
		}
		final JFrame frame = new JFrame("ECT Configuration Manager");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(gui);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * load frame
	 */
	JFrame frame;
	/**
	 * load frame switched panel
	 */
	JTabbedPane switchedPanel;
	/**
	 * load frame component panel
	 */
	JPanel componentPanel;
	/**
	 * load frame create panel
	 */
	JPanel createPanel;
	/**
	 * load frame properties panel
	 */
	JPanel propertiesPanel;
	/**
	 * load frame links panel
	 */
	JPanel linksPanel;
	/**
	 * load frame status line
	 */
	JLabel status;
	/**
	 * load frame component choice panel label
	 */
	JLabel componentLabel;
	/**
	 * load frame component choice panel action chooser
	 */
	JComboBox<String> componentAction;
	/**
	 * load frame component choice panel 'go'
	 */
	JButton componentGo;
	/**
	 * load frame, component choice panel, component table model
	 */
	DefaultTableModel componentTableModel;
	/**
	 * load frame, component choice panel, component table
	 */
	JTable componentTable;
	/**
	 * load frame, component choice panel, component chooser
	 */
	JComboBox<String> candidatesCombo;
	/**
	 * load frame, component choice panel, component chooser model
	 */
	DefaultComboBoxModel<String> candidatesComboModel;

	/**
	 * load frame, create panel, progress bar
	 */
	JProgressBar createProgress;

	/**
	 * load frame properties panel action chooser
	 */
	JComboBox<String> propertiesAction;

	/**
	 * load frame properties panel 'go'
	 */
	JButton propertiesGo;

	/**
	 * load frame, properties panel, component table model
	 */
	DefaultTableModel propertiesTableModel;
	/**
	 * load frame, properties panel, component table
	 */
	JTable propertiesTable;
	/**
	 * load frame, properties panel, progress bar
	 */
	JProgressBar propertiesProgress;
	/**
	 * load frame links panel action chooser
	 */
	JComboBox<String> linksAction;
	/**
	 * load frame links panel 'go'
	 */
	JButton linksGo;
	/**
	 * load frame, links panel, link table model
	 */
	DefaultTableModel linksTableModel;
	/**
	 * load frame, links panel, link table
	 */
	JTable linksTable;
	/**
	 * load frame log text area
	 */
	JTextArea logText;
	/**
	 * load frame log area done button
	 */
	JButton doneButton;
	/**
	 * load frame, current xml component
	 */
	Element component;
	/**
	 * load frame, current xml components
	 */
	Element components;
	/**
	 * load frame, current xml capabilities (all)
	 */
	Element capabilities;
	/**
	 * load frame, component choice panel, current chosen action
	 */
	String action;
	/**
	 * load frame, component choice panel, all candidates for choice (String (name) -> GUID)
	 */
	Map<String, GUID> candidates;
	/**
	 * load frame, component choice panel, current chosen candidate component/capability
	 */
	GUID candidate;
	/**
	 * load frame, component choice panel, current candidate is a capability?
	 */
	boolean capabilityOnly;
	/**
	 * load frame, mapping from xml component id (String) to new/current component GUID
	 */
	Map<String, GUID> componentMapping;
	/**
	 * load frame, mapping from xml property id (String) to new/current property GUID
	 */
	Map<String, GUID> propertyMapping;
	/**
	 * load frame, create panel, created component
	 */
	GUID created;
	/**
	 * load frame, create panel, create cancelled
	 */
	boolean createCancelled;
	/**
	 * load frame, properties panel, now component
	 */
	GUID currentComponentId;
	/**
	 * load frame, properties panel, properties action
	 */
	String propertiesActionChosen;
	/**
	 * load frame, links panel, current link elemnt
	 */
	Element linkel;
	/**
	 * links done sync
	 */
	boolean[] linksDone = new boolean[1];
	protected ComponentProperty srcprop;
	protected ComponentProperty dstprop;
	protected boolean automatic = false;
	protected boolean giveup = false;
	/**
	 * frame
	 */
	JFrame restoreFrame;
	/**
	 * restore frame status line
	 */
	JLabel restoreStatus;
	/**
	 * restore frame, progress bar
	 */
	JProgressBar restoreProgress;
	/**
	 * restore frame log text area
	 */
	JTextArea restoreLogText;

	/**
	 * restore frame log area done button
	 */
	JButton restoreDoneButton;

	/**
	 * cons for internal use by GraphEditor etc
	 */
	public ConfigurationManager(final DataspaceBean dataspace)
	{
		this.dataspace = dataspace;

		compReqTemplate = new ComponentRequest((GUID) null);
		// dataspace.addDataspaceEventListener(compReqTemplate.tuple, false, this);
		linkReqTemplate = new PropertyLinkRequest((GUID) null);
		// dataspace.addDataspaceEventListener(linkReqTemplate.tuple, false, this);
		rdfTemplate = new RDFStatement((GUID) null);
		// dataspace.addDataspaceEventListener(rdfTemplate.tuple, false, this);
		compAdTemplate = new ComponentAdvert((GUID) null);
		// dataspace.addDataspaceEventListener(compAdTemplate.tuple, false, this);
		compPropTemplate = new ComponentProperty((GUID) null);
		// dataspace.addDataspaceEventListener(compPropTemplate.tuple, false, this);
		capTemplate = new Capability((GUID) null);
		// dataspace.addDataspaceEventListener(capTemplate.tuple, false, this);
		templates = new CompInfo[]{compReqTemplate, linkReqTemplate, rdfTemplate};
	}

	/**
	 * default cons
	 */
	public ConfigurationManager(final String dataspaceUrl, final String directory) throws java.io.IOException,
			DataspaceInactiveException
	{
		this.directory = new File(directory);
		JPanel panel = createInnerPanel();
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		// setPreferredSize(defaultSize);

		dataspace = ContainerManagerHelper.createDataSpaceBean(dataspaceUrl);
		compReqTemplate = new ComponentRequest((GUID) null);
		dataspace.addDataspaceEventListener(compReqTemplate.tuple, false, this);
		linkReqTemplate = new PropertyLinkRequest((GUID) null);
		dataspace.addDataspaceEventListener(linkReqTemplate.tuple, false, this);
		rdfTemplate = new RDFStatement((GUID) null);
		dataspace.addDataspaceEventListener(rdfTemplate.tuple, false, this);
		compAdTemplate = new ComponentAdvert((GUID) null);
		dataspace.addDataspaceEventListener(compAdTemplate.tuple, false, this);
		compPropTemplate = new ComponentProperty((GUID) null);
		dataspace.addDataspaceEventListener(compPropTemplate.tuple, false, this);
		capTemplate = new Capability((GUID) null);
		dataspace.addDataspaceEventListener(capTemplate.tuple, false, this);
		templates = new CompInfo[]{compReqTemplate, linkReqTemplate, rdfTemplate};
		final DirectoryMonitor directoryMonitor = new DirectoryMonitor(this.directory, true, false);
		new Thread(directoryMonitor).start();
		directoryMonitor.addDirectoryEventListener(this);
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
	 * load an XML configuration
	 */
	public boolean doLoadConfiguration(final File f)
	{
		try
		{
			automatic = false;
			giveup = false;
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					if (frame == null)
					{
						createLoadGui();
					}
					doneButton.setEnabled(false);
					frame.setVisible(true);
					logText.setText("Recreating configuration from file " + f);
					status.setText("Loading document...");
				}
			});

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(f);
			System.out.println("Read document OK");
			final Element root = document.getDocumentElement();

			status.setText("Getting top-level elements...");

			// components
			components = getNamedChildElement(root, ELcomponents);
			final Element component[] = (components != null) ? getNamedChildElements(components, ELcomponent) : null;

			capabilities = getNamedChildElement(root, ELcapabilities);
			// final Element componentrequests = getNamedChildElement(root, ELcomponentrequests);
			final Element linksel = getNamedChildElement(root, ELlinks);
			final Element rdfstatements = getNamedChildElement(root, ELrdfstatements);

			componentMapping = new HashMap<String, GUID>();
			propertyMapping = new HashMap<String, GUID>();

			for (int parents = 1; parents >= 0; parents--)
			{
				System.out.println("parents? " + (parents != 0));
				for (int ci = 0; component != null && ci < component.length; ci++)
				{
					boolean parent = true;
					{
						final Element props[] = getNamedChildElements(getNamedChildElement(component[ci],
								ELproperties),
								ELproperty);
						for (int pi = 0; parent && props != null && pi < props.length; pi++)
						{
							final String propname = getElementText(getNamedChildElement(props[pi], ELname));
							if (propname != null && propname.equals("parent"))
							{
								parent = false;
							}
						}
					}
					if (parent ^ (parents != 0))
					{
						continue;
					}

					final int fci = ci;
					final boolean hasParent = !parent;
					this.component = component[ci];
					// capabilities = capabilities;
					invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								// initialise for next component
								status.setText("Dealing with component " + fci + "...");

								componentLabel.setText("Component "
										+ getElementText(getNamedChildElement(ConfigurationManager.this.component,
										ELname)));
								String action = ACTION_CREATE_NEW_DYNAMIC_SAME_CLASS;
								if (hasParent)
								{
									action = ACTION_USE_EXISTING_SAME_CLASS;
								}
								componentAction.getModel().setSelectedItem(action);
								handleComponentAction(action);
							}
							catch (final Exception e)
							{
								System.err.println("ERROR dealing with component: " + e);
								e.printStackTrace(System.err);
							}
						}
					});
					// what kind of component is it?
					// - dynamically requested (has request and capability)?
					// - sub-component - presumed auto-generated of another component
					// -- was the parent component dynamic/a subcomponent/...
					// --- try to make it/find it
					// -- is the parent component similarly configured, e.g. started
					// - neither - presumed explicitly created

					// boolean wasDynamic = false;
					// if (component[ci].hasAttribute(ATcomponentrequestref))
					// {
					// wasDynamic = true;
					// }

					// final GUID id = stringToGUID(component[ci].getAttribute(ATid));
					// final boolean exists = (dataspace.getItem(id) != null);

					// wait for GO!
					synchronized (this)
					{
						try
						{
							if (!automatic && !giveup)
							{
								wait();
							}
							if (giveup)
							{
								throw new GiveUpException();
							}
						}
						catch (final InterruptedException e)
						{
							System.err.println("Interrupted!");
						}
					}
					// handle chosen action
					if (action.equals(ACTION_IGNORE))
					{
						System.out.println("Ignored");
						log("Ignored component " + getElementText(getNamedChildElement(component[ci], ELname)) + "\n");
					}
					else if (action.equals(ACTION_USE_ORIGINAL) || action.equals(ACTION_USE_EXISTING_SAME_CLASS)
							|| action.equals(ACTION_USE_EXISTING_ANY_CLASS))
					{
						log("Used current component " + candidate + " in place of component "
								+ getElementText(getNamedChildElement(component[ci], ELname)) + "\n");
						componentMapping.put(this.component.getAttribute(ATid), candidate);
						System.out.println("Use existing component: " + candidate);
						doHandleProperties(candidate);
						mapComponentProperties(this.component, candidate);
					}
					else if (action.equals(ACTION_CREATE_NEW_DYNAMIC_SAME_CLASS)
							|| action.equals(ACTION_CREATE_NEW_DYNAMIC_ANY_CLASS))
					{
						doHandleCreateComponent(candidate);
						if (created == null)
						{
							// try again
							ci--;
							continue;
						}
						componentMapping.put(this.component.getAttribute(ATid), created);
						doHandleProperties(created);
						mapComponentProperties(this.component, candidate);
					}
					// ACTION_CREATE_NEW_SUBCOMPONENT,
					// ACTION_CREATE_NEW_BY_HAND ACTION_USE_EXISTING_SAME_CLASS))
					else
					{
						// ....
						System.out.println("WARNING: unknown action: " + action);
					}
				}
			}
			final Element linkels[] = (linksel != null) ? getNamedChildElements(linksel, ELlink) : null;
			for (int li = 0; linkels != null && li < linkels.length; li++)
			{
				doHandleLink(linkels[li]);
			}

			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{

					status.setText("Add RDFStatements");
					switchedPanel.setEnabledAt(0, false);
					switchedPanel.setEnabledAt(1, false);
					switchedPanel.setEnabledAt(2, false);
					switchedPanel.setEnabledAt(3, false);
					switchedPanel.setSelectedIndex(4);
					doneButton.setEnabled(true);
					// frame.setVisible(false);
				}
			});

			// mapping
			final Map<String, String> rdfmapping = new HashMap<String, String>();
			for (final String ck : componentMapping.keySet())
			{
				final GUID id = componentMapping.get(ck);
				rdfmapping.put(RDFStatement.GUID_NAMESPACE + ck.substring(1, ck.length() - 1),
						RDFStatement.GUIDToUrl(id));
			}
			for (final String pk : propertyMapping.keySet())
			{
				final GUID id = propertyMapping.get(pk);
				rdfmapping.put(RDFStatement.GUID_NAMESPACE + pk.substring(1, pk.length() - 1),
						RDFStatement.GUIDToUrl(id));
			}
			final Element rdfels[] = (rdfstatements != null) ? getNamedChildElements(rdfstatements, ELrdfstatement)
					: null;
			for (int ri = 0; rdfels != null && ri < rdfels.length; ri++)
			{
				String subject = getElementText(getNamedChildElement(rdfels[ri], ELsubject));
				if (rdfmapping.containsKey(subject))
				{
					subject = rdfmapping.get(subject);
				}
				String predicate = getElementText(getNamedChildElement(rdfels[ri], ELpredicate));
				if (rdfmapping.containsKey(predicate))
				{
					predicate = rdfmapping.get(predicate);
				}
				String object = getElementText(getNamedChildElement(rdfels[ri], ELobject));
				if (rdfmapping.containsKey(object))
				{
					object = rdfmapping.get(object);
				}
				final GUID id = dataspace.allocateId();
				final RDFStatement s = new RDFStatement(id, subject, predicate, object);

				dataspace.addPersistent(s.tuple, null);
				log("Recreated mapped statement " + id + " (" + s.getSubject() + "," + s.getPredicate() + ","
						+ s.getObject() + ")\n");
			}

			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{

					status.setText("Done");
					doneButton.setEnabled(true);
					// frame.setVisible(false);
				}
			});

			// ....
			return true;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR reading " + f + ": " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * load an XML configuration - NOT swing thread
	 */
	public boolean doRestoreConfiguration(final File f)
	{
		try
		{
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
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
				}
			});

			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			// factory.setValidating(true);
			// factory.setNamespaceAware(true);
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(f);
			System.out.println("Read document OK");
			final Element root = document.getDocumentElement();

			restoreSetStatus("Getting top-level elements...");

			capabilities = getNamedChildElement(root, ELcapabilities);

			final Map<String, GUID> requestComponents = new HashMap<String, GUID>();
			final Map<GUID, GUID> idMapping = new HashMap<GUID, GUID>();

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
						created = found[0];
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

			components = getNamedChildElement(root, ELcomponents);
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

			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					restoreDoneButton.setEnabled(true);
				}
			});
			return true;
		}
		catch (final Exception e)
		{
			restoreLog("ERROR reading " + f + ": " + e + "\n");
			try
			{
				invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						restoreDoneButton.setEnabled(true);
					}
				});
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
	public void filesAdded(final List<File> files)
	{
		for (final File file : files)
		{
			if (!file.getName().endsWith(FILE_SUFFIX))
			{
				System.out.println("Ignore file " + file);
				return;
			}
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
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
				}
			});
		}
	}

	/**
	 * DirectoryEventListener implementation
	 */
	@Override
	public void fileDeleted(final File file)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				listModel.removeElement(file);
			}
		});
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
	 * load configuration from file
	 */
	public boolean loadConfiguration(final File f)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				doLoadConfiguration(f);
			}
		}.start();
		return true;
	}

	/**
	 * load configuration from file; wait for completion, and return Hashtable of GUID strings ->
	 * new GUIDs
	 */
	public void loadConfigurationAndMapping(final File f, final Map<String, GUID> mapping, final Runnable continuation)
	{
		new Thread()
		{
			@Override
			public void run()
			{

				doLoadConfiguration(f);

				// Hashtable mapping = new Hashtable();
				if (componentMapping != null)
				{
					mapping.putAll(componentMapping);
				}
				if (propertyMapping != null)
				{
					mapping.putAll(propertyMapping);
				}

				try
				{
					invokeAndWait(continuation);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in loadConfigurationAndMapping continuation: " + e);
					e.printStackTrace(System.err);
				}
			}
		}.start();
	}

	/**
	 * load configuration from file
	 */
	public boolean loadNonXferConfiguration(final File f)
	{
		try
		{
			final equip.runtime.ObjectInputStream ins = new equip.runtime.ObjectInputStream(
					new java.io.FileInputStream(f));

			clearConfiguration();

			int c = 0;
			while (true)
			{
				final ItemData item = (ItemData) ins.readObject();
				if (item == null)
				{
					break;
				}
				dataspace.addPersistent(item, null);
				c++;
			}
			System.out.println("Loaded " + c + " item in total");
			ins.close();
			return true;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR reading configiration from " + f + ": " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * load an XML configuration - swing thread
	 */
	public boolean restoreConfiguration(final File f)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				doRestoreConfiguration(f);
			}
		}.start();
		return true;
	}

	public boolean saveConfigurationAs(File file)
	{
		return saveXMLDoc(file, toXML());
	}

	public boolean saveXMLDoc(File file, Document doc)
	{
		try
		{
			// Use a Transformer for output
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final Transformer transformer = tFactory.newTransformer();
			// we want to pretty format the XML output
			// note : aparently this is broken in jdk1.5 beta!
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			final DOMSource source = new DOMSource(doc);
			final StreamResult result = new StreamResult(file);
			// StreamResult result = new StreamResult(System.out);
			transformer.transform(source, result);

			System.out.println("Done");
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * save configuration to file (overwrite). Save in (hopefully) transferable and relatively
	 * future-proof XML format with supporting information.
	 */
	public Document toXML()
	{
		try
		{
			final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final Document doc = builder.newDocument();
			final Element root = doc.createElement(ELectconfiguration);
			doc.appendChild(root);

			final Map<GUID, GUID> captable = new HashMap<GUID, GUID>();
			final Map<GUID, GUID> reqtable = new HashMap<GUID, GUID>();

			// components
			final Element components = doc.createElement(ELcomponents);
			doc.getDocumentElement().appendChild(components);

			for (final ItemData item : dataspace.copyCollect(compAdTemplate.tuple))
			{
				final ComponentAdvert compitem = new ComponentAdvert((TupleImpl) item);
				final Element component = doc.createElement(ELcomponent);
				components.appendChild(component);
				component.setAttribute(ATid, compitem.getComponentID().toString());
				if (compitem.getCapabilityID() != null)
				{
					captable.put(compitem.getComponentID(), compitem.getCapabilityID());
					component.setAttribute(ATcapabilityref, compitem.getCapabilityID().toString());
				}
				if (compitem.getComponentRequestID() != null)
				{
					reqtable.put(compitem.getComponentID(), compitem.getComponentRequestID());
					component.setAttribute(ATcomponentrequestref, compitem.getComponentRequestID().toString());
				}
				if (compitem.getComponentName() != null)
				{
					final Element name = doc.createElement(ELname);
					name.appendChild(doc.createTextNode(DataspaceUtils.getCurrentName(compitem)));
					component.appendChild(name);
				}
				if (compitem.getHostID() != null)
				{
					final Element name = doc.createElement(ELhost);
					name.appendChild(doc.createTextNode(compitem.getHostID()));
					component.appendChild(name);
				}
				if (compitem.getContainerID() != null)
				{
					final Element name = doc.createElement(ELcontainer);
					name.appendChild(doc.createTextNode(compitem.getContainerID().toString()));
					component.appendChild(name);
				}
				// persistent?
				ItemBinding binding = dataspace.getDataProxy().getItemBinding(compitem.getComponentID());
				if (binding != null && binding.info != null)
				{
					component.setAttribute(ATpersistent, !binding.info.processBound ? VALtrue : VALfalse);
				}

				final Element properties = doc.createElement(ELproperties);
				component.appendChild(properties);

				final ComponentProperty propTemplate = new ComponentProperty((GUID) null);
				propTemplate.setComponentID(compitem.getComponentID());
				for (final ComponentProperty prop : propTemplate.copyCollectAsComponentProperty(dataspace))
				{
					final Element property = doc.createElement(ELproperty);
					properties.appendChild(property);
					property.setAttribute(ATid, prop.getID().toString());

					final ValueBase value = prop.getAttributeValue("dynamic");
					if (value != null && value instanceof BooleanBox && ((BooleanBox) value).value)
					{
						property.setAttribute("dynamic", "true");
					}

					// readonly??
					if (prop.getPropertyName() != null)
					{
						final Element name = doc.createElement(ELname);
						name.appendChild(doc.createTextNode(prop.getPropertyName()));
						property.appendChild(name);
					}
					if (prop.getPropertyClass() != null)
					{
						final Element name = doc.createElement(ELclass);
						name.appendChild(doc.createTextNode(prop.getPropertyClass()));
						property.appendChild(name);
					}
					// persistent?
					binding = dataspace.getDataProxy().getItemBinding(prop.getID());
					if (binding != null && binding.info != null)
					{
						property.setAttribute(ATpersistent, !binding.info.processBound ? VALtrue : VALfalse);
					}

					try
					{
						Object val = prop.getPropertyValue();
						val = Coerce.toClass(val, String.class);
						final String sval = (String) val;
						final Element name = doc.createElement(ELvalue);
						if (sval != null)
						{
							name.appendChild(doc.createTextNode(sval));
						}
						else
						{
							name.setAttribute(ATisnull, VALtrue);
						}
						property.appendChild(name);
					}
					catch (final Exception vex)
					{
						System.err.println("ERROR getting property value: " + vex);
						vex.printStackTrace(System.err);
					}
				}
			}

			// capabilities
			final Element capabilities = doc.createElement(ELcapabilities);
			doc.getDocumentElement().appendChild(capabilities);

			final ItemData[] capitems = dataspace.copyCollect(capTemplate.tuple);
			for (int i = 0; capitems != null && i < capitems.length; i++)
			{
				final Capability capitem = new Capability((TupleImpl) capitems[i]);
				if (!captable.containsValue(capitem.getID()))
				{
					// capabilities.appendChild(doc.createComment("Capability " + capitem.getID() +
					// " unreferenced"));
					continue;
				}
				final Element capability = doc.createElement(ELcapability);
				capabilities.appendChild(capability);
				capability.setAttribute(ATid, capitem.getID().toString());
				if (capitem.getCapabilityName() != null)
				{
					final Element name = doc.createElement(ELname);
					name.appendChild(doc.createTextNode(capitem.getCapabilityName()));
					capability.appendChild(name);
				}
				if (capitem.getHostID() != null)
				{
					final Element name = doc.createElement(ELhost);
					name.appendChild(doc.createTextNode(capitem.getHostID()));
					capability.appendChild(name);
				}
				if (capitem.getContainerID() != null)
				{
					final Element name = doc.createElement(ELcontainer);
					name.appendChild(doc.createTextNode(capitem.getContainerID().toString()));
					capability.appendChild(name);
				}
				if (capitem.getCapabilityClass() != null)
				{
					final Element name = doc.createElement(ELclass);
					name.appendChild(doc.createTextNode(capitem.getCapabilityClass()));
					capability.appendChild(name);
				}
			}

			// component requests
			final Element componentrequests = doc.createElement(ELcomponentrequests);
			doc.getDocumentElement().appendChild(componentrequests);

			final ItemData[] reqitems = dataspace.copyCollect(compReqTemplate.tuple);
			for (int i = 0; reqitems != null && i < reqitems.length; i++)
			{
				final ComponentRequest reqitem = new ComponentRequest((TupleImpl) reqitems[i]);
				final Element componentrequest = doc.createElement(ELcomponentrequest);
				componentrequests.appendChild(componentrequest);
				if (!reqtable.containsValue(reqitem.getID()))
				{
					componentrequest.appendChild(doc.createComment("ComponentRequest " + reqitem.getID()
							+ " unreferenced"));
					// still include it
				}

				// requestid field is unused/spurious
				componentrequest.setAttribute(ATid, reqitem.getID().toString());
				if (reqitem.getCapabilityID() != null)
				{
					componentrequest.setAttribute(ATcapabilityref, reqitem.getCapabilityID().toString());
				}
				if (reqitem.getHostID() != null)
				{
					final Element name = doc.createElement(ELhost);
					// this host id is a guid for some (unknown) reason!
					name.appendChild(doc.createTextNode(reqitem.getHostID().toString()));
					componentrequest.appendChild(name);
				}
				if (reqitem.getContainerID() != null)
				{
					final Element name = doc.createElement(ELcontainer);
					name.appendChild(doc.createTextNode(reqitem.getContainerID().toString()));
					componentrequest.appendChild(name);
				}
				// persistent?
				final ItemBinding binding = dataspace.getDataProxy().getItemBinding(reqitem.getID());
				if (binding != null && binding.info != null)
				{
					componentrequest.setAttribute(ATpersistent, !binding.info.processBound ? VALtrue : VALfalse);
				}
			}

			// property link requests
			final Element links = doc.createElement(ELlinks);
			doc.getDocumentElement().appendChild(links);

			final ItemData[] linkreqitems = dataspace.copyCollect(linkReqTemplate.tuple);
			for (int i = 0; linkreqitems != null && i < linkreqitems.length; i++)
			{
				final PropertyLinkRequest linkreqitem = new PropertyLinkRequest((TupleImpl) linkreqitems[i]);
				final Element link = doc.createElement(ELlink);
				links.appendChild(link);
				link.setAttribute(ATid, linkreqitem.getID().toString());
				if (linkreqitem.getSourcePropID() != null)
				{
					link.setAttribute(ATsrcpropref, linkreqitem.getSourcePropID().toString());
				}
				if (linkreqitem.getDestinationPropID() != null)
				{
					link.setAttribute(ATdstpropref, linkreqitem.getDestinationPropID().toString());
				}
				if (linkreqitem.getSourcePropertyName() != null)
				{
					link.setAttribute(ATsrcpropname, linkreqitem.getSourcePropertyName());
				}
				if (linkreqitem.getDestinationPropertyName() != null)
				{
					link.setAttribute(ATdstpropname, linkreqitem.getDestinationPropertyName());
				}
				if (linkreqitem.getSourceComponentID() != null)
				{
					link.setAttribute(ATsrccompref, linkreqitem.getSourceComponentID().toString());
				}
				if (linkreqitem.getDestComponentID() != null)
				{
					link.setAttribute(ATdstcompref, linkreqitem.getDestComponentID().toString());
				}
				// persistent?
				final ItemBinding binding = dataspace.getDataProxy().getItemBinding(linkreqitem.getID());
				if (binding != null && binding.info != null)
				{
					link.setAttribute(ATpersistent, !binding.info.processBound ? VALtrue : VALfalse);
				}
			}

			return doc;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR Convering to XML");
			e.printStackTrace(System.err);
		}
		return null;
	}

	/**
	 * save configuration to file (overwrite)
	 */
	public boolean saveNonXferConfigurationAs(final File f)
	{
		try
		{
			final equip.runtime.ObjectOutputStream outs = new equip.runtime.ObjectOutputStream(
					new java.io.FileOutputStream(f));

			for (final CompInfo template : templates)
			{
				final ItemData[] cret = dataspace.copyCollect(template.tuple);
				System.out.println("Write " + cret.length + " " + template.getType());
				for (final ItemData element : cret)
				{
					// only persistent?!
					outs.writeObject(element);
				}
			}

			// trailing null
			outs.writeObject(null);
			outs.close();
			return true;
		}
		catch (final Exception e)
		{
			System.err.println("ERROR writing configiration as " + f + ": " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * list selection listener
	 */
	@Override
	public void valueChanged(final ListSelectionEvent e)
	{
		final boolean enabled = (compList.getSelectedValue() != null);
		for (final AbstractAction element : ifSelected)
		{
			element.setEnabled(enabled);
		}
	}


	protected JPanel createInnerPanel()
	{
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(compList), BorderLayout.CENTER);
		compList.setModel(listModel);
		compList.setCellRenderer(new CellRenderer());
		compList.addListSelectionListener(this);
		compList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JPanel toolbar = new JPanel(new FlowLayout());
		saveAsFileChooser = new JFileChooser(directory);
		saveAsFileChooser.setFileFilter(new javax.swing.filechooser.FileFilter()
		{
			@Override
			public boolean accept(final File f)
			{
				return f.getName().endsWith(".ect");
			}

			@Override
			public String getDescription()
			{
				return ".ect";
			}
		});
		saveAsFileChooser.setDialogTitle("Save current configuration as...");
		toolbar.add(new JButton(new AbstractAction("Save as New...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Save as New...");
				if (saveAsFileChooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION)
				{
					File f = saveAsFileChooser.getSelectedFile();
					if (!f.getName().endsWith(".ect"))
					{
						f = new File(f.getAbsolutePath() + ".ect");
					}
					saveConfigurationAs(f);
				}
			}
		}));
		ifSelected = new AbstractAction[3];
		toolbar.add(new JButton(ifSelected[0] = new AbstractAction("Save as")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File f = compList.getSelectedValue();
				if (f != null)
				{
					System.out.println("Save as " + f);
					saveConfigurationAs(f);
				}
			}
		}));
		toolbar.add(new JButton(new AbstractAction("Clear Current")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Clear current");
				clearConfiguration();
			}
		}));
		toolbar.add(new JButton(ifSelected[1] = new AbstractAction("Clear and Restore")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File f = compList.getSelectedValue();
				if (f != null)
				{
					System.out.println("Clear current");
					clearConfiguration();
					System.out.println("Restore " + f);
					restoreConfiguration(f);
				}
			}
		}));
		toolbar.add(new JButton(ifSelected[1] = new AbstractAction("Merge/import")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File f = compList.getSelectedValue();
				if (f != null)
				{
					System.out.println("Load " + f);
					loadConfiguration(f);
				}
			}
		}));
		toolbar.add(new JButton(ifSelected[2] = new AbstractAction("Delete")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File f = compList.getSelectedValue();
				if (f != null)
				{
					System.out.println("Delete " + f);
					f.delete();
				}
			}
		}));
		for (final AbstractAction element : ifSelected)
		{
			element.setEnabled(false);
		}
		panel.add(toolbar, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * create load GUI
	 */
	protected void createLoadGui()
	{
		// load frame - should be preserved between calls
		frame = new JFrame("load configuration");
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				System.out.println("Cancel load");
				frame.setVisible(false);
				giveup = true;
				synchronized (ConfigurationManager.this)
				{
					ConfigurationManager.this.notifyAll();
				}
			}
		});
		status = new JLabel();
		frame.getContentPane().setLayout(new BorderLayout());
		/*
		 * JMenuBar menus = new JMenuBar(); JMenu auto = new JMenu("Auto"); auto.add(new
		 * JMenuItem(new AbstractAction("GO!") { public void actionPerformed(ActionEvent ae) {
		 * System.out.println("GO go auto..."); automatic = true; // .... } }));
		 */
		frame.getContentPane().add(status, BorderLayout.SOUTH);
		// frame.getContentPane().setPreferredSize(500,600);

		componentPanel = new JPanel();
		componentPanel.setLayout(new BorderLayout());

		JPanel p = new JPanel();
		// SpringLayout pl = new SpringLayout();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		componentLabel = new JLabel("Component...");
		p.add(componentLabel);
		// pl.putConstraint(SpringLayout.NORTH, componentLabel, Spring.constant(0),
		// SpringLayout.NORTH, p);
		// pl.putConstraint(SpringLayout.WEST, componentLabel, Spring.constant(0),
		// SpringLayout.WEST, p);
		// pl.putConstraint(SpringLayout.EAST, componentLabel, Spring.constant(0),
		// SpringLayout.EAST, p);

		p.add(new JLabel("Select action"));
		componentAction = new JComboBox<String>(new String[]{ACTION_IGNORE, ACTION_USE_ORIGINAL,
				ACTION_USE_EXISTING_SAME_CLASS,
				// ACTION_USE_EXISTING_ANY_CLASS,
				ACTION_CREATE_NEW_DYNAMIC_SAME_CLASS // ,
				// ACTION_CREATE_NEW_DYNAMIC_ANY_CLASS,
				// ACTION_CREATE_NEW_SUBCOMPONENT,
				// ACTION_CREATE_NEW_BY_HAND
		});
		componentAction.setEditable(false);
		componentAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String item = (String) componentAction.getSelectedItem();
				System.out.println("Component action: " + item);
				handleComponentAction(item);
			}
		});
		p.add(componentAction);

		candidatesComboModel = new DefaultComboBoxModel<String>();
		candidatesCombo = new JComboBox<String>(candidatesComboModel);
		candidatesCombo.setEditable(false);
		candidatesCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String item = (String) candidatesCombo.getSelectedItem();
				System.out.println("Candidate: " + item);
				handleCandidate(item);
			}
		});
		p.add(new JLabel("Select current component/capability"));
		p.add(candidatesCombo);
		// pl.putConstraint(SpringLayout.NORTH, componentAction, Spring.constant(0),
		// SpringLayout.SOUTH, componentLabel);
		// pl.putConstraint(SpringLayout.WEST, componentAction, Spring.constant(0),
		// SpringLayout.WEST, p);

		final JPanel p2 = new JPanel();
		componentGo = new JButton(new AbstractAction("OK")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("GO!");
				synchronized (ConfigurationManager.this)
				{
					ConfigurationManager.this.notify();
				}
			}
		});
		p2.add(componentGo);
		p2.add(new JButton(new AbstractAction("Automatic!")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("GO auto!");
				automatic = true;
				synchronized (ConfigurationManager.this)
				{
					ConfigurationManager.this.notify();
				}
			}
		}));
		componentPanel.add(p2, BorderLayout.SOUTH);
		// pl.putConstraint(SpringLayout.NORTH, componentGo, Spring.constant(0),
		// SpringLayout.NORTH, componentAction);
		// pl.putConstraint(SpringLayout.SOUTH, componentGo, Spring.constant(0),
		// SpringLayout.SOUTH, componentAction);
		// pl.putConstraint(SpringLayout.WEST, componentGo, Spring.constant(0),
		// SpringLayout.EAST, componentAction);
		// pl.putConstraint(SpringLayout.EAST, componentGo, Spring.constant(0,0,1000),
		// SpringLayout.EAST, p);

		// pl.putConstraint(SpringLayout.SOUTH, componentAction, Spring.constant(0),
		// SpringLayout.SOUTH, p);
		// pl.layoutContainer(p);

		p.add(new JLabel("Component comparison"));

		componentTableModel = new MyDefaultTableModel(new String[]{"Attribute", "Old", "New"}, 0);
		componentTable = new JTable(componentTableModel);
		JScrollPane tableScroll = new JScrollPane(componentTable);
		// p.add(tableScroll);

		// pl.putConstraint(SpringLayout.NORTH, tableScroll, Spring.constant(0),
		// SpringLayout.SOUTH, componentAction);
		// pl.putConstraint(SpringLayout.SOUTH, tableScroll, Spring.constant(0, 0, 1000),
		// SpringLayout.SOUTH, p);
		// pl.putConstraint(SpringLayout.WEST, tableScroll, Spring.constant(0, 0, 1000),
		// SpringLayout.WEST, p);
		// pl.putConstraint(SpringLayout.EAST, tableScroll, Spring.constant(0),
		// SpringLayout.EAST, p);
		componentPanel.add(tableScroll, BorderLayout.CENTER);

		componentPanel.add(p, BorderLayout.NORTH);

		switchedPanel = new JTabbedPane();
		switchedPanel.add("Component", componentPanel);

		createPanel = new JPanel();
		createPanel.setLayout(new BorderLayout());
		// createPanel.add(new JLabel("Create component..."), BorderLayout.NORTH);
		p = new JPanel();
		// SpringLayout pl = new SpringLayout();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new JLabel("Create component..."));
		p.add(new JLabel("Waiting..."));
		createProgress = new JProgressBar(0, 100);
		p.add(createProgress);
		// p.setValue(X);
		final JButton cancel = new JButton(new AbstractAction("Cancel")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				handleCancelCreate();
			}
		});
		// p.add(cancel);
		createPanel.add(cancel, BorderLayout.SOUTH);
		createPanel.add(p, BorderLayout.CENTER);

		switchedPanel.add("Create", createPanel);

		propertiesPanel = new JPanel();
		propertiesPanel.setLayout(new BorderLayout());
		// propertiesPanel.add(new JLabel("Properties..."), BorderLayout.NORTH);

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new JLabel("Component's Properties..."));

		propertiesAction = new JComboBox<String>(new String[]{ACTION_LEAVE, ACTION_SET_MATCHING // ,
				// ACTION_SET_SELECTED
		});
		propertiesAction.setEditable(false);
		propertiesAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String item = (String) propertiesAction.getSelectedItem();
				System.out.println("Properties action: " + item);
				handlePropertiesAction(item);
			}
		});
		p.add(new JLabel("Select action"));
		p.add(propertiesAction);

		propertiesGo = new JButton(new AbstractAction("OK")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("GO!");
				handlePropertiesDone();
			}
		});
		// p.add(propertiesGo);
		propertiesPanel.add(propertiesGo, BorderLayout.SOUTH);

		propertiesProgress = new JProgressBar(0, 1000);
		p.add(new JLabel("Progress... (when OK'd)"));
		p.add(propertiesProgress);
		p.add(new JLabel("Property comparison"));

		propertiesTableModel = new MyDefaultTableModel(new String[]{"Attribute", "Before", "Now"}, 0);
		propertiesTable = new JTable(propertiesTableModel);
		tableScroll = new JScrollPane(propertiesTable);

		propertiesPanel.add(tableScroll, BorderLayout.CENTER);

		propertiesPanel.add(p, BorderLayout.NORTH);

		switchedPanel.add("Properties", propertiesPanel);

		// links
		linksPanel = new JPanel();
		linksPanel.setLayout(new BorderLayout());

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(new JLabel("Re-create Link..."));

		linksAction = new JComboBox<String>(new String[]{ACTION_OMIT, ACTION_CREATE_EXACT, ACTION_CREATE_ALTERNATIVE});
		linksAction.setEditable(false);
		linksAction.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String item = (String) linksAction.getSelectedItem();
				System.out.println("Links action: " + item);
				handleLinksAction(item);
			}
		});
		p.add(new JLabel("Select action"));
		p.add(linksAction);

		linksGo = new JButton(new AbstractAction("OK")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("GO!");
				handleLinksDone();
			}
		});
		// p.add(linksGo);
		linksPanel.add(linksGo, BorderLayout.SOUTH);

		linksPanel.add(p, BorderLayout.NORTH);

		p.add(new JLabel("Link properties"));

		linksTableModel = new MyDefaultTableModel(new String[]{"Attribute", "From", "To"}, 0);
		linksTable = new JTable(linksTableModel);
		tableScroll = new JScrollPane(linksTable);

		linksPanel.add(tableScroll, BorderLayout.CENTER);

		switchedPanel.add("Links", linksPanel);

		final JPanel logPanel = new JPanel();
		logText = new JTextArea(80, 40);
		logText.setEditable(false);
		logPanel.setLayout(new BorderLayout());
		logPanel.add(new JLabel("Log output"), BorderLayout.NORTH);
		tableScroll = new JScrollPane(logText);
		logPanel.add(tableScroll, BorderLayout.CENTER);
		doneButton = new JButton(new AbstractAction("Done")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Done");
				frame.setVisible(false);
			}
		});
		logPanel.add(doneButton, BorderLayout.SOUTH);
		doneButton.setEnabled(false);

		switchedPanel.add(logPanel, "Log");

		// wrap it up
		frame.getContentPane().add(switchedPanel, BorderLayout.CENTER);

		frame.pack();
		// frame.setSize(500,600);
	}

	protected void createRestoreGui()
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
	 * load frame, create panel, handle create of capability. NB not swing thread
	 */
	protected void doHandleCreateComponent(final GUID capabilityid)
	{
		created = null;
		createCancelled = false;
		DataSession session = null;
		GUID requestid = null;
		try
		{
			final GUID[] found = new GUID[1];
			found[0] = null;

			final ItemData capitem = dataspace.getItem(capabilityid);
			final Capability capability = new Capability((TupleImpl) capitem);

			requestid = dataspace.allocateId();
			final ComponentRequest request = new ComponentRequest(requestid);
			request.setRequestID(requestid.toString());
			request.setHostID(DataspaceUtils.stringToGUID(capability.getHostID()));// !!hack yuck - null?!
			request.setContainerID(capability.getContainerID());
			request.setCapabilityID(capability.getID());

			dataspace.addPersistent(request.tuple, null);
			System.out.println("Added request");

			final ComponentAdvert template = new ComponentAdvert((GUID) null);
			template.setComponentRequestID(requestid);

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

			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					status.setText("Create dynamic component...");
					switchedPanel.setEnabledAt(0, false);
					switchedPanel.setEnabledAt(1, true);
					switchedPanel.setEnabledAt(2, false);
					switchedPanel.setEnabledAt(3, false);
					switchedPanel.setSelectedIndex(1);
					createProgress.setValue(0);
				}
			});
			final int MAX = 300;
			for (int count = 0; found[0] == null && !createCancelled && count < MAX; count++)
			{
				Thread.sleep(100);
				final int fcount = count;
				invokeAndWait(new Runnable()
				{
					@Override
					public void run()
					{
						createProgress.setValue(fcount * 100 / MAX);
					}
				});
			}

			dataspace.removeDataspaceEventListener(session);
			session = null;
			if (found[0] == null)
			{
				System.out.println("Failed - removing request");
				dataspace.delete(requestid);
				requestid = null;
				log("FAILED to create new component from capability " + capabilityid + "\n");
			}
			else
			{
				System.out.println("Succeeded: " + found[0]);
				created = found[0];
				log("Create new component " + created + " from capability " + capabilityid + "\n");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing create component: " + e);
			e.printStackTrace(System.err);
			// tidy up?
			try
			{
				if (requestid != null)
				{
					dataspace.delete(requestid);
				}
			}
			catch (final Exception e2)
			{
				System.err.println("ERROR tidying up request: " + e2);
			}
			try
			{
				if (session != null)
				{
					dataspace.removeDataspaceEventListener(session);
				}
			}
			catch (final Exception e2)
			{
				System.err.println("ERROR tidying up listener: " + e2);
			}
		}
	}

	/**
	 * do load frame, handle link - non-swing
	 */
	protected void doHandleLink(final Element l) throws GiveUpException
	{
		try
		{
			linkel = l;
			linksDone[0] = false;
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						// initialise for next link
						status.setText("Dealing with a link...");
						switchedPanel.setEnabledAt(0, false);
						switchedPanel.setEnabledAt(1, false);
						switchedPanel.setEnabledAt(2, false);
						switchedPanel.setEnabledAt(3, true);
						switchedPanel.setSelectedIndex(3);

						String defaultAction = ACTION_CREATE_EXACT;
						// does the same thing exist??
						final String srccompref = linkel.getAttribute(ATsrccompref);
						// final String srcpropname = linkel.getAttribute(ATsrcpropname);
						final String dstcompref = linkel.getAttribute(ATdstcompref);
						// final String dstpropname = linkel.getAttribute(ATdstpropname);

						final GUID srccompid = componentMapping.get(srccompref);
						final GUID dstcompid = componentMapping.get(dstcompref);

						if (srccompid == null)
						{
							System.out.println("Source component has no current mapping");
							defaultAction = ACTION_IGNORE;
						}
						else if (dstcompid == null)
						{
							System.out.println("Destination component has no current mapping");
							defaultAction = ACTION_IGNORE;
						}

						linksAction.getModel().setSelectedItem(defaultAction);
						handleLinksAction(defaultAction);
					}
					catch (final Exception e)
					{
						System.err.println("ERROR dealing with component: " + e);
						e.printStackTrace(System.err);
					}
				}
			});
			synchronized (linksDone)
			{
				while (!automatic && !linksDone[0] && !giveup)
				{
					linksDone.wait();
				}
				if (giveup)
				{
					throw new GiveUpException();
				}
			}
			if (srcprop != null && dstprop != null)
			{
				final GUID myLinkID = dataspace.allocateId();
				final PropertyLinkRequest link = new PropertyLinkRequest(myLinkID);
				link.setSourcePropertyName(srcprop.getPropertyName());
				link.setSourcePropID(srcprop.getID());
				link.setSourceComponentID(srcprop.getComponentID());
				link.setDestinationPropertyName(dstprop.getPropertyName());
				link.setDestinationPropID(dstprop.getID());
				link.setDestComponentID(dstprop.getComponentID());

				dataspace.addPersistent(link.tuple, null);
				System.out.println("Made link");
				log("Recreated link " + l.getAttribute(ATsrcpropname) + " -> " + l.getAttribute(ATdstpropname) + " as "
						+ srcprop.getPropertyName() + " -> " + dstprop.getPropertyName() + "\n");
			}
			else
			{
				log("Did not recreate link " + l.getAttribute(ATsrcpropname) + " -> " + l.getAttribute(ATdstpropname)
						+ "\n");
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR handling links: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, properties panel, handle setting of properties. NB not swing thread
	 */
	protected void doHandleProperties(final GUID componentid) throws GiveUpException
	{
		currentComponentId = componentid;

		try
		{
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						status.setText("Set properties...");
						switchedPanel.setEnabledAt(0, false);
						switchedPanel.setEnabledAt(1, false);
						switchedPanel.setEnabledAt(2, true);
						switchedPanel.setEnabledAt(3, false);
						switchedPanel.setSelectedIndex(2);

						propertiesTableModel.setRowCount(0);
						populateTableModel2(propertiesTableModel, component, capabilities, currentComponentId, false);

						propertiesAction.getModel().setSelectedItem(ACTION_SET_MATCHING);
						handlePropertiesAction(ACTION_SET_MATCHING);
					}
					catch (final DataspaceInactiveException e)
					{
						System.err.println("ERROR: " + e);
						e.printStackTrace(System.err);
					}
				}
			});
			synchronized (this)
			{
				if (!automatic && !giveup)
				{
					wait();
				}
				if (giveup)
				{
					throw new GiveUpException();
				}
			}

			System.out.println("Properties action: " + propertiesActionChosen);
			if (propertiesActionChosen.equals(ACTION_LEAVE))
			{
				// no op
			}
			else if (propertiesActionChosen.equals(ACTION_SET_MATCHING))
			{
				// GUID -> value (String)
				final Map<GUID, String> targetValues = new HashMap<GUID, String>();

				// matching are...
				final Element[] properties = getNamedChildElements(getNamedChildElement(component, ELproperties),
						ELproperty);
				final ComponentProperty propTemplate = new ComponentProperty((GUID) null);
				propTemplate.setComponentID(currentComponentId);

				ComponentProperty props[] = propTemplate.copyCollectAsComponentProperty(dataspace);

				for (int i = 0; properties != null && i < properties.length; i++)
				{
					final String propname = getElementText(getNamedChildElement(properties[i], ELname));
					for (int j = 0; props != null && j < props.length; j++)
					{
						if (props[j].getPropertyName().equals(propname))
						{
							if (props[j].isReadonly())
							{
								System.out.println("Ignore readonly property " + props[j].getPropertyName());
							}
							else
							{
								final String val = getElementText(getNamedChildElement(properties[i], ELvalue));
								if (!val.startsWith("localinterface:"))
								{
									// not local interfaces
									targetValues.put(props[j].getID(), val);
								}
							}
						}
					}
				}

				doSetProperties(targetValues);

				// did any more get created e.g. dynamic bean shell!?
				Thread.sleep(1000);
				final Map<GUID, String> newTargetValues = new HashMap<GUID, String>();

				props = propTemplate.copyCollectAsComponentProperty(dataspace);

				for (int i = 0; properties != null && i < properties.length; i++)
				{
					final String propname = getElementText(getNamedChildElement(properties[i], ELname));
					for (int j = 0; props != null && j < props.length; j++)
					{
						if (props[j].getPropertyName().equals(propname) && !targetValues.containsKey(props[j].getID()))
						{
							if (props[j].isReadonly())
							{
								System.out.println("Ignore readonly property " + props[j].getPropertyName());
							}
							else
							{
								final String val = getElementText(getNamedChildElement(properties[i], ELvalue));
								if (!val.startsWith("localinterface:"))
								{
									// not local interfaces
									newTargetValues.put(props[j].getID(), val);
								}
							}
						}
					}
				}

				doSetProperties(newTargetValues);
			}
			else if (propertiesActionChosen.equals(ACTION_SET_SELECTED))
			{
				// ....
			}
			else
			{
			}

		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing handle properties: " + e);
			e.printStackTrace(System.err);
		}
		// ....
		// .....
	}

	/**
	 * load frame, properties panel, set properties - not swing thread
	 */
	protected void doSetProperties(final Map<GUID, String> targetValues)
	{
		try
		{
			// do config..., configured, other
			for (int stage = 0; stage < 3; stage++)
			{
				for (final GUID pid : targetValues.keySet())
				{
					final String value = targetValues.get(pid);

					doSetProperty(pid, value, stage);
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR setting properties: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, properties panel, set property - not swing thread
	 */
	protected void doSetProperty(final GUID pid, final String newvalue, final int stage)
	{
		try
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

			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						status.setText("Set property " + targetProperty.getPropertyName() + "...");
						// refresh in case changed
						propertiesTableModel.setRowCount(0);
						populateTableModel2(propertiesTableModel, component, capabilities, currentComponentId, false);
						propertiesProgress.setValue(0);
					}
					catch (final DataspaceInactiveException e)
					{
						System.err.println("Dataspace inactive!");
					}
				}
			});

			System.out.println("Set property " + pid + " to " + newvalue + "...");

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
						 * notify of event
						 * (cf
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
										value[0] = (String) Coerce.toClass(p.getPropertyValue(),
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
										value[0] = (String) Coerce.toClass(p.getPropertyValue(),
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
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							propertiesProgress.setValue(prog);
						}
					});

					value.wait(1000);
				}
			}
			if (!done)
			{
				System.err.println("FAILED to set property: " + (value[0] != null ? value[0] + " instead" : ""));
			}
			else
			{
				log("set property " + targetProperty.getPropertyName() + " to " + newvalue + "\n");
			}

			dataspace.delete(myLinkID);
			dataspace.delete(myPropertyID);
			dataspace.removeDataspaceEventListener(session);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR setting property " + pid + ": " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, properties panel, set property - swing thread, no gui
	 */
	protected void doSetProperty2(final GUID pid, final String newvalue, final int stage) throws Exception
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
	protected String getElementText(final Element el)
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
	protected Element getNamedChildElement(final Element el, final String name)
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
	 * get element with given 'id'
	 */
	protected Element getNamedChildElementById(final Element el, final String name, final String id)
	{
		if (el == null)
		{
			return null;
		}
		final NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE && children.item(i).getNodeName().equals(name)
					&& ((Element) (children.item(i))).hasAttribute(ATid)
					&& ((Element) (children.item(i))).getAttribute(ATid).equals(id))
			{
				return (Element) (children
						.item(i));
			}
		}
		return null;
	}

	/**
	 * get array of so-named child elements
	 */
	protected Element[] getNamedChildElements(final Element el, final String name)
	{
		if (el == null)
		{
			return null;
		}
		final List<Element> res = new ArrayList<Element>();
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

	/**
	 * load frame, create panel, handle cancel - GUI callback
	 */
	protected void handleCancelCreate()
	{
		System.out.println("Cancel create");
		createCancelled = true;
	}

	/**
	 * load frame, component choice panel, select one candidate - GUI callback. by GUID
	 */
	protected void handleCandidate(final GUID id)
	{
		try
		{
			componentTableModel.setRowCount(0);
			if (id != null && populateTableModel2(componentTableModel, component, capabilities, id, capabilityOnly))
			{
				candidate = id;
				componentGo.setEnabled(true);
			}
			else
			{
				candidate = null;
				componentGo.setEnabled(false);
				componentTableModel.setRowCount(0);
				populateTableModel1(componentTableModel, component, capabilities);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR handle candidate " + id + ": " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, component choice panel, select one candidate - GUI callback. by name (from
	 * candidates Hashtable)
	 */
	protected void handleCandidate(final String name)
	{
		final GUID id = name != null ? (GUID) candidates.get(name) : null;
		handleCandidate(id);
	}

	/**
	 * load frame, component choice panel, reconfigure the interace for a particular action
	 */
	protected void handleComponentAction(final String a)
	{
		switchedPanel.setEnabledAt(0, true);
		switchedPanel.setEnabledAt(1, false);
		switchedPanel.setEnabledAt(2, false);
		switchedPanel.setEnabledAt(3, false);
		switchedPanel.setSelectedIndex(0);

		try
		{
			action = a;
			candidatesComboModel.removeAllElements();
			if (action.equals(ACTION_IGNORE))
			{
				componentGo.setEnabled(true);
				componentTableModel.setRowCount(0);
				populateTableModel1(componentTableModel, component, capabilities);
				return;
			}
			else if (action.equals(ACTION_USE_ORIGINAL))
			{
				final GUID id = DataspaceUtils.stringToGUID(component.getAttribute(ATid));
				handleCandidate(id);
			}
			else if (action.equals(ACTION_USE_EXISTING_SAME_CLASS))

			{
				final String compname = getElementText(getNamedChildElement(component, ELname));
				final String capabilityref = component.hasAttribute(ATcapabilityref) ? component
						.getAttribute(ATcapabilityref) : null;
				String capabilityname = null;
				String capabilityclass = null;
				String host = getElementText(getNamedChildElement(component, ELhost));
				if (capabilityref != null)
				{
					final Element capability = getNamedChildElementById(capabilities, ELcapability, capabilityref);
					if (capability != null)
					{
						capabilityname = getElementText(getNamedChildElement(capability, ELname));
						capabilityclass = getElementText(getNamedChildElement(capability, ELclass));
						host = getElementText(getNamedChildElement(capability, ELhost));
					}
				}
				// find components of same class
				final Map<String, GUID> candidates = new HashMap<String, GUID>();
				final ItemData[] compitems = dataspace.copyCollect(compAdTemplate.tuple);
				String first = null;
				// corresponding child (persistentChild) of corresponding parent?!
				final Element props[] = getNamedChildElements(getNamedChildElement(component, ELproperties), ELproperty);
				String persistentChild = null, parent = null;
				for (int pi = 0; props != null && pi < props.length; pi++)
				{
					final String propname = getElementText(getNamedChildElement(props[pi], ELname));
					if (propname != null && propname.equals("parent"))
					{
						parent = getElementText(getNamedChildElement(props[pi], ELvalue));
					}
					if (propname != null && propname.equals("persistentChild"))
					{
						persistentChild = getElementText(getNamedChildElement(props[pi], ELvalue));
					}
				}
				final GUID parentid = parent != null ? componentMapping.get(parent) : null;
				System.out.println("Component parent=" + parent + " (now " + parentid + "), child=" + persistentChild);

				// ComponentAdvert [] comps =
				// compAdTemplate.copyCollectAsComponentAdvert(dataspace);
				// for (int i=0; comps!=null && i<comps.length; i++)
				int firstRank = 0;
				for (int i = 0; compitems != null && i < compitems.length; i++)
				{
					final ComponentAdvert comp = new ComponentAdvert((TupleImpl) compitems[i]);
					final String compname2 = comp.getComponentName();
					String capabilityname2 = null;
					String capabilityclass2 = null;
					String host2 = comp.getHostID();
					final GUID capid = comp.getCapabilityID();

					if (capid != null)
					{
						final ItemData capitem = dataspace.getItem(capid);
						if (capitem instanceof TupleImpl)
						{
							final Capability cap = new Capability((TupleImpl) capitem);
							capabilityname2 = cap.getCapabilityName();
							capabilityclass2 = cap.getCapabilityClass();
							if (cap.getHostID() != null)
							{
								host2 = cap.getHostID();
							}
						}
					}
					if (compname.equals(compname2)
							|| (capabilityname != null && capabilityname2 != null && capabilityname
							.equals(capabilityname2))
							|| (capabilityclass != null && capabilityclass2 != null && capabilityclass
							.equals(capabilityclass2)))
					{
						final String key = compname2
								+ (capabilityname2 != null && !compname2.equals(capabilityname2) ? " ("
								+ capabilityname2 + ")" : "") + (host2 != null ? " on " + host2 : "")
								+ (capabilityclass2 != null ? " (" + capabilityclass2 + ")" : "") + " " + comp.getID();
						candidates.put(key, comp.getID());
						if (host != null && host2 != null && host.equals(host2) && firstRank < 1)
						{
							first = key;
							firstRank = 1;
						}
						if (parentid != null)
						{
							// child of current parent?!
							ComponentProperty srcproptemplate = new ComponentProperty((GUID) null);
							srcproptemplate.setComponentID(comp.getComponentID());
							srcproptemplate.setPropertyName("persistentChild");

							ItemData srcpropitems[] = dataspace.copyCollect(srcproptemplate.tuple);
							if (srcpropitems != null && srcpropitems.length > 0)
							{
								ComponentProperty p = new ComponentProperty((TupleImpl) srcpropitems[0]);
								String value = (String) Coerce.toClass(p.getPropertyValue(), String.class);
								if (value != null && value.equals(persistentChild) && firstRank < 2)
								{
									System.out.println("Same persistentChild value: " + persistentChild);
									first = key;
									firstRank = 2;
								}
								// mapped parent?
								srcproptemplate = new ComponentProperty((GUID) null);
								srcproptemplate.setComponentID(comp.getComponentID());
								srcproptemplate.setPropertyName("parent");

								srcpropitems = dataspace.copyCollect(srcproptemplate.tuple);
								if (srcpropitems != null && srcpropitems.length > 0)
								{
									p = new ComponentProperty((TupleImpl) srcpropitems[0]);
									value = (String) Coerce.toClass(p.getPropertyValue(), String.class);
									if (value != null && value.equals(parentid.toString()) && firstRank < 3)
									{
										System.out.println("Same parent id value: " + parentid.toString());
										first = key;
										firstRank = 3;
									}
								}
							}
						}
					}
				}
				capabilityOnly = false;
				handleComponentCandidates(candidates, first);
			}
			else if (action.equals(ACTION_CREATE_NEW_DYNAMIC_SAME_CLASS))
			{
				final String compname = getElementText(getNamedChildElement(component, ELname));
				final String capabilityref = component.hasAttribute(ATcapabilityref) ? component
						.getAttribute(ATcapabilityref) : null;
				String capabilityname = null;
				String capabilityclass = null;
				String host = getElementText(getNamedChildElement(component, ELhost));
				if (capabilityref != null)
				{
					final Element capability = getNamedChildElementById(capabilities, ELcapability, capabilityref);
					if (capability != null)
					{
						capabilityname = getElementText(getNamedChildElement(capability, ELname));
						capabilityclass = getElementText(getNamedChildElement(capability, ELclass));
						host = getElementText(getNamedChildElement(capability, ELhost));
					}
				}
				// find components of same class
				final Map<String, GUID> candidates = new HashMap<String, GUID>();
				final ItemData[] capitems = dataspace.copyCollect(capTemplate.tuple);
				String first = null;
				// ComponentAdvert [] comps =
				// compAdTemplate.copyCollectAsComponentAdvert(dataspace);
				// for (int i=0; comps!=null && i<comps.length; i++)
				for (int i = 0; capitems != null && i < capitems.length; i++)
				{
					final Capability cap = new Capability((TupleImpl) capitems[i]);
					final String capabilityname2 = cap.getCapabilityName();
					final String capabilityclass2 = cap.getCapabilityClass();
					final String host2 = cap.getHostID();

					if (compname.equals(capabilityname2)
							|| (capabilityname != null && capabilityname2 != null && capabilityname
							.equals(capabilityname2))
							|| (capabilityclass != null && capabilityclass2 != null && capabilityclass
							.equals(capabilityclass2)))
					{
						final String key = (capabilityname2 != null ? capabilityname2 : "unnamed")
								+ (host2 != null ? " on " + host2 : "")
								+ (capabilityclass2 != null ? " (" + capabilityclass2 + ")" : "") + " " + cap.getID();
						candidates.put(key, cap.getID());
						if (host != null && host2 != null && host.equals(host2))
						{
							first = key;
						}
					}
				}
				capabilityOnly = true;
				handleComponentCandidates(candidates, first);
			}
			else
			{
				System.out.println("WARNING: unknown action: " + action);
				componentGo.setEnabled(false);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: handling action " + a + ": " + e);
			e.printStackTrace(System.err);
		}

	}

	/**
	 * load frame, component choice panel, reconfigure the interface for a set of candidate existing
	 * components.capabilities
	 */
	protected void handleComponentCandidates(final Map<String, GUID> c, String first)
	{
		candidates = c;
		candidatesComboModel.removeAllElements();
		// String first = null;
		if (first != null)
		{
			candidatesComboModel.addElement(first);
		}
		for (final String key : candidates.keySet())
		{
			if (first != null && first.equals(key))
			{
				// done already
			}
			else
			{
				candidatesComboModel.addElement(key);
				if (first == null)
				{
					first = key;
				}
			}
		}
		if (first == null)
		{
			candidatesComboModel.addElement("(none)");
			componentGo.setEnabled(false);
		}
		else
		{
			handleCandidate(first);
		}
	}

	/**
	 * handle links action - gui callback
	 */
	protected void handleLinksAction(final String a)
	{
		try
		{
			linksTableModel.setRowCount(0);
			if (a.equals(ACTION_OMIT))
			{
				srcprop = dstprop = null;
				linksGo.setEnabled(true);
				populateLinkTableModel(linksTableModel, linkel);
			}
			else if (a.equals(ACTION_CREATE_EXACT))
			{
				linksGo.setEnabled(false);
				// does the same thing exist??
				final String srccompref = linkel.getAttribute(ATsrccompref);
				final String srcpropname = linkel.getAttribute(ATsrcpropname);
				final String dstcompref = linkel.getAttribute(ATdstcompref);
				final String dstpropname = linkel.getAttribute(ATdstpropname);

				final GUID srccompid = componentMapping.get(srccompref);
				final GUID dstcompid = componentMapping.get(dstcompref);

				if (srccompid == null)
				{
					System.out.println("Source component has no current mapping");
					return;
				}
				if (dstcompid == null)
				{
					System.out.println("Destination component has no current mapping");
					return;
				}
				final ComponentProperty srcproptemplate = new ComponentProperty((GUID) null);
				srcproptemplate.setComponentID(srccompid);
				srcproptemplate.setPropertyName(srcpropname);

				final ItemData srcpropitems[] = dataspace.copyCollect(srcproptemplate.tuple);
				if (srcpropitems == null || srcpropitems.length == 0)
				{
					System.err.println("Source component property " + srcpropname + " does not exist");
					return;
				}

				final ComponentProperty dstproptemplate = new ComponentProperty((GUID) null);
				dstproptemplate.setComponentID(dstcompid);
				dstproptemplate.setPropertyName(dstpropname);

				final ItemData dstpropitems[] = dataspace.copyCollect(dstproptemplate.tuple);
				if (dstpropitems == null || dstpropitems.length == 0)
				{
					System.err.println("Destination component property " + dstpropname + " does not exist");
					return;
				}
				srcprop = new ComponentProperty((TupleImpl) srcpropitems[0]);
				dstprop = new ComponentProperty((TupleImpl) dstpropitems[0]);

				System.out.println("Corresponding link exists");
				linksGo.setEnabled(true);

				populateLinkTableModel(linksTableModel, srcprop, dstprop);
			}
			else if (a.equals(ACTION_CREATE_ALTERNATIVE))
			{
				linksGo.setEnabled(false);
				// ....
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR handling links action " + a + ": " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * handle links done - gui callback
	 */
	protected void handleLinksDone()
	{
		synchronized (linksDone)
		{
			linksDone[0] = true;
			linksDone.notify();
		}
	}

	/**
	 * load frame, properties panel, handle choice of action - GUI callback
	 */
	protected void handlePropertiesAction(final String a)
	{
		propertiesActionChosen = a;
	}

	/**
	 * load frame, properties panel, handle choice of action - GUI callback
	 */
	protected void handlePropertiesDone()
	{
		try
		{
			synchronized (this)
			{
				notify();
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR handling properties done: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * log
	 */
	protected void log(final String msg)
	{
		final Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					System.out.print("LOG: " + msg);
					logText.getDocument().insertString(logText.getDocument().getLength(), msg, null);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR logging " + msg + ": " + e);
					e.printStackTrace(System.err);
				}
			}
		};
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		}
		else
		{
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * map mapped component properties
	 */
	protected void mapComponentProperties(final Element component, final GUID newcomponentid)
	{
		try
		{
			// matching are...
			final Element[] properties = getNamedChildElements(getNamedChildElement(component, ELproperties),
					ELproperty);
			final ComponentProperty propTemplate = new ComponentProperty((GUID) null);
			propTemplate.setComponentID(currentComponentId);

			ComponentProperty props[] = propTemplate.copyCollectAsComponentProperty(dataspace);

			for (int i = 0; properties != null && i < properties.length; i++)
			{
				final String propname = getElementText(getNamedChildElement(properties[i], ELname));
				for (ComponentProperty prop : props)
				{
					if (prop.getPropertyName().equals(propname))
					{
						final String propid = properties[i].getAttribute(ATid);
						propertyMapping.put(propid, prop.getID());
					}
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR mapping component property values: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, links panel, populate table from current properties/components
	 */
	protected void populateLinkTableModel(final DefaultTableModel model, final ComponentProperty srcprop,
	                                      final ComponentProperty dstprop)
	{
		try
		{
			model.setRowCount(0);

			final ItemData srccompitem = dataspace.getItem(srcprop.getComponentID());
			final ComponentAdvert srccomp = new ComponentAdvert((TupleImpl) srccompitem);
			final ItemData dstcompitem = dataspace.getItem(dstprop.getComponentID());
			final ComponentAdvert dstcomp = new ComponentAdvert((TupleImpl) dstcompitem);

			String name = srccomp.getComponentName();
			String name2 = dstcomp.getComponentName();
			model.addRow(new String[]{"Component name", name, name2});

			StringBuffer otherNames = new StringBuffer();
			RDFStatement rdfTemplate = new RDFStatement(null, RDFStatement.GUIDToUrl(srccomp.getID()), RDFStatement.DC_TITLE, null);
			RDFStatement rdfstatements[];
			rdfstatements = rdfTemplate.copyCollectAsRDFStatement(dataspace);
			for (int i = 0; rdfstatements != null && i < rdfstatements.length; i++)
			{
				if (i > 0)
				{
					otherNames.append(",");
				}
				otherNames.append(rdfstatements[i].getObject());
			}
			StringBuffer otherNames2 = new StringBuffer();
			rdfTemplate = new RDFStatement(null, RDFStatement.GUIDToUrl(dstcomp.getID()), RDFStatement.DC_TITLE, null);
			rdfstatements = rdfTemplate.copyCollectAsRDFStatement(dataspace);
			for (int i = 0; rdfstatements != null && i < rdfstatements.length; i++)
			{
				if (i > 0)
				{
					otherNames2.append(",");
				}
				otherNames2.append(rdfstatements[i].getObject());
			}
			model.addRow(new String[]{"Other names", otherNames.toString(), otherNames2.toString()});
			model.addRow(new String[]{"Container (ID)", srccomp.getContainerID().toString(),
					dstcomp.getContainerID().toString(),});
			model.addRow(new String[]{"Host (ID)", srccomp.getHostID(), dstcomp.getHostID()});
			final GUID capabilityid = srccomp.getCapabilityID();
			final GUID capabilityid2 = dstcomp.getCapabilityID();
			final GUID componentrequestid = srccomp.getComponentRequestID();
			final GUID componentrequestid2 = dstcomp.getComponentRequestID();
			model.addRow(new String[]{"Explicitly requested", componentrequestid != null ? "Yes" : "No",
					componentrequestid2 != null ? "Yes" : "No"});

			Capability cap = null;
			if (capabilityid != null)
			{
				final ItemData capitem = dataspace.getItem(capabilityid);
				if (capitem != null && capitem instanceof TupleImpl)
				{
					cap = new Capability((TupleImpl) capitem);
				}
			}
			Capability cap2 = null;
			if (capabilityid2 != null)
			{
				final ItemData capitem = dataspace.getItem(capabilityid2);
				if (capitem != null && capitem instanceof TupleImpl)
				{
					cap2 = new Capability((TupleImpl) capitem);
				}
			}

			model.addRow(new String[]{"Capability ID", cap != null ? cap.getID().toString() : null,
					cap2 != null ? cap2.getID().toString() : null});
			name = cap != null ? cap.getCapabilityName() : null;
			name2 = cap2 != null ? cap2.getCapabilityName() : null;
			model.addRow(new String[]{"Capability name", name, name2});
			otherNames = new StringBuffer();
			if (capabilityid != null)
			{
				rdfTemplate = new RDFStatement(null, RDFStatement.GUIDToUrl(capabilityid), RDFStatement.DC_TITLE, null);
				rdfstatements = rdfTemplate.copyCollectAsRDFStatement(dataspace);
				for (int i = 0; rdfstatements != null && i < rdfstatements.length; i++)
				{
					if (i > 0)
					{
						otherNames.append(",");
					}
					otherNames.append(rdfstatements[i].getObject());
				}
			}
			otherNames2 = new StringBuffer();
			if (capabilityid2 != null)
			{
				rdfTemplate = new RDFStatement(null, RDFStatement.GUIDToUrl(capabilityid2), RDFStatement.DC_TITLE, null);
				rdfstatements = rdfTemplate.copyCollectAsRDFStatement(dataspace);
				for (int i = 0; rdfstatements != null && i < rdfstatements.length; i++)
				{
					if (i > 0)
					{
						otherNames2.append(",");
					}
					otherNames2.append(rdfstatements[i].getObject());
				}
			}
			model.addRow(new String[]{"Other capability names", otherNames.toString(), otherNames2.toString()});
			model.addRow(new String[]{"Capability container (ID)",
					cap != null ? cap.getContainerID().toString() : null,
					cap2 != null ? cap2.getContainerID().toString() : null});
			model.addRow(new String[]{"Capability host", cap != null ? cap.getHostID() : null,
					cap2 != null ? cap2.getHostID() : null,});
			model.addRow(new String[]{"Property name", srcprop.getPropertyName(), dstprop.getPropertyName()});
			String otherValue = null;
			String otherValue2 = null;
			try
			{
				otherValue = (String) Coerce.toClass(srcprop.getPropertyValue(), String.class);
				otherValue2 = (String) Coerce.toClass(dstprop.getPropertyValue(), String.class);
			}
			catch (final ClassNotFoundException e)
			{
				// Do nothing?
			}
			catch (final IOException e)
			{
				// Do nothing?
			}

			model.addRow(new String[]{"Property value", otherValue, otherValue2});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR populating linktable: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, links panel, populate table from current properties/components
	 */
	protected void populateLinkTableModel(final DefaultTableModel model, final Element linkel)
	{
		try
		{
			model.setRowCount(0);

			final String srccompref = linkel.getAttribute(ATsrccompref);
			final String dstcompref = linkel.getAttribute(ATdstcompref);
			// final String srcpropname = linkel.getAttribute(ATsrcpropname);
			// final String dstpropname = linkel.getAttribute(ATdstpropname);
			final String srcpropref = linkel.getAttribute(ATsrcpropref);
			final String dstpropref = linkel.getAttribute(ATdstpropref);

			final Element srccomp = getNamedChildElementById(components, ELcomponent, srccompref);
			final Element srcprops = getNamedChildElement(srccomp, ELproperties);
			final Element srcprop = getNamedChildElementById(srcprops, ELproperty, srcpropref);

			System.out.println("srccompref=" + srccompref + ", srccom=" + srccomp + ", srcpropref=" + srcpropref
					+ ", srcprop=" + srcprop);

			final Element dstcomp = getNamedChildElementById(components, ELcomponent, dstcompref);
			final Element dstprops = getNamedChildElement(dstcomp, ELproperties);
			final Element dstprop = getNamedChildElementById(dstprops, ELproperty, dstpropref);

			System.out.println("dstcompref=" + dstcompref + ", dstcom=" + dstcomp + ", dstpropref=" + dstpropref
					+ ", dstprop=" + dstprop);

			final String name = getElementText(getNamedChildElement(srccomp, ELname));
			final String name2 = getElementText(getNamedChildElement(dstcomp, ELname));
			model.addRow(new String[]{"Component name", name, name2});
			model.addRow(new String[]{"Container (ID)", getElementText(getNamedChildElement(srccomp, ELcontainer)),
					getElementText(getNamedChildElement(dstcomp, ELcontainer))});
			model.addRow(new String[]{"Host (ID)", getElementText(getNamedChildElement(srccomp, ELhost)),
					getElementText(getNamedChildElement(dstcomp, ELhost))});
			final String capabilityref = component.hasAttribute(ATcapabilityref) ? srccomp
					.getAttribute(ATcapabilityref) : null;
			final String componentrequestref = component.hasAttribute(ATcomponentrequestref) ? srccomp
					.getAttribute(ATcomponentrequestref) : null;
			final String capabilityref2 = component.hasAttribute(ATcapabilityref) ? dstcomp
					.getAttribute(ATcapabilityref) : null;
			final String componentrequestref2 = component.hasAttribute(ATcomponentrequestref) ? dstcomp
					.getAttribute(ATcomponentrequestref) : null;
			model.addRow(new String[]{"Explicitly requested", componentrequestref != null ? "Yes" : "No",
					componentrequestref2 != null ? "Yes" : "No"});
			final Element capability = capabilityref != null ? getNamedChildElementById(capabilities, ELcapability,
					capabilityref) : null;
			final Element capability2 = capabilityref2 != null ? getNamedChildElementById(capabilities, ELcapability,
					capabilityref2) : null;
			model.addRow(new String[]{"Capability ID", capability != null ? capability.getAttribute(ATid) : null,
					capability2 != null ? capability2.getAttribute(ATid) : null});
			model.addRow(new String[]{
					"Capability name",
					capability != null ? getElementText(getNamedChildElement(capability, ELname))
							: null,
					capability2 != null ? getElementText(getNamedChildElement(capability2, ELname))
							: null});
			model.addRow(new String[]{
					"Capability container (ID)",
					capability != null ? getElementText(getNamedChildElement(capability,
							ELcontainer))
							: null,
					capability2 != null ? getElementText(getNamedChildElement(capability2,
							ELcontainer))
							: null});
			model.addRow(new String[]{
					"Capability host",
					capability != null ? getElementText(getNamedChildElement(capability, ELhost))
							: null,
					capability2 != null ? getElementText(getNamedChildElement(capability2, ELhost))
							: null});

			model.addRow(new String[]{"Property name", getElementText(getNamedChildElement(srcprop, ELname)),
					getElementText(getNamedChildElement(dstprop, ELname))});
			model.addRow(new String[]{"Property value", getElementText(getNamedChildElement(srcprop, ELvalue)),
					getElementText(getNamedChildElement(dstprop, ELvalue))});

		}
		catch (final Exception e)
		{
			System.err.println("ERROR populating linktable: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * load frame, component choice panel, populate component table from xml only
	 */
	protected void populateTableModel1(final DefaultTableModel model, final Element component,
	                                   final Element capabilities)
	{
		final String name = getElementText(getNamedChildElement(component, ELname));
		model.addRow(new String[]{"Component name", name});
		model.addRow(new String[]{"Container (ID)", getElementText(getNamedChildElement(component, ELcontainer))});
		model.addRow(new String[]{"Host (ID)", getElementText(getNamedChildElement(component, ELhost))});
		final String capabilityref = component.hasAttribute(ATcapabilityref) ? component.getAttribute(ATcapabilityref)
				: null;
		final String componentrequestref = component.hasAttribute(ATcomponentrequestref) ? component
				.getAttribute(ATcomponentrequestref) : null;
		model.addRow(new String[]{"Explicitly requested", componentrequestref != null ? "Yes" : "No"});
		final Element capability = capabilityref != null ? getNamedChildElementById(capabilities, ELcapability,
				capabilityref) : null;
		model.addRow(new String[]{"Capability ID", capability != null ? capability.getAttribute(ATid) : null});
		model.addRow(new String[]{
				"Capability name",
				capability != null ? getElementText(getNamedChildElement(capability, ELname))
						: null});
		model.addRow(new String[]{
				"Capability container (ID)",
				capability != null ? getElementText(getNamedChildElement(capability, ELcontainer))
						: null});
		model.addRow(new String[]{
				"Capability host",
				capability != null ? getElementText(getNamedChildElement(capability, ELhost))
						: null});
		final Element[] properties = getNamedChildElements(getNamedChildElement(component, ELproperties), ELproperty);
		for (int i = 0; properties != null && i < properties.length; i++)
		{
			model.addRow(new String[]{
					"Property '" + getElementText(getNamedChildElement(properties[i], ELname))
							+ "'", getElementText(getNamedChildElement(properties[i], ELvalue))});
			// parents??
			// ....
		}
	}

	/**
	 * load frame, component choice panel, populate component table from xml plus a current
	 * component or capability (iff capabilityOnly true)
	 */
	protected boolean populateTableModel2(final DefaultTableModel model, final Element component,
	                                      final Element capabilities, final GUID id, final boolean capabilityOnly) throws DataspaceInactiveException
	{
		final ItemData compitem = dataspace.getItem(id);
		if (compitem == null)
		{
			System.err.println("Item " + id + " is not there (expected ComponentAdvert/Capability)");
			return false;
		}
		if (!(compitem instanceof TupleImpl))
		{
			System.err.println("Item " + id + " is not a ComponentAdvert as expected (" + compitem.getClass().getName()
					+ ")");
			return false;
		}
		final ComponentAdvert comp = !capabilityOnly ? new ComponentAdvert((TupleImpl) compitem) : null;
		String name = getElementText(getNamedChildElement(component, ELname));
		String name2 = comp != null ? comp.getComponentName() : null;
		model.addRow(new String[]{"Component name", name, name2});
		model.addRow(new String[]{"Container (ID)", getElementText(getNamedChildElement(component, ELcontainer)),
				comp != null ? comp.getContainerID().toString() : null});
		model.addRow(new String[]{"Host (ID)", getElementText(getNamedChildElement(component, ELhost)),
				comp != null ? comp.getHostID() : null});
		final String capabilityref = component.hasAttribute(ATcapabilityref) ? component.getAttribute(ATcapabilityref)
				: null;
		final GUID capabilityid = capabilityOnly ? id : comp.getCapabilityID();
		final String componentrequestref = component.hasAttribute(ATcomponentrequestref) ? component
				.getAttribute(ATcomponentrequestref) : null;
		final GUID componentrequestid = comp != null ? comp.getComponentRequestID() : null;
		model.addRow(new String[]{"Explicitly requested", componentrequestref != null ? "Yes" : "No",
				!capabilityOnly && componentrequestid != null ? "Yes" : "No"});
		final Element capability = capabilityref != null ? getNamedChildElementById(capabilities, ELcapability,
				capabilityref) : null;

		Capability cap = null;
		if (capabilityid != null)
		{
			final ItemData capitem = dataspace.getItem(capabilityid);
			if (capitem != null && capitem instanceof TupleImpl)
			{
				cap = new Capability((TupleImpl) capitem);
			}
		}

		model.addRow(new String[]{"Capability ID", capability != null ? capability.getAttribute(ATid) : null,
				cap != null ? cap.getID().toString() : null});
		name = getElementText(getNamedChildElement(capability, ELname));
		name2 = cap != null ? cap.getCapabilityName() : null;
		model.addRow(new String[]{"Capability name", name, name2});
		model.addRow(new String[]{
				"Capability container (ID)",
				capability != null ? getElementText(getNamedChildElement(capability, ELcontainer))
						: null, cap != null ? cap.getContainerID().toString() : null});
		model.addRow(new String[]{
				"Capability host",
				capability != null ? getElementText(getNamedChildElement(capability, ELhost))
						: null, cap != null ? cap.getHostID() : null});
		final Element[] properties = getNamedChildElements(getNamedChildElement(component, ELproperties), ELproperty);
		ComponentProperty props[] = null;
		if (!capabilityOnly && id != null)
		{
			final ComponentProperty propTemplate = new ComponentProperty((GUID) null);
			propTemplate.setComponentID(id);

			props = propTemplate.copyCollectAsComponentProperty(dataspace);
		}
		for (int i = 0; properties != null && i < properties.length; i++)
		{
			final String propname = getElementText(getNamedChildElement(properties[i], ELname));
			String otherValue = null;
			for (int j = 0; props != null && j < props.length; j++)
			{
				if (props[j].getPropertyName().equals(propname))
				{
					try
					{
						otherValue = (String) Coerce.toClass(props[j].getPropertyValue(), String.class);
					}
					catch (final ConnectionPointTypeException e)
					{
						System.err.println("WARNING: unhandled connection point type: " + e);
					}
					catch (final ClassNotFoundException e)
					{
						// Do nothing?
					}
					catch (final IOException e)
					{
						// Do nothing?
					}
				}
			}
			model.addRow(new String[]{"Property '" + propname + "'",
					getElementText(getNamedChildElement(properties[i], ELvalue)), otherValue});
		}
		// any remaining properties
		for (int j = 0; props != null && j < props.length; j++)
		{
			final String propname = props[j].getPropertyName();
			boolean done = false;
			for (int i = 0; !done && properties != null && i < properties.length; i++)
			{
				final String propname1 = getElementText(getNamedChildElement(properties[i], ELname));
				if (propname.equals(propname1))
				{
					done = true;
				}
			}
			if (!done)
			{
				String value = null;
				try
				{
					value = (String) Coerce.toClass(props[j].getPropertyValue(), String.class);
				}
				catch (final ConnectionPointTypeException e)
				{
					System.err.println("WARNING: unhandled connection point type: " + e);
				}
				catch (final ClassNotFoundException e)
				{
					// Do nothing?
				}
				catch (final IOException e)
				{
					// Do nothing?
				}

				model.addRow(new String[]{"Property '" + propname + "'", null, value});
			}
		}
		// parents??
		// ....
		return true;
	}

	protected void restoreLog(final String msg)
	{
		try
		{
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
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
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	protected void restoreSetProgress(final float v)
	{
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					restoreProgress.setValue((int) (1000 * v));
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	protected void restoreSetStatus(final String s)
	{
		try
		{
			invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					restoreStatus.setText(s);
				}
			});
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	private boolean idExists(final Map<GUID, GUID> mapping, final String id)
	{
		return mapping.containsKey(DataspaceUtils.stringToGUID(id));
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
