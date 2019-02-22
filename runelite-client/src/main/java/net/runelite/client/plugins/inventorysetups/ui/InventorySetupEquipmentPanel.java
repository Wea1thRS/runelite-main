package net.runelite.client.plugins.inventorysetups.ui;

import net.runelite.api.EquipmentInventorySlot;
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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventorySetupEquipmentPanel extends InventorySetupContainerPanel
{
	private HashMap<EquipmentInventorySlot, InventorySetupSlot> equipmentSlots;

	public InventorySetupEquipmentPanel(final ItemManager itemManager, final InventorySetupPlugin plugin)
	{
		super(itemManager, plugin, "Equipment", "No equipment for this setup.");
	}

	@Override
	public void setupContainerPanel(final JPanel containerSlotsPanel)
	{
		this.equipmentSlots = new HashMap<>();
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			equipmentSlots.put(slot, new InventorySetupSlot(ColorScheme.DARKER_GRAY_COLOR));
		}

		final GridLayout gridLayout = new GridLayout(5, 3, 1, 1);
		containerSlotsPanel.setLayout(gridLayout);

		// add the grid layouts, including invisible ones
		containerSlotsPanel.add(new InventorySetupSlot(ColorScheme.DARK_GRAY_COLOR));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.HEAD));
		containerSlotsPanel.add(new InventorySetupSlot(ColorScheme.DARK_GRAY_COLOR));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.CAPE));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.AMULET));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.AMMO));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.WEAPON));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.BODY));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.SHIELD));
		containerSlotsPanel.add(new InventorySetupSlot(ColorScheme.DARK_GRAY_COLOR));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.LEGS));
		containerSlotsPanel.add(new InventorySetupSlot(ColorScheme.DARK_GRAY_COLOR));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.GLOVES));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.BOOTS));
		containerSlotsPanel.add(equipmentSlots.get(EquipmentInventorySlot.RING));
	}

	public void setEquipmentSetupSlots(final InventorySetup setup)
	{
		final ArrayList<InventorySetupItem> equipment = setup.getEquipment();

		final AtomicBoolean hasEquipment = new AtomicBoolean(false);
		for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			int i = slot.getSlotIdx();
			super.setContainerSlot(i, equipmentSlots.get(slot), equipment, hasEquipment);
		}

		removeAll();
		add(hasEquipment.get() ? containerPanel : emptyContainerPanel);

		validate();
		repaint();

	}

	public void highlightDifferences(final ArrayList<InventorySetupItem> currEquipment, final InventorySetup inventorySetup)
	{
		final ArrayList<InventorySetupItem> equipToCheck = inventorySetup.getEquipment();

		// check to see if the inventory is all empty
		boolean allEmpty = equipToCheck.stream().allMatch(item -> item.getId() == -1);

		// equipment setup is empty but the current equipment is not, make the text red
		if (allEmpty)
		{
			super.modifyNoContainerCaption(currEquipment);
			return;
		}

		for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{

			int slotIdx = slot.getSlotIdx();
			super.highlightDifferentSlotColor(equipToCheck.get(slotIdx), currEquipment.get(slotIdx), equipmentSlots.get(slot));
		}
	}

	public void resetEquipmentSlotsColor()
	{
		for (final EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			equipmentSlots.get(slot).setBackground(ColorScheme.DARKER_GRAY_COLOR);
		}

		emptyContainerLabel.setForeground(originalLabelColor);
	}

}
