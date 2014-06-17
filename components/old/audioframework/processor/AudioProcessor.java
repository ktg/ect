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

Created by: Chris Greenhalgh (University of Nottingham)
Contributors:
  Chris Greenhalgh (University of Nottingham)

 */
package equip.ect.components.audioframework.processor;

import java.io.IOException;
import java.util.Vector;

import equip.ect.components.audioframework.common.AbstractAudioProcessor;
import equip.ect.components.audioframework.common.FrameProcessor;

/**
 * Configurable audio processor (part of Media/Audio/Analysis framework).
 * 
 * <H3>Description</H3> Audio processing component which can receive audio frames (via sink), apply
 * various processing steps to the frame, and then output it to another audio component (via
 * source).
 * 
 * <H3>Installation</H3> See AudioCaptureManager component for video capture requirements.
 * 
 * <H3>Configuration</H3> Configured by setting configuration property. Allows value of the form
 * "PROCESSOR1(ARG1,ARG2,...),PROCESSOR2(ARG1,...),...".
 * <p>
 * Available processors are:
 * <ul>
 * <li>"Abs()" - make each sample absolute (i.e. negative values made positive). Generally
 * "Square()" is a better choice.</li>
 * <li>"Square()" - make each sample the square of the input - good for determining noise/energy
 * level in the sound with a following AudioAverage component.</li>
 * <li>"Subsample(N)", e.g. "Subsample(4)" - emits frames with each sample the average of N input
 * samples. Note: this simple averaging will result in aliasing artefacts in the final sound; a
 * better subsampling with low-pass filtering should be implemented and used in sonically demanding
 * applications.</li>
 * <li>"Window(ALPHA,MAXLENSECONDS)", e.g. "Window(0.02,1)" - emits frames with a hamming-style
 * window (alpha+(1-alpha)*(1+cos(2*pi*isample/framesize))/2) applied to the frame. The true hamming
 * window has alpha=0.02. This should be done before any operation which convert to frequency domain
 * (e.g. "FrequencyMatch") to reduce the effects of the sound being effectively truncated at the
 * beginning and end of the frame.</li>
 * <li>"Gain(GAIN)", e.g. "Gain(10)" - emits frames with each sample scaled by the specified amount
 * (and clipped in short representation if off-scale).</li>
 * <li>"FrequencyMatch(F0, F1, F2, ...)", e.g. "FrequencyMatch(0, 100, 200)" - emits frames with
 * each sample being the amount of energy in the input frame at the specified frequency. The
 * frequency 0 is used to request total frame energy. Energies are scaled to be per-sample.
 * Configuration syntax will output three 'samples', the total evergy per sample (average), the
 * energy at 100Hz and the energy at 200Hz. The selectivity of this process will depend on the
 * number of cycles per frame.</li>
 * <ul>
 * <li>A few notes... normal concert A is 440Hz. There are 12 semitones per octave (A A# B B C C# D#
 * E F F# G G#) in the european scale. One octave is a factor of 2 frequency. Semitones (are
 * approximately) equal ratio steps, i.e. 12th root of 2, = 1.0594630943592952645618252949463, or
 * 5.9463% change in frequency. Frequency ratios in the full scale are:</li>
 * <ul>
 * <li>A 1</li>
 * <li>A# 1.0594630943592952645618252949463</li>
 * <li>B 1.1224620483093729814335330496792</li>
 * <li>C 1.1892071150027210667174999705605</li>
 * <li>C# 1.2599210498948731647672106072782</li>
 * <li>D 1.3348398541700343648308318811845</li>
 * <li>D# 1.4142135623730950488016887242097</li>
 * <li>E 1.4983070768766814987992807320298</li>
 * <li>F 1.5874010519681994747517056392723</li>
 * <li>F# 1.6817928305074290860622509524664</li>
 * <li>G 1.781797436280678609480452411181</li>
 * <li>G# 1.8877486253633869932838263133351</li>
 * <li>A' 2</li>
 * </ul>
 * <li>Frequencies for A below concert A to concert A are:</li>
 * <ul>
 * <li>A 220Hz</li>
 * <li>A# 233.08188075904495820360156488812</li>
 * <li>B 246.94165062806205591537727092938</li>
 * <li>C 261.6255653005986346778499935232</li>
 * <li>C# 277.18263097687209624878633360116</li>
 * <li>D 293.66476791740756026278301386048<br>
 * </li>
 * <li>D# 311.12698372208091073637151932598<br>
 * </li>
 * <li>E 329.62755691286992973584176104638<br>
 * </li>
 * <li>F 349.22823143300388444537524063984<br>
 * </li>
 * <li>F# 369.99442271163439893369520954252<br>
 * </li>
 * <li>G 391.99543598174929408569953045982<br>
 * </li>
 * <li>G# 415.3046975799451385224417889337</li>
 * <li>A' 440Hz</li>
 * <li>A# 466.1637615180899164072031297762</li>
 * <li>B 493.8833012561241118307545418586</li>
 * <li>C 523.2511306011972693556999870464</li>
 * <li>C# 554.3652619537441924975726672022</li>
 * <li>D 587.3295358348151205255660277208<br>
 * </li>
 * <li>D# 622.2539674441618214727430386518<br>
 * </li>
 * <li>E 659.2551138257398594716835220926<br>
 * </li>
 * <li>F 698.4564628660077688907504812796<br>
 * </li>
 * <li>F# 739.988845423268797867390419085<br>
 * </li>
 * <li>G 783.9908719634985881713990609196<br>
 * </li>
 * <li>G# 830.6093951598902770448835778674</li>
 * <li>A'' 880Hz</li>
 * <li>e.g. Format(float),Window(0.02),FrequencyMatch(0,587.3). <br>
 * </li>
 * </ul>
 * <li>Consider using this with an AudioSelectValue component to extract the required value from the
 * output frame.</li>
 * </ul>
 * <li>"Format(float | short)", e.g. "Format(float)" - emits frames in the requested format.</li>
 * </ul>
 * 
 * <H3>Usage</H3>
 * Typically, link to sink from the source property of a AudioCaptureDevice component (created by an
 * AudioCaptureManager component) or from the source property of a chained AudioProcessor component
 * to a AudioSelectValue or AudioAverage component which exposes the final processing result.
 * <p>
 * View the component web page using the URL in the configUrl property, which includes some
 * visualisation facilities.
 * 
 * <H3>Technical Details</H3> Part of the audioframework set of components; extends
 * AbstractAudioProcessor.
 * 
 * @classification Media/Audio/Analysis
 * @technology JMF audio processing
 * @defaultInputProperty sink
 * @defulatOutputProperty source
 */
public class AudioProcessor extends AbstractAudioProcessor
{
	/**
	 * cons
	 */
	public AudioProcessor() throws IOException
	{
		super("AudioProcessor");
		setConfiguration("");
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	@Override
	protected FrameProcessor getProcessor(final String name, final Vector args)
	{
		final Vector args2 = (Vector) args.clone();
		final FrameProcessor p = super.getProcessor(name, args2);
		if (p != null) { return p; }
		System.out.println("getProcessor(AudioProcessor): " + name);
		if (name.equals("Abs2")) { return new AbsProcessor(); }
		System.err.println("AudioProcessor asked for unknown processor: " + name);
		args.setElementAt(((String) (args2.elementAt(0))) + "|Abs2*", 0);
		return null;
	}
}
