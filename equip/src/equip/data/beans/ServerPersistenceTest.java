/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
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
package equip.data.beans;

import equip.net.*;
import equip.runtime.*;
import equip.data.*;
import equip.data.beans.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/** equip.data.beans.ServerPersistenceTest.java
 * Chris Greenhalgh, 2004-05-11
 *
 * test app for server persistence.
 * configure a server for persistence, e.g. FileBackedMemoryDataStore
 * <pre>equip.data.DataManagerBrowser: t
 * equip_//128.243.22.74_9123/.dataStore1Name: HostDS
 * equip_//128.243.22.74_9123/.dataStore1Class: equip.data.FileBackedMemoryDataStore
 * HostDS.maxFlushIntervalS: 2
 * </pre>
 */

public class ServerPersistenceTest
{
    /***********************************************************************
     * main
     */

	public static void main (String [] args) 
	{
		if (args.length!=3 ||
			args[0].length()!=1 ||
			(args[0].charAt(0)!='1' &&
			args[0].charAt(0)!='2' &&
			args[0].charAt(0)!='3')) 
		{
			System.err.println("Usage: equip.data.beans.DataspaceBeanTupleTest m|s|a <name> <server-url>");
			System.err.println(" - 1 = monitor");
			System.err.println(" - 2 = create/update");
			System.err.println(" - 3 = delete");
			System.exit(-1);
		}
		System.out.println("ServerPersistenceTest...");
		new ServerPersistenceTest(args[0].charAt(0), args[1], args[2]);
	}
  
    /**********************************************************************
     * constructor/initilise
     */
    /** dataspace */
    DataspaceBean dataspace;

    /** ID with which we publish our own data item */
    private GUID publishId;

    /** name with which we publish/monitor data item(s) */
    private String name;

    /** replication/notification callback session */
    private DataSession session;

    /** pseudo-class name for our own kind of tuple */
    static final String myTupleClassName = "equip.data.beans.ServerPersistenceTest.Item";

	private Tuple tuple = null;

    /** constructor - does the business */
	private ServerPersistenceTest(char mode, String itemName, 
		String dataspaceName) 
	{
    
		try 
		{
			// create dataspace bean
			System.err.println("Create DataspaceBean");
			dataspace = new DataspaceBean();
			dataspace.setRetryConnect(true);
			// set URL, which will try to activate the bean
			System.err.println("Set dataspace url to "+dataspaceName+" (retry enabled)");
			try 
			{
				dataspace.setDataspaceUrl(dataspaceName);
			} 
			catch (Exception e) 
			{
				System.err.println("ERROR (DataspaceBeanTupleTest.<cons>): "+e);
			}
			// if not active, give up
			if (!dataspace.isActive()) 
			{
				System.err.println("ERROR: Failed to activate!");
				System.exit(-1);
			}
	    
			this.name = itemName;
	    
			System.err.println("Sync with DS...");

			// item add/delete/update callback(s)
			System.out.println("Create notification callback session...");
	    
			// Tuple as a template/pattern - just fill in the fields you need
			// to match
			equip.data.Tuple template = 
				new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
				new StringBoxImpl(name), null);
	    
			// add the event listener
			// master is a local pattern, i.e. does not cause replication 
			//   to this process from the server; others are not, i.e. do
			//   cause replication from the server, and so the pattern is
			//   global and not local.
			session = dataspace.addDataspaceEventListener
				(template, /*localFlag*/false, new EventHandler());
	    
			System.err.print("Wait for connect...");
			while (!dataspace.isConnected()) 
			{
				try 
				{
					System.err.print(".");
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {}
			}
			System.err.println("OK");

			// optional - synchronize with the server to be sure that we
			//   have received replicas of all matching items already known
			//   to the server.
			System.err.print("Sync...");
			dataspace.getDataProxy().waitForEvents(false /*i.e. non-local*/);
			System.err.println("OK");

			if (mode=='2') 
			{
				if (tuple==null) 
				{
					System.out.println("Create as 1 - tuple not found...");
		
					// new ID for the item that we publish
					publishId = dataspace.allocateId();
		
					// make a tuple
					equip.data.Tuple item1 = makeTuple(1);
		
					// publish in the dataspace 
					dataspace.addPersistent(item1, null);
					try 
					{
						System.err.println("Wait before update...");
						Thread.sleep(10000);
					} 
					catch (Exception e) {}
				} 

				int val = ((IntBox)(tuple.fields[2])).value+1;
				publishId = tuple.id;

				System.out.println("Update to "+val+" - tuple found...");
			
				// make a tuple
				equip.data.Tuple item1 = makeTuple(val);
		
				// publish in the dataspace 
				dataspace.addPersistent(item1, null);
				
				try 
				{
					System.err.println("Wait before delete...");
					Thread.sleep(10000);
				} 
				catch (Exception e) {}
			} 
			if (mode=='2' || mode=='3') 
			{
				if (tuple==null) 
				{
					System.out.println("Cannot delete - not found");
				} 
				else 
				{
					System.out.println("Try to delete...");

					dataspace.delete(tuple.id);
				}
			}
			// All set...
			System.out.println("Running...");
		}
		catch (DataspaceInactiveException e) 
		{
			System.err.println("ERROR: "+e);
			e.printStackTrace(System.err);
		}
	}

    /** make a new tuple value helper */
	private Tuple makeTuple(int c) 
	{
		// create a tuple;
		//   first field is pseudo-classname,
		//   second field is array [x position, y position]
		equip.data.Tuple item1 = 
			new equip.data.TupleImpl(new StringBoxImpl(myTupleClassName),
			new StringBoxImpl(name), 
			new IntBoxImpl(c));
	
		// also fill in tuple ID, and optionally tuple name
		item1.id = publishId;
		item1.name = name;

		return item1;
	}

    /**********************************************************************
     ** data service event listener.
     * 
     * Called by events matching item template ('item monitor')
     * as set up in constructor, i.e. add, update or delete.
     */
	private class EventHandler implements DataspaceEventListener
	{
		public void dataspaceEvent(DataspaceEvent event) 
		{
			Tuple t = (Tuple)event.getAddItem();
			if (t!=null) 
			{
				System.out.println("Found matching item "+t.id+", name "+
					t.fields[1]+", value "+((IntBox)t.fields[2]).value);
				if (tuple==null)
					tuple = t;
				else if (!t.id.equals(tuple.id))
					System.err.println("ERROR - different item: "+t.id+" vs "+tuple.id);
				else 
				{
					System.out.println("Item "+t.id+" updated to "+((IntBox)t.fields[2]).value);
					tuple = t;
				}
				return;
			} 
			t = (Tuple)event.getUpdateItem();
			if (t!=null) 
			{
				System.out.println("Item "+t.id+" updated to "+t.fields[2]);
				if (tuple==null)
					System.err.println("ERROR - we don't have an item");
				else if (!t.id.equals(tuple.id))
					System.err.println("ERROR - different item: "+t.id+" vs "+tuple.id);
				else
					tuple = t;
				return;
			}
			GUID id = event.getDeleteId();
			if (id!=null)
			{
				System.out.println("Deleting item "+id);
				if (tuple==null)
					System.err.println("ERROR - we don't have an item");
				else if (!id.equals(tuple.id))
					System.err.println("ERROR - different item: "+id+" vs "+tuple.id);
				else
					tuple = null;
				return;
			}											 
		}    
	}
}
//EOF
