/*
<COPYRIGHT>

Copyright (c) 2006, University of Nottingham
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
package equip.ect.logs;

import java.io.FileInputStream;
import java.io.IOException;

import equip.data.BooleanBox;
import equip.data.Dictionary;
import equip.data.FloatBox;
import equip.data.IntBox;
import equip.data.ItemData;
import equip.data.StringBox;
import equip.data.Tuple;

/**
 * summary of equip dataspace log
 */
public class EquipLogSummary
{
	/**
	 * nanos per second
	 */
	public static final long NANOS_PER_SECOND = 1000000000;
	/**
	 * nanos per usecond
	 */
	public static final long NANOS_PER_USEC = 1000;

	/**
	 * main: usage: filename ...
	 */
	public static void main(final String[] args)
	{
		if (args.length == 0)
		{
			System.out.println("Usage: filename ...");
			System.exit(-1);
		}
		for (int i = 0; i < args.length; i++)
		{
			try
			{
				final String filename = args[i];
				System.out.println("Reading file " + (i + 1) + ":" + filename);

				final equip.runtime.ObjectInputStream ins = new equip.runtime.ObjectInputStream(new FileInputStream(
						filename));
				// read header
				// comprising a String ("equip-checkpoint")
				final String header = ins.readString();
				if (header.equals(equip.data.FileBackedMemoryDataStore.CHECKPOINT_HEADER))
				{
					readCheckpointFile(ins);
				}
				else if (header.equals(equip.data.FileBackedMemoryDataStore.EVENTS_HEADER))
				{
					readEventsFile(ins);
				}
				else
				{
					System.out.println("Unknown file type: '" + header + "'");
				}
			}
			catch (final Exception e)
			{
				System.out.println("Error processing file " + args[i] + ": " + e);
				e.printStackTrace();
			}
		}
	}

	static String summariseField(final Object field)
	{
		if (field instanceof StringBox) { return ((StringBox) field).value; }
		if (field instanceof BooleanBox) { return new Boolean(((BooleanBox) field).value).toString(); }
		if (field instanceof IntBox) { return new Integer(((IntBox) field).value).toString(); }
		if (field instanceof FloatBox) { return new Float(((FloatBox) field).value).toString(); }
		if (field instanceof Dictionary)
		{
			final StringBuffer buf = new StringBuffer();
			buf.append("{");
			final Dictionary dict = (Dictionary) field;
			for (int i = 0; dict.entries != null && i < dict.entries.length; i++)
			{
				if (i > 0)
				{
					buf.append(", ");
				}
				buf.append(dict.entries[i].name + "=");
				buf.append(summariseField(dict.entries[i].value));
			}
			buf.append("}");
			return buf.toString();
		}
		if (field == null) { return "null"; }
		return field.toString();
	}

	static void summariseItem(final String context, final ItemData item)
	{
		final StringBuffer buf = new StringBuffer();
		buf.append("id=" + item.id + ", name=" + item.name);
		// ect...
		if (item instanceof Tuple)
		{
			final Tuple tuple = (Tuple) item;
			for (int i = 0; tuple.fields != null && i < tuple.fields.length; i++)
			{
				buf.append(", " + summariseField(tuple.fields[i]));
			}
		}
		else
		{
			buf.append(item.toString());
		}
		System.out.println(context + " " + buf.toString());
	}

	protected static void readCheckpointFile(final equip.runtime.ObjectInputStream ins) throws IOException,
			ClassNotFoundException, InstantiationException
	{
		// an int32 file/serialisation version number
		final int version = ins.readInt();
		if (version > equip.data.FileBackedMemoryDataStore.CHECKPOINT_VERSION || version < 1
		/*
		 * equip.data.FileBackedMemoryDataStore.MIN_CHECKPOINT_VERSION
		 */
		) { throw new RuntimeException("unsupported checkpoint version: " + version + " (expected "
				+ equip.data.FileBackedMemoryDataStore.CHECKPOINT_VERSION + ")"); }
		if (version >= 2)
		{
			// an equip.data.GUID event log unique id for this checkpoint file [new for v2],
			final equip.data.GUID lastCheckpointEventLogId = (equip.data.GUID) ins.readObject();
			// an equip.data.GUID event log unique id for the event file which this immediately
			// follows (else null or nullid) [new for v2],
			final equip.data.GUID lastEventLogId = (equip.data.GUID) ins.readObject();
		}
		// an equip.data.GUID responsble id
		final equip.data.GUID resp = (equip.data.GUID) ins.readObject();
		// an equip.runtime.Time of the start time
		final equip.runtime.Time time = (equip.runtime.Time) ins.readObject();
		final long timestamp = time.sec * NANOS_PER_SECOND + time.usec * NANOS_PER_USEC;

		// an int32 item count
		final int count = ins.readInt();
		System.out.println("No. objects: " + count);
		int i;
		for (i = 0; i < count; i++)
		{
			final equip.data.ItemBinding binding = (equip.data.ItemBinding) ins.readObject();

		}
		// ....
	}

