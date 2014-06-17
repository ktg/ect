/*
 * $Id: util.h,v 1.1 2005/05/03 11:54:38 cgreenhalgh Exp $
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
 * This file contains some useful utils.
 */

#ifndef _UTIL_H_
#define _UTIL_H_

#ifndef MIN
#define MIN(a, b)	(((a)<(b))?(a):(b))
#endif

/**
 * @defgroup UTILS util.c: Useful utils for the enduser (p_util_...)
 */

/*@{*/

/** @brief Escaping data
 *
 * Escaping reserved symbols from in_data.
 * @param in_data pointer to the data that should be escaped
 * @param out_data pointer to the escaped result
 * @param len lenght of in_data
 * @param maxlen maximum lenght which is reserved for out_data
 * @return length of out_data
 */
LIBPARTICLE_API int p_util_escape(unsigned char * out_data, const unsigned char * in_data, size_t len, size_t maxlen);

/** @brief Unescaping data
 *
 * Unescaping in_data. Reserved symbols will be put back into the string.
 * @param in_data pointer to the data that should be unescaped
 * @param out_data pointer to the unescaped result
 * @param len lenght of in_data
 * @param maxlen maximum lenght which is reserved for out_data
 * @return length of out_data
 */
LIBPARTICLE_API int p_util_unescape(unsigned char * out_data, const unsigned char * in_data, size_t len, size_t maxlen);

/** @brief Decoding a dotted string to an array of bytes
 *
 * @param out pointer to the byte array where the result is stored
 * @param dotted_str pointer to the dotted string which schould be decoded
 * @param len maximum length of the out array
 * @return length of out
 */
LIBPARTICLE_API int p_util_decode_dot(uint8_t * out, const char * dotted_str, size_t len);

/** @brief decoding 3 characters out of 2 bytes
 *
 * Function to decode three characters out of two bytes.
 * out can be set to NULL if result should only be returned and not stored.
 * @param *acl_type 2 byte Input
 * @param *out pointer, where the decodes 3 characters should be stored
 * @return out
 */
LIBPARTICLE_API char *p_util_acl2str(const uint8_t * acl_type, char * out);

/** @brief coding 3 characters into 2 bytes
 *
 * Function to encode three characters into two bytes.
 * acl_type can be set to NULL if result should only be returned and not stored.
 * @param *in Input string
 * @param *acl_type where 2 byte encoded type is stored
 * @return acl_type
 */
LIBPARTICLE_API uint8_t  *p_util_str2acl(const char * in, uint8_t * acl_type);

/** @brief Encode / Calculate a location-string
 *	
 *	This function takes the information about the root, 4 nodes, x, y and z-values 
 *  and the deviation and calculates a location string which is according to this informations.
 *	A location string has 44 bytes.
 *
 *  The first 16 bytes of the location-string will be the 16 bytes of the root-information.
 *
 *  After that, 6 bytes are reserved for each of the 4 nodes, which have 9 characters and will be 3-to-2 encoded.
 *
 *	Bytes 41, 42 and 43 are for the x, y and z-value, which will be encoded with AB_ENCODE
 *
 *	The last byte is the deviation.
 *  @param root Pointer to the root (16 byte)
 *  @param node1 Pointer to node 1 (9 byte chars)
 *  @param node2 Pointer to node 2 (9 byte chars)
 *  @param node3 Pointer to node 3 (9 byte chars)
 *  @param node4 Pointer to node 4 (9 byte chars)
 *  @param x x-value
 *  @param y y-value
 *  @param z z-value
 *	@param deviation The known deviation of the 3 x-, y- and z-values
 *  @param location Pointer to the place, where the calculated location should be stored.
 */
LIBPARTICLE_API void	p_util_location2raw(uint8_t root[16] , char * node1, char * node2, char * node3, char * node4,
	    int x, int y, int z, uint8_t deviation, unsigned char location[44]);

