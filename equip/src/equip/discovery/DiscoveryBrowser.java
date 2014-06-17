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
/* DiscoveryBrowser
 */


package equip.discovery;

import equip.runtime.*;
import java.net.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import java.awt.Component;
import javax.swing.*;
import javax.swing.tree.*;

/** A Swing tree-based browser for the discovery service.
 * Chris Greenhalgh 2003-10-14
 */
public class DiscoveryBrowser extends DiscoveryClientAgentImpl {

    DefaultMutableTreeNode top;
    JTree tree;
    DefaultTreeModel treeModel;
    JFrame frame;
    ImageIcon currentIcon;
    ImageIcon currentblipIcon;
    ImageIcon oldIcon;

    class MyTreeCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
						      java.lang.Object value,
						      boolean selected,
						      boolean expanded,
						      boolean leaf,
						      int row,
						      boolean hasFocus) {
	    if (leaf) {
		//System.out.println("render "+value+" (class "+value.getClass().getName()+")");
		if (value instanceof DefaultMutableTreeNode &&
		    ((DefaultMutableTreeNode)value).getUserObject() instanceof UrlNodeInfo) {
		    UrlNodeInfo info = (UrlNodeInfo)((DefaultMutableTreeNode)value).getUserObject();
		    if (info.oldFlag) 
		    //System.out.println("-- old");
			setLeafIcon(oldIcon);
		    else if (info.blipCount>0)
			setLeafIcon(currentblipIcon);
		    else
			setLeafIcon(currentIcon);
		}
		else
		    setLeafIcon(currentIcon);
	    }
		
	    return super.getTreeCellRendererComponent(tree, value, selected, expanded,leaf, row, hasFocus);
	}
    }

    /** make gui */
    void makeGui() {

	top = new DefaultMutableTreeNode("Discovery", true);
	treeModel = new DefaultTreeModel(top);
	tree = new JTree(treeModel);

	try 
	{
	    InputStream ins = this.getClass().getClassLoader().getResourceAsStream("equip/discovery/current.gif");
	    int size = ins.available();
	    byte [] data = new byte[size];
	    ins.read(data);
	    currentIcon = new ImageIcon(data);
	    
	    ins = this.getClass().getClassLoader().getResourceAsStream("equip/discovery/old.gif");
	    size = ins.available();
	    data = new byte[size];
	    ins.read(data);
	    oldIcon = new ImageIcon(data);

	    ins = this.getClass().getClassLoader().getResourceAsStream("equip/discovery/currentblip.gif");
	    size = ins.available();
	    data = new byte[size];
	    ins.read(data);
	    currentblipIcon = new ImageIcon(data);

	    DefaultTreeCellRenderer renderer = new MyTreeCellRenderer();
	    renderer.setLeafIcon(currentIcon);
	    tree.setCellRenderer(renderer);
	} catch (Exception e) {
	    System.err.println("ERROR reading icons equip/discovery/current.gif (or old.gif): "+e);
	    e.printStackTrace(System.err);
	}

	JScrollPane treeView = new JScrollPane(tree);
	frame = new JFrame("DiscoveryBrowser");
	frame.getContentPane().add(treeView);
	frame.getContentPane().setSize(300,400);
	frame.pack();
	frame.setVisible(true);

	Runnable expireTask = new Runnable() {
		public void run() {
		    try { 
			while(true) {
			    Thread.sleep(1000);
			    SwingUtilities.invokeAndWait(new Runnable() {
				    public void run() {
					expireNodes();
				    }});
			}
		    } catch (InterruptedException e) {
			System.err.println("DiscoveryBrowser expireTask interrupted - terminating");
		    } catch (Exception ee) {
			System.err.println("DiscoveryBrowser expireTask error: "+ee);
			ee.printStackTrace(System.err);
		    }			
		}};
	new Thread(expireTask).start();
    }
    
    void expireNodes() {
	long now = System.currentTimeMillis();
	//System.out.println("expireNodes at "+now);

	Enumeration groups = top.children();
	while(groups.hasMoreElements()) {
	    DefaultMutableTreeNode group = 
		(DefaultMutableTreeNode)groups.nextElement();
	    Enumeration types = group.children();
	    while(types.hasMoreElements()) {
		DefaultMutableTreeNode type = 
		    (DefaultMutableTreeNode)types.nextElement();
		Enumeration urls = type.children();
		while(urls.hasMoreElements()) {
		    DefaultMutableTreeNode url = 
			(DefaultMutableTreeNode)urls.nextElement();
		    UrlNodeInfo info = (UrlNodeInfo)url.getUserObject();

		    //System.out.println("= check "+info.url+", last "+
		    //info.lastRefreshTime+", announce "+
		    //info.announce);
				      
		    if (info.blipCount>0) {
			info.blipCount--;
			//System.out.println("MADE OLD");
			if (info.blipCount==0)
			    treeModel.nodeChanged(url);
		    }
		    if (!info.oldFlag &&
			(info.lastRefreshTime+
			 info.announce*ANNOUNCEMENT_EXPIRE_COUNT.value+1000 <
			 now)) {
			info.oldFlag  = true;
			//System.out.println("MADE OLD");
			treeModel.nodeChanged(url);
		    }
		}
	    }
	}
    }
			
		    
    /** url and last refresh time */
    static class UrlNodeInfo {
	String url;
	long lastRefreshTime;
	boolean oldFlag = true;
	int blipCount = 0;
	/** announcement interval - millis */
	int announce; 
	UrlNodeInfo(String url) {
	    this.url = url;
	}
	public String toString() { return url; }
    }

    /** over-ride to keep track of group/type associations */
    void processAnnouncement(final ServerAnnouncement announce) {
	super.processAnnouncement(announce);
	Runnable task = new Runnable() {
		public void run() {
		    int si;
		    // each announcement
		    for (si=0; announce.infos!=null && si<announce.infos.length; si++) {
			if (announce.infos[si].groups==null ||
			    announce.infos[si].groups.length==0)
			    // no groups!
			    continue;
			// look for group(s)
			int gi, ti, ui;
			for (gi=0; gi<announce.infos[si].groups.length; gi++) {
			    String groupName = announce.infos[si].groups[gi];
			    DefaultMutableTreeNode groupNode = lookForName(groupName, top);
			    if (groupNode==null)
				groupNode = addNode(groupName, treeModel, top, true);
			    // look for type(s)
			    for (ti=0; ti<announce.infos[si].serviceTypes.length; ti++) {
				String typeName = announce.infos[si].serviceTypes[ti];
				DefaultMutableTreeNode typeNode = lookForName(typeName, groupNode);
				if (typeNode==null)
				    typeNode = addNode(typeName, treeModel, groupNode, true);
				// look for URL(s)
				for (ui=0; ui<announce.infos[si].urls.length; ui++) {
				    String url = announce.infos[si].urls[ui];
				    DefaultMutableTreeNode urlNode = lookForName(url, typeNode);
				    UrlNodeInfo info = null;
				    if (urlNode==null) {
					info = new UrlNodeInfo(url);
					urlNode = addNode(info, treeModel, typeNode, false);
				    } else
					info = (UrlNodeInfo)urlNode.getUserObject();
				    // update age
				    info.lastRefreshTime = System.currentTimeMillis();
				    info.oldFlag = false;
				    info.blipCount = 2;
				    info.announce = announce.announcementIntervalMillis;
				    // icon??
				    treeModel.nodeChanged(urlNode);
				    // ....
				}
			    }
			}
		    }		    
		    // ....
		}};
	
	if (SwingUtilities.isEventDispatchThread())
	    task.run();
	else
	    SwingUtilities.invokeLater(task);
    }
    
    static DefaultMutableTreeNode lookForName(String name, DefaultMutableTreeNode pnode) {
		
	Enumeration children = pnode.children();
	while(children.hasMoreElements()) {
	    DefaultMutableTreeNode node = 
		(DefaultMutableTreeNode)children.nextElement();
	    String nodeName = (String)node.getUserObject().toString();
	    if (name.equals(nodeName)) 
		// found 
		return node;
	}
	// not found
	return null;
    }
    static DefaultMutableTreeNode
	addNode(java.lang.Object name, DefaultTreeModel treeModel, DefaultMutableTreeNode pnode, boolean hasChildrenFlag) {
	DefaultMutableTreeNode node = new DefaultMutableTreeNode(name, hasChildrenFlag);

	treeModel.insertNodeInto(node, pnode,
				 pnode.getChildCount());
	return node;
    }
	
	/*
	int si;
	for (si=0; announce.infos!=null && si<announce.infos.length; si++) {
	    // match?
	    if (DiscoveryServerAgentImpl.
		matchServiceTypes(announce.infos[si].serviceTypes,
				  serviceTypes) &&
		DiscoveryServerAgentImpl.
		matchServiceTypes(announce.infos[si].groups,
				  groups)) {
		// so far so good
		int ui;
		for (ui=0; announce.infos[si].urls!=null &&
			  ui < announce.infos[si].urls.length; ui++) {
		    announceUrl(announce.infos[si].urls[ui], 
				announce.announcementIntervalMillis);
		}
	    }
	}
	*/

    /** main constructor */
    public DiscoveryBrowser(String serviceType, String group) {

	makeGui();

	DiscoveryEventListener listen = new DiscoveryEventListenerImpl() {
		public void discoveryEvent(DiscoveryClientAgent agent, 
					   String url) {
		    System.err.println("- Discovered: "+url);
		    printAll(agent);
		}
		public void discoveryRemoveEvent(DiscoveryClientAgent agent, 
					   String url) {
		    System.err.println("- Lost: "+url);
		    printAll(agent);
		}
		void printAll(DiscoveryClientAgent agent) {
		    String [] urls = agent.getKnownServers();
		    int i;
		    System.err.println("Known servers:");
		    for (i=0; urls!=null && i<urls.length; i++)
			System.err.println("  "+urls[i]);
		}
	    };
		
	// go...
	startDefault(listen, 
		     new String[] { serviceType },
		     new String[] { group });
    }
    /** sample main - as a helper for another server */
    public static void main(String [] args) {
	if (args.length!=2) {
	    System.err.println("Usage: equip.discovery.DiscoveryBrowser <serviceType> <group>");
	    System.exit(-1);
	}
	new DiscoveryBrowser(args[0], args[1]);
    }

} /* class DiscoveryClientAgent */

/* EOF */
