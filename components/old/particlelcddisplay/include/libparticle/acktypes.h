/*
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
 */


/**
* This File describes the standard types used for send_acknowledged.
*
* Important note: The defined types are mandatory!!!
*
* TecO, University of Karlsruhe, Germany
* http://www.teco.edu
* 2005/02/10
*
*	changes:
*	-------
*/

#ifndef __ACKTYPES_H__
#define	__ACKTYPES_H__			003

typedef 
union
{
  struct 
  {
    unsigned is_ack:1; /** if set CAC-tuple is reply to an ack request */
    unsigned type:7;   /** see types below */
  }; 
  uint8_t raw;
}
ACLACKData;

/********* Types*******/

/** CAC-tuple will be auto-acked on receive*/
#define	ACL_ACK_TYPE_AUTO		0x00
/** CAC-tuple is fragmentation sequence. */
#define	ACL_ACK_TYPE_FRAG_DATA		0x01
/** CAC-tuple is end of fragmentation.*/
#define	ACL_ACK_TYPE_FRAG_LASTDATA	0x02

#endif
