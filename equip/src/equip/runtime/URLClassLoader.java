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

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;

/**
 * Loads class bytes from a URL (probably inherited from elsewhere).
 *
 */
public class URLClassLoader extends MultiClassLoader {

//---------- Private Fields ------------------------------
private String    urlString;

//---------- Initialization ------------------------------
public URLClassLoader(String urlString) {
    this.urlString = urlString;
}
//---------- Abstract Implementation ---------------------
protected byte[] loadClassBytes(String className) {

    className = formatClassName(className);
    try {
        URL url = new URL(urlString + className);
        URLConnection connection = url.openConnection();
        if (sourceMonitorOn) {
            print("Loading from URL: " + connection.getURL() );
        }
        monitor("Content type is: " + connection.getContentType());

        InputStream inputStream = connection.getInputStream();
        int length = connection.getContentLength();
        monitor("InputStream length = " + length); // Failure if -1

        byte[] data = new byte[length];
        inputStream.read(data); // Actual byte transfer
        inputStream.close();
        return data;

    } catch(Exception ex) {
        print("### URLClassLoader.loadClassBytes() - Exception:");
        ex.printStackTrace();
        return null;
    }
}

} // End class
