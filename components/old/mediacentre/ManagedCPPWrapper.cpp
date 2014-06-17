#using <mscorlib.dll>
#using "CSharpJniTest.netmodule"
using namespace System;
#include <string.h>

__gc class ManagedCPPWrapper {
public:
	CSharpJniTest __gc *t;

	ManagedCPPWrapper() {
		t = new CSharpJniTest();
	}

	void method(char* title, char* date) {
		printf("In managed CPP: %s", title);
		//String myStr = "test";
		char titleBuf[128];
		strcpy(titleBuf, title);
		
		char dateBuf[128];
		strcpy(dateBuf, date);
		
		printf("Still alive");
		

		t -> testMethod(dateBuf, titleBuf);

		//return response;
	}
};