/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2005, University of Nottingham All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the University of Nottingham nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * </COPYRIGHT>
 * 
 * Created by: Jan Humble (University of Nottingham)
 * 
 * Contributors: Jan Humble (University of Nottingham) Jan Humble (University
 * of Nottingham)
 */



#include <jni.h>
#include "equip_ect_components_play3dsound_Play3DSound.h"
#include <stdio.h>
#include "dxstdafx.h"

//-----------------------------------------------------------------------------
// Defines, constants, and global variables
//-----------------------------------------------------------------------------
#define SAFE_DELETE(p)  { if(p) { delete (p);     (p)=NULL; } }
#define SAFE_RELEASE(p) { if(p) { (p)->Release(); (p)=NULL; } }


//-----------------------------------------------------------------------------
// Function-prototypes
//-----------------------------------------------------------------------------
VOID SetObjectProperties( D3DVECTOR* pvPosition, D3DVECTOR* pvVelocity );
INT Initialize(HWND hDlg);


#define ORBIT_MAX_RADIUS        5.0f
#define IDT_MOVEMENT_TIMER      1


CSoundManager*          g_pSoundManager       = NULL;
CSound*                 g_pSound              = NULL;
LPDIRECTSOUND3DBUFFER   g_pDS3DBuffer         = NULL;   // 3D sound buffer
LPDIRECTSOUND3DLISTENER g_pDSListener         = NULL;   // 3D listener object
DS3DBUFFER              g_dsBufferParams;               // 3D buffer properties
DS3DLISTENER            g_dsListenerParams;             // Listener properties
BOOL                    g_bDeferSettings      = FALSE;
BOOL                    g_bAllowMovementTimer = TRUE;
INT                     g_nGridW, g_nGridH;             // and dimensions
INT lDirection = 1;
LONG lSpeed = 50;

HWND hDlg;


INT Initialize() {
	 
	// Init the common control dll 
	printf("\nStarting common controls ...");
    //InitCommonControls();

	if (NULL == hDlg) {
		// printf("\nCreating window on initialize ...");
		// DXUTCreateWindow(L"Bogus Window", NULL, NULL, NULL, CW_USEDEFAULT , CW_USEDEFAULT);
		hDlg = GetForegroundWindow();//DXUTGetHWND();
	
	} 
	if (NULL == hDlg) {
		printf("Warning: Failed to create window on initialize");
		return FALSE;
	}
	
	return Initialize(hDlg);					 
}

INT Initialize(HWND hDlg) {
	 
 HRESULT hr;	
#ifdef _WIN64
    HINSTANCE hInst = (HINSTANCE) GetWindowLongPtr( hDlg, GWLP_HINSTANCE );
#else
    HINSTANCE hInst = (HINSTANCE) GetWindowLong( hDlg, GWL_HINSTANCE );
#endif
	
	// Create a static IDirectSound in the CSound class.  
    // Set coop level to DSSCL_PRIORITY, and set primary buffer 
    // format to stereo, 22kHz and 16-bit output.

	if (NULL == g_pSoundManager) {
		g_pSoundManager = new CSoundManager();
	}
	if (NULL == g_pSoundManager) {
		DXTRACE_ERR_MSGBOX( TEXT("Initialize"), E_OUTOFMEMORY );	
		printf("\nError creating sound manager");
		return FALSE;
	}
	
	hr = g_pSoundManager->Initialize( hDlg, DSSCL_PRIORITY );
       
    hr |= g_pSoundManager->SetPrimaryBufferFormat( 2, 22050, 16 );
    
    // Get the 3D listener, so we can control its params
    hr |= g_pSoundManager->Get3DListenerInterface( &g_pDSListener );

	if( FAILED(hr) ) {
		DXTRACE_ERR_MSGBOX( TEXT("Get3DListenerInterface"), hr );	
		printf("\nFailed initializing sound manager.");
		return FALSE;
	}

	 // Get listener parameters
    g_dsListenerParams.dwSize = sizeof(DS3DLISTENER);
    g_pDSListener->GetAllParameters( &g_dsListenerParams );

	return TRUE;
}

