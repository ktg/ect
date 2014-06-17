/*
<COPYRIGHT>

Copyright (c) 2005, University of Bristol and University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Bristol and University of Nottingham
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
  Paul Duff (University of Bristol) 
  Shahram Izadi (University of Nottingham)
Contributors:
  Paul Duff (University of Bristol) 
  Shahram Izadi (University of Nottingham)

*/
#include <gsl/gsl_blas.h>
#include <gsl/gsl_vector.h>
#include <gsl/gsl_multifit_nlin.h>


/* Number of transmitters */
#define RXS 6

/* Number of transmitters NOT used in each minimisation */
#define SPARE 1

/* Define below to read from serial port */
#define READTTY

/* Minimum distance to find an object */
#define MIN_PROXIMITY 10000000000.0

#define UNKNOWN 0
#define KNOWN 1

#define SQ(x) ((x)*(x))


/* Operating modes */
#define BASIC 1
#define TRAIN 2
#define IDENTIFY 3


/* Structure for passing distance readings to functions */
struct data {
   double *dist;             /* Distance readings from sensors */
   double **rx;              /* Position of receivers */
   double *ftotal;           /* Sum of squared errors */
};


/* Use to hold either a known value or a vector index for the unknown */
union tdata {
   double value;
   int index;
};


/* Structure used to pass variable estimates and errors */
struct sols {
   double *x;
   double *f;
   double *covar;
   double *dist;
};


int findpos(double *dist, double **rx, int iter_lim, double precision,
            double *answer, double *accuracy);
int runsolver(double *dist, double **rx, double *xInit, struct sols *solutions,
              int iter_max, double err_level, double *ans, double *acc);

int findsensors_f(const gsl_vector *x, void *params, gsl_vector *f);
int findsensors_df(const gsl_vector *x, void *params, gsl_matrix *J);
int findsensors_fdf(const gsl_vector *x, void *params, gsl_vector *f, gsl_matrix *J);
int print_state(size_t iter, gsl_multifit_fdfsolver *s,
                double **rx, double *dist);

void setReceiver(double **rx, int num,
                 double x, double y, double z);
double getrand(double min, double max);
void swap(double *a, double *b);
