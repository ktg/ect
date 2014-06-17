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

import equip.data.beans.DataspaceBean;
import equip.ect.apps.configurationmgr.ConfigurationManager;
import equip.ect.apps.editor.*;
import equip.ect.apps.editor.PropertyLinkDialog;
import equip.ect.apps.editor.state.EditorState;
import equip.ect.apps.editor.state.EditorStateManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
			final JPanel mainPanel = new BasicPanel("Settings");
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
			cb = new JCheckBox("Audio effects", AudioManager.audioOn);
			cb.addActionListener(this);
			panel.add(cb);
			cb = new JCheckBox("Show trash", false);
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

			final JPanel cosmeticPanel = new JPanel();
			cosmeticPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.black), "Look & Feek"));
			final JPanel bgTexturePanel = new JPanel();
			bgTexturePanel.add(new JLabel("Background Texture:"));
			final JTextField bgTF = new JTextField();
			bgTexturePanel.add(bgTF);
			final JButton bgB = new JButton("Browse");
			bgB.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(final ActionEvent arg0)
				{
					final JFileChooser chooser = new JFileChooser("./")
					{

						@Override
						public boolean accept(final File file)
						{
							return file.isDirectory() || file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".gif");
						}
					};
					final int returnVal = chooser.showOpenDialog(GraphEditor.this);
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						final File file = chooser.getSelectedFile();
						if (file.exists())
						{

							try
							{
								final String path = file.getCanonicalPath();
								final TexturePaint tp = getActiveCanvas().createImageTexture(path, false);
								if (tp != null)
								{
									bgTF.setText(file.getAbsolutePath());
									settings.setProperty("BACKGROUND_TEXTURE", path);
								}
								else
								{
									Info.message(this, "Could not load background texture file => " + path);
								}
							}
							catch (final IOException e)
							{
								Info.message(this, e.getMessage());
							}

						}

					}
				}
			});

			bgTexturePanel.add(bgB);
			cosmeticPanel.add(bgTexturePanel);
			mainPanel.add(panel);
			mainPanel.add(cosmeticPanel);
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
			else if (command.equals("Audio effects"))
			{
				AudioManager.audioOn = ((AbstractButton) ae.getSource()).isSelected();
				settings.setProperty("AUDIO", String.valueOf(AudioManager.audioOn));
			}
			else if (command.equals("Show trash"))
			{
				getActiveCanvas().showTrash(((AbstractButton) ae.getSource()).isSelected());
				// settings.setProperty("SHOW_TRASH_ICON",
				// String.valueOf(GraphEditorCanvas.allowComponentSelfConnect));
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

	class TabPopup extends JPopupMenu
	{
		TabPopup(final String tabName, final Component parent, final int x, final int y)
		{
			final JMenuItem renameItem = new JMenuItem(new AbstractAction("Rename view")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final String canvasName = JOptionPane.showInputDialog(GraphEditor.this, "Enter new name for view:");

					if (!(canvasName.equals(tabName)))
					{
						canvasManager.renameCanvas(canvasName, canvasManager.getCanvas(tabName));
						updateCanvasPane();
					}
				}
			});

			final JMenuItem deleteItem = new JMenuItem(new AbstractAction("Close view")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					canvasManager.removeCanvas(canvasManager.getCanvas(tabName));
					updateCanvasPane();
				}
			});

			final JMenuItem saveItem = new JMenuItem(new AbstractAction("Save view")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final File file = EditorStateManager.chooseSaveFile(GraphEditor.this);

					if (file != null)
					{
						stateManager.saveState((GraphEditorCanvas) (canvasManager.getCanvas(tabName)), file);
						if (!(file.getName().equals(tabName)))
						{
							canvasManager.renameCanvas(file.getName(), canvasManager.getCanvas(tabName));
							updateCanvasPane();
						}
					}
				}
			});

			add(renameItem);
			add(deleteItem);
			add(saveItem);
			show(parent, x, y);
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
					else if (arg.equals("-audio"))
					{
						AudioManager.audioOn(true);
						// AudioManager.getAudioManager().playSoundResource("welcome");
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
			try
			{
				final File logFile = new File("grapheditor.log");
				logFile.createNewFile();

				e.printStackTrace(new PrintWriter(new FileWriter(logFile)));
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
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

	final BeanChoicePanel bcp;

	private Window propertyLinkDialog, capabilityDialog;

	private Properties settings = new Properties();

	private final GraphEditorStateManager stateManager;

	private ConfigurationManager configurationManager;

	private final InteractiveCanvasManager canvasManager;

	JTabbedPane pane;

	private GraphEditor()
	{
		super("ECT Graph Editor");
		final JPanel mainPanel = new TexturedPanel(new BorderLayout());

		final TexturedPanel top = new TexturedPanel(new BorderLayout());
		bcp = new GraphEditorChoicePanel();
		top.add(BorderLayout.CENTER, bcp);

		mainPanel.add(BorderLayout.NORTH, top);
		getContentPane().add(mainPanel);

		final JMenuBar menuBar = new JMenuBar();
		final JMenu fileMenu = new JMenu("File");

		fileMenu.add(new AbstractAction("Save")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final File file = configurationManager.chooseSaveFile(getActiveCanvas());
				if (file != null)
				{
					configurationManager.saveConfigurationAs(file);
				}
			}
		});

		fileMenu.add(new AbstractAction("Load")
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
					configurationManager.clearConfiguration();
					configurationManager.restoreConfiguration(file);

				}
			}
		});

		fileMenu.add(new AbstractAction("Clear")
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

		fileMenu.addSeparator();

		fileMenu.add(new AbstractAction("Add new view")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String canvasName = JOptionPane.showInputDialog(GraphEditor.this, "Enter name for view:");
				addCanvas(new GraphEditorCanvas(canvasName.trim()));
			}
		});

		fileMenu.add(new AbstractAction("Load view...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				// if (clearLayout() == JOptionPane.CANCEL_OPTION)
				// return;
				// getActiveCanvas().showTrash(false);
				// getActiveCanvas().removeItems(
				// (Vector) (getActiveCanvas().getItems().clone()));
				// getActiveCanvas().showTrash(true);

				final File file = EditorStateManager.chooseOpenFile(GraphEditor.this);
				if (file != null)
				{
					final EditorState state = GraphEditorStateManager.loadState(file);
					if (state != null)
					{
						final String fileName = file.getName();
						final GraphEditorCanvas newCanvas = new GraphEditorCanvas(fileName);

						final String addedName = addCanvas(newCanvas);
						pane.setSelectedIndex(pane.indexOfTab(addedName));

						stateManager.restoreState(newCanvas, state);
					}
				}
			}
		});

		fileMenu.add(new AbstractAction("Clear all views")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				clearLayout();
			}
		});

		fileMenu.addSeparator();
		fileMenu.add(new AbstractAction("Exit")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.exit(0);
			}
		});
		menuBar.add(fileMenu);
		final JMenu viewMenu = new JMenu("View");
		viewMenu.add(new AbstractAction("Capability browser")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				showCapabilityBrowser();
			}
		});
		viewMenu.add(new AbstractAction("Link browser")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				showPropertyLinkBrowser();
			}
		});
		/*
		 * viewMenu.add(new AbstractAction("Compound component editor") { public void
		 * actionPerformed(ActionEvent ae) { showCompoundComponentEditor(); } });
		 */
		viewMenu.addSeparator();
		viewMenu.add(new AbstractAction("Options ...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				showSettingsDialog();
			}
		});
		menuBar.add(viewMenu);
		final JMenu editMenu = new JMenu("Edit");
		editMenu.add(new AbstractAction("Add new compound component...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				CompoundComponentEditor.handleAddNewCompoundComponent(GraphEditor.this);
			}
		});

		menuBar.add(editMenu);
		setJMenuBar(menuBar);
		// panel.add(BorderLayout.CENTER, new JScrollPane(getActiveCanvas()));

		this.canvasManager = new InteractiveCanvasManager(new GraphEditorCanvas("new_view"));

		pane = new JTabbedPane();
		pane.setBackground(mainPanel.getBackground());

		pane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					// loop through all tabs in the tabbed pane, seeing
					// if user has clicked in one

					for (int i = 0; i < pane.getTabCount(); i++)
					{
						final Rectangle r = pane.getBoundsAt(i);
						if (r.contains(e.getPoint()))
						{
							new TabPopup(pane.getTitleAt(i), pane, e.getX(), e.getY());
							break;
						}
					}
				}
			}
		});

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
		mainPanel.add(BorderLayout.CENTER, pane);

		loadSettings();
		showCapabilityBrowser();

		stateManager = new GraphEditorStateManager(this);
		restoreAllStates();
	}

	public String addCanvas(final BeanGraphPanel canvas)
	{
		final String name = canvas.getName();
		final String addedName = canvasManager.addCanvas(name, canvas);
		updateCanvasPane();
		return addedName;
	}

	public void connect(final String url)
	{
		getActiveCanvas().startDaemon();
		DataspaceMonitor.getMonitor().startListening(url);
	}

	public void exit()
	{
		System.out.println("Graph Editor shutting down ...");
		// TODO fix this for all canvases
		stateManager.saveState(getActiveCanvas());
		storeSettings();
		System.out.println("Goodbye.");
	}

	public GraphEditorCanvas getActiveCanvas()
	{
		return (GraphEditorCanvas) canvasManager.getActiveCanvas();
	}

	public InteractiveCanvas[] getCanvases()
	{
		return canvasManager.getCanvases();
	}

	public InteractiveCanvasManager getCanvasManager()
	{
		return this.canvasManager;
	}

	public EditorStateManager getStateManager()
	{
		return this.stateManager;
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
			AudioManager.audioOn = getBooleanProperty("AUDIO", AudioManager.audioOn, settings);
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

	public void removeCanvas(final BeanGraphPanel canvas)
	{
		//final String name = canvas.getName();
		canvasManager.removeCanvas(canvas);
		updateCanvasPane();
	}

	public void setActiveCanvas(final GraphEditorCanvas canvas)
	{
		canvasManager.setActiveCanvas(canvas);
		ToolTipManager.sharedInstance().registerComponent(canvas);
	}

	/**
	 * added by Alastair Hampshire to allow the graph editor to be started by a replaytool viewer
	 */
	public void setConfigurationManager(final DataspaceBean dataspace)
	{
		configurationManager = new ConfigurationManager(dataspace);
	}

	/*
	 * public void showCompoundComponentEditor() { if (compoundComponentDialog == null) {
	 * compoundComponentDialog = new CompoundComponentEditor();
	 * compoundComponentDialog.addWindowListener(new WindowAdapter() { public void
	 * windowClosed(WindowEvent we) {
	 * DataspaceMonitor.getMonitor().removeDataspaceConfigurationListener
	 * ((DataspaceConfigurationListener) compoundComponentDialog);
	 * compoundComponentDialog.dispose(); compoundComponentDialog = null; } }); }
	 * compoundComponentDialog.setLocationRelativeTo(this); compoundComponentDialog.show(); }
	 */
	public void showCapabilityBrowser()
	{
		if (capabilityDialog == null)
		{
			capabilityDialog = new CapabilityDialog();
			capabilityDialog.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(final WindowEvent we)
				{
					DataspaceMonitor.getMonitor()
							.removeDataspaceConfigurationListener((DataspaceConfigurationListener) capabilityDialog);
					capabilityDialog.dispose();
				}
			});
		}
		capabilityDialog.setVisible(true);
		capabilityDialog.toFront();
	}

	public void showPropertyLinkBrowser()
	{
		if (propertyLinkDialog == null)
		{
			propertyLinkDialog = new PropertyLinkDialog();
			propertyLinkDialog.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(final WindowEvent we)
				{
					DataspaceMonitor.getMonitor()
							.removeDataspaceConfigurationListener((DataspaceConfigurationListener) propertyLinkDialog);
					propertyLinkDialog.dispose();
					propertyLinkDialog = null;
				}
			});
		}
		propertyLinkDialog.setLocationRelativeTo(this);
		propertyLinkDialog.setVisible(true);

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

	// return JOptionPane option
	protected int clearConfiguration()
	{
		return JOptionPane.showConfirmDialog(getActiveCanvas(), "Abandon current component configuration?");
	}

	// return JOptionPane option
	protected int clearLayout()
	{
		final int option = JOptionPane.showConfirmDialog(getActiveCanvas(), "Remove all current views?");

		canvasManager.removeAllCanvases();
		updateCanvasPane();

		return option;
	}

	protected void restoreAllStates()
	{

		final String fileName = stateManager.getDefaultFileName(getActiveCanvas());
		final EditorState state = GraphEditorStateManager.loadState(new File(GraphEditorResources.CONFIG_PATH
				+ fileName));
		if (state != null)
		{
			Info.message(this, "Restoring state => " + fileName);
			stateManager.restoreState(getActiveCanvas(), state);
		}
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
