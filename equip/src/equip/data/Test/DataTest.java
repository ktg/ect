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
// DataTest.java
// Chris Greenhalgh, 28/03/01
//
// 2D app example of publishing/subscibing to info via the dataservice

package equip.data.Test;
 
import equip.net.*;
import equip.runtime.*;
import equip.data.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/** The original Java dataspace example application; now available
    in many flavours (see {@link equip.data}) */
public class DataTest {
  /***********************************************************************
   * main
   */

  public static void main (String [] args) {
    if (args.length!=3 ||
	args[0].length()!=1 ||
	(args[0].charAt(0)!='m' &&
	 args[0].charAt(0)!='s' &&
	 args[0].charAt(0)!='a')) {
      System.err.println("Usage: DataTest m|s|a <name> <server-url>");
      System.err.println(" - m = master");
      System.err.println(" - s = slave");
      System.err.println(" - a = any");
      System.exit(-1);
    }
    System.out.println("DataTest...");
    new DataTest(args[0].charAt(0), args[1], args[2]);
  }
  
  /**********************************************************************
   * constructor/initilise
   */
  private DataProxy dataservice;
  private GUIDFactory idFactory;
  private GUID publishId;
  private DataTest(char kind, String name, String serverUrl) {
    
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
      // async test
      dataservice.activateAsync();
      
      /* if (!dataservice.activate(null, null)) {
	 System.err.println("ERROR: could not activate");
	 System.exit(-1);
	 }
      */
      System.out.println("OK");
    }
    
    // redraw session 
    redrawSession = dataservice.createSession(new RedrawEventHandler(), 
					      null);

    // create GUI
    System.out.println("Create GUI...");
    createGUI();

    if (kind=='m') {
      System.out.println("Publish our information...");

      // the item that we publish
      publishId = idFactory.getUnique();
      equip.data.Test.MyType item1 = new equip.data.Test.MyTypeImpl();
      item1.id = publishId;
      item1.pos = new equip.data.Test.PositionImpl();
      item1.pos.x = item1.pos.y = 0;
      item1.name = name;
      // item,locked,processBound,local,lease
      dataservice.addItem(item1, LockType.LOCK_HARD, true, false, null);
    }

    // callback(s)
    System.out.println("Create notification session...");
    session = dataservice.createSession(new EventHandler(), null);
    
    // ask for stuff
    EventPattern pattern = new EventPatternImpl();
    equip.data.Test.MyType item = new equip.data.Test.MyTypeImpl();
    item.pos = null;
    if (kind=='s') {
      item.name = name;
    }
    // master is local; others are not
    pattern.initAsSimpleItemMonitor(item, kind=='m');
    session.addPattern(pattern);
    System.err.print("Wait for monitor events...");
    //dataservice.waitForEvents(false);
    System.err.println("OK");
    // ...
    System.out.println("Running...");
  }
  /**********************************************************************
   * Item GUI
   */
  private JFrame mainFrame;
  private Canvas area;
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

  private void click(int x, int y) {
    System.out.println("Click at "+x+","+y);
    if (publishId!=null) {
      // publish
      equip.data.Test.MyType t = 
	(equip.data.Test.MyType)dataservice.getItem(publishId);
      equip.data.Test.MyType newt = new equip.data.Test.MyTypeImpl();
      newt.id = t.id;
      newt.name = t.name;
      newt.pos = new equip.data.Test.PositionImpl();
      newt.pos.x = x;
      newt.pos.y = y;
      dataservice.updateItem(newt, false, true);
    }
    //drawPoint(x, y);
  } 
  private void drawPoint(int x, int y) {
    Graphics g = area.getGraphics();
    g.drawLine(x-10,y-10,x+10,y+10);
    g.drawLine(x+10,y-10,x-10,y+10);
    // ...
  }

  /**********************************************************************
   * data service notification handler - nested instance class
   */
  private DataSession session;
  private DataSession redrawSession;
  private GUID ignoreId = null;

  private class EventHandler extends DataCallback implements Runnable {
    public void notify(equip.data.Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
	System.out.println("notify...");
	// copy notify information into runnable for sync with Swing
	// thread (for rendering thread-safe-ness)
	this.event = event;
	this.pattern = pattern;
	this.patternDeleted = patternDeleted;
	this.session = session;
	this.closure = closure;
	try {
	  if (!SwingUtilities.isEventDispatchThread())
	    SwingUtilities.invokeLater(this);
	  else {
	      // delaying notify
	      final Runnable t = this;
	      new Thread() {
		      public void run() {
			  SwingUtilities.invokeLater(t);
		      }
		  }.start();
	  }
	  //run();
	} catch(Exception e) {
	  System.err.println("notify error: "+e);
	}
	System.out.println("...done");
    }
    private equip.data.Event event;
    private EventPattern pattern;
    private boolean patternDeleted;
    private DataSession session;
    private ValueBase closure;
    public void run() {
      if (event!=null)
	System.out.println("(sync) event "+event);
      ignoreId = null;
      if (event!=null && event instanceof DeleteEvent) {
	System.err.println("delete...");
	DeleteEvent del = (DeleteEvent)event;
	System.err.println("ignore deleting item "+del.id);
	ignoreId = del.id;
      }
      // synchronized -> after the session event that is causing this...
      // redraw all known items
      area.repaint();//0,0,1000,1000);
    }
  }

  class MyPanel extends Canvas {
    public void paint(Graphics g) {
      System.out.println("redraw...");
      System.err.print("Wait for local events...");
      dataservice.waitForEvents(true);
      System.err.println("OK");
      //   Generate synchronous callbacks for all known local items of 
      // the given type.
      //   The redrawSession will call RedrawEventHandler, which
      // will actually render each one.
      //   A copy-collect is the appropriate model here - the pattern
      // is auto-deleted after checking.
      EventPattern pattern = new EventPatternImpl();
      equip.data.Test.MyType item = new equip.data.Test.MyTypeImpl();
      pattern.initAsSimpleCopyCollect(item, true);
      // do it
      System.out.println("redraw add pattern...");
      redrawSession.addPattern(pattern);
      // done
      System.out.println("redraw done");
    }
  }

  private class RedrawEventHandler extends DataCallback {
    public void notify(equip.data.Event event, EventPattern pattern,
		       boolean patternDeleted,
		       DataSession session,
		       ValueBase closure) {
      System.out.println("redraw notify...");
      if (event==null || !(event instanceof AddEvent))
	return;
      System.err.println("add...");
      AddEvent add = (AddEvent)event;
      if (add.binding.item==null ||
	  !(add.binding.item instanceof equip.data.Test.MyType))
	return;
      equip.data.Test.MyType t = 
	(equip.data.Test.MyType)add.binding.item;
      drawPoint(t.pos.x, t.pos.y);
    }
  }
}
