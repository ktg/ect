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

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;

import equip.ect.Capability;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.PropertyLinkRequest;

public abstract class BeanChoicePanel extends BasicPanel implements ComponentListener, ComponentPropertyListener,
		ComponentMetadataListener, DataspaceConfigurationListener
{

	protected final JList<BeanCanvasItem> beanList;
	protected final BeanListModel<BeanCanvasItem> listModel;

	protected static Map<String, BeanCanvasItem> templates = new HashMap<String, BeanCanvasItem>();

	public static Map<String, BeanCanvasItem> getTemplates()
	{
		return templates;
	}

	/**
	 * enables this component to be a Drag Source
	 */
	// DragSource dragSource = null;
	public BeanChoicePanel()
	{
		super("Active Components");

		// Construct the bean list
		// beanList = new
		// TexturedJList(GraphEditorResources.BACKGROUND_TEXTURE);
		listModel = new BeanListModel<BeanCanvasItem>();

		beanList = new JList<BeanCanvasItem>();

		beanList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		beanList.setVisibleRowCount(-1); // hack to make it work, java bug
		beanList.setModel(listModel);
		beanList.setCellRenderer(new BeanCellRenderer());
		// beanList.setFixedCellHeight(80);
		// beanList.setFixedCellWidth(80);

		add(BorderLayout.CENTER, beanList);

		/*
		 * Drag and Drop: JList have automatic drag and drop as of 1.4. However it currently doesn't
		 * react to drags until a selection is valid (after a mouse release). We want to initiate a
		 * drag at once, so use our own mouse listener for this and turn off automatic dnd.
		 */
		beanList.setTransferHandler(new BeanTransferHandler());
		beanList.setDragEnabled(true); // turn off automatic drag and drop
		/*
		 * MouseListener ml = new MouseAdapter() { public void mousePressed(MouseEvent e) {
		 * JComponent c = (JComponent)e.getSource(); // Info.message(this, "COMP: " + c);
		 * TransferHandler th = c.getTransferHandler(); th.exportAsDrag(c, e, TransferHandler.COPY);
		 * } }; beanList.addMouseListener(ml);
		 */

		DataspaceMonitor.getMonitor().addComponentListener(this);
		DataspaceMonitor.getMonitor().addComponentPropertyListener(this);
		DataspaceMonitor.getMonitor().addComponentMetadataListener(this);
		DataspaceMonitor.getMonitor().addDataspaceConfigurationListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#capabilityAdded(ect.Capability)
	 */
	@Override
	public void capabilityAdded(final Capability cap)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#capabilityDeleted(ect.Capability)
	 */
	@Override
	public void capabilityDeleted(final Capability cap)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#capabilityUpdated(ect.Capability)
	 */
	@Override
	public void capabilityUpdated(final Capability cap)
	{
	}

	@Override
	public void componentAdvertUpdated(ComponentAdvert compAd)
	{

	}

	@Override
	public void componentAdvertAdded(final ComponentAdvert compAdvert)
	{
		// avoid duplicates
		if (templates.containsKey(compAdvert.getComponentID().toString())) { return; }
		final BeanCanvasItem newComp = createComponentTemplate(compAdvert);
		templates.put(compAdvert.getComponentID().toString(), newComp);
		listModel.removeElement(newComp);
	}

	@Override
	public void componentAdvertDeleted(final ComponentAdvert compAdvert)
	{
		final BeanCanvasItem oldComp = templates.remove(compAdvert.getComponentID().toString());
		listModel.removeElement(oldComp);
	}

	public void componentMetadataChanged(final Object metadata)
	{
	}

	@Override
	public void componentPropertyAdded(final ComponentProperty compProp)
	{
	}

	@Override
	public void componentPropertyDeleted(final ComponentProperty compProp)
	{
	}

	public void componentPropertyUpdated(final ComponentProperty compProp)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ect.apps.editor.DataspaceConfigurationListener#componentRequestAdded(ect.
	 * ComponentRequest)
	 */
	@Override
	public void componentRequestAdded(final ComponentRequest compReq)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ect.apps.editor.DataspaceConfigurationListener#componentRequestDeleted(ect.
	 * ComponentRequest)
	 */
	@Override
	public void componentRequestDeleted(final ComponentRequest compReq)
	{
	}

	public abstract BeanCanvasItem createComponentTemplate(ComponentAdvert compAdvert);

	/*
	 * (non-Javadoc)
	 * 
	 * @see ect.apps.editor.DataspaceConfigurationListener#propertyLinkRequestAdded(ect.
	 * PropertyLinkRequest)
	 */
	@Override
	public void propertyLinkRequestAdded(final PropertyLinkRequest linkReq)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#propertyLinkRequestDeleted(ect
	 * .PropertyLinkRequest)
	 */
	@Override
	public void propertyLinkRequestDeleted(final PropertyLinkRequest linkReq)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ect.apps.editor.DataspaceConfigurationListener#propertyLinkRequestUpdated(ect
	 * .PropertyLinkRequest)
	 */
	@Override
	public void propertyLinkRequestUpdated(final PropertyLinkRequest linkReq)
	{
	}
}
