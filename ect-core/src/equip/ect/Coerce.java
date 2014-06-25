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

import equip.data.DictionaryImpl;
import equip.data.StringBox;
import equip.data.StringBoxImpl;
import equip.runtime.ValueBase;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities to coerce between types, esp for setting properties from source properties of another
 * type.
 */
public class Coerce
{
	private static final NumberFormat numberFormatter = new DecimalFormat("#,##0.###");

	/**
	 * simple/primitive class
	 */
	public static boolean isSimpleClass(final Class<?> c)
	{
		return c.isPrimitive() || Number.class.isAssignableFrom(c) || Boolean.class.equals(c)
				|| Character.class.equals(c);
	}

	/**
	 * check for stringified array
	 */
	public static boolean isStringifiedArray(final String s)
	{
		final String st = s.trim();
		if (!st.startsWith("{") || !st.endsWith("}")) { return false; }
		// but first, check it isn't a stringified dictionary (look for name=...)
		final int iequals = st.indexOf('=');
		boolean isDictionary = iequals >= 0;
		for (int j = 1; isDictionary && j < iequals; j++)
		{
			final char c = st.charAt(j);
			if (c == '"' || c == ',' || c == '{')
			{
				isDictionary = false;
			}
		}
		return !isDictionary;
	}

	/**
	 * check for stringified dictionary
	 */
	public static boolean isStringifiedDictionary(final String s)
	{
		final String st = s.trim();
		if (!st.startsWith("{") || !st.endsWith("}")) { return false; }
		// but first, check it isn't a stringified dictionary (look for name=...)
		final int iequals = st.indexOf('=');
		boolean isDictionary = iequals >= 0;
		for (int j = 1; isDictionary && j < iequals; j++)
		{
			final char c = st.charAt(j);
			if (c == '"' || c == ',' || c == '{')
			{
				isDictionary = false;
			}
		}
		return isDictionary;
	}

