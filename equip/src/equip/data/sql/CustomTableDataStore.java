/*
<COPYRIGHT>

Copyright (c) 2003-2005, University of Nottingham
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
package equip.data.sql;

import equip.config.ConfigManager;
import equip.data.*;
import equip.runtime.SingletonManager;
import equip.runtime.ValueBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Implementation of {@link IDataStore} interface which
 * holds data for one specific (run-time configured) tuple type in a
 * single JDBC table. <p>
 * <p/>
 * See notes for {@link DataDelegate} for configuring a dataspace
 * to make use of a custom data store such as this. <p>
 * <p/>
 * This version is intended to make it relatively easy to expose
 * an existing table of data through Equip. To do this it requires
 * an auxiliary table to contain data-table to Equip mapping information
 * and additional Equip-specific information. <p>
 * <p/>
 * <p>It is configured as follows:</p>
 * <pre>
 * &lt;storeId&gt;.url: &lt;JDBC-URL&gt; [e.g. "jdbc:mysql://serverURL/dbName"]
 * &lt;storeId&gt;.user: &lt;JDBC-username&gt; [default: root]
 * &lt;storeId&gt;.password: &lt;JDBC-password&gt; [Optional]
 * &lt;storeId&gt;.driver: &lt;JDBC-driver-classname&gt; [e.g. "com.mysql.jdbc.Driver"]
 * &lt;storeId&gt;.readonly: &lt;readonly&gt; [default: true]
 * &lt;storeId&gt;.dataTableName: &lt;data-table-name&gt;
 * &lt;storeId&gt;.dataKeyType: &lt;type&gt; [default: int, options: string, float, double, char]
 * &lt;storeId&gt;.dataKeyColumn: &lt;data-table-key-column-name&gt;
 * &lt;storeId&gt;.dataKeyMapClass: &lt;class-to-map-data-key-to-guid&gt; [default: equip.data.sql.DataKeyMapGetUnique]
 * &lt;storeId&gt;.dataKeyAllocateClass: &lt;class-to-get-a-new-data-key&gt; [default: equip.data.sql.DataKeyAllocateMax]
 * &lt;storeId&gt;.dataNameColumn: &lt;data-table-item-data-name-column-name&gt; [optional; defaults to using helper table, column "NAME"]
 * &lt;storeId&gt;.helperTableName: &lt;equip-specific-helper-table-name&gt; [see below for schema]
 * &lt;storeId&gt;.tupleTypeName: &lt;tuple-type-name&gt; [used in field 0 of tuple, StringBox]
 * &lt;storeId&gt;.tupleSize: &lt;N&gt;
 * &lt;storeId&gt;.field&lt;n&gt;Type: &lt;type&gt; [default: int, options: string, float, double, char; n=1..N]
 * &lt;storeId&gt;.field&lt;n&gt;Column: &lt;data-table-colum-name-for-field-n&gt; [required, n=1..N]
 * </pre>
 * <p>The helper table schema is typically something like that produced by:
 * <pre>
 * CREATE DATABASE FOODB;
 * USE FOODB;
 *
 * CREATE TABLE EQUIPFOO (
 * GUID VARCHAR(100) NOT NULL,
 * NAME VARCHAR(255),
 * DATAKEY INTEGER NOT NULL,
 * PRIMARY KEY (GUID)
 * );
 * </pre>
 * where DATAKEY is the foreign key (same type) as the primary key of the data
 * table. </p>
 * <p>For example, consider the following simple data table:
 * <pre>CREATE TABLE FOO (
 * SEQUENCE INTEGER NOT NULL,
 * INTVALUE INTEGER NOT NULL,
 * PRIMARY KEY (SEQUENCE)
 * );
 * INSERT INTO FOO VALUES(1, 10);
 * INSERT INTO FOO VALUES(2, 20);
 * INSERT INTO FOO VALUES(3, 30);
 * <p/>
 * GRANT ALL ON FOODB TO 'equip'@127.0.0.1 IDENTIFIED BY 'password';
 * FLUSH PRIVILEGES;
 * </pre>
 * in a mysql database called "<code>FOODB</code>" on the local host, with account
 * name "<code>equip</code>" and password "<code>password</code>"...</p>
 * <p>This could be exposed as a tuple with 3 fields:
 * <ul><li>field[0], tuple type name, "equip.data.sql.CustomTable.FOO"</li>
 * <li>field[1], sequence number (SEQUENCE), in an IntBox</li>
 * <li>field[2], value (INTVALUE), in an IntBox</li>
 * </ul>using the following configuration (assuming that the <code>storeId</code>
 * is "<code>FooTable</code>":
 * <pre>
 * FooTable.url: jdbc:mysql://localhost:3306/FOODB
 * FooTable.user: equip
 * FooTable.password: password
 * FooTable.driver: com.mysql.jdbc.Driver
 * FooTable.readonly: false
 * FooTable.dataTableName: FOO
 * FooTable.dataKeyType: int
 * FooTable.dataKeyColumn: SEQUENCE
 * FooTable.dataKeyMapClass: equip.data.sql.DataKeyMapGetUnique
 * FooTable.dataKeyAllocateClass: equip.data.sql.DataKeyAllocateMax
 * #FooTable.dataNameColumn:
 * FooTable.helperTableName: EQUIPFOO
 * FooTable.tupleTypeName: equip.data.sql.CustomTable.FOO
 * FooTable.tupleSize: 2
 * FooTable.field1Type: int
 * FooTable.field1Column: SEQUENCE
 * FooTable.field2Type: int
 * FooTable.field2Column: INTVALUE
 * </pre>
 *
 * @author Chris Greenhalgh, 2003-10-27
 */
