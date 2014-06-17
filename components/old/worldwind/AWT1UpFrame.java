/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package equip.ect.components.worldwind;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.examples.BasicDragger;
import gov.nasa.worldwind.examples.StatusBar;
import gov.nasa.worldwind.formats.georss.GeoRSSParser;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3;
import gov.nasa.worldwind.layers.Earth.WorldMapLayer;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceSector;
import gov.nasa.worldwind.render.UserFacingIcon;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Tom Gaskins
 * @version $Id: AWT1UpFrame.java,v 1.2 2012/04/03 12:27:27 chaoticgalen Exp $
 */

public class AWT1UpFrame extends javax.swing.JFrame
{
	StatusBar statusBar;
	JLabel cursorPositionDisplay;
	WorldWindowGLCanvas wwd;

	WWIcon lastPickedIcon;

	private static final String lineTestString = "<gml:LineString> <gml:posList>45.256 -110.45 46.46 -109.48 43.84 -109.86</gml:posList></gml:LineString>";

	private static final String itemTestString = "<item>    <title>M 3.2, Mona Passage</title>    <link>http://example.org/2005/09/09/atom01</link>    <pubDate>Wed, 17 Aug 2005 07:02:32 GMT</pubDate>    <georss:where>      <gml:Polygon>        <gml:exterior>          <gml:LinearRing>            <gml:posList>    			        45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45        	       </gml:posList>          </gml:LinearRing>   </gml:exterior>  </gml:Polygon> </georss:where> </item>";

