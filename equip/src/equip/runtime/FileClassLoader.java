/*
<COPYRIGHT>

Copyright (c) 2002-2005, University of Nottingham
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
package equip.runtime;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Loads class bytes from a file (probably inherited from elsewhere). */
public class FileClassLoader extends MultiClassLoader {

//---------- Private Fields ------------------------------
private String    filePrefix;

//---------- Initialization ------------------------------
/**
 * Attempts to
 * load from a local file using the relative "filePrefix",
 * ie starting at the current directory. For example
 * @param filePrefix could be "webSiteClasses\\site1\\".
 */
public FileClassLoader(String filePrefix) {
    this.filePrefix = filePrefix;
}
//---------- Abstract Implementation ---------------------
protected byte[] loadClassBytes(String className) {

    className = formatClassName(className);
    if (sourceMonitorOn) {
        print(">> from file: " + className);
    }
    byte result[];
    String fileName = filePrefix + className;
    try {
	System.out.println("FileClassLoad: "+fileName);

        FileInputStream inStream = new FileInputStream(fileName);
        // *** Is available() reliable for large files?
        result = new byte[inStream.available()];
        inStream.read(result);
        inStream.close();
        return result;

    } catch (Exception e) {
        // If we caught an exception, either the class
        // wasn't found or it was unreadable by our process.
        print("### File '" + fileName + "' not found.");
        return null;
    }
}

} // End class
