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
package equip.net;

import java.io.*;
import java.net.*;

/** a program to monitor and echo a file on the filesystem to stdout
 * with additional timestamps, e.g. when the file lacks accurate
 * timestamps, such as Orinico wifi client logs.
 */
public class LogFileEcho {
    /** file check interval ms */
    public static final int CHECK_INTERVAL_MS = 100;

    /** buffer size */
    public static final int BUFFER_SIZE = 10000;

    /** monitor given file, echo with timestamps with given id as first item
     * after timestamp on line. */
    public LogFileEcho(final File file, final String id) throws IOException {
	file.length(); // check
	new Thread() {
	    public void run() {
		try {
		    byte [] buffer = new byte[BUFFER_SIZE];
		    StringBuffer sbuf = new StringBuffer();
		    int bufferSize=0, bufferPos=0;
		    long gotSize = 0;
		    // we buffer stuff ourself to avoid reading past the
		    // current end of file
		    while(true) {
			while (bufferPos < bufferSize) {
			    // interpret bytes as chars, assuming ascii
			    char c = (char)buffer[bufferPos++];
			    if (c!='\n') 
				sbuf.append(c);
			    else {
				// output
				System.out.println("["+System.currentTimeMillis()+
						   "] "+id+" "+sbuf.toString());
				sbuf = new StringBuffer();
			    }
			}
			// buffer exhausted - check available
			long available = 0;
			do {
			    available = file.length()-gotSize;
			    if(available==0)
				Thread.sleep(CHECK_INTERVAL_MS);
			}
			while (available == 0);
			
			FileInputStream fins = null;
			do {
			    try {
				fins = new FileInputStream(file);
			    } catch (FileNotFoundException e) {
				// may be concurrent user - just wait a minute
				System.err.println("Note: LogFileEcho retrying");
				Thread.sleep(CHECK_INTERVAL_MS);
				continue;
			    }
			} while(false);
			fins.skip(gotSize);
			long size = (int)available;
			if (size > BUFFER_SIZE)
			    size = BUFFER_SIZE;
			bufferSize = fins.read(buffer, 0, (int)size);
			fins.close();
			gotSize += bufferSize;
			bufferPos = 0;
		    }
		} catch (Exception e) {
		    System.err.println("ERROR in LogFileEcho "+id+": "+e);
		    e.printStackTrace(System.err);
		}
	    }
	}.start();
    }
    /** main: usage: java equip.net.LogFileEcho filename id */
    public static void main(String [] args) {
	if (args.length!=2) {
	    System.err.println("Usage: java equip.net.LogFileEcho filename id");
	    System.exit(-1);
	} 
	try {
	    new LogFileEcho(new File(args[0]), args[1]);
	} catch (Exception e) {
	    System.err.println("Error creating LogFileEcho: "+e);
	    e.printStackTrace(System.err);
	}
    }
}
// EOF