	public AWT1UpFrame()
	{
		try
		{
			System.out.println(gov.nasa.worldwind.Version.getVersion());

			wwd = new gov.nasa.worldwind.awt.WorldWindowGLCanvas();
			wwd.setPreferredSize(new java.awt.Dimension(800, 600));
			this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);

			this.statusBar = new StatusBar();
			this.getContentPane().add(statusBar, BorderLayout.PAGE_END);

			this.pack();

			final java.awt.Dimension prefSize = this.getPreferredSize();
			java.awt.Dimension parentSize;
			final java.awt.Point parentLocation = new java.awt.Point(0, 0);
			parentSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			final int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
			final int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
			this.setLocation(x, y);
			this.setResizable(true);

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

			m.getLayers().add(this.buildShapesLayer());
			m.getLayers().add(this.buildIconLayer());
			m.getLayers().add(this.buildGeoRSSLayer());
			m.setShowWireframeExterior(false);
			m.setShowWireframeInterior(false);
			wwd.setModel(m);

			// Forward events to the status bar to provide the cursor position info.
			this.statusBar.setEventSource(wwd);

			this.wwd.addRenderingListener(new RenderingListener()
			{
				@Override
				public void stageChanged(final RenderingEvent event)
				{
					// Do nothing; just showing how to use it.
					// if (event.getSource() instanceof WorldWindow)
					// {
					// Double frameRate = (Double) ((WorldWindow)
					// event.getSource()).getValue(AVKey.FRAME_RATE);
					// if (frameRate != null)
					// System.out.println(frameRate);
					// }
				}
			});

			this.wwd.addSelectListener(new SelectListener()
			{
				private WWIcon lastToolTipIcon = null;
				private BasicDragger dragger = new BasicDragger(wwd);

				@Override
				public void selected(final SelectEvent event)
				{
					if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
					{
						if (event.hasObjects())
						{
							System.out.println("Single clicked " + event.getTopObject());
							if (event.getTopObject() instanceof WorldMapLayer)
							{
								// Left click on World Map : iterate view to target position
								final Position targetPos = event.getTopPickedObject().getPosition();
								final OrbitView view = (OrbitView) AWT1UpFrame.this.wwd.getView();
								final Globe globe = AWT1UpFrame.this.wwd.getModel().getGlobe();
								// Use a PanToIterator
								view.applyStateIterator(FlyToOrbitViewStateIterator
										.createPanToIterator(	view,
																globe,
																new LatLon(targetPos.getLatitude(), targetPos
																		.getLongitude()), Angle.ZERO, Angle.ZERO,
																targetPos.getElevation()));
							}

						}
						else
						{
							System.out.println("Single clicked " + "no object");
						}
					}
					else if (event.getEventAction().equals(SelectEvent.LEFT_DOUBLE_CLICK))
					{
						if (event.hasObjects())
						{
							System.out.println("Double clicked " + event.getTopObject());
						}
						else
						{
							System.out.println("Double clicked " + "no object");
						}
					}
					else if (event.getEventAction().equals(SelectEvent.RIGHT_CLICK))
					{
						if (event.hasObjects())
						{
							System.out.println("Right clicked " + event.getTopObject());
						}
						else
						{
							System.out.println("Right clicked " + "no object");
						}
					}
					else if (event.getEventAction().equals(SelectEvent.HOVER))
					{
						if (lastToolTipIcon != null)
						{
							lastToolTipIcon.setShowToolTip(false);
							this.lastToolTipIcon = null;
							AWT1UpFrame.this.wwd.repaint();
						}

						if (event.hasObjects() && !this.dragger.isDragging())
						{
							if (event.getTopObject() instanceof WWIcon)
							{
								this.lastToolTipIcon = (WWIcon) event.getTopObject();
								lastToolTipIcon.setShowToolTip(true);
								AWT1UpFrame.this.wwd.repaint();
							}
						}
					}
					else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
					{
						AWT1UpFrame.this.highlight(event.getTopObject());
					}
					else if (event.getEventAction().equals(SelectEvent.DRAG_END)
							|| event.getEventAction().equals(SelectEvent.DRAG))
					{
						// Delegate dragging computations to a dragger.
						this.dragger.selected(event);
						if (event.getTopObject() instanceof WWIcon)
						{
							if (event.getEventAction().equals(SelectEvent.DRAG_END))
							{
								lastPickedIcon.setAlwaysOnTop(false);
							}
							else
							{
								lastPickedIcon = (WWIcon) event.getTopObject();
								lastPickedIcon.setAlwaysOnTop(true);
							}
						}
						if (event.getEventAction().equals(SelectEvent.DRAG_END))
						{
							final PickedObjectList pol = wwd.getObjectsAtCurrentPosition();
							if (pol != null)
							{
								AWT1UpFrame.this.highlight(pol.getTopObject());
							}
						}
					}
				}
			});

			this.wwd.addPositionListener(new PositionListener()
			{
				@Override
				public void moved(final PositionEvent event)
				{
					// Do nothing; just show how to add a position listener.
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	private RenderableLayer buildGeoRSSLayer()
	{
		try
		{
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setNamespaceAware(true);
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final Document doc = docBuilder.parse(new File("./common/GeoRSSTestData.xml"));
			final List<Renderable> shapes = GeoRSSParser.parseShapes(doc);

			// List<Renderable> shapes = GeoRSSParser.parseFragment(itemTestString, null);

			// StringBuffer sb = new StringBuffer();
			// FileReader fr = new FileReader("./common/feed.xml");
			// for (int c = fr.read(); c >=0; c = fr.read())
			// sb.append((char) c);
			// List<Renderable> shapes = GeoRSSParser.parseShapes(sb.toString());

			final RenderableLayer layer = new RenderableLayer();
			if (shapes != null)
			{
				for (final Renderable shape : shapes)
				{
					layer.addRenderable(shape);
				}
			}

			return layer;
		}
		catch (final ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		catch (final SAXException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private IconLayer buildIconLayer()
	{
		final IconLayer layer = new IconLayer();

		for (double lat = 0; lat < 10; lat += 10)
		{
			for (double lon = -180; lon < 180; lon += 10)
			{
				double alt = 0;
				if (lon % 90 == 0)
				{
					alt = 2000000;
				}
				final WWIcon icon = new UserFacingIcon("images/32x32-icon-nasa.png", new Position(
						Angle.fromDegrees(lat), Angle.fromDegrees(lon), alt));
				icon.setHighlightScale(1.5);
				icon.setToolTipFont(this.makeToolTipFont());
				icon.setToolTipText(icon.getImageSource().toString());
				icon.setToolTipTextColor(java.awt.Color.YELLOW);
				layer.addIcon(icon);
			}
		}

		return layer;
	}

	private RenderableLayer buildShapesLayer()
	{
		final RenderableLayer layer = new RenderableLayer();

		final Color interiorColor = new Color(1f, 1f, 0f, 0.3f);
		final Color borderColor = new Color(1f, 1f, 0f, 0.4f);

		SurfaceSector quad = new SurfaceSector(new Sector(Angle.fromDegrees(41.0), Angle.fromDegrees(41.6),
				Angle.fromDegrees(-122.5), Angle.fromDegrees(-121.7)), interiorColor, borderColor);
		layer.addRenderable(quad);

		quad = new SurfaceSector(new Sector(Angle.fromDegrees(38.9), Angle.fromDegrees(39.3),
				Angle.fromDegrees(-120.2), Angle.fromDegrees(-119.9)), new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f,
				1f, 0.4f));
		layer.addRenderable(quad);

		final double originLat = 28;
		final double originLon = -82;
		ArrayList<LatLon> positions = new ArrayList<LatLon>();
		positions.add(new LatLon(Angle.fromDegrees(originLat + 5.0), Angle.fromDegrees(originLon + 2.5)));
		positions.add(new LatLon(Angle.fromDegrees(originLat + 5.0), Angle.fromDegrees(originLon - 2.5)));
		positions.add(new LatLon(Angle.fromDegrees(originLat + 2.5), Angle.fromDegrees(originLon - 5.0)));
		positions.add(new LatLon(Angle.fromDegrees(originLat - 2.5), Angle.fromDegrees(originLon - 5.0)));
		positions.add(new LatLon(Angle.fromDegrees(originLat - 5.0), Angle.fromDegrees(originLon - 2.5)));
		positions.add(new LatLon(Angle.fromDegrees(originLat - 5.0), Angle.fromDegrees(originLon + 2.5)));
		positions.add(new LatLon(Angle.fromDegrees(originLat - 2.5), Angle.fromDegrees(originLon + 5.0)));
		positions.add(new LatLon(Angle.fromDegrees(originLat + 2.5), Angle.fromDegrees(originLon + 5.0)));

		final SurfacePolygon polygon = new SurfacePolygon(positions, new Color(1f, 0.11f, 0.2f, 0.4f), new Color(1f,
				0f, 0f, 0.6f));
		polygon.setStroke(new BasicStroke(2f));
		layer.addRenderable(polygon);

		// Test +180/-180 lon span Polyline
		positions = new ArrayList<LatLon>();
		positions.add(new LatLon(Angle.fromDegrees(-10), Angle.fromDegrees(170)));
		positions.add(new LatLon(Angle.fromDegrees(-10), Angle.fromDegrees(-170)));
		final Polyline polyline = new Polyline(positions, 1000);
		polyline.setPathType(Polyline.GREAT_CIRCLE);
		layer.addRenderable(polyline);
		// Test +180/-180 lon span SurfacePolyline
		positions = new ArrayList<LatLon>();
		positions.add(new LatLon(Angle.fromDegrees(20), Angle.fromDegrees(-170)));
		positions.add(new LatLon(Angle.fromDegrees(15), Angle.fromDegrees(170)));
		positions.add(new LatLon(Angle.fromDegrees(10), Angle.fromDegrees(-175)));
		positions.add(new LatLon(Angle.fromDegrees(5), Angle.fromDegrees(170)));
		positions.add(new LatLon(Angle.fromDegrees(0), Angle.fromDegrees(-170)));
		positions.add(new LatLon(Angle.fromDegrees(20), Angle.fromDegrees(-170)));
		final SurfacePolygon surfacePolygon = new SurfacePolygon(positions, new Color(1f, 0.11f, 0.2f, 0.4f),
				new Color(1f, 0f, 0f, 0.6f));
		surfacePolygon.setStroke(new BasicStroke(4f));
		layer.addRenderable(surfacePolygon);

		return layer;
	}

	private void highlight(final Object o)
	{
		if (this.lastPickedIcon == o) { return; // same thing selected
		}

		if (this.lastPickedIcon != null)
		{
			this.lastPickedIcon.setHighlighted(false);
			this.lastPickedIcon = null;
		}

		if (o != null && o instanceof WWIcon)
		{
			this.lastPickedIcon = (WWIcon) o;
			this.lastPickedIcon.setHighlighted(true);
		}
	}

	private Font makeToolTipFont()
	{
		final HashMap<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>();

		fontAttributes.put(TextAttribute.BACKGROUND, new java.awt.Color(0.4f, 0.4f, 0.4f, 1f));
		return Font.decode("Arial-BOLD-14").deriveFont(fontAttributes);
	}
}
