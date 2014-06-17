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

import java.util.regex.Pattern;

/**
 * Class used to help check URL paths and return useful information to allow for easier debugging
 * and error handling.
 */
public abstract class URLUtility
{

	public static final String

	/** Regex: matches any combination of letters, numbers and symbols */
	ANYTHING = "(.*)",

	/** Regex: allows 'jar' to be specified in any combination of upper and lowercase */
	JAR_SUBPROTOCOL = "((?i)jar:)",

	/** Regex: the jar subprotocol or no subprotocol */
	JAR_SUBPROTOCOL_OR_NIL = "(" + JAR_SUBPROTOCOL + ")?",

	/** Regex: allows 'http' to be specified in any combination of upper and lowercase */
	HTTP_PROTOCOL = "((?i)http):(/*)",

	/** Regex: allows 'file' to be specified in any combination of upper and lowercase */
	FILE_PROTOCOL = "((?i)file):(/*)",

	/** Regex: always follows the 'jar' extension in URLs to jar archives */
	JAR_SEPARATOR = "!/",

	/** Regex: any drive letter between a-z (in upper or lower case) followed by ':/' */
	DRIVE = "([a-zA-Z]:/)",

	/**
	 * Regex: a filename can be any string that doesn't contain any of these \/:*?<>!.
	 * <p>
	 * <i>Note: </i> In later versions of Windows, '.' is allowed in a file name however this
	 * confuses pattern matching with file extensions and so the dot has been disallowed.
	 **/
	FILENAME = "([^\\/:*?\"<>!\\.]+)",

	/** Regex: a filename or nothing */
	FILENAME_OR_NIL = "(" + FILENAME + ")?",

	/** Regex: allows 'class' extension to be specified in any combination of upper and lowercase */
	CLASS_FILE = FILENAME + "\\.((?i)class)",

	/** Regex: a classfile or nothing */
	CLASS_FILE_OR_NIL = "(" + CLASS_FILE + ")?",

	/** Regex: allows 'jar' extension to be specified in any combination of upper and lowercase */
	JAR_FILE = FILENAME + "\\.((?i)jar)",

	/** Regex: a jarfile or nothing */
	JAR_FILE_OR_NIL = "(" + JAR_FILE + ")?",

	/** Regex: any valid filename followed by any valid extension (of arbitrary length) */
	ANY_FILE = FILENAME + "\\.([^\\/:*?\"<>!]*)",

	/** Regex: anyfile or nothing */
	ANY_FILE_OR_NIL = "(" + ANY_FILE + ")?",

	/**
	 * Regex: a path can be any number of filenames seperated by slashes with an optional slash at
	 * the end
	 */
	PATH = FILENAME + "(/" + FILENAME + ")*" + "/?",

	/** Regex: a path or nothing */
	PATH_OR_NIL = "(" + PATH + ")?",

	/**
	 * Regex: any valid 'internet' URL that doesn't use "reserved" or "unsafe" characters
	 * <p>
	 * <a href "http://www.blooberry.com/indexdot/html/topics/urlencoding.htm>URL Encoding
	 * Standards></a> states that "only alphanumerics [0-9a-zA-Z], the special characters:
	 * "$-_.+!*'()," [not including quotes], and reserved characters used for their reserved
	 * purposes may be used unencoded within a URL." This should handle %xx escape sequences; I've
	 * also added ~ for now :-)
	 * <p>
	 * <i>Note:</i> in this pattern the '!' character is not allowed since this causes confusion
	 * with the jar_separator {@see #JAR_SEPARATOR} sequence.
	 **/
	RESOURCE = "([~%0-9a-zA-Z$\\-_\\.\\+\\*'\\(\\),]*)",

	/**
	 * Regex: a URL path can be any number of resources seperated by slashes with an optional slash
	 * at the end
	 */
	URL_PATH = RESOURCE + "(/" + RESOURCE + ")*" + "/?",

