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

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  James Mathrick (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.webstart;

import equip.ect.util.process.ProcessUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Object representing a container for startup purposes. Typically an instance of this class
 * represents and manages a child process which is the actual container (e.g. another JVM).
 *
 * @author jym
 */
class Container
{
	/**
	 * wait for clean death
	 */
	public static final int DESTROY_DELAY_MS = 5000;
	private static final boolean DUMP_HEAP = false;

	/**
	 * For automatic creation of {@link equip.ect.webstart.Container} instance (unstarted) representing a Java
	 * container, as we're running Java. Creates appropriate configuration file(s).
	 */
	public static Container createJavaContainer(File instPath, final String url)
	{
		try
		{
			instPath = instPath.getCanonicalFile();
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR getting canonical path for " + instPath + ": " + e);
		}
		// Prevent stupidity...
		if (!instPath.exists())
		{
			return null;
		}
		final File javaContainer = new File(instPath.getAbsolutePath() + BootGlobals.getSeparator()
				+ BootGlobals.JAVA_CONTAINER + BootGlobals.getSeparator());
		// ...and again...
		if (javaContainer.exists())
		{
			return null;
		}
		final File javaComponents = new File(javaContainer.getAbsolutePath()
				+ BootGlobals.getSeparator() + "components" + BootGlobals.getSeparator());
		javaComponents.mkdirs();
		final File persistFile = new File(javaContainer.getAbsolutePath() + BootGlobals.getSeparator()
				+ equip.ect.PersistenceManager.PERSISTENCE_FILE);

		String hostname = "Unknown";
		try
		{
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (final java.net.UnknownHostException e)
		{
			if (BootGlobals.DEBUG)
			{
				System.out.println(e.getMessage());
			}
		}

		// setup its boot props
		final java.util.Properties javaContProps = new java.util.Properties();
		javaContProps.setProperty(BootGlobals.EXEC_PROP, "java equip.ect.ContainerManagerHelper");
		javaContProps.setProperty(BootGlobals.COMPONENT_DIR_PROP, javaComponents.getAbsolutePath());
		javaContProps.setProperty(BootGlobals.CONTAINER_NAME, "java on " + hostname /*
																					 * use more
																					 * meaningful
																					 * name/desc
																					 * here
																					 */);
		javaContProps.setProperty(BootGlobals.PERSIST_FILE_PROP, persistFile.getAbsolutePath());
		BootGlobals.savePropertyFile(new File(javaContainer.getAbsolutePath() + BootGlobals.getSeparator()
				+ BootGlobals.PROP_FILE_NAME), javaContProps);
		return new Container(javaContainer, url);
	}

	/**
	 * remove quotes
	 */
	public static String removeQuotes(final String in)
	{
		final StringBuilder out = new StringBuilder(in);
		for (int i = 0; i < out.length(); i++)
		{
			if (out.charAt(i) == '"')
			{
				out.deleteCharAt(i);
				i--;
			}
		}
		return out.toString();
	}

	private File componentDir = null;
	private String url = "";
	private boolean isValid = false;
	private boolean iHaveCrashed = false;
	private boolean iAmRunning = false;
	private File persistFile = null;
	private String containerName = null;
	private Process myProcess = null;
	private HostManager monitor = null;
	private String commonDir = null;
	private String path;
	private String containerDirectory;

	/**
	 * main constructor - execs a separate. Reads container property file (if present).
	 *
	 * @param path Configuration/persistence directory for this container.
	 * @param url  Dataspace URL to use for coordination.
	 */
	public Container(File path, final String url)
	{
		setupShutdownHook();
		try
		{
			path = path.getCanonicalFile();
			this.path = path.getCanonicalPath();
		}
		catch (final java.io.IOException e)
		{
			System.err.println("ERROR getting canonical path for " + path + ": " + e);
			this.path = path.getAbsolutePath();
		}
		this.containerDirectory = path.getName();
		final File propPath = new File(path.getAbsolutePath() + BootGlobals.getSeparator()
				+ BootGlobals.PROP_FILE_NAME + BootGlobals.getSeparator());
		this.commonDir = path.getAbsolutePath() + File.separator + "common";
		// read config...
		final java.util.Properties contProperties = BootGlobals.loadPropertyFile(propPath);
		if (contProperties != null)
		{
			final String exec = contProperties.getProperty(BootGlobals.EXEC_PROP);
			final String compDir = contProperties.getProperty(BootGlobals.COMPONENT_DIR_PROP);
			final String persistFileStr = contProperties.getProperty(BootGlobals.PERSIST_FILE_PROP);
			containerName = contProperties.getProperty(BootGlobals.CONTAINER_NAME);

			this.url = url;
			// validate
			if (exec != null && !exec.equals("") && compDir != null && !compDir.equals("")
					&& this.url != null && !this.url.equals("") && persistFileStr != null && !persistFileStr.equals(""))
			{
				this.componentDir = new File(compDir);
				this.isValid = true;
				persistFile = new File(persistFileStr);
			}
		}
	}

	public synchronized void crashDetected(final Process thisProcess)
	{
		if (thisProcess != myProcess)
		{
			return;
		}
		if (BootGlobals.DEBUG)
		{
			System.err.println("Crash Detected in container " + this);
		}
		iAmRunning = false;
		iHaveCrashed = true;
		if (this.monitor != null)
		{
			this.monitor.containerCrashed(this);
		}
		// may be trying clean terminate
		this.notifyAll();
	}

	/**
	 * jsut the name part of the directory, eg to compare with autostart
	 */
	public String getContainerDirectory()
	{
		return containerDirectory;
	}

	public String getContainerName()
	{
		return containerName;
	}

	public synchronized boolean isCrashed()
	{
		return iHaveCrashed;
	}

	public synchronized boolean isRunning()
	{
		return iAmRunning;
	}

	/**
	 * stop and start container process.
	 */
	public synchronized boolean restart()
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Restarting container " + this);
		}
		stop();
		return start(this.monitor);
	}

	/**
	 * start container. Currently hard-coded to run 'java' and the
	 * {@link equip.ect.apps.ExporterGUI} class, with arg 1 = component directory, arg 2 =
	 * persistence file, arg 3 = container name.
	 */
	public synchronized boolean start(final HostManager m)
	{
		if (!this.isValid)
		{
			return false;
		}
		if (BootGlobals.DEBUG)
		{
			System.err.println("Starting container " + this);
		}
		this.monitor = m;
		try
		{
			// already using regular class path since in Installation
			System.err.println("java.library.path=" + System.getProperty("java.library.path"));
			String libraryPath = removeQuotes(System.getProperty("java.library.path"));
			System.err.println("libraryPath -quotes = " + libraryPath);
			String javaextdirs = removeQuotes(System.getProperty("java.ext.dirs"));
			if (javaextdirs == null)
			{
				javaextdirs = commonDir + File.separator + "ext";
			}
			else
			{
				javaextdirs = javaextdirs + File.pathSeparator + commonDir + File.separator + "ext";
			}
			System.err.println("java.ext.dirs=" + javaextdirs);
			// include all JARs in the common subdirectory
			final File common = new File(commonDir);
			final StringBuilder commonClassPath = new StringBuilder();
			if (common.exists() && common.isDirectory())
			{
				final File jars[] = common.listFiles();
				if (jars != null)
				{
					for (final File jar : jars)
					{
						if (jar.isFile() && jar.getName().endsWith(equip.ect.apps.ExporterGUI.JAR_SUFFIX))
						{
							System.out.println("Add common jar: " + jar);
							commonClassPath.append(File.pathSeparator);
							commonClassPath.append(jar.getCanonicalPath());
						}
					}
				}
				if (libraryPath == null)
				{
					libraryPath = common.getCanonicalPath();
				}
				else if (!libraryPath.contains(common.getCanonicalPath()))
				{
					libraryPath = common.getCanonicalPath() + File.pathSeparator + libraryPath;
				}
				System.err.println("libraryPath -common = " + libraryPath);
			}

			System.out.println("Library path -native = " + libraryPath);
			libraryPath = Boot.makePathAbsolute(libraryPath);
			System.out.println("Library path (abs) = " + libraryPath);

			final File logFile = new File(this.path + File.separator + Boot.LOG_PREFIX + System.currentTimeMillis()
					+ Boot.LOG_SUFFIX);

			String execStr = "java";

			if (DUMP_HEAP)
			{
				execStr = execStr + " -Xrunhprof:heap=all,format=b,file=container.txt ";
			}

			execStr = execStr + " \"-Djava.ext.dirs=" + javaextdirs + "\" " + " \"-DDataspaceSecret="
					+ System.getProperty("DataspaceSecret") + "\" " + Boot.getProxyProperties() + "-classpath \""
					+ System.getProperty("java.class.path") + "\" " + "\"-DcontainerClassPath=" + commonClassPath.toString() + "\" "
					+ "\"-Djava.library.path=" + libraryPath + "\" " + "\"-Duser.dir=" + this.path + "\" "
					+ " ect.apps.ExporterGUI " + url + " \"" + componentDir.getCanonicalPath() + "\" \""
					+ persistFile.getCanonicalPath() + "\" \""
					+ (containerName == null ? "Unspecified" : containerName) + "\" \"" + logFile.getAbsolutePath()
					+ "\"";

			System.out.println("launching container: " + execStr);

			if (Boot.isWindows())
			{
				// explicitly load dll...modifying java.library.path via
				// setProperty at runtime doesn't work!
				try
				{
					System.load(common.getAbsolutePath() + File.separatorChar + "process_jni.dll");
					System.out.println("explicitly loaded dll = process_jni.dll");

					myProcess = ProcessUtils.exec(execStr, new File(path).getAbsolutePath());

				}
				catch (final Throwable t)
				{
					t.printStackTrace();
				}
			}
			else
			{
				myProcess = Runtime.getRuntime().exec(Boot.parseCommandLine(execStr), null, new File(this.path));
			}

			final Process thisProcess = myProcess;
			try
			{
				final OutputStream ps = new BufferedOutputStream(new FileOutputStream(logFile));
				new StreamPump(myProcess.getErrorStream(), ps, true);
				new StreamPump(myProcess.getInputStream(), ps);
			}
			catch (final Exception e)
			{
				System.err.println("ERROR writing installation output to file: " + e);
				final DebugFrame debugFrame = new DebugFrame("Container Output", 400, 400);
				debugFrame.setVisible(true);
				debugFrame.processInputStream(myProcess.getErrorStream());
				debugFrame.processInputStream(myProcess.getInputStream());
			}
			new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						thisProcess.waitFor();
						System.out.println("joined container");
					}
					catch (final Exception e)
					{
						System.err.println("ERROR joining Container process: " + e);
					}
					synchronized (Container.this)
					{
						crashDetected(thisProcess);
					}
				}
			}.start();
		}
		catch (final Exception e)
		{
			if (BootGlobals.DEBUG)
			{
				e.getMessage();
			}
			return false;
		}
		iAmRunning = true;
		return true;
	}

	/**
	 * stop container process.
	 */
	public synchronized void stop()
	{
		if (BootGlobals.DEBUG)
		{
			System.err.println("Stopping container " + this);
		}
		if (this.myProcess == null || this.monitor == null)
		{
			return;
		}
		destroy(this.myProcess);
		this.myProcess = null;
		iAmRunning = false;
		iHaveCrashed = false;
	}

	/**
	 * destroy child process (chance for clean stop first)
	 */
	protected synchronized void destroy(final Process p)
	{
		if (p == null)
		{
			return;
		}
		// try sending "quit"
		try
		{
			System.out.println("Try clean shutdown of child process...");
			if (p.getOutputStream() != null)
			{
				p.getOutputStream().write("quit\n".getBytes());
				p.getOutputStream().flush();
				// hope for crashDetected
				this.wait(DESTROY_DELAY_MS);
			}
			if (iAmRunning)
			{
				System.out.println("Strange - still seem to be running...?");
			}
			else
			{
				System.out.println("shutdown complete");
				// done
				return;
			}
		}
		catch (final InterruptedException e)
		{
			System.err.println("Child process destroy timed out: " + e);
			e.printStackTrace(System.err);
		}
		catch (final Exception e)
		{
			System.err.println("Child process destroy error: " + e);
			e.printStackTrace();
		}
		System.out.println("Destroying child process!");
		// destroy
		p.destroy();
	}

	/**
	 * on shutdown of this JVM call {@link Process#destroy()} on the child container JVM.
	 */
	private void setupShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				System.out.println("killing launched container...");
				if (myProcess != null)
				{
					Container.this.destroy(myProcess);
				}
			}
		});
	}
}