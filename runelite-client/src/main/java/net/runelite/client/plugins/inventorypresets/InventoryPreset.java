package net.runelite.client.plugins.inventorypresets;

import lombok.Getter;

public class InventoryPreset
{
	@Getter
	private final InventoryPresets preset;

	InventoryPreset(InventoryPresets preset)
	{
		this.preset = preset;
	}

	public String getName()
	{
		return "preset" + preset.getId();
	}

}
