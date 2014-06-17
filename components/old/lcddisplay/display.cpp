/*
<COPYRIGHT>

Copyright (c) 2005, University of Munich and University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Munich and University of Nottingham
   nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</COPYRIGHT>

Created by: 
  Paul Holleis (University of Munich)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)
  			
Contributors:
  Paul Holleis (University of Munich)
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)
*/

#define _CRT_INSECURE_NO_DEPRECATE

#include "display.h"
#include "equip_ect_components_lcddisplay_LCDDisplay.h"

#define DOACK true
//#define DOACK false

#define STD_DISP 0
#define COLORTHRESHOLD 0

// Variables
//- cl_packet *packet;
//+
struct p_packet *packet = NULL;


int send_sock;
int recv_sock;
char text[200];
//int num_of_retries = 10;
//int timeout = 300;
bool sendToId_set = false;
uint8_t sendToId[8];
unsigned char seq = 1;

long acc_x, acc_y, acc_z, acc_u;
const int ACC_X = 0;
const char ACC_X_TYPE[] = "SGX";
const char ACC_X_DESC[] = "x-axis accelerometer";
const int ACC_Y = 1;
const char ACC_Y_TYPE[] = "SGY";
const char ACC_Y_DESC[] = "y-axis accelerometer";
const int ACC_Z = 2;
const char ACC_Z_TYPE[] = "SGZ";
const char ACC_Z_DESC[] = "z-axis accelerometer";
const int ACC_U = 3;
const char ACC_U_TYPE[] = "SGU";
const char ACC_U_DESC[] = "u-axis accelerometer";

const int ALL_SENSORS = -1;

//byte display[5][16];

int comm_open(int sendPort, int recvPort) {
  // Create packets
  //- packet = create_cl_packet(2);
  //+
  packet = p_pkt_alloc();
	

  //printf("Displaying CL packet:\n\n");
  //print_cl_packet(packet);

  // Create a particle socket
  //- send_sock = p_socket(5556, 0);
  //- recv_sock = p_socket(5555, 0);
  //+
  send_sock = p_socket_open(0, 0, sendPort);
  recv_sock = p_socket_open(0, 0, recvPort);
  p_socket_set_recv_option(send_sock, 1);
  p_socket_set_blocking(recv_sock, 0);
  p_socket_set_autoack(send_sock, 1);
  p_socket_set_retry(send_sock, 50, 1000);
  //p_describe_socket(send_sock);
  //+
	
  ////p_init_recv(recv_sock);
  ////p_add_filter(recv_sock, "SGX");
  ////p_add_filter(recv_sock, "SGY");
  ////p_add_filter(recv_sock, "SGZ");
  ////p_add_filter(recv_sock, "SGU");

  ////p_add_filter(recv_sock, "ACM");
  ////p_add_filter(recv_sock, "CAD");
  ////p_add_filter(recv_sock, "CAC");

  return 0;
}


int comm_close() {
  // Cleanup
  //- p_close(send_sock);
  //+
  p_socket_close(send_sock);
  p_socket_close(recv_sock);
  //+

  return 0;
}

/**
 * specifies the particle ID to which the next commands will send
 */
signed int setSendToId(const uint8_t *id) {
  ////p_rm_id_filter(recv_sock, (const char*)sendToId);

  memcpy(sendToId, id, 8);
  sendToId_set = true;

  ////p_add_id_filter(recv_sock, (const char*)sendToId);

  return 0;
}

/**
 * send a packet
 * ack true means send acknowledged if a receiver ID has been set
 */
signed int dosend(struct p_packet *pkt, bool ack) {
  int sent = 0;
  if (ack && sendToId_set) {
    //- sent = p_send_acked(send_sock, recv_sock, packet, sendToId, num_of_retries, timeout);
    //+
    sent = p_socket_send_acked(send_sock, recv_sock, pkt, sendToId);
		
  } else {
    //- sent = p_send(send_sock, packet);
    //+
    sent = p_socket_send(send_sock, pkt);
		
    //p_util_sleepms(200);
  }
  if (sent <= 0)
    //-printf("\n  ERROR sending packet (seq=%i)", packet->seq);
    printf("\n  ERROR sending packet (seq=%i)", p_pkt_get_seq(pkt));
  else
    //-printf("\nsent packet (seq=%i)", packet->seq);
    printf("\nsent packet (seq=%i)", p_pkt_get_seq(pkt));

  // seq number for duplicate check
  if (seq > 255) seq = 1;

  return sent;
}

/**
 * clears entire standard display
 */
signed int d_clear(unsigned int background) {
  return d_clear_display(STD_DISP, background);
}
/**
 * clears entire display
 */
signed int d_clear_display(unsigned int dispnr, unsigned int background) {
  int sent;
  const int sendlen = 4;
  uint8_t sendtext[sendlen];

  sendtext[0] = seq++;
  sendtext[1] = OP_CLEAR;
  sendtext[2] = dispnr;
  sendtext[3] = background;
  //- add_acl_tupel(cl, create_acl_tupel("RDA", (const unsigned char *)sendtext, sendlen));
  //+
  packet = p_pkt_alloc();
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);

  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

/**
 *
 */
