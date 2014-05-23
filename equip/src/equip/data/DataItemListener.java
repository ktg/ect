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
package equip.data;

import equip.data.EquipConnector;

import equip.net.*;
import equip.runtime.ValueBase;

import java.io.*;

/**
 * listens for EQUIP events for CityMack data types
 *
 * inner class ItemCallback is default and should be extended
 *
 * @author Tom Rodden
 * @author Ian Taylor
 * @author Nick (sheep) Dalton
 * @author Ian MacColl
 *
 * @version $Revision: 1.1.1.1 $
 *
 * $Id: DataItemListener.java,v 1.1.1.1 2005/03/08 16:17:19 cgreenhalgh Exp $
 * 
 * $Log: DataItemListener.java,v $
 * Revision 1.1.1.1  2005/03/08 16:17:19  cgreenhalgh
 * From Nottingham CVS
 *
 * Revision 1.9  2005/03/08 14:36:39  cmg
 * added BSD license
 *
 * Revision 1.8  2003/10/22 15:00:42  cmg
 * added initial support for Leased items (requires synchronized clocks)
 *
 * Revision 1.5  2002/12/06 17:17:13  cmg
 * updated eqidl - abstract classes and methods and associated fixes
 *
 * Revision 1.4  2002/08/28 09:19:38  ianm
 * added pattern state and cleanup method to delete listener
 *
 * Revision 1.3  2002/02/10 20:31:47  ianm
 * turned debug trace off and corrected usage message
 *
 * Revision 1.2  2001/11/15 22:46:17  ianm
 * remove module mutual dependency
 *
 * Revision 1.1  2001/10/29 14:37:19  ianm
 * initial version
 *
 */

public class DataItemListener {

    /**
     * flag to enable trace output
     */
    protected static boolean DEBUG = false;

    /**
     * connection to equip (param only?)
     */
    EquipConnector ec = null;

    /**
     * listener data session (for deletion)
     */
    DataSession ds = null;

    /**
     * event pattern ID (for deletion)
     */
    GUID epID = null;

    /**
     * connects to equip and provides default explanation handler
     *
     * @param url EQUIP data service URL
     */
    public DataItemListener(String url) {
        this(new EquipConnector(url),null,null);
    }

    /**
     * installs equip connector and callback handler for 
     *
     * @param ec EQUIP connection to install
     * @param dc callback handler to install
     * @param id item template
     */
    public DataItemListener(EquipConnector ec, DataCallback dc, ItemData id) {
        this.ec = ec;
        // @@@ check ec != null?
        
        if (id == null)
            id = new ItemDataImpl();
        // @@@ fail rather than matching everything?

        
        if (dc == null)
            dc = new ItemCallback();
        
       // listen for Item events
       EventPattern ep = new EventPatternImpl();
       synchronized(ec) { // necessary?
           ds = ec.dataservice.createSession(dc, null);
           ep.id=ec.idFactory.getUnique();
       }
       epID = ep.id;
       ep.initAsSimpleItemMonitor(id, false);
       // ep.initAsSimpleItemMonitor(id, false);
       ds.addPattern(ep);
       if (DEBUG) System.err.println("ITEMLISTENER listening"); 	
    }

    /**
     * deletes pattern and session associated with this listener
     */
    public void cleanup() {
        if (ds != null) {
            ds.deletePattern(epID);
            synchronized(ec) { // necessary?
                ec.dataservice.deleteSession(ds);
            }
            // do we need to delete the ItemData as well?
        }
    }

    /**
     * default callback handler for Item events
     */
    protected class ItemCallback extends DataCallback {
        /*
         * handles Item events by printing to System.out
         */
        public void notify(equip.data.Event event, EventPattern pattern,
                    boolean patternDeleted,
                    DataSession session,
                    ValueBase closure) {
            if (DEBUG) System.err.println("NOTIFY " + event);

            // handle add
            if (event instanceof AddEvent) {
                AddEvent add = (AddEvent)event;
                if (DEBUG) System.err.println("NOTIFY add " + add);
            }

            // handle delete
            if (event instanceof DeleteEvent) {
                DeleteEvent del = (DeleteEvent)event;
                if (DEBUG) System.err.println("NOTIFY delete " + del);
            }

            // handle update
            if (event instanceof UpdateEvent) {
                UpdateEvent upd = (UpdateEvent)event;
                if (DEBUG) System.err.println("NOTIFY update " + upd);
            }

           if (DEBUG) System.err.println("NOTIFY notified " + event);
        }
    }
    public static void main(String[] args) {
        if (args.length!=1) {
            System.err.println("Usage: DataItemListener  <server-url>");
            System.exit(-1);
        }
        DataItemListener itemListen = new DataItemListener(args[0]);
    } // main

} // DataItemListener
