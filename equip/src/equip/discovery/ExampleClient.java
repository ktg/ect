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
/* ExampleClient.java
 * 
 * Chris Greenhalgh, 5/12/2002
 */


package equip.discovery;

import equip.runtime.*;
import java.net.*;
import java.util.Vector;
import java.util.Hashtable;
import java.io.*;

public class ExampleClient {

    /** sample main - as a helper for another server */
    public static void main(String [] args) {
	if (args.length>1) {
	    System.err.println("Usage: equip.discovery.ExampleClient [<group>]");
	    System.exit(-1);
	}
	DiscoveryEventListener listen = new DiscoveryEventListenerImpl() {
		public void discoveryEvent(DiscoveryClientAgent agent, 
					   String url) {
		    System.err.println("- Discovered: "+url);
		    printAll(agent);
		}
		public void discoveryRemoveEvent(DiscoveryClientAgent agent, 
					   String url) {
		    System.err.println("- Lost: "+url);
		    printAll(agent);
		}
		void printAll(DiscoveryClientAgent agent) {
		    String [] urls = agent.getKnownServers();
		    int i;
		    System.err.println("Known servers:");
		    for (i=0; urls!=null && i<urls.length; i++)
			System.err.println("  "+urls[i]);
		}
	    };
		
	String serviceType = "equip.data.DataProxy:2.0";
	/* should be this but that means my new version of data...
	   DATASPACE_SERVICE_TYPE.value */
	String group = DISCOVERY_GROUP_DEFAULT.value;
	if (args.length>0)
	    group = args[0];
	// go...
	DiscoveryClientAgent agent = new DiscoveryClientAgentImpl();
	agent.startDefault(listen, 
			   new String[] { serviceType },
			   new String[] { group });
    }

} /* class DiscoveryClientAgent */

/* EOF */
