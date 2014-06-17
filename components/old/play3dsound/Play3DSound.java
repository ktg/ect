/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2005, University of Nottingham All rights reserved.
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
 * Contributors: 
 * Jan Humble (University of Nottingham)
 *
 */
package equip.ect.components.play3dsound;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * A 3-D sound component that plays an audio file.
 * 
 * <H3>Description</H3> This component allows special effect controls on a sound source based on the
 * audio source movement. One is able to specify the geographical origin of the sound from the frame
 * of reference of the listener. It also allows one to specify certain parameters such as 'doppler
 * factor', 'rolloff factor', etc.
 * 
 * <H3>Installation</H3> Requires a Microsoft Windows OS with a DirectX compatible sound card.
 * Should be standard on most PC machines.
 * 
 * <H3>Configuration</H3> In order to properly appreciate the 3D sound effects, you need to have a
 * surround sound system. You should configure your speaker setup around the listener.
 * 
 * <H3>Usage</H3> To play an audio file, set the path for the location of the audio file in the
 * 'soundfile' property. Set 'loop' to true in order to loop the sound track. Set 'soundPos' in the
 * format X, Y, Z in order to set the virtual location of the sound source.
 * 
 * <H3>Technical Details</H3> Makes use of DirectX Audio API.
 * 
 * @classification Media/Audio/Output
 * @technology DirectX audio processing
 * @author humble
 * 
 * 
 */
public class Play3DSound extends JFrame implements Serializable
{

	private class EllipsePath extends Thread
	{

		@Override
		public void run()
		{
			int time = 0;
			final double ORBIT_MAX_RADIUS = 5.0f;
			final double speed = 50.0f;
			double fXScale, fYScale;
			while (true)
			{
				fXScale = 10 / 100.0f;
				fYScale = 10 / 100.0f;
				final double t = speed * time / 10000.0f;

				// Move the sound object around the listener. The maximum radius
				// of the
				// orbit is 27.5 units.
				final double[] pos = new double[6];

				pos[0] = ORBIT_MAX_RADIUS * fXScale * Math.sin(t);
				pos[1] = 0.0f;
				pos[2] = ORBIT_MAX_RADIUS * fYScale * Math.cos(t);

				pos[3] = ORBIT_MAX_RADIUS * fXScale * Math.sin(t + 0.05f);
				pos[4] = 0.0f;
				pos[5] = ORBIT_MAX_RADIUS * fYScale * Math.cos(t + 0.05f);

				setSoundPos(pos);
				time++;
				try
				{
					sleep(100);
				}
				catch (final InterruptedException ie)
				{

				}

			}

		}

	}

	public native static int close();

	public native static int initialize();

