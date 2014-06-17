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

Created by: James Mathrick (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)
  James Mathrick (University of Nottingham)

 */

package equip.ect.webstart;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import equip.discovery.DiscoveryClientAgent;
import equip.discovery.DiscoveryClientAgentImpl;
import equip.discovery.DiscoveryEventListenerImpl;

/**
 * GUI giving user boot options. Created by {@link equip.ect.webstart.Boot} if not auto-starting a known
 * {@link Installation}.
 * 
 * @author jym
 */
class BootFrame extends JFrame
{

	class DataspaceDiscoverer extends DiscoveryEventListenerImpl
	{
		BootFrame parent;

		public DataspaceDiscoverer(final BootFrame p)
		{
			this.parent = p;
		}

		@Override
		public void discoveryEvent(final DiscoveryClientAgent agent, final String url)
		{
			parent.addDiscoveredServer(url);
		}

		@Override
		public void discoveryRemoveEvent(final DiscoveryClientAgent agent, final String url)
		{
			parent.removeDiscoveredServer(url);
		}
	}

	/**
	 * Discovered {@link Installation}s (using multicast discovery).
	 */
	class DiscInstPane extends KnownInstPane
	{
		DiscInstPane(final BootFrame p)
		{
			super(p, p.discoveredInsts.values());
		}

		/**
		 * re-start/re-join. Calls {@link Installation#start}. Disposes of GUI and stops discovery
		 * agent on success.
		 */
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			final List<Installation> installations = list.getSelectedValuesList();
			for (final Installation installation: installations)
			{
				if (!installation.isMaster())
				{
					if (!installation.isMasterAlive()) { return; }
				}
				// password should be in installation boot props already if known?!
				// but no if discovered...
				final JDialog secretDialog = new JDialog(parent, "Installation secret");
				final JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				p.add(new JLabel("Please enter installation secret"));
				final JTextField secretField = new JTextField(20);
				p.add(secretField);
				final AbstractAction doit = new AbstractAction("OK")
				{
					@Override
					public void actionPerformed(final ActionEvent ae)
					{
						final String password = secretField.getText();
						if (!installation.isMaster())
						{
							if (!installation.isMasterAlive(password)) { return; }
						}
						if (BootGlobals.DEBUG)
						{
							System.out.println("BootFrame starting Installation " + installation);
						}
						if (!installation.start(password)) { return; }
						// move to known
						knownInsts.put(installation.getDirectoryName(), installation);
						knownInstsPane.listChanged();
						// removed from discovered
						final Iterator di = discoveredInsts.keySet().iterator();
						boolean done = false;
						while (di.hasNext())
						{
							final String surl = (String) di.next();
							final Installation i = discoveredInsts.get(surl);
							if (i == installation)
							{
								discoveredInsts.remove(surl);
								done = true;
								break;
							}
						}
						if (!done)
						{
							System.err.println("ERROR: could not find installation " + installation
									+ " in discovered list (moving to known)");
						}
						else
						{
							listChanged();
						}
						// agent.stop();
						secretDialog.setVisible(false);
						parent.setVisible(false);
					}
				};
				p.add(new JButton(doit));
				secretField.addActionListener(doit);
				secretDialog.getContentPane().add(p);
				secretDialog.pack();
				secretDialog.setVisible(true);
			}
		}

		@Override
		public void listChanged()
		{
			refreshList(parent.discoveredInsts.values());
		}

