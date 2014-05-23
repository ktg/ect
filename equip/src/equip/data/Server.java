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

*/
package equip.data;

import equip.runtime.*;
import equip.net.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.Enumeration;
import equip.config.ConfigManager;
import equip.config.ConfigManagerImpl;

/** The normal implementation of the IDL'd dataspace API, {@link
 * DataProxy} as a dataspace server, delegating most of the
 * work to an instance of class {@link DataDelegate}. <p>
 *
 * Note 1: you should
 * NOT normally create these directly; use the singleton {@link
 * DataManager} to allow sharing of dataspace replicas within a single
 * application. <P>
 * 
 * Note 2: you will probably find the {@link
 * equip.data.beans.IDataspace} API easier to use and more familar in
 * terms of Java idiom (see {@link equip.data.beans.DataspaceBean}). <P> 
 * 
 * Note 3: see the package documentation for notes on security between
 * dataspace server and clients. <P>
 * */
public class Server extends DataProxy implements Runnable
{
    /* Lifecycle */
  public Server(short port) {
    super();
    System.err.println("Starting DataServer");
    dataDelegate.setProxy(this);
    serverSap = null;
    
  SimpleTCPMoniker moniker = new SimpleTCPMonikerImpl();
  moniker.port = port;
  moniker.encoding = Encoding.EQENCODE_EQ_OBJECT_STREAM;
  try
  {
	  byte[] ip = ServerSap.rewriteLocalAddress(InetAddress.getLocalHost()).getAddress();
	  moniker.addr = 
				((((int)ip[0]) & 0xff) << 24) |
				((((int)ip[1]) & 0xff) << 16) |
				((((int)ip[2]) & 0xff) << 8) |
				((((int)ip[3]) & 0xff));
	  moniker.port = port;
	  moniker.token = null;
	  init(moniker);
  }
  catch (Exception e)
  {
  }
  }

  // if you already know a moniker, e.g. from ServerURL
  public Server(SimpleMoniker moniker) {
    super();
    System.err.println("Starting DataServer");
  dataDelegate.setProxy(this);
  init(moniker);
  }

  public static void main(String args[]) {
    if (args.length!=1) {
      System.err.println("Usage: equip.data.Serer <equip-url>");
      System.exit(-1);
    }
    System.err.println("Starting data server " + args[0] + "...");
    DataManager.getInstance().getDataspace(args[0], 
					   DataManager.DATASPACE_SERVER,
					   true);
  }

  /* DataProxy API - real implementations */
  public void addEvent(Event event) {
    // ....
    dataDelegate.addEvent(event);
  }
	/** (always) Queued add of a new event into the dataspace.
	 *
	 * @param event The event to published. */
	public void queueEvent(equip.data.Event event) 
	{
		dataDelegate.queueEvent(event);
	}
  public ItemBinding getItemBinding(GUID id) 
  { 
    return dataDelegate.getItemBinding(id);
  }
  public ItemData getItem(GUID id) {
    return dataDelegate.getItem(id);
  }
  public void setDefaultAgent(GUID defaultAgentId) {
    this.defaultAgentId = defaultAgentId;
  }
  public void addItem(ItemData item, int locked, boolean local, boolean processBound, Lease itemLease) {
    AddEvent event = new AddEventImpl();
    event.initFromItem(item, defaultAgentId, locked, local,
		       processBound, itemLease);
    addEvent(event);
  }
  public void updateItem2(ItemData item, boolean local, boolean reliable, int priority) {
    UpdateEvent event = new UpdateEventImpl();
    event.initFromItem2(item, defaultAgentId, local, reliable, priority);
    addEvent(event);
  }    
  public void updateItem(ItemData item, boolean local, boolean reliable) {
    updateItem2(item, local, reliable, 0);
  }    
  public void deleteItem(GUID id, boolean local) {
    DeleteEvent event = new DeleteEventImpl();
    event.initFromID(id, defaultAgentId, local);
    addEvent(event);
  }
  public DataSession createSession(DataCallback callback, ValueBase closure) {
    return dataDelegate.createSession(callback, closure);
  }
    
  public void deleteSession(DataSession session) {
    dataDelegate.deleteSession(session);
  }

  /* ServiceProxy API - dummies */
  public boolean activate(DeactivateCallback callback, ValueBase closure){
    return true;
  }
  public void deactivate() {
  }

