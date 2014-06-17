package equip.ect.components.imageframework;

import equip.ect.Category;
import equip.ect.ECTComponent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @classification Media/Image
 * @displayName ImageDisplay
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
@ECTComponent
@Category("Media/Image")
public class ImageDisplay extends ImageSink implements Serializable
{
	private class ImagePanel extends JPanel
	{
		// BufferedImage image;
		//
		// public void setImage(BufferedImage image)
		// {
		// this.image = new BufferedImage(getWidth(), getHeight(),
		// BufferedImage.TYPE_INT_ARGB);
		// Graphics2D g2 = this.image.createGraphics();
		// g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		// g2.dispose();
		// }

		@Override
		protected void paintComponent(final Graphics g)
		{
			final Graphics2D g2 = (Graphics2D) g;
			if (outputImage.getImage() != null)
			{
				g2.drawImage(outputImage.getImage(), 0, 0, getWidth(), getHeight(), this);
				// g2.drawRenderedImage(outputImage.getImage(), null);
			}
			else
			{
				g2.setBackground(Color.black);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}

	private boolean fullscreen;
	private JFrame window = new JFrame();
	private final ImagePanel panel = new ImagePanel();

	public ImageDisplay()
	{
		window.add(panel);
		window.setSize(640, 480);
	}

	public boolean getFullscreen()
	{
		return fullscreen;
	}

	@Override
	public void imageUpdated(final BufferedImage image)
	{
		outputImage.setImage(image);

		final boolean visible = image != null;
		if (window.isVisible() != visible)
		{
			showWindow(visible);
		}
		if (visible)
		{
			window.repaint();
		}
	}

	public void setFullscreen(final boolean fullscreen)
	{
		final boolean oldscreen = getFullscreen();
		this.fullscreen = fullscreen;

		if (oldscreen != fullscreen)
		{
			showWindow(false);
			final JFrame oldWindow = window;

			window = new JFrame();
			window.add(panel);
			window.setResizable(!fullscreen);
			window.setUndecorated(fullscreen);
			if (!fullscreen)
			{
				window.setSize(640, 480);
			}

			oldWindow.dispose();
		}

		showWindow(outputImage.getImage() != null);

		propertyChangeListeners.firePropertyChange("fullscreen", oldscreen, fullscreen);
	}

	private void showWindow(final boolean visible)
	{
		if (fullscreen)
		{
			final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			System.out.println("Fullscreen Supported: " + devices[0].isFullScreenSupported());

			if (visible)
			{
				devices[0].setFullScreenWindow(window);
			}
			else
			{
				devices[0].setFullScreenWindow(null);
			}
		}
		else
		{
			window.setVisible(visible);
		}
	}
}