public class CustomTableDataStore implements IDataStore
{
	static final String TYPE_INT = "int";
	static final String TYPE_STRING = "string";
	static final String TYPE_FLOAT = "float";
	static final String TYPE_DOUBLE = "double";
	static final String TYPE_CHAR = "char";
	static final String DEFAULT_NAME_COLUMN = "NAME";
	static final String DEFAULT_ID_COLUMN = "GUID";
	static final String DEFAULT_DATAKEY_COLUMN = "DATAKEY";
	private static boolean debug = true;
	/**
	 * single shared connection
	 */
	Connection conn;
	/**
	 * readonly?
	 */
	boolean readonly;
	/**
	 * config
	 */
	String dataTableName;
	/**
	 * config
	 */
	String dataKeyType;
	/**
	 * config
	 */
	String dataKeyColumn;
	/**
	 * config
	 */
	IDataKeyMap dataKeyMapInstance;
	/**
	 * config
	 */
	IDataKeyAllocate dataKeyAllocateInstance;
	/**
	 * config (null if "NAME" in helper table)
	 */
	String dataNameColumn;
	/**
	 * config
	 */
	String helperTableName;
	/**
	 * config
	 */
	String tupleTypeName;
	/**
	 * config
	 */
	int tupleSize;
	/**
	 * config
	 */
	String[] tupleFieldTypes;
	/**
	 * config
	 */
	String[] tupleFieldColumns;

	/**
	 * SELECT ... WHERE part of query to get data from data table
	 */
	String getValuesQuery;

	GUID responsible;

