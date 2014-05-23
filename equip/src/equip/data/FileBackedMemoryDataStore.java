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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

*/
package equip.data;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import equip.config.ConfigManager;
import equip.runtime.SingletonManager;

/** Like {@link MemoryDataStore}, but attempts to persist dataspace
 * contents through use of checkpoint and event log files.
 * <p>
 * See notes for {@link DataDelegate} for configuring a dataspace
 * to make use of a custom data store such as this. Note that you should
 * probably also fix the responsible ID for the DataDelegate.<p>
 * </p><p>
 * Configuration:
 * <pre>
 * STOREID.path: DIRECTORYPATH [default "." - will use/create STOREID subdirectory]
 * STOREID.checkpointEventCount: EVENTS-BETWEEN-CHECKPOINTS [default 1000]
 * STOREID.maxFlushIntervalS: MAX-TIME-BETWEE-EVENTS-FLUSH [default 10, 0 to flush every event]
 * STOREID.writeProcessBound: T/F [default T]
 * STOREID.writePureEvents: T/F [default T]
 * STOREID.restoreProcessBound: T/F [default F - don't enable this unless you are really sure :-)]
 * </pre>
 * </p><p>
 * Files/formats. All files are in the STOREID subdirectory of the STOREID.path.
 * <ul>
 * <li>checkpoint-UNIXTIME.eqbser - a binary serialisation of the datastore checkpoint
 * from time UNIXTIME comprising a String ("equip-checkpoint"), an int32 file/serialisation version number 
 * (currently 2), 
 * an equip.data.GUID event log unique id for this checkpoint file [new for v2], 
 * an equip.data.GUID event log unique id for the event file which this immediately follows (else null or nullid) [new for v2], 
 * an equip.data.GUID responsble id, 
 * an equip.runtime.Time of the start time, an int32 item count, and that number of ItemBinding subclasses.</li>
 * <li>events-UNIXTIME.eqbser - a binary serialisation of events starting from UNIXTIME
 * comprising a String ("equip-events"), an int32 file/serialisation version number (currently 2),
 * an equip.data.GUID event log unique id for this event file [new for v2], 
 * an equip.data.GUID event log unique id for the checkpoint file which this follows (else null or nullid) [new for v2], 
 * an equip.data.GUID responsble id, 
 * an equip.runtime.Time of the start time, and an EOF-terminated sequence of equip.runtime.Time of the 
 * event, an int32 of the size in bytes of the previous record (if known, including time, size, etc, else 0), 
 * an int32 of the size in bytes of the current record (if known, including time, size, etc, else 0),
 * and the serialisation equip.data.Event subclass.
 * </ul></p> 
 */
public class FileBackedMemoryDataStore extends MemoryDataStore 
{
	private static boolean debug = true;
	private GUID responsible;
	private String storeId;
	private String lastCheckpointFileName, lastEventsFileName;
	private long lastCheckpointFileTimestamp, lastEventsFileTimestamp;
	private GUID lastCheckpointEventLogId, lastEventLogId;
	public static final String CHECKPOINT_PREFIX = "checkpoint-";
	public static final String CHECKPOINT_SUFFIX = ".eqbser";
	public static final String TEMP_SUFFIX = ".temp";
	public static final String EVENTS_PREFIX = "events-";
	public static final String EVENTS_SUFFIX = ".eqbser";
	public static final String CHECKPOINT_HEADER = "equip-checkpoint";
	public static final int CHECKPOINT_VERSION = 2;
	public static final int MIN_CHECKPOINT_VERSION = 1;
	public static final String EVENTS_HEADER = "equip-events";
	public static final int EVENTS_VERSION = 2;
	public static final int MIN_EVENTS_VERSION = 1;
	private equip.runtime.ObjectOutputStream eventsOuts;
	private int eventsWritten = 0;
	private static int DEFAULT_CHECKPOINT_EVENT_COUNT = 1000;
	private int checkpointEventCount = DEFAULT_CHECKPOINT_EVENT_COUNT;
	private equip.runtime.Time lastFlushTime = null;
	private int maxFlushIntervalS;
	private static final int MAX_FLUSH_INTERVAL_S = 10;
	private boolean terminatedFlag = false;
	private boolean writeProcessBound = true;
	private boolean writePureEvents = true;
	private boolean restoreProcessBound = false;

