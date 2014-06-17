/*
 * $Id: conf.h,v 1.1 2005/09/29 12:36:19 janhumble Exp $
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
 */

#ifndef _CONF_H_
#define _CONF_H_

#ifndef CL_FORMAT_VERSION
#define CL_FORMAT_VERSION	2
#endif

#ifdef WIN32
typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;

typedef u_long in_addr_t;
typedef int socklen_t;

#define snprintf _snprintf
#else
#include <stdint.h>

#endif /* WIN32 */

struct in_addr;

// definitions to declare dll exports/imports
#ifdef WIN32
  #ifdef __cplusplus
    #ifdef _EXPORTING
      #define LIBPARTICLE_API extern "C" __declspec( dllexport )
    #else
      #define LIBPARTICLE_API extern "C" __declspec( dllimport )
    #endif
  #else
    #ifdef _EXPORTING
      #define LIBPARTICLE_API __declspec( dllexport )
    #else
      #define LIBPARTICLE_API __declspec( dllimport )
    #endif
  #endif
#else
  #define LIBPARTICLE_API
#endif

#endif /* _CONF_H_ */
