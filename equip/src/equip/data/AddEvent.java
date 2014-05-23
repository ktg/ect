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
/* AddEvent 
 * autogenerated from common/idl/eqDataTypes.idl
 * by eqidl
 * DO NOT MODIFY
 */


package equip.data;

import equip.runtime.*;

/** An event representing the addition of a {@link ItemData}
	instance to the dataspace. Typically generated by calls to
	{@link DataProxy#addItem}. */
public abstract class AddEvent extends equip.data.Event {

  /** Default no-arg constructor */
  public AddEvent() {}

  /* member variables */
/** The item's value and its binding to the dataspace */
  public equip.data.ItemBinding binding;
/** The kind of add event (see {@link ItemEventKind}) */
  public equip.data.ItemEventKindBox kind;
/** Initialisation helper; see {@link ItemBindingInfo} for most
	  arguments. */
  public abstract void initFromItem(equip.data.ItemData item, equip.data.GUID agentId, int locked, boolean processBound, boolean local, equip.data.Lease itemLease);
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
    return _equals_helper((AddEvent)c);
  }
  /** Internal IDL-generated equality test helper */
  public boolean _equals_helper(AddEvent c) {
    if (c==null) return false;
    if (!super._equals_helper(c)) return false;
    if (binding!=c.binding && (binding==null || !binding.equals(c.binding))) return false;
    if (kind!=c.kind && (kind==null || !kind.equals(c.kind))) return false;
    return true;
  }
  /** Standard IDL-generated template match test. 
  * @param c The object to be checked against this template.
  * @return true if <code>this</code> (as a template) matches the argument
  */
  public boolean matches(java.lang.Object c) {
    if (c==null || !(c instanceof AddEvent)) return false;
    return _matches_helper((AddEvent)c);
  }
  /** Internal IDL-generated match test helper */
  public boolean _matches_helper(AddEvent c) {
    if (c==null) return false;
    if (!super._matches_helper(c)) return false;
    if (binding!=null && !binding.matches(c.binding)) return false;
    if (kind!=null && !kind.matches(c.kind)) return false;
    return true;
  }
  /** Internal IDL-generated serialisation helper. Used by {@link equip.runtime.ObjectInputStream} and {@link equip.runtime.ObjectOutputStream} only. */
  public void writeObject(ObjectOutputStream out)
    throws java.io.IOException {
    super.writeObject(out);
    out.writeObjectStart();
    out.writeObject(binding);
    out.writeObject(kind);
    out.writeObjectEnd();
  }
  /** Internal IDL-generated serialisation helper. Used by {@link ObjectInputStream} and {@link ObjectOutputStream} only. */
  public void readObject(ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException, 
      InstantiationException {
    super.readObject(in);
    in.readObjectStart();
    binding = (equip.data.ItemBinding )in.readObject();
    kind = (equip.data.ItemEventKindBox )in.readObject();
    in.readObjectEnd();
  }


} /* class AddEvent */

/* EOF */
