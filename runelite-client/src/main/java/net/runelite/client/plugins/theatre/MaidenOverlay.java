package net.runelite.client.plugins.theatre;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import net.runelite.api.Client;

public class MaidenOverlay extends Overlay
{
	private final Client client;
	private final TheatrePlugin plugin;
	private final TheatreConfig config;

	private static final int MAIDEN_BLOOD_POOL = GraphicID.MAIDEN_BLOOD_POOL;

	@Inject
	MaidenOverlay(Client client, TheatrePlugin plugin, TheatreConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showMaidenPools() && plugin.inTheatre())
		{
			List<GraphicsObject> graphicsObjects = client.getGraphicsObjects();

			for (int i = 0; i < graphicsObjects.size(); i++)
			{
				GraphicsObject object = graphicsObjects.get(i);
				if (object.getId() == MAIDEN_BLOOD_POOL)
				{
					LocalPoint localCenter = object.getLocation();
					Polygon poly = Perspective.getCanvasTileAreaPoly(client, localCenter, 1);
					if (poly != null)
					{
						OverlayUtil.renderPolygon(graphics, poly, Color.RED);
					}

				}
			}

		}

		return null;
	}

}