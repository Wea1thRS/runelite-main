package net.runelite.client.plugins.inventorysetups;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.http.api.loottracker.GameItem;

import java.util.ArrayList;

/**
 * Created by Dillon on 1/31/2019.
 */
public class InventorySetup {

	// TODO use GameItem
    private ArrayList<GameItem> inventoryIds;
    private ArrayList<GameItem> equipmentIds;

    public InventorySetup(final ItemContainer inventory, final ItemContainer equipment)
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
            for (int i = 0; i < invIds.length; i++)
            {
                final Item item  = invIds[i];
                inventoryIds.add(new GameItem(item.getId(), item.getQuantity()));
            }
        }

        if (equipIds != null)
        {
            for (int i = 0; i < equipIds.length; i++)
            {
                final Item item = equipIds[i];
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
