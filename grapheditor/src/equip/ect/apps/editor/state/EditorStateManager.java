/*
 <COPYRIGHT>

 Copyright (c) 2005-2006, University of Nottingham
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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)
 Stefan Rennick Egglestone (University of Nottingham);
 */

package equip.ect.apps.editor.state;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import equip.ect.apps.AppsResources;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.ExtensionFileFilter;

public abstract class EditorStateManager
{

	public static File chooseOpenFile(final JFrame parent)
	{
		JFileChooser chooser = null;
		final String defaultDirectory = System.getProperty(AppsResources.DEFAULT_DIR_PROPERTY_NAME);

		if (defaultDirectory == null)
		{
			chooser = new JFileChooser(".");
		}
		else
		{
			chooser = new JFileChooser(defaultDirectory);
		}

		final ExtensionFileFilter filter = new ExtensionFileFilter("state", "Editor state files");
		chooser.setFileFilter(filter);
		final int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
			return chooser.getSelectedFile();
		}
		return null;
	}

	public static File chooseSaveFile(final JFrame parent)
	{
		final String defaultDirectory = System.getProperty(AppsResources.DEFAULT_DIR_PROPERTY_NAME);

		JFileChooser chooser;

		if (defaultDirectory == null)
		{
			chooser = new JFileChooser(".");
		}
		else
		{
			chooser = new JFileChooser(defaultDirectory);
		}

		final ExtensionFileFilter filter = new ExtensionFileFilter("state", "Editor state files");
		chooser.setFileFilter(filter);
		final int returnVal = chooser.showSaveDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File selected = chooser.getSelectedFile();
			if (!selected.getName().endsWith(".state"))
			{
				selected = new File(selected.getParent(), selected.getName() + ".state");
			}
			System.out.println("You chose to open this file: " + selected.getName());
			return selected;
		}
		return null;
	}

	public static void saveState(final EditorState state, final String stateFile)
	{
		final File file = new File(stateFile);
		state.saveToFile(file);
	}

	protected final Map<String, EditorState> editorStates;

	public EditorStateManager()
	{
		editorStates = new HashMap<String, EditorState>();
	}

	public EditorState getState(final BeanGraphPanel canvas)
	{
		return getState(canvas.getName());
	}

	public EditorState getState(final String canvasName)
	{
		if (canvasName == null) { return null; }
		return editorStates.get(canvasName);
	}

	public abstract void restoreState(BeanGraphPanel canvas, EditorState state);

}
