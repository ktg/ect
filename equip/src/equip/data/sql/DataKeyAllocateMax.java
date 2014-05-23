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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;

/** class for generating a new SQL primary key as MAX in column+1 (assumes int). 
 */
public class DataKeyAllocateMax  implements IDataKeyAllocate {
    /** allocate key.
     * @param keyType Key type (see {@link CustomTableDataStore}).
     * @param dataTableName name of SQL table
     * @param dataKeyColumn name of PK column in table
     * @param conn open SQL connection to database
     */
    public String allocateKey(String keyType, String dataTableName, String dataKeyColumn, Connection conn) {
	if (!(keyType.equals(CustomTableDataStore.TYPE_INT))) {
	    System.err.println("ERROR: DataKeyAllocateMax is only defined for  int keys (not "+
			       keyType+")");
	    return null;
	}
	try {
	    Statement stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT MAX("+dataKeyColumn+") FROM "+dataTableName);
	    if (!rs.next())
		// assume start from 1
		return "1";
	    return new Integer(rs.getInt(1)+1).toString();
	} catch (Exception e) {
	    System.err.println("ERROR: DataKeyAllocateMax for "+dataKeyColumn+", "+
			       dataTableName+" failed: "+e);
	    e.printStackTrace(System.err);
	}
	return null;
    }
}
//EOF



