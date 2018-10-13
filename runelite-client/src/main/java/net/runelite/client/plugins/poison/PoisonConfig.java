package net.runelite.client.plugins.poison;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("poison")
public interface PoisonConfig extends Config
{
	@ConfigItem(
		keyName = "showInfoboxes",
		name = "Show Infoboxes",
		description = "Configures whether to show the infoboxes"
	)
	default boolean showInfoboxes()
	{
		return true;
	}
}
