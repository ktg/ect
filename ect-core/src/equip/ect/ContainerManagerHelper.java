/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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

 Created by: Shahram Izadi (University of Nottingham)
 Contributors:
 Tom Rodden (University of Nottingham)
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)
 Shahram Izadi (University of Nottingham)
 James Mathrick (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect;

import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceInactiveException;
import equip.ect.discovery.DataspaceDiscover;
import equip.ect.util.DirectoryMonitor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ContainerManagerHelper implements XMLConstants
{
	public static final String JAR_EXTENSION = ".jar";
	public static final String CLASSNAME_SUFFIX = ".class";
	public static final String BEAN_CLASSNAME_SUFFIX = "BeanInfo.class";
	private static final String MANIFEST_BEAN = "Java-Bean";
	private static final String SHORT_DESCRIPTION = BeanDescriptorHelper.SHORT_DESCRIPTION;
	private static final String HTML_DESCRIPTION = "htmlDescription";
	private static final String HTML_FILE_NAME = "htmlFile";
	private static final String NO_HTML = "nohtml";
	private static final int frequency = 1000;
	//private static final int defaultPort = 9137;
	private static File defaultRootDirectory = null;

	//private static File httpDirectory;
	//private static SimpleHttpServer httpServer = null;
	private static ContainerManagerHelper _instance;

	private static class ContainerClassLoader extends URLClassLoader
	{
		public ContainerClassLoader(final ClassLoader cl)
		{
			super(new URL[]{}, cl);
			final String commonClassPath = System.getProperty("containerClassPath", null);
			if (commonClassPath != null)
			{
				final java.util.StringTokenizer toks = new java.util.StringTokenizer(commonClassPath,
						java.io.File.pathSeparator, false);
				while (toks.hasMoreTokens())
				{
					final String t = toks.nextToken();
					if (t.equals(""))
					{
						continue;
					}
					System.out.println("Add to common class path: " + t);
					try
					{
						addURL(new File(t).toURI().toURL());
					}
					catch (final Exception e)
					{
						System.err.println("ERROR adding file " + t + ": " + e);
					}
				}
			}
		}

		public synchronized void forceResolve(final Class<?> c)
		{
			resolveClass(c);
		}

		@Override
		public Class<?> loadClass(final String name) throws ClassNotFoundException
		{
			return loadClass(name, false);

		}

		@Override
		protected synchronized void addURL(final URL url)
		{
			final URL urls[] = getURLs();
			for (final URL url2 : urls)
			{
				if (url2.equals(url))
				{
					return;
				}
			}
			super.addURL(url);
		}

		@Override
		protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException
		{
			Class<?> cl = findLoadedClass(name);
			if (cl != null)
			{
				return cl;
			}
			try
			{
				cl = findClass(name);
			}
			catch (final ClassNotFoundException e)
			{
				// try parent
				return super.loadClass(name, resolve);
			}
			return cl;
		}
	}

	private File persistFile = null;
	private DirectoryMonitor directoryMonitor = null;
	private final ContainerClassLoader classLoader = new ContainerClassLoader(getClass().getClassLoader());
	private final DataspaceBean dataSpaceBean;
	private ContainerManager containerManager = null;

	public ContainerManagerHelper(final DataspaceBean dataSpaceBean, final File componentsDirectory,
	                              final File persistFile, final String hostName) throws IOException
	{
		synchronized (ContainerManagerHelper.class)
		{
			if (_instance == null)
			{
				_instance = this;
				if (componentsDirectory.exists())
				{
					defaultRootDirectory = componentsDirectory.getParentFile();
//					httpDirectory = createDirectory("http");
//					try
//					{
//						httpServer = new SimpleHttpServer(defaultPort, httpDirectory.getCanonicalPath());
//					}
//					catch (final IOException e)
//					{
//						e.printStackTrace();
//					}
				}
			}
		}

		if (dataSpaceBean == null)
		{
			throw new IllegalArgumentException("DataspaceBean cannot be null");
		}
		if (componentsDirectory == null)
		{
			throw new IllegalArgumentException("Components directory cannot be null");
		}
		this.persistFile = persistFile == null ? PersistenceManager.PERSISTENCE_FILE : persistFile;
		this.dataSpaceBean = dataSpaceBean;
		String dir;
		if (componentsDirectory.exists())
		{
			dir = componentsDirectory.getCanonicalPath();
		}
		else
		{
			dir = persistFile.getCanonicalFile().getParent();
		}
		System.out.println("Components directory: " + dir);

		//loadExistingState(hostName, dir);
		if (containerManager == null)
		{
			containerManager = new ContainerManager(dataSpaceBean, dir, hostName, dataSpaceBean.allocateId());
			containerManager.setContainerManagerHelper(this);
		}
		if (componentsDirectory.exists())
		{
			try
			{
				directoryMonitor = new DirectoryMonitor(componentsDirectory, true, true);
				new Thread(directoryMonitor).start();
			}
			catch (final Exception e)
			{
				System.err.println("ERROR starting directory monitor for " + componentsDirectory
						+ " - no capabilities will be exported: " + e);
			}
		}
		else
		{
			System.err.println("WARNING: components directory " + componentsDirectory
					+ " does not exist - no capabilities will be exported");
		}
		// and local subdir
		//startPersistence();
	}

	public ContainerManagerHelper(final String dataSpaceURL, final String componentsDirectory,
	                              final String persistFile, final String hostName) throws IOException
	{
		this(createDataSpaceBean(parseDataSpaceURL(dataSpaceURL)),
				componentsDirectory == null ? createDirectory("../components") : new File(componentsDirectory),
				persistFile == null ? PersistenceManager.PERSISTENCE_FILE : new File(persistFile),
				hostName == null ? "Unspecified" : hostName);
	}

	public static DataspaceBean createDataSpaceBean(final String dataSpaceURL)
	{
		final DataspaceBean dataSpaceBean = new DataspaceBean();
		if (dataSpaceURL != null)
		{
			try
			{
				dataSpaceBean.setRetryConnect(true);
				dataSpaceBean.setDataspaceUrl(dataSpaceURL);
			}
			catch (final DataspaceInactiveException e)
			{
				e.printStackTrace();
			}
		}
		return dataSpaceBean;
	}

	public static File createDirectory(final String folderStr)
	{
		File folder = new File(folderStr);
		if (defaultRootDirectory != null)
		{
			folder = new File(defaultRootDirectory, folderStr);
		}
		if (!folder.exists())
		{
			folder.mkdir();
		}
		return folder;
	}

	public static ContainerManagerHelper getInstance()
	{
		return _instance;
	}

	public static byte[] loadByteArrayFromFile(final String file, final ClassLoader cLoader)
	{
		byte[] bytes = null;

		if (cLoader != null)
		{
			final InputStream is = cLoader.getResourceAsStream(file);
			if (is != null)
			{
				try
				{
					final BufferedInputStream bis = new BufferedInputStream(is);
					final int avail = bis.available();
					bytes = new byte[avail];
					bis.read(bytes);
					bis.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			// naive attempt
			try
			{
				final FileInputStream fis = new FileInputStream(file);
				final int tot = fis.available();
				bytes = new byte[tot];
				fis.read(bytes);
				fis.close();
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

		return bytes;
	}

	public static String parseDataSpaceURL(String dataSpaceURL)
	{
		if (dataSpaceURL != null)
		{
			if (!dataSpaceURL.contains(":"))
			{
				System.out.println("Using discovery with group " + dataSpaceURL);
				dataSpaceURL = new DataspaceDiscover(dataSpaceURL).getFirstDataspace();
				System.out.println("-> " + dataSpaceURL);
			}
		}
		return dataSpaceURL;
	}

	// export using new allocated ids
	public void exportFromJarFile(final File file) throws IOException
	{
		System.out.println("Importing from " + file.toString());
		if (containerManager.capexport != null)
		{
			final BeanJarContent beanJarContent = new BeanJarContent();
			final List<Class<?>> beanClasses = loadFromJarFile(file, beanJarContent);

			if (beanClasses != null)
			{
				for (final Class<?> beanClass : beanClasses)
				{
					final String className = beanClass.getName();
					containerManager.capexport.exportCapability(beanClass, beanJarContent.getBeanContent(className));
				}
			}
		}
	}

	public ContainerManager getContainerManager()
	{
		return containerManager;
	}

	public DataspaceBean getDataSpaceBean()
	{
		return dataSpaceBean;
	}

	public DirectoryMonitor getDirectoryMonitor()
	{
		return directoryMonitor;
	}

	public boolean canImport(File file)
	{
		return file != null && file.exists() && file.getName().toLowerCase().endsWith(JAR_EXTENSION);
	}

	public List<Class<?>> loadFromJarFile(final File file, final BeanJarContent beanContent) throws IOException
	{
		if (canImport(file))
		{
			loadJar(file);
			final JarFile jarFile = new JarFile(file);
			final Enumeration<JarEntry> entries = jarFile.entries();
			final List<String> classNames = new ArrayList<String>();
			Manifest manifest = jarFile.getManifest();
			if (manifest != null && manifest.getEntries().size() == 0)
			{
				manifest = null;
			}
			if (manifest == null)
			{
				while (entries.hasMoreElements())
				{
					JarEntry entry = entries.nextElement();
					final String name = entry.getName();
					if(name.endsWith(CLASSNAME_SUFFIX))
					{
						if (name.endsWith(BEAN_CLASSNAME_SUFFIX))
						{
							final String className = name.replace('/', '.').substring(0, name.length() - BEAN_CLASSNAME_SUFFIX.length());
							classNames.add(className);
						}
						else
						{
							final String className = name.replace('/', '.').substring(0, name.length() - CLASSNAME_SUFFIX.length());
							try
							{
								Class<?> componentClass = classLoader.loadClass(className);
								if(componentClass.isAnnotationPresent(ECTComponent.class))
								{
									classNames.add(className);
								}
							}
							catch(Throwable e)
							{
								// Do nothing
								//System.err.println("Error whilst loading " + className);
								//e.printStackTrace();
							}
						}
					}
				}
			}
			else
			{
				System.out.println("NOTE: using manifest of jar " + file);
				final Map<String, Attributes> ents = manifest.getEntries();
				for (String name : ents.keySet())
				{
					final Attributes attributes = ents.get(name);
					/*
					 * DEBUG: System.out.println("Manifest entry "+name+": "); Iterator ai =
					 * attributes.keySet().iterator(); while(ai.hasNext()) { Object an = ai.next();
					 * System.out.println(" "+an+" = "+attributes.get(an)); }
					 */
					final String value = attributes.getValue(MANIFEST_BEAN);

					// DEBUG:System.out.println("Attribute "+MANIFEST_BEAN+" =
					// "+value);
					if (value != null && value.trim().toUpperCase().startsWith("T"))
					{
						String className = name.trim().replace('/', '.');
						if (className.endsWith(".class"))
						{

							className = className.substring(0, className.length() - 6);
							classNames.add(className);

							final String classification = attributes.getValue(Capability.CLASSIFICATION);

							if (classification != null)
							{
								System.out.println("Bean class " + className + " classified in manifest as "
										+ classification);
								beanContent.put(className, BeanJarContent.CLASSIFICATION_KEY, classification.trim());
							}

							final String shortDescription = attributes.getValue(SHORT_DESCRIPTION);

							if (shortDescription != null)
							{
								System.out.println("Bean class " + className + " has short description "
										+ shortDescription);
								beanContent.put(className, BeanJarContent.SHORT_DESCRIPTION_KEY,
										shortDescription.trim());
							}

							final String defaultInputProperty = attributes.getValue("defaultInputProperty");
							if (defaultInputProperty != null)
							{

								beanContent.put(className, BeanJarContent.DEFAULT_INPUT_PROPERTY_KEY,
										defaultInputProperty.trim());
							}

							final String defaultOutputProperty = attributes.getValue("defaultOutputProperty");

							if (defaultOutputProperty != null)
							{

								beanContent.put(className, BeanJarContent.DEFAULT_OUTPUT_PROPERTY_KEY,
										defaultOutputProperty.trim());
							}

							final String icon = attributes.getValue("icon");

							if (icon != null)
							{
								beanContent.put(className, BeanJarContent.ICON_KEY, icon.trim());
							}

							// If developer wishes to indicate that no html description is required for a component,
							// they should specify the NO_HTML attribute in the manifest. This will speed container
							// startup as the container will not search the jar file for an html file
							final String noHTML = attributes.getValue(NO_HTML);

							if (noHTML == null)
							{
								final String htmlDescription = attributes.getValue(HTML_DESCRIPTION);
								final String htmlFileName = attributes.getValue(HTML_FILE_NAME);

								// 3 ways of specifying html description for beans defined by manifest entries
								// 1. don't specify anyting. Code below will then look in the jar from which the
								// manifest has come for a file called <class name>.html
								// 2. specify htmlDescription attribute in manifest
								// 3. specify htmlFile attribute in manifest - code will look for that file in the jar
								String htmlString;
								if (htmlDescription != null)
								{
									// if an html description exists in manifest, fetch it
									htmlString = htmlDescription.trim();
								}
								else
								{
									if (htmlFileName != null)
									{
										htmlString = getHTMLDescriptionFromFile(htmlFileName, jarFile);
									}
									else
									{
										final int pos = className.lastIndexOf(".");
										final String classNameNotFQ = className.substring(pos + 1);

										final String htmlName = classNameNotFQ + ".html";

										htmlString = getHTMLDescriptionFromFile(htmlName, jarFile);
									}
								}

								if (htmlString != null)
								{
									// if html description has been found using
									// one of the above methods
									beanContent.put(className, BeanJarContent.HTML_DESCRIPTION_KEY, htmlString.trim());

								}

							}
						}
						else
						{
							System.err.println("WARNING: Unable to handle Java Bean \"" + name + "\"");
						}
					}
				}
			}

			final List<Class<?>> classes = new ArrayList<Class<?>>();
			for (String className : classNames)
			{
				try
				{
					// System.err.println("Try to load bean class " + className + " from jar " + file + "...");
					final Class<?> beanClass = classLoader.loadClass(className);
					// System.err.println("Now try to realize class " + className + "...");
					classLoader.forceResolve(beanClass);
					System.err.println("Component Class " + className + " OK");
					classes.add(beanClass);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR loading bean class " + className + ": " + e);
					e.printStackTrace();
				}
			}
			return classes;
		}
		return null;
	}

	public void loadJar(final File jarFile) throws MalformedURLException
	{
		if (jarFile != null)
		{
			classLoader.addURL(jarFile.toURI().toURL());
		}
	}

	protected String getHTMLDescriptionFromFile(final String fileName, final JarFile jarFile)
	{
		try
		{
			// looks for fileName in jarFile. If found, loads its contents
			// into a string and returns
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				final JarEntry entry = entries.nextElement();
				final String entryName = entry.getName();

				if (entryName.endsWith(fileName))
				{
					final InputStream is = jarFile.getInputStream(entry);

					final InputStreamReader isr = new InputStreamReader(is);
					final BufferedReader br = new BufferedReader(isr);

					String line;
					String htmlString = "";
					while ((line = br.readLine()) != null)
					{
						htmlString = htmlString + line;
					}

					return htmlString;
				}
			}
		}
		catch (final IOException e)
		{
			System.out.println("WARNING: Could not find html file '" + fileName + "' described in manifest attribute.");
		}

		// if not file has been found, then return null
		return null;
	}

	private void loadExistingState(final String hostName, final String dir)
	{

		try
		{
			System.out.print("attempting container recovery...");
			// spin until we connect to dataspace
			while (!dataSpaceBean.isConnected())
			{
				try
				{
					Thread.sleep(frequency);
				}
				catch (final Exception e)
				{
					// Do nothing
				}
			}
			final PersistenceManager persistenceManager = PersistenceManager.getPersistenceManager();
			final File file = persistenceManager.getValidPersistFile(persistFile);
			if (file != null)
			{
				System.out.println("ERROR: could not find a valid persist xml file");
				containerManager = PersistenceManager.getPersistenceManager().recoverContainer(this, file, hostName, dir);
				containerManager.setContainerManagerHelper(this);
			}

			System.out.println("done");
		}
		catch (final Exception e)
		{
			System.out.println("CONTAINER MANAGER: unable to load existing state: " + e);
		}
	}

	private void startPersistence()
	{
		//System.out.println("starting persistence...");
		final PersistenceManager persistenceManager = PersistenceManager.getPersistenceManager();
		new Thread()
		{
			@Override
			public void run()
			{
				persistenceManager.startPersistence(persistFile, dataSpaceBean, containerManager, frequency);
			}
		}.start();
	}
}