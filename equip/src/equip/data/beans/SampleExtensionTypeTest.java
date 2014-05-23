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
/* Example of extending from TupleImpl for non-IDLd but readable classes. 
 * Chris Greenhalgh 2004-02-09
 */


package equip.data.beans;

import equip.runtime.*;
import equip.data.*;

/** Example of extending from TupleImpl for non-IDLd but readable classes
 * with DataspaceBean API.
 */
public class SampleExtensionTypeTest  implements DataspaceEventListener {
    /* lifecycle */
    public SampleExtensionTypeTest() {
    }
    /** main - run to see usage */
    public static void main(String [] args) {
	if (args.length!=3) {
	    System.err.println("Usage: equip.data.beans.SimpleExtensionTypeTest <dataspace> <x> <y>");
	    System.exit(-1);
	}
	try {
	    // parse args
	    String dataspaceName = args[0];
	    int x = new Integer(args[1]).intValue();
	    int y = new Integer(args[2]).intValue();

	    // run class instance
	    new SampleExtensionTypeTest(dataspaceName, x, y);
	}
	catch(Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
    }
    protected SampleExtensionTypeTest(String dataspaceName, int x, int y) {
	try {

	    //------------------------------------------------------------
	    // PUBLISHER or SUBSCRIBER

	    // dataspace client
	    DataspaceBean dataspace = new DataspaceBean();
	    dataspace.setDataspaceUrl(dataspaceName);
	    if (!dataspace.isActive()) {
		System.err.println("Unable to activate dataspace client for "+
				   dataspaceName);
		System.exit(-1);
	    }

	    //------------------------------------------------------------
	    // SUBSCRIBER only
	    
	    // make a template we are interested in - TupleImpl subclass
	    SampleExtensionTypeImpl myTemplate = 
		new SampleExtensionTypeImpl();

	    // add a listener, non-local, callback on this
	    dataspace.addDataspaceEventListener(myTemplate, false,
						this);
	  
	    //------------------------------------------------------------
	    // PUBLISHER only

	    // make an item
	    SampleExtensionTypeImpl myItem = 
		new SampleExtensionTypeImpl();
	    myItem.id = dataspace.allocateId();
	    // box by hand in Java
	    myItem.setX(new Integer(x));
	    myItem.setY(new Integer(y));
	    dataspace.add(myItem);

	    System.out.println("Added "+myItem+" "+myItem.id+
			       ": x="+myItem.getX()+", y="+myItem.getY());

	    // wait...
	    Thread.sleep(3000);

	    // update - new item value
	    SampleExtensionTypeImpl myNewItem =
		(SampleExtensionTypeImpl)myItem.clone();
	    myNewItem.setX(new Integer(x+1));
	    myNewItem.setY(new Integer(y+1));
	    dataspace.update(myNewItem);

	    System.out.println("Updated to "+myNewItem+" "+myNewItem.id+
			       ": x="+myNewItem.getX()+
			       ", y="+myNewItem.getY());

	    // wait...
	    Thread.sleep(3000);

	    System.out.println("Added "+myItem+": x="+myItem.getX()+", y="+
			       myItem.getY());

	    // delete
	    dataspace.delete(myItem.id);

	    System.out.println("Deleted "+myItem.id);

	    //------------------------------------------------------------

	    // wait...
	    Thread.sleep(10000);

	    System.out.println("Done");
	}
	catch(Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}	    
    }

    //------------------------------------------------------------
    // SUBSCRIBER only

    public void dataspaceEvent(DataspaceEvent dsEvent) {
	System.out.println("Received event:");
	SampleExtensionTypeImpl myItem;
	// Add item?
	myItem = (SampleExtensionTypeImpl)dsEvent.getAddItem();
	if (myItem!=null) {
	    System.out.println("- added item "+myItem.id+
			       " at "+myItem.getX()+","+myItem.getY());
	}
	// update item?
	myItem = (SampleExtensionTypeImpl)dsEvent.getUpdateItem();
	if (myItem!=null) {
	    System.out.println("- updated item "+myItem.id+
			       " to "+myItem.getX()+","+myItem.getY());
	}
	// delete item?
	if (dsEvent.getDeleteId()!=null) {
	    myItem = (SampleExtensionTypeImpl)dsEvent.getOldValue();
	    System.out.println("- deleted item "+myItem.id+
			       " from "+myItem.getX()+","+myItem.getY());
	    
	}
    }
	
    //------------------------------------------------------------

} /* class Tuple */

/* EOF */
