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

 */
package equip.ect.components.ipod;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Registers mouse and key events. <H3>Summary</H3> Registers mouse and key events.
 * 
 * <H3>Description</H3> A straightforward component to register mouse and key events from a dummy
 * frame. This is basically intended to monitor GUI interactions.
 * 
 * <H3>Installation</H3>
 * 
 * 
 * <H3>Configuration</H3>
 * 
 * <H3>Usage</H3> Run the component and a dummy frame should be created to monitor all mouse and key
 * events.
 * 
 * <H3>Technical Details</H3> Currently uses a dummy window to listen in to the events.
 * 
 * @classification Media/GUI
 * @technology IPod interfacing
 * @author humble
 * 
 */
public class IPodInterface
{

	class MediaFileSorter
	{

		void sort(final File file)
		{
			final String name = file.getName();
			if (name.endsWith(".mp3"))
			{
				mp3Cache.add(createMP3Resource(file));
			}
			else if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".avi"))
			{
				videoCache.add(createVideoResource(file));
			}
			else if (name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png"))
			{
				imageCache.add(createImageResource(file));
			}
		}
	}

	public static MP3Resource createMP3Resource(final File mp3File)
	{
		final MP3Meta meta = ID3Utils.scanMp3VdHeide(mp3File);
		String path = "";
		try
		{
			path = mp3File.getCanonicalPath().intern();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String resourceURL = "file:/" + path;
		return new MP3Resource(resourceURL, meta);
	}

	public static void main(final String[] args)
	{
		final IPodInterface ii = new IPodInterface();
		// System.out.println("name= " + ii.getIPodName());
		ii.setScanDirectory(args[0]);
		// ii.loadIPodDatabase();
		// System.out.println("name = " + ii.getIPodName());

		ii.getAllMedia();
		System.out.println("Doing cache to HTML");
		ii.cacheToHTML("jans_ipod_media.html");
		// ii.loadIPodDatabase();
	}

	public static Hashtable MP3MetaToHashtable(final MP3Meta meta)
	{
		final Hashtable mappings = new Hashtable();
		mappings.put("Album", meta.getAlbum());
		mappings.put("Artist", meta.getArtist());
		mappings.put("ArtistWebpage", meta.getArtistWebpage());
		mappings.put("AudioFileWebpage", meta.getAudioFileWebpage());
		mappings.put("AudioSourceWebpage", meta.getAudioSourceWebpage());
		mappings.put("Band", meta.getBand());
		mappings.put("Bitrate", String.valueOf(meta.getBitrate()));
		mappings.put("Bpm", meta.getBpm());
		mappings.put("CDIdentifier", meta.getCdIdentifier());
		mappings.put("Comment", meta.getComment());
		mappings.put("Composer", meta.getComposer());
		mappings.put("Conductor", meta.getConductor());
		mappings.put("Date", meta.getDate());
		mappings.put("Duration", String.valueOf(meta.getDuration()));
		mappings.put("Equalisation", meta.getEqualisation());
		mappings.put("Filename", meta.getFilename());
		mappings.put("FilePath", meta.getFilePath());
		mappings.put("FileType", meta.getFileType());
		mappings.put("FileLength", String.valueOf(meta.getFilesize()));
		mappings.put("Genre", meta.getGenre());
		mappings.put("InternetRadioStationName", meta.getInternetRadioStationName());
		mappings.put("InternetRadioStationOwner", meta.getInternetRadioStationOwner());
		mappings.put("InternetRadioStationWebpage", meta.getInternetRadioStationWebpage());
		mappings.put("ISRC", meta.getIsrc());
		mappings.put("Language", meta.getLanguage());
		mappings.put("MediaType", meta.getMediaType());
		mappings.put("MPEGLevel", String.valueOf(meta.getMPEGLevel()));
		mappings.put("Name", meta.getName());
		mappings.put("Picture", meta.getPicture());
		mappings.put("Publisher", meta.getPublisher());
		mappings.put("PublishersWebpage", meta.getPublishersWebpage());
		mappings.put("Title", meta.getTitle());

		return mappings;
	}

	protected IPod ipod;

	protected File scanDir;

	protected Date lastScanTime;

	protected Vector mp3Cache = new Vector();

	protected Vector videoCache = new Vector();

	protected Vector imageCache = new Vector();

	private Hashtable[] mediaFiles;

	private Backend backend;

	protected Object scanLock = new Object();

	private boolean scan = false;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public IPodInterface()
	{
		// this.ipod = new IPod();
		// this.backend = new Backend();
		// this.ipod = this.backend.getIPod();
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public void cacheToHTML(final String filename)
	{

		try
		{
			final String html = cacheToHTML();
			final FileWriter fos = new FileWriter(new File(filename));
			final StringReader reader = new StringReader(html);
			int rc = reader.read();
			while (rc != -1)
			{
				fos.write(rc);
				rc = reader.read();
			}
			fos.flush();
			fos.close();
			reader.close();

		}
		catch (final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getAllMedia()
	{
		if (scanDir == null) { return; }
		/*
		 * new Thread() { public void run() { synchronized (scanLock) {
		 */
		mp3Cache.clear();
		videoCache.clear();
		imageCache.clear();
		scanDirsRec(scanDir, new MediaFileSorter());
		lastScanTime = Calendar.getInstance().getTime();
		final int size = mp3Cache.size();
		System.out.println("Got mp3 cache " + mp3Cache.size());
		/*
		 * Hashtable[] newMediaFiles = new Hashtable[size]; for (int i = 0; i < size; i++) {
		 * 
		 * newMediaFiles[i] = MP3MetaToHashtable((MP3Meta) mp3Cache.get(i)); }
		 * setMediaFiles(mediaFiles);
		 */
		/*
		 * scanLock.notifyAll(); }
		 * 
		 * } }.start();
		 */
	}

	public String getIPodName()
	{
		return this.ipod.getName();
	}

	public Date getLastScanTime()
	{
		return this.lastScanTime;
	}

	public Hashtable[] getMediaFiles()
	{
		return this.mediaFiles;
	}

	public void loadIPodDatabase()
	{
		System.out.println("IPOD PATH: " + backend.getPreferences().getIPodPath());

		final ITunesDB db = ipod.getITunesDB();
		// ITunesDBParser dbParser = new ITunesDBParser();
		// ITunesDB db = dbParser.load();
		// backend.getIPod().setITunesDB(db);
		/*
		 * int recSize = db.getRecordSize(); for (int i = 0; i < recSize; i++) {
		 * System.out.println("rec=> " + db.getFilename(i)); }
		 */
	}

	/**
	 * Property Change Listeners
	 */

	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setScan(final boolean scan)
	{
		final boolean old = this.scan;
		this.scan = scan;
		if (scan)
		{
			getAllMedia();
			setScan(false);
		}
		propertyChangeListeners.firePropertyChange("scan", old, scan);
	}

	public void setScanDirectory(final File newDir)
	{
		if (newDir.isDirectory())
		{
			final File old = this.scanDir;
			this.scanDir = newDir;
			// backend.getPreferences().setIPodPath(dir);
			propertyChangeListeners.firePropertyChange("scanDir", old, scanDir);
		}
		else
		{
			System.out.println("IPodInterface: warning directory '" + newDir + "' non-existent");
		}

	}

	public void setScanDirectory(final String dir)
	{
		final File newDir = new File(dir);
		setScanDirectory(newDir);
	}

	public void sortMP3Cache(final Vector cache)
	{
		Collections.sort(cache, new Comparator()
		{

			@Override
			public int compare(final Object res1, final Object res2)
			{
				final MP3Meta meta1 = ((MP3Resource) res1).mp3Meta;
				final MP3Meta meta2 = ((MP3Resource) res2).mp3Meta;
				final String artist1 = meta1.getArtist();
				final String artist2 = meta2.getArtist();
				if (artist1 == null) { return -1; }
				if (artist2 == null) { return 1; }
				return artist1.compareToIgnoreCase(artist2);
			}
		});
	}

	String cacheToHTML()
	{

		// sortMP3Cache(mp3Cache);

		final StringBuffer html = new StringBuffer();
		html.append("<HTML>\n");

		html.append("<TITLE>Scan Media Files</TITLE>\n\n");
		html.append("<H1>Media Files</H1>\n");
		html.append("<B>Generated by :</B> IPodInterface by <A HREF=\"mailto:jch@cs.nott.ac.uk\">Jan Humble</A><BR>\n");
		html.append("<B>Scanned Directory:</B> " + scanDir + "<BR>\n");
		html.append("<B>Scan Date:</B> " + lastScanTime + "<BR>\n");
		html.append("<table style=\"width: 100%; text-align: left;\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n");
		html.append("<tbody>\n");
		html.append("<tr>\n");
		html.append("<td>\n");
		html.append("<H2>Music</H2>\n");
		html.append("<td>\n");
		html.append("<H2>Video</H2>\n");
		html.append("<td>\n");
		html.append("<H2>Images</H2>\n");
		html.append("<tr>\n");
		html.append("<td>\n");

		System.out.println("size = " + mp3Cache.size());
		if (mp3Cache.size() > 0)
		{
			html.append("<TABLE>\n");
			html.append("<TR>\n");
			html.append("<TD><B>Title</B><TD><B>Artist</B><TD><B>Album</B>\n</TR>\n");
			for (final Enumeration e = mp3Cache.elements(); e.hasMoreElements();)
			{
				html.append("<TR>\n");

				final MP3Resource resource = ((MP3Resource) e.nextElement());
				if (resource != null)
				{
					final MP3Meta meta = resource.mp3Meta;
					if (meta != null)
					{
						html.append("<TD>\n");
						html.append("<A HREF=" + resource.url + ">" + meta.getTitle() + "</A><BR>\n");
						html.append("<TD>\n");
						html.append(meta.getArtist());
						html.append("<TD>\n");
						html.append(meta.getAlbum());
						html.append("</TR>\n");
					}
				}
				// System.out.println("*** Writing => " + meta.getTitle());
			}

			html.append("</TABLE>\n");
		}
		html.append("</td>\n");
		html.append("<td>\n");
		// html.append("<H2>VIDEO</H2>\n");
		if (videoCache.size() > 0)
		{
			for (final Enumeration e = videoCache.elements(); e.hasMoreElements();)
			{
				final VideoResource resource = ((VideoResource) e.nextElement());
				html.append("<A HREF=" + resource.url + ">" + resource.title + "</A><BR>\n");
			}
		}
		html.append("</td>\n");
		html.append("<td>\n");
		// html.append("<H2>IMAGES</H2>\n");
		if (imageCache.size() > 0)
		{
			for (final Enumeration e = imageCache.elements(); e.hasMoreElements();)
			{
				final ImageResource resource = ((ImageResource) e.nextElement());
				html.append("<A HREF=" + resource.url + ">" + resource.title + "</A><BR>\n");
			}
		}
		html.append("</td>\n");
		html.append("</tr>\n");
		html.append("</tbody>\n");
		html.append("</HTML>");
		return html.toString();
		// html.append("<TD>\n");

	}

	protected void scanDirsRec(final File topDir, final MediaFileSorter mediaSorter)
	{
		if (topDir.isDirectory())
		{
			try
			{
				System.out.println("Scanning " + topDir.getCanonicalPath());
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final File[] files = topDir.listFiles();
			if (files != null)
			{
				for (final File file : files)
				{
					if (file.isDirectory())
					{
						scanDirsRec(file, mediaSorter);
					}
					else
					{
						mediaSorter.sort(file);
					}
				}
			}
		}
	}

	protected void setMediaFiles(final Hashtable[] mediaFiles)
	{
		final Hashtable[] old = this.mediaFiles;
		this.mediaFiles = mediaFiles;
		propertyChangeListeners.firePropertyChange("mediaFiles", old, mediaFiles);
	}

	private ImageResource createImageResource(final File imageFile)
	{
		String path = "";
		try
		{
			path = imageFile.getCanonicalPath().intern();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String resourceURL = "file:/" + path;
		return new ImageResource(resourceURL, imageFile.getName());
	}

	private VideoResource createVideoResource(final File videoFile)
	{
		String path = "";
		try
		{
			path = videoFile.getCanonicalPath().intern();
		}
		catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final String resourceURL = "file:/" + path;
		return new VideoResource(resourceURL, videoFile.getName());
	}

}

class ImageResource extends MediaResource
{

	final String title;

	ImageResource(final String url, final String title)
	{
		super(url);
		this.title = title;
	}

}

class MediaResource
{

	final String url;

	MediaResource(final String url)
	{
		this.url = url;
	}

}

class MP3Resource extends MediaResource
{

	final MP3Meta mp3Meta;

	MP3Resource(final String url, final MP3Meta mp3Meta)
	{
		super(url);
		this.mp3Meta = mp3Meta;
	}
}

class VideoResource extends MediaResource
{

	final String title;

	VideoResource(final String url, final String title)
	{
		super(url);
		this.title = title;
	}

}
