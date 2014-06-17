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
package equip.ect.components.visionframework.artoolkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.jartoolkit.core.JARToolKit;
import equip.ect.components.visionframework.common.AbstractVideoProcessor;
import equip.ect.components.visionframework.common.Frame;
import equip.ect.components.visionframework.common.FrameProcessor;
import equip.ect.components.visionframework.common.UnsupportedFormatException;

/**
 * ARToolkit vision framework component, to track ARToolkit glyphs.<br>
 * <H3>Summary</H3> ARToolkit vision framework component, using jARToolkit v2.0 Java wrapper for
 * ARToolkit to track ARToolkit glyphs.<br>
 * <H3>Installation</H3> See http://www.hitl.washington.edu/artoolkit/ and
 * http://www.c-lab.de/jartoolkit/ and http://sourceforge.net/projects/jartoolkit/ download.
 * Multiple instances in the same JVM share the same ARToolkit instance and will not re-evaluate the
 * same frame. Config files are in resources/ARToolkit (=install/ARToolkit). <H3>Configuration</H3>
 * Create one instance per glyph to be tracked. Configure using properties:<br>
 * 
 * threshold - match threshold - can leave as default <br>
 * glyphUrl - URL (probably HTTP) of ARToolkit pattern file of the glyph to be watched for by this
 * instance <br>
 * cameraConfigUrl - URL of ARToolkit camera configuration file <br>
 * <br>
 * Link source from a CaptureDevice component to the ARToolkit component's sink. <H3>Usage</H3>
 * Current outputs are:<br>
 * glyphVisible (boolean) - has the glyph been recognised in the most recent frame <br>
 * glyphTransform (current String) - actually a space-separated list of doubles result from
 * ARToolkit getTransMatrix (current with width 80 and centre 0)<br>
 * <H3>Technical Details</H3> No technical details given<br>
 * 
 * @classification Media/Video/Analysis
 * @displayName VideoARToolkitGlyphTracker
 * @preferred
 * @defaultInputProperty sink
 * @technology JMF video processing
 * @technology ARToolkit
 */
public class ARToolkitGlyphTracker extends AbstractVideoProcessor
{
	/**
	 * internal FrameProcessor
	 */
	public class MyFrameProcessor implements FrameProcessor
	{
		/**
		 * new {@link Frame}.
		 * 
		 * @return processed frame (may be the same).
		 */
		@Override
		public Frame processFrame(final Frame frame, final Frame reference)
		{
			try
			{
				if (debug)
				{
					System.out.println("ARToolkitGlyph process frame: " + frame);
				}

				final double t[] = ARToolkitEngine.instance().processFrame(frame, threshold, cameraConfigUrl, glyphUrl);

				// System.out.println("AXH: skip property update");
				if (t != null)
				{
					// doSetGlyphTransform(t);

					/*
					 * glyphTransform = t;
					 * 
					 * for (int a=0; a<handlers.size(); a++) { //System.out.println("in loop...");
					 * GlyphTransformHandler handler = (GlyphTransformHandler)
					 * handlers.elementAt(a); handler.updateQlyphTransfrom(t); }
					 */
					final double[] old = transformationMatrix;
					transformationMatrix = t; // .setMatrix(t);
					propertyChangeListeners.firePropertyChange("glyphTransform", old, t);
				}
				setGlyphVisible(t != null);
				return frame;
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in ARToolkitGlyph.processFrame: " + e);
				e.printStackTrace(System.err);
			}
			return frame;
		}
	}

	/**
	 * singleton class wrapping (j)ARToolkit use
	 */
	protected static class ARToolkitEngine
	{
		/**
		 * get instance
		 */
		static synchronized ARToolkitEngine instance() throws InstantiationException
		{
			if (_instance == null)
			{
				_instance = new ARToolkitEngine();
			}
			return _instance;
		}

		/**
		 * JARToolkit instance
		 */
		JARToolKit tk;
		/**
		 * instance
		 */
		static ARToolkitEngine _instance = null;
		/**
		 * config file
		 */
		String configFile = null;
		/**
		 * map of loaded glyph files -> Integer
		 */
		Map<String, Integer> glyphMap = new HashMap<String, Integer>();
		/**
		 * last size
		 */
		int xsize, ysize;
		/**
		 * frame
		 */
		Frame lastFrame = null;
		/**
		 * markers
		 */
		int[] markers = null;
		/**
		 * threshold
		 */
		float threshold = 0;

		/**
		 * cons
		 */
		protected ARToolkitEngine() throws InstantiationException
		{
			this.tk = JARToolKit.create();
		}

