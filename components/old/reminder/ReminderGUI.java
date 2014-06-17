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

Created by: Chris Allsop (University of Nottingham)
Contributors:
  Chris Allsop (University of Nottingham)

 */
/*
 * Reminder Tool 
 *
 * Version: 1.0 
 * Date: 09/07/2004
 *
 * Author: Chris Allsopp 
 * cpa02u@cs.nott.ac.uk
 * 
 */

package equip.ect.components.reminder;

import mseries.Calendar.MDateChanger;
import mseries.Calendar.MDefaultPullDownConstraints;
import mseries.ui.MDateEntryField;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * <b>ReminderGUI</b>.
 * <p/>
 * Graphical User Interace(GUI) for the {@link Reminder} Bean component.
 * <p/>
 * Provides a 'spinner' for inputting the reminder 'trigger-time' and a drop down calendar for
 * selecting the reminder 'trigger-date'.
 * <p/>
 * Reminder messages can be specified in an editable TextArea and set using the <i>'Set Reminder</i>
 * button.
 * <p/>
 * Diagnostic information and current system time is also reported by the GUI. specified date and
 * time in the future.
 * <p/>
 *
 * @author Chris allsopp &lt;cpa02u@cs.nott.ac.uk&gt;
 * @see Reminder
 * @see ReminderBeanInfo
 */
public final class ReminderGUI extends JFrame implements Serializable
{

	/**
	 * JFrame Title.
	 */
	private final static String guiTitle = "Reminder Tool";
	/**
	 * Specifies the format that time information is displayed in the spinner box.
	 * <p/>
	 * Example spinner value is as follows <code>17:48:03<\code>
	 */
	private final static String timeFormat = "HH:mm:ss";
	/**
	 * Specifies the format that date information is displayed in the dateSelector box.
	 * <p/>
	 * Example dateSelector value is as follows <code>Monday, July 19 2004<\code>
	 */
	private final static String dateFormat = "EE, MMMM dd yyyy";
	private final static Dimension fillerPanel_FillDim = new Dimension(350, 10);
	/**
	 * log4j logger, used to print to console (System.out) by default.
	 *
	 * @see <a href="http://logging.apache.org/log4j/docs/">Log4j Documentation</a>
	 */
	private static Logger logger = Logger.getLogger(ReminderGUI.class);

	static
	{
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
	}

	private final Dimension timePanel_FillDim = new Dimension(20, 0);

	// ---------------------- Time and Date Panel Components ----------------------//
	private final Dimension timePanel_PrefDim = new Dimension(340, 26);
	private final Dimension msgPanel_FillDim = new Dimension(0, 30);
	private final Dimension msgPanel_PrefDim = new Dimension(340, 150);
	private final Dimension msgPanel_PrefSetButtonDim = new Dimension(110, 150);
	private final Dimension innerPanel_PrefDim = new Dimension(240, 150);
	private final Dimension diagPanel_PrefDim = new Dimension(300, 60);
	private final Dimension clockPanel_PrefDim = new Dimension(300, 20);

	// -------------------------- Filler Panel Components -------------------------//
	/**
	 * The bean associated with this particular GUI instance.
	 */
	private Reminder logic;
	/**
	 * The reminder date and time that the user has requested to be set.
	 */
	private GregorianCalendar proposedReminderDate = new GregorianCalendar(TimeZone.getDefault());
	private JPanel timePanel;

	// ------------------------ Message Panel Components ----------------------//
	/**
	 * Used to select the time in hours, minutes and seconds that the reminder should be fired.
	 * <p/>
	 * Times will be displayed in the format specified by {@link #timeFormat}. i.e. <Hour (24hr
	 * clock)> : <Minutes> : <Seconds>
	 * <p/>
	 * There are no restrictions as to which times may be entered via the spinner. This will be
	 * checked when the user commits the entered time.
	 */
	private JSpinner spinner;
	/**
	 * Used to select the date in days, months and years that the reminder should be fired.
	 * <p/>
	 * Dates will be displayed in the format specified by {@link #dateFormat}. i.e. [Abbreviated Day
	 * Name], [Full Month Name] [Day Date] [4 Digit Year]
	 * <p/>
	 * There are no restrictions as to which dates may be entered via the dateSelector. This will be
	 * checked when the user commits the entered date.
	 */
	private MDateEntryField dateSelector;

