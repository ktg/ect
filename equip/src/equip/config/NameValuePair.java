/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
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
/* NameValuePair 
 * autogenerated from ../../include/equip_config_types.idl
 * by eqidl
 * DO NOT MODIFY
 */


package equip.config;

import equip.runtime.ObjectInputStream;
import equip.runtime.ObjectOutputStream;
import equip.runtime.ValueBase;

class NameValuePair extends ValueBase
{

	/**
	 * Default no-arg constructor
	 */
	NameValuePair() {}

  /* member variables */
	/**
	 * name of name,value pair
	 */
	public String name;
	/**
	 * value of name,value pair
	 */
	public String value;
	/**
	 * priority (high value takes priority).
	 */
	public int rating;

	/**
	 * IDL-generated helper routine to get module name (currently <b>unimplemented</b>).
	 *
	 * @return name of this class's module
	 */
	public String getModuleName() { return null; }

	/**
	 * Standard IDL-generated equality test.
	 *
	 * @param c The object to be compared against this.
	 * @return true if this is equal to <code>c</code>
	 */
	public boolean equals(java.lang.Object c)
	{
		if (c == null) return false;
		if (!c.getClass().equals(getClass())) return false;
		return _equals_helper((NameValuePair) c);
	}

	/**
	 * Internal IDL-generated equality test helper
	 */
	public boolean _equals_helper(NameValuePair c)
	{
		if (c == null) return false;
		if (!super._equals_helper(c)) return false;
		if (name != c.name && (name == null || c.name == null || !name.equals(c.name)))
			return false;
		if (value != c.value && (value == null || c.value == null || !value.equals(c.value)))
			return false;
		if (c.rating != rating) return false;
		return true;
	}

	/**
	 * Standard IDL-generated template match test.
	 *
	 * @param c The object to be checked against this template.
	 * @return true if <code>this</code> (as a template) matches the argument
	 */
	public boolean matches(java.lang.Object c)
	{
		if (c == null || !(c instanceof NameValuePair)) return false;
		return _matches_helper((NameValuePair) c);
	}

	/**
	 * Internal IDL-generated match test helper
	 */
	public boolean _matches_helper(NameValuePair c)
	{
		if (c == null) return false;
		if (!super._matches_helper(c)) return false;
		if (name != null && (c.name == null || !name.equals(c.name))) return false;
		if (value != null && (c.value == null || !value.equals(c.value))) return false;
		if (c.rating != rating) return false;
		return true;
	}

	/**
	 * Internal IDL-generated serialisation helper. Used by {@link equip.runtime.ObjectInputStream} and {@link equip.runtime.ObjectOutputStream} only.
	 */
	public void writeObject(ObjectOutputStream out)
			throws java.io.IOException
	{
		out.writeObjectStart();
		out.writeString(name);
		out.writeString(value);
		out.writeInt(rating);
		out.writeObjectEnd();
	}

	/**
	 * Internal IDL-generated serialisation helper. Used by {@link ObjectInputStream} and {@link ObjectOutputStream} only.
	 */
	public void readObject(ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException,
			InstantiationException
	{
		in.readObjectStart();
		name = in.readString();
		value = in.readString();
		rating = in.readInt();
		in.readObjectEnd();
	}


} /* class NameValuePair */

/* EOF */
