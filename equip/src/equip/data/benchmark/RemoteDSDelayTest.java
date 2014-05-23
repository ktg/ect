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
/* RemoteDSDelayTest.java
 * chris greenhalgh 24 oct 2002
 * publisher updates a tuple at 10Hz, an async thread pushes changes to 
 * tuple space as fast as possible.
 * monitor pulls updates as fast as possible to determine delay.
 */
package equip.data.benchmark;

import equip.runtime.*;
import equip.net.*;
import equip.data.*;
import equip.data.beans.*;

import java.util.Date;


public class RemoteDSDelayTest {

    public static void main(String [] args) {
	if (args.length!=2) {
	    System.err.println("ERROR: usage: java equip.data.benchmark.RemoteDSDelayTest p|m <equip-ds-url>");
	    System.exit(-1);
	}
	boolean publisherFlag = false;
	if (args[0].equals("p")) {
	    publisherFlag = true;
	} else if (args[0].equals("m")) {
	} else {
	    System.err.println("ERROR: unknown mode: "+args[0]);
	    System.err.println
		("ERROR: usage: java equip.data.benchmark.RemoteDSDelayTest "+
		 "p|m <equip-ds-url>");
	    System.exit(-1);
	}	
	System.err.println("Create DataspaceBean");
	DataspaceBean dataspace = new DataspaceBean();
	System.err.println("Set dataspace url to "+args[1]);
	try {
	    dataspace.setDataspaceUrl(args[1]);
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	}
	if (!dataspace.isActive()) {
	    System.err.println("ERROR: Failed to activate!");
	    System.exit(-1);
	}
	
	GUID itemId=null;
	if (publisherFlag) {
	    System.err.println("Publishing at 100Hz...");

	    itemId = dataspace.allocateId();
	    try {
		dataspace.add(createItem(itemId));
	    } catch (Exception e) {
		System.err.println("ERROR: "+e);
	    }

	    ItemData [] itemHolder = new ItemData[1];

	    new PublisherThread(dataspace, itemHolder);

	    while (true) {
		try {
		    Thread.sleep(10);
		} catch (Exception e) {}
		synchronized(itemHolder) {
		    itemHolder[0] = createItem(itemId);
		    itemHolder.notify();
		}
	    }
	}
	else {
	    System.err.println("Monitoring...");
	    System.out.println("#sendtime seqno delay");
	    try {
		dataspace.addDataspaceEventListener
		    (createItemTemplate(),
		     false, 
		     new DataspaceEventListener() {
			     public void dataspaceEvent(DataspaceEvent event) {
				 long now = new Date().getTime();
				 try {
				     UpdateEvent upd = 
					 (UpdateEvent)(event.getEvent());
				     Tuple item = 
					 (Tuple)(upd.item);
				     int sequenceNo = ((IntBox)(item.fields[1])).value;
				     long sent = ((LongBox)(item.fields[2])).value;
				     int delayMs = (int)(now-sent);
				     System.out.println(""+sent+" "+sequenceNo+
							" "+delayMs);
				 } catch (Exception e) {
				     System.err.println("ERROR: "+e);
				     e.printStackTrace(System.err);
				 }
			     }
			 });
	    } catch (Exception e) {
		System.err.println("ERROR: "+e);
	    }
	    // monitor...
	    return;
	}
    }
    static class PublisherThread extends Thread {
	DataspaceBean dataspace;
	ItemData[] itemHolder;
	
	PublisherThread(DataspaceBean dataspace,
			ItemData[] itemHolder) {
	    this.dataspace = dataspace;
	    this.itemHolder = itemHolder;
	    start();
	}
	public void run() {
	    while (true) {
		ItemData item = null;
		synchronized(itemHolder) {
		    while (itemHolder[0]==null) {
			try {
			    itemHolder.wait();
			} catch (Exception e) {}
		    }
		    item = itemHolder[0];
		    itemHolder[0] = null;
		}
		try {
		    // unreliable
		    dataspace.update(item, false);
		} catch (Exception e) {
		    System.err.println("ERROR: "+e);
		}
	    }
	}
    }

    static int sequenceNo = 0;
    static ItemData createItem(GUID id) {
	ItemData item;
	item = new TupleImpl(new StringBoxImpl("equip.data.benchmark.RemoteDSDelayTest.data"),
			     new IntBoxImpl(sequenceNo++),
			     new LongBoxImpl(new Date().getTime()));
	item.id = id;
	return item;
    }
    static ItemData createItemTemplate() {
	ItemData item;
	item = new TupleImpl(new StringBoxImpl("equip.data.benchmark.RemoteDSDelayTest.data"), 
			     null,
			     null);
	return item;
    }
}
/* EOF */