		/**
		 * process frame - glyphTransform
		 */
		public synchronized double[] processFrame(final Frame frame, final float threshold,
				final String cameraConfigUrl, final String glyphUrl) throws ARToolkitException,
				UnsupportedFormatException
		{
			boolean process = false;

			if (configFile == null)
			{
				System.out.println("ARToolkit read config " + cameraConfigUrl);
				try
				{
					final String fname = getUrl(cameraConfigUrl).getPath();
					System.out.println("- local file " + fname);
					final int result = tk.paramLoad(fname);
					if (result != 0) { throw new ARToolkitException("ERROR: ARToolkit paramLoad " + cameraConfigUrl
							+ " as " + fname + " failed"); }
				}
				catch (final Exception e)
				{
					System.err.println("ERROR reading config: " + e);
					e.printStackTrace(System.err);
					throw new ARToolkitException("ERROR: ARToolkit paramLoad " + cameraConfigUrl + " failed: " + e);
				}
				configFile = cameraConfigUrl;
				process = true;
			}
			if (!configFile.equals(cameraConfigUrl))
			{
				System.err.println("WARNING: ARToolkit cannot re-load difference camera config: " + cameraConfigUrl
						+ " vs " + configFile);
			}

			if (frame.width != xsize || frame.height != ysize)
			{
				xsize = frame.width;
				ysize = frame.height;
				System.out.println("ARToolkit resize to " + xsize + "," + ysize);
				int result = tk.paramChangeSize(xsize, ysize);
				if (result != 0) { throw new ARToolkitException("Error in method: JARToolKit.paramChangeSize(" + xsize
						+ "," + ysize + ")."); }
				result = tk.initCparam();
				if (result != 0) { throw new ARToolkitException("Error in method: JARToolKit.initCparam()."); }
				tk.paramDisplay();
				process = true;
			}

			// glyph?
			Integer patt_id = glyphMap.get(glyphUrl);
			if (patt_id == null)
			{
				System.out.println("load glyph " + glyphUrl);
				try
				{
					final String fname = getUrl(glyphUrl).getPath();
					System.out.println("- local file " + fname);
					final int p = tk.loadPattern(fname);
					if (p < 0) { throw new ARToolkitException("ERROR: ARToolkit loadPattern " + glyphUrl + " as "
							+ fname + " failed"); }
					patt_id = new Integer(p);
				}
				catch (final Exception e)
				{
					throw new ARToolkitException("ERROR: ARToolkit loadPattern " + glyphUrl + " failed: " + e);
				}

				glyphMap.put(glyphUrl, patt_id);
				System.out.println("pattern id: " + patt_id);
				process = true;
			}

			if (threshold != this.threshold)
			{
				this.threshold = threshold;
				process = true;
				System.out.println("Threshold changed to " + threshold);
			}

			if (!(frame == lastFrame) || markers == null)
			{
				process = true;
			}

			if (process)
			{
				lastFrame = frame;
				// packed RGB??
				final Frame f = frame.getInFormat(Frame.TYPE_RGB, 1, Integer.TYPE).getPacked();
				markers = tk.detectMarker((int[]) f.data, (int) threshold);
				// System.out.println("Detected "+markers.length+" markers");
			}

			for (final int marker : markers)
			{
				if (marker == patt_id.intValue())
				{
					// found!
					// System.out.println("Found marker "+glyphUrl);
					return tk.getTransMatrix(marker, /* width */80, /* centre */0, 0);
				}
			}
			// System.out.println("Did not find marker "+glyphUrl);
			return null;
		}

		/**
		 * get URL as local file
		 */
		File getUrl(final String url) throws IOException
		{
			final URL theURL = new URL(url);
			if (theURL.getProtocol().equals("file"))
			{
				String path = theURL.getPath();
				if (path.startsWith("/"))
				{
					path = path.substring(1);
				}
				final File f = new File(path);
				return f;
			}
			final InputStream istream = theURL.openStream();
			final File f = File.createTempFile("cache-", "-" + new File(theURL.getPath()).getName());
			final FileOutputStream fout = new FileOutputStream(f);
			final byte[] buf = new byte[20000];
			while (true)
			{
				final int c = istream.read(buf);
				if (c <= 0)
				{
					break;
				}
				fout.write(buf, 0, c);
			}
			fout.close();
			istream.close();
			return f;
		}
	}

	/**
	 * defualt glyph url - hiro
	 */
	public static final String DEFAULT_GLYPH_URL = "file:///C:/inScape/ect/resources/ARToolkit/data/A.patt";
	/**
	 * defualt camera config url
	 */
	public static final String DEFAULT_CAMERA_CONFIG_URL = "file:///C:/inScape/ect/resources/ARToolkit/data/camera_para.dat";
	/**
	 * default threshold
	 */
	public static final float DEFAULT_THRESHOLD = 100;
	/**
	 * threshold
	 */
	protected float threshold = DEFAULT_THRESHOLD;
	/**
	 * glyph url
	 */
	protected String glyphUrl = DEFAULT_GLYPH_URL;
	/**
	 * camera config url
	 */
	protected String cameraConfigUrl = DEFAULT_CAMERA_CONFIG_URL;
	/**
	 * output - glyph visible exceeded
	 */
	protected boolean glyphVisible;
	// protected double[] glyphTransfrom;
	// protected TransformationMatrixHolder transformationMatrix = new TransformationMatrixHolder();
	protected double[] transformationMatrix;

