#include <jni.h>
#include "JniTest.h"
#include "ManagedCPPWrapper.cpp"

JNIEXPORT void JNICALL Java_JniTest_testMethod (JNIEnv *env, jobject obj) {
	ManagedCPPWrapper* t = new ManagedCPPWrapper();
	t->method();
	//printf("testing jni!!!");
	//return;
}