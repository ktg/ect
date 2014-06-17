/*
 * $Id: libparticle.h,v 1.1 2005/05/03 11:54:38 cgreenhalgh Exp $
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

/* BEGIN: DEBUG, only needed for the uni-test */
#define DEBUG 1 /* DEBUG=1 to get the function debug_get_socket()
				   DEBUG=0 for the final release */
#ifdef DEBUG
struct p_socket *d_get_socket(int);
#endif
/* END: DEBUG */

/** @file
 *
 * This file contains the interface for programmers using this library.
 */

#ifndef _LIBPARTICLE_H_
#define _LIBPARTICLE_H_

#include "libparticle/conf.h"
#include "libparticle/util.h"
#include "libparticle/filter.h"
#include "libparticle/frag.h"
#include "libparticle/packet.h"
#include "libparticle/socket.h"
#include "libparticle/acktypes.h"
#include "libparticle/error.h"

/** @mainpage
 *
 * This library simplifies the programming of particles, also known as smart-its,
 * a technology developed at the TecO.
 * You can visit the corresponding website http://particle.teco.edu or http://www.teco.edu
 * for further information.
 * New releases of this software can be found on http://particle.teco.edu/developer/index.html
 *
 * @version 0.4.1
 * @author Max Laier <mlaier@freebsd.org>
 * @author Michael Biebl <biebl@teco.edu>
 * @author Thomas Morper <morper@teco.edu>
 * @author Philipp Scholl <scholl@teco.edu>
 * @author Till Riedl <riedl@teco.edu>
 *
 *
 *
 * Build instructions for Microsoft Windows:
 *
 * You may compile all projects by opening the "libparticle.dsw" in Visual Studio 6 or "libparticle.sln" in Visual Studio .NET. Make a batch build to compile all necessary files.
 * As a result the library "particle32.dll" will be compiled as Debug and Release version and can be found in the corresponding directory. If you want to compile and link your own programs you need the file "particle32.lib" too.
 *
 *
 * Build instructions for Unix/Linux/etc:
 *
 * On Unix-like platforms compilation can be done by the standard ./configure && make && make install chain.
 *
 */

#endif /* _LIBPARTICLE_H_ */
