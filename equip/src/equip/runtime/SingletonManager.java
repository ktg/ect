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
package equip.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a single static method to get singleton instances
 * of name class(es).
 * <p>
 * Also implemented in C++, but not IDL'd.
 */

public class SingletonManager
{
	private Map<String, java.lang.Object> singletons = new HashMap<>();

	/**
	 * Own singleton. Statically initialised.
	 */
	private static SingletonManager _instance = new SingletonManager();

	/**
	 * Private constructor - only the singleton should exist.
	 */
	private SingletonManager()
	{
	}

	/**
	 * Return a reference to the SingletonManager's singleton instance
	 * of the named class.
	 * <p>
	 * In the future we may be able to use weak references to
	 * still allow GC on the singleton, but for now it will probably
	 * never go away (unless you use the - interrim/dangerous? - remove).
	 *
	 * @param name The dot-qualified full name of the class to be
	 *             returned, e.g. 'equip.config.ConfigManagerImpl'.
	 * @return A reference to a singleton instance of the class, or
	 * <code>null</code> if the class could not be loaded.
	 */
	public static java.lang.Object get(String name)
	{
		return _instance._get(name);
	}

	/**
	 * internal implementation of {@link #get}
	 */
	private synchronized java.lang.Object _get(String name)
	{
		java.lang.Object obj = singletons.get(name);
		if (obj == null)
		{
			System.err.println("SingletonManager creating new singleton: " + name + "...");
			java.lang.Class c = null;
			try
			{
				c = ObjectInputStream.loader.loadClass(name);
			}
			catch (Exception e)
			{
				c = null;
			}
			if (c == null)
			{
				// modules ....
				System.err.println("ERROR: could not find class " + name);
			}
			else
			{
				try
				{
					obj = c.newInstance();
				}
				catch (Exception e)
				{
					obj = null;
				}
				if (obj == null)
				{
					System.err.println("ERROR: could not create instance for class " + name);
				}
				else
				{
					System.err.println("ok");
				}
			}
			singletons.put(name, obj);
		}
		if (obj == null)
		{
			System.err.println("WARNING: SingletonManager returning null for " + name);
		}
		return obj;
	}

	/**
	 * you probably don't want to do this :-) - an experimental operation
	 * to remove the singleton from the internal hashtable (for GC).
	 */
	public static void remove(String name)
	{
		_instance._remove(name);
	}

	/**
	 * internal implementation of {@link #remove}
	 */
	private synchronized void _remove(String name)
	{
		if (singletons.remove(name) != null)
		{
			System.err.println("SingletonManager removed singleton: " + name);
		}
	}
}
