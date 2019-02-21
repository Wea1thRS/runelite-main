package net.runelite.client.plugins.inventorysetups;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;

import java.util.ArrayList;


@Slf4j
public class InventorySetup
{

	private ArrayList<InventorySetupItem> inventory;
	private ArrayList<InventorySetupItem> equipment;

	public InventorySetup(final ItemContainer inventoryToAdd, final ItemContainer equipmentToAdd, final ItemManager itemManager)
	{
		this.inventory = new ArrayList<>();
		this.equipment = new ArrayList<>();
		populateContainer(inventoryToAdd, inventory, itemManager);
		populateContainer(equipmentToAdd, equipment, itemManager);
	}

	public final ArrayList<InventorySetupItem> getInventory()
	{
		return inventory;
	}

	public final ArrayList<InventorySetupItem> getEquipment()
	{
		return equipment;
	}

	private void populateContainer(final ItemContainer container, final ArrayList<InventorySetupItem> containerToPopulate, final ItemManager itemManager)
	{
		Item[] items = null;
		if (container != null)
		{
			items = container.getItems();
		}

		if (items != null)
		{
			for (final Item item : items)
			{
				// get the item name from the client thread
				containerToPopulate.add(new InventorySetupItem(item.getId(), itemManager.getItemComposition(item.getId()).getName(), item.getQuantity()));
			}
		}
	}

}