signed int d_draw_ellipse(unsigned int center_row, unsigned int center_col,
			  unsigned int hor_rad, unsigned int ver_rad, unsigned int thickness) {
  return d_draw_ellipse_display(STD_DISP, center_row, center_col, hor_rad, ver_rad, thickness);
}
/**
 *
 */
signed int d_draw_ellipse_display(unsigned int dispnr, unsigned int center_row, unsigned int center_col,
				  unsigned int hor_rad, unsigned int ver_rad, unsigned int thickness) {
  const int sendlen = 8;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_ELLIPSE;
  sendtext[2] = dispnr;
  sendtext[3] = center_row;
  sendtext[4] = center_col;
  sendtext[5] = hor_rad;
  sendtext[6] = ver_rad;
  sendtext[7] = thickness;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

signed int d_draw_box(unsigned int start_row, unsigned int start_col,
		      unsigned int end_row, unsigned int end_col, unsigned int thickness) {
  return d_draw_box_display(STD_DISP, start_row, start_col, end_row, end_col, thickness);
}
signed int d_draw_box_display(unsigned int dispnr, unsigned int start_row, unsigned int start_col,
			      unsigned int end_row, unsigned int end_col, unsigned int thickness) {
  const int sendlen = 8;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_BOX;
  sendtext[2] = dispnr;
  sendtext[3] = start_row;
  sendtext[4] = start_col;
  sendtext[5] = end_row;
  sendtext[6] = end_col;
  sendtext[7] = thickness;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

signed int d_scroll_rect_display(unsigned dispnr, unsigned int start_row, 
				 unsigned int start_col, unsigned int end_row, unsigned int end_col, 
				 unsigned int direction, unsigned int speed, unsigned int steps) {
  const int sendlen = 10;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_SCROLL;
  sendtext[2] = dispnr;
  sendtext[3] = start_row;
  sendtext[4] = start_col;
  sendtext[5] = end_row;
  sendtext[6] = end_col;
  sendtext[7] = direction;
  sendtext[8] = speed;
  sendtext[9] = steps;

  packet = p_pkt_alloc();
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  p_pkt_free(packet);
		
  return sent;
}

signed int d_draw_line(unsigned int start_row, unsigned int start_col,
		       unsigned int end_row, unsigned int end_col, unsigned int thickness) {
  return d_draw_line_display(STD_DISP, start_row, start_col, end_row, end_col, thickness);
}
signed int d_draw_line_display(unsigned int dispnr, unsigned int start_row, unsigned int start_col,
			       unsigned int end_row, unsigned int end_col, unsigned int thickness) {
  const int sendlen = 8;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_LINE;
  sendtext[2] = dispnr;
  sendtext[3] = start_row;
  sendtext[4] = start_col;
  sendtext[5] = end_row;
  sendtext[6] = end_col;
  sendtext[7] = thickness;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

signed int d_draw_hline(unsigned int row, unsigned int start_col, 
			unsigned int end_col, unsigned int thickness) {
  return d_draw_hline_display(STD_DISP, row, start_col, end_col, thickness);
}
signed int d_draw_hline_display(unsigned int dispnr, unsigned int row, unsigned int start_col, 
				unsigned int end_col, unsigned int thickness) {
  return d_draw_line_display(dispnr, row, start_col, row, end_col, thickness);
}

signed int d_draw_vline(unsigned int start_row, unsigned int col, 
			unsigned int end_row, unsigned int thickness) {
  return d_draw_vline_display(STD_DISP, start_row, col, end_row, thickness);
}
signed int d_draw_vline_display(unsigned int dispnr, unsigned int start_row, unsigned int col, 
				unsigned int end_row, unsigned int thickness) {
  return d_draw_line_display(dispnr, start_row, col, end_row, col, thickness);
}

signed int d_invert() {
  return d_invert_display(STD_DISP);
}
signed int d_invert_display(unsigned int dispnr) {
  const int sendlen = 3;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_INVERT;
  sendtext[2] = dispnr;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

/**
 * print text at given row / col; row and col refer to 
 * pixel positions: row in [0,MAX_ROW], col in [0,MAX_COL]
 * rotation is one of DEG0, DEG90, DEG180, DEG270
 */
signed int d_text(unsigned int row, unsigned int col, char* text, int rotation, unsigned int draw, unsigned int wordwrap) {
  return d_text_display(STD_DISP, row, col, text, rotation, draw, wordwrap);
}
/**
 * print text at given row / col; row and col refer to 
 * pixel positions: row in [0,MAX_ROW], col in [0,MAX_COL]
 * rotation is one of DEG0, DEG90, DEG180, DEG270
 * no word wrapping
 */
signed int d_text(unsigned int row, unsigned int col, char* text, int rotation, unsigned int draw) {
  return d_text(row, col, text, rotation, draw, 0);
}
/**
 * print text at given row / col; row and col refer to 
 * pixel positions: row in [0,MAX_ROW], col in [0,MAX_COL]
 * rotation is one of DEG0, DEG90, DEG180, DEG270
 * no word wrapping
 */
signed int d_text_display(unsigned int dispnr, signed int row, signed int col, char* text, int rotation, unsigned int draw) {
  return d_text_display(dispnr, row, col, text, rotation, draw, 0);
}
/**
 * print text at given row / col; row and col refer to 
 * pixel positions: row in [0,MAX_ROW], col in [0,MAX_COL]
 * rotation is one of DEG0, DEG90, DEG180, DEG270
 */
signed int d_text_display(unsigned int dispnr, signed int row, signed int col, char* text, int rotation, unsigned int draw, unsigned int wordwrap) {
  const int sendlen = 7;
  uint8_t sendtext[PACKET_SIZE];
  int sent;

  char op = OP_TEXT;
  if (rotation == DEG0) op = OP_TEXT0;
  else if (rotation == DEG90) op = OP_TEXT90;
  else if (rotation == DEG180) op = OP_TEXT180;
  else if (rotation == DEG270) op = OP_TEXT270;

  sendtext[0] = seq++;
  sendtext[1] = op;
  sendtext[2] = dispnr;
  sendtext[3] = row;
  sendtext[4] = col;
  sendtext[5] = draw;
  sendtext[6] = wordwrap;
  memcpy(&(sendtext[sendlen]), (const void*)text, strlen(text));

  //+
  packet = p_pkt_alloc();
  //~
  if (sendlen + strlen(text) > 40) return -1; // too many bytes for 1 packet
  p_acl_add_str(packet, "RDA", sendtext, sendlen + strlen(text), 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}
signed int d_text_scroll_display(unsigned int dispnr, char* text, int rotation, unsigned int draw, unsigned int wordwrap) {
  const int sendlen = 5;
  uint8_t sendtext[PACKET_SIZE];
  int sent;

  char op = OP_TEXT_SCROLL0;
  if (rotation == DEG0) op = OP_TEXT_SCROLL0;
  else if (rotation == DEG90) op = OP_TEXT_SCROLL90;
  else if (rotation == DEG180) op = OP_TEXT_SCROLL180;
  else if (rotation == DEG270) op = OP_TEXT_SCROLL270;

  sendtext[0] = seq++;
  sendtext[1] = op;
  sendtext[2] = dispnr;
  sendtext[3] = draw;
  sendtext[4] = wordwrap;
  memcpy(&(sendtext[sendlen]), (const void*)text, strlen(text));

  packet = p_pkt_alloc();
  if (sendlen + strlen(text) > 40) return -1; // too many bytes for 1 packet
  p_acl_add_str(packet, "RDA", sendtext, sendlen + strlen(text), 0);
  sent = dosend(packet, DOACK);
  p_pkt_free(packet);

  return sent;
}


signed int d_clear_text_buffer(unsigned int dispnr) {
	const int sendlen = 3;
	uint8_t sendtext[PACKET_SIZE];
	int sent;

	char op = OP_CLEAR_TEXT_BUFFER;
	
	sendtext[0] = seq++;
	sendtext[1] = op;
	sendtext[2] = dispnr;
	
	packet = p_pkt_alloc();
	if (sendlen > 40) return -1; // too many bytes for 1 packet
	p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
	sent = dosend(packet, DOACK);
	p_pkt_free(packet);

	return sent;
}

signed int d_add_to_text_buffer(unsigned int dispnr, char* text) {
	const int sendlen = 3;
	uint8_t sendtext[PACKET_SIZE];
	int sent;

	char op = OP_ADD_TO_TEXT_BUFFER;

	sendtext[0] = seq++;
	sendtext[1] = op;
	sendtext[2] = dispnr;
	memcpy(&(sendtext[sendlen]), (const void*)text, strlen(text));

	packet = p_pkt_alloc();
	if (sendlen + strlen(text) > 40) return -1; // too many bytes for 1 packet
	p_acl_add_str(packet, "RDA", sendtext, sendlen + strlen(text), 0);
	sent = dosend(packet, DOACK);
	p_pkt_free(packet);

	return sent;
}

signed int d_scroll_text_buffer(unsigned int dispnr, unsigned int orientation, signed int row, signed int col, int rotation, unsigned int draw, unsigned int wordwrap, unsigned int speed, unsigned int nrsteps) {
 const int sendlen = 11;
	uint8_t sendtext[PACKET_SIZE];
	int sent;

	char op = OP_SCROLL_TEXT_BUFFER;

	sendtext[0] = seq++;
	sendtext[1] = op;
	sendtext[2] = dispnr;
	sendtext[3] = orientation;
	sendtext[4] = row;
	sendtext[5] = col;
	sendtext[6] = rotation;
	sendtext[7] = draw;
	sendtext[8] = wordwrap;
	sendtext[9] = speed;
	sendtext[10] = nrsteps;

	packet = p_pkt_alloc();
	if (sendlen > 40) return -1; // too many bytes for 1 packet
	p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
	sent = dosend(packet, DOACK);
	p_pkt_free(packet);

	return sent;
}


/**
 * print text at given row / col; row and col refer to real 
 * character positions: row in [0,4], col in [0,15]
 */
/*
  void d_printline(char* text, unsigned int row, unsigned int col) {
  int sent;
  //int c, overlap;
  uint8_t sendtext[200];

  if (row == 0 && col == 0)
  sprintf(sendtext, "%c%c%s", OP_TEXT, 255, text);
  else
  sprintf(sendtext, "%c%c%s", OP_TEXT, row*16 + col, text);
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);

  //// it does not work to add another tuple of the same type:

  sent = dosend(packet, DOACK);
  free_acl_packet(packet);
  }
*/

/**
 * reset the internal buffer image
 */
signed int d_resetImage() {
  const int sendlen = 3;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_RESETIMAGE;
  sendtext[2] = 255;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

///**
// * adds data to the internal image buffer
// * len is the size of data; the data will be sent in packets of size PACKET_SIZE
// * width and height specify the dimensions of the image that is being created
// *
// * call d_printImage or d_orImage to write the image buffer to the display
// */
//void d_addToImage(unsigned char *data, unsigned int len, unsigned int width, unsigned int height) {
//	unsigned char sendtext[200];
//	int sent;
//	unsigned int i, pack;
//	// this is the allowed size of the image data so that the packet size is 
//	// at most PACKET_SIZE
//	const int image_size = PACKET_SIZE - 3;
//
//	// cant send 0 => 255
//	if (width == 0) width = 255;
//	if (height == 0) height = 255;
//
//	for (pack = 0; pack < len; pack += image_size) {
//		sprintf(sendtext, "%c%c%c", OP_ADDTOIMAGE, width, height);
//		for (i = pack; i < len && i < pack + image_size; i++) {
//			sendtext[i-pack+3] = data[i];
//		}
//		//+
//		packet = p_pkt_alloc();
//		//~
//		p_acl_add_str(packet, "RDA", sendtext, i-pack+3);
//		sent = dosend(packet, DOACK);
//		free_acl_packet(packet);
//		if (!DOACK) p_util_sleepms(200);
//	}
//}

/**
 * writes data to the internal image buffer which is then written on the display;
 * row and col specify the position on the display where the image should start;
 * width and height specify the dimensions of the image that is being created;
 * rotation specifies an optional rotation (must be one of DEGx)
 * data is the array of bytes that will be sent
 * len is the size of data; the data will be sent in packets of size PACKET_SIZE;
 */
signed int d_sendImage(unsigned int width, unsigned int height, 
		       unsigned char* data, unsigned int len) {
  const int sendlen = PACKET_SIZE;
  uint8_t sendtext[sendlen];
  int sent;
  unsigned int i, pack;
  unsigned int counter;
  // this is the allowed size of the image data so that the packet size is 
  // at most PACKET_SIZE
  const int image_size = PACKET_SIZE - 5;

  // used for simple duplicate check
  counter = 1;
  // add data to buffer
  for (pack = 0; pack < len; pack += image_size) {
    sendtext[0] = seq++;
    sendtext[1] = OP_ADDTOIMAGE;
    sendtext[2] = counter; counter++; if (counter > 254) counter = 1;
    sendtext[3] = width;
    sendtext[4] = height;

    for (i = pack; i < len && i < pack + image_size; i++) {
      sendtext[i-pack+5] = data[i];
    }

    //+
    packet = p_pkt_alloc();
    //~
    p_acl_add_str(packet, "RDA", sendtext, i-pack+5, 0);
    sent = dosend(packet, DOACK);
    //- free_acl_packet(packet);
    //+
    p_pkt_free(packet);
		

    if (sent < 0) return sent;
  }
	
  return sent;
}

signed int d_writeCurrentImage(unsigned int row, unsigned int col,
			       unsigned int width, unsigned int height, int rotation) {
  return d_writeCurrentImage_display(STD_DISP, row, col, width, height, rotation);
}
signed int d_writeCurrentImage_display(unsigned int dispnr, unsigned int row, unsigned int col,
				       unsigned int width, unsigned int height, int rotation) {
  // initiate writing on display
  const int sendlen = 8;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_WRITEIMAGE;
  sendtext[2] = dispnr;
  sendtext[3] = row;
  sendtext[4] = col;
  sendtext[5] = width;
  sendtext[6] = height;
  sendtext[7] = rotation;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
	
  return sent;
}

/**
 * writes data to the internal image buffer which is then written on the display;
 * row and col specify the position on the display where the image should start;
 * width and height specify the dimensions of the image that is being created;
 * rotation specifies an optional rotation (must be one of DEGx)
 * data is the array of bytes that will be sent
 * len is the size of data; the data will be sent in packets of size PACKET_SIZE;
 */
signed int d_writeImage(unsigned int row, unsigned int col, unsigned int width, unsigned int height, 
			int rotation, unsigned char* data, unsigned int len) {
  return d_writeImage_display(STD_DISP, row, col, width, height, rotation, data, len);
}
/**
 * writes data to the internal image buffer which is then written on the display;
 * row and col specify the position on the display where the image should start;
 * width and height specify the dimensions of the image that is being created;
 * rotation specifies an optional rotation (must be one of DEGx)
 * data is the array of bytes that will be sent
 * len is the size of data; the data will be sent in packets of size PACKET_SIZE;
 */
signed int d_writeImage_display(unsigned int dispnr, unsigned int row, unsigned int col, unsigned int width, unsigned int height, 
				int rotation, unsigned char* data, unsigned int len) {
  int sent = 0;

  sent = d_sendImage(width, height, data, len);
  if (sent < 0) return sent;
	
  return d_writeCurrentImage_display(dispnr, row, col, width, height, rotation);

  //const int sendlen = PACKET_SIZE;
  //uint8_t sendtext[sendlen];
  //int sent;
  //unsigned int i, pack;
  //unsigned int counter;
  //// this is the allowed size of the image data so that the packet size is 
  //// at most PACKET_SIZE
  //const int image_size = PACKET_SIZE - 4;

  //   // used for simple duplicate check
  //counter = 1;
  //// add data to buffer
  //for (pack = 0; pack < len; pack += image_size) {
  //	sendtext[0] = OP_ADDTOIMAGE;
  //	sendtext[1] = counter; counter++; if (counter > 254) counter = 1;
  //	sendtext[2] = width;
  //	sendtext[3] = height;

  //	for (i = pack; i < len && i < pack + image_size; i++) {
  //		sendtext[i-pack+4] = data[i];
  //	}

  //  //+
  //  packet = p_pkt_alloc();
  //  //~
  //	p_acl_add_str(packet, "RDA", sendtext, i-pack+4);
  //	sent = dosend(packet, DOACK);
  //	free_acl_packet(packet);

  //	if (sent < 0) return sent;
  //}

  //// initiate writing on display
  //sendtext[0] = OP_WRITEIMAGE;
  //sendtext[1] = row;
  //sendtext[2] = col;
  //sendtext[3] = width;
  //sendtext[4] = height;
  //sendtext[5] = rotation;
  ////+
  //packet = p_pkt_alloc();
  ////~
  //p_acl_add_str(packet, "RDA", sendtext, 6);
  //sent = dosend(packet, DOACK);
  //free_acl_packet(packet);
  //
  //return sent;
}


signed int d_storeImage(unsigned int width, unsigned int height) {
  const int sendlen = 4;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_STOREIMAGE;
  sendtext[2] = width;
  sendtext[3] = height;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
  return sent;
}

signed int d_storeImageAt(unsigned long flashpos, unsigned int width, unsigned int height) {
  const int sendlen = 8;
  uint8_t sendtext[sendlen];
  int sent;

  // ACLAddData((byte)(value>>8));         // value
  //ACLAddData((byte)(value&0x00FF));       // value

  sendtext[0] = seq++;
  sendtext[1] = OP_STOREIMAGEAT;
  sendtext[2] = (flashpos & 0xFF000000) >> 24;
  sendtext[3] = (flashpos & 0x00FF0000) >> 16;
  sendtext[4] = (flashpos & 0x0000FF00) >> 8;
  sendtext[5] = flashpos & 0x000000FF;
  sendtext[6] = width;
  sendtext[7] = height;

  packet = p_pkt_alloc();
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  p_pkt_free(packet);
   
  return sent;
}

signed int d_loadImage(unsigned int imageNr) {
  const int sendlen = 3;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_LOADIMAGE;
  sendtext[2] = imageNr;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
  return sent;
}

signed int d_loadImageAt(unsigned long flashpos) {
  const int sendlen = 6;
  uint8_t sendtext[sendlen];
  int sent;

  sendtext[0] = seq++;
  sendtext[1] = OP_LOADIMAGEAT;
  sendtext[2] = (flashpos & 0xFF000000) >> 24;
  sendtext[3] = (flashpos & 0x00FF0000) >> 16;
  sendtext[4] = (flashpos & 0x0000FF00) >> 8;
  sendtext[5] = flashpos & 0x000000FF;
   
  packet = p_pkt_alloc();
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  p_pkt_free(packet);

  return sent;
}

char* prettyPrintSensorStart(int format, char* startText) {
  switch (format) {
  case 0: // xml
    strcat(startText, "<sensors>");
    strcat(startText, NL);
    break;

  case 1: // txt
    strcat(startText, "id, value, type, description");
    strcat(startText, NL);
    strcat(startText, NL);
    break;

  case 2: // html
    strcat(startText, "<html><head><title>Sensor Values:</title></head>");
    //strcat(startText, "<body><h1>Sensor Values:</h1><br>");
    strcat(startText, "<table width=\"50%\">\n");
    strcat(startText, "  <tr><th>id</th><th>value</th><th>type</th><th>description</th></tr>\n");
    break;

  default: break;
  }

  return startText;
}

char* prettyPrintSensorEnd(int format, char* startText) {
  switch (format) {
  case 0: // xml
    strcat(startText, "</sensors>");
    strcat(startText, NL);
    break;

  case 1: // txt
    break;

  case 2: // html
    strcat(startText, "</table>");
    strcat(startText, NL);
    break;

  default: break;
  }

  return startText;
}

char* prettyPrintSensor(int format, char* sensor, int id, const char* type, const char* desc, long value) {
  char tmpstr[40];

  switch (format) {
  case 0: // xml
    strcat(sensor, WS);
    strcat(sensor, "<sensor id=\"");
    sprintf(tmpstr, "%i", id);
    strcat(sensor, tmpstr);
    strcat(sensor, "\" type=\"");
    sprintf(tmpstr, "%s", type);
    strcat(sensor, tmpstr);
    strcat(sensor, "\" desc=\"");
    sprintf(tmpstr, "%s", desc);
    strcat(sensor, tmpstr);
    strcat(sensor, "\" value=\"");
    sprintf(tmpstr, "%li", value);
    strcat(sensor, tmpstr);
    strcat(sensor, "\"/>");
    strcat(sensor, NL);
    break;

  case 1: // txt
    sprintf(tmpstr, "%i, %li, %s, %s", id, value, type, desc);
    strcat(sensor, tmpstr);
    strcat(sensor, NL);
    break;

  case 2: // html
    strcat(sensor, "<tr><td>");
    sprintf(tmpstr, "%i", id);
    strcat(sensor, tmpstr);
    strcat(sensor, "</td><td>");
    sprintf(tmpstr, "%li", value);
    strcat(sensor, tmpstr);
    strcat(sensor, "</td><td>");
    sprintf(tmpstr, "%s", type);
    strcat(sensor, tmpstr);
    strcat(sensor, "</td><td>");
    sprintf(tmpstr, "%s", desc);
    strcat(sensor, tmpstr);
    strcat(sensor, "</td></tr>");
    strcat(sensor, NL);
    break;

  default: break;
  }
	
  return sensor;
}


long getValueFromPacket(struct p_packet *packet, const char* type) {
  long val = 0;
  //-	st_acl_tupel* acltup;
  //+
  struct p_acl_tuple *acltup = NULL;

  //- acltup = get_next_acl_tupel_by_type(packet, type);
  //+
  p_acl_findfirst_str(packet, type, &acltup);

  if (acltup != NULL) {
    //- if (acltup->len == 3) {
    //+
    if (acltup->acl_len == 3) {
      //- val = acltup->data[0] * 255 + acltup->data[1];
      //+
      unsigned char **data = (unsigned char**)malloc(sizeof(unsigned char*));
      p_acl_get_data(acltup, (char**)data);
      val = (*data)[0] * 255 + (*data)[1];
      free(data);
      //+
    } else {
      // ?
    }
    return val;
  }
  return 0;		
}


/**
 * queries the capabilities and description of the display
 * sensorData must be preallocated
 *
 * returns 0 if call succeeded
 */
signed int d_get_description(unsigned int format, char* desctext) {
  switch (format) {
  case 0: // xml
    strcat(desctext, "XML format currently not supported ... use format=html\n\n");
    break;

  case 1: // txt
    strcat(desctext, "Display Data\n\n");
    strcat(desctext, "feature, value, description\n");

    strcat(desctext, "graphical LCD, , each pixel can be switched on or off\n");
    strcat(desctext, "width, 96, 96 pixel columns</td></tr>\n");
    strcat(desctext, "height, 40, 40 pixel rows</td></tr>\n");
    strcat(desctext, "color depth, 1, only black (1) and white (0)</td></tr>\n");
    strcat(desctext, "text height, 8, a letter is 8 pixels high</td></tr>\n");
    strcat(desctext, "..., ..., ...</td></tr>\n");
    strcat(desctext, "..., ..., ...</td></tr>\n");
    strcat(desctext, "..., ..., ...</td></tr>\n");
    strcat(desctext, "..., ..., ...</td></tr>\n");
    break;

  case 2: // html
    strcat(desctext, "<html><head><title>Display Data:</title></head>");
    strcat(desctext, "<body><h1>Display Data:</h1><br>");
    strcat(desctext, "<table width=\"50%\">\n");
    strcat(desctext, "  <tr><th>feature</th><th>value</th><th>description</th></tr>\n");

    strcat(desctext, "  <tr><td>graphical LCD</td> <td></td> <td>each pixel can be switched on or off</td></tr>\n");
    strcat(desctext, "  <tr><td>width</td> <td>96</td> <td>96 pixel columns</td></tr>\n");
    strcat(desctext, "  <tr><td>height</td> <td>40</td> <td>40 pixel rows</td></tr>\n");
    strcat(desctext, "  <tr><td>color depth</td> <td>1</td> <td>only black (1) and white (0)</td></tr>\n");
    strcat(desctext, "  <tr><td>text height</td> <td>8</td> <td>a letter is 8 pixels high</td></tr>\n");
    strcat(desctext, "  <tr><td>...</td> <td>...</td> <td>...</td></tr>\n");
    strcat(desctext, "  <tr><td>...</td> <td>...</td> <td>...</td></tr>\n");
    strcat(desctext, "  <tr><td>...</td> <td>...</td> <td>...</td></tr>\n");
    strcat(desctext, "  <tr><td>...</td> <td>...</td> <td>...</td></tr>\n");

    strcat(desctext, "</table></body></html>\n");
    break;

  default: break;
  }

  return 0;
}

/**
 * equal to a call to d_get_sensors(unsigned int timeout, unsigned int format,
 * signed int sensId, char* sensorData) with sensId set to ALL_SENSORS
 *
 * returns 0 if call succeeded
 */
signed int d_get_sensors(unsigned int timeout, unsigned int format, char* sensorData) {
  return d_get_sensors(timeout, format, (signed int)-1, sensorData);
}

/**
 * query the particle for its sensors
 * dont wait longer than timeout
 * only query for sensor with ID sensId (all sensors if -1)
 * print result in given format into sensorData
 *
 * returns 0 if call succeeded
 */
signed int d_get_sensors(unsigned int timeout, unsigned int format, 
			 signed int sensId, char* sensorData) {
  const int sendlen = 3;
  uint8_t sendtext[sendlen];
  int sent;
  char sensor[200];
  struct _timeb timestruc;
  double curtime = 0.0;
  double newtime = 0.0;
  //- cl_packet* clrec;
  //+
  struct p_packet *recpkt = NULL;

  // send request
  sendtext[0] = seq++;
  sendtext[1] = OP_GETSENSORS;
  sendtext[2] = sensId;
  //+
  packet = p_pkt_alloc();
  //~
  p_acl_add_str(packet, "RDA", sendtext, sendlen, 0);
  sent = dosend(packet, DOACK);
  //- free_acl_packet(packet);
  //+
  p_pkt_free(packet);
	
  if (sent < 0) return sent;
   
  // wait for answer
  _ftime(&timestruc);
  curtime = (double)timestruc.time + (double)(timestruc.millitm / 1000.0);
  newtime = curtime;

  recpkt = NULL;
  while(recpkt == NULL && (newtime-curtime)*1000 < timeout) {
    //- recpkt = p_recv(recv_sock, send_sock);
    //+
    recpkt = p_socket_recv(recv_sock, send_sock);

    // see if the packet came from the (correct) particle
    if (recpkt != NULL) {
      //-
      //if (recpkt->acl_packet == NULL)
      //	recpkt = NULL;
      //if (strcmp((const char*)recpkt->id, (const char*)sendToId) != 0)
      //	recpkt = NULL;
      //-
      //+
      if (p_acl_first(recpkt) == NULL)
	recpkt = NULL;
      else if (strcmp((const char*)p_pkt_get_srcid(recpkt), (const char*)sendToId) != 0)
	recpkt = NULL;
      //+
    }
    _ftime(&timestruc);
    newtime = (double)timestruc.time + (double)(timestruc.millitm / 1000.0);
  }

  if (recpkt != NULL) {
    // retrieve values from packet
    acc_x = getValueFromPacket(recpkt, ACC_X_TYPE);
    acc_y = getValueFromPacket(recpkt, ACC_Y_TYPE);
    acc_z = getValueFromPacket(recpkt, ACC_Z_TYPE);
    acc_u = getValueFromPacket(recpkt, ACC_U_TYPE);
			
    // write sensor values

    // default format is html
    if (format > 2) format = 1;
		
    prettyPrintSensorStart(format, sensorData);

    sensor[0] = '\0';
    if (sensId == ALL_SENSORS || sensId == ACC_X) {
      prettyPrintSensor(format, sensor, ACC_X, ACC_X_TYPE, ACC_X_DESC, acc_x);
      strcat(sensorData, sensor);
    }
    sensor[0] = '\0';
    if (sensId == ALL_SENSORS || sensId == ACC_Y) {
      prettyPrintSensor(format, sensor, ACC_Y, ACC_Y_TYPE, ACC_Y_DESC, acc_y);
      strcat(sensorData, sensor);
    }
		
    sensor[0] = '\0';
    if (sensId == ALL_SENSORS || sensId == ACC_Z) {
      prettyPrintSensor(format, sensor, ACC_Z, ACC_Z_TYPE, ACC_Z_DESC, acc_z);
      strcat(sensorData, sensor);
    }
    sensor[0] = '\0';
    if (sensId == ALL_SENSORS || sensId == ACC_U) {
      prettyPrintSensor(format, sensor, ACC_U, ACC_U_TYPE, ACC_U_DESC, acc_u);
      strcat(sensorData, sensor);
    }


    prettyPrintSensorEnd(format, sensorData);

  } else {
    // got no data
    prettyPrintSensorStart(format, sensorData);
    prettyPrintSensorEnd(format, sensorData);
    return -1;
  }

  return 0;
}

unsigned char* parseBitmap(unsigned char* bitmap, int length, int *w, int *h) {
    long width, height;
    char* image;
    unsigned char* convImage;

    image = ConvertBMP(bitmap, &width, &height);
    *w = (int)width;
    *h = (int)height;

    // debug
    printf("\n");
    for (int row = 0; row < height; row++) {
        for (int col = 0; col < width; col++) {
            if (image[row*width + col] > COLORTHRESHOLD) {
                printf("1");
            } else {
                printf("0");
            }
        }
        printf("\n");
    }
    printf("\n");

    convImage = (unsigned char*)malloc(
        ((height-1)/8 +1) * width * sizeof(char));

    // check each 8th row
    for (int row = 0; row < height; row += 8) {
        char c = 0;
        // check each column
        for (int col = 0; col < width; col++) {
            // collapse 8 rows into one byte
            c = 0;
            for (int bt = 0; bt < 8 && row+bt < height; bt++) {
                if (image[(row+bt)*width + col] > COLORTHRESHOLD) {
                    c |= (1 << bt);
                }
            }
            convImage[(row/8)*width + col] = c;
        }
    }
    
    free(image);
    return convImage;
}


// jni calls

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_open
(JNIEnv *, jclass, jint sendPort, jint recvPort) {
    return comm_open(sendPort, recvPort);	
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_close
(JNIEnv *, jclass) {
    return comm_close();	
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_clear
(JNIEnv *, jclass, jint dispNumb, jint background) {
    return d_clear_display(dispNumb, background);
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_drawLine
    (JNIEnv *, jclass, jint dispNumb, jint startRow, 
    jint startCol, jint endRow, jint endCol, jint thickness) {    
    
    return d_draw_line_display(dispNumb, startRow, 
        startCol, endRow, endCol, thickness);
}

JNIEXPORT jint JNICALL 
Java_equip_ect_components_lcddisplay_LCDDisplay_drawEllipse
    (JNIEnv *, jclass, jint dispNumb, jint centerRow, 
    jint centerCol, jint horRad, jint verRad, jint thickness) {
            
    return d_draw_ellipse_display(dispNumb, centerRow, 
        centerCol, horRad, verRad, thickness);
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_drawBox
    (JNIEnv *, jclass, jint dispNumb, jint startRow, jint startCol, 
    jint endRow, jint endCol, jint thickness) {    
    
    return d_draw_box_display(dispNumb, startRow, 
        startCol, endRow, endCol, thickness);
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_textDisplay
    (JNIEnv *env, jclass, jint dispNumb, jint row, 
    jint col, jstring textJ, jint rot, jint draw, jint wordwrap) {    

    int ret = -1;    
    char *textC = (char *)env->GetStringUTFChars(textJ, NULL);    
    ret = d_text_display(dispNumb, row, col, textC, rot, draw, wordwrap);    
    env->ReleaseStringUTFChars(textJ, textC);
    return ret;
}

JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_textScrollDisplay
    (JNIEnv *env, jclass, jint dispNumb, jstring textJ, jint rot, jint draw, jint wordwrap) {    

    int ret = -1;    
    char *textC = (char *)env->GetStringUTFChars(textJ, NULL);    
    ret = d_text_scroll_display(dispNumb, textC, rot, draw, wordwrap);    
    env->ReleaseStringUTFChars(textJ, textC);
    return ret;
}



JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_invert
    (JNIEnv *, jclass, jint dispNumb) {    
    
    return d_invert_display(dispNumb);
}

JNIEXPORT VOID JNICALL 
Java_equip_ect_components_lcddisplay_LCDDisplay_setParticleID
    (JNIEnv *env, jclass, jbyteArray idJ) {
        
    if (idJ) {        
        jbyte* idC = env->GetByteArrayElements(idJ, 0);            
        setSendToId((const unsigned char*)idC);        
        env->ReleaseByteArrayElements(idJ, idC, 0);    
    } else {
        sendToId_set = false;
    }
}

JNIEXPORT jint JNICALL 
Java_equip_ect_components_lcddisplay_LCDDisplay_drawImage
    (JNIEnv *env, jclass, jint dispNumb, jbyteArray imgJ, jint len, 
    jint posx, jint posy, jint orientation, jboolean store) {    

    jbyte* imgC = env->GetByteArrayElements(imgJ, 0);    
    int width, height, ret;    
    unsigned char* bitmap = parseBitmap(
        (unsigned char*)imgC, len, &width, &height);    
    d_resetImage();
    if (store) {
        d_sendImage(width, height, bitmap, 
            ((height-1)/8 +1) * width * sizeof(char));
        ret = d_storeImage(width, height);
    } else {
        ret = d_writeImage_display(dispNumb, posy, posx, width, height, 
            orientation, bitmap, ((height-1)/8 +1) * width * sizeof(char));
    }    
    env->ReleaseByteArrayElements(imgJ, imgC, 0);
    free(bitmap);    
    return ret;
}

JNIEXPORT jint JNICALL 
Java_equip_ect_components_lcddisplay_LCDDisplay_drawFromMem
    (JNIEnv *env, jclass, jint dispNumb, jint index) {    
    
    d_resetImage();
    d_loadImage(index);
    return d_writeCurrentImage_display(dispNumb, 0, 0, 96, 40, DEG0);
}

/*
 * Class:     equip_ect_components_lcddisplay_LCDDisplay
 * Method:    clearTextBuffer
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_clearTextBuffer(JNIEnv * env, jclass, jint disp) {
  return d_clear_text_buffer(disp);
}

/*
 * Class:     equip_ect_components_lcddisplay_LCDDisplay
 * Method:    addToTextBuffer
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_addToTextBuffer(JNIEnv * env, jclass, jint disp, jstring textJ) {
  int ret = -1;    
  char *textC = (char *)env->GetStringUTFChars(textJ, NULL);    
  ret = d_add_to_text_buffer(disp, textC);
  env->ReleaseStringUTFChars(textJ, textC);
  return ret;
}

/*
 * Class:     equip_ect_components_lcddisplay_LCDDisplay
 * Method:    scrollTextBuffer
 * Signature: (IIIIIIIII)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_lcddisplay_LCDDisplay_scrollTextBuffer(JNIEnv * env, jclass, jint disp, jint orientation, jint row, jint col, jint rotation, jint draw, jint wordwrap, jint speed, jint nrsteps) {
  return d_scroll_text_buffer(disp, orientation, row, col, rotation, draw, wordwrap, speed, nrsteps);
}

