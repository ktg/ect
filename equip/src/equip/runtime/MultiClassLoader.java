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
import java.util.Hashtable;

/**
 * A simple test class loader capable of loading from multiple
 * sources, such as local files or a URL (probably inherited from
 * elsewhere).
 *
 * Must be subclassed and the abstract method loadClassBytes()
 * implemented to provide the preferred source. */
public abstract class MultiClassLoader extends ClassLoader {

//---------- Fields --------------------------------------
private Hashtable classes = new Hashtable();
private char      classNameReplacementChar;

protected boolean   monitorOn = false;
protected boolean   sourceMonitorOn = true;

//---------- Initialization ------------------------------
public MultiClassLoader() {
}
//---------- Superclass Overrides ------------------------
/**
 * This is a simple version for external clients since they
 * will always want the class resolved before it is returned
 * to them.
 */
public Class loadClass(String className) throws ClassNotFoundException {
    return (loadClass(className, true));
}
//---------- Abstract Implementation ---------------------
public synchronized Class loadClass(String className,
        boolean resolveIt) throws ClassNotFoundException {

    Class   result;
    byte[]  classBytes;
    monitor(">> MultiClassLoader.loadClass(" + className + ", " + resolveIt + ")");

    //----- Check our local cache of classes
    result = (Class)classes.get(className);
    if (result != null) {
        monitor(">> returning cached result.");
        return result;
    }

    //----- Check with the primordial class loader
    try {
        result = super.findSystemClass(className);
        monitor(">> returning system class (in CLASSPATH).");
	classes.put(className, result);
        return result;
    } catch (ClassNotFoundException e) {
        monitor(">> Not a system class.");
    }

    //----- Try our own class loader
    try {
	result = this.getClass().getClassLoader().loadClass(className);
        monitor(">> returning own class loader class.");
	classes.put(className, result);
        return result;
    } catch (Exception e) {
	monitor(">> Not available to our class loader.");
    }
    //----- Try to load it from preferred source
    // Note loadClassBytes() is an abstract method
    classBytes = loadClassBytes(className);
    if (classBytes == null) {
        throw new ClassNotFoundException();
    }

    //----- Define it (parse the class file)
    result = defineClass(classBytes, 0, classBytes.length);
    if (result == null) {
        throw new ClassFormatError();
    }

    //----- Resolve if necessary
    if (resolveIt) resolveClass(result);

    // Done
    classes.put(className, result);
    monitor(">> Returning newly loaded class.");
    return result;
}
//---------- Public Methods ------------------------------
/**
 * This optional call allows a class name such as
 * "COM.test.Hello" to be changed to "COM_test_Hello",
 * which is useful for storing classes from different
 * packages in the same retrival directory.
 * In the above example the char would be '_'.
 */
public void setClassNameReplacementChar(char replacement) {
    classNameReplacementChar = replacement;
}
//---------- Protected Methods ---------------------------
protected abstract byte[] loadClassBytes(String className);

protected String formatClassName(String className) {
    if (classNameReplacementChar == '\u0000') {
        // '/' is used to map the package to the path
        return className.replace('.', '/') + ".class";
    } else {
        // Replace '.' with custom char, such as '_'
        return className.replace('.',
            classNameReplacementChar) + ".class";
    }
}
protected void monitor(String text) {
    if (monitorOn) print(text);
}
//--- Std
protected static void print(String text) {
    System.out.println(text);
}

} // End class
