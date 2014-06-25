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
/*
 * BeanTransferHandler, $RCSfile: BeanTransferHandler.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:26 $
 *
 * $Author: chaoticgalen $
 * Original Author: Jan Humble
 * Copyright (c) 2002, Swedish Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import equip.data.GUID;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Transfer handler for creating trasnferable objects for drag and drop for the toolbox.
 */

public class ComponentGUIDTransferHandler extends TransferHandler
{

	ComponentGUIDTransferHandler()
	{
		super();
	}

	@Override
	public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors)
	{
		return false;
	}

	@Override
	public int getSourceActions(final JComponent c)
	{
		return COPY;
	}

	@Override
	public boolean importData(final JComponent comp, final Transferable t)
	{
		if (comp instanceof BeanGraphPanel)
		{
			Object obj = null;
			try
			{
				obj = t.getTransferData(new DataFlavor());
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			if (obj != null && obj instanceof BeanCanvasItem)
			{
				System.out.println("Doing this");
				/*
				 * ((BeanGraphPanel) comp).setBean(((BeanCanvasItem) t).getBeanID(), 0, 0);
				 */
			}
		}
		return true;
	}

	@Override
	protected Transferable createTransferable(final JComponent c)
	{
		if (c instanceof JList)
		{
			final JList list = (JList) c;
			final Object selected = list.getSelectedValue();
			if (selected != null)
			{
				if (selected instanceof BeanCanvasItem)
				{
					return new ComponentGUIDTransferableSupport(((BeanCanvasItem) selected).beanid,
							ComponentGUIDTransferableSupport.componentGUIDDataFlavor);
				}
			}
			else
			{
				Info.message(this, "nothing was selected");
			}
		}
		else if (c instanceof JTree)
		{
			final JTree tree = (JTree) c;
			final Object selected = tree.getLastSelectedPathComponent();
			if (selected instanceof DefaultMutableTreeNode)
			{
				Object userObject = ((DefaultMutableTreeNode) selected).getUserObject();
				if (userObject instanceof GUID)
				{
					return new ComponentGUIDTransferableSupport(userObject.toString(),
							ComponentGUIDTransferableSupport.componentGUIDDataFlavor);
				}
			}
		}
		return null;
	}

}
