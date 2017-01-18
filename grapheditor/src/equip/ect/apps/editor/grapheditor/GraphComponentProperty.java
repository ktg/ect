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
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)

 */

package equip.ect.apps.editor.grapheditor;

import equip.ect.BeanDescriptorHelper;
import equip.ect.ComponentProperty;
import equip.ect.apps.editor.BeanCanvasItem;
import equip.ect.apps.editor.Connectable;
import equip.ect.apps.editor.dataspace.DataspaceUtils;
import equip.ect.apps.editor.Link;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class GraphComponentProperty extends BeanCanvasItem implements Connectable, Comparable<GraphComponentProperty>
{
	private final GraphComponent parent;
	private String value;
	private ComponentProperty compProp;

	private List<Link> outputLinks, inputLinks;

	GraphComponentProperty(final GraphComponent parent, final ComponentProperty compProp)
	{
		super(
				parent.getTargetCanvas(),
				new GraphComponentPropertyView(
						(compProp.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME) instanceof equip.data.StringBox ? ((equip.data.StringBox) compProp
								.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME)).value : compProp
								.getPropertyName()), DataspaceUtils.getPropValueAsString(compProp)), compProp.getID()
						.toString(), compProp.getPropertyName());
		this.parent = parent;
		updateValue(compProp);
	}

	@Override
	public void cleanUp()
	{
		removeAllLinks();
	}

	@Override
	public Object clone(final Component canvas)
	{
		return new GraphComponentProperty(parent, getComponentProperty());
	}

	@Override
	public int compareTo(final GraphComponentProperty other)
	{
		if (name != null)
		{
			return this.name.compareTo(other.getName());
		}
		return -1;
	}

	@Override
	public Point getInAnchorPoint()
	{
		return new Point(posX, (int) (posY + 0.5 * height));
	}

	@Override
	public Point getOutAnchorPoint()
	{
		return new Point(posX + width, (int) (posY + 0.5 * height));
	}

	/**
	 * Since we never add this component to the canvas for repaint, we need to call the paint on the
	 * canvas method directly;
	 */
	@Override
	public void repaint()
	{
		parent.repaint();
	}

	@Override
	public void setPosition(final int posX, final int posY)
	{
		super.setPosition(posX, posY);
		if (outputLinks != null)
		{
			for (Link link : outputLinks)
			{
				link.setStartPoint(getOutAnchorPoint());
			}
		}

		if (inputLinks != null)
		{
			for (Link link : inputLinks)
			{
				link.setEndPoint(getInAnchorPoint());
			}
		}
	}

	@Override
	public void setSelected(final boolean selected)
	{
		super.setSelected(selected);
		this.parent.setSelected(selected);
	}

	void addInputLink(final Link link)
	{
		if (inputLinks == null)
		{
			inputLinks = new ArrayList<>();
		}
		if (!inputLinks.contains(link))
		{
			inputLinks.add(link);
		}
	}

	void addOutputLink(final Link link)
	{
		if (outputLinks == null)
		{
			outputLinks = new ArrayList<>();
		}
		if (!outputLinks.contains(link))
		{
			outputLinks.add(link);
		}
	}

	void componentPropertyUpdated(final ComponentProperty compProp)
	{
		updateValue(compProp);
		repaint();
	}

	final ComponentProperty getComponentProperty()
	{
		// return DataspaceMonitor.getMonitor().getComponentProperty(beanid);
		return compProp;
	}

	String getDisplayValue()
	{
		return value;

	}

	final List<Link> getInputLinks()
	{
		return inputLinks;
	}

	final List<Link> getOutputLinks()
	{
		return outputLinks;
	}

	final GraphComponent getParent()
	{
		return this.parent;
	}

	final boolean isLinked()
	{
		return ((inputLinks != null && inputLinks.size() > 0) || (outputLinks != null && outputLinks.size() > 0));
	}

	void removeAllInputLinks()
	{
		inputLinks = null;
	}

	void removeAllLinks()
	{
		removeAllInputLinks();
		removeAllOutputLinks();
	}

	void removeAllOutputLinks()
	{
		outputLinks = null;
	}

	void removeInputLink(final Link link)
	{
		if (inputLinks != null && link != null)
		{
			inputLinks.remove(link);
		}
	}

	void removeOutputLink(final Link link)
	{
		if (outputLinks != null && link != null)
		{
			outputLinks.remove(link);
		}
	}

	private void updateValue(final ComponentProperty prop)
	{
		this.compProp = prop;
		this.name = prop.getPropertyName();
		this.value = DataspaceUtils.getPropValueAsString(prop);
		((GraphComponentPropertyView) view)
				.setName(compProp.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME) instanceof equip.data.StringBox ? ((equip.data.StringBox) compProp
						.getAttributeValue(BeanDescriptorHelper.DISPLAY_NAME)).value : name);
		((GraphComponentPropertyView) view).setValue(value);
	}
}
