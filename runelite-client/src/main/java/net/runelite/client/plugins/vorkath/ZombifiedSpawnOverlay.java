package net.runelite.client.plugins.vorkath;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;

public class ZombifiedSpawnOverlay extends Overlay {

    private Client client;
    private VorkathPlugin plugin;

    @Inject
    public ZombifiedSpawnOverlay(Client client, VorkathPlugin plugin)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
    }


    @Override
    public Dimension render(Graphics2D graphics) {

        if (plugin.getSpawn() != null) {
            ZombifiedSpawn spawn = plugin.getSpawn();
            OverlayUtil.renderActorOverlayImage(graphics, spawn.getNpc(), ImageUtil.getResourceStreamFromClass(getClass(), "ice.png"), Color.green, 10);
        }

        return null;
    }
}
