/*
 <COPYRIGHT>

 Copyright (c) 2004-2005, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */

package equip.ect.apps.editor;

import java.awt.Component;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LinkGroup extends Link
{

	private final Map<String, Link> links;

	public LinkGroup(final Component canvas, final Point startPoint, final Point endPoint, final Connectable source,
			final Connectable target, final RenderableLink linkView, final List<Link> links)
	{
		super(canvas, startPoint, endPoint, source, target, linkView, null);

		this.links = new HashMap<>();
		addLinks(links);
	}

	public final void addLink(final Link link)
	{
		if (!links.containsValue(link))
		{
			links.put(link.getBeanID(), link);
		}
	}

	public final void addLinks(final List<Link> links)
	{
		if (links != null)
		{
			links.forEach(this::addLink);
		}
	}

	@Override
	public void cleanUp()
	{
		links.values().forEach(Link::cleanUp);
	}

	public final Link getLink(final String beanid)
	{
		return links.get(beanid);
	}

	public final Iterable<Link> getLinks()
	{
		return links.values();
	}

	public final int nrLinks()
	{
		return links.size();
	}

	public final Link removeLink(final Link link)
	{
		return links.remove(link.getBeanID());
	}
}