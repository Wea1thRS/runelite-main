package net.runelite.client.plugins.poison;

import net.runelite.client.ui.overlay.infobox.Counter;
import java.awt.image.BufferedImage;

public class PoisonInfobox extends Counter
{
	private int count;
	private boolean venom;

	PoisonInfobox(BufferedImage image, PoisonPlugin plugin, int damage, boolean venom)
	{
		super(image, plugin, null);
		this.count = damage;
		this.venom = venom;
	}

	void setCount(int hit)
	{
		this.count = hit;
	}

	@Override
	public String getText()
	{
		return Integer.toString(this.count);
	}

	@Override
	public String getTooltip()
	{
		if (this.venom)
		{
			return "Next venom damage: " + Integer.toString(this.count);
		}
		else
		{
			return "Next poison damage: " + Integer.toString(this.count);
		}
	}
}

