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
/* UDPPacketHeader.java
   Chris Greenhalgh
   19/9/2001

  // 3   24 bits, magic +
  // 1    8 bits, version
  char magic[3], version;
  // 4   32 bits, originator ip (for forward/bridging)
  PRUint32 src_addr;
  // 2   16 bits, originator port (ditto)
  PRUint16 src_port;
  // 2   16 bits, 0 (padding/future use)
  PRUint16 res0;
  // 4   32 bits, originator session id
  PRUint32 src_sid;
  // 4   32 bits, originator object/message id
  PRUint32 src_mid;
  //      1 bit,  last fragment in object flag+
  // 4   31 bits, fragment # (in object/message)
  PRUint32 src_frag;

*/

package equip.net;

/** UDP (Not JCP) Packet header, i think this is legacy from
 * UDP discovery, now superceded by {@link equip.discovery} (cmg).
 *
 * <pre>  // 3   24 bits, magic +
  // 1    8 bits, version
  char magic[3], version;
  // 4   32 bits, originator ip (for forward/bridging)
  PRUint32 src_addr;
  // 2   16 bits, originator port (ditto)
  PRUint16 src_port;
  // 2   16 bits, 0 (padding/future use)
  PRUint16 res0;
  // 4   32 bits, originator session id
  PRUint32 src_sid;
  // 4   32 bits, originator object/message id
  PRUint32 src_mid;
  //      1 bit,  last fragment in object flag+
  // 4   31 bits, fragment # (in object/message)
  PRUint32 src_frag;
</pre>
 */
public class UDPPacketHeader {
  private byte [] b;
  public UDPPacketHeader(byte [] b) {
    this.b = b;
  }
  public byte getMagic(int i) {
    return b[i];
  }
  public void setMagic(int i, byte bb) {
    b[i] = bb;
  }
  public byte getVersion() {
    return b[3];
  }
  public void setVersion(byte bb) {
    b[3] = bb;
  }
  protected int getInt(int i) {
    return 
      ((((int)b[i+0]) & 0xff) << 24) |
      ((((int)b[i+1]) & 0xff) << 16) |
      ((((int)b[i+2]) & 0xff) << 8) |
      ((((int)b[i+3]) & 0xff));
  }
  protected void setInt(int i, int addr) {
    b[i+0] = (byte)((addr >> 24) & 0xff);
    b[i+1] = (byte)((addr >> 16) & 0xff);
    b[i+2] = (byte)((addr >> 8) & 0xff);
    b[i+3] = (byte)((addr) & 0xff);
  }
  protected void setInt(int i, byte [] bb) {
    b[i+0] = bb[0];
    b[i+1] = bb[1];
    b[i+2] = bb[2];
    b[i+3] = bb[3];
  }
  protected short getShort(int i) {
      return (short)(
		     ((((short)b[i+0]) & 0xff) << 8) |
		     ((((short)b[i+1]) & 0xff)));
  }
  protected void setShort(int i, short addr) {
    b[i+0] = (byte)((addr >> 8) & 0xff);
    b[i+1] = (byte)((addr) & 0xff);
  }

  public int getSrcAddr() {
    return getInt(4);
  }
  public void setSrcAddr(int addr) {
    setInt(4, addr);
  }
  public void setSrcAddr(byte [] addr) {
    setInt(4, addr);
  }
  public int getSrcPort() {
    return getShort(8);
  }
  public void setSrcPort(short port) {
    setShort(8, port);
  }
  public int getRes0() {
    return getShort(10);
  }
  public void setRes0(short port) {
    setShort(10, port);
  }
  public int getSrcSid() {
    return getInt(12);
  }
  public void setSrcSid(int sid) {
    setInt(12, sid);
  }
  public int getSrcMid() {
    return getInt(16);
  }
  public void setSrcMid(int mid) {
    setInt(16, mid);
  }
  public int getSrcFrag() {
    return getInt(20);
  }
  public void setSrcFrag(int frag) {
    setInt(20, frag);
  }
  public static int getSize() {
      return 24;
  }
  public static final byte magic_0 = (byte)'E';
  public static final byte magic_1 = (byte)'q';
  public static final byte magic_2 = (byte)'u';
  public static final byte version = (byte)1;
}

