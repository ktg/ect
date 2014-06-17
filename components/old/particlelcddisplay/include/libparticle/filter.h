/*
 * $Id: filter.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 *			  Till Riedel <riedel@teco.edu>
 *			  Thomas Morper <morper@teco.edu>
 */

/** @file
 *
 * This file contains some useful utils.
 */

/**
 * @defgroup FILTER filter.c: Functions for filtering data
 */

#ifndef _FILTER_H_
#define _FILTER_H_

#include "conf.h"
#include "_filter_enum.h"

#if _HAS_SYS_QUEUE_H_
#include <sys/queue.h>
#else
#include "tqueue.h"
#endif

#define NONAME

struct p_filterlist
{
  TAILQ_HEAD(NONAME, p_fchain) head;
};

extern struct p_filterlist pfl;

struct p_fchain
{
	int			key;
	const char	*name;
    TAILQ_HEAD	(NONAME, p_filter)	head;
	TAILQ_ENTRY	(p_fchain)		link;
};


/*@{*/

/** @brief Structure for a filter */
struct p_filter {
	/** Type of the filter */
	int	  type;
	/** length of data */
	size_t	  data_len;
	TAILQ_ENTRY(p_filter) link;
	/** data of the filter, depending on the type.
	 * An acl-type / location-ID / ... which should be filtered.
	 */
	uint16_t *data;
};

struct p_packet;

/** @brief Create a new filter
 *
 * Creates a new Filter. The filter will be initialized with "AND 1",
 * so it will return true for every packet until no other conditions have been added.
 * @param filter_name Name of the new filter, only used by the
 * p_filter_describe() function.
 * @return pointer to the new filter on success, -1 on error
 */
LIBPARTICLE_API int p_filter_create(const char * filter_name);

/** @brief destroy a filter
 * Destroys a filter and frees memory allocated for internal structures.
 * @param filter Pointer to the filter
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_filter_destroy(int filter);

/** @brief Adds a new condition to a filter
 *
 * You need to specify the condition by using a combination of the FILTER_ 
 * constants and the right arg which is of FILTER_TYPE_*. For example adding 
 * a condition which will match all packets which are not from the particle with
 * ID "10.0.0.0.18.0.0.27", requires executing this commands:
 * 
 * @code
 * uint8_t id[8] = { 10, 0, 0, 0, 18, 0, 0, 27 };
 * p_filter_add(filter_nr, FILTER_TYPE_ID|FILTER_MODE_NOT|FILTER_CONCAT_AND, id); 
 * @endcode
 *
 * Please note: The first condition which is added to a filter must be concated by AND.
 * As every filter is initialized with "TRUE", concating the first condition with OR would result
 * in "TRUE OR condition" which is always TRUE. Cause of this, p_filter_add will return -1, if the
 * first condition is added with OR!
 *
 * p_filter_add can also combine 2 existing filters.
 * @code
 * p_filter_add(filter_nr, FILTER_TYPE_FILTER|FILTER_CONCAT_AND, p_filter_arg(filter_nr2)); 
 * @endcode
 * The added filter (in this example, filter_nr2) will be destroyed while adding it into the first filter.
 * If you try to free filter_nr2 after adding it into filter_nr, it will result in an error!
 *
 * @param filter pointer to the filter, to which the new condition should be added
 * @param type a combination of the FILTER_ Constants.
 * @param arg the argument for the given FILTER_ type.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_filter_add(int filter, int type, void * arg);

/** @brief Applys a filter onto a packet
 *
 * @param filter Filter that should be applyed
 * @param packet CL-packet that should be tested
 * @return 1 if the filter applies to the packet, otherwise 0; -1 on error.
 */
LIBPARTICLE_API int p_filter_apply(int filter, struct p_packet * packet);

/** @brief Prints out all informations about a filter.
 *
 * Debug function, which prints out all internal informations of filter to stdout.
 * @param filter Filter that should be described
 */
LIBPARTICLE_API void p_describe_filter(int filter);

/** @brief Get a pointer for a filter
 *
 * Little util which is only needed when adding one filter into another.
 *
 * @param filter Filter, for which the pointer should be get
 *
 * @sa: p_filter_add
 */
LIBPARTICLE_API void *p_filter_arg(int filter);

/*@}*/

struct p_fchain_head* _p_fchain_get (int id);

#endif /* _FILTER_H */
