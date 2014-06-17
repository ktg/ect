/*
<COPYRIGHT>

Copyright (c) 2004-2005, Lancaster University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Lancaster University
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

Created by: Craig Morrall (Lancaster University)
Contributors:
  Craig Morrall (Lancaster University)

 */
package equip.ect.components.analogue_clock;

/**
 * analoue clock component
 * displays a clock in analogue format
 * @author Craig Morrall
 * @date 03/06/2004
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.JFrame;

// Implementation of an analogue clock display, taken from:
// http://www.lut.fi/~mraatika/AnalogClkSource.html.
// edited to become a bean and to do some other features

public class AnalogueClock extends JFrame implements Serializable, PropertyChangeListener
{
	/**
	 * main method
	 */
	public static void main(final String args[])
	{
		final AnalogueClock clock = new AnalogueClock();
	}// end of main method

	// Property Change
	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// properties
	private int m_hour;
	private int m_minute;

	private int m_second;

	private int m_lastxs, m_lastys, m_lastxm, m_lastym, m_lastxh, m_lastyh;

	/**
	 * constructor method
	 */
	public AnalogueClock()
	{
		super();
		this.setTitle("Analogue Clock");
		this.setSize(200, 200);
		m_lastxs = 0;
		m_lastys = 0;
		m_lastxm = 0;
		m_lastym = 0;
		m_lastxh = 0;
		m_lastxh = 0;
		addPropertyChangeListener(this); // add us to property listener
		setVisible(true);
	}// end of constructor method

	// set methods

	@Override
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public int getHours()
	{
		return m_hour;
	}// end of method getHours

	public int getMinutes()
	{
		return m_minute;
	}// end of method getMinutes

	// get methods

	public int getSeconds()
	{
		return m_second;
	}// end of method getSeconds

	/**
	 * method to draw the clock
	 */
	@Override
	public void paint(final Graphics g)
	{
		int xh, yh, xm, ym, xs, ys, xcenter, ycenter;

		xcenter = 100;
		ycenter = 107;

		// a = s * pi / 2 - pi / 2 (to switch 0,0 from 3:00 to 12:00)
		// x = r(cos a) + xcenter, y = r(sin a) + ycenter
		xs = (int) (Math.cos(m_second * 3.14f / 30 - 3.14f / 2) * 45 + xcenter);
		ys = (int) (Math.sin(m_second * 3.14f / 30 - 3.14f / 2) * 45 + ycenter);
		xm = (int) (Math.cos(m_minute * 3.14f / 30 - 3.14f / 2) * 40 + xcenter);
		ym = (int) (Math.sin(m_minute * 3.14f / 30 - 3.14f / 2) * 40 + ycenter);
		xh = (int) (Math.cos((m_hour * 30 + m_minute / 2) * 3.14f / 180 - 3.14f / 2) * 30 + xcenter);
		yh = (int) (Math.sin((m_hour * 30 + m_minute / 2) * 3.14f / 180 - 3.14f / 2) * 30 + ycenter);

		// Draw the circle and numbers
		g.setFont(new Font("Helvetica", Font.PLAIN, 14));
		g.setColor(Color.blue);
		circle(xcenter, ycenter, 50, g);
		g.setColor(Color.darkGray);
		g.drawString("9", xcenter - 45, ycenter + 3);
		g.drawString("3", xcenter + 40, ycenter + 3);
		g.drawString("12", xcenter - 5, ycenter - 37);
		g.drawString("6", xcenter - 3, ycenter + 45);

		// Erase if necessary, and redraw
		g.setColor(getBackground());
		if (xs != m_lastxs || ys != m_lastys)
		{
			g.drawLine(xcenter, ycenter, m_lastxs, m_lastys);
		}
		if (xm != m_lastxm || ym != m_lastym)
		{
			g.drawLine(xcenter, ycenter - 1, m_lastxm, m_lastym);
			g.drawLine(xcenter - 1, ycenter, m_lastxm, m_lastym);
		}
		if (xh != m_lastxh || yh != m_lastyh)
		{
			g.drawLine(xcenter, ycenter - 1, m_lastxh, m_lastyh);
			g.drawLine(xcenter - 1, ycenter, m_lastxh, m_lastyh);
		}
		g.setColor(Color.darkGray);
		g.drawLine(xcenter, ycenter, xs, ys);
		g.setColor(Color.blue);
		g.drawLine(xcenter, ycenter - 1, xm, ym);
		g.drawLine(xcenter - 1, ycenter, xm, ym);
		g.drawLine(xcenter, ycenter - 1, xh, yh);
		g.drawLine(xcenter - 1, ycenter, xh, yh);
		m_lastxs = xs;
		m_lastys = ys;
		m_lastxm = xm;
		m_lastym = ym;
		m_lastxh = xh;
		m_lastyh = yh;
	}// end of method paint

	// signal a property change event
	@Override
	public void propertyChange(final PropertyChangeEvent e)
	{
	}

	// Property Change Listeners
	@Override
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public void setHours(final int hour)
	{
		if (hour != m_hour)
		{
			propertyChangeListeners.firePropertyChange("hours", m_hour, hour);
			m_hour = hour;
			repaint();
		}// end of if statement
	}// end of method setHours

	public void setMinutes(final int minute)
	{
		if (minute != m_minute)
		{
			propertyChangeListeners.firePropertyChange("minutes", m_minute, minute);
			m_minute = minute;
			repaint();
		}// end of if statement
	}// end of method setMinutes

	public void setSeconds(final int second)
	{
		if (second != m_second)
		{
			propertyChangeListeners.firePropertyChange("seconds", m_second, second);
			m_second = second;
			repaint();
		}// end of if statement
	}// end of method setSeconds

	/**
	 * stop/kill
	 */
	public void stop()
	{
		this.dispose();
	}

	/**
	 * method to draw the circle
	 */
	protected void circle(final int x0, final int y0, final int r, final Graphics g)
	{
		int x, y;
		float d;
		x = 0;
		y = r;
		d = 5 / 4 - r;
		plotpoints(x0, y0, x, y, g);
		while (y > x)
		{
			if (d < 0)
			{
				d = d + 2 * x + 3;
				x++;
			}
			else
			{
				d = d + 2 * (x - y) + 5;
				x++;
				y--;
			}
			plotpoints(x0, y0, x, y, g);
		}
	}// end of circle method

	// Plotpoints allows calculation to only cover 45 degrees of the circle,
	// and then mirror.
	protected void plotpoints(final int x0, final int y0, final int x, final int y, final Graphics g)
	{
		g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
		g.drawLine(x0 + y, y0 + x, x0 + y, y0 + x);
		g.drawLine(x0 + y, y0 - x, x0 + y, y0 - x);
		g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
		g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
		g.drawLine(x0 - y, y0 - x, x0 - y, y0 - x);
		g.drawLine(x0 - y, y0 + x, x0 - y, y0 + x);
		g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
	}// end of method plotPoitns
}// end of class AnalogueClock
