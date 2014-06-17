package equip.ect.components.worldwind;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

public class WorldWindPanel extends JPanel
{
	WorldWindowGLCanvas wwd;

	public WorldWindPanel(final Dimension size)
	{
		setLayout(new BorderLayout());

		wwd = new WorldWindowGLCanvas();

		wwd.setPreferredSize(size);
		setPreferredSize(size);

		final Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);

		final LayerList layers = m.getLayers();
		for (final Layer layer : layers)
		{
			if (layer instanceof TiledImageLayer)
			{
				((TiledImageLayer) layer).setShowImageTileOutlines(false);
			}
			if (layer instanceof LandsatI3)
			{
				((TiledImageLayer) layer).setDrawBoundingVolumes(false);
			}
			if (layer instanceof CompassLayer)
			{
				((CompassLayer) layer).setShowTilt(true);
			}
		}

		/*
		 * m.getLayers().add(buildShapesLayer()); m.getLayers().add(buildIconLayer());
		 * m.getLayers().add(buildGeoRSSLayer());
		 */

		m.setShowWireframeExterior(false);
		m.setShowWireframeInterior(false);

		wwd.setModel(m);

		// wwd.addSelectListener(new ClickAndGoSelectListener(wwd, WorldMapLayer.class));

		add(wwd, BorderLayout.CENTER);
	}
}
