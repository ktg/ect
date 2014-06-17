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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Shahram Izadi (University of Nottingham)

*/
#include <windows.h>
#include <stdio.h>
#include "equip_ect_util_process_ProcessUtils.h"

BOOL CALLBACK TerminateProcessEnum(HWND hwnd, LPARAM lParam);
   
JNIEXPORT jint JNICALL Java_equip_ect_util_process_ProcessUtils_createProcess
(JNIEnv *env, jclass, jstring jCommandLine, jint processPriority,
    jstring jStartDir, jboolean showWindow) {
        
    if (!jCommandLine) {
        printf("ProcessUtils.createProcess NULL command line\n");
        return -1;
    }
    
    char *cStartDir = NULL;
    if (jStartDir) {
        cStartDir = (char *)env->GetStringUTFChars(jStartDir, NULL);
    } else {
        printf("ProcessUtils.createProcess: NULL starting directory ");
        printf("(will use parent's directory instead)\n");
    }
    
    char *cCommandLine = (char *)env->GetStringUTFChars(jCommandLine, NULL);
    
    jint pid = -1;
    if (cCommandLine) {
        
        STARTUPINFO startupInfo;
        PROCESS_INFORMATION processInfo;

        ZeroMemory(&startupInfo, sizeof(startupInfo));
        startupInfo.cb = sizeof(startupInfo);
        ZeroMemory(&processInfo, sizeof(processInfo));
        
        if (!showWindow) {
            startupInfo.dwFlags = STARTF_USESHOWWINDOW;
            startupInfo.wShowWindow  = SW_HIDE;
        }

        // create the process...ensuring handles are not inherited
        if (CreateProcess(NULL, cCommandLine,
            NULL, /* Process handle not inheritable */
            NULL, /* Thread handle not inheritable */
            FALSE, /* Handle inheritance FALSE */
            processPriority,
            NULL, /* Use parent's environment block */
            cStartDir, &startupInfo, &processInfo)) {
        
            pid = processInfo.dwProcessId;
            CloseHandle(processInfo.hProcess);
            CloseHandle(processInfo.hThread);
            
        } else {
            printf("ProcessUtils.createProcess failed (%d)\n", GetLastError());
        }
        env->ReleaseStringUTFChars(jCommandLine, cCommandLine);
    }
    
    if (cStartDir) {
        env->ReleaseStringUTFChars(jStartDir, cStartDir);
    }
    return pid;
}

JNIEXPORT void JNICALL Java_equip_ect_util_process_ProcessUtils_terminateProcess
(JNIEnv *env, jclass, jint pid, jint timeout) {

    // generally not a good idea to use terminateprocess
    // (doesn't notify the process nor any attached dlls
    // of termination so they can cleanup)...
    // instead we first enum all top-level windows owned
    // by the process and post a WM_CLOSE to them...only
    // opting for terminateprocess if this fails
    
    HANDLE hProcess = OpenProcess(SYNCHRONIZE|PROCESS_TERMINATE, FALSE, pid);

    if (!hProcess) {
         return;
    }

    EnumWindows((WNDENUMPROC)TerminateProcessEnum, (LPARAM)pid) ;

    if (WaitForSingleObject(hProcess, timeout) != WAIT_OBJECT_0) {
         TerminateProcess(hProcess,0);
    }
    CloseHandle(hProcess);
}

JNIEXPORT void JNICALL Java_equip_ect_util_process_ProcessUtils_waitFor
(JNIEnv *env, jclass, jint pid, jint timeout) {

    HANDLE hProcess = OpenProcess(SYNCHRONIZE, FALSE, pid);

    if (!hProcess) {
         return;
    }

    WaitForSingleObject(hProcess, timeout<0?INFINITE:timeout);
    
    CloseHandle(hProcess);
}

JNIEXPORT jint JNICALL Java_equip_ect_util_process_ProcessUtils_exitCode
(JNIEnv *env, jclass, jint pid) {

    HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION, FALSE, pid);

    if (!hProcess) {
         return -2;
    }

    DWORD exitCode;
    if (GetExitCodeProcess(hProcess, &exitCode) == 0) {
        exitCode = -2;
    }
    CloseHandle(hProcess);
    return exitCode;
}

BOOL CALLBACK TerminateProcessEnum(HWND hwnd, LPARAM lParam) {
      
      DWORD pid;
      GetWindowThreadProcessId(hwnd, &pid) ;
      
      if(pid == (DWORD)lParam) {
         PostMessage(hwnd, WM_CLOSE, 0, 0);
      }
      return TRUE ;
}
