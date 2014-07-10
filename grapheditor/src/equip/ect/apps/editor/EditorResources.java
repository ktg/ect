/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Jan Humble (Swedish Institute of Computer Science AB)

 */

package equip.ect.apps.editor;

import javax.swing.ImageIcon;

public abstract class EditorResources
{
	public final static java.awt.Color BACKGROUND_COLOR = java.awt.Color.gray.brighter();

	public final static String TRASH_ICON = "/bin.png";

	public final static String FOLDER_ICON = "/folder.png";

	public final static String COMPONENT_ICON = "/component.png";

	public final static String LINK_ICON = "/link.png";


	public final static String SEARCH_ICON = "/search.png";

	public final static String COMPONENTREQUEST_ICON = "/componentRequest.png";

	public final static String CLEAR_ICON = "/layer_remove.png";

	public final static String SAVE_ICON = "/layer_save.png";

	public final static String LOAD_ICON = "/layer_open.png";

	public final static String ADD_TAG = "/layer_add.png";

	public final static String DELETE_TAG = "/layer_delete.png";

	public final static String RENAME_TAG = "/layer_command.png";

	public final static String SETTINGS_ICON = "/settings.png";

	public static ImageIcon createImageIcon(String path, String description)
	{
		java.net.URL imgURL = EditorResources.class.getResource(path);
		if (imgURL != null)
		{
			return new ImageIcon(imgURL, description);
		}
		else
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}