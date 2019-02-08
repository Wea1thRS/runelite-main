package net.runelite.client.plugins.vorkath;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;

public class VorkathOverlay extends Overlay {

    private static final Color COLOR_ICON_BACKGROUND = new Color(0, 0, 0, 128);
    private static final Color COLOR_ICON_BORDER = new Color(0, 0, 0, 255);
    private static final Color COLOR_ICON_BORDER_FILL = new Color(219, 175, 0, 255);
    private static final int OVERLAY_ICON_DISTANCE = 30;
    private static final int OVERLAY_ICON_MARGIN = 1;
    private static final int FILL_START_ALPHA = 25;
    private static final int OUTLINE_START_ALPHA = 255;

    private Client client;
    private VorkathPlugin plugin;

    @Inject
    public VorkathOverlay(Client client, VorkathPlugin plugin)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
    }

    private BufferedImage getIcon(Vorkath.AttackStyle attackStyle)
    {
        switch (attackStyle)
        {
            case MAGERANGE: return ImageUtil.getResourceStreamFromClass(getClass(), "magerange.png");
            case ICE: return ImageUtil.getResourceStreamFromClass(getClass(), "ice.png");
            case ACID: return ImageUtil.getResourceStreamFromClass(getClass(), "acid.png");
        }
        return null;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if(plugin.getVorkath() != null){
            Vorkath vorkath = plugin.getVorkath();

            LocalPoint localLocation = vorkath.getNpc().getLocalLocation();
            if(localLocation != null){
                Point point = Perspective.localToCanvas(client, localLocation, client.getPlane(), vorkath.getNpc().getLogicalHeight() + 16);
                if (point != null) {
                    point = new Point(point.getX(), point.getY());

                    BufferedImage icon = null;
                    if(vorkath.getPhase() == 0){
                        icon = getIcon(Vorkath.AttackStyle.MAGERANGE);
                    } else if(vorkath.getPhase() == 1) {
                        icon = getIcon(Vorkath.AttackStyle.ACID);
                    } else if(vorkath.getPhase() == 2) {
                        icon = getIcon(Vorkath.AttackStyle.ICE);
                    }

                    if (vorkath.isBomb())
                    {
                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(client, vorkath.getBombLoc(), 3);
                        if (tilePoly != null)
                        {
                            double progress = (System.currentTimeMillis() - vorkath.getBombTime()) / (double) 4000;

                            int fillAlpha = (int) ((1 - progress) * FILL_START_ALPHA);//alpha drop off over lifetime
                            int outlineAlpha = (int) ((1 - progress) * OUTLINE_START_ALPHA);

                            if (fillAlpha < 0)
                            {
                                fillAlpha = 0;
                            }
                            if (outlineAlpha < 0)
                            {
                                outlineAlpha = 0;
                            }

                            if (fillAlpha > 255)
                            {
                                fillAlpha = 255;
                            }
                            if (outlineAlpha > 255)
                            {
                                outlineAlpha = 255;//Make sure we don't pass in an invalid alpha
                            }
                            graphics.setColor(new Color(255, 0, 0, outlineAlpha));
                            graphics.drawPolygon(tilePoly);
                            graphics.setColor(new Color(255, 0, 0, fillAlpha));
                            graphics.fillPolygon(tilePoly);
                        }
                    }

                    assert icon != null;
                    int totalWidth = icon.getWidth() * OVERLAY_ICON_MARGIN;
                    int bgPadding = 8;
                    int currentPosX = 0;

                    graphics.setStroke(new BasicStroke(2));
                    graphics.setColor(COLOR_ICON_BACKGROUND);
                    graphics.fillOval(
                            point.getX() - totalWidth / 2 + currentPosX - bgPadding,
                            point.getY() - icon.getHeight() / 2 - OVERLAY_ICON_DISTANCE - bgPadding,
                            icon.getWidth() + bgPadding * 2,
                            icon.getHeight() + bgPadding * 2);

                    graphics.setColor(COLOR_ICON_BORDER);
                    graphics.drawOval(
                            point.getX() - totalWidth / 2 + currentPosX - bgPadding,
                            point.getY() - icon.getHeight() / 2 - OVERLAY_ICON_DISTANCE - bgPadding,
                            icon.getWidth() + bgPadding * 2,
                            icon.getHeight() + bgPadding * 2);

                    graphics.drawImage(
                            icon,
                            point.getX() - totalWidth / 2 + currentPosX,
                            point.getY() - icon.getHeight() / 2 - OVERLAY_ICON_DISTANCE,
                            null);

                    graphics.setColor(COLOR_ICON_BORDER_FILL);
                    Arc2D.Double arc = new Arc2D.Double(
                            point.getX() - totalWidth / 2 + currentPosX - bgPadding,
                            point.getY() - icon.getHeight() / 2 - OVERLAY_ICON_DISTANCE - bgPadding,
                            icon.getWidth() + bgPadding * 2,
                            icon.getHeight() + bgPadding * 2,
                            90.0,
                            -360.0 * (vorkath.ATTACKS_PER_SWITCH -
                                    vorkath.getAttacksUntilSwitch()) / vorkath.ATTACKS_PER_SWITCH,
                            Arc2D.OPEN);
                    graphics.draw(arc);
                }
            }
        }

        return null;
    }

}
