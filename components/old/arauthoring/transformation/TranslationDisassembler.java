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
import java.util.StringTokenizer;

/**
 * Calculates an x, y and z axis translation (movement), given a 4x4 transformation matrix<br>
 * <H3>Summary</H3> Calculates an x, y and z axis translation (movement), given a 4x4 transformation
 * matrix.<br>
 * <H3>Usage</H3> Set 'transformationMatrix' to a 4x4 transformation matrix specified in column
 * major order as a space seperated list of 16 numbers.<br>
 * For example the 'glyphTransform' property on the 'VideoARToolkitGlyphTracker' component produces
 * a 4x4 transformation matrix as above.<br>
 * <br>
 * The output properties are:<br>
 * 'xTranslation': movement in the x-axis<br>
 * 'yTranslation': movement in the y-axis<br>
 * 'zTranslation': movement in the z-axis<br>
 * 
 * @display Transformation Disassembler
 * @classification Media/3D/Utils
 * @preferred
 */
public class TranslationDisassembler implements Serializable
{
	protected String transformationMatrix = "";

	String xTranslation = "0.0";

	String zTranslation = "0.0";

	String yTranslation = "0.0";

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

	public void disassembleTranslation()
	{
		if (transformationMatrix == null) { return; }

		final StringTokenizer transformationTokens = new StringTokenizer(transformationMatrix, " ");
		if (transformationTokens.countTokens() != 16) { return; }

		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();

		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();

		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();
		transformationTokens.nextToken();

		// get the translation
		final String oldXTrans = xTranslation;
		final String oldYTrans = yTranslation;
		final String oldZTrans = zTranslation;

		xTranslation = transformationTokens.nextToken();
		yTranslation = transformationTokens.nextToken();
		zTranslation = transformationTokens.nextToken();

		propertyChangeListeners.firePropertyChange("xTranslation", oldXTrans, this.xTranslation);
		propertyChangeListeners.firePropertyChange("yTranslation", oldYTrans, this.yTranslation);
		propertyChangeListeners.firePropertyChange("zTranslation", oldZTrans, this.zTranslation);
	}

	public synchronized String getTransformationMatrix()
	{
		return transformationMatrix;
	}

	public String getXTranslation()
	{
		return xTranslation;
	}

	public String getYTranslation()
	{
		return yTranslation;
	}

	public String getZTranslation()
	{
		return zTranslation;
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

		disassembleTranslation();
	}
}