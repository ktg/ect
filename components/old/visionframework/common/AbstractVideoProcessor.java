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
package equip.ect.components.visionframework.common;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import equip.ect.ContainerManager;
import equip.ect.Persistable;
import equip.ect.PersistenceManager;
import equip.ect.http.SimpleFormHttpServer;
import equip.ect.http.SimpleFormModel;
import equip.ect.http.SimpleFormProvider;

/**
 * abstract base class for frame-in, frame-out video processor with web server config and image
 * access and text-based configuration.
 */
public class AbstractVideoProcessor implements Serializable, FrameHandler, SimpleFormProvider, Persistable
{
	/**
	 * custom HTTP server
	 */
	protected class MyHttpServer extends SimpleFormHttpServer
	{
		/**
		 * cons
		 */
		MyHttpServer(final String name, final SimpleFormProvider provider) throws IOException
		{
			super(name, provider);
		}

		/**
		 * http get - override
		 */
		@Override
		protected void getHttp(final InputStream in, final OutputStream out, final String path) throws IOException
		{
			if (path.startsWith("/action?") || path.startsWith("/config"))
			{
				super.getHttp(in, out, path);
				return;
			}
			if (path.startsWith("/image/") && path.length() > 7 && Character.isDigit(path.charAt(7)))
			{
				final int i = path.charAt(7) - '0';
				writeImage(out, i);
				return;
			}
			else if (path.startsWith("/histo/") && path.length() > 7 && Character.isDigit(path.charAt(7)))
			{
				final int i = path.charAt(7) - '0';
				writeHistogram(out, i, path.substring(8));
				return;
			}
			// top page
			writeHeader(out);

			final OutputStreamWriter outs = new OutputStreamWriter(out);
			writeMainPage(outs, path);
		}

		/**
		 * write image histogram including header
		 */
		protected void writeHistogram(final OutputStream out, final int i, final String suffix) throws IOException
		{
			Frame f = null;
			synchronized (frames)
			{
				if (i < 0 || i > frames.size())
				{
					writeFileNotFoundHeader(out);
					return;
				}
				synchronized (referenceHandler)
				{
					if (i == 0)
					{
						if (referenceFrame == null)
						{
							writeFileNotFoundHeader(out);
							return;
						}
						f = referenceFrame;
					}
				}
				writeHeader(out, "image/jpeg");
				// get image
				if (i > 0)
				{
					f = (Frame) frames.elementAt(i - 1);
				}
			}
			try
			{
				final BufferedImage image = f.getHistogramBufferedImage();
				final JPEGImageEncoder jpegEnc = JPEGCodec.createJPEGEncoder(out);
				jpegEnc.encode(image);
				System.out.println("Written histogram image " + i);
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ERROR returning JPEG image: " + e + " (frame " + f + ")");
			}
		}

		/**
		 * write image including header
		 */
		protected void writeImage(final OutputStream out, final int i) throws IOException
		{
			Frame f = null;
			synchronized (frames)
			{
				if (i < 0 || i > frames.size())
				{
					writeFileNotFoundHeader(out);
					return;
				}
				synchronized (referenceHandler)
				{
					if (i == 0)
					{
						if (referenceFrame == null)
						{
							writeFileNotFoundHeader(out);
							return;
						}
						f = referenceFrame;
					}
				}
				writeHeader(out, "image/jpeg");
				// get image
				if (i > 0)
				{
					f = (Frame) frames.elementAt(i - 1);
				}
			}
			try
			{
				final BufferedImage image = f.getBufferedImage();
				final JPEGImageEncoder jpegEnc = JPEGCodec.createJPEGEncoder(out);
				jpegEnc.encode(image);
				System.out.println("Written image " + i);
			}
			catch (final UnsupportedFormatException e)
			{
				System.err.println("ERROR returning JPEG image: " + e + " (frame " + f + ")");
			}
		}

