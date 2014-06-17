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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import equip.data.DictionaryImpl;
import equip.data.StringBoxImpl;

/**
 * Component allowing the specification of rules used to filter dictionary objects. <h3>Description</h3>
 * <P>
 * Many components produce complex messages called <i>dictionaries</i>. Examples include RSSClient
 * and EmailReceiver. This component can be used to filter such messages. For example, an email
 * message might be filtered so that only emails from a certain person are accepted. Details of
 * filtering are specified using a set of rules.
 * </P>
 * If you are interested in filtering emails, then you should look at EmailFilter, a version of this
 * component that provides simple functionality to specify filters using safe lists and blocked
 * lists. <h3>Usage</h3> If your filter is configured with a number of rules, then any
 * dictionary/message placed onto property <i>inputMessages</i> will be filtered using these rules.
 * Dictionaries that pass this filtering stage will be placed onto property <i>acceptedMessages</i>,
 * and those that fail will be placed onto property <i>rejectedMessages</i>. <h3>Configuration</h3>
 * <h4>Configuration using default rule types</h4> This component is currently configured to support
 * the specification of filters using the following types of rule:
 * <ul>
 * <li>&lt;field name&gt; contains &lt;text&gt;
 * <li>&lt;field name&gt; doesn't contain &lt;text&gt;
 * <li>&lt;field name&gt; is &lt;text&gt;
 * <li>&lt;field name&gt; isn't &lt;text&gt;
 * <li>&lt;field name&gt; starts with &lt;text&gt;
 * <li>&lt;field name&gt; ends with &lt;text&gt;
 * </ul>
 * To configure the filter, you provide instances of these rule types to properties <i>allRules</i>
 * and <i>anyRules</i>. For example, if you wanted a filter allowing any dictionary through which
 * has a <i>from</i> field containing <tt>sre@cs.nott.ac.uk</tt> but not
 * <tt>stefan_egglestone@hotmail.com</tt>, you would set <i>allRules</i> to
 * <tt>{from contains sre@cs.nott.ac.uk,from doesn't contain stefan_egglestone@hotmail.com}</tt> <h4>
 * Configuration with user-defined rule types</h4> Hopefully, the default rule types will be
 * sufficient for the specification of most filters. But, they can be replaced, with other rule
 * types using different keywords. Note that this is not an easy process to perform, requiring
 * knowledge of regular expressions. If you want to do it, you'll need to modify the following
 * properties:
 * <ul>
 * <li><i>configRegularExpressions</i> - provide regular expressions that can be used to match your
 * keywords
 * <li><i>configKeywords</i> - provide your new keywords here
 * <li><i>configNegationDefinitions</i> - provide boolean values indicating whether keyword involves
 * negation or not - eg keyword "is" doesn't, but keyword "isn't" does.
 * <li><i>configTemplateRegularExpressions</i> - provide regular expressions that match a rule
 * constructed using your keywords. Such regular expressions should include the string <tt>text</tt>
 * , which will be replaced by the keyword to generate a valid regular expression, eg
 * <tt>(.)*text(.)*</tt>
 * </ul>
 * <P>
 * In each of these properties, you should place an array of values, with each element in the array
 * corresponding to one keyword that you are defining.
 * </P>
 * 
 * @classification Data/Dictionary
 * @defaultInputProperty inputMessages
 * @defaultOutputProperty acceptedMessages
 * @preferred
 */
public class DictionaryFilter implements Serializable
{
	// keys of dictionary must not include
	// keywords - then can assume that
	// first occurence of key word is
	// the key word

	DictionaryImpl[] inputDict = null;
	DictionaryImpl[] outputDict = null;
	DictionaryImpl[] rejectDict = null;

	RegexpMatcher[] allMatchers = null;
	RegexpMatcher[] anyMatchers = null;

	String[] allRules = null;
	String[] anyRules = null;

	public static final String[] DEFAULT_KEYWORDS = { "contains", "doesn't contain", "is", "isn't", "starts with",
														"ends with" };

	public static final String[] DEFAULT_KEYWORD_REGEXPS = { "(\\s)+contains(\\s)+", "(\\s)+doesn't contain(\\s)+",
															"(\\s)+is+(\\s)+", "(\\s)+isn't+(\\s)+",
															"(\\s)+starts with+(\\s)+", "(\\s)+ends with+(\\s)+" };

