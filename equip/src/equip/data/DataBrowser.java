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
  Chris Greenhalgh (University of Nottingham)

*/
// DataBrowser.java
// Chris Greenhalgh, 28/03/01

package equip.data;

import equip.net.*;
import equip.runtime.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class DataBrowser {
  /***********************************************************************
   * main
   */

  public static void main (String [] args) {
    if (args.length!=2) {
      System.err.println("Usage: DataBrowser <types-file> <server-url>");
      System.exit(-1);
    }
    System.out.println("DataBrowser...");
    new DataBrowser(args[0], args[1]);
  }
  
  /**********************************************************************
   * constructor/initilise
   */
  private DataProxy dataservice;
  private GUIDFactory idFactory;
  private DataBrowser(String typesFilename, String serverUrl) {
    
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
    createGUI();

    // initial sample item
    GUID id1 = idFactory.getUnique();
    ItemData item1 = new ItemDataImpl();
    item1.id = id1;
    // item,locked,local,processBound,lease
    dataservice.addItem(item1, LockType.LOCK_NONE, false, false, null);

    // callback(s)
    System.out.println("Create notification session...");
    session = dataservice.createSession(new EventHandler(this), null);

    // ask for everything!
    EventPattern pattern = new EventPatternImpl();
    pattern.initAsSimpleItemMonitor(new ItemDataImpl(), false);
    session.addPattern(pattern);

    // additional sample item
    GUID id2 = idFactory.getUnique();
    TreeNode item2 = new TreeNodeImpl();
    item2.id = id2;
    item2.parent = id1;
    // item,locked,local,processBound,lease
    dataservice.addItem(item2, LockType.LOCK_NONE, false, false, null);

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
    // GUID -> JFrame?
  private void createGUI() {
      mainFrame = new JFrame();
      mainFrame.getContentPane().setLayout(new BorderLayout());

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
      JScrollPane scrollTree = new JScrollPane(tree);
      scrollTree.setPreferredSize(new Dimension(300,500));
      mainFrame.getContentPane().add(scrollTree,
				     BorderLayout.WEST);
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
      final JList classList = new JList(knownClassNames);
      mainFrame.getContentPane().add(new JScrollPane(classList),
				     BorderLayout.EAST);
      MouseListener mouseListener = new MouseAdapter() {
	  public void mouseClicked(MouseEvent e) {
	    if (e.getClickCount() == 2) {
	      int index = classList.locationToIndex(e.getPoint());
	      addNewInstanceWindow((String)knownClassNames.elementAt(index));
	    }}}; 
      classList.addMouseListener(mouseListener); 

      //mainFrame.getRootPane().setPreferredSize(new Dimension(500,500));
      mainFrame.pack();
      mainFrame.setVisible(true);
  }
  /**********************************************************************
   * list of known classes 
   */

  // classes that can be added to the system
  private DefaultListModel knownClassNames = new DefaultListModel();
  
  private void addClassName(String name) {
    // alphabetical
    synchronized(knownClassNames) {
      int i;
      for (i=0; i<knownClassNames.size(); i++) {
	String val = (String)knownClassNames.elementAt(i);
	if (name.compareTo(val) > 0)
	  break;
      }
      knownClassNames.insertElementAt(name, i);
      System.out.println("- added class "+name);
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
    DataBrowser browser;
    public EventHandler(DataBrowser browser) {
      this.browser = browser;
    }
    public void notify(Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
	System.out.println("notify...");
	// lock DataBrowser...!
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
    public void run() {
	System.out.println("(sync) event "+event);
	if (event instanceof AddEvent) {
	    // add
  	    System.err.println("add...");
	    AddEvent add = (AddEvent)event;
	    ItemData item = add.binding.item;
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
		    itemTreeModel.insertNodeInto(tnode, tparent, 0);
		else {
		    System.err.println("WARNING: could not find parent node "+
				       node.parent+" in tree node map");
		    itemTreeModel.insertNodeInto(tnode, itemRoot, 0);
		}
	    } else
	      itemTreeModel.insertNodeInto(tnode, itemRoot, 0);
		
	} else if (event instanceof DeleteEvent) {
  	    System.err.println("delete...");
	    DeleteEvent del = (DeleteEvent)event;
	    System.err.println("Delete ignored...");
	    // ....
	    //root.insert(new DefaultMutableTreeNode("Child1"),0);
	} else
	    System.err.println("event ignored: "+event);
	// ....
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
	    JFrame window = (JFrame)itemWindowMap.get(id);
	    if (window==null) {
		createItemWindow(binding.item);
	    } 
	    else
		// to top?
		window.setVisible(true);      
	}

    }
    // recurse for subclasses
    private void createItemWindowDoClass(JPanel panel, 
					 ItemData item, Class cl) {
	Class scl = cl.getSuperclass();
	if (scl!=null)
	    // recurse
	    createItemWindowDoClass(panel, item, scl);
	// ignore Impl class
	String cname = cl.getName();
	if (cname.length()>=4 && 
	    cname.substring(cname.length()-4).equals("Impl"))
	    return;
	// Fields?
	Field [] fields = cl.getFields();
	if (fields.length==0)
	    return;
	panel.add(new JSeparator());
	panel.add(new JLabel(cname+":"));
	int i;
	for (i=0; i<fields.length; i++) {
	    if (fields[i].getDeclaringClass()!=cl)
		continue;
	    JPanel fpanel = new JPanel();
	    fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.X_AXIS));
	    panel.add(fpanel);
	    fpanel.add(new JLabel(fields[i].getName()+":"));
	    try {
		fpanel.add(new JTextField(fields[i].get(item).toString()));
	    } catch (Exception e) {
		System.err.println("ERROR getting field value: "+e);
		fpanel.add(new JLabel(e.toString()));
	    }
	}
    }
    public void createItemWindow(ItemData item) {
	JFrame window;
	window = new JFrame(item.id.toString());
	//window.getContentPane().setLayout(new BorderLayout());
	JPanel panel = new JPanel();
	// ....
	window.getContentPane().add(new JScrollPane(panel));
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.add(new JLabel("ID: "+item.id));
	panel.add(new JLabel("Class: "+item.getClass().getName()));
	createItemWindowDoClass(panel, item, item.getClass());
	//panel.add(new JLabel("...."));
	// ....
	window.pack();
	window.setVisible(true);      
	itemWindowMap.put(item.id, window);
    }
}
