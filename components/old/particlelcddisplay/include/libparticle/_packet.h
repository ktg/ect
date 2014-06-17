/*
 * $Id: _packet.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 *			  Thomas Morper <morper@teco.edu>
 */

/** @file
 *
 * This file contains definitions of the structs p_packet and p_acl_tuple.
 */

/**
 * @ defgroup PACKET_STRUCT _packet.c: Definition of the structs p_packet and p_acl_tuple
 */


#include "conf.h"

#if _HAS_SYS_QUEUE_H_
#include <sys/queue.h>
#else
#include "tqueue.h"
#endif

TAILQ_HEAD(p_acl_list, p_acl_tuple);

#define PKT_SIZE_MAX		311	/* 255 + CL (1+44+8+1) = 311 */
#define PKT_SIZE_MIN		59	/* CL + 1 ACL + CRC */
#define PKT_CL_SIZE		54	/* 1 + 44 + 8 + 1 */


#if !defined(_CL_FORMAT_VERSION) || (_CL_FORMAT_VERSION == 2)
#define PKT_CL_VERSION		2
#define PKT_ACL_HEAD		3
#elif _CL_FORMAT_VERSION == 3
#define PKT_CL_VERSION		3
#define PKT_ACL_HEAD		4
#endif

#define PKT_CAD_OVERHEAD    (PKT_ACL_HEAD + 8) 
#define PKT_CAC_OVERHEAD    (PKT_CAD_OVERHEAD + PKT_ACL_HEAD + 2  )

#define PKT_ACL_SIZE_MAX	64
#define PKT_ACL_TUPLE_MAX	64


#define PKT_INIT_VERSION	0x0001
#define PKT_INIT_LOCATION	0x0002
#define PKT_INIT_SRC_ID 	0x0004
#define PKT_INIT_SEQ		0x0008
#define PKT_INIT_MASK		0x000F

#define PKT_CRC_OK			0x0010
#define PKT_CRC_DONE		0x0020
#define PKT_CRC_FAIL		0x0040
#define PKT_CRC_MASK		0x00F0

#define PKT_ACK_REQ			0x0100
#define PKT_ACK_DONE		0x0200
#define PKT_ACK_NACK		0x0400
#define PKT_ACK_MASK		0x0F00

#define PKT_ACL_DONE		0x1000
#define PKT_FRAGMENT		0x2000

#define PKT_NEW_FLAGS		0x0000

/** @brief Structure for an ACL tupel consisting of type, length and data */
struct p_acl_tuple
{
	/** Type of the tupel defined as 2 byte tupel.
	 * @sa acltypes.h
	 */
	uint8_t acl_type[2];
	/**  length of data */
#if !defined(_CL_FORMAT_VERSION) || (_CL_FORMAT_VERSION == 2)
	uint8_t acl_len;
#elif _CL_FORMAT_VERSION == 3
	uint16_t acl_len;
#endif
	/** data */
	uint8_t *acl_data;
	TAILQ_ENTRY(p_acl_tuple) entries;
};


/** @brief Structure for a packet */
struct p_packet
{
    /** 1 byte for the flags of the packet */
	int	p_flags;
	/** destination of the packet
	 * @sa: p_socket_set_ipdest
	 */
	uint8_t p_dest_id[8];
	/** acking sequence-number */
	uint8_t p_ack_seq;

	struct sockaddr p_bgd_peer;

	socklen_t	p_bgd_len;

	/** CL HEADER START */

	/** Version of the CL-Packet */
	uint8_t p_version;
	/** location ID (44 Byte) */
	uint8_t p_location[44];
	/** Source ID (8 Byte) */
	uint8_t p_src_id[8];
	/** Sequence number */
	uint8_t p_seq;

	/** CL HEADER END */

	/** size of the ACL-payload */
	size_t	p_acl_size;
	/** counter for the ACL-touples */
	size_t	p_acl_count;
	/** list of all the ACL-touples */
	struct p_acl_list p_acl_list;
	/** pointer to the next ACL-touple */
	struct p_packet *p_nextpkt;
	/** pointer to the first ACL-touple */
	struct p_packet *p_first;
};
