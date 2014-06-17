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
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <string.h>

#include "findpos.h"

/*---------------------------------------------------------------------------*/
/* Sets up and runs the solver                                               */
/*---------------------------------------------------------------------------*/
int findpos(double *dist, double **rx, int iter_lim, double precision,
            double *answer, double *accuracy)
{
   double **rx_remap = (double**)malloc((RXS-SPARE) * sizeof(double*));
   double *dist_remap = (double*)malloc((RXS-SPARE) * sizeof(double));
   int i;
   int r[RXS-SPARE-1];
   struct sols solution;
   int numVars = 3;
   double *xInit = (double*)malloc(numVars * sizeof(double));
   double ans[3] = {1000000000, 1000000000, 1000000000};
   double acc = 1000000000;

   /* Always place zero receiver at start of array */
   rx_remap[0] = rx[0];
   dist_remap[0] = dist[0];

   /* Run Levenberg-Marquardt on different combinations of receivers */
   for (r[0] = 1; r[0] < RXS; r[0]++) {
      for (r[1] = r[0]+1; r[1] < RXS; r[1]++) {
         for (r[2] = r[1]+1; r[2] < RXS; r[2]++) {
            for (r[3] = r[2]+1; r[3] < RXS; r[3]++) {
               /* Map transmitters */
               for (i = 0; i < RXS-SPARE-1; i++) {
                  rx_remap[i+1] = rx[r[i]];
                  dist_remap[i+1] = dist[r[i]];
               }
               
               /* Initialise unknowns */
               for (i = 0; i < 3; i++) {
                  xInit[i] = getrand(30.0, 90.0);  /*  1m < {x,y,z} < 3m    */
               }
               
               /* Run the solver */
               if (runsolver(dist_remap, rx_remap, xInit, &solution, iter_lim, precision, ans, &acc) == 1) return 1;

               /* Better answer? */
               
               if (acc < *accuracy) {
                  *accuracy = acc;
                  for (i = 0; i < 3; i++) answer[i] = ans[i];
               }
            }
         }
      }
   }

   free(xInit);
   free(rx_remap);
   free(dist_remap);
   return 0;
}


/*---------------------------------------------------------------------------*/
/* Sets up and runs the solver                                               */
/* Pass number of sets of readings plus arrays of distances from each sensor */
/* sort_list and var_list are used to pass back estimated values & errors    */
/*---------------------------------------------------------------------------*/
int runsolver(double *dist, double **rx, double *xInit, struct sols *solutions,
              int iter_max, double err_level, double *ans, double *acc)
{
   int status;
   int i, iter = 0;
   int ret;

   const gsl_multifit_fdfsolver_type *T;
   const size_t numFuncs = RXS-SPARE-1;
   const size_t numVars = 3;
   gsl_multifit_function_fdf mulFunc;
   gsl_multifit_fdfsolver *solver;
   //  gsl_matrix *covar = gsl_matrix_alloc(numVars, numVars);
   double *ftotal_final = (double *)malloc(sizeof(double));

   struct data readings = {dist, rx, ftotal_final};
   gsl_vector_view x;

   x = gsl_vector_view_array(xInit, numVars);

   if (numFuncs < numVars) {
      return 1;
   }
   
   /* Set up multifit function details */
   mulFunc.f = &findsensors_f;
   mulFunc.df = &findsensors_df;
   mulFunc.fdf = &findsensors_fdf;
   mulFunc.n = numFuncs;
   mulFunc.p = numVars;
   mulFunc.params = &readings;

   T = gsl_multifit_fdfsolver_lmsder;
   solver = gsl_multifit_fdfsolver_alloc(T, numFuncs, numVars);
   ret = gsl_multifit_fdfsolver_set(solver, &mulFunc, &x.vector);
   
   /* Fit data */
   do {
      iter++;
      status = gsl_multifit_fdfsolver_iterate(solver);
      if (status == GSL_ETOLF || status == GSL_ETOLX || status == GSL_ETOLG) {
         break;
      }
      
      status = gsl_multifit_test_delta(solver->dx, solver->x, err_level, err_level);
   }
   while (status == GSL_CONTINUE && iter < iter_max);

   print_state(iter, solver, rx, dist);

   /* Pass back results */
   *acc = *ftotal_final;
   for (i = 0; i < 3; i++) ans[i] = gsl_vector_get(solver->x, i);
   
   /* Free up used memory */
   gsl_multifit_fdfsolver_free(solver);
   free(ftotal_final);
   return 0;
}