wchar_t * 
mbstowcs_alloc (const char *string) {
  size_t size = strlen (string) + 1;
  wchar_t *buf = (wchar_t *)malloc (size * sizeof (wchar_t));

  size = mbstowcs (buf, string, size);
  if (size == (size_t) -1) {
    return NULL;
  }
  buf = (wchar_t *)realloc (buf, (size + 1) * sizeof (wchar_t));
  return buf;
}


INT OpenSoundFile(const char* strFileName, HWND hDlg) {
	GUID    guid3DAlgorithm = GUID_NULL;
     HRESULT hr; 

	 if (NULL == hDlg) {
		hDlg = GetForegroundWindow();
	 } 
	if( g_pSound ) {
        g_pSound->Stop();
        g_pSound->Reset();
    }
 // Free any previous sound, and make a new one
    SAFE_DELETE( g_pSound );
	

	//ShowWindow(hDlg, SW_SHOWNORMAL);	

	wchar_t *file =	mbstowcs_alloc(strFileName);

    HANDLE hFile = CreateFile( file, 0, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL );
	// Verify the file is small
    if( hFile != NULL ) {
        // If you try to open a 100MB wav file, you could run out of system memory with this
        // sample cause it puts all of it into a large buffer.  If you need to do this, then 
        // see the "StreamData" sample to stream the data from the file into a sound buffer.
        DWORD dwFileSizeHigh = 0;
        DWORD dwFileSize = GetFileSize( hFile, &dwFileSizeHigh );
        CloseHandle( hFile );
		printf("\nFile '%S' (%i, %i).", file, dwFileSize, dwFileSizeHigh);
        if( dwFileSizeHigh != 0 || dwFileSize > 100000) {
            printf("\nFile '%S' too large (%i, %i).  You should stream large files.", file, dwFileSize, dwFileSizeHigh);
            return FALSE;
        }
    }

    CWaveFile waveFile;
    waveFile.Open( file, NULL, WAVEFILE_READ );
    WAVEFORMATEX* pwfx = waveFile.GetFormat();
    if( pwfx == NULL ) {
        printf("\nInvalid wave file format.");
        return FALSE;
    }

    if( pwfx->nChannels > 1 ) {
        // Too many channels in wave.  Sound must be mono when using DSBCAPS_CTRL3D
        printf("\nWave file must be mono for 3D control.");
        return FALSE;
    }

    if( pwfx->wFormatTag != WAVE_FORMAT_PCM ) {
        // Sound must be PCM when using DSBCAPS_CTRL3D
        printf("\nWave file must be PCM for 3D control.");
		return FALSE;
    }

    // Get the software DirectSound3D emulation algorithm to use
    // Ask the user for this sample, so display the algorithm dialog box.
    int nResult = 0;
		/*
		(int)DialogBox( NULL, MAKEINTRESOURCE(IDD_3D_ALGORITHM), 
                              NULL, AlgorithmDlgProc );
							  */
    switch( nResult )
    {
    case -1: // User canceled dialog box
        printf("\nLoad aborted.");
        return 0;

    case 0: // User selected DS3DALG_NO_VIRTUALIZATION  
        guid3DAlgorithm = DS3DALG_NO_VIRTUALIZATION;
        break;

    case 1: // User selected DS3DALG_HRTF_FULL  
        guid3DAlgorithm = DS3DALG_HRTF_FULL;
        break;

    case 2: // User selected DS3DALG_HRTF_LIGHT
        guid3DAlgorithm = DS3DALG_HRTF_LIGHT;
        break;
    }

    // Load the wave file into a DirectSound buffer
    hr = g_pSoundManager->Create( &g_pSound, file, DSBCAPS_CTRL3D|DSBCAPS_STICKYFOCUS, guid3DAlgorithm );  
    if( FAILED( hr ) || hr == DS_NO_VIRTUALIZATION )
    {
        DXTRACE_ERR( TEXT("Create"), hr );
        if( DS_NO_VIRTUALIZATION == hr )
        {
            printf("\nThe 3D virtualization algorithm requested is not supported under this operating system.  It is available only on Windows 2000, Windows ME, and Windows 98 with WDM drivers and beyond.  Creating buffer with no virtualization.");
        }

        // Unknown error, but not a critical failure, so just update the status
		printf("\nCould not create sound buffer.");
        return FALSE; 
    }

    // Get the 3D buffer from the secondary buffer
    if( FAILED( hr = g_pSound->Get3DBufferInterface( 0, &g_pDS3DBuffer ) ) )
    {
        DXTRACE_ERR_MSGBOX( TEXT("Get3DBufferInterface"), hr );
		printf("\nCould not get 3D buffer.");
		return FALSE;
    }

    // Get the 3D buffer parameters
    g_dsBufferParams.dwSize = sizeof(DS3DBUFFER);
    g_pDS3DBuffer->GetAllParameters( &g_dsBufferParams );

    // Set new 3D buffer parameters
    g_dsBufferParams.dwMode = DS3DMODE_HEADRELATIVE;
    g_pDS3DBuffer->SetAllParameters( &g_dsBufferParams, DS3D_IMMEDIATE );

    DSBCAPS dsbcaps;
    ZeroMemory( &dsbcaps, sizeof(DSBCAPS) );
    dsbcaps.dwSize = sizeof(DSBCAPS);

    LPDIRECTSOUNDBUFFER pDSB = g_pSound->GetBuffer( 0 );
    pDSB->GetCaps( &dsbcaps );
	if( ( dsbcaps.dwFlags & DSBCAPS_LOCHARDWARE ) != 0 ) {
        printf("\nFile loaded using hardware mixing.");
	} else {
        printf("\nFile loaded using software mixing.");
	}
    
    //g_bAllowMovementTimer = TRUE;

    
	return TRUE;

}


