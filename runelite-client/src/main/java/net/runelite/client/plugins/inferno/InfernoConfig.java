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

import java.awt.Color;
import java.awt.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Stub;
import net.runelite.client.plugins.inferno.displaymodes.InfernoPrayerOverlayMode;
import net.runelite.client.plugins.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.plugins.inferno.displaymodes.SafespotDisplayMode;

@ConfigGroup("inferno")
public interface InfernoConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "prayer",
		name = "Prayer",
		description = ""
	)
	default Stub prayer()
	{
		return new Stub();
	}

	@ConfigItem(
		position = 1,
		keyName = "Prayer Helper",
		name = "Prayer Helper",
		description = "Indicates the correct prayer"
	)
	default boolean showPrayerHelp()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "prayerHelperMode",
		name = "Prayer Helper Mode",
		description = "Display prayer indicator in the prayer tab or in the bottom right corner of the screen"
	)
	default InfernoPrayerOverlayMode prayerOverlayMode()
	{
		return InfernoPrayerOverlayMode.PRAYER_TAB;
	}

	@ConfigItem(
		position = 3,
		keyName = "ticksOnNpc",
		name = "Ticks on NPC",
		description = "Draws the amount of ticks before an NPC is going to attack on the NPC"
	)
	default boolean ticksOnNpc()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "descendingBoxes",
		name = "Descending Boxes",
		description = "Draws timing boxes above the prayer icons, as if you were playing Piano Tiles"
	)
	default boolean descendingBoxes()
	{
		return true;
	}

	@ConfigItem(
		position = 5,
		keyName = "indicateWhenPrayingCorrectly",
		name = "Indicate When Praying Correctly",
		description = "Indicate the correct prayer, even if you are already praying that prayer"
	)
	default boolean indicateWhenPrayingCorrectly()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "monsters",
		name = "Monsters",
		description = ""
	)
	default Stub monsters()
	{
		return new Stub();
	}

	@ConfigItem(
		position = 7,
		keyName = "Nibbler Overlay",
		name = "Nibbler Overlay",
		description = "Shows if there are any Nibblers left"
	)
	default boolean displayNibblerOverlay()
	{
		return false;
	}

	@ConfigItem(
		position = 8,
		keyName = "indicateActiveHealers",
		name = "Indicate Active Healers",
		description = "Indicate healers that are still healing Jad"
	)
	default boolean indicateActiveHealers()
	{
		return true;
	}

	@ConfigItem(
		position = 9,
		keyName = "waves",
		name = "Waves",
		description = ""
	)
	default Stub waves()
	{
		return new Stub();
	}

	@ConfigItem(
		position = 10,
		keyName = "waveDisplay",
		name = "Wave display",
		description = "Shows monsters that will spawn on the selected wave(s)."
	)
	default InfernoWaveDisplayMode waveDisplay()
	{
		return InfernoWaveDisplayMode.BOTH;
	}

	@ConfigItem(
		position = 11,
		keyName = "getWaveOverlayHeaderColor",
		name = "Wave Header",
		description = "Color for Wave Header"
	)
	default Color getWaveOverlayHeaderColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		position = 12,
		keyName = "getWaveTextColor",
		name = "Wave Text Color",
		description = "Color for Wave Texts"
	)
	default Color getWaveTextColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 13,
		keyName = "debug",
		name = "Debug",
		description = ""
	)
	default Stub debug()
	{
		return new Stub();
	}

	@ConfigItem(
		position = 14,
		keyName = "indicateNonSafespotted",
		name = "Non-safespotted NPC's",
		description = "Indicate NPC's that can attack you"
	)
	default boolean indicateNonSafespotted()
	{
		return false;
	}

	@ConfigItem(
		position = 15,
		keyName = "indicateTemporarySafespotted",
		name = "Temporary safespotted NPC's",
		description = "Indicate NPC's that have to move to attack you"
	)
	default boolean indicateTemporarySafespotted()
	{
		return false;
	}

	@ConfigItem(
		position = 16,
		keyName = "indicateSafespotted",
		name = "Safespotted NPC's",
		description = "Indicate NPC's that are safespotted (can't attack you)"
	)
	default boolean indicateSafespotted()
	{
		return false;
	}

	@ConfigItem(
		position = 17,
		keyName = "indicateObstacles",
		name = "Obstacles",
		description = "Indicate obstacles that NPC's cannot pass through"
	)
	default boolean indicateObstacles()
	{
		return false;
	}

	@ConfigItem(
		position = 18,
		keyName = "indicateNpcPosition",
		name = "NPC Position",
		description = "Indicate the main tile for multi-tile NPC's. This tile is used for line-of-sight and pathfinding."
	)
	default boolean indicateNpcPosition()
	{
		return false;
	}

	@ConfigItem(
		position = 19,
		keyName = "indicateZukShieldSafespots",
		name = "Zuk Shield Safespots",
		description = "Indicate the zuk shield safespots."
	)
	default boolean indicateZukShieldSafespots()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 20,
		keyName = "zukShieldSafespotsColor",
		name = "Zuk Shield Safespots Color",
		description = "The color of the zuk shield safespots."
	)
	default Color zukShieldSafespotsColor()
	{
		return new Color(0, 255, 0, 100);
	}

	@ConfigItem(
		position = 21,
		keyName = "indicateNibblers",
		name = "Indicate Nibblers",
		description = "Indicate's nibblers that are alive"
	)
	default boolean indicateNibblers()
	{
		return true;
	}

	@ConfigItem(
		position = 22,
		keyName = "indicateNonPriorityDescendingBoxes",
		name = "Indicate Non-Priority Boxes",
		description = "Render descending boxes for prayers that are not the priority prayer for that tick"
	)
	default boolean indicateNonPriorityDescendingBoxes()
	{
		return true;
	}

	@ConfigItem(
		position = 23,
		keyName = "indicateBlobDetectionTick",
		name = "Indicate Blob Dection Tick",
		description = "Show a prayer indicator (default: magic) for the tick on which the blob will detect prayer"
	)
	default boolean indicateBlobDetectionTick()
	{
		return true;
	}

	@ConfigItem(
			position = 24,
			keyName = "Indicate Safespots",
			name = "indicateSafespots",
			description = "Indicate safespots on the ground: safespot (white), pray melee (red), pray range (green), pray magic (blue) and combinations of those"
	)
	default SafespotDisplayMode indicateSafespots()
	{
		return SafespotDisplayMode.OFF;
	}

	@ConfigItem(
			position = 25,
			keyName = "safespotsCheckSize",
			name = "Safespots Check Size",
			description = "The size of the area around the player that should be checked for safespots (SIZE x SIZE area)"
	)
	default int safespotsCheckSize()
	{
		return 10;
	}

	@Getter
	@AllArgsConstructor
	enum FontStyle
	{
		BOLD("Bold", Font.BOLD),
		ITALIC("Italic", Font.ITALIC),
		PLAIN("Plain", Font.PLAIN);

		private String name;
		private int font;

		@Override
		public String toString()
		{
			return getName();
		}
	}
}