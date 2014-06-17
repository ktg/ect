/*
 * <COPYRIGHT>
 * 
 * Copyright (c) 2005, University of Nottingham All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  - Neither the name of the University of Nottingham nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * </COPYRIGHT>
 * 
 * Created by: Shahram Izadi (University of Nottingham) 
 * 
 * Contributors: 
 * 		Shahram Izadi (University of Nottingham) 
 * 		Jan Humble (University of Nottingham)
 */
package equip.ect.components.lcddisplay;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

public class LCDDisplay implements Serializable
{

	private final static int MAX_PACKET_TEXT_BUFFER_SIZE = 30;

	private static final int VERTICAL_SCROLL = 1;

	private static final int HORIZONTAL_SCROLL = 2;
	static
	{
		try
		{
			System.loadLibrary("particle32");
			System.loadLibrary("lcddisplay");
		}
		catch (final Throwable t)
		{
			t.printStackTrace();
		}
	}

	public native static int addToTextBuffer(int dispnr, String text);

	public native static int clear(int dispNum, int background);

	public native static int clearTextBuffer(int dispnr);

	public native static int close();

	public native static int drawBox(int dispNum, int startRow, int startCol, int endRow, int endCol, int thickness);

	public native static int drawEllipse(int dispNum, int centerRow, int centerCol, int horRad, int verRad,
			int thickness);

	public native static int drawFromMem(int dispNum, int index);

	public native static int drawImage(int dispNum, byte[] img, int len, int posx, int posy, int orientation,
			boolean store);

	public native static int drawLine(int dispNum, int startRow, int startCol, int endRow, int endCol, int thickness);

	public native static int invert(int dispNum);

	public static void main(final String[] args)
	{
		// test();
		segmentString("H", MAX_PACKET_TEXT_BUFFER_SIZE);
	}

	// native jni calls
	public native static int open(int sendPort, int receivePort);

	public native static int scrollRectDisplay(int dispnr, int start_row, int start_col, int end_row, int end_col,
			int direction, int speed, int steps);

	public native static int scrollTextBuffer(int dispnr, int orientation, int row, int col, int rotation, int draw,
			int wordwrap, int speed, int nrsteps);

	public static String[] segmentString(final String text, final int segmentSize)
	{
		final int nrSegments = (int) (Math.ceil(text.length() / (double) segmentSize));
		final String[] segments = new String[nrSegments];
		for (int i = 0; i < nrSegments; i++)
		{
			final int startIndex = i * segmentSize;
			int endIndex = startIndex + segmentSize;
			if (endIndex > text.length())
			{
				endIndex = text.length();
			}
			segments[i] = text.substring(startIndex, endIndex);
			// System.out.println(segment);
		}
		return segments;
	}

	public native static void setParticleID(byte[] id);

	public static void test()
	{
		clearTextBuffer(0);
		addToTextBuffer(0, "Hello Stef.");
		scrollTextBuffer(0, VERTICAL_SCROLL, -5, 0, 180, 1, 1, 4, 40);

	}

	public native static int textDisplay(int dispNum, int row, int col, String text, int rot, int draw, int wordwrap);

	public native static int textScrollDisplay(int dispnr, String text, int rotation, int draw, int wordwrap);

	// text display
	private String text = null;

	private String scrollText = null;

	private int textLimit = 190; // max chars

	private boolean autoClearText = true;

	private boolean clearTextBuffer = false;

	private String textBuffer = "Hello World";

	private String scrollBufferCommand = VERTICAL_SCROLL + ", -5, 0, 180, 1, 1, 4, 40";

	private String longTextBuffer = "Hello beautiful, lonesome, and troublesome little world.  How is everything?";

	// primitive drawing operations
	private String drawLine = null;

	private String drawBox = null;

	private String drawEllipse = null;

	// clear the display 0 = pixels off, 1 = pixels on
	private float clear = 0.0f;

	// invert pixels on display
	private float invert = 0.0f;

	// image operations
	private String imageURL = new String();

	private String storeImage = new String();

	private int retrieveImage = -1;

	// particles comms
	private String particleID = "BROADCAST";

	private int sendPort = 5556;

	private int receivePort = 5555;

	// NB. expose following variable to address
	// multiple displays off the single particle
	private int dispNum = 0;

	private boolean wordWrap = true;

	private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public LCDDisplay()
	{
		open(sendPort, receivePort);
		// test();
	}

