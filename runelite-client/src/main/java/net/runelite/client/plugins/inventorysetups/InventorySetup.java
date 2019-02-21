package net.runelite.client.plugins.inventorysetups;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

import java.util.ArrayList;

public class InventorySetup {

    private ArrayList<InventorySetupItem> inventory;
    private ArrayList<InventorySetupItem> equipment;

    public InventorySetup(final ItemContainer inventoryToAdd, final ItemContainer equipmentToAdd, final ItemManager itemManager, final ClientThread clientThread)
    {
    	this.inventory = new ArrayList<>();
    	this.equipment = new ArrayList<>();
    	populateContainer(inventoryToAdd, inventory, clientThread, itemManager);
    	populateContainer(equipmentToAdd, equipment, clientThread, itemManager);
    }

    public final ArrayList<InventorySetupItem> getInventory()
    {
    	return inventory;
    }

    public final ArrayList<InventorySetupItem> getEquipment()
    {
    	return equipment;
    }

    private void populateContainer(final ItemContainer container, final ArrayList<InventorySetupItem> containerToPopulate, ClientThread clientThread, final ItemManager itemManager)
    {
    	Item[] items = null;
    	if (container != null)
	    {
	    	items = container.getItems();
	    }

    	if (items != null)
	    {
		    for (int i = 0; i < items.length; i++)
		    {
			    final Item item = items[i];
			    final StringBuilder nameBuilder = new StringBuilder();

			    // get the item name from the client thread
			    clientThread.invoke(() ->  nameBuilder.append(itemManager.getItemComposition(item.getId()).getName()));
			    containerToPopulate.add(new InventorySetupItem(item.getId(), nameBuilder.toString(), item.getQuantity()));
		    }
	    }
    }

}