	// HTML Tags allow for a multiline text button since 'Set\nReminder' doesn't work
	/**
	 * Constraints to the {@link #dateSelector} which specify how the drop down calendar looks and
	 * functions.
	 */
	private MDefaultPullDownConstraints cons;
	private JPanel fillerPanel, fillerPanel2;
	private JPanel msgPanel, innerPanel;
	/**
	 * TextArea used to display the reminder message that has/will be set.
	 */
	private JTextArea msgbox = new JTextArea();
	/**
	 * The reminder message may be bigger than the msgbox can accomodate so scrollbars are added as
	 * and when required.
	 */
	private JScrollPane msgboxScrollPane = new JScrollPane(msgbox);
	/**
	 * Button used for commiting reminder data that the user has entered.
	 */
	private JButton setButton = new JButton("<html><CENTER><H>Set<br>Reminder</CENTER></html>");
	private JPanel diagPanel;

	// ----------------------- Diagnostics Panel Components -----------------------//
	/**
	 * TextArea used to display feedback information to the user.
	 * <p/>
	 * Appropriate information is displayed on successfully setting a new reminder or when a
	 * reminder is ignored for being invalid.
	 */
	private JTextArea diagbox = new JTextArea();
	/**
	 * The diagnostic message may be bigger than the msgbox can accomodate so scrollbars are added
	 * as and when required.
	 */
	private JScrollPane diagboxScrollPane = new JScrollPane(diagbox);
	private JPanel clockPanel;

	/**
	 * A text String representing the current system time properties of this GUIs associated
	 * {@link Reminder} bean.
	 */
	private JLabel systemClock;

	/**
	 * Constructs a Graphical User Interface to the {@link Reminder} bean component referenced by
	 * the <code>logic</code> pointer passed as an argument to the constructor.
	 *
	 * @param rem_logic Ties the GUI to the Reminder bean component that constructed it.
	 */
	public ReminderGUI(final Reminder rem_logic)
	{

		super(guiTitle);
		this.logic = rem_logic;

		addWindowListener(new WindowAdapter()
		{
			/**
			 * On closing the GUI, the {@link Reminder} associated with this GUI instance is
			 * informed that the GUI has been closed by setting it's gui pointer to
			 * <code>null</code>.
			 */
			@Override
			public void windowClosing(final WindowEvent e)
			{
				logger.debug("---Window closed---");
				logic.setGUI(null);
			}
		});

		makeTimeAndDatePanel();
		makeFillerPanels();
		makeMessagePanel();
		makeDiagnosticsPanel();
		makeClockPanel();

		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(timePanel);
		mainPanel.add(fillerPanel); // provides gap between two main panels
		mainPanel.add(msgPanel);
		mainPanel.add(fillerPanel2); // provides gap between two main panels
		mainPanel.add(diagPanel);
		mainPanel.add(clockPanel);

		this.getContentPane().add(mainPanel);
		this.setResizable(false);
		this.pack();

	}// end constructor

	// --------------------------- Clock Panel Components -------------------------//

