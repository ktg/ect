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
 * Creates a 4x4 transformation matrix which will scale an object in the x,y and z axes<br>
 * <H3>Summary</H3> Creates a 4x4 transformation matrix which will scale an object in the x,y and z
 * axes.<br>
 * <H3>Usage</H3> Specify scale in the x, y and z zxes in properties 'scaleX', 'scaleY' and 'scaleZ'
 * respectively.<br>
 * The resultant property 'transformationMatrix' will contain a matrix which can be used to scale
 * (resize) and object as specified.<br>
 * 
 * @display Scale Creator
 * @classification Media/3D/Utils
 * @preferred
 */
public class ScaleCreator implements Serializable
{
	protected String transformationMatrix = "";

	String scaleX = "1.0";

	String scaleY = "1.0";

	String scaleZ = "1.0";

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
		 * scale the object as specified in scale X, Y and Z properties
		 */
		double doubScaleX = 1.0;
		double doubScaleY = 1.0;
		double doubScaleZ = 1.0;

		try
		{
			doubScaleX = Double.parseDouble(this.scaleX);
		}
		catch (final NumberFormatException nfe)
		{
		}

		try
		{
			doubScaleY = Double.parseDouble(this.scaleY);
		}
		catch (final NumberFormatException nfe)
		{
		}

		try
		{
			doubScaleZ = Double.parseDouble(this.scaleZ);
		}
		catch (final NumberFormatException nfe)
		{
		}

		final Vector3d vector = new Vector3d(doubScaleX, doubScaleY, doubScaleZ);
		final Transform3D transform = new Transform3D();
		transform.setScale(vector);

		/*
		 * set the transformation
		 */
		final double[] transMatrixDouble = new double[16];
		transform.get(transMatrixDouble);
		// we need to convert from Java3d to ar toolkit column format and co-ordinate system
		final String oldMatrix = transformationMatrix;
		transformationMatrix = transMatrixDouble[0] + " " + transMatrixDouble[4] + " " + transMatrixDouble[8] + " "
				+ transMatrixDouble[12] + " " + transMatrixDouble[1] + " " + transMatrixDouble[5] + " "
				+ transMatrixDouble[9] + " " + transMatrixDouble[13] + " " + transMatrixDouble[2] + " "
				+ transMatrixDouble[6] + " " + transMatrixDouble[10] + " " + transMatrixDouble[14] + " "
				+ transMatrixDouble[3] + " " + transMatrixDouble[7] + " " + transMatrixDouble[11] + " "
				+ transMatrixDouble[15];

		propertyChangeListeners.firePropertyChange("transformationMatrix", oldMatrix, this.transformationMatrix);
	}

	public String getScaleX()
	{
		return scaleX;
	}

	public String getScaleY()
	{
		return scaleY;
	}

	public String getScaleZ()
	{
		return scaleZ;
	}

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

	public void setScaleX(final String newScale)
	{
		final String oldScaleX = this.scaleX;
		this.scaleX = newScale;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("scaleX", oldScaleX, this.scaleX);

		createTransformation();
	}

	public void setScaleY(final String newScale)
	{
		final String oldScaleY = this.scaleY;
		this.scaleY = newScale;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("scaleY", oldScaleY, this.scaleY);

		createTransformation();
	}

	public void setScaleZ(final String newScale)
	{
		final String oldScaleZ = this.scaleZ;
		this.scaleZ = newScale;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("scaleZ", oldScaleZ, this.scaleZ);

		createTransformation();
	}
}