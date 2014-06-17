/*
 <COPYRIGHT>

 Copyright (c) 2004-2005, University of Southampton
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the University of Southampton
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

 Created by: Mark Thompson (University of Southampton)
 Contributors:
 Mark Thompson (University of Southampton)
 Chris Greenhalgh (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.atomblogger;

/**
 * @classification Networked Services/Blog
 * @author Mark Thompson <mkt@ecs.soton.ac.uk>
 */
public class AtomBlogger extends AtomBloggerBase
{

	String topic = "";
	String content = "";

	public AtomBlogger()
	{
		super();
	}

	public synchronized String getContent()
	{
		return content;
	}

	public synchronized Object getPost()
	{
		return null;
	}

	public synchronized String getTopic()
	{
		return topic;
	}

	public synchronized void setContent(final String newContent)
	{
		final String oldContent = this.content;
		this.content = newContent;

		setAttention(null);

		propertyChangeListeners.firePropertyChange("content", oldContent, newContent);
	}

	public synchronized void setPost(final Object value)
	{
		if (value != null)
		{
			uploadEntry(topic, content);
		}
	}

	public synchronized void setTopic(final String newTopic)
	{
		final String oldTopic = this.topic;
		this.topic = newTopic;

		setAttention(null);

		propertyChangeListeners.firePropertyChange("topic", oldTopic, newTopic);
	}

	@Override
	public void stop()
	{
		super.stop();
	}
}
