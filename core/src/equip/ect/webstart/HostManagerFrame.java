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
  Shahram Izadi (University of Nottingham)
  Jan Humble (University of Nottingham)
  James Mathrick (University of Nottingham)
  Stefan Rennick Egglestone (University of Nottingham)
 */

package equip.ect.webstart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * GUI giving admin interface to host. Currently largely unimplemented.
 * 
 * @author jym
 */
class HostManagerFrame extends JFrame
{
	class DailyTask extends TimerTask
	{
		int hours;
		int minutes;

		Date date;

		DailyTask(final Date date)
		{
			this.date = date;
		}

		@Override
		public void run()
		{
			restartContainers();

			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_YEAR, 1);

			setDailyRestart(cal.getTime());
		}

		Date getNextRestart()
		{
			return date;
		}
	}

	class FixedSizeFilter extends DocumentFilter
	{
		int maxSize;

		// limit is the maximum number of characters allowed.
		public FixedSizeFilter(final int limit)
		{
			maxSize = limit;
		}

		// This method is called when characters are inserted into the document
		@Override
		public void insertString(final FilterBypass fb, final int offset, final String str,
				final AttributeSet attr) throws BadLocationException
		{
			replace(fb, offset, 0, str, attr);
		}

		// This method is called when characters in the document are replace with other characters
		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, final String str,
				final AttributeSet attrs) throws BadLocationException
		{
			final int newLength = fb.getDocument().getLength() - length + str.length();
			if (newLength <= maxSize)
			{
				fb.replace(offset, length, str, attrs);
			}
			else
			{
				throw new BadLocationException("New characters exceeds max size of document", offset);
			}
		}
	}

	class PeriodicTask extends TimerTask
	{
		int hours;
		int minutes;

		PeriodicTask(final int hours, final int minutes)
		{
			super();
			this.hours = hours;
			this.minutes = minutes;
		}

		@Override
		public void run()
		{
			restartContainers();
			setPeriodicRestart(hours, minutes);
		}

		long calculateDelayMillis()
		{
			return ((minutes * 60000) + (hours * 3600000));
		}
	}

	class RestartScheduler extends JFrame
	{
		JLabel infoLabel;
		JLabel instrLabel;
		JTextField minutesField;
		JTextField hoursField;

		final String PERIODIC_TEXT = "Schedule periodic reset (hours:mins)";
		final String DAILY_TEXT = "Schedule daily reset (hours:mins)";

		JButton scheduleButton;

		RestartScheduler()
		{
			super("Schedule a container restart");

			setDefaultCloseOperation(DISPOSE_ON_CLOSE);

			getContentPane().setLayout(new BorderLayout());

			final Box northPanel = new Box(BoxLayout.X_AXIS);

			final JPanel southPanel = new JPanel();
			southPanel.setLayout(new FlowLayout());

			getContentPane().add(northPanel, BorderLayout.CENTER);
			getContentPane().add(southPanel, BorderLayout.SOUTH);

			scheduleButton = new JButton(new AbstractAction("schedule")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final int hours = Integer.parseInt(hoursField.getText());
					final int minutes = Integer.parseInt(minutesField.getText());

					if (infoLabel.getText().equals(PERIODIC_TEXT))
					{
						setPeriodicRestart(hours, minutes);
					}
					else
					{
						setInitialDailyRestart(hours, minutes);
					}

					dispose();
				}
			});

			scheduleButton.setEnabled(false);
			southPanel.add(scheduleButton);

			final JButton cancelButton = new JButton(new AbstractAction("cancel")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					dispose();
				}
			});
			southPanel.add(cancelButton);

			southPanel.setBorder(BorderFactory.createRaisedBevelBorder());
			southPanel.setBackground(Color.LIGHT_GRAY);

			final ButtonGroup buttonGroup = new ButtonGroup();

			final JRadioButton buttonPeriodic = new JRadioButton(new AbstractAction("Periodic restart")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					switchToPeriodic();
				}
			});

			final JRadioButton buttonDaily = new JRadioButton(new AbstractAction("Daily restart")
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					switchToDaily();

				}
			});

			buttonGroup.add(buttonPeriodic);
			buttonGroup.add(buttonDaily);

			final Box northWestPanel = new Box(BoxLayout.Y_AXIS);
			northWestPanel.add(buttonPeriodic);
			northWestPanel.add(buttonDaily);

			final Box northEastPanel = new Box(BoxLayout.Y_AXIS);

			infoLabel = new JLabel("some text");
			northEastPanel.add(infoLabel);

			final JPanel timePanel = new JPanel();
			timePanel.setLayout(new FlowLayout());

			instrLabel = new JLabel("some text");

			hoursField = new JTextField("0", 2);

			final AbstractDocument hoursDoc = (AbstractDocument) hoursField.getDocument();
			hoursDoc.setDocumentFilter(new FixedSizeFilter(2));

			hoursField.addCaretListener(new CaretListener()
			{
				@Override
				public void caretUpdate(final CaretEvent e)
				{
					validateTextFields();
				}
			});

			minutesField = new JTextField("0", 2);

			final AbstractDocument minutesDoc = (AbstractDocument) minutesField.getDocument();
			minutesDoc.setDocumentFilter(new FixedSizeFilter(2));

			minutesField.addCaretListener(new CaretListener()
			{
				@Override
				public void caretUpdate(final CaretEvent e)
				{
					validateTextFields();
				}
			});

			timePanel.add(instrLabel);
			timePanel.add(hoursField);
			timePanel.add(new JLabel(":"));
			timePanel.add(minutesField);

			northEastPanel.add(timePanel);

			northPanel.add(northWestPanel);
			northPanel.add(northEastPanel);

			// simulates a user clicking on the periodic button -
			// needed so that we can set an initial choice

			buttonPeriodic.doClick();

			pack();

			// setSize(300,200);
			setVisible(true);
		}

		void switchToDaily()
		{
			infoLabel.setText(DAILY_TEXT);
			instrLabel.setText("Reset at: ");
			validateTextFields();
		}

		void switchToPeriodic()
		{
			infoLabel.setText(PERIODIC_TEXT);
			instrLabel.setText("Reset every: ");
			validateTextFields();
		}

		void validateDaily(final int hours, final int minutes)
		{
			if (hours > 23)
			{
				scheduleButton.setEnabled(false);
				return;
			}

			if (minutes > 59)
			{
				scheduleButton.setEnabled(false);
				return;
			}

			scheduleButton.setEnabled(true);
		}

		void validatePeriodic(final int hours, final int minutes)
		{
			if (minutes > 59)
			{
				scheduleButton.setEnabled(false);
				return;
			}

			if ((hours == 0) && (minutes == 0))
			{
				scheduleButton.setEnabled(false);
				return;
			}

			scheduleButton.setEnabled(true);
		}

		void validateTextFields()
		{

			final String hourText = hoursField.getText();
			final String minutesText = minutesField.getText();

			int hoursInt;
			int minutesInt;

			try
			{
				hoursInt = Integer.parseInt(hourText);

				if (hoursInt < 0)
				{
					scheduleButton.setEnabled(false);
					return;
				}

				minutesInt = Integer.parseInt(minutesText);

				if (minutesInt < 0)
				{
					scheduleButton.setEnabled(false);
					return;
				}
			}
			catch (final NumberFormatException e)
			{
				scheduleButton.setEnabled(false);
				return;
			}

			if (infoLabel.getText().equals(PERIODIC_TEXT))
			{
				validatePeriodic(hoursInt, minutesInt);
			}
			else
			{
				validateDaily(hoursInt, minutesInt);
			}
		}
	}

	class RestartTimer extends java.util.Timer
	{
		void scheduleDailyTask(final DailyTask task)
		{
			final Date date = task.getNextRestart();
			super.schedule(task, date);
		}

		void schedulePeriodicTask(final PeriodicTask task)
		{
			final long timeMillis = task.calculateDelayMillis();
			super.schedule(task, timeMillis);
		}
	}

	/**
	 * container Table model
	 */
	protected class ContainerTableModel extends AbstractTableModel
	{
		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public String getColumnName(final int column)
		{
			switch (column)
			{
				case 0:
				default:
					return "Name";
				case 1:
					return "Auto-start";
				case 2:
					return "Status";
			}
		}

		@Override
		public int getRowCount()
		{
			synchronized (containers)
			{
				return containers.size();
			}
		}

		@Override
		public Object getValueAt(final int row, final int column)
		{
			synchronized (containers)
			{
				if (row < 0 || row >= containers.size()) { return "null"; }
				final Container c = (Container) containers.elementAt(row);
				switch (column)
				{
					case 0:
					default:
						return c.getContainerName();
					case 1:
						return parent.initiator.isContainerAutostart(c.getContainerDirectory()) ? "auto" : "manual";
					case 2:
						return c.isRunning() ? "Running" : (c.isCrashed() ? "Crashed" : "Stopped");
				}
			}
		}
	}

	/**
	 * host manager
	 */
	protected HostManager parent;

	/**
	 * normal cons. Creates GUI.
	 */

	protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

	protected final String NO_RESTARTS = " no restarts currently scheduled";
	protected JButton cancelButton;
	protected JLabel restartLabel;
	protected static final boolean DUMP_HEAP = false;
	/**
	 * status label
	 */
	protected JLabel statusLabel;
	/**
	 * my container table model
	 */
	protected ContainerTableModel model;
	/**
	 * container list
	 */
	protected Vector containers = new Vector();
	RestartTimer timer;

	public HostManagerFrame(final HostManager parent)
	{
		super("Installation Manager");
		this.parent = parent;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// TODO next week ... whole of HostManagerFrame.java
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel l = new JLabel("Installation");
		l.setAlignmentX(0.0f);
		p.add(l);
		final JTextField f = new JTextField(parent.initiator.toString(), 40);
		f.setAlignmentX(0.0f);
		f.setEditable(false);
		p.add(f);
		if (parent.initiator.isMaster())
		{
			l = new JLabel("Dataspace Server");
			l.setAlignmentX(0.0f);
			p.add(l);
		}
		else if (parent.initiator.getDataspace() != null)
		{
			final DataspaceConnectedPanel ds = new DataspaceConnectedPanel(parent.initiator.getDataspace());
			ds.setAlignmentX(0.0f);
			p.add(ds);
		}
		final JCheckBox autostart = new JCheckBox(new AbstractAction("Auto-start installation")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final boolean start = ((JCheckBox) ae.getSource()).isSelected();
				System.out.println("Autostart: " + start);
				// ....
			}
		});
		autostart.setAlignmentX(0.0f);
		// p.add(autostart);
		final JButton exb = new JButton(new AbstractAction("Export shared secret")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final JFrame f = new JFrame("Shared secret");
				final JTextField tf = new JTextField(parent.initiator.getSharedSecret());
				tf.setEditable(false);
				f.getContentPane().add(tf);
				f.pack();
				f.setVisible(true);
			}
		});
		exb.setAlignmentX(0.0f);
		p.add(exb);
		p.add(new JSeparator());
		l = new JLabel("Containers");
		l.setAlignmentX(0.0f);
		p.add(l);
		final JPanel contp = new JPanel();
		contp.setAlignmentX(0.0f);
		contp.setLayout(new FlowLayout(FlowLayout.LEFT));
		contp.add(new JButton(new AbstractAction("Stop all")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				System.out.println("Stop all containers...");
				synchronized (containers)
				{
					for (int i = 0; i < containers.size(); i++)
					{
						final Container c = (Container) containers.elementAt(i);
						if (c.isRunning())
						{
							setStatus("Stopping container " + c);
							c.stop();
							updateContainerStatus(c);
							setStatus("Done");
						}
					}
				}
			}
		}));
		contp.add(new JButton(new AbstractAction("(Re)start all")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				restartContainers();
			}
		}));

		contp.add(new JButton(new AbstractAction("Schedule restart")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				new RestartScheduler();
			}
		}));

		cancelButton = new JButton(new AbstractAction("Cancel restart")
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				cancelRestart();

			}
		});

		cancelButton.setEnabled(false);
		contp.add(cancelButton);

		p.add(contp);

		final Box restartPanel = new Box(BoxLayout.X_AXIS);

		restartPanel.add(new JLabel("Next restart:"));
		restartPanel.setAlignmentX(0.0f);

		restartLabel = new JLabel(NO_RESTARTS);
		restartPanel.add(restartLabel);

		p.add(restartPanel);

		final JTable table = new JTable(model = new ContainerTableModel());
		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				handle(e, true);
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				handle(e, false);
			}

			protected void handle(final MouseEvent e, final boolean pushed)
			{
				// System.out.println("Popup Handler "+e);
				if (e.isPopupTrigger())
				{
					final int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
					// System.out.println("popup row "+row);
					if (row >= 0)
					{
						final JPopupMenu popup = new JPopupMenu();
						synchronized (containers)
						{
							final Container c = (Container) containers.elementAt(row);
							if (c.isRunning())
							{
								popup.add(new JMenuItem(new AbstractAction("Stop")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										setStatus("Stopping container " + c);
										c.stop();
										updateContainerStatus(c);
										setStatus("Done");
									}
								}));
								popup.add(new JMenuItem(new AbstractAction("Restart")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										setStatus("Restarting container " + c);
										c.restart();
										updateContainerStatus(c);
										setStatus("Done");
									}
								}));
							}
							else
							{
								popup.add(new JMenuItem(new AbstractAction("Start")
								{
									@Override
									public void actionPerformed(final ActionEvent ae)
									{
										setStatus("Starting container " + c);
										c.start(parent);
										updateContainerStatus(c);
										setStatus("Done");
									}
								}));
							}
							popup.add(new JMenuItem(new AbstractAction("Toggle autostart")
							{
								@Override
								public void actionPerformed(final ActionEvent ae)
								{
									System.out.println("Toggle autostart...");
									parent.initiator.setContainerAutostart(c.getContainerDirectory(), !parent.initiator
											.isContainerAutostart(c.getContainerDirectory()));
									updateContainerStatus(c);
								}
							}));
						}
						popup.show((Component) e.getSource(), e.getX(), e.getY());
					}
				}
			}
		});
		table.setPreferredScrollableViewportSize(new java.awt.Dimension(400, 400));
		final JScrollPane sp = new JScrollPane(table);
		p.add(sp);
		sp.setAlignmentX(0.0f);
		p.add(new JSeparator());
		l = new JLabel("Tools");
		p.add(l);
		l.setAlignmentX(0.0f);
		final JPanel toolp = new JPanel();
		toolp.setAlignmentX(0.0f);
		toolp.setLayout(new FlowLayout(FlowLayout.LEFT));

		// tools...
		/*
		 * File path = new
		 * File(parent.initiator.getPath()+File.separator+"java"+File.separator+"tools"); if
		 * (!path.isDirectory()) { System.err.println("Tools directory not found ("+path+")"); }
		 * else { File tooljars [] = path.listFiles(); for (int fi=0; fi<tooljars.length; fi++) {
		 * System.out.println("Tool jar "+tooljars[fi]+"..."); if (!tooljars[fi].exists() ||
		 * !tooljars[fi].canRead() || !tooljars[fi].getName().endsWith(".jar")) {
		 * System.out.println("Ignore/can't read tool jar "+tooljars[fi]); continue; }
		 * java.util.zip.ZipFile zin = null; try { Vector classes = new Vector(); zin = new
		 * java.util.zip.ZipFile(tooljars[fi]); java.util.Enumeration ze = zin.entries();
		 * while(ze.hasMoreElements()) { java.util.zip.ZipEntry zen =
		 * (java.util.zip.ZipEntry)ze.nextElement(); String name = zen.getName(); if
		 * (name.endsWith(".class")) { classes.addElement(name);
		 * System.out.println("Found class file "+name); } else
		 * System.out.println("Ignored entry "+name); } try { zin.close(); } catch (Exception ee) {}
		 * } catch (Exception e) { try { zin.close(); } catch (Exception ee) {}
		 * System.err.println("ERROR reading tool jar "+tooljars[fi]+": "+e); } // .... } }
		 */
		/*
		 * toolp.add(new JButton(new AbstractAction("Component Browser") { public void
		 * actionPerformed(ActionEvent ae) { setStatus("Starting dataspace browser");
		 * execJava("ect.apps.browser.Browser "+parent.initiator.getDataspaceUrl());
		 * setStatus("Done"); } }));
		 */
		toolp.add(new JButton(new AbstractAction("Graph Editor")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setStatus("Starting graph editor");
				execJava("ect.apps.editor.grapheditor.GraphEditor " + parent.initiator.getDataspaceUrl());
				setStatus("Done");
			}
		}));

		toolp.add(new JButton(new AbstractAction("Display Editor")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setStatus("Starting display editor");
				execJava("ect.apps.editor.displayeditor.DisplayEditor " + parent.initiator.getDataspaceUrl());
				setStatus("Done");
			}
		}));

		/*
		 * toolp.add(new JButton(new AbstractAction("Time View") { public void
		 * actionPerformed(ActionEvent ae) { setStatus("Starting time view");
		 * execJava("ect.apps.timeview.Main "+parent.initiator.getDataspaceUrl());
		 * setStatus("Done"); } }));
		 */
		toolp.add(new JButton(new AbstractAction("Configuration Manager")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setStatus("Starting configuration manager");
				execJava("ect.apps.configurationmgr.ConfigurationManager " + parent.initiator.getDataspaceUrl()
						+ " " + parent.initiator.getPath());
				setStatus("Done");
			}
		}));
		p.add(toolp);
		p.add(new JSeparator());
		final JButton b = new JButton(new AbstractAction("Terminate Installation")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				setStatus("Terminating installation");
				System.out.println("Request to terminate installation");
				terminate();
			}
		});
		p.add(b);
		b.setAlignmentX(0.0f);
		p.add(new JSeparator());
		statusLabel = new JLabel("Status...");
		statusLabel.setAlignmentX(0.0f);
		p.add(statusLabel);
		final JMenuBar menus = new JMenuBar();
		final JMenu help = new JMenu("Debug");
		menus.add(help);
		final JMenu config = new JMenu("Configuration");
		menus.add(config);
		help.add(new AbstractAction("Show console...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				Boot.showOutput();
			}
		});
		config.add(new AbstractAction("Export Dataspace URL...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final JFrame f = new JFrame("Dataspace URL");
				final JTextField tf = new JTextField(parent.initiator.getDataspaceUrl());
				tf.setEditable(false);
				f.getContentPane().add(tf);
				f.pack();
				f.setVisible(true);
			}
		});
		config.add(new AbstractAction("Export Download URL...")
		{
			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final JFrame f = new JFrame("Download URL");
				final JTextField tf = new JTextField(parent.initiator.getDownloadUrl());
				tf.setEditable(false);
				f.getContentPane().add(tf);
				f.pack();
				f.setVisible(true);
			}
		});
		if (parent.initiator.isMaster())
		{
			help.add(new AbstractAction("Request slave file upload")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					parent.initiator.requestSlaveUpload();
				}
			});
			config.add(new AbstractAction("Export Webserver URL...")
			{
				@Override
				public void actionPerformed(final ActionEvent ae)
				{
					final JFrame f = new JFrame("Webserver URL");
					final JTextField tf = new JTextField(parent.initiator.getWebserverUrl());
					tf.setEditable(false);
					f.getContentPane().add(tf);
					f.pack();
					f.setVisible(true);
				}
			});
		}
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(menus, BorderLayout.NORTH);
		this.getContentPane().add(p, BorderLayout.CENTER);
		this.pack();
		this.setVisible(true);
	}

	/**
	 * add container
	 */
	public void addContainer(final Container c)
	{
		int r = 0;
		synchronized (containers)
		{
			r = containers.size();
			containers.addElement(c);
		}
		model.fireTableRowsInserted(r, r);
	}

	/**
	 * set status
	 */
	public void setStatus(final String s)
	{
		final Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				statusLabel.setText(s);
			}
		};
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		}
		else
		{
			SwingUtilities.invokeLater(r);
		}
	}

	/**
	 * update displayed status - may not be swing thread
	 */
	public void updateContainerStatus(final Container c)
	{
		final Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				int r = 0;
				synchronized (containers)
				{
					r = containers.indexOf(c);
					if (r < 0)
					{
						addContainer(c);
						return;
					}
				}
				model.fireTableRowsUpdated(r, r);
			}
		};
		if (SwingUtilities.isEventDispatchThread())
		{
			r.run();
		}
		else
		{
			SwingUtilities.invokeLater(r);
		}
	}

	void cancelRestart()
	{
		cancelTimer();
		cancelButton.setEnabled(false);
		restartLabel.setText(NO_RESTARTS);
	}

	void cancelTimer()
	{
		if (timer != null)
		{
			timer.cancel();
		}
		timer = new RestartTimer();
	}

	void restartContainers()
	{
		System.out.println("Re/start all containers...");
		synchronized (containers)
		{
			for (int i = 0; i < containers.size(); i++)
			{
				final Container c = (Container) containers.elementAt(i);
				if (c.isRunning())
				{
					setStatus("Restarting container " + c);
					c.restart();
				}
				else
				{
					setStatus("Starting container " + c);
					c.start(parent);
				}
				updateContainerStatus(c);
				setStatus("Done");
			}
		}
	}

	void setDailyRestart(final Date date)
	{
		cancelTimer();
		final DailyTask task = new DailyTask(date);
		timer.scheduleDailyTask(task);
		cancelButton.setEnabled(true);
		updateRestartLabelDaily(task);
	}

	void setInitialDailyRestart(final int hours, final int minutes)
	{
		cancelTimer();

		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);

		if (cal.before(Calendar.getInstance()))
		{
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

		final DailyTask task = new DailyTask(cal.getTime());

		timer.scheduleDailyTask(task);
		cancelButton.setEnabled(true);
		updateRestartLabelDaily(task);
	}

	void setPeriodicRestart(final int hours, final int minutes)
	{
		// first, calculate time until restart in milliseconds
		// needed by timer class

		cancelTimer();
		final PeriodicTask task = new PeriodicTask(hours, minutes);
		timer.schedulePeriodicTask(task);

		cancelButton.setEnabled(true);
		updateRestartLabelPeriodic(task);
	}

	void updateRestartLabelDaily(final DailyTask task)
	{
		final Date date = task.getNextRestart();
		restartLabel.setText(sdf.format(date));
	}

	void updateRestartLabelPeriodic(final PeriodicTask task)
	{
		// get the current time and date
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, task.hours);
		cal.add(Calendar.MINUTE, task.minutes);
		final Date time = cal.getTime();

		restartLabel.setText(sdf.format(time));
	}

	/**
	 * exec java
	 */
	protected void execJava(final String cmd)
	{
		// really start...
		try
		{

			final StringBuffer cpbuf = new StringBuffer();
			final String dirs[] = new String[] { "tools", "common", "common/ext" };
			for (final String dir : dirs)
			{
				final File path = new File(parent.initiator.getPath() + File.separator + "java" + File.separator + dir);
				if (!path.isDirectory())
				{
					System.err.println(dir + " directory not found (" + path + ")");
				}
				else
				{
					final File tooljars[] = path.listFiles();
					for (int fi = 0; fi < tooljars.length; fi++)
					{
						System.out.println(dir + " jar " + tooljars[fi] + "...");
						if (!tooljars[fi].exists() || !tooljars[fi].canRead()
								|| !tooljars[fi].getName().endsWith(".jar"))
						{
							System.out.println("Ignore/can't read " + dir + " jar " + tooljars[fi]);
							continue;
						}
						if (cpbuf.length() > 0)
						{
							cpbuf.append(File.pathSeparator);
						}
						cpbuf.append(tooljars[fi].getAbsolutePath());
					}
				}
			}
			if (cpbuf.length() > 0)
			{
				cpbuf.append(File.pathSeparator);
			}
			cpbuf.append(Container.removeQuotes(Boot.deriveFullWebStartClassPath()));
			final String classPath = cpbuf.toString();
			String execStr = "java ";

			if (DUMP_HEAP)
			{
				final String[] bits = cmd.split(" ");
				execStr = execStr + " -Xrunhprof:heap=all,format=b,file=" + bits[0];
			}

			execStr = execStr + " -DDataspaceSecret=\"" + System.getProperty("DataspaceSecret") + "\" "
					+ " -DDefaultDirectory=\"" + parent.initiator.getPath() + "\" " + " -classpath " + "\"" + classPath
					+ "\"" + " " + cmd;

			System.out.println("launching tool: " + execStr);
			final Process myProcess = Runtime.getRuntime().exec(execStr);
			try
			{
				// System.out.println(parent.initiator.getPath());

				final OutputStream ps = new BufferedOutputStream(new FileOutputStream(new File(
						parent.initiator.getPath() + File.separator + "tool-console-" + System.currentTimeMillis()
								+ Boot.LOG_SUFFIX)));
				new StreamPump(myProcess.getErrorStream(), ps, true);
				new StreamPump(myProcess.getInputStream(), ps);

			}
			catch (final Exception e)
			{
				System.err.println("ERROR writing tool output to file: " + e);
				final DebugFrame debugFrame = new DebugFrame("Tool Output", 400, 400);
				debugFrame.setVisible(true);
				debugFrame.processInputStream(myProcess.getErrorStream());
				debugFrame.processInputStream(myProcess.getInputStream());
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR execing " + cmd + ": " + e);
			e.printStackTrace(System.err);
			return;
		}
	}

	// copied from the internet - a filter to limit contents of text fields
	// to a certain size

	/**
	 * terminate
	 */
	protected void terminate()
	{
		synchronized (containers)
		{
			for (int i = 0; i < containers.size(); i++)
			{
				final Container c = (Container) containers.elementAt(i);
				if (c.isRunning())
				{
					c.stop();
				}
			}
		}
		// Boot.showBootFrame();
		this.dispose();
		System.exit(0);
	}

}
