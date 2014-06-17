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
  Tom Rodden (University of Nottingham)
  Shahram Izadi (University of Nottingham)
  Jan Humble (Swedish Institute of Computer Science AB)

 */
package equip.ect.components.camera;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import equip.ect.ContainerManagerHelper;
import se.sics.motionstudio.MotionStudio;

import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraPictureGrabber
{

	public static final String DEFAULT_CAPTURE_DEVICE = "vfw://0";
	public static final String DEFAULT_IMAGE_NAME = "picture.jpg";

	private Object grabLock = new Object();
	private String captureDevice = null;
	private FrameGrabbingControl control = null;
	private VideoFormat format = null;

	public CameraPictureGrabber()
	{
		this(DEFAULT_CAPTURE_DEVICE);
	}

	public CameraPictureGrabber(final String captureDevice)
	{
		this.captureDevice = (captureDevice == null) ? DEFAULT_CAPTURE_DEVICE : captureDevice;
		initCamera();
	}

	public File grabAndDumpToFile(final File directory) throws IOException
	{
		final Image image = grabFrame();

		if (image != null)
		{
			final File file = ContainerManagerHelper.createLocalFile(DEFAULT_IMAGE_NAME, directory);
			final FileOutputStream fos = new FileOutputStream(file);
			final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_INT_RGB);
			final Graphics2D g2 = bufferedImage.createGraphics();
			g2.drawImage(image, null, null);
			final JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
			encoder.encode(bufferedImage);
			fos.close();
			return file;
		}
		return null;
	}

	public Image grabFrame()
	{
		try
		{
			synchronized (grabLock)
			{
				final BufferToImage bti = new BufferToImage(format);
				return bti.createImage(control.grabFrame());
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void stop()
	{
		MotionStudio.getInstance().close();
	}

	private void initCamera()
	{
		final MotionStudio ms = MotionStudio.getInstance();
		ms.open(captureDevice);
		control = ms.getFrameGrabbingControl();
		format = ms.getCurrentVideoFormat();
	}
}
