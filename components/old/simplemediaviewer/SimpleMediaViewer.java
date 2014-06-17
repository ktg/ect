/*
 <COPYRIGHT>

 Copyright (c) 2005, University of Nottingham
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

 */
package equip.ect.components.simplemediaviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 * Simple frame for displaying media in the form of urls resources.
 * 
 * <H3>Description</H3> A frame for display web pages, audio clips, images and possibly animations.
 * Just set the URL of the media resource and it should display. Remember to set your proxy settings
 * if behind a firewall. A simple history of visited resources is also implemented.
 * 
 * <h3>Configuration</h3> Although this component requires no configuration as such, if your network
 * requires you to access the web through a web-cache/proxy, then the component must be used in a
 * java environment where web-cache/proxy access has been configured properly (eg jvm caching
 * properties set).
 * 
 * <H3>Usage</H3> Just set the URL of the media resource and it should display.
 * 
 * <H3>Technical Details</H3> Currently uses the Java EditorPane->Styled Document to render HTML
 * pages. Complex pages (CSS, etc) might not display properly.
 * 
 * @defaultInputProperty browserURL
 * 
 * @classification Media/Display
 * 
 */
public class SimpleMediaViewer extends JFrame implements LineListener
{
	/**
	 * All purpose MediaCanvas. Chosen over multiple panes for different purposes, e g via a
	 * CardLayout. If more sophisticated media rendering is required then more likely to use a more
	 * complex scheme, such as CardLayout or dymanic paneling.
	 * 
	 * @author humble
	 * 
	 */
	class MediaCanvas extends JEditorPane
	{

		private Image currentImage = null;

		@Override
		public void paintComponent(final Graphics g)
		{
			super.paintComponent(g);
			if (mode == IMAGE_MODE)
			{
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				if (currentImage != null)
				{
					g.drawImage(currentImage, 0, 0, this);
				}
				else
				{
					g.setColor(Color.BLACK);
					g.drawString("Unable to display image", 10, 10);
				}
			}
			else
			{
				super.paintComponent(g);
			}
		}

		@Override
		protected void paintChildren(final Graphics g)
		{
			/*
			 * for image mode we need to stop from rendering children since for framed html pages it
			 * tends to create children components to display content, causing any direct rendering
			 * to be obstructed.
			 */
			if (mode == IMAGE_MODE)
			{

			}
			else
			{
				super.paintChildren(g);
			}
		}

	}

	class URLHandlerThread extends Thread
	{

		private final String url;

		URLHandlerThread(final String url)
		{
			this.url = url;
		}

		@Override
		public void run()
		{
			goB.setEnabled(false);
			try
			{
				final URL webURL = new URL(url);

				if (isSoundResource(url))
				{

					playSoundResource(webURL);

				}
				else if (isImageResource(url))
				{
					viewImageResource(webURL);

				}
				else
				{
					viewHTMLResource(webURL);

				}
				selHistory.addToHistory(url);
				backB.setEnabled(true);
				final String old = browserURL;
				browserURL = webURL.toExternalForm();
				propertyChangeListeners.firePropertyChange("browserURL", old, browserURL);

				// selHistory.setSelectedIndex(0);
			}
			catch (final MalformedURLException e)
			{
				System.out.println("SimpleMediaViewer: " + e.getMessage());
			}
			catch (final IOException e)
			{
				// TODO Auto-generated catch block
				System.out.println("SimpleMediaViewer: " + e.getMessage());
			}
			goB.setEnabled(true);

		}
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final JFrame f = new SimpleMediaViewer();
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	private JButton goB;

	private String browserURL = "http://www.equator.ac.uk/technology/ect";

	private MediaCanvas htmlPane;

	private boolean fullscreen = false;

	private SelectionHistory selHistory;

	private final static int HTML_MODE = 1;

	private final static int IMAGE_MODE = 2;

	private final static int SOUND_MODE = 3;

	protected int mode = HTML_MODE;

	private static final Dimension DEFAULT_BROWSER_SIZE = new Dimension(800, 800);

	private JButton backB;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SimpleMediaViewer()
	{
		super("Media Viewer");

		htmlPane = new MediaCanvas();
		htmlPane.setEditable(false);

		htmlPane.addHyperlinkListener(createHyperLinkListener());

		final JScrollPane scroller = new JScrollPane();
		final JViewport vp = scroller.getViewport();
		vp.add(htmlPane);
		final JPanel browserPanel = new JPanel(new BorderLayout());
		final JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.WEST, new JLabel("URL:"));
		// this.browserURLField = new JTextField(browserURL);
		selHistory = new SelectionHistory(10);
		// selHistory.add(browserURL);
		selHistory.getEditor().getEditorComponent().addKeyListener(new KeyAdapter()
		{

			@Override
			public void keyPressed(final KeyEvent ke)
			{

				if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				{
					final String url = (String) selHistory.getEditor().getItem();
					// We can't really use a selected item from
					// the combobox since the editing field hasn't
					// yet accepted the current entry
					// String url = getSelectedURL();
					setBrowserURL(url);
				}
			}

		});
		p.add(BorderLayout.CENTER, selHistory);
		this.goB = new JButton(new AbstractAction("Go")
		{

			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String url = getSelectedURL();
				if (url != null)
				{
					setBrowserURL(url);

				}

			}
		});
		p.add(BorderLayout.EAST, goB);
		final JPanel navPanel = new JPanel();
		backB = new JButton("<<");

