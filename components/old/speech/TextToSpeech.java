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

 Created by: Jan Humble (University of Nottingham)
 Contributors:
 Jan Humble (University of Nottingham)

 */
package equip.ect.components.speech;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Converts text input to a synthesized voice.
 * 
 * <H3>Description</H3> This simple program shows how to use FreeTTS without requiring the Java
 * Speech API (JSAPI).
 * 
 * <H3>Installation</H3> No installation required. freetts.jar should install automatically with
 * ECT.
 * 
 * <H3>Configuration</H3> <H3>Usage</H3> Set the voice to use. Currently supports voices "kevin",
 * "kevin16" Set the 'text' property to start the synthesizer.
 * 
 * <H3>Technical Details</H3> Uses the FreeTTS speech synthesizer library.
 * 
 * @classification Media/Audio/Output
 * @technology FreeTTS speech synthesis
 * @defaultInputProperty text
 * 
 * @author humble
 */
public class TextToSpeech implements Serializable
{

	/**
	 * Example of how to list all the known voices.
	 */
	public static Voice[] getAllVoices()
	{
		System.out.println();
		System.out.println("All voices available:");
		final VoiceManager voiceManager = VoiceManager.getInstance();
		final Voice[] voices = voiceManager.getVoices();

		return voices;
		// setAvailableVoices(voiceNames);
	}

	public static String[] getVoiceNames(final Voice[] voices)
	{
		if (voices != null)
		{
			final String[] voiceNames = new String[voices.length];
			for (int i = 0; i < voices.length; i++)
			{
				voiceNames[i] = voices[i].getName();
			}
			return voiceNames;
		}
		return null;
	}

	public static void listAllVoices()
	{

		final Voice[] voices = getAllVoices();
		for (int i = 0; i < voices.length; i++)
		{
			System.out.println("    " + voices[i].getName() + " (" + voices[i].getDomain() + " domain)");

		}
	}

	public static void main(final String[] args)
	{

		listAllVoices();

		final String voiceName = (args.length > 0) ? args[0] : "alan";

		System.out.println();
		System.out.println("Using voice: " + voiceName);

		/*
		 * The VoiceManager manages all the voices for FreeTTS.
		 */
		final VoiceManager voiceManager = VoiceManager.getInstance();
		final Voice helloVoice = voiceManager.getVoice(voiceName);

		if (helloVoice == null)
		{
			System.err.println("Cannot find a voice named " + voiceName + ".  Please specify a different voice.");
			System.exit(1);
		}

		/*
		 * Allocates the resources for the voice.
		 */
		helloVoice.allocate();

		/*
		 * Synthesize speech.
		 */
		helloVoice.speak("Thank you for giving me a voice. " + "I'm so glad to say hello to this world.");

		/*
		 * Clean up and leave.
		 */
		helloVoice.deallocate();
		System.exit(0);
	}

	protected String text = "hello";

	// private Synthesizer synthesizer = null;

	protected String voiceName = "kevin";

	private final VoiceManager voiceManager;

	private Voice voice;

	private String[] availableVoices;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public TextToSpeech()
	{
		/*
		 * The VoiceManager manages all the voices for FreeTTS.
		 */

		setAvailableVoices(getVoiceNames(getAllVoices()));
		voiceManager = VoiceManager.getInstance();

	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public String[] getAvailableVoices()
	{
		return this.availableVoices;
	}

	/**
	 * input getter
	 */
	public synchronized String getText()
	{
		return text;
	}

	/**
	 * input getter
	 */
	public synchronized String getVoice()
	{
		return voiceName;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * input setter
	 */
	public synchronized void setText(final String text)
	{
		final String oldInput = this.text + "!$"; // change to trigger new everytime
		this.text = text;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("text", oldInput, this.text);
		speak(text);
	}

	/**
	 * input setter
	 */
	public synchronized void setVoice(final String voiceName)
	{
		final String oldInput = this.voiceName;
		if (voiceManager == null) { return; }
		final Voice newVoice = voiceManager.getVoice(voiceName);
		if (newVoice != null)
		{
			if (voice != null)
			{
				voice.deallocate();
			}
			this.voiceName = voiceName;
			voice = newVoice;
			voice.allocate();
			propertyChangeListeners.firePropertyChange("voiceName", oldInput, this.voiceName);
		}
	}

	public void speak(final String text)
	{
		/*
		 * if (synthesizer != null) { System.out.println("Saying: " + text);
		 * synthesizer.speakPlainText(text, null); }
		 */
		if (voice != null)
		{
			System.out.println("Saying: " + text);
			voice.speak(text);
		}

	}

	public void stop()
	{

		if (voice != null)
		{
			voice.deallocate();
		}
	}

	protected void setAvailableVoices(final String[] available)
	{
		final String[] old = this.availableVoices;
		this.availableVoices = available;
		propertyChangeListeners.firePropertyChange("availableVoices", old, availableVoices);
	}

}
