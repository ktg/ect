/*
<COPYRIGHT>

Copyright (c) 2003-2005, University of Nottingham
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
// equip.data.TupleTest.java
// Chris Greenhalgh, 25/09/02, based on DataTest.java
//
// 2D app example of publishing/subscibing to info via the dataspace
// showing use of post notification, and generic tuple data item
 
package equip.data.beans;

import equip.net.*;
import equip.runtime.*;
import equip.data.*;
import equip.data.beans.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class DataspaceBeanTupleTest {
    /***********************************************************************
     * main
     */

    public static void main (String [] args) {
	if (args.length!=3 ||
	    args[0].length()!=1 ||
	    (args[0].charAt(0)!='m' &&
	     args[0].charAt(0)!='s' &&
	     args[0].charAt(0)!='a')) {
	    System.err.println("Usage: equip.data.beans.DataspaceBeanTupleTest m|s|a <name> <server-url>");
	    System.err.println(" - m = master");
	    System.err.println(" - s = slave");
	    System.err.println(" - a = any");
	    System.exit(-1);
	}
	System.out.println("DataspaceBeanTupleTest...");
	new DataspaceBeanTupleTest(args[0].charAt(0), args[1], args[2]);
    }
  
    /**********************************************************************
     * constructor/initilise
     */
    /** dataspace */
    DataspaceBean dataspace;

    /** ID with which we publish our own data item */
    private GUID publishId;

    /** name with which we publish/monitor data item(s) */
    private String name;

    /** replication/notification callback session */
    private DataSession session;

    /** pseudo-class name for our own kind of tuple */
    static final String myTupleClassName = "equip.data.TupleTest.Item2";

    /** constructor - does the business */
    private DataspaceBeanTupleTest(char mode, String itemName, 
				   String dataspaceName) {
    
	try {
	    // create dataspace bean
	    System.err.println("Create DataspaceBean");
	    dataspace = new DataspaceBean();
	    
	    // set URL, which will try to activate the bean
	    System.err.println("Set dataspace url to "+dataspaceName);
	    try {
		dataspace.setDataspaceUrl(dataspaceName);
	    } catch (Exception e) {
		System.err.println("ERROR (DataspaceBeanTupleTest.<cons>): "+e);
	    }
	    // if not active, give up
	    if (!dataspace.isActive()) {
		System.err.println("ERROR: Failed to activate!");
		System.exit(-1);
	    }
	    
	    // create GUI
	    System.out.println("Create GUI...");
	    createGUI();
	    
	    this.name = itemName;
	    
	    if (mode=='m') {
		System.out.println("Publish our information...");
		
		// new ID for the item that we publish
		publishId = dataspace.allocateId();
		
		// make a tuple
		equip.data.Tuple item1 = makeTuple(0, 0);
		
		// publish in the dataspace 
		dataspace.add(item1);
	    }
	    
	    // item add/delete/update callback(s)
	    System.out.println("Create notification callback session...");
	    
	    // Tuple as a template/pattern - just fill in the fields you need
	    // to match
	    equip.data.Tuple template = 
		new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
					 null);
	    if (mode=='s') {
		// fill in name if following only a particular named Tuple
		template.name = name;
	    }
	    
	    // add the event listener
	    // master is a local pattern, i.e. does not cause replication 
	    //   to this process from the server; others are not, i.e. do
	    //   cause replication from the server, and so the pattern is
	    //   global and not local.
	    session = dataspace.addDataspaceEventListener
		(template, /*localFlag*/(mode=='m'), new EventHandler());
	    
	    // optional - synchronize with the server to be sure that we
	    //   have received replicas of all matching items already known
	    //   to the server.
	    System.err.print("Sync...");
	    dataspace.getDataProxy().waitForEvents(false /*i.e. non-local*/);
	    System.err.println("OK");

	    // All set...
	    System.out.println("Running...");
	}
	catch (DataspaceInactiveException e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
    }

    /** make a new tuple value helper */
    private Tuple makeTuple(int x, int y) {
	// create a tuple;
	//   first field is pseudo-classname,
	//   second field is array [x position, y position]
	equip.data.Tuple item1 = 
	    new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				     new IntArrayBoxImpl(new int[] {x,y}));
	
	// also fill in tuple ID, and optionally tuple name
	item1.id = publishId;
	item1.name = name;

	return item1;
    }

    /**********************************************************************
     * Item GUI
     */
    private JFrame mainFrame;
    private Canvas area;
    /** create simple GUI */
    private void createGUI() {
	mainFrame = new JFrame();
	mainFrame.getContentPane().setLayout(new BorderLayout());
	final Canvas area = new MyPanel();
	MouseListener ml = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    click(e.getX(), e.getY());
		}};
	area.addMouseListener(ml); 
	this.area = area;

	mainFrame.getContentPane().add(area);

	mainFrame.getRootPane().setPreferredSize(new Dimension(200,200));
	mainFrame.pack();
	mainFrame.setVisible(true);
    }

    /** when the user clicks the mouse we update our published item (if any) 
     */
    private void click(int x, int y) {
	System.out.println("Click at "+x+","+y);
	if (publishId!=null) {

	    // create a new Tuple value to replace the old one.
	    //   NB never make changes to a published data item
	    //   directly - treat it as immutable.
	    equip.data.Tuple item1 = makeTuple(x, y);

	    try {
		// publish globally and reliably
		dataspace.update(item1);
	    } catch(DataspaceInactiveException e) {
		System.err.println("ERROR (DataspaceBeanTupleTest.click): "+e);
		e.printStackTrace(System.err);
	    }
	}
	//drawPoint(x, y);
    } 

    /** drawing utility - draw an X */
    private void drawPoint(int x, int y) {
	Graphics g = area.getGraphics();
	g.drawLine(x-10,y-10,x+10,y+10);
	g.drawLine(x+10,y-10,x-10,y+10);
	// ...
    }

    /**********************************************************************
     ** data service event listener.
     * 
     * Called by events matching item template ('item monitor')
     * as set up in constructor, i.e. add, update or delete.
     * 
     * Here we just use this information to know that we need to 
     * redraw the screen.
     */
    private class EventHandler implements DataspaceEventListener, Runnable {
	public void dataspaceEvent(DataspaceEvent event) {
	    System.out.println("notify...");

	    // run repaint in a Swing thread
	    try {
		if (!SwingUtilities.isEventDispatchThread())
		    SwingUtilities.invokeLater(this);
		else 
		    run();
	    } catch(Exception e) {
		System.err.println("notify error: "+e);
	    }

	    System.out.println("...done");
	}

	/** swing-related functionality.
	 * here for thread-safeness with swing.
	 */
	public void run() {
	    // post -> after the session event that is causing this...
	    // redraw all known items
	    area.repaint();
	}
    }

    /** our own Canvas class, uses a (local) copyCollect to find
     * and draw all (locally) known tuples of the kind we want.
     */
    class MyPanel extends Canvas {
	public void paint(Graphics g) {

	    System.out.println("redraw...");

	    // Synchronously get all items matching our template
	    // (NB will only return items which are local or already
	    // replicated by another session request; in this case
	    // that is the main session in the constructor).

	    equip.data.Tuple template = 
		new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
					 null);
	    ItemData [] items = null;
	    try {
		System.out.println("redraw add pattern...");
		
		items = dataspace.copyCollect(template);
		
		System.out.println("got "+items.length+" items");
	    } catch (DataspaceInactiveException e) {
		System.err.println("ERROR (DataspaceBeanTupleTest.paint): "+e);
		e.printStackTrace(System.err);
		return;
	    }

	    // draw items as 'X's
	    int i;
	    for (i=0; i<items.length; i++) {
		
		equip.data.Tuple t = 
		    (equip.data.Tuple)items[i];
		// fields are: class, x, y
		IntArrayBox values = (IntArrayBox)(t.fields[1]);
		int x = values.value[0];
		int y = values.value[1];

		drawPoint(x, y);
	    }
	}
    }
}
//EOF
