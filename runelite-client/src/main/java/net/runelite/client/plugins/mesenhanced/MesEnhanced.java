/*
 * Copyright (c) 2019, Ganom <https://github.com/ganom>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.mesenhanced;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Menu;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.spellbook.Spellbook;

@PluginDescriptor(
	name = "Mes Enhanced",
	description = "1 Click MES Features -DO NOT ABUSE/AUTOCLICK THESE-",
	type = PluginType.UTILITY
)
public class MesEnhanced extends Plugin
{
	private static final int GOBLIN_SALUTE = 2128;
	private static final String LIGHT = "Light";
	private static final String QUICK_BONE = "Quick Bone";
	private static final String CHISEL = "Chisel";
	private static final String COOK = "BURN";
	private static final String LAVAIFY = "Lava-ify";
	private static final String NPC_CONTACT = "NPC Contact";
	private static final Set<Integer> TINDER = ImmutableSet.of(
		ItemID.TINDERBOX, ItemID.GOLDEN_TINDERBOX
	);
	private static final Set<Integer> CHISELS = ImmutableSet.of(
		ItemID.CHISEL
	);
	private static final Set<Integer> LIGHTABLE_LOGS = ImmutableSet.of(
		ItemID.LOGS, ItemID.ACHEY_TREE_LOGS, ItemID.OAK_LOGS,
		ItemID.WILLOW_LOGS, ItemID.TEAK_LOGS, ItemID.ARCTIC_PINE_LOGS, ItemID.MAPLE_LOGS,
		ItemID.MAHOGANY_LOGS, ItemID.YEW_LOGS, ItemID.MAGIC_LOGS, ItemID.REDWOOD_LOGS
	);
	private static final Set<Integer> BONES = ImmutableSet.of(
		ItemID.BONES, ItemID.BIG_BONES, ItemID.JOGRE_BONES, ItemID.ZOGRE_BONES,
		ItemID.BABYDRAGON_BONES, ItemID.WYRM_BONES, ItemID.DRAGON_BONES,
		ItemID.WYVERN_BONES, ItemID.DRAKE_BONES, ItemID.LAVA_DRAGON_BONES,
		ItemID.FAYRG_BONES, ItemID.RAURG_BONES, ItemID.HYDRA_BONES,
		ItemID.DAGANNOTH_BONES, ItemID.OURG_BONES, ItemID.SUPERIOR_DRAGON_BONES
	);
	private static final Set<Integer> FOOD = ImmutableSet.of(
		ItemID.RAW_KARAMBWAN
	);
	private static final Set<Integer> CHISELABLE_ITEMS = ImmutableSet.of(
		ItemID.DARK_ESSENCE_BLOCK
	);
	private static final Set<Integer> RUNES = ImmutableSet.of(
		ItemID.EARTH_RUNE
	);
	private static final Set<Integer> DEPLETED_POUCHES = ImmutableSet.of(
		ItemID.MEDIUM_POUCH_5511,
		ItemID.LARGE_POUCH_5513,
		ItemID.GIANT_POUCH_5515
	);

	@Inject
	private Client client;
	@Inject
	private EventBus eventBus;
	@Inject
	private MesEnhancedConfig config;
	private boolean bones;
	private boolean food;
	private boolean runes;
	private boolean leftClickLog;
	private boolean quickBone;
	private boolean quickBurn;
	private boolean lavaStuff;
	private boolean leftClickEssenceBlock;
	//private boolean npcContactPouch;
	private boolean tick;
	private boolean tinder;
	private boolean chisel;
	//private boolean npcPouch;
	private int bonesId;
	private int bonesIdx;
	private int foodId;
	private int foodIdx;
	private int tinderId;
	private int tinderIdx;
	private int chiselId;
	private int chiselIdx;
	private int earthId;
	private int earthIdx;

	@Provides
	MesEnhancedConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MesEnhancedConfig.class);
	}

	@Override
	public void startUp()
	{
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(ItemContainerChanged.class, this, this::onItemContainerChanged);
		eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded);
		eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked);
	}

	@Override
	public void shutDown()
	{
		eventBus.unregister(this);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("mesEnhanced"))
		{
			return;
		}

		this.quickBone = config.quickBones();
		this.leftClickLog = config.leftClickLog();
		this.leftClickEssenceBlock = config.leftClickEssenceBlock();
		this.quickBurn = config.quickBurn();
		this.lavaStuff = config.lavaStuff();
		//this.npcContactPouch = config.npcContactPouch();
	}

	private void onGameTick(GameTick event)
	{
		if (tick)
		{
			tick = false;
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != client.getLocalPlayer() || event.getActor().getAnimation() != 3705 || !bones || !food)
		{
			return;
		}

		event.getActor().setActionFrame(0);
		event.getActor().setAnimation(GOBLIN_SALUTE);
	}

	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!this.leftClickLog && !this.quickBone && !this.leftClickEssenceBlock && !this.quickBurn && !this.lavaStuff)
		{
			return;
		}

		final int id = event.getIdentifier();

		if (this.leftClickLog && tinder && event.getType() == MenuOpcode.ITEM_USE.getId()
			&& LIGHTABLE_LOGS.contains(id))
		{
			event.getMenuEntry().setOption(LIGHT);
			event.setWasModified(true);
		}
		else if (this.leftClickEssenceBlock && chisel && event.getType() == MenuOpcode.ITEM_USE.getId()
			&& CHISELABLE_ITEMS.contains(id))
		{
			event.getMenuEntry().setOption(CHISEL);
			event.setWasModified(true);
		}
		/*else if (this.npcContactPouch && npcPouch && event.getType() == MenuOpcode.ITEM_USE.getId()
			&& DEPLETED_POUCHES.contains(id))
		{
			event.getMenuEntry().setOption(NPC_CONTACT);
			event.setWasModified(true);
		}*/
		else if (this.quickBone && bones && event.getType() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& event.getTarget().toLowerCase().contains("altar"))
		{
			event.getMenuEntry().setOption(QUICK_BONE);
			event.setWasModified(true);
		}
		else if (this.quickBurn && food && event.getType() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& (event.getTarget().toLowerCase().contains("range") || event.getTarget().toLowerCase().contains("fire")))
		{
			event.getMenuEntry().setOption(COOK);
			event.setWasModified(true);
		}
		else if (this.lavaStuff && runes && event.getType() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& event.getTarget().toLowerCase().contains("altar"))
		{
			event.getMenuEntry().setOption(LAVAIFY);
			event.setWasModified(true);
		}
	}

	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!this.leftClickLog && !this.quickBone && !this.leftClickEssenceBlock && !this.quickBurn && !this.lavaStuff)
		{
			return;
		}

		final MenuEntry entry = event.getMenuEntry();

		if (this.leftClickLog && tinder && event.getOpcode() == MenuOpcode.ITEM_USE.getId()
			&& event.getOption().equals(LIGHT))
		{
			entry.setOpcode(MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId());
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(tinderIdx);
			client.setSelectedItemID(tinderId);
		}
		else if (this.leftClickEssenceBlock && chisel && event.getOpcode() == MenuOpcode.ITEM_USE.getId()
			&& event.getOption().equals(CHISEL))
		{
			entry.setOpcode(MenuOpcode.ITEM_USE_ON_WIDGET_ITEM.getId());
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(chiselIdx);
			client.setSelectedItemID(chiselId);
		}
		/*else if (this.npcContactPouch && npcPouch && event.getOpcode() == MenuOpcode.ITEM_USE.getId()
			&& event.getOption().equals(NPC_CONTACT))
		{
			this.setSelectSpell(WidgetInfo.SPELL_NPC_CONTACT);
			entry.setIdentifier(1);
			entry.setOpcode(MenuOpcode.WIDGET_DEFAULT.getId());
			entry.setParam0(-1);
			entry.setParam1(WidgetInfo.SPELL_NPC_CONTACT.getId());
			entry.setOption("Dark Mage");
		}*/
		else if (this.quickBone && bones && event.getOption().equals(QUICK_BONE) && tick)
		{
			event.consume();
		}
		else if (this.quickBone && bones && event.getOpcode() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& event.getOption().equals(QUICK_BONE))
		{
			entry.setOpcode(MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId());
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(bonesIdx);
			client.setSelectedItemID(bonesId);
			tick = true;
		}
		else if (this.quickBurn && food && event.getOption().equals(COOK) && tick)
		{
			event.consume();
		}
		else if (this.quickBurn && food && event.getOpcode() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& event.getOption().equals(COOK))
		{
			entry.setOpcode(MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId());
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(foodIdx);
			client.setSelectedItemID(foodId);
			tick = true;
		}
		else if (this.lavaStuff && runes && event.getOpcode() == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId()
			&& event.getOption().equals(LAVAIFY))
		{
			entry.setOpcode(MenuOpcode.ITEM_USE_ON_GAME_OBJECT.getId());
			client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
			client.setSelectedItemSlot(earthIdx);
			client.setSelectedItemID(earthId);
		}
	}

	private void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!this.leftClickLog && !this.quickBone && !this.leftClickEssenceBlock && !this.quickBurn && !this.lavaStuff)
		{
			return;
		}

		final ItemContainer itemContainer = event.getItemContainer();
		final List<Item> items = Arrays.asList(itemContainer.getItems());

		if (itemContainer != client.getItemContainer(InventoryID.INVENTORY) ||
			(!Collections.disjoint(items, BONES) && !Collections.disjoint(items, LIGHTABLE_LOGS) && !Collections.disjoint(items, CHISELABLE_ITEMS)))
		{
			return;
		}

		tinderIdx = -1;
		tinderId = -1;
		chiselIdx = -1;
		chiselId = -1;
		bonesIdx = -1;
		bonesId = -1;
		foodIdx = -1;
		foodId = -1;
		earthIdx = -1;
		earthId = -1;
		tinder = false;
		chisel = false;
		bones = false;
		food = false;
		runes = false;

		for (int i = 0; i < items.size(); i++)
		{
			final int itemId = items.get(i).getId();

			if (TINDER.contains(itemId))
			{
				tinderIdx = i;
				tinderId = itemId;
				tinder = true;
			}
			else if (CHISELS.contains(itemId))
			{
				chiselIdx = i;
				chiselId = itemId;
				chisel = true;
			}
			else if (BONES.contains(itemId))
			{
				bonesIdx = i;
				bonesId = itemId;
				bones = true;
				break;
			}
			else if (FOOD.contains(itemId))
			{
				foodIdx = i;
				foodId = itemId;
				food = true;
				break;
			}
			else if (RUNES.contains(itemId))
			{
				earthIdx = i;
				earthId = itemId;
				runes = true;
				break;
			}
		}
	}

	private void setSelectSpell(WidgetInfo info)
	{
		Widget widget = client.getWidget(info);
		client.setSelectedSpellName(widget.getName());
		client.setSelectedSpellWidget(widget.getId());
		client.setSelectedSpellChildIndex(-1);
	}
}