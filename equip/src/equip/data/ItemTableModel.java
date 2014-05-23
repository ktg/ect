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
package equip.data;

import javax.swing.table.*;
import java.util.Vector;

import java.lang.reflect.Field;

/**
 *
 *
 * @author Tom 
 * @author Ian Taylor
 *
 * @version $Revision: 1.1.1.1 $
 *
 * $Id: ItemTableModel.java,v 1.1.1.1 2005/03/08 16:17:20 cgreenhalgh Exp $
 * 
 * $Log: ItemTableModel.java,v $
 * Revision 1.1.1.1  2005/03/08 16:17:20  cgreenhalgh
 * From Nottingham CVS
 *
 * Revision 1.6  2005/03/08 14:36:43  cmg
 * added BSD license
 *
 * Revision 1.5  2003/10/14 09:41:17  cmg
 * updated eqidl-generated files with more javadoc comments
 *
 * Revision 1.1  2001/10/29 14:45:10  ianm
 * initial
 *
 */
public class ItemTableModel extends AbstractTableModel {

  Vector fieldname = new Vector();
  Vector fieldvalue = new Vector();

  public ItemTableModel(Object item) {

     Class itemClass = item.getClass();

     Field[] fields = itemClass.getFields();
     try {
       for (int i=0; i <fields.length;i++) {
          Field field = fields[i];
          fieldname.add(field.getName());
          fieldvalue.add(field.get(item));
       }
    } catch (Exception ex) {
       System.out.println("Problem accessing vlaues:" +ex);
     }
  }
  public int getColumnCount() {
    return 2;
  }
  public Object getValueAt(int row, int col) {


   if (row >= fieldname.size()) return null;
   if (col == 0 ) return fieldname.get(row);
   if (col == 1) {
               return fieldvalue.get(row);
      } else {
             return null; // Should never be reached
      }

  }

  public int getRowCount() {
    return 8;
  }

  public String getColumnName(int column) {
      if (column == 0 ) return "Field Name";
      if (column == 1) {
               return "Field Value";
      } else {
             return null; // Should never be reached
      }
    }


}
