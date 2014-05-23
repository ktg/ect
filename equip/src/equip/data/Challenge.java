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

*/
package equip.data;
import java.security.*;
/** challenge-response utility class
 */
public class Challenge {
	/** secure random for challenges
	 */
	protected static SecureRandom random;
	/** challenge size
	 */
	protected static final int CHALLENGE_SIZE = 10;
	/** make a new challenge
	 */
	public synchronized static String makeChallenge() 
	{
		if (random==null) 
		{
			random = new SecureRandom();
		}
		byte challenge[] = new byte[CHALLENGE_SIZE];
		random.nextBytes(challenge);

		return bytesToString(challenge);
	}
	/** secret size
	 */
	protected static final int SECRET_SIZE = 10;
	/** make a new secret
	 */
	public synchronized static String makeSecret() 
	{
		if (random==null) 
		{
			random = new SecureRandom();
		}
		byte challenge[] = new byte[SECRET_SIZE];
		random.nextBytes(challenge);

		return bytesToString(challenge);
	}
	/** digest algorithm
	 */
	protected static MessageDigest digest;
	/** make a response to a challenge
	 */
	public synchronized static String makeResponse(String secret, String challenge) 
	{
		if (digest==null) 
		{
			try 
			{
				digest = MessageDigest.getInstance("SHA");
			} 
			catch (NoSuchAlgorithmException e) 
			{
				System.err.println("ERROR: Cannot make response: "+e);
				e.printStackTrace(System.err);
				return null;
			}
		}
		// feed secret & challenge through one-way secure hash
		digest.reset();
		digest.update(secret.getBytes());
		digest.update(challenge.getBytes());
		byte res [] = digest.digest();
		return bytesToString(res);
	}
	/** check a response
	 */
	public static boolean acceptResponse(String secret, String challenge, String response) 
	{
		return response.equals(makeResponse(secret, challenge));
	}
	/** bytes to string (hex for now?)
	 */
	public static String bytesToString(byte b[]) 
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<b.length; i++) 
		{
			int hi = (((int)b[i]) >> 4) & 0x0f;
			int lo = ((int)b[i]) & 0x0f;
			buf.append(toHex(hi));
			buf.append(toHex(lo));
		}
		return buf.toString();
	}
	/** nibble to char
	 */
	public static char toHex(int nibble) 
	{
		if (nibble<10)
			return (char)('0'+nibble);
		return (char)('A'+(nibble-10));
	}
	/** char to nibble
	 */
	public static int fromHex(char c) 
	{
		if (c>='0' && c<='9')
			return c-'0';
		if (c>='A' && c<='H')
			return 10+(c-'A');
		if (c>='a' && c<='h')
			return 10+(c-'a');
		System.err.println("Invalid hex: "+c);
		return 0;
	}
}