INT Close() {
	printf("\nClosing sound managers ...");
	SAFE_RELEASE( g_pDSListener );	
            SAFE_RELEASE( g_pDS3DBuffer );

            SAFE_DELETE( g_pSound );
            SAFE_DELETE( g_pSoundManager );
	return TRUE;

}

INT SetSoundPos(float x, float y, float z, float velX, float velY, float velZ) {
	// Move the sound object around the listener. The maximum radius of the
    // orbit is 27.5 units.
	D3DVECTOR vPosition;
    vPosition.x = x;
    vPosition.y = y;
    vPosition.z = z;

    D3DVECTOR vVelocity;
    vVelocity.x = velX;
    vVelocity.y = velY;
    vVelocity.z = velZ;

	printf("\nSetting sound pos: %f %f %f %f %f %f", x, y, z, velX, velY, velZ); 
    // Set the sound buffer velocity and position
    SetObjectProperties( &vPosition, &vVelocity );
	return TRUE;
}

//-----------------------------------------------------------------------------
// Name: OnPlaySound()
// Desc: User hit the "Play" button
//-----------------------------------------------------------------------------
HRESULT PlaySound(HWND hDlg) {
    HRESULT hr;

	hDlg = GetForegroundWindow();
	if( NULL == g_pSound ) {
		printf("Warning: Sound failed to play (g_pSound=null).");
        return E_FAIL;
	}

    // Play buffer always in looped mode just for this sample
	if( FAILED( hr = g_pSound->Play( 0, DSBPLAY_LOOPING) ) ) {
		printf("Warning: Sound failed to play.");
		   return DXTRACE_ERR( TEXT("Play"), hr );
	}

	
    return S_OK;
}


//-----------------------------------------------------------------------------
// Name: Set3DParameters()
// Desc: Set the 3D buffer parameters
//-----------------------------------------------------------------------------
VOID Set3DParameters( FLOAT fDopplerFactor, FLOAT fRolloffFactor,
                      FLOAT fMinDistance,   FLOAT fMaxDistance ) {
    // Every change to 3-D sound buffer and listener settings causes 
    // DirectSound to remix, at the expense of CPU cycles. 
    // To minimize the performance impact of changing 3-D settings, 
    // use the DS3D_DEFERRED flag in the dwApply parameter of any of 
    // the IDirectSound3DListener or IDirectSound3DBuffer methods that 
    // change 3-D settings. Then call the IDirectSound3DListener::CommitDeferredSettings 
    // method to execute all of the deferred commands at once.
    DWORD dwApplyFlag = ( g_bDeferSettings ) ? DS3D_DEFERRED : DS3D_IMMEDIATE;

    g_dsListenerParams.flDopplerFactor = fDopplerFactor;
    g_dsListenerParams.flRolloffFactor = fRolloffFactor;

    if( g_pDSListener )
        g_pDSListener->SetAllParameters( &g_dsListenerParams, dwApplyFlag );

    g_dsBufferParams.flMinDistance = fMinDistance;
    g_dsBufferParams.flMaxDistance = fMaxDistance;

	printf("\nSetting 3D params, dopp=%f roff=%f minD=%f maxD=%f", fDopplerFactor, fRolloffFactor, fMinDistance, fMaxDistance); 

	
    if( g_pDS3DBuffer )
        g_pDS3DBuffer->SetAllParameters( &g_dsBufferParams, dwApplyFlag );
}


