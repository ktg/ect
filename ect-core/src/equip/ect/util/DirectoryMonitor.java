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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DirectoryMonitor implements Runnable
{
	private static final int frequency = 1000;

	protected class MapEntry
	{
		protected File file = null;
		long lastModified = -1;

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
	private Collection<DirectoryEventListener> listeners = new HashSet<>();
	private Map<String, MapEntry> currentMap = new HashMap<>();
	private Map<String, MapEntry> previousMap = new HashMap<>();
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
					currentMap = new HashMap<>();
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

	private void fireFileAdd(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileAdd(file);
			}
		}
	}

	private void fireFileAddComplete(final List<File> files)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.filesAdded(files);
			}
		}
	}

	private void fireFileDeleted(final File file)
	{
		synchronized (listenersLock)
		{
			for (final DirectoryEventListener listener : listeners)
			{
				listener.fileDeleted(file);
			}
		}
	}

	private void fireFileModified(final File file)
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
			final Set<String> currentKeys = new HashSet<>(currentMap.keySet());
			final List<File> added = new ArrayList<>();

			for (final String currentFilename : currentKeys)
			{
				final MapEntry previousEntry = previousMap.get(currentFilename);
				final MapEntry currentEntry = currentMap.get(currentFilename);
				if (currentEntry != null)
				{
					if (previousEntry == null)
					{
						fireFileAdd(currentEntry.file);
						added.add(currentEntry.file);
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
			final Set<MapEntry> deleted = new HashSet<>(previousMap.values());
			for (final MapEntry deletedEntry : deleted)
			{
				fireFileDeleted(deletedEntry.file);
			}

			if(added.size() != 0)
			{
				fireFileAddComplete(added);
			}
		}
	}

	private void createDirectoryMap()
	{
		synchronized (mapsLock)
		{
			currentMap = new HashMap<>();
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
