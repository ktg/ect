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
package equip.data.benchmark;

import equip.runtime.*;
import equip.net.*;
import equip.data.*;
import java.util.Date;
import java.util.Vector;

public class RemoteDSRateTest {
    public static void main(String [] args) {
	// start a low priority thread
	Thread low = new MyThread();
	low.setPriority(Thread.MIN_PRIORITY);
	low.start();
	try { Thread.sleep(1000); } catch (Exception e) {}
	try {
	    System.err.println("Create GUIDFactory");
	    // create local DS server
	    GUIDFactory guids = new GUIDFactoryImpl();

	    System.err.println("Create dataspace");
	    String dsUrl = "equip://128.243.22.25/default";
	    if (args.length>0)
		dsUrl = args[0];
	    DataProxy dataspace = DataManager.getInstance().getDataspace
		(dsUrl, DataManager.DATASPACE_CLIENT, true);
	    
	    int delay = 10;
	    int target = 1000;
	    for (target=1000; target<=3000; target+=1000) {
		int size = 100;
		for (size=0; size<=2000; size+=1000) {
		    System.err.print("Adding "+target+" items, array size "+
				     size+": ");
		    int i;
		    Date start = new Date();
		    Vector ids = new Vector();
		    clearDelay();
		    for (i=0; i<target; i++) {
			TestItem item = new TestItemImpl();
			item.id = guids.getUnique();
			item.data = new int [size];
			ids.addElement(item.id);
			
			dataspace.addItem(item, LockType.LOCK_HARD, true, false,
					  null);
			doDelay(low);
		    }
		    Date end = new Date();
		    long elapsed = end.getTime()-start.getTime();
		    System.err.println("Elapsed ms = "+elapsed);
		    
		    System.err.print("Update "+target+" items, array size "+
				     size+": ");
		    start = new Date();
		    clearDelay();
		    for (i=0; i<ids.size(); i++) {
			GUID id = (GUID)ids.elementAt(i);
			TestItem item = new TestItemImpl();
			item.id = id;
			item.data = new int [size];
			
			dataspace.updateItem(item, false, true);
			doDelay(low);
		    }
		    end = new Date();
		    elapsed = end.getTime()-start.getTime();
		    System.err.println("Elapsed ms = "+elapsed);
		    
		    
		    System.err.print("Delete "+target+" items, array size "+
				     size+": ");
		    start = new Date();
		    clearDelay();
		    for (i=0; i<ids.size(); i++) {
			GUID id = (GUID)ids.elementAt(i);
			
			dataspace.deleteItem(id, false);
			doDelay(low);
		    }
		    end = new Date();
		    ids.removeAllElements();
		    elapsed = end.getTime()-start.getTime();
		    System.err.println("Elapsed ms = "+elapsed);

		    try { Thread.sleep(30000); } catch (Exception e) {}
		}
	    }
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
	System.err.println("DONE");
    }
    static class MyThread extends Thread {
	public void run() {
	    synchronized(this) {
		System.err.println("MyThread running at low priority");
		while(true) {
		    try {
			this.wait();
		    } catch(Exception e) {}
		    try {
			this.notify();
		    } catch(Exception e) {}
		}
	    }
	}
    }
    static void clearDelay() {
    }
    static void doDelay(java.lang.Object o) {
	synchronized (o) {
	    try {
		o.notify();
		o.wait();
	    } catch(Exception e) {}
	}
    }
}
