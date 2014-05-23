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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect;

import equip.data.GUID;
import equip.data.StringBoxImpl;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;

/**
 * This is the tuple used to communicate individual RDF Statements. Really a convience class to make
 * the tuple implementation more readable. Currently we put out <br>
 * <p/>
 * Field 0 -String set to "RDFStatement"<br>
 * Field 1 - String set to offset for fields <br>
 * Field 2 - String set to Subject (URL) Field 3 - String set to Predicate (URL) <br>
 * Field 4 = String set to Object/Value (String) <br>
 */
public class RDFStatement extends CompInfo
{

	/**
	 * dublin core 'title' predicate
	 */
	public static final String DC_TITLE = "http://purl.org/dc/elements/1.1/title";

	public static final String ECT_ACTIVE_TITLE = "http://www.equator.ac.uk/ect_metadata/active_title";
	// private static int TYPE_INDEX = 0;
	public static final int SUBJECT_INDEX = 2;
	// public Tuple tuple = new TupleImpl();
	/**
	 * GUID url prefix
	 */
	public static final String GUID_NAMESPACE = "http://www.equator.ac.uk/equip/guid#";
	public static String TYPE = "RDFStatement2";
	/**
	 * GUID pattern
	 */
	protected static java.util.regex.Pattern guidPattern;
	private static int NO_OF_FIELDS = 3;
	private static int PREDICATE_INDEX = 3;
	private static int OBJECT_INDEX = 4;

	public RDFStatement(final GUID id)
	{
		super(id, TYPE, NO_OF_FIELDS); // create the RDFStatement
	}

	/**
	 * convenience cons
	 */
	public RDFStatement(final GUID id, final String subject, final String predicate, final String object)
	{
		super(id, TYPE, NO_OF_FIELDS); // create the RDFStatement
		setSubject(subject);
		setPredicate(predicate);
		setObject(object);
	}

	public RDFStatement(final TupleImpl tuple)
	{
		super(tuple);
	}

	/**
	 * GUID to url
	 */
	public static String GUIDToUrl(final GUID id)
	{
		final String guids = id.toString();
		return GUID_NAMESPACE + guids.substring(1, guids.length() - 1);
	}

	/**
	 * url to GUID
	 */
	public synchronized static GUID urlToGUID(final String url)
	{
		if (url == null)
		{
			return null;
		}
		if (!url.startsWith(GUID_NAMESPACE))
		{
			return null;
		}
		final String guids = "[" + url.substring(GUID_NAMESPACE.length()) + "]";
		/*
		 * GUID toString: return "["+((host_id >> 24) & 0xff)+"."+ ((host_id >> 16) & 0xff)+"."+
		 * ((host_id >> 8) & 0xff)+"."+ ((host_id) & 0xff)+":"+ ((proc_id >> 16) & 0xffff)+"."+
		 * ((proc_id) & 0xffff)+":"+ item_id+":"+time_s+"]";
		 */
		if (guidPattern == null)
		{
			guidPattern = java.util.regex.Pattern
					.compile("^\\[(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+):(\\d+)\\.(\\d+):(\\d+):(\\d+)\\]");
		}
		final java.util.regex.Matcher matcher = guidPattern.matcher(guids);
		if (!matcher.matches())
		{
			System.err
					.println("WARNING: GUID URL " + url + " -> GUID string " + guids + " does not match GUID pattern");
			return null;
		}
		try
		{
			final GUID guid = new equip.data.GUIDImpl();
			guid.host_id = (new Integer(matcher.group(1)).intValue() << 24)
					| (new Integer(matcher.group(2)).intValue() << 16)
					| (new Integer(matcher.group(3)).intValue() << 8) | (new Integer(matcher.group(4)).intValue());
			guid.proc_id = (new Integer(matcher.group(5)).intValue() << 16)
					| (new Integer(matcher.group(6)).intValue());
			guid.item_id = (new Integer(matcher.group(7)).intValue());
			guid.time_s = (new Integer(matcher.group(8)).intValue());

			// test/temp check
			if (!guid.toString().equals(guids))
			{
				System.err.println("WARNING: GUID URL " + url + " -> GUID string " + guids + " -> GUID " + guid
						+ " - reverse check failed");
			}
			return guid;
		}
		catch (final Exception e)
		{
			System.err.println("WARNING: GUID URL " + url + " -> GUID string " + guids + " raises exception: " + e);
			e.printStackTrace(System.err);
			// return null;
		}
		return null;
	}

	/**
	 * type-specific copy collect
	 */
	public RDFStatement[] copyCollectAsRDFStatement(final DataspaceBean dataspace) throws DataspaceInactiveException
	{

		final equip.data.ItemData[] ret = dataspace.copyCollect(this.tuple);
		if (ret != null)
		{
			final RDFStatement[] returnvals = new RDFStatement[ret.length];

			for (int i = 0; i < ret.length; i++)
			{
				returnvals[i] = new RDFStatement((TupleImpl) ret[i]);
			}
			return returnvals;
		}
		else
		{
			return null;
		}
	}

	/**
	 * object/value field getter
	 */
	public String getObject()
	{
		if (tuple.fields.length >= (OBJECT_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[OBJECT_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	/**
	 * object/value field setter
	 */
	public void setObject(final String url)
	{
		if (tuple.fields.length >= (OBJECT_INDEX + 1))
		{
			tuple.fields[OBJECT_INDEX] = new StringBoxImpl(url);
		}
	}

	/**
	 * predicate field getter
	 */
	public String getPredicate()
	{
		if (tuple.fields.length >= (PREDICATE_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[PREDICATE_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	/**
	 * predicate field setter
	 */
	public void setPredicate(final String url)
	{
		if (tuple.fields.length >= (PREDICATE_INDEX + 1))
		{
			tuple.fields[PREDICATE_INDEX] = new StringBoxImpl(url);
		}
	}

	/**
	 * subject field getter
	 */
	public String getSubject()
	{
		if (tuple.fields.length >= (SUBJECT_INDEX + 1))
		{
			return ((StringBoxImpl) tuple.fields[SUBJECT_INDEX]).value;
		}
		else
		{
			return null;
		}
	}

	/**
	 * subject field setter
	 */
	public void setSubject(final String url)
	{
		if (tuple.fields.length >= (SUBJECT_INDEX + 1))
		{
			tuple.fields[SUBJECT_INDEX] = new StringBoxImpl(url);
		}
	}
}