		@Override
		public void valueChanged(final ListSelectionEvent e)
		{
			final List<Installation> installations = list.getSelectedValuesList();
			if (installations.isEmpty() || installations.get(0) == null) { return; }
			actionButton.setText("Join Installation");
			actionButton.setEnabled(true);
			this.repaint();
		}
	}

	/**
	 * Tab inner classes for known {@link Installation}s. Allows user to:
	 * <ul>
	 * <li>Restart an {@link Installation} master.</li>
	 * <li>Re-join a running {@link Installation} if not master for it.</li>
	 * </ul>
	 */
	class KnownInstPane extends JPanel implements ActionListener, ListSelectionListener
	{
		protected BootFrame parent;
		protected JList<Installation> list;
		private DefaultListModel<Installation> listModel;
		protected JButton actionButton = new JButton("Please Select Installation");

		KnownInstPane(final BootFrame p, final Collection<Installation> ins)
		{
			super(new BorderLayout());
			this.parent = p;
			listModel = new DefaultListModel<Installation>();
			list = new JList<Installation>();
			list.setVisibleRowCount(10);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(this);
			refreshList(ins);
			this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			final JLabel foo = new JLabel("Please select an Installation to start/join:");
			foo.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
			this.add(foo, BorderLayout.NORTH);
			this.add(new JScrollPane(list), BorderLayout.CENTER);
			actionButton.addActionListener(this);
			actionButton.setEnabled(false);
			this.add(actionButton, BorderLayout.SOUTH);
		}

		/**
		 * re-start/re-join. Calls {@link Installation#start}. Disposes of GUI and stops discovery
		 * agent on success.
		 */
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			for (Installation installation: list.getSelectedValuesList())
			{
				// password should be in installation boot props already if known?!
				// but no if discovered... (which extends this class!)
				if (!installation.isMaster())
				{
					if (!installation.isMasterAlive() && !installation.isDiscoveryDisabled()) { return; }
				}
				if (BootGlobals.DEBUG)
				{
					System.out.println("BootFrame starting Installation " + installation);
				}
				if (!installation.start(null)) { return; }
				// agent.stop();
				parent.setVisible(false);
			}
		}

		public void listChanged()
		{
			refreshList(parent.knownInsts.values());
		}

		public void refreshList(final Collection<Installation> installations)
		{
			listModel.clear();
			for (Installation installation : installations)
			{
				listModel.addElement(installation);
			}
			list.setModel(listModel);
			repaint();
		}

		@Override
		public void valueChanged(final ListSelectionEvent e)
		{
			final List<Installation> installations = list.getSelectedValuesList();
			if (installations.isEmpty() || installations.get(0) == null) { return; }

			final Installation installation = installations.get(0);
			if (installation.isMaster())
			{
				actionButton.setText("Restart Installation");
				actionButton.setEnabled(true);
			}
			else if (installation.isMasterAlive() || installation.isDiscoveryDisabled())
			{
				actionButton.setText("Rejoin Installation");
				actionButton.setEnabled(true);
			}
			else
			{
				actionButton.setText("Master is not alive");
				actionButton.setEnabled(false);
				list.clearSelection();
			}

			this.repaint();
		}
	}

	/**
	 * New Installation pane. Allows specification of {@link Installation} name.
	 */
	class NewInstPane extends JPanel implements ActionListener, FocusListener
	{
		BootFrame parent;
		JTextField instName = new JTextField(20);
		JTextField secretField = new JTextField(20);
		JButton actionButton = new JButton("Create New Installation...");

		NewInstPane(final BootFrame p)
		{
			super();
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			this.parent = p;
			actionButton.setEnabled(false);
			this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			final JLabel foo = new JLabel("Please enter the name of an Installation to create:");
			foo.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
			this.add(foo);
			final JPanel bar = new JPanel(new BorderLayout());
			instName.addFocusListener(this);
			bar.add(instName);
			this.add(bar);
			this.add(new JLabel("Installation secret"));
			this.add(secretField);
			secretField.setText(equip.data.Challenge.makeSecret());
			final JPanel buttons = new JPanel();
			buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
			this.add(buttons);
			buttons.add(actionButton);
			actionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					System.out.println("Create new master...");
					final JFrame fslave = new JFrame("Create new master");
					fslave.getContentPane().setLayout(new BorderLayout());
					fslave.getContentPane().add(new JLabel("Installation File-Download URL:"), BorderLayout.NORTH);
					final JTextField text = new JTextField(40);
					text.setEditable(true);
					final String dl = System.getProperty("download", "");
					text.setText(dl);
					fslave.getContentPane().add(text, BorderLayout.CENTER);
					fslave.getContentPane().add(new JButton(new AbstractAction("OK")
					{
						@Override
						public void actionPerformed(final ActionEvent ae)
						{
							final String dl2 = text.getText();
							System.setProperty("download", dl2);
							System.out.println("Update default download url to " + dl2);
							fslave.setVisible(false);
							NewInstPane.this.actionPerformed(ae);
						}
					}), BorderLayout.SOUTH);
					fslave.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					fslave.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosing(final WindowEvent e)
						{
							System.out.println("Cancel create slave");
							fslave.setVisible(false);
						}
					});
					fslave.pack();
					fslave.setVisible(true);
				}
			});
			buttons.add(new JButton(new AbstractAction("Join Remote Installation...")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					System.out.println("Create remote slave...");
					final JFrame fslave = new JFrame("Create remote slave");
					fslave.getContentPane().setLayout(new BoxLayout(fslave.getContentPane(), BoxLayout.Y_AXIS));
					fslave.getContentPane().add(new JLabel("Installation Webserver URL:"));
					final JTextField dltext = new JTextField(40);
					dltext.setEditable(true);
					final String dl = System.getProperty("download");
					dltext.setText("http://:8088/");
					fslave.getContentPane().add(dltext);
					fslave.getContentPane().add(new JLabel("Enter Installation Dataspace URL"));
					final JTextField text = new JTextField(40);
					text.setEditable(true);
					fslave.getContentPane().add(text);
					fslave.getContentPane().add(new JButton(new AbstractAction("OK")
					{
						@Override
						public void actionPerformed(final ActionEvent ae)
						{
							final String dl2 = dltext.getText() + "download/";
							System.setProperty("download", dl2);
							System.out.println("Update default download url to " + dl2);

							final String dsurl = text.getText();
							System.out.println("Create slave for DS " + dsurl);
							fslave.setVisible(false);

							if (instName.getText().equals("")) { return; }
							if (BootGlobals.DEBUG)
							{
								System.out.println("BootFrame creating slave Installation " + instName.getText());
							}
							final Installation foo = new Installation(instName.getText(), dsurl);
							if (BootGlobals.DEBUG)
							{
								System.out.println("BootFrame starting Installation " + instName.getText());
							}
							final boolean s = foo.start(secretField.getText());
							// restore!?
							System.setProperty("download", dl);
							if (!s) { return; }
							// agent.stop();
							parent.setVisible(false);
							instName.setText("");
							secretField.setText(equip.data.Challenge.makeSecret());
							knownInsts.put(foo.getDirectoryName(), foo);
							knownInstsPane.listChanged();
							System.err.println("Added " + foo.getDirectoryName() + " to known insts");
						}
					}));
					fslave.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					fslave.addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosing(final WindowEvent e)
						{
							System.out.println("Cancel create slave");
							fslave.setVisible(false);
						}
					});
					fslave.pack();
					fslave.setVisible(true);
				}
			}));
		}

		/**
		 * create new master {@link Installation} with specified name. Also disposes of GUI and ends
		 * multicast discovery.
		 */
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (instName.getText().equals("")) { return; }
			if (BootGlobals.DEBUG)
			{
				System.out.println("BootFrame creating Installation " + instName.getText());
			}
			final Installation foo = new Installation(instName.getText(), true);
			if (BootGlobals.DEBUG)
			{
				System.out.println("BootFrame starting Installation " + instName.getText());
			}
			if (!foo.start(secretField.getText())) { return; }
			// agent.stop();
			parent.setVisible(false);
			instName.setText("");
			secretField.setText(equip.data.Challenge.makeSecret());
			knownInsts.put(foo.getDirectoryName(), foo);
			knownInstsPane.listChanged();
			System.err.println("Added " + foo.getDirectoryName() + " to known insts");
		}

		@Override
		public void focusGained(final FocusEvent e)
		{
			actionButton.setText("Create New Installation...");
			actionButton.setEnabled(true);
			this.repaint();
		}

		@Override
		public void focusLost(final FocusEvent e)
		{

		}
	}

	// data
	protected Map<String, Installation> knownInsts = new HashMap<String, Installation>();
	protected Map<String, Installation> discoveredInsts = new HashMap<String, Installation>();

	// Swing components
	JTabbedPane contentPane;
	DiscInstPane discInsts;

	KnownInstPane knownInstsPane;

	/**
	 * main cons, called from {@link equip.ect.webstart.Boot}. Creates {@link equip.ect.webstart.BootFrame.KnownInstPane}, {@link equip.ect.webstart.BootFrame.DiscInstPane} and
	 * {@link equip.ect.webstart.BootFrame.NewInstPane}. Also starts discovery {@link #startupDiscovery()}
	 * 
	 * @param ki
	 *            Known {@link Installation} objects, created from existing directories on start-up.
	 */
	public BootFrame(final Map<String, Installation> ki)
	{
		super("ECT Boot Manager");
		this.knownInsts = ki;
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
		help.add(new AbstractAction("Show installation files...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final JFileChooser ch = new JFileChooser(BootGlobals.getLocalFilestoreRoot());
				ch.showOpenDialog(BootFrame.this);
			}
		});
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(menus, BorderLayout.NORTH);
		if (BootGlobals.DEBUG)
		{
			System.err.println("Setting up all host discovery...");
		}
		contentPane = new JTabbedPane(SwingConstants.BOTTOM);
		knownInstsPane = new KnownInstPane(this, knownInsts.values());
		contentPane.addTab("Previous", knownInstsPane);
		discInsts = new DiscInstPane(this);
		contentPane.addTab("Discovered", discInsts);
		contentPane.addTab("New", new NewInstPane(this));
		this.getContentPane().add(contentPane, BorderLayout.CENTER);
		this.pack();
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// this.setVisible(true);
		startupDiscovery();
	}

	public Map<String, Installation> getKnownInsts()
	{
		return knownInsts;
	}

	/**
	 * update {@link equip.ect.webstart.BootFrame.DiscInstPane} list of {@link Installation}s.
	 */
	protected void addDiscoveredServer(final String surl)
	{
		if (BootGlobals.DEBUG)
		{
			System.out.println("Discovered: " + surl);
		}
		if (surl.lastIndexOf('*') < 0)
		{
			if (BootGlobals.DEBUG)
			{
				System.out.println("Foreign equip dataspace found, ignoring:  " + surl);
			}
			return;
		}
		// If previously known installation comes to life...
		final String folderName = BootGlobals.clobberGroupName(surl.substring(	surl.indexOf('*') + 1,
																				surl.lastIndexOf('*')));
		System.out.println("-> folder name " + folderName);
		if (knownInsts.containsKey(folderName))
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("...already known!");
			}
			// Update the installation
			knownInsts.get(folderName).setMasterAlive(true);
			knownInsts.get(folderName).setDataspaceUrl(surl);
			knownInstsPane.listChanged();
			return;
		}
		final Installation foo = new Installation(surl, false);
		discoveredInsts.put(surl, foo);
		this.discInsts.listChanged();
	}

	/**
	 * update {@link equip.ect.webstart.BootFrame.DiscInstPane} list of {@link Installation}s.
	 */
	protected void removeDiscoveredServer(final String surl)
	{
		if (surl.lastIndexOf('*') < 0) { return; }
		if (BootGlobals.DEBUG)
		{
			System.out.println("Discovery Server died: " + surl);
		}
		// If previously known installation dies...
		final String folderName = BootGlobals.clobberGroupName(surl.substring(	surl.indexOf('*') + 1,
																				surl.lastIndexOf('*')));
		if (knownInsts.containsKey(folderName))
		{
			if (BootGlobals.DEBUG)
			{
				System.err.println("...previously known Installation!");
			}
			// Update the installation
			knownInsts.get(folderName).setMasterAlive(false);
			knownInstsPane.listChanged();
			return;
		}
		discoveredInsts.remove(surl);
		this.discInsts.listChanged();
	}

	// Discovery
	private void startupDiscovery()
	{
		DiscoveryClientAgent agent = new DiscoveryClientAgentImpl();
		DataspaceDiscoverer discoverer = new DataspaceDiscoverer(this);
		agent.startDefault(discoverer, new String[]{BootGlobals.DEFAULT_SERVICE}, new String[]{""});

	}
}