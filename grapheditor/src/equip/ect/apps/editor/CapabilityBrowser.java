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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

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
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;

public class CapabilityBrowser extends JPanel implements DataspaceConfigurationListener
{

	/**
	 * tooltip tree cell renderer
	 */
	public class TooltipTreeCellRenderer extends DefaultTreeCellRenderer
	{

		private String pattern = null;

		private Icon component;

		/**
		 * dataspace
		 */
		protected DataspaceBean dataspace;

		/**
		 * cons
		 */
		public TooltipTreeCellRenderer(final DataspaceBean dataspace)
		{
			super();
			this.dataspace = dataspace;
			final Icon folder = MediaFactory.getImageIcon(EditorResources.FOLDER_ICON, CapabilityBrowser.this);
			setClosedIcon(folder);
			setOpenIcon(folder);
			setLeafIcon(MediaFactory.getImageIcon(EditorResources.COMPONENTREQUEST_ICON, CapabilityBrowser.this));
			component = MediaFactory.getImageIcon(EditorResources.COMPONENT_ICON, CapabilityBrowser.this);
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
				final String display = treeNodeToString(value);

				//final GUIDJTree guidtree = (GUIDJTree) tree;
				// GUID guid = (GUID)userValue;
				// String display = DataspaceUtils.getCapabilityDisplayName(dataspace,guid);

				// String rval = RDFStatement.GUIDToUrl(guid);
				// String display = DataspaceUtils.getDisplayString(dataspace,
				// rval);

				try
				{
					final ItemData item = dataspace.getItem((GUID) userValue);
					if (item instanceof TupleImpl)
					{
						final TupleImpl tuple = (TupleImpl) item;
						if (tuple.fields[CompInfo.ATTRIBUTES_INDEX] instanceof DictionaryImpl)
						{
							final DictionaryImpl d = (DictionaryImpl) tuple.fields[CompInfo.ATTRIBUTES_INDEX];
							final java.lang.Object val = d.get(BeanDescriptorHelper.SHORT_DESCRIPTION);
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
				setIcon(component);
				setText(display);
			}

			setToolTipText(text);

			if ((pattern != null) && (getText() != null))
			{
				if (getText().toLowerCase().matches(pattern))
				{
					setText("<html><font color=\"red\">" + getText() + "</font></html>");
				}
			}

			return this;
		}

		/**
		 * @param pattern
		 *            The pattern to set.
		 */
		public void setPattern(final String pattern)
		{
			this.pattern = pattern;
		}
	}

	abstract class CapabilityView extends JPanel implements MouseListener, TreeSelectionListener,
			DataspaceConfigurationListener
	{

		private String title;

		protected DefaultMutableTreeNode capabilityRoot;

		protected DefaultTreeModel capabilityTreeModel;

		protected JTree capabilityTree;

		protected Map<String, DefaultMutableTreeNode> capabilityTrees = new HashMap<String, DefaultMutableTreeNode>();

		protected Map<GUID, DefaultMutableTreeNode> capabilityNodeMap = new HashMap<GUID, DefaultMutableTreeNode>();

		protected Map<DefaultMutableTreeNode, GUID> capabilityNodeMapRev = new HashMap<DefaultMutableTreeNode, GUID>();

		private boolean supportsTreeView;

		private short clickCounter = 0;

		CapabilityView(final String title)
		{
			this(title, true);
		}

		CapabilityView(final String title, final boolean supportsTreeView)
		{
			this.title = title;
			this.supportsTreeView = supportsTreeView;
		}

		@Override
		public void capabilityUpdated(final Capability cap)
		{
		}

		/*
		 * Capability discovery
		 */
		@Override
		public void componentRequestAdded(final ComponentRequest creq)
		{

			// may get called twice due to async events vs check
			if (capabilityNodeMap.get(creq.getID()) != null)
			{
				System.out.println("Ignore duplicate componentRequestAdded for " + creq.getID());
				return;
			}

			final DefaultMutableTreeNode capNode = capabilityNodeMap.get(creq
					.getCapabilityID());
			if (capNode == null)
			{
				System.err.println("note: capability " + creq.getCapabilityID() + " not (yet) known (ComponentRequest "
						+ creq.getID() + ")");
				return;
			}
			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode("ComponentRequest " + creq.getID());

			capabilityNodeMap.put(creq.getID(), addNode);
			capabilityNodeMapRev.put(addNode, creq.getID());

			capabilityTreeModel.insertNodeInto(addNode, capNode, capNode.getChildCount());
		}

		/*
		 * Component advert deletion
		 */
		@Override
		public void componentRequestDeleted(final ComponentRequest compReq)
		{
			final GUID id = compReq.getID();
			final DefaultMutableTreeNode node = capabilityNodeMap.get(id);
			if (node == null)
			{
				System.err.println("Unable to delete (unknown component request) item" + id);
			}
			else
			{
				// writeToConsole("ComponentRequest deleted....\n" + id +
				// "\n-");
				capabilityTreeModel.removeNodeFromParent(node);
				capabilityNodeMap.remove(id);
				capabilityNodeMapRev.remove(node);
			}

		}

		public JTree getMainTree()
		{
			return capabilityTree;
		}

		public String getTitle()
		{
			return title;
		}

		@Override
		public void mouseClicked(final MouseEvent e)
		{
		}

		@Override
		public void mouseEntered(final MouseEvent e)
		{
		}

		@Override
		public void mouseExited(final MouseEvent e)
		{
		}

		@Override
		public void mousePressed(final MouseEvent e)
		{
			handle(e, true);
		}

		@Override
		public void mouseReleased(final MouseEvent e)
		{
			handle(e, false);
		}

		@Override
		public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
		{

		}

		/*
		 * Capability discovery
		 */

		@Override
		public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
		{
		}

		@Override
		public void propertyLinkRequestUpdated(final PropertyLinkRequest linkReq)
		{

		}

		public boolean supportsTreeView()
		{
			return this.supportsTreeView;
		}

		@Override
		public void valueChanged(final TreeSelectionEvent e)
		{
			// the user has changed their selection of a row in the tree
			// A text field on this panel displays the short description of the
			// currently
			// selected capability, so may need to update this

			final int[] selectionRows = capabilityTree.getSelectionRows();

			if ((selectionRows == null) || (selectionRows.length == 0))
			{
				// there are no capabilities s
				textField.setText("");
			}
			else
			{
				// tree is set up to be single selection, so there will only
				// be one value in this arrary

				final int selectionRow = selectionRows[0];
				final TreePath path = capabilityTree.getPathForRow(selectionRow);

				final Object elems[] = path.getPath();
				if (elems.length > 0)
				{
					final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) elems[elems.length - 1];
					if (treeNode != null)
					{
						final GUID id = capabilityNodeMapRev.get(treeNode);
						if (id != null)
						{
							ItemData i = null;

							try
							{
								i = dataspace.getItem(id);
							}
							catch (final DataspaceInactiveException ex)
							{
								System.err.println("ERROR: dataspace inactive");
								textField.setText("ERROR: dataspace inactive");
							}

							final Capability cap = new Capability((TupleImpl) i);

							final StringBox sb = (StringBox) (cap.getAttributeValue("shortDescription"));

							if (sb == null)
							{
								textField.setText("No description exists");
							}
							else
							{
								System.out.println("got short description: " + sb.value);
								textField.setText(sb.value);
							}

						}
						else
						{
							// no capabiltiy currently selected
							textField.setText("");
						}
					}
					else
					{
						// no capabiltiy currently selected
						textField.setText("");
					}
				}
				else
				{
					// no capabiltiy currently selected
					textField.setText("");
				}
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

		protected void handle(final MouseEvent e, final boolean pressed)
		{
			//final int selRow = capabilityTree.getRowForLocation(e.getX(), e.getY());
			final TreePath selPath = capabilityTree.getPathForLocation(e.getX(), e.getY());
			if (selPath != null && (e.isPopupTrigger() /* || !pressed */))
			{
				final Object elems[] = selPath.getPath();
				if (elems.length > 0)
				{
					final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) elems[elems.length - 1];
					if (treeNode != null)
					{
						final GUID id = capabilityNodeMapRev.get(treeNode);
						if (id != null)
						{
							// component popup
							final JPopupMenu popup = new JPopupMenu();
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

								// <stef> user has request pop-up for capability
								// so we give them the options to create
								// a new component, or to list names of existing
								// components, or to view documentation for
								// component
								// if it exists

								popup.add(new AbstractAction("Names...")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										new NameDialog(null, DataspaceMonitor.getMonitor().getDataspace(), id);
									}
								});
								popup.add(new AbstractAction("Create request")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										CapabilityBrowser.this.requestComponent(item);
									}
								});

								// now get html description if it exists
								final Capability cap = new Capability((TupleImpl) item);
								final StringBox sb = (StringBox) (cap.getAttributeValue("htmlDescription"));

								if (sb != null)
								{
									popup.add(new AbstractAction("View documentation")
									{
										@Override
										public void actionPerformed(final ActionEvent ae)
										{

											displayDocs(cap);

										}
									});

								}

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
							popup.show(capabilityTree, e.getX(), e.getY());
						}
						else
						{
							// System.out.println("Warning: node
						}
						// "+treeNode+"
						// not found in capabilityNodeMapRev");
						return;
					}
					else
					{
						// System.out.println("Warning: treeNode not
						// found");
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
							final GUID id = capabilityNodeMapRev.get(treeNode);
							if (id != null)
							{
								ItemData item = null;
								try
								{
									item = dataspace.getItem(id);
								}
								catch (final DataspaceInactiveException e1)
								{
									// TODO Auto-generated catch block
									e1.printStackTrace();
									return;
								}
								if (item != null && ((TupleImpl) item).name.equals(Capability.TYPE))
								{
									CapabilityBrowser.this.requestComponent(item);
								}
							}
						}
					}
				}
			}

		}

		// //////////////////////////////////////////////////////////////
		// Capability tree interaction
		protected void requestComponent(final TreePath path)
		{

			final Object elems[] = path.getPath();
			GUID id = null;
			DefaultMutableTreeNode treeNode = null;

			try
			{
				treeNode = (DefaultMutableTreeNode) elems[elems.length - 1];
				id = capabilityNodeMapRev.get(treeNode);
				if (treeNode == capabilityRoot) { return; }

			}
			catch (final Exception e)
			{

				System.err.println("Error: Id not found for tree node " + treeNode);
				return;
			}

			if (id == null)
			{
				System.err.println("ERROR: item could not be found (null)");
				return;
			}

			ItemData item = null;
			try
			{
				item = dataspace.getItem(id);
			}
			catch (final DataspaceInactiveException ex)
			{
				System.err.println("ERROR: dataspace inactive");
			}

			if (((TupleImpl) item).name.equals(Capability.TYPE))
			{
				CapabilityBrowser.this.requestComponent(item);
			}
			else if (((TupleImpl) item).name.equals(ComponentRequest.TYPE))
			{
				deleteComponentRequest(id);
			}
		}
	}

	class ClassificationView extends CapabilityView
	{

		ClassificationView()
		{
			super("Classification View");
			setLayout(new BorderLayout());
			// set up capability tree
			capabilityRoot = new DefaultMutableTreeNode("Capability Root");
			capabilityTreeModel = new DefaultTreeModel(capabilityRoot);
			capabilityTree = new GUIDJTree(dataspace, capabilityTreeModel);

			capabilityTree.addTreeSelectionListener(this);
			capabilityTree.setCellRenderer(new TooltipTreeCellRenderer(dataspace));
			ToolTipManager.sharedInstance().registerComponent(capabilityTree);
			final JPanel panel = makeTextPanel(capabilityTree);
			capabilityTree.addMouseListener(this);
			add(panel, BorderLayout.CENTER);

		}

		@Override
		public void capabilityAdded(final Capability cap)
		{

			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(cap.getID());
			capabilityNodeMap.put(cap.getID(), addNode);
			capabilityNodeMapRev.put(addNode, cap.getID());

			final String hostID = cap.getHostID();
			DefaultMutableTreeNode hostTreeNode = capabilityTrees.get(hostID);
			if (hostTreeNode == null)
			{
				final String hostName = hostID + " (containerID=" + cap.getContainerID().toString() + ")";
				hostTreeNode = new DefaultMutableTreeNode(hostName);
				capabilityTrees.put(hostID, hostTreeNode);
				capabilityTreeModel.insertNodeInto(hostTreeNode, capabilityRoot, 0);
				// make sure path is visible
				capabilityTree.expandPath(new TreePath(hostTreeNode.getPath()));
			}

			String classification = cap.getClassification();
			if (classification == null)
			{
				classification = "Unclassified";
			}
			else
			{
				// System.out.println("Capability " + cap.getID()
				// + " classified as " + classification);
			}

			// by classification
			final StringTokenizer toks = new StringTokenizer(classification, "/");
			MutableTreeNode parentNode = hostTreeNode;
			int i;
			while (toks.hasMoreTokens())
			{
				final String tok = toks.nextToken();

				boolean found = false;
				for (i = 0; i < parentNode.getChildCount(); i++)
				{
					final DefaultMutableTreeNode cn = (DefaultMutableTreeNode) parentNode.getChildAt(i);
					final String text2 = treeNodeToString(cn);
					if (text2.equals(tok))
					{
						found = true;
						parentNode = cn;
						break;
					}
					if (text2.compareTo(tok) > 0)
					{
						break;
					}
				}
				if (!found)
				{
					final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(tok);
					capabilityTreeModel.insertNodeInto(newNode, parentNode, i);
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

			capabilityTreeModel.insertNodeInto(addNode, parentNode, i);
			checkComponentRequests(cap.getID(), true);
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

			final DefaultMutableTreeNode node = capabilityNodeMap.get(id);
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
				capabilityNodeMap.remove(id);
				capabilityNodeMapRev.remove(node);
				hostTreeNode.remove(node);
				// cascaded removal of empty classification/host folders
				while (hostTreeNode != capabilityRoot && hostTreeNode.getChildCount() < 1)
				{
					final TreeNode parentNode = hostTreeNode.getParent();
					capabilityTreeModel.removeNodeFromParent(hostTreeNode);
					if (parentNode == capabilityRoot)
					{
						capabilityTrees.remove(hostID);
						break;
					}
					hostTreeNode = (MutableTreeNode) parentNode;
				}
			}

		}

	}

	/**
	 * GUID-translating JTree
	 */
	class GUIDJTree extends JTree
	{
		/**
		 * dataspace
		 */
		protected DataspaceBean dataspace;

		/**
		 * cons
		 */
		public GUIDJTree(final DataspaceBean dataspace, final TreeModel model)
		{
			super(model);
			this.dataspace = dataspace;
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		}

		/*
		 * public String convertValueToText(Object value, boolean selected, boolean expanded,
		 * boolean leaf, int row, boolean hasFocus) { Object userValue = (value instanceof
		 * DefaultMutableTreeNode) ? ((DefaultMutableTreeNode) value) .getUserObject() : null; if
		 * (dataspace != null && userValue instanceof GUID) { String rval =
		 * RDFStatement.GUIDToUrl((GUID) userValue); String display =
		 * NameDialog.getDisplayString(dataspace, rval); //
		 * System.out.println("GUID "+userValue+" -> "+rval+" -> // "+display); return display; }
		 * String rval = super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
		 * // System.out.println("Convert value "+value+" // ("+value.getClass().getName()+") to
		 * text -> "+rval); return rval; }
		 */
	}

	class HostView extends CapabilityView
	{

		HostView()
		{
			super("Host View");
			setLayout(new BorderLayout());
			capabilityRoot = new DefaultMutableTreeNode("Host Root");
			capabilityTreeModel = new DefaultTreeModel(capabilityRoot);
			capabilityTree = new GUIDJTree(dataspace, capabilityTreeModel);

			capabilityTree.setCellRenderer(new TooltipTreeCellRenderer(dataspace));
			capabilityTree.addTreeSelectionListener(this);
			ToolTipManager.sharedInstance().registerComponent(capabilityTree);
			capabilityTree.addMouseListener(this);
			final JPanel panel = makeTextPanel(capabilityTree);
			add(BorderLayout.CENTER, panel);

		}

		@Override
		public void capabilityAdded(final Capability cap)
		{

			final DefaultMutableTreeNode addNode = new DefaultMutableTreeNode(cap.getID());
			capabilityNodeMap.put(cap.getID(), addNode);
			capabilityNodeMapRev.put(addNode, cap.getID());

			final String hostID = cap.getHostID();
			DefaultMutableTreeNode hostTreeNode = capabilityTrees.get(hostID);
			if (hostTreeNode == null)
			{
				final String hostName = hostID + " (containerID=" + cap.getContainerID().toString() + ")";
				hostTreeNode = new DefaultMutableTreeNode(hostName);
				capabilityTrees.put(hostID, hostTreeNode);
				capabilityTreeModel.insertNodeInto(hostTreeNode, capabilityRoot, 0);
				// make sure path is visible
				capabilityTree.expandPath(new TreePath(hostTreeNode.getPath()));
			}

			// in order?!
			final String text = treeNodeToString(addNode);
			int i;
			for (i = 0; i < hostTreeNode.getChildCount(); i++)
			{
				final DefaultMutableTreeNode cn = (DefaultMutableTreeNode) hostTreeNode.getChildAt(i);
				final String text2 = treeNodeToString(cn);
				if (text2.compareTo(text) > 0)
				{
					break;
				}
			}

			capabilityTreeModel.insertNodeInto(addNode, hostTreeNode, i);
			checkComponentRequests(cap.getID(), true);
		}

		/*
		 * Component advert deletion
		 */
		@Override
		public void capabilityDeleted(final Capability cap)
		{
			final GUID id = cap.getID();
			final String hostID = cap.getHostID();
			final DefaultMutableTreeNode node = capabilityNodeMap.get(id);
			if (node == null)
			{
				System.err.println("Unable to delete item" + id);
			}
			else
			{
				final MutableTreeNode hostTreeNode = (MutableTreeNode) node.getParent();

				checkComponentRequests(id, false);

				capabilityNodeMap.remove(id);
				capabilityNodeMapRev.remove(node);
				hostTreeNode.remove(node);
				// hostTreeNode.remove(node);
				if (hostTreeNode.getChildCount() < 1)
				{
					capabilityTreeModel.removeNodeFromParent(hostTreeNode);
					capabilityTrees.remove(hostID);
				}
			}

		}

	}

	protected static JPanel makeTextPanel(final JTree tree)
	{
		final JPanel panel = new JPanel(new BorderLayout(), false);
		final JScrollPane scrollPane = new JScrollPane(tree);
		panel.add(scrollPane);
		return panel;
	}

	private final CapabilityView[] capabilityViews;

	private final DataspaceBean dataspace = DataspaceMonitor.getMonitor().getDataspace();

	private JTextField textField;

	public CapabilityBrowser()
	{
		super();

		setLayout(new BorderLayout());

		textField = new JTextField(40);

		add(textField, BorderLayout.SOUTH);

		capabilityViews = new CapabilityView[2];

		capabilityViews[0] = new ClassificationView();
		capabilityViews[1] = new HostView();

		final JTabbedPane viewsPane = new JTabbedPane();
		for (final CapabilityView capabilityView : capabilityViews)
		{
			viewsPane.addTab(capabilityView.getTitle(), capabilityView);
		}
		add(BorderLayout.CENTER, viewsPane);

		ToolTipManager.sharedInstance().setEnabled(true);
		setPreferredSize(new Dimension(400, 500));

		final List<Capability> currentCapabilities = DataspaceMonitor.getMonitor().getCapabilities();
		if (currentCapabilities != null)
		{
			for(Capability cap: currentCapabilities)
			{
				capabilityAdded(cap);
			}
		}
		DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);

		final JTextField searchField = new JTextField();
		// searchField.setPreferredSize(new Dimension(100, 20));
		searchField.setMaximumSize(new Dimension(100, 20));

		final ActionListener searchAction = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final String regexString = ".*" + searchField.getText().toLowerCase() + ".*";
				for (final CapabilityView capabilityView : capabilityViews)
				{
					final JTree mainCapTree = capabilityView.getMainTree();

					final TooltipTreeCellRenderer renderer = (TooltipTreeCellRenderer) mainCapTree.getCellRenderer();
					renderer.setPattern(regexString);

					final DefaultTreeModel treeModel = (DefaultTreeModel) mainCapTree.getModel();
					final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
					final Enumeration<?> en = rootNode.depthFirstEnumeration();
					while (en.hasMoreElements())
					{
						final DefaultMutableTreeNode theNode = (DefaultMutableTreeNode) en.nextElement();
						final Object obj = theNode.getUserObject();
						String nodeString = null;
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
		searchButton.setPreferredSize(new Dimension(20, 20));
		searchButton.setMaximumSize(new Dimension(20, 20));
		searchButton.setEnabled(false);
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
				for (final CapabilityView capabilityView : capabilityViews)
				{
					final JTree mainCapTree = capabilityView.getMainTree();
					final TooltipTreeCellRenderer renderer = (TooltipTreeCellRenderer) mainCapTree.getCellRenderer();
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
		searchLabel.setText(" Search Capabilites ");
		searchLabel.setLabelFor(searchField);

		searchButton.addActionListener(searchAction);

		final JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.add(searchLabel);
		toolbar.add(searchField);
		toolbar.add(searchButton);

		add(BorderLayout.NORTH, toolbar);
	}

	@Override
	public void capabilityAdded(final Capability cap)
	{
		for (final CapabilityView capabilityView : capabilityViews)
		{
			capabilityView.capabilityAdded(cap);
		}

	}

	@Override
	public void capabilityDeleted(final Capability cap)
	{
		for (final CapabilityView capabilityView : capabilityViews)
		{
			capabilityView.capabilityDeleted(cap);
		}

	}

	@Override
	public void capabilityUpdated(final Capability cap)
	{
		for (final CapabilityView capabilityView : capabilityViews)
		{
			capabilityView.capabilityUpdated(cap);
		}

	}

	@Override
	public void componentRequestAdded(final ComponentRequest compReq)
	{
		for (final CapabilityView capabilityView : capabilityViews)
		{
			capabilityView.componentRequestAdded(compReq);
		}
	}

	@Override
	public void componentRequestDeleted(final ComponentRequest compReq)
	{
		for (final CapabilityView capabilityView : capabilityViews)
		{
			capabilityView.componentRequestDeleted(compReq);
		}
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

	/**
	 * tree node to text
	 */

	public String treeNodeToString(final Object value)
	{
		final Object userValue = (value instanceof DefaultMutableTreeNode) ? ((DefaultMutableTreeNode) value)
				.getUserObject() : null;
		if (userValue instanceof GUID)
		{

			final String display = DataspaceUtils.getCapabilityDisplayName((GUID) userValue);

			// String rval = RDFStatement.GUIDToUrl((GUID) userValue);
			// String display = DataspaceUtils.getDisplayString(dataspace, rval);
			// System.out.println("GUID "+userValue+" -> "+rval+" -> "+display);

			return display;
		}
		return value.toString();
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

	private void displayDocs(final Capability cap)
	{
		// display documentation for the component represented by this
		// capability

		new DocsDialog(null, cap);
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
		compReq.setHostID(dataspace.allocateId()); // - UNCOMMENT WHEN TYPES
		// CONSOLIDATED
		compReq.setContainerID(cap.getContainerID());
		compReq.setRequestID("Request " + cap.getCapabilityName());

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
