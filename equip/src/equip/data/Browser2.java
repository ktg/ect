/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
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
  Marek Bell (University of Glasgow)
  Chris Greenhalgh (University of Nottingham)

*/
// Browser2.java (was DataBrowser.java)
// Chris Greenhalgh, 28/03/01

package equip.data;

import equip.net.*;
import equip.runtime.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class Browser2 {
  /***********************************************************************
   * main
   */

  public static void main (String [] args) {
    if (args.length!=1 && args.length!=2) {
      System.err.println("Usage: equip.data.Browser2 <server-url> [<types-file>]");
      System.exit(-1);
    }
    System.out.println("DataBrowser...");
    new Browser2(args[0], (args.length>1 ? args[1] : null));
  }
  
  /**********************************************************************
   * constructor/initilise
   */
  private DataProxy dataservice;
  private GUIDFactory idFactory;
  private Browser2(String serverUrl, String typesFilename) {
    
    if (typesFilename!=null) {
	/*
	// read types file
	System.out.println("read types file: "+typesFilename);
	try {
	    File typesFile = new File(typesFilename);
	    BufferedReader in = new BufferedReader
		(new InputStreamReader
		    (new FileInputStream(typesFile)));
	    do {
		String next = in.readLine();
		if (next==null)
		    break;
		addClassName(next);
	      } while (true);
	} catch(Exception e) {
	    System.err.println("Error reading types file "+typesFilename+
			       ": "+e);
	    System.exit(-1);
	}
	*/
    }

    // resolve server URL
    Moniker serviceMoniker = null;
    System.out.println("Resolve server "+serverUrl);
    ServerURL url = new ServerURL(serverUrl);
    serviceMoniker = url.getMoniker();
    if (serviceMoniker==null) {
      System.err.println("ERROR: could not understand url: "+serverUrl);
      System.exit(-1);
    }
    String checkUrl = url.getURL();
    if (checkUrl==null) 
	System.err.println("Could not get back url from ServerURL");
    else
	System.err.println("ServerURL -> "+checkUrl);

    // GUID factory
    idFactory = new GUIDFactoryImpl();
    
    // create data proxy
    dataservice = new DataProxyImpl();
    dataservice.serviceMoniker = serviceMoniker;
    dataservice.setDefaultAgent(idFactory.getUnique());

    if (serviceMoniker!=null) {
      System.out.println("Activate...");
      if (!dataservice.activate(null, null)) {
	System.err.println("ERROR: could not activate");
	System.exit(-1);
      }
      System.out.println("OK");
    }
    
    // create GUI
    System.out.println("Create GUI...");
    createGUI(serverUrl, dataservice);

    /*
    // initial sample item
    GUID id1 = idFactory.getUnique();
    ItemData item1 = new ItemDataImpl();
    item1.id = id1;
    // item,locked,local,processBound,lease
    //dataservice.addItem(item1, LockType.LOCK_NONE, false, false, null);
    */

    // callback(s)
    System.out.println("Create notification session...");
    session = dataservice.createSession(new EventHandler(this), null);

    // ask for everything!
    EventPattern pattern = new EventPatternImpl();
    pattern.initAsSimpleEventMonitor(new EventImpl(), false);
    session.addPattern(pattern);

    /*
    // additional sample item
    GUID id2 = idFactory.getUnique();
    TreeNode item2 = new TreeNodeImpl();
    item2.id = id2;
    item2.parent = id1;
    // item,locked,local,processBound,lease
    dataservice.addItem(item2, LockType.LOCK_NONE, false, false, null);
    */

    // ...
    System.out.println("Running...");
  }
  /** table of ds browsers, DataProxy -> Browser2 */
  static Hashtable browsers = new Hashtable();

  /** open browser */
  static void openBrowser(String name, DataProxy dataservice) {
      synchronized(browsers) {
	  Browser2 browser = (Browser2)browsers.get(dataservice);
	  if (browser==null) {
	      browser = new Browser2(name, dataservice);
	      browsers.put(dataservice, browser);
	  } else
	      browser.mainFrame.setVisible(true);
      }
  }

  /** create over in-memory dataspace */
  Browser2(String name, DataProxy  dataservice) {
    this.dataservice = dataservice;

    // GUID factory
    idFactory = (GUIDFactory)SingletonManager.get("equip.data.GUIDFactoryImpl");
    
    // create GUI
    System.out.println("Create GUI...");
    createGUI(name, dataservice);

    // callback(s)
    System.out.println("Create notification session...");
    session = dataservice.createSession(new EventHandler(this), null);

    // ask for everything!
    EventPattern pattern = new EventPatternImpl();
    // local only on internal dataspace!
    pattern.initAsSimpleEventMonitor(new EventImpl(), true);
    session.addPattern(pattern);

    // ...
    System.out.println("Running...");
  }
  /**********************************************************************
   * Item GUI
   */
  private JFrame mainFrame;
  DefaultTreeModel itemTreeModel;
  private DefaultMutableTreeNode itemRoot;
  private HashMap itemNodeMap = new HashMap(); 
    // GUID -> DefaultMutableTreeNode
  private HashMap itemNodeMapRev = new HashMap(); 
    // DefaultMutableTreeNode -> GUID
  private HashMap itemWindowMap = new HashMap(); 
    // GUID -> ItemWindow

    private DefaultListModel eventListModel;
    private JList eventList;

  private void createGUI(final String name, final DataProxy dataspace) {
      mainFrame = new JFrame(name);
      JSplitPane splitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      JSplitPane splitPane2 = new JSplitPane();

      // running status display = events
      eventListModel = new DefaultListModel();
      eventList = new JList(eventListModel);
      eventList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      mainFrame.getContentPane().add(splitPane1);
      JScrollPane runningPane = new JScrollPane(eventList);
      runningPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      //runningPane.setPreferredSize(new Dimension(400,300));
      splitPane1.setRightComponent(runningPane);

      ListSelectionModel listSelectionModel = eventList.getSelectionModel();
      listSelectionModel.addListSelectionListener
	  (new ListSelectionListener() {
		  public void valueChanged(ListSelectionEvent e) {
		      if (e.getValueIsAdjusting() == false) {
			  if (eventList.getSelectedValue() == null) {
			      //No selection
			  } else {
			      java.lang.Object object = eventList.getSelectedValue();
			      System.err.println("Select item "+object+"...");
			      //Selection, pop up
			      new ItemWindow(object);
			      eventList.clearSelection();
			  }
		      }
		  }});

      // items
      itemRoot = new DefaultMutableTreeNode("Root");
      GUID n = new GUIDImpl();
      n.setNull();
      itemNodeMap.put(n, itemRoot);
      itemNodeMapRev.put(itemRoot, n);
      //root.insert(new DefaultMutableTreeNode("Child1"),0);
      //root.insert(new DefaultMutableTreeNode("Child2"),1);
      itemTreeModel = new DefaultTreeModel(itemRoot);
      final JTree tree = new JTree(itemTreeModel);
      tree.expandRow(0);
      tree.setShowsRootHandles(true);
      JScrollPane scrollTree = new JScrollPane(tree);
      scrollTree.setPreferredSize(new Dimension(400,500));
      splitPane2.setLeftComponent(scrollTree);

      MouseListener ml = new MouseAdapter() {
	      public void mouseClicked(MouseEvent e) {
		  int selRow = tree.getRowForLocation(e.getX(),e.getY());
		  TreePath selPath = tree.getPathForLocation(e.getX(), 
							     e.getY());
		  if(selRow != -1) {             
		      if(e.getClickCount() == 1) {
			  itemTreeSingleClick(selRow, selPath); }
		      else if(e.getClickCount() == 2) {
			  itemTreeDoubleClick(selRow, selPath); }
		  }}};
      tree.addMouseListener(ml); 

      // classes
      final JTree classList = new JTree(knownClasses);
      classList.setShowsRootHandles(true);

      JScrollPane scrollTree2= new JScrollPane(classList);
      scrollTree2.setPreferredSize(new Dimension(400,500));
      splitPane2.setRightComponent(scrollTree2);

      splitPane1.setLeftComponent(splitPane2);

      //mainFrame.getRootPane().setPreferredSize(new Dimension(500,500));
      mainFrame.pack();
      mainFrame.setVisible(true);

      mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      mainFrame.addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
		  System.out.println("Closing data browser "+name+"...");
		  dataspace.deleteSession(session);
		  synchronized(browsers) {
		      browsers.remove(dataspace);
		  }
	      }
	  });

  }
  /**********************************************************************
   * list of known classes 
   */

  // classes that can be added to the system
  private DefaultTreeModel knownClasses = 
      new DefaultTreeModel(new DefaultMutableTreeNode("java.lang.Object"));
  
  private void addClass(Class cl) {
      // should do class hierarchy....
      // just list for now
      String cname = cl.getName();
      Class scl = cl.getSuperclass();
      if (scl!=null)
	  // recurse
	  addClass(scl);
      if (scl==null)
	  return;
      String scname = scl.getName();
      if (cname.length()>=4 && 
	  cname.substring(cname.length()-4).equals("Impl")) 
	  // do not add implementation class(es)
	  ;
      else
	  addClassName(cname, scname);
  }
  private DefaultMutableTreeNode findClassNode(DefaultMutableTreeNode parent,
					       String name) {
      if (parent.getUserObject()!=null &&
	  parent.getUserObject().equals(name))
	  return parent;
      Enumeration treeNodes = parent.depthFirstEnumeration();
      while (treeNodes.hasMoreElements()) {
	  DefaultMutableTreeNode n = (DefaultMutableTreeNode)treeNodes.nextElement();
	  if (n.getUserObject()!=null &&
	      n.getUserObject().equals(name))
	      return n;
	  //System.out.println("--findClassNode - check "+n.getUserObject());
      }
      System.err.println("WARNING: findClassNode failed for "+name);
      return null;
  }

  private void addClassName(String name, String scname) {
    // alphabetical
    synchronized(knownClasses) {
      // find superclass
	DefaultMutableTreeNode parent = null, child = null;
	DefaultMutableTreeNode root = 
	    (DefaultMutableTreeNode)knownClasses.getRoot();
	parent = findClassNode(root, scname);
	if (parent==null) {
	    System.err.println("ERROR _ parent not found");
	    return;
	}
	int i;
	for (i=0; i<parent.getChildCount(); i++) {
	    child = 
		(DefaultMutableTreeNode)parent.getChildAt(i);
	    if (child.getUserObject()!=null &&
		child.getUserObject().equals(name))
		// found
		return;
	    if (child.getUserObject()!=null &&
		((String)(child.getUserObject())).compareTo(name)>0)
		// not found (order)
		break;
	}
	child = new DefaultMutableTreeNode(name);
	knownClasses.insertNodeInto(child, parent, i);
	System.out.println("- added class "+name+" (extends "+scname+")");
    }
  }
  // new instance...
  private void addNewInstanceWindow(String cname) {
    System.err.println("addNewInstanceWindow for class "+cname);
    Class cl = null;
    java.lang.Object ob = null;
    try {
      cl = Class.forName(cname+"Impl");
      ob = cl.newInstance();
    } catch (Exception e) {
      System.err.println("ERROR: Unable to create instance of class "+
			 cname+"Impl");
      return;
    }
    if (!(ob instanceof ItemData)) {
      System.err.println("ERROR: class does not derive from ItemData");
      return;
    }
    ItemData item = (ItemData)ob;
    item.id = idFactory.getUnique();
    if (ob instanceof TreeNode) {
      TreeNode tnode = (TreeNode)ob;
      tnode.parent = idFactory.getNull();
    }
    System.err.println("Adding item "+item.id);
    dataservice.addItem(item, LockType.LOCK_NONE, false, false, null);
  }

  /**********************************************************************
   * data service notification handler - nested instance class
   */
  private DataSession session;

  private class EventHandler extends DataCallback implements Runnable {
    Browser2 browser;
    public EventHandler(Browser2 browser) {
      this.browser = browser;
    }
    public void notify(Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
	System.out.println("notify...");
	// lock Browser...!
	synchronized(this) {
	  // will block/wait for response so can share instance vars.
	  this.event = event;
	  this.pattern = pattern;
	  this.patternDeleted = patternDeleted;
	  this.session = session;
	  this.closure = closure;
	  try {
	    if (!SwingUtilities.isEventDispatchThread())
	      SwingUtilities.invokeAndWait(this);
	    else
	      run();
	  } catch(Exception e) {
	    System.err.println("notify error: "+e);
	  }
	  System.out.println("...done");
	}
    }
    private Event event;
    private EventPattern pattern;
    private boolean patternDeleted;
    private DataSession session;
    private ValueBase closure;
    private boolean itemPresent;
    public void run() {
       
	//	System.out.println("(sync) event "+event);
	addClass(event.getClass());
	if (event instanceof AddEvent) {
	    // add
  	    System.err.println("add...");
	    AddEvent add = (AddEvent)event;
	    ItemData item = add.binding.item;

	    // lease add => update
	    if (add.kind.data==ItemEventKind.EQDATA_KIND_LEASE_RENEW) {
		System.out.println("Add for already present -> update");
		// updatewindow if any
		ItemWindow window = (ItemWindow)itemWindowMap.get(item.id);
		
		if (window!=null) {
		    System.out.println("- update window");
		    window.update(item);
		}
		return;
	    }

	    addClass(item.getClass());
	    DefaultMutableTreeNode tnode = 
		new DefaultMutableTreeNode(item.getClass().getName()+
					   item.id.toString());
	    itemNodeMap.put(item.id, tnode);
	    itemNodeMapRev.put(tnode, item.id);

	    if (item instanceof TreeNode) {
		TreeNode node = (TreeNode)item;
		DefaultMutableTreeNode tparent =
		    (DefaultMutableTreeNode)itemNodeMap.get(node.parent);
		if (tparent!=null)
		    itemTreeModel.insertNodeInto(tnode, tparent, 
						 tparent.getChildCount());
		else {
		    System.err.println("WARNING: could not find parent node "+
				       node.parent+" in tree node map");
		    itemTreeModel.insertNodeInto(tnode, itemRoot, 
						 itemRoot.getChildCount());
		}
	    } else
	      itemTreeModel.insertNodeInto(tnode, itemRoot, 
					   itemRoot.getChildCount());

	} else if (event instanceof DeleteEvent) {
  	    System.err.println("delete...");
	    DeleteEvent del = (DeleteEvent)event;
	    DefaultMutableTreeNode node = 
		(DefaultMutableTreeNode)itemNodeMap.get(del.id);
	    if (node==null) {
		System.err.println("Delete unable to find item "+del.id);
	    } else {
		itemTreeModel.removeNodeFromParent(node);
	    }
		
	    // remove window if any
	    ItemWindow window = (ItemWindow)itemWindowMap.get(del.id);
	    
	    if (window!=null) {
		System.out.println("- dispose of window");
		itemWindowMap.remove(del.id);
		window.dispose();
	    }
	    
	} else if (event instanceof UpdateEvent) {
	    
	    UpdateEvent update = (UpdateEvent)event;
	    // update window if any
	    ItemWindow window = (ItemWindow)itemWindowMap.get(update.item.id);
	    
	    if (window!=null) {
		System.out.println("- update window");
		window.update(update.item);
	    }
	    // ....
	} else {
	    eventListModel.addElement(event);
	    int shorten = eventListModel.size() - 50;
	    if (shorten>0) {
		try {
		eventListModel.removeRange(0, shorten-1);
		}
		catch (Exception e) {
		}
	    }
	}
    }
  }
  /**********************************************************************
   * click on item tree */
    public void itemTreeSingleClick(int row, TreePath path) {
	System.out.println("Single click "+row);
	// ....
    }
    public void itemTreeDoubleClick(int row, TreePath path) {
	System.out.println("Double click "+row);
	// ....
	java.lang.Object [] elems = path.getPath();
	int i;
	System.out.println("- path len = "+elems.length);
	for (i=0; i<elems.length; i++) 
	    System.out.println("  ["+i+"] = "+elems[i]);
	// last element is the ItemData(Impl) subclass of that item
	if (elems.length==0)
	  return;
	GUID id = null;
	DefaultMutableTreeNode tnode = null;
	try {
	    tnode = (DefaultMutableTreeNode)elems[elems.length-1];
	    id = (GUID)itemNodeMapRev.get(tnode);
	} catch (Exception e) {
	  System.err.println("ERROR: node item not ItemData subclass: "+e);
	  System.err.println("- tclass "+elems[elems.length-1].
			     getClass().getName());
	  if (itemNodeMapRev.get(tnode)!=null)
	      System.err.println("- class "+itemNodeMapRev.get(tnode).
				 getClass().getName());
	  return;
	}
	if (id==null) {
	    System.err.println("ERROR: item could not be found (null)");
	    return;
	}
	synchronized(this) {
	    // create new window
	    System.out.println("New windows for item "+id);
	    ItemBinding binding = dataservice.getItemBinding(id);
	    if (binding==null) {
		System.err.println("ERROR: item "+id+" not found in database");
		return;
	    }
	    ItemWindow window = (ItemWindow)itemWindowMap.get(id);
	    if (window==null) {
		itemWindowMap.put(id, new ItemWindow(binding.item));
	    } 
	    else
		// to top?
		window.setVisible(true);      
	}

    }
    // nested class for item
    private class ItemWindow {
	DefaultTreeModel itemFieldsTreeModel;
	ItemFieldNode itemFieldsRoot;
	JFrame window;

	private String getFieldText(Class fieldClass,
				    String fieldName, 
				    java.lang.Object fieldValue,
				    String fieldNamePrefix,
				    boolean hasChildren[]) { 
	    hasChildren[0] = false;
	    String fieldValueText;
	    if (fieldClass.getName().equals("java.lang.String")) {
		fieldValueText = "ERROR";
		if (fieldValue==null)
		    fieldValueText = "null";
		else
		    fieldValueText = '\"' + fieldValue.toString() + '\"';
	    } else if (fieldClass.isPrimitive()) {
		fieldValueText = fieldValue.toString();
	    } else {
		if (fieldValue!=null) {
		    hasChildren[0] = true;
		    fieldValueText = "...";
		} else
		    fieldValueText = "null";
	    }
	    
	    String cname = fieldClass.getName();
	    if (fieldClass.isArray()) {
		cname = fieldClass.getComponentType().getName()+"[]";
	    } else if (fieldValue!=null &&
		       !fieldClass.isPrimitive()) {
		cname = fieldValue.getClass().getName();
	    }
	    /*
	    // ignore Impl class
	    if (cname.length()>=4 && 
		cname.substring(cname.length()-4).equals("Impl")) {
		cname = cname.substring(0,cname.length()-4);
	    }
	    */
	    return fieldNamePrefix+"."+fieldName+
		    " : "+cname+
		    " = "+fieldValueText;
	}
	public ItemWindow(java.lang.Object item) {
	    final GUID id = (item instanceof ItemData) ? ((ItemData)item).id : null;

	    window = new JFrame(id!=null ? id.toString() : item.toString());
	    window.getContentPane().setLayout(new BorderLayout());

	    itemFieldsRoot = new ItemFieldNode(" :"+item.getClass().getName()+
					       " = "+(item==null ? "null" : "..."));
	    itemFieldsTreeModel = new DefaultTreeModel(itemFieldsRoot);
	    final JTree tree = new JTree(itemFieldsTreeModel);
	    tree.setShowsRootHandles(true);
	    JScrollPane scrollTree = new JScrollPane(tree);
	    scrollTree.setPreferredSize(new Dimension(300,500));
	    window.getContentPane().add(scrollTree,
					BorderLayout.CENTER);

	    updateItemWindowDoClass(item, item.getClass(),
				    itemFieldsRoot, "");
	    tree.expandRow(0);

	    window.pack();
	    window.setVisible(true);      
	    window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	    if (id!=null) {
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
			    System.out.println("Closing item window...");
			    itemWindowMap.remove(id);
			}
		    });
	    }
	}
	public void dispose() {
	    window.dispose();
	}
	public void update(ItemData item) {
	    updateItemWindowDoClass(item, item.getClass(),
				    itemFieldsRoot, "");
	    //window.repaint();
	}
	private void updateItemWindowDoClass(java.lang.Object item, Class cl,
					     ItemFieldNode parent,
					     String fieldNamePrefix) {
	    String cname = cl.getName();
	    // ignore Impl class
	    if (cname.length()>=4 && 
		cname.substring(cname.length()-4).equals("Impl")) {
		cl = cl.getSuperclass();
		if (cl==null)
		    return;
	    }

	    // Fields?
	    Field [] fields = cl.getFields();
	    if (fields.length==0)
		return;
	    int i;
	    for (i=0; i<fields.length; i++) {
		Class fieldClass = fields[i].getType();
		String fieldName = fields[i].getName();
		java.lang.Object fieldValue = null;
		try {
		    fieldValue = fields[i].get(item);
		} catch (Exception e) {
		    System.err.println("ERROR getting field "+fieldName+" value: "+e);
		}
		boolean [] hasChildren = new boolean[1];
		String text = getFieldText(fieldClass, fieldName, 
					   fieldValue, 
					   fieldNamePrefix, hasChildren);
		ItemFieldNode itemField = null;

		if (parent.getChildCount()>i) {
		    itemField = (ItemFieldNode)parent.getChildAt(i);
		    if (itemField.text.equals(text))
			// no change at this level
			;
		    else {
			// change this item
			itemField.changeText(text);
			if (!hasChildren[0]) {
			    // ensure children are removed
			    itemField.removeAllChildren();
			    itemFieldsTreeModel.nodeStructureChanged(itemField);
			} 
		    }
		} else {
		    // new node
		    itemField = new ItemFieldNode(text);
		    itemFieldsTreeModel.insertNodeInto(itemField, parent, 
						       parent.getChildCount());
		}

		    
		if (hasChildren[0]) {
		    // recurse
		    if (fieldClass.isArray()) {
			// array
			int j;
			Class componentType = fieldClass.getComponentType();
			//fieldValue!=null
			for (j=0; j<java.lang.reflect.Array.getLength(fieldValue); j++) {
			    java.lang.Object arrayValue = 
				java.lang.reflect.Array.get(fieldValue, j);
			    text = getFieldText(componentType, 
						fieldName+"["+j+"]", 
						arrayValue,
						fieldNamePrefix, hasChildren);
			    
			    /* XXXX */
			    /* need to introduce items for each element value
			       ...
			    */
			    ItemFieldNode arrayNode = null;
			    if (itemField.getChildCount()>j) {
				arrayNode = (ItemFieldNode)itemField.getChildAt(j);
				if (arrayNode.text.equals(text))
				    // no change at this level
				    ;
				else {
				    // change this item
				    arrayNode.changeText(text);
				    if (!hasChildren[0]) {
					// ensure children are removed
					arrayNode.removeAllChildren();
					itemFieldsTreeModel.nodeStructureChanged(arrayNode);
				    } 
				}
			    } else {
				// new node
				arrayNode = new ItemFieldNode(text);
				itemFieldsTreeModel.insertNodeInto(arrayNode, 
							     itemField,
							     itemField.getChildCount());
			    }
			    if (hasChildren[0])
				updateItemWindowDoClass(arrayValue,
							arrayValue.getClass(),
							arrayNode,
							fieldNamePrefix+"."+
							fieldName+"["+j+"]");
			}
			while(itemField.getChildCount()>j) {
			    ItemFieldNode arrayNode =
				(ItemFieldNode)itemField.getChildAt(j);
			    itemFieldsTreeModel.removeNodeFromParent(arrayNode);
			}

		    } else {
			// reference
			updateItemWindowDoClass(fieldValue,
						fieldValue.getClass(),
						itemField,
						fieldNamePrefix+"."+
						fieldName);
		    }
		}
	    }
	    while(parent.getChildCount()>i) {
		ItemFieldNode itemField = (ItemFieldNode)parent.getChildAt(i);
		itemFieldsTreeModel.removeNodeFromParent(itemField);
	    }
	}
	public void setVisible(boolean flag) {
	    window.setVisible(flag);
	}
	// nested class for field/etc
	private class ItemFieldNode extends DefaultMutableTreeNode {
	    String text;
	    String name, typeName, valueText;
	    boolean simpleValueFlag;
	    boolean arrayFlag;
	    int arraySize;
	    String fieldName;
	    ItemFieldNode(String text) {
		super(text);
		this.text = text;
	    }
	    void changeText(String text) {
		System.out.println("- node text change: "+this.text+" -> "
				   +text);
		this.text = text;
		javax.swing.tree.TreeNode [] path = 
		    itemFieldsTreeModel.getPathToRoot(this);
		itemFieldsTreeModel.valueForPathChanged(new TreePath(path), text);
		itemFieldsTreeModel.nodeChanged(this);
	    }
	}
    }
}
