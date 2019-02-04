package net.runelite.client.plugins.theatre;

import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;
import java.util.Arrays;
import java.util.Collection;

@PluginDescriptor(
		name = "Theatre Of Blood"
)

public class TheatrePlugin extends Plugin
{
	@Inject
	private Client client;
	public NPC sot;
	public int sotAnimation;
	public int mageOrbCount;
	boolean inTheatre()
	{
		if (client.getGameState() == GameState.LOGGED_IN && client.getVar(Varbits.THEATRE_OF_BLOOD) == 2)
		{
			return true;
		}
		else {
			return false;
		}

	}

	@Inject
	private TheatreConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MaidenOverlay maidenOverlay;

	@Inject
	private BloatFleshOverlay bloatfleshOverlay;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(maidenOverlay);
		overlayManager.add(bloatfleshOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(maidenOverlay);
	}
	@Subscribe
	public void onNpcSpawned(final NpcSpawned event)
	{
		final int id = event.getNpc().getId();

		if (id == NpcID.SOTETSEG || id == NpcID.SOTETSEG_8388)
		{
			sot = event.getNpc();
		}
	}

	@Subscribe
	public void onNpcDespawned(final NpcDespawned event)
	{
		if (sot == event.getNpc())
		{
			sot = null;
		}
	}
	@Subscribe
	public void onAnimationChanged(final AnimationChanged event)
	{
		if (event.getActor() != sot)
		{
			return;
		}

		if (sot.getAnimation() == 8139)
		{
			mageOrbCount--;
		}
	}
	@Provides
	TheatreConfig provideConfig(ConfigManager configManager) {return configManager.getConfig(TheatreConfig.class);}

}