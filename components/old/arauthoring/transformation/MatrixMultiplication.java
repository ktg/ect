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

import Jama.Matrix;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * Perfrom matrix multiplication: matrixResult = matrixA x matrixB<br>
 * <H3>Summary</H3> Perfroms matrix multiplication: matrixResult = matrixA x matrixB.<br>
 * This component is designed to multiply transformation matrices hence<br>
 * all matrices are a 4x4 matrix specified as a space seperated<br>
 * list of double values in column major order.<br>
 * <H3>Usage</H3> To multiply a matrx A by a matrix B, in that order, set the properties<br>
 * 'matrixA' and 'matrixB' respectively. The property 'matrixResult' will contain<br>
 * the result of the multiplication.<br>
 * <br>
 * Errors will be reported in the 'attention' property.<br>
 *
 * @display Matrix Multiplication
 * @classification Media/3D/Utils
 * @preferred
 */
public class MatrixMultiplication implements Serializable
{
	protected String attention = "";

	protected double[] matrixA;

	protected double[] matrixB;

	protected double[] matrixResult;

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public MatrixMultiplication()
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

	public synchronized double[] getMatrixA()
	{
		return matrixA;
	}

	public synchronized void setMatrixA(final double[] matrix)
	{
		final double[] oldMatrix = this.matrixA;
		this.matrixA = matrix;
		propertyChangeListeners.firePropertyChange("matrixA", oldMatrix, this.matrixA);

		performMultiplication();
	}

	public synchronized double[] getMatrixB()
	{
		return matrixB;
	}

	public synchronized void setMatrixB(final double[] matrix)
	{
		final double[] oldMatrix = this.matrixB;
		this.matrixB = matrix;
		propertyChangeListeners.firePropertyChange("matrixB", oldMatrix, this.matrixB);

		performMultiplication();
	}

	public synchronized double[] getMatrixResult()
	{
		return matrixResult;
	}

	public void performMultiplication()
	{
		/*
		 * if (matrixA == null || matrixB == null) { return; }
		 *
		 * StringTokenizer matrixAStr = new StringTokenizer(matrixA, " "); if
		 * (matrixAStr.countTokens() != 16) {
		 * reportError("Warning, matrixA string does not contain 16 tokens"); return; }
		 *
		 * StringTokenizer matrixBStr = new StringTokenizer(matrixB, " "); if
		 * (matrixBStr.countTokens() != 16) {
		 * reportError("Warning, matrixB string does not contain 16 tokens"); return; }
		 *
		 * double[] matrixADoubs = new double[16]; double[] matrixBDoubs = new double[16];
		 *
		 * try { for (int i=0; i<16; i++) { matrixADoubs[i] =
		 * Double.parseDouble(matrixAStr.nextToken()); matrixBDoubs[i] =
		 * Double.parseDouble(matrixBStr.nextToken()); } } catch (NumberFormatException nfe) {
		 * reportError(
		 * "A specified input transformation matrix does not contain 16 space seperated double values. Cannot perform calculation"
		 * ); }
		 */

		final Matrix matA = new Matrix(matrixA, 4);
		final Matrix matB = new Matrix(matrixB, 4);
		final Matrix matRes = matA.times(matB);

		/*
		 * // build the result String matResStr = ""; for (int i=0; i<4; i++) { for (int j=0; j<4;
		 * j++) { matResStr = matResStr + matRes.get(j, i) + " "; } } matResStr =
		 * matResStr.substring(0, matResStr.length() - 1);
		 */

		final double[] oldMatrix = this.matrixResult;
		this.matrixResult = matRes.getColumnPackedCopy();
		propertyChangeListeners.firePropertyChange("matrixResult", oldMatrix, this.matrixResult);
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
}