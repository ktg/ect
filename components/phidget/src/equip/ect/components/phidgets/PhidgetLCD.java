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
  Stefan Rennick Egglestone (University of Nottingham)

 */
package equip.ect.components.phidgets;

import java.io.Serializable;

import com.phidgets.PhidgetException;
import com.phidgets.TextLCDPhidget;
import equip.ect.Category;
import equip.ect.ECTComponent;

//import equip.ect.components.phidgets.synch.SynchedTextLCDPhidget;

/**
 * Used to control a PhidgetLCD screen. <h3>Summary</h3> This component can be used to connect to
 * and control a PhidgetTextLCD screen. <h3>Description</h3> This component can be used to connect
 * to and control a PhidgetLCD device (see http://www.phidgets.com). It can be used to specify text
 * to be displayed on either it to place scrolling (or both) rows of the PhidgetTextLCD. <h3>
 * Configuration</h3> If you only have one PhidgetTextLCD device connected to your computer, then
 * simply create a PhidgetLCD component, and set its <i>configured</i> property to <tt>true</tt>.
 * The component will then connect to the device. If you have more than one device connected, find
 * out the serial number of the board you wish to the component to connect to, specify this to the
 * component using the <i>configSerialNumber</i> property, and then set the <i>configured</i>
 * property to <tt>true</tt>. <h3>Usage</h3> Once your component has been configured, to make text
 * appear on the PhidgetTextLCD, set either the <i>lineOneText</i> or the <i>lineTwoText</i>
 * properties to the item of text you want to appear. If text is too long to fit on the display,
 * then it will be scrolled. <h3>Installation</h3> <h4>Installing the Phidget library</h4> Before
 * using any Phidget component, you should install the Phidget library. This can be downloaded from
 * http://www.phidgets.com. Make sure you get version 2.1 of the library, and not version 2.0 - this
 * component will not work reliably with version 2.0, due to bugs in this library version. <h4>
 * Installing a PhidgetTextLCD device</h4> Before using this component, you should connect your
 * PhidgetTextLCD device via USB to your computer. Note that the component should be able to cope
 * with a user repeatedly disconnecting and reconnecting an LCD screen, but this is not guaranteed.
 * 
 * @classification Hardware/Output
 * @preferred
 * @technology Phidgets
 * @defaultInputProperty lineOneText
 * @defaultOutputProperty lineOneText
 */
@ECTComponent
@Category("Hardware/Input & Output")
public class PhidgetLCD extends PhidgetBase implements Serializable
{
	class ScrollingRunnable implements Runnable
	{
		String text;
		int lcdLine;
		TextLCDPhidget phid;
		String toScroll;

		static final long INTERNAL_SCROLL_DELAY = 400;
		static final int LCD_WIDTH = 20;

		boolean shouldRun = true;
		boolean finishedRunning = false;

		ScrollingRunnable(final String text, final int lcdLine, final TextLCDPhidget phid)
		{
			this.text = text;
			this.lcdLine = lcdLine;
			this.phid = phid;

			// now create string to scroll

			String toAdd = "";

			for (int i = 0; i < LCD_WIDTH; i++)
			{
				toAdd = toAdd + " ";
			}

			toScroll = text + toAdd;
		}

		// this thread scrolls a single line of text across a phidget
		// lcd screen until interrupted

		@Override
		public void run()
		{
			try
			{
				int scrollPos = 0;

				while (shouldRun)
				{
					final String firstPart = toScroll.substring(scrollPos);
					final String lastPart = toScroll.substring(0, scrollPos);

					String toOutput = firstPart + lastPart;

					// only send characters that should appear
					// get the characters that
					// should appear on the LCD
					// if you don't do this, and try
					// to write a large string to the phidget
					// lcd,
					// , there
					// seems to be a bug in the phidgets
					// libraries that causes spurious
					// characters to be written on the lcd

					toOutput = toOutput.substring(0, LCD_WIDTH);

					try
					{
						synchronized (phid)
						{
							phid.setDisplayString(lcdLine, toOutput);
						}
					}
					catch (final PhidgetException e)
					{
					}

					scrollPos++;

					if (scrollPos >= toOutput.length())
					{
						scrollPos = 0;
					}

					Thread.sleep(INTERNAL_SCROLL_DELAY);
				}

				finishedRunning = true;
			}
			catch (final InterruptedException e)
			{
				// once this thread has been interrupted, then
				// stop execution
			}
		}

