/*
 <COPYRIGHT>

 Copyright (c) 2005-2006, University of Nottingham
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
package equip.ect.components.websearch;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * 
 * 
 * <H3>Description</H3>
 * 
 * <H3>Installation</H3>
 * 
 * <H3>Configuration</H3>
 * 
 * <H3>Usage</H3>
 * 
 * <H3>Technical Details</H3>
 * 
 * @author humble
 */
public class WebSearch implements Serializable
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{

		/*
		 * // Parse the command line if (args.length != 3) { printUsageAndExit(); }
		 */
		final String clientKey = "BOfvyOdQFHIjoDQDnbO/SLr5TFkLr6nF";
		final String directive = args[0];
		final String directiveArg = args[1];

		// Report the arguments received
		System.out.println("Parameters:");
		System.out.println("Client key = " + clientKey);
		System.out.println("Directive  = " + directive);
		System.out.println("Args       = " + directiveArg);

		// Create a Google Search object, set our authorization key
		final GoogleSearch s = new GoogleSearch();
		s.setKey(clientKey);

		s.setProxyHost("wwwcache.lancs.ac.uk");
		s.setProxyPort(8080);
		// Depending on user input, do search or cache query, then print out
		// result
		try
		{
			if (directive.equalsIgnoreCase("search"))
			{
				s.setQueryString(directiveArg);
				final GoogleSearchResult r = s.doSearch();
				System.out.println("Google Search Results:");
				System.out.println("======================");
				System.out.println(r.toString());
			}
			else if (directive.equalsIgnoreCase("cached"))
			{
				System.out.println("Cached page:");
				System.out.println("============");
				final byte[] cachedBytes = s.doGetCachedPage(directiveArg);
				// Note - this conversion to String should be done with
				// reference
				// to the encoding of the cached page, but we don't do that
				// here.
				final String cachedString = new String(cachedBytes);
				System.out.println(cachedString);
			}
			else if (directive.equalsIgnoreCase("spell"))
			{
				System.out.println("Spelling suggestion:");
				final String suggestion = s.doSpellingSuggestion(directiveArg);
				System.out.println(suggestion);
			}
		}
		catch (final GoogleSearchFault f)
		{
			System.out.println("The call to the Google Web APIs failed:");
			System.out.println(f.toString());
		}

	};

	protected static Hashtable googleResultToDictionary(final ResultElement resultElement)
	{
		final Hashtable dict = new Hashtable(4);
		dict.put("url", resultElement.getURL());
		dict.put("title", resultElement.getTitle());
		dict.put("snippet", resultElement.getSnippet());
		dict.put("summary", resultElement.getSummary());

		return dict;
	}

	private String clientKey = "BOfvyOdQFHIjoDQDnbO/SLr5TFkLr6nF";

	private String searchArgs = "+equator +ect";

	private int maxNrResults = 10;

	private Hashtable[] results = null;

	private GoogleSearch searchClient;

	private boolean safeSearch;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public WebSearch()
	{
		searchClient = new GoogleSearch();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * @return Returns the clientKey.
	 */
	public String getClientKey()
	{
		return clientKey;
	}

	/**
	 * @return Returns the maxResults.
	 */
	public int getMaxNrResults()
	{
		return maxNrResults;
	}

	/**
	 * @return Returns the results.
	 */
	public Hashtable[] getResults()
	{
		return results;
	}

	/**
	 * @return Returns the searchArgs.
	 */
	public String getSearchArgs()
	{
		return searchArgs;
	}

	/**
	 * @return Returns the safeSearch.
	 */
	public boolean isSafeSearch()
	{
		return safeSearch;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * @param clientKey
	 *            The clientKey to set.
	 */
	public void setClientKey(final String clientKey)
	{
		final String old = this.clientKey;
		this.clientKey = clientKey;
		searchClient.setKey(clientKey);
		propertyChangeListeners.firePropertyChange("clientKey", old, clientKey);
	}

	/**
	 * @param maxResults
	 *            The maxResults to set.
	 */
	public void setMaxNrResults(final int maxNrResults)
	{
		final int old = this.maxNrResults;
		this.maxNrResults = maxNrResults;
		searchClient.setMaxResults(maxNrResults);
		propertyChangeListeners.firePropertyChange("maxNrResults", old, maxNrResults);
	}

	/**
	 * @param safeSearch
	 *            The safeSearch to set.
	 */
	public void setSafeSearch(final boolean safeSearch)
	{
		final boolean old = this.safeSearch;
		this.safeSearch = safeSearch;
		searchClient.setSafeSearch(safeSearch);
		propertyChangeListeners.firePropertyChange("safeSearch", old, safeSearch);
	}

	/**
	 * @param searchArgs
	 *            The searchArgs to set.
	 */
	public void setSearchArgs(final String searchArgs)
	{
		final String old = this.searchArgs;
		this.searchArgs = searchArgs;
		if (!old.equals(searchArgs))
		{
			searchClient.setQueryString(searchArgs);
			doSearch();
		}
		propertyChangeListeners.firePropertyChange("searchArgs", old, searchArgs);
	}

	protected void doSearch()
	{
		synchronized (searchClient)
		{
			searchClient.setKey(clientKey);
			searchClient.setQueryString(searchArgs);
			searchClient.setMaxResults(maxNrResults);
			searchClient.setSafeSearch(safeSearch);

			try
			{

				final GoogleSearchResult gsr = searchClient.doSearch();
				final ResultElement[] results = gsr.getResultElements();
				final Hashtable[] dictResults = new Hashtable[results.length];
				for (int i = 0; i < results.length; i++)
				{
					dictResults[i] = googleResultToDictionary(results[i]);
				}
				setResults(dictResults);
			}
			catch (final GoogleSearchFault e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * @param results
	 *            The results to set.
	 */
	protected void setResults(final Hashtable[] results)
	{
		final Hashtable[] old = this.results;
		this.results = results;
		propertyChangeListeners.firePropertyChange("results", old, results);
	}

}
