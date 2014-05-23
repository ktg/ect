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
/* DataCallbackPost 
 * autogenerated from common/idl/eqDataTypes.idl
 * by eqidl
 * DO NOT MODIFY
 */


package equip.data;

import equip.runtime.*;

/** post-event callback interface, alternative to {@link
     * DataCallback}, and normally more intuitive.<P>
     * 
     * Use in place of a DataCallback object if you wish to 
     * receive a notification immediately AFTER an event has
     * been enacted, e.g. after an add event has added its item
     * to the environment. 
     */
public abstract class DataCallbackPost extends equip.data.DataCallback {

  /** Default no-arg constructor */
  public DataCallbackPost() {}

  /* member variables */
/** notification of a matched event or pseudo event.
       *
       * As with {@link DataCallback}, you pass your own {@link
       * DataCallbackPost} subclass as an argument to {@link
       * DataProxy#createSession} when creating a new {@link
       * DataSession}.
       *
       * Note that post callbacks may be received <b>AFTER</b> a
       * session or pattern has been deleted (if and only if this is
       * done in a notify callback on the same event).
       *  
       * NOTE: this class is not currently (2003-10-16) supported in the C++
       * version.
       *
       * @param event The matched event being enacted by the dataspace.
       * @param pattern The pattern that was matched.
       * @param patternDeleted Whether the pattern has been deleted 
       * as a result of the event, e.g. matched.
       * @param session The {@link DataSession} generating this callback.
       * @param dataspace The local dataspace generating this callback.
       * @param oldValue (for update and delete only) the old value 
       * of the data item (before this event was enacted)
       * @param oldBinding (for update and delete only) the old value 
       * of the item binding (before this event was enacted)
       * @param closure As provided when the {@link DataSession} was
       * created.
       */
  public abstract void notifyPost(equip.data.Event event, equip.data.EventPattern pattern, boolean patternDeleted, equip.data.DataSession session, equip.data.DataProxy dataspace, equip.data.ItemData oldValue, equip.data.ItemBinding oldBinding, equip.runtime.ValueBase closure);
  /** IDL-generated helper routine to get module name (currently <b>unimplemented</b>).
  * @return name of this class's module
  */
  public String getModuleName() { return null; }
  /** Standard IDL-generated equality test.
  * @param c The object to be compared against this.
  * @return true if this is equal to <code>c</code>
  */
  public boolean equals(java.lang.Object c) {
    if (c==null) return false;
    if (!c.getClass().equals(getClass())) return false;
    return _equals_helper((DataCallbackPost)c);
  }
  /** Internal IDL-generated equality test helper */
  public boolean _equals_helper(DataCallbackPost c) {
    if (c==null) return false;
    if (!super._equals_helper(c)) return false;
    return true;
  }
  /** Standard IDL-generated template match test. 
  * @param c The object to be checked against this template.
  * @return true if <code>this</code> (as a template) matches the argument
  */
  public boolean matches(java.lang.Object c) {
    if (c==null || !(c instanceof DataCallbackPost)) return false;
    return _matches_helper((DataCallbackPost)c);
  }
  /** Internal IDL-generated match test helper */
  public boolean _matches_helper(DataCallbackPost c) {
    if (c==null) return false;
    if (!super._matches_helper(c)) return false;
    return true;
  }
  /** Internal IDL-generated serialisation helper. Used by {@link equip.runtime.ObjectInputStream} and {@link equip.runtime.ObjectOutputStream} only. */
  public void writeObject(ObjectOutputStream out)
    throws java.io.IOException {
    super.writeObject(out);
    out.writeObjectStart();
    out.writeObjectEnd();
  }
  /** Internal IDL-generated serialisation helper. Used by {@link ObjectInputStream} and {@link ObjectOutputStream} only. */
  public void readObject(ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException, 
      InstantiationException {
    super.readObject(in);
    in.readObjectStart();
    in.readObjectEnd();
  }


} /* class DataCallbackPost */

/* EOF */
