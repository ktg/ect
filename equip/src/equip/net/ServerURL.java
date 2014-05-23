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
// ServerURL.java
// Chris Greenhalgh 28/03/01

package equip.net;

/** Class for holding and parsing a single EQUIP URL.
 *
 * <b>Note:</b> defined independently in both C++ and Java, but not
 * IDL'd. <P>
 *
 * The URL is parsed to form a {@link Moniker}, that can then be 
 * resolved and used to contact the server. The current URL schemes are:
 * <ul>
 *
 * <li><code>equip:</code> (default) - creates a {@link SimpleTCPMoniker}
 * for communication with the server over a TCP connection (e.g. 
 * {@link ConnectionSapTcp}).
 *
 * <li><code>equipu:</code> - creates a {@link SimpleUDPMoniker} for
 * communication with the server over a JCP (reliable UDP) connection
 * (i.e. {@link ConnectionSapJcp}).
 *
 * <li><code>equipm:</code> - creates a {@link MulticastUDPMoniker}
 * for multicast-based server discovery. This is now obsoleted by
 * {@link equip.discovery}.
 *
 * </ul>
 * In all cases (currently), if a value is specified for the URL
 * path (e.g. as in <code>equip://host:port/path</code>) then
 * the above moniker will be used to determine how to contact a
 * {@link TraderProxy} service, and the <code>path</code> will
 * be passed to the trader service in a lookup operation; the 
 * information returned from the trader will then be used to 
 * contact the actual server.<P>
 *
 * Consequently, the default <code>port</code> for an EQUIP URL
 * is {@link DEFAULT_TRADER_PORT#value}, and should normally be
 * used only to communicate with trader processes.<P>
 *
 * The default <code>host</code> is an IP address of the local host as
 * returned by {@link java.net.InetAddress#getLocalHost}. The loopback
 * address is not used since this results in non-global URLs and
 * monikers. Of course, IP addresses are not global if you cross a NAT
 * firewall...<P>
 */
public class ServerURL {
  /** The moniker that the URL has parsed into */
  private Moniker moniker;
  
  /** Create by parsing the supplied string as an EQUIP URL. */
  public ServerURL(String url) {
    String scheme, host, port, name;
    int p;
    // scheme:
    scheme = url;
    p = 0;
    for (; p<url.length() && url.charAt(p)!=':'; p++);
    if (p>=url.length()) {
      // no scheme
      scheme = "equip";
      p = 0;
    } else {
      scheme = url.substring(0,p);
      p++;
    }
    // //host:port/
    if (p+2<=url.length() && url.charAt(p)=='/' && 
	url.charAt(p+1)=='/') {
      p += 2;
    }
    int hostBegin = p;
    for (; p<url.length() && url.charAt(p)!=':' && 
	   url.charAt(p)!='/'; p++);
    if (hostBegin==p)
      // default
      host = null;
    else
      host = url.substring(hostBegin,p);
    port = null; // default...
    if (p<url.length() && url.charAt(p)==':') {
      // port
      p++;
      int portBegin = p;
      for (;p<url.length() && url.charAt(p)!='/'; p++);
      port = url.substring(portBegin,p);
    }
    if (p<url.length() && url.charAt(p)=='/') {
      // name
      p++;
      name = url.substring(p);
    } else
      name = null;
    
    short port_no = (short)DEFAULT_TRADER_PORT.value;
    if (scheme.equals("equip")) {
	SimpleTCPMoniker tcp = new SimpleTCPMonikerImpl();
	if (port!=null) 
	    try {
		port_no = (short)Integer.parseInt(port);
	    } catch (Exception e) {
		System.err.println("Parsing url port "+port+": "+e);
		port_no = 0;
	    }
	if (host==null)
	    tcp.initFromPort(port_no);
	else
	    tcp.initFromHost(host, port_no);
	moniker = tcp;
    } else if (scheme.equals("equipu")) {
	SimpleUDPMoniker tcp = new SimpleUDPMonikerImpl();
	if (port!=null) 
	    try {
		port_no = (short)Integer.parseInt(port);
	    } catch (Exception e) {
		System.err.println("Parsing url port "+port+": "+e);
		port_no = 0;
	    }
	if (host==null)
	    tcp.initFromPort(port_no);
	else
	    tcp.initFromHost(host, port_no);
	moniker = tcp;
    } else if (scheme.equals("equipm")) {
	MulticastUDPMoniker tcp = new MulticastUDPMonikerImpl();
	if (port!=null) 
	    try {
		port_no = (short)Integer.parseInt(port);
	    } catch (Exception e) {
		System.err.println("Parsing url port "+port+": "+e);
		port_no = 0;
	    }
	if (host==null) {
	    // default multicast address?!
	    host = "224.1.2.3";
	    System.err.println("WARNING: using default multicast group " 
			       + host);
	}
	tcp.initFromHost(host, port_no);
	moniker = tcp;
    } else {
	System.err.println("ERROR: ServerURL does not understand scheme "+
			   scheme+" (url "+url+")");
	return;
    }
    
    if (name==null || name.length()==0) {
      if (port_no==(short)DEFAULT_TRADER_PORT.value) {
	System.err.println("Warning: ServerURL non-trader moniker "+
			   "defaults to trader port");
      }
    } else {
      TraderMoniker tmon = new TraderMonikerImpl();
      tmon.trader = moniker;
      tmon.classname = null;
      tmon.name = name;
      moniker = tmon;
    }
  }
  /** Create directly from a {@link Moniker} */
  public ServerURL(Moniker moniker) {
    this.moniker = moniker;
  }

