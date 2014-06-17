package equip.ect.components.imageframework;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.image.BufferedImageOp;
import java.io.Serializable;

/**
 * 
 * @classification Media/Image
 * @displayName ImageBlur
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
@ECTComponent
@Category("Media/Image")
public class ImageBlur extends ImageFilter implements Serializable
{
	private BoxBlurFilter blurFilter = new BoxBlurFilter();

	private float blur = 0;
	private int factor = 100;

	public float getBlur()
	{
		return blur;
	}

	public void setBlur(final float blur)
	{
		final double oldBlur = this.blur;
		this.blur = blur;

		propertyChangeListeners.firePropertyChange("blur", oldBlur, blur);

		final int blursize = (int) (blur * factor);
		blurFilter.setRadius(blursize);
		imageUpdated(image);
	}

	@Override
	protected BufferedImageOp getImageOp()
	{
		if (blur == 0) { return null; }
		return blurFilter;
	}
}
