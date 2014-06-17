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
package equip.ect.components.visionframework.barcode1d;

import java.util.Vector;

/**
 * attempt to read 1D barcodes from image stripes. fairly knackered at the moment (though slightly
 * improved), and just here so i can come back to it at some point. current limitations: - assumes
 * bar code highly linear over full length. - assumes starts with narrow bar, narrow space, narrow
 * bar (untrue for CODE 128). - does not convert to encoded data, just returns bar pattern. Note:
 * looking at real bar codes with a camera gives lousy separation even in the ideal case. Perhaps a
 * level model rather than diffs would be better. I was thinking about reimplementing to try to
 * assign all widths to 1,2,3 or 4, but am not seeing good basic separation at present. May be ok
 * with own (large) barcodes?!
 */
public class BarCode1DReader
{
	/**
	 * diff pulse info class
	 */
	public class Diff
	{
		/**
		 * pos
		 */
		public float pos;
		/**
		 * position in estimated bit/bar space
		 */
		public float barPos;
		/**
		 * delta value
		 */
		public float delta;
		/**
		 * 0 to 1?
		 */
		public boolean rising;

		/**
		 * cons
		 */
		public Diff()
		{
		}
	}

	/**
	 * info class
	 */
	public class Info
	{
		/**
		 * matched?
		 */
		public boolean matched;
		/**
		 * confidence measure
		 */
		public float confidence;
		/**
		 * error detected?
		 */
		public boolean error;
		/**
		 * position cf pos[]
		 */
		public float startPos, endPos;
		/**
		 * bit sequence
		 */
		public boolean[] data;
		/**
		 * parsed form
		 */
		public String text;
		/**
		 * bit width cf pos[] at startPos
		 */
		public float bitWidth;
		/**
		 * degree of 0th interval from 1st interval skew, e.g. +0.1 => 0th interval is 110% width cf
		 * 1st 90% width
		 */
		public float skew;
		/**
		 * pos scaling factor, units per unit; 1=> pos values treated consistently; 0.9 => width at
		 * pos-startPos considered to be 1+diverge*(pos-startPos) and dpos' =
		 * (1+diverge*(pos-startPos))*dpos and therefore pos'-startPos =
		 * pos-startPos-diverge*startPos
		 * *(pos-startPos)+0.5*diverge*pos*pos-0.5*diverge*startPos*startPos =
		 * pos-startPos+0.5*diverge*pos*pos+0.5*diverge*startPos*startPos-diverge*startPos*pos; =
		 * (pos-startPos)+0.5*diverge*(pos-startPos)*(pos-startPos)
		 */
		public float diverge;
		/**
		 * width of convolution filter half, approx. min of width of narrowest 1 or 0 bar,
		 * min(bitWidth*(1-skew),bitWidth*(1+skew))
		 */
		public float filterWidth;
		/**
		 * max adapt bit width
		 */
		public static final float MAX_ADAPT_STEP = 0.05f;
		/**
		 * overlap penalty - bad
		 */
		public static final float OVERLAP_PENALTY = 2;
		public static final float BIG_DIFF_MULTIPLIER = 10;

		/**
		 * cons
		 */
		public Info()
		{
		}

		/**
		 * score, low is good
		 */
		protected float mapAndScore(final Diff ds[], final int fromI, final int toI, float startPos,
				final float startBit, final float bitWidth)
		{
			// adapt start pos
			float bestOffset = 0;
			float bestScore = 10;
			for (float offset = (-0.25f); offset < 0.25f; offset = offset + 0.05f)
			{
				final float score = mapAndScore2(ds, fromI, toI, startPos + offset * bitWidth, startBit, bitWidth);
				if (score < bestScore)
				{
					bestOffset = offset;
					bestScore = score;
				}
			}
			startPos = startPos + bestOffset * bitWidth;
			// System.out.println("Start time adjusted by "+bestOffset+" to "+startPos+" (score "+bestScore+")");
			return mapAndScore2(ds, fromI, toI, startPos, startBit, bitWidth);
		}

