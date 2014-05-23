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
package equip.data.benchmark;

import equip.runtime.*;
import equip.net.*;
import equip.data.*;
import java.util.Date;
import java.util.Vector;
import java.io.*;

public class FileWriteTest {
    public static void main(String [] args) {
	try {
	    System.err.println("Create GUIDFactory");
	    // create local DS server
	    GUIDFactory guids = new GUIDFactoryImpl();
	    
	    int buffered;
	    for (buffered=1; buffered>=0; buffered--) {
		System.err.println((buffered==1)?"===Buffered===":
				   "===Unbuffered===");

		int target = 1000;
		for (target=1000; target<=1000; target+=1000) {
		    int size = 100;
		    for (size=0; size<=200; size+=100) {
			System.err.print("Write "+target+" items, array size "+
				     size+": ");
			FileOutputStream fout = new FileOutputStream("test.eqaser");
			equip.runtime.ObjectOutputStream oout;
			if(buffered==1)
			    oout = new equip.runtime.ObjectOutputStream
				(new BufferedOutputStream(fout));
			else
			    oout = new equip.runtime.ObjectOutputStream(fout);

			TestItem item = new TestItemImpl();
			item.id = guids.getUnique();
			item.data = new int [size];

			int i;
			Date start = new Date();
			for (i=0; i<target; i++) {
			    oout.writeObject(item);
			}
			Date end = new Date();
			long elapsed = end.getTime()-start.getTime();
			System.err.println("Elapsed ms = "+elapsed);
			
			System.err.print("Read "+target+" items, array size "+
					   size+": ");
			FileInputStream fin = new FileInputStream("test.eqaser");
			equip.runtime.ObjectInputStream oin;
			if(buffered==1)
			    oin = new equip.runtime.ObjectInputStream
				(new BufferedInputStream(fin));
			else
			    oin = new equip.runtime.ObjectInputStream(fin);
			
			start = new Date();
			for (i=0; i<target; i++) {
			    oin.readObject();
			}
			end = new Date();
			elapsed = end.getTime()-start.getTime();
			System.err.println("Elapsed ms = "+elapsed);
			
		    }
		}
	    }
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	    e.printStackTrace(System.err);
	}
	System.err.println("DONE");
    }
}
