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
package equip.runtime;
import java.io.*;

/** EQUIP-specific serialisation output stream for writing
 * serialised instances of {@link equip.runtime.ValueBase} to.
 *
 * Used in place of {@link java.io.ObjectOutputStream} since
 * the serialisation is the same for EQUIP IDL-defined classes
 * in C++ as well. */
public class ObjectOutputStream extends DataOutputStream {

  /** Internal constant - stream header magic number ('E','q') */
  public static final short MAGIC = 0x4571; // 'E' 'q'

  /** Internal constant - serialisation version */
  public static final short VERSION = 0x0001;

  /** Internal constant - code for a null value */
  final static byte TC_NULL = (byte)0x70;

  /** Internal constant - code for an object */
  final static byte TC_OBJECT = (byte)0x73;

  /** Internal constant - code for a string */
  final static byte TC_STRING = (byte)0x74;  

  /** Construct over a {@link java.io.OutputStream}.
   *
   * <b>Note:</b> like {@link java.io.ObjectOutputStream}, this
   * constructor immediately write header information to the output
   * stream and flush it; this information is required by the
   * corresponding {@link equip.runtime.ObjectInputStream}.
   *
   * @throws IOException Some internal problem (not likely). */
  public ObjectOutputStream(OutputStream out) throws IOException {
    super(out);
    // header
    //System.err.println("ObjectOutputStream: write header");
    writeShort(MAGIC);
    writeShort(VERSION);
    flush();
  }
  /** Write a Java string; will send a null if provided */ 
  public void writeString(String s) throws IOException {
    if (s==null) {
      writeByte(ObjectOutputStream.TC_NULL);
      return; 
    }
    writeByte(ObjectOutputStream.TC_STRING);
    writeUTF(s);
  }
    /** Write an object (subclass of {@link equip.runtime.ValueBase})
     * to the output stream; will write a nulls if provided.
     *
     * @throws IOException Miscellaneous problem, e.g. end of stream.
     */
  public void writeObject(ValueBase object) throws IOException {
    if (object==null) {
      writeByte(ObjectOutputStream.TC_NULL);
      //flush();
      return;
    }
    writeByte(ObjectOutputStream.TC_OBJECT);
    // write class
    String classname = object.getClass().getName();
    // minus Impl
    int len = classname.length();
    if (len>=4 && classname.substring(len-4,len).equals("Impl"))
      classname = classname.substring(0,len-4);
    writeUTF(classname);
    // module, etc. ....
    // write object
    object.writeObject(this);
    //flush();
  }
  /** Internal operation use by object serialisers */
  public void writeObjectStart() throws IOException { }
  /** Internal operation use by object serialisers */
  public void writeObjectEnd() throws IOException { }
}
