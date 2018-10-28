package net.runelite.client.plugins.inventorypresets;

import net.runelite.api.ItemContainer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class InventoryPresets
{
	private long id;
	private String name;
	private boolean visible;
	private ItemContainer inventory;
	private ItemContainer equipment;
}