	/**
	 * testing main
	 */
	public static void main(final String args[])
	{
		try
		{
			if (args.length < 2)
			{
				System.err.println("Usage: Coerce <stringvalue> <toclass1> ...");
				System.exit(-1);
			}
			Object value = args[0];
			for (int i = 1; i < args.length; i++)
			{
				final String toclassname = args[i];
				final Class<?> toclass = Class.forName(toclassname);
				System.out.println("Convert " + value.getClass() + " " + value + " to class " + toclass + "...");
				value = toClass(value, toclass);
				if (value instanceof DictionaryImpl)
				{
					System.out.println("Dictionary:");
					final DictionaryImpl d = (DictionaryImpl) value;
					for (int j = 0; d.entries != null && j < d.entries.length; j++)
					{
						System.out.println("  " + d.entries[j].name + " = " + d.entries[j].value);
					}
				}
				if (value instanceof DictionaryImpl[])
				{
					final DictionaryImpl ds[] = (DictionaryImpl[]) value;
					for (int k = 0; k < ds.length; k++)
					{
						System.out.println("Dictionary " + k + ":");
						for (int j = 0; ds[k].entries != null && j < ds[k].entries.length; j++)
						{
							System.out.println("  " + ds[k].entries[j].name + " = " + ds[k].entries[j].value);
						}
					}
				}
			}
			System.out.println("Ends with " + value.getClass() + " " + value);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * split stringified array
	 */
	public static String[] splitStringifiedArray(final String s)
	{
		final String st = s.trim();
		final String st2 = st.substring(1, st.length() - 1);

		// handle string quotes & escapes
		int i;
		boolean inQuote = false, hadComma = false;
		int brackets = 0;
		StringBuffer buf = new StringBuffer();
		final List<String> rv = new ArrayList<String>();
		for (i = 0; i < st2.length(); i++)
		{
			boolean handle = false;
			char nc = st2.charAt(i);
			if (inQuote && nc == '\\')
			{
				if (brackets != 0)
				{
					buf.append(nc);
				}
				// escape
				i++;
				if (i < st2.length())
				{
					nc = st2.charAt(i);
					if (brackets != 0)
					{
						buf.append(nc);
					}
					else
					{
						// standard escape
						if (nc == 'n')
						{
							buf.append('\n');
						}
						if (nc == 't')
						{
							buf.append('\t');
						}
						else
						{
							buf.append(nc);
						}
					}
				}
			}
			else if (inQuote && nc == '"')
			{
				if (brackets != 0)
				{
					buf.append(nc);
				}
				inQuote = false;
			}
			else if (!inQuote && nc == '"')
			{
				if (brackets != 0)
				{
					buf.append(nc);
				}
				inQuote = true;
			}
			else if (!inQuote && nc == '{')
			{
				buf.append(nc);
				brackets++;
			}
			else if (!inQuote && nc == '}')
			{
				buf.append(nc);
				brackets--;
			}
			else if (!inQuote && nc == ',' && brackets == 0)
			{
				// discard and handle
				handle = true;
				hadComma = true;
			}
			else if (inQuote || (!Character.isWhitespace(nc)) || brackets != 0)
			{
				// discard whitespace outside quotes at top
				buf.append(nc);
			}
			if (i + 1 >= st2.length() && (hadComma || buf.length() > 0))
			{
				handle = true;
			}

			if (handle)
			{
				rv.add(buf.toString());
				buf = new StringBuffer();
				handle = false;
			}
		}
		return (String[]) rv.toArray(new String[rv.size()]);
	}

	/**
	 * return an object assignable to the requested class from the given value
	 */
	public static <T> T toClass(final Object o, final Class<T> cls) throws ClassNotFoundException, IOException
	{
		return toClass(o, cls, false);
	}

	/**
	 * return an object assignable to the requested class from the given value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T toClass(Object o, Class<T> cls, final boolean escapeStrings) throws ClassNotFoundException,
			IOException
	{
		if (escapeStrings && o instanceof String)
		{
			final StringBuffer buf = new StringBuffer();
			appendEscapedString(buf, (String) o);
			o = buf.toString();
		}
		Class<?> cls2 = cls;
		// ok already?
		if (o != null && cls.isAssignableFrom(o.getClass())) { return (T)o; }
		java.lang.reflect.Field valuef = null;
		// special handling for ValueBase subclasses - our own container/transport classes
		if (o instanceof Map)
		{
			o = toDictionary((Map) o);
		}
		if (cls.equals(Map.class))
		{
			// convert via dictionary
			final DictionaryImpl d = (DictionaryImpl) toClass(o, DictionaryImpl.class);
			if (d == null) { return null; }
			return (T)toHashtable(d);
		}
		if (o instanceof ValueBase)
		{
			// except for Dictionary, value field is what we want...
			if (!(o instanceof DictionaryImpl))
			{
				try
				{
					valuef = o.getClass().getField("value");
					o = valuef.get(o);
					// late string arrival?
					if (escapeStrings && o instanceof String)
					{
						final StringBuffer buf = new StringBuffer();
						appendEscapedString(buf, (String) o);
						o = buf.toString();
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in Coerce getting field value from ValueBase subclass " + o);
					e.printStackTrace(System.err);
				}
			}
		}
		boolean mapValuebase = false;
		if (ValueBase.class.equals(cls))
		{
			// just the abstract base class implies do our best!!
			if (o == null) { return null; }
			String arraySuffix = "", suffix = "BoxImpl";
			Class<?> c2 = o.getClass();
			if (c2.isArray())
			{
				arraySuffix = "Array";
				c2 = c2.getComponentType();
				if (c2.isArray())
				{
					arraySuffix = "Array2D";
					c2 = c2.getComponentType();
				}
			}
			String typePrefix = "";
			if (Double.class.equals(c2) || Double.TYPE.equals(c2))
			{
				typePrefix = "Double";
			}
			else if (Float.class.equals(c2) || Float.TYPE.equals(c2))
			{
				typePrefix = "Float";
			}
			else if (Short.class.equals(c2) || Short.TYPE.equals(c2))
			{
				typePrefix = "Short";
			}
			else if (Integer.class.equals(c2) || Integer.TYPE.equals(c2))
			{
				typePrefix = "Int";
			}
			else if (Long.class.equals(c2) || Long.TYPE.equals(c2))
			{
				typePrefix = "Long";
			}
			else if (Boolean.class.equals(c2) || Boolean.TYPE.equals(c2))
			{
				typePrefix = "Boolean";
			}
			else if (Character.class.equals(c2) || Character.TYPE.equals(c2))
			{
				typePrefix = "Char";
			}
			else if (Byte.class.equals(c2) || Byte.TYPE.equals(c2))
			{
				typePrefix = "Byte";
			}
			else if (String.class.equals(c2))
			{
				typePrefix = "String";
			}
			else if (DictionaryImpl.class.equals(c2))
			{
				// default
				if (arraySuffix.equals(""))
				{
					typePrefix = "Dictionary";
					suffix = "Impl";
				}
				else
				{
					// no 2D
					arraySuffix = "Array";
					typePrefix = "ValueBase";
				}
			}
			else if (Serializable.class.isAssignableFrom(c2))
			{
				System.err.println("WARNING: No natural ValueBase subtype found for class " + c2);
				// default
				if (arraySuffix.equals(""))
				{
					// try a dictionary :-)
					typePrefix = "Dictionary";
					suffix = "Impl";
				}
				else
				{
					// no 2D
					arraySuffix = "Array";
					typePrefix = "ValueBase";
				}
			}
			else
			{
				System.err.println("WARNING: No natural ValueBase subtype found for class " + c2);
				// default
				typePrefix = "String";
			}
			try
			{
				cls = (Class<T>) Class.forName("equip.data." + typePrefix + arraySuffix + suffix);
				// debug
				// System.out.println("Boxing a "+o.getClass()+" as a "+cls);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR finding derived ValueBase class " + "equip.data." + typePrefix + arraySuffix
						+ suffix + ": " + e);
				e.printStackTrace(System.err);
				return null;
			}
		}
		if (ValueBase.class.isAssignableFrom(cls))
		{
			// except for Dictionary, value field is what we want...
			if (!DictionaryImpl.class.isAssignableFrom(cls))
			{
				mapValuebase = true;

				try
				{
					valuef = cls.getField("value");
					cls2 = valuef.getType();
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in Coerce getting field value type from ValueBase subclass " + cls + ": "
							+ e);
					e.printStackTrace(System.err);
				}
			}
		}

		Object r = toClass2(o, cls2, escapeStrings);

		if (mapValuebase)
		{
			if (r == null) { return null; }
			if (valuef != null)
			{
				try
				{
					final ValueBase vb = (ValueBase) cls.newInstance();
					valuef.set(vb, r);
					r = vb;
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in Coerce setting field value for ValueBase subclass " + cls + ": " + e);
					e.printStackTrace(System.err);
				}
			}
		}
		return (T)r;
	}

	public static Object toClass2(Object o, Class<?> cls, final boolean escapeStrings) throws ClassNotFoundException,
			IOException
	{
		// repeat in case of ValueBase Box
		if (o != null && cls.isAssignableFrom(o.getClass())) { return o; }

		if (cls.isArray())
		{
			if (o == null) { return null; // or zero-size array??
			}
			if (o.getClass().isArray())
			{
				final Object res = Array.newInstance(cls.getComponentType(), Array.getLength(o));
				for (int i = 0; i < Array.getLength(o); i++)
				{
					Array.set(res, i, toClass(Array.get(o, i), cls.getComponentType()));
				}
				return res;
			}
			else if (o instanceof String)
			{
				final String s = (String) o;
				if (isStringifiedArray(s))
				{
					final String[] els = splitStringifiedArray(s);
					final Object res = Array.newInstance(cls.getComponentType(), els.length);
					for (int i = 0; i < els.length; i++)
					{
						Array.set(res, i, toClass(els[i], cls.getComponentType()));
					}
					return res;
				}
			}

			// assume singleton element
			final Object res = Array.newInstance(cls.getComponentType(), 1);
			Array.set(res, 0, toClass(o, cls.getComponentType()));
			return res;

		}
		else if (o != null && o.getClass().isArray() && !cls.equals(String.class))
		{
			// ie an array being cast to a class that is not an array and not String
			// use first element (if any, else null)
			if (Array.getLength(o) == 0)
			{
				o = null;
			}
			else
			{
				o = Array.get(o, 0);
			}
			return toClass(o, cls);
		}
		else if (o instanceof String && isStringifiedArray((String) o))
		{
			final String els[] = splitStringifiedArray((String) o);
			if (els.length == 0)
			{
				o = null;
			}
			else
			{
				o = els[0];
			}
		}
		else if (isSimpleClass(cls))
		{
			if (o instanceof String && isStringifiedDictionary((String) o))
			{
				o = toClass(o, DictionaryImpl.class);
			}
			if (o instanceof DictionaryImpl)
			{
				final DictionaryImpl d = (DictionaryImpl) o;
				// try value field
				return toClass(d.get("value"), cls);
			}
		}

		if (cls.isPrimitive())
		{
			// we will work instead with the reflection types
			if (cls.equals(Boolean.TYPE))
			{
				cls = Boolean.class;
			}
			else if (cls.equals(Byte.TYPE))
			{
				cls = Byte.class;
			}
			else if (cls.equals(Short.TYPE))
			{
				cls = Short.class;
			}
			else if (cls.equals(Integer.TYPE))
			{
				cls = Integer.class;
			}
			else if (cls.equals(Long.TYPE))
			{
				cls = Long.class;
			}
			else if (cls.equals(Float.TYPE))
			{
				cls = Float.class;
			}
			else if (cls.equals(Double.TYPE))
			{
				cls = Double.class;
			}
			else if (cls.equals(Character.TYPE))
			{
				cls = Character.class;
			}
			if (o == null)
			{
				// not null
				o = new Boolean(false);
			}
		}
		else if (o == null)
		{
			// not prim - can be null
			return null;
		}
		else if ((o instanceof String) && ((String) o).length() == 0)
		{
			// empty string -> null
			return null;
		}

		if (Number.class.isAssignableFrom(cls))
		{
			// to a number
			Number asNumber = null;
			if (o instanceof Number)
			{
				asNumber = (Number) o;
			}
			else if (o instanceof Boolean)
			{
				asNumber = new Integer(((Boolean) o).booleanValue() ? 1 : 0);
			}
			else
			{
				String asString = null;
				if (o instanceof String)
				{
					asString = (String) o;
				}
				else
				{
					asString = o.toString();
				}
				asString = asString.replaceAll(",", "");
				try
				{
					asNumber = new Double(asString);
				}
				catch (final NumberFormatException e)
				{
					// not a number; treat as boolean?
					if (asString.length() == 0 || asString.charAt(0) == '-' || asString.charAt(0) == 'n'
							|| asString.charAt(0) == 'f' || asString.charAt(0) == 'N' || asString.charAt(0) == 'F')
					{
						asNumber = new Integer(0);
					}
					else
					{
						asNumber = new Integer(1);
					}
				}
			}
			if (cls.equals(Integer.class))
			{
				return new Integer(asNumber.intValue());
			}
			else if (cls.equals(Long.class))
			{
				return new Long(asNumber.longValue());
			}
			else if (cls.equals(Short.class))
			{
				return new Short(asNumber.shortValue());
			}
			else if (cls.equals(Byte.class))
			{
				return new Byte(asNumber.byteValue());
			}
			else if (cls.equals(Float.class))
			{
				return new Float(asNumber.floatValue());
			}
			else if (cls.equals(Double.class)) { return new Double(asNumber.doubleValue()); }
		}
		else if (cls.equals(Boolean.class))
		{
			String asString = null;
			if (o instanceof String)
			{
				asString = (String) o;
			}
			else if (o instanceof Character)
			{
				asString = o.toString();
			}
			else
			{
				asString = o.toString();
			}
			if (asString.length() == 0 || asString.charAt(0) == '-' || asString.charAt(0) == 'n'
					|| asString.charAt(0) == 'f' || asString.charAt(0) == 'N' || asString.charAt(0) == 'F'
					|| asString.charAt(0) == '0')
			{
				return new Boolean(false);
			}
			else
			{
				return new Boolean(true);
			}
		}
		else if (cls.equals(String.class))
		{
			if (o.getClass().isArray())
			{
				final StringBuffer buf = new StringBuffer();
				buf.append("{");
				for (int i = 0; i < Array.getLength(o); i++)
				{
					final Object o2 = Array.get(o, i);
					final Object r2 = toClass(o2, String.class, true);
					if (r2 != null)
					{
						buf.append((String) r2);
					}
					if (i + 1 < Array.getLength(o))
					{
						buf.append(",");
					}
				}
				buf.append("}");
				return buf.toString();
			}
			if (o instanceof DictionaryImpl)
			{
				final DictionaryImpl d = (DictionaryImpl) o;
				// special case of single element "value"
				if (d.entries != null && d.entries.length == 1 && d.entries[0].name.equals("value")) { return toClass(	d.entries[0].value,
																														cls); }

				// dictionary to string
				final StringBuffer res = new StringBuffer();
				res.append("{");
				// class should be first (if present)
				final StringBox cb = (StringBox) d.get("class");
				boolean comma = false;
				if (cb != null)
				{
					res.append("class=\"");
					res.append(cb.value);
					res.append("\"");
					comma = true;
				}
				// Dictionary to String
				for (int i = 0; d.entries != null && i < d.entries.length; i++)
				{
					if (d.entries[i].name.equals("class"))
					{
						continue;
					}
					if (comma)
					{
						res.append(",");
					}
					res.append(d.entries[i].name);
					res.append("=");
					final String sv = (String) toClass(d.entries[i].value, String.class, true);
					if (sv != null)
					{
						res.append(sv);
					}
					comma = true;
				}
				// empty dictionary marker
				if (d.entries == null && d.entries.length == 0)
				{
					res.append("=");
				}
				res.append("}");
				return res.toString();
			}
			if (o.getClass().isPrimitive() || o instanceof Number || o instanceof Boolean || o instanceof Character)
			{
				// treat simply
				if (o instanceof Number)
				{
					return numberFormatter.format(o);
				}
				return o.toString();
			}
			if (o instanceof Serializable)
			{
				// default introspection -> properties string
				final StringBuffer res = new StringBuffer();
				res.append("{");
				res.append("class=\"");
				res.append(o.getClass().getName());
				res.append("\"");

				try
				{
					// public fields
					final Field fs[] = o.getClass().getFields();
					for (final Field element : fs)
					{
						final int mod = element.getModifiers();
						if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
						{
							continue;
						}
						res.append(",");
						res.append(element.getName());
						res.append("=");
						final Object v = element.get(o);
						final String sv = (String) toClass(v, String.class, true);
						if (sv != null)
						{
							res.append(sv);
						}
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR introspecting value " + o + ": " + e);
					e.printStackTrace(System.err);
				}
				res.append("}");
				return res.toString();
			}
			return o.toString();
		}
		else if (cls.equals(Character.class))
		{
			final String asString = o.toString();
			if (asString.length() == 0)
			{
				return new Character('?');
			}
			else
			{
				return new Character(asString.charAt(0));
			}
		}
		else if (cls.equals(DictionaryImpl.class) && o instanceof String)
		{
			// string -> dictionary
			final DictionaryImpl d = new DictionaryImpl();
			final String s = (String) o;

			if (isStringifiedDictionary(s))
			{
				final String els[] = splitStringifiedArray(s);
				int iequals = -1;
				for (final String el : els)
				{
					iequals = el.indexOf('=');
					if (iequals < 0)
					{
						System.err.println("ERROR: string -> dictionary missing field name in " + el);
					}
					else if (iequals > 0)
					{
						final String name = el.substring(0, iequals);
						final String vs = el.substring(iequals + 1);
						final ValueBase vb = (ValueBase) toClass(vs, ValueBase.class);
						d.put(name, vb);
					}
					// ignore els with no name
				}
				return d;
			}
			// ill-formed string -> dictionary?!
			d.put("value", new StringBoxImpl(s));
			return d;
		}
		else if (cls.equals(DictionaryImpl.class) && o instanceof Serializable)
		{
			// simple/primitive type?
			if (isSimpleClass(o.getClass()))
			{
				final DictionaryImpl d = new DictionaryImpl();
				d.put("value", (ValueBase) toClass(o, ValueBase.class));
				return d;
			}

			// default introspection -> properties
			final DictionaryImpl d = new DictionaryImpl();
			d.put("class", new StringBoxImpl(o.getClass().getName()));
			try
			{
				// public fields
				final Field fs[] = o.getClass().getFields();
				for (final Field element : fs)
				{
					final int mod = element.getModifiers();
					if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
					{
						continue;
					}
					final Object v = element.get(o);
					final ValueBase sv = (ValueBase) toClass(v, ValueBase.class);
					d.put(element.getName(), sv);
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR introspecting value " + o + ": " + e);
				e.printStackTrace(System.err);
			}
			return d;
		}
		else if (Serializable.class.isAssignableFrom(cls) && o instanceof DictionaryImpl)
		{
			final DictionaryImpl d = (DictionaryImpl) o;
			// special case of single element "value"
			if (d.entries != null && d.entries.length == 1 && d.entries[0].name.equals("value")) { return toClass(	d.entries[0].value,
																													cls); }
			// reverse default introspection -> properties
			// named class is subclass of proposed class??
			final StringBox cb = (StringBox) d.get("class");
			String classname = null;
			if (cb != null)
			{
				classname = cb.value;
			}
			if (classname != null)
			{
				try
				{
					final Class cls2 = cls.getClassLoader().loadClass(classname);
					if (cls.isAssignableFrom(cls2))
					{
						cls = cls2;
					}
					else
					{
						System.err.println("WARNING: object that was of class " + classname + " coerced to " + cls);
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR checking for unknown class " + classname + ": " + e);
					e.printStackTrace(System.err);
				}
			}
			try
			{
				final Object r = cls.newInstance();
				// public fields
				final Field fs[] = o.getClass().getFields();
				for (final Field element : fs)
				{
					final int mod = element.getModifiers();
					if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
					{
						continue;
					}

					final Object v = d.get(element.getName());
					if (v != null)
					{
						final Object v2 = toClass(v, element.getType());
						element.set(o, v2);
					}
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR rebuilding value of class " + cls + " from " + o + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		else if (Serializable.class.isAssignableFrom(cls) && o instanceof String)
		{
			// convert via Dictionary
			final DictionaryImpl d = (DictionaryImpl) toClass(o, DictionaryImpl.class);
			return toClass(d, cls);
		}
		// ???
		System.err.println("Cannot coerce " + o.getClass().getName() + " to " + cls.getName());
		return null;
	}

	/**
	 * hashtable to dictionary boxing values
	 */
	public static DictionaryImpl toDictionary(final Map h) throws ClassNotFoundException, IOException
	{
		final DictionaryImpl d = new DictionaryImpl();
		for(Object key: h.keySet())
		{
			final ValueBase value = (ValueBase) toClass(h.get(key), ValueBase.class);
			d.put(key.toString(), value);
		}
		return d;
	}

	/**
	 * dictionary to hashtable unboxing values
	 */
	public static Map toHashtable(final DictionaryImpl d) throws ClassNotFoundException, IOException
	{
		final Map h = new HashMap();
		for (int i = 0; d.entries != null && i < d.entries.length; i++)
		{
			h.put(d.entries[i].name, toClass(d.entries[i].value, Serializable.class));
		}
		return h;
	}

	static protected void appendEscapedString(final StringBuffer buf, final String s2)
	{
		buf.append('"');
		for (int i = 0; i < s2.length(); i++)
		{
			final char nc = s2.charAt(i);
			if (nc == '{' || nc == '}' || nc == ',' || nc == '"')
			{
				// escape
				buf.append('\\');
				buf.append(nc);
			}
			// primitive escapes
			else if (nc == '\n')
			{
				buf.append("\\n");
			}
			else if (nc == '\t')
			{
				buf.append("\\t");
			}
			else
			{
				buf.append(nc);
			}
		}
		buf.append('"');
	}
}