	/**
	 * cons
	 */
	public ARToolkitGlyphTracker() throws IOException, InstantiationException
	{
		super("ARToolkitGlyphTracker");
		setConfiguration("ARToolkitGlyph(" + threshold + "," + cameraConfigUrl + "," + glyphUrl + ")");
		try
		{
			ARToolkitEngine.instance();
		}
		catch (final InstantiationException e)
		{
			System.err.println("ERROR instantiating JARToolkit: " + e);
		}
	}

	/**
	 * getter
	 */
	public String getCameraConfigUrl()
	{
		return cameraConfigUrl;
	}

	public double[] getGlyphTransform()
	{
		return transformationMatrix;
	}

	/**
	 * getter
	 */
	public String getGlyphUrl()
	{
		return glyphUrl;
	}

	/**
	 * getter
	 */
	public boolean getGlyphVisible()
	{
		return glyphVisible;
	}

	/**
	 * getter
	 */
	public float getThreshold()
	{
		return threshold;
	}

	/**
	 * setter
	 */
	public synchronized void setCameraConfigUrl(final String s)
	{
		final Object old = cameraConfigUrl;
		cameraConfigUrl = s;
		propertyChangeListeners.firePropertyChange("cameraConfigUrl", old, s);
	}

	/**
	 * setter
	 */
	public synchronized void setGlyphUrl(final String s)
	{
		final Object old = glyphUrl;
		glyphUrl = s;
		propertyChangeListeners.firePropertyChange("glyphUrl", old, s);
	}

	/**
	 * setter
	 */
	public synchronized void setThreshold(final float t)
	{
		final Object old = new Float(threshold);
		threshold = t;
		propertyChangeListeners.firePropertyChange("threshold", old, new Float(t));
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	@Override
	protected synchronized FrameProcessor getProcessor(final String name, final Vector args)
	{
		if (!name.equals("ARToolkitGlyph"))
		{
			System.err.println("ARToolkitGlyphTracker asked for unknown processor: " + name);
			args.setElementAt("*ARToolkitGlyph*", 0);
			return null;
		}
		try
		{
			final String vals[] = parseStringArgs(args, 3);
			if (vals == null) { return null; }
			final float t = new Float(vals[0]).floatValue();
			setThreshold(t);
			setCameraConfigUrl(vals[1]);
			setGlyphUrl(vals[2]);
		}
		catch (final NumberFormatException e)
		{
			args.setElementAt("*ERROR*" + ((String) args.elementAt(1)), 1);
			System.err.println("Error parsing int " + args.elementAt(1) + ": " + e);
			return null;
		}
		return new MyFrameProcessor();
	}

	/**
	 * setter - internal
	 */
	protected synchronized void setGlyphVisible(final boolean t)
	{
		if (glyphVisible == t) { return; }
		final Object old = new Boolean(glyphVisible);
		glyphVisible = t;
		propertyChangeListeners.firePropertyChange("glyphVisible", old, new Boolean(t));
	}

	// /** output - glyph transform (as string)
	// */
	// Vector handlers = new Vector();
	// protected class MyGlyphTransformSource implements GlyphTransformSource
	// {
	//
	// public void registerGlyphTransformHandler(GlyphTransformHandler handler)
	// {
	// System.out.println("register glyph handler...");
	// addHandler(handler);
	// }
	// }
	//
	// public void addHandler(GlyphTransformHandler handler)
	// {
	// handlers.add(handler);
	// }
	//
	// MyGlyphTransformSource source = new MyGlyphTransformSource();
	// protected double [] glyphTransform;
	// /** getter
	// */
	// public synchronized GlyphTransformSource getGlyphTransform() {
	// //System.out.println("getting source...");
	// //System.out.println(source.working());
	// return source;
	// /*
	// if (glyphTransform==null)
	// return null;
	// StringBuffer b = new StringBuffer();
	// for (int i=0; i<glyphTransform.length; i++)
	// {
	// b.append(new Double(glyphTransform[i]).toString());
	// if (i+1<glyphTransform.length)
	// b.append(" ");
	// }
	// return b.toString();
	// */
	// }
	/**
	 * setter - internal
	 */
	/*
	 * protected synchronized void doSetGlyphTransform(double t[]) {
	 * //System.out.println("do set glyph trans"); //Object old = getGlyphTransform();
	 * glyphTransform = t;
	 * 
	 * for (int a=0; a<handlers.size(); a++) { //System.out.println("in loop...");
	 * GlyphTransformHandler handler = (GlyphTransformHandler) handlers.elementAt(a);
	 * handler.updateQlyphTransfrom(t); }
	 * //propertyChangeListeners.firePropertyChange("glyphTransform", old, getGlyphTransform()); }
	 */
}
