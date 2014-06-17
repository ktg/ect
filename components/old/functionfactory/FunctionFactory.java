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

Created by: Stefan Rennick Egglestone (University of Nottingham)
Contributors:
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.functionfactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

/**
 * Used to create components which can calculate functions. <H3>Summary</H3>
 * <P>
 * FunctionFactory is a component which allows a user to select from a list of pre-defined
 * mathematical functions.
 * </P>
 * <P>
 * It provides a graphical user interface which
 * <ul>
 * <li>lists available functions
 * <li>allows a user to render a preview of each available function, using default parameters
 * <li>allows a user to create a component capable of calculating the equation representing the
 * function
 * <li>allows a user to modify properties of this component, thereby modifying parameters that
 * define the function and altering the calculation performed by the component
 * </ul>
 * </P>
 * <P>
 * FunctionFactory is supplied with a number of pre-defined functions, and developers can easily add
 * new functions using a simple plugin system.
 * </P>
 * <H3>Usage</H3>
 * <P>
 * Request an instance of the FunctionFactory component. This will cause a window to be opened on
 * your desktop, which is your graphical user interface to the function factory.
 * </P>
 * <H3>Technical Details</H3>
 * <P>
 * Developers can easily plug new functions into the FunctionFactory component. Simply
 * <ul>
 * <li>add a class implementing the <tt>ect.components.functionfactory.Function</tt> interface
 * to directory <tt>src/equip/ect/components/functionfactory/functions</tt><BR>
 * The easiest way to do this may be by extending the
 * <tt>ect.components.functionfactory.functions.AbstractFunction</tt> class. See class
 * <tt>ect.components.functionfactory.functions.Linear</tt> for an exampe of how to do this</li>
 * <li>modify file <tt>src/equip/ect/components/functionfactory/functionslist.txt</tt> file to
 * include your new class</li>
 * <li>recompile and reinstall using the <tt>functionfactory</tt> target in the ECT build file</li>
 * </ul>
 * </P>
 * 
 * @classification Behaviour/Simple Mapping
 * @displayName FunctionFactory
 * @preferred
 */

public class FunctionFactory implements Serializable
{

	public static void main(final String[] args)
	{
		new FunctionFactory();
	}

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	private int childId = 0;

	// private FunctionCreationFrame creationFrame;
	private FunctionManagementFrame managementFrame;

	private Vector children = new Vector();

	Vector childPersistenceInfo = new Vector();

