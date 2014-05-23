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

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

/**
 * Transfer handler for creating trasnferable objects for drag and drop for the toolbox.
 */

public class BeanTransferHandler extends TransferHandler
{

	BeanTransferHandler()
	{
		super();
	}

	@Override
	public boolean canImport(final JComponent comp, final DataFlavor[] transferFlavors)
	{
		return false;
	}

	@Override
	public void exportAsDrag(final JComponent comp, final InputEvent ie, final int action)
	{
		super.exportAsDrag(comp, ie, action);
	}

	@Override
	public void exportDone(final JComponent comp, final Transferable data, final int action)
	{
		if (comp instanceof BeanGraphPanel)
		{
			final BeanGraphPanel bgp = ((BeanGraphPanel) comp);
			bgp.removeSelectedItems();
		}
		else if (comp instanceof JList)
		{
			Component parent = comp;
			while ((parent = parent.getParent()) != null)
			{
				/*
				 * if (parent instanceof BeanGroupPanel) { //System.out.println("CLEARING");
				 * ((BeanGroupPanel) parent).removeSelected(); break; }
				 */
			}
		}

	}

	@Override
	public int getSourceActions(final JComponent c)
	{
		return COPY;
	}

	@Override
	public Icon getVisualRepresentation(final Transferable t)
	{
		if (t instanceof BeanCanvasItem)
		{
			final Icon icon = new ImageIcon(((BeanCanvasItem) t).getIconView());
			System.out.println(icon);
			return icon;
		}
		return super.getVisualRepresentation(t);
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
					final BeanCanvasItemTransferableSupport ts = new BeanCanvasItemTransferableSupport(selected,
							BeanCanvasItemTransferableSupport.beanCanvasItemDataFlavor);
					return ts;
				}
				/*
				 * else if (selected instanceof BeanGroupSupport) { JigsawPieceTransferableSupport
				 * ts = new JigsawPieceTransferableSupport(selected,
				 * JigsawPieceTransferableSupport.beanGroupDataFlavor); return ts; }
				 */
			}
			else
			{
				Info.message(this, "nothing was selected");
			}
		} /*
		 * else if (c instanceof BeanGraphPanel) { Vector items = ((BeanGraphPanel)
		 * c).getSelectedItems(); BeanGroupSupport group = ((JigsawPiece)
		 * items.firstElement()).getGroup(); if (group != null) { JigsawPieceTransferableSupport ts
		 * = new JigsawPieceTransferableSupport(group,
		 * JigsawPieceTransferableSupport.beanGroupDataFlavor); return ts; } }
		 */
		return null;
	}

}
