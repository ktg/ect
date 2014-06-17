/*
 * $Id: _socket.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
 *
 * Copyright (c) 2004
 * Telecooperation Office (TecO), Universitaet Karlsruhe (TH), Germany.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above
 *    copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 * 3. Neither the name of the Universitaet Karlsruhe (TH) nor the names
 *    of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Author(s): Max Laier <mlaier@freebsd.org>
 *	      Michael Biebl <biebl@teco.edu>
 *	      Thomas Morper <morper@teco.edu>
 */


/** @file
 *
 * This file contains the socket-structure.
 */

/**
 * @ defgroup SOCKET_STRUCT socket.c: definition of the struct p_socket
 */

#if _HAS_SYS_QUEUE_H_
#include <sys/queue.h>
#else
#include "tqueue.h"
#endif

#include "conf.h"

TAILQ_HEAD(p_socketlist, p_socket);
extern struct p_socketlist p_allsockets;

//int _send_ack(int fd_out, struct p_packet * p);

/**@{*/

/** @brief Structure for a socket */
struct p_socket {
	/** Pointer to the socket */
	int fd;
	/** Port number */
	uint16_t portnum;
	/** Address, from which packets will be recieved
	 * @sa p_socket_set_ipsrc
	 */
	struct sockaddr_in *bind_addr;
	/** Address, to which the packets will be send
	 * @sa p_socket_set_ipdest
	 */
	struct sockaddr_in  dest_addr;
	/** Number of retries
	 * @sa p_socket_set_retry
	 * @sa p_socket_send_acked
	 */
	int	ack_retry;
	/** Timeout
	 * @sa p_socket_set_retry
	 * @sa p_socket_send_acked
	 */
	uint32_t ack_to;
	/** Blocking-mode (0 or 1)
	 * @sa p_socket_set_blocking
	 */
	int	blocking:1;
	/** Socket already bound? (0 or 1) */
	int	bound:1;
	/** Send to bradcast? (0 or 1)
	 * @sa p_socket_set_broadcast
	 */
	int	broadcast:1;
	/** Send ack automatic? (0 or 1)
	 * @sa p_socket_set_autoack
	 * @sa p_socket_send_acked
	 */
	int	autoack:1;

	/** Net-ID of this particle */
	uint8_t my_id[8];
	/** Location-ID of this particle */
	uint8_t my_loc[44];
	/** Last sequence Number */
	uint8_t last_seq;

	/** Recieve-Option for this socket (0 or 1)
	 * @sa p_socket_set_recv_option
     */
	uint8_t socket_recv_option;
	/** Fragmentation-Option for this socket (0 or 1)
	 * @sa p_socket_set_frag_option
     */
	uint8_t socket_frag_option;

	TAILQ_ENTRY(p_socket) entries;
};


/**@}*/

