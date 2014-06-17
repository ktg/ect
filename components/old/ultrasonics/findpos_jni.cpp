/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

*/
#include "equip_ect_components_ultrasonics_Ultrasonics.h"
#include "findpos.c"

JNIEXPORT jdoubleArray JNICALL 
    Java_equip_ect_components_ultrasonics_Ultrasonics_findPos
        (JNIEnv *env, jclass, jdoubleArray distJ, 
            jdoubleArray rxJ, jint inter, jdouble precision) {

    // copy java arrays for reference native side
    jdouble* distC = env->GetDoubleArrayElements(distJ, 0);    
    jdouble* rxC = env->GetDoubleArrayElements(rxJ, 0);
    // determine number of receivers
    jint n = env->GetArrayLength(rxJ);
    int recvCount = n / 3;
    // store receiver positions into 2d array
    int counter = 0;	
    double **rx = (double **)malloc(recvCount * sizeof(double *));
    for (int i = 0; i < recvCount; i++) {
        rx[i] = (double *)malloc(3 * sizeof(double));
    }
    for (int i=0; i<recvCount; i++) {
        setReceiver((double **)rx, i,   rxC[counter],  
        rxC[counter+1],  rxC[counter+2]);
        counter += 3;
    }
    // Remap distances and transmitters so 
    // first is nearest to user position
    int tj;
    for (int i=0; i<6; i++) {
        if (distC[i] == 0.0) {
           tj = i;
        }
    }
    swap(&distC[0], &distC[tj]);
    for (i = 0; i < 3; i++) {
        swap(&rx[0][i], &rx[tj][i]);
    }
    // calculate position
    double answer[3]; 
    double accuracy = 1000000000;
    findpos(distC, (double **)rx, inter, precision, answer, &accuracy); 
    // store answer
    jdoubleArray answerJ = env->NewDoubleArray(4);
    env->SetDoubleArrayRegion(answerJ, 0, 3, answer);
    // store accuracy
    double accArray[1] = { accuracy };
    env->SetDoubleArrayRegion(answerJ, 3, 1, accArray);
    // clean up
    env->ReleaseDoubleArrayElements(distJ, distC, 0);
    env->ReleaseDoubleArrayElements(rxJ, rxC, 0);
    for (int i = 0; i < RXS; i++) { 
        free(rx[i]);
    }
    free(rx);
    // return the result in form { x, y, z, accuracy }
    return answerJ;
}
