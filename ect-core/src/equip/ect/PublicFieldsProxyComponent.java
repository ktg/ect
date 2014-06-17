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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:

 */
package equip.ect;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * a dynamic component which reflects the public fields of the given object. requires polling.
 */
public class PublicFieldsProxyComponent extends SimpleDynamicComponent implements PropertyChangeListener
{
	/**
	 * test class
	 */
	public static class TestClass
	{
		public int anInt = 4;
		public String aString = "hello";
		int anotherInt = 3;

		TestClass()
		{
		}
	}

	/**
	 * property info
	 */
	protected class FieldInfo
	{
		/**
		 * name
		 */
		String name;
		/**
		 * field
		 */
		Field field;
		/**
		 * value
		 */
		Object value;

		/**
		 * cons
		 */
		FieldInfo(final String name, final Field field, final Object value)
		{
			this.name = name;
			this.field = field;
			this.value = value;
		}
	}

	public static boolean debug = false;

	/**
	 * test main
	 */
	public static void main(final String args[])
	{
		try
		{
			String dataspaceUrl = "equip://:9123";
			String dataspaceSecret = null;
			if (args.length > 0)
			{
				dataspaceUrl = args[0];
			}
			if (args.length > 1)
			{
				dataspaceSecret = args[1];
			}
			final InetAddress localhost = InetAddress.getLocalHost();
			final SimpleObjectContainer container = new SimpleObjectContainer(dataspaceUrl,
					"PublicFieldsProxyComponent.main on " + localhost.getHostName(),
					"PublicFieldsProxyComponent.main.persist.xml", dataspaceSecret);

			// purely scripted object
			final TestClass test = new TestClass();
			final PublicFieldsProxyComponent proxy = new PublicFieldsProxyComponent(test);

			container.exportComponent(proxy, "testproxy");
			// ok
			proxy.exported();

			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							Thread.sleep(1000);
						}
						catch (final InterruptedException e)
						{
						}
						test.anInt = test.anInt + 3;
						test.aString = test.aString + ".";
						System.out.println("Changed to " + test.anInt + ", " + test.aString);
						proxy.poll();
					}

				}
			}).start();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * target/subject
	 */
	protected Object target;
	/**
	 * fields
	 */
	protected Map<String, FieldInfo> fields = new HashMap<String, FieldInfo>();
	/**
	 * protect fields during initial export
	 */
	protected boolean protectFields = true;

	/**
	 * cons
	 */
	public PublicFieldsProxyComponent(final Object target)
	{
		this.target = target;
		// make properties
		final Field[] fields = target.getClass().getDeclaredFields();
		for (final Field field : fields)
		{
			final int mod = field.getModifiers();
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod))
			{
				Object value = null;
				try
				{
					value = field.get(target);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR getting field value " + field.getName() + ": " + e);
					e.printStackTrace(System.err);
				}
				// ok
				final FieldInfo fi = new FieldInfo(field.getName(), field, value);
				this.fields.put(field.getName(), fi);
				addProperty(fi.name, field.getType(), value);
				System.out.println("Added property " + fi.name + " (" + field.getType() + ") = " + value);
			}
		}
		addPropertyChangeListener(this);
	}

	/**
	 * exported - unprotect fields
	 */
	public synchronized void exported()
	{
		if (protectFields)
		{
			protectFields = false;
			poll();
		}
	}

	/**
	 * poll
	 */
	public synchronized void poll()
	{
		for(FieldInfo fi: fields.values())
		{
			Object value = null;
			try
			{
				value = fi.field.get(target);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR getting field value " + fi.name + ": " + e);
				e.printStackTrace(System.err);
			}
			if (value == fi.value || (value != null && fi.value != null && value.equals(fi.value)))
			{
				continue; // unchanged
			}
			if (debug)
			{
				System.out.println("Poll Field " + fi.name + " changed to " + value);
			}
			fi.value = value;
			try
			{
				dynSetProperty(fi.name, value);
			}
			catch (final NoSuchPropertyException e)
			{
				System.err.println("ERROR setting dynamic property " + fi.name + " on poll: " + e);
			}
		}
	}

	/**
	 * external? set
	 */
	@Override
	public synchronized void propertyChange(final PropertyChangeEvent ev)
	{
		// System.out.println("Field "+ev.getPropertyName()+" changed externally to "+ev.getNewValue());
		final String name = ev.getPropertyName();
		final FieldInfo fi = (FieldInfo) fields.get(name);
		if (fi == null)
		{
			System.out.println("WARNING: set of unknown field/property " + name);
			return;
		}
		final Object value = ev.getNewValue();
		if (value == fi.value || (value != null && fi.value != null && value.equals(fi.value)))
		{
			// System.out.println("Field/property "+name+" unchanged as "+fi.value);
			return; // unchanged
		}
		fi.value = value;
		if (protectFields)
		{
			System.out.println("Note: field " + fi.name + " currently protected");
		}
		else
		{
			if (debug)
			{
				System.out.println("Field " + fi.name + " changed externally to " + value + " - setting field");
			}

			try
			{
				final Object value2 = Coerce.toClass(value, fi.field.getType());
				fi.field.set(target, value2);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR setting field " + fi.name + " to " + value + ": " + e);
				e.printStackTrace(System.err);
			}
		}
	}
}
