/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2004-2005, University of Nottingham All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the University of Nottingham nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * </COPYRIGHT>
 * 
 * Created by: Jan Humble (University of Nottingham)
 * 
 * Contributors: Jan Humble (University of Nottingham)
 *  
 */

package equip.ect.components.webripper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class WebRipper extends JFrame
{

	private class UpdateThread extends Thread
	{

		private int updateFreq;

		UpdateThread(final int updateFreqSecs)
		{
			setUpdateFreq(updateFreqSecs);
		}

		@Override
		public void run()
		{
			while (updateFreq > 0)
			{
				try
				{
					if (!WebRipper.this.getRipping())
					{
						WebRipper.this.setRipping(true);
					}
					sleep(updateFreq);
				}
				catch (final InterruptedException e)
				{

					e.printStackTrace();
				}
			}
		}

		void setUpdateFreq(final int updateFreqSecs)
		{
			this.updateFreq = updateFreqSecs * 1000;
		}
	}

	public static void main(final String[] args)
	{
		final WebRipper ripper = new WebRipper();
		ripper.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected static String[] findPatternMatches(final String input, final String regex)
	{
		// Need to specify DOTALL (+MULTILINE) greedy, since we want to
		// match over line breaks
		final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
				| Pattern.MULTILINE);
		final Vector matchResults = new Vector();
		final Matcher matcher = pattern.matcher(input);
		while (matcher.find())
		{
			matchResults.add(matcher.group());
		}

		return (String[]) matchResults.toArray(new String[matchResults.size()]);
	}

	private int updateFrequency = -1;

	private String targetURL = "http://www.loot.com/rs6/cl.asp?action=srslt&pn=1&ps=10&c1=3812&kyd=&psc=sw18+4nz&psc_dis=1";

	private int ripStartIndex = 0, ripLength = -1;

	private String keyword = "";

	private String matchPattern = "<A title=\"Click here to see more details of this ad\".*?href.*?A>";

	private String startMatchPattern = "<A title=\"Click here to see more details of this ad\".*?href.*?>";

	private String endMatchPattern = "More.*?</A>";

	private JTextField startKeywordTF;

	private JFormattedTextField startIndexTF;

	private JFormattedTextField lengthTF;

	private JFormattedTextField updateFreqTF;

	private JTextField urlTF;

	private JTextArea resultsTA;

	private byte[] result;

	private boolean ripping = false;

	private UpdateThread updateThread;

	private String[] patternMatchResults;

	private JTextField patternTF;

	private JTextArea patternResultsTA;
	private JButton updateButton;

	private JTextField startPatternTF;

	private JTextField endPatternTF;

	private AbstractButton compactResultsCB;

	private boolean compactResults = true;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * This is the default constructor
	 */
	public WebRipper()
	{
		super("WebRipper");
		final JPanel mainPanel = new JPanel(new BorderLayout());

		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.WEST, new JLabel("Target URL: "));
		urlTF = new JTextField(targetURL, 30);
		p.add(urlTF);
		topPanel.add(p);

		p = new JPanel(new FlowLayout());
		p.add(new JLabel("start keyword: "));
		startKeywordTF = new JTextField(keyword, 10);
		p.add(startKeywordTF);

		final NumberFormat numFormat = NumberFormat.getIntegerInstance();
		numFormat.setParseIntegerOnly(true);

		p.add(new JLabel("start index: "));
		startIndexTF = new JFormattedTextField(numFormat);
		startIndexTF.setValue(new Integer(ripStartIndex));
		startIndexTF.setColumns(4);
		p.add(startIndexTF);

		p.add(new JLabel("length: "));
		lengthTF = new JFormattedTextField(numFormat);
		lengthTF.setValue(new Integer(ripLength));
		lengthTF.setColumns(4);
		p.add(lengthTF);
		topPanel.add(p);

		p = new JPanel();
		p.add(new JLabel("update frequency (secs): "));
		updateFreqTF = new JFormattedTextField(numFormat);
		updateFreqTF.setValue(new Integer(updateFrequency));
		updateFreqTF.setColumns(4);
		p.add(updateFreqTF);
		topPanel.add(p);

		p = new JPanel(new GridLayout(2, 1));
		p.setBorder(new TitledBorder("Match Pattern"));
		final JPanel sp = new JPanel(new BorderLayout());
		sp.add(BorderLayout.WEST, new JLabel("start: "));
		startPatternTF = new JTextField(startMatchPattern, 30);
		sp.add(startPatternTF);
		p.add(sp);
		final JPanel ep = new JPanel(new BorderLayout());
		ep.add(BorderLayout.WEST, new JLabel("end: "));
		endPatternTF = new JTextField(endMatchPattern, 30);
		ep.add(endPatternTF);
		p.add(ep);
		topPanel.add(p);

		p = new JPanel(new FlowLayout());
		compactResultsCB = new JCheckBox("compact results");
		compactResultsCB.setSelected(compactResults);
		compactResultsCB.setAction(new AbstractAction("compact results")
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				setCompactResults(compactResultsCB.isSelected());
			}

		});

		p.add(compactResultsCB);
		topPanel.add(p);

		/*
		 * p = new JPanel(new FlowLayout()); p.add(new JLabel("Pattern (reg ex): ")); patternTF =
		 * new JTextField(matchPattern); p.add(patternTF); topPanel.add(p);
		 */

		final JPanel actionPanel = new JPanel();
		updateButton = new JButton("RIP");
		updateButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				WebRipper.this.readFieldValues();
				WebRipper.this.setRipping(true);
			}

		});

		actionPanel.add(updateButton);

		topPanel.add(actionPanel);

		final JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
		JPanel resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.add(BorderLayout.NORTH, new JLabel("Results: "));
		resultsTA = new JTextArea();
		resultsTA.setText("Nothing yet.");
		resultsTA.setEditable(false);
		resultsPanel.add(BorderLayout.CENTER, new JScrollPane(resultsTA));
		bottomPanel.add(resultsPanel);

		resultsPanel = new JPanel(new BorderLayout());
		resultsPanel.add(BorderLayout.NORTH, new JLabel("Match Results: "));
		patternResultsTA = new JTextArea();
		patternResultsTA.setText("Nothing yet.");
		patternResultsTA.setEditable(false);
		resultsPanel.add(BorderLayout.CENTER, new JScrollPane(patternResultsTA));
		bottomPanel.add(resultsPanel);

		mainPanel.add(BorderLayout.NORTH, topPanel);
		mainPanel.add(BorderLayout.CENTER, bottomPanel);

		getContentPane().add(mainPanel);
		setSize(700, 700);
		setVisible(true);
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * @return Returns the endMatchPattern.
	 */
	public String getEndMatchPattern()
	{
		return endMatchPattern;
	}

	/**
	 * @return Returns the keyword.
	 */
	public String getKeyword()
	{
		return keyword;
	}

	/**
	 * @return Returns the matchPattern.
	 */
	public String getMatchPattern()
	{
		return matchPattern;
	}

	/**
	 * @return Returns the matchResults.
	 */
	public String[] getPatternMatchResults()
	{
		return patternMatchResults;
	}

	public byte[] getResult()
	{
		return this.result;
	}

	/**
	 * @return Returns the ripLength.
	 */
	public int getRipLength()
	{
		return ripLength;
	}

	public boolean getRipping()
	{
		return this.ripping;
	}

	/**
	 * @return Returns the ripStartIndex.
	 */
	public int getRipStartIndex()
	{
		return ripStartIndex;
	}

	/**
	 * @return Returns the startMatchPattern.
	 */
	public String getStartMatchPattern()
	{
		return startMatchPattern;
	}

	/**
	 * @return Returns the targetURL.
	 */
	public String getTargetURL()
	{
		return targetURL;
	}

	/**
	 * @return Returns the updateFrequency.
	 */
	public int getUpdateFrequency()
	{
		return updateFrequency;
	}

	/**
	 * @return Returns the compactResults.
	 */
	public boolean isCompactResults()
	{
		return compactResults;
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * @param compactResults
	 *            The compactResults to set.
	 */
	public void setCompactResults(final boolean compactResults)
	{
		final boolean old = this.compactResults;
		this.compactResults = compactResults;
		propertyChangeListeners.firePropertyChange("compactResults", old, this.compactResults);
		compactResultsCB.setSelected(compactResults); // this does not trigger
		// an action, so no
		// unwanted recursion.
	}

	/**
	 * @param endMatchPattern
	 *            The endMatchPattern to set.
	 */
	public void setEndMatchPattern(final String endMatchPattern)
	{
		final String old = this.endMatchPattern;
		this.endMatchPattern = endMatchPattern;
		propertyChangeListeners.firePropertyChange("endMatchPattern", old, endMatchPattern);
		endPatternTF.setText(endMatchPattern);

	}

	/**
	 * @param keyword
	 *            The keyword to set.
	 */
	public void setKeyword(final String keyword)
	{
		final String old = this.keyword;
		this.keyword = keyword;
		propertyChangeListeners.firePropertyChange("keyword", old, keyword);
		startKeywordTF.setText(keyword);
	}

	/**
	 * @param matchPattern
	 *            The matchPattern to set.
	 */
	public void setMatchPattern(final String matchPattern)
	{
		final String old = this.matchPattern;
		this.matchPattern = matchPattern;
		propertyChangeListeners.firePropertyChange("matchPattern", old, matchPattern);
		// patternTF.setText(matchPattern);
	}

	/**
	 * @param ripLength
	 *            The ripLength to set.
	 */
	public void setRipLength(final int ripLength)
	{
		final int old = this.ripLength;
		this.ripLength = ripLength;
		propertyChangeListeners.firePropertyChange("ripLength", old, ripLength);
		this.lengthTF.setValue(new Integer(ripLength));
	}

	public synchronized void setRipping(final boolean ripping)
	{
		final boolean old = this.ripping;
		this.ripping = ripping;
		if (ripping && !old)
		{
			updateButton.setEnabled(false);
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					extractInfo();
					setMatchPattern(startMatchPattern + ".*?" + endMatchPattern);
					setRipping(false);
					updateButton.setEnabled(true);
				}
			});
		}
		propertyChangeListeners.firePropertyChange("ripping", old, ripping);
	}

	/**
	 * @param ripStartIndex
	 *            The ripStartIndex to set.
	 */
	public void setRipStartIndex(final int ripStartIndex)
	{
		final int old = this.ripStartIndex;
		this.ripStartIndex = ripStartIndex;
		propertyChangeListeners.firePropertyChange("ripStartIndex", old, ripStartIndex);
		startIndexTF.setValue(new Integer(ripStartIndex));
	}

	/**
	 * @param startMatchPattern
	 *            The startMatchPattern to set.
	 */
	public void setStartMatchPattern(final String startMatchPattern)
	{
		final String old = this.startMatchPattern;
		this.startMatchPattern = startMatchPattern;
		propertyChangeListeners.firePropertyChange("startMatchPattern", old, startMatchPattern);
		startPatternTF.setText(startMatchPattern);

	}

	/**
	 * @param targetURL
	 *            The targetURL to set.
	 */
	public void setTargetURL(final String targetURL)
	{
		final String old = this.targetURL;
		this.targetURL = targetURL;
		propertyChangeListeners.firePropertyChange("targetURL", old, targetURL);
		urlTF.setText(targetURL);
	}

	/**
	 * @param updateFrequency
	 *            The updateFrequency to set.
	 */
	public void setUpdateFrequency(final int updateFrequency)
	{
		final int old = this.updateFrequency;
		this.updateFrequency = updateFrequency;
		propertyChangeListeners.firePropertyChange("updateFrequency", old, updateFrequency);
		updateFreqTF.setValue(new Integer(updateFrequency));

		if (updateThread == null)
		{
			if (updateFrequency > 0)
			{
				(updateThread = new UpdateThread(updateFrequency)).start();
			}
		}
		else
		{
			updateThread.setUpdateFreq(updateFrequency);
		}
	}

	public void stop()
	{
		this.dispose();
	}

	protected String[] filterPatternMatches(final String[] matches)
	{
		final Pattern startPattern = Pattern.compile(startMatchPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE | Pattern.MULTILINE);
		final Pattern endPattern = Pattern.compile(endMatchPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE
				| Pattern.UNICODE_CASE | Pattern.MULTILINE);
		final String[] filteredMatches = new String[matches.length];
		for (int i = 0; i < matches.length; i++)
		{
			final String match = matches[i];
			// System.out.println("Doing filtering " + i);
			String filteredMatch = startPattern.matcher(match).replaceFirst("");
			// System.out.println(filteredMatch);
			filteredMatch = endPattern.matcher(filteredMatch).replaceFirst("");
			filteredMatches[i] = filteredMatch;
		}

		if (compactResults)
		{

			// create a pattern to remove HTML tags
			// simple version, just anything between <>
			// NOTE: could have used String.replaceAll(), but this
			// way we get better control and should be the same.
			final Pattern htmlTagPattern = Pattern.compile("<.*?>", Pattern.DOTALL | Pattern.MULTILINE);

			for (int i = 0; i < filteredMatches.length; i++)
			{
				String match = filteredMatches[i];
				match = htmlTagPattern.matcher(match).replaceAll("");
				// remove all extra spaces
				match = match.replaceAll("[\t\r\n]", "");
				match = match.trim();
				filteredMatches[i] = match;
			}
		}

		return filteredMatches;
	}

	/**
	 * @param matchResults
	 *            The matchResults to set.
	 */
	protected void setPatternMatchResults(final String[] patternMatchResults)
	{
		final String[] old = this.patternMatchResults;
		this.patternMatchResults = patternMatchResults;
		propertyChangeListeners.firePropertyChange("patternMatchResults", old, patternMatchResults);
		setPatternResultsText(patternMatchResults);
	}

	/**
	 * @param result
	 */
	protected void setResult(final byte[] result)
	{
		// The result of ripping can get very large.
		// If result was stored as a string, then it
		// would be transmitted to the dataspace
		// through a DataOutputStream, but these are limited
		// in size, and this has been causing problems

		// so store as utf-8 encoded bytes, in which case
		// it will be transmitted using a different type
		// of stream with much larger capacity

		final byte[] old = this.result;
		this.result = result;
		propertyChangeListeners.firePropertyChange("result", old, this.result);

		try
		{
			// need to covert bytes to a string
			// to put into gui
			final String stringResult = new String(result, "UTF-8");
			resultsTA.setText("Ripping Succeeded! ...\n" + stringResult);
			resultsTA.setCaretPosition(0);
		}
		catch (final UnsupportedEncodingException e)
		{
			// should never happen
		}

	}

	private synchronized void extractInfo()
	{

		URLConnection conn = null;

		try
		{
			conn = new URL(targetURL).openConnection();
		}
		catch (final Exception e)
		{
			resultsTA.setText("Error: " + e.getMessage());
			return;
		}

		final StringBuffer data = new StringBuffer();
		try
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (true)
			{
				final int c = in.read();

				if (c == -1)
				{
					break;
				}
				data.append((char) c);
			}
		}
		catch (final IOException e)
		{
			resultsTA.setText(e.getMessage());
			return;
		}

		int startIndex = this.ripStartIndex;
		if (keyword.length() > 0)
		{
			final int i = data.indexOf(keyword);
			if (i > -1)
			{
				startIndex = this.ripStartIndex + i + keyword.length();

			}

		}
		startIndex = (startIndex < data.length()) ? startIndex : data.length() - 1;
		startIndex = (startIndex < 0) ? 0 : startIndex;
		int endIndex = startIndex + this.ripLength;
		endIndex = (endIndex < data.length()) ? endIndex : data.length() - 1;
		String result = null;
		if (endIndex > 0)
		{
			result = data.substring(startIndex, endIndex);
		}
		else
		{
			result = data.substring(startIndex);
		}
		// this.resultsTA.setText("Ripping succeeded, result: \n" + result);

		try
		{
			// see comments in the setResult method
			// for why we convert into bytes
			setResult(result.getBytes("UTF-8"));
		}
		catch (final UnsupportedEncodingException e)
		{
			// should never happen
		}

		if (matchPattern != null && matchPattern.length() > 0)
		{
			String[] matchResults = findPatternMatches(result, matchPattern);
			matchResults = filterPatternMatches(matchResults);
			setPatternMatchResults(matchResults);
		}
	}

	private int objectToInt(final Object obj)
	{
		if (obj instanceof Long)
		{
			return (int) ((Long) obj).longValue();
		}
		else if (obj instanceof Integer) { return ((Integer) obj).intValue(); }
		return -1;
	}

	private void readFieldValues()
	{
		setTargetURL(urlTF.getText());
		setKeyword(startKeywordTF.getText());
		setRipStartIndex(objectToInt(startIndexTF.getValue()));
		setRipLength(objectToInt(lengthTF.getValue()));
		setUpdateFrequency(objectToInt(updateFreqTF.getValue()));
		// setMatchPattern(patternTF.getText());
		setStartMatchPattern(startPatternTF.getText());
		setEndMatchPattern(endPatternTF.getText());
	}

	private void setPatternResultsText(final String[] patternResults)
	{
		if (patternResults == null)
		{
			patternResultsTA.setText("No patterns found");
			return;
		}
		patternResultsTA.setText("Found " + patternResults.length + " patterns:");
		for (int i = 0; i < patternResults.length; i++)
		{
			patternResultsTA.append("\n" + (i + 1) + ".\n" + patternResults[i] + "\n");
		}

		patternResultsTA.setCaretPosition(0);
	}
}
