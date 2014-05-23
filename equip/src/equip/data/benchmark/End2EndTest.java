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
/* End2EndTest.java
 * chris greenhalgh 11 nov 2002
 * based on RemoteDSDelayTest.java
 * publish updates 'as fast as possible' and monitor received
 * (remote) updates per second and delay.
 * for comparison with TSpaces and JavaSpaces.
 */
/*
  JavSpaces (jini 1.2.1)
  java -jar -Djava.security.policy=/dev/TupleSpaces/jini1_2_1/policy/policy.all -Djava.rmi.server.codebase=file:/dev/TupleSpaces/jini1_2_1/lib/outrigger-dl.jar transient-outrigger.jar

  TSpaces: 
  set TSPACES_HOME=c:\dev\TupleSpaces\tspaces
  set CLASSPATH=%TSPACES_HOME%\lib\tspaces.jar;%CLASSPATH%
  set CLASSPATH=%TSPACES_HOME%\lib\tspaces_client.jar;%CLASSPATH%
  set CLASSPATH=%TSPACES_HOME%\classes;%CLASSPATH%

  server: java  com.ibm.tspaces.server.TSServer
  slace: java TspaceEnd2EndTest s localhost > test1.out
  master: java TspaceEnd2EndTest m localhost

  cat test1.out | awk '{ if (active && donefirst==0) { t1=$1; s1=$2; c=0; donefirst=1; } if (active) { lt=$1; ls=$2; sumd=sumd+$3; c++; } if ($1=="#sendtime") active=1; } END { printf "%d updates out of %d, %f/second, ave delay = %f\n", c, ls+1-s1, 1000*(c-1)/(lt-t1), sumd/c; }'

  tspaces laptop 1763 updates out of 1763, 156.538735/second, ave delay = 5.226319
  equip laptop 6511 updates out of 6511, 558.990211/second, ave delay = 25.713715
  javaspaces degas/pc 368 updates out of 667, 16.942111/second, ave delay = 361.084239
 */
package equip.data.benchmark;

import equip.runtime.*;
import equip.net.*;
import equip.data.*;
import equip.data.beans.*;

import java.util.Date;


public class End2EndTest {

    public static void main(String [] args) {
	if (args.length!=3 && args.length!=4) {
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url> [<max-rate>]");
	    System.exit(-1);
	}
	boolean publisherFlag = false;
	boolean subscriberFlag = false;
	if (args[0].equals("m")) {
	    publisherFlag = true;
	} else if (args[0].equals("s")) {
	    subscriberFlag = true;
	} else if (args[0].equals("b")) {
	    publisherFlag = true;
	    subscriberFlag = true;	    
	} else {
	    System.err.println("ERROR: unknown mode: "+args[0]);
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url>");
	    System.exit(-1);
	}	
	double reliableFraction = 0.0;
	try {
	    reliableFraction = 0.01*new Double(args[1]).doubleValue();
	} catch (Exception e) {
	    System.err.println("ERROR: parsing reliability % ("+
			       args[1]+"): "+e);
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url>");
	    System.exit(-1);
	}
	if (reliableFraction<0 || reliableFraction>1) {
	    System.err.println("ERROR: reliability % out of range ("+
			       reliableFraction*100);
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url> [<max-rate>]");
	    System.exit(-1);
	}
	double maxRate = 0.0;
	try {
	    if (args.length>3)
		maxRate = new Double(args[3]).doubleValue();
	} catch (Exception e) {
	    System.err.println("ERROR: parsing reliability % ("+
			       args[3]+"): "+e);
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url> [<max-rate>]");
	    System.exit(-1);
	}
	if (maxRate<0) {
	    System.err.println("ERROR: max rate out of range ("+
			       maxRate+")");
	    System.err.println("ERROR: usage: java equip.data.benchmark.End2EndTest m(aster)|s(lave)|b(oth) <reliable%> <equip-ds-url> [<max-rate>]");
	    System.exit(-1);
	}

	// go
	new End2EndTest(publisherFlag, subscriberFlag, args[2],
			reliableFraction, maxRate);
    }
    public End2EndTest(boolean publisherFlag, boolean subscriberFlag,
		       String dsName, double reliableFraction,
		       double maxRate) {
	System.err.println("Create DataspaceBean");
	DataspaceBean dataspace = new DataspaceBean();
	System.err.println("Set dataspace url to "+dsName);
	try {
	    dataspace.setDataspaceUrl(dsName);
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	}
	if (!dataspace.isActive()) {
	    System.err.println("ERROR: Failed to activate!");
	    System.exit(-1);
	}
	
	GUID itemId=null;
	if (publisherFlag) {
	    System.err.println("Publishing...");

	    itemId = dataspace.allocateId();
	    try {
		dataspace.add(createItem(itemId));
	    } catch (Exception e) {
		System.err.println("ERROR: "+e);
	    }

	    new PublisherThread(dataspace, itemId, reliableFraction, maxRate);
	}
	if (subscriberFlag) {
	    subscribe(dataspace);
	}
    }
    public void subscribe(DataspaceBean dataspace) {
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
				 System.err.println("ERROR in subscribe: "+e);
				 e.printStackTrace(System.err);
			     }
			 }
		     });
	} catch (Exception e) {
	    System.err.println("ERROR in subscribe: "+e);
	}
	// monitor...
	return;
    }
    class PublisherThread extends Thread {
	DataspaceBean dataspace;
	GUID itemId;
	double reliableFraction;
	double relCount;
	double count;
	double maxRate;
	PublisherThread(DataspaceBean dataspace,
			GUID id, double reliableFraction, double maxRate) {
	    this.dataspace = dataspace;
	    this.itemId = id;
	    this.reliableFraction = reliableFraction;
	    this.maxRate = maxRate;
	    start();
	}
	public void run() {
	    DataProxyImpl proxy = (DataProxyImpl)dataspace.getDataProxy();
	    double nextUpdateTime = 0.001*new Date().getTime();
	    double minUpdateInterval = 0;
	    if (maxRate>0)
		minUpdateInterval = 1/maxRate;
	    while (true) {
		nextUpdateTime = nextUpdateTime+minUpdateInterval;
		double now = 0.001*new Date().getTime();
		if (now < nextUpdateTime) {
		    try {
			Thread.sleep((int)(0.999+1000*(nextUpdateTime-now)));
		    } catch (Exception e) {}
		}
		// wait for space in outbound queue?!
		// nasty hack for benchmarking purposes...
		while (proxy.getUnsentEvents()>0) {
		    try {
			//System.err.print(".");
			Thread.sleep(1);
		    } catch(Exception e) {}
		}
		ItemData item = createItem(itemId);
		try {
		    count += 1;
		    boolean reliable = relCount/count < reliableFraction;
		    // unreliable
		    dataspace.update(item, reliable);
		} catch (Exception e) {
		    System.err.println("ERROR: "+e);
		}
		try {
		    // yield
		    Thread.sleep(0);
		} catch(Exception e) {}
	    }
	}
    }

    int sequenceNo = 0;
    ItemData createItem(GUID id) {
	ItemData item;
	item = new TupleImpl(new StringBoxImpl("equip.data.benchmark.End2EndTest.data"),
			     new IntBoxImpl(sequenceNo++),
			     new LongBoxImpl(new Date().getTime()));
	item.id = id;
	return item;
    }
    static ItemData createItemTemplate() {
	ItemData item;
	item = new TupleImpl(new StringBoxImpl("equip.data.benchmark.End2EndTest.data"), 
			     null,
			     null);
	return item;
    }
}
/* EOF */
