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
public class StringConcatenation extends AbstractStringProcessing
{

	String pre = "";
	String post = "";

	public synchronized String getPost()
	{
		return post;
	}

	public synchronized String getPre()
	{
		return pre;
	}

	@Override
	public String operate(final String input)
	{
		final StringBuffer sb = new StringBuffer();

		if (getPre() != null)
		{
			sb.append(getPre());
		}

		sb.append(input);

		if (getPost() != null)
		{
			sb.append(getPost());
		}

		return (sb.toString());
	}

	public synchronized void setPost(final String newValue)
	{
		final String oldValue = this.post;
		this.post = newValue;

		propertyChangeListeners.firePropertyChange("post", oldValue, newValue);

		updateOutput();
		updateOutputArray();
	}

	public synchronized void setPre(final String newValue)
	{
		final String oldValue = this.pre;
		this.pre = newValue;

		propertyChangeListeners.firePropertyChange("pre", oldValue, newValue);

		updateOutput();
		updateOutputArray();
	}
}
