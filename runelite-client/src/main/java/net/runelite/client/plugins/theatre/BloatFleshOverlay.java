package net.runelite.client.plugins.theatre;

import net.runelite.api.Client;
import net.runelite.api.GraphicID;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

public class BloatFleshOverlay extends Overlay{

	private final Client client;
	private final TheatrePlugin plugin;
	private final TheatreConfig config;

	private static final int[] BLOAT_FALLING_FLESH = {1570,1571,1572,1573,1574,1575,1576,1577,1578};

	@Inject
	BloatFleshOverlay(Client client, TheatrePlugin plugin, TheatreConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.showBloatFlesh() && plugin.inTheatre())
		{
			List<GraphicsObject> graphicsObjects = client.getGraphicsObjects();

			for (int i = 0; i < graphicsObjects.size(); i++)
			{
				GraphicsObject object = graphicsObjects.get(i);
				//pretty sure a for loop is more efficient than doing an IntStream, but was having trouble getting to work right smh
				boolean fleshCheck = IntStream.of(BLOAT_FALLING_FLESH).anyMatch(x -> x == object.getId());

				if (fleshCheck)
				{
					LocalPoint localCenter = object.getLocation();
					Polygon poly = Perspective.getCanvasTileAreaPoly(client, localCenter, 1);
					if (poly != null)
					{
						OverlayUtil.renderPolygon(graphics, poly, Color.red);
					}

				}
			}

		}

		return null;
	}

}