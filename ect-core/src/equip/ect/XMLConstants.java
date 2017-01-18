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

public interface XMLConstants
{
	String CONTAINER_TAG = "Container";
	String COMPONENT_TAG = "Component";
	String CAPABILITY_TAG = "Capability";
	String COMPONENT_REQUEST_TAG = "ComponentRequest";
	String EXPORTED_CAPABILITIES_TAG = "ExportedCapabilities";
	String RUNNING_COMPONENTS_TAG = "RunningComponents";
	String NAME_ATTRIBUTE = "name";
	String URL_ATTRIBUTE = "url";
	String PERSIST_FILE_ATTRIBUTE = "persistFile";

	String COMPONENT_PROPERTY_TAG = "ComponentProperty";
	String COMPONENT_PROPERTIES_TAG = "ComponentProperties";
	String PROPERTY_NAME_ATTRIBUTE = "propertyName";
	String PROPERTY_VALUE_ATTRIBUTE = "propertyValue";
	String PROPERTY_CLASS_ATTRIBUTE = "propertyClass";

	String GUID_TAG = "GUID";
	String HOST_ID_ATTRIBUTE = "host_id";
	String ITEM_ID_ATTRIBUTE = "item_id";
	String PROC_ID_ATTRIBUTE = "proc_id";
	String TIME_S_ATTRIBUTE = "time_s";
}