/*---------------------------------------------------------------------------*/
/* Function to be minimised                                                  */
/*---------------------------------------------------------------------------*/
int findsensors_f(const gsl_vector *x, void *params, gsl_vector *f)
{
   double tx, ty, tz;
   int r;
   double ftotal = 0.0;
   double val;
   double lhs, rhs;
   double sqr, sqrj;
   
   /* Get pointers to arrays of distance readings */
   double **rx = ((struct data *)params)->rx;
   double *dist = ((struct data *)params)->dist;
   double *ftotal_addr = ((struct data *)params)->ftotal;

   
   /* Feed readings and current refinements of unknowns into model describing
      location/sensor relationship
      
      NB: Distances from transceivers:  dist[reading_num][transceiver_num]
          Transceiver locations:        transc[transceiver_num][dimension]   */
   
   tx = gsl_vector_get(x, 0);
   ty = gsl_vector_get(x, 1);
   tz = gsl_vector_get(x, 2);

   sqrj = sqrt(SQ(tx-rx[0][0]) + SQ(ty-rx[0][1]) + SQ(tz-rx[0][2]));
   
   for (r = 1; r < RXS-SPARE; r++) {
      sqr  = sqrt(SQ(tx-rx[r][0]) + SQ(ty-rx[r][1]) + SQ(tz-rx[r][2]));
      lhs = dist[r];
      rhs = sqr - sqrj;
      val = SQ(lhs - rhs);
      gsl_vector_set(f, r-1, val);
      ftotal += val;
   }
   *ftotal_addr = ftotal;
   
   return GSL_SUCCESS;
}


/*---------------------------------------------------------------------------*/
/* Computes Jacobian matrix of derivatives                                   */
/*---------------------------------------------------------------------------*/
int findsensors_df(const gsl_vector *x, void *params, gsl_matrix *J)
{
   double tx, ty, tz;
   double sqr, sqrj, bracket1;
   double val;
   int r;

   /* Get pointers to arrays of distance readings */
   double **rx = ((struct data *)params)->rx;
   double *dist = ((struct data *)params)->dist;
      
   
   /* Compute first order derivatives to build up sparse matrix J */
   /* All uncalculated derivatives are ones that would compute as zero */
   tx = gsl_vector_get(x, 0);
   ty = gsl_vector_get(x, 1);
   tz = gsl_vector_get(x, 2);

   sqrj = sqrt(SQ(tx-rx[0][0]) + SQ(ty-rx[0][1]) + SQ(tz-rx[0][2]));

   for (r = 1; r < RXS-SPARE; r++) {
      sqr  = sqrt(SQ(tx-rx[r][0]) + SQ(ty-rx[r][1]) + SQ(tz-rx[r][2]));
      bracket1 = dist[r] - sqr + sqrj;

      /* Derivative w.r.t. x */
      val = bracket1 * ((-(2*tx - 2*rx[r][0]) / sqr) + ((2*tx - 2*rx[0][0]) / sqrj));
      gsl_matrix_set(J, r-1, 0, val);
      //printf("val = %f\n", val);
      
      /* Derivative w.r.t. y */
      val = bracket1 * ((-(2*ty - 2*rx[r][1]) / sqr) + ((2*ty - 2*rx[0][1]) / sqrj));
      gsl_matrix_set(J, r-1, 1, val);
      //printf("val = %f\n", val);

      /* Derivative w.r.t. z */
      val = bracket1 * ((-(2*tz - 2*rx[r][2]) / sqr) + ((2*tz - 2*rx[0][2]) / sqrj));
      gsl_matrix_set(J, r-1, 2, val);
   }
   
   return GSL_SUCCESS;
}


/*---------------------------------------------------------------------------*/
/* Computes both function and Jacobian matrix together                       */
/*---------------------------------------------------------------------------*/
int findsensors_fdf(const gsl_vector *x, void *params, gsl_vector *f, gsl_matrix *J)
{
   /* Note: Current code is not optimal, should be rewritten in long form to
      save on processing time */
   findsensors_f(x, params, f);
   findsensors_df(x, params, J);

   return GSL_SUCCESS;
}


/*---------------------------------------------------------------------------*/
/* Print current state of the solver                                         */
/*---------------------------------------------------------------------------*/
int print_state(size_t iter, gsl_multifit_fdfsolver *s,
                double **rx, double *dist)
{
      return 0;
}


/*---------------------------------------------------------------------------*/
/* Set up receiver knowns and unknowns                                       */
/*---------------------------------------------------------------------------*/
void setReceiver(double **rx, int num,
                 double x, double y, double z)
{
   rx[num][0] = x;
   rx[num][1] = y;
   rx[num][2] = z;
}


/*---------------------------------------------------------------------------*/
/* Get random number within a range                                          */
/*---------------------------------------------------------------------------*/
double getrand(double min, double max)
{
   double range_out = max - min;
   double range_rand = (double)RAND_MAX;
   return min + (range_out / range_rand) * (double)rand();
}


/*---------------------------------------------------------------------------*/
/* Swap two values                                                           */
/*---------------------------------------------------------------------------*/
void swap(double *a, double *b)
{
   double tmp = *a;
   *a = *b;
   *b = tmp;
}