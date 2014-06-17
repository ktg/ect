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

import equip.ect.util.URLUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;

/**
 * First port of call for Webstart. Sorts out autostarting installations.
 *
 * @author jym
 */
public class Boot
{

	/**
	 * default download/config URL
	 */
	public static final String DEFAULT_DOWNLOAD_URL = "http://www.cs.nott.ac.uk/~sxi/ect/download_xml";
	/**
	 * log file prefix
	 */
	public static final String LOG_PREFIX = "console-";
	/**
	 * log file suffix
	 */
	public static final String LOG_SUFFIX = ".txt";
	/**
	 * proxy property names
	 */
	public static final String httpProperties[] = {"http.proxyHost", "http.proxyPort", "proxyHost", "proxyPort",
	                                               "http.nonProxyHosts"};
	/**
	 * the instance
	 */
	protected static Boot _instance;
	static private DebugFrame debugFrame = null;
	static private BootFrame bf;

	/**
	 * copies output to debugFrame using {@link equip.ect.webstart.DebugFrame#out}.
	 */
	static class FilteredStream extends FilterOutputStream
	{
		OutputStream os;

		public FilteredStream(final OutputStream os)
		{
			super(os);
			this.os = os;
		}

		@Override
		public void write(final byte b[]) throws IOException
		{
			final String s = new String(b);
			debugFrame.out(s);
			if (os != null)
			{
				os.write(b);
			}
		}

		@Override
		public void write(final byte b[], final int off, final int len) throws IOException
		{
			final String s = new String(b, off, len);
			debugFrame.out(s);
			if (os != null)
			{
				os.write(b, off, len);
			}
		}
	}

	public static String addJarsToClassPath(final File directory)
	{
		String classPath = "";
		if (directory != null)
		{
			final File files[] = directory.listFiles();
			if (files != null)
			{
				for (final File file : files)
				{
					try
					{
						JarFile jarFile = new JarFile(file);
						if (jarFile.size() > 0)
						{
							classPath += File.pathSeparatorChar + file.getCanonicalPath();
						}
					}
					catch (final IOException e)
					{
						// ignore
					}
				}
			}
		}
		return classPath;
	}

	public static String deriveFullWebStartClassPath()
	{
		try
		{
			String wsClassPath = makePathAbsolute(System.getProperty("java.class.path") + File.pathSeparator + ".");
			final String jarURL = Boot.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
			System.out.println("deriveFullWebStartClassPath: " + jarURL);
			final File jarFile = jarURL.toLowerCase().startsWith("file:") ? new File(URLUtils.decode(jarURL
					.substring(5))) : new File(new URI(jarURL));
			if (jarFile.exists())
			{
				wsClassPath += File.pathSeparatorChar + jarFile.getCanonicalPath();
				final File parent = jarFile.getParentFile();
				final File files[] = parent.listFiles();
				for (final File file : files)
				{
					if (file.isDirectory())
					{
						wsClassPath += File.pathSeparatorChar + addJarsToClassPath(file);
					}
				}
			}
			return wsClassPath;
		}
		catch (final Exception e)
		{
			// ignore
			return makePathAbsolute(System.getProperty("java.class.path", "."));
		}
	}

	/**
	 * get java command properties to deal with proxy settings
	 */
	public static String getProxyProperties()
	{
		// e.g. -Dhttp.proxyHost=wwwcache.nottingham.ac.uk -Dhttp.proxyPort=3128
		// -DproxyHost=wwwcache.nottingham.ac.uk -DproxyPort=3128
		final StringBuffer b = new StringBuffer();
		for (final String httpPropertie : httpProperties)
		{
			final String p = System.getProperty(httpPropertie);
			if (p != null)
			{
				b.append(" -D");
				b.append(httpPropertie);
				b.append("=\"");
				b.append(p);
				b.append("\" ");
			}
		}
		return b.toString();
	}

	public static boolean isWindows()
	{
		final String os = System.getProperty("os.name", "").toLowerCase();
		return os.startsWith("windows");
	}

	/**
	 * main method - no args.
	 */
	public static void main(final String[] argsv)
	{
		// static {

		// }
		final Boot b = new Boot();
	}

