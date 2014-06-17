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

package equip.ect.components.goldsmiths;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * @classification user/goldsmiths
 * @preferred
 */

public class LootTextRipper extends LootRipper
{
	protected String[] titleSelectors = { "ad-free-header", "ad-paid1-header", "ad-paid2-header", "ad-paid3-header",
											"ad-paid4-header" };

	protected String[] bodySelectors = { "ad-free-body", "ad-paid1-body", "ad-paid2-body", "ad-paid3-body",
										"ad-paid4-body" };

	protected DictionaryImpl[] adverts;

	public DictionaryImpl[] getAdverts()
	{
		return adverts;
	}

	public String[] getBodySelectors()
	{
		return bodySelectors;
	}

	public String[] getTitleSelectors()
	{
		return titleSelectors;
	}

	public void setBodySelectors(final String[] newValue)
	{
		final String[] oldValue = this.bodySelectors;
		this.bodySelectors = newValue;

		propertyChangeListeners.firePropertyChange("bodySelectors", oldValue, newValue);
	}

	public void setTitleSelectors(final String[] newValue)
	{
		final String[] oldValue = this.titleSelectors;
		this.titleSelectors = newValue;

		propertyChangeListeners.firePropertyChange("titleSelectors", oldValue, newValue);
	}

	@Override
	protected void clearOutputs()
	{
		setAdverts(null);
	}

	protected boolean isBodyAttribute(final String attributeName)
	{
		return (isValidAttribute(attributeName, getBodySelectors()));
	}

	protected boolean isTitleAttribute(final String attributeName)
	{
		return (isValidAttribute(attributeName, getTitleSelectors()));
	}

	@Override
	protected void ripAdvert(final HTMLDocument doc)
	{
		setAttention("Starting to rip advert");

		String title = null;
		String body = null;

		final NodeList nl = doc.getElementsByTagName("div");

		final List<DictionaryImpl> advertsVector = new ArrayList<DictionaryImpl>();

		for (int i = 0; i < nl.getLength(); i++)
		{
			final HTMLElement elem = (HTMLElement) (nl.item(i));

			if (elem.hasAttribute("class"))
			{
				final String classAtt = elem.getAttribute("class");

				if (isTitleAttribute(classAtt))
				{
					final NodeList nl1 = elem.getChildNodes();

					for (int j = 0; j < nl1.getLength(); j++)
					{
						final Node n = nl1.item(j);

						if (n instanceof HTMLAnchorElement)
						{
							final NodeList children = n.getChildNodes();

							for (int k = 0; k < children.getLength(); k++)
							{
								final Node n2 = children.item(k);
								if (n2 instanceof Text)
								{
									title = ((Text) n2).getData();
									break;
								}
							}
						}
						if (title != null)
						{
							break;
						}
					}
				}

				if (isBodyAttribute(classAtt))
				{
					final NodeList nl2 = elem.getChildNodes();

					for (int j = 0; j < nl2.getLength(); j++)
					{
						final Node n = nl2.item(j);

						if (n instanceof Text)
						{
							body = ((Text) n).getData();
							break;

						}
					}
				}
			}

			if ((title != null) && (body != null))
			{
				final DictionaryImpl di = new DictionaryImpl();

				di.put("title", new StringBoxImpl(title));
				di.put("body", new StringBoxImpl(body));

				advertsVector.add(di);

				title = null;
				body = null;
			}
		}

		setAdverts(advertsVector.toArray(new DictionaryImpl[advertsVector.size()]));

	}

	protected void setAdverts(final DictionaryImpl[] newValue)
	{
		final DictionaryImpl[] oldValue = this.adverts;
		this.adverts = newValue;

		propertyChangeListeners.firePropertyChange("adverts", oldValue, newValue);
	}
}