	public static final String[] DEFAULT_REGEXP_TEMPLATES = { "(.)*text(.)*", "(.)*text(.)*", "text", "text",
																"text(.)*", "(.)*text" };

	public static final boolean[] DEFAULT_NEGATION_DEFINITIONS = { false, true, false, true, false, false };

	// keywords to be used in filter rules
	// eg rule might be "<field> contains <value>"
	String[] keywords = DEFAULT_KEYWORDS;

	// this is an array of regular expressions
	// that can be used to recognize the keywords
	// in a rule. Each keyword is defined as starting
	// and ending in a whitespace character

	String[] keywordsRegexps = DEFAULT_KEYWORD_REGEXPS;

	// these regular expressoins are used to check to see
	// if the value defined for a particular key
	// matches a particular rule

	// so if our rule is
	// "to contains sre@cs.nott.ac.uk"
	// eg field to contains the string sre@cs.nott.ac.uk
	// then

	// 1. since "contains" is the first keyword in
	// array keywords above, then the first element of
	// regExpTemplates is selected
	// 2. string "text" in this regexp is replaced with
	// sre@cs.nott.ac.uk, producing (.)*sre@cs.nott.ac.uk(.)*
	// 3. for a given dictionary, then the to field of this
	// dictionary is tested against this regexp - ie to see
	// if it contains sre@cs.nott.ac.uk

	String[] regExpTemplates = DEFAULT_REGEXP_TEMPLATES;

	boolean[] negationRequired = DEFAULT_NEGATION_DEFINITIONS;

	boolean configured = false;

