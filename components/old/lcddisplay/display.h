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

#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include <sys/types.h>

#ifdef WIN32
#include <windows.h>
#else
#include <sys/socket.h>

#include <netinet/in.h>
#include <arpa/inet.h>
#endif

#include <errno.h>
#include <fcntl.h>
#ifndef WIN32
#include <netdb.h>
#include <unistd.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "libparticle/conf.h"
#include "libparticle/error.h"
#include "libparticle/packet.h"
#include "libparticle/socket.h"
#include "libparticle/util.h"
#include <libparticle.h>
#include <libparticle/_packet.h>

#include <sys/timeb.h>
#include <time.h>

#include "bmp.h"


//// for old libparticle:

//#include <stdio.h>
//#include <stdlib.h>
//#include <string.h>
//
//#ifdef __cplusplus
//extern "C" {
//#endif
//#include "libparticle.h"
//#include "acl.h"
//#include "cl.h"
//#ifdef __cplusplus
//}
//#endif
//
//#ifdef WIN32
//#include <windows.h>
//#include <winsock2.h>
//#endif

//using namespace std ;



// operation codes (don't use 0)
// must be same as in the PIC graphics.h file
#define OP_CLEAR 1
#define OP_TEXT 2
#define OP_LINE 4
#define OP_BOX 5
#define OP_CIRCLE 6
#define OP_ELLIPSE 7
#define OP_TEXT0 8
#define OP_TEXT_SCROLL0 9
#define OP_TEXT90 10
#define OP_TEXT_SCROLL90 11
#define OP_TEXT180 12
#define OP_TEXT_SCROLL180 13
#define OP_TEXT270 14
#define OP_TEXT_SCROLL270 15
#define OP_RESETIMAGE 20
#define OP_ADDTOIMAGE 22
#define OP_WRITEIMAGE 24
#define OP_INVERT 26
#define OP_GETSENSORS 40
#define OP_STOREIMAGE 50
#define OP_STOREIMAGEAT 51
#define OP_LOADIMAGE 52
#define OP_LOADIMAGEAT 53
#define OP_SCROLL 60
#define OP_ADD_TO_TEXT_BUFFER 70;
#define OP_CLEAR_TEXT_BUFFER 71;
#define OP_SCROLL_TEXT_BUFFER 72;

#define DEG0 0
#define DEG90 90
#define DEG180 180
#define DEG270 270
// negative rotates
#define DEGM90 270
#define DEGM180 180
#define DEGM270 90

#define VERTICAL_SCROLL 1
#define HORIZONTAL_SCROLL 2

// constant for upwards movement
#define MOVEUP 1
// constant for downwards movement
#define MOVEDOWN 2
// constant for movement to left
#define MOVELEFT 3
// constant for movement to right
#define MOVERIGHT 4

#define PACKET_SIZE 40

#define WS "  "
#define NL "\n"
#define NLWS "\n  "

int comm_open();
int comm_close();

signed int setSendToId(const unsigned char* id);
signed int dosend(struct p_packet *packet, bool ack);

// --------------------- SEVERAL DISPLAYS ---------------------------------- //

signed int d_clear_text_buffer(unsigned int dispnr);
signed int d_add_to_text_buffer(unsigned int dispnr, char* text);
signed int d_scroll_text_buffer(unsigned int dispnr, unsigned int orientation, signed int row, signed int col, int rotation, unsigned int draw, unsigned int wordwrap, unsigned int speed, unsigned int nrsteps);

signed int d_clear_display(unsigned int dispnr, unsigned int background);
signed int d_draw_line_display(unsigned int dispnr, unsigned int start_row, unsigned int start_col,
				 unsigned int end_row, unsigned int end_col, unsigned int thickness);
signed int d_draw_vline_display(unsigned int dispnr, unsigned int start_row, unsigned int col, 
				  unsigned int end_row, unsigned int thickness);
