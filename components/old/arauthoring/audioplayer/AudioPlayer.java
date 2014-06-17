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

  Created by: Alastair Hampshire (University of Nottingham)
  Contributors:
  Alastair Hampshire (University of Nottingham)

 */
package equip.ect.components.arauthoring.audioplayer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Wav audio file player.<br>
 * <H3>Summary</H3> Component to play .wav audio files<br>
 * <H3>Configuration</H3> To load a wav file, specify the filename and file path in the
 * 'audioFilename' property.<br>
 * Errors that occur whilst loading a wav file will be reported in the 'attention' property.<br>
 * <H3>Usage</H3> Once an audio file has been loaded (see Configuration for details) the file can be
 * played by setting <br>
 * the 'play' property to \"true\". Play can be stopped by setting the 'play' property to \"false\".<br>
 * When an audio file finished playing (i.e. reaches the end), the 'play' property will
 * automatically be set<br>
 * to \"false\".<br>
 * 
 * @display Audio Player
 * @classification Media/Audio/Output
 */
public class AudioPlayer implements Serializable // , Runnable
{

	class FinishedPlayingClipThread extends Thread
	{
		@Override
		public void run()
		{
			while (playSound)
			{
				try
				{
					Thread.sleep(200);
				}
				catch (final InterruptedException ie)
				{
				}

				if (!clip.isActive())
				{
					final String oldPlay = play;
					play = "false";
					propertyChangeListeners.firePropertyChange("play", oldPlay, play);

					clip.setFramePosition(0);
					volumeControl.setValue(0);
					playSound = false;
				}
			}
		}
	}

	class StopPlayingSoundThread extends Thread
	{
		@Override
		public void run()
		{

			while (playSound)
			{
				// System.out.println("Gain: " + volumeControl.getValue());

				try
				{
					Thread.sleep(100);
				}
				catch (final InterruptedException ie)
				{
				}

				// reduce the volume if we're stopping playing
				if (stopPlayingTime != -1 && stopSoundDelay != 0 && stoppingPlaying == true)
				{
					// float diff = (float) stopPlayingTime - (float) System.currentTimeMillis();
					final float diff2 = (stopPlayingTime - System.currentTimeMillis());
					// System.out.println("diff: " + diff);
					// System.out.println("Set gain " + stopPlayingTime + ", " +
					// System.currentTimeMillis() + ", " + stopSoundDelay);
					final float gain = -35 * (1 - (diff2 / stopSoundDelay));
					// System.out.println("Gain: " + gain);
					volumeControl.setValue(gain);
				}

				// we're carrying on playing, so up the volume again
				if (stopPlayingTime != -1 && stopSoundDelay != 0 && stoppingPlaying == false
						&& System.currentTimeMillis() - stopPlayingTime < (stopSoundDelay / 3))
				{
					// float diff = (float) stopPlayingTime - (float) System.currentTimeMillis();
					final float diff2 = (System.currentTimeMillis() - stopPlayingTime);
					// System.out.println("diff: " + diff);
					// System.out.println("Set gain " + stopPlayingTime + ", " +
					// System.currentTimeMillis() + ", " + stopSoundDelay);
					final float setGain = gain * (1 - (diff2 / (stopSoundDelay / 3)));
					// System.out.println("Gain: " + setGain);
					volumeControl.setValue(setGain);
				}

				if (stoppingPlaying == true && System.currentTimeMillis() > stopPlayingTime)
				{
					clip.stop();
					clip.setFramePosition(0);
					volumeControl.setValue(0);
					playSound = false;
				}
			}
		}
	}

	private static final int EXTERNAL_BUFFER_SIZE = 1024;
	// private SourceDataLine line = null;
	private Clip clip = null;
	private FloatControl volumeControl = null;
	private AudioInputStream audioInputStream = null;

	// FinishedPlayingClipThread finishedPlayingClipThread = null;
	// StopPlayingSoundThread stopPlayingSoundThread = null;

	private boolean changing = false;
	private float gain = 0;
	protected String attention = "";

	protected long stopSoundDelay = 2500;
	protected String audioFilename = "";
	protected String play = "";

