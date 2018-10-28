package net.runelite.client.plugins.inventorypresets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPresets
{
	private long id;
	private String name;
	private boolean visible;
}
