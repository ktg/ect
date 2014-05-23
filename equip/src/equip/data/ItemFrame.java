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

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import equip.net.*;
import equip.data.*;
import equip.runtime.ValueBase;

/**
 *
 *
 * @author Tom 
 * @author Ian Taylor
 *
 * @version $Revision: 1.1.1.1 $
 *
 * $Id: ItemFrame.java,v 1.1.1.1 2005/03/08 16:17:20 cgreenhalgh Exp $
 * 
 * $Log: ItemFrame.java,v $
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

public class ItemFrame extends JFrame {
  JScrollPane jScrollPane1 = new JScrollPane();
  JPanel jPanel1 = new JPanel();
  JButton close = new JButton();
  JTable beanProperties = new JTable();
  ItemTableModel itemmodel;
  Object dataitem;


  public ItemFrame( Object selectedObject ) {
    dataitem = selectedObject;
    itemmodel = new ItemTableModel(dataitem);

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {


    close.setText("Close");
    close.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close_actionPerformed(e);
      }
    });
    this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(beanProperties, null);
  //s  jScrollPane1.getViewport().setSize(200,50);
   // jScrollPane1.setSize(300,300);
    beanProperties.setModel(itemmodel);
    this.getContentPane().add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(close, null);
     this.pack();

  }

  void close_actionPerformed(ActionEvent e) {
      this.dispose();
  }


}
