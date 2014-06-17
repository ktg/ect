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
package equip.ect.components.visiondemo;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Stack;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class EquipVisionDemo
{

	public static void main(final String[] args) throws Exception
	{
		// Does a quick test

		final EquipVisionDemo evd = new EquipVisionDemo();

		evd.trainValues("http://www.cs.nott.ac.uk/~smx/skin.jpg", "http://www.cs.nott.ac.uk/~smx/background.jpg");

		evd.setMedianRadius(2);
		evd.setMinBlobSize(100);

		evd.imageUpdate("http://www.cs.nott.ac.uk/~smx/frame1.jpg");
		evd.imageUpdate("http://www.cs.nott.ac.uk/~smx/frame2.jpg");

		for (int b = 0; b < evd.getNumBlobs(); b++)
		{
			System.out.println("Blob at (" + (int) evd.getBlobX(b) + ", " + (int) evd.getBlobY(b) + ") with "
					+ evd.getBlobSize(b) + " pixels");
		}

	}

	// Frame memory
	double lastU[][];

	double lastV[][];
	// Located blobs
	int numBlobs;
	int blobSize[];
	double blobX[];
	double blobY[];

	int blobLabel[];
	// Nasty parameters
	double Uavg;
	double Vavg;
	double Urange;
	double Vrange;
	double motionThreshold;
	int medianRadius;

	int minBlobSize;

	EquipVisionDemo()
	{
		lastU = null;
		lastV = null;
		numBlobs = 0;
		blobSize = new int[numBlobs];
		blobX = new double[numBlobs];
		blobY = new double[numBlobs];
		blobLabel = new int[numBlobs];
		// the constants below are
		// -14 - average U value of skin
		// 7 - range of U values
		// 15 - average V value of skin
		// 4 - range of V values
		// Currently very rough guesses
		Uavg = -14;
		Vavg = 15;
		Urange = 3 * 3;
		Vrange = 3 * 3;
		motionThreshold = 3 * 3;
		medianRadius = 3;
		minBlobSize = 25;
	}

	int getBlobSize(final int index)
	{
		return blobSize[index];
	}

	double getBlobX(final int index)
	{
		return blobX[index];
	}

	double getBlobY(final int index)
	{
		return blobY[index];
	}

	int getNumBlobs()
	{
		return numBlobs;
	}

	void imageUpdate(final String urlName) throws Exception
	{
		// Read the image from the pathname
		final URL theURL = new URL(urlName);
		final InputStream istream = theURL.openStream();
		final JPEGImageDecoder jpegDec = JPEGCodec.createJPEGDecoder(istream);
		final BufferedImage thisImage = jpegDec.decodeAsBufferedImage();

		final int maxx = thisImage.getWidth() / 2;
		final int maxy = thisImage.getHeight() / 2;

		final double thisU[][] = new double[maxx][maxy];
		final double thisV[][] = new double[maxx][maxy];

		// Compute this frame's U and V
		// And the overall difference between the frames
		int rgb, r, g, b;

		final boolean diff[][] = new boolean[maxx][maxy];
		final boolean skin[][] = new boolean[maxx][maxy];

		double dU, dV, dist;

		for (int x = 0; x < maxx; x++)
		{
			for (int y = 0; y < maxy; y++)
			{
				rgb = thisImage.getRGB(x * 2, y * 2);
				r = (rgb & 0x00ff0000) >> 16;
				g = (rgb & 0x0000ff00) >> 8;
				b = (rgb & 0x000000ff);
				thisU[x][y] = -0.169 * r - 0.331 * g + 0.5 * b;
				thisV[x][y] = 0.5 * r - 0.419 * g - 0.081 * b;
				if (lastV != null)
				{
					// Do some actual computation

					// Distance between this and object
					dU = (thisU[x][y] - Uavg) / Urange;
					dV = (thisV[x][y] - Vavg) / Vrange;
					dist = dU * dU + dV * dV;
					skin[x][y] = dist <= 1;

					// Difference from last frame, using U
					dU = thisU[x][y] - lastU[x][y];
					dV = thisV[x][y] - lastV[x][y];
					dist = dU * dU + dV * dV;
					diff[x][y] = dist >= motionThreshold;
				}
			}
		}
		if (lastV != null)
		{
			// Post processing

			// smack it with a median filter
			final int threshold = (medianRadius * 2 + 1) * (medianRadius * 2 + 1) / 2;

			final boolean skin2[][] = new boolean[maxx][maxy];
			final boolean diff2[][] = new boolean[maxx][maxy];
			for (int x = medianRadius; x < maxx - medianRadius; x++)
			{
				for (int y = medianRadius; y < maxy - medianRadius; y++)
				{
					int skinCount = 0;
					int diffCount = 0;
					for (int dx = -medianRadius; dx <= medianRadius; dx++)
					{
						for (int dy = -medianRadius; dy <= medianRadius; dy++)
						{
							if (skin[x + dx][y + dy])
							{
								skinCount++;
							}
							if (diff[x + dx][y + dy])
							{
								diffCount++;
							}
						}
					}
					skin2[x][y] = skinCount > threshold;
					diff2[x][y] = diffCount > threshold;
				}
			}

			// Find the connected components
			final int label[][] = new int[maxx][maxy];

			// mark as unlabelled
			for (int x = 0; x < maxx; x++)
			{
				for (int y = 0; y < maxy; y++)
				{
					label[x][y] = 0;
				}
			}
			final Stack S = new Stack();
			Point P = new Point();

			// Label the pixels, avoiding the unfiltered area
			int currentLabel = 0;
			for (int x = 0; x < maxx; x++)
			{
				for (int y = 0; y < maxy; y++)
				{
					if (skin2[x][y] && diff2[x][y] && (label[x][y] == 0))
					{
						// An unlabelled skin pixel
						currentLabel++;
						label[x][y] = currentLabel;
						P.x = x;
						P.y = y;
						S.push(P);
						while (!S.empty())
						{
							P = (Point) S.pop();
							final int sx = P.x;
							final int sy = P.y;
							for (int cx = sx - 1; cx <= sx + 1; cx++)
							{
								for (int cy = sy - 1; cy <= sy + 1; cy++)
								{
									if (skin2[cx][cy] && diff2[cx][cy] && (label[cx][cy] == 0))
									{
										label[cx][cy] = currentLabel;
										P = new Point();
										P.x = cx;
										P.y = cy;
										S.push(P);
									}
								}
							}
						}
					}
				}
			}

			// Compute blob info

			numBlobs = currentLabel;
			blobSize = new int[numBlobs];
			blobX = new double[numBlobs];
			blobY = new double[numBlobs];
			blobLabel = new int[numBlobs];

			for (b = 0; b < numBlobs; b++)
			{
				blobSize[b] = 0;
				blobX[b] = 0;
				blobY[b] = 0;
				blobLabel[b] = b + 1;
			}
			for (int x = 0; x < maxx; x++)
			{
				for (int y = 0; y < maxy; y++)
				{
					if (label[x][y] != 0)
					{
						blobSize[label[x][y] - 1]++;
						blobX[label[x][y] - 1] += x;
						blobY[label[x][y] - 1] += y;
					}
				}
			}

			// Sort the blobs, ignoring small ones
			int tempSize;
			double tempX;
			double tempY;
			int tempLabel;
			for (b = 0; b < numBlobs; b++)
			{
				int max = blobSize[b];
				int maxIx = b;
				for (int b2 = b + 1; b2 < numBlobs; b2++)
				{
					if (blobSize[b2] > max)
					{
						max = blobSize[b2];
						maxIx = b2;
					}
				}
				if (max < minBlobSize)
				{
					// Why bother?
					numBlobs = b;
				}
				else
				{
					// Swap them
					tempSize = blobSize[b];
					blobSize[b] = blobSize[maxIx];
					blobSize[maxIx] = tempSize;
					tempX = blobX[b];
					blobX[b] = blobX[maxIx];
					blobX[maxIx] = tempX;
					tempY = blobY[b];
					blobY[b] = blobY[maxIx];
					blobY[maxIx] = tempY;
					tempLabel = blobLabel[b];
					blobLabel[b] = blobLabel[maxIx];
					blobLabel[maxIx] = tempLabel;
				}
			}

			for (b = 0; b < numBlobs; b++)
			{
				blobX[b] /= blobSize[b];
				blobY[b] /= blobSize[b];
			}

			// Draw a picture for posterity
			for (int x = 0; x < maxx; x++)
			{
				for (int y = 0; y < maxy; y++)
				{
					// Skin region in upper left
					if (skin2[x][y])
					{
						rgb = 0xffffffff;
					}
					else
					{
						rgb = 0xff000000;
					}
					// Moving region in upper right
					thisImage.setRGB(x, y, rgb);
					if (diff2[x][y])
					{
						rgb = 0xffffffff;
					}
					else
					{
						rgb = 0xff000000;
					}
					// Moving skin in lower left
					thisImage.setRGB(x + maxx, y, rgb);
					if (skin2[x][y] && diff2[x][y])
					{
						rgb = 0xffffffff;
					}
					else
					{
						rgb = 0xff000000;
					}
					thisImage.setRGB(x, y + maxy, rgb);
					// 3 largest blobs in lower right
					if ((numBlobs > 0) && (label[x][y] == blobLabel[0]))
					{
						rgb = 0xff00ff00;
					}
					else if ((numBlobs > 1) && (label[x][y] == blobLabel[1]))
					{
						rgb = 0xffff0000;
					}
					else if ((numBlobs > 2) && (label[x][y] == blobLabel[2]))
					{
						rgb = 0xff0000ff;
					}
					else
					{
						rgb = 0xff000000;
					}
					thisImage.setRGB(x + maxx, y + maxy, rgb);
				}
			}
		}

		if (lastV != null)
		{
			// Write the image out
			final OutputStream ostream = new FileOutputStream("out.jpg");
			final JPEGImageEncoder jpegEnc = JPEGCodec.createJPEGEncoder(ostream);
			jpegEnc.encode(thisImage);
		}

		lastU = thisU;
		lastV = thisV;
	}

	void setMedianRadius(final int newRadius)
	{
		medianRadius = newRadius;
	}

	void setMinBlobSize(final int newMinSize)
	{
		minBlobSize = newMinSize;
	}

	void setMotionThreshold(final double newThreshold)
	{
		motionThreshold = newThreshold * newThreshold;
	}

	void setUavg(final double newUavg)
	{
		Uavg = newUavg;
	}

	void setUrange(final double newUrange)
	{
		Urange = newUrange * newUrange;
	}

	void setVavg(final double newVavg)
	{
		Vavg = newVavg;
	}

	void setVrange(final double newVrange)
	{
		Vrange = newVrange * newVrange;
	}

	void trainValues(final String objectURL, final String backgroundURL) throws Exception
	{
		// Read in the images
		URL theURL = new URL(objectURL);
		InputStream istream = theURL.openStream();
		JPEGImageDecoder jpegDec = JPEGCodec.createJPEGDecoder(istream);
		final BufferedImage objImage = jpegDec.decodeAsBufferedImage();
		theURL = new URL(backgroundURL);
		istream.close();
		istream = theURL.openStream();
		jpegDec = JPEGCodec.createJPEGDecoder(istream);
		final BufferedImage bkgImage = jpegDec.decodeAsBufferedImage();

		// Assume that the middle 25% along both axes is object
		final int maxx = objImage.getWidth() / 4;
		final int maxy = objImage.getHeight() / 4;
		final int xOff = 3 * maxx / 2;
		final int yOff = 3 * maxy / 2;

		final double objU[][] = new double[maxx][maxy];
		final double objV[][] = new double[maxx][maxy];
		final double bkgU[][] = new double[maxx][maxy];
		final double bkgV[][] = new double[maxx][maxy];

		// Compute the UV values
		int rgb, r, g, b;
		double objUavg = 0;
		double objVavg = 0;
		double bkgUavg = 0;
		double bkgVavg = 0;
		for (int x = 0; x < maxx; x++)
		{
			for (int y = 0; y < maxy; y++)
			{
				rgb = objImage.getRGB(x + xOff, y + yOff);
				objImage.setRGB(x, y, rgb);
				r = (rgb & 0x00ff0000) >> 16;
				g = (rgb & 0x0000ff00) >> 8;
				b = (rgb & 0x000000ff);
				objU[x][y] = -0.169 * r - 0.331 * g + 0.5 * b;
				objV[x][y] = 0.5 * r - 0.419 * g - 0.081 * b;
				objUavg += objU[x][y];
				objVavg += objV[x][y];
				rgb = bkgImage.getRGB(x + xOff, y + yOff);
				bkgImage.setRGB(x, y, rgb);
				r = (rgb & 0x00ff0000) >> 16;
				g = (rgb & 0x0000ff00) >> 8;
				b = (rgb & 0x000000ff);
				bkgU[x][y] = -0.169 * r - 0.331 * g + 0.5 * b;
				bkgV[x][y] = 0.5 * r - 0.419 * g - 0.081 * b;
				bkgU[x][y] = Math.abs(objU[x][y] - bkgU[x][y]);
				bkgV[x][y] = Math.abs(objV[x][y] - bkgV[x][y]);
				bkgUavg += bkgU[x][y];
				bkgVavg += bkgV[x][y];
			}
		}
		objUavg /= (maxx * maxy);
		objVavg /= (maxx * maxy);
		bkgUavg /= (maxx * maxy);
		bkgVavg /= (maxx * maxy);

		// Do the stats
		double objUvar = 0;
		double objVvar = 0;
		double bkgUvar = 0;
		double bkgVvar = 0;
		double diff;
		for (int x = 0; x < maxx; x++)
		{
			for (int y = 0; y < maxy; y++)
			{
				diff = objU[x][y] - objUavg;
				objUvar += diff * diff;
				diff = objV[x][y] - objVavg;
				objVvar += diff * diff;
				diff = bkgU[x][y] - bkgUavg;
				bkgUvar += diff * diff;
				diff = bkgV[x][y] - bkgVavg;
				bkgVvar += diff * diff;
			}
		}
		objUvar /= (maxx * maxy);
		objVvar /= (maxx * maxy);
		bkgUvar /= (maxx * maxy);
		bkgVvar /= (maxx * maxy);

		// Set the threholds
		Uavg = objUavg;
		Vavg = objVavg;
		Urange = 2 * objUvar;
		Vrange = 2 * objVvar;
		motionThreshold = (bkgUvar + bkgVvar) / 2;

	}

}

class Point
{
	public int x, y;
}