	public FunctionFactory()
	{
		try
		{
			// creationFrame = new FunctionCreationFrame(this);
			managementFrame = new FunctionManagementFrame(this);
		}
		catch (final IOException e)
		{
			throw (new Error(e));
		}
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public FunctionComponent functionAdded(final Function f)
	{
		// called by creation frame when a new function is created
		return addChild(f);
	}

	public void functionDeleted(final FunctionComponent fc)
	{
		// graphical editor calls this to request removal of a function
		// component. Delete both the component, and any associated
		// persistence information

		int componentPos = -1;

		for (int i = 0; i < children.size(); i++)
		{
			final Object ob = children.elementAt(i);

			if (ob == fc)
			{
				componentPos = i;
				break;
			}
		}

		final Object[] oldChildren = getChildren();
		final Object[] oldInfo = getChildPersistenceInfo();

		children.removeElementAt(componentPos);
		childPersistenceInfo.removeElementAt(componentPos);

		final Object[] newChildren = getChildren();
		final Object[] newInfo = getChildPersistenceInfo();

		propertyChangeListeners.firePropertyChange("children", oldChildren, newChildren);

		propertyChangeListeners.firePropertyChange("childPersistenceInfo", oldInfo, newInfo);

	}

	public int getChildId()
	{
		return childId;
	}

	public String[] getChildPersistenceInfo()
	{
		return (String[]) childPersistenceInfo.toArray(new String[childPersistenceInfo.size()]);
	}

	public FunctionComponent[] getChildren()
	{
		synchronized (this)
		{
			return (FunctionComponent[]) children.toArray(new FunctionComponent[children.size()]);
		}
	}

	// Property Change Listeners
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public void setChildId(final int childId)
	{
		final int oldChildId = this.childId;
		this.childId = childId;

		propertyChangeListeners.firePropertyChange("childId", oldChildId, this.childId);
	}

	public void setChildPersistenceInfo(final String[] info)
	{
		final Vector oldInfo = childPersistenceInfo;
		childPersistenceInfo = new Vector();

		for (final String element : info)
		{
			childPersistenceInfo.add(element);
		}

		// hopefully, this method is only called when
		// a functionfactory is re-created from persistence.
		// the info held in this property will then allow
		// the function factory to re-create functions whose
		// properties have been persisted.
		// each entry in the array consists of a comma-seperated
		// entry whose first element is a function class, and
		// whose second element is the id that was used for
		// the component representing this function

		// so

		// 1. load function from class (all functions must have
		// 0-arg constructors)
		// 2. create new child component containing function
		// 3. persistence should then provide parameters for function

		int childrenAdded = 0;

		final Object[] oldChildren = getChildren();

		// get rid of any sub-components that currently exist?
		// probably not necessary

		children = new Vector();

		for (final String element : info)
		{
			try
			{
				final String[] bits = element.split(",");

				final String className = bits[0];
				final int id = (new Integer(bits[1])).intValue();

				final Class c = Class.forName(className);
				final Function f = (Function) (c.newInstance());

				final FunctionComponent fc = new FunctionComponent(id, f);
				children.add(fc);

				childrenAdded++;
			}

			// if class not found, then no worries - just move
			// onto the next one

			catch (final ClassNotFoundException e)
			{
			}
			catch (final InstantiationException e)
			{
			}
			catch (final IllegalAccessException e)
			{
			}
		}

		if (childrenAdded > 0)
		{
			final Object[] newChildren = getChildren();

			// now add these into the function management frame
			// so that they can be deleted/modified etc

			managementFrame.insertComponents((FunctionComponent[]) newChildren);

			propertyChangeListeners.firePropertyChange("children", oldChildren, newChildren);
		}

		propertyChangeListeners.firePropertyChange("childPersistenceInfo", oldInfo, childPersistenceInfo);
	}

	public void stop()
	{
		// creationFrame.dispose();
		managementFrame.dispose();
	}

	protected FunctionComponent addChild(final Function f)
	{
		FunctionComponent[] oldValue = null;
		FunctionComponent[] newValue = null;

		String[] oldInfo = null;
		String[] newInfo = null;

		boolean done = false;

		FunctionComponent newChild = null;

		synchronized (this)
		{
			oldValue = getChildren();
			oldInfo = getChildPersistenceInfo();

			try
			{
				// get a new id, add a function component using
				// that id. Will cause subcomponent creation.
				// als, add an entry indicating function class and id
				// This is used to recreate function component
				// when function factory re-created from storage

				final int newChildId = getChildId();

				newChild = new FunctionComponent(newChildId, f);

				children.addElement(newChild);

				final String functionInfo = f.getClass().getName() + "," + newChildId;

				childPersistenceInfo.addElement(functionInfo);

				setChildId(newChildId + 1);

				newValue = getChildren();
				newInfo = getChildPersistenceInfo();

				done = true;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating child: " + e);
				e.printStackTrace(System.err);
			}
		}
		if (done)
		{
			propertyChangeListeners.firePropertyChange("children", oldValue, newValue);

			propertyChangeListeners.firePropertyChange("childPersistenceInfo", oldInfo, newInfo);

			return newChild;
		}
		else
		{
			return null;
		}
	}

	protected void setChildren(final FunctionComponent[] children)
	{

		final FunctionComponent[] oldValue = getChildren();

		this.children = new Vector();

		for (final FunctionComponent element : children)
		{
			this.children.addElement(element);
		}

		final FunctionComponent[] newValue = getChildren();

		propertyChangeListeners.firePropertyChange("children", oldValue, newValue);
	}
}
