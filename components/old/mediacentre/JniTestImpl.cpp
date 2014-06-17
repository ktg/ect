#include <jni.h>
#include "JniTest.h"
#include <stdio.h>
#include <string.h>
#include "ManagedCPPWrapper.cpp"

/*
int main ()
{
	ManagedCPPWrapper* t = new ManagedCPPWrapper();
	t->method();
}
*/

JNIEXPORT void JNICALL Java_JniTest_testMethod
  (JNIEnv *env, jobject obj, jstring title, jstring date) {
	//char buf[128];
    const char *titleStr = env->GetStringUTFChars(title, 0);
	const char *dateStr = env->GetStringUTFChars(date, 0);

	printf("In CPP: %s", titleStr);
    printf("In CPP: %s", dateStr);
    
	char titleBuf[128];
	strcpy(titleBuf, titleStr);
	char dateBuf[128];
	strcpy(dateBuf, dateStr);

	ManagedCPPWrapper* t = new ManagedCPPWrapper();
	t->method(titleBuf, dateBuf);
	//printf("From CPP: %s", str);

	env->ReleaseStringUTFChars(title, titleStr);
	env->ReleaseStringUTFChars(date, dateStr);

	//char buf[128];
	//strcpy(buf, "from CPP");
    //return env->NewStringUTF(response);
	//return "from cpp";
}