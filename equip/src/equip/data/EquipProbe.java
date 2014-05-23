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

import java.io.*;
import java.net.*;

import equip.runtime.*;
import equip.data.*;

/** a simple EQUIP network probe to help with mobile networking tests.
 * creates/join a dataspace using TCP/JCP according to URL, monitors heartbeat tuples
 * and publishes its own.
 */
public class EquipProbe {
    /** dataspace */
    DataProxy dataspace;

    /** tuple type name */
    public static final String typeName = "equip.data.EquipProbe.probe";

    /** default interval, ms */
    public static final int INTERVAL_MS = 1000;

    /** cons on given multicast group - runs immediately */
    public EquipProbe(final String dsurl, final boolean serverFlag) {
	
	final GUIDFactory guids = (GUIDFactory)SingletonManager.get(GUIDFactoryImpl.class.getName());
	GUID responsible = guids.getUnique();
	// long timeout
	responsible.time_s = 1;

	final DataProxy dataspace = 
	    DataManager.getInstance().getDataspace(dsurl, 
						   (serverFlag ? 
						    DataManager.DATASPACE_SERVER : 
						    DataManager.DATASPACE_CLIENT),
						   true, true, 
						   responsible);

	DataSession session = dataspace.createSession(new DataCallback() {
		public void notify(equip.data.Event event, 
				   equip.data.EventPattern pattern, 
				   boolean patternDeleted, 
				   equip.data.DataSession session,
				   equip.runtime.ValueBase closure) {
		    Tuple data = null;
		    if (event instanceof AddEvent) {
			data = (Tuple)(((AddEvent)event).binding.item);
		    } else if (event instanceof UpdateEvent) {
			data = (Tuple)(((UpdateEvent)event).item);
		    }
		    if (data!=null) {
			System.out.println("["+System.currentTimeMillis()+"] EquipProbe receivedDataItem id "+
					   data.id+" sentAt "+((StringBox)data.fields[1]).value);
		    }
		}},
						      null);
	
	Tuple template = new TupleImpl(new StringBoxImpl(typeName));
	EventPattern pattern = new EventPatternImpl();
	pattern.id = guids.getUnique();
	pattern.initAsSimpleItemMonitor(template, false);
	session.addPattern(pattern);

	// send thread
	new Thread() {
	    public void run() {
		try {
		    GUID id = guids.getUnique();
		    boolean createFlag = true;
		    while(true) {
			long now = System.currentTimeMillis();
			Tuple data = new TupleImpl(new StringBoxImpl(typeName),
						   new StringBoxImpl(new Long(now).toString()));
			data.id = id;
			if (createFlag) {
			    dataspace.addItem(data, LockType.LOCK_NONE, true, false, null);
			    createFlag = false;
			}
			else
			    dataspace.updateItem(data, false, true);
			Thread.sleep(INTERVAL_MS);
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in UnicastProbe sending thread: "+e);
		    e.printStackTrace(System.err);
		}
		System.err.println("UnicastProbe sending thread exited");
	    }}.start();
    }

    /** test main: usage java equip.data.EquipProbe &lt;equipurl&gt; s(erver)|c(lient) */

    public static void main(String [] args) {
	if (args.length!=2 || (args[1].charAt(0)!='s' && args[1].charAt(0)!='c')) {
	    System.err.println("Usage:  java equip.data.EquipProbe <equipurl> s(erver)|c(lient)");
	    System.exit(-1);
	}
	try {
	    new EquipProbe(args[0], args[1].charAt(0)=='s');
	} catch (Exception e) {
	    System.err.println("ERROR creating EquipProbe: "+e);
	    e.printStackTrace(System.err);
	}
    }
}
// EOF
