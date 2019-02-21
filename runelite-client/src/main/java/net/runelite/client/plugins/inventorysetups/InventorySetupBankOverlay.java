package net.runelite.client.plugins.inventorysetups;

import net.runelite.api.Client;
import net.runelite.api.Query;
import net.runelite.api.SpritePixels;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.util.QueryRunner;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class InventorySetupBankOverlay extends Overlay
{
	private final Client client;
	private final QueryRunner queryRunner;
	private final InventorySetupPlugin plugin;
	private final InventorySetupConfig config;

	@Inject
	public InventorySetupBankOverlay(Client client, QueryRunner queryRunner, ItemManager itemManager, InventorySetupPlugin plugin, InventorySetupConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		this.client = client;
		this.queryRunner = queryRunner;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.getBankHighlight())
		{
			int[] ids = plugin.getCurrentInventorySetupIds();
			if(ids == null)
			{
				return null;
			}
			final Query query = new BankItemQuery().idEquals(ids);
			final WidgetItem[] widgetItems = queryRunner.runQuery(query);

			for (final WidgetItem item : widgetItems)
			{
				if (item.getId() == -1)
				{
					return null;
				}

				final Color color = config.getBankHighlightColor();

				if (color != null)
				{
					final BufferedImage outline = loadItemOutline(item.getId(), item.getQuantity(), color);
					graphics.drawImage(outline, item.getCanvasLocation().getX() + 1, item.getCanvasLocation().getY() + 1, null);
				}

				client.getWidget(WidgetInfo.BANK_CONTAINER).revalidate();
			}
		}
		return null;
	}

	private BufferedImage loadItemOutline(final int itemId, final int itemQuantity, final Color outlineColor)
	{
		final SpritePixels itemSprite = client.createItemSprite(itemId, itemQuantity, 2, 0, 0, true, 710);
		return itemSprite.toBufferedOutline(outlineColor);
	}
}