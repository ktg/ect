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

  Created by: Alastair Hampshire (University of Nottingham)
  Contributors:
  Alastair Hampshire (University of Nottingham)

 */
package equip.ect.components.arauthoring.scenegraph;

import com.sun.j3d.loaders.Loader;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.utils.universe.SimpleUniverse;
import equip.ect.components.visionframework.common.FrameHandler;
import equip.ect.components.visionframework.common.PushFrameSource;
import equip.ect.components.visionframework.common.UnsupportedFormatException;
import org.web3d.j3d.loaders.X3DLoader;

import javax.media.j3d.*;
import javax.vecmath.Point3d;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @displayScene Graph Base
 * @classification
 * @preferred
 */

/**
 * Loads and displays a 3D scene<br>
 * <H3>Summary</H3> This component will load and display a scene graph as a 3D scene.<br>
 * <H3>Description</H3> This component will load X3D files (similar to VRML).<br>
 * Subcomponents will be created to allow interaction with specified points in the scene graph.<br>
 * Supported scene graph nodes:<br>
 * - switch: allows an object in the scene to be set as visible/ invisible<br>
 * - transformation: allows an object in the scene to be tranformed according to a specified
 * transformation matrix<br>
 * <br>
 * This component also allows a video feed to be rendered onto the background of the 3D scene.<br>
 * <H3>Configuration</H3> Load an X3D file by specifying the filename and path in the property
 * 'x3dFilename'.<br>
 * This will load the selected file and create a 3D output window named \"3D output\".<br>
 * A sub-component will be created from each node in the scene graph which has been noted as of
 * interest.<br>
 * These subcomponents allow modification of the scene, e.g. transforming (scaling, rotating and
 * moving) objects<br>
 * (Note: see 'Technical Details' for a description of how to define scene graph nodes as nodes of
 * interest.)<br>
 * <br>
 * Currently 2 node types are supported:<br>
 * - SceneGraphSwitch<br>
 * - SceneGraphTransformation<br>
 * <H3>Usage</H3> Currently, 3 components are used to interact with a 3d scene. The '3D Scene Base'
 * (this component) and <br>
 * its 2 children 'SceneGraphSwitch' and 'SceneGraphTransformation'<br>
 * - SceneGraphSwitch: this is used to set an object as visible or invisible<br>
 * The 'name' property identifies the entry point into the scene graph<br>
 * The 'visible' property can be set to true/ false to make the specified object visible/ invisible<br>
 * - SceneGraphTransformation: this is used to rotate, move and scale and object<br>
 * The 'name' property identifies the entry point into the scene graph<br>
 * The 'transformationMatrix' property can be set to rotate, move and scale the specified 3d object<br>
 * 'transformationMatrix' takes a 4x4 vector specified as a space seperated list of 16 numbers in
 * column major order<br>
 * A number of utility components exist which can generate these transformation matrices <br>
 * - 3D Scene Base: used to load scene file (see configuration) and set video feed as background.<br>
 * The 'sink' property can be connected to a media source (e.g. JMFVideoCaptureDevice).<br>
 * The specified video feed will be rendered onto the background of the 3d scene<br>
 * For example: Create a JMFVideoCaptureManager component. This will create a JMFVideoCaptureDevice
 * subcomponent for all available cameras.<br>
 * Now connect the 'JMFVideoCaptureDevice' component's 'source' property to the '3D Scene Base'
 * components 'sink' property.<br>
 * A video feed should appear rendered onto the background of the 3d scene<br>
 * <H3>Technical Details</H3> To specify that a particular node in the scene graph is of particular
 * interest, set the name of that node to \"ECT_<some name>\"<br>
 * e.g. using DEF=\"ECT_<some_name>\" in the X3D file.<br>
 * The 'SceneGraphBase' component will create a subcomponent for the given node with the name
 * <some_name>.<br>
 * It will be helpful to make <some_name> descriptive to ease identification. <br>
 * Note: currently only switch and transformation nodes are supported.<br>
 *
 * @display 3D Scene Base
 * @classification Media/3D/Output
 */
