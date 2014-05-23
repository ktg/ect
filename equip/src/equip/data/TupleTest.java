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
// equip.data.TupleTest.java
// Chris Greenhalgh, 25/09/02, based on DataTest.java
//
// 2D app example of publishing/subscibing to info via the dataspace
// showing use of post notification, and generic tuple data item
 
package equip.data;

import equip.net.*;
import equip.runtime.*;
import equip.data.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/** A version of the standard dataspace demo app (see {@link
     equip.data}) using the {@link DataProxy} 'low-level' API but with
     generic tuple data items ({@link equip.data.Tuple}) rather than
     custom IDL'd data types.  */
public class TupleTest {
  /***********************************************************************
   * main
   */

  public static void main (String [] args) {
    if (args.length!=3 ||
	args[0].length()!=1 ||
	(args[0].charAt(0)!='m' &&
	 args[0].charAt(0)!='s' &&
	 args[0].charAt(0)!='a')) {
      System.err.println("Usage: equip.data.TupleTest m|s|a <name> <server-url>");
      System.err.println(" - m = master");
      System.err.println(" - s = slave");
      System.err.println(" - a = any");
      System.exit(-1);
    }
    System.out.println("TupleTest...");
    new TupleTest(args[0].charAt(0), args[1], args[2]);
  }
  
  /**********************************************************************
   * constructor/initilise
   */
  /** GUID factory - shared by application */
  private GUIDFactory guids;

  /** reference to the (client) dataspace we are using */
  private DataProxy dataspace;

  /** ID with which we publish our own data item */
  private GUID publishId;
  /** name with which we publish/monitor data item(s) */
  private String name;

  /** replication/notification callback session */
  private DataSession session;

  /** redraw copy/collect callback session */
  private DataSession redrawSession;

    
  /** pseudo-class name for our own kind of tuple */
  static final String myTupleClassName = "equip.data.TupleTest.Item2";

  /** constructor - does the business */
  private TupleTest(char kind, String name, String dataspaceUrl) {
    
    // GUID factory
    guids = new GUIDFactoryImpl();
    
    // join dataspace as a client, with synchronous (blocking) 
    // activation (joining)

    System.err.println("Join dataspace "+dataspaceUrl);

    dataspace = DataManager.getInstance().getDataspace
	(dataspaceUrl, DataManager.DATASPACE_CLIENT, true, false);
    
    if (dataspace==null){
      System.err.println("ERROR: could not join dataspace: "+dataspaceUrl);
      System.exit(-1);
    }

    // redraw session - will be used later with a CopyCollect to call
    //   a RedrawEventHandler.
    System.out.println("Create redraw callback session...");
    redrawSession = dataspace.createSession(new RedrawEventHandler(), 
					      null);

    // create GUI
    System.out.println("Create GUI...");
    createGUI();
    
    this.name = name;
      
    if (kind=='m') {
      System.out.println("Publish our information...");

      // new ID for the item that we publish
      publishId = guids.getUnique();

      // create a tuple;
      //   first field is pseudo-classname,
      //   second field is array [x position, y position]
      equip.data.Tuple item1 = 
	  new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				   new IntArrayBoxImpl(new int[] {0,0}));

      // also fill in tuple ID, and optionally tuple name
      item1.id = publishId;
      item1.name = name;

      // publish in the dataspace - 
      //   item,locked,processBound,local,lease
      dataspace.addItem(item1, LockType.LOCK_HARD, true, false, null);
    }

    // item add/delete/update callback(s)
    System.out.println("Create notification callback session...");

    // create a session with the EventHandler to be called with
    //   matched events (see patterns, below)
    session = dataspace.createSession(new EventHandler(), null);
    
    // ask for stuff
    EventPattern pattern = new EventPatternImpl();
    // Tuple as a pattern - just fill in the fields you need
    equip.data.Tuple item = 
	new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				 null);
    if (kind=='s') {
	// fill in name if following only a particular named Tuple
	item.name = name;
    }
    
    // master is a local pattern, i.e. does not cause replication 
    //   to this process from the server; others are not, i.e. do
    //   cause replication from the server, and so the pattern is
    //   global and not local.
    pattern.initAsSimpleItemMonitor(item, kind=='m');

    // add the pattern to the EventHandler callback session
    session.addPattern(pattern);

    // optional - synchronize with the server to be sure that we
    //   have received replicas of all matching items already known
    //   to the server.
    System.err.print("Sync...");
    dataspace.waitForEvents(false /*i.e. non-local*/);
    System.err.println("OK");

    // All set...
    System.out.println("Running...");
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
      equip.data.Tuple item1 = 
	  new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				   new IntArrayBoxImpl(new int[] {x, y}));
      // also fill in tuple ID and (optionally) name
      item1.id = publishId;
      item1.name = name;

      // publish globally and reliably
      //  (args: item.local,reliable)
      dataspace.updateItem(item1, false, true);
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
   ** data service notification handler - nested instance class.
   * 
   * Post-event callback. 
   * 
   * Called by events matching ItemMonitor
   * in session set up in constructor, i.e. add, update or delete.
   * 
   * Here we just use this information to know that we need to 
   * redraw the screen.
   */
  private class EventHandler extends DataCallbackPostImpl implements Runnable {
    public void notifyPost(equip.data.Event event, EventPattern pattern,
			   boolean patternDeleted,
			   DataSession session,
			   DataProxy dataspace, 
			   ItemData oldValue, 
			   ItemBinding oldBinding, 
			   ValueBase closure) {
	System.out.println("notify...");

	// run repaing in a Swing thread
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

      //   Generate synchronous callbacks for all known local items of 
      // the given type.
      //   The redrawSession will call RedrawEventHandler, which
      // will actually render each one.
      //   A copy-collect is the appropriate model here - the pattern
      // is auto-deleted after checking.

      EventPattern pattern = new EventPatternImpl();
      equip.data.Tuple item = 
	  new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				   null);
      pattern.initAsSimpleCopyCollect(item, true);

      // do it
      System.out.println("redraw add pattern...");
      redrawSession.addPattern(pattern);
      System.out.println("redraw done");
    }
  }

  /** callback within MyCanvas.paint do draw tuples.
   * 
   * this DataCallback is used with the CopyCollect, above. 
   * 
   * As such, pre/post does not matter.
   * Each notification will be a pseudo-add event, and this will
   * draw an X for the corresponding item.
   */
  private class RedrawEventHandler extends DataCallback {
    public void notify(equip.data.Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
      System.out.println("redraw notify...");
      try {
	  AddEvent add = (AddEvent)event;
	  equip.data.Tuple t = 
	      (equip.data.Tuple)add.binding.item;
	  // fields are: class, x, y
	  IntArrayBox values = (IntArrayBox)(t.fields[1]);
	  int x = values.value[0];
	  int y = values.value[1];

	  drawPoint(x, y);
      } catch (Exception e) {
	  System.err.println("ERROR in RedrawEventHandler: "+e);
      }
    }
  }
}
