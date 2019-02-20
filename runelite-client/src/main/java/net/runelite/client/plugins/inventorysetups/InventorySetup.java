package net.runelite.client.plugins.inventorysetups;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.http.api.loottracker.GameItem;

import java.util.ArrayList;

@Slf4j
public class InventorySetup {

	// TODO use GameItem
    private ArrayList<GameItem> inventoryIds;
    private ArrayList<GameItem> equipmentIds;

    InventorySetup(final ItemContainer inventory, final ItemContainer equipment)
    {
    	Item[] invIds = null;
    	if (inventory != null)
	    {
	    	invIds = inventory.getItems();
	    }

	    Item[] equipIds = null;
    	if (equipment != null)
	    {
	    	equipIds = equipment.getItems();
	    }

        inventoryIds = new ArrayList<>();
        equipmentIds = new ArrayList<>();

        if (invIds != null)
        {
			for (final Item item : invIds)
			{
				inventoryIds.add(new GameItem(item.getId(), item.getQuantity()));
			}
        }

        if (equipIds != null)
        {
			for (final Item item : equipIds)
			{
				equipmentIds.add(new GameItem(item.getId(), item.getQuantity()));
			}
        }
    }

    public final ArrayList<GameItem> getInventory()
    {
    	return inventoryIds;
    }

    public final ArrayList<GameItem> getEquipment()
    {
    	return equipmentIds;
    }

}
