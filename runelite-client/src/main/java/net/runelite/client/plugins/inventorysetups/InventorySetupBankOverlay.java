package net.runelite.client.plugins.inventorysetups;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.SpritePixels;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

@Slf4j
public class InventorySetupBankOverlay extends WidgetItemOverlay
{
	private final Client client;
	private final InventorySetupPlugin plugin;
	private final InventorySetupConfig config;

	@Inject
	public InventorySetupBankOverlay(Client client, InventorySetupPlugin plugin, InventorySetupConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		showOnBank();
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		if (config.getBankHighlight())
		{
			int[] ids = plugin.getCurrentInventorySetupIds();
			if (ids == null)
			{
				return;
			}
			ids = Arrays.stream(ids)
					.filter(Objects::nonNull)
					.filter(id -> id != -1)
					.toArray();
			if (IntStream.of(ids).noneMatch(x -> x == itemWidget.getId()))
			{
				return;
			}
			final Widget bankContainer = client.getWidget(WidgetInfo.BANK_CONTAINER);
			Point canvasLocation = itemWidget.getCanvasLocation();
			Rectangle canvasBounds = itemWidget.getCanvasBounds();
			Point windowLocation = bankContainer.getCanvasLocation();

			if (canvasLocation == null || windowLocation == null)
			{
				return;
			}

			if (!(canvasLocation.getY() + 60 >= windowLocation.getY() + bankContainer.getHeight()) && !(canvasLocation.getY() + canvasBounds.getHeight() <= windowLocation.getY() + 90))
			{
				final Color color = config.getBankHighlightColor();

				if (color != null)
				{
					final BufferedImage outline = loadItemOutline(itemWidget.getId(), itemWidget.getQuantity(), color);
					graphics.drawImage(outline, itemWidget.getCanvasLocation().getX() + 1, itemWidget.getCanvasLocation().getY() + 1, null);
					if (itemWidget.getQuantity() > 1)
					{
						drawQuantity(graphics, itemWidget, Color.YELLOW);
					}
					else if (itemWidget.getQuantity() == 0)
					{
						drawQuantity(graphics, itemWidget, Color.YELLOW.darker());
					}
				}
			}
		}
	}

	private void drawQuantity(Graphics2D graphics, WidgetItem itemWidget, Color darker)
	{
		graphics.setColor(Color.BLACK);
		graphics.drawString(String.valueOf(itemWidget.getQuantity()), itemWidget.getCanvasLocation().getX() + 1, itemWidget.getCanvasLocation().getY() + 10);
		graphics.setColor(darker);
		graphics.setFont(FontManager.getRunescapeSmallFont());
		graphics.drawString(String.valueOf(itemWidget.getQuantity()), itemWidget.getCanvasLocation().getX(), itemWidget.getCanvasLocation().getY() + 9);
	}

	private BufferedImage loadItemOutline(final int itemId, final int itemQuantity, final Color outlineColor)
	{
		final SpritePixels itemSprite = client.createItemSprite(itemId, itemQuantity, 2, 0, 0, true, 710);
		return itemSprite.toBufferedOutline(outlineColor);
	}
}