	/**
	 * helper method to make sure all path elements are absolute
	 */
	public static String makePathAbsolute(final String path)
	{
		final java.util.StringTokenizer toks = new java.util.StringTokenizer(path, File.pathSeparator, false);
		final StringBuffer buf = new StringBuffer();
		while (toks.hasMoreTokens())
		{
			final String t = toks.nextToken();
			if (t.equals(""))
			{
				continue;
			}
			if (buf.length() > 0)
			{
				buf.append(File.pathSeparator);
			}
			try
			{
				buf.append(new File(t).getCanonicalPath());
			}
			catch (final Exception e)
			{
				System.err.println("ERROR getting canonical path for file " + t + ": " + e);
				e.printStackTrace(System.err);
			}
		}
		return buf.toString();
	}

	/**
	 * helper to parse command line taking account of quotes
	 */
	public static String[] parseCommandLine(final String cmd)
	{
		System.out.println("Parse command line " + cmd);
		final Vector v = new Vector();
		int from, to;
		int quoteCount = 0;
		for (from = 0, to = 0; from < cmd.length(); )
		{
			if (to >= cmd.length())
			{
				// off the end - must be it
				if (to > from)
				{
					final String arg = cmd.substring(from);
					// start and end with quotes? Leave them in, at least on windows!!! - java
					// appears to re-parse command line
					// if (arg.length()>=2 && arg.charAt(0)=='"' && arg.charAt(arg.length()-1)=='"')
					// arg = arg.substring(1, arg.length()-1);
					System.out.println("arg[" + v.size() + "]=" + arg);
					v.addElement(arg);
				}
				break;
			}
			final char c = cmd.charAt(to);
			if (c == '"')
			{
				quoteCount++;
				to++;
				continue;
			}
			if (Character.isWhitespace(c) && (quoteCount & 1) == 0)
			{
				// unquoted space - next word?!
				if (to > from)
				{
					final String arg = cmd.substring(from, to);
					// start and end with quotes? Leave them in, at least on windows!!! - java
					// appears to re-parse command line
					// if (arg.length()>=2 && arg.charAt(0)=='"' && arg.charAt(arg.length()-1)=='"')
					// arg = arg.substring(1, arg.length()-1);
					System.out.println("arg[" + v.size() + "]=" + arg);
					v.addElement(arg);
				}
				to++;
				from = to;
				continue;
			}
			// append
			to++;
		}
		return (String[]) v.toArray(new String[v.size()]);
	}

	public static void redirectOutput(final File file)
	{
		redirectOutput(file, "Console output");
	}

	public static void redirectOutput(final File file, final String name)
	{
		try
		{
			debugFrame = new DebugFrame(name, 600, 600);
			debugFrame.setBufferSize(DebugFrame.LARGE_BUFFER_SIZE);

			final PrintStream out = new PrintStream(new FilteredStream(new FileOutputStream(file)));
			System.setOut(out);
			System.setErr(out);
		}
		catch (final Exception e)
		{
			System.out.println("error piping to output file " + file);
		}
	}

	public static void redirectOutput(final String file)
	{
		redirectOutput(file, "Console output");
	}

	public static void redirectOutput(final String fileStr, final String name)
	{
		redirectOutput(new File(fileStr), name);
	}

	/**
	 * re-show boot frame
	 */
	public static void showBootFrame()
	{
		// reload installation property files?!
		_instance.loadProperties();
		if (_instance.checkAutostart(bf.getKnownInsts()))
		{
			Boot.bf.setVisible(true);
		}
	}

	static public void showOutput()
	{
		if (debugFrame != null)
		{
			debugFrame.setVisible(true);
		}
	}

	private HostManagerFrame hmf;
	private java.util.Properties hostProperties = new java.util.Properties();

