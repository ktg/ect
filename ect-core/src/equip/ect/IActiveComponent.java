/*
<COPYRIGHT>

Copyright (c) 2005, University of Nottingham
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

 */
package equip.ect;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;

/**
 * interface to be implemented by a component which wishes to receive additional information about
 * its container and lifecycle, etc.
 */
public interface IActiveComponent
{
	/**
	 * initialise
	 */
	public void initialise(ContainerManager cmgr, DataspaceBean dataspace);

	/**
	 * property link request added to this component
	 */
	public void linkToAdded(String propertyName, GUID requestId);

	/**
	 * property link request added to this component
	 */
	public void linkToDeleted(String propertyName, GUID requestId);

	/**
	 * attempt to set a property due to a link add / source update.
	 * 
	 * @return true if update completely handled (no need to call setter).
	 */
	public boolean linkToUpdated(String propertyName, GUID requestId, Object value);

	/**
	 * more to go?
	 */
	/**
	 * standard stop method for all components (including non-active)
	 */
	public void stop();
}