	/** Regex: a URL path or nothing */
	URL_PATH_OR_NIL = "(" + URL_PATH + ")?";

	// VALID PATTERNS
	public static final Pattern NONJAR_CLASSFILE = Pattern.compile("(" + HTTP_PROTOCOL + URL_PATH + CLASS_FILE + ")"
			+ "|" + "(" + FILE_PROTOCOL + DRIVE + PATH_OR_NIL + CLASS_FILE + ")"),

	NONJAR_DIRECTORY = Pattern.compile("(" + FILE_PROTOCOL + DRIVE + PATH_OR_NIL + ")"),
	// URLS to 'Http directories' are not allowed

			JAR_ARCHIVE = Pattern.compile("(" + JAR_SUBPROTOCOL + HTTP_PROTOCOL + URL_PATH + JAR_FILE + JAR_SEPARATOR
					+ ")" + "|" + "(" + JAR_SUBPROTOCOL + FILE_PROTOCOL + DRIVE + PATH_OR_NIL + JAR_FILE
					+ JAR_SEPARATOR + ")"),

			JAR_CLASSFILE = Pattern.compile("(" + JAR_SUBPROTOCOL + HTTP_PROTOCOL + URL_PATH + JAR_FILE + JAR_SEPARATOR
					+ PATH_OR_NIL + CLASS_FILE + ")" + "|" + "(" + JAR_SUBPROTOCOL + FILE_PROTOCOL + DRIVE
					+ PATH_OR_NIL + JAR_FILE + JAR_SEPARATOR + PATH_OR_NIL + CLASS_FILE + ")"),

			JAR_SUBDIRECTORY = Pattern.compile("(" + JAR_SUBPROTOCOL + HTTP_PROTOCOL + URL_PATH + JAR_FILE
					+ JAR_SEPARATOR + PATH + ")" + "|" + "(" + JAR_SUBPROTOCOL + FILE_PROTOCOL + DRIVE + PATH_OR_NIL
					+ JAR_FILE + JAR_SEPARATOR + PATH + ")"),

			MISSING_JAR_SEPARATOR = Pattern.compile("(" + JAR_SUBPROTOCOL_OR_NIL + HTTP_PROTOCOL + URL_PATH + JAR_FILE
					+ PATH_OR_NIL + CLASS_FILE_OR_NIL + ")" + "|" + "(" + JAR_SUBPROTOCOL_OR_NIL + FILE_PROTOCOL
					+ DRIVE + PATH_OR_NIL + JAR_FILE + PATH_OR_NIL + CLASS_FILE_OR_NIL + ")"),

			MISSING_JAR_SUBPROTOCOL = Pattern.compile("(" + HTTP_PROTOCOL + URL_PATH + JAR_FILE + JAR_SEPARATOR
					+ PATH_OR_NIL + CLASS_FILE_OR_NIL + ")" + "|" + "(" + FILE_PROTOCOL + DRIVE + PATH_OR_NIL
					+ JAR_FILE + JAR_SEPARATOR + PATH_OR_NIL + CLASS_FILE_OR_NIL + ")"),

			INCORRECT_USE_OF_JAR_SUBPROTOCOL = Pattern.compile("(" + JAR_SUBPROTOCOL + HTTP_PROTOCOL + URL_PATH
					+ CLASS_FILE_OR_NIL + ")" + "|" + "(" + JAR_SUBPROTOCOL + FILE_PROTOCOL + DRIVE + PATH_OR_NIL
					+ CLASS_FILE_OR_NIL + ")"),

			MISSING_FILE_PROTOCOL = Pattern.compile(JAR_SUBPROTOCOL_OR_NIL + DRIVE + PATH_OR_NIL + "(" + JAR_FILE
					+ JAR_SEPARATOR + PATH_OR_NIL + ")?" + CLASS_FILE_OR_NIL),

