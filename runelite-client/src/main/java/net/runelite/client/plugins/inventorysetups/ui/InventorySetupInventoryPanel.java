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

	public void highlightDifferentSlots(final ArrayList<InventorySetupItem> currInventory, final InventorySetup inventorySetup) {

		final ArrayList<InventorySetupItem> inventoryToCheck = inventorySetup.getInventory();

		assert currInventory.size() == inventoryToCheck.size() : "size mismatch";

		// check to see if the inventory is all empty
		boolean allEmpty = inventoryToCheck.stream().allMatch(item -> item.getId() == -1);

		// inventory setup is empty but the current inventory is not, make the text red
		if (allEmpty)
		{
			super.modifyNoContainerCaption(currInventory);
			return;
		}

		for (int i = 0; i < NUM_INVENTORY_ITEMS; i++)
		{
			super.highlightDifferentSlotColor(inventoryToCheck.get(i), currInventory.get(i), inventorySlots.get(i));
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
