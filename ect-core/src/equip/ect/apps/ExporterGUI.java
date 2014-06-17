/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Nottingham
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

Created by: Shahram Izadi (University of Nottingham)
Contributors:
  Tom Rodden (University of Nottingham)
  Chris Greenhalgh (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */
package equip.ect.apps;

import equip.data.DataManager;
import equip.data.DataProxy;
import equip.ect.ContainerManager;
import equip.ect.ContainerManagerHelper;
import equip.ect.util.DirectoryEventListener;
import equip.ect.webstart.Boot;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI Application which is the main (current) Java container.
 */
public class ExporterGUI extends JPanel implements DirectoryEventListener
{
	public static final String JAR_SUFFIX = ".jar";
	private static final Dimension defaultSize = new Dimension(225, 400);
	private static final int delay = 1000;

	/**
	 * app main. Usage: [ dataspaceUrl [ [ componentsDir persistFile ] hostname ] ]. Default
	 * dataspace url "equip://:9123".
	 */
	public static void main(final String args[]) throws Exception
	{
		final JFrame f = new JFrame("ECT Java Container");
		f.getContentPane().add(new JLabel("Starting up - please wait"));
		f.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				System.exit(0);
			}
		});
		f.pack();
		f.setVisible(true);

		if (args.length > 4)
		{
			Boot.redirectOutput(args[4]);
		}

		System.out.println("ECT Java Container");
		initialiseJavaxComm();

		ExporterGUI gui;
		if (args.length == 1)
		{
			gui = new ExporterGUI(args[0]);
		}
		else if (args.length == 2)
		{
			gui = new ExporterGUI(args[0], args[1]);
		}
		else if (args.length == 4 || args.length == 5)
		{
			gui = new ExporterGUI(args[0], args[1], args[2], args[3]);

		}
		else
		{
			gui = new ExporterGUI("equip://:9123");
		}
		final JFrame frame = new JFrame("ECT Java Container");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		final JMenuBar menus = new JMenuBar();
		final JMenu help = new JMenu("Help");
		menus.add(help);
		help.add(new AbstractAction("Show console...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				Boot.showOutput();
			}
		});
		frame.getContentPane().add(menus, BorderLayout.NORTH);
		frame.getContentPane().add(gui, BorderLayout.CENTER);
		frame.pack();
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				System.exit(0);
			}
		});
		f.dispose();
		frame.setVisible(true);
		// clean shutdown
		try
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			while (true)
			{
				final String line = in.readLine();
				if (line != null && (line.startsWith("q") || line.startsWith("Q")))
				{
					System.err.println("Exiting on null/q input...");
					System.exit(0);
				}
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR doing clean terminate input: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * force javax.comm initialisation
	 */
	private static void initialiseJavaxComm()
	{
		/*
		 * - problem with win32com.dll being loaded in multiple class loaders...?! String
		 * drivernames[] = new String [] {"com.sun.comm.Win32Driver"}; for(int i=0;
		 * i<drivernames.length; i++) { String drivername = drivernames[i]; try { java.lang.Object
		 * driver = Class.forName(drivername).newInstance(); java.lang.reflect.Method init =
		 * driver.getClass().getMethod("initialize", new Class[0]); init.invoke(driver, new
		 * Object[0]); //((javax.comm.Driver)driver).initialize();
		 * System.out.println("Initialised javax.comm driver "+drivername+" OK"); break; } catch
		 * (Exception e) { System.out.println
		 * ("ERROR initialising javax.comm driver "+drivername+": "+e.getMessage ()); } }
		 */
	}

	private class CellRenderer extends JLabel implements ListCellRenderer<File>
	{
		public CellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(final JList list, final File value, final int index,
		                                              final boolean isSelected, final boolean cellHasFocus)
		{
			setText(value.getName());
			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return this;
		}
	}

	private final List<File> importing = new ArrayList<File>();
	private final JList<File> compList = new JList<File>();
	private final DefaultListModel<File> listModel = new DefaultListModel<File>();
	private final DataProxy dataspace;
	private ContainerManagerHelper containerHelper = null;

	public ExporterGUI(final String dataSpaceURL) throws IOException
	{
		this(dataSpaceURL, InetAddress.getLocalHost().getHostName());
	}

	public ExporterGUI(final String dataSpaceURL, final String hostname) throws IOException
	{
		this(dataSpaceURL, null, null, hostname);
	}

	public ExporterGUI(final String dataSpaceURL, final String componentsDirectory, final String persistFile,
	                   final String hostname) throws IOException
	{
		dataspace = DataManager.getInstance().getDataspace(dataSpaceURL, DataManager.DATASPACE_SERVER, true);
		
		//this.hostName = hostname;
		containerHelper = new ContainerManagerHelper(dataSpaceURL, componentsDirectory, persistFile, hostname);
		containerHelper.getDirectoryMonitor().addDirectoryEventListener(this);
		System.out.println("Container helper created");
		JPanel panel = createInnerPanel();
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setPreferredSize(defaultSize);
	}

	@Override
	public void fileAdd(final File file)
	{
	}

	@Override
	public void fileAddComplete(final File file)
	{
		if(containerHelper.canImport(file))
		{
			System.out.println("Adding " + file.toString());
			try
			{
				containerHelper.loadJar(file);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (listModel)
					{
						try
						{
							synchronized (ContainerManager.class)
							{
								containerHelper.exportFromJarFile(file);
							}
						}
						catch (final IOException e)
						{
							System.err.println("ERROR Exporting " + file + ": " + e);
							e.printStackTrace(System.err);
						}
						// in lexical order
						for (int i = 0; i < listModel.getSize(); i++)
						{
							final File f = listModel.getElementAt(i);
							if (f.getName().compareTo(file.getName()) > 0)
							{
								listModel.insertElementAt(file, i);
								return;
							}
						}
						listModel.addElement(file);
					}
				}
			});
		}
	}

	@Override
	public void fileDeleted(final File file)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				listModel.removeElement(file);
			}
		});
	}

	@Override
	public void fileModified(final File file)
	{
	}

	protected JPanel createInnerPanel()
	{
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(compList), BorderLayout.CENTER);
		compList.setModel(listModel);
		compList.setCellRenderer(new CellRenderer());
		final JPanel toolbar = new JPanel(new FlowLayout());
		final JButton exportButton = new JButton("export capabilities");
		panel.add(toolbar, BorderLayout.SOUTH);
		exportButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final Object selected = compList.getSelectedValue();
				if (selected != null)
				{
					try
					{
						synchronized (ContainerManager.class)
						{
							containerHelper.exportFromJarFile((File) selected);
						}
					}
					catch (final IOException ioe)
					{
						ioe.printStackTrace();
					}
				}
			}
		});
		return panel;
	}
}