			MISSING_HTTP_PROTOCOL = Pattern.compile(JAR_SUBPROTOCOL_OR_NIL + URL_PATH + "(" + JAR_FILE + JAR_SEPARATOR
					+ PATH_OR_NIL + ")?" + CLASS_FILE_OR_NIL);

	public final static Pattern[] VALID_PATTERNS = { NONJAR_CLASSFILE, NONJAR_DIRECTORY, JAR_ARCHIVE, JAR_CLASSFILE,
													JAR_SUBDIRECTORY };

	public final static Pattern[] ERROR_PATTERNS = { MISSING_JAR_SEPARATOR, MISSING_JAR_SUBPROTOCOL,
													INCORRECT_USE_OF_JAR_SUBPROTOCOL, MISSING_FILE_PROTOCOL,
													MISSING_HTTP_PROTOCOL };

	public final static String JAR_EXTENSION = ".jar";

	/**
	 * Checks to see if a URLConnection is valid or not by matching a valid pattern.
	 * <p>
	 * The <B>5</B> formats in which a URL address can be written for specifying a applet class file
	 * are:
	 * <ol>
	 * <li>
	 * <code><var>&ltprotocol&gt</var>///<var>&ltpath&gt</var>/<var>&ltname.class&gt</var></code></li>
	 * <br>
	 * e.g file:///C:/Processing/drawcircle.class
	 * <p>
	 * 
	 * <li>
	 * <code>jar:<var>&ltprotocol&gt</var>///<var>&ltpath&gt</var>/<var>&ltname.jar&gt</var>!/</code>
	 * </li>
	 * <br>
	 * e.g jar:file:///C:/Processing/simpleShapes.jar!/
	 * <p>
	 * 
	 * <li>
	 * <code>jar:<var>&ltprotocol&gt</var>///<var>&ltpath&gt</var>/<var>&ltname.jar&gt</var>!/<var>&ltname.class&gt</var></code>
	 * </li>
	 * <br>
	 * e.g jar:file:///C:/Processing/simpleShapes.jar!/drawcircle.class
	 * <p>
	 * 
	 * <li>
	 * <code>jar:<var>&ltprotocol&gt</var>///<var>&ltpath&gt</var>/<var>&ltname.jar&gt</var>!/<var>&ltjarpath&gt</var></code>
	 * /</li>
	 * <br>
	 * e.g jar:file:///C:/Processing/simpleShapes.jar!/classfiles/
	 * <p>
	 * 
	 * <li>
	 * <code>jar:<var>&ltprotocol&gt</var>///<var>&ltpath&gt</var>/<var>&ltname.jar&gt</var>!/<var>&ltjarpath&gt</var>/<var>&ltname.class&gt</var></code>
	 * </li>
	 * <br>
	 * e.g jar:file:///C:/Processing/simpleShapes.jar!/classfiles/drawcircle.class
	 * <p>
	 * </ol>
	 */

	/*
	 * A {@link MalformedAppletURLException} is thrown if the URL fails to match any of the above
	 * formats. The MalformedAppletURLException has a list of flags which will be set by this method
	 * to indicate which type of error(s) seems to be the cause of the problem. Code blocks can then
	 * catch MalformedAppletURLExceptions and try to rectify the bad url by acting upon the error
	 * flags that have been set by calling {@link MalformedAppletURLException#getFlags()}.
	 * 
	 * @param url The url that will be used to access a processing class or jar archive.
	 * 
	 * @throws MalformedAppletURLException if <code>url</code> is badly formed.
	 */
	public static Pattern matchesPattern(final String urlPath, final Pattern[] patterns)
			throws NoMatchingPatternException
	{

		Pattern pattern;
		for (final Pattern pattern2 : patterns)
		{

			pattern = pattern2;

			if (Pattern.matches(pattern.pattern(), urlPath)) { return pattern; }
		}
		throw new NoMatchingPatternException("Couldn't match " + urlPath + " with a recognised pattern.");
	}
}
