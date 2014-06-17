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

Created by: Jan Humble (University of Nottingham)
Contributors:
  Jan Humble (University of Nottingham)
  Tom Rodden (University of Nottingham)
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.blogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * BlogPoster is an API client to post to Movable Type blogs. It includes methods for posting files
 * locally to the blog.
 * 
 */
public class BlogPoster
{

	public static final String BLOGGER_APPLICATION_KEY = "NOT APPLICABLE";

	public static byte[] getBytesFromFile(final String path, final String filename)
	{
		try
		{
			final File file = new File(path, filename);
			final FileInputStream is = new FileInputStream(file);
			final byte[] bytes = new byte[is.available()];
			is.read(bytes);
			return bytes;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] getBytesFromURL(final URL url)
	{
		try
		{
			final URLConnection conn = url.openConnection();
			final InputStream is = conn.getInputStream();
			final int length = conn.getContentLength();
			final byte[] bytes = new byte[length];
			System.out.println("BlogPoster- Reading content: " + conn.getContentType() + ", length: " + length);
			int read = is.read(bytes);
			if (read != length)
			{
				while (read < length)
				{
					read += is.read(bytes, read, length - read);
					System.out.println("Actually read " + read);
				}
			}
			return bytes;
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void main(final String[] argv)
	{
		final BlogPoster blogger = new BlogPoster("http://ada.sics.se/mt/mt-xmlrpc.cgi", "family1", "family1", "6",
				"wwwcache.nott.ac.uk", "3128");
		System.out.println(blogger.getBlogIDByName("ACCORD"));
		System.out.println(blogger.postMediaObject(".", "test.jpg", "Testing"));
		/*
		 * String postID = blogger.createPost(
		 * "<TITLE>New</TITLE>\n<DESCRIPTION>Jan testing java interface blog postings</DESCRIPTION>"
		 * , true); if (postID != null) { System.out.println("Post ok, id = " + postID); } else {
		 * System.out.println("Post error"); }
		 */
	}

	private String currentBlogID = "6"; // Accord

	private XmlRpcClient client;

	private String server, username, password;

	private String lastPostID;

	public BlogPoster(final String server, final String username, final String password, final String blogID)
	{
		this(server, username, password, blogID, null, null);
	}

	public BlogPoster(final String server, final String username, final String password, final String blogID,
			final String proxyHost, final String proxyPort)
	{
		this.username = username;
		this.password = password;
		setCurrentBlogID(blogID);

		if (proxyHost != null)
		{
			System.setProperty("http.proxyHost", proxyHost);
		}
		if (proxyPort != null)
		{
			System.setProperty("http.proxyPort", proxyPort);
		}
		// XmlRpc.setDebug(true);
		try
		{
			client = new XmlRpcClient(server);
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new textual posting, accepting HTML formatting.
	 */
	public String createPost(final String text, final boolean publish)
	{
		try
		{
			final Vector params = new Vector();
			params.addElement(BLOGGER_APPLICATION_KEY);
			params.addElement(currentBlogID);
			params.addElement(username);
			params.addElement(password);
			params.addElement(text);
			params.addElement(new Boolean(publish));
			lastPostID = (String) client.execute("blogger.newPost", params);
			return lastPostID;
		}
		catch (final XmlRpcException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the id for the given blog from the given name.
	 */
	public String getBlogIDByName(final String blogName)
	{
		final Vector blogs = getUsersBlogs();
		if (blogs != null)
		{
			final Iterator it = blogs.iterator();
			while (it.hasNext())
			{
				final Hashtable blogInfo = (Hashtable) it.next();
				final String name = (String) blogInfo.get("blogName");
				if (name.equals(blogName)) { return (String) blogInfo.get("blogid"); }
			}
		}
		return null;
	}

	/**
	 * Returns a vector of structs - Hashtables - each containing the data for the nrOfPosts.
	 */
	public Vector getRecentPosts(final int nrOfPosts)
	{
		try
		{
			final Vector params = new Vector();
			params.addElement(BLOGGER_APPLICATION_KEY);
			params.addElement(currentBlogID);
			params.addElement(username);
			params.addElement(password);
			params.addElement(new Integer(nrOfPosts));
			return (Vector) client.execute("blogger.getRecentPosts", params);
		}
		catch (final XmlRpcException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns all the blogs which the current user has access to.
	 */
	public Vector getUsersBlogs()
	{
		try
		{
			final Vector params = new Vector();
			params.addElement(BLOGGER_APPLICATION_KEY);
			params.addElement(username);
			params.addElement(password);
			return (Vector) client.execute("blogger.getUsersBlogs", params);
		}
		catch (final XmlRpcException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the post id.
	 */
	public String postMediaObject(final byte[] data, final String filename, final String description)
	{
		final String url = transferMediaObject(data, filename);
		final String text = "<img src=\"" + url + "\">\n" + description;
		return createPost(text, true);
	}

	/**
	 * Returns the post id.
	 */
	public String postMediaObject(final String path, final String filename, final String description)
	{
		final String url = transferMediaObject(path, filename);
		final String text = "<img src=\"" + url + "\">\n" + description;
		return createPost(text, true);
	}

	/**
	 * Sets the current blogID to which to interact with.
	 */
	public void setCurrentBlogID(final String id)
	{
		this.currentBlogID = id;
	}

	/**
	 * Returns the url of the residence of the transfered object.
	 */
	public String transferMediaObject(final byte[] data, final String filename)
	{

		final Hashtable file = new Hashtable();
		file.put("bits", data);
		file.put("name", filename);
		file.put("type", "image");
		try
		{
			final Vector params = new Vector();
			params.addElement(currentBlogID);
			params.addElement(username);
			params.addElement(password);
			params.addElement(file);
			// XmlRpc.setDebug(true);
			final Hashtable info = (Hashtable) client.execute("metaWeblog.newMediaObject", params);
			final String url = (String) info.get("url");
			return url;
		}
		catch (final XmlRpcException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns the url to the residence of the transfered object.
	 */
	public String transferMediaObject(final String path, final String filename)
	{
		final byte[] bytes = getBytesFromFile(path, filename);
		if (bytes != null) { return transferMediaObject(bytes, filename); }
		return null;
	}

	public String transferMediaObject(final URL url, final String filename)
	{
		final byte[] bytes = getBytesFromURL(url);
		if (bytes != null) { return transferMediaObject(bytes, filename); }
		return null;
	}
}