		protected float mapAndScore2(final Diff ds[], final int fromI, final int toI, final float startPos,
				final float startBit, final float bitWidth)
		{
			float s = 0;
			for (int i = fromI; i < toI; i++)
			{
				ds[i].barPos = mapToBarPos(ds[i], startPos, startBit, bitWidth, (ds[i].rising != ds[0].rising) ? skew
						: 0);

				int barPos = 0;
				barPos = Math.round(ds[i].barPos);
				if (i > 1 && barPos == Math.round(ds[i - 1].barPos))
				{
					s = s + OVERLAP_PENALTY * (toI - fromI);
				}
				float multiplier = 1;
				if (ds[i].barPos - barPos > 0.25 || ds[i].barPos - barPos < -0.25)
				{
					multiplier = BIG_DIFF_MULTIPLIER;
				}
				s = s + multiplier * (ds[i].barPos - barPos) * (ds[i].barPos - barPos);
			}
			return s / (toI - fromI);
		}

		/**
		 * map to bar pos
		 */
		protected float mapToBarPos(final Diff d, final float startPos, final float startBit, final float bitWidth,
				final float skew)
		{
			float dpos = d.pos - startPos;
			// skew adjust
			dpos = dpos - skew * bitWidth;
			final float barPos = dpos / bitWidth + startBit;
			return barPos;
		}

