/*
<COPYRIGHT>

Copyright (c) 2003-2005, University of Nottingham
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
package equip.data.sql;
import equip.data.*;
import equip.runtime.*;

/** class for mapping SQL primary key to new unique GUID. 
 */
public class DataKeyMapGetUnique implements IDataKeyMap {
    /** instance GUID factory */
    GUIDFactory factory;

    /** map key.
     * @param key SQL key
     */
    public GUID mapKey(String type, String key) {
	GUID id = factory.getUnique();
	return id;
    }

    /** initialise. */
    public void init(String storeId) {
	factory = (GUIDFactory)SingletonManager.get(equip.data.GUIDFactoryImpl.class.getName());
    }

    /** reverse-map key (may not be supported).
     * @param id data item guid (from mapKey)
     * @return Stringified DB primary key corresponding to GUID iff algorithmically calculatable, else null.
     */
    public String mapKeyReverse(GUID id, String type) {
	return null;
    }
}
//EOF



