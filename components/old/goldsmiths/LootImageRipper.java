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

import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLImageElement;

/**
 * @classification user/goldsmiths
 * @preferred
 */

public class LootImageRipper extends LootRipper
{

	protected String[] filters = { "/common/img/icons/no_photo_icon2_motoring_FREE.gif",
									"/common/img/icons/no_photo_icon2_motoring.gif" };

	protected String[] imageSelectors = { "ad-free-img", "ad-paid1-img", "ad-paid2-img", "ad-paid3-img", "ad-paid4-img" };

	String lootBase = "http://www.loot.com";

	String[] images;

	public String[] getFilters()
	{
		return filters;
	}

	public String[] getImages()
	{
		return images;
	}

	public String[] getImageSelectors()
	{
		return imageSelectors;
	}

	public String getLootBase()
	{
		return lootBase;
	}

	public void setFilters(final String[] newValue)
	{
		final String[] oldValue = this.filters;
		this.filters = newValue;

		propertyChangeListeners.firePropertyChange("filters", oldValue, newValue);
	}

	public void setImageSelectors(final String[] newValue)
	{
		final String[] oldValue = this.imageSelectors;
		this.imageSelectors = newValue;

		propertyChangeListeners.firePropertyChange("imageSelectors", oldValue, newValue);
	}

	public void setLootBase(final String newValue)
	{
		final String oldValue = this.lootBase;
		this.lootBase = newValue;

		propertyChangeListeners.firePropertyChange("lootBase", oldValue, newValue);
	}

	@Override
	protected void clearOutputs()
	{
		setImages(null);
	}

	protected boolean isDefaultImage(final String imageURL)
	{
		return (isValidAttribute(imageURL, filters));
	}

	protected boolean isImageAttribute(final String attribute)
	{
		return (isValidAttribute(attribute, imageSelectors));
	}

	@Override
	protected void ripAdvert(final HTMLDocument doc)
	{
		// iterate through html doc, looking for images
		// these are the source of an image tag which has a class property
		// somethingl ike ad-free-img or ad-paidx-img (x=1,2,3 ...)
		// eg <img src="..." class="ad-paid1-img" ...

		final List<String> tempImages = new ArrayList<String>();

		final NodeList nl = doc.getElementsByTagName("img");

		for (int i = 0; i < nl.getLength(); i++)
		{
			final HTMLImageElement elem = (HTMLImageElement) (nl.item(i));

			final String classAtt = elem.getAttribute("class");

			if (isImageAttribute(classAtt))
			{
				final String partialImageURL = elem.getSrc();

				System.out.println(partialImageURL);

				if (!(isDefaultImage(partialImageURL)))
				{
					System.out.println("adding\n");

					if (partialImageURL.startsWith("http"))
					{
						tempImages.add(partialImageURL);
					}
					else
					{
						final String fullImageURL = getLootBase() + partialImageURL;
						tempImages.add(fullImageURL);
					}
				}

			}
		}

		final String[] imageArray = ((String[]) (tempImages.toArray(new String[tempImages.size()])));

		setImages(imageArray);
	}

	protected void setImages(final String[] newValue)
	{
		final String[] oldValue = this.images;
		this.images = newValue;

		propertyChangeListeners.firePropertyChange("images", oldValue, newValue);
	}
}
