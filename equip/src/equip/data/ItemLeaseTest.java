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
package equip.data;

import equip.runtime.*;
import equip.net.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/** ItemLeaseTest.
 * Chris Greenhalgh, 2003-10-21 */
public class ItemLeaseTest {
    public static int LEASE_TIME_S = 4;
    static int [] update_intervals = new int[] { 4, 1, 1, 1, 1, 1, 6, 1, 1, 1, 1, 6 };

    /** app.
     * Usage: equip.data.ItemLeaseTest <ds-url>
     */
    public static void main(String [] args) {
	if (args.length!=1) {
	    System.err.println("Usage: equip.data.ItemLeaseTest <ds-url>");
	    System.exit(-1);
	}
	String url = args[0];
	try {
	    DataManager mgr = DataManager.getInstance();
	    // client, activated
	    DataProxy ds = mgr.getDataspace(url,
					    DataManager.DATASPACE_CLIENT,
					    true);
	    GUIDFactory fact = new GUIDFactoryImpl();
	    GUID id = fact.getUnique();

	    DataSession session = ds.createSession(new DataCallbackPostImpl() {
		    public void notifyPost(Event event, 
					   EventPattern pattern,
					   boolean patternDeleted,
					   DataSession session,
					   DataProxy dataspace,
					   ItemData oldValue,
					   ItemBinding oldBinding,
					   ValueBase closure) {
			if (event instanceof AddEvent) {
			    AddEvent add = (AddEvent)event;
			    System.err.println("Add of "+add.binding.item.getClass().getName()+" id "+add.binding.item.id);
			    Tuple tuple = (Tuple)add.binding.item;
			    int value = ((IntBox)(tuple.fields[1])).value;
			    System.err.println("Add "+
					       (add.kind.data==ItemEventKind.EQDATA_KIND_LEASE_RENEW ? "renew" : 
						add.kind.data==ItemEventKind.EQDATA_KIND_NORMAL ? "normal" : ""+add.kind.data)+
					       " tuple "+tuple.id+": "+value);
			    System.err.println("- was "+(oldValue==null ? "null" : oldValue.toString()));
			} else if (event instanceof UpdateEvent) {
			    UpdateEvent upd = (UpdateEvent)event;
			    Tuple tuple = (Tuple)upd.item;
			    int value = ((IntBox)(tuple.fields[1])).value;
			    System.err.println("Update tuple "+tuple.id+": "+value);
			} else if (event instanceof DeleteEvent) {
			    DeleteEvent del = (DeleteEvent)event;
			    System.err.println("Delete tuple "+del.id);
			}
		    }
		}, 
						   null);
	    ItemData template = getItemTemplate();
	    EventPattern pattern = new EventPatternImpl();
	    pattern.initAsSimpleItemMonitor(template, false);
	    pattern.id = fact.getUnique();
	    session.addPattern(pattern);

	    int i;
	    for (i=0; i<update_intervals.length+1; i++) {
		if(i>0) {
		    System.err.println("Sleep for "+update_intervals[i-1]);
		    try {
			Thread.sleep(1000*update_intervals[i-1]);
		    } catch (InterruptedException e) {
			System.err.println("Sleep interrupted!");
		    }
		}
		ItemData item = getItemValue(id, i);
		Lease lease = new LeaseImpl();
		lease.initFromTimeToLive(LEASE_TIME_S);
		ds.addItem(item, LockType.LOCK_NONE, false,
			   false, lease);
	    }
	    System.err.println("Done");
	    try {
		Thread.sleep(1000*10);
	    } catch (InterruptedException e) {
		System.err.println("Sleep interrupted!");
	    }
	    System.err.println("Exit");	    
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
    }
    static ItemData getItemValue(GUID id, int i) {
	Tuple tuple = new TupleImpl(new StringBoxImpl("ItemLeaseTest"),
				    new IntBoxImpl(i));
	tuple.id = id;
	return tuple;
    }
    static ItemData getItemTemplate() {
	Tuple tuple = new TupleImpl(new StringBoxImpl("ItemLeaseTest"),
				    null);
	return tuple;
    }
}
//EOF
