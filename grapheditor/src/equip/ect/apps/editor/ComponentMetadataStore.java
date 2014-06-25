/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2006, University of Nottingham All rights reserved.
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
 * Created by: Stefan Rennick Egglestone (University of Nottingham)
 * 
 * Contributors: 
 *  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.apps.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import equip.data.GUID;
import equip.ect.apps.editor.dataspace.ComponentMetadataListener;

/**
 * Allows metadata events for a particular component to be stored until required.
 * <P>
 * When applications such as the display editor start up, they often register to receive events from
 * the dataspace with information about metadata relating to a particular component. However, since
 * the dataspace does not gurantee the delivery order of events, then these events can arrive before
 * the event indicating the component has been created! In this case, applications can use an
 * instance of ComponentMetadataStore to store these events and to replay them when required.
 * </P>
 */

public class ComponentMetadataStore
{
	public static final String META_DATA_ADDED = "meta_data_added";
	public static final String META_DATA_UPDATED = "meta_data_updated";
	public static final String META_DATA_DELETED = "meta_data_deleted";

	private final Map<GUID, List<Object>> guidToMetadata = new HashMap<GUID, List<Object>>();
	private final Map<GUID, List<String>> guidToType = new HashMap<GUID, List<String>>();

	public synchronized void addItem(final GUID guid, final String type, final Object metadata)
	{
		List<Object> metadataVector;
		List<String> typeVector;

		if (!(guidToMetadata.containsKey(guid)))
		{
			metadataVector = new ArrayList<Object>();
			guidToMetadata.put(guid, metadataVector);

			typeVector = new ArrayList<String>();
			guidToType.put(guid, typeVector);
		}
		else
		{
			metadataVector = guidToMetadata.get(guid);
			typeVector = guidToType.get(guid);
		}

		metadataVector.add(metadata);
		typeVector.add(type);
	}

	public synchronized boolean containsMetadataFor(final GUID guid)
	{
		if (guidToMetadata.containsKey(guid))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public synchronized void dropMetadataFor(final GUID guid)
	{
		guidToMetadata.remove(guid);
		guidToType.remove(guid);
	}

	public synchronized void replayMetadata(final GUID guid, final ComponentMetadataListener cml)
	{
		if (guidToMetadata.containsKey(guid))
		{
			final List<Object> metadataVector = guidToMetadata.get(guid);
			final List<String> typeVector = guidToType.get(guid);

			for (int i = 0; i < metadataVector.size(); i++)
			{
				final Object metadataObject = metadataVector.get(i);
				final String type = typeVector.get(i);

				if (type.equals(META_DATA_ADDED))
				{
					cml.componentMetadataAdded(metadataObject);
					continue;
				}

				if (type.equals(META_DATA_UPDATED))
				{
					cml.componentMetadataUpdated(metadataObject);
					continue;
				}

				if (type.equals(META_DATA_DELETED))
				{
					cml.componentMetadataDeleted(metadataObject);
					continue;
				}
			}
			guidToMetadata.remove(guid);
			guidToType.remove(guid);
		}
	}
}