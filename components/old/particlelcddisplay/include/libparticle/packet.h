/*
 * $Id: packet.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 * This file contains functions to work with the packets.
 */

/**
 * @defgroup PACKET packet.c: Functions for working with the packets (p_pkt_..., p_acl_...)
 */

#ifndef _PACKET_H_
#define _PACKET_H_

/**@{*/

/** @brief Create a new packet
 *
 * Creates a new (empty) Packet.
 * @return pointer to the new packet on success, -1 on error
 */
LIBPARTICLE_API struct p_packet *p_pkt_alloc(void);

/** @brief destroy a packet
 *
 * Destroys a packet and frees memory allocated for internal structures.
 * @param packet Pointer to the packet
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_free(struct p_packet * packet);

/** @brief transforming a buffer into a packet
 *
 * Parses the data of a buffer (for example, after p_revc_raw) into the structure of a packet.
 * @param packet Pointer to the packet, where the parsed data should be stored
 * @param buffer Pointer to the buffer which should be parsed
 * @param len Size of buffer
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_parse(struct p_packet * packet, uint8_t * buffer, size_t len);

/** @brief get location from a packet
 *
 * Gets the location-ID stored in a CL-packet. Output value will be a pointer to an array of integers.
 * @param packet Pointer to the packet, from which the location-ID should be get
 * @return Pointer to the location-ID from packet on success, NULL on error
 */
LIBPARTICLE_API uint8_t *p_pkt_get_location(struct p_packet * packet);

/** @brief Gets the source-ID stored in a CL-packet.
 *
 * @param packet Pointer to the packet, from which the source-ID should be get.  Output value will be a pointer to an array of integers.
 * @return Pointer to the source-ID from packet on success, NULL on error
 */
LIBPARTICLE_API uint8_t *p_pkt_get_srcid(struct p_packet * packet);

/** @brief get seq-# from a packet
 *
 * Gets the sequence-number from a CL-packet.
 * @param packet Pointer to the packet, from which the sequence-number should be get
 * @return sequence-number from packet on success, -1 on error
 */
LIBPARTICLE_API int  p_pkt_get_seq(struct p_packet * packet);

/** @brief get acknowledge-state from a packet
 *
 * Gets the acknowledge-state from a CL-packet.
 * @param packet Pointer to the packet, from which the sequence-number should be get
 * @return 1 if packet was already acknowleged, 0 if not, -1 on error.
 */
LIBPARTICLE_API int p_pkt_get_ack(struct p_packet * packet);

/** @brief get location-ID from a packet as string
 *
 * Gets the location-ID stored in a CL-packet. Output value will be a pointer to a "string" / array of characters
 * @param packet Pointer to the packet, from which the location-ID should be get
 * @param buffer Pointer to the buffer, where the location-ID should be stored
 * @param maxlen Size of the buffer
 * @return pointer to the location-ID from packet on success, NULL on error
 */
LIBPARTICLE_API char *p_pkt_get_location_str(struct p_packet * packet, char * buffer, size_t maxlen);

/** @brief get src-ID from a packet as string
 *
 * Gets the source-ID stored in a CL-packet. Output value will be a pointer to a "string" / array of characters
 * @param packet Pointer to the packet, from which the source-ID should be get
 * @param buffer Pointer to the buffer, where the source-ID should be stored
 * @param maxlen Size of the buffer
 * @return pointer to the source-ID from packet on success, NULL on error
 */
LIBPARTICLE_API char *p_pkt_get_srcid_str(struct p_packet * packet, char * buffer, size_t maxlen);

