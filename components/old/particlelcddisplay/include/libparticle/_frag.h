/*
 * $Id: _frag.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 * Author(s): Thomas Morper <morper@teco.edu>
 */


/** @file
 *
 * This file contains the socket-structure.
 */

/**
 * @ defgroup FRAG_STRUCT socket.c: definition of the struct p_chunck and p_frag
 */

#if _HAS_SYS_QUEUE_H_
#include <sys/queue.h>
#else
#include "tqueue.h"
#endif

#define OBSTACK_CHUNK_SIZE 1024
#include "_obstack.h"

#include "conf.h"


TAILQ_HEAD(p_fraglist, p_frag);
extern struct p_fraglist p_allfrags;

/*@{*/


/** @brief Structure for a fragmentation */
struct p_frag
{
	/** Address of the particle, which is the partner for this fragmentation */
	uint8_t partner_address[8];
	/** holds the Flags, in which state this frag currently is and in which role */
	uint16_t state;
	/** complete lenght of the data */
	uint32_t length;
	/** last acked position in the data-field */
	uint32_t current_position;
	/** number of the last send bytes */
	uint8_t	last_send_bytes;
	/** Socket for the outgoing packets */
	int	fd_out;
	/** availible retries for the last packet.*/
	int retries;
	/** Time, when the last packet was send / recieved. */
	uint32_t last_packet;
	/** sequence-number of the last packet */
	uint8_t  sequence;
	/** acl-type, which will be set as the subject of all frag-packets */
	uint8_t  acl_type[2];
	/** Pointer to the buffer, which holds the data */
	uint8_t  *frag_data;
	/** obstack */
	struct _obstack frag_obstack;
	/** Pointer to the next frag */
	TAILQ_ENTRY(p_frag) entries;
};

/**@}*/

