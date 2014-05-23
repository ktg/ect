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
package equip.net;

import equip.runtime.*;
import java.io.*;

/** A Simple Server class using a single {@link ServerSapTcp}
 * access point, delegating request handling to an implementation
 * of {@link SimpleServerHandler} */
public class SimpleServer implements Runnable {
  /* Default (null) constructor */
  public SimpleServer() {
  }
  
  /** if you already know a moniker, e.g. from ServerURL; null closure */
  public boolean init(Moniker moniker_in, SimpleServerHandler handler) {
    return init(moniker_in, handler, null);
  }
  /** if you already know a moniker, e.g. from ServerURL */
  public boolean init(Moniker moniker_in, SimpleServerHandler handler, 
	       java.lang.Object closure) {
    // tcp -> use port
    short port = 0;
    if (moniker_in!=null && moniker_in instanceof SimpleTCPMoniker) {
      SimpleTCPMoniker tcp = (SimpleTCPMoniker)(moniker_in);
      port = tcp.port;
    }
    // init
    return init(port, handler, closure);
  }
  /** if you know a port (even 0) but not a moniker; null closure */
  public boolean init(short port, SimpleServerHandler handler) {
    return init(port, handler, null);
  }
  /** if you know a port (even 0) but not a moniker */ 
  public boolean init(short port, SimpleServerHandler handler, 
	       java.lang.Object closure) {
    shutdown();
    this.handler = handler;
    this.closure = closure;
    serverSap = new ServerSapTcp(port);
    if (serverSap.getStatus()!=StatusValues.STATUS_OK) {
      System.err.println("ERROR: SimpleServer could not start server on port "
			 + port);
      //delete serverSap;
      serverSap = null;
      return false;
    }
    port = serverSap.getPort();
    System.err.println("SimpleServer port = " + (((int)port)&0xffff));
    SimpleTCPMoniker tcpMoniker = new SimpleTCPMonikerImpl();
    moniker = tcpMoniker;
    tcpMoniker.initFromPort(port);
    System.err.println("SimpleServer created on port " + port);

    new Thread(this).start();
    return true;
  }    
    /** shut down server */
    public void shutdown() {
	if (serverSap==null)
	    return;
	System.err.println("Warning: SimpleServer::shutdown - could be dangerous...");
	serverSap.close();
	//delete serverSap;
	serverSap = null;
	moniker = null;
    }
    /** get server moniker */
    public SimpleMoniker getMoniker() {
	return moniker;
    }
    
    /* internal */
    public void run() {
	System.err.println("SimpleServer::serverThread running...");
	while (true) {
	    ConnectionSap s = serverSap.accept();
	    if (s==null) {
		System.err.println("ERROR: SimpleServer::serverThread failed on accept - exiting");
		break;
	    }
	    System.err.println("SimpleServer::serverThread accepted new client...");
	    new Thread(new ClientThread(s, this)).start();
	}
    }

    /* internal */
    protected class ClientThread implements Runnable {
	ConnectionSap connection;
	SimpleServer server;
	ClientThread(ConnectionSap connection, SimpleServer server) {
	    this.connection = connection;
	    this.server = server;
	}
	public void run() {
	    System.err.println("SimpleServer::clientThread running...");
	    
	    while (true) {
		System.err.println("SimpleServer::clientThread wait for next object...");
		ValueBase obj=null;
		try {
		    obj = connection.readObject();
		} catch (IOException e) {
		    System.err.println("SimpleServer - readObject failed: "+e);
		    obj = null;
		}
		if (obj==null) {
		    System.err.println("Warning - object read failed - assume connection closed");
		    break;
		}
		// sequential
		synchronized (server) {
		    if (handler!=null) 
			handler.handleServerMessage(connection,
						    obj, closure);
		}
	    }
	    System.err.println("SimpleServer::clientThread terminating");
	    try {
		Thread.sleep(2000); // testing - who dies first
	    } catch (Exception e) {}
	    System.err.println("Bye");
	}
    }
    /* internal */
    protected SimpleMoniker moniker=null;
    /* internal */
    protected ServerSap serverSap=null;
    /* internal */
    protected SimpleServerHandler handler=null;
    /* internal */
    protected java.lang.Object closure=null;
    /* internal - sync to single worker thread (expt). */
    protected java.lang.Object workerLock;
}
