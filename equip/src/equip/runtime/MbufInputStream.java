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
/* MbufInputStream.java
   Chris Greenhalgh
   19/9/2001
*/
package equip.runtime;

import java.io.*;

/** Concrete Message buffer ({@link equip.runtime.Mbuf}) input stream. */
public class MbufInputStream extends InputStream {
  /** Default no-arg constructor (use {@link #reset} to initialise 
   * for reading). */
  public MbufInputStream() {
    buf = null;
    pos = 0;
  }
  
  /** input stream API (return -1 for end of stream) */
  public int read() throws IOException {
    byte [] b = new byte[1];
    if (read2(b,1,1)<1)
      return -1;
    return ((int)b[0]) & 0xff;
  }
  
  /** reset/set new linked list of message buffers to read from */
  public void reset(Mbuf first) {
    buf = first;
    pos = buf.getHeaderSize();
    if (debug)
	System.err.println("MbufInputStream.reset pos="+pos+
			   ", used="+buf.getUsed());
  }
  /** internal implementation of {@link #read} */
  public int read2(byte []cbuf, int minSize, int maxSize) {
    if (debug)
	System.err.println("MbufInputStream.read2 minSize="+minSize+
			   ", maxSize="+maxSize+", pos="+pos+", used="+
			   (buf!=null ? buf.getUsed() : 0));

    int cnt = 0;
    while (cnt < maxSize) {
      if (buf==null) {
        if (debug)
	    System.err.println("MbufInputStream.read2 failed, cnt="+cnt+
			       ", buf=null");
	return cnt;
      }
      if (pos >= buf.getUsed()) {
	buf = buf.getNext();
	pos = 0;
	if (buf!=null)
	  pos = buf.getHeaderSize();
	else {
	  if (debug)
	      System.err.println("MbufInputStream.read2 failed, cnt="+cnt+
				 ", buf.getNext()=null");
	  // run out
	  return cnt;
	}
	if (debug)
	    System.err.println("MbufInputStream.read2 next buf, pos="+
			       pos+", used="+buf.getUsed());
	// recheck non-null & used
	continue;
      }
      
      int num = maxSize-cnt;
      if (num > buf.getUsed()-pos)
	num = buf.getUsed()-pos;
    
      int j;
      byte [] bbuf = buf.getBuf();
      for (j=0; j<num; j++)
	cbuf[cnt+j] = bbuf[pos+j];
      pos += num;
      cnt += num;
    }
    return cnt;
  }
    
  // instance data 
  protected Mbuf buf;
  protected int pos;

  private boolean debug = false;
};
