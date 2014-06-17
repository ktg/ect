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

Created by: Chris Allsop (University of Nottingham)
Contributors:
  Chris Allsop (University of Nottingham)

 */

package equip.ect.components.x10;

import java.io.Serializable;

public interface X10_Constants extends Serializable
{

	/** String representing the CM11U Controller (UK 110V) */
	public static final String CM11U_CONTROLLER = "CM11U";

	/** String representing the CM11U Controller (UK 240V) */
	public static final String CM12U_CONTROLLER = "CM12U";

	/** String representing the CM11A Controller (American) */
	public static final String CM11A_CONTROLLER = "CM11A";

	/** String representing the CM17A Firecracker Controller (American) */
	// public static final String CM17A_CONTROLLER = "CM17A";

	/** The default name for all X10 components and their subcomponents (if any) */
	public static final String DEFAULT_NAME = "unnamed";

	public static final byte APPLIANCE_MODULE_TYPE = 0;

	public static final byte LAMP_MODULE_TYPE = 1;

	public static final char MIN_HOUSE_CODE = 'A';

	public static final char MAX_HOUSE_CODE = 'P';

	public static final int MIN_UNIT_CODE = 1;

	public static final int MAX_UNIT_CODE = 16;

	public static final String ALL_UNITS_OFF_CMD = "All_Units_Off";

	public static final String ALL_LIGHTS_ON_CMD = "All_Lights_On";

	public static final String ALL_LIGHTS_OFF_CMD = "All_Lights_Off";

	public static final String ON_CMD = "On";

	public static final String OFF_CMD = "Off";

	public static final String DIM_CMD = "Dim";

	public static final String BRIGHT_CMD = "Bright";

	public static final double MIN_LEVEL = 0.0f;

	public static final double MAX_LEVEL = 1.0f;

	public static final int MIN_PERCENTAGE = 0;

	public static final int MAX_PERCENTAGE = 100;

	public static final String DUMMY_ADDRESS = "P16";

	public static final int NUM_BRIGHTNESS_LEVELS = 16;

}
