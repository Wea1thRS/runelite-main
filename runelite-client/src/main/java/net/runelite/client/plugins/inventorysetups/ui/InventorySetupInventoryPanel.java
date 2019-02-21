package net.runelite.client.plugins.inventorysetups.ui;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.inventorysetups.InventorySetup;
import net.runelite.client.plugins.inventorysetups.InventorySetupItem;
import net.runelite.client.plugins.inventorysetups.InventorySetupPlugin;
import net.runelite.client.ui.ColorScheme;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventorySetupInventoryPanel extends InventorySetupContainerPanel
{

	private static final int ITEMS_PER_ROW = 4;
	private static final int NUM_INVENTORY_ITEMS = 28;

	private ArrayList<InventorySetupSlot> inventorySlots;

	public InventorySetupInventoryPanel(final ItemManager itemManager, final InventorySetupPlugin plugin) {
		super(itemManager, plugin, "Inventory", "No inventory specified for this setup");
	}


	@Override
	public void setupContainerPanel(final JPanel containerSlotsPanel)
	{
		this.inventorySlots = new ArrayList<>();
		for (int i = 0; i < NUM_INVENTORY_ITEMS; i++)
		{
			inventorySlots.add(new InventorySetupSlot(ColorScheme.DARKER_GRAY_COLOR));
		}

		int numRows = (NUM_INVENTORY_ITEMS + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW;
		containerSlotsPanel.setLayout(new GridLayout(numRows, ITEMS_PER_ROW, 1, 1));
		for (int i = 0; i < NUM_INVENTORY_ITEMS; i++)
		{
			containerSlotsPanel.add(inventorySlots.get(i));
		}
	}

	public void setInventorySetupSlots(final InventorySetup setup)
	{
		ArrayList<InventorySetupItem> inventory = setup.getInventory();

		final AtomicBoolean hasInventory = new AtomicBoolean(false);
		for (int i = 0; i < NUM_INVENTORY_ITEMS; i++)
		{
			super.setContainerSlot(i, inventorySlots.get(i), inventory, hasInventory);
		}

		removeAll();
		add(hasInventory.get() ? containerPanel : emptyContainerPanel);

		validate();
		repaint();

	}

	public void highlightDifferentSlots(final ItemContainer currInventory, final InventorySetup inventorySetup) {

		Item[] items = null;
		if (currInventory != null) {
			items = currInventory.getItems();
		}

		final ArrayList<InventorySetupItem> inventoryToCheck = inventorySetup.getInventory();

		// check to see if the inventory is all empty
		boolean allEmpty = inventoryToCheck.isEmpty() || inventoryToCheck.stream().allMatch(item -> item.getId() == -1);

		// inventory setup is empty but the current inventory is not, make the text red
		if (allEmpty && items != null && items.length > 0)
		{
			super.modifyNoContainerCaption(inventoryToCheck, items);
			return;
		}

		for (int i = 0; i < NUM_INVENTORY_ITEMS; i++)
		{
			super.highlightDifferentSlotColor(inventoryToCheck, items, inventorySlots.get(i), i);
		}
	}

	public void resetInventorySlotsColor()
	{
		for (int i = 0; i < inventorySlots.size(); i++)
		{
			inventorySlots.get(i).setBackground(ColorScheme.DARKER_GRAY_COLOR);
		}

		emptyContainerLabel.setForeground(originalLabelColor);
	}

}
