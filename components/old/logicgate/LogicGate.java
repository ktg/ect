/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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
package equip.ect.components.logicgate;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * A general-purpose two-input logic gate.
 * 
 * <h3>Description</h3> This component provides the logic operations and, nand, or, nor, xor and
 * xnor. There is an output property for each logic operation, named after that operation, and the
 * value of these properties gets updated appropriately according to any changes to the input
 * properties input1 and input2.
 * 
 * @author Chris Greenhalgh
 * @displayName Logic Gate
 * @classification Behaviour/Logic
 * @preferred
 */
public class LogicGate implements Serializable
{
	/**
	 * input values
	 */
	protected boolean inputs[] = new boolean[2];
	/**
	 * output indexes
	 */
	public static final int AND = 0;
	public static final int NAND = 1;
	public static final int OR = 2;
	public static final int NOR = 3;
	public static final int XOR = 4;
	public static final int XNOR = 5;
	/**
	 * output property names
	 */
	public static final String[] outputNames = new String[] { "and", "nand", "or", "nor", "xor", "xnor" };
	/**
	 * output values
	 */
	protected boolean outputs[] = new boolean[6];
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public LogicGate()
	{
		updateOutputs();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * Logical AND of inputs.
	 * 
	 * @preferred
	 */
	public boolean getAnd()
	{
		return outputs[AND];
	}

	/**
	 * input getter
	 */
	public synchronized boolean getInput1()
	{
		return inputs[0];
	}

	/**
	 * input getter
	 */
	public synchronized boolean getInput2()
	{
		return inputs[1];
	}

	/**
	 * Logical NAND of inputs, ie opposite of AND.
	 */
	public boolean getNand()
	{
		return outputs[NAND];
	}

	/**
	 * Logical NOR of inputs, ie opposite of OR.
	 */
	public boolean getNor()
	{
		return outputs[NOR];
	}

	/**
	 * Logical OR of inputs.
	 * 
	 * @preferred
	 */
	public boolean getOr()
	{
		return outputs[OR];
	}

	/**
	 * Logical XNOR of inputs, ie neither or both.
	 */
	public boolean getXnor()
	{
		return outputs[XNOR];
	}

	/**
	 * Logical XOR of inputs, ie one but not both.
	 */
	public boolean getXor()
	{
		return outputs[XOR];
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * The first input value (boolean).
	 * 
	 * @preferred
	 */
	public synchronized void setInput1(final boolean v)
	{
		setInput(0, v);
	}

	/**
	 * The second input value (boolean).
	 * 
	 * @preferred
	 */
	public synchronized void setInput2(final boolean v)
	{
		setInput(1, v);
	}

	/**
	 * stop - called when component request is removed. Release all resources - GUI, IO, files, etc.
	 * But be aware that the 'same' component may be recreated again in the future.
	 */
	public synchronized void stop()
	{
		// no op
	}

	protected synchronized void setInput(final int i, final boolean v)
	{
		if (i < 0 || i >= inputs.length || inputs[i] == v) { return; }
		final Boolean oldValue = new Boolean(inputs[i]);
		inputs[i] = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("input" + (i + 1), oldValue, new Boolean(this.inputs[i]));
		updateOutputs();
	}

	/**
	 * output internal setter - NB protected, so that it cannot be called by the framework
	 */
	protected synchronized void setOutput(final int i, final boolean v)
	{
		if (i < 0 || i >= outputs.length || outputs[i] == v) { return; }
		final Boolean oldValue = new Boolean(outputs[i]);
		outputs[i] = v;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange(outputNames[i], oldValue, new Boolean(this.outputs[i]));
	}

	/**
	 * behaviour
	 */
	protected void updateOutputs()
	{
		setOutput(AND, inputs[0] && inputs[1]);
		setOutput(NAND, !(inputs[0] && inputs[1]));
		setOutput(OR, inputs[0] || inputs[1]);
		setOutput(NOR, !(inputs[0] || inputs[1]));
		setOutput(XOR, inputs[0] ^ inputs[1]);
		setOutput(XNOR, !(inputs[0] ^ inputs[1]));
	}
}
