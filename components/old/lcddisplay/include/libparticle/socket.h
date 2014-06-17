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
 *
 * Author(s): Max Laier <mlaier@freebsd.org>
 *			  Thomas Morper <morper@teco.edu>
 */

#ifndef _SOCKET_H_
#define _SOCKET_H_

#include "conf.h"

/**
 * @defgroup SOCKET socket.c: Socket functions like initializing the socket, send and receive. (p_socket_...) 
 * @{
 */

/** @brief Create a particle socket.
 *
 * This function creates a UDP broadcasting socket.
 * If both localip and broadcast_add are 0, the socket_open function will call p_util_get_local_adresses
 * to get a local IP-adress and the according local broadcast-address.
 *
 * Very important when you work with the libparticle under Linux/FreeBSD/etc.: There is a little "problem". When you bind a socket to an IP-address,
 * it will recive only packets, witch have been send FROM this address. It is not possible, to send them on the local broadcast (e.g. 127.0.255.255
 * when the own IP is 127.0.0.3 and the subnet-mask is 0.0.255.255) and recieve them when the socket is bind to the IP-address (Windows does it).
 * Couse of this, libparticle will set the ipsrc-address to ANY (0.0.0.0) under Linux as default and only bind it to the own IP under Windows.
 *
 * The first time a receive- or send-function is called, the socket is bound.
 *
 * @param port Port for sending or receiving.
 * @param localip IP-adress of the computer, which runs the libparticle.
 * @param broadcast_add Broadcast-adress of the local network.
 *
 * localip and broadcast_add must be in network byte order. Use the socket function "htonl" if they aren't.
 *
 * @return Particle socket descriptor
 * @sa p_util_get_local_addresses
 */
LIBPARTICLE_API int p_socket_open(uint32_t localip, uint32_t broadcast_add, int port);

/** @brief Close a particle socket.
 *
 * Closes an open UDP sockets and frees memory allocated for internal structures.
 * @param sock Particle socket descriptor
 */
LIBPARTICLE_API int p_socket_close(int sock);

/** @brief Set a Socket to broadcast mode.
 *
 * Sets a socket to mode broadcast.
 * @param sock Particle socket descriptor
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_broadcast(int sock);

/** @brief Set the destination-add for a socket
 *
 * Sets the IP-address, where the packets will be send to (broadcast, local broadcast, ...)
 * Don't mix up with addressed sending!
 * @param sock Particle socket descriptor
 * @param address Address, where the packest should be brought to.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_ipdest(int sock, struct in_addr * address);

/** @brief Set the destination-add for a socket [string]
 *
 * Sets the IP-address, where the packets will be send to (broadcast, local broadcast, ...)
 * Don't mix up with addressed sending!
 * @param sock Particle socket descriptor
 * @param address Address, where the packest should be brought to. Must be a Pointer to a dotted string.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_ipdest_str(int sock, char * address);

/** @brief Set a Socket to recieve all-mode.
 *
 * Socket will recieve packets from any source-address.
 * @param sock Particle socket descriptor
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_ipsrc_all(int sock);

/** @brief Set the source-add for a socket
 *
 * Sets the IP-address, from where packets will be recieved.
 * Don't mix up with addressed sending!
 * @param sock Particle socket descriptor
 * @param address Address, from where packest should be recieved.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_ipsrc(int sock, struct in_addr * address);

/** @brief Set the source-add for a socket [string]
 *
 * Sets the IP-address, from where packets will be recieved.
 * Don't mix up with addressed sending!
 * @param sock Particle socket descriptor
 * @param address Address, from where packest should be recieved. Must be a Pointer to a dotted string.
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_ipsrc_str(int sock	, char * address);

/** @brief Sets a particle sockets own net ID
 *
 * Sets the own net ID for socket sock to the value of the byte-array add.
 * add must be the base address of a 8 byte long array.
 * @param sock Particle socket
 * @param add* Location, must be a 8 byte long array
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_id(int sock, uint8_t * add);

/** @brief Sets a particle sockets own net ID
 *
 * Sets the own net ID for socket sock to the value of the dotted string addstr.
 * addstr must be a dotted string including either 8 or 4 numbers between 0 and 255.
 * If addstr includes 8 numbers, the net ID of socket sock will be set to exactly this net ID.
 * If addstr includes only 4 numbers, the net ID will be set to the virtual net ID "1.1.1.1.addstr".
 * @param sock Particle socket
 * @param addstr Location, must be a dotted string including 44 numbers
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_id_str(int sock, char * addstr);

/** @brief Sets a particle sockets location-ID
 *
 * Sets the Location-ID for socket sock to the value of the byte-array loc.
 * loc must be the base address of a 44 byte long array.
 * @param sock Particle socket
 * @param loc* Location, must be a 44 byte long array
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_loc(int sock, uint8_t * loc);

/** @brief Sets a particle sockets location-ID
 *
 * Sets the Location-ID for socket sock to the value of the dotted string locstr.
 * locstr must be a dotted string including 44 numbers between 0 and 255.
 * @param sock Particle socket
 * @param locstr Location, must be a dotted string including 44 numbers
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_loc_str(int sock, char * locstr);

/** @brief Sets a particle socket to blocking/nonblocking mode
 *
 * Makes the recieve and send calls you do in future on this socket
 * blocking or nonblocking.
 * @param sock Particle socket
 * @param mode 1 for blocking mode and 0 for nonblocking mode
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_blocking(int sock, int mode);

/** @brief Sets a particle socket to autoack/nonautoack mode
 *
 * Decides, if the recv-function should send an ack-packet if a packet which is
 * calling for an ack-packet was received.
 * @param sock Particle socket
 * @param mode 1 autoack mode (default) and 0 for nonautock mode
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_autoack(int sock, int mode);

/** @brief Sets the parameters for acknowleged sending.
 *
 * Sets the number of retrys and the time until timeout (per retrie) for acknowleged sending.
 * @param sock Particle socket
 * @param retries number of retries which should be done if no ack-packet is received
 * @param timeout time (in milliseconds), how long the particle waits for the ack-packet after each retrie
 * @return Zero on success or -1 on error
 */