	String attention = "";

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public DictionaryFilter()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized DictionaryImpl[] getAcceptedMessages()
	{
		return outputDict;
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	public synchronized String[] getConfigAllRules()
	{
		return allRules;
	}

	public synchronized String[] getConfigAnyRules()
	{
		return anyRules;
	}

	public String[] getConfigKeywordRegularExpressions()
	{
		return keywordsRegexps;
	}

	public String[] getConfigKeywords()
	{
		return keywords;
	}

	public boolean[] getConfigNegationDefinitions()
	{
		return negationRequired;
	}

	public String[] getConfigTemplateRegularExpressions()
	{
		return regExpTemplates;
	}

	public boolean getConfigured()
	{
		return configured;
	}

	public synchronized DictionaryImpl[] getInputMessages()
	{
		return inputDict;
	}

	public synchronized DictionaryImpl[] getRejectedMessages()
	{
		return rejectDict;
	}

	public Object getTriggerReturnToDefaults()
	{
		return null;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setAttention(final String newValue)
	{
		final String oldValue = this.attention;
		this.attention = newValue;

		propertyChangeListeners.firePropertyChange("attention", oldValue, newValue);
	}

	public synchronized void setConfigAllRules(final String[] newRules)
	{

		final String[] oldRules = allRules;
		allRules = newRules;

		propertyChangeListeners.firePropertyChange("configAllRules", oldRules, newRules);

		// construct matchers using rules defined
		// in new rules, placing resultant matchers in array
		// allMatchers
		allMatchers = new RegexpMatcher[newRules.length];
		constructMatchers(newRules, allMatchers);

	}

	public synchronized void setConfigAnyRules(final String[] newRules)
	{

		final String[] oldRules = anyRules;
		anyRules = newRules;

		propertyChangeListeners.firePropertyChange("configAnyRules", oldRules, newRules);

		// construct matchers using rules defined
		// in new rules, placing resultant matchers in array
		// anyMatchers
		anyMatchers = new RegexpMatcher[newRules.length];
		constructMatchers(newRules, anyMatchers);
	}

	public void setConfigKeywordRegularExpressions(final String[] newRegExp)
	{
		final String[] oldRegExp = keywordsRegexps;
		keywordsRegexps = newRegExp;

		propertyChangeListeners.firePropertyChange("configKeywordRegularExpressions", oldRegExp, newRegExp);

		// now check if properties
		// configKeywords, configKeywordRegularExpressions,
		// configNegationDefined and
		// configTemplateRegularExpressions are consistent
		evaluateKeywordSetup();
	}

	public void setConfigKeywords(final String[] newKeywords)
	{
		final String[] oldKeywords = this.keywords;
		this.keywords = newKeywords;

		propertyChangeListeners.firePropertyChange("configKeywords", oldKeywords, newKeywords);

		// now check if properties
		// configKeywords, configKeywordRegularExpressions,
		// configNegationDefined and
		// configTemplateRegularExpressions are consistent
		evaluateKeywordSetup();
	}

	public void setConfigNegationDefinitions(final boolean[] newBooleans)
	{
		final boolean[] oldBooleans = negationRequired;
		negationRequired = newBooleans;

		propertyChangeListeners.firePropertyChange("configNegationDefinitions", oldBooleans, newBooleans);

		// now check if properties
		// configKeywords, configKeywordRegularExpressions,
		// configNegationDefined and
		// configTemplateRegularExpressions are consistent
		evaluateKeywordSetup();
	}

	public void setConfigTemplateRegularExpressions(final String[] newTemplates)
	{
		final String[] oldTemplates = regExpTemplates;
		regExpTemplates = newTemplates;

		propertyChangeListeners.firePropertyChange("configTemplateRegularExpressions", oldTemplates, newTemplates);

		// now check if properties
		// configKeywords, configKeywordRegularExpressions,
		// configNegationDefined and
		// configTemplateRegularExpressions are consistent
		evaluateKeywordSetup();
	}

	public synchronized void setInputMessages(final DictionaryImpl[] newDict)
	{
		final DictionaryImpl[] oldDict = inputDict;
		inputDict = newDict;

		propertyChangeListeners.firePropertyChange("inputMessages", oldDict, newDict);

		// causes filter to examine current input
		// dictionary, apply filter to it, and modify
		// output if required
		applyFilter();
	}

	public void setTriggerReturnToDefaults(final Object trigger)
	{
		if (trigger != null)
		{
			setConfigNegationDefinitions(DEFAULT_NEGATION_DEFINITIONS);
			setConfigKeywords(DEFAULT_KEYWORDS);
			setConfigKeywordRegularExpressions(DEFAULT_KEYWORD_REGEXPS);
			setConfigTemplateRegularExpressions(DEFAULT_REGEXP_TEMPLATES);
		}
	}

	void applyFilter()
	{
		final DictionaryImpl[] inputMessages = getInputMessages();

		if (getConfigured() == false)
		{
			// if not filter specified, then all message
			// get through
			setAcceptedMessages(inputMessages);
			setRejectedMessages(null);
		}
		else
		{
			if (inputDict != null)
			{
				// now see which of the dictionaries
				// in the input are accepted by the filter
				// and which are rejected

				final Vector acceptedVec = new Vector();
				final Vector rejectedVec = new Vector();

				for (final DictionaryImpl inputMessage : inputMessages)
				{
					if (inputMessage != null)
					{
						final Hashtable messageHash = inputMessage.getHashtable();

						if (matchesAll(messageHash) && matchesAny(messageHash))
						{
							acceptedVec.add(inputMessage);
						}
						else
						{
							rejectedVec.add(inputMessage);
						}
					}
				}

				// now make messages available

				if (acceptedVec.size() == 0)
				{
					setAcceptedMessages(null);
				}
				else
				{
					final DictionaryImpl[] accMes = (DictionaryImpl[]) (acceptedVec
							.toArray(new DictionaryImpl[acceptedVec.size()]));
					setAcceptedMessages(accMes);

				}

				if (rejectedVec.size() == 0)
				{
					setRejectedMessages(null);
				}
				else
				{
					final DictionaryImpl[] accMes = (DictionaryImpl[]) (rejectedVec
							.toArray(new DictionaryImpl[rejectedVec.size()]));
					setRejectedMessages(accMes);

				}
			}
			else
			{
				setAcceptedMessages(null);
				setRejectedMessages(null);
			}
		}
	}

	String constructMatchers(final String[] rules, final RegexpMatcher[] targetArray)
	{
		// construct matchers based on the rules
		// provided by a user

		// returns an error message if failed, null otherwise

		String errorMessage = null;

		for (int i = 0; i < rules.length; i++)
		{

			int count = -1;
			String[] bits = null;

			for (int j = 0; j < keywords.length; j++)
			{
				bits = rules[i].split(keywordsRegexps[j]);

				if (bits.length == 2)
				{
					// ie if the keyword was found in
					// this rule

					// record which keyword was found
					count = j;
					break;
				}
			}

			if (count != -1)
			{
				final String field = bits[0];
				final String toSearch = bits[1];

				final String template = regExpTemplates[count];
				final String regExp = template.replaceFirst("text", toSearch);

				final RegexpMatcher rm = new RegexpMatcher(field, regExp, negationRequired[count]);

				targetArray[i] = rm;
			}
			else
			{
				errorMessage = "Error in rule " + i + ": does not use valid keyword";
				// todo: deal with invalid rule
			}

			if (errorMessage != null)
			{
				// if an error has been reported in
				// creating a rule, then go no further
				break;
			}
		}

		// now see if we have an error message. If so, then
		// give user a warning, and set configured to false
		// (indicating that filter is not functional)

		if (errorMessage != null)
		{
			setAttention(errorMessage);
			setConfigured(false);
			applyFilter();
		}
		else
		{
			setAttention("Rules OK");
			setConfigured(true);
			applyFilter();
		}

		return errorMessage;
	}

	void evaluateKeywordSetup()
	{
		final int length = keywords.length;

		if (keywordsRegexps.length != length)
		{
			setAttention("Property configKeywordRegularExpressions does not have as many entries as property configKeywords");
			setConfigured(false);
			return;
		}

		if (negationRequired.length != length)
		{
			setAttention("Property configNegationDefinitions does not have as many entries as property configKeywords");
			setConfigured(false);
			return;
		}

		if (regExpTemplates.length != length)
		{
			setAttention("Property configTemplateRegularExpressions does not have as many entries as property configKeywords");
			setConfigured(false);
			return;
		}

		// now check to see if existing rules fit with
		// the new keywords that have been supplied

		// will set configured to true if this is the case

		if ((anyRules != null) && (anyRules.length > 0))
		{
			anyMatchers = new RegexpMatcher[anyRules.length];
			constructMatchers(anyRules, anyMatchers);
		}

		if ((allRules != null) && (allRules.length > 0))
		{
			allMatchers = new RegexpMatcher[allRules.length];
			constructMatchers(allRules, allMatchers);
		}
	}

	boolean matchesAll(final Hashtable hash)
	{
		if (allMatchers == null)
		{
			return true;
		}
		else
		{
			boolean matches = true;

			for (int i = 0; i < allMatchers.length; i++)
			{
				if (!(allMatchers[i].matches(hash)))
				{
					matches = false;
					break;
				}
			}

			return matches;
		}
	}

	boolean matchesAny(final Hashtable hash)
	{
		if (anyMatchers == null)
		{
			return true;
		}
		else
		{
			boolean matches = false;

			for (final RegexpMatcher anyMatcher : anyMatchers)
			{
				if (anyMatcher.matches(hash))
				{
					matches = true;
					break;
				}
			}

			return matches;
		}
	}

	void setupProperties(final String errorMessage)
	{
		if (errorMessage != null)
		{
			setAttention(errorMessage);
			setConfigured(false);
			applyFilter();
		}
		else
		{
			setAttention("Rules OK");
			setConfigured(true);
			applyFilter();
		}
	}

	protected synchronized void setAcceptedMessages(final DictionaryImpl[] newDict)
	{
		final DictionaryImpl[] oldDict = outputDict;
		outputDict = newDict;

		propertyChangeListeners.firePropertyChange("acceptedMessages", oldDict, newDict);
	}

	protected void setConfigured(final boolean configured)
	{
		final boolean oldConfigured = this.configured;
		this.configured = configured;

		propertyChangeListeners.firePropertyChange("configured", oldConfigured, configured);

	}

	protected synchronized void setRejectedMessages(final DictionaryImpl[] newDict)
	{
		final DictionaryImpl[] oldDict = rejectDict;
		rejectDict = newDict;

		propertyChangeListeners.firePropertyChange("rejectedMessages", oldDict, newDict);
	}
}

class RegexpMatcher
{
	Pattern pattern;
	String field;
	boolean negationRequired;

	RegexpMatcher(final String field, final String regexp, final boolean negationRequired)
	{
		final int flagExp = Pattern.DOTALL + Pattern.CASE_INSENSITIVE;

		pattern = Pattern.compile(regexp, flagExp);
		this.field = field;
		this.negationRequired = negationRequired;
	}

	boolean matches(final Hashtable hash)
	{
		// first, get the relevant field

		if (!hash.containsKey(field))
		{
			return false;
		}
		else
		{
			final Object value = hash.get(field);

			if (!(value instanceof StringBoxImpl))
			{
				return false;
			}
			else
			{
				final String stringValue = ((StringBoxImpl) value).value;

				final Matcher m = pattern.matcher(stringValue);

				if (negationRequired)
				{
					return !(m.matches());
				}
				else
				{
					return (m.matches());
				}
			}
		}
	}
}
