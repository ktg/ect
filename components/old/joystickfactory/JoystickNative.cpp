/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

*/
/* implementation - Chris Greenhalgh 20040906 */
#include "equip_ect_components_joystickfactory_JoystickNative.h"
#include "windows.h"

/*
 * Class:     equip_ect_components_joystickfactory_JoystickNative
 * Method:    getNum
 * Signature: ()I
 */
jint JNICALL Java_equip_ect_components_joystickfactory_JoystickNative_getNum
(JNIEnv *, jclass) {
	return joyGetNumDevs();
}

/*
 * Class:     equip_ect_components_joystickfactory_JoystickNative
 * Method:    poll
 * Signature: ([Z[I[I[I[I)V
 */
void JNICALL Java_equip_ect_components_joystickfactory_JoystickNative_poll
(JNIEnv *env, jclass cls, jbooleanArray oks, jintArray xs, jintArray ys, jintArray zs, jintArray bs) {
	/* get refs to arrays */
	int noks = oks!=NULL ? env->GetArrayLength(oks) : 0;
	int nxs = xs!=NULL ? env->GetArrayLength(xs) : 0;
	int nys = ys!=NULL ? env->GetArrayLength(ys) : 0;
	int nzs = zs!=NULL ? env->GetArrayLength(zs) : 0;
	int nbs = bs!=NULL ? env->GetArrayLength(bs) : 0;
	jboolean *ok = (oks!=NULL) ? env->GetBooleanArrayElements(oks, NULL) : NULL;
	jint *x = (xs!=NULL) ? env->GetIntArrayElements(xs, NULL) : NULL;
	jint *y = (ys!=NULL) ? env->GetIntArrayElements(ys, NULL) : NULL;
	jint *z = (zs!=NULL) ? env->GetIntArrayElements(zs, NULL) : NULL;
	jint *b = (bs!=NULL) ? env->GetIntArrayElements(bs, NULL) : NULL;
	for (int i=0; i<noks; i++) {
		if (ok[i]) {
			JOYINFO info;
			MMRESULT res = joyGetPos(i, &info);
			if (res==JOYERR_NOERROR) {
				if (nxs>i)
					x[i] = (jint)info.wXpos;
				if (nys>i)
					y[i] = (jint)info.wYpos;
				if (nzs>i)
					z[i] = (jint)info.wZpos;
				if (nbs>i)
					b[i] = (jint)info.wButtons;
			} else
				ok[i] = false;
		}
			
	//	printf("ok[%d]=%d\n", i, ok[i]);
	}
	if (ok!=NULL) 	
		env->ReleaseBooleanArrayElements(oks, ok, 0);
	if (x!=NULL)
		env->ReleaseIntArrayElements(xs, x, 0);
	if (y!=NULL)
		env->ReleaseIntArrayElements(ys, y, 0);
	if (z!=NULL)
		env->ReleaseIntArrayElements(zs, z, 0);
	if (b!=NULL)
		env->ReleaseIntArrayElements(bs, b, 0);
}