LIBPARTICLE_API int p_socket_set_retry(int sock, uint8_t retries, uint32_t timeout);

/** @brief Prints out all informations about a socket.
 *
 * Debug function, which prints out all internal informations of socket sock to stdout.
 * @param sock Particle socket
 */
LIBPARTICLE_API void p_describe_socket(int sock);

/** @brief Set recieve Option
 *
 * Set recieve Option. If set to 1, all pakets will be received, also such which weren't addressed to this computer
 * If set to 0 (deafult value), only such packets will be received, which were send to broadcast or which had been adressed to us.
 * p_socket_recv will return NULL, if a package, which was not adressed to us, was recived
 *
 * Also, the socket_recv option decides, if you will see all packets of an active fragmentation.
 * If it is set to 0, only the last data will be returned to you, indicating that you have recieved some data.
 * If set to 1, you will see all packets of the running fragmentation.
 *
 * @param fd Socket, for which the option should be set
 * @param option 0 or 1
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_recv_option(int fd, int option);

/** @brief Set fragmentation Option
 *
 * Must be set to 1 if you want to send or recieve fragmentated data on this socket. Otherwise, frag-packets will be treated as normal packets.
 * 
 * @param fd Socket, for which the option should be set
 * @param option 0 or 1
 * @return 0 on success, -1 on error
 */
LIBPARTICLE_API int p_socket_set_frag_option(int fd, int option);

/*
 * END: SOCKET ABSTRACTION
 */

/*
 * BEGIN: RECEIVE DATA
 */

/** @brief Receive data.
 *
 * Receive data from socket sock. Received data will be parsed to a CL-packet before it will be passed to the user.
 * Acknowledges will be send automaticly if autoack was set to 1.
 * @param recv_sock Particle socket descriptor used for receiving
 * @param send_sock Particle socket descriptor used for sending ACK packets...
 * @return Pointer to a cl_packet on success or NULL on error.
 */
LIBPARTICLE_API struct p_packet *p_socket_recv(int recv_sock, int send_sock);


/** @brief Receive data filtered.
 *
 * Receive data from socket sock. Received data will be filtered with recv_filter.
 * @param recv_sock Particle socket descriptor used for receiving
 * @param send_sock Particle socket descriptor used for sending ACK packets...
 * @param recv_filter Filter, with which the received data should be filtered
 * @return Pointer to a cl_packet on success or NULL on error. Also NULL if a
 * packet was received but the recv_filter didn't match.
 */
LIBPARTICLE_API struct p_packet *p_socket_recv_filtered(int recv_sock, int send_sock, int recv_filter);

/** @brief Receive raw data.
 *
 * Receive raw data from socket sock.
 * @param sock Particle socket descriptor
 * @param *data Pointer to the data buffer where received data is stored
 * @param maxlen Size of data buffer
 * @return Number of received bytes on success or -1 on error.
 */
LIBPARTICLE_API int p_socket_recv_raw(int sock, char * data, size_t maxlen);

/*
 * END: RECEIVE DATA
 */

/*
 * BEGIN: SEND DATA
 */

/** @brief Send data.
 * Send CL-packet send_data on socket send_sock.
 *
 * @param send_sock Particle socket descriptor
 * @param send_data Pointer to a CL packet that is to be sent.
 * @return Number of sent bytes on success, -1 on error
 */
LIBPARTICLE_API int p_socket_send(int send_sock, struct p_packet * send_data);

/** @brief Send data to a particle specified by its ID.
 *
 * Send Data addressed to one particle, specified by its net ID.
 * The neccessary control message ACL Types will be added automatically.
 *
 * @param send_sock Particle socket descriptor
 * @param send_data Pointer to a CL packet that is to be sent.
 * @param add Array containing the recievers ID
 * @return Number of sent bytes on success, -1 on error
 */