/** @brief Sets the location-ID of a CL-packet.
 *
 * @param packet Pointer to the packet, where the location-ID should be set
 * @param location Pointer to the location-ID, which should be set at the packet. Must be a 44 byte array
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_set_location(struct p_packet * packet, const uint8_t * location);

/** @brief Sets the source-ID of a CL-packet.
 *
 * @param packet Pointer to the packet, where the source-ID should be set
 * @param source Pointer to the source-ID, which should be set at the packet. Must be a 8 byte array
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_set_srcid(struct p_packet * packet, const uint8_t * source);

/** @brief Sets the seqence-number of a CL-packet.
 *
 * Please note: The flag PKT_INIT_SEQ will be reset after sending a packet. Second sending of the packet will be with the seq-ID from the socket.
 * If you want to send a packet more than one time with a special seq-ID, you have to set it explicit before every sending!
 * @param packet Pointer to the packet, where the seq-number.
 * @param seq The sequence number you wnat to set to the packet.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_set_seq(struct p_packet * packet, const uint8_t seq);

/** @brief Sets the location-ID of a CL-packet with a string
 *
 * @param *packet Pointer to the packet, where the location-ID should be set
 * @param location Pointer to the location-ID, which should be set at the packet. Must be a dotted string with 44 numbers between 0 and 255.
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_set_location_str(struct p_packet * packet, const char * location);

/** @brief Sets the source-ID of a CL-packet with a string
 *
 * dource must be a dotted string including either 8 or 4 numbers between 0 and 255.
 * If source includes 8 numbers, the source ID will be set to exactly this source-ID.
 * If source includes only 4 numbers, the source ID will be set to the ID "1.1.1.1.source".
 * @param *packet Pointer to the packet, where the location-ID should be set
 * @param source Pointer to the source-ID, which should be set at the packet.
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_pkt_set_srcid_str(struct p_packet * packet, const char * source);

/** @brief Prints out all informations about a packet.
 *
 * Debug function, which prints out all informations of packet to stdout.
 * @param packet Packet that should be described
 */
LIBPARTICLE_API void p_describe_pkt(struct p_packet * packet);

struct p_acl_tuple;
/** @brief Find the first ACL-touple in a packet
 *
 * @param packet Pointer to the packet, where the first ACL-touple should be found in.
 * @return Pointer to the first ACL-touple in packet on success, NULL on error or if there isn't any ACL-touple in packet
 */
LIBPARTICLE_API struct p_acl_tuple *p_acl_first(const struct p_packet * packet);

/** @brief Find the next ACL-touple in a packet
 *
 * Find the next ACL-touple in a packet after the current ACL-touple
 * @param packet Pointer to the packet, where the next ACL-touple should be found in.
 * @param acl_touple Pointer to the current ACL-touple
 * @return Pointer to the next ACL-touple in packet on success, NULL on error or if there are no more ACL-touples in packet
 */
LIBPARTICLE_API struct p_acl_tuple *p_acl_next(const struct p_packet * packet, const struct p_acl_tuple * acl_touple);

/** @brief Find the first ACL-touple in a packet by type
 *
 * Find the first ACL-touple in a packet, where the ACL-type is the same as acl_type
 * @param packet Pointer to the packet, where the ACL-touple should be found in.
 * @param acl_type Pointer to the ACL-type, which should be searched for. Must be coded with 3to2
 * @param found_touple Pointer to the adress, where the found ACL-touple should be stored.
 * @return Length of the found touple on success, -1 on error.
 */
LIBPARTICLE_API int p_acl_findfirst(const struct p_packet * packet, const uint8_t * acl_type, struct p_acl_tuple ** found_touple);

/** @brief Find the first ACL-touple in a packet by type, ACL-type as string
 *
 * Find the first ACL-touple in a packet, where the ACL-type is the same as acl_type
 * @param packet Pointer to the packet, where the ACL-touple should be found in.
 * @param acl_type Pointer to the ACL-type, which should be searched for. Must be 3 characters
 * @param found_touple Pointer to the adress, where the found ACL-touple should be stored.
 * @return Length of the found touple on success, -1 on error.
 */
LIBPARTICLE_API int p_acl_findfirst_str(const struct p_packet * packet, const char * acl_type, struct p_acl_tuple ** found_touple);

/** @brief Find the next ACL-touple in a packet by type
 *
 * Find the next ACL-touple in a packet, where the ACL-type is the same as the ACL-type of the current ACL-touple
 * @param packet Pointer to the packet, where the ACL-touple should be found in.
 * @param current_touple Pointer to the current ACL-touple.
 * @param found_touple Pointer to the adress, where the found ACL-touple should be stored.
 * @return Length of the found touple on success, -1 on error.
 */
