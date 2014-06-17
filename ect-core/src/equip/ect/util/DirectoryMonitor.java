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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

 */
package equip.ect.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DirectoryMonitor implements Runnable
{
	private static final int frequency = 1000;

	protected class FileAddedEventDispatcher extends Thread
	{

		protected int frequency = 10;
		private File file = null;

		protected FileAddedEventDispatcher(final File file)
		{
			this.file = file;
		}

		@Override
		public void run()
		{
			if (file != null)
			{
				if (!file.isDirectory())
				{
					while (!existsAndReadable(file))
					{
						if (!file.exists() || !file.canRead())
						{
							// check if file no longer exists (e.g. if a file copy
							// has been cancelled) or has been locked by another
							// process...and bail out.
							return;
						}
						try
						{
							Thread.sleep(this.frequency);
						}
						catch (final InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
				fireFileAddComplete(file);
			}
		}
	}

	protected class MapEntry
	{
		protected File file = null;
		protected long lastModified = -1;

		MapEntry(final File file, final long lastModified)
		{
			this.file = file;
			this.lastModified = lastModified;
		}
	}

	private final Object stopLock = new Object();
	private final Object listenersLock = new Object();
	private final Object mapsLock = new Object();
	private boolean stop = false;
	private File directory = null;
	private Collection<DirectoryEventListener> listeners = new HashSet<DirectoryEventListener>();
	private Map<String, MapEntry> currentMap = new HashMap<String, MapEntry>();
	private Map<String, MapEntry> previousMap = new HashMap<String, MapEntry>();
	private boolean isRecursive = false;
	private boolean includeExisting = false;

	public DirectoryMonitor(final File directory, final boolean includeExisting, final boolean isRecursive)
			throws IOException
	{

		if (directory == null)
		{
			throw new IllegalArgumentException("null directory");
		}
		if (!directory.isDirectory())
		{
			throw new IOException("Path not valid: " + directory.getAbsolutePath());
		}
		this.directory = directory;
		this.includeExisting = includeExisting;
		this.isRecursive = isRecursive;
	}

	/**
	 * Check if the specified file exists and is readable.
	 * <p/>
	 * Note: the methods File.exists(), File.canRead() & File.canWrite() return true even when the
	 * file has not been completley written to the backing store or is locked by another process so
	 * instead we try and open the file and see if an error is thrown.
	 */
	public static boolean existsAndReadable(final File file)
	{
		try
		{
			final FileInputStream input = new FileInputStream(file);
			input.close();
		}
		catch (final FileNotFoundException fnfe)
		{
			return false;
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
		}
		return true;
	}

	public void addDirectoryEventListener(final DirectoryEventListener listener)
	{
		synchronized (listenersLock)
		{
			if (listener != null && !listeners.contains(listener))
			{
				listeners.add(listener);
			}
		}
	}

	public boolean isStopped()
	{
		synchronized (stopLock)
		{
			return stop;
		}
	}

	public void removeDirectoryEventListener(final DirectoryEventListener listener)
	{
		synchronized (listenersLock)
		{
			listeners.remove(listener);
		}
	}

	@Override
	public void run()
	{
		if (!includeExisting)
		{
			if (isRecursive)
			{
				createDirectoryMapRecursive(directory);
			}
			else
			{
				createDirectoryMap();
			}
		}
		while (!stop)
		{
			synchronized (mapsLock)
			{
				previousMap = currentMap;
			}
			try
			{
				Thread.sleep(frequency);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			if (isRecursive)
			{
				synchronized (mapsLock)
				{
					currentMap = new HashMap<String, MapEntry>();
					createDirectoryMapRecursive(directory);
				}
			}
			else
			{
				createDirectoryMap();
			}
			compareDirectoryMaps();
		}
	}

	public void stop()
	{
		synchronized (stopLock)
		{
			stop = true;
		}
	}

	protected void fireFileAdd(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileAdd(file);
			}
		}
	}

	protected void fireFileAddComplete(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileAddComplete(file);
			}
		}
	}

	protected void fireFileDeleted(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileDeleted(file);
			}
		}
	}

	protected void fireFileModified(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileModified(file);
			}
		}
	}

	private void compareDirectoryMaps()
	{
		synchronized (mapsLock)
		{
			// returns non-null
			final Set<String> currentKeys = currentMap.keySet();
			final String currentFilenames[] = currentKeys.toArray(new String[0]);

			MapEntry previousEntry = null;
			MapEntry currentEntry = null;

			for (final String currentFilename : currentFilenames)
			{
				previousEntry = previousMap.get(currentFilename);
				currentEntry = currentMap.get(currentFilename);
				if (currentEntry != null)
				{
					if (previousEntry == null)
					{
						fireFileAdd(currentEntry.file);
						new FileAddedEventDispatcher(currentEntry.file).start();
					}
					else
					{
						if (currentEntry.lastModified != previousEntry.lastModified)
						{
							fireFileModified(currentEntry.file);
						}
						previousMap.remove(currentFilename);
					}
				}
			}

			// returns non-null
			final MapEntry deletedEntries[] = previousMap.values().toArray(new MapEntry[0]);
			for (final MapEntry deletedEntry : deletedEntries)
			{
				fireFileDeleted(deletedEntry.file);
			}
		}
	}

	private void createDirectoryMap()
	{
		synchronized (mapsLock)
		{
			currentMap = new HashMap<String, MapEntry>();
			final File files[] = directory.listFiles();
			if (files != null)
			{
				for (final File file : files)
				{
					currentMap.put(file.getName(), new MapEntry(file, file.lastModified()));
				}
			}
		}
	}

	private void createDirectoryMapRecursive(final File directory)
	{
		if (directory != null && directory.isDirectory())
		{
			final File files[] = directory.listFiles();
			if (files != null)
			{
				for (final File file : files)
				{
					createDirectoryMapRecursive(file);
					currentMap.put(file.getPath(), new MapEntry(file, file.lastModified()));
				}
			}
		}
	}
}
