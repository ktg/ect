/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
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
/* Example of extending from TupleImpl for non-IDLd but readable classes. 
 * Chris Greenhalgh 2004-02-09
 */


package equip.data;

import equip.runtime.*;

/** Example of extending from TupleImpl for non-IDLd but readable classes.
 * class name must end in 'Impl' (at present). This one has
 * two properties, x and y, and uses fields[0] for class name. 
 * This example also uses language-specific box types externally.
 */
public class SampleExtensionTypeImpl extends TupleImpl {
    /* lifecycle */
    public SampleExtensionTypeImpl() {
	//....
	fields = new ValueBase[3];
	fields[0] = new StringBoxImpl(this.getClass().getName());
    }
    /** convenience constructor */
    public SampleExtensionTypeImpl(Integer x, Integer y) {
	fields = new ValueBase[3];
	fields[0] = new StringBoxImpl(this.getClass().getName());
	fields[1] = (x != null) ? new IntBoxImpl(x.intValue()) : null;
	fields[2] = (y != null) ? new IntBoxImpl(y.intValue()) : null;
    }
    /* set x */
    public void setX(Integer x) {
	fields[1] = (x != null) ? new IntBoxImpl(x.intValue()) : null;
    }
    /* get x */
    public Integer getX() {
	return fields[1]!=null ? new Integer(((IntBox)fields[1]).value) : null;
    }

    /* set y */
    public void setY(Integer y) {
	fields[2] = (y != null) ? new IntBoxImpl(y.intValue()) : null;
    }
    /* get y */
    public Integer getY() {
	return fields[2]!=null ? new Integer(((IntBox)fields[2]).value) : null;
    }

} /* class Tuple */

/* EOF */