		/**
		 * write header
		 */
		protected void writeMainPage(final OutputStreamWriter outs, final String path) throws IOException
		{
			System.err.println("Get \"" + path + "\"");
			outs.write("<html><head><title>AbstractVideoProcessor</title></head><body>\n<H1>Get " + path + "</H1>\n");
			outs.write("<A HREF=\"/config\">Configuration</A><P>\n");
			synchronized (frames)
			{
				if (referenceFrame != null)
				{
					outs.write("<A HREF=\"/image/0.jpg\">Reference Image</A><P>\n");
					outs.write("<A HREF=\"/histo/0.jpg\">Histogram for Reference Image</A><P>\n");
				}
				for (int fi = 1; fi <= frames.size(); fi++)
				{
					outs.write("<A HREF=\"/image/" + fi + ".jpg\">Image " + fi + "</A><P>\n");
					outs.write("<A HREF=\"/histo/" + fi + ".jpg\">Histogram for Image " + fi + "</A><P>\n");
				}
			}
			outs.write("</body></html>");
			outs.flush();
		}
	}

	/**
	 * PushFrameSource class to expose
	 */
	protected class MySource implements PushFrameSource
	{
		/**
		 * register a {@link FrameHandler}
		 */
		@Override
		public void registerFrameHandler(final FrameHandler handler)
		{
			synchronized (handlers)
			{
				handlers.addElement(handler);
			}
		}

		/**
		 * unregister
		 */
		@Override
		public void unregisterFrameHandler(final FrameHandler handler)
		{
			synchronized (handlers)
			{
				// note could be in handleFrame callback
				final int i = handlers.indexOf(handler);
				if (i >= 0)
				{
					handlers.removeElementAt(i);
				}
				else
				{
					System.out.println("NOTE: unregisterFrameHandler for unknown handler " + handler);
				}
			}
		}
	}

	/**
	 * debug printing
	 */
	protected boolean debug = false;

	/**
	 * configuration string
	 */
	protected String configuration = "";

	/**
	 * config url property
	 */
	protected String configUrl;
	/**
	 * configuration is a pipe-line of {@link FrameProcessor}s
	 */
	protected Vector processors = new Vector();

	/**
	 * configuration model
	 */
	protected SimpleFormModel configModel = new SimpleFormModel();

	/**
	 * server
	 */
	protected SimpleFormHttpServer configServer;
	/**
	 * stopped?
	 */
	protected boolean stopped = false;
	/**
	 * source
	 */
	protected PushFrameSource sink = null;
	/**
	 * last frame(s)
	 */
	protected Vector frames = new Vector();
	/**
	 * reference sink
	 */
	protected PushFrameSource referenceSink = null;
	/**
	 * last frame(s)
	 */
	protected Frame referenceFrame = null;
	/**
	 * reference sink
	 */
	protected FrameHandler referenceHandler = new FrameHandler()
	{
		/**
		 * new {@link Frame}.
		 * 
		 * @return whether to call with subsequent frames (i.e. false to discard this handler)
		 */
		@Override
		public boolean handleFrame(final Frame frame)
		{
			return handleReferenceFrame(frame);
		}
	};
	/**
	 * Property Change
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	/**
	 * handlers ({@link FrameHandler})
	 */
	protected Vector handlers = new Vector();
	/**
	 * source property
	 */
	protected MySource source = new MySource();
	/**
	 * persist file - re-use
	 */
	protected File persistFile = null;

	/**
	 * header
	 */
	public static final String PERSIST_HEADER = "AbstractVideoProcessor.1.0";

