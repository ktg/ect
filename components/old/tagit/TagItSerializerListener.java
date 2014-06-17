/*
<COPYRIGHT>

Copyright (c) 2004-2005, University of Sussex
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of the University of Sussex
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

Created by: Ted Phelps (University of Sussex)
Contributors:
  Ted Phelps (University of Sussex)

 */
package equip.ect.components.tagit;

/*
 * Tag-It RF-ID tag reader unmarshaling listener interface
 * $RCSfile: TagItSerializerListener.java,v $
 *
 * $Revision: 1.2 $
 * $Date: 2012/04/03 12:27:27 $
 *
 * $Author: chaoticgalen $
 * Original Author: Ted Phelps
 *
 * $Log: TagItSerializerListener.java,v $
 * Revision 1.2  2012/04/03 12:27:27  chaoticgalen
 * Tidying up. Fixed xml reading/writing in Java 6. Some new components
 *
 * Revision 1.1  2005/05/03 11:54:40  cgreenhalgh
 * Import from dumas cvs
 *
 * Revision 1.2  2005/04/28 15:59:22  cmg
 * add BSD license boilerplates
 *
 * Revision 1.1  2004/08/19 12:22:38  phelps
 * Initial
 *
 */

public interface TagItSerializerListener
{
	public void tagItInventory(long[] ids);

	public void tagItVersion(String version);
}
