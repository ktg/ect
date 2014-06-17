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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.filter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Can be used to filter emails. <h3>Description</h3> EmailFilter is a specialized version of the
 * DictionaryFilter component. It provides functionality to specify email filters using safe lists
 * and blocked lists of email addresses. It is intended to be used on messages produced by the
 * EmailReceiver component. <h3>Configuration</h3>
 * <ul>
 * <li>to add email addresses to a safe-list defined by this component, set property
 * <i>addToSafeList</i> to something like {sre@cs.nott.ac.uk,stefan_egglestone@hotmail.com}
 * <li>to remove these addresses, set property <i>removeFromSafeList</i> to
 * {sre@cs.nott.ac.uk,stefan_egglestone@hotmail.com}
 * <li>perform similar operations involving properties <i>addToBlockedList</i> and
 * <i>removeFromBlockedList</i> to manage your blocked list
 * </ul>
 * <h3>Usage</h3> Once you have configured rules for your filter, any messages that are placed on
 * property <i>inputMessages</i> will be filtered. Accepted messages will appear on propert
 * <i>acceptedMessages</i> and rejected messages will appear on property <i>rejectedMessages</i>
 * 
 * @classification Data/Dictionary
 * @defaultInputProperty inputMessages
 * @defaultOutputProperty acceptedMessages
 * @preferred
 */
public class EmailFilter extends DictionaryFilter implements Serializable
{
	public EmailFilter()
	{
	}

	// when given an array of email addresses, adds
	// a set of rules that will allow any emails through
	// if they are from someone with that address

	public synchronized String[] getAddToBlockedList()
	{
		return null;
	}

	public synchronized String[] getAddToSafeList()
	{
		return null;
	}

	public synchronized String[] getRemoveFromBlockedList()
	{
		return null;
	}

	public synchronized String[] getRemoveFromSafeList()
	{
		return null;
	}

	// when given an array of email addresses, adds
	// a set of rules that will not allow an emails through
	// if it is from anyone on the list

	public synchronized void setAddToBlockedList(final String[] addresses)
	{
		if ((addresses != null) && (addresses.length > 0))
		{
			// first, make sure that the constructs that we need
			// for constructing these filters are there

			setTriggerReturnToDefaults("a random trigger value");

			// now loop through array, constrcuting rules from
			// addresses we find there

			final Vector currentRules = new Vector();

			final String[] allRules = getConfigAllRules();

			if (allRules != null)
			{
				for (final String allRule : allRules)
				{
					if (allRule.trim().length() != 0)
					{
						currentRules.add(allRule);
					}
				}
			}

			for (final String addresse : addresses)
			{
				if (addresse.trim().length() != 0)
				{
					final String rule = "from doesn't contain " + addresse;
					currentRules.add(rule);
				}
			}

			final String[] newRules = (String[]) (currentRules.toArray(new String[currentRules.size()]));

			setConfigAllRules(newRules);

		}
	}

	public synchronized void setAddToSafeList(final String[] addresses)
	{
		if ((addresses != null) && (addresses.length > 0))
		{
			// first, make sure that the constructs that we need
			// for constructing these filters are there

			setTriggerReturnToDefaults("a random trigger value");

			// now loop through array, constrcuting rules from
			// addresses we find there

			final Vector currentRules = new Vector();

			final String[] anyRules = getConfigAnyRules();

			if (anyRules != null)
			{
				for (final String anyRule : anyRules)
				{
					if (anyRule.trim().length() != 0)
					{
						currentRules.add(anyRule);
					}
				}
			}

			for (final String addresse : addresses)
			{
				if (addresse.trim().length() != 0)
				{
					final String rule = "from contains " + addresse;
					currentRules.add(rule);
				}
			}

			final String[] newRules = (String[]) (currentRules.toArray(new String[currentRules.size()]));

			setConfigAnyRules(newRules);
		}
	}

	public synchronized void setRemoveFromBlockedList(final String[] addresses)
	{
		if ((addresses != null) && (addresses.length > 0))
		{
			final String[] currentRules = getConfigAllRules();

			// search through rules building hashtable
			// mapping from email to rule

			final Hashtable hash = buildRulesHash(currentRules, "doesn't contain");

			removeAddresses(addresses, hash);
			final String[] newRules = convertToRulesArray(hash);

			setConfigAllRules(newRules);

		}
	}

	public synchronized void setRemoveFromSafeList(final String[] addresses)
	{
		if ((addresses != null) && (addresses.length > 0))
		{
			final String[] currentRules = getConfigAnyRules();

			// search through rules building hashtable
			// mapping from email to rule

			final Hashtable hash = buildRulesHash(currentRules, "contains");
			removeAddresses(addresses, hash);
			final String[] newRules = convertToRulesArray(hash);

			setConfigAnyRules(newRules);
		}
	}

	Hashtable buildRulesHash(final String[] rules, final String keyword)
	{
		final Hashtable hash = new Hashtable();

		for (final String rule : rules)
		{
			System.out.println("processing: " + rule);

			final String[] bits = rule.split(keyword);

			// typical rule is
			// "from contains <email adddress>"

			hash.put(bits[1].trim(), rule);
		}

		return hash;
	}

	String[] convertToRulesArray(final Hashtable hash)
	{
		// now iterate through hashtable, getting
		// array of rules

		final Collection c = hash.values();
		final String[] rules = (String[]) (c.toArray(new String[c.size()]));

		return rules;
	}

	void removeAddresses(final String[] addresses, final Hashtable hash)
	{

		// now iterate through addresses, removing
		// from hashtable if one found

		for (final String addresse : addresses)
		{
			if (hash.containsKey(addresse.trim()))
			{
				hash.remove(addresse.trim());
			}
		}
	}

}
