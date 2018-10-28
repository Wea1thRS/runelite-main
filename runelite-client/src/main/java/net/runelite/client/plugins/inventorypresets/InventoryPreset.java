package net.runelite.client.plugins.inventorypresets;

import lombok.Getter;
import net.runelite.api.ItemContainer;

public class InventoryPreset
{
	@Getter
	private final InventoryPresets preset;

	InventoryPreset(InventoryPresets preset)
	{
		this.preset = preset;
	}

	public InventoryPreset(long id, String name, boolean visible, ItemContainer inventory, ItemContainer equipment)
	{
		this.preset = new InventoryPresets(id, name, visible, inventory, equipment);
	}

}