	/**
	 * default constructor.
	 */
	public CustomTableDataStore(String storeId, GUID responsible)
			throws DataStoreConfigurationException
	{
		this.responsible = responsible;
		// config...
		ConfigManager config = (ConfigManager) SingletonManager.get(equip.config.ConfigManagerImpl.class.getName());
		String jdbcUrl = config.getStringValue(storeId + ".url", null);
		String jdbcUsername = config.getStringValue(storeId + ".user", "root");
		String jdbcPassword = config.getStringValue(storeId + ".password", "");
		String jdbcDriver = config.getStringValue(storeId + ".driver", null);
		if (jdbcUrl == null || jdbcDriver == null)
		{
			System.err.println("ERROR: CustomTableDataStore(" + storeId + ") not cofigured; requires:");
			System.err.println(storeId + ".url: <JDBC-URL> [e.g. \"jdbc:mysql://serverURL/dbName\"]");
			System.err.println(storeId + ".user: <JDBC-username> [default: root]");
			System.err.println(storeId + ".password: <JDBC-password> [Optional]");
			System.err.println(storeId + ".driver: <JDBC-driver-classname> [e.g. \"com.mysql.jdbc.Driver\"]");
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId + " not configured");
		}
		// initialise JDBC driver
		try
		{
			// The newInstance() call is a work around for some
			// broken Java implementations
			Class.forName(jdbcDriver).newInstance();
		}
		catch (Exception e)
		{
			throw new DataStoreConfigurationException("Unable to load SQL driver " + jdbcDriver);
		}
		// get single connection
		try
		{
			if (jdbcPassword.equals(""))
			{
				jdbcPassword = null;
			}
			conn = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
		}
		catch (SQLException E)
		{
			throw new DataStoreConfigurationException("Unable to connect to database " +
					jdbcUrl + " with username " +
					jdbcUsername + " and password " +
					(jdbcPassword == null ? "(null)" : jdbcPassword) +
					": " + E.toString());
		}
		// initialise config variables
		readonly = config.getBooleanValue(storeId + ".readonly", true);

