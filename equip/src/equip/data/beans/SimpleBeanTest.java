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
/* Chris Greenhalgh
 * 4 Nov Sept 2002
 * $Id: SimpleBeanTest.java,v 1.1.1.1 2005/03/08 16:17:22 cgreenhalgh Exp $
 */
package equip.data.beans;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import equip.runtime.*;
import equip.data.*;

/** sample equip.data.beans program
 *
 * Uses a PropertyPublisherBean to publish the 'value' property
 * of a (demo) InputPanelBean to the dataspace as a Tuple (via the
 * DataspaceBean), and a PropertySubscriberBean to pull the value
 * back in the dataspace and update the 'value' property on a (demo)
 * OutputPanelBean.
 * 
 * Command line option modes 'm'(master), 's'(slave) and 'b'(both)
 * allow you to split roles between multiple applications, including
 * multiple slaves (all follow the same master) and multiple masters
 * (subjectively most recently seen added being followed by each slave).
 */
public class SimpleBeanTest extends JFrame {

    /** main - creates an instance with args */
    public static void main(String [] args) {
	if (args.length!=3 ||
	    (!args[0].equals("m") && 
	     !args[0].equals("s") &&
	     !args[0].equals("b"))) {
	    System.err.println("Usage: java equip.data.beans.SimpleBeanTest "+
			       "m|s|b <name> <dataspace>");
	    System.exit(-1);
	}
	new SimpleBeanTest(args[0], args[1], args[2]);
    }

    /** dataspace */
    DataspaceBean dataspace;

    /** out nominal class name */
    public static final String myItemClassName = 
	"equip.data.TupleTest.Item2";

    /** constructor - does everything */
    public SimpleBeanTest(String mode, String itemName, String dataspaceName) {
	// create dataspace bean
	System.err.println("Create DataspaceBean");
	DataspaceBean dataspace = new DataspaceBean();

	// set URL, which will try to activate the bean
	System.err.println("Set dataspace url to "+dataspaceName);
	try {
	    dataspace.setDataspaceUrl(dataspaceName);
	} catch (Exception e) {
	    System.err.println("ERROR: "+e);
	}
	// if not active, give up
	if (!dataspace.isActive()) {
	    System.err.println("ERROR: Failed to activate!");
	    System.exit(-1);
	}

	if (mode.equals("m") || mode.equals("b")) {
	    // master or both => do input/publish

	    // InputPanelBean
	    InputPanelBean input = new InputPanelBean();
	    input.setPreferredSize(new Dimension(100,100));

	    // add to frame
	    this.getContentPane().add(new JLabel("Input:"));
	    this.getContentPane().add(input);

	    // property publisher to publish 'value' property
	    PropertyPublisherBean publisher = 
		new PropertyPublisherBean();
	    // configure...
	    publisher.setSource(input);
	    publisher.setPropertyName("value");
	    publisher.setDataspace(dataspace);
	    publisher.setItemName(itemName);
	    publisher.setItemClassName(myItemClassName);
	}

	if (mode.equals("s") || mode.equals("b")) {
	    // slave or both => do subscribe/output

	    // OutputPanelBean
	    OutputPanelBean output = new OutputPanelBean();
	    output.setPreferredSize(new Dimension(100,100));
	    
	    // add to frame
	    this.getContentPane().add(new JLabel("Output:"));
	    this.getContentPane().add(output);

	    // property subscriber to pull in value
	    PropertySubscriberBean subscriber = 
		new PropertySubscriberBean();
	    // configure...
	    subscriber.setDataspace(dataspace);
	    subscriber.setItemName(itemName);
	    subscriber.setItemClassName(myItemClassName);

	    // set value on target (output bean)
	    subscriber.setPushTarget(output);
	    subscriber.setPushPropertyName("value");
	}

	this.getContentPane().setLayout(new FlowLayout());
	this.pack();
	this.setVisible(true);
    }
}
/* EOF */
