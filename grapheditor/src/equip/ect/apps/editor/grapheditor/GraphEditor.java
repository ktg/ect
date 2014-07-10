/*
 <COPYRIGHT>

 Copyright (c) 2004-2006, University of Nottingham
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
 Chris Greenhalgh (University of Nottingham)
 Jan Humble (University of Nottingham)

 */

package equip.ect.apps.editor.grapheditor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import equip.ect.apps.configurationmgr.ConfigurationManager;
import equip.ect.apps.editor.BeanGraphPanel;
import equip.ect.apps.editor.ComponentBrowser;
import equip.ect.apps.editor.EditorResources;
import equip.ect.apps.editor.Info;
import equip.ect.apps.editor.SelectionModel;
import equip.ect.apps.editor.dataspace.DataspaceMonitor;
import equip.ect.apps.editor.interactive.CleanerTask;
import equip.ect.apps.editor.interactive.InteractiveCanvas;
import equip.ect.apps.editor.interactive.InteractiveCanvasManager;
import equip.ect.apps.editor.state.ProgressDialog;
import equip.ect.apps.editor.state.State;
import equip.ect.apps.editor.state.StateManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GraphEditor extends JFrame
{
	class SettingsDialog extends JDialog implements ActionListener
	{
		private final JFormattedTextField tf;

		SettingsDialog(final GraphEditor parent)
		{
			super(parent, "Editor Settings", true);
			final JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			final JPanel panel = new JPanel(new GridLayout(3, 2));
			JCheckBox cb = new JCheckBox("Show property values", GraphComponentPropertyView.renderPropValue);
			cb.addActionListener(this);
			panel.add(cb);
			cb = new JCheckBox("Animate property updates", BeanGraphPanel.animatePropertyUpdate);
			cb.addActionListener(this);
			panel.add(cb);
			cb = new JCheckBox("Allow self-connect", GraphEditorCanvas.allowComponentSelfConnect);
			cb.addActionListener(this);
			panel.add(cb);
			final JPanel ttkP = new JPanel();
			cb = new JCheckBox();
			ttkP.add(cb);
			ttkP.add(new JLabel("Remove idle items after "));
			tf = new JFormattedTextField((int) (CleanerTask.timeUntilKill / 1000.0));
			tf.setColumns(3);
			tf.addActionListener(this);
			tf.setActionCommand("TIME_TO_KILL");
			cb.addActionListener(this);

			ttkP.add(tf);
			ttkP.add(new JLabel("secs"));
			ttkP.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(ttkP);

			mainPanel.add(panel);
			final JPanel dataspacePanel = new JPanel();
			final JButton b = new JButton("Reconnect");
			dataspacePanel.add(b);
			final JTextField dataspaceTF = new JTextField("equip://<insert address>", 25);
			dataspacePanel.add(dataspaceTF);
			mainPanel.add(dataspacePanel);
			getContentPane().add(mainPanel);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			pack();
		}

		@Override
		public void actionPerformed(final ActionEvent ae)
		{
			final String command = ae.getActionCommand();
			if (command.equals("Show property values"))
			{
				GraphComponentPropertyView.renderPropValue = ((AbstractButton) ae.getSource()).isSelected();
				getActiveCanvas().repaint();
				settings.setProperty("RENDER_PROP_VALUE", String.valueOf(GraphComponentPropertyView.renderPropValue));
			}
			else if (command.equals("Animate property updates"))
			{
				BeanGraphPanel.animatePropertyUpdate = ((AbstractButton) ae.getSource()).isSelected();
				settings.setProperty("ANIMATE_PROP_UPDATE", String.valueOf(BeanGraphPanel.animatePropertyUpdate));
			}
			else if (command.equals("Allow self-connect"))
			{
				GraphEditorCanvas.allowComponentSelfConnect = ((AbstractButton) ae.getSource()).isSelected();
				settings.setProperty("ALLOW_COMP_SELF_CONNECT",
						String.valueOf(GraphEditorCanvas.allowComponentSelfConnect));
			}
			else if (command.equals("TIME_TO_KILL"))
			{
				final Long value = (Long) tf.getValue();
				CleanerTask.timeUntilKill = value * 1000;
				settings.setProperty("TIME_UNTIL_ITEM_CLEAN", String.valueOf(CleanerTask.timeUntilKill));
			}
			else if (command.equals("AUTO_CLEAN_UP"))
			{
				final boolean autoClean = ((JCheckBox) ae.getSource()).isSelected();
				tf.setEnabled(autoClean);
				GraphEditorResources.autoCleanComponents = autoClean;
				if (autoClean)
				{
					final Long value = (Long) tf.getValue();
					CleanerTask.timeUntilKill = value * 1000;
				}
				settings.setProperty("AUTO_CLEAN_COMPONENTS", String.valueOf(autoClean));
			}
		}
	}

	static GraphEditor instance;
	static boolean fullscreen;

	/**
	 * You coould have several instances for controlling different spaces simultaneously.
	 */
	public static GraphEditor getInstance()
	{
		if (instance == null)
		{
			instance = new GraphEditor();
		}
		return instance;
	}

	public static void main(final String[] args)
	{
		try
		{
			String url = "equip://:9123";
			if (args.length > 0)
			{
				for (final String arg : args)
				{
					if (arg.equals("-fullscreen"))
					{
						fullscreen = true;
					}
					else if (arg.equals("-debug"))
					{
						Info.setDebug(true);
					}
					else
					{
						url = arg;
					}
				}
			}

			final GraphEditor editor = GraphEditor.getInstance();
			editor.setSize(500, 500);
			editor.setLocationRelativeTo(null); // should center on screen
			editor.connect(url);

			// create configuration manager
			editor.configurationManager = new ConfigurationManager(DataspaceMonitor.getMonitor().getDataspace());

			editor.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(final WindowEvent we)
				{
					System.exit(0);
				}
			});

			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					editor.exit();
				}
			});
			editor.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static boolean getBooleanProperty(final String key, final boolean defaultValue, final Properties settings)
	{
		final String boolString = settings.getProperty(key);
		if (boolString == null)
		{
			return defaultValue;
		}
		return Boolean.parseBoolean(boolString);
	}

	static long getLongProperty(final String key, final long defaultValue, final Properties settings)
	{
		final String intString = settings.getProperty(key);
		if (intString == null)
		{
			return defaultValue;
		}
		return Long.parseLong(intString);
	}

	private Properties settings = new Properties();

	private ConfigurationManager configurationManager;

	private final InteractiveCanvasManager canvasManager;

	private final SelectionModel selectionModel = new SelectionModel();

	private JTabbedPane pane;

	private GraphEditor()
	{
		super("ECT Graph Editor");
		final JToolBar toolbar = new JToolBar();

		toolbar.setFloatable(false);
		toolbar.setBorderPainted(false);
		toolbar.setRollover(true);
		toolbar.add(new AbstractAction("Clear", EditorResources.createImageIcon(EditorResources.CLEAR_ICON, "Clear"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				if (clearConfiguration() == JOptionPane.YES_OPTION)
				{
					configurationManager.clearConfiguration();
				}
			}
		});
		toolbar.add(new AbstractAction("Load", EditorResources.createImageIcon(EditorResources.LOAD_ICON, "Load"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				/*
				 * int option = clearConfiguration(); if (option == JOptionPane.CANCEL_OPTION)
				 * return; if (option == JOptionPane.YES_OPTION)
				 * configurationManager.clearConfiguration();
				 */

				final File file = configurationManager.chooseOpenFile(getActiveCanvas());
				if (file != null)
				{
					try
					{
						configurationManager.clearConfiguration();

						BufferedReader reader = new BufferedReader(new FileReader(file));
						if(reader.readLine().startsWith("<?xml"))
						{
							reader.close();
							configurationManager.restoreConfiguration(file);
						}
						else
						{
							reader.close();

							Gson gson = new GsonBuilder().create();
							final State state = gson.fromJson(new FileReader(file), State.class);

							new Thread(new Runnable() {
								@Override
								public void run()
								{
									StateManager.restoreState(state, GraphEditor.this, new ProgressDialog());
									updateCanvasPane();
								}
							}).start();
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		toolbar.add(new AbstractAction("Save", EditorResources.createImageIcon(EditorResources.SAVE_ICON, "Save"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File file = configurationManager.chooseSaveFile(getActiveCanvas());
				if (file != null)
				{
					try
					{
						State state = StateManager.createState(canvasManager);
						Gson gson = new GsonBuilder().create();

						FileWriter writer = new FileWriter(file);
						gson.toJson(state, writer);
						writer.flush();
						writer.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		});
		toolbar.addSeparator();
		toolbar.add(new AbstractAction("New View", EditorResources.createImageIcon(EditorResources.ADD_TAG, "New View"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				addCanvas("New View");
			}
		});
		toolbar.add(new AbstractAction("Rename View", EditorResources.createImageIcon(EditorResources.RENAME_TAG, "Rename View"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String canvasName = JOptionPane.showInputDialog(GraphEditor.this, "Enter new name for view:", getActiveCanvas().getName());
				canvasManager.renameCanvas(canvasName, getActiveCanvas());
				updateCanvasPane();
			}
		});
		toolbar.add(new AbstractAction("Delete View", EditorResources.createImageIcon(EditorResources.DELETE_TAG, "Delete View"))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				removeCanvas(getActiveCanvas());
			}
		});

		toolbar.addSeparator();

		toolbar.add(new AbstractAction("Settings...", EditorResources.createImageIcon(EditorResources.SETTINGS_ICON, "Settings..."))
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				showSettingsDialog();
			}
		});

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(300);
		splitPane.setContinuousLayout(true);
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(true);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);
		getContentPane().add(toolbar, BorderLayout.PAGE_START);

		this.canvasManager = new InteractiveCanvasManager(new GraphEditorCanvas("Editor", selectionModel));

		pane = new JTabbedPane();
		pane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				if (pane.getSelectedIndex() > -1)
				{

					final String activeCanvasName = canvasManager.getActiveCanvas().getName();

					final String tabName = pane.getTitleAt(pane.getSelectedIndex());

					if (!(activeCanvasName.equals(tabName)))
					{
						// find the canvas with the selected
						// name and set it as being the active
						// canvas

						setActiveCanvas((GraphEditorCanvas) (canvasManager.getCanvas(tabName)));
					}

				}
			}
		});

		updateCanvasPane();
		splitPane.setRightComponent(pane);
		splitPane.setLeftComponent(new ComponentBrowser(selectionModel));

		loadSettings();
	}

	public GraphEditorCanvas addCanvas(final String name)
	{
		final GraphEditorCanvas canvas = new GraphEditorCanvas(name, selectionModel);
		canvasManager.addCanvas(name, canvas);
		updateCanvasPane();
		return canvas;
	}

	public void connect(final String url)
	{
		getActiveCanvas().startDaemon();
		DataspaceMonitor.getMonitor().startListening(url);
	}

	public void removeCanvas(final BeanGraphPanel canvas)
	{
		//final String name = canvas.getName();
		canvasManager.removeCanvas(canvas);
		updateCanvasPane();
	}

	public void exit()
	{
		System.out.println("Graph Editor shutting down ...");
		storeSettings();
		System.out.println("Goodbye.");
	}

	public GraphEditorCanvas getCanvas(String name)
	{
		return (GraphEditorCanvas)canvasManager.getCanvas(name);
	}

	public GraphEditorCanvas getActiveCanvas()
	{
		return (GraphEditorCanvas) canvasManager.getActiveCanvas();
	}

	public void loadSettings()
	{
		try
		{
			final File file = new File(GraphEditorResources.SETTINGS_FILE);
			final FileInputStream fis = new FileInputStream(file);
			settings.load(fis);
			GraphComponentPropertyView.renderPropValue = getBooleanProperty("RENDER_PROP_VALUE",
					GraphComponentPropertyView.renderPropValue,
					settings);
			GraphEditorCanvas.allowComponentSelfConnect = getBooleanProperty("ALLOW_COMP_SELF_CONNECT",
					GraphEditorCanvas.allowComponentSelfConnect,
					settings);
			BeanGraphPanel.animatePropertyUpdate = getBooleanProperty("ANIMATE_PROP_UPDATE",
					BeanGraphPanel.animatePropertyUpdate, settings);
			CleanerTask.timeUntilKill = getLongProperty("TIME_UNTIL_ITEM_CLEAN", CleanerTask.timeUntilKill, settings);
		}
		catch (final FileNotFoundException fnfe)
		{
			Info.message("Warning: Could not find settings file, using defaults.");
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	public void setActiveCanvas(final GraphEditorCanvas canvas)
	{
		canvasManager.setActiveCanvas(canvas);
		ToolTipManager.sharedInstance().registerComponent(canvas);
	}

	public void showSettingsDialog()
	{
		final SettingsDialog sd = new SettingsDialog(this);
		sd.setLocationRelativeTo(this);
		sd.setVisible(true);
	}

	public synchronized void storeSettings()
	{
		try
		{
			final FileOutputStream fos = new FileOutputStream(GraphEditorResources.SETTINGS_FILE);
			settings.store(fos, "Graph Editor Settings");
		}
		catch (final FileNotFoundException fnfe)
		{
			Info.message("Warning: Could not find settings file, using defaults.");
		}
		catch (final IOException ioe)
		{
			ioe.printStackTrace();
		}
	}

	protected int clearConfiguration()
	{
		return JOptionPane.showConfirmDialog(getActiveCanvas(), "Delete all components?");
	}

	protected void updateCanvasPane()
	{

		final InteractiveCanvas[] canvases = canvasManager.getCanvases();

		// first, search through all of the current tabs to
		// see if any are no longer required

		final List<Component> componentsToRemove = new ArrayList<Component>();

		for (int i = 0; i < pane.getTabCount(); i++)
		{
			final String tabName = pane.getTitleAt(i);
			boolean found = false;

			for (final InteractiveCanvas canvase : canvases)
			{
				if (canvase.getName().equals(tabName))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				componentsToRemove.add(pane.getComponentAt(i));
			}
		}

		for (Component aComponentsToRemove : componentsToRemove)
		{
			pane.remove(aComponentsToRemove);
		}

		for (int i = 0; i < canvases.length; i++)
		{
			// if canvas is not already in pane ...
			if (pane.indexOfTab(canvases[i].getName()) < 0)
			{
				final JScrollPane scrollPane = new JScrollPane(canvases[i]);
				scrollPane.setViewportView(canvases[i]);

				pane.addTab(canvases[i].getName(), scrollPane);

				if (canvases[i] == canvasManager.getActiveCanvas())
				{
					pane.setSelectedIndex(i);
				}
			}
		}
	}
}
