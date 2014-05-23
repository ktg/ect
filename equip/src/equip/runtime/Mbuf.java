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
/* Mbuf.java */

package equip.runtime;
 
/*======================================================================*/
/** message(byte) buffer used by {@link equip.runtime.MbufInputStream} and
 * {@link equip.runtime.MbufOutputStream}.
 * 
 * <b>NB</b> be careful not to re-use in parallel if making use of ref
 * counting.  */

public class Mbuf {
  /** Default buffer size ({@value}) */
  public static final int DEFAULT_SIZE = 1000;

  /** initialise to {@link #DEFAULT_SIZE} and no header */
  public Mbuf() {
    size = DEFAULT_SIZE;
    headerSize = 0;
    buf = new byte [size];
    used = 0;
  }
  /** initialise to specified total and header size.
   *
   * @param size Total size of buffer.
   * @param headerSize Amount of buffer to be allocated as header */
  public Mbuf(int size, int headerSize) {
    this.size = size;
    buf = new byte [size];
    this.headerSize = headerSize;
    used = 0;
  }
  /** single linked list.
   *
   * @return Next buffer in single linked list */
  public Mbuf getNext() {
    return next;
  }
  /** set next buffer in single linked list.
   *
   * @param next Next buffer in single linked list. */
  public void setNext(Mbuf next) {
    this.next = next;
  }
  
  /** get number of bytes in use.
   *
   * @return number of bytes in use. */
  public int getUsed() {
    return used;
  }
  /** set number of bytes in use.
   *
   * @param used Number of bytes now used. */
  public void setUsed(int used) {
    this.used = used;
  }
  /** get total size of buffer.
   *
   * @return total size of buffer. */
  public int getSize() {
    return size;
  }
  /** get reference to internal buffer.
   *
   * @return Reference to internal buffer (not a copy of the buffer). */
  public byte[] getBuf() {
    return buf;
  }
  /** get size of buffer header space.
   *
   * @return Amount of buffer allocated to header. */
  public int getHeaderSize() {
    return headerSize;
  }
  /** set size of buffer header space.
   * 
   * @param size New amount of buffer to be allocated as header. */
  public void setHeaderSize(int size) {
    headerSize = size;
  }
  /** set internal buffer
   *
   * @param length amount of buffer {@link #used} */
  public void setData(byte[] b, int length)
  {
	buf = b;
	used = length;
  }
  
  /** internal buffer */
  protected byte [] buf;
  /** amount of internal buffer in use */
  protected int used;
  /** total size of internal buffer in use */
  protected int size;
  /** amount of internal buffer allocated to header space */
  protected int headerSize;
  /** pointer to next {@link Mbuf} in single linked list */
  protected Mbuf next;
}