  /** Get the created {@link Moniker}, which can be resolved and
   * used to locate the service. */
  public Moniker getMoniker() {
    return moniker;
  }
  /** From the parsed {@link Moniker}, work back to an equivalent
   * EQUIP URL.
   *
   * Since the URL is not kept as a string, this may be different
   * from the value originally specified.
   *
   * @return null if the attempt failed */
  public String getURL() {
    StringBuffer buf = new StringBuffer();
    if (moniker==null) {
      System.err.println("Warning: ServerURL::getURL called with null moniker");
      return null;
    }
    int addr=0;
    short port=0;
    Moniker mon = moniker;
    String scheme = "equip";
    TraderMoniker tmon = null;
    if (moniker instanceof TraderMoniker) {
      tmon = (TraderMoniker)moniker;
      mon = tmon.trader;
    }
    if (mon instanceof SimpleTCPMoniker) {
	SimpleTCPMoniker tcp = (SimpleTCPMoniker)mon;
	addr = tcp.addr;
	port = tcp.port;
	scheme = "equip";
    } else if (mon instanceof SimpleUDPMoniker) {
	SimpleUDPMoniker tcp = (SimpleUDPMoniker)mon;
	addr = tcp.addr;
	port = tcp.port;
	scheme = "equipu";
    } else if (mon instanceof MulticastUDPMoniker) {
	MulticastUDPMoniker tcp = (MulticastUDPMoniker)mon;
	addr = tcp.addr;
	port = tcp.port;
	scheme = "equipm";
    } else {
      System.err.println("Warning: ServerURL::getURL called with "+
			 "unknown moniker type ("+
			 mon.getClass().getName()+")");
      return null;
    }
    buf.append(scheme);
    buf.append("://");
    buf.append((addr >> 24) & 0xff);
    buf.append('.');
    buf.append((addr >> 16) & 0xff);
    buf.append('.');
    buf.append((addr >> 8) & 0xff);
    buf.append('.');
    buf.append((addr) & 0xff);
    buf.append(':');
    buf.append(((long)port) & 0xffff);
    buf.append('/');
    if (tmon!=null)
      buf.append(tmon.name);

    return buf.toString();
  }
}

    
