package net.runelite.client.plugins.theatre;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("theatre")

public interface TheatreConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "showMaidenPools",
			name = "Maiden Blood Pool Overlay",
			description = "Toggles tile overlay for Maiden blood pool attacks"
	)
	default boolean showMaidenPools() { return true;}

	@ConfigItem(
			position = 1,
			keyName = "showBloatFlesh",
			name = "Bloat Falling Flesh Overlay",
			description = "Toggles tile overlay for Bloat Falling Flesh"
	)
	default boolean showBloatFlesh() { return true;}

	@ConfigItem(
			position = 2,
			keyName = "bloatTurnWarnings",
			name = "Bloat Turn Warning(not done)",
			description = "Toggles warnings for Bloat turns"
	)
	default boolean bloatTimers() { return false;}

	@ConfigItem(
			position = 3,
			keyName = "bloatDownTimer",
			name = "Bloat stop & start timers",
			description = "Shows timers for bloat downtime, and uptime"
	)
	default boolean bloatDownTimer() { return false;}

}