	/**
	 * read/index an event file
	 * 
	 * @param file
	 *            Description of Parameter
	 */
	protected static void readEventsFile(final equip.runtime.ObjectInputStream ins) throws IOException,
			ClassNotFoundException, InstantiationException
	{
		// an int32 file/serialisation version number
		final int version = ins.readInt();
		if (version > equip.data.FileBackedMemoryDataStore.EVENTS_VERSION || version < 1
		/*
		 * equip.data.FileBackedMemoryDataStore.MIN_EVENTS_VERSION
		 */
		) { throw new RuntimeException("unsupported event log version: " + version + " (expected "
				+ equip.data.FileBackedMemoryDataStore.EVENTS_VERSION + ")"); }
		if (version >= 2)
		{
			// an equip.data.GUID event log unique id for this event file [new for v2],
			final equip.data.GUID lastEventLogId = (equip.data.GUID) ins.readObject();
			// an equip.data.GUID event log unique id for the checkpoint file which this immediately
			// follows (else null or nullid) [new for v2],
			final equip.data.GUID lastCheckpointEventLogId = (equip.data.GUID) ins.readObject();
		}
		// an equip.data.GUID responsble id
		final equip.data.GUID resp = (equip.data.GUID) ins.readObject();
		// an equip.runtime.Time of the start time
		final equip.runtime.Time time = (equip.runtime.Time) ins.readObject();
		final long timestamp = time.sec * NANOS_PER_SECOND + time.usec * NANOS_PER_USEC;

		int count = 0;
		while (true)
		{
			try
			{
				// of equip.runtime.Time of the event,
				final equip.runtime.Time tev = (equip.runtime.Time) ins.readObject();
				final long ts = tev.sec * NANOS_PER_SECOND + tev.usec * NANOS_PER_USEC;
				// an int32 of the size in bytes of the previous record (if known, including time,
				// size, etc, else 0),
				final int prevSize = ins.readInt();
				// an int32 of the size in bytes of the current record (if known, including time,
				// size, etc, else 0),
				final int thisSize = ins.readInt();
				// and the serialisation equip.data.Event subclass.

				final equip.data.Event event = (equip.data.Event) ins.readObject();

				// System.out.println("Read event " + count + ": "+event);

				equip.data.GUID id = null;
				equip.data.ItemData item = null;
				long endts = 0;
				boolean ignore = false;
				if (event instanceof equip.data.AddEvent)
				{
					final equip.data.AddEvent add = (equip.data.AddEvent) event;
					if (add.binding != null)
					{
						if (add.binding.item != null)
						{
							id = add.binding.item.id;
						}
						item = add.binding.item;

						summariseItem("add", item);
						// System.out.println("Add "+item);
					}
				}
				else if (event instanceof equip.data.UpdateEvent)
				{
					final equip.data.UpdateEvent upd = (equip.data.UpdateEvent) event;
					if (upd.item != null)
					{
						id = upd.item.id;
					}
					item = upd.item;

					summariseItem("update", item);
					// System.out.println("Update "+item);
				}
				else if (event instanceof equip.data.DeleteEvent)
				{
					final equip.data.DeleteEvent del = (equip.data.DeleteEvent) event;
					id = del.id;
					ignore = true;
					// just serves to end valid time of last update/add
					System.out.println("Delete " + id);
				}
				else
				{
					// assumed ephemeral
					endts = ts;
				}

				count++;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR reading event log item " + count + ": " + e + ": assuming truncated");
				break;
			}
		}
		System.out.println("Read " + count + " events");
	}
}
