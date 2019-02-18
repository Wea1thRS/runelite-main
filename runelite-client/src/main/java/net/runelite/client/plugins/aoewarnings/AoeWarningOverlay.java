
package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.Instant;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class AoeWarningOverlay extends Overlay
{
	private static final int FILL_START_ALPHA = 25;
	private static final int OUTLINE_START_ALPHA = 255;

	private final Client client;
	private final AoeWarningPlugin plugin;
	private final AoeWarningConfig config;

	@Inject
	public AoeWarningOverlay(@Nullable Client client, AoeWarningPlugin plugin, AoeWarningConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enabled())
			return null;

		for (Iterator<AoeProjectile> it = plugin.getProjectiles().values().iterator(); it.hasNext();)
		{
			AoeProjectile aoeProjectile = it.next();

			if (Instant.now().isAfter(aoeProjectile.getStartTime().plus(aoeProjectile.getAoeProjectileInfo().getLifeTime())))
			{
				it.remove();
				continue;
			}

			Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, aoeProjectile.getTargetPoint(), aoeProjectile.getAoeProjectileInfo().getAoeSize());

			if (tilePoly == null)
				continue;

			// how far through the projectiles lifetime between 0-1.
			double progress = (System.currentTimeMillis() - aoeProjectile.getStartTime().toEpochMilli()) / (double) aoeProjectile.getAoeProjectileInfo().getLifeTime().toMillis();

			int fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);//alpha drop off over lifetime
			int outlineAlpha = (int) ((1 - progress) * OUTLINE_START_ALPHA);

			if (fillAlpha < 0)
			{
				fillAlpha = 0;
			}
			if (outlineAlpha < 0)
			{
				outlineAlpha = 0;
			}

			if (fillAlpha > 255)
			{
				fillAlpha = 255;
			}
			if (outlineAlpha > 255)
			{
				outlineAlpha = 255;//Make sure we don't pass in an invalid alpha
			}

			if (config.isOutlineEnabled())
				OverlayUtil.renderPolygon(graphics, tilePoly, new Color(255, 0, 0, outlineAlpha));

			graphics.setColor(new Color(255, 0, 0, fillAlpha));
			graphics.fillPolygon(tilePoly);
		}
		return null;
	}
}
