/*
 * $Id: frag.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 * This file contains functions for transfering large files
 */

/**
 * @defgroup FRAGMENTATION frag.c: Functions for transfering large files (p_frag_*)
 */
#ifndef __FRAG_H__
#define __FRAG_H__ 1
#define FRAG_ZEROFLAG	0x00

#define FRAG_ACTIVE	0x00
#define FRAG_FINISHED	0x01
#define FRAG_ABORTED	0x02

#define FRAG_STATEFLAG	0x03

#define FRAG_SENDER	0x00
#define FRAG_RECIEVER	0x04
#define FRAG_ROLEFLAG	0x04

/*@{*/


/** @brief Inits a new fragmentation, this computer will be the reciever of the frag.
 *
 * Before starting a Fragmentation, you must set the frag_option of the recieving socket to 1.
 *
 * @param fd_out Socket where the ack-packets will be send on.
 * @param first_data Pointer to the first packet of this fragmentation, which was recieved from the sender.
 * @return 1 on success, -1 on error.
 * @sa p_socket_set_frag_option
 * @sa p_socket_send_file
 */
LIBPARTICLE_API int p_frag_init_recv(int fd_out, struct p_packet * first_data);

/** @brief Inits a new fragmentation, this computer will be the sender of the frag.
 *
 * You must set the socket frag-option of the recieving socket to 1 before starting a fragmenation.
 * Socket_frag_option will be set to 1 automaticly when using p_socket_send_file or p_socket_send_acked
 *
 * @param fd_out Socket where all packets of this frag will be sended
 * @param partner_add Pointer to the address of the particle, where the data will be send to. Must be 8 * 0-255
 * @param data Pointer to the data, which should be send.
 * @param len Lenght of data
 * @param acl_type Pointer to an field which holds an acl-type (2*0-255). This acl-type will be the subject of all packets for this fragmentation.
 * @return 1 on success, -1 on error.
 * @sa p_socket_set_frag_option
 * @sa p_socket_send_file
 */
LIBPARTICLE_API int p_frag_init_send(int fd_out, uint8_t * partner_add, uint8_t * data, uint32_t len, uint8_t *acl_type);

/** @brief Gets all information about one fragmentation, specified by the partern-address
 *
 * This function will return a pointer to a struct, which holds all the informations about this fragmentation
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag we are interested in.
 * @return NULL on error, Pointer to the struct on success.
 */
LIBPARTICLE_API struct p_frag * p_frag_get_frag(uint8_t * partner_add);

/** @brief Gets the status-flags of one fragmentation.
 *
 * This function will return an integer which includes the specified status-flags.
 *
 * the following status-flags are in use:
 * FRAG_STATEEFLAG: FRAG_ACTIVE, FRAG_FINISHED, FRAG_ABORTED
 * FRAG_ROLEFLAG: FRAG_SENDER, FRAG_RECIEVER
 *
 * The following code will test, if this computer is the sender of the fragmentation specified by loc:
 * @code
 * p_frag_get_frag(loc, FRAG_ROLEFLAG)
 * @endcode
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag we are interested in.
 * @param type Type of Flag you want to check.
 * @return integer with the recomended status-flag on succes, -1 on error
 */
LIBPARTICLE_API int p_frag_get_status(uint8_t * partner_add, uint16_t type);

/** @brief Gets the length of the data-field from one fragmentation.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag we are interested in.
 * @return uint32_t-integer with length of the data stored in this fragmentation
 */
LIBPARTICLE_API uint32_t p_frag_get_data_length(uint8_t * partner_add);

/** @brief Gets the data from a fragmentation.
 *
 * This function copys the data, which is stored in the data-field of the fragmentation specified by partner_add
 * into the buffer, where data points to.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag we are interested in.
 * @param data Address of the buffer, where the data will be copyed to.
 * @return length of the data on success, -1 on error.
 */
LIBPARTICLE_API uint32_t p_frag_get_data(uint8_t * partner_add, uint8_t ** data);

/** @brief Sets the status of a fragmentation to FRAG_ABORTED
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag which should be aborted
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_frag_abort(uint8_t * partner_add);

/** @brief Deletes a fragmentation
 *
 * This function deletes all informations about a fragmentation specified by partner_add.
 * Note: This informations will only get deleted, if status is set to FRAG_ABORTED!
 * Note also: as p_frag_get_data retruns you a pointer to the data field of the fragmentation and this field will get freed by using p_frag_delete, you have to copy this data (e.g. with memcpy) if you still want to use it later.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag which should be aborted
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_frag_delete(uint8_t * partner_add);

/** @brief Marking the last data-packet of this fragmentation as acked.
 *
 * This function marks the last send data packet of this fragmentation as acknowleged.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for this fragmentation
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_frag_acked(uint8_t * partner_add);

/** @brief Sending the next data-part for a fragmentation
 *
 * This function sends the next data-packet for a fragmentation. It can both be used for resend of the last packet and
 * for sending the next part. If it should send the next part, the last packet hast to be acked with p_frag_acked, otherwise it will be a resend of the last packet.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for the frag which should be aborted
 * @return 1 on success, -1 on error
 * @sa: p_frag_acked
 */
LIBPARTICLE_API int p_frag_next_part(uint8_t * partner_add);

/** @brief Adding new recieved data to the obstack of a fragmentation
 *
 * This function adds the recieved data for a fragmentation to the obstack of this fragmentation. Also, the acknowledge for this packet will be send.
 *
 * @param partner_add Pointer to the address of the particle, which is the partner for this fragmentation
 * @param data Pointer to the recievd packet, which stores the new data.
 * @return 1 on success, -1 on error
 */
LIBPARTICLE_API int p_frag_append(uint8_t * partner_add, struct p_packet * data);

/** @brief Prints out all informations about a fragmentation.
 *
 * Debug function, which prints out all internal informations of fragmentation to stdout.
 * @param fragmentation partner-address of the frag, which should be described.
 */
LIBPARTICLE_API void p_describe_frag(uint8_t * fragmentation);

/*@}*/

#endif //__FRAG_H__

