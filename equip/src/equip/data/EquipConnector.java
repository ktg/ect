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
package equip.data;

import equip.net.Moniker;
import equip.net.ServerURL;
import equip.data.DataProxy;
import equip.data.DataProxyImpl;
import equip.data.GUIDFactory;
import equip.data.GUID;
import equip.data.ItemDataImpl;
import equip.data.GUIDFactoryImpl;

import java.lang.reflect.Field;

/**
 * This object provides a simple connection to the Equip data service
 * (this is effectively Tom's alternative to the {@link
 * equip.data.beans.IDataspace} helper API).
 *
 * This essentiall handles all of the details associated with making
 * the connectiony and provides a handle to access the basic
 * properties of the connection. It also provides a convience method
 * for creating new equip objects derived from ItemData.
 *
 * @author Tom Rodden
 * @author Ian Taylor
 *
 * @version $Revision: 1.1.1.1 $
 *
 * $Id: EquipConnector.java,v 1.1.1.1 2005/03/08 16:17:19 cgreenhalgh Exp $
 * 
 * $Log: EquipConnector.java,v $
 * Revision 1.1.1.1  2005/03/08 16:17:19  cgreenhalgh
 * From Nottingham CVS
 *
 * Revision 1.9  2005/03/08 14:36:41  cmg
 * added BSD license
 *
 * Revision 1.8  2003/10/22 15:00:42  cmg
 * added initial support for Leased items (requires synchronized clocks)
 *
 * Revision 1.4  2003/06/23 11:51:35  cmg
 * at least one comment per class!
 *
 * Revision 1.3  2002/08/28 09:21:19  ianm
 * added close method to terminate connection
 *
 * Revision 1.2  2002/02/10 20:31:07  ianm
 * tidied error messages and reformatted
 *
 * Revision 1.1  2001/11/15 23:59:44  ianm
 * initial
 *
 * Revision 1.1  2001/10/29 14:58:02  ianm
 * initial
 *
 */
public class EquipConnector {
	
	// The main points of contact
	public DataProxy dataservice;
	public GUIDFactory idFactory;

	protected boolean DEBUG = false;

	public EquipConnector(String serverUrl) {

		// resolve server URL
		Moniker serviceMoniker = null;
		ServerURL url = new ServerURL(serverUrl);
		serviceMoniker = url.getMoniker();
		if (serviceMoniker==null) {
			System.err.println("ERROR: could not understand url: "+serverUrl);
			System.exit(-1);
		}
		String checkUrl = url.getURL();
		if (checkUrl==null)
            System.err.println("ERROR: Could not get back url from ServerURL");
		else
            if (DEBUG) System.err.println("ServerURL -> "+checkUrl);

		// GUID factory
		idFactory = new GUIDFactoryImpl();

        // create data proxy
		dataservice = new DataProxyImpl();
		dataservice.serviceMoniker = serviceMoniker;
		dataservice.setDefaultAgent(idFactory.getUnique());
        
		// activate data proxy
        if (serviceMoniker!=null) {
			if (DEBUG) System.out.println("Activate...");
			// if (!dataservice.activate(null, null)) {
            if (!dataservice.activate(new ECDeactivateCallback(), null)) {
                System.err.println("ERROR: could not activate");
                System.exit(-1);
			}
if (DEBUG)
			System.out.println("Running...");
		}
	}

    class ECDeactivateCallback extends equip.net.DeactivateCallback {
        public void notifyDeactivate(equip.net.ServiceProxy proxy, equip.runtime.ValueBase closure) {
            System.out.println("EC deactivated");
            return;
        }
    }

    /**
     * closes connection to equip
     */
    public void close() {
        dataservice.deactivate();
        dataservice = null;
        idFactory = null;
    }


        // This makes an equip object and sets name and id for user
        public Object makeEquipObject(Class objectClass) {

            String className = objectClass.getName();
            String propertyImplClass = className + "Impl";

            Class propImpl;

            try {
           	propImpl = Class.forName(propertyImplClass);

            } catch (ClassNotFoundException e) {
                System.err.println("ERROR: Class "+ objectClass +" does not have Impl - " + e);
                e.printStackTrace();
                return null;
            }

            Object returnObjt;

            try {
            	returnObjt = propImpl.newInstance();

            } catch (Exception e) {
                System.err.println("ERROR: Could not create new "+propImpl+"instance - " + e);
                e.printStackTrace();
                return null;
            }

            // if this has an id and name field then set them
            if ( propImpl.isAssignableFrom(ItemDataImpl.class) ) {
                try {
                    Field idParam = propImpl.getField("id");
                    idParam.set(returnObjt,idFactory.getUnique());
                    Field nameParam = propImpl.getField("name");
                    nameParam.set(returnObjt,className);
                } catch (Exception e) {
                    System.err.println( "ERROR: Could not set field - " + e);
                    e.printStackTrace();
                    return null;
                }
                return returnObjt;
            } else {
                System.err.println( "ERROR: Not derived from ItemData");
                return null;
            }
        }
}
