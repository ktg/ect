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
/* SimpleUDPMoniker 
 * autogenerated from ../../include/eqNetTypes.idl
 * by eqidl
 * DO NOT MODIFY
 */


package equip.net;

import equip.runtime.*;
import java.net.*;

/** Implementation of IDL'd class {@link SimpleUDPMoniker} */
public class SimpleUDPMonikerImpl extends SimpleUDPMoniker {
  /* lifecycle */
  public SimpleUDPMonikerImpl()
  {
	this.encoding = Encoding.EQENCODE_EQ_OBJECT_PACKET;
  }

  /* API */
  public void initFromPort(short port) {
      String host = null;
      try {
	  host = System.getProperty("LOCALHOST");
	  if (host==null) 
		  host = ServerSap.rewriteLocalAddress(InetAddress.getLocalHost()).getHostAddress();
	  } 
	  catch(Exception e) 
	  {
	  System.err.println("Could not get local host name: "+e);
	  e.printStackTrace(System.err);
	  initFromAddr(0, port);
	  return;
      }
      initFromHost(host, port);
  }
  public void initFromHost(String host, short port) {
      InetAddress addr = null;
      try {
	  addr = InetAddress.getByName(host);
      } catch(Exception e) {
	  System.err.println("Could not get ip for host name "+host+": "+e);
	  e.printStackTrace(System.err);
	  initFromAddr(0, port);
	  return;
      }
      byte as [] = addr.getAddress();
      if (as.length!=4) {
	  System.err.println("Got ip address with "+as.length+" bytes - "+
			     "expected 4");
	  initFromAddr(0, port);
	  return;
      }
      int a = (((int)as[3]) & 0xff) |
	  ((((int)as[2])  << 8) & 0xff00) |
	  ((((int)as[1])  << 16) & 0xff0000) |
	  ((((int)as[0])  << 24) & 0xff000000);
      initFromAddr(a, port);
  }
  public void initFromAddr(int addr, short port) {
      this.addr = addr;
      this.port = port;
      this.encoding = Encoding.EQENCODE_EQ_OBJECT_PACKET;
  }
  /* subclasses....*/


/* subclass SimpleMoniker */


/* subclass Moniker */
  public SimpleMoniker resolve() {
    return this;
  }


} /* class SimpleUDPMoniker */

/* EOF */