		dataTableName = config.getStringValue(storeId + ".dataTableName", null);
		if (dataTableName == null)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" must have .dataTableName configured");
		}
		dataKeyType = config.getStringValue(storeId + ".dataKeyType", TYPE_INT);
		if (!dataKeyType.equals(TYPE_INT) &&
				!dataKeyType.equals(TYPE_STRING) &&
				!dataKeyType.equals(TYPE_FLOAT) &&
				!dataKeyType.equals(TYPE_DOUBLE) &&
				!dataKeyType.equals(TYPE_CHAR))
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					".dataKeyType unknown: " + dataKeyType);
		}
		dataKeyColumn = config.getStringValue(storeId + ".dataKeyColumn", null);
		if (dataTableName == null)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" must have .dataKeyColumn configured");
		}
		String dataKeyMapClass = config.getStringValue(storeId + ".dataKeyMapClass",
				equip.data.sql.DataKeyMapGetUnique.class.getName());
		try
		{
			Class clazz = Class.forName(dataKeyMapClass);
			dataKeyMapInstance = (IDataKeyMap) clazz.getConstructor(new Class[0]).newInstance();
			dataKeyMapInstance.init(storeId);
		}
		catch (Exception e)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" could not create instance of dataKeyMapClass " +
					dataKeyMapClass + ": " + e);
		}
		String dataKeyAllocateClass = config.getStringValue(storeId + ".dataKeyAllocateClass",
				equip.data.sql.DataKeyAllocateMax.class.getName());
		try
		{
			Class clazz = Class.forName(dataKeyAllocateClass);
			dataKeyAllocateInstance = (IDataKeyAllocate) clazz.getConstructor(new Class[0]).newInstance();
		}
		catch (Exception e)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" could not create instance of dataKeyAllocateClass " +
					dataKeyAllocateClass + ": " + e);
		}
		dataNameColumn = config.getStringValue(storeId + ".dataNameColumn", null);
		helperTableName = config.getStringValue(storeId + ".helperTableName", null);
		if (helperTableName == null)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" must have .helperTableName configured");
		}
		tupleTypeName = config.getStringValue(storeId + ".tupleTypeName", null);
		if (tupleTypeName == null)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" must have .tupleTypeName configured");
		}
		tupleSize = config.getLongValue(storeId + ".tupleSize", -1);
		if (tupleSize <= 0)
		{
			throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
					" must have .tupleSize configured (>0)");
		}
		int i;
		tupleFieldTypes = new String[tupleSize];
		tupleFieldColumns = new String[tupleSize];
		for (i = 1; i <= tupleSize; i++)
		{
			tupleFieldTypes[i - 1] = config.getStringValue(storeId + ".field" + i + "Type", TYPE_INT);
			if (!tupleFieldTypes[i - 1].equals(TYPE_INT) &&
					!tupleFieldTypes[i - 1].equals(TYPE_STRING) &&
					!tupleFieldTypes[i - 1].equals(TYPE_FLOAT) &&
					!tupleFieldTypes[i - 1].equals(TYPE_DOUBLE) &&
					!tupleFieldTypes[i - 1].equals(TYPE_CHAR))
			{
				throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
						".tupleField" + i + "Type unknown: " +
						tupleFieldTypes[i - 1]);
			}
			tupleFieldColumns[i - 1] = config.getStringValue(storeId + ".field" + i + "Column", null);
			if (tupleFieldColumns[i - 1] == null)
			{
				throw new DataStoreConfigurationException("CustomTableDataStore " + storeId +
						" must have .tupleField" + i + "Column configured");
			}
		}//for(i)

		// get value(s) query basis
		StringBuilder getValuesQuery = new StringBuilder();
		getValuesQuery.append("SELECT ");
		getValuesQuery.append(dataKeyColumn);
		//name in data table?
		getValuesQuery.append(",");
		getValuesQuery.append((dataNameColumn != null ? dataNameColumn : dataKeyColumn));

		for (i = 0; i < tupleSize; i++)
		{
			getValuesQuery.append(",");
			getValuesQuery.append(tupleFieldColumns[i]);
		}
		getValuesQuery.append(" FROM ")
				.append(dataTableName)
				.append(" WHERE ");
		this.getValuesQuery = getValuesQuery.toString();

		// try early test(s) on tables to catch config problems if poss.
		// ....
	}

	/**
	 * string to guid
	 */
	private static GUID parseGUID(String guid)
	{
		GUID id = new GUIDImpl();
		StringTokenizer toks = new StringTokenizer(guid, "[].:");
		try
		{
			id.host_id = new Integer(toks.nextToken()) << 24 |
					(new Integer(toks.nextToken()) << 16) |
					(new Integer(toks.nextToken()) << 8) |
					(new Integer(toks.nextToken()));
			id.proc_id = (new Integer(toks.nextToken()) << 16) |
					(new Integer(toks.nextToken()));
			id.item_id = new Integer(toks.nextToken());
			id.time_s = new Integer(toks.nextToken());
			return id;
		}
		catch (Exception e)
		{
			System.err.println("ERROR parsing GUID " + guid + ": " + e);
		}
		return null;
	}

	/**
	 * check if store would like to/be prepared to handle this
	 * {@link AddEvent}.
	 *
	 * @param add The {@link AddEvent} to be handled.
	 * @return true iff this store would be happy/able to handle it.
	 */
	public boolean checkAdd(AddEvent add)
	{
		if (readonly)
		{
			return false;
		}

		// we can't handle leases (at the moment, anyway)
		if (add.binding.info.itemLease != null)
		{
			return false;
		}

		// we can't handle process bound
		if (add.binding.info.processBound)
		{
			return false;
		}

		// we aren't handling locking or ownership either! FIXME

		// must be a tuple, with the right number of fields,
		// and the right field[0] tuple type name
		if (!(add.binding.item instanceof equip.data.Tuple))
		{
			return false;
		}
		Tuple tuple = (Tuple) add.binding.item;
		if (tuple.fields.length != tupleSize + 1 ||
				!(tuple.fields[0] instanceof StringBox) ||
				!((StringBox) (tuple.fields[0])).value.equals(tupleTypeName))
		{
			return false;
		}

		// ok
		return true;
	}

	/**
	 * Flush any pending persistent records
	 */
	public void flush()
	{
	}

	/**
	 * get the ItemBinding maintained by this store which should be
	 * considered when pattern matching the associated itemTemplates
	 * for an add/delete while present pattern.
	 *
	 * @param itemTemplates array of template data items, else
	 *                      null or zero length list for a wild-card (any item).
	 * @return Enumeration of ItemBindings that should be considered
	 * (guaranteed to be a superset of possible matches).
	 */
	public synchronized Iterable<ItemBinding> getCandidateItemBindings(ItemData[] itemTemplates)
	{
		try
		{
			List<String> dataKeys = null;
			if (itemTemplates == null ||
					itemTemplates.length == 0)
			{
				// wildcard - return all items!
				String sql = "SELECT " + dataKeyColumn + " FROM " + dataTableName;
				dataKeys = doSQLQuery(sql);
			}
			else
			{
				// return only specific items
				// plausible => equip.data.Tuple, field[0] = null or tupleTypeName
				int it;
				boolean couldMatch = false;
				for (it = 0; !couldMatch && it < itemTemplates.length; it++)
				{
					if (itemTemplates[it] instanceof equip.data.Tuple)
					{
						equip.data.Tuple template = (equip.data.Tuple) itemTemplates[it];
						if (template.fields.length == 0 ||
								(template.fields[0] instanceof StringBox &&
										((StringBox) (template.fields[0])).value.equals(tupleTypeName)))
						{
							couldMatch = true;
						}
					}
				}
				if (!couldMatch)
				{
					return null;
				}

				// ....
				// oh, well, all for now... FIXME
				String sql = "SELECT " + dataKeyColumn + " FROM " + dataTableName;
				dataKeys = doSQLQuery(sql);
			}

			// make ItemBindings for each...
			List<ItemBinding> bindings = new ArrayList<>();
			for (String dataKey : dataKeys)
			{
				// do we have a GUID already?
				List<String> res = doSQLQuery("SELECT GUID FROM " + helperTableName + " WHERE DATAKEY=" +
						quoteType(dataKeyType, dataKey));
				GUID id;
				if (res.size() == 0)
				{
					// allocate GUID for item
					id = dataKeyMapInstance.mapKey(dataKeyType, dataKey);
					// write to table
					doSQLUpdate("INSERT INTO " + helperTableName +
							" (GUID,DATAKEY) VALUES ('" + id + "'," +
							quoteType(dataKeyType, dataKey) + ")");
				}
				else
				{
					// parse existing GUID
					id = parseGUID(res.get(0));
				}
				bindings.add(getItemBinding(id));
			}
			return bindings;
		}
		catch (Exception e)
		{
			System.err.println("ERROR in CustomTableDataStore.getCandidateItemBindings: " + e);
			e.printStackTrace(System.err);
		}
		return null;
	}

	/**
	 * returns GUIDs of all leased items expiring at or before time
	 * 'now'.
	 *
	 * @param now The current time of the expiration clock.
	 * @return Enumeration of {@link GUID}s of now expiring data items
	 * which the call might now reasonably issue delete events
	 * for).
	 */
	public synchronized Iterable<GUID> getExpiredGUIDs(equip.runtime.Time now)
	{
		// we don't accept leased (at the mo - FIXME)
		return null;
	}

	/**
	 * returns lowest (soonest, or furthest in past) expire time of
	 * any leased item in this store.
	 *
	 * @return lowest (soonest, or furthest in past) expire time, else null.
	 */
	public synchronized equip.runtime.Time getFirstExpireTime()
	{
		// we don't accept leased (at the mo - FIXME)
		return null;
	}

	/**
	 * get the ItemBinding for the given id iff it is maintained by
	 * this store, else null.
	 *
	 * @param id the id of the data item being requested.
	 * @return the {@link ItemBinding} for that item, else null iff
	 * unknown to this store.
	 */
	public synchronized ItemBinding getItemBinding(GUID id)
	{
		try
		{
			// we must have the GUID
			List<String> res = doSQLQuery("SELECT NAME,DATAKEY FROM " + helperTableName +
					" WHERE GUID='" + id.toString() + "'");
			if (res.size() < 2)
			{
				return null;
			}
			String name = res.get(0);
			String dataKey = res.get(1);

			// get value(s)
			res = doSQLQuery(getValuesQuery + " " + dataKeyColumn + "=" +
					quoteType(dataKeyType, dataKey));
			if (res.size() != tupleSize + 2)
			{
				System.err.println("ERROR: query for dataKey=" + dataKey +
						" value returned " + res.size() + " items; expected " +
						(tupleSize + 1) + "; ignoring item!");
				return null;
			}

			// make Tuple
			Tuple tuple = new TupleImpl();
			tuple.id = id;
			// name from data table?
			tuple.name = dataNameColumn != null ? res.get(1) : name;
			tuple.fields = new ValueBase[tupleSize + 1];
			tuple.fields[0] = new StringBoxImpl(tupleTypeName);
			int i;
			for (i = 0; i < tupleSize; i++)
			{
				// res.elementAt(0) = Prim.key.
				// res.elementAt(1) = name (optional)
				tuple.fields[i + 1] = newTupleField(tupleFieldTypes[i], res.get(i + 2));
			}

			// make ItemBinding
			ItemBinding binding = new ItemBindingImpl();
			binding.item = tuple;
			ItemBindingInfo info = new ItemBindingInfoImpl();
			//GUID agentId, int locked, boolean processBound, boolean local, Lease itemLease
			info.init(responsible, LockType.LOCK_NONE, false, false, null);
			info.responsible = responsible;
			binding.info = info;

			return binding;
		}
		catch (Exception e)
		{
			System.err.println("ERROR in CustomTableDataStore.getItemBinding: " + e);
			e.printStackTrace(System.err);
		}
		return null;
	}

	/**
	 * get GUIDs of data items in this store which are process bound
	 * to the given responsible ID as per the RemoveResponsible event
	 * (or not, according to inverse flag).
	 *
	 * @param remove the {@link RemoveResponsible} event.
	 * @return an Enumeration of the GUIDs of locally maintained
	 * data items that should now be deleted.
	 */
	public synchronized Iterable<ItemBinding> getRemoveResponsibleGUIDs(final RemoveResponsible remove)
	{
		if (readonly)
		{
			return null;
		}

		// we don't accept process bound (at the mo - FIXME)
		return null;
	}

	/**
	 * request that store handle the Add event, adding to itself
	 * accordingly.
	 * <p/>
	 * Note: also has to handle lease updates.
	 *
	 * @param add The {@link AddEvent} to be handled.
	 * @return true iff this store has handled the add event; false if declined.
	 * (normally as indicated by a call to {@link IDataStore#checkAdd}
	 */
	public synchronized boolean handleAdd(AddEvent add)
	{
		if (!checkAdd(add))
		{
			return false;
		}

		try
		{
			Tuple tuple = (Tuple) add.binding.item;

			// get a new PK
			String dataKey = dataKeyAllocateInstance.allocateKey(dataKeyType,
					dataTableName,
					dataKeyColumn,
					conn);
			// try adding to data first...
			StringBuilder addValueUpdate = new StringBuilder();
			addValueUpdate.append("INSERT INTO ")
					.append(dataTableName)
					.append(" (").append(dataKeyColumn);
			if (dataNameColumn != null)
			{
				addValueUpdate.append(",")
						.append(dataNameColumn);
			}

			int i;
			for (i = 0; i < tupleSize; i++)
			{
				if (tupleFieldColumns[i].equals(dataKeyColumn) ||
						(dataNameColumn != null &&
								tupleFieldColumns[i].equals(dataNameColumn)))
				// already done
				{
					continue;
				}
				addValueUpdate.append(",");
				addValueUpdate.append(tupleFieldColumns[i]);
			}

			addValueUpdate.append(") VALUES (")
					.append(quoteType(dataKeyType, dataKey));
			if (dataNameColumn != null)
			{
				addValueUpdate.append(",")
						.append(quoteType(TYPE_STRING, add.binding.item.name));
			}

			for (i = 0; i < tupleSize; i++)
			{
				if (tupleFieldColumns[i].equals(dataKeyColumn) ||
						(dataNameColumn != null &&
								tupleFieldColumns[i].equals(dataNameColumn)))
				// already done
				{
					continue;
				}
				addValueUpdate.append(",");
				addValueUpdate.append(fieldToSQLString(tupleFieldTypes[i],
						tuple.fields[i + 1]));
			}
			addValueUpdate.append(")");

			doSQLUpdate(addValueUpdate.toString());

			// check max value if dataKey null
			if (dataKey == null)
			{
				List<String> res = doSQLQuery("SELECT MAX(" + dataKeyColumn + ") FROM " + dataTableName);
				dataKey = res.get(0);
			}

			// add to helper table
			doSQLUpdate("INSERT INTO " + helperTableName +
					" (GUID,NAME,DATAKEY) VALUES ('" +
					add.binding.item.id.toString() + "'," +
					quoteType(TYPE_STRING, add.binding.item.name) + "," +
					quoteType(dataKeyType, dataKey) + ")");

			return true;
		}
		catch (Exception e)
		{
			System.err.println("ERROR: CustomTableDataStore.handleAdd: " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * request that store handles the given delete, which it must
	 * iff it is currently maintaining state for the corresponding
	 * data item.
	 *
	 * @param del the {@link UpdateEvent} to be handled.
	 * @return true iff the delete has been handled (and necessarily
	 * the item's state was maintained by this store).
	 */
	public synchronized boolean handleDelete(DeleteEvent del)
	{
		if (readonly)
		{
			return false;
		}
		try
		{
			// we must have the GUID
			List<String> res = doSQLQuery("SELECT NAME,DATAKEY FROM " + helperTableName +
					" WHERE GUID='" + del.id.toString() + "'");
			if (res.size() < 2)
			{
				return false;
			}

			// delete from data
			doSQLUpdate("DELETE FROM " + dataTableName + " WHERE " + dataKeyColumn +
					"=" + quoteType(dataKeyType, res.get(1)));

			// delete from helpers
			doSQLUpdate("DELETE FROM " + helperTableName + " WHERE GUID='" +
					del.id.toString() + "'");

			return true;
		}
		catch (Exception e)
		{
			System.err.println("ERROR: CustomTableDataStore.handleDelete: " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * request that store handles the given update, which it must
	 * iff it is currently maintaining state for it.
	 * <p/>
	 * Note: implies currently no migration of item state between
	 * store(s).
	 *
	 * @param update the {@link UpdateEvent} to be handled.
	 * @return true iff the update has been handled (and necessarily
	 * the item's state is maintained by this store).
	 */
	public synchronized boolean handleUpdate(UpdateEvent update)
	{
		if (readonly)
		{
			return false;
		}

		try
		{
			// we must have the GUID
			List<String> res = doSQLQuery("SELECT NAME,DATAKEY FROM " + helperTableName +
					" WHERE GUID='" + update.item.id.toString() + "'");
			if (res.size() < 2)
			{
				return false;
			}

			Tuple tuple = (Tuple) update.item;

			// update data
			StringBuilder updateValueUpdate = new StringBuilder();
			updateValueUpdate.append("UPDATE ")
					.append(dataTableName)
					.append(" SET");
			if (dataNameColumn != null)
			{
				updateValueUpdate.append(" ")
						.append(dataNameColumn)
						.append("=")
						.append(quoteType(TYPE_STRING, update.item.name));
			}

			int i;
			for (i = 0; i < tupleSize; i++)
			{
				if (tupleFieldColumns[i].equals(dataKeyColumn) ||
						(dataNameColumn != null &&
								tupleFieldColumns[i].equals(dataNameColumn)))
				// already done
				{
					continue;
				}
				updateValueUpdate.append(" ")
						.append(tupleFieldColumns[i])
						.append("=").append(fieldToSQLString(tupleFieldTypes[i], tuple.fields[i + 1]));
			}
			updateValueUpdate.append(" WHERE ")
					.append(dataKeyColumn)
					.append("=")
					.append(quoteType(dataKeyType, res.get(1)));

			doSQLUpdate(updateValueUpdate.toString());

			// update helper if holding name (and changed)
			if (dataNameColumn == null &&
					((res.get(0) == null &&
							update.item.name != null) ||
							(res.get(0) != null &&
									update.item.name == null) ||
							(res.get(0) != null &&
									update.item.name != null &&
									!update.item.name.equals(res.get(0)))))
			{
				doSQLUpdate("UPDATE " + helperTableName + " SET NAME=" +
						quoteType(TYPE_STRING, update.item.name) +
						" WHERE GUID='" + update.item.id.toString() + "'");
			}

			return true;
		}
		catch (Exception e)
		{
			System.err.println("ERROR: CustomTableDataStore.handleUpdate: " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * check if store is maintaining state for the given {@link GUID}.
	 *
	 * @param id The GUID of the data item in question.
	 * @return true iff the store currently has state for this item.
	 */
	public boolean holdsGUID(GUID id)
	{
		try
		{
			// we must have the GUID
			List<String> res = doSQLQuery("SELECT NAME,DATAKEY FROM " + helperTableName +
					" WHERE GUID='" + id.toString() + "'");
			return res.size() >= 2;
		}
		catch (Exception e)
		{
			System.err.println("ERROR: CustomTableDataStore.holdsGUID: " + e);
			e.printStackTrace(System.err);
		}
		return false;
	}

	/**
	 * terminate - tidy up.
	 */
	public synchronized void terminate()
	{
		try
		{
			conn.close();
		}
		catch (Exception e)
		{
			System.err.println("WARNING: CustomTableDataStore.terminate: " + e);
		}
	}

	/**
	 * reduce lease on all leased items with given responsible id to
	 * expire at 'expire time'.
	 *
	 * @param responsible required item responsible id
	 * @param expire      the new expire time for matched items
	 */
	public void truncateExpireTimes(GUID responsible, equip.runtime.Time expire)
	{
		// we don't accept leased (at the mo - FIXME)
	}

	/**
	 * sql query.
	 *
	 * @return Vector of Strings
	 */
	private List<String> doSQLQuery(String query) throws SQLException
	{
		try
		{
			// Use some connection we've already created
			Statement stmt = conn.createStatement();
			System.out.println("EXECUTING QUERY: " + query);
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData meta = rs.getMetaData();
			List<String> res = new ArrayList<>();
			while (rs.next())
			{
				int i;
				for (i = 1; i <= meta.getColumnCount(); i++)
				{
					res.add(rs.getString(i));
				}
			}
			rs.close();
			stmt.close();
			return res;
		}
		catch (SQLException e)
		{
			System.err.println("ERROR(CustomTableDataStore) executing SQL update \"" +
					query + "\": " + e);
			throw e;
		}
	}

	/**
	 * sql update.
	 */
	private void doSQLUpdate(String update) throws SQLException
	{
		try
		{
			// Use some connection we've already created
			Statement stmt = conn.createStatement();
			System.out.println("EXECUTING UPDATE: " + update);
			stmt.executeUpdate(update);
			stmt.close();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR(CustomTableDataStore) executing SQL query \"" +
					update + "\": " + e);
			throw e;
		}
	}

	private String fieldToSQLString(String type, ValueBase field)
	{
		if (field == null)
		{
			return "NULL";
		}
		if (type.equals(TYPE_STRING))
		{
			return quoteType(TYPE_STRING,
					((StringBox) field).value);
		}
		if (type.equals(TYPE_INT))
		{
			return Integer.toString(((IntBox) field).value);
		}
		if (type.equals(TYPE_FLOAT))
		{
			return Float.toString(((FloatBox) field).value);
		}
		if (type.equals(TYPE_DOUBLE))
		{
			return Double.toString(((DoubleBox) field).value);
		}
		if (type.equals(TYPE_CHAR))
		{
			return "'" + ((CharBox) field).value + "'";
		}
		return null;
	}

	/**
	 * make a correctly typed typle field value
	 */
	private ValueBase newTupleField(String type, String value)
	{
		try
		{
			if (type.equals(TYPE_STRING))
			{
				return new StringBoxImpl(value);
			}
			if (type.equals(TYPE_INT))
			{
				return new IntBoxImpl(new Integer(value));
			}
			if (type.equals(TYPE_FLOAT))
			{
				return new FloatBoxImpl(new Float(value));
			}
			if (type.equals(TYPE_DOUBLE))
			{
				return new DoubleBoxImpl(new Double(value));
			}
			if (type.equals(TYPE_CHAR))
			{
				return new CharBoxImpl(value.charAt(0));
			}
			System.err.println("Unknown tuple field type: " + type);
		}
		catch (Exception e)
		{
			System.err.println("ERROR doing CustomTableDataStore.newTupleField(" +
					type + ", " + value + "): " + e);
		}
		return null;
	}

	/**
	 * to type-specific quoting if required
	 */
	private String quoteType(String type, String value)
	{
		if (type.equals(TYPE_STRING))
		{
			if (value == null)
			{
				return "NULL";
			}
			return "'" + value + "'";
		}
		return value;
	}
}
//EOF




