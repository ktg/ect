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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter
{

	private static String TYPE_UNKNOWN = "Type Unknown";
	private static String HIDDEN_FILE = "Hidden File";

	private Hashtable filters = null;
	private String description = null;
	private String fullDescription = null;
	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are accepted.
	 * 
	 * @see #addExtension
	 */
	public CustomFileFilter()
	{
		this.filters = new Hashtable();
	}

	/**
	 * Creates a file filter that accepts files with the given extension. Example: new
	 * ExampleFileFilter("jpg");
	 * 
	 * @see #addExtension
	 */
	public CustomFileFilter(final String extension)
	{
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new ExampleFileFilter("jpg",
	 * "JPEG Image Images");
	 * 
	 * Note that the "." before the extension is not needed. If provided, it will be ignored.
	 * 
	 * @see #addExtension
	 */
	public CustomFileFilter(final String extension, final String description)
	{
		this();
		if (extension != null)
		{
			addExtension(extension);
		}
		if (description != null)
		{
			setDescription(description);
		}
	}

	/**
	 * Creates a file filter from the given string array. Example: new ExampleFileFilter(String
	 * {"gif", "jpg"});
	 * 
	 * Note that the "." before the extension is not needed adn will be ignored.
	 * 
	 * @see #addExtension
	 */
	public CustomFileFilter(final String[] filters)
	{
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description. Example: new
	 * ExampleFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 * 
	 * @see #addExtension
	 */
	public CustomFileFilter(final String[] filters, final String description)
	{
		this();
		for (final String filter : filters)
		{
			// add filters one by one
			addExtension(filter);
		}
		if (description != null)
		{
			setDescription(description);
		}
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	 * @see #getExtension
	 * @see FileFilter#accepts
	 */
	@Override
	public boolean accept(final File f)
	{
		if (f != null)
		{
			if (f.isDirectory()) { return true; }
			final String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) { return true; };
		}
		return false;
	}

	/**
	 * Adds a filetype "dot" extension to filter against.
	 * 
	 * For example: the following code will create a filter that filters out all files except those
	 * that end in ".jpg" and ".tif":
	 * 
	 * ExampleFileFilter filter = new ExampleFileFilter(); filter.addExtension("jpg");
	 * filter.addExtension("tif");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	public void addExtension(String extension)
	{
		if (filters == null)
		{
			filters = new Hashtable(5);
		}

		// ignore '.' if provided
		if (extension.charAt(0) == '.')
		{
			extension = extension.substring(1, extension.length());
		}

		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example:
	 * "JPEG and GIF Image Files (*.jpg, *.gif)"
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 * @see FileFilter#getDescription
	 */
	@Override
	public String getDescription()
	{
		if (fullDescription == null)
		{
			if (description == null || isExtensionListInDescription())
			{
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				final Enumeration extensions = filters.keys();
				if (extensions != null)
				{
					fullDescription += "." + (String) extensions.nextElement();
					while (extensions.hasMoreElements())
					{
						fullDescription += ", ." + (String) extensions.nextElement();
					}
				}
				fullDescription += ")";
			}
			else
			{
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Return the extension portion of the file's name .
	 * 
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public String getExtension(final File f)
	{
		if (f != null)
		{
			final String filename = f.getName();
			final int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) { return filename.substring(i + 1).toLowerCase(); };
		}
		return null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in the human readable
	 * description.
	 * 
	 * Only relevent if a description was provided in the constructor or using setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see setExtensionListInDescription
	 */
	public boolean isExtensionListInDescription()
	{
		return useExtensionsInDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 */
	public void setDescription(final String description)
	{
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in the human readable
	 * description.
	 * 
	 * Only relevent if a description was provided in the constructor or using setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see isExtensionListInDescription
	 */
	public void setExtensionListInDescription(final boolean b)
	{
		useExtensionsInDescription = b;
		fullDescription = null;
	}
}
