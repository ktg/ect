/*
<COPYRIGHT>

Copyright (c) 2002-2005, Swedish Institute of Computer Science AB
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the Swedish Institute of Computer Science AB
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

Created by: Jan Humble (Swedish Institute of Computer Science AB)
Contributors:
  Jan Humble (Swedish Institute of Computer Science AB)

 */
/*
 * Attractor, $RCSfile: Attractor.java,v $
 * 
 * $Revision: 1.2 $ $Date: 2012/04/03 12:27:26 $
 * 
 * $Author: chaoticgalen $ Original Author: Jan Humble Copyright (c) 2002, Swedish
 * Institute of Computer Science AB
 */

package equip.ect.apps.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Attractor implements Serializable
{

	protected int fieldRadius;

	protected int posX, posY;

	private List<Attractor> attachedObjects;

	private final Object parent;

	public final static int IN = 0;

	public final static int OUT = 1;

	private int type;

	public final static int ATTACHABLE = 0;

	public final static int UNATTACHABLE = 1;

	private int state;

	private boolean allowMultipleConnections;

	public Attractor(final Object parent, final int posX, final int posY, final int fieldRadius, final int type,
			final boolean allowMultipleConnections)
	{
		this.parent = parent;
		this.type = type;
		this.state = ATTACHABLE;
		this.allowMultipleConnections = allowMultipleConnections;
		setRadius(fieldRadius);
		setAbsolutePosition(posX, posY);
	}

	/**
	 * The attaches an attractor to another. You can specify if the foreign object should also
	 * attach this instance to itself.
	 */
	public void attach(final Attractor obj, final boolean attachForeign)
	{
		if (attachedObjects == null)
		{
			attachedObjects = new ArrayList<Attractor>();
		}
		else if (isAttached(obj))
		{
			System.out.println("Object already attached!");
			return;
		}
		attachedObjects.add(obj);
		if (attachForeign)
		{
			obj.attach(this, false);
		}
		// System.out.println("ATTACH");
	}

	public final boolean canAttach(final Attractor attractor)
	{
		return (inField(attractor) >= 0 && checkValidity(attractor));
	}

	public boolean checkValidity(final Attractor foreignAtt)
	{
		return ((type != foreignAtt.type) && (!foreignAtt.allowMultipleConnections && foreignAtt.isAttached()));
	}

	/**
	 * The detaches an attractor from another. You can specify if the foreign object should also
	 * detach this instance from itself.
	 */
	public void detach(final Attractor obj, final boolean detachForeign)
	{
		if (attachedObjects == null) { return; }

		attachedObjects.remove(obj);

		if (detachForeign)
		{
			obj.detach(this, false);
		}

		if (attachedObjects.size() < 1)
		{
			attachedObjects = null;
		}
		// System.out.println("DETACH");
	}

	public void detachAll(final boolean detachForeign)
	{

		if (attachedObjects == null) { return; }

		if (detachForeign)
		{
			for(final Attractor foreign: attachedObjects)
			{
				foreign.detach(this, false);
			}
		}

		attachedObjects = null;
	}

	public List<Attractor> getAttached()
	{
		return attachedObjects;
	}

	public Object getParent()
	{
		return this.parent;
	}

	public int[] getPosition()
	{
		return new int[] { posX, posY };
	}

	public int getPosX()
	{
		return posX;
	}

	public int getPosY()
	{
		return posY;
	}

	public int getState()
	{
		return this.state;
	}

	public int getType()
	{
		return type;
	}

	/**
	 * Returns the distance to the given attractor if with its field. If not with field returns -1.
	 */
	public final double inField(final Attractor attractor)
	{
		final double distance = inField(attractor.posX, attractor.posY);
		if (distance >= 0)
		{
			return distance;
		}
		else
		{
			return -1.0;
		}
	}

	public final boolean isAttached()
	{
		return (attachedObjects != null);
	}

	public boolean isAttached(final Attractor foreignAttractor)
	{
		return (attachedObjects != null && attachedObjects.contains(foreignAttractor));
	}

	public void paint(final Graphics g)
	{
		final Graphics2D g2 = (Graphics2D) g;

		if (type == IN)
		{
			g2.setColor(Color.red);
		}
		else
		{
			g2.setColor(Color.blue);
		}
		g2.setStroke(new BasicStroke(2));
		g2.drawOval(posX - fieldRadius, posY - fieldRadius, fieldRadius * 2, fieldRadius * 2);

		if (state == UNATTACHABLE)
		{
			paintNoConnection(g2);
		}
	}

	public void paintNoConnection(final Graphics2D g2)
	{
		g2.setStroke(new BasicStroke(10));
		g2.setColor(Color.red);
		g2.drawLine(posX - 30, posY - 30, posX + 30, posY + 30);
		g2.drawLine(posX + 30, posY - 30, posX - 30, posY + 30);
	}

	public void setAbsolutePosition(final int x, final int y)
	{
		this.posX = x;
		this.posY = y;
	}

	public void setRadius(final int radius)
	{
		this.fieldRadius = radius;
	}

	public void setState(final int state)
	{
		this.state = state;
	}

	public void translatePosition(final int dx, final int dy)
	{
		this.posX += dx;
		this.posY += dy;
	}

	protected double inField(final int x, final int y)
	{
		final double distance = (posX - x) * (posX - x) + (posY - y) * (posY - y);
		if (distance <= fieldRadius * fieldRadius)
		{
			return distance;
		}
		else
		{
			return -1.0;
		}
	}

}