	/** The directory we keep our files in
	 */
	private File dir;

	public FileBackedMemoryDataStore(String storeId, GUID responsible) 
		throws DataStoreConfigurationException 
	{
		super();

		this.responsible = responsible;
		this.storeId = storeId;

		// config...
		ConfigManager config = (ConfigManager)SingletonManager.get(equip.config.ConfigManagerImpl.class.getName());
		String pathName = config.getStringValue(storeId+".path", ".");
		checkpointEventCount = config.getLongValue(storeId+".checkpointEventCount", DEFAULT_CHECKPOINT_EVENT_COUNT);
		maxFlushIntervalS = config.getLongValue(storeId+".maxFlushIntervalS", MAX_FLUSH_INTERVAL_S);
		writeProcessBound = config.getBooleanValue(storeId+".writeProcessBound", writeProcessBound);
		writePureEvents = config.getBooleanValue(storeId+".writePureEvents", writePureEvents);
		restoreProcessBound = config.getBooleanValue(storeId+".restoreProcessBound", restoreProcessBound);

		try 
		{
			File path = new File(pathName);
			if (!path.exists() || !path.isDirectory()) 
			{ 
				System.err.println("ERROR: FileBackedMemoryDataStore root directory does not exist: "+pathName);
				System.err.println("(Configure using "+storeId+".path");
				throw new DataStoreConfigurationException("root directory "+pathName+" does not exist");
			}
			dir = new File(path, storeId);
			if (!dir.exists()) 
			{
				System.err.println("NOTE: try to create FileBackedMemoryDataStore directory "+dir);
				dir.mkdir();
			}
			if (!dir.exists() || !dir.isDirectory()) 
			{
				System.err.println("ERROR: FileBackedMemoryDataStore store directory does not exist/could not be created: "+dir);
				System.err.println("(Configure using "+storeId+".path");
				throw new DataStoreConfigurationException("store directory "+dir+" does not exist");
			}
			// look for last checkpoint and event file
			File [] files = dir.listFiles();
			int i;
			lastCheckpointFileName = null;
			lastEventsFileName = null;
			lastCheckpointFileTimestamp = lastEventsFileTimestamp = 0;
			for (i=0; files!=null && i<files.length; i++) 
			{
				String name = files[i].getName();
				if (name.startsWith(CHECKPOINT_PREFIX) &&
					name.endsWith(CHECKPOINT_SUFFIX)) 
				{
					long timestamp = new Long(name.substring(CHECKPOINT_PREFIX.length(),
						name.length()-CHECKPOINT_SUFFIX.length())).longValue();
					if (timestamp > lastCheckpointFileTimestamp) 
					{
						lastCheckpointFileName = name;
						lastCheckpointFileTimestamp = timestamp;
					}
				}
				if (name.startsWith(EVENTS_PREFIX) &&
					name.endsWith(EVENTS_SUFFIX)) 
				{
					long timestamp = new Long(name.substring(EVENTS_PREFIX.length(),
						name.length()-EVENTS_SUFFIX.length())).longValue();
					if (timestamp > lastEventsFileTimestamp) 
					{
						lastEventsFileName = name;
						lastEventsFileTimestamp = timestamp;
					}
				}
			}
			boolean needCheckpoint = false;
			if (lastCheckpointFileName==null) 
			{
				needCheckpoint = true;
				System.err.println("NOTE: FileBackedMemoryDataStore "+storeId+" could not find any previous checkpoints in "+dir);
			}
			else
				readCheckpoint(lastCheckpointFileName);

			// events following that checkpoint?
			if (lastEventsFileTimestamp >= lastCheckpointFileTimestamp &&
				lastEventsFileName!=null) 
			{
				try 
				{
					if (readEvents(lastEventsFileName) > 0)
						needCheckpoint = true;
				} 
				catch (Exception e) 
				{
					System.err.println("WARNING: unable to read any event from event log "+lastEventsFileName+": "+e+" - running from checkpoint only");
				}
			}
			
			if (needCheckpoint)
				writeCheckpoint();

			startEventsFile();

			// flush thread
			if (maxFlushIntervalS>0) 
			{
				new Thread() 
				{
					public void run() 
					{
						try 
						{
							while(!terminatedFlag) 
							{
								checkFlush();
								sleep(maxFlushIntervalS*1000);
							} 
						}
						catch (InterruptedException e) {}
					}
				}.start();
			}
		} 
		catch (DataStoreConfigurationException de) 
		{
			throw de;
		}
		catch (Exception e) 
		{
			System.err.println("ERROR: constructing FileBackedMemoryDataStore "+storeId+": "+e);
			e.printStackTrace(System.err);
			throw new DataStoreConfigurationException("Internal exception: "+e);
		}
	}

