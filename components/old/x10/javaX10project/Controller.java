/*
 * Copyright 2002-2003, Wade Wassenberg  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package equip.ect.components.x10.javaX10project;

import java.io.Serializable;

/**
 * Controller is implemented by any class that can act as an entry point for controlling x10
 * devices. A Controller must be able to distribute added Commands to ALL registered x10 hardware
 * and software modules. A Controller must also handle the addition and removal of UnitListeners.
 * 
 * 
 * @author Wade Wassenberg
 * 
 * @version 1.0
 * @see x10.Command
 * @see x10.UnitListener
 */

public interface Controller extends Serializable
{
	public void addCommand(Command command);

	public void addCommandPair(CommandPair pair);
}