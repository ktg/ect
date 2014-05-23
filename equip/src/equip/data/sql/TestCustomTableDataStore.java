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
package equip.data.sql;

import equip.data.*;
import equip.runtime.*;
import equip.config.ConfigManager;
import equip.config.ConfigManagerImpl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

import java.sql.*;

/** test app for {@link CustomTableDataStore}, when configured as per javadocs.
 *
 * @author Chris Greenhalgh, 2003-10-27
 */
public class TestCustomTableDataStore {
    /** usage: java equip.data.sql.TestCustomTableDataStore &lt;equipurl&gt;
     */
    public static void main(String [] args) {
	if (args.length!=3) {
	    System.err.println("usage: java equip.data.sql.TestCustomTableDataStore <equipurl> <seq> <value>");
	    System.err.println("where dataspace server so named has CustomTableDataStore configured as per javadocs");
	    System.exit(-1);
	}
	try {
	    System.err.println("Join dataspace "+args[0]+"...");
	    DataProxy dataspace = DataManager.getInstance().getDataspace(args[0], DataManager.DATASPACE_CLIENT, true);

	    GUIDFactory guids = (GUIDFactory)SingletonManager.get(equip.data.GUIDFactoryImpl.class.getName());
	    GUID id = guids.getUnique();
	    
	    Thread.sleep(5000);

	    int sequence = new Integer(args[1]).intValue();
	    int value = new Integer(args[2]).intValue();

	    // create suitable data item
	    System.err.println("Create new item "+id+" seq="+sequence+" value="+value);
	    Tuple tuple = makeTuple(id, sequence, value);

	    dataspace.addItem(tuple, LockType.LOCK_NONE, false, false, null);
	    
	    Thread.sleep(5000);
	    
	    // update it
	    System.err.println("Update to seq="+(sequence+1)+" value="+(value+1));
	    tuple = makeTuple(id, sequence+1, value+1);
	    dataspace.updateItem(tuple, false, true);

	    Thread.sleep(5000);

	    // delete it
	    System.err.println("Delete item");
	    dataspace.deleteItem(id, false);

	    Thread.sleep(5000);

	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
    }
    static Tuple makeTuple(GUID id, int sequence, int value) {
	Tuple tuple = new TupleImpl(new StringBoxImpl("equip.data.sql.CustomTable.FOO"),
				new IntBoxImpl(sequence),
				new IntBoxImpl(value));
	
	tuple.id = id;
	return tuple;
    }
}
//EOF




