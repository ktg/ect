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
/* SapConstants.java
   Chris Greenhalgh
   20/9/2001 
*/
package equip.net;

/** Various constants used in {@link ServerSap}, 
 * {@link ConnectionSap} and sub-classes. */
public class SapConstants {

    /** default input/output buffer size ({@value}).
     *
     * a small buffer will give better local effective suppression of
     * unreliable update cascades  */
    public final static int DEFAULT_BUF_SIZE = 64000;

    /**  Default receive timeout ({@value}); 0=blocking */
    public final static int DEFAULT_RECV_TIMEOUT_MS = 0;

    /** Default keep-alive message send interval, milliseconds
     * ({@value}) */
    public final static int DEFAULT_KEEP_ALIVE_TIME_MS = 5000;

    /** Default slow response time, milliseconds ({@value}) */
    public final static int DEFAULT_SLOW_RESPONSE_TIME_MS = 2000;

    /** TCP slow response time, ms ({@value}).  
     *
     * TCP does not ACK by default, so waiting for keep alives in
     * worst case */
    public final static int DEFAULT_SLOW_RESPONSE_TIME_MS_TCP = 
	(DEFAULT_KEEP_ALIVE_TIME_MS+DEFAULT_SLOW_RESPONSE_TIME_MS);

    /** UDP slow response time, ms ({@value}).
     *
     * UDP should ACK */
    public final static int DEFAULT_SLOW_RESPONSE_TIME_MS_UDP =
	DEFAULT_SLOW_RESPONSE_TIME_MS;

    /** Default channel probing time, ms (UDP?!) ({@value}) */
    public final static int DEFAULT_PROBING_INTERVAL_MS = 2000;

    /** Connection timeout, ms (not configurable for tcp)
     * ({@value}) */
    public final static int DEFAULT_CONNECTION_TIMEOUT_MS = 30000;

    /** Alternative connection timeout for 'long' timeouts, e.g.
     * intermittantly connected clients, ms ({@value}). 
     *
     * VERY LONG VALUE FOR WIRE EXPERIMENTS - SHOULD BE SET
     * BETTER/DYNAMICALLY!
     */
    public final static int DEFAULT_LONG_CONNECTION_TIMEOUT_MS = 30*60*1000;

    /** ack every packet?? (JCP only, I think).
     *
     * tentative workaround for 0.5 second (retrans.) stall when flow
     * control bites */
    public final static boolean ACK_EVERY_PACKET = true;
}

