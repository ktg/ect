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
/* Encoding 
 * autogenerated from ../../include/eqNetTypes.idl
 * by eqidl
 * DO NOT MODIFY
 */


package equip.net;

import equip.runtime.*;

/** different encoding that may be specified for communication. 
     * encoding/stream types 
     * {IDL'd in eqNetTypes.idl).
     *
     * Values:
     * <ul>
     * <li>simple sending of objects over the stream: 
     *  <ul><li>EQENCODE_EQ_OBJECT_STREAM, 
     *  <li>EQENCODE_JAVA_OBJECT_STREAM</ul>
     * <li>encapsulation of objects in messages over the stream:
     *  <ul><li>EQENCODE_MESSAGE</ul>
     * <li>objects serialised into packets: 
     *  <ul><li>EQENCODE_EQ_OBJECT_PACKET, 
     *  <li>EQENCODE_JAVA_OBJECT_PACKET</ul>
     * </ul>
     */
public class Encoding {
/** EQUIP's own serialisation of objects */
  public static final int EQENCODE_EQ_OBJECT_STREAM = 0;
/** Java's normal serialisation of objects */
  public static final int EQENCODE_JAVA_OBJECT_STREAM = 1;
/** distinct messages */
  public static final int EQENCODE_MESSAGE = 2;
/** EQUIP serialisation of objects, mapped to packets */
  public static final int EQENCODE_EQ_OBJECT_PACKET = 3;
/** Java serialisation of objects, mapped to packets */
  public static final int EQENCODE_JAVA_OBJECT_PACKET = 4;
};
/* EOF */
