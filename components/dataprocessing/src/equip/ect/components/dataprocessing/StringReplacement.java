/*
 <COPYRIGHT>

 Copyright (c) 2006, University of Nottingham
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

 Created by: Stefan Rennick Egglestone(University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone(University of Nottingham)

 */
package equip.ect.components.dataprocessing;

import equip.ect.Category;
import equip.ect.ECTComponent;

/**
 * @classification Data/Text
 * @defaultOutputProperty output
 * @defaultInputProperty input
 * @author Stefan Rennick Egglestone
 */
@ECTComponent
@Category("Text")
public class StringReplacement extends AbstractStringProcessing
{
	String findString = null;
	String replaceString = null;

	public synchronized String getFindString()
	{
		return findString;
	}

	public synchronized String getReplaceString()
	{
		return replaceString;
	}

	@Override
	public String operate(final String input)
	{
		if ((findString != null) && (!(findString.equals(""))) && (input != null) && (!(input.equals("")))
				&& (replaceString != null))
		{
			return (input.replaceAll(findString, replaceString));
		}
		else
		{
			return input;
		}
	}

	public synchronized void setFindString(final String newValue)
	{
		final String oldValue = this.findString;
		this.findString = newValue;

		propertyChangeListeners.firePropertyChange("findString", oldValue, newValue);
		if (newValue != null)
		{
			updateOutput();
			updateOutputArray();
		}
	}

	public synchronized void setReplaceString(final String newValue)
	{
		final String oldValue = this.replaceString;
		this.replaceString = newValue;

		propertyChangeListeners.firePropertyChange("replaceString", oldValue, newValue);

		if (newValue != null)
		{
			updateOutput();
			updateOutputArray();
		}
	}
}
