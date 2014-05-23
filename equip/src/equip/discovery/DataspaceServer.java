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
/* DataspaceServer
 * discoverable dataspace server
 * 
 * Chris Greenhalgh, 5/12/2002
 */


package equip.discovery;

import equip.runtime.*;
import equip.data.*;

public class DataspaceServer {
  public static void main(String args[]) {
    if (args.length!=1 && args.length!=2) {
      System.err.println("Usage: equip.discovery.DataspaceServer <equip-url> [<group>]");
      System.exit(-1);
    }
    System.err.println("Starting data server " + args[0] + "...");
    DataProxy dataspace = DataManager.getInstance().getDataspace(args[0], 
					   DataManager.DATASPACE_SERVER,
					   true);
	equip.net.ServerURL surl = new equip.net.ServerURL(((Server)dataspace).getMoniker());
	System.err.println("SURL = "+surl.getURL());
	String dataspaceName = surl.getURL();
    System.err.println("Starting discovery server...");

    ServerDiscoveryInfo [] servers = new ServerDiscoveryInfo[1];
    servers[0] = new ServerDiscoveryInfoImpl();
    servers[0].serviceTypes = new String [1];
    servers[0].serviceTypes[0] = "equip.data.DataProxy:2.0";
    /* should be this but that means my new version of data...
       DATASPACE_SERVICE_TYPE.value */
    System.err.println("- serviceType = "+servers[0].serviceTypes[0]);
    servers[0].groups = new String [1];
    if (args.length>1)
	servers[0].groups[0] = args[1];
    else
	servers[0].groups[0] = DISCOVERY_GROUP_DEFAULT.value;
    System.err.println("- group = "+servers[0].groups[0]);
    servers[0].urls = new String [1];
    servers[0].urls[0] = dataspaceName; //args[0];
    System.err.println("- url = "+servers[0].urls[0]);
    
    // go...
    DiscoveryServerAgent agent = new DiscoveryServerAgentImpl();
    agent.startDefault(servers);
    
    System.err.println("OK");
  }
}

/* EOF */