	/** read a checkpoint file and populate memory data store structures
	 */
	private synchronized void readCheckpoint(String name) 
		throws DataStoreConfigurationException 
	{
		try 
		{
			File file = new File(dir, name);
			if (!file.exists() || !file.canRead()) 
				throw new DataStoreConfigurationException("Cannot read checkpoint: "+file);
			System.err.println("DataStore "+storeId+" read checkpoint "+file);
			// a binary serialisation...
			equip.runtime.ObjectInputStream ins = new equip.runtime.ObjectInputStream
				(new java.io.BufferedInputStream(new java.io.FileInputStream(file)));
			// comprising a String ("equip-checkpoint")
			String header = ins.readString();
			if (!header.equals(CHECKPOINT_HEADER))
				throw new DataStoreConfigurationException("invalid checkpoint header: "+header+" (expected "+CHECKPOINT_HEADER+")");
			// an int32 file/serialisation version number 
			int version = ins.readInt();
			if (version>CHECKPOINT_VERSION || version<MIN_CHECKPOINT_VERSION)
				throw new DataStoreConfigurationException("unsupported checkpoint version: "+version+" (expected "+CHECKPOINT_VERSION+")");
			if (version>=2) 
			{
				// an equip.data.GUID event log unique id for this checkpoint file [new for v2], 
				lastCheckpointEventLogId = (equip.data.GUID)ins.readObject();
				// an equip.data.GUID event log unique id for the event file which this immediately follows (else null or nullid) [new for v2], 
				lastEventLogId = (equip.data.GUID)ins.readObject();
			}
			// an equip.data.GUID responsble id
			equip.data.GUID resp = (equip.data.GUID)ins.readObject();
			if (!resp.equals(responsible)) 
			{
				System.err.println("WARNING: checkpoint "+file+" has different responsible ID: "+resp+" (we are "+responsible+")");
			}
			// an equip.runtime.Time of the start time
			equip.runtime.Time time = (equip.runtime.Time)ins.readObject();
			// an int32 item count
			int count = ins.readInt();
			int i;
			for (i=0; i<count; i++) 
			{
				try 
				{
					equip.data.ItemBinding binding = (equip.data.ItemBinding)ins.readObject();
					if (!restoreProcessBound &&
						binding!=null && 
						binding.info!=null &&
						binding.info.processBound)
						; // ignore
					else 
					{
						// fake add
						equip.data.AddEvent add = new equip.data.AddEventImpl();
						add.binding = binding;
						super.handleAdd(add);
					}	
				}
				catch (Exception e) 
				{
					System.err.println("ERROR reading checkpoint "+file+" item "+i+": "+e+": assuming truncated");
					break;
				}
			}
		}
		catch (DataStoreConfigurationException de) 
		{
			throw de;
		}
		catch (Exception e) 
		{
			throw new DataStoreConfigurationException("Error reading checkpoint "+name+": "+e);
		}
	}
	/** read an events file and process events.
	 * @return no of events read
	 */
	private synchronized int readEvents(String name) 
		throws DataStoreConfigurationException 
	{
		try 
		{
			File file = new File(dir, name);
			if (!file.exists() || !file.canRead()) 
				throw new DataStoreConfigurationException("Cannot read events: "+file);
			System.err.println("DataStore "+storeId+" read events "+file);
			// a binary serialisation...
			equip.runtime.ObjectInputStream ins = new equip.runtime.ObjectInputStream
				(new java.io.BufferedInputStream(new java.io.FileInputStream(file)));
			// comprising a String ("equip-events")
			String header = ins.readString();
			if (!header.equals(EVENTS_HEADER))
				throw new DataStoreConfigurationException("invalid events header: "+header+" (expected "+EVENTS_HEADER+")");
			// an int32 file/serialisation version number 
			int version = ins.readInt();
			if (version>EVENTS_VERSION || version<MIN_EVENTS_VERSION)
				throw new DataStoreConfigurationException("unsupported events version: "+version+" (expected "+EVENTS_VERSION+")");
			if (version>=2) 
			{
				// an equip.data.GUID event log unique id for this file [new for v2],
				lastEventLogId = (equip.data.GUID)ins.readObject();
				// an equip.data.GUID event log unique id for the checkpoint file which this follows (else null or nullid) [new for v2], 
				lastCheckpointEventLogId = (equip.data.GUID)ins.readObject();
			}
			// an equip.data.GUID responsble id
			equip.data.GUID resp = (equip.data.GUID)ins.readObject();
			if (!resp.equals(responsible)) 
			{
				System.err.println("WARNING: events "+file+" has different responsible ID: "+resp+" (we are "+responsible+")");
			}
			// an equip.runtime.Time of the start time
			equip.runtime.Time time = (equip.runtime.Time)ins.readObject();
			
			// and an EOF-terminated sequence 
			int count = 0;
			while (true) 
			{
				try 
				{
					// of equip.runtime.Time of the event, 
					equip.runtime.Time tev = (equip.runtime.Time)ins.readObject();
					// an int32 of the size in bytes of the previous record (if known, including time, size, etc, else 0), 
					int prevSize = ins.readInt();
					// an int32 of the size in bytes of the current record (if known, including time, size, etc, else 0),
					int thisSize = ins.readInt();
					// and the serialisation equip.data.Event subclass.
					equip.data.Event event = (equip.data.Event)ins.readObject();

					if (event instanceof equip.data.AddEvent) 
					{
						equip.data.AddEvent add = (equip.data.AddEvent)event;
						if (!restoreProcessBound &&
							add.binding!=null && 
							add.binding.info!=null &&
							add.binding.info.processBound)
							; // ignore
						else
							super.handleAdd(add);
					}
					else if (event instanceof equip.data.UpdateEvent) 
					{
						UpdateEvent upd =(UpdateEvent)event;
						// only if process bound
						if (!restoreProcessBound && 
							upd.item!=null && upd.item.id!=null &&
							itemIsProcessBound(upd.item.id))
							; // ignore(equip.data.UpdateEvent
						else 
							super.handleUpdate(upd);
					}
					else if (event instanceof equip.data.DeleteEvent) 
					{
						DeleteEvent del = (DeleteEvent)event;
						if (!restoreProcessBound &&
							del.id!=null && itemIsProcessBound(del.id))
							; // ignore
						else
							super.handleDelete((equip.data.DeleteEvent)event);
					}
					else 
						System.err.println("WARNING: data store "+storeId+" ignoring event type "+
							event.getClass().getName()+" in events file "+file);
					count++;
				} 
				catch (Exception e) 
				{
					System.err.println("assumed end of events "+file+" after "+count+" events ("+e+")");
					break;
				}
			}
			return count;
		}
		catch (DataStoreConfigurationException de) 
		{
			throw de;
		}
		catch (Exception e) 
		{
			throw new DataStoreConfigurationException("Error reading events "+name+": "+e);
		}
		//notreachable return 0;
	}
	/** check if item in DB is process bound
	 */
	private boolean itemIsProcessBound(GUID id) 
	{
		ItemBinding binding = getItemBinding(id);
		if(binding!=null &&
			binding.info!=null)
			return binding.info.processBound;
		return false; // default no
	}
	/** write checkpoint
	 */
	private synchronized void writeCheckpoint() 
	{
		// start a new checkpoint file
		equip.runtime.Time now = new equip.runtime.TimeImpl();
		now.getCurrentTime();
		long timestamp = now.sec;
		if (timestamp <= lastCheckpointFileTimestamp) 
		{
			System.err.println("WARNING: existing checkpoint file time later than current time: "+lastCheckpointFileName+" vs "+now.sec);
			timestamp = lastCheckpointFileTimestamp+1;
		}
		if (timestamp <= lastEventsFileTimestamp) 
		{
			System.err.println("WARNING: existing events file time later than current time: "+lastEventsFileName+" vs "+now.sec);
			timestamp = lastEventsFileTimestamp+1;
		}
		lastCheckpointFileTimestamp = timestamp;
		String name = CHECKPOINT_PREFIX+timestamp+CHECKPOINT_SUFFIX;
		try 
		{
			File realFile = new File(dir, name);
			// temp
			File file = new File(dir, name+TEMP_SUFFIX);
			// a binary serialisation...
			equip.runtime.ObjectOutputStream outs = new equip.runtime.ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)));
			System.err.println("Writing checkpoint file "+file);
			// comprising a String ("equip-checkpoint")
			outs.writeString(CHECKPOINT_HEADER);
			// an int32 file/serialisation version number 
			outs.writeInt(CHECKPOINT_VERSION);
			// an equip.data.GUID event log unique id for this checkpoint file [new for v2], 
			equip.data.GUIDFactory factory = (equip.data.GUIDFactory)SingletonManager.get(equip.data.GUIDFactoryImpl.class.getName());
			GUID newId = factory.getUnique();
			outs.writeObject(newId);
			// an equip.data.GUID event log unique id for the event file which this immediately follows (else null or nullid) [new for v2], 
			outs.writeObject(lastEventLogId);
			// an equip.data.GUID responsble id
			outs.writeObject(responsible);
			// an equip.runtime.Time of the start time
			outs.writeObject(now);
			// an int32 item count
			java.util.Set keys = idMap.keySet();

			// this is it now
			lastCheckpointEventLogId = newId;

			int count = 0;
			java.util.Iterator ikeys = keys.iterator();
			while(ikeys.hasNext()) 
			{
				equip.data.GUID id =(equip.data.GUID)ikeys.next();
				if (!writeProcessBound && itemIsProcessBound(id))
					continue;
				count++;
			}

			//int count = keys.size();
			outs.writeInt(count);
			ikeys = keys.iterator();
			int i=0;
			while (ikeys.hasNext()) 
			{
				equip.data.GUID id =(equip.data.GUID)ikeys.next();
				if (!writeProcessBound && itemIsProcessBound(id))
					continue;
				i++;
				try 
				{
					equip.data.ItemBinding binding = (equip.data.ItemBinding)idMap.get(id);
					outs.writeObject(binding);
				} 
				catch (Exception e) 
				{
					System.err.println("ERROR writing checkpoint "+file+" item "+i+": "+e);
					e.printStackTrace(System.err);
				}
			}
			outs.flush();
			outs.close();
			if (file.renameTo(realFile))
				// renamed
				System.out.println("Renamed checkpoint to "+realFile);
			else
				System.err.println("WARNING: Unable to rename checkpoint file "+file+" to "+realFile);
		} 
		catch (Exception e) 
		{
			System.err.println("ERROR writing checkpoint "+name+": "+e);
			e.printStackTrace(System.err);
		}
		// err
	}
	/** start events file
	 */
	private synchronized void startEventsFile() 
	{
		// start a new checkpoint file
		equip.runtime.Time now = new equip.runtime.TimeImpl();
		now.getCurrentTime();
		long timestamp = now.sec;
		if (timestamp < lastCheckpointFileTimestamp) 
		{
			System.err.println("WARNING: existing checkpoint file time later than current time: "+lastCheckpointFileName+" vs "+now.sec);
			timestamp = lastCheckpointFileTimestamp+1;
		}
		if (timestamp <= lastEventsFileTimestamp) 
		{
			System.err.println("WARNING: existing events file time later than current time: "+lastEventsFileName+" vs "+now.sec);
			timestamp = lastEventsFileTimestamp+1;
		}
		lastEventsFileTimestamp = timestamp;
		String name = EVENTS_PREFIX+timestamp+EVENTS_SUFFIX;
		try 
		{
			File file = new File(dir, name);
			// a binary serialisation...
			eventsOuts = new equip.runtime.ObjectOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)));
			System.err.println("Starting events file "+file);
			// comprising a String ("equip-events")
			eventsOuts.writeString(EVENTS_HEADER);
			// an int32 file/serialisation version number 
			eventsOuts.writeInt(EVENTS_VERSION);
			// an equip.data.GUID event log unique id for this event file [new for v2],
			equip.data.GUIDFactory factory = (equip.data.GUIDFactory)SingletonManager.get(equip.data.GUIDFactoryImpl.class.getName());
			GUID newId = factory.getUnique();
			eventsOuts.writeObject(newId);
			// an equip.data.GUID event log unique id for the checkpoint file which this follows (else null or nullid) [new for v2], 
			eventsOuts.writeObject(lastCheckpointEventLogId);
			// an equip.data.GUID responsble id
			eventsOuts.writeObject(responsible);
			// an equip.runtime.Time of the start time
			eventsOuts.writeObject(now);
			eventsOuts.flush();
			lastFlushTime = now;
			eventsWritten = 0;

			// this is now us!
			lastEventLogId = newId;
		} 
		catch (Exception e) 
		{
			System.err.println("ERROR writing events "+name+": "+e);
			e.printStackTrace(System.err);
			eventsOuts = null;
		}
		// err
	}
	/** check/flush events file
	 */
	private synchronized void checkFlush()
	{
		if (eventsOuts==null) 
			return;
		flush();
	}

	/** write event record to file
	 */
	private synchronized void writeEvent(equip.data.Event event) 
	{
		if (eventsOuts==null) 
			return;
		// of equip.runtime.Time of the event, 
		equip.runtime.Time now = new equip.runtime.TimeImpl();
		now.getCurrentTime();
		try 
		{
			eventsOuts.writeObject(now);
			// an int32 of the size in bytes of the previous record (if known, including time, size, etc, else 0), 
			eventsOuts.writeInt(0); // not known
			// an int32 of the size in bytes of the current record (if known, including time, size, etc, else 0),
			eventsOuts.writeInt(0); // not known
			// and the serialisation equip.data.Event subclass.
			eventsOuts.writeObject(event);

			if (maxFlushIntervalS==0)
				flush();
		}
		catch (Exception e) 
		{
			System.err.println("ERROR writing event "+event+": "+e);
			e.printStackTrace(System.err);
			eventsOuts = null;
		}
	
		eventsWritten ++;
		if (eventsWritten >= checkpointEventCount) 
		{
			closeEventsFile();
			writeCheckpoint();
			startEventsFile();
		}
	}
	/** request that store handle the Add event, adding to itself
	 * accordingly.
	 *
	 * Note: also has to handle lease updates.
	 *
	 * @param add The {@link AddEvent} to be handled.
	 * @return true iff this store has handled the add event; false if declined.
	 *         (normally as indicated by a call to {@link IDataStore.checkAdd}
	 */
	public boolean handleAdd(AddEvent add)
	{
		writeEvent(add);
		return super.handleAdd(add);
	}

	/** request that store handles the given update, which it must 
	 * iff it is currently maintaining state for it.
	 *
	 * Note: implies currently no migration of item state between
	 * store(s).
	 *
	 * @param upd the {@link UpdateEvent} to be handled.
	 * @return true iff the update has been handled (and necessarily
	 *         the item's state is maintained by this store).
	 */
	public boolean handleUpdate(UpdateEvent upd)
	{
		writeEvent(upd);
		return super.handleUpdate(upd);
	}

	/** request that store handles the given delete, which it must 
	 * iff it is currently maintaining state for the corresponding 
	 * data item.
	 *
	 * @param upd the {@link UpdateEvent} to be handled.
	 * @return true iff the delete has been handled (and necessarily
	 *         the item's state was maintained by this store).
	 */
	public boolean handleDelete(DeleteEvent del) 
	{
		writeEvent(del);
		return super.handleDelete(del);
	}

	/** terminate - tidy up.
	 */
	public synchronized void terminate() 
	{
		flush();
		closeEventsFile();
		terminatedFlag = true;
	}
	private synchronized void closeEventsFile() {
		if (eventsOuts!=null) 
		{
			try 
			{
				eventsOuts.flush();
				eventsOuts.close();
			} 
			catch (java.io.IOException e) 
			{
				System.err.println("ERROR closing event file: "+e);
			}
			eventsOuts = null;
		}
	}

	/** Flush any pending persistent records
	 */
	public synchronized void flush() 
	{
		if (eventsOuts!=null) 
		{
			try 
			{
				eventsOuts.flush();
			}
			catch (java.io.IOException e) 
			{
				System.err.println("ERROR flushing event file: "+e);
			}
			equip.runtime.Time now = new equip.runtime.TimeImpl();
			now.getCurrentTime();
			lastFlushTime = now;
		}
	}
}