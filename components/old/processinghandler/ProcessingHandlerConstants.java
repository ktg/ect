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

Created by: Chris Allsop (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  Chris Allsop (University of Nottingham)

 */
package equip.ect.components.processinghandler;

import java.io.File;
import java.io.Serializable;

public interface ProcessingHandlerConstants extends Serializable
{

	/** The .class extension. */
	public final static String CLASS_EXTENSION = ".class";

	/** The .jar extension. */
	public final static String JAR_EXTENSION = ".jar";

	/**
	 * The jar separator, '!/' which must directly follow the .jar extension for jar specific URLs.
	 */
	public final static String JAR_SEPARATOR = "!/";

	/** The 'file:" protocol. */
	public final static String FILE_PROTOCOL = "file:";

	/** The 'http:" protocol. */
	public final static String HTTP_PROTOCOL = "http:";

	/**
	 * The 'jar:" subprotocol, must be prefixed to the file or http protocol for jar specific URLs.
	 */
	public final static String JAR_SUBPROTOCOL = "jar:";

	/**
	 * The superclass name that all processing Applets inherit from for versions up to 0069
	 * <p>
	 * <i>Note:</i>This may be prior to changes in future releases of <a
	 * href="http://processing.org">Processing</a>
	 */
	public final static String BAPPLET_NAME = "BApplet";

	/**
	 * The superclass name that all processing Applets inherit from for versions up to 0069
	 * <p>
	 * <i>Note:</i>This may be prior to changes in future releases of <a
	 * href="http://processing.org">Processing</a>
	 */
	public final static String PAPPLET_NAME = "processing.core.PApplet";

	/**
	 * The list of parameters that the BApplet Constructor takes.
	 * <p>
	 * <i>Note:</i>This may be prior to changes in future releases of <a
	 * href="http://processing.org">Processing</a>
	 */
	public final static Class[] BAPPLET_CONSTRUCTOR_PARAMETERS = new Class[] {};

	/**
	 * The suffix used to identify those fields and/or methods added at runtime to a class using
	 * javassist.
	 */
	public final static String DYNAMIC_IDENTIFIER = "__dynamic";

	/**
	 * The prefix used to identify duplicated fields added at runtime to a class for use by the
	 * poll_dynamic() method.
	 */
	public final static String OLD_IDENTIFIER = "old__";

	/** The class suffix used to identify a java bean's beaninfo class. */
	public final static String BEAN_INFO_SUFFIX = "BeanInfo";

	/**
	 * The directory where component jars are stored and monitored by the component browser.
	 */
	public final static File COMPONENTS_DIRECTORY = new File("./components");

	/**
	 * A subdirectory for storing runtime generated capabilities in the directory where component
	 * jars are stored and monitored by component browser.
	 */
	public final static File COMPONENTS_SUBDIRECTORY = new File(COMPONENTS_DIRECTORY + "/runtimeGenerated");

}
