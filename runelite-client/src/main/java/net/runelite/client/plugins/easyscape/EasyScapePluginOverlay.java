package net.runelite.client.plugins.easyscape;

import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;

public class EasyScapePluginOverlay extends Overlay
{
	private final Client client;
	private final EasyScapePluginConfig config;

	@Inject
	private EasyScapePluginOverlay(Client client, EasyScapePluginConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(OverlayPriority.LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.getShowSafespot())
		{	System.out.println("2");
			final Actor interacting = client.getLocalPlayer().getInteracting();

			if (interacting instanceof NPC)
			{
				System.out.println("3");
				NPC target = (NPC) interacting;
				WorldPoint wp = findSafespot(target);

				if (wp == null)
				{
					System.out.println("---");
					return null;
				}

				LocalPoint lp = LocalPoint.fromWorld(client, wp);

				if (lp == null)
				{
					System.out.println("----");
					return null;
				}

				final Polygon poly = Perspective.getCanvasTilePoly(client, lp);
				if (poly == null)
				{
					System.out.println("-----");
					return null;
				}
				System.out.println("4");
				OverlayUtil.renderPolygon(graphics, poly, Color.CYAN);
			}
		}
		return null;
	}

	// Locate a safespot based on a certain monster.
	private WorldPoint findSafespot(NPC mob) {
		Player player = client.getLocalPlayer();

		if (player == null) {
			System.out.println("player");
			return null;
		} else {
			WorldArea playerArea = player.getWorldArea();
			WorldArea mobArea = mob.getWorldArea();

			Tile[][][] map = client.getScene().getTiles();

			boolean inMelee = mobArea.isInMeleeDistance(playerArea);
			boolean inRange = playerArea.hasLineOfSightTo(client, playerArea);
			boolean isSafe = !inMelee && !inRange;

			if (isSafe) {
				return player.getWorldLocation();
			} else {
				System.out.println("not safe");
				return null;
			}
		}
	}
}
