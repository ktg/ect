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
 Shahram Izadi (University of Nottingham)

 */
package equip.ect.components.associationlearner;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;

import equip.ect.ContainerManager;
import equip.ect.Persistable;
import equip.ect.PersistenceManager;

/**
 * This component can learn mappings between an input and an output, and then return the learned
 * output if the input is used again.
 * 
 * <h3>Description</h3> The component learns mappings between values provided to in, and
 * trainingOut. Setting the in property will try to match that input to any of the keys in the map
 * set and output the corresponding value in out. Returns an empty string if no mapping is present
 * for that input. Setting the trainingOut value will learn a mapping between the trainingOut and
 * whatever the in property is set as.
 * 
 * @author Chris Greenhalgh
 * @classification Behaviour/Learning
 * @displayName Association Learner
 * @defaultInputProperty in
 * @defaultOutputProperty out
 */
public class AssociationLearner implements Serializable, Persistable
{

	public static void main(final String[] args)
	{
		new AssociationLearner();
	}

	private Hashtable associations = new Hashtable();

	private TreeSet trainedInputs = new TreeSet();

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// Properties
	private Object in = null;
	private Object out = null;
	private Object trainingOut = null;

	private File persistFile = null;

	public AssociationLearner()
	{
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public Hashtable getAssociations()
	{
		return associations;
	}

	public Object getIn()
	{
		return in;
	}

	public Object getOut()
	{
		return out;
	}

	public TreeSet getTrainedInputs()
	{
		return trainedInputs;
	}

	public Object getTrainingOut()
	{
		return trainingOut;
	}

	@Override
	public synchronized void load(final File persistFile, final ContainerManager containerManager) throws IOException
	{
		if (persistFile != null)
		{
			try
			{
				final ClassLoader loader = containerManager.getContainerManagerHelper().getClassLoader();
				final Object obj = PersistenceManager.getPersistenceManager().loadObject(persistFile, loader);
				if (obj != null && obj instanceof AssociationLearner)
				{
					final AssociationLearner loadedLearner = ((AssociationLearner) obj);
					final TreeSet trainedInputs = loadedLearner.getTrainedInputs();
					final Hashtable associations = loadedLearner.getAssociations();
					if (associations != null && trainedInputs != null)
					{
						this.associations = associations;
						this.trainedInputs = trainedInputs;
					}
				}
				this.persistFile = persistFile;
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized File persist(final ContainerManager containerManager) throws IOException
	{
		if (persistFile == null)
		{
			persistFile = File
					.createTempFile("AssociationLearner", "",
									PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY);
		}
		try
		{
			PersistenceManager.getPersistenceManager().persistObject(persistFile, this);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return persistFile;
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setIn(final Object in)
	{
		final Object oldIn = this.in;
		this.in = in;
		// not sure that the java publisher framework expects this on an
		// input?!
		propertyChangeListeners.firePropertyChange("in", oldIn, in);
		runScript("in");
	}

	public void setTrainingOut(final Object in)
	{
		final Object oldIn = this.trainingOut;
		this.trainingOut = in;
		// not sure that the java publisher framework expects this on an
		// input?!
		propertyChangeListeners.firePropertyChange("trainingOut", oldIn, in);
		runScript("trainingOut");
	}

	protected Object recall(final Object in)
	{
		// just discrete equality for now
		if (in == null) { return null; }
		Object out = associations.get(in);
		if (out == null)
		{
			// try inexact??
			if (in instanceof Number)
			{
				// find one before
				final SortedSet allBefore = trainedInputs.headSet(in);
				Number before = (Number) ((allBefore.size() == 0) ? null : allBefore.last());
				// find one after
				// when the item is not in the set it seems to give the one before
				final SortedSet allAfter = trainedInputs.tailSet(in);
				Number after = (Number) ((allAfter.size() == 0) ? null : allAfter.first());
				/*
				 * if (((Comparable)after).compareTo(in)>0) { // in < after; try next if
				 * (allAfter.size()>=2) { Iterator i = allAfter.iterator(); // discard first
				 * i.next(); after = (Number)i.next(); } else after = null; }
				 */
				if (before == null)
				{
					before = after;
				}
				else if (after == null)
				{
					after = before;
				}
				if (after == null)
				{
					// nothing
					return null;
				}

				// interpolate?
				final Object beforeOut = associations.get(before);
				final Object afterOut = associations.get(after);
				if (beforeOut == null) { return afterOut; }
				if (afterOut == null) { return beforeOut; }

				System.out.println("Recall " + in + ": before = " + before + " (" + beforeOut + "), after = " + after
						+ " (" + afterOut + ")");

				// how much from before to after?
				double alpha = 0;
				if (!before.equals(after))
				{
					alpha = (((Number) in).doubleValue() - before.doubleValue())
							/ (after.doubleValue() - before.doubleValue());
				}

				// interpolate or choose?
				if (beforeOut instanceof Number && afterOut instanceof Number)
				{
					// interpolate
					final double outVal = alpha * ((Number) afterOut).doubleValue() + (1 - alpha)
							* ((Number) beforeOut).doubleValue();
					out = null;
					if (afterOut instanceof Integer)
					{
						out = new Integer((int) outVal);
					}
					else if (afterOut instanceof Double)
					{
						out = new Double(outVal);
					}
					else if (afterOut instanceof Float)
					{
						out = new Float((float) outVal);
					}
					// ADD MORE TYPES HERE - I CAN'T THINK HOW TO DO THIS
					// WITH REFLECTION...
					System.out.println("Interpolated (fraction " + alpha + ")");
				}
				else
				{
					// choose
					if (alpha < 0.5)
					{
						out = beforeOut;
					}
					else
					{
						out = afterOut;
					}
					System.out.println("Chosen (fraction " + alpha + ")");
				}
			}
		}
		System.out.println("Recall " + in + " -> " + (out == null ? "null" : out));
		return out;
	}

	// // implementation of Persistable interface

	protected void runScript(final String propertyChanged)
	{
		if (propertyChanged.equals("in"))
		{
			setOut(recall(this.in));
		}
		else if (propertyChanged.equals("trainingOut"))
		{
			train(this.in, this.trainingOut);
			setOut(recall(this.in));
		}
	}

	protected void setOut(final Object out)
	{
		final Object oldOut = this.out;
		this.out = out;
		propertyChangeListeners.firePropertyChange("out", oldOut, out);
	}

	protected void train(final Object in, final Object out)
	{
		if (in == null)
		{
			System.out.println("Ignore train on null in");
			return;
		}
		if (in instanceof Number)
		{
			trainedInputs.add(in);
		}
		associations.put(in, out);
		System.out.println("Train " + in + " -> " + (out == null ? "null" : out));
	}

}
