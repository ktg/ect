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
package equip.ect.components.arauthoring.transformation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.StringTokenizer;

/**
 * Calculates a roll and pitch, given a 4x4 transformation matrix<br>
 * <H3>Summary</H3> Calculates a roll and pitch angle, given a 4x4 transformation matrix.<br>
 * Roll specifies the angle between the z-axis and the transformed z-axis in the direction of the
 * y-axis<br>
 * (i.e. the angle the tranformation would rotate an object about the x-axis by).<br>
 * <br>
 * Pitch specifies the angle between the z-axis and the transformed z-axis in the direction of the
 * x-axis<br>
 * (i.e. the angle the tranformation would rotate an object about the y-axis by).<br>
 * <H3>Usage</H3> Set 'transformationMatrix' to a 4x4 transformation matrix specified in column
 * major order as a space seperated list of 16 numbers.<br>
 * For example the 'glyphTransform' property on the 'VideoARToolkitGlyphTracker' component produces
 * a 4x4 transformation matrix as above.<br>
 * <br>
 * The output properties are:<br>
 * 'pitch': the angle the tranformation would rotate an object about the y-axis by<br>
 * pitch value ranges from -1.0 to 1.0 where -1.0 corresponds to pitching backwards 180 degrees and
 * +1.0 corresponds to pitching forward 180 degrees<br>
 * 'roll': the angle the tranformation would rotate an object about the x-axis by<br>
 * roll value ranges from -1.0 to 1.0 where -1.0 corresponds to rolling backwards 180 degrees and
 * +1.0 corresponds to rolling forward 180 degrees<br>
 * 
 * @display Rotation Disassembler
 * @classification Media/3D/Utils
 */
public class RotationDisassembler implements Serializable
{
	protected String transformationMatrix = "1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0 0.0 0.0 0.0 0.0 1.0";

	String roll = "0.0";

	String pitch = "0.0";

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

	public void disassembleRotation()
	{
		if (transformationMatrix == null) { return; }

		final StringTokenizer transformationTokens = new StringTokenizer(transformationMatrix, " ");
		if (transformationTokens.countTokens() != 16)
		{
			System.out.println("Warning, transformation matrix does not contain 16 elements. Unable to disassemble.");
			return;
		}

		double ax;
		double ay;
		double az;

		// skip the first 8 numbers
		for (int i = 0; i < 8; i++)
		{
			transformationTokens.nextToken();
		}

		try
		{
			ax = Double.parseDouble(transformationTokens.nextToken());
			ay = Double.parseDouble(transformationTokens.nextToken());
			az = Double.parseDouble(transformationTokens.nextToken());
		}
		catch (final NumberFormatException nfe)
		{
			// setAttention("One of the matrix elements does not contain a numerical value");
			return;
		}

		// calculate the roll
		// i.e. take the dot product of the z axis [0,0,1] and the rotated z axis without the x
		// component [0, ay, az]
		// and divide it by length of the z axis (i.e. 1) * the length of the rotated z axis
		// (ignoring the x component) (i.e. sqrt(ay*ay + az*az)))
		// then take the inverse (arc) cos
		// or put more simply, acos(az / sqrt(ay*ay + az*az))
		double rollDb;
		if (ay >= 0)
		{
			rollDb = (Math.acos(az / Math.sqrt(ay * ay + az * az))) / Math.PI;
		}
		else
		{
			rollDb = (-Math.acos(az / Math.sqrt(ay * ay + az * az))) / Math.PI;
		}

		// calculate the pitch
		// i.e. take the dot product of the z axis [0,0,1] and the rotated z axis without the y
		// component [ax, 0, az]
		// and divide it by length of the z axis (i.e. 1) * the length of the rotated z axis
		// (ignoring the y component) (i.e. sqrt(ax*ax + az*az)))
		// then take the inverse (arc) cos
		// or put more simply, acos(az / sqrt(ax*ax + az*az))
		double pitchDb;
		if (ax >= 0)
		{
			pitchDb = (Math.acos(az / Math.sqrt(ax * ax + az * az))) / Math.PI;
		}
		else
		{
			pitchDb = (-Math.acos(az / Math.sqrt(ax * ax + az * az))) / Math.PI;
		}

		final String oldPitch = pitch;
		final String oldRoll = roll;

		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);