LIBPARTICLE_API int p_acl_findnext(const struct p_packet * packet, const struct p_acl_tuple * current_touple, struct p_acl_tuple ** found_touple);

/** @brief Adds an ACL-touple to a CL-packet
 *
 * Adds an ACL-touple to a CL-packet. The added ACL-touple will be stored at the end of the CL-packet.
 * @param packet Pointer to the packet, where the ACL-touple should be stored.
 * @param type ACL-type of the touple which should be added to the packet. Must be 2 byte (3-to-2 coded).
 * @param data Data which should be stored in the ACL-touple
 * @param len Length of data
 * @param may_frag if set to 1, this packet will be send fragmented.
 * IMPORTANT: If set to 1, you can only add one single ACL-tuple to this packet and you only can send it with the frag-functions
 * (send_addr and send_ack will initialize them automaticly). Please set mayfrag only to 1, if you really need it (large data in ACL-tuple), otherwise set it to 0.
 * @return -1 on error, 0 otherwise
 */
LIBPARTICLE_API int p_acl_add(struct p_packet * packet, uint8_t * type, uint8_t * data, uint16_t len, int may_frag);

/** @brief Adds an ACL-touple to a CL-packet, ACL-type as string
 *
 * Adds an ACL-touple to a CL-packet. The added ACL-touple will be stored at the end of the CL-packet.
 * @param packet Pointer to the packet, where the ACL-touple should be stored.
 * @param type ACL-type of the touple which should be added to the packet. Must be 3 characters
 * @param data Data which should be stored in the ACL-touple
 * @param len Length of data
 * @param may_frag Reserved for future use with fragmentation. Should always be set to 0 at the moment.
 * @return -1 on error, 0 otherwise
 */
LIBPARTICLE_API int p_acl_add_str(struct p_packet * packet, char * type, uint8_t * data, uint16_t len, int may_frag);

/** @brief Creates a new ACL-touple
 *
 * Creates a new ACL-touple
 * @param type ACL-type of the touple which should be added to the packet. Must be 2 byte (3-to-2 coded).
 * @param data Data which should be stored in the ACL-touple
 * @param len Length of data
 * @param may_frag Reserved for future use with fragmentation. Should always be set to 0 at the moment.
 * @return Pointer to the ACL-touple on success, NULL on error.
 */
LIBPARTICLE_API struct p_acl_tuple *p_acl_new(uint8_t *type, uint8_t * data, uint16_t len, int may_frag );

/** @brief Creates a new ACL-touple, ACL-type as string
 *
 * Creates a new ACL-touple
 * @param type_str ACL-type of the touple which should be added to the packet. Must be 3 characters
 * @param data Data which should be stored in the ACL-touple
 * @param len Length of data
 * @param may_frag Reserved for future use with fragmentation. Should always be set to 0 at the moment.
 * @return Pointer to the ACL-touple on success, NULL on error.
 */
LIBPARTICLE_API struct p_acl_tuple *p_acl_new_str(char *type_str, uint8_t *data, uint16_t len, int may_frag );

/** @brief Get the data, which is stored in an ACL-touple
 *
 * @param acl Pointer to the acl-touple, where the data is stored in.
 * @param data data Pointer, which will be set to the data-field of acl
 * @return Length of data on success, -1 on error.
 */
LIBPARTICLE_API uint8_t p_acl_get_data( struct p_acl_tuple * acl, char ** data) ;

/** @brief Get the ACL-Type of an ACL-touple
 *
 * @param acl Pointer to the acl-touple, where the type is stored in.
 * @return Pointer to the ACL-type of acl on success, NULL on error.
 */
LIBPARTICLE_API uint8_t * p_acl_get_type( struct p_acl_tuple * acl) ;

/** @brief Get the ACL-Type of an ACL-touple as a string
 *
 * @param acl Pointer to the acl-touple, where the type is stored in.
 * @param buf Pointer to a buffer, where the string will be stored in.
 * @return char-pointer to the ACL-type of acl on success, NULL on error.
 */
LIBPARTICLE_API char * p_pkt_get_type_str(struct p_acl_tuple *acl, char *buf);

/**@}*/

#endif /* _PACKET_H_ */