LIBPARTICLE_API int p_socket_send_addressed(int send_sock, struct p_packet * send_data, uint8_t * add);

/** @brief Send data to a particle specified by its ID.
 *
 * Send Data addressed to one particle, specified by its net ID.
 * The neccessary control message ACL Types will be added automatically.
 *
 * @param send_sock Particle socket descriptor
 * @param send_data Pointer to a CL packet that is to be sent.
 * @param add_str dotted string containing the recievers ID
 * @return Number of sent bytes on success, -1 on error
 */
LIBPARTICLE_API int p_socket_send_addressed_str(int send_sock, struct p_packet * send_data, char * add_str);

/** @brief Send data to a particle specified by its ID and wait for ACK control message.
 *
 *	Send data to a particle specified by its ID and wait for ACK control message.
 *  Resend packet if ACK is not recieved within timeout.
 *	The neccessary control message ACL Types will be added automatically.
 *
 * @param send_sock Particle socket descriptor used for sending CL packets
 * @param recv_sock Particle socket descriptor used for receiving ACK packets
 * @param send_data Pointer to a CL packet that is to be sent.
 * @param add Pointer to an array containing the recievers ID
 * @sa p_socket_set_retry
 * @return Number of sent bytes on success, -1 on error (not sent or not ACKed)
 */
LIBPARTICLE_API int p_socket_send_acked(int send_sock, int recv_sock, struct p_packet * send_data, uint8_t * add);


/** @brief Send data to a particle specified by its ID and wait for ACK control message.
 *
 *	Send data to a particle specified by its ID and wait for ACK control message.
 *  Resend packet if ACK is not recieved within timeout.
 *	The neccessary control message ACL Types will be added automatically.
 *
 * @param send_sock Particle socket descriptor used for sending CL packets
 * @param recv_sock Particle socket descriptor used for receiving ACK packets
 * @param send_data Pointer to a CL packet that is to be sent.
 * @param addstr dotted string containing the recievers ID
 * @sa p_socket_set_retry
 * @return Number of sent bytes on success, -1 on error (not sent or not ACKed)
 */
LIBPARTICLE_API int p_socket_send_acked_str(int send_sock, int recv_sock, struct p_packet * send_data, char * addstr);

/** @brief Sending a data puffer to a particle.
 *
 *	Send a data-buffer to a particle specified by its ID. The buffer will be send in fragments.
 *
 * It is recommended, that you set the socket frag-option to 1 before starting a fragmenation as the sender.
 *
 * @param fd_out Particle socket descriptor used for sending the frag-packets
 * @param fd_in Particle socket descriptor used for recieving frag-packets. NOTE: You can place NULL in here, but then you have to set the frag_option yourself!
 * @param buf Pointer to the buffer, which holds the data you want to send.
 * @param len Length of the data stored in buf
 * @param sendto Pointer to an array containing the recievers ID
 * @param acl_type Pointer to an array containing the ACL-type, which shoud be the subject of all packets send for this fragmentation.
 * @sa p_socket_set_frag_option
 * @return Number of sent bytes of the first packet on success, -1 on error.
 */
LIBPARTICLE_API int p_socket_send_file(int fd_out, int fd_in, uint8_t * buf, uint32_t len, uint8_t * sendto, uint8_t * acl_type);

/** @brief Sending a data puffer to a particle [ID and ACL as string]
 *
 *	Send a data-buffer to a particle specified by its ID. The buffer will be send in fragments.
 *
 * It is recommended, that you set the socket frag-option to 1 before starting a fragmenation as the sender.
 *
 * @param fd_out Particle socket descriptor used for sending the frag-packets
 * @param fd_in Particle socket descriptor used for recieving frag-packets. NOTE: You can place NULL in here, but then you have to set the frag_option yourself!
 * @param buf Pointer to the buffer, which holds the data you want to send.
 * @param len Length of the data stored in buf
 * @param sendto dotted string containing the recievers ID
 * @param acl_type ACL-Type which shoud be the subject of all packets send for this fragmentation.
 * @sa p_socket_set_frag_option
 * @return Number of sent bytes of the first packet on success, -1 on error.
 */
LIBPARTICLE_API int p_socket_send_file_str(int fd_out, int fd_in, uint8_t * buf, uint32_t len, char * sendto, char * acl_type);

/** @brief Send raw data.
 *
 * @param sock Particle socket descriptor
 * @param *data Pointer to the data buffer that is to be sent
 * @param len Size of data buffer
 * @return Number of sent bytes on success or -1 on error
 */
LIBPARTICLE_API int p_socket_send_raw(int sock, char * data, size_t len);

/*
 * END: SEND DATA
 */

/**@}*/

#endif /* _SOCKET_H_ */