		pitch = nf.format(pitchDb);
		roll = nf.format(rollDb);

		propertyChangeListeners.firePropertyChange("roll", oldRoll, roll);
		propertyChangeListeners.firePropertyChange("pitch", oldPitch, pitch);
	}

	public String getPitch()
	{
		return pitch;
	}

	public String getRoll()
	{
		return roll;
	}

	/*
	 * public void disassembleRotation() { if (transformationMatrix == null) { return; }
	 * 
	 * StringTokenizer transformationTokens = new StringTokenizer(transformationMatrix, " "); if
	 * (transformationTokens.countTokens() != 16) { System.out.println(
	 * "Warning, transformation matrix does not contain 16 elements. Unable to disassemble.");
	 * return; }
	 * 
	 * double place02; double place00; double place10; double place11; double place12; double
	 * place20; double place22; try { place00 =
	 * Double.parseDouble(transformationTokens.nextToken()); place10 =
	 * Double.parseDouble(transformationTokens.nextToken()); transformationTokens.nextToken();
	 * place20 = Double.parseDouble(transformationTokens.nextToken());
	 * 
	 * transformationTokens.nextToken(); place11 =
	 * Double.parseDouble(transformationTokens.nextToken()); transformationTokens.nextToken();
	 * transformationTokens.nextToken();
	 * 
	 * place02 = Double.parseDouble(transformationTokens.nextToken()); place12 =
	 * Double.parseDouble(transformationTokens.nextToken()); place22 =
	 * Double.parseDouble(transformationTokens.nextToken()); transformationTokens.nextToken(); }
	 * catch (NumberFormatException nfe) {
	 * //setAttention("One of the matrix elements does not contain a numerical value"); return; }
	 * 
	 * NumberFormat nf = NumberFormat.getInstance(); nf.setMaximumFractionDigits(3);
	 * 
	 * //System.out.println("place11: " + place11); //System.out.println("place12: " + place12);
	 * 
	 * //place00 = Math.acos(place00); //place02 = -Math.asin(place02); place10 =
	 * -Math.asin(place10); place11 = Math.acos(place11); place12 = Math.asin(place12);
	 * 
	 * place20 = Math.asin(place20); place22 = Math.acos(place22);
	 * 
	 * //System.out.println("place11: " + place11); //System.out.println("place12: " + place12);
	 * 
	 * // calculate the rotations String oldRot = this.rotateX; if (place12 < 0) { rotateX =
	 * nf.format(place11 / (2 * Math.PI)); } else { rotateX = nf.format((2 * Math.PI - place11) / (2
	 * * Math.PI)); }
	 * 
	 * //rotateX = nf.format(Math.asin(place12)); //(Math.asin(place12) / (Math.PI * 2));
	 * propertyChangeListeners.firePropertyChange("rotateX", oldRot, rotateX);
	 * 
	 * oldRot = this.rotateY; if (place20 < 0) { rotateY = nf.format(place22 / (2 * Math.PI)); }
	 * else { rotateY = nf.format((2 * Math.PI - place22) / (2 * Math.PI)); } //rotateY =
	 * nf.format(-Math.asin(place02)); //((-Math.asin(place02)) / (Math.PI * 2));
	 * propertyChangeListeners.firePropertyChange("rotateY", oldRot, rotateY); oldRot =
	 * this.rotateZ; if (place10 < 0) { rotateZ = nf.format(place11 / (2 * Math.PI)); } else {
	 * rotateZ = nf.format((2 * Math.PI - place11) / (2 * Math.PI)); } //rotateZ =
	 * nf.format(-Math.asin(place10)); //((-Math.asin(place10)) / (Math.PI * 2));
	 * propertyChangeListeners.firePropertyChange("rotateZ", oldRot, rotateZ); }
	 */

	public synchronized String getTransformationMatrix()
	{
		return transformationMatrix;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setTransformationMatrix(final String transformationMatrix)
	{
		final String oldMatrix = this.transformationMatrix;
		this.transformationMatrix = transformationMatrix;
		propertyChangeListeners.firePropertyChange("transformationMatrix", oldMatrix, this.transformationMatrix);

		disassembleRotation();
	}
}