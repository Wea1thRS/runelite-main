/*
 * Copyright (c) 2019, Jacky <liangj97@gmail.com>
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
package net.runelite.client.plugins.inferno;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.inferno.displaymodes.InfernoPrayerOverlayMode;
import net.runelite.client.plugins.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.plugins.inferno.displaymodes.SafespotDisplayMode;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "Inferno",
	description = "Inferno helper",
	tags = {"combat", "overlay", "pve", "pvm"},
	type = PluginType.PVM
)
@Slf4j
@Singleton
public class InfernoPlugin extends Plugin
{
	private static final int INFERNO_REGION = 9043;
	private static final int ZUK_SHIELD = 7707;
	private static List<String> NPCS_HIDDEN_ON_DEATH;
	static
	{
		ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
		builder.add("Jal-Nib");
		NPCS_HIDDEN_ON_DEATH = builder.build();
	}

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfernoOverlay infernoOverlay;

	@Inject
	private InfernoWaveOverlay waveOverlay;

	@Inject
	private InfernoInfoBoxOverlay jadOverlay;

	@Inject
	private InfernoOverlay prayerOverlay;

	@Inject
	private InfernoConfig config;

	@Inject
	private EventBus eventBus;

	@Getter(AccessLevel.PACKAGE)
	private InfernoConfig.FontStyle fontStyle = InfernoConfig.FontStyle.BOLD;

	@Getter(AccessLevel.PACKAGE)
	private int textSize = 32;

	//TODO: Why are there 2 waveNumbers ???
	@Getter(AccessLevel.PACKAGE)
	private int currentWave = -1;

	@Getter(AccessLevel.PACKAGE)
	private int currentWaveNumber;

	private final Map<NPC, InfernoNPC> infernoNpcs = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private List<WorldPoint> obstacles = new ArrayList<>();

	private NPC zukShield = null;
	private WorldPoint zukShieldLastPosition = null;
	private int zukShieldCornerTicks = -2;
	@Getter(AccessLevel.PACKAGE)
	private List<WorldPoint> zukShieldSafespots = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE)
	private boolean finalPhase = false;

	// 0 = total safespot
	// 1 = pray melee
	// 2 = pray range
	// 3 = pray magic
	// 4 = pray melee, range
	// 5 = pray melee, magic
	// 6 = pray range, magic
	// 7 = pray all
	@Getter(AccessLevel.PACKAGE)
	final HashMap<WorldPoint, Integer> safeSpotMap = new HashMap<>();
	@Getter(AccessLevel.PACKAGE)
	final HashMap<Integer, List<WorldPoint>> safeSpotAreas = new HashMap<>();

	//@Getter(AccessLevel.PACKAGE)
	//private final Map<Integer, ArrayList<InfernoNPC>> monsterCurrentAttackMap = new HashMap<>(6);

	//@Getter(AccessLevel.PACKAGE)
	//private final InfernoNPC[] priorityNPC = new InfernoNPC[4];

	@Getter
	private long lastTick;

	@Getter(AccessLevel.PACKAGE)
	private boolean displayNibblerOverlay;
	@Getter(AccessLevel.PACKAGE)
	private boolean showPrayerHelp;
	@Getter(AccessLevel.PACKAGE)
	private InfernoPrayerOverlayMode prayerOverlayMode;
	@Getter(AccessLevel.PACKAGE)
	private boolean descendingBoxes;
	@Getter(AccessLevel.PACKAGE)
	private boolean ticksOnNpc;
	private InfernoWaveDisplayMode waveDisplay;
	private Color getWaveOverlayHeaderColor;
	private Color getWaveTextColor;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateWhenPrayingCorrectly;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateActiveHealers;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateNonSafespotted;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateTemporarySafespotted;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateSafespotted;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateObstacles;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateNpcPosition;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateZukShieldSafespots;
	@Getter(AccessLevel.PACKAGE)
	private Color zukShieldSafespotsColor;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateNibblers;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateNonPriorityDescendingBoxes;
	@Getter(AccessLevel.PACKAGE)
	private boolean indicateBlobDetectionTick;
	@Getter(AccessLevel.PACKAGE)
	private SafespotDisplayMode indicateSafespots;
	@Getter(AccessLevel.PACKAGE)
	private int safespotsCheckSize;

	@Provides
	InfernoConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InfernoConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		updateConfig();
		addSubscriptions();

		waveOverlay.setDisplayMode(this.waveDisplay);
		waveOverlay.setWaveHeaderColor(this.getWaveOverlayHeaderColor);
		waveOverlay.setWaveTextColor(this.getWaveTextColor);

		if (isInInferno())
		{
			overlayManager.add(infernoOverlay);

			if (this.waveDisplay != InfernoWaveDisplayMode.NONE)
			{
				overlayManager.add(waveOverlay);
			}

			overlayManager.add(jadOverlay);
			overlayManager.add(prayerOverlay);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(this);

		overlayManager.remove(infernoOverlay);
		overlayManager.remove(waveOverlay);
		overlayManager.remove(jadOverlay);
		overlayManager.remove(prayerOverlay);

		currentWaveNumber = -1;
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
		eventBus.subscribe(NpcSpawned.class, this, this::onNpcSpawned);
		eventBus.subscribe(NpcDespawned.class, this, this::onNpcDespawned);
		eventBus.subscribe(GameStateChanged.class, this, this::onGameStateChanged);
		eventBus.subscribe(ChatMessage.class, this, this::onChatMessage);
		eventBus.subscribe(GameTick.class, this, this::onGameTick);
		eventBus.subscribe(AnimationChanged.class, this, this::onAnimationChanged);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!"inferno".equals(event.getGroup()))
		{
			return;
		}

		updateConfig();

		if (event.getKey().endsWith("color"))
		{
			waveOverlay.setWaveHeaderColor(this.getWaveOverlayHeaderColor);
			waveOverlay.setWaveTextColor(this.getWaveTextColor);
		}
		else if ("waveDisplay".equals(event.getKey()))
		{
			overlayManager.remove(waveOverlay);

			waveOverlay.setDisplayMode(this.waveDisplay);

			if (isInInferno() && this.waveDisplay != InfernoWaveDisplayMode.NONE)
			{
				overlayManager.add(waveOverlay);
			}
		}
	}

	private WorldPoint lastLocation = new WorldPoint(0, 0, 0);

	private void onGameTick(GameTick GameTickEvent)
	{
		if (!isInInferno())
		{
			return;
		}

		lastTick = System.currentTimeMillis();

		zukShieldSafespots.clear();
		if (zukShield != null)
		{
			for (int x = zukShield.getWorldLocation().getX() - 1; x <= zukShield.getWorldLocation().getX() + 3; x++)
			{
				for (int y = zukShield.getWorldLocation().getY() - 4; y <= zukShield.getWorldLocation().getY() - 2; y++)
				{
					zukShieldSafespots.add(new WorldPoint(x, y, client.getPlane()));
				}
			}

			final WorldPoint zukShieldCurrentPosition = zukShield.getWorldLocation();

			if (zukShieldLastPosition != null && zukShieldLastPosition.getX() != zukShieldCurrentPosition.getX()
				&& zukShieldCornerTicks == -2)
			{
				zukShieldCornerTicks = -1;
			}

			zukShieldLastPosition = zukShield.getWorldLocation();
		}

		obstacles.clear();
		for (NPC npc : client.getNpcs())
		{
			for (WorldPoint worldPoint : npc.getWorldArea().toWorldPointList())
			{
				obstacles.add(worldPoint);
			}
		}

		for (InfernoNPC infernoNPC : infernoNpcs.values())
		{
			infernoNPC.gameTick(client, lastLocation, finalPhase);

			if (infernoNPC.getType() == InfernoNPC.Type.ZUK && zukShieldCornerTicks == -1)
			{
				infernoNPC.updateNextAttack(InfernoNPC.Attack.UNKNOWN, 12);
				zukShieldCornerTicks = 0;
			}
		}

		int checkSize = (int) Math.floor(safespotsCheckSize / 2.0);

		safeSpotMap.clear();
		for (int x = -checkSize; x <= checkSize; x++)
		{
			for (int y = -checkSize; y <= checkSize; y++)
			{
				final WorldPoint checkLoc = client.getLocalPlayer().getWorldLocation().dx(x).dy(y);

				if (obstacles.contains(checkLoc))
				{
					continue;
				}

				safeSpotMap.put(checkLoc, 0);

				for (InfernoNPC infernoNPC : infernoNpcs.values())
				{
					if (infernoNPC.getType().getPriority() < 99 && infernoNPC.getType() != InfernoNPC.Type.JAD
							&& (infernoNPC.canAttack(client, checkLoc)
							|| infernoNPC.canMoveToAttack(client, checkLoc, obstacles)))
					{
						if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MELEE)
						{
							if (safeSpotMap.get(checkLoc) == 0)
							{
								safeSpotMap.put(checkLoc, 1);
							}
							else if (safeSpotMap.get(checkLoc) == 2)
							{
								safeSpotMap.put(checkLoc, 4);
							}
							else if (safeSpotMap.get(checkLoc) == 3)
							{
								safeSpotMap.put(checkLoc, 5);
							}
							else if (safeSpotMap.get(checkLoc) == 6)
							{
								safeSpotMap.put(checkLoc, 7);
							}
						}

						if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.RANGED
								|| (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.UNKNOWN
								&& safeSpotMap.get(checkLoc) != 3 &&  safeSpotMap.get(checkLoc) != 5))
						{
							if (safeSpotMap.get(checkLoc) == 0)
							{
								safeSpotMap.put(checkLoc, 2);
							}
							else if (safeSpotMap.get(checkLoc) == 1)
							{
								safeSpotMap.put(checkLoc, 4);
							}
							else if (safeSpotMap.get(checkLoc) == 3)
							{
								safeSpotMap.put(checkLoc, 6);
							}
							else if (safeSpotMap.get(checkLoc) == 4)
							{
								safeSpotMap.put(checkLoc, 7);
							}
						}

						if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MAGIC
								|| (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.UNKNOWN
								&& safeSpotMap.get(checkLoc) != 2 &&  safeSpotMap.get(checkLoc) != 4))
						{
							if (safeSpotMap.get(checkLoc) == 0)
							{
								safeSpotMap.put(checkLoc, 3);
							}
							else if (safeSpotMap.get(checkLoc) == 1)
							{
								safeSpotMap.put(checkLoc, 5);
							}
							else if (safeSpotMap.get(checkLoc) == 2)
							{
								safeSpotMap.put(checkLoc, 6);
							}
							else if (safeSpotMap.get(checkLoc) == 5)
							{
								safeSpotMap.put(checkLoc, 7);
							}
						}
					}
				}
			}
		}

		safeSpotAreas.clear();
		for (WorldPoint worldPoint : safeSpotMap.keySet())
		{
			if (!safeSpotAreas.containsKey(safeSpotMap.get(worldPoint)))
			{
				safeSpotAreas.put(safeSpotMap.get(worldPoint), new ArrayList<>());
			}

			safeSpotAreas.get(safeSpotMap.get(worldPoint)).add(worldPoint);
		}

		lastLocation = client.getLocalPlayer().getWorldLocation();
	}

	private void onNpcSpawned(NpcSpawned event)
	{
		if (!isInInferno())
		{
			return;
		}

		if (event.getNpc().getId() == ZUK_SHIELD)
		{
			zukShield = event.getNpc();
		}

		final InfernoNPC.Type infernoNPCType = InfernoNPC.Type.typeFromId(event.getNpc().getId());

		if (infernoNPCType == null)
		{
			return;
		}

		if (infernoNPCType == InfernoNPC.Type.ZUK)
		{
			System.out.println("Zuk spawn detected, not in final phase");
			finalPhase = false;
			zukShieldCornerTicks = -2;
			zukShieldLastPosition = null;
		}
		if (infernoNPCType == InfernoNPC.Type.HEALER_ZUK)
		{
			System.out.println("Final phase detected!");
			finalPhase = true;
		}

		infernoNpcs.put(event.getNpc(), new InfernoNPC(event.getNpc()));
	}

	private void onNpcDespawned(NpcDespawned event)
	{
		if (!isInInferno())
		{
			return;
		}

		if (event.getNpc().getId() == ZUK_SHIELD)
		{
			zukShield = null;
		}

		if (infernoNpcs.containsKey(event.getNpc()))
		{
			infernoNpcs.remove(event.getNpc());
		}
	}

	private void onAnimationChanged(AnimationChanged event)
	{
		if (!isInInferno())
		{
			return;
		}

		if (event.getActor() instanceof NPC)
		{
			final NPC npc = (NPC) event.getActor();

			if (ArrayUtils.contains(InfernoNPC.Type.NIBBLER.getNpcIds(), npc.getId())
				&& npc.getAnimation() == 7576)
			{
				infernoNpcs.remove(npc);
			}
		}
	}

	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!isInInferno())
		{
			currentWaveNumber = -1;

			overlayManager.remove(infernoOverlay);
			overlayManager.remove(waveOverlay);
			overlayManager.remove(jadOverlay);
			overlayManager.remove(prayerOverlay);
		}
		else if (currentWaveNumber == -1)
		{
			currentWaveNumber = 1;

			overlayManager.add(infernoOverlay);
			overlayManager.add(jadOverlay);

			//TODO: Only add if prayhelper is on. Also check in configChanged and in srartUp/shutDown
			overlayManager.add(prayerOverlay);

			if (this.waveDisplay != InfernoWaveDisplayMode.NONE)
			{
				overlayManager.add(waveOverlay);
			}
		}
	}

	private void onChatMessage(ChatMessage event)
	{
		if (!isInInferno() || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage();

		if (event.getMessage().contains("Wave:"))
		{
			message = message.substring(message.indexOf(": ") + 2);
			currentWaveNumber = Integer.parseInt(message.substring(0, message.indexOf("<")));
		}
	}

	HashMap<NPC, InfernoNPC> getInfernoNpcs()
	{
		final HashMap<NPC, InfernoNPC> sortedInfernoNpcs = new HashMap<>();

		for (Map.Entry<NPC, InfernoNPC> entry : infernoNpcs.entrySet())
		{
			if (entry.getValue().getType() == InfernoNPC.Type.BLOB)
			{
				continue;
			}

			sortedInfernoNpcs.put(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<NPC, InfernoNPC> entry : infernoNpcs.entrySet())
		{
			if (entry.getValue().getType() != InfernoNPC.Type.BLOB)
			{
				continue;
			}

			sortedInfernoNpcs.put(entry.getKey(), entry.getValue());
		}

		return sortedInfernoNpcs;
	}

	private boolean isInInferno()
	{
		return ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION);
	}

	int getNextWaveNumber()
	{
		return currentWaveNumber == -1 || currentWaveNumber == 69 ? -1 : currentWaveNumber + 1;
	}

	private void updateConfig()
	{
		this.displayNibblerOverlay = config.displayNibblerOverlay();
		this.showPrayerHelp = config.showPrayerHelp();
		this.prayerOverlayMode = config.prayerOverlayMode();
		this.ticksOnNpc = config.ticksOnNpc();
		this.waveDisplay = config.waveDisplay();
		this.getWaveOverlayHeaderColor = config.getWaveOverlayHeaderColor();
		this.getWaveTextColor = config.getWaveTextColor();
		this.descendingBoxes = config.descendingBoxes();
		this.indicateWhenPrayingCorrectly = config.indicateWhenPrayingCorrectly();
		this.indicateActiveHealers = config.indicateActiveHealers();
		this.indicateNonSafespotted = config.indicateNonSafespotted();
		this.indicateTemporarySafespotted = config.indicateTemporarySafespotted();
		this.indicateSafespotted = config.indicateSafespotted();
		this.indicateObstacles = config.indicateObstacles();
		this.indicateNpcPosition = config.indicateNpcPosition();
		this.indicateZukShieldSafespots = config.indicateZukShieldSafespots();
		this.zukShieldSafespotsColor = config.zukShieldSafespotsColor();
		this.indicateNibblers = config.indicateNibblers();
		this.indicateNonPriorityDescendingBoxes = config.indicateNonPriorityDescendingBoxes();
		this.indicateBlobDetectionTick = config.indicateBlobDetectionTick();
		this.indicateSafespots = config.indicateSafespots();
		this.safespotsCheckSize = config.safespotsCheckSize();
	}
}