/** @brief Decoding / parse a location-string
 *	
 *	This function takes a location-string and decodes the stored informations (root, 4 nodes, x, y, z and deviation)
 *  See p_util_location2raw for more information about the location-string and it's calculation.
 *  @param root Pointer to the root (16 byte)
 *  @param node1 Pointer to node 1 (10 byte, 9 for chars + 0-termination)
 *  @param node2 Pointer to node 2 (10 byte, 9 for chars + 0-termination)
 *  @param node3 Pointer to node 3 (10 byte, 9 for chars + 0-termination)
 *  @param node4 Pointer to node 4 (10 byte, 9 for chars + 0-termination)
 *  @param x x-value
 *  @param y y-value
 *  @param z z-value
 *	@param deviation The known deviation of the 3 x-, y- and z-values
 *  @param location Pointer to the lcation-string to be parsed
 *	@sa p_util_location2raw
 */
LIBPARTICLE_API void	p_util_raw2location(uint8_t root[16] , char node1[10], char node2[10], char node3[10],
	    char node4[10], int * x, int * y, int * z, uint8_t * deviation, unsigned char location[44]);

/** @brief: coding strings with 3to2
 *
 *  Function to encode code longer stings with the 3-to-2 coding.
 *  @param *in pointer to the Input string
 *  @param *out pointer to the output (byte-array)
 *  @param *len lenght of the input data.
 *  @return Length of the encoded data
 */
LIBPARTICLE_API int p_util_3to2(uint8_t * out, const char * in, size_t len);

/** @brief: decoding strings with 2to3
 *
 *  Function to decode code longer byte-arrays with the 2-to-3 decoding.
 *  @param *in pointer to the input (byte-array)
 *  @param *out pointer to the output string
 *  @param *len lenght of the input data.
 *  @return Length of the decoded data
 */
LIBPARTICLE_API int p_util_2to3(char * out, const uint8_t * in, size_t len);

 /** @brief Calculates CRC16
  *
  * Calculates CRC16 using the CCITT 16bit algorithm (X^16 + X^12 + X^5 + 1)
  *	@param *data Buffer to calculate CRC for
  *	@param length Length of data in data
  *	@param *phb initial value for the Highbyte
  *	@param *plb initial value for the Lowbyte
  * @return 1 on success, -1 on error
  */
LIBPARTICLE_API int	p_util_crc16(const char * data, size_t length, uint8_t * phb, uint8_t * plb);

 /** @brief Calculates CRC16 and appends the result to data
  *
  * Calculates CRC16 using the CCITT 16bit algorithm (X^16 + X^12 + X^5 + 1).
  * The reuslt will be stored in the last two bytes of data.
  *	@param *data Buffer to calculate CRC for
  *	@param length Length of data in data
  * @return 1 on success, -1 on error
  */
LIBPARTICLE_API int	p_util_crc16_append(char * data, size_t length);

/** @brief Tests whether the CRC is correct
 *
 * @param *data Buffer to be checked
 * @param length Length of data in data
 * @return returns 1 if CRC is OK, 0 if CRC is wrong and -1 on error.
 */
LIBPARTICLE_API int	p_util_crc16_check(char * data, size_t length);

/** @brief Get local IP-address and according broadcast-address
 *
 *	Get the IP-address from a local network adaptor. If at least one non-loopback device is found,
 *  the addresses for the first non-loopback device will be returned. If non non-loopback-device is found,
 *  the address from a loopback-device will be returned.
 *	@param ip Pointer to an uint32_t, where the local IP should be stored
 *	@param broadcast Pointer to an uint32_t, where the according broadcast-address should be stored
 */
LIBPARTICLE_API int p_util_get_local_addresses(uint32_t * ip, uint32_t * broadcast);

/** @brief Get a timestamp
 *
 *	@return timestamp (milliseconds)
 */
LIBPARTICLE_API uint32_t p_util_getmstime();

/** @brief Lets the program sleep
 *
 *	@param milliseconds How long should the program sleep?
 */
LIBPARTICLE_API void p_util_sleepms(unsigned long milliseconds);

/*@}*/

#endif /* _UTIL_H_ */
