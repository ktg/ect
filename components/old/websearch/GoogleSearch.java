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

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;

public class GoogleSearch implements GoogleSearchPort
{

	private GoogleSearchPort googleSearchPort;

	private String key;

	private String searchArgs;

	private boolean safeSearch;

	private String proxyHost;

	private int maxNrResults;

	private int proxyPort;

	private int start = 1;

	private boolean filter = false;

	private String ie = "UTF-8"; // input encoding

	private String lr = "lang_en"; // language code

	private String oe = "UTF-8"; // output encoding

	private String restrict = "";

	public GoogleSearch()
	{

		try
		{

			final GoogleSearchServiceLocator serviceLocator = new GoogleSearchServiceLocator();
			this.googleSearchPort = serviceLocator.getGoogleSearchPort();
		}
		catch (final ServiceException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] doGetCachedPage(final String directiveArg) throws GoogleSearchFault
	{
		try
		{
			return doGetCachedPage(key, directiveArg);
		}
		catch (final RemoteException re)
		{
			throw (new GoogleSearchFault(re));
		}
	}

	@Override
	public byte[] doGetCachedPage(final String key, final String url) throws RemoteException
	{
		return googleSearchPort.doGetCachedPage(key, url);
	}

	@Override
	public GoogleSearchResult doGoogleSearch(final String key, final String q, final int start, final int maxResults,
	                                         final boolean filter, final String restrict, final boolean safeSearch, final String lr, final String ie,
	                                         final String oe) throws RemoteException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public GoogleSearchResult doSearch() throws GoogleSearchFault
	{

		try
		{
			return googleSearchPort.doGoogleSearch(key, searchArgs, start, maxNrResults, filter, restrict, safeSearch,
					lr, ie, oe);
		}
		catch (final RemoteException e)
		{
			throw (new GoogleSearchFault(e));
		}
	}

	public String doSpellingSuggestion(final String directiveArg) throws GoogleSearchFault
	{
		try
		{
			return doSpellingSuggestion(key, directiveArg);
		}
		catch (final RemoteException re)
		{
			throw (new GoogleSearchFault(re));
		}
	}

	@Override
	public String doSpellingSuggestion(final String key, final String phrase) throws RemoteException
	{
		return googleSearchPort.doSpellingSuggestion(key, phrase);
	}

	public void setKey(final String clientKey)
	{
		this.key = clientKey;
	}

	public void setMaxResults(final int maxNrResults)
	{
		this.maxNrResults = maxNrResults;

	}

	public void setProxyHost(final String proxyHost)
	{
		this.proxyHost = proxyHost;

	}

	public void setProxyPort(final int proxyPort)
	{
		this.proxyPort = proxyPort;

	}

	public void setQueryString(final String searchArgs)
	{
		this.searchArgs = searchArgs;

	}

	public void setSafeSearch(final boolean safeSearch)
	{
		this.safeSearch = safeSearch;

	}

}
