package equip.ect.components.imageframework;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.Color;
import java.awt.image.BufferedImageOp;
import java.io.Serializable;

/**
 * 
 * @classification Media/Image
 * @displayName ImageFade
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
@ECTComponent
@Category("Media/Image")
public class ImageFade extends ImageFilter implements Serializable
{
	private ColourFilter colourFilter = new ColourFilter(Color.black);

	public float getFade()
	{
		return colourFilter.getMixValue();
	}

	public void setFade(final float fade)
	{
		final float oldFade = colourFilter.getMixValue();
		colourFilter.setMixValue(fade);

		propertyChangeListeners.firePropertyChange("fade", oldFade, colourFilter.getMixValue());
		imageUpdated(image);
	}

	@Override
	protected BufferedImageOp getImageOp()
	{
		return colourFilter;
	}
}