signed int d_draw_hline_display(unsigned int dispnr, unsigned int row, unsigned int start_col, 
				  unsigned int end_col, unsigned int thickness);
signed int d_draw_ellipse_display(unsigned int dispnr, unsigned int center_row, unsigned int center_col,
			    unsigned int hor_rad, unsigned int ver_rad, unsigned int thickness);
signed int d_draw_box_display(unsigned int dispnr, unsigned int start_row, unsigned int start_col,
			    unsigned int end_row, unsigned int end_col, unsigned int thickness);
signed int d_text_display(unsigned int dispnr, signed int row, signed int col, char* text, int rotation, unsigned int draw, unsigned int wordwrap);
signed int d_text_display(unsigned int dispnr, signed int row, signed int col, char* text, int rotation, unsigned int draw);
//signed int d_text_display(unsigned int dispnr, unsigned int row, unsigned int col, char* text, int rotation, int draw);
signed int d_invert_display(unsigned int dispnr);
signed int d_text_scroll_display(unsigned int dispnr, char* text, int rotation, unsigned int draw, unsigned int wordwrap);
signed int d_scroll_rect_display(unsigned dispnr, unsigned int start_row, 
              unsigned int start_col, unsigned int end_row, unsigned int end_col, 
              unsigned int direction, unsigned int speed, unsigned int steps);

signed int d_writeCurrentImage_display(unsigned int dispnr, unsigned int row, unsigned int col,
							   unsigned int width, unsigned int height, int rotation);
signed int d_writeImage_display(unsigned int dispnr, unsigned int row, unsigned int col, unsigned int width, unsigned int height, 
				  int rotation, unsigned char* data, unsigned int len);

//void d_printline(char* text, unsigned int row, unsigned int col);
signed int d_clear(unsigned int background);
signed int d_draw_line(unsigned int start_row, unsigned int start_col,
				 unsigned int end_row, unsigned int end_col, unsigned int thickness);
signed int d_draw_vline(unsigned int start_row, unsigned int col, 
				  unsigned int end_row, unsigned int thickness);
signed int d_draw_hline(unsigned int row, unsigned int start_col, 
				  unsigned int end_col, unsigned int thickness);
signed int d_draw_ellipse(unsigned int center_row, unsigned int center_col,
			    unsigned int hor_rad, unsigned int ver_rad, unsigned int thickness);
signed int d_draw_box(unsigned int start_row, unsigned int start_col,
			    unsigned int end_row, unsigned int end_col, unsigned int thickness);
signed int d_text(unsigned int row, unsigned int col, char* text, int rotation, unsigned int draw);
signed int d_text(unsigned int row, unsigned int col, char* text, int rotation, unsigned int draw, unsigned int wordwrap);
signed int d_invert();

// image methods
signed int d_resetImage();
//void d_addToImage(unsigned char* data, unsigned int len, unsigned int width, unsigned int height);
//void d_writeImage(unsigned int row, unsigned int col, unsigned int width, unsigned int height, int rotation);
signed int d_sendImage(unsigned int width, unsigned int height, 
				       unsigned char* data, unsigned int len);
signed int d_writeCurrentImage(unsigned int row, unsigned int col,
							   unsigned int width, unsigned int height, int rotation);

signed int d_writeImage(unsigned int row, unsigned int col, unsigned int width, 
				unsigned int height, int rotation, unsigned char* data, unsigned int len);

signed int d_storeImage(unsigned int width, unsigned int height);
signed int d_storeImageAt(unsigned long flashpos, unsigned int width, unsigned int height);
signed int d_loadImage(unsigned int imageNr);
signed int d_loadImageAt(unsigned long flashpos);


signed int d_get_description(unsigned int format, char* desctext);

signed int d_get_sensors(unsigned int timeout, unsigned int format, char* sensorData);
signed int d_get_sensors(unsigned int timeout, unsigned int format, 
						 signed int sensId, char* sensorData);

//int main(int argc, char *argv[]);
