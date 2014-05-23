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

import equip.data.EquipConnector;

import javax.swing.UIManager;
import java.awt.*;

/**
 *
 * 
 * @author Tom 
 * @author Ian Taylor
 *
 * @version $Revision: 1.1.1.1 $
 *
 * $Id: EquipDataBrowser.java,v 1.1.1.1 2005/03/08 16:17:19 cgreenhalgh Exp $
 * 
 * $Log: EquipDataBrowser.java,v $
 * Revision 1.1.1.1  2005/03/08 16:17:19  cgreenhalgh
 * From Nottingham CVS
 *
 * Revision 1.6  2005/03/08 14:36:41  cmg
 * added BSD license
 *
 * Revision 1.5  2003/10/14 09:41:17  cmg
 * updated eqidl-generated files with more javadoc comments
 *
 * Revision 1.2  2002/02/10 20:30:02  ianm
 * import equip.data.EquipConnector
 *
 * Revision 1.1  2001/10/29 14:45:10  ianm
 * initial
 *
 */

public class EquipDataBrowser {
  boolean packFrame = false;

  /**Construct the application*/
  public EquipDataBrowser(EquipConnector ec) {
    DataBrowserFrame frame = new DataBrowserFrame(ec);
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }
  /**Main method*/
  public static void main(String[] args) {

     if (args.length!=1) {
      System.err.println("Usage: EquipDataBrowser  <server-url>");
      System.exit(-1);
    }

    EquipConnector ec = new EquipConnector(args[0]);

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    new EquipDataBrowser(ec);
  }
}