		void stop()
		{
			shouldRun = false;

			while (true)
			{
				try
				{
					Thread.sleep(100);
					if (finishedRunning == true) { return; }
				}
				catch (final InterruptedException e)
				{
				}
			}
		}
	}

	// Property

	TextLCDPhidget phid;
	private String attention = "";
	private String lineOneText = "";

	private String lineTwoText = "";
	private ScrollingRunnable lineOneRunnable = null;

	private ScrollingRunnable lineTwoRunnable = null;

	public PhidgetLCD()
	{
		super();

		try
		{
			phid = new TextLCDPhidget();
			absphid = phid;
		}
		catch (final PhidgetException e)
		{

		}
	}

	@Override
	public void detachment()
	{
	}

	@Override
	public void firstAttachment()
	{
		try
		{
			phid.setBacklight(true);
		}
		catch (final PhidgetException e)
		{
		}

		// if some text has been placed in the
		// lineOneText property, make sure
		// that it will now be displayed on
		// the phidget lcd
		refreshLineOneText();
		refreshLineTwoText();
	}

	@Override
	public synchronized String getAttention()
	{
		return attention;
	}

	public synchronized String getLineOneText()
	{
		return lineOneText;
	}

	public synchronized String getLineTwoText()
	{
		return lineTwoText;
	}

	@Override
	public synchronized void setAttention(final String newAtt)
	{
		final String oldAtt = this.attention;
		this.attention = newAtt;

		propertyChangeListeners.firePropertyChange("attention", oldAtt, newAtt);
	}

	public synchronized void setLineOneText(final String newCurrTxt)
	{
		final String oldCurrTxt = this.lineOneText;
		this.lineOneText = newCurrTxt;

		propertyChangeListeners.firePropertyChange("lineOneText", oldCurrTxt, newCurrTxt);

		// now get message to appear on screen
		refreshLineOneText();
	}

	public synchronized void setLineTwoText(final String newCurrTxt)
	{
		final String oldCurrTxt = this.lineTwoText;
		this.lineTwoText = newCurrTxt;

		propertyChangeListeners.firePropertyChange("lineTwoText", oldCurrTxt, newCurrTxt);

		refreshLineTwoText();

	}

	@Override
	public void subsequentAttachment()
	{
	}

	void refreshLineOneText()
	{
		try
		{

			// stop any threads that are currently controlling
			// line one of the display

			if (lineOneRunnable != null)
			{
				lineOneRunnable.stop();

			}

			if ((lineOneText == null) || (lineOneText.length() == 0))
			{
				synchronized (phid)
				{
					phid.setDisplayString(0, " ");
				}
			}
			else
			{
				if (lineOneText.length() <= phid.getColumnCount())
				{
					synchronized (phid)
					{
						phid.setDisplayString(0, lineOneText);
					}
				}
				else
				{
					// start thread to scroll
					lineOneRunnable = new ScrollingRunnable(lineOneText, 0, phid);
					final Thread lineOneThread = new Thread(lineOneRunnable);
					lineOneThread.start();
				}
			}
		}
		catch (final PhidgetException e)
		{

		}
	}

	void refreshLineTwoText()
	{

		try
		{
			// stop any threads that are currently controlling
			// line one of the display

			if (lineTwoRunnable != null)
			{
				lineTwoRunnable.stop();
			}

			if ((lineTwoText == null) || (lineTwoText.length() == 0))
			{
				synchronized (phid)
				{
					phid.setDisplayString(1, " ");
				}
			}
			else
			{
				if (lineTwoText.length() <= phid.getColumnCount())
				{
					synchronized (phid)
					{
						phid.setDisplayString(1, lineTwoText);
					}
				}
				else
				{
					// start thread to scroll
					lineTwoRunnable = new ScrollingRunnable(lineTwoText, 1, phid);
					final Thread lineTwoThread = new Thread(lineTwoRunnable);
					lineTwoThread.start();
				}
			}
		}
		catch (final PhidgetException e)
		{
		}
	}
}