		/**
		 * got an initial estimate - now try to fit whole bar code. initially without diverge.
		 * 
		 * @param values
		 *            intensity values on stripe that may contain a 1D barcode; assumed scale 0-1
		 * @param pos
		 *            linear position values on stripe, in pixel equivalents, increasing
		 * @param noiseValue
		 *            estimated changes in values due to noise alone
		 */
		protected void tryFit(final float values[], final float pos[], final int startI, final float noiseValue)
		{
			matched = false;
			final float initBitWidth = bitWidth;
			final float maxAdapt = 1.0f / (bitWidth + 1);

			// hunt for all diffs
			final Vector diffs = new Vector();
			int diffI = startI;

			// look along for diff pulses +/-
			boolean rising = true, falling = true;
			int zeroCount = 0;
			final int zeroTarget = (int) (bitWidth * TRAILING_BUFFER_BITS);
			final int len = values.length;
			int lastDiffI = 0;
			Diff lastDiff = null;
			float lastPosSum = 0;
			float lastPosN = 0;

			while (diffI + 1 < len && zeroCount < zeroTarget)
			{
				final float delta = filter(values, pos, diffI, filterWidth);
				// System.out.println("delta "+diffI+" = "+delta+" (cf noise "+noiseValue+") at "+pos[diffI]);
				// float ave = 0.5f*(values[startI]+values[startI+1]);
				if ((delta > KNOISE * noiseValue && rising) || (delta < -KNOISE * noiseValue && falling))
				{
					final Diff d = new Diff();
					d.pos = pos[diffI];
					lastPosSum = delta * pos[diffI];
					lastPosN = delta;
					d.delta = delta;
					d.rising = (d.delta > 0);
					// next change direction
					rising = !d.rising;
					falling = !rising;
					diffs.addElement(d);
					lastDiffI = diffI;
					lastDiff = d;
				}
				else if (lastDiff != null && lastDiffI + 1 == diffI
						&& ((delta > KNOISE * noiseValue && !rising) || (delta < -KNOISE * noiseValue && !falling)))
				{
					// this is a continuation of the last diff
					lastPosSum = lastPosSum + delta * pos[diffI];
					lastPosN = lastPosN + delta;
					// pos = centre of grav.
					lastDiff.pos = lastPosSum / lastPosN;
					lastDiffI = diffI;
				}
				if (diffs.size() > 0 && delta > -KNOISE * noiseValue && delta < KNOISE * noiseValue)
				{
					zeroCount++;
				}
				else
				{
					zeroCount = 0;
				}
				diffI++;
			}

			if (zeroCount < zeroTarget)
			{
				System.err.println("Did not find trailing blank");
				return;
			}
			if (diffs.size() < MIN_DELTAS)
			{
				System.err.println("Found only " + diffs.size() + " apparent changes");
				return;
			}

			final Diff ds[] = (Diff[]) diffs.toArray(new Diff[diffs.size()]);
			if (debug)
			{
				System.out.println("Found " + ds.length + " initial diffs");

				for (int ii = 1; ii < ds.length; ii++)
				{
					System.out.println("diff " + ii + " at " + ((ds[ii - 1].pos + ds[ii].pos) / 2) + " width "
							+ ((ds[ii].pos - ds[ii - 1].pos) * (ds[ii].rising ? 1 : -1)));
				}
			}

			// try fitting positions
			int diFitted = /* 3 */ds.length - 1;
			int diKeep = 0;
			float startBit = 0;
			float startPos = ds[0].pos;
			ds[0].barPos = 0;
			while (!matched && !error)
			{
				matched = true;

				for (int di = diFitted; di < ds.length; di++)
				{
					// System.out.println("  diff "+di+", "+(ds[di].rising ? "rising" :
					// "falling")+", at "+ds[di].barPos);

					// current values places us at...
					ds[di].barPos = mapToBarPos(ds[di], startPos, startBit, bitWidth,
												(ds[di].rising != ds[0].rising) ? skew : 0);
					int barPos = 0;
					// estimate target
					barPos = Math.round(ds[di].barPos);
					float bestBarPos = 0;
					float bestScore = 0;
					for (float p = barPos; p >= 1 && p > barPos / 2; p = p - /* 0.1f */1)
					{
						final float bw = (ds[di].pos - startPos) / (p - startBit);
						if (p != barPos && (bw > initBitWidth / (1 - maxAdapt) /*
																				 * || bw >
																				 * bitWidth/(
																				 * 1-MAX_ADAPT_STEP)
																				 */))
						{
							break;
						}
						final float score = mapAndScore(ds, diKeep, di + 1, startPos, startBit, bw);
						if (bestBarPos == 0 || score < bestScore)
						{
							bestScore = score;
							bestBarPos = p;
						}
						else
						{
							// getting worse
							;// break;
						}
					}
					for (float p = barPos + /* 0.1f */1; p < barPos * 2; p = p + /* 0.1f */1)
					{
						final float bw = (ds[di].pos - startPos) / (p - startBit);
						if (p != barPos && (bw < initBitWidth * (1 - maxAdapt) /*
																				 * || bw <
																				 * bitWidth*(
																				 * 1-MAX_ADAPT_STEP)
																				 */))
						{
							break;
						}
						final float score = mapAndScore(ds, diKeep, di + 1, startPos, startBit, bw);
						if (bestBarPos == 0 || score < bestScore)
						{
							bestScore = score;
							bestBarPos = p;
						}
						else
						{
							// getting worse
							;// break;
						}
					}
					bitWidth = (ds[di].pos - (startPos)) / (bestBarPos - startBit);
					mapAndScore(ds, diKeep, di + 1, startPos, startBit, bitWidth);
					if (debug)
					{
						System.out.println("Iter " + di + " adjusts bit width to " + bitWidth + ", score=" + bestScore);
					}

					if (bestScore > 1)
					{
						System.err.println("Score is really bad - give up?!");
						error = true;
						break;
					}

					// slide window
					if ((ds[di].rising == ds[0].rising) && (di > diKeep + KEEP_WINDOW)
							&& (di > diFitted + KEEP_WINDOW / 2))
					{
						diKeep = di - KEEP_WINDOW;
						diFitted = di;
						startBit = ds[diKeep].barPos;
						startPos = ds[diKeep].pos;
						final float bp = mapToBarPos(	ds[di], startPos, startBit, bitWidth,
														(ds[di].rising != ds[0].rising) ? skew : 0);
						if (debug)
						{
							System.out.println("Adjust keep window to " + diKeep + " and startPos " + startPos
									+ ", bit " + startBit);
							System.out.println("- changes " + di + " bar pos from " + ds[di].barPos + " to " + bp);
						}
						matched = false;
						break;
					} // else
						// System.out.println("di "+di+", diKeep="+diKeep+", KEEP_WINDOW="+KEEP_WINDOW+", diFitted="+diFitted+", rising="+
						// ds[di].rising+", rising[0]="+ds[0].rising+", ==?"+(ds[di].rising ==
						// ds[0].rising)+", di?"+(di > diKeep+KEEP_WINDOW)+", diFitted?"+);

				}// for di
			}// while(!matched)
				// bits
			if (error)
			{
				matched = false;
				return;
			}
			final Vector bits = new Vector();
			for (int di = 1; di < ds.length; di++)
			{
				int barPos = 0;
				barPos = Math.round(ds[di].barPos);
				if (barPos <= bits.size())
				{
					System.err.println("ERROR: diff " + di + " clashes with last diff at " + barPos);
					matched = false;
					return;
				}
				for (int bi = bits.size(); bi < barPos; bi++)
				{
					bits.addElement(new Boolean(ds[di].rising));
				}
			}
			data = new boolean[bits.size()];
			if (debug)
			{
				System.out.print("Data: ");
			}
			for (int bi = 0; bi < bits.size(); bi++)
			{
				data[bi] = ((Boolean) bits.elementAt(bi)).booleanValue();
				if (debug)
				{
					System.out.print(data[bi] ? "1" : "0");
				}
			}
			if (debug)
			{
				System.out.println();
				// parse??
				// ....
			}
		}// tryFit
	}// class Info

