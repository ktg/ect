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

/**
 * Creates a 4x4 transformation matrix which defines a rotation about the x,y and z axis.<br>
 * <H3>Summary</H3> Creates a 4x4 transformation matrix given a rotation about the x,y and z axis.
 * Roatation of 0.0 to 1.0 equals 0 deg to 360 deg<br>
 * <H3>Usage</H3> Specify rotations about the x, y and z axis in properties 'rotateX', 'rotateY' and
 * 'rotateZ' respectively.<br>
 * Roatation properties take values between 0.0 and 1.0 which corresponds to rotations of 0 degrees
 * to 360 degrees.<br>
 * The property 'transformationMatrix' will contain a matrix which can be used to rotate and object
 * as specified.<br>
 * 
 * @display Rotation Creator
 * @classification Media/3D/Utils
 */
public class RotationCreator implements Serializable
{
	protected double[] transformationMatrix = { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
												0.0, 1.0 };

	double rotateX = 0.0;

	double rotateY = 0.0;

	double rotateZ = 0.0;

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

	public void createRotation()
	{
		/*
		 * rotate the object as specified in rotate X, Y and Z fields
		 */
		final Transform3D rotate = new Transform3D();

		final Transform3D rotateXTrans = new Transform3D();
		final Transform3D rotateYTrans = new Transform3D();
		final Transform3D rotateZTrans = new Transform3D();

		/*
		 * double rotateX = 0.0; double rotateY = 0.0; double rotateZ = 0.0;
		 * 
		 * 
		 * try { rotateX = Double.parseDouble(this.rotateX); } catch (NumberFormatException nfe) { }
		 * 
		 * 
		 * try { rotateY = Double.parseDouble(this.rotateY); } catch (NumberFormatException nfe) { }
		 * 
		 * try { rotateZ = Double.parseDouble(this.rotateZ); } catch (NumberFormatException nfe) { }
		 */

		rotateXTrans.rotX(Math.PI * 2 * rotateX);
		rotateYTrans.rotY(Math.PI * 2 * rotateY);
		rotateZTrans.rotZ(Math.PI * 2 * rotateZ);

		rotate.mul(rotateXTrans);
		rotate.mul(rotateYTrans);
		rotate.mul(rotateZTrans);

		/*
		 * set the transformation
		 */
		// double[] transMatrixDouble = new double[16];
		// we need to convert from Java3d to ar toolkit column format
		final double[] oldMatrix = transformationMatrix;
		rotate.get(transformationMatrix);

		// transformationMatrix = transMatrixDouble[0] + " " + transMatrixDouble[4] + " " +
		// transMatrixDouble[8] + " " + transMatrixDouble[12] +
		// " " + transMatrixDouble[1] + " " + transMatrixDouble[5] + " " + transMatrixDouble[9] +
		// " " + transMatrixDouble[13] +
		// " " + transMatrixDouble[2] + " " + transMatrixDouble[6] + " " + transMatrixDouble[10] +
		// " " + transMatrixDouble[14] +
		// " " + transMatrixDouble[3] + " " + transMatrixDouble[7] + " " + transMatrixDouble[11] +
		// " " + transMatrixDouble[15];

		propertyChangeListeners.firePropertyChange("transformationMatrix", oldMatrix, this.transformationMatrix);
	}

	public double getRotateX()
	{
		return rotateX;
	}

	public double getRotateY()
	{
		return rotateY;
	}

	public double getRotateZ()
	{
		return rotateZ;
	}

	public synchronized double[] getTransformationMatrix()
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

	public void setRotateX(final double rotateX)
	{
		final double oldRot = this.rotateX;
		this.rotateX = rotateX;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("rotateX", oldRot, this.rotateX);

		createRotation();
	}

	public void setRotateY(final double rotateY)
	{
		final double oldRot = this.rotateY;
		this.rotateY = rotateY;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("rotateY", oldRot, this.rotateY);

		createRotation();
	}

	public void setRotateZ(final double rotateZ)
	{
		final double oldRot = this.rotateZ;
		this.rotateZ = rotateZ;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("rotateZ", oldRot, this.rotateZ);

		createRotation();
	}
}