		backB.setEnabled(false);

		backB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				selHistory.back();

				final String currentURL = (String) (selHistory.getCurrentItem().item);

				setBrowserURL((String) selHistory.getCurrentItem().item);

			}

		});
		navPanel.add(backB);
		final JButton forwB = new JButton(">>");
		forwB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				selHistory.forward();
				setBrowserURL((String) selHistory.getCurrentItem().item);
			}

		});
		navPanel.add(forwB);

		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.WEST, navPanel);
		topPanel.add(BorderLayout.CENTER, p);
		browserPanel.add(BorderLayout.NORTH, topPanel);
		browserPanel.add(BorderLayout.CENTER, scroller);
		getContentPane().add(browserPanel);
		setSize(DEFAULT_BROWSER_SIZE);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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

	public HyperlinkListener createHyperLinkListener()
	{

		return new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					if (e instanceof HTMLFrameHyperlinkEvent)
					{
						((HTMLDocument) htmlPane.getDocument())
								.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) e);
					}
					setBrowserURL(e.getURL().toExternalForm());

				}
			}

		};
	}

	public String getBrowserURL()
	{
		return this.browserURL;
	}

	public boolean isFullscreen()
	{
		return fullscreen;
	}

	/**
	 * Plays a sound file from a valid resource name.
	 */
	public boolean playSoundResource(final URL resourcePath)
	{

		final Clip outline = getAudioLineFromFile(resourcePath);
		if (outline != null)
		{
			mode = SOUND_MODE;
			outline.start();
			return true;
		}
		return false;
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setBrowserURL(final String url)
	{
		// SwingUtilities.invokeLater(new URLHandlerThread(url));
		new URLHandlerThread(url).start();
	}

	public void setFullscreen(final boolean fullscreen)
	{
		final boolean old = this.fullscreen;
		this.fullscreen = fullscreen;
		if (fullscreen)
		{
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.getDefaultScreenDevice().setFullScreenWindow(this);
		}
		else
		{
			this.setSize(DEFAULT_BROWSER_SIZE);
			this.setVisible(true);
		}
		propertyChangeListeners.firePropertyChange("fullscreen", old, fullscreen);
	}

	public void stop()
	{
		dispose();
	}

	@Override
	public void update(final LineEvent event)
	{
		// System.out.println("Got an event on this " + event.getType());
		if (event.getType().equals(LineEvent.Type.STOP))
		{
			final Line outline = event.getLine();
			outline.close();
		}

	}

	public boolean viewImageResource(final URL imageURL)
	{
		mode = IMAGE_MODE;
		final ImageIcon imageIcon = new ImageIcon(imageURL);
		htmlPane.currentImage = imageIcon.getImage();
		htmlPane.repaint();
		return true;
	}

	boolean isImageResource(final String path)
	{
		return path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".png");
	}

	boolean isSoundResource(final String path)
	{
		return path.endsWith(".wav") || path.endsWith(".au") || path.endsWith(".mp3") || path.endsWith(".wma");
	}

	/**
	 * Internal implementation for streaming audio from a file.
	 */
	private Clip getAudioLineFromFile(final URL url)
	{
		AudioInputStream audioInputStream = null;
		try
		{
			if (url != null)
			{
				audioInputStream = AudioSystem.getAudioInputStream(url);
			}
		}
		catch (final UnsupportedAudioFileException uafe)
		{
			System.out.println(uafe.getMessage());
			return null;
		}
		catch (final IOException ioe)
		{
			System.out.println(ioe.getMessage());
			return null;
		}

		if (audioInputStream == null)
		{
			System.out.println("SimpleMediaViewer: Warning resource not found '" + url.toExternalForm() + "'");
			return null;
		}
		AudioFormat streamFormat = null;
		streamFormat = audioInputStream.getFormat();
		final DataLine.Info info = new DataLine.Info(Clip.class, streamFormat);

		if (!AudioSystem.isLineSupported(info))
		{
			System.out.println("AudioManager: An out line matching " + info + " is not found.");
			return null;
		}

		// get and open the source data line for playback.
		final Clip outline;
		try
		{
			outline = (Clip) AudioSystem.getLine(info);
			outline.addLineListener(this);
			outline.open(audioInputStream);
		}
		catch (final LineUnavailableException ex)
		{
			System.err.println("AudioManager: Unable to open the out line: " + ex);
			return null;
		}
		catch (final IOException ex)
		{
			System.err.println("AudioManager: Unable to open the out line: " + ex);
			return null;
		}

		if (!outline.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			System.out.println("AudioManager: Gain Control is not supported on output line");
		}
		else
		{
			final FloatControl gainControl = (FloatControl) outline.getControl(FloatControl.Type.MASTER_GAIN);
			/*
			 * System.out.println("Gain was " +gainControl.getValue() +"("+gainControl.getMaximum()
			 * +")");
			 */
			gainControl.setValue(gainControl.getMaximum());
		}

		// play back the captured audio data

		return outline;
	}

	private String getSelectedURL()
	{
		final Object value = selHistory.getModel().getSelectedItem();

		String url = null;
		if (value instanceof HistoryItem)
		{
			url = (String) ((HistoryItem) value).item;

		}
		else if (value instanceof String)
		{
			url = (String) value;
		}
		return url;
	}

	private void viewHTMLResource(final URL webURL) throws IOException
	{
		mode = HTML_MODE;
		htmlPane.setPage(webURL);
		htmlPane.repaint();

	}

}