	protected boolean debug = false;
	/**
	 * filter fraction - <1 to be conservative about initial width guess
	 */
	public static final float FILTER_FRACTION = 0.5f;
	/**
	 * min no-delta space before code
	 */
	public static final int MIN_LEAD_SPACE = 3;
	/**
	 * constant factor
	 */
	public static final float KNOISE = 1;
	/**
	 * min skew
	 */
	public static final float MIN_SKEW = -0.8f;
	/**
	 * max skew
	 */
	public static final float MAX_SKEW = 0.8f;
	/**
	 * non-linear window
	 */
	public static final int KEEP_WINDOW = 100;
	/**
	 * bits in trailing white space
	 */
	public static final int TRAILING_BUFFER_BITS = 5;
	/**
	 * min deltas
	 */
	public static final int MIN_DELTAS = 8;

	/**
	 * test main. e.g. java BarCode1DReader 000000909090900000000 e.g. java BarCode1DReader
	 * 00000000909000090000000000909090900000000 e.g. java BarCode1DReader 0000009009009009000000000
	 * e.g. java BarCode1DReader 000000990099009900990000000000000
	 */
	public static void main(final String[] args)
	{
		try
		{
			final String in = args[0];
			final float values[] = new float[in.length()];
			final float pos[] = new float[in.length()];
			for (int i = 0; i < in.length(); i++)
			{
				values[i] = 0.1f * ((in.charAt(i) - '0'));
				pos[i] = i;
			}
			final BarCode1DReader reader = new BarCode1DReader();
			final Info info = reader.read(values, pos, 0, 0.15f);
		}
		catch (final Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.err);
		}
	}

	/**
	 * cons
	 */
	public BarCode1DReader()
	{
	}

	/**
	 * read
	 * 
	 * @param values
	 *            intensity values on stripe that may contain a 1D barcode; assumed scale 0-1
	 * @param pos
	 *            linear position values on stripe, in pixel equivalents, increasing
	 * @param startI
	 *            starting index in array(s)
	 * @param noiseValue
	 *            estimated changes in values due to noise alone
	 * @return BarCode1DReader.Info
	 */
	public Info read(final float values[], final float pos[], int startI, final float noiseValue)
	{
		final Info info = new Info();
		// init
		info.matched = false;
		info.error = false;
		if (values.length == 0)
		{
			System.err.println("Cannot read zero-length bar code");
			return info;
		}
		info.startPos = pos[startI];
		final int len = values.length;
		final float endPos = pos[len - 1];
		// repeatedly look...
		while (startI < len)
		{
			// start with assumption of width 1, no skew, no divergence
			info.bitWidth = 1;
			info.skew = 0;
			info.diverge = 0;
			info.filterWidth = 1;

			final Vector diffs = new Vector();
			int ndiffs = 0;
			int diffI = startI;
			int firstDiffI = startI;

			// look along for three "significant" diff pulses +/-
			boolean rising = true, falling = true;
			while (diffI + 1 < len && ndiffs < 3)
			{
				final float delta = values[diffI + 1] - values[diffI];
				// System.out.println("Delta "+diffI+" = "+delta+", cf noise "+noiseValue*KNOISE);
				// float ave = 0.5f*(values[startI]+values[startI+1]);
				if ((delta > KNOISE * noiseValue && rising) || (delta < -KNOISE * noiseValue && falling))
				{
					final Diff d = new Diff();
					d.pos = pos[diffI];
					d.delta = delta;
					d.rising = (d.delta > 0);
					// next change direction
					rising = !d.rising;
					falling = !rising;
					diffs.addElement(d);
					if (ndiffs == 0)
					{
						firstDiffI = diffI;
					}
					ndiffs++;
				}
				diffI++;
			}

			// potentially valid? including blank lead space?
			if (ndiffs >= 3 && firstDiffI > startI + MIN_LEAD_SPACE)
			{
				final Diff ds[] = (Diff[]) diffs.toArray(new Diff[diffs.size()]);
				boolean invert = false;
				if (ds[0].delta < 0)
				{
					invert = true;
				}
				// estimate real bit width & skew from positions
				info.bitWidth = 0.5f * (ds[2].pos - ds[0].pos);

				if (firstDiffI - startI < info.bitWidth * TRAILING_BUFFER_BITS)
				{
					if (debug)
					{
						System.err.println("Sorry - leading space too small (" + (firstDiffI - startI) + "/"
								+ (info.bitWidth * TRAILING_BUFFER_BITS) + " at " + firstDiffI + ")");
					}
				}
				else
				{
					startI = (startI + firstDiffI) / 2;

					info.skew = 0; /*
									 * don't trust it without adaptation
									 * (ds[1].pos-ds[0].pos)/info.bitWidth-1;
									 */
					info.diverge = 0;
					info.startPos = ds[0].pos;
					System.out.println("Estimate 0, starting at " + info.startPos + ": bitWidth=" + info.bitWidth
							+ ", skew=" + info.skew + ", invert=" + invert);
					if (info.skew < MIN_SKEW || info.skew > MAX_SKEW)
					{
						if (debug)
						{
							System.err.println("Sorry - skew too large (" + info.skew + ")");
						}
					}
					else
					{
						info.matched = info.error = false;
						if (info.skew < 0)
						{
							info.filterWidth = FILTER_FRACTION * info.bitWidth * (1 + info.skew);
						}
						else
						{
							info.filterWidth = FILTER_FRACTION * info.bitWidth * (1 - info.skew);
						}
						info.tryFit(values, pos, startI, noiseValue);
						if (info.matched) { return info; }
					}
				}
			}
			// move along and try again
			if (ndiffs > 0)
			{
				startI = firstDiffI;
			}
			startI++;
			// not for now
			// break;
		}
		return info;
	}

	/**
	 * apply rising edge filter.
	 * 
	 * @param i
	 *            is index just this side of the filter
	 */
	protected float filter(final float values[], final float pos[], final int i, final float width)
	{
		// ignore width (averaged anyway)
		// width = 1;
		final float p = (pos[i + 1] + pos[i]) * 0.5f;
		// minimal
		float add = values[i + 1];
		int nadd = 1;
		// grow pos?
		for (int j = 2; i + j < values.length && pos[i + j] - width < p; j++)
		{
			add = add + values[i + j];
			nadd++;
		}
		float sub = values[i];
		int nsub = 1;
		// grow neg?
		for (int j = 1; i - j >= 0 && pos[i - j] + width > p; j++)
		{
			sub = sub + values[i - j];
			nsub++;
		}
		return (add / nadd) - (sub / nsub);
	}
}
