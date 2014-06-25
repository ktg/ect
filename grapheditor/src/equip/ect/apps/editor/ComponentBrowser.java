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
 Stefan Rennick Egglestone (University of Nottingham)

 */

package equip.ect.apps.editor;

import equip.data.DictionaryImpl;
import equip.data.GUID;
import equip.data.ItemData;
import equip.data.StringBox;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.CompInfo;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;
import equip.ect.apps.editor.dataspace.ComponentListener;
import equip.ect.apps.editor.dataspace.DataspaceConfigurationListener;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.dataspace.DataspaceUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ComponentBrowser extends JPanel
{
	private class DataspaceTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private final Icon component;
		private final Icon request;
		private final Icon link;
		/**
		 * dataspace
		 */
		protected DataspaceBean dataspace;
		private String pattern = null;

		/**
		 * cons
		 */
		public DataspaceTreeCellRenderer(final DataspaceBean dataspace)
		{
			super();
			this.dataspace = dataspace;
			final Icon folder = MediaFactory.getImageIcon(EditorResources.FOLDER_ICON, ComponentBrowser.this);
			setClosedIcon(folder);
			setOpenIcon(folder);
			request = MediaFactory.getImageIcon(EditorResources.COMPONENTREQUEST_ICON, ComponentBrowser.this);
			component = MediaFactory.getImageIcon(EditorResources.COMPONENT_ICON, ComponentBrowser.this);
			link = MediaFactory.getImageIcon(EditorResources.LINK_ICON, ComponentBrowser.this);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
		                                              final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getParent() == null)
			{
				setIcon(getClosedIcon());
			}
			// System.out.println("Tree cell tooltip ("+value+")");
			final Object userValue = (value instanceof DefaultMutableTreeNode) ? ((DefaultMutableTreeNode) value)
					.getUserObject() : null;
			String text = null;
			if (dataspace != null && userValue instanceof GUID)
			{
				String display = treeNodeToString(value);
				Icon icon = component;

				try
				{
					final ItemData item = dataspace.getItem((GUID) userValue);
					if (item instanceof TupleImpl)
					{
						final TupleImpl tuple = (TupleImpl) item;
						if (tuple.name.equals(Capability.TYPE))
						{
							Capability capability = new Capability(tuple);

						}
						else if (tuple.name.equals(ComponentRequest.TYPE))
						{
							ComponentRequest componentRequest = new ComponentRequest(tuple);
							display = componentRequest.getRequestID();
							icon = request;

						}
						else if (tuple.name.equals(ComponentAdvert.TYPE))
						{
							ComponentAdvert componentAdvert = new ComponentAdvert(tuple);
							display = DataspaceUtils.getCurrentName(componentAdvert);
							icon = component;
						}
						else if (tuple.name.equals(PropertyLinkRequest.TYPE))
						{
							icon = link;
						}

						if (tuple.fields[CompInfo.ATTRIBUTES_INDEX] instanceof DictionaryImpl)
						{
							final DictionaryImpl d = (DictionaryImpl) tuple.fields[CompInfo.ATTRIBUTES_INDEX];
							final Object val = d.get(BeanDescriptorHelper.SHORT_DESCRIPTION);
							if (val instanceof StringBox)
							{
								text = ((StringBox) val).value;
							}
						}
					}
				}
				catch (final DataspaceInactiveException e)
				{
					System.err.println("unable to get tooltip: " + e);
				}
				setText(display);
				setIcon(icon);
			}

			setToolTipText(text);

			if ((pattern != null) && (getText() != null))
			{
				if (getText().toLowerCase().matches(pattern))
				{
					setText("<html><font color=\"#F93232\">" + getText() + "</font></html>");
				}
			}

			return this;
		}

		/**
		 * @param pattern The pattern to set.
		 */
		public void setPattern(final String pattern)
		{
			this.pattern = pattern;
		}
	}

	private abstract class DataspaceTreeView extends JScrollPane implements SelectionModel.SelectionListener
	{
		protected final Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<String, DefaultMutableTreeNode>();
		protected final DefaultMutableTreeNode root;
		protected final DefaultTreeModel treeModel;
		protected final JTree tree;
		private short clickCounter = 0;
		private boolean selectionModelChange = false;

		public DataspaceTreeView(String name)
		{
			setName(name);
			root = new DefaultMutableTreeNode(name);
			treeModel = new DefaultTreeModel(root);
			tree = new JTree(treeModel);
			tree.setRootVisible(false);
			tree.setCellRenderer(new DataspaceTreeCellRenderer(dataspace));
			tree.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(final MouseEvent e)
				{
					handle(e);
				}

				@Override
				public void mouseReleased(final MouseEvent e)
				{
					handle(e);
				}
			});
			tree.addTreeSelectionListener(new TreeSelectionListener()
			{
				@Override
				public void valueChanged(TreeSelectionEvent e)
				{
					if(selectionModelChange)
					{
						return;
					}
					Collection<String> selected = new HashSet<String>();
					if(tree.getSelectionPaths() != null)
					{
						for (TreePath path : tree.getSelectionPaths())
						{
							if (path.getLastPathComponent() instanceof DefaultMutableTreeNode)
							{
								Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
								if (userObject instanceof GUID)
								{
									selected.add(userObject.toString());
								}
							}
						}
					}
					selectionModel.set(selected);
				}
			});
			ToolTipManager.sharedInstance().registerComponent(tree);

			setViewportView(tree);
		}

		private void handle(final MouseEvent e)
		{
			final TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
			if (selPath != null && (e.isPopupTrigger()))
			{
				final Object elems[] = selPath.getPath();
				if (elems.length > 0)
				{
					final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) elems[elems.length - 1];
					if (treeNode != null)
					{
						final GUID id = (GUID) treeNode.getUserObject();
						if (id != null)
						{
							// component popup
							final JPopupMenu popup = new JPopupMenu();
							tree.setSelectionPath(new TreePath(treeNode.getPath()));
							ItemData i = null;
							try
							{
								i = dataspace.getItem(id);
							}
							catch (final DataspaceInactiveException ex)
							{
								System.err.println("ERROR: dataspace inactive");
							}

							final ItemData item = i;
							if (item != null && ((TupleImpl) item).name.equals(Capability.TYPE))
							{

								popup.add(new AbstractAction("Create request")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										ComponentBrowser.this.requestComponent(item);
									}
								});
							}
							else if (item != null && ((TupleImpl) item).name.equals(ComponentRequest.TYPE))
							{
								popup.add(new AbstractAction("Delete request")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										deleteComponentRequest(id);
									}
								});
							}
							else if (item != null && ((TupleImpl) item).name.equals(ComponentAdvert.TYPE))
							{
								popup.add(new AbstractAction("Delete request")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										deleteComponent(id);
									}
								});
							}
							popup.show(tree, e.getX(), e.getY());
						}
					}
				}
			}
			else if (selPath != null && e.getModifiers() == InputEvent.BUTTON1_MASK && e.getClickCount() == 2)
			{
				/**
				 * on double clicks mouse pressed seems to be triggered twice, even with an click
				 * count of 2 on the first event. So just wait for the second event.
				 */
				clickCounter++;
				if (clickCounter == 2)
				{
					clickCounter = 0;

					final Object[] path = selPath.getPath();
					if (path.length > 0)
					{
						final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path[path.length - 1];
						if (treeNode != null)
						{
							final GUID id = (GUID) treeNode.getUserObject();
							if (id != null)
							{
								ItemData item;
								try
								{
									item = dataspace.getItem(id);
								}
								catch (final DataspaceInactiveException e1)
								{
									e1.printStackTrace();
									return;
								}
								if (item != null && ((TupleImpl) item).name.equals(Capability.TYPE))
								{
									ComponentBrowser.this.requestComponent(item);
								}
							}
						}
					}
				}
			}
		}

		public JTree getMainTree()
		{
			return tree;
		}

		@Override
		public void selectionChanged(Collection<String> selection)
		{
			List<TreePath> treePaths = new ArrayList<TreePath>();
			for (String item : selection)
			{
				DefaultMutableTreeNode node = nodeMap.get(item);
				if (node != null)
				{
					treePaths.add(new TreePath(node.getPath()));
				}
			}

			selectionModelChange = true;
			tree.setSelectionPaths(treePaths.toArray(new TreePath[treePaths.size()]));
			selectionModelChange = false;
		}
	}

	private class CapabilityView extends DataspaceTreeView implements DataspaceConfigurationListener
	{
		CapabilityView()
		{
			super("Capabilities");
			final Collection<Capability> currentCapabilities = DataspaceMonitor.getMonitor().getCapabilities();
			if (currentCapabilities != null)
			{
				for (Capability cap : currentCapabilities)
				{
					capabilityAdded(cap);
				}
			}

			DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);
		}

		@Override
		public void componentRequestDeleted(final ComponentRequest compReq)
		{
			final GUID id = compReq.getID();
			final DefaultMutableTreeNode node = nodeMap.get(id.toString());
			if (node == null)
			{
				System.err.println("Unable to delete (unknown component request) item" + id);
			}
			else
			{
				// writeToConsole("ComponentRequest deleted....\n" + id +
				// "\n-");
				treeModel.removeNodeFromParent(node);
				nodeMap.remove(id.toString());
			}
		}

		/**
		 * check for ComponentRequests against Capability still present and add/remove
		 */
		protected void checkComponentRequests(final GUID capId, final boolean addFlag)
		{
			final ComponentRequest template = new ComponentRequest((GUID) null);
			template.setCapabilityID(capId);
			try
			{
				final CompInfo[] creqs = template.copyCollectAsComponentRequest(dataspace);

				for (final CompInfo creq2 : creqs)
				{
					final ComponentRequest creq = (ComponentRequest) creq2;
					if (creq.getCapabilityID().equals(capId))
					{
						if (addFlag)
						{
							componentRequestAdded(creq);
						}
						else
						{
							componentRequestDeleted(creq);
						}
					}
				}
			}
			catch (final DataspaceInactiveException e)
			{
				System.err.println("checkComponentRequests: " + e);
			}
		}

		@Override
		public void componentRequestAdded(final ComponentRequest creq)
		{

			// may get called twice due to async events vs check
			if (nodeMap.get(creq.getID().toString()) != null)
			{
				System.out.println("Ignore duplicate componentRequestAdded for " + creq.getID());
				return;
			}

			final DefaultMutableTreeNode capNode = nodeMap.get(creq
					.getCapabilityID().toString());
			if (capNode == null)
			{
				System.err.println("note: capability " + creq.getCapabilityID() + " not (yet) known (ComponentRequest "
						+ creq.getID() + ")");
				return;
			}
			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode("ComponentRequest " + creq.getID());
			addNode.setUserObject(creq.getID());

			nodeMap.put(creq.getID().toString(), addNode);
			treeModel.insertNodeInto(addNode, capNode, capNode.getChildCount());
		}

		@Override
		public void propertyLinkRequestAdded(PropertyLinkRequest linkReq)
		{

		}

		@Override
		public void propertyLinkRequestDeleted(PropertyLinkRequest linkReq)
		{

		}

		@Override
		public void propertyLinkRequestUpdated(PropertyLinkRequest linkReq)
		{

		}

		@Override
		public void capabilityAdded(final Capability cap)
		{
			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(cap.getID());
			nodeMap.put(cap.getID().toString(), addNode);
			addNode.setUserObject(cap.getID());

			final String hostID = cap.getHostID();
			DefaultMutableTreeNode hostTreeNode = nodeMap.get(hostID);
			if (hostTreeNode == null)
			{
				hostTreeNode = new DefaultMutableTreeNode(hostID);
				nodeMap.put(hostID, hostTreeNode);
				treeModel.insertNodeInto(hostTreeNode, root, 0);
			}

			MutableTreeNode parentNode = hostTreeNode;
			int i;
			final String[] categories = DataspaceMonitor.getCategories(cap);
			for (String category : categories)
			{
				boolean found = false;

				for (i = 0; i < parentNode.getChildCount(); i++)
				{
					final DefaultMutableTreeNode cn = (DefaultMutableTreeNode) parentNode.getChildAt(i);
					final String text2 = treeNodeToString(cn);
					if (text2.equals(category))
					{
						found = true;
						parentNode = cn;
						break;
					}
					if (text2.compareTo(category) > 0)
					{
						break;
					}
				}
				if (!found)
				{
					final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(category);
					treeModel.insertNodeInto(newNode, parentNode, i);
					parentNode = newNode;
				}
			}

			// in order?!
			final String text = treeNodeToString(addNode);
			for (i = 0; i < parentNode.getChildCount(); i++)
			{
				final DefaultMutableTreeNode cn = (DefaultMutableTreeNode) parentNode.getChildAt(i);
				final String text2 = treeNodeToString(cn);
				if (text2.compareTo(text) > 0)
				{
					break;
				}
			}

			treeModel.insertNodeInto(addNode, parentNode, i);
			checkComponentRequests(cap.getID(), true);
			// make sure path is visible
			tree.expandPath(new TreePath(hostTreeNode.getPath()));
		}

		/*
		 * Component advert deletion
		 */
		@Override
		public void capabilityDeleted(final Capability cap)
		{
			final GUID id = cap.getID();
			final String hostID = cap.getHostID();
			// MutableTreeNode hostTreeNode =
			// (MutableTreeNode) capabilityTrees.get(hostID);

			final DefaultMutableTreeNode node = nodeMap.get(id.toString());
			if (node == null)
			{
				System.err.println("Unable to delete item" + id);
			}
			else
			{
				MutableTreeNode hostTreeNode = (MutableTreeNode) node.getParent();

				checkComponentRequests(id, false);

				// writeToConsole("Capability deleted....\n" + id + "\n-");
				// node.removeNodeFromParent();
				nodeMap.remove(id.toString());
				hostTreeNode.remove(node);
				// cascaded removal of empty classification/host folders
				while (hostTreeNode != root && hostTreeNode.getChildCount() < 1)
				{
					final TreeNode parentNode = hostTreeNode.getParent();
					treeModel.removeNodeFromParent(hostTreeNode);
					if (parentNode == root)
					{
						nodeMap.remove(hostID);
						break;
					}
					hostTreeNode = (MutableTreeNode) parentNode;
				}
			}

		}

		@Override
		public void capabilityUpdated(Capability cap)
		{

		}
	}

	private class ComponentView extends DataspaceTreeView implements ComponentListener
	{
		ComponentView()
		{
			super("Components");
			// set up capability tree

			tree.setTransferHandler(new ComponentGUIDTransferHandler());
			tree.setDragEnabled(true); // turn off automatic drag and drop

			DataspaceMonitor.getMonitor().addComponentListener(this);
		}

		@Override
		public void componentAdvertAdded(ComponentAdvert comp)
		{
			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(comp.getID());
			nodeMap.put(comp.getID().toString(), addNode);
			addNode.setUserObject(comp.getID());

			// in order?!
			try
			{
				final String text = treeNodeToString(addNode);
				int i;
				for (i = 0; i < root.getChildCount(); i++)
				{
					final DefaultMutableTreeNode cn = (DefaultMutableTreeNode) root.getChildAt(i);
					final String text2 = treeNodeToString(cn);
					if (text2 != null && text2.compareTo(text) > 0)
					{
						break;
					}
				}

				treeModel.insertNodeInto(addNode, root, i);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			//checkComponentRequests(comp.getID(), true);
			// make sure path is visible
			tree.expandPath(new TreePath(root.getPath()));
		}

		@Override
		public void componentAdvertUpdated(ComponentAdvert comp)
		{
			final String id = comp.getID().toString();
			final DefaultMutableTreeNode node = nodeMap.get(id);
			if (node != null)
			{
				treeModel.nodeChanged(node);
			}
		}

		@Override
		public void componentAdvertDeleted(ComponentAdvert comp)
		{
			final String id = comp.getID().toString();
			final DefaultMutableTreeNode node = nodeMap.get(id);
			if (node == null)
			{
				System.err.println("Unable to delete item" + id);
			}
			else
			{
				nodeMap.remove(id);
				treeModel.removeNodeFromParent(node);
			}
		}
	}

	private final List<DataspaceTreeView> dataspaceTreeViews = new ArrayList<DataspaceTreeView>();
	private final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();
	private final SelectionModel selectionModel;

	public ComponentBrowser(final SelectionModel selectionModel)
	{
		super(new BorderLayout());

		this.selectionModel = selectionModel;

		dataspaceTreeViews.add(new CapabilityView());
		dataspaceTreeViews.add(new ComponentView());

		final JTabbedPane pane = new JTabbedPane();
		for (DataspaceTreeView tree : dataspaceTreeViews)
		{
			selectionModel.add(tree);
			pane.add(tree.getName(), tree);
		}

		add(BorderLayout.CENTER, pane);

		ToolTipManager.sharedInstance().setEnabled(true);
		setPreferredSize(new Dimension(400, 500));

		final JTextField searchField = new JTextField();
		searchField.setMaximumSize(new Dimension(100, 20));

		final ActionListener searchAction = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final String regexString = ".*" + searchField.getText().toLowerCase() + ".*";
				for (final DataspaceTreeView capabilityView : dataspaceTreeViews)
				{
					final JTree mainCapTree = capabilityView.getMainTree();

					final DataspaceTreeCellRenderer renderer = (DataspaceTreeCellRenderer) mainCapTree.getCellRenderer();
					renderer.setPattern(regexString);

					final DefaultTreeModel treeModel = (DefaultTreeModel) mainCapTree.getModel();
					final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
					final Enumeration<?> en = rootNode.depthFirstEnumeration();
					while (en.hasMoreElements())
					{
						final DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en.nextElement();
						final Object obj = theNode.getUserObject();
						String nodeString;
						if (dataspace != null && obj instanceof GUID)
						{
							final String rval = RDFStatement.GUIDToUrl((GUID) obj);
							nodeString = DataspaceUtils.getDisplayString(dataspace, rval);
						}
						else
						{
							nodeString = theNode.toString();
						}
						// System.out.println("Matching " +
						// obj.getClass().getName());
						if (nodeString.toLowerCase().matches(regexString))
						{
							final TreePath path = new TreePath(treeModel.getPathToRoot(theNode));
							mainCapTree.scrollPathToVisible(path);
						}
					}
				}
			}
		};
		searchField.addActionListener(searchAction);
		final JButton searchButton = new JButton();
		searchButton.setIcon(MediaFactory.getImageIcon(EditorResources.SEARCH_ICON, this));
		searchButton.setEnabled(false);
		searchButton.setRolloverEnabled(true);
		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			@Override
			public void insertUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			@Override
			public void removeUpdate(final DocumentEvent e)
			{
				checkStatus();
			}

			void cancelHighlight()
			{
				for (final DataspaceTreeView dataspaceTreeView : dataspaceTreeViews)
				{
					final JTree mainCapTree = dataspaceTreeView.getMainTree();
					final DataspaceTreeCellRenderer renderer = (DataspaceTreeCellRenderer) mainCapTree.getCellRenderer();
					if (searchField.getText().equals(""))
					{
						renderer.setPattern(null);
					}
					else
					{
						final String regexString = ".*" + searchField.getText().toLowerCase() + ".*";
						renderer.setPattern(regexString);
					}
					mainCapTree.repaint();
				}
			}

			private void checkStatus()
			{
				// Always remove highlight information as it is now
				// no longer in synch with the regex in the text
				// field
				cancelHighlight();
				// Check whether the search button should be enabled
				if (!searchField.getText().equals(""))
				{
					searchButton.setEnabled(true);
				}
				else
				{
					searchButton.setEnabled(false);
				}
			}

		});

		final JLabel searchLabel = new JLabel();
		searchLabel.setText(" Search ");
		searchLabel.setLabelFor(searchField);

		searchButton.addActionListener(searchAction);

		final JPanel searchPanel = new JPanel(new BorderLayout());
		searchPanel.add(searchLabel, BorderLayout.LINE_START);
		searchPanel.add(searchField, BorderLayout.CENTER);
		searchPanel.add(searchButton, BorderLayout.LINE_END);

		add(BorderLayout.NORTH, searchPanel);
	}

	/**
	 * tree node to text
	 */

	public String treeNodeToString(final Object value)
	{
		final Object userValue = (value instanceof DefaultMutableTreeNode) ? ((DefaultMutableTreeNode) value)
				.getUserObject() : null;
		if (userValue instanceof GUID)
		{
			// String rval = RDFStatement.GUIDToUrl((GUID) userValue);
			// String display = DataspaceUtils.getDisplayString(dataspace, rval);
			// System.out.println("GUID "+userValue+" -> "+rval+" -> "+display);

			return DataspaceUtils.getCapabilityDisplayName((GUID) userValue);
		}
		return value.toString();
	}

	private void deleteComponent(final GUID id)
	{
		try
		{
			System.err.println("Delete ComponentRequest " + id);
			ComponentAdvert component = DataspaceMonitor.getMonitor().getComponentAdvert(id.toString());
			if(component != null)
			{
				dataspace.delete(component.getComponentRequestID());
			}
		}
		catch (final DataspaceInactiveException e)
		{
			System.err.println("deleteComponentRequest: " + e);
		}
	}

	private void deleteComponentRequest(final GUID id)
	{
		try
		{
			System.err.println("Delete ComponentRequest " + id);
			dataspace.delete(id);
		}
		catch (final DataspaceInactiveException e)
		{
			System.err.println("deleteComponentRequest: " + e);
		}
	}

	/**
	 * really do the request
	 */
	private void requestComponent(final ItemData item)
	{
		final Capability cap = new Capability((TupleImpl) item);

		// Issue request
		final ComponentRequest compReq = new ComponentRequest(dataspace.allocateId());

		compReq.setCapabilityID(cap.getID());
		compReq.setHostID(dataspace.allocateId());
		compReq.setContainerID(cap.getContainerID());
		compReq.setRequestID(cap.getCapabilityName() + " Request");

		try
		{
			compReq.addtoDataSpacePersistent(dataspace, /* lease */null);
			System.out.println("Issued Component Request....\n" + cap.getCapabilityName() + "\n-");

		}
		catch (final DataspaceInactiveException ex1)
		{

			System.out.println("Error: Dataspace Inactive");
			ex1.printStackTrace();
		}
	}
}