  /* extra API */
  public short getPort() 
  {
    if (moniker!=null)
	{
		if(moniker instanceof SimpleUDPMoniker)
		{
			return ((SimpleUDPMoniker) moniker).port;
		}
		else if(moniker instanceof SimpleTCPMoniker)
		{
			return ((SimpleTCPMoniker) moniker).port;
		}
    }
    return 0;
  }
  public SimpleMoniker getMoniker() {
    return moniker;
  }
  //public short getUDPPort();
  //public SimpleUDPMoniker* getUDPMoniker();

  /* internals */

  private boolean init(SimpleMoniker m)
  {
	this.moniker = m;

	SimpleMoniker resolved = m.resolve();
	if(resolved instanceof SimpleTCPMoniker)
	{
		SimpleTCPMoniker tcp = (SimpleTCPMoniker) resolved;
		serverSap = new ServerSapTcp(tcp.port);

		if (serverSap.getStatus()!= StatusValues.STATUS_OK) 
		{
			System.err.println("ERROR: DataServer could not start server on port " + tcp.port);
			serverSap = null;
			return false;
		}

		System.err.println("DataServer (TCP) port = " + serverSap.getPort());

		// Fill in moniker port and address fields, which could have been
		// wildcarded with 0 values. Moniker will be registered with Trader
		// and so should have absolute values.
		try
		{
		byte[] ip = ServerSap.rewriteLocalAddress(InetAddress.getLocalHost()).getAddress();
		tcp.addr = 
			((((int)ip[0]) & 0xff) << 24) |
			((((int)ip[1]) & 0xff) << 16) |
			((((int)ip[2]) & 0xff) << 8) |
			((((int)ip[3]) & 0xff));
		}
		catch(Exception e) {;}
		tcp.port = serverSap.getPort();

		moniker = tcp;
	}
	else if(resolved instanceof SimpleUDPMoniker)
	{
		SimpleUDPMoniker udp = (SimpleUDPMoniker) resolved;
		serverSap = new ServerSapUdp(udp.port);

		if (serverSap.getStatus()!= StatusValues.STATUS_OK) 
		{
			System.err.println("ERROR: DataServer could not start server on port " + udp.port);
			serverSap = null;
			return false;
		}

		System.err.println("DataServer (UDP) port = " + serverSap.getPort());

		moniker = udp;

		// Fill in moniker port and address fields, which could have been
		// wildcarded with 0 values. Moniker will be registered with Trader
		// and so should have absolute values.
		try
		{
		byte[] ip = ServerSap.rewriteLocalAddress(InetAddress.getLocalHost()).getAddress();
		udp.addr = 
			((((int)ip[0]) & 0xff) << 24) |
			((((int)ip[1]) & 0xff) << 16) |
			((((int)ip[2]) & 0xff) << 8) |
			((((int)ip[3]) & 0xff));
		}
		catch(Exception e) {;}
		udp.port = serverSap.getPort();
	}

	clientMapLock = new java.lang.Object();
    
    new Thread(this).start();
    dataDelegate.setStatus(true, false, false);
    return true;
  }

	/*
  private boolean init(short port) {
    serverSap = new ServerSapTcp(port);
    if (serverSap.getStatus()!=StatusValues.STATUS_OK) {
      System.err.println("ERROR: DataServer could not start server on port "
			 + port);
      //delete serverSap;
      serverSap = null;
      return false;
    }
    port = serverSap.getPort();
    System.err.println("DataServer port = " +port);
    
    moniker = new SimpleTCPMonikerImpl();
    moniker.initFromPort(port);

    System.err.println("DataServer created on port "+port);

    clientMapLock = new java.lang.Object();
    
    new Thread(this).start();
    dataDelegate.setStatus(true, false, false);
    return true;
  }
  */

  private SimpleMoniker moniker=null;
  private ServerSap serverSap=null;
  private SimpleUDPMoniker udp_moniker=null;
  private DatagramSocket udp_socket=null;
  private DataDelegate dataDelegate = new DataDelegate();
  private GUID defaultAgentId=null;  
  //private typedef std::map<sGUID,ConnectionSap*,sGUIDCompare> ClientMap;
  private Hashtable clientMap=new Hashtable();
  private java.lang.Object clientMapLock=null;