	/**
	 * Returns an <code>ImageIcon</code>, or <code>null</code> if the path was invalid.
	 *
	 * @param path The path to the required jpeg or gif file.
	 * @return The appropriate <code>ImageIcon</code> or <code>null</code>.
	 */
	protected static ImageIcon createImageIcon(final String path)
	{

		final java.net.URL image = ReminderGUI.class.getResource(path);
		if (image != null)
		{
			logger.debug("Found the image");
			return new ImageIcon(image);
		}
		else
		{
			logger.warn("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Creates a popup window that is displayed in the centre of the screen with the reminder
	 * message and the date/time the popup window was created.
	 */
	public void createPopupReminder()
	{

		final JFrame popupWindow = new JFrame("Reminder!");
		final JPanel popupPanel = new JPanel(new BorderLayout());

		final JTextArea theMessage = new JTextArea();
		final JLabel theTime = new JLabel(logic.reminder_propertyValues());

		final JScrollPane scroll = new JScrollPane(theMessage);
		final JLabel exclamation = new JLabel(createImageIcon("/Exclamation.gif"));

		final Dimension popupPreferredDim = new Dimension(400, 150);
		// get the screen size
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		// used as coordinated to centre the popup window in the middle of the screen
		final int x_centre = (int) (screen.getWidth() / 2 - 200);
		final int y_centre = (int) (screen.getHeight() / 2 - 75);

		theMessage.setFont(new Font("Arial", Font.PLAIN, 16));
		theMessage.setText(logic.getReminderMessage());
		theMessage.setEditable(false);
		theMessage.setWrapStyleWord(true);
		theMessage.setLineWrap(true);
		theMessage.setBackground(new Color(255, 255, 220));

		theTime.setHorizontalAlignment(SwingConstants.CENTER);

		popupPanel.setBackground(Color.WHITE);

		popupPanel.add(new Box.Filler(new Dimension(0, 15), new Dimension(0, 15), new Dimension(0, 15)),
				BorderLayout.NORTH);

		popupPanel.add(new Box.Filler(new Dimension(15, 0), new Dimension(15, 0), new Dimension(15, 0)),
				BorderLayout.EAST);

		popupPanel.add(exclamation, BorderLayout.WEST);
		popupPanel.add(scroll, BorderLayout.CENTER);
		popupPanel.add(theTime, BorderLayout.SOUTH);
		popupPanel.setPreferredSize(popupPreferredDim);

		popupWindow.getContentPane().add(popupPanel);
		popupWindow.setLocation(x_centre, y_centre);
		popupWindow.pack();
		popupWindow.setVisible(true);

	}

	/**
	 * Sets the date values displayed by the dateSelector.
	 */
	public void setDateSelector(final Date value)
	{
		dateSelector.setValue(value);
	}

	/**
	 * Sets the message displayed in the TextArea contained in the Message panel.
	 */
	public void setMsgboxText(final String msgToDisplay)
	{
		msgbox.setText(msgToDisplay);
	}

	// ----------------------------------------------------------------------------//

	/**
	 * Sets the time values displayed by the spinner.
	 */
	public void setSpinner(final Date value)
	{
		spinner.setValue(value);
	}

	// ------------------------------ Other Methods -------------------------------//

	/**
	 * Sets the String representation of the current system time that is to be displayed in the
	 * System Clock panel.
	 */
	public void setSystemClockText(final String timeToDisplay)
	{
		systemClock.setText(timeToDisplay);
	}

	/**
	 * Constructs the panel displaying the current system time information.
	 */
	private void makeClockPanel()
	{

		clockPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		clockPanel.setPreferredSize(clockPanel_PrefDim);

		systemClock = new JLabel("System Time: " + logic.system_propertyValues());
		systemClock.setFont(Font.getFont("Arial"));

		clockPanel.add(systemClock);
	}

	/**
	 * Constructs the diagnostics panel used for information and error reporting.
	 */
	private void makeDiagnosticsPanel()
	{

		diagPanel = new JPanel(new BorderLayout());
		diagPanel.setPreferredSize(diagPanel_PrefDim);

		diagbox.setLineWrap(true);
		diagbox.setWrapStyleWord(true);
		diagbox.setEditable(false);
		diagbox.setDisabledTextColor(Color.GRAY);
		diagbox.setBackground(Color.LIGHT_GRAY);

		diagPanel.add(diagboxScrollPane, BorderLayout.CENTER);
	}

	/**
	 * Constructs a blank panel to sit between the Time and Date Panel and the Message Panel and
	 * between the Message Panel and the Diagnostics Panel.
	 */
	private void makeFillerPanels()
	{

		fillerPanel = new JPanel();
		fillerPanel.add(new Box.Filler(fillerPanel_FillDim, fillerPanel_FillDim, fillerPanel_FillDim));

		fillerPanel2 = new JPanel();
		fillerPanel2.add(new Box.Filler(fillerPanel_FillDim, fillerPanel_FillDim, fillerPanel_FillDim));
	}

	/**
	 * Constructs the message panel used for editing and setting the reminder message that is to be
	 * fired.
	 */
	/*
	 * <br> +----------------------------+ <br> | msgPanel | <br> | +----------+ +---------+ |_*
	 * inner panel contains msgBoxScrollPane <br> | | inner | | set | | * msgBoxScrollPane contains
	 * msgbox <br> | | Panel | | Button | | <br> | +----------+ +---------+ | <br>
	 * +----------------------------+ <br>
	 */
	private void makeMessagePanel()
	{

		msgPanel = new JPanel(new BorderLayout());
		innerPanel = new JPanel(new BorderLayout());

		msgPanel.setPreferredSize(msgPanel_PrefDim);
		msgboxScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		msgbox.setLineWrap(true);
		msgbox.setWrapStyleWord(true);

		setButton.setMinimumSize(msgPanel_PrefSetButtonDim);
		setButton.addActionListener(new ActionListener()
		{
			/**
			 * Calls the following set methods of this GUIs associated bean component providing that
			 * the data taken from the values of the {@link spinner} and the {@link dateSelector}
			 * are valid:
			 * <p>
			 * <ul>
			 * <li>{@link Reminder#setReminderCalendar(GregorianCalendar)} value</li>
			 * <li>{@link Reminder#setReminderSeconds(int)} value</li>
			 * <li>{@link Reminder#setReminderMinutes(int)} value</li>
			 * <li>{@link Reminder#setReminderHours(int)} value</li>
			 * <li>{@link Reminder#setReminderDayOfMonth(int)} value</li>
			 * <li>{@link Reminder#setReminderMonth(int)} value</li>
			 * <li>{@link Reminder#setReminderYear(int)} value</li>
			 * <li>{@link Reminder#setReminderMessage(String)} value</li>
			 * </ul>
			 * <p>
			 * Setter values are taken on clicking this button.
			 */
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				proposedReminderDate = userinputToCalendar();

				if (!logic.isValidReminderDate(proposedReminderDate))
				{
					diagbox.setText("**Error! Cannot set reminder in the past.**\n");
					return;

				}
				else
				{ // reminder OK to be set
					logic.setReminderMessage(msgbox.getText());
					logic.setReminderCalendar(proposedReminderDate); // also updates individual
					// property fields
					diagbox.setText("Reminder Set:\n" + logic.reminder_propertyValues());
				}
			}
		});

		msgPanel.add(new Box.Filler(msgPanel_FillDim, msgPanel_FillDim, msgPanel_FillDim));

		innerPanel.setPreferredSize(innerPanel_PrefDim);
		innerPanel.add(msgboxScrollPane, BorderLayout.CENTER);

		msgPanel.add(innerPanel, BorderLayout.WEST);
		msgPanel.add(setButton, BorderLayout.EAST);
	}

	/**
	 * Constructs the Time And Date Panel and it's associated components.
	 */
	private void makeTimeAndDatePanel()
	{

		timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
		timePanel.setPreferredSize(timePanel_PrefDim);

		spinner = new JSpinner(new SpinnerDateModel(logic.getSystemCalendar().getTime(), null, null, Calendar.SECOND));

		spinner.setEditor(new JSpinner.DateEditor(spinner, timeFormat));

		dateSelector = new MDateEntryField(new SimpleDateFormat(dateFormat));
		dateSelector.setShowTodayButton(true);

		// Sets up some simple Calendar stuff - look of the GUI etc.
		cons = new MDefaultPullDownConstraints();
		cons.changerStyle = MDateChanger.SPINNER;
		cons.firstDay = Calendar.SATURDAY;
		dateSelector.setConstraints(cons);

		// Add Components to the panel
		timePanel.add(spinner);
		timePanel.add(new Box.Filler(timePanel_FillDim, timePanel_FillDim, timePanel_FillDim));
		timePanel.add(dateSelector);
	}

	/**
	 * Formats the time value that has been set in the spinner and the date value that has been set
	 * in the dateSelector as a <code>GregorianCalendar</code> object.
	 * <p/>
	 *
	 * @return <code>GregorianCalendar</code> representation of the user's input data.
	 * <p/>
	 * <i>NOTE:</I><br>
	 * The GUI's spinner does not track the day/month/year properties and the GUI's
	 * dateSelector does not track second/minute/hour properties such is the implementation
	 * of the dateSelector and spinner by mseries. This method combines the time properties
	 * from spinner and the date properties of dateSelector to a GregorianCalendar object.
	 */
	private GregorianCalendar userinputToCalendar()
	{
		final GregorianCalendar dateReader = new GregorianCalendar(TimeZone.getDefault());
		final GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());

		cal.setTime((Date) spinner.getValue()); // sets cal's hour/mins/sec fields
		try
		{
			dateReader.setTime(dateSelector.getValue());
		}
		catch (final ParseException e)
		{
			logger.error(e.getMessage() + " in userinputToCalendar() method ");
		}
		cal.set(Calendar.DAY_OF_MONTH, dateReader.get(Calendar.DAY_OF_MONTH));
		cal.set(Calendar.MONTH, dateReader.get(Calendar.MONTH));
		cal.set(Calendar.YEAR, dateReader.get(Calendar.YEAR));
		return cal;
	}

} // end class
