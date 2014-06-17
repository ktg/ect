/*
 * <COPYRIGHT> Copyright (c) 2004-2005, University of Nottingham All rights
 * reserved. Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: -
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. - Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. - Neither the name of the University of Nottingham nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission. THIS
 * SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </COPYRIGHT> Created by: Alastair Hampshire (University of Nottingham)
 * Contributors: Alastair Hampshire (University of Nottingham)
 */
package equip.ect.components.arauthoring.transformation;


import Jama.Matrix;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Calculates the relative transformation of one transformation matrix relative to another
 * transformation matrix.<br>
 * <H3>Summary</H3> Calculates the relative transformation of one transformation matrix relative to
 * another transformation matrix.<br>
 * Specifically, 'relativeTransformationMatrix' shows transformation of
 * 'satelliteTransformationMatrix' relative to 'baseTransformationMatrix'<br>
 *
 * @display Relative Transformation
 * @classification Media/3D/Behaviour
 * @preferred
 */
public class RelativeDistance implements Serializable
{
	/*
	 * Error feedback/ user reporting
	 */
	protected String attention = "";

	/*
	 * base, satellite and relative transformation matrices.
	 */
	protected double[] baseTransformationMatrix = null;
	protected double[] relativeTransformationMatrix = null;
	protected double[] satelliteTransformationMatrix = null;

	protected boolean baseVisible = false;
	protected boolean satelliteVisible = false;

	protected int closeness = 150;

	protected boolean isClose = false;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	/**
	 * no-args constructor (required)
	 */
	public RelativeDistance()
	{
	}

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

	public double[] getBaseTransformationMatrix()
	{
		return baseTransformationMatrix;
	}

	public void setBaseTransformationMatrix(final double[] matrix)
	{
		final double[] oldMatrix = this.baseTransformationMatrix;
		this.baseTransformationMatrix = matrix;
		propertyChangeListeners
				.firePropertyChange("baseTransformationMatrix", oldMatrix, this.baseTransformationMatrix);

		tryToCalcRelativeTransformation();
	}

	public int getCloseness()
	{
		return closeness;
	}

	public void setCloseness(final int closeness)
	{
		this.closeness = closeness;
	}

	public double[] getRelativeTransformationMatrix()
	{
		return relativeTransformationMatrix;
	}

	/*
	 * public void updateRelativeTransformationMatrix(double[] matrix) { double[] oldMatrix =
	 * this.relativeTransformationMatrix; this.relativeTransformationMatrix = matrix;
	 * propertyChangeListeners.firePropertyChange("relativeTransformationMatrix", oldMatrix,
	 * this.relativeTransformationMatrix); }
	 */

	public double[] getSatelliteTransformationMatrix()
	{
		return satelliteTransformationMatrix;
	}

	public void setSatelliteTransformationMatrix(final double[] matrix)
	{
		final double[] oldMatrix = this.satelliteTransformationMatrix;
		this.satelliteTransformationMatrix = matrix;
		propertyChangeListeners.firePropertyChange("satelliteTransformationMatrix", oldMatrix,
				this.satelliteTransformationMatrix);

		tryToCalcRelativeTransformation();
	}

	public boolean isBaseVisible()
	{
		return baseVisible;
	}

	public void setBaseVisible(final boolean baseVisible)
	{
		final boolean oldBaseVisible = this.baseVisible;
		if (oldBaseVisible != baseVisible)
		{
			this.baseVisible = baseVisible;
			propertyChangeListeners.firePropertyChange("baseVisible", oldBaseVisible, baseVisible);
			tryToCalcRelativeTransformation();
		}
	}

	public boolean isClose()
	{
		return isClose;
	}

	public boolean isSatelliteVisible()
	{
		return satelliteVisible;
	}

	public void setSatelliteVisible(final boolean satelliteVisible)
	{
		final boolean oldSatelliteVisible = this.satelliteVisible;
		if (oldSatelliteVisible != satelliteVisible)
		{
			this.satelliteVisible = satelliteVisible;
			propertyChangeListeners.firePropertyChange("satelliteVisible", oldSatelliteVisible, satelliteVisible);
			tryToCalcRelativeTransformation();
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void reportError(final String newAtt)
	{
		final String oldAtt = this.attention;
		this.attention = newAtt;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("attention", oldAtt, this.attention);
	}

	public synchronized void stop()
	{
	}

	/*
	 * called to calculate transformation of satellite relative to base
	 */
	public void tryToCalcRelativeTransformation()
	{
		final boolean oldClose = isClose;

		if (!baseVisible || !satelliteVisible)
		{
			isClose = false;
		}
		else if (satelliteTransformationMatrix == null || baseTransformationMatrix == null)
		{
			reportError("A base and a satellite transformation matrix must be specified");
			// updateRelativeTransformationMatrix("");
			isClose = false;
		}
		else
		{
			final Matrix baseMatrixObj = new Matrix(baseTransformationMatrix, 4);
			// baseMatrixObj = baseMatrixObj.inverse();
			Matrix satelliteMatrixObj = new Matrix(satelliteTransformationMatrix, 4);
			satelliteMatrixObj = satelliteMatrixObj.inverse();
			final Matrix relativeMatrix = satelliteMatrixObj.times(baseMatrixObj);

			final double[] oldMatrix = this.relativeTransformationMatrix;
			this.relativeTransformationMatrix = relativeMatrix.getColumnPackedCopy();

			propertyChangeListeners.firePropertyChange("relativeTransformationMatrix", oldMatrix,
					this.relativeTransformationMatrix);

			final double x = relativeMatrix.get(0, 3);
			final double y = relativeMatrix.get(1, 3);
			final double z = relativeMatrix.get(2, 3);

			final double distance = Math.sqrt(x * x + y * y + z * z);
			isClose = distance < closeness;
			reportError("");
		}
		propertyChangeListeners.firePropertyChange("isClose", oldClose, isClose);
	}
}