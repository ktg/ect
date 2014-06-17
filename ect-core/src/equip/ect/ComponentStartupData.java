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
package equip.ect;

import java.io.File;

import equip.data.GUID;

public class ComponentStartupData
{

	// guids to reuse...for persistence support
	private GUID componentGUID = null;
	private ComponentProperty[] properties = null;
	private String name = null;
	private String jarURL = null;
	private GUID requestGUID = null;
	private File persistFile = null;

	public ComponentStartupData()
	{
	}

	public ComponentStartupData(final String name, final String jarURL, final GUID componentGUID,
			final GUID requestGUID, final ComponentProperty[] properties, final File persistFile)
	{

		this.componentGUID = componentGUID;
		this.properties = properties;
		this.requestGUID = requestGUID;
		this.name = name;
		this.jarURL = jarURL;
		this.persistFile = persistFile;
	}

	public ComponentStartupData(final String name, final String jarURL, final GUID componentGUID,
			final GUID requestGUID, final ComponentProperty[] properties, final String persistFile)
	{

		this(name, jarURL, componentGUID, requestGUID, properties, persistFile == null ? null : new File(persistFile));
	}

	public GUID getComponentGUID()
	{
		return componentGUID;
	}

	public String getJarURL()
	{
		return jarURL;
	}

	public String getName()
	{
		return name;
	}

	public File getPersistFile()
	{
		return persistFile;
	}

	public ComponentProperty[] getProperties()
	{
		return properties;
	}

	public GUID getRequestGUID()
	{
		return requestGUID;
	}

	public void setComponentGUID(final GUID componentGUID)
	{
		this.componentGUID = componentGUID;
	}

	public void setJarURL(final String jarURL)
	{
		this.jarURL = jarURL;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public void setPersistFile(final File persistFile)
	{
		this.persistFile = persistFile;
	}

	public void setProperties(final ComponentProperty[] properties)
	{
		this.properties = properties;
	}

	public void setRequestGUID(final GUID requestGUID)
	{
		this.requestGUID = requestGUID;
	}

}