	public synchronized void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.addPropertyChangeListener(listener);
	}

	public boolean getAutoClearText()
	{
		return autoClearText;
	}

	public float getClear()
	{
		return clear;
	}

	public boolean getClearTextBuffer()
	{
		return clearTextBuffer;
	}

	public String getDrawBox()
	{
		return drawBox;
	}

	public String getDrawEllipse()
	{
		return drawEllipse;
	}

	public String getDrawLine()
	{
		return drawLine;
	}

	public String getImageURL()
	{
		return imageURL;
	}

	public float getInvert()
	{
		return invert;
	}

	public String getLongTextBuffer()
	{
		return longTextBuffer;
	}

	public String getParticleID()
	{
		return particleID;
	}

	public int getRetrieveImage()
	{
		return retrieveImage;
	}

	public String getScrollBufferCommand()
	{
		return scrollBufferCommand;
	}

	public String getScrollText()
	{
		return this.scrollText;
	}

	public String getStoreImage()
	{
		return storeImage;
	}

	public String getText()
	{
		return text;
	}

	public String getTextBuffer()
	{
		return textBuffer;
	}

	public boolean getWordWrap()
	{
		return this.wordWrap;
	}

	public byte[] parseParamsByte(final String params)
	{
		if (params != null)
		{
			try
			{
				final String tokens[] = params.split(",");
				final byte vals[] = new byte[tokens.length];
				for (int i = 0; i < tokens.length; i++)
				{
					vals[i] = (byte) Integer.parseInt(tokens[i].trim());
				}
				return vals;
			}
			catch (final Exception e)
			{
			}
		}
		return null;
	}

	public int[] parseParamsInt(final String params)
	{
		if (params != null)
		{
			try
			{
				final String tokens[] = params.split(",");
				final int vals[] = new int[tokens.length];
				for (int i = 0; i < tokens.length; i++)
				{
					vals[i] = Integer.parseInt(tokens[i].trim());
				}
				return vals;
			}
			catch (final Exception e)
			{
			}
		}
		return null;
	}

	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		propertyChangeListeners.removePropertyChangeListener(listener);
	}

	public synchronized void sendImage(final String imageURL, final boolean store)
	{
		try
		{
			final URL url = new URL(imageURL);
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final InputStream is = url.openStream();
			int actual = 0;
			byte buffer[] = new byte[96 * 40];
			while ((actual = is.read(buffer)) != -1)
			{
				os.write(buffer, 0, actual);
			}
			os.flush();
			os.close();
			is.close();
			buffer = os.toByteArray();
			drawImage(dispNum, buffer, buffer.length, 0, 0, 0, store);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setAutoClearText(final boolean autoClearText)
	{
		propertyChangeListeners.firePropertyChange("autoClearText", this.autoClearText, autoClearText);
		this.autoClearText = autoClearText;
	}

	public void setClear(final float clear)
	{
		clear(dispNum, (int) clear);
		propertyChangeListeners.firePropertyChange("clear", new Float(this.clear), new Float(clear));
		this.clear = clear;
	}

	public void setClearTextBuffer(final boolean clearTextBuffer)
	{
		final boolean old = this.clearTextBuffer;
		this.clearTextBuffer = clearTextBuffer;
		propertyChangeListeners.firePropertyChange("clearTextBuffer", old, clearTextBuffer);
		if (clearTextBuffer)
		{
			clearTextBuffer(this.dispNum);
			// change the text buffer
			// no need to send anything to the particle
			propertyChangeListeners.firePropertyChange("textBuffer", this.textBuffer, "");
			this.textBuffer = "";
			setClearTextBuffer(false);
		}
	}

	public void setDrawBox(final String drawBox)
	{
		final int vals[] = parseParamsInt(drawBox);
		if (vals != null && vals.length > 4)
		{
			drawBox(dispNum, vals[0], vals[1], vals[2], vals[3], vals[4]);
		}
		propertyChangeListeners.firePropertyChange("drawBox", this.drawBox, drawBox);
		this.drawBox = drawBox;
	}

	public void setDrawEllipse(final String drawEllipse)
	{
		final int vals[] = parseParamsInt(drawEllipse);
		if (vals != null && vals.length > 4)
		{
			drawEllipse(dispNum, vals[0], vals[1], vals[2], vals[3], vals[4]);
		}
		propertyChangeListeners.firePropertyChange("drawEllipse", this.drawEllipse, drawEllipse);
		this.drawEllipse = drawEllipse;
	}

	public void setDrawLine(final String drawLine)
	{
		final int vals[] = parseParamsInt(drawLine);
		if (vals != null && vals.length > 4)
		{
			drawLine(dispNum, vals[0], vals[1], vals[2], vals[3], vals[4]);
		}
		propertyChangeListeners.firePropertyChange("drawLine", this.drawLine, drawLine);
		this.drawLine = drawLine;
	}

	public void setImageURL(final String imageURL)
	{
		sendImage(imageURL, false);
		propertyChangeListeners.firePropertyChange("imageURL", this.imageURL, imageURL);
		this.imageURL = imageURL;
	}

	public void setInvert(final float invert)
	{
		invert(dispNum);
		propertyChangeListeners.firePropertyChange("invert", new Float(this.invert), new Float(invert));
		this.invert = invert;
	}

	public synchronized void setLongTextBuffer(final String longTextBuffer)
	{
		final String old = this.longTextBuffer;
		this.longTextBuffer = longTextBuffer;
		final String[] segments = segmentString(longTextBuffer, MAX_PACKET_TEXT_BUFFER_SIZE);
		setClearTextBuffer(true);
		for (final String segment : segments)
		{
			setTextBuffer(segment);
		}
		setScrollBufferCommand(this.scrollBufferCommand);
		propertyChangeListeners.firePropertyChange("longTextBuffer", old, longTextBuffer);
	}

	public void setParticleID(String particleID)
	{
		final byte vals[] = parseParamsByte(particleID);
		if (vals != null && vals.length > 7)
		{
			setParticleID(vals);
		}
		else
		{
			setParticleID((byte[]) null);
			particleID = "BROADCAST";
		}
		propertyChangeListeners.firePropertyChange("particleID", this.particleID, particleID);
		this.particleID = particleID;
	}

	public void setRetrieveImage(final int retrieveImage)
	{
		drawFromMem(dispNum, retrieveImage);
		propertyChangeListeners.firePropertyChange("retrieveImage", this.retrieveImage, retrieveImage);
		this.retrieveImage = retrieveImage;
	}

	public void setScrollBufferCommand(final String scrollBufferCommand)
	{
		final String old = "Something else"; // this is just to force a property
		// update on all commands
		this.scrollBufferCommand = scrollBufferCommand;
		final int[] vals = parseParamsInt(scrollBufferCommand);
		final int orientation = vals[0];
		final int row = vals[1];
		final int col = vals[2];
		final int rotation = vals[3];
		final int draw = vals[4];
		final int wordwrap = vals[5];
		final int speed = vals[6];
		final int nrsteps = vals[7];
		scrollTextBuffer(this.dispNum, orientation, row, col, rotation, draw, wordwrap, speed, nrsteps);
		propertyChangeListeners.firePropertyChange("scrollBufferCommand", old, scrollBufferCommand);
	}

	public void setScrollText(String text)
	{
		if (text != null)
		{
			if (autoClearText)
			{
				clear(dispNum, 0);
			}
			try
			{
				if (text.length() > textLimit)
				{
					text = text.substring(0, textLimit);
				}
				final String tokens[] = text.split(",");
				final int rot = Integer.parseInt(tokens[0].trim());
				final int draw = Integer.parseInt(tokens[1].trim());
				String parsed = new String();
				for (int i = 2; i < tokens.length; i++)
				{
					parsed += tokens[i];
				}
				textScrollDisplay(dispNum, parsed.length() > 0 ? parsed : text, rot, draw, wordWrap ? 1 : 0);
			}
			catch (final Exception e)
			{
				// just display text ignore params
				textScrollDisplay(dispNum, text, 0, 1, wordWrap ? 1 : 0);
			}
		}
		propertyChangeListeners.firePropertyChange("scrollText", this.scrollText, text);
		this.scrollText = text;
	}

	public void setStoreImage(final String storeImage)
	{
		sendImage(storeImage, true);
		propertyChangeListeners.firePropertyChange("storeImage", this.storeImage, storeImage);
		this.storeImage = storeImage;
	}

	public void setText(String text)
	{
		if (text != null)
		{
			if (autoClearText)
			{
				clear(dispNum, 0);
			}
			try
			{
				if (text.length() > textLimit)
				{
					text = text.substring(0, textLimit);
				}
				final String tokens[] = text.split(",");
				final int row = Integer.parseInt(tokens[0].trim());
				final int col = Integer.parseInt(tokens[1].trim());
				final int rot = Integer.parseInt(tokens[2].trim());
				final int draw = Integer.parseInt(tokens[3].trim());
				String parsed = new String();
				for (int i = 4; i < tokens.length; i++)
				{
					parsed += tokens[i];
				}
				textDisplay(dispNum, row, col, parsed.length() > 0 ? parsed : text, rot, draw, wordWrap ? 1 : 0);
			}
			catch (final Exception e)
			{
				// just display text ignore params
				textDisplay(dispNum, 0, 0, text, 0, 1, wordWrap ? 1 : 0);
			}
		}
		propertyChangeListeners.firePropertyChange("text", this.text, text);
		this.text = text;
	}

	public void setTextBuffer(final String addText)
	{
		final String old = this.textBuffer;
		this.textBuffer = this.textBuffer + addText;
		propertyChangeListeners.firePropertyChange("textBuffer", old, this.textBuffer);
		addToTextBuffer(0, addText);
	}

	public void setWordWrap(final boolean wordWrap)
	{
		final boolean old = this.wordWrap;
		this.wordWrap = wordWrap;
		propertyChangeListeners.firePropertyChange("wordWrap", old, wordWrap);
	}

	public void stop()
	{
		close();
	}
}