  public void run() {
    System.err.println("DataServer::serverThread running...");
    while (true) {
      ConnectionSap s = serverSap.accept();
      if (s==null) {
	System.err.println("ERROR: DataServer::serverThread failed on accept - exiting");
	break;
      }
      System.err.println("DataServer::serverThread accepted new client...");
      ClientThread clientInfo = new ClientThread(this, s);
      new Thread(clientInfo).start();
    }
  }
    // record of current client connection (only one per client
    // allowed at any time!)
    private class ClientThread implements Runnable {
      Server server;
      ConnectionSap connection;
      ClientThread(Server server, ConnectionSap connection) {
	this.server = server;
      this.connection = connection;
      }
      public void run() {
	System.err.println("DataServer::clientThread running...");
		  // security challenge?
		  DataManager.getInstance();
		  ConfigManager config = (ConfigManager)SingletonManager.get(ConfigManagerImpl.class.getName());
		  String secret = System.getProperty("DataspaceSecret", config.getStringValue("DataspaceSecret", null));
		  String challenge = null;
		  if (secret!=null) 
		  {
			  System.err.println("DataServer issuing security challenge...");
			  PeerChallengeEvent pchallenge = new PeerChallengeEventImpl();
			  pchallenge.challengeType = ChallengeType.CHALLENGE_SIMPLE_CHALLENGE;
			  pchallenge.challenge = challenge = Challenge.makeChallenge();
			  try 
			  {
				  connection.writeObject(pchallenge);
			  } 
			  catch (Exception e) 
			  {
				  System.err.println("ERROR writing challenge to connection: "+e+" - assume connection closed");
				  try 
				  {
					  Thread.sleep(2000);
				  } 
				  catch(Exception ee) {}
				  connection.close();
				  //delete connection;
				  return;
			  }
		  }

	// looking for a PeerConnectEvent
	System.err.println("DataServer::clientThread wait for next object...");
	ValueBase obj=null;
	try {
	  obj = connection.readObject();
	} catch (Exception e) {
	  obj = null;
	}
	if (obj==null) {
	  System.err.println("Warning - object read failed - assume connection closed");
	  try {
	    Thread.sleep(2000);
	  } catch(Exception e) {}
	  connection.close();
	  //delete connection;
	  return;
	}
	if (!(obj instanceof PeerConnectEvent)) {
	  System.err.println("ERROR: DataServer::clientThread did not receive "
			     +"PeerConnectEvent first ("+obj.getClass().getName() 
			     +") - rejecting client");
	  try { Thread.sleep(2000); } catch (Exception e) {}
	  connection.close();
	  return;
	}
	PeerConnectEvent connect = (PeerConnectEvent)obj;
	
	if (connect.peerId==null) {
	  System.err.println("ERROR: DataServer::clientThread PeerConnectEvent "
			     +"did not specify peerId - rejecting client");
	  try { Thread.sleep(2000); } catch (Exception e) {}
	  connection.close();
	  return;
	}	
	if (connect.peerId.time_s < 1000) {
	    // HACK HACK for persistent dataspace peer -> long timeout
	    System.err.println("NOTE: increasing connection timeout for "+
			       "presumed persistent connection (remote)");
	    connection.setConnectionTimeout(SapConstants.DEFAULT_LONG_CONNECTION_TIMEOUT_MS);
	}
		  if (secret!=null) 
		  {
			  System.err.println("Awaiting challenge response from client "+connect.peerId);
			  ValueBase resp=null;
			  try 
			  {
				  resp = connection.readObject();
			  } 
			  catch (Exception e) 
			  {
				  resp = null;
			  }
			  if (resp==null) 
			  {
				  System.err.println("Warning - (challenge) object read failed - assume connection closed");
				  try 
				  {
					  Thread.sleep(2000);
				  } 
				  catch(Exception e) {}
				  connection.close();
				  //delete connection;
				  return;
			  }
			  if (!(resp instanceof PeerChallengeEvent)) 
			  {
				  System.err.println("ERROR: DataServer::clientThread did not receive "
					  +"PeerChallengeEvent second ("+resp.getClass().getName() 
					  +") - rejecting client");
				  try { Thread.sleep(2000); } 
				  catch (Exception e) {}
				  connection.close();
				  return;
			  }
			  PeerChallengeEvent response = (PeerChallengeEvent)resp;
			  if (response.challengeType!=ChallengeType.CHALLENGE_SIMPLE_CHALLENGE_RESPONSE) 
			  {
				  System.err.println("ERROR: received unexpected challenge type "+response.challengeType+" - rejecting client");
				  try { Thread.sleep(2000); } 
				  catch (Exception e) {}
				  connection.close();
				  return;
			  }
			  if (!Challenge.acceptResponse(secret, challenge, response.response)) {
				  System.err.println("ERROR: client failed challenge - rejecting client");
				  PeerChallengeEvent reject = new PeerChallengeEventImpl();
				  reject.challengeType = ChallengeType.CHALLENGE_FAILURE;
				  try {
					  connection.writeObject(reject);
					  Thread.sleep(2000); 
				  } 
				  catch (Exception e) {}
				  connection.close();
				  return;
			  }
			  if (response.challenge!=null) 
			  {
				  PeerChallengeEvent pchallenge = new PeerChallengeEventImpl();
				  pchallenge.challengeType = ChallengeType.CHALLENGE_SIMPLE_RESPONSE;
				  pchallenge.challenge = null;
				  pchallenge.response = Challenge.makeResponse(secret, response.challenge);
				  try 
				  {
					  connection.writeObject(pchallenge);
				  } 
				  catch (Exception e) 
				  {
					  System.err.println("ERROR writing response to connection: "+e+" - assume connection closed");
					  try 
					  {
						  Thread.sleep(2000);
					  } 
					  catch(Exception ee) {}
					  connection.close();
					  //delete connection;
					  return;
				  }
			  }
		  }
	// get peer
	System.err.println("Connect from client " + connect.peerId);
	DataDelegatePeer peer = dataDelegate.createPeer(connect.peerId, 
							true);
	
	// do we have an open connection from this peer already?
	// if so, nuke it before we act on this one.
	
	int timeout = 10;
	boolean okFlag = false;
	while(timeout>0) {
	  synchronized (clientMapLock) {
	    ClientThread cl = (ClientThread)clientMap.get(connect.peerId);
	    
	    if (cl==null) {
	      // new peer - OK 
	      System.err.println("- client is unknown");
	      okFlag = true;
	      clientMap.put(connect.peerId, this);
	      break;
	    } 
	    // already present
	    System.err.println("- client is already connected - killing old connection...");
	    cl.connection.close();
	  }
	  try { Thread.sleep(2000); } catch (Exception e) {}
	  timeout--;
	}; // while
	if (!okFlag) {
	  System.err.println("ERROR: old connection will not go - sorry - cannot accept new connection");
	  return;
	}
	
	// get again in case deleted
	peer = dataDelegate.createPeer(connect.peerId, 
				       true);
	peer.handleEvent(connect);
	// this will send back our own connect
	DataDelegatePeerTCP driver = new DataDelegatePeerTCP(peer);
	boolean persistFlag = false;
	if (connect.policy!=null)
	  persistFlag = connect.policy.persistFlag;
	driver.run(connection, null, persistFlag, null, null);
	System.err.println("Client " +connect.peerId +" terminated");
	if (!persistFlag) {
	  System.err.println("- deleting non-persistent peer");
	  dataDelegate.deletePeer(connect.peerId);
	}
	connection.close(false);
	try { Thread.sleep(1000); } catch (Exception e) {} // testing - who dies first
	driver.cleanup();
	driver = null;
	
	// delete connection with lock to avoid another thread closing it in
	// race
	synchronized(clientMapLock) {
	  if (clientMap.remove(connect.peerId)==null) {
	    System.err.println("INTERNAL ERROR: client could not find self in map");
	  }
	  //delete connection;
	  connection.close();
	  connection = null;
	}
	return;
      }
    }
    /// wait for all pending events 
    public void waitForEvents(boolean local) {
	dataDelegate.waitForEvents(local);
    }
    public void beginBusy() { // ??
    }
    public void endBusy() { // ??
    }
    public void setPersist(boolean persistFlag) {
	//no op 
    }
    public void activateAsync() {
	// no op 
    }
    /** get responsible (replica) ID */
    public GUID getResponsible() {
      return dataDelegate.getPeerId();
    }

    /** Destroy this dataspace, terminating any communication and threads
     * and (hopefully) allowing all resources to be released/GCed.
     */
    public void terminate() {
	System.err.println("WARNING: equip.data.Server.terminate not yet implemented");
    }
}




