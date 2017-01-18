/*
<COPYRIGHT>

 * Copyright (c) 2003 Sun Microsystems, Inc. All  Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * 
</COPYRIGHT>
 */

/*
 * @(#)ExampleFileFilter.java	1.14 03/01/23
 */

package equip.ect.apps.configurationmgr;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

/**
 * A convenience implementation of FileFilter that filters out all files except for those type
 * extensions that it knows about.
 * <p>
 * Extensions are of the type ".foo", which is typically found on Windows and Unix boxes, but not on
 * Macinthosh. Case is ignored.
 * <p>
 * Example - create a new filter that filerts out all files but gif and jpg image files:
 * <p>
 * JFileChooser chooser = new JFileChooser(); ExampleFileFilter filter = new ExampleFileFilter( new
 * String{"gif", "jpg"}, "JPEG & GIF Images") chooser.addChoosableFileFilter(filter);
 * chooser.showOpenDialog(this);
 *
 * @author Jeff Dinkins
 * @version 1.14 01/23/03
 */
public class ExtensionFileFilter extends FileFilter
{
	//private static String TYPE_UNKNOWN = "Type Unknown";
	//private static String HIDDEN_FILE = "Hidden File";

	private static Map<String, ExtensionFileFilter> filters = new HashMap<>();

	private String description = null;

	private String fullDescription = null;

	/**
	 * Creates a file filter that accepts the given file type. Example: new ExampleFileFilter("jpg",
	 * "JPEG Image Images");
	 * <p>
	 * Note that the "." before the extension is not needed. If provided, it will be ignored.
	 *
	 * @see #addExtension
	 */
	ExtensionFileFilter(final String extension, final String description)
	{
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
	 * Return true if this file should be shown in the directory pane, false if it shouldn't.
	 * <p>
	 * Files that begin with "." are ignored.
	 *
	 * @see #getExtension
	 * @see FileFilter#accept(File)
	 */
	@Override
	public boolean accept(final File f)
	{
		if (f != null)
		{
			if (f.isDirectory())
			{
				return true;
			}
			final String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a filetype "dot" extension to filter against.
	 * <p>
	 * For example: the following code will create a filter that filters out all files except those
	 * that end in ".jpg" and ".tif":
	 * <p>
	 * ExampleFileFilter filter = new ExampleFileFilter(); filter.addExtension("jpg");
	 * filter.addExtension("tif");
	 * <p>
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	private void addExtension(final String extension)
	{
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example: "JPEG and GIF Image Files
	 * (*.jpg, *.gif)"
	 *
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
				final Iterator<String> extensions = filters.keySet().iterator();
				fullDescription += "." + extensions.next();
				while (extensions.hasNext())
				{
					fullDescription += ", ." + extensions.next();
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
	private String getExtension(final File f)
	{
		if (f != null)
		{
			final String filename = f.getName();
			final int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1)
			{
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in the human readable
	 * description.
	 * <p>
	 * Only relevent if a description was provided in the constructor or using setDescription();
	 */
	private boolean isExtensionListInDescription()
	{
		return true;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 */
	private void setDescription(final String description)
	{
		this.description = description;
		fullDescription = null;
	}
}
