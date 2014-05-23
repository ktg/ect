/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
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
  Ian MacColl (University of Glasgow)

*/
package equip.data;

import equip.config.ConfigManager;

import equip.runtime.*;
import equip.net.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
/** Dataspace manager, the preferred factory for dataspace clients and
    servers.  Use this to get dataspace clients and servers; see
    {@link
    #getDataspace(equip.net.ServerURL,int,boolean,boolean,equip.data.GUID)}. */
public class DataManager {
    // dataspace type
    /** type for a dataspace client */
    public static final int DATASPACE_CLIENT = 0;
    /** type for a dataspace server */
    public static final int DATASPACE_SERVER = 1;
    /** type for a dataspace peer (not yet implemented) */
    public static final int DATASPACE_PEER = 2;

    
    /** get singleton data manager (now just uses {@link
     * equip.runtime.SingletonManager}). */
    public static DataManager getInstance() {
      return (DataManager)SingletonManager.get("equip.data.DataManager");
    }

    /* API */
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * Converts the <code>url</code> to a {@link equip.net.ServerURL}, 
     * defaults <code>asyncFlag</code> to false, and 
     * allocates a new random <code>responsible</code> ID (see {@link 
     * #getDataspace(equip.net.ServerURL,int,boolean,boolean,equip.data.GUID)}).
    */
    public DataProxy getDataspace(String url, int type,
				  boolean activateFlag) {
	return getDataspace(url, type, activateFlag, false);
    }
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * Converts the <code>url</code> to a {@link equip.net.ServerURL},
     * and allocates a new random <code>responsible</code> ID (see
     * {@link #getDataspace(String,int,boolean,boolean,GUID)}).
     */
    public DataProxy getDataspace(String url, int type,
				  boolean activateFlag, 
				  boolean asyncFlag) {
	return getDataspace(url, type, activateFlag, asyncFlag, null);
    }
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * Converts the <code>url</code> to a {@link equip.net.ServerURL},
     * (see {@link #getDataspace(String,int,boolean,boolean,GUID)}).
     */
    public DataProxy getDataspace(String url, int type,
				  boolean activateFlag, 
				  boolean asyncFlag, GUID responsible) {
      ServerURL surl = new ServerURL(url);
      if (surl.getURL()==null) {
	  System.err.println("ERROR: DataManager::getDataspace for ill-formed url: "
			     + url);
	  return null;
      }
      return getDataspace(surl, type, activateFlag, asyncFlag, responsible);
    }
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * Defaults <code>asyncFlag</code> to false, and 
     * allocates a new random <code>responsible</code> ID (see {@link 
     * #getDataspace(equip.net.ServerURL,int,boolean,boolean,equip.data.GUID)}).
    */
    public DataProxy getDataspace(ServerURL url, int type, 
				  boolean activateFlag) {
	return getDataspace(url, type, activateFlag, false);
    }
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * Allocates a new random <code>responsible</code> ID (see {@link
     * #getDataspace(equip.net.ServerURL,int,boolean,boolean,equip.data.GUID)}).
    */
    public DataProxy getDataspace(ServerURL surl, int type,
				  boolean activateFlag,
				  boolean asyncFlag) {
	return getDataspace(surl, type, activateFlag, asyncFlag, null); 
    }
    /**  Find a dataspace replica in the local cache, else create and
     * add to the cache.
     * 
     * @param url EQUIP dataspace {@link equip.net.ServerURL}
     * @param type {@link #DATASPACE_CLIENT}, {@link #DATASPACE_SERVER} or
     * {@link #DATASPACE_PEER} (peer is not yet implemented).
     * (if the dataspace is not already present in the cace). 
     * @param activateFlag call {link DataProxy#activate} before
     * returning (caller calls deactivate)
     * (if the dataspace is not already present in the cace). 
     * @param asyncFlag if <code>activateFlag</code> is true, then
     * call {@link DataProxy#activateAsync} rather than
     * {link DataProxy#activate}
     * (if the dataspace is not already present in the cace). 
     * @param responsible Use this {@link GUID} as the replica ID
     * (if the dataspace is not already present in the cace). 
     * @return A reference to a new or cached {@link DataProxyImpl} or
     * {@link Server} dataspace replica.
    */
    public DataProxy getDataspace(ServerURL surl, int type,
				  boolean activateFlag,
				  boolean asyncFlag, GUID responsible) {
	String buf = surl.getURL();
	if (buf==null) {
	    System.err.println("ERROR: DataManager::getDataspace for ill-formed ServerURL");
	    return null;
	}
	
	if (type==DATASPACE_PEER) {
	    System.err.println("Warning: DATASPACE_PEER not fully supported - "
			       + "using DATASPACE_SERVER");
	}
	
	String name = buf;
	synchronized(this) {
	    DataspaceInfo iter;
	    iter = (DataspaceInfo)dataspaceMap.get(name);
	    if (iter!=null) {
		// found - do _add_ref and inc refCount
		//iter.dataspace->_add_ref();
		iter.refCount++;
		this.notifyAll();
		return iter.dataspace;
	    }
	    // need to create and add
	    DataProxy dataspace = null;
	    Moniker moniker = surl.getMoniker();

	    if (type==DATASPACE_SERVER || type==DATASPACE_PEER) 
		{
      System.out.println("DataManager: starting data server " + buf);

	  Server server = null;
	  if(moniker instanceof SimpleMoniker)
	  {
		// Argument is a simple moniker. Create server on specified
		// port.
		if ((moniker instanceof SimpleTCPMoniker &&
		     ((SimpleTCPMoniker)moniker).port==0) ||
		    (moniker instanceof SimpleUDPMoniker &&
		     ((SimpleUDPMoniker)moniker).port==0)) 
		    name = null; // re-create
		    
		server = new Server((SimpleMoniker) moniker);
		if (name==null) {
		    // dynamically allocated -> work out canonical name based on actual port
		    name = new ServerURL(server.getMoniker()).getURL();
		    System.err.println("New server on random port/url: "+name);
		}
	  }
      else if (moniker instanceof TraderMoniker) 
	  {
		TraderMoniker tmon = (TraderMoniker) moniker;

		// Argument is a trader moniker.
		  
		// Get trader address as simple moniker.
		if(tmon.trader instanceof SimpleMoniker)
		{
			SimpleMoniker smon = (SimpleMoniker) tmon.trader;

			// Base server moniker on trader moniker.
			// New instance will have default port, address and encoding.
			try
			{
				SimpleMoniker serverMoniker = (SimpleMoniker) smon.getClass().newInstance();
				server = new Server(serverMoniker);
			}
			catch(Exception e)
			{
				System.err.println("ERROR: DataManager.getDataspace " + e);
				return null;
			}
		}
		else
		{
			System.err.println("ERROR: DataManager::getDataspace: Could create server for unknown moniker " + tmon.trader.getClass().getName());
			return null;
		}

		// Convert trader moniker to TCP as trader currently
		// only supports TCP.
		// HACK! We shouldn't be using the URL to define the
		// server protocol, trader TCP address and dataspace
		// name. These should be specified separatly using
		// multiple URLs. This method also means that we have
		// to add code here to convert from other protocols,
		// which may be impossible if they don't specify
		// address and port fields.
		if(tmon.trader instanceof SimpleUDPMoniker)
		{
			SimpleUDPMoniker udpMoniker = (SimpleUDPMoniker) tmon.trader;
			SimpleTCPMoniker tcpMoniker = new SimpleTCPMonikerImpl();
			tcpMoniker.initFromAddr(udpMoniker.addr, udpMoniker.port);
			tmon.trader = tcpMoniker;
		}

		// Rebind server with Trader.
		System.err.println("- rebinding trader moniker for this server...");
		SimpleMoniker serverMon = server.getMoniker();
		tmon.rebind(serverMon);
      }
      dataspace = server; // take ref
      if (activateFlag) {
		if (asyncFlag)
		  dataspace.activateAsync();
		else
		  // should be null op
		  dataspace.activate(null,null);
	      }
	      System.err.println("- OK");
	    } else {
	      // proxy
	      System.err.println("DataManager: creating new proxy " + buf + "...");
	      // setting responsible to consistent value may assist in expiring
	      // old connections from previous incarnations and/or persistent
	      // connections
	      dataspace = new DataProxyImpl(responsible);
	      dataspace.serviceMoniker = moniker;
	      System.err.println("- Activate...");
	      if (activateFlag) {
		if (asyncFlag)
		  dataspace.activateAsync();
		else if (!dataspace.activate(null, null)) {
		  System.err.println("WARNING: activate failed for dataspace " + buf);
		} 
	      }
	      System.err.println("- OK");
	    }
	    
	    // found - add
	    DataspaceInfo info= new DataspaceInfo();
	    info.dataspace = dataspace;
	    info.refCount = 1;
	    info.type = type;
	    info.name = name;
	    dataspaceMap.put(name, info);
	    this.notifyAll();
    
	    return dataspace;

	}//synchronized(this)
    }
    /** Complementary to <code>getDataspace</code>, reduces use count 
     * by one, and removes from internal table if use count falls to 0. 
     * Matching calls required for garbage collection to have any chance 
     * of a dataspace. */
  public void releaseDataspace(DataProxy dataspace) {
    if (dataspace==null)
      return;
    synchronized(this) {
      Enumeration e;
      String name=null;
      DataspaceInfo iter=null;
      boolean matched = false;
      for (e = dataspaceMap.keys() ; e.hasMoreElements() ;) {
	name = (String)e.nextElement();
	iter = (DataspaceInfo)dataspaceMap.get(name);
	if (iter.dataspace==dataspace) {
	    matched = true;
	    break;
	}
      }
      if (!matched) {
	  System.err.println("ERROR: DataManager::releaseDataspace for unknown dataspace");
	  return;
      }
      iter.refCount--;
      this.notifyAll();
      System.err.println("DataManager::releaseDataspace -> ref count "
			 +iter.refCount);
      if (iter.refCount<=0) {
	  System.err.println("Note: DataManager::releaseDataspace removing final reference "
			     + "to dataspace...");
	  dataspaceMap.remove(name);
	  iter.dataspace.terminate();
	  iter.dataspace = null;
	  System.err.println("DataManager::releaseDataspace done");
      }
    }
  }
    /** get current data proxies known */
    public synchronized DataspaceInfo [] getDataspaceInfos() {
      Enumeration e;
      Vector res = new Vector();
      for (e = dataspaceMap.keys() ; e.hasMoreElements() ;) {
	  String name = (String)e.nextElement();
	  DataspaceInfo iter = (DataspaceInfo)dataspaceMap.get(name);
	  res.addElement(iter);
      }
      return (DataspaceInfo[])res.toArray(new DataspaceInfo[res.size()]);
    }

    /* internals */
    /** Lifecycle - don't use these - rely on the static singleton */
    public DataManager() {
	System.err.println("DataManager created");
	ConfigManager config = (ConfigManager)SingletonManager.get("equip.config.ConfigManagerImpl");
	config.readConfigFile("equip.eqconf", 0, null);
	if (config.getBooleanValue("equip.data.DataManagerBrowser", false))
	    new DataManagerBrowser(this);
    }
    
    /** Internal class of {@link DataManager} */
    public class DataspaceInfo {
	DataProxy dataspace;
	int refCount;
	int type;
	String name;
    };
    //typedef std::map<std::string,DataspaceInfo> DataspaceMap; // url -> DataProxy
    /** Internal cache of dataspaces */
    private Hashtable dataspaceMap = new Hashtable();
}
