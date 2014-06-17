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

 Created by: Stefan Rennick Egglestone (University of Nottingham)
 Contributors:
 Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.components.rsscreator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import equip.ect.ContainerManagerHelper;

/**
 * Exports messages as an RSS feed. <h3>Description</h3>
 * <P>
 * This component uses an internal web-server provided by the container hosting it to export an RSS
 * feed.
 * </P>
 * <P>
 * The component provides properties to allow a user to specify meta-information about the feed
 * (such as a title, a description etc). Items in the feed are composed of messages which have been
 * provided to this component. Simple messages can be just items of text, and more complex messages
 * (such as those provided by components such as EmailReceiver) can contain more structured
 * information. RSSCreator will do its best to present messages through RSS in a simple manner.
 * </P>
 * <h3>Configuration</h3>
 * <P>
 * Use the <tt>description</tt>, <tt>encoding</tt>, <tt>language</tt>, <tt>link</tt> and
 * <tt>title</tt> properties to specify the meta-details of your feed. Note that none of these have
 * been made mandatory by the component, but you might like to check the RSS specs to find out which
 * RSS considers to be mandatory. You can also use the <tt>cssLocation</tt> property to specify the
 * URL of an optional stylesheet which might be used to format your feed.
 * </p>
 * <P>
 * Once these properties have been set, then set the <tt>fileName</tt> property to indicate where on
 * the embedded web-server the file will appear. Property <tt>location</tt> will then be set to the
 * URL where you can find any RSS produced.
 * </P>
 * <h3>Usage</h3> The RSS placed onto the web-server will include &lt;item&gt; elements constructed
 * using messages placed on the <tt>messages</tt> property of this component. Change the messages on
 * this property to change the content of the RSS. You might like to use a storage component, such
 * as FIFOQueue, to build up a list of messages to be placed on this property <h3>Technical details</h3>
 * Produces RSS conforming to the RSS 2.0 spec. You can validate any feed produced using
 * http://feedvalidator.org/
 * 
 * @classification Local Services
 * @displayName HTTPpublisher
 * @technology RSS
 * @defaultInputProperty messages
 * @defaultOutputProperty location
 */

public class HTTPPublisher extends RSSCreator
{
	@Override
	protected File getFile(final String fileName)
	{
		final File httpDir = ContainerManagerHelper.getHttpDirectory();
		final File outputFile = new File(httpDir.getAbsolutePath() + "/" + fileName);

		return outputFile;
	}

	@Override
	protected String getFileLocation(final File file) throws IOException
	{
		final URL url = ContainerManagerHelper.uploadToHttp(file);
		return url.toString();
	}
}
