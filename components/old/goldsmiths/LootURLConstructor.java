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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * @classification user/goldsmiths
 * @preferred
 */
public class LootURLConstructor implements Serializable
{

	protected String[] categories = { "&atp=motoring&c1=11077", "&atp=property&c1=11074", "&atp=homegarden&c1=11081",
										"&atp=electrical&c1=11073", "&atp=music&c1=11082", "&atp=hobbies&c1=11083" };

	protected String distance = "&psc_dis=1";

	protected String baseAddress = "http://www.loot.com/rs6/cl.asp?ss_id=&action=fsrlt&pn=1&cc=&ad_id=&subtap=am_lvl2&cpg=sf&c2=&kyd=&do=&iss=&prc=&ukloc=&ps=10&ord=distance_a&trd=1";

	protected String[] postcodes = { "&psc=sw18+4nz", "&psc=le11+4px" };

	protected String[] lootAddresses;

	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public LootURLConstructor()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized String getBaseAddress()
	{
		return baseAddress;
	}

	public synchronized String[] getCategories()
	{
		return categories;
	}

	public synchronized String getDistance()
	{
		return distance;
	}

	public synchronized String[] getLootAddresses()
	{
		return lootAddresses;
	}

	public synchronized String[] getPostcodes()
	{
		return postcodes;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setBaseAddress(final String newValue)
	{
		final String oldValue = this.baseAddress;
		this.baseAddress = newValue;

		propertyChangeListeners.firePropertyChange("baseAddress", oldValue, newValue);

		constructURLs();
	}

	public synchronized void setCategories(final String[] newValue)
	{

		final String[] oldValue = this.categories;
		this.categories = newValue;

		propertyChangeListeners.firePropertyChange("categories", oldValue, newValue);

		constructURLs();
	}

	public synchronized void setDistance(final String newValue)
	{
		final String oldValue = this.distance;
		this.distance = newValue;

		propertyChangeListeners.firePropertyChange("distance", oldValue, newValue);

		constructURLs();
	}

	public synchronized void setPostcodes(final String[] newValue)
	{

		final String[] oldValue = this.postcodes;
		this.postcodes = newValue;

		propertyChangeListeners.firePropertyChange("postcodes", oldValue, newValue);

		constructURLs();
	}

	protected void constructURLs()
	{
		// if postcodes, a distance, a base url and some categories have been
		// specified, the create some urls

		if ((postcodes != null) && (distance != null) && (baseAddress != null) && (categories != null)
				&& (baseAddress.length() != 0) && (distance.length() != 0))
		{
			// first construct croos product of postcode and categories arrays

			final StringBuffer[] newBuffers = new StringBuffer[(postcodes.length * categories.length)];

			int pos = 0;

			for (final String postcode : postcodes)
			{
				for (final String categorie : categories)
				{
					newBuffers[pos] = new StringBuffer();
					newBuffers[pos].append(baseAddress);
					newBuffers[pos].append(distance);
					newBuffers[pos].append(categorie);
					newBuffers[pos].append(postcode);

					pos++;
				}
			}

			final String[] newAddresses = new String[newBuffers.length];

			for (int i = 0; i < newAddresses.length; i++)
			{
				newAddresses[i] = newBuffers[i].toString();
			}

			setLootAddresses(newAddresses);
		}
		else
		{
			setLootAddresses(null);
		}
	}

	protected synchronized void setLootAddresses(final String[] newValue)
	{
		final String[] oldValue = this.lootAddresses;
		this.lootAddresses = newValue;

		propertyChangeListeners.firePropertyChange("lootAddresses", oldValue, newValue);
	}
}
