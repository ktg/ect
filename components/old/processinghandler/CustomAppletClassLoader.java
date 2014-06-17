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

Created by: Chris Allsop (University of Nottingham)
Contributors:
  Chris Allsop (University of Nottingham)

 */
package equip.ect.components.processinghandler;

import java.applet.Applet;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomAppletClassLoader extends URLClassLoader implements Serializable
{

	public CustomAppletClassLoader()
	{
		super(new URL[] {});
	}

	public CustomAppletClassLoader(final URL url)
	{
		super(new URL[] { url });
	}

	public CustomAppletClassLoader(final URL[] urls)
	{
		super(urls);
	}

	public void addNewURL(final URL url)
	{
		final int index = url.toExternalForm().indexOf(ProcessingHandlerConstants.JAR_SEPARATOR);

		if (index != -1)
		{
			try
			{
				addURL(new URL(url.toExternalForm().substring(	0,
																index
																		+ ProcessingHandlerConstants.JAR_SEPARATOR
																				.length())));

			}
			catch (final MalformedURLException ex)
			{
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				return;
			}
		}
		else
		{
			addURL(url);
		}

	}

	public Applet loadApplet(final String classname) throws AppletNotFoundException, AppletInstantiationException,
			ClassCastException
	{
		Class loadedclass;

		try
		{
			loadedclass = this.loadClass(classname);

			if ((Class.forName(Applet.class.getName())).isAssignableFrom(loadedclass))
			{

				return (Applet) loadedclass.newInstance();

			}
			else
			{
				throw new ClassCastException();
			}

		}
		catch (final ClassNotFoundException cnfe)
		{
			cnfe.printStackTrace();
			throw new AppletNotFoundException("Could not load applet " + classname + " from CustomURLClassLoader");

		}
		catch (final Exception allOtherExceptions)
		{
			throw new AppletInstantiationException("Could not create applet " + classname + " in CustomURLClassLoader");
		}
	}

	@Override
	public Class loadClass(final String name) throws ClassNotFoundException
	{
		return loadClass(name, false);
	}

	public Class loadClass(final String name, final byte bytes[]) throws ClassFormatError
	{

		Class clazz = null;

		clazz = findLoadedClass(name);

		if (clazz == null)
		{
			clazz = defineClass(name, bytes, 0, bytes.length);
			resolveClass(clazz);
		}
		return clazz;
	}

	@Override
	protected Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException
	{

		Class clazz = null;

		clazz = findLoadedClass(name);

		if (clazz == null)
		{
			try
			{
				clazz = findClass(name);

			}
			catch (final ClassNotFoundException cnfe)
			{
				// try delegating to the parent loader
				clazz = super.loadClass(name, resolve);
			}
		}
		return clazz;
	}
}
