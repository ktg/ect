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
/* MbufOutputStream.h
   Chris Greenhalgh
   19/9/2001
*/
package equip.runtime;

import java.io.*;

/** Concrete message buffer ({@link equip.runtime.Mbuf}) output stream */
public class MbufOutputStream extends OutputStream {
  /** initialise to create {@link Mbuf}s with default size and no header */
  public MbufOutputStream() {
    mbufSize = Mbuf.DEFAULT_SIZE;
    mbufHeaderSize = 0;
    current = null;
    pos = 0;
  }
  /** initialise to create {@link Mbuf}s with given size and header */
  public MbufOutputStream(int mbufSize, int mbufHeaderSize) {
    this.mbufSize = mbufSize;
    this.mbufHeaderSize = mbufHeaderSize;
    current = null;
    pos = 0;
  }

  /** OutputStream API */
  public void write(int bin) throws IOException {
    byte [] b = new byte[1];
    b[0] = (byte)bin;
    if (write2(b,1)<1)
      throw new IOException();
  }
  
  /** internal implementation of {@link #write} */
  public int write2(byte[] cbuf, int size) {
    if (size==0)
      return 0;
    if (first==null) {
      first = current = new Mbuf(mbufSize, mbufHeaderSize);
      pos = mbufHeaderSize;
    }
    int cnt = 0;
    while (cnt<size) {
      int num = size-cnt;
      if (num > current.getSize()-pos)
	num = current.getSize()-pos;
      if (num==0) {
	// new buf
	Mbuf next = new Mbuf(mbufSize, mbufHeaderSize);
	current.setNext(next);
	// no add ref
	current = next;
	pos = mbufHeaderSize;
	continue;
      }
      byte [] bbuf = current.getBuf();
      int j;
      for (j=0; j<num; j++)
	bbuf[pos+j] = cbuf[cnt+j];
      pos += num;
      cnt += num;
      current.setUsed(pos);
    }
    return cnt;
  }    
  /** OutputStream API */
  public void flush()
  {}

  /** Return head of list of {@link Mbuf}s that have been written into; 
   * clears internal reference. */
  public Mbuf takeMbuf() {
    Mbuf ret = first;
    current = null;
    first = null;
    pos = 0;
    return ret;
  }

  //instance data
  protected int mbufSize;
  protected int mbufHeaderSize;
  protected Mbuf first;
  protected Mbuf current;
  protected int pos;
};