public class SceneGraphBase implements Serializable, FrameHandler // , GlyphTransformHandler //,
		// SceneGraphParent // extends
		// java.awt.Frame
{
	// Vector containing SceneGraphEntities
	public Vector children = new Vector();
	protected String x3dFilename = "";
	protected boolean alwaysOnTop = false;
	/**
	 * source
	 */
	protected PushFrameSource sink = null;
	// private Canvas3D canvas = null;
	protected String attention = "";
	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
	// private Appearance app = null;
	// private Texture2D tex = null;
	private BranchGroup objRoot = null;
	private Background background = null;
	private SimpleUniverse universe = null;
	private java.awt.Frame frame = null;
	private AffineTransformOp atop = null;
	private float xScale = -1;
	private float yScale = -1;
	private int width = -1;
	private int height = -1;

	/**
	 * no-args constructor (required)
	 */
	public SceneGraphBase()
	{
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	public synchronized boolean getAlwaysOnTop()
	{
		return alwaysOnTop;
	}

	public synchronized void setAlwaysOnTop(final boolean onTop)
	{
		final boolean oldAlwaysOnTop = alwaysOnTop;
		alwaysOnTop = onTop;

		if (frame != null)
		{
			frame.setAlwaysOnTop(alwaysOnTop);
		}

		propertyChangeListeners.firePropertyChange("alwaysOnTop", oldAlwaysOnTop, this.alwaysOnTop);
	}

	public synchronized String getAttention()
	{
		return attention;
	}

	public synchronized void setAttention(final String newAtt)
	{
		final String oldAtt = this.attention;
		this.attention = newAtt;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("attention", oldAtt, this.attention);
	}

	public SceneGraphObject[] getChildren()
	{
		synchronized (this)
		{
			return (SceneGraphObject[]) children.toArray(new SceneGraphObject[children.size()]);
		}
	}

	/**
	 * source getter
	 */
	public synchronized PushFrameSource getSink()
	{
		return sink;
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

	public synchronized String getX3dFilename()
	{
		return x3dFilename;
	}

	public synchronized void setX3dFilename(final String filename)
	{
		final String oldFilename = this.x3dFilename;
		this.x3dFilename = filename;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("x3dFilename", oldFilename, this.x3dFilename);

		loadX3dFile();
	}

	@Override
	public boolean handleFrame(final ect.components.visionframework.common.Frame frame)
	{
		/*
		 * if (app == null) { //System.out.println("Cannot display image frame. No object loaded.");
		 * return true; }
		 */

		if (background == null)
		{
			// System.out.println("Cannot display image frame. No object loaded.");
			return true;
		}

		// get the buffered image
		try
		{
			BufferedImage bimg = frame.getBufferedImage();

			// resize to fit on a texture
			if (xScale == -1 || yScale == -1)
			{
				final int xmax = bimg.getWidth();
				final int ymax = bimg.getHeight();
				width = getClosestPowerOf2(xmax);
				height = getClosestPowerOf2(ymax);
				xScale = (float) width / (float) xmax;
				yScale = (float) height / (float) ymax;

				// canvas.setSize(width, height);
			}

			// scale if scales aren't 1.0
			// BufferedImage scaledImg = null;
			if (!(xScale == 1.0f && yScale == 1.0f))
			{
				// System.out.println("Resizing background image...");
				// BufferedImage origImg = (BufferedImage) bimg.getImage();
				if (atop == null)
				{
					final AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
					atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				}
				final java.awt.image.Raster raster = atop.filter(bimg.getRaster(), null);

				// BufferedImage scaledImg = new BufferedImage(width, height, bimg.getType());
				// scaledImg.setData(raster);
				bimg = new BufferedImage(width, height, bimg.getType());
				bimg.setData(raster);
			}

			final ImageComponent2D imgcmp = new ImageComponent2D(ImageComponent.FORMAT_RGBA, bimg, false, false); // false,
			// false
			background.setImage(imgcmp);

			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, width, height);
			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, bimg.getWidth(),
			// bimg.getHeight());

			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, width, height);
			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, bimg.getWidth(),
			// bimg.getHeight());
			// tex.setMagFilter(Texture.FASTEST);
			// tex.setMinFilter(Texture.FASTEST);

			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, xmax, ymax);

			/*
			 * ImageComponent2D imgcmp = new ImageComponent2D(ImageComponent.FORMAT_RGB, width,
			 * height); //imgcmp.set(new BufferedImage(width, height, ImageComponent.FORMAT_RGB));
			 * BufferedImage newBimg = new BufferedImage(width, height, ImageComponent.FORMAT_RGB);
			 * WritableRaster raster = newBimg.getRaster(); raster.setDataElements(0, 0,
			 * bimg.getData()); //System.out.println("Img Comp: " + imgcmp.getType());
			 * //imgcmp.setCapability(ImageComponent.ALLOW_IMAGE_WRITE);
			 * //imgcmp.setSubImage((RenderedImage) bimg, bimg.getWidth(), bimg.getHeight(), 0, 0,
			 * 0, 0); imgcmp.set(newBimg);
			 *
			 * tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGBA, width, height);
			 * //bimg.getWidth(), bimg.getHeight());
			 */

			// tex.setImage(0, imgcmp);
			// app.setTexture(tex);

			return true;
		}
		catch (final UnsupportedFormatException ufe)
		{
			System.out.println("Image format not supported: " + ufe.getMessage());

			return false;
		}
	}

	public void loadX3dFile()
	{
		// try to load the scene
		Scene scene = null;
		try
		{
			final Loader ldr = new X3DLoader(Loader.LOAD_ALL);
			scene = ldr.load(x3dFilename);
		}
		catch (final java.io.FileNotFoundException fnfe)
		{
			setAttention("Could not find file: " + fnfe.getMessage());
			return;
		}

		// looks good, so close any existing windows and destory existing scene graph objects
		if (universe != null)
		{
			universe.cleanup();
		}
		final SceneGraphObject[] oldValue = getChildren();
		children.removeAllElements();
		final SceneGraphObject[] newValue = getChildren();

		propertyChangeListeners.firePropertyChange("children", oldValue, newValue);
		if (frame != null)
		{
			frame.setVisible(false);
		}

		frame = new java.awt.Frame();

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final GraphicsDevice gs = ge.getDefaultScreenDevice();
		final GraphicsConfigTemplate3D devconfig = new GraphicsConfigTemplate3D();
		final GraphicsConfiguration config = gs.getBestConfiguration(devconfig);
		// Canvas3D aCanvas3D = new Canvas3D(config);

		// GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		final Canvas3D canvas = new Canvas3D(config);
		frame.add("Center", canvas);

		universe = new SimpleUniverse(canvas);

		final View view = canvas.getView();
		view.setBackClipDistance(500);
		view.setMinimumFrameCycleTime(40);

		objRoot = new BranchGroup();
		// objRoot.setCapability(Group.ALLOW_CHILDREN_EXTEND);

		/*
		 * // add a box to paint the background onto app = new Appearance();
		 * app.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
		 *
		 * TextureAttributes texAttr = new TextureAttributes();
		 * //texAttr.setTextureMode(TextureAttributes.MODULATE);
		 * texAttr.setTextureMode(TextureAttributes.REPLACE); app.setTextureAttributes(texAttr);
		 * TransformGroup backgroundTrans = new TransformGroup();
		 * //backgroundTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		 * //backgroundTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		 * backgroundTrans.setTransform(new Transform3D( new double[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0,
		 * 1, -2500, 0, 0, 0, 1} )); Box box = new Box(1000f, 750f, 1f, Box.GENERATE_TEXTURE_COORDS,
		 * app); backgroundTrans.addChild(box); objRoot.addChild(backgroundTrans);
		 */

		final BoundingSphere worldBounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
		background = new Background();
		background.setCapability(Background.ALLOW_IMAGE_WRITE);
		background.setImageScaleMode(Background.SCALE_FIT_ALL);
		background.setApplicationBounds(worldBounds);
		objRoot.addChild(background);

		objRoot.compile();
		universe.addBranchGraph(objRoot);

		frame.setSize(640, 480);
		frame.setTitle("3D output");
		frame.setAlwaysOnTop(alwaysOnTop);
		frame.setVisible(true);

		final BranchGroup loadedObjRoot = scene.getSceneGroup();

		// get out all the named objects that start with ECT - that's what we're interestted in.
		final Map<String, Object> named = scene.getNamedObjects();
		final Map<String, Object> objects = new HashMap<String, Object>();

		for (String key: named.keySet())
		{
			if (!key.startsWith("ECT_"))
			{
				continue;
			}

			final StringTokenizer keyTokens = new StringTokenizer(key, "_");
			if (keyTokens.countTokens() != 3)
			{
				/*
				 * Support the old way of doing things, i.e. create a seperate component for each
				 * scene graph node
				 *
				 * String name = key.substring(4, key.length());
				 *
				 * Object namedObj = named.get(key);
				 *
				 * //System.out.println("Node type: " + namedObjChild.getClass().getName());
				 *
				 *
				 * if (!(namedObj instanceof SharedGroup)) { continue; }
				 *
				 * if (((SharedGroup) namedObj).numChildren() <= 0) { continue; }
				 *
				 *
				 * Object namedObjChild = ((SharedGroup) namedObj).getChild(0);
				 *
				 *
				 * if (namedObjChild instanceof TransformGroup) { ((TransformGroup)
				 * namedObjChild).setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				 * SceneGraphTransformation sceneGraphTrans = new
				 * SceneGraphTransformation((TransformGroup) namedObjChild, name);
				 * //addChild(sceneGraphTrans); } else if (namedObjChild instanceof Switch) {
				 * ((Switch) namedObjChild).setCapability(Switch.ALLOW_SWITCH_WRITE);
				 * SceneGraphSwitch sceneGraphSwitch = new SceneGraphSwitch((Switch) namedObjChild,
				 * name); //addChild(sceneGraphSwitch); }
				 */
			}
			else
			{
				/*
				 * The new way of doing things. Create 1 component per object to be manipulated 1
				 * object contains all the nodes intended to manipulate that object the Naming scene
				 * in the scene graph is "ECT_<object name>_<node name>"
				 */
				keyTokens.nextToken();
				final String objectName = keyTokens.nextToken();
				final String nodeName = keyTokens.nextToken();

				Vector objectNodes = (Vector) objects.get(objectName);
				if (objectNodes == null)
				{
					objectNodes = new Vector();
					objects.put(objectName, objectNodes);
				}

				final Object namedObj = named.get(key);
				if (namedObj instanceof Appearance)
				{
					objectNodes.add(namedObj);
					continue;
				}

				if (!(namedObj instanceof SharedGroup))
				{
					continue;
				}

				if (((SharedGroup) namedObj).numChildren() <= 0)
				{
					continue;
				}

				final Object namedObjChild = ((SharedGroup) namedObj).getChild(0);

				objectNodes.add(namedObjChild);
			}
		}

		for (String name: objects.keySet())
		{
			final SceneGraphObject sceneGraphObject = new SceneGraphObject();
			sceneGraphObject.setName(name);

			final Vector objectNodes = (Vector) objects.get(name);
			for (int d = 0; d < objectNodes.size(); d++)
			{
				final Object objectNode = objectNodes.elementAt(d);
				if (objectNode instanceof TransformGroup)
				{
					((TransformGroup) objectNode).setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
					sceneGraphObject.addTransformationNode((TransformGroup) objectNode);
					sceneGraphObject.addType(SceneGraphObject.TRANSFORMATION_TYPE_NAME);
				}
				else if (objectNode instanceof Switch)
				{
					((Switch) objectNode).setCapability(Switch.ALLOW_SWITCH_WRITE);
					sceneGraphObject.addSwitchNode((Switch) objectNode);
					sceneGraphObject.addType(SceneGraphObject.SWITCH_TYPE_NAME);
				}
				else if (objectNode instanceof Appearance)
				{
					((Appearance) objectNode).setCapability(Appearance.ALLOW_TEXTURE_WRITE);
					sceneGraphObject.addAppearanceNode((Appearance) objectNode);
					sceneGraphObject.addType(SceneGraphObject.APPEARANCE_TYPE_NAME);
				}
			}

			addChild(sceneGraphObject);
		}

		for(String key: named.keySet())
		{
			if (!key.startsWith("ECT_"))
			{
				continue;
			}

			final Object namedObj = named.get(key);

			if (namedObj instanceof SharedGroup)
			{
				((SharedGroup) namedObj).compile();
			}
		}

		// Have Java 3D perform optimizations on this scene graph.
		loadedObjRoot.compile();

		// if (universe != null) {
		universe.getLocale().addBranchGraph(loadedObjRoot);
		// }
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void stop()
	{
		System.out.println("Stopping 3d scene...");
		final SceneGraphObject[] oldValue = getChildren();
		children.removeAllElements();
		final SceneGraphObject[] newValue = getChildren();

		propertyChangeListeners.firePropertyChange("children", oldValue, newValue);
		// universe.getLocale().finalize();
		if (universe != null)
		{
			universe.cleanup();
		}
		if (frame != null)
		{
			frame.setVisible(false);
		}
		// dispose();
		System.out.println("Done.");
	}

	protected void addChild(final SceneGraphObject sceneGraphObject)
	{

		System.out.println("adding child...");

		SceneGraphObject[] oldValue = null;
		SceneGraphObject[] newValue = null;

		final boolean done = false;

		synchronized (this)
		{
			oldValue = getChildren();

			try
			{
				children.addElement(sceneGraphObject);

				newValue = getChildren();

				propertyChangeListeners.firePropertyChange("children", oldValue, newValue);

			}
			catch (final Exception e)
			{
				System.err.println("ERROR creating child: " + e);
				e.printStackTrace(System.err);
			}
		}
	}

	private int getClosestPowerOf2(final int value)
	{
		if (value < 1)
		{
			return value;
		}

		int powerValue = 1;
		for (; ; )
		{
			powerValue *= 2;
			if (value < powerValue)
			{
				// Found max bound of power, determine which is closest

				final int minBound = powerValue / 2;
				if ((powerValue - value) > (value - minBound))
				{
					return minBound;
				}
				else
				{
					return powerValue;
				}
			}
		}
	}
}
