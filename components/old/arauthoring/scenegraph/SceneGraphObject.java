package equip.ect.components.arauthoring.scenegraph;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.j3d.Appearance;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Switch;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import equip.data.GUID;
import equip.data.beans.DataspaceBean;
import equip.ect.ContainerManager;
import equip.ect.DynamicProperties;
import equip.ect.DynamicPropertiesSupport;
import equip.ect.DynamicPropertyDescriptor;
import equip.ect.IActiveComponent;
import equip.ect.NoSuchPropertyException;

/*
 * @author Alastair Hampshire
 * 
 * This component is designed to represent one or more scene graph nodes
 * For neatness and to reduce the number of components it will combine
 * e.g. a switch node and a transformation node into one component
 * 
 * Currently supported nodes:
 * - transformation node
 * - switch node (currently used just to set a single subgraph as either visible or invisible
 * 
 * Note: for transformation nodes, this component provides both a transformation matrix input
 * and a set of scalar rotation, scale and translation values for the x, y and z axis.
 * This is a simplification that allows a marker to place an object and then input deivces to manipulate
 * the object. The alternative would be to use the translation|scale|rotation components together with the
 * matrix multiplication component, but this produces much more complex grapghs.
 * The order of manipulation is:
 * - transformation matrix
 * - rotation
 * - translation
 * - scale
 * 
 * Note: this class really needs to be split into several seperate classes (currently 3): one
 * for each scene graph node type supported. This would make it easier to add support for
 * new scene graph nodes. I have an idea about how this should be done, but haven't yet had time
 * to code it...
 */
public class SceneGraphObject implements Serializable, DynamicProperties, IActiveComponent
{

	/*
	 * a set of properties that tell this component what properties, default values and types to use
	 * for each scene graph node type the component supports
	 */
	// switch
	public static final String SWITCH_TYPE_NAME = "switch";
	private static final String VISIBLE_PROPERTY_NAME = "visible";
	private static final String[] SWITCH_TYPE_PROPERTY_NAMES = new String[] { "visible" };
	private static final Class[] SWITCH_TYPE_PROPERTY_TYPES = new Class[] { String.class };
	private static final String[] SWITCH_TYPE_PROPERTY_VALUES = new String[] { new String("false") };

	// transformation
	public static final String TRANSFORMATION_TYPE_NAME = "transformation";
	private static final String[] TRANSFORMATION_TYPE_PROPERTY_NAMES = new String[] { "transformationMatrix",
																						"rotateX", "rotateY",
																						"rotateZ", "scaleX", "scaleY",
																						"scaleZ", "translateX",
																						"translateY", "translateZ",
																						"authoringRotateX",
																						"authoringRotateY",
																						"authoringRotateZ",
																						"authoringScaleX",
																						"authoringScaleY",
																						"authoringScaleZ",
																						"authoringTranslateX",
																						"authoringTranslateY",
																						"authoringTranslateZ" };
	private static final Class[] TRANSFORMATION_TYPE_PROPERTY_TYPES = new Class[] { double[].class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class, Double.class,
																					Double.class };
	private static final Object[] TRANSFORMATION_TYPE_PROPERTY_VALUES = new Object[] { null, null, null, null, null,
																						null, null, null, null, null,
																						null, null, null, null, null,
																						null, null, null, null };

	// appearance
	public static final String APPEARANCE_TYPE_NAME = "appearance";
	private static final String[] APPEARANCE_TYPE_PROPERTY_NAMES = new String[] { "textureImageFilename" };
	private static final Class[] APPEARANCE_TYPE_PROPERTY_TYPES = new Class[] { String.class };
	private static final Object[] APPEARANCE_TYPE_PROPERTY_VALUES = new Object[] { new String("") };

	// the set of scene graph nodes this component can operate on
	private Switch switchObj = null;
	private TransformGroup objTrans = null;
	private Appearance objAppearance = null;

	// transformation information

	// this is the standard transform matrix that can be used to transform the object in any way
	private Transform3D matrixTransformation = null;

	// this is a special authoring transformation matrix that can be used to nudge or adjust
	// how the object appears. Whilst it can be set at runtime, it is intended to be used
	// at design/ authoring time. Because the intention is to set this at authoring and
	// therefore this should not change at operation time, the whole matrix is calculated
	// whenever one of the components (rotate, scale, translate in x, y, z axis) is set.
	// The hashtable is used to remember the authoring transformation values.
	private Transform3D authoringTransformationMatrix = null;
	private Hashtable authroningTransformationValues = new Hashtable();

