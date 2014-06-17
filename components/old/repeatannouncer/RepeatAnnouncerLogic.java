/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Southampton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Southampton
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

Created by: Mark Thompson (University of Southampton)
Contributors:
  Mark Thompson (University of Southampton)

 */
package equip.ect.components.repeatannouncer;

// vim: expandtab sw=4 ts=4 sts=4:

/*
 * Repeat Announcer, $RCSfile: RepeatAnnouncerLogic.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 */

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import equip.data.DataSession;
import equip.data.GUID;
import equip.data.StringArrayBoxImpl;
import equip.data.StringBoxImpl;
import equip.data.Tuple;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;

/**
 * <b>RepeatAnnouncerLogic</b> encanpsulates the business logic of the RepeatAnnouncer component.
 * 
 * @author Mark Thompson &lt;mkt@ecs.soton.ac.uk&gt;
 */
public class RepeatAnnouncerLogic implements PropertyChangeListener
{

	/**
	 * The <code>DSItemEventHandler</code> has its <code>dataspaceEvent</code> method invoked when
	 * an event occurs of interest in the dataspace
	 */
	private class DSItemEventHandler implements DataspaceEventListener
	{
		@Override
		public void dataspaceEvent(final DataspaceEvent evt)
		{
			logger.info("Notified of a dataspace event");
		}
	}

	/**
	 * The <code>PublisherTask</code> is invoked at regular intervals when the <code>interval</code>
	 * property is non-zero. This is where the main meat of the business logic of this component is
	 * realised: the message is published to the dataspace for other processes to consume.
	 */
	private class PublisherTask extends SwingTimerTask
	{
		@Override
		public void doRun()
		{
			logger.debug("PublisherTask.doRun() - would say something about '" + component.getMessage() + "'");
			if (myId != null)
			{
				final Tuple item = makeTuple(component.getMessage());
				try
				{
					dataspace.update(item);
				}
				catch (final DataspaceInactiveException _)
				{
					logger.error("Dataspace gawn saath: " + _.toString());
				}
			}
			else
			{
				logger.error("Can't publish - don't have a GUID");
			}
		}
	}

	/**
	 * The <code>SwingTimerTask</code> is a Swing-friendly <code>TimerTask</code> that doesn't
	 * impose on the interactivity of the GUI event loop
	 * 
	 * @see java.util.TimerTask
	 */
	private abstract class SwingTimerTask extends TimerTask
	{
		public abstract void doRun();

		@Override
		public void run()
		{
			if (!EventQueue.isDispatchThread())
			{
				EventQueue.invokeLater(this);
			}
			else
			{
				doRun();
			}
		}
	}

	/** Logging through good-ole log4j */
	private static Logger logger = Logger.getLogger(RepeatAnnouncerLogic.class);
	/** Glue back to hosting equip component and our GUI */
	private RepeatAnnouncer component;
	/** Regular emissions of our message */
	private Timer timer;
	/** Bean handle to Equip dataspace */
	private DataspaceBean dataspace;
	/** replication/notification callback session */
	private DataSession session;

	/** ID with which we publish our message to the space */
	private GUID myId;

	/** Handle name for our component */
	final static private String myName = "RepeatAnnouncer";

	/** Pseudo-classname for our message (hey, it's not a string, and it's not a real datatype) */
	final static private String myType = "ect.components.repeatannouncer.Announcement";

	/**
	 * Constructor - initialises business logic with values from bean
	 * 
	 * @param ra
	 *            hook back to controlling component and GUI
	 */
	public RepeatAnnouncerLogic(final RepeatAnnouncer ra)
	{
		logger.debug("Repeat Announcer Logic");
		component = ra;
		component.addPropertyChangeListener(this);

		// set up timers
		timer = null;
		if (component.getInterval() > 0)
		{
			// logger.debug("scheduling publisher timer");
			timer = new Timer();
			timer.scheduleAtFixedRate(	new PublisherTask(), component.getInterval() * 1000L,
										component.getInterval() * 1000L);
		}

		// set up equip
		logger.debug("bootstrapping equip");
		try
		{
			dataspace = new DataspaceBean();
			try
			{
				dataspace.setDataspaceUrl("equip://:9123");
			}
			catch (final Exception __)
			{
				logger.error(__.toString());
			}

			// fail if dataspace inactive
			if (!dataspace.isActive())
			{
				logger.error("Dataspace appears inactive!");
				throw new DataspaceInactiveException("post-discovery");
			}

			// get a GUID
			myId = dataspace.allocateId();
			logger.debug("Allocated GUID: " + myId.toString());

			// `publish initial message
			dataspace.add(makeTuple(component.getMessage()));

			// Don't think we need to worry about other instances awakening(?)
			logger.debug("Create notification callback session");
			final Tuple template = new TupleImpl(new StringBoxImpl(myType), null);
			template.name = myName;
			session = dataspace.addDataspaceEventListener(template, true, new DSItemEventHandler());

			// logger.debug("Sync...");
			// dataspace.getDataProxy().waitForEvents(false);
			logger.debug("OK");
		}
		catch (final DataspaceInactiveException _)
		{
			logger.error("Dataspace fell over: " + _.toString());
		}
	}

	/**
	 * Implementation of <code>java.beans.PropertyChangeListener</code> interface
	 * 
	 * @param evt
	 *            details of the property change
	 */
	@Override
	public void propertyChange(final PropertyChangeEvent evt)
	{
		// logger.debug("informed of property change");
		if (evt.getPropertyName().equals("interval"))
		{
			final int ov = ((Integer) evt.getOldValue()).intValue();
			final int nv = ((Integer) evt.getNewValue()).intValue();

			// logger.debug("it's the interval - we check timers ("+ov+","+nv+")");
			if ((ov != nv) || (nv == 0))
			{
				// logger.debug("either value has changed or new value is 0: killing existing timer");
				if (timer != null)
				{
					timer.cancel();
					timer = null;
				}
			}
			if (nv != 0)
			{
				logger.debug("Scheduling publisher task for " + nv);
				timer = new Timer();
				timer.scheduleAtFixedRate(new PublisherTask(), nv * 1000L, nv * 1000L);
			}
		}
		else
		{
			// logger.debug("if it's not the interval, we don't care");
		}
	}

	/**
	 * Helper function to make application-specific tuples
	 * 
	 * @param msg
	 *            the message to be announced as a tuple
	 * @return populated tuple ready for addition to the dataspace
	 */
	private Tuple makeTuple(final String msg)
	{
		final String time = Long.toString(System.currentTimeMillis());
		final Tuple t = new TupleImpl(new StringBoxImpl(myType), new StringArrayBoxImpl(new String[] { msg, time }));
		t.id = myId;
		t.name = myName;
		return t;
	}
}
