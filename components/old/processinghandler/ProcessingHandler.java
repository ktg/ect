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
  Chris Greenhalgh (University of Nottingham)
  Chris Allsop (University of Nottingham)

 */
package equip.ect.components.processinghandler;

import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class ProcessingHandler implements Serializable, ProcessingHandlerConstants
{

	/** Property Change Delegate. */
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/** Bean Properties. */
	private String status = "Specify a URL to load an Applet";
	private URL url;
	private JarFile jarfile;
	private JarURLConnection conn;

	/** Non Bean Properties */
	private CustomAppletClassLoader loader;
	private static final String FILE_CHOOSER_TITLE = "Select a Jar or Class file exported from Processing";
	private JFrame frame;
	private JFileChooser filechooser;

	/**
	 * Contains all of the fixed Jar entries that are included in the exported Jar file required by
	 * the 'processing' language to run a processing applet in a browser environment.
	 * 
	 * The <code>FIXED_JAR_ENTRIES</code> are <i>likely</i> to change in future releases of the
	 * Processing language, as new functionality is added.
	 * 
	 * Current the <code>FIXED_JAR_ENTRIES</code> hashmap is set to include on static
	 * initialization, all 18 fixed export files included in the <a
	 * href="http://processing.org/download/index.html">0068 Alpha Release</a> of the software.
	 * 
	 * Ideally, the processing language should be included in a fixed directory in the ect toolkit
	 * so that the list of fixed jar files can be determined dynamically at runtime by adding
	 * entries to the hashmap for each file located in the
	 * <code><i>&gtProcessingDir&lt</i>/lib/export</code> directory.
	 * 
	 * NB as of version 0070 (approx.) these are completely wrong, and processing classes have been
	 * moved to the processing.core package.
	 * 
	 * Each element in the hashmap must contain the Filename string as the key and the corresponding
	 * {@link java.util.jar.JarEntry} instance.
	 */
	public final static HashMap FIXED_JAR_ENTRIES;
	static
	{
		FIXED_JAR_ENTRIES = new HashMap();
		FIXED_JAR_ENTRIES.put("BApplet$1.class", new JarEntry("BApplet$1.class"));
		FIXED_JAR_ENTRIES.put("BApplet$2.class", new JarEntry("BApplet$2.class"));
		FIXED_JAR_ENTRIES.put("BApplet$3.class", new JarEntry("BApplet$1.class"));
		FIXED_JAR_ENTRIES.put("BApplet$4.class", new JarEntry("BApplet$2.class"));
		FIXED_JAR_ENTRIES.put("BApplet$Stopper.class", new JarEntry("BApplet$Stopper.class"));
		FIXED_JAR_ENTRIES.put("BApplet.class", new JarEntry("BApplet.class"));
		FIXED_JAR_ENTRIES.put("BClient.class", new JarEntry("BClient.class"));
		FIXED_JAR_ENTRIES.put("BConstants.class", new JarEntry("BConstants.class"));
		FIXED_JAR_ENTRIES.put("BFont.class", new JarEntry("BFont.class"));
		FIXED_JAR_ENTRIES.put("BGraphics.class", new JarEntry("BGraphics.class"));
		FIXED_JAR_ENTRIES.put("BImage.class", new JarEntry("BImage.class"));
		FIXED_JAR_ENTRIES.put("BLine.class", new JarEntry("BLine.class"));
		FIXED_JAR_ENTRIES.put("BPolygon.class", new JarEntry("BPolygon.class"));
		FIXED_JAR_ENTRIES.put("BServer.class", new JarEntry("BServer.class"));
		FIXED_JAR_ENTRIES.put("BSonic.class", new JarEntry("BSonic.class"));
		FIXED_JAR_ENTRIES.put("BSound.class", new JarEntry("BSound.class"));
		FIXED_JAR_ENTRIES.put("BTriangle.class", new JarEntry("BTriangle.class"));
		FIXED_JAR_ENTRIES.put("BServer$BServerClient.class", new JarEntry("BServer$BServerClient.class"));
	}
	/**
	 * NB as of version 0070 (approx.) these are completely wrong, and processing classes have been
	 * moved to the processing.core package.
	 */
	public static final String PROCESSING_PACKAGE = "processing.";
	public static final String PROCESSING_PATH = "processing/";
	public static final String PROCESSING_CORE_PACKAGE = "processing.core.";
	public static final String PROCESSING_CORE_PATH = "processing/core/";

	/**
	 * log4j logger, used to print to console (System.out) by default.
	 * 
	 * @see <a href="http://logging.apache.org/log4j/docs/">Log4j Documentation</a>
	 */
	private static transient Logger logger = Logger.getLogger(ProcessingHandler.class);
	static
	{
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}

	public static void main(final String[] args)
	{
		final ProcessingHandler host = new ProcessingHandler(true);

		/** WORKING **/
		// host.setURL("C:\\ProcessingExpert\\sketchbook\\examples\\typography\\kinetic_type\\applet\\kinetic_type.class");
		/** WORKING **/
		// host.setURL("C:\\ProcessingExpert\\sketchbook\\examples\\typography\\kinetic_type\\applet\\");
		/** WORKING **/
		// host.setURL("C:\\ProcessingExpert\\sketchbook\\examples\\typography\\kinetic_type\\applet");
		/** WORKING **/
		// host.setURL("C:/ProcessingExpert/sketchbook\\examples\\typography\\helix\\applet/helix.jar!\\\n");
		/** WORKING **/
		// host.setURL("\nfile:///C:/ProcessingExpert/sketchbook\\examples\\typography\\helix\\applet/helix.jar!\\helix.class");

		/** WITH JAR SUBDIRECTORIES **/
		/** FAILED - DOESN'T WORK BECAUSE PROCESSING DOESN'T DIRECTLY SUPPORT PACKAGES **/
		// host.setURL("C:/ProcessingExpert/sketchbook\\examples\\typography\\kinetic_type\\applet/kinetic_type.jar!\\Subdir/");
		/** FAILED **/
		// host.setURL("C:/ProcessingExpert/sketchbook\\examples\\typography\\helix\\aplet/helix.jar!\\");
		/** FAILED **/
		// host.setURL("C:/ProcessingExpert/sketchbook\\examples\\typography\\kinetic_type\\applet/kinetic_type.jar!\\Subdir\\kinetic_type.class");

		/** WITH URLS **/
		/** WORKING **/
		// host.setURL("http://www.crg.cs.nott.ac.uk/~cmg/Equator/Downloads/docs/ect/data/choose4.jar!/choose4.class");
		/** WORKING **/
		// host.setURL("http://www.crg.cs.nott.ac.uk/~cmg/Equator/Downloads/docs/ect/data/choose4.jar!/");

	}

	public ProcessingHandler()
	{

		super();
		loader = new CustomAppletClassLoader();
		url = null;
		jarfile = null;
	}

	public ProcessingHandler(final boolean withGui)
	{

		super();
		loader = new CustomAppletClassLoader();
		url = null;
		jarfile = null;

		if (!withGui) { return; }

		frame = new JFrame(FILE_CHOOSER_TITLE);
		// frame.setDefaultCloseOperation(JFrame..EXIT_ON_CLOSE);

		filechooser = new JFileChooser(".\\");
		filechooser.setFileFilter(new CustomFileFilter(new String[] { JAR_EXTENSION, CLASS_EXTENSION },
				"Processing Jar or Class files"));

		filechooser.setAcceptAllFileFilterUsed(false);
		filechooser.setDragEnabled(false);
		filechooser.setMultiSelectionEnabled(false);

		filechooser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent event)
			{
				// Handle open button action.

				if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)
						&& filechooser.getSelectedFile() != null)
				{
					try
					{
						String urlpath = filechooser.getSelectedFile().toURL().toExternalForm();
						// the urlpath returned will not have the jar subprotocol or
						// jar extension included
						if (urlpath.indexOf(JAR_EXTENSION) != -1)
						{
							urlpath = JAR_SUBPROTOCOL + urlpath + JAR_SEPARATOR;
						}
						setURL(urlpath);

						JOptionPane.showMessageDialog(frame, status == null ? "null" : status);
						System.exit(0);

					}
					catch (final MalformedURLException mue)
					{
						logger.error("File chooser returned bad path!!!");
						return;
					}
				}
			}
		});

		frame.getContentPane().add(filechooser);
		frame.pack();
		frame.setVisible(true);
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public String correctInvalidPath(final String path, final Pattern knownErrorPattern)
	{

		String correctedPath = path;

		if (knownErrorPattern.equals(URLUtility.MISSING_JAR_SEPARATOR))
		{
			// Add the jar seperator after the .jar extension
			correctedPath = path.replaceFirst(JAR_EXTENSION, (JAR_EXTENSION + JAR_SEPARATOR));

		}
		else if (knownErrorPattern.equals(URLUtility.MISSING_JAR_SUBPROTOCOL))
		{
			// Add the jar subprotocol 'jar:' in front of the path
			correctedPath = JAR_SUBPROTOCOL + path;

		}
		else if (knownErrorPattern.equals(URLUtility.INCORRECT_USE_OF_JAR_SUBPROTOCOL))
		{
			// Remove the jar subprotocol 'jar:' from the front of the path
			correctedPath = path.substring(JAR_SUBPROTOCOL.length());

		}
		else if (knownErrorPattern.equals(URLUtility.MISSING_FILE_PROTOCOL))
		{
			// Add the file protocol 'file:/' either to the front of the
			// path or directly after the jar subprotocol 'jar:' if it
			// appears in the path
			if (path.indexOf(JAR_SUBPROTOCOL) == -1)
			{
				correctedPath = FILE_PROTOCOL + path;
			}
			else
			{
				correctedPath = path.replaceFirst(JAR_SUBPROTOCOL, JAR_SUBPROTOCOL + FILE_PROTOCOL);
			}

		}
		else if (knownErrorPattern.equals(URLUtility.MISSING_HTTP_PROTOCOL))
		{
			// Add the http protocol 'http:/' either to the front of the
			// path or directly after the jar subprotocol 'jar:' if it appears
			// in the path
			if (path.indexOf(JAR_SUBPROTOCOL) == -1)
			{
				correctedPath = HTTP_PROTOCOL + path;
			}
			else
			{
				correctedPath = path.replaceFirst(JAR_SUBPROTOCOL, JAR_SUBPROTOCOL + HTTP_PROTOCOL);
			}
		}
		return correctedPath;
	}

	public String getStatus()
	{
		return this.status;
	}

	/**
	 * Returns the String representation of the URL that this process-viewer is pointing to.
	 * 
	 * @return the path of the URL.
	 */
	public String getURL()
	{
		if (url != null)
		{
			return url.toExternalForm();
		}
		else
		{
			return null;
		}
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	/**
	 * Searches a directory for a class which extends Applet.
	 * 
	 * The first class file that extends Applet that is found <i>(searching of entries is in
	 * alphabetical order)</i> in the specified {@link java.io.File} is instantiated and returned by
	 * the method.
	 * <p>
	 * The search does not recurse through any subdirectories that may be present in the search
	 * directory that was specified.
	 * <p>
	 * 
	 * @param directory
	 *            the directory file which is to be searched.
	 * 
	 * @returns an instance of the first Applet found by the search.
	 * 
	 * @throws AppletNotFoundException
	 *             if no class that extends Applet was found in the directory or the if the the
	 *             directory given is invalid.
	 * 
	 * @throws AppletInstantiationException
	 *             if there was an error when trying to instantiate an applet that was found in the
	 *             search.
	 * @throws IOException
	 */
	public Applet searchDirectoryForApplet(final File directory) throws IOException, AppletNotFoundException,
			AppletInstantiationException
	{
		final Applet app = null;
		String classname = null;
		String filename = null;

		// double check
		if (!directory.isDirectory()) { throw new AppletNotFoundException(directory.getName() + " is not a directory"); }
		final File[] files = directory.listFiles();

		for (int i = 0; i < files.length; i++)
		{
			if (!files[i].isFile())
			{
				// ignore packages (including processing), at least for now
				continue;
			}
			filename = files[i].getName();

			if (!FIXED_JAR_ENTRIES.containsKey(filename) && filename.endsWith(CLASS_EXTENSION)
					&& filename.indexOf('$') == -1)
			{

				// remove the dot and extension from the filename
				classname = filename.substring(0, filename.length() - CLASS_EXTENSION.length());
				try
				{
					return loader.loadApplet(classname);

				}
				catch (final ClassCastException cce)
				{
					// ok since we may find a class that isn't an instance of Applet
					continue;
				}
			}
		} // end loop

		// if we never found the desired class file in the above while loop,
		// throw an exception
		if (app != null)
		{
			return app;
		}
		else
		{
			throw new AppletNotFoundException("Could not find an applet class in " + "directory " + directory.getName());
		}
	}

	/**
	 * Searches a jarfile for a class which extends Applet.
	 * 
	 * The first class file that extends Applet that is found <i>(searching of entries is in
	 * alphabetical order)</i> in the specified {@link java.util.jar.JarFile} is instantiated and
	 * returned by the method.
	 * <p>
	 * The search does not recurse through any subdirectories that may be present in the directory
	 * that was specified in the <code>jarfile</code>.
	 * <p>
	 * 
	 * @param jarfile
	 *            the jarfile which is to be searched.
	 * 
	 * @returns an instance of the first Applet found by the search.
	 * 
	 * @throws AppletNotFoundException
	 *             if no class was found in the jar file that extends Applet.
	 * @throws AppletInstantiationException
	 *             if there was an error when trying to instantiate an applet that was found in the
	 *             search.
	 * @throws IOException
	 */
	public Applet searchJarForApplet(final JarFile jarfile) throws IOException, AppletNotFoundException,
			AppletInstantiationException
	{
		final Applet app = null;
		JarEntry entry;
		String absoluteEntryname; // e.g. subdir1/subdir2/entry.class
		String entryname; // e.g. entry.class
		String classname;
		int indexOfSlash;
		int startOfEntryname;

		final Enumeration entries = jarfile.entries();

		while (entries.hasMoreElements())
		{
			entry = (JarEntry) entries.nextElement();
			absoluteEntryname = entry.getName();
			indexOfSlash = absoluteEntryname.lastIndexOf('/');
			startOfEntryname = (indexOfSlash == -1) ? 0 : (indexOfSlash + 1);

			entryname = absoluteEntryname;// why ignore package?? .substring(startOfEntryname);

			if (!FIXED_JAR_ENTRIES.containsKey(entryname) && !entryname.startsWith(PROCESSING_PATH)
					&& entryname.endsWith(CLASS_EXTENSION) && entryname.indexOf('$') == -1)
			{

				// remove the dot and extension from the entryname
				classname = entryname.substring(0, entryname.length() - CLASS_EXTENSION.length());

				try
				{
					return loader.loadApplet(absoluteEntryname.substring(0, absoluteEntryname.length()
							- CLASS_EXTENSION.length()));

				}
				catch (final ClassCastException cce)
				{
					logger.debug(classname + " caused a class cast exception");
					// ok since we may find a class that isn't an instance of Applet
					continue;
				}
			}
		}

		if (app != null)
		{
			return app;
		}
		else
		{
			throw new AppletNotFoundException("Could not find an applet class in jarfile" + jarfile.getName());
		}
	} // end search method

	// ----------------------------------------------------------------------------//

	public void setStatus(final String text)
	{
		propertyChangeListeners.firePropertyChange("status", this.status, text);
		this.status = text;
		logger.debug("Status = " + status);
	}

	/** Sets the URL path to a jar file, directory or applet for this bean. */
	public void setURL(String path)
	{

		Applet originalApplet;
		Pattern match = null;
		String correctedPath;
		final URL oldURL = url;
		boolean fileSpecified = false;
		int startIndex = 0;
		int endIndex = path.length();

		try
		{
			// remove leading white space and trailing white space and replace
			// all forward slashes with backslashesd
			while (path.charAt(startIndex) == '\n')
			{
				startIndex++;
			}
			while (path.charAt(endIndex - 1) == '\n')
			{
				endIndex--;
			}
			path = path.substring(startIndex, endIndex);
			path = path.replace('\\', '/');

			try
			{
				match = URLUtility.matchesPattern(path, URLUtility.VALID_PATTERNS);
				if (match.equals(URLUtility.JAR_SUBDIRECTORY) || match.equals(URLUtility.NONJAR_DIRECTORY))
				{
					// append the slash at the end if not already present for directories
					path += ((!path.endsWith("/")) ? "/" : "");
				}

			}
			catch (final NoMatchingPatternException nfpe)
			{
				// No valid match was found so match against common error patterns
				// *This call will throw any NoMatchingPatternExceptions to the
				// outer catch block*
				match = URLUtility.matchesPattern(path, URLUtility.ERROR_PATTERNS);
				correctedPath = correctInvalidPath(path, match);

				// call setURL recursively using the hopefully corrected Path
				if (!correctedPath.equals(path))
				{
					setURL(correctedPath);
					return;
				}
			}

			url = new URL(path); // shouldnt throw an exception since we have checked path
			propertyChangeListeners.firePropertyChange("url", oldURL, url);

			// close any previously opened jarfile
			if (jarfile != null)
			{
				jarfile.close();
			}
			// See if the pattern match refers to a Jar archive and if so,
			// connect to this jarfile
			if (match.equals(URLUtility.JAR_CLASSFILE) || match.equals(URLUtility.JAR_ARCHIVE)
					|| match.equals(URLUtility.JAR_SUBDIRECTORY))
			{
				conn = (JarURLConnection) url.openConnection();
				jarfile = conn.getJarFile();
			}
			else
			{
				jarfile = null;
			}

			// See if the pattern match refers to a specific class file
			fileSpecified = (match.equals(URLUtility.JAR_CLASSFILE) || match.equals(URLUtility.NONJAR_CLASSFILE));

			// load the applet from the jar, file or directory exactly as it
			// has been written
			originalApplet = retrieveApplet(url, fileSpecified);

			DynamicAppletGenerator.generateNewAppletCapability(jarfile, originalApplet);

			setStatus("Successfully generated capability for '" + originalApplet.getClass().getName() + "'.\n");

		}
		catch (final Exception ex)
		{
			setStatus(ex.getMessage());
		}
	}

	// ----------------------------------------------------------------------------//

	private Applet retrieveApplet(final URL url, final boolean filenameSpecified) throws AppletNotFoundException,
			AppletInstantiationException
	{

		String classname = null; // the classname to be loaded
		final String file = url.getFile(); // the file specified by the URL
		final String path = url.getPath(); // the path specified by the URL
		final String externalForm = url.toExternalForm(); // the complete URL as a string
		Applet retrievedApplet;

		try
		{

			if (filenameSpecified)
			{ // explicit path given
				/*
				 * Add the URL to the CustomAppletClassLoader. Since a file has been specified (e.g
				 * file:///C:/dir/filename.class) then we remove the filename to leave the directory
				 * name (& trailing '/') that the file is residing in.
				 */
				loader.addNewURL(new URL(externalForm.substring(0, externalForm.lastIndexOf('/') + 1)));

				// Remove the path to the file and its .class extension
				classname = file.substring(file.lastIndexOf('/') + 1, file.length() - CLASS_EXTENSION.length());

				retrievedApplet = loader.loadApplet(classname);

			}
			else if (path.indexOf(JAR_EXTENSION) != -1)
			{ // path to a jar archive

				loader.addNewURL(url);
				retrievedApplet = searchJarForApplet(this.jarfile);

			}
			else
			{ // path not to a jar archive

				loader.addNewURL(url);
				retrievedApplet = searchDirectoryForApplet(new File(path));

			} // end if/else

			return retrievedApplet;

		}
		catch (final MalformedURLException mue)
		{
			throw new AppletNotFoundException();

		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();

			throw new AppletInstantiationException("Couldn't create applet from URL: " + externalForm
					+ " due to an I/O error");
		}

	} // end retrieveApplet

} // end ProcessingHandler class