	/**
	 * cons
	 */
	public AbstractVideoProcessor(final String name) throws IOException
	{
		// text value
		configModel.addProperty("configuration", "Processor configuration", configuration);
		try
		{
			configServer = new MyHttpServer("Video processor " + name, this);
			configUrl = configServer.getBaseURL();
		}
		catch (final IOException e)
		{
			System.err.println("ERROR creating AbstractVideoProcessor form server: " + e);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/**
	 * apply configuration to a single function
	 */
	/**
	 * getter
	 */
	public synchronized String getConfiguration()
	{
		return configuration;
	}

	/**
	 * config url getter
	 */
	public synchronized String getConfigUrl()
	{
		return configUrl;
	}

	/**
	 * get {@link SimpleFormModel} to show - for config
	 */
	@Override
	public synchronized SimpleFormModel getModel()
	{
		return configModel;
	}

	/**
	 * source getter
	 */
	public synchronized PushFrameSource getReferenceSink()
	{
		return referenceSink;
	}

	/**
	 * source getter
	 */
	public synchronized PushFrameSource getSink()
	{
		return sink;
	}

	/**
	 * source getter
	 */
	public synchronized PushFrameSource getSource()
	{
		return source;
	}

	/**
	 * new {@link Frame}.
	 * 
	 * @return whether to call with subsequent frames (i.e. false to discard this handler)
	 */
	@Override
	public boolean handleFrame(final Frame frame)
	{
		Frame lastFrame = null;
		synchronized (this)
		{
			if (stopped)
			{
				// die die die
				return false;
			}

			try
			{
				Frame f = frame;
				final Frame reference = referenceFrame;
				synchronized (frames)
				{
					// frames.removeAllElements();
					if (frames.size() == 0)
					{
						frames.addElement(f);
					}
					else
					{
						frames.setElementAt(f, 0);
					}

					if (debug)
					{
						System.out.println("AbstractVideoProcessor new frame: " + frame);
					}
					for (int pi = 0; pi < processors.size(); pi++)
					{
						final FrameProcessor p = (FrameProcessor) processors.elementAt(pi);
						try
						{
							final Frame fin = f;
							if (debug)
							{
								System.out.println("  call processor " + pi + ": " + p);
							}
							f = p.processFrame(f, reference);
							if (f == null)
							{
								System.err.println("Processor " + p + " failed on " + fin);
								break;
							}
							else if (pi + 1 < frames.size())
							{
								frames.setElementAt(f, pi + 1);
							}
							else
							{
								frames.addElement(f);
							}
						}
						catch (final Exception e)
						{
							System.err.println("ERROR in processor " + pi + ": " + e);
							e.printStackTrace(System.err);
						}
					}
				}
				// push!
				if (f != null)
				{
					lastFrame = f;
				}
				if (debug)
				{
					System.out.println("  handle frame done");
				}
			}
			catch (final Exception e)
			{
				System.err.println("ERROR in BooleaPixelCount.handleFrame: " + e);
				e.printStackTrace(System.err);
			}
		}
		// not synced
		if (lastFrame != null)
		{
			pushFrame(lastFrame);
		}
		return true;
	}

	/**
	 * handle reference frame
	 */
	public synchronized boolean handleReferenceFrame(final Frame frame)
	{
		if (stopped)
		{
			// die die die
			return false;
		}
		referenceFrame = frame;
		System.out.println("Reference frame set");
		return true;
	}

	/**
	 * Persistable - load
	 */
	@Override
	public synchronized void load(final File persistFile, final ContainerManager containerManager) throws IOException
	{

		if (persistFile != null)
		{
			final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(persistFile)));
			final String header = in.readLine();
			if (header == null || !header.equals(PERSIST_HEADER)) { throw new IOException(
					"File header mismatch: expected " + PERSIST_HEADER + ", read " + (header == null ? "null" : header)); }
			final String c = in.readLine();
			if (c == null) { throw new IOException("AbstractVideoProcessor could not read configuration line"); }

			System.out.println("Read persistent config: " + c);
			setConfiguration(c);

			in.close();
			this.persistFile = persistFile;
		}
	}

