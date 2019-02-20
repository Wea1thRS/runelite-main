package net.runelite.client.plugins.inventorysetups.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.client.game.AsyncBufferedImage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.inventorysetups.InventorySetupConfig;
import net.runelite.client.plugins.inventorysetups.InventorySetupPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.http.api.loottracker.GameItem;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class InventorySetupContainerPanel extends JPanel
{

	protected ItemManager itemManager;

	JPanel containerPanel;

	JPanel emptyContainerPanel;

	JLabel emptyContainerLabel;

	final Color originalLabelColor;

	private final InventorySetupPlugin plugin;

	InventorySetupContainerPanel(final ItemManager itemManager, final InventorySetupPlugin plugin, String captionText, final String emptyContainerText)
	{
		this.itemManager = itemManager;
		this.plugin = plugin;
		this.containerPanel = new JPanel();
		this.emptyContainerPanel = new JPanel();
		this.emptyContainerLabel = new JLabel(emptyContainerText);
		this.originalLabelColor = emptyContainerLabel.getForeground();

		emptyContainerPanel.add(emptyContainerLabel);

		final JPanel containerSlotsPanel = new JPanel();

		setupContainerPanel(containerSlotsPanel);

		// caption
		final JLabel caption = new JLabel(captionText);
		caption.setHorizontalAlignment(JLabel.CENTER);
		caption.setVerticalAlignment(JLabel.CENTER);

		// panel that holds the caption and any other graphics
		final JPanel captionPanel = new JPanel();
		captionPanel.add(caption);

		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(captionPanel, BorderLayout.NORTH);
		containerPanel.add(containerSlotsPanel, BorderLayout.CENTER);
	}

	void setContainerSlot(int index,
	                             final InventorySetupSlot containerSlot,
	                             final ArrayList<GameItem> items,
                                 final AtomicBoolean hasItems)
	{
		if (index >= items.size() || items.get(index).getId() == -1)
		{
			containerSlot.setImageLabel(null, null);
			return;
		}

		hasItems.set(true);

		int itemId = items.get(index).getId();
		int quantity = items.get(index).getQty();
		AsyncBufferedImage itemImg = itemManager.getImage(itemId, quantity, quantity > 1);
		String toolTip = itemManager.getItemComposition(itemId).getName();

		if (quantity > 1)
		{
			toolTip += " (" + quantity + ")";
		}
		containerSlot.setImageLabel(toolTip, itemImg);
	}

	void modifyNoContainerCaption(final Item[] currContainer)
	{
		// inventory setup is empty but the current inventory is not, make the text red
		boolean hasDifference = false;
		for (Item item : currContainer)
		{
			if (item.getId() != -1)
			{
				hasDifference = true;
				break;
			}
		}

		if (hasDifference)
		{
			final Color highlightColor = plugin.getConfig().getHighlightColor();
			emptyContainerLabel.setForeground(highlightColor);
		}
		else
		{
			emptyContainerLabel.setForeground(this.originalLabelColor);
		}

	}

	void highlightDifferentSlotColor(final ArrayList<GameItem> containerToCheck,
	                                     final Item[] currContainer,
	                                     final InventorySetupSlot containerSlot,
	                                     int index)
	{
		final InventorySetupConfig config = plugin.getConfig();
		final Color highlightColor = config.getHighlightColor();
		// both inventories are smaller than the current iteration, no need to change
		if (index >= containerToCheck.size() && (currContainer == null || index >= currContainer.length))
		{
			containerSlot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			return;
		}

		// the current inventory is smaller in size than the inventory to check
		if ((currContainer == null || index >= currContainer.length) && index < containerToCheck.size())
		{
			if (containerToCheck.get(index).getId() != -1)
			{
				containerSlot.setBackground(highlightColor);
			}
			return;
		}

		// the inventory to check is smaller than the current inventory
		if (index >= containerToCheck.size() && (currContainer != null && index < currContainer.length))
		{
			if (currContainer[index].getId() != -1)
			{
				containerSlot.setBackground(highlightColor);
			}
			else
			{
				containerSlot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
			return;
		}

		int currId = currContainer[index].getId();
		int checkId = containerToCheck.get(index).getId();

		if (!config.getVariationDifference())
		{
			currId = ItemVariationMapping.map(currId);
			checkId = ItemVariationMapping.map(checkId);
		}

		if (config.getStackDifference() && currContainer[index].getQuantity() != containerToCheck.get(index).getQty())
		{
			containerSlot.setBackground(highlightColor);
			return;
		}

		if (currId != checkId)
		{
			containerSlot.setBackground(highlightColor);
			return;
		}

		// set the color back to the original, because they match
		containerSlot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
	}

	abstract public void setupContainerPanel(final JPanel containerSlotsPanel);

}