//-----------------------------------------------------------------------------
// Name: SetObjectProperties()
// Desc: Sets the position and velocity on the 3D buffer
//-----------------------------------------------------------------------------
VOID SetObjectProperties( D3DVECTOR* pvPosition, D3DVECTOR* pvVelocity ) {
    // Every change to 3-D sound buffer and listener settings causes 
    // DirectSound to remix, at the expense of CPU cycles. 
    // To minimize the performance impact of changing 3-D settings, 
    // use the DS3D_DEFERRED flag in the dwApply parameter of any of 
    // the IDirectSound3DListener or IDirectSound3DBuffer methods that 
    // change 3-D settings. Then call the IDirectSound3DListener::CommitDeferredSettings 
    // method to execute all of the deferred commands at once.
    memcpy( &g_dsBufferParams.vPosition, pvPosition, sizeof(D3DVECTOR) );
    memcpy( &g_dsBufferParams.vVelocity, pvVelocity, sizeof(D3DVECTOR) );

	
	if( g_pDS3DBuffer ) {
        g_pDS3DBuffer->SetAllParameters( &g_dsBufferParams, DS3D_IMMEDIATE );
	}
}




/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    initialize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_initialize
(JNIEnv *, jclass) {
	if (Initialize()) {
		printf("Direct X Sound initialization succeeded.");
		//OpenSoundFile("D:/users/humble/projects/ect/java/ding.wav", hDlg);
		// OnPlaySound();
	}
	return 0;

}




/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    setSoundPos
 * Signature: (DDD)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_setSoundPos
(JNIEnv *, jclass, jdouble xpos, jdouble ypos, jdouble zpos, jdouble xvel, jdouble yvel, jdouble zvel) {
	return SetSoundPos((float) xpos, (float) ypos, (float) zpos, (float) xvel, (float) yvel, (float) zvel);
}


/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    openSoundFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_openSoundFile
(JNIEnv *env, jclass, jstring soundFileName) {
	const char *strFileName = env->GetStringUTFChars(soundFileName, 0);
	printf("\nOpening sound file: %s", strFileName);
	int res = OpenSoundFile(strFileName, hDlg);
	env->ReleaseStringUTFChars(soundFileName, strFileName);
	return res;
}

/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    setLooping
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_setLooping
(JNIEnv *, jclass, jboolean) {
	return 0;
}

/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    play
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_play
(JNIEnv *, jclass) {
	printf("\nPlaying ...");
	HRESULT hr;
	if( FAILED( hr = PlaySound( hDlg ) ) ) {
			DXTRACE_ERR_MSGBOX( TEXT("OnPlaySound"), hr );
			printf("Error playing sound file.");
			return -1;
	}
	return 0;
}

/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    stop
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_stop
(JNIEnv *, jclass) {
	printf("\nStopping ...");
	if( g_pSound )
                    {
						
                        g_pSound->Stop();
                        g_pSound->Reset();
						printf("stopped!");
                    }

	return 0;
}

/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    set3DParameters
 * Signature: (DDDD)I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_set3DParameters
(JNIEnv *, jclass, jdouble dopplerFactor, jdouble rolloffFactor, jdouble minDistance, jdouble maxDistance) {
	Set3DParameters((FLOAT) dopplerFactor, (FLOAT) rolloffFactor, (FLOAT) minDistance, (FLOAT) maxDistance);
	return 0;
}

/*
 * Class:     equip_ect_components_play3dsound_Play3DSound
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_equip_ect_components_play3dsound_Play3DSound_close
(JNIEnv *, jclass) {
	return Close();
}