	/**
	 * main cons - get the installation manager going.
	 * <ul>
	 * <li>Reads boot properties file, {@link BootGlobals#PROP_FILE_NAME}. If present:</li>
	 * <ul>
	 * <li>creates {@link Installation} objects for previously known installations from their
	 * directories.</li>
	 * <li>calls {@link Installation#start} if installation is in boot props
	 * {@link BootGlobals#STARTUP_PROP} value</li>
	 * </ul>
	 * <li>If no successful auto-start, creates a {@link BootFrame} to allow user
	 * configuration/interaction.</li> </ul>
	 */
	public Boot()
	{
		System.setProperty("user.home", ".");
		_instance = this;
		setOutput(BootGlobals.getLocalFilestoreRoot(), "ECT Boot Manager Console");
		boolean prompt = true;
		// Check for reincarnation ... auto-start installations
		if (BootGlobals.DEBUG)
		{
			System.err.println("Checking for reincarnation...");
		}
		loadProperties();

		String rootLoc = null;
		try
		{
			rootLoc = BootGlobals.getLocalFilestoreRoot().getCanonicalPath();
		}
		catch (final IOException e)
		{
			System.err.println("ERROR getting canonical path for filestore root " + BootGlobals.getLocalFilestoreRoot()
					+ ": " + e);
			rootLoc = BootGlobals.getLocalFilestoreRoot().getAbsolutePath();
		}
		final Map<String, Installation> knownInsts = new HashMap<String, Installation>();
		String insts = null;
		String[] prevInsts = null;
		if (hostProperties != null)
		{
			insts = BootGlobals.listSubDirs(BootGlobals.getLocalFilestoreRoot());
			prevInsts = BootGlobals.parseStringArray(insts);
			// Iterate through the installations we know about, creating Installation objects for
			// them...
			if (BootGlobals.DEBUG)
			{
				System.err.println("Iterating known installations...");
			}
			for (final String prevInst : prevInsts)
			{
				knownInsts.put(prevInst, new Installation(new File(rootLoc + BootGlobals.getSeparator()
						+ prevInst), false));
			}
		}
		else if (BootGlobals.DEBUG)
		{
			System.err.println("Host boot.prop not present...");
		}

		bf = new BootFrame(knownInsts);
		prompt = checkAutostart(knownInsts);

		if (BootGlobals.DEBUG)
		{
			System.err.println("Installation setup finished, start boot frame: " + prompt);
		}
		// else give the user choice instead ...
		if (prompt)
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("Firing up BootFrame...");
			}
			// NB. bf responsible for discovery of new Installations
			bf.setVisible(true);
		}
	}

	/**
	 * sets {@link System#out} and {@link System#err} to a
	 * {@link equip.ect.webstart.Boot.FilteredStream} which appends to debugFrame.
	 */
	public void setOutput()
	{
		debugFrame.setVisible(true);
		final PrintStream ps = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
		System.setOut(ps);
		System.setErr(ps);
	}

	protected boolean checkAutostart(final Map<String, Installation> knownInstallations)
	{
		boolean prompt = true;

		if (hostProperties != null)
		{
			final String insts = BootGlobals.listSubDirs(BootGlobals.getLocalFilestoreRoot());
			// if bootInst isn't empty/stupid, start() those installations listed, and we're done
			// here...
			if (BootGlobals.DEBUG)
			{
				System.err.println("Checking for auto-start installations...");
			}
			final String bootTemp = hostProperties.getProperty(BootGlobals.STARTUP_PROP);
			if (bootTemp != null && !bootTemp.equals(BootGlobals.NULL_VALUE))
			{
				final String[] bootInst = BootGlobals.parseStringArray(bootTemp);
				for (int i = 0; i < bootInst.length; i++)
				{
					if (insts.indexOf(bootInst[i]) > -1)
					{
						// Don't autostart/join an already started installation (on the same
						// machine)
						if (!knownInstallations.get(bootInst[i]).isMaster()
								&& knownInstallations.get(bootInst[i]).hasDataspaceFolder())
						{
							continue;
						}
						final JFrame yesno = new JFrame("Auto-start");
						yesno.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
						yesno.getContentPane().setLayout(new BorderLayout());
						final int countdown = 10;
						final JLabel text = new JLabel("Auto-start installation " + bootInst[i] + " (" + countdown
								+ ")?");
						yesno.getContentPane().add(text, BorderLayout.CENTER);
						final JPanel buttonp = new JPanel();
						buttonp.setLayout(new FlowLayout());
						final int status[] = new int[1];
						yesno.addWindowListener(new WindowAdapter()
						{
							@Override
							public void windowClosing(final WindowEvent e)
							{
								System.err.println("Abandon autostart");
								synchronized (status)
								{
									status[0] = 2;
									status.notify();
								}
							}
						});
						buttonp.add(new JButton(new AbstractAction("OK")
						{
							@Override
							public void actionPerformed(final ActionEvent ae)
							{
								System.out.println("OK");
								synchronized (status)
								{
									status[0] = 1;
									status.notify();
								}
							}
						}));
						buttonp.add(new JButton(new AbstractAction("Cancel")
						{
							@Override
							public void actionPerformed(final ActionEvent ae)
							{
								System.out.println("Cancel");
								synchronized (status)
								{
									status[0] = 2;
									status.notify();
								}
							}
						}));
						yesno.getContentPane().add(buttonp, BorderLayout.SOUTH);
						yesno.pack();
						yesno.setVisible(true);
						final int fi = i;
						new Thread()
						{
							@Override
							public void run()
							{
								final long start = System.currentTimeMillis();
								synchronized (status)
								{
									while (status[0] == 0)
									{
										try
										{
											status.wait(200);
										}
										catch (final InterruptedException e)
										{
											break;
										}
										final long now = System.currentTimeMillis();
										final int seconds = (int) (countdown - (now - start) / 1000);
										SwingUtilities.invokeLater(new Runnable()
										{
											@Override
											public void run()
											{
												text.setText("Auto-start installation " + bootInst[fi] + " (" + seconds
														+ ")?");

											}
										});
										if (seconds <= 0)
										{
											break;
										}
									}
								}
								SwingUtilities.invokeLater(new Runnable()
								{
									@Override
									public void run()
									{
										yesno.setVisible(false);
										boolean prompt2 = true;
										if (status[0] != 2)
										{
											// start!
											final boolean success = ((Installation) (knownInstallations.get(bootInst[fi])))
													.start(null);
											if (prompt2 && success)
											{
												prompt2 = false;
											}
										}
										// else give the user choice instead ...
										if (prompt2)
										{
											if (BootGlobals.DEBUG)
											{
												System.err.println("Firing up BootFrame...");
											}
											// NB. bf responsible for discovery of new Installations
											bf.setVisible(true);
										}
									}
								});
							}
						}.start();
						// not now, later
						prompt = false;
					}
				}
			}
			else if (BootGlobals.DEBUG)
			{
				System.err.println("No startup property defined...");
			}
		}
		else if (BootGlobals.DEBUG)
		{
			System.err.println("Host boot.prop not present...");
		}

		return prompt;

	}

	protected void loadProperties()
	{
		String rootLoc = null;
		try
		{
			rootLoc = BootGlobals.getLocalFilestoreRoot().getCanonicalPath();
		}
		catch (final IOException e)
		{
			System.err.println("ERROR getting canonical path for filestore root " + BootGlobals.getLocalFilestoreRoot()
					+ ": " + e);
			rootLoc = BootGlobals.getLocalFilestoreRoot().getAbsolutePath();
		}
		final String bootLoc = rootLoc + BootGlobals.getSeparator() + BootGlobals.PROP_FILE_NAME;
		hostProperties = BootGlobals.loadPropertyFile(new File(bootLoc));
		String download = DEFAULT_DOWNLOAD_URL;
		if (hostProperties != null)
		{
			download = hostProperties.getProperty("download", download);
		}
		download = System.getProperty("download", download);
		System.setProperty("download", download);
	}

	/**
	 * sets {@link System#out} and {@link System#err} to a
	 * {@link equip.ect.webstart.Boot.FilteredStream} which appends to debugFrame.
	 */
	private void setOutput(final File dir, final String name)
	{
		redirectOutput(new File(dir, LOG_PREFIX + System.currentTimeMillis() + LOG_SUFFIX), name);
	}
}
