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

/** EQUIP IDL top class, comparable to equip.lang.Object, providing the
 * abstract API for serialisation, equality, and pattern matching
 * (and type-safe downcasting in C++). */ 
public class ValueBase extends equip.runtime.Object implements java.lang.Cloneable {
  /** Return a clone of this object; just calls {@link java.lang.Object#clone} */
  public java.lang.Object clone() throws CloneNotSupportedException {
      return super.clone();
  }
  /** Internal serialisation helper. Used by {@link ObjectInputStream} and {@link ObjectOutputStream} only. */
  public void readObject(ObjectInputStream in) 
    throws java.io.IOException, ClassNotFoundException, 
      InstantiationException  { }
  /** Internal serialisation helper. Used by {@link ObjectInputStream} and {@link ObjectOutputStream} only. */
  public void writeObject(ObjectOutputStream out) 
    throws java.io.IOException { }
  /** Standard IDL-generated equality test.
  * @param c The object to be compared against this.
  * @return true if this is equal to <code>c</code>
  */
  public boolean equals(java.lang.Object c) {
      if (c==null) return false;
      if (!c.getClass().equals(getClass())) return false;
      return _equals_helper((ValueBase)c);
  }
  /** Internal IDL-generated equality test helper */
  public boolean _equals_helper(ValueBase c) {
      if (c==null) return false;
      return true;
  }
  /** Standard IDL-generated template match test. 
  * @param c The object to be checked against this template.
  * @return true if <code>this</code> (as a template) matches the argument
  */
  public boolean matches(java.lang.Object c) {
    if (c==null || !(c instanceof ValueBase)) return false;
    return _matches_helper((ValueBase)c);
  }
  /** Internal IDL-generated match test helper */
  public boolean _matches_helper(ValueBase c) {
      if (c==null) return false;
      return true;
  }
}
