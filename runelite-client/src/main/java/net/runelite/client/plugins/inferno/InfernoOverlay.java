package net.runelite.client.plugins.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.inferno.displaymodes.InfernoPrayerOverlayMode;
import net.runelite.client.plugins.inferno.displaymodes.SafespotDisplayMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class InfernoOverlay extends Overlay
{
	private static final int TICK_PIXEL_SIZE = 60;
	private static final int BLOB_WIDTH = 10;
	private static final int BLOB_HEIGHT = 5;

	private final InfernoPlugin plugin;
	private final Client client;

	@Inject
	private InfernoOverlay(final Client client, final InfernoPlugin plugin)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGHEST);

		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		// Debug: obstacles
		if (plugin.isIndicateObstacles())
		{
			for (WorldPoint worldPoint : plugin.getObstacles())
			{
				final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

				if (localPoint == null)
				{
					continue;
				}

				final Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

				if (tilePoly == null)
				{
					continue;
				}

				OverlayUtil.renderPolygon(graphics, tilePoly, Color.BLUE);
			}
		}

		// Indicate safespots (indicate entire area)
		if (plugin.getIndicateSafespots() == SafespotDisplayMode.AREA)
		{
			for (int safeSpotId : plugin.getSafeSpotAreas().keySet())
			{
				if (safeSpotId > 6)
				{
					continue;
				}

				Color colorEdge1 = null;
				Color colorEdge2 = null;
				Color colorFill = null;

				switch (safeSpotId)
				{
					case 0:
						colorEdge1 = Color.WHITE;
						colorFill = Color.WHITE;
						break;
					case 1:
						colorEdge1 = Color.RED;
						colorFill = Color.RED;
						break;
					case 2:
						colorEdge1 = Color.GREEN;
						colorFill = Color.GREEN;
						break;
					case 3:
						colorEdge1 = Color.BLUE;
						colorFill = Color.BLUE;
						break;
					case 4:
						colorEdge1 = Color.RED;
						colorEdge2 = Color.GREEN;
						colorFill = Color.YELLOW;
						break;
					case 5:
						colorEdge1 = Color.RED;
						colorEdge2 = Color.BLUE;
						colorFill = new Color(255, 0, 255);
						break;
					case 6:
						colorEdge1 = Color.GREEN;
						colorEdge2 = Color.BLUE;
						colorFill = new Color(0, 255, 255);
						break;
					default:
						continue;
				}

				//Add all edges, calculate average edgeSize and indicate tiles
				final List<int[][]> allEdges = new ArrayList<>();
				int edgeSizeSquared = 0;

				for (WorldPoint worldPoint : plugin.getSafeSpotAreas().get(safeSpotId))
				{
					final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

					if (localPoint == null)
					{
						continue;
					}

					final Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

					if (tilePoly == null)
					{
						continue;
					}

					OverlayUtil.renderAreaTilePolygon(graphics, tilePoly, colorFill);

					final int[][] edge1 = new int[][]{{tilePoly.xpoints[0], tilePoly.ypoints[0]}, {tilePoly.xpoints[1], tilePoly.ypoints[1]}};
					edgeSizeSquared += Math.pow(tilePoly.xpoints[0] - tilePoly.xpoints[1], 2) + Math.pow(tilePoly.ypoints[0] - tilePoly.ypoints[1], 2);
					allEdges.add(edge1);
					final int[][] edge2 = new int[][]{{tilePoly.xpoints[1], tilePoly.ypoints[1]}, {tilePoly.xpoints[2], tilePoly.ypoints[2]}};
					edgeSizeSquared += Math.pow(tilePoly.xpoints[1] - tilePoly.xpoints[2], 2) + Math.pow(tilePoly.ypoints[1] - tilePoly.ypoints[2], 2);
					allEdges.add(edge2);
					final int[][] edge3 = new int[][]{{tilePoly.xpoints[2], tilePoly.ypoints[2]}, {tilePoly.xpoints[3], tilePoly.ypoints[3]}};
					edgeSizeSquared += Math.pow(tilePoly.xpoints[2] - tilePoly.xpoints[3], 2) + Math.pow(tilePoly.ypoints[2] - tilePoly.ypoints[3], 2);
					allEdges.add(edge3);
					final int[][] edge4 = new int[][]{{tilePoly.xpoints[3], tilePoly.ypoints[3]}, {tilePoly.xpoints[0], tilePoly.ypoints[0]}};
					edgeSizeSquared += Math.pow(tilePoly.xpoints[3] - tilePoly.xpoints[0], 2) + Math.pow(tilePoly.ypoints[3] - tilePoly.ypoints[0], 2);
					allEdges.add(edge4);
				}

				if (allEdges.size() <= 0)
				{
					continue;
				}

				edgeSizeSquared /= allEdges.size();

				//Find and indicate unique edges
				final int toleranceSquared = (int) Math.ceil(edgeSizeSquared / 6);

				for (int i = 0; i < allEdges.size(); i++)
				{
					int[][] baseEdge = allEdges.get(i);

					boolean duplicate = false;

					for (int j = 0; j < allEdges.size(); j++)
					{
						if (i == j)
						{
							continue;
						}

						int[][] checkEdge = allEdges.get(j);

						if (edgeEqualsEdge(baseEdge, checkEdge, toleranceSquared))
						{
							duplicate = true;
							break;
						}
					}

					if (!duplicate)
					{
						OverlayUtil.renderFullLine(graphics, baseEdge, colorEdge1);

						if (colorEdge2 != null)
						{
							OverlayUtil.renderDashedLine(graphics, baseEdge, colorEdge2);
						}
					}
				}

			}
		}
		// Indicate safespots (every tile individually indicated)
		else if (plugin.getIndicateSafespots() == SafespotDisplayMode.INDIVIDUAL_TILES)
		{
			for (WorldPoint worldPoint : plugin.getSafeSpotMap().keySet())
			{
				final int safeSpotId = plugin.getSafeSpotMap().get(worldPoint);

				if (safeSpotId > 3)
				{
					continue;
				}

				final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

				if (localPoint == null)
				{
					continue;
				}

				final Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

				if (tilePoly == null)
				{
					continue;
				}

				// TODO: Config values
				Color color;
				switch (safeSpotId)
				{
					case 0:
						color = Color.WHITE;
						break;
					case 1:
						color = Color.RED;
						break;
					case 2:
						color = Color.GREEN;
						break;
					case 3:
						color = Color.BLUE;
						break;
					default:
						continue;
				}

				OverlayUtil.renderPolygon(graphics, tilePoly, color);
			}
		}

		// Indicate safespots for the zuk shield
		if (plugin.isIndicateZukShieldSafespots())
		{
			for (WorldPoint worldPoint : plugin.getZukShieldSafespots())
			{
				final LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

				if (localPoint == null)
				{
					continue;
				}

				final Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

				if (tilePoly == null)
				{
					continue;
				}

				OverlayUtil.renderPolygon(graphics, tilePoly, plugin.getZukShieldSafespotsColor());
			}
		}

		final HashMap<Integer, HashMap<InfernoNPC.Attack, Integer>> upcomingAttacks = new HashMap<>();

		for (InfernoNPC infernoNPC : plugin.getInfernoNpcs().values())
		{
			// Indicate active healers and safespotted NPC's
			if (infernoNPC.getNpc().getConvexHull() != null)
			{
				if (plugin.isIndicateNibblers() && infernoNPC.getType() == InfernoNPC.Type.NIBBLER)
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				}
				else if (plugin.isIndicateActiveHealers() && infernoNPC.getType() == InfernoNPC.Type.HEALER_JAD
					&& infernoNPC.getNpc().getInteracting() != client.getLocalPlayer())
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				}
				else if (plugin.isIndicateNonSafespotted() && infernoNPC.getType().getPriority() < 99
					&& infernoNPC.canAttack(client, client.getLocalPlayer().getWorldLocation()))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.RED);
				}
				else if (plugin.isIndicateTemporarySafespotted() && infernoNPC.getType().getPriority() < 99
					&& infernoNPC.canMoveToAttack(client, client.getLocalPlayer().getWorldLocation(), plugin.getObstacles()))
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.YELLOW);
				}
				else if (plugin.isIndicateSafespotted() && infernoNPC.getType().getPriority() < 99)
				{
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.GREEN);
				}
			}

			// Debug: npc main position tile
			if (plugin.isIndicateNpcPosition())
			{
				final LocalPoint localPoint = LocalPoint.fromWorld(client, infernoNPC.getNpc().getWorldLocation());

				if (localPoint != null)
				{
					final Polygon tilePolygon = Perspective.getCanvasTilePoly(client, localPoint);

					if (tilePolygon != null)
					{
						OverlayUtil.renderPolygon(graphics, tilePolygon, Color.BLUE);
					}
				}
			}

			if (infernoNPC.getTicksTillNextAttack() <= 0)
			{
				continue;
			}

			// Tick timer on NPC's
			if (plugin.isTicksOnNpc() && infernoNPC.getType().getPriority() < 100)
			{
				final Color color = (infernoNPC.getTicksTillNextAttack() == 1
					|| (infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() == 4))
					? infernoNPC.getNextAttack().getCriticalColor() : infernoNPC.getNextAttack().getNormalColor();
				final Point canvasPoint = infernoNPC.getNpc().getCanvasTextLocation(
					graphics, String.valueOf(infernoNPC.getTicksTillNextAttack()), 0);
				OverlayUtil.renderTextLocation(graphics, String.valueOf(infernoNPC.getTicksTillNextAttack()),
					plugin.getTextSize(), plugin.getFontStyle().getFont(), color, canvasPoint, false, 0);
			}

			// Map all upcoming attacks and their priority + determine which NPC is about to attack next
			if (infernoNPC.getType().getPriority() < 99
				&& (infernoNPC.getNextAttack() != InfernoNPC.Attack.UNKNOWN
				|| (plugin.isIndicateBlobDetectionTick() && infernoNPC.getType() == InfernoNPC.Type.BLOB
				&& infernoNPC.getTicksTillNextAttack() >= 4)))
			{
				if (plugin.isIndicateBlobDetectionTick() && infernoNPC.getType() == InfernoNPC.Type.BLOB
					&& infernoNPC.getTicksTillNextAttack() >= 4)
				{
					if (!upcomingAttacks.containsKey(infernoNPC.getTicksTillNextAttack()))
					{
						upcomingAttacks.put(infernoNPC.getTicksTillNextAttack() , new HashMap<>());
					}
					if (!upcomingAttacks.containsKey(infernoNPC.getTicksTillNextAttack() - 3))
					{
						upcomingAttacks.put(infernoNPC.getTicksTillNextAttack() - 3, new HashMap<>());
					}
					if (!upcomingAttacks.containsKey(infernoNPC.getTicksTillNextAttack() - 4))
					{
						upcomingAttacks.put(infernoNPC.getTicksTillNextAttack() - 4, new HashMap<>());
					}

					// If there's already a magic attack on the detection tick, group them
					if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoNPC.Attack.MAGIC))
					{
						if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoNPC.Attack.MAGIC) > 6)
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, 6);
						}
					}
					// If there's already a ranged attack on the detection tick, group them
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoNPC.Attack.RANGED))
					{
						if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoNPC.Attack.RANGED) > 6)
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, 6);
						}
					}
					// If there's going to be a magic attack on the blob attack tick, pray range on the detect tick so magic is prayed on the attack tick
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(InfernoNPC.Attack.MAGIC)
							|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey(InfernoNPC.Attack.MAGIC))
					{
						if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoNPC.Attack.RANGED)
								|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoNPC.Attack.RANGED) > 6)
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.RANGED, 6);
						}
					}
					// If there's going to be a ranged attack on the blob attack tick, pray magic on the detect tick so range is prayed on the attack tick
					else if (upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(InfernoNPC.Attack.RANGED)
							|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 4).containsKey(InfernoNPC.Attack.RANGED))
					{
						if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).containsKey(InfernoNPC.Attack.MAGIC)
								|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).get(InfernoNPC.Attack.MAGIC) > 6)
						{
							upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, 6);
						}
					}
					// If there's no magic or ranged attack on the detection tick, create a magic pray blob
					else
					{
						upcomingAttacks.get(infernoNPC.getTicksTillNextAttack() - 3).put(InfernoNPC.Attack.MAGIC, 6);
					}
				}
				else
				{
					if (!upcomingAttacks.containsKey(infernoNPC.getTicksTillNextAttack()))
					{
						upcomingAttacks.put(infernoNPC.getTicksTillNextAttack(), new HashMap<>());
					}

					final InfernoNPC.Attack attack = infernoNPC.getNextAttack();
					final int priority = infernoNPC.getType().getPriority();

					if (!upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).containsKey(attack)
						|| upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).get(attack) > priority)
					{
						upcomingAttacks.get(infernoNPC.getTicksTillNextAttack()).put(attack, priority);
					}
				}
			}
		}

		if (plugin.isShowPrayerHelp()
			&& (plugin.getPrayerOverlayMode() == InfernoPrayerOverlayMode.PRAYER_TAB
			|| plugin.getPrayerOverlayMode() == InfernoPrayerOverlayMode.BOTH)
			&& (!client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC).isHidden()
			&& !client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES).isHidden()
			&& !client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE).isHidden()))
		{
			InfernoNPC.Attack closestAttack = null;
			int closestTick = 999;
			int closestPriority = 999;

			if (plugin.isDescendingBoxes())
			{
				// Descending boxes (Guitar Hero)
				for (Integer tick : upcomingAttacks.keySet())
				{
					final HashMap<InfernoNPC.Attack, Integer> attackPriority = upcomingAttacks.get(tick);
					int bestPriority = 999;
					InfernoNPC.Attack bestAttack = null;

					for (InfernoNPC.Attack currentAttack : attackPriority.keySet())
					{
						final int currentPriority = attackPriority.get(currentAttack);
						if (currentPriority < bestPriority)
						{
							bestAttack = currentAttack;
							bestPriority = currentPriority;
						}

						if (tick < closestTick || (tick == closestTick && currentPriority < closestPriority))
						{
							closestAttack = currentAttack;
							closestPriority = currentPriority;
							closestTick = tick;
						}
					}

					for (InfernoNPC.Attack currentAttack : attackPriority.keySet())
					{
						//TODO: Config values for these colors
						final Color color = (tick == 1 && currentAttack == bestAttack) ? Color.RED : Color.ORANGE;
						final Widget prayerWidget = client.getWidget(currentAttack.getPrayer().getWidgetInfo());

						int baseX = (int) prayerWidget.getBounds().getX();
						baseX += prayerWidget.getBounds().getWidth() / 2;
						baseX -= BLOB_WIDTH / 2;

						int baseY = (int) prayerWidget.getBounds().getY() - tick * TICK_PIXEL_SIZE - BLOB_HEIGHT;
						baseY += TICK_PIXEL_SIZE - ((plugin.getLastTick() + 600 - System.currentTimeMillis()) / 600.0 * TICK_PIXEL_SIZE);

						final Polygon blob = new Polygon(new int[]{0, BLOB_WIDTH, BLOB_WIDTH, 0}, new int[]{0, 0, BLOB_HEIGHT, BLOB_HEIGHT}, 4);
						blob.translate(baseX, baseY);

						if (currentAttack == bestAttack)
						{
							OverlayUtil.renderFilledPolygon(graphics, blob, color);
						}
						else if (plugin.isIndicateNonPriorityDescendingBoxes())
						{
							OverlayUtil.renderOutlinePolygon(graphics, blob, color);
						}
					}
				}
			}

			if (closestAttack != null)
			{
				// Prayer indicator in prayer tab
				InfernoNPC.Attack prayerForAttack = null;
				if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC))
				{
					prayerForAttack = InfernoNPC.Attack.MAGIC;
				}
				else if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES))
				{
					prayerForAttack = InfernoNPC.Attack.RANGED;
				}
				else if (client.isPrayerActive(Prayer.PROTECT_FROM_MELEE))
				{
					prayerForAttack = InfernoNPC.Attack.MELEE;
				}

				if (closestAttack != prayerForAttack || plugin.isIndicateWhenPrayingCorrectly())
				{
					final Widget prayerWidget = client.getWidget(closestAttack.getPrayer().getWidgetInfo());
					final Polygon prayer = new Polygon(
						new int[]{0, (int) prayerWidget.getBounds().getWidth(), (int) prayerWidget.getBounds().getWidth(), 0},
						new int[]{0, 0, (int) prayerWidget.getBounds().getHeight(), (int) prayerWidget.getBounds().getHeight()},
						4);
					prayer.translate((int) prayerWidget.getBounds().getX(), (int) prayerWidget.getBounds().getY());

					//TODO: Config values for these colors
					Color prayerColor;
					if (closestAttack == prayerForAttack)
					{
						prayerColor = Color.GREEN;
					}
					else
					{
						prayerColor = Color.RED;
					}

					OverlayUtil.renderOutlinePolygon(graphics, prayer, prayerColor);
				}
			}
		}

		return null;
	}

	private boolean edgeEqualsEdge(int[][] edge1, int[][] edge2, int toleranceSquared)
	{
		if ((pointEqualsPoint(edge1[0], edge2[0], toleranceSquared) && pointEqualsPoint(edge1[1], edge2[1], toleranceSquared))
				|| (pointEqualsPoint(edge1[0], edge2[1], toleranceSquared) && pointEqualsPoint(edge1[1], edge2[0], toleranceSquared)))
		{
			return true;
		}

		return false;
	}

	private boolean pointEqualsPoint(int[] point1, int[] point2, int toleranceSquared)
	{
		double distanceSquared = Math.pow(point1[0] - point2[0], 2) + Math.pow(point1[1] - point2[1], 2);

		return distanceSquared <= toleranceSquared;
	}
}
