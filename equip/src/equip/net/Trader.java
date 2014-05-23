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
import java.util.Hashtable;
import java.net.*;

/** Java Implementation of {@link TraderProxy}, to provide a simple
 * service trader */
public class Trader extends TraderProxy 
  implements SimpleServerHandler, Runnable
{
    /* Lifecycle */
  public Trader(short port) {
    super();
    mc_socket = null;
    System.err.println("Starting TraderServer");
    server.init(port, this);
    serviceMoniker = server.getMoniker();
  }

    /* TraderProxy API - real implementations */
  public ValueBase lookup(String name, String classname) {
    if (name==null) {
      System.err.println("ERROR: TraderServer::lookup called with null name");
      return null;
    }
    System.err.println("TraderServer::lookup " + name + " (" 
		       + (classname!=null ? classname : "NULL") + ")");

    ValueBase value = (ValueBase)bindings.get(name);
    if (value == null) {
      System.err.println("- not found");
      return null;
    }
    if (classname!=null) {
      Class c = value.getClass();
      do {
	if (classname.equals(c.getName()))
	  break;
	c = c.getSuperclass();
      } while (c!=null);
      if (c==null) {
	System.err.println("- class mismatch (" + value.getClass().getName());
	return null;
      }
    }
    return value;
  }
  public boolean bind(String name, ValueBase proxy) {
    if (bindings.get(name)!=null)
      return false;
    bindings.put(name, proxy);
    return true;
  }
  public boolean rebind(String name, ValueBase proxy) {
    bindings.put(name, proxy);
    return true;
  }
  
    /* ServiceProxy API - dummies */
  public boolean activate(DeactivateCallback callback, ValueBase closure){
    return true;
  }
  public void deactivate() {}

    /* extra API */
    public short getPort() {
      SimpleMoniker mon = server.getMoniker();
      if (mon!=null && mon instanceof SimpleTCPMoniker) {
	SimpleTCPMoniker tcpMon = (SimpleTCPMoniker)(mon);
	return tcpMon.port;
      }
      return 0;
    }

    /* add listening on equipm-specified multicast group/port */
    public int /*StatusValues*/ addMulticast(String equipm_url) {
      ServerURL url = new ServerURL(equipm_url);
      Moniker moniker = url.getMoniker();
      if (!(moniker instanceof MulticastUDPMoniker)) {
	System.err.println("ERROR: TraderServer::addMulticast called for non-multicast "
			  + "url " + equipm_url);
	return StatusValues.STATUS_ERROR;
      }
      MulticastUDPMoniker mc = (MulticastUDPMoniker)(moniker);
      session_id = 1;
      try {
	mc_socket = new MulticastSocket(mc.port);
	InetAddress group = InetAddress.getByName
	  (""+((mc.addr >> 24) & 0xff)+"."+
	   ((mc.addr >> 16) & 0xff)+"."+
	   ((mc.addr >> 8) & 0xff)+"."+
	   ((mc.addr) & 0xff));
	mc_socket.joinGroup(group);
      } catch (Exception e) {
	System.err.println("ERROR: TraderServer::addMulticast unable to join multicast group "
			   + equipm_url + ": "+e);
	mc_socket = null;
	return StatusValues.STATUS_ERROR;
      }
      new Thread(this).start();
      return StatusValues.STATUS_OK;
    }
      

    /* internals */
    /* ServerMixinHandler */
    public void handleServerMessage(ConnectionSap connection, 
				    ValueBase object,
				    java.lang.Object closure) {
      System.err.println("TraderServer::handleClientMessage:"+
			 "- read object (class " + object.getClass().getName()
			 + ") ok");
  
      if (object instanceof TraderLookup) {
	TraderLookup lookupRequest = (TraderLookup)(object);
	ValueBase reply = lookup(lookupRequest.name, 
				 lookupRequest.classname);
	try {
	  connection.writeObject(reply);
	} catch(Exception e) {
	  System.err.println("Trader.handleServerMessage writeObject failed: "+e);
	}
	return;
      } 
      if (object instanceof TraderRebind) {
	TraderRebind rebindRequest = (TraderRebind)(object);
	boolean reply = false;
	if (rebindRequest.replaceFlag) {
	  reply = rebind(rebindRequest.name, rebindRequest.binding);
	}
	else {
	  reply = bind(rebindRequest.name, rebindRequest.binding);
	}

	TraderRebindReply repobj = new TraderRebindReplyImpl();
	repobj.okFlag = reply;
	try {
	  connection.writeObject(repobj);
	} catch(Exception e) {
	  System.err.println("Trader.handleServerMessage writeObject failed: "+e);
	}
	return;
      } 
      System.err.println("ERROR: TraderServer: unknown request type " 
			 +object.getClass().getName());
      // free ref
      try {
	connection.writeObject(null);
      } catch(Exception e) {
	System.err.println("Trader.handleServerMessage writeObject failed: "+e);
      }
    }

    private SimpleServer server = new SimpleServer();
    //typedef std::map<std::string, ValueBase_var> Bindings;
    private Hashtable bindings = new Hashtable();
    // multicast...
    private MulticastSocket mc_socket= null;
    private int session_id;
    public void run() {
	//  void mcThread();
      // PORT FROM C++!!!
      System.err.println("Sorry: Trader Multicast not yet ported to Java!\n");
/*
  cerr << "Trader ready for multicast requests\n";
  
  DatagramPacket_var packet = new DatagramPacket();
  do {

    //------------------------------------------------------------
    // receive request
    packet->setData(new Mbuf(65535, sizeof(UDPPacketHeader)));
    
    if (mc_socket->receive(packet.in())!=STATUS_OK) {
      cerr << "ERROR: trying to receive multicast packet\n";
      continue;
    }
    
    cerr << "Handle multicast request...\n";

    Mbuf_var rmbuf = packet->takeData();
    if (rmbuf.in()==NULL) {
      cerr << "ERROR: TraderServer::mcThread failed to get Mbuf from packet\n";
      continue;
    }
    char *rbuf = rmbuf->getBuf();
    if (rbuf==NULL) {
      cerr << "ERROR: TraderServer::mcThread failed to get buf from Mbuf\n";
      continue;
    }
      
    UDPPacketHeader *rheader = (UDPPacketHeader*)rbuf;
    if (rheader->magic[0] != UDPPacketHeader_magic_0 ||
	rheader->magic[1] != UDPPacketHeader_magic_1 ||
	rheader->magic[2] != UDPPacketHeader_magic_2) {
      cerr << "ERROR: received packet magic number did not match ("
	   << rheader->magic[0] << "," 
	   << rheader->magic[1] << "," 
	   << rheader->magic[2] << " vs " 
	   << UDPPacketHeader_magic_0 << ","
	   << UDPPacketHeader_magic_1 << ","
	   << UDPPacketHeader_magic_2 << ")\n";
      continue;
    }
    if (rheader->version != UDPPacketHeader_version) {
      cerr << "ERROR: received packet version number did not match ("
	   << rheader->version << " vs " 
	   << UDPPacketHeader_version << ")\n";
      continue;
    }
    rheader->src_addr = PR_ntohl(rheader->src_addr);
    rheader->src_port = PR_ntohs(rheader->src_port);
    rheader->src_sid = PR_ntohl(rheader->src_sid);
    rheader->src_mid = PR_ntohl(rheader->src_mid);
    rheader->src_frag = PR_ntohl(rheader->src_frag);
    if (rheader->src_frag != 0x80000000) {
      cerr << "Sorry: received packet that was fragmented - not yet supported\n";
      continue;
    }
    
    //------------------------------------------------------------
    // decode request

    MbufInputStream mbin;
    //cerr << "- reset MbufInputStream\n";
    mbin.reset(rmbuf.in());
    //cerr << "- construct BinaryObjectInputStream\n";
    BinaryObjectInputStream oin(&mbin);
    
    //cerr << "- read object\n";
    ValueBase_var request;
    if (oin.readObject(&(request.inout()))!=STATUS_OK ||
	oin.getStatus()!=STATUS_OK) {
      cerr << "ERROR: TraderServer failed to read received multicast object\n";
      continue;
    }
    if (request.in()==NULL) {
      cerr << "Warning: TraderServer read null multicast object\n";
      continue;
    }
    //cerr << "TraderServer received and read multicast request...\n";

    //------------------------------------------------------------
    // process request

    cerr << "- read object (class " << request->getClassName() << ") ok\n";
    ValueBase_var reply;

    TraderLookup* lookupRequest = TraderLookup::_downcast(request.in());
    if (lookupRequest!=NULL) {
      reply = lookup(lookupRequest->name, 
		     lookupRequest->classname);
    } else {
      TraderRebind* rebindRequest = TraderRebind::_downcast(request.in());
      if (rebindRequest!=NULL) {
	PRBool reply_flag = PR_FALSE;
	if (rebindRequest->replaceFlag) {
	  reply_flag = rebind(rebindRequest->name, 
			      rebindRequest->binding.in());
	}
	else {
	  reply_flag = bind(rebindRequest->name, 
			    rebindRequest->binding.in());
	}
	TraderRebindReply* repobj = new TraderRebindReply();
	repobj->okFlag = reply_flag;
	reply = repobj;
      }
      else {
	cerr << "ERROR: TraderServer: unknown (multicast) request type " 
	     << request->getClassName() << "\n";
	continue;
      }
    }

    //------------------------------------------------------------
    // make response - to sending address

    MbufOutputStream mout(65535, sizeof(UDPPacketHeader));

    BinaryObjectOutputStream oout(&mout);
    oout.writeObject(reply.in());
    oout.flush();
    if (oout.getStatus()!=STATUS_OK) {
      cerr << "ERROR: TraderServer failed on object write (multicast)\n";
      continue;
    }

    Mbuf_var response_mbuf = mout.takeMbuf();
    if (response_mbuf.in()==NULL) {
      cerr << "INTERNAL ERROR: TraderServer (multicast) "
	   << "writeObject failed to return an Mbuf\n";
      continue;
    }
    Mbuf_var mbuf = response_mbuf;
    PRUint32 src_frag = 0;
    
    cerr << "- sending response direct to client\n";

    while (mbuf.in()!=NULL) {
      // fill in header
      char *buf = mbuf->getBuf();
      UDPPacketHeader *header = (UDPPacketHeader*)buf;
      header->magic[0] = UDPPacketHeader_magic_0;
      header->magic[1] = UDPPacketHeader_magic_1;
      header->magic[2] = UDPPacketHeader_magic_2;
      header->version = UDPPacketHeader_version;
      header->src_addr = PR_htonl(mc_socket->getLocalAddress());
      header->src_port = PR_htons(mc_socket->getLocalPort());
      header->res0 = 0;
      header->src_sid = PR_htonl(session_id++);
      header->src_mid = PR_htonl(1);
      header->src_frag = PR_htonl(src_frag++);
      
      Mbuf_var next = mbuf->getNext();
      if (next.in()==NULL)
	// msb set to mark end of message
	header->src_frag = PR_htonl(PR_ntohl(header->src_frag) | 0x80000000);
      
      packet->setData(mbuf.in());
      
      if (mc_socket->send(packet.in(),1)!=STATUS_OK) {
	cerr << "ERROR: sending packet in ProxyDelegate::doSimpleRpcMulticast\n";
      }
      
      mbuf = next;
    }
  }
  while(PR_TRUE);
*/
    }

    public static void main( String args[] ) {
      if (args.length>1) {
	System.err.println("Usage: equip.net.Trader [port]");
	System.exit(-1);
      }
      int port=DEFAULT_TRADER_PORT.value;
      if (args.length>0) {
	try {
	  port = Integer.valueOf(args[0]).intValue();
	} catch (Exception e) {
	  System.err.println("ERROR reading port number ("+args[0]+"): "+e);
	  System.exit(-1);
	}
      }
      new Trader((short)port);
    }

  public Moniker getMonikerDefault() {
      SimpleTCPMoniker mon = new SimpleTCPMonikerImpl();
      mon.initFromPort((short)DEFAULT_TRADER_PORT.value);
      return mon;
  }
  public Moniker getMonikerFromPort(short port) {
      SimpleTCPMoniker mon = new SimpleTCPMonikerImpl();
      mon.initFromPort(port);
      return mon;
  }
  public Moniker getMonikerFromHost(String host, short port) {
      SimpleTCPMoniker mon = new SimpleTCPMonikerImpl();
      mon.initFromHost(host, port);
      return mon;
  }
  public Moniker getMonikerFromAddr(int addr, short port) {
      SimpleTCPMoniker mon = new SimpleTCPMonikerImpl();
      mon.initFromAddr(addr, port);
      return mon;
  }
};