	public static void main(final String[] args)
	{

		final JFrame f = new Play3DSound();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public native static int openSoundFile(String soundFile);

	public native static int play();

	public native static int set3DParameters(double fDopplerFactor, double fRolloffFactor, double fMinDistance,
			double fMaxDistance);

	public native static int setLooping(boolean looping);

	public native static int setSoundPos(double x, double y, double z, double velX, double velY, double velZ);

	public native static int stop();

	static double convertLinearSliderPosToLogScale(final int sliderPos)
	{
		if (sliderPos > 0 && sliderPos <= 10)
		{
			return sliderPos * 0.01d;
		}
		else if (sliderPos > 10 && sliderPos <= 20)
		{
			return (sliderPos - 10) * 0.1d;
		}
		else if (sliderPos > 20 && sliderPos <= 30)
		{
			return (sliderPos - 20) * 1.0d;
		}
		else if (sliderPos > 30 && sliderPos <= 40) { return (sliderPos - 30) * 10.0d; }
		return 0.0d;
	}

	static int convertLogScaleToLinearSliderPos(final double value)
	{
		if (value > 0.0d && value <= 0.1d)
		{
			return (int) (value / 0.01d);
		}
		else if (value > 0.1d && value <= 1.0d)
		{
			return (int) (value / 0.1d) + 10;
		}
		else if (value > 1.0d && value <= 10.0d)
		{
			return (int) (value / 1.0d) + 20;
		}
		else if (value > 10.0d && value <= 100.0d) { return (int) (value / 10.0d) + 30; }

		return 0;
	}

	private String soundFile = "./testing.wav";

	private double[] soundPos = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	private boolean loop = true, playing = false;

	private JTextField soundFileTF;

	private JCheckBox loopCB;

	private JButton playB;

	private Double dopplerFactor = new Double(0.0d);

	private Double rolloffFactor = new Double(0.0d);

	private Double minDistance = new Double(5.0d);

	private Double maxDistance = new Double(10.0d);

	private JSlider doppSlider;

	private JTextField doppTF;

	private JTextField rolloffTF;

	private JSlider rolloffSlider;

	private JTextField minDistTF;

	private JSlider minDistSlider;

	private JTextField maxDistTF;

	private JSlider maxDistSlider;

	private JFormattedTextField xTF, yTF, zTF;

	private JFormattedTextField xVelTF;

	private JFormattedTextField yVelTF;

	private JFormattedTextField zVelTF;

	static
	{
		try
		{
			System.loadLibrary("play3dsound");
		}
		catch (final Throwable t)
		{
			t.printStackTrace();
		}
	}

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public Play3DSound()
	{
		super("Play3DSound");
		// setSize(400, 400);
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel p = new JPanel();
		p.add(new JLabel("Sound file: "));
		this.soundFileTF = new JTextField(30);
		p.add(soundFileTF);
		JButton b = new JButton("browse ...");
		b.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser chooser = new JFileChooser("./");
				chooser.setFileFilter(new FileFilter()
				{

					@Override
					public boolean accept(final File f)
					{
						if (f.getName().endsWith(".wav")) { return true; }
						return false;
					}

					@Override
					public String getDescription()
					{
						return new String("Only wav files accepted");
					}

				});

				final int returnVal = chooser.showOpenDialog(Play3DSound.this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					final File file = chooser.getSelectedFile();
					if (file != null)
					{
						System.out.println("You chose to open this file: " + file.getName());
						try
						{
							setSoundFile(file.getCanonicalPath());
						}
						catch (final IOException e1)
						{
							System.out.println("Error accessing the file");
							e1.printStackTrace();
						}
					}
				}
			}

		});
		p.add(b);

		mainPanel.add(p);

		p = new JPanel();
		p.add(new JLabel("X:"));
		p.add(xTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[0])));
		xTF.setColumns(5);
		p.add(new JLabel("Y:"));
		p.add(yTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[1])));
		yTF.setColumns(5);
		p.add(new JLabel("Z:"));
		p.add(zTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[2])));
		zTF.setColumns(5);
		p.add(new JLabel("X VEL:"));
		p.add(xVelTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[3])));
		xVelTF.setColumns(5);
		p.add(new JLabel("Y VEL:"));
		p.add(yVelTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[4])));
		yVelTF.setColumns(5);
		p.add(new JLabel("Z VEL:"));
		p.add(zVelTF = new JFormattedTextField(NumberFormat.getNumberInstance().format(soundPos[5])));
		zVelTF.setColumns(5);

		b = new JButton("set");
		b.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				System.out.println(xTF.getValue().getClass());
				final double[] pos = { Double.parseDouble(xTF.getText()), Double.parseDouble(yTF.getText()),
										Double.parseDouble(zTF.getText()), Double.parseDouble(xVelTF.getText()),
										Double.parseDouble(yVelTF.getText()), Double.parseDouble(zVelTF.getText()), };
				setSoundPos(pos);
			}

		});
		p.add(b);
		mainPanel.add(p);

		loopCB = new JCheckBox("loop");
		loopCB.setSelected(loop);
		loopCB.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				setLoop(loopCB.isSelected());
			}
		});

		mainPanel.add(loopCB);

		playB = new JButton("Play");
		playB.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent ae)
			{
				final String command = ae.getActionCommand();
				if (command.equals("Play"))
				{
					setPlaying(true);

				}
				else if (command.equals("Stop"))
				{
					setPlaying(false);
				}

			}

		});

		// create basic 3D controls
		final JPanel controlPanel = new JPanel(new GridLayout(4, 1));

		doppTF = new JTextField(dopplerFactor.toString(), 5);
		doppTF.setEditable(false);
		doppSlider = new JSlider(0, 100);
		doppSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent ce)
			{
				final double value = convertLinearSliderPosToLogScale(doppSlider.getValue());
				doppTF.setText(Double.toString(value));
				setDopplerFactor(new Double(value));
			}
		});
		doppSlider.setValue(convertLogScaleToLinearSliderPos(dopplerFactor.doubleValue()));
		controlPanel.add(createSliderControlPanel("doppler factor", doppSlider, doppTF));

		rolloffTF = new JTextField(rolloffFactor.toString(), 5);
		rolloffTF.setEditable(false);
		rolloffSlider = new JSlider(0, 100);
		rolloffSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent ce)
			{
				System.out.println("change listener triggered");
				final double value = convertLinearSliderPosToLogScale(rolloffSlider.getValue());
				rolloffTF.setText(Double.toString(value));
				setRolloffFactor(new Double(value));
			}
		});
		rolloffSlider.setValue(convertLogScaleToLinearSliderPos(rolloffFactor.doubleValue()));
		controlPanel.add(createSliderControlPanel("rolloff factor", rolloffSlider, rolloffTF));

		minDistTF = new JTextField(minDistance.toString(), 5);
		minDistTF.setEditable(false);
		minDistSlider = new JSlider(0, 40);
		minDistSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent ce)
			{
				final double value = convertLinearSliderPosToLogScale(minDistSlider.getValue());
				minDistTF.setText(Double.toString(value));
				setMinDistance(new Double(value));
			}
		});
		minDistSlider.setValue(convertLogScaleToLinearSliderPos(minDistance.doubleValue()));
		controlPanel.add(createSliderControlPanel("min distance", minDistSlider, minDistTF));

		maxDistTF = new JTextField(maxDistance.toString(), 5);
		maxDistTF.setEditable(false);
		maxDistSlider = new JSlider(0, 40);
		maxDistSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent ce)
			{
				final double value = convertLinearSliderPosToLogScale(maxDistSlider.getValue());
				maxDistTF.setText(Double.toString(value));
				setMaxDistance(new Double(value));
			}
		});
		maxDistSlider.setValue(convertLogScaleToLinearSliderPos(maxDistance.doubleValue()));
		controlPanel.add(createSliderControlPanel("max distance", maxDistSlider, maxDistTF));

		mainPanel.add(controlPanel);
		mainPanel.add(playB);
		getContentPane().add(mainPanel);

		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(final WindowEvent we)
			{
				close();
				// System.exit(0);
			}
		});

		pack();
		setVisible(true);

		/*
		 * NOTE: initialize() needs to be called after the window context is created and visible, as
		 * DirectX requires the active window to be active for setting callbacks and coop levels.
		 * Should perhaps fix this with by sending this Java frame handle to the init call. But what
		 * happens when called from ect?
		 */
		initialize();
		setSoundFile(soundFile);
		// (new EllipsePath()).start();
	}

	/**
	 * Property Change Listeners
	 */
	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	@Override
	public void finalize() throws Throwable
	{
		close();
		super.finalize();
	}

	/**
	 * @return Returns the dopplerFactor.
	 */
	public Double getDopplerFactor()
	{
		return dopplerFactor;
	}

	/**
	 * @return Returns the maxDistance.
	 */
	public Double getMaxDistance()
	{
		return maxDistance;
	}

	/**
	 * @return Returns the minDistance.
	 */
	public Double getMinDistance()
	{
		return minDistance;
	}

	/**
	 * @return Returns the rolloffFactor.
	 */
	public Double getRolloffFactor()
	{
		return rolloffFactor;
	}

	/**
	 * @return Returns the soundFile.
	 */
	public String getSoundFile()
	{
		return soundFile;
	}

	/**
	 * @return Returns the soundPos.
	 */
	public double[] getSoundPos()
	{
		return soundPos;
	}

	/**
	 * @return Returns the loop.
	 */
	public boolean isLooping()
	{
		return loop;
	}

	/**
	 * @return Returns the playing.
	 */
	public boolean isPlaying()
	{
		return playing;
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
	 * @param dopplerFactor
	 *            The dopplerFactor to set.
	 */
	public void setDopplerFactor(final Double dopplerFactor)
	{
		final Double old = this.dopplerFactor;
		this.dopplerFactor = dopplerFactor;
		update3DSettings();
		propertyChangeListeners.firePropertyChange("dopplerFactor", old, dopplerFactor);
	}

	/**
	 * @param loop
	 *            The loop to set.
	 */
	public void setLoop(final boolean looping)
	{
		final boolean old = this.loop;
		this.loop = looping;
		propertyChangeListeners.firePropertyChange("loop", old, loop);
	}

	/**
	 * @param maxDistance
	 *            The maxDistance to set.
	 */
	public void setMaxDistance(final Double maxDistance)
	{
		final Double old = this.maxDistance;
		this.maxDistance = maxDistance;
		update3DSettings();
		propertyChangeListeners.firePropertyChange("maxDistance", old, maxDistance);
	}

	/**
	 * @param minDistance
	 *            The minDistance to set.
	 */
	public void setMinDistance(final Double minDistance)
	{
		final Double old = this.minDistance;
		this.minDistance = minDistance;
		update3DSettings();
		propertyChangeListeners.firePropertyChange("minDistance", old, minDistance);
	}

	/**
	 * @param playing
	 *            The playing to set.
	 */
	public void setPlaying(final boolean playing)
	{
		this.playing = playing;
		if (playing)
		{
			playB.setText("Stop");
			play();
			update3DSettings();
			// (new EllipsePath()).start();
		}
		else
		{
			stop();
			playB.setText("Play");
		}
	}

	/**
	 * @param rolloffFactor
	 *            The rolloffFactor to set.
	 */
	public void setRolloffFactor(final Double rolloffFactor)
	{
		final Double old = this.rolloffFactor;
		this.rolloffFactor = rolloffFactor;
		update3DSettings();
		propertyChangeListeners.firePropertyChange("rolloffFactor", old, rolloffFactor);
	}

	/**
	 * @param soundFile
	 *            The soundFile to set.
	 */
	public void setSoundFile(final String soundFile)
	{
		final File file = new File(soundFile);
		if (!file.isFile())
		{
			System.out.println("Warning: Sound file not found '" + soundFile + "'");
			return;
		}
		final String old = this.soundFile;

		try
		{
			this.soundFile = file.getCanonicalPath();
		}
		catch (final IOException e)
		{

			e.printStackTrace();
			return;
		}
		soundFileTF.setText(soundFile);
		openSoundFile(soundFile);
		propertyChangeListeners.firePropertyChange("soundFile", old, soundFile);
	}

	/**
	 * @param soundPos
	 *            The soundPos to set.
	 */
	public void setSoundPos(final double[] soundPos)
	{
		final double[] old = this.soundPos;

		if (soundPos.length == 6)
		{
			this.soundPos = soundPos;

		}
		else if (soundPos.length == 3)
		{
			this.soundPos = new double[6];
			this.soundPos[0] = soundPos[0];
			this.soundPos[1] = soundPos[1];
			this.soundPos[2] = soundPos[2];
			this.soundPos[3] = old[3];
			this.soundPos[4] = old[4];
			this.soundPos[5] = old[5];

		}

		setSoundPos(soundPos[0], soundPos[1], soundPos[2], soundPos[3], soundPos[4], soundPos[5]);
		xTF.setText(String.valueOf(soundPos[0]));
		yTF.setText(String.valueOf(soundPos[1]));
		zTF.setText(String.valueOf(soundPos[2]));
		xVelTF.setText(String.valueOf(soundPos[3]));
		yVelTF.setText(String.valueOf(soundPos[4]));
		zVelTF.setText(String.valueOf(soundPos[5]));
		propertyChangeListeners.firePropertyChange("soundPos", old, soundPos);
	}

	JPanel createSliderControlPanel(final String name, final JSlider slider, final JTextField valueField)
	{
		final JPanel p = new JPanel(new FlowLayout());
		final JLabel l = new JLabel(name);
		l.setPreferredSize(new Dimension(40, l.getHeight()));
		p.add(l);
		p.add(valueField);
		p.add(slider);
		return p;
	}

	protected synchronized void update3DSettings()
	{
		set3DParameters(dopplerFactor.doubleValue(), rolloffFactor.doubleValue(), minDistance.doubleValue(),
						maxDistance.doubleValue());
	}

}
