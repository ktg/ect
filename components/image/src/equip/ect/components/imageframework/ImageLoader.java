package equip.ect.components.imageframework;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;

import javax.imageio.ImageIO;

/**
 * @classification Media/Image
 * @displayName ImageLoader
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a> *
 */
@ECTComponent
@Category("Media/Image")
public class ImageLoader extends ImageSource implements Serializable
{
	/**
	 * Clones a BufferedImage.
	 * 
	 * @param image
	 *            the image to clone
	 * @return the cloned image
	 */
	private static BufferedImage cloneImage(final BufferedImage image)
	{
		final BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = newImage.createGraphics();
		g.drawRenderedImage(image, null);
		g.dispose();
		return newImage;
	}

	private String filename;

	private String status = "No File";

	public String getFilename()
	{
		return filename;
	}

	public String getStatus()
	{
		return status;
	}

	public void setFilename(final String filename)
	{
		final String oldFilename = this.filename;
		this.filename = filename;

		propertyChangeListeners.firePropertyChange("filename", oldFilename, filename);
		loadImage();
	}

	private void loadImage()
	{
		final String oldStatus = status;
		try
		{
			if (filename.equals(""))
			{
				outputImage.setImage(null);
				status = "No File";
			}
			else
			{
				outputImage.setImage(cloneImage(ImageIO.read(new File(filename))));
				status = "OK";
			}
		}
		catch (final Exception e)
		{
			status = "ERROR: " + e.getMessage();
			outputImage.setImage(null);
			e.printStackTrace();
		}
		propertyChangeListeners.firePropertyChange("status", oldStatus, status);
	}
}