	/**
	 * persist - write configuration
	 */
	@Override
	public synchronized File persist(final ContainerManager containerManager) throws IOException
	{
		if (persistFile == null)
		{
			persistFile = File
					.createTempFile("AbstractVideoProcessor", ".txt",
									PersistenceManager.getPersistenceManager().COMPONENT_PERSISTENCE_DIRECTORY);
		}
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(persistFile)));
		try
		{
			out.println(PERSIST_HEADER);
			out.println(configuration);
			out.flush();
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		out.close();
		return persistFile;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	/**
	 * configuration setter
	 */
	public synchronized void setConfiguration(final String newtext)
	{
		final String oldtext = configuration;
		if (oldtext == newtext) { return; // prevent any looping
		}
		this.configuration = parseConfiguration(newtext);
		// update model
		configModel.setValue("configuration", this.configuration);

		propertyChangeListeners.firePropertyChange("configuration", oldtext, this.configuration);
	}

	/**
	 * set {@link SimpleFormModel}.
	 * 
	 * @return Confirmed/filtered model (else null)
	 */
	@Override
	public synchronized SimpleFormModel setModel(final SimpleFormModel model)
	{
		final String newtext = (String) model.getValue("configuration");
		System.out.println("Configuration set by setModel (" + newtext + ")");
		setConfiguration(newtext);
		return model;
	}

	/**
	 * source setter
	 */
	public synchronized void setReferenceSink(final PushFrameSource s)
	{
		System.err.println("Reference Sink Source set to " + s);
		try
		{
			System.out.println("Unregister old");
			if (referenceSink != null)
			{
				referenceSink.unregisterFrameHandler(this.referenceHandler);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR unregistering FrameHandler: " + e);
			e.printStackTrace(System.err);
		}
		try
		{
			System.out.println("Register new");
			this.referenceSink = s;
			if (referenceSink != null)
			{
				referenceSink.registerFrameHandler(this.referenceHandler);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR registering FrameHandler: " + e);
			e.printStackTrace(System.err);
		}

		propertyChangeListeners.firePropertyChange("referenceSink", null, s);
	}

	/**
	 * source setter
	 */
	public synchronized void setSink(final PushFrameSource s)
	{
		System.err.println("Sink Source set to " + s);
		// ....
		try
		{
			System.out.println("Unregister old");
			if (sink != null)
			{
				sink.unregisterFrameHandler(this);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR unregistering FrameHandler: " + e);
			e.printStackTrace(System.err);
		}
		try
		{
			System.out.println("Register new");
			this.sink = s;
			if (sink != null)
			{
				sink.registerFrameHandler(this);
			}
		}
		catch (final Exception e)
		{
			System.err.println("ERROR registering FrameHandler: " + e);
			e.printStackTrace(System.err);
		}

		propertyChangeListeners.firePropertyChange("sink", null, s);
	}

	/**
	 * stop
	 */
	public synchronized void stop()
	{
		System.err.println("Stop AbstractVideoProcessor component...");
		try
		{
			configServer.terminate();
		}
		catch (final Exception e)
		{
			System.err.println("Error stopping server: " + e);
		}
		// stop pulling
		System.err.println("Stopped");
		setSink(null);
		setReferenceSink(null);
		propertyChangeListeners.firePropertyChange("source", source, null);
		source = null;
		stopped = true;
	}

	/**
	 * check configuration/apply - do no overrider.
	 * 
	 * @functions is Vector of Vector, 1st el of each is function name, rest are args in order
	 * @return true if error
	 */
	protected synchronized boolean checkConfiguration(final Vector functions)
	{
		synchronized (frames)
		{
			// clear frames
			frames.removeAllElements();
		}
		processors = new Vector();
		for (int fi = 0; fi < functions.size(); fi++)
		{
			final Vector function = (Vector) functions.elementAt(fi);
			final FrameProcessor p = getProcessor((String) function.elementAt(0), function);
			if (p == null)
			{
				// unknown
				function.setElementAt("*ERROR*" + ((String) function.elementAt(0)), 0);
				return true;
			}
			processors.addElement(p);
		}
		System.out.println("Configured OK");
		return false;
	}

	/**
	 * get a processor given name and vector of (String) args (zeroeth el is name again); override!.
	 * 
	 * @return null on error
	 */
	protected FrameProcessor getProcessor(final String name, final Vector args)
	{
		return null;
	}

	/**
	 * parse configuration. Format := [ functions ] functions := function | functions ',' function
	 * function := NAME '(' [ args ] ')' args := arg | args ',' arg arg := [^,()]
	 * 
	 * @return parsed configuration (e.g. with in-lined errors)
	 */
	protected String parseConfiguration(final String text)
	{
		try
		{
			final StringTokenizer toks = new StringTokenizer(text, ",()", true);
			boolean error = false;
			// parsed = Vector of Vectors, 1 per function, 1st el of each is function name, then
			// args in order
			final Vector functions = new Vector();
			while (!error && toks.hasMoreTokens())
			{
				// function.NAME
				String tok = toks.nextToken().trim();
				if (tok.equals(",") || tok.equals("(") || tok.equals(")"))
				{
					// error
					System.err.println("Expected function name; found " + tok);
					error = true;
					break;
				}
				System.out.println("Function \"" + tok + "\"");
				final String fname = tok;
				final Vector function = new Vector();
				function.addElement(fname);
				functions.addElement(function);

				tok = toks.hasMoreTokens() ? toks.nextToken().trim() : null;
				if (tok == null || !tok.equals("("))
				{
					// error
					System.err.println("Expected ( after function " + fname + "; found " + (tok != null ? tok : "EOF"));
					error = true;
					break;
				}
				// args (optional)
				tok = toks.hasMoreTokens() ? toks.nextToken().trim() : null;
				if (tok == null)
				{
					// error
					System.err.println("Expected ) after function " + fname + " '('; found EOF");
					error = true;
					break;
				}
				while (!tok.equals(")"))
				{
					if (tok.equals(",") || tok.equals("("))
					{
						// error
						System.err.println("Expected function " + fname + " arg " + (function.size()) + " or ); found "
								+ tok);
						error = true;
						break;
					}
					// arg
					System.out.println("  arg " + (function.size()) + ": " + tok);
					function.addElement(tok);
					// ,)
					tok = toks.hasMoreTokens() ? toks.nextToken().trim() : null;
					if (tok != null && tok.equals(")"))
					{
						break;
					}
					if (tok == null || !tok.equals(","))
					{
						// error
						System.err.println("Expected ) or , after function " + fname + " arg " + (function.size() - 1)
								+ "; found " + (tok != null ? tok : "EOF"));
						error = true;
						break;
					}
					tok = toks.hasMoreTokens() ? toks.nextToken().trim() : null;
					if (tok == null)
					{
						// error
						System.err.println("Expected function " + fname + " arg " + function.size()
								+ " after ','; found EOF");
						error = true;
						break;
					}
				}
				// , or end
				tok = toks.hasMoreTokens() ? toks.nextToken().trim() : null;
				if (tok == null)
				{
					// done
					break;
				}
				if (!tok.equals(","))
				{
					// error
					System.err.println("Expected , or EOF after function " + fname + "(); found " + tok);
					error = true;
					break;
				}
			}
			if (!error)
			{
				error = checkConfiguration(functions);
			}

			// convert back to string
			final StringBuffer b = new StringBuffer();
			for (int fi = 0; fi < functions.size(); fi++)
			{
				final Vector function = (Vector) functions.elementAt(fi);
				b.append(function.elementAt(0));
				b.append('(');
				for (int ai = 1; ai < function.size(); ai++)
				{
					b.append(function.elementAt(ai));
					if (ai + 1 < function.size())
					{
						b.append(',');
					}
				}
				b.append(')');
				if (fi + 1 < functions.size())
				{
					b.append(',');
				}
			}
			if (error)
			{
				b.append("*ERROR*");
				while (toks.hasMoreTokens())
				{
					b.append(toks.nextToken());
				}
			}
			return b.toString();
		}
		catch (final Exception e)
		{
			System.err.println("ERROR parsing configuration \"" + text + "\": " + e);
			e.printStackTrace(System.err);
		}
		return text;
	}

	/**
	 * parse args String Vector to float array of given size
	 */
	protected float[] parseFloatArgs(final Vector args, final int num)
	{
		if (args.size() > num + 1)
		{
			args.setElementAt("*TOO*MANY*ARGS*" + ((String) args.elementAt(num + 1)), num + 1);
			return null;
		}
		else if (args.size() < num + 1)
		{
			args.addElement("*TOO*FEW*ARGS*");
			return null;
		}
		final float vals[] = new float[num];
		for (int i = 0; i < num; i++)
		{
			try
			{
				vals[i] = new Float((String) args.elementAt(i + 1)).floatValue();
			}
			catch (final Exception e)
			{
				args.setElementAt("*ERROR*" + ((String) args.elementAt(i + 1)), i + 1);
				System.err.println("Error parsing int " + args.elementAt(i + 1) + ": " + e);
				return null;
			}
		}
		return vals;
	}

	/**
	 * parse args String Vector to int array of given size
	 */
	protected int[] parseIntArgs(final Vector args, final int num)
	{
		if (args.size() > num + 1)
		{
			args.setElementAt("*TOO*MANY*ARGS*" + ((String) args.elementAt(num + 1)), num + 1);
			return null;
		}
		else if (args.size() < num + 1)
		{
			args.addElement("*TOO*FEW*ARGS*");
			return null;
		}
		final int vals[] = new int[num];
		for (int i = 0; i < num; i++)
		{
			try
			{
				vals[i] = new Integer((String) args.elementAt(i + 1)).intValue();
			}
			catch (final Exception e)
			{
				args.setElementAt("*ERROR*" + ((String) args.elementAt(i + 1)), i + 1);
				System.err.println("Error parsing int " + args.elementAt(i + 1) + ": " + e);
				return null;
			}
		}
		return vals;
	}

	/**
	 * parse args String Vector to string array of given size
	 */
	protected String[] parseStringArgs(final Vector args, final int num)
	{
		if (args.size() > num + 1)
		{
			args.setElementAt("*TOO*MANY*ARGS*" + ((String) args.elementAt(num + 1)), num + 1);
			return null;
		}
		else if (args.size() < num + 1)
		{
			args.addElement("*TOO*FEW*ARGS*");
			return null;
		}
		final String vals[] = new String[num];
		for (int i = 0; i < num; i++)
		{
			try
			{
				vals[i] = (String) args.elementAt(i + 1);
			}
			catch (final Exception e)
			{
				args.setElementAt("*ERROR*" + ((String) args.elementAt(i + 1)), i + 1);
				System.err.println("Error parsing int " + args.elementAt(i + 1) + ": " + e);
				return null;
			}
		}
		return vals;
	}

	/**
	 * a new frame
	 */
	protected void pushFrame(final Frame frame)
	{
		if (debug)
		{
			System.out.println("AbstractVideoProcessor.pushFrame...");
		}
		FrameHandler hs[] = null;
		synchronized (this)
		{
			hs = (FrameHandler[]) handlers.toArray(new FrameHandler[handlers.size()]);
		}
		// pass on to registered handlers
		// synchronized(handlers)
		{
			for (int ihandler = 0; ihandler < hs.length; ihandler++)
			{
				final FrameHandler handler = hs[ihandler];
				boolean retain = false;
				try
				{
					if (debug)
					{
						System.out.println("  push to handler " + ihandler + " " + handler);
					}
					// no lock during actual handling chain
					retain = handler.handleFrame(frame);
					if (retain == false)
					{
						System.out.println("NOTE: frame handler " + handler + " requests removal");
					}
				}
				catch (final Exception e)
				{
					System.err.println("ERROR in frame handler: " + e);
					e.printStackTrace(System.err);
					retain = false;
				}
				if (!retain)
				{
					synchronized (this)
					{
						handlers.removeElement(handler);
					}
				}
			}
		}
	}
}
