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
/* Tuple 
 * initially autogenerated from ../../include/equip_data_tuple.idl
 * by eqidl
 */


package equip.data;

import equip.runtime.*;

/** Implementation of IDL'd abstract class {@link Tuple} */
public class TupleImpl extends Tuple {
  /* lifecycle */
  public TupleImpl() {
    //....
  }
  /** convenience constructor - 1 field */
  public TupleImpl(equip.runtime.ValueBase f1) {
      fields = new equip.runtime.ValueBase[1];
      fields[0] = f1;
  }
  /** convenience constructor - 2 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2) {
      fields = new equip.runtime.ValueBase[2];
      fields[0] = f1;
      fields[1] = f2;
  }
  /** convenience constructor - 3 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3) {
      fields = new equip.runtime.ValueBase[3];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
  }
  /** convenience constructor - 4 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4) {
      fields = new equip.runtime.ValueBase[4];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
  }
  /** convenience constructor - 5 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4,
		   equip.runtime.ValueBase f5) {
      fields = new equip.runtime.ValueBase[5];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
      fields[4] = f5;
  }
  /** convenience constructor - 6 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4,
		   equip.runtime.ValueBase f5,
		   equip.runtime.ValueBase f6) {
      fields = new equip.runtime.ValueBase[6];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
      fields[4] = f5;
      fields[5] = f6;
  }
  /** convenience constructor - 7 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4,
		   equip.runtime.ValueBase f5,
		   equip.runtime.ValueBase f6,
		   equip.runtime.ValueBase f7) {
      fields = new equip.runtime.ValueBase[7];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
      fields[4] = f5;
      fields[5] = f6;
      fields[6] = f7;
  }
  /** convenience constructor - 8 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4,
		   equip.runtime.ValueBase f5,
		   equip.runtime.ValueBase f6,
		   equip.runtime.ValueBase f7,
		   equip.runtime.ValueBase f8) {
      fields = new equip.runtime.ValueBase[8];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
      fields[4] = f5;
      fields[5] = f6;
      fields[6] = f7;
      fields[7] = f8;
  }
  /** convenience constructor - 9 fields */
  public TupleImpl(equip.runtime.ValueBase f1,
		   equip.runtime.ValueBase f2,
		   equip.runtime.ValueBase f3,
		   equip.runtime.ValueBase f4,
		   equip.runtime.ValueBase f5,
		   equip.runtime.ValueBase f6,
		   equip.runtime.ValueBase f7,
		   equip.runtime.ValueBase f8,
		   equip.runtime.ValueBase f9) {
      fields = new equip.runtime.ValueBase[9];
      fields[0] = f1;
      fields[1] = f2;
      fields[2] = f3;
      fields[3] = f4;
      fields[4] = f5;
      fields[5] = f6;
      fields[6] = f7;
      fields[7] = f8;
      fields[8] = f9;
  }

  /* API */
  /* subclasses....*/


/* subclass ItemData */

  /** custom matching */
  public boolean _matches_helper(Tuple c) {
    if (c==null) return false;
    if (!super._matches_helper((ItemData)c)) return false;
    //if (fields!=null && fields.length!=0 && (c.fields==null || c.fields.length!=fields.length)) return false;
    if (fields!=null && fields.length!=0 && (c.fields==null || c.fields.length<fields.length)) return false;
    if (fields==null)
	// match all
	return true;
    int i1;
    for (i1=0; i1<fields.length; i1++) {
      if (fields[i1]!=null && !fields[i1].matches(c.fields[i1])) return false;
    }
    return true;
  }

} /* class Tuple */

/* EOF */
