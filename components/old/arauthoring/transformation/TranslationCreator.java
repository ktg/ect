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

import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;

/**
 * Creates a 4x4 transformation matrix which defines a movement in the x,y and z direction<br>
 * <H3>Summary</H3> Creates a 4x4 transformation matrix given a translation (movement) in the x,y
 * and z direction.<br>
 * <H3>Usage</H3> Specify translation in the x, y and z direction in properties 'xTranslation',
 * 'yTranslation' and 'zTranslation' respectively.<br>
 * The resultant property 'transformationMatrix' will contain a matrix which can be used to
 * translate (move) and object as specified.<br>
 * 
 * @display Translation Creator
 * @classification Media/3D/Utils
 * @preferred
 */
public class TranslationCreator implements Serializable
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

	public void createTransformation()
	{
		/*
		 * translate the object as specified in translate X, Y, Z
		 */
		double transX = 0.0;
		double transY = 0.0;
		double transZ = 0.0;

		try
		{
			transX = Double.parseDouble(this.xTranslation);
		}
		catch (final NumberFormatException nfe)
		{
		}

		try
		{
			transY = Double.parseDouble(this.yTranslation);
		}
		catch (final NumberFormatException nfe)
		{
		}

		try
		{
			transZ = Double.parseDouble(this.zTranslation);
		}
		catch (final NumberFormatException nfe)
		{
		}

		final Vector3d translationVector = new Vector3d(transX, transY, transZ);
		final Transform3D translation = new Transform3D();
		translation.setTranslation(translationVector);

		/*
		 * set the transformation
		 */
		final double[] transMatrixDouble = new double[16];
		translation.get(transMatrixDouble);
		// we need to convert from Java3d to ar toolkit column format
		final String oldMatrix = transformationMatrix;
		transformationMatrix = transMatrixDouble[0] + " " + transMatrixDouble[4] + " " + transMatrixDouble[8] + " "
				+ transMatrixDouble[12] + " " + transMatrixDouble[1] + " " + transMatrixDouble[5] + " "
				+ transMatrixDouble[9] + " " + transMatrixDouble[13] + " " + transMatrixDouble[2] + " "
				+ transMatrixDouble[6] + " " + transMatrixDouble[10] + " " + transMatrixDouble[14] + " "
				+ transMatrixDouble[3] + " " + transMatrixDouble[7] + " " + transMatrixDouble[11] + " "
				+ transMatrixDouble[15];

		propertyChangeListeners.firePropertyChange("transformationMatrix", oldMatrix, this.transformationMatrix);

		// setTransformationMatrix(matrixStr);
		// setAttention(attention);
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

	public void setXTranslation(final String newTranslation)
	{
		final String oldTrans = this.xTranslation;
		this.xTranslation = newTranslation;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("xTranslation", oldTrans, this.xTranslation);

		createTransformation();
	}

	public void setYTranslation(final String newTranslation)
	{
		final String oldTrans = this.yTranslation;
		this.yTranslation = newTranslation;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("yTranslation", oldTrans, this.yTranslation);

		createTransformation();
	}

	public void setZTranslation(final String newTranslation)
	{
		final String oldTrans = this.zTranslation;
		this.zTranslation = newTranslation;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("zTranslation", oldTrans, this.zTranslation);

		createTransformation();
	}
}