	private boolean playSound = false;
	long stopPlayingTime = -1;
	boolean stoppingPlaying = false;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	/*
	 * protected String stop = "";
	 * 
	 * public String getStop() { return stop; }
	 * 
	 * public void setStop(String newStop) { String oldStop = this.stop; this.stop = newStop;
	 * propertyChangeListeners.firePropertyChange("stop", oldStop, this.stop);
	 * 
	 * boolean stopPlaying = true; }
	 */

	public String getAudioFilename()
	{
		return audioFilename;
	}

	public String getPlay()
	{
		return play;
	}

	public Long getStopSoundDelay()
	{
		return stopSoundDelay;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setAttention(final String newAtt)
	{
		final String oldAtt = this.attention;
		this.attention = newAtt;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("attention", oldAtt, this.attention);
	}

	/*
	 * public void tryToPlaySound() { if (play.equals("true")) { if (playSound == false) { playSound
	 * = true; new Thread(this).start(); clip.start(); } } else { // build in a delay before
	 * stopping the sounds try { Thread.sleep(stopSoundDelay); } catch (InterruptedException ie) { }
	 * 
	 * // now check again - are we still stopping the sound clip if (play.equals("false")) {
	 * clip.stop(); clip.setFramePosition(0); playSound = false; } } }
	 */

	public void setAudioFilename(final String filename)
	{
		final String oldFilename = this.audioFilename;
		this.audioFilename = filename;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("audioFilename", oldFilename, this.audioFilename);

		tryToLoadAudioFile();
	}

	public void setPlay(final String newPlay)
	{
		final String oldPlay = this.play;
		this.play = newPlay;
		propertyChangeListeners.firePropertyChange("play", oldPlay, this.play);

		if (clip == null) { return; }

		if (play.equals("true"))
		{
			// stopPlayingTime = -1;
			stopPlayingTime = System.currentTimeMillis();
			stoppingPlaying = false;
			gain = volumeControl.getValue();

			if (playSound == false)
			{
				playSound = true;

				final FinishedPlayingClipThread finishedPlayingClipThread = new FinishedPlayingClipThread();
				final StopPlayingSoundThread stopPlayingSoundThread = new StopPlayingSoundThread();

				finishedPlayingClipThread.start();
				stopPlayingSoundThread.start();
				clip.start();
			}
		}
		else
		{
			stopPlayingTime = System.currentTimeMillis() + stopSoundDelay;
			stoppingPlaying = true;
		}

		/*
		 * 
		 * //stopPlaying = false; if (changing == false) { changing = true; tryToPlaySound();
		 * changing = false; }
		 */
	}

	public void setStopSoundDelay(final Long newDelay)
	{
		final long oldDelay = stopSoundDelay;
		this.stopSoundDelay = newDelay;

		propertyChangeListeners.firePropertyChange("stopSoundDelay", oldDelay, newDelay);
	}

	public void stop()
	{
		if (clip != null)
		{
			clip.drain();
			clip.close();

			try
			{
				audioInputStream.close();
			}
			catch (final IOException ioe)
			{
			}
		}
	}

	public void tryToLoadAudioFile()
	{
		if (clip != null)
		{
			clip.drain();
			clip.close();
		}

		if (audioInputStream != null)
		{
			try
			{
				audioInputStream.close();
			}
			catch (final IOException ioe)
			{
			}
		}

		final File soundFile = new File(audioFilename);

		try
		{
			audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			audioInputStream.mark(1048576000);
		}
		catch (final UnsupportedAudioFileException uafe)
		{
			setAttention("Audio file format not supported by audio player");
			return;
		}
		catch (final IOException ioe)
		{
			setAttention("IO exception: " + ioe.getMessage());
			return;
		}

		final AudioFormat audioFormat = audioInputStream.getFormat();

		// DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try
		{
			// line = (SourceDataLine) AudioSystem.getLine(info);
			clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			// line.open(audioFormat);
		}
		catch (final LineUnavailableException lue)
		{
			setAttention("Selected audio line unavailable: " + lue.getMessage());
			return;
		}
		catch (final Exception e)
		{
			setAttention("Audio exception: " + e.getMessage());
			return;
		}

		volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

		// line.start();

		final String oldPlay = this.play;
		this.play = "false";
		propertyChangeListeners.firePropertyChange("play", oldPlay, this.play);

		setAttention("");
	}
}