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

import equip.config.ConfigManager;

import equip.runtime.*;
import equip.net.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
/** test data proxy termination via Dataspace manager. */
public class DataProxyTerminateTest {
    public static final int ITERATIONS = 20;
    /** usage: equip.data.DataProxyTerminateTest <equipurl> */
    public static void main(String [] args) {
	if (args.length!=1) {
	    System.err.println("Usage: equip.data.DataProxyTerminateTest <equipurl>");
	    System.exit(-1);
	}
	String dsurl = args[0];
	
	DataManager mgr = DataManager.getInstance();

	int i;
	for (i=0; i<ITERATIONS; i++) {

	    System.err.println("Create client DS for "+dsurl+", activate ("+i+")");
	    DataProxy ds = mgr.getDataspace(dsurl, DataManager.DATASPACE_CLIENT,
					    true, false, null);
	    try { 
		Thread.sleep(4000);
	    } catch (InterruptedException e) {}
	    
	    System.err.println("release");
	    mgr.releaseDataspace(ds);
	    ds = null;
	    try { 
		Thread.sleep(4000);
	    } catch (InterruptedException e) {}
	    
	    System.err.println("Create client DS for "+dsurl+", activate async ("+i+")");
	    ds = mgr.getDataspace(dsurl, DataManager.DATASPACE_CLIENT,
				  true, true, null);
	    try { 
		Thread.sleep(4000);
	    } catch (InterruptedException e) {}
	    
	    System.err.println("release");
	    mgr.releaseDataspace(ds);
	    try { 
		Thread.sleep(4000);
	    } catch (InterruptedException e) {}
	}
	System.err.println("done");
    }
}
//EOF
