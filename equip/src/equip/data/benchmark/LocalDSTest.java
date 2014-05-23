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

public class LocalDSTest {
    public static void main(String [] args) {
	try {
	    System.err.println("Create GUIDFactory");
	    // create local DS server
	    GUIDFactory guids = new GUIDFactoryImpl();

	    System.err.println("Create dataspace");
	    String dsUrl = "equip:///default";
	    DataProxy dataspace = DataManager.getInstance().getDataspace
		(dsUrl, DataManager.DATASPACE_SERVER, true);
	    
	    // number of monitors/callbacks
	    int nc, lastnc = 0;
	    for (nc=0; nc<=2; nc++) {
		System.err.println("Number of monitors: "+nc);
		int j;
		for(j=lastnc; j<nc; j++) {
		    System.err.println("Adding a local monitor");
		    DataSession dsSession;
		    dsSession = dataspace.createSession(new MyDataCallback(), null);
		    EventPattern pattern = new EventPatternImpl();
		    pattern.id = guids.getUnique();
		    TestItem item = new TestItemImpl();
		    pattern.initAsSimpleItemMonitor(item, true);
		    
		    dsSession.addPattern(pattern);
		}
		int target = 1000;
		for (target=1000; target<=3000; target+=1000) {
		    int size = 100;
		    for (size=0; size<=2000; size+=1000) {
			System.err.print("Adding "+target+" items, array size "+
					   size+": ");
			int i;
			Date start = new Date();
			Vector ids = new Vector();
			for (i=0; i<target; i++) {
			    TestItem item = new TestItemImpl();
			    item.id = guids.getUnique();
			    item.data = new int [size];
			    ids.addElement(item.id);
			    
			    dataspace.addItem(item, LockType.LOCK_HARD, true, false,
					      null);
			}
			Date end = new Date();
			long elapsed = end.getTime()-start.getTime();
			System.err.println("Elapsed ms = "+elapsed);
			
			System.err.print("Update "+target+" items, array size "+
					   size+": ");
			start = new Date();
			for (i=0; i<ids.size(); i++) {
			    GUID id = (GUID)ids.elementAt(i);
			    TestItem item = new TestItemImpl();
			    item.id = id;
			    item.data = new int [size];
			    
			    dataspace.updateItem(item, false, true);
			}
			end = new Date();
			elapsed = end.getTime()-start.getTime();
			System.err.println("Elapsed ms = "+elapsed);
			
			
			System.err.print("Delete "+target+" items, array size "+
					 size+": ");
			start = new Date();
			for (i=0; i<ids.size(); i++) {
			    GUID id = (GUID)ids.elementAt(i);
			    
			    dataspace.deleteItem(id, false);
			}
			end = new Date();
			ids.removeAllElements();
			elapsed = end.getTime()-start.getTime();
			System.err.println("Elapsed ms = "+elapsed);
		    }
		}
	    }
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
	System.err.println("DONE");
    }
	           
    static class MyDataCallback extends DataCallback {
	public void notify(equip.data.Event event, EventPattern pattern,
			   boolean patternDeleted,
			   DataSession session,
			   equip.runtime.ValueBase closure) {
	}
    }
	}
