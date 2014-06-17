package equip.ect.components.functionfactory;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FunctionManagementFrame extends JFrame implements MouseListener
{
	private FunctionFactory functionFactory;

	private String title = "FunctionFactory - browser";
	private Dimension defaultSize = new Dimension(400, 300);

	private JTree tree;
	private DefaultTreeModel model;

	private Hashtable compToModFrame = new Hashtable();

	public FunctionManagementFrame(final FunctionFactory factory) throws IOException
	{
		this.functionFactory = factory;

		final FunctionLoader fl = new FunctionLoader();
		final Function[] functions = fl.getFunctions();

		final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		model = new DefaultTreeModel(root);

		for (final Function function : functions)
		{
			root.add(new FunctionNode(function));
		}

		tree = new JTree(model);

		tree.addMouseListener(this);

		getContentPane().add(tree);

		setSize(defaultSize);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle(title);
		setVisible(true);
	}

	public void insertComponents(final FunctionComponent[] components)
	{

		// need to insert components which have been loaded
		// from persistence into the component tree

		// first, get a record of all functions that have been loaded
		// by the functionloader and the nodes in the tree that represent them

		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) (model.getRoot());
		final Enumeration children = root.children();
		final Hashtable classNameToNode = new Hashtable();

		while (children.hasMoreElements())
		{
			final FunctionNode node = (FunctionNode) (children.nextElement());
			final String className = node.getFunction().getClass().getName();
			classNameToNode.put(className, node);
		}

		// now insert function components into relevant node, dependant
		// upon the function they represent

		for (final FunctionComponent component2 : components)
		{
			final Function f = component2.retrieveFunction();
			final String className = f.getClass().getName();

			if (classNameToNode.containsKey(className))
			{
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) (classNameToNode.get(className));

				model.insertNodeInto(new FunctionComponentNode(component2), node, node.getChildCount());
			}
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
		handleMouseEvent(e);
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
		handleMouseEvent(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e)
	{
		handleMouseEvent(e);
	}

	private void handleFunctionComponentNode(final FunctionComponentNode fcn, final MouseEvent e)
	{
		final JPopupMenu popup = new JPopupMenu("Options");
		final JMenuItem deleteComponent = new JMenuItem("Delete component");

		deleteComponent.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				// first delete functioncomponent from factory,
				// then delete from jtree
				// also close and remove any frames that
				// may be open that can modify the components

				final FunctionComponent fc = fcn.getFunctionComponent();

				functionFactory.functionDeleted(fc);
				model.removeNodeFromParent(fcn);

				if (compToModFrame.containsKey(fc))
				{
					final FunctionComponentModificationFrame fcfm = (FunctionComponentModificationFrame) (compToModFrame
							.get(fc));
					// get frame to close any
					// resources it might be using
					fcfm.disposeYourself();
					compToModFrame.remove(fc);
				}
			}
		});

		popup.add(deleteComponent);

		final Function f = fcn.getFunctionComponent().retrieveFunction();

		String itemTitle = null;

		if (f.parametersAreModifiable())
		{
			itemTitle = "Modify component details";
		}
		else
		{
			itemTitle = "View component details";
		}

		final JMenuItem modifyComponent = new JMenuItem(itemTitle);

		modifyComponent.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				// first, check to see if modification
				// frame already exists fo this components

				// if it does, make it visible

				final FunctionComponent fc = fcn.getFunctionComponent();

				if (compToModFrame.containsKey(fc))
				{
					((FunctionComponentModificationFrame) (compToModFrame.get(fc))).setVisible(true);
				}
				else
				{
					// if not, create a new one!

					final FunctionComponentModificationFrame fcmf = new FunctionComponentModificationFrame(fcn
							.getFunctionComponent());
					compToModFrame.put(fc, fcmf);
				}
			}
		});

		popup.add(modifyComponent);

		popup.show(tree, e.getX(), e.getY());
	}

	private void handleFunctionNode(final FunctionNode fn, final MouseEvent e, final int parentRow)
	{
		final JPopupMenu popup = new JPopupMenu("Options");
		final JMenuItem createComponent = new JMenuItem("Create component");

		// add listener for create component action

		createComponent.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				try
				{
					// add new function component

					final Function f = fn.getFunction();
					final Function fclone = (f.getClass().newInstance());
					final FunctionComponent fc = functionFactory.functionAdded(fclone);

					model.insertNodeInto(new FunctionComponentNode(fc), fn, fn.getChildCount());

					// expand node that child has been added to so that we can
					// see it
					tree.expandRow(parentRow);
				}
				catch (final InstantiationException f)
				{
					f.printStackTrace();
				}
				catch (final IllegalAccessException f)
				{
					f.printStackTrace();
				}

			}
		});

		popup.add(createComponent);

		final JMenuItem renderPreview = new JMenuItem("Render preview");

		renderPreview.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				// render function into a seperate frame, using
				// default parameters

				new FunctionPreviewFrame(fn.getFunction());
			}
		});

		popup.add(renderPreview);

		popup.show(tree, e.getX(), e.getY());
	}

	private void handleMouseEvent(final MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			// find entry in jtree that user has
			// triggered pop up for
			// if functionnode, then offer ability to create
			// instance of function

			final int row = tree.getRowForLocation(e.getX(), e.getY());
			final TreePath path = tree.getPathForRow(row);
			final Object last = path.getLastPathComponent();

			if (last instanceof FunctionNode)
			{
				handleFunctionNode((FunctionNode) last, e, row);
				return;
			}
			if (last instanceof FunctionComponentNode)
			{
				handleFunctionComponentNode((FunctionComponentNode) last, e);
			}
		}
	}

}

class FunctionComponentNode extends DefaultMutableTreeNode
{
	private int id;
	private FunctionComponent fc;

	FunctionComponentNode(final FunctionComponent fc)
	{
		this.fc = fc;
		this.id = id;
	}

	@Override
	public String toString()
	{
		return fc.getDescription();
	}

	FunctionComponent getFunctionComponent()
	{
		return fc;
	}

	int getID()
	{
		return id;
	}

}

class FunctionNode extends DefaultMutableTreeNode
{
	private Function f;

	FunctionNode(final Function f)
	{
		this.f = f;
	}

	@Override
	public String toString()
	{
		return (f.getDisplayName());
	}

	Function getFunction()
	{
		return f;
	}

}