	// This is a set of transformation that are intended to be used when the system is in operation
	// This functionality could all be achieved using the maxtrix transformation above, however
	// this is a simplification that reduces component complexity when e.g. using a slider to rotate
	// an object
	private Transform3D rotateXTrans = null;
	private Transform3D rotateYTrans = null;
	private Transform3D rotateZTrans = null;

	private Transform3D scaleXTrans = null;
	private Transform3D scaleYTrans = null;
	private Transform3D scaleZTrans = null;

	private Transform3D translateXTrans = null;
	private Transform3D translateYTrans = null;
	private Transform3D translateZTrans = null;

	/*
	 * The type property is a comma seperated list of scene graph nodes supported by this component
	 */
	protected Vector type = new Vector();
	/*
	 * Support for dynamic properties
	 */
	protected DynamicPropertiesSupport dynsup;
	protected String name = "";

	/**
	 * Property Change support
	 */
	protected transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);

	public SceneGraphObject()
	{
		dynsup = new DynamicPropertiesSupport(propertyChangeListeners);
	}

	public void addAppearanceNode(final Appearance appearance)
	{
		objAppearance = appearance;
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void addPropertyChangeListener(final PropertyChangeListener l)
	{
		// super.addPropertyChangeListener(l);
		propertyChangeListeners.addPropertyChangeListener(l);
	}

	/*
	 * Called by SceneGraphBase
	 */
	public void addSwitchNode(final Switch newSwitch)
	{
		switchObj = newSwitch;
	}

	public void addTransformationNode(final TransformGroup newTransformation)
	{
		objTrans = newTransformation;

		matrixTransformation = new Transform3D();

		objTrans.setTransform(matrixTransformation);
	}

	public void addType(final String typeName)
	{
		System.out.println("add type...");
		type.add(typeName);
		if (typeName.equals(SWITCH_TYPE_NAME))
		{
			for (int a = 0; a < SWITCH_TYPE_PROPERTY_NAMES.length; a++)
			{
				dynsup.addProperty(	SWITCH_TYPE_PROPERTY_NAMES[a], SWITCH_TYPE_PROPERTY_TYPES[a],
									SWITCH_TYPE_PROPERTY_VALUES[a]);
				performFunction(SWITCH_TYPE_PROPERTY_NAMES[a], SWITCH_TYPE_PROPERTY_VALUES[a]);
			}
		}
		else if (typeName.equals(TRANSFORMATION_TYPE_NAME))
		{
			for (int a = 0; a < TRANSFORMATION_TYPE_PROPERTY_NAMES.length; a++)
			{
				dynsup.addProperty(	TRANSFORMATION_TYPE_PROPERTY_NAMES[a], TRANSFORMATION_TYPE_PROPERTY_TYPES[a],
									TRANSFORMATION_TYPE_PROPERTY_VALUES[a]);
			}
		}
		else if (typeName.equals(APPEARANCE_TYPE_NAME))
		{
			for (int a = 0; a < APPEARANCE_TYPE_PROPERTY_NAMES.length; a++)
			{
				dynsup.addProperty(	APPEARANCE_TYPE_PROPERTY_NAMES[a], APPEARANCE_TYPE_PROPERTY_TYPES[a],
									APPEARANCE_TYPE_PROPERTY_VALUES[a]);
			}
		}
	}

	@Override
	public DynamicPropertyDescriptor[] dynGetProperties()
	{
		return dynsup.dynGetProperties();
	}

	@Override
	public Object dynGetProperty(final String name) throws NoSuchPropertyException
	{
		return dynsup.dynGetProperty(name);
	}

	@Override
	public void dynSetProperty(final String name, final Object value) throws NoSuchPropertyException
	{
		dynsup.dynSetProperty(name, value);

		performFunction(name, value);
	}

	public synchronized String getName()
	{
		return name;
	}

	public String getPersistentChild()
	{
		return getName();
	}

	public String getType()
	{
		String returnType = null;

		for (int a = 0; a < type.size(); a++)
		{
			if (returnType == null)
			{
				returnType = (String) type.elementAt(a);
			}
			else
			{
				returnType += "," + (String) type.elementAt(a);
			}
		}

		return returnType;
	}

	@Override
	public void initialise(final ContainerManager cmgr, final DataspaceBean dataspace)
	{
	}

	@Override
	public void linkToAdded(final String propertyName, final GUID requestId)
	{
	}

	@Override
	public void linkToDeleted(final String propertyName, final GUID requestId)
	{
		System.out.println("link to deleted");

		if (propertyName.equals(VISIBLE_PROPERTY_NAME))
		{

			try
			{
				dynSetProperty(VISIBLE_PROPERTY_NAME, "false");
			}
			catch (final NoSuchPropertyException e)
			{
				// do nothing
			}
		}

	}

	@Override
	public boolean linkToUpdated(final String propertyName, final GUID requestId, final Object value)
	{
		// false has to be returned otherwise ECT gets confused. Not sure why
		return false;
	}

	/*
	 * This is where all the scene graph functionality is kept Currently supports transformation and
	 * switch (as set visible)
	 */
	public void performFunction(final String name, final Object value)
	{

		if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[0]))
		{
			/*
			 * perform transformation
			 */
			// check we have something to scale first
			if (objTrans == null)
			{
				System.out.println("No trans object to transfrom...");
				return;
			}

			if (!(value instanceof double[]))
			{
				System.out.println("the transfromation matrix value is not a double array...");
				return;
			}

			if (value == null)
			{
				System.out.println("Transformation matrix is null...");
				return;
			}

			final double[] glyphTransform = (double[]) value;

			if (glyphTransform.length < 16) { return; }

			final double[] matrix = new double[16];

			// we need to reverse y and z because Java3D uses a different co-ordinate system
			// we also need to convert from column major (ARtoolkit/ opengl form) to row major (java
			// 3d form)
			matrix[0] = glyphTransform[0];
			matrix[4] = -glyphTransform[1];
			matrix[8] = -glyphTransform[2];
			matrix[12] = glyphTransform[3];

			matrix[1] = glyphTransform[4];
			matrix[5] = -glyphTransform[5];
			matrix[9] = -glyphTransform[6];
			matrix[13] = glyphTransform[7];

			matrix[2] = glyphTransform[8];
			matrix[6] = -glyphTransform[9];
			matrix[10] = -glyphTransform[10];
			matrix[14] = glyphTransform[11];

			matrix[3] = glyphTransform[12];
			matrix[7] = -glyphTransform[13];
			matrix[11] = -glyphTransform[14];
			matrix[15] = glyphTransform[15];

			matrixTransformation.set(matrix);
			// do the transformation
			// Transform3D matrixTransformation = new Transform3D(matrix);

			transformObject();
		}
		else if (name.startsWith("rotate"))
		{
			/*
			 * rotation
			 */
			if (objTrans == null)
			{
				System.out.println("No trans object to transfrom...");
				return;
			}

			if (!(value instanceof Double))
			{
				System.out.println("Rotation value is not a double...");

				return;
			}

			if (value == null)
			{
				System.out.println("Rotation value is null...");
				return;
			}

			if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[1]))
			{
				rotateXTrans = new Transform3D();
				rotateXTrans.rotX(Math.PI * 2 * ((Double) value).doubleValue());

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[2]))
			{
				rotateYTrans = new Transform3D();
				rotateYTrans.rotY(Math.PI * 2 * ((Double) value).doubleValue());

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[3]))
			{
				rotateZTrans = new Transform3D();
				rotateZTrans.rotZ(Math.PI * 2 * ((Double) value).doubleValue());

				transformObject();
			}
		}
		else if (name.startsWith("scale"))
		{
			/*
			 * scale
			 */
			if (objTrans == null)
			{
				System.out.println("No trans object to transfrom...");
				return;
			}

			if (!(value instanceof Double))
			{
				System.out.println("Scale value is not a double...");

				return;
			}

			if (value == null)
			{
				System.out.println("Scale value is null...");
				return;
			}

			if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[4]))
			{
				scaleXTrans = new Transform3D();

				final Vector3d vector = new Vector3d(((Double) value).doubleValue(), 1, 1);
				scaleXTrans.setScale(vector);

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[5]))
			{
				scaleYTrans = new Transform3D();
				final Vector3d vector = new Vector3d(1, ((Double) value).doubleValue(), 1);
				scaleYTrans.setScale(vector);

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[6]))
			{
				scaleZTrans = new Transform3D();
				final Vector3d vector = new Vector3d(1, 1, ((Double) value).doubleValue());
				scaleZTrans.setScale(vector);

				transformObject();
			}
		}
		else if (name.startsWith("translate"))
		{
			/*
			 * translate
			 */
			if (objTrans == null)
			{
				System.out.println("No trans object to transfrom...");
				return;
			}

			if (!(value instanceof Double))
			{
				System.out.println("Translate value is not a double...");

				return;
			}

			if (value == null)
			{
				System.out.println("Translate value is null...");
				return;
			}

			if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[7]))
			{
				translateXTrans = new Transform3D();

				final Vector3d vector = new Vector3d(((Double) value).doubleValue(), 1, 1);
				translateXTrans.setTranslation(vector);

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[8]))
			{
				translateYTrans = new Transform3D();
				final Vector3d vector = new Vector3d(1, ((Double) value).doubleValue(), 1);
				translateYTrans.setTranslation(vector);

				transformObject();
			}
			else if (name.equals(TRANSFORMATION_TYPE_PROPERTY_NAMES[9]))
			{
				translateZTrans = new Transform3D();
				final Vector3d vector = new Vector3d(1, 1, ((Double) value).doubleValue());
				translateZTrans.setTranslation(vector);

				transformObject();
			}
		}
		// sets the object as visible/ not visible
		else if (name.equals(SWITCH_TYPE_PROPERTY_NAMES[0]))
		{
			/*
			 * make the object visible/ invisible
			 */
			if (switchObj == null) { return; }

			if (!(value instanceof String))
			{
				System.out.println("Switch value not String");
				return;
			}

			final String valueStr = (String) value;

			if (valueStr.equals("true"))
			{
				// System.out.println("Setting as visible");
				switchObj.setWhichChild(Switch.CHILD_ALL);
			}
			else
			{
				switchObj.setWhichChild(Switch.CHILD_NONE);
			}
		}
		// sets the filename of a texture to apply to a texture node
		else if (name.equals(APPEARANCE_TYPE_PROPERTY_NAMES[0]))
		{
			if (objAppearance == null)
			{
				System.out.println("No appearance node to set...");
				return;
			}

			if (!(value instanceof String))
			{
				System.out.println("Image filename not String");
				return;
			}

			BufferedImage bimg = null;
			try
			{
				final FileInputStream in = new FileInputStream((String) value);
				final JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
				bimg = decoder.decodeAsBufferedImage();
				in.close();
			}
			catch (final FileNotFoundException fnfe)
			{
				System.out.println("Could not find the file " + (String) value);
				return;
			}
			catch (final IOException ioe)
			{
				System.out.println("IO exc: " + ioe.getMessage());
				return;
			}

			// resize to fit on a texture
			final int xmax = bimg.getWidth();
			final int ymax = bimg.getHeight();

			final int width = getClosestPowerOf2(xmax);
			final int height = getClosestPowerOf2(ymax);
			final float xScale = (float) width / (float) xmax;
			final float yScale = (float) height / (float) ymax;

			// scale if scales aren't 1.0
			// BufferedImage scaledImg = null;
			if (!(xScale == 1.0f && yScale == 1.0f))
			{
				System.out.println("Resizing image...");
				// BufferedImage origImg = (BufferedImage) bimg.getImage();
				final AffineTransform at = AffineTransform.getScaleInstance(xScale, yScale);
				final AffineTransformOp atop = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				final BufferedImage scaledImg = atop.filter(bimg, null);

				bimg = scaledImg;
			}

			final ImageComponent2D imgcmp = new ImageComponent2D(ImageComponent.FORMAT_RGBA, bimg, false, false); // false,
																													// false

			final Texture2D tex = new Texture2D(Texture.BASE_LEVEL, Texture.RGB, width, height);
			// tex = new Texture2D(Texture2D.BASE_LEVEL, Texture.RGB, xmax, ymax);
			tex.setImage(0, imgcmp);
			objAppearance.setTexture(tex);
		}
		// this is used to set the transformation. It is intended to be used at authoring time
		// hence, the whole transformation is calculated each time one of its components changes
		// because the expectation is that these values shouldn't change after authoring time
		else if (name.startsWith("authoring"))
		{
			if (value == null)
			{
				System.out.println("Authoring transformation value " + name + " is null.");

				authroningTransformationValues.remove(name.substring(9, name.length()));
				return;
			}

			if (!(value instanceof Double))
			{
				System.out.println("Authoring transformation value " + name + " is not double.");
				return;
			}

			// remember the value
			System.out.println("Setting " + name.substring(9, name.length()) + " to " + value);
			authroningTransformationValues.put(name.substring(9, name.length()), value);

			// get the values
			double rotateX = 0;
			if (authroningTransformationValues.get("RotateX") != null)
			{
				rotateX = (Double) authroningTransformationValues.get("RotateX");
				System.out.println("Got rot X: " + rotateX);
			}
			double rotateY = 0;
			if (authroningTransformationValues.get("RotateY") != null)
			{
				rotateY = (Double) authroningTransformationValues.get("RotateY");
				System.out.println("Got rot Y: " + rotateY);
			}
			double rotateZ = 0;
			if (authroningTransformationValues.get("RotateZ") != null)
			{
				rotateZ = (Double) authroningTransformationValues.get("RotateZ");
				System.out.println("Got rot Z: " + rotateZ);
			}

			double scaleX = 1;
			if (authroningTransformationValues.get("ScaleX") != null)
			{
				scaleX = (Double) authroningTransformationValues.get("ScaleX");
			}
			double scaleY = 1;
			if (authroningTransformationValues.get("ScaleY") != null)
			{
				scaleY = (Double) authroningTransformationValues.get("ScaleY");
			}
			double scaleZ = 1;
			if (authroningTransformationValues.get("ScaleZ") != null)
			{
				scaleZ = (Double) authroningTransformationValues.get("ScaleZ");
			}

			double translateX = 0;
			if (authroningTransformationValues.get("TranslateX") != null)
			{
				translateX = (Double) authroningTransformationValues.get("TranslateX");
			}
			double translateY = 0;
			if (authroningTransformationValues.get("TranslateY") != null)
			{
				translateY = (Double) authroningTransformationValues.get("TranslateY");
			}
			double translateZ = 0;
			if (authroningTransformationValues.get("TranslateZ") != null)
			{
				translateZ = (Double) authroningTransformationValues.get("TranslateZ");
			}

			// calculate the transformation
			authoringTransformationMatrix = new Transform3D();

			final Transform3D transRotX = new Transform3D();
			transRotX.rotX(Math.PI * 2 * rotateX);

			final Transform3D transRotY = new Transform3D();
			transRotY.rotY(Math.PI * 2 * rotateY);

			final Transform3D transRotZ = new Transform3D();
			transRotZ.rotZ(Math.PI * 2 * rotateZ);

			final Transform3D transRot = new Transform3D();
			transRot.set(transRotX);
			transRot.mul(transRotY);
			transRot.mul(transRotZ);

			authoringTransformationMatrix.set(transRot);

			final Vector3d translateVector = new Vector3d(translateX, translateY, translateZ);
			authoringTransformationMatrix.setTranslation(translateVector);

			final Vector3d scaleVector = new Vector3d(scaleX, scaleY, scaleZ);
			authoringTransformationMatrix.setScale(scaleVector);

			transformObject();
		}
	}

	/**
	 * Property Change Listeners
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener l)
	{
		// super.removePropertyChangeListener(l);
		propertyChangeListeners.removePropertyChangeListener(l);
	}

	public synchronized void setName(final String newName)
	{
		final String oldName = this.name;
		this.name = newName;
		// fire property change - make sure the name matches the bean info name
		propertyChangeListeners.firePropertyChange("name", oldName, this.name);
	}

	@Override
	public void stop()
	{
	}

	/*
	 * This method is called to calculate how to transform an object.
	 * 
	 * First it performs the master transformation Then it performs any authoring time
	 * transformation Then it performs the operation time transformations in the order rotate,
	 * transform, scale
	 * 
	 * Note, this has been customised somewhat towards the requirements of the ARTECT project. I.e.
	 * the transformation matrix can be used to set the marker transformation, the authoring time
	 * transformation is set by experience authors and the operation time transformations is set by
	 * users e.g. using sliders. This customisation is not ideal, but was deemed worthwhile to make
	 * the ARTECT project work well. It somewhat compromises the general purpose nature and future
	 * usefulnes of this components...
	 */
	public void transformObject()
	{
		final Transform3D transNow = new Transform3D();

		if (matrixTransformation != null)
		{
			transNow.mul(matrixTransformation);
		}

		if (authoringTransformationMatrix != null)
		{
			transNow.mul(authoringTransformationMatrix);
		}

		if (rotateXTrans != null)
		{
			transNow.mul(rotateXTrans);
		}

		if (rotateYTrans != null)
		{
			transNow.mul(rotateYTrans);
		}

		if (rotateZTrans != null)
		{
			transNow.mul(rotateZTrans);
		}

		if (translateXTrans != null)
		{
			transNow.mul(translateXTrans);
		}

		if (translateYTrans != null)
		{
			transNow.mul(translateYTrans);
		}

		if (translateZTrans != null)
		{
			transNow.mul(translateZTrans);
		}

		if (scaleXTrans != null)
		{
			transNow.mul(scaleXTrans);
		}

		if (scaleYTrans != null)
		{
			transNow.mul(scaleYTrans);
		}

		if (scaleZTrans != null)
		{
			transNow.mul(scaleZTrans);
		}

		objTrans.setTransform(transNow);
	}

	private int getClosestPowerOf2(final int value)
	{
		if (value < 1) { return value; }

		int powerValue = 1;
		for (;;)
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
