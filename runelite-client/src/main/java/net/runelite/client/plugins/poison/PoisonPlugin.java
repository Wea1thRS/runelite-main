/*
 * Copyright (c) 2018, Raqes <j.raqes@gmail.com>
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
package net.runelite.client.plugins.poison;

import com.google.common.eventbus.Subscribe;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(
	name = "Poison & Venom",
	description = "Tracks current damage values for Poison and Venom",
	tags = {"combat", "poison", "venom"}
)
@Slf4j
public class PoisonPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PoisonOverlay poisonOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private SpriteManager spriteManager;

	@Getter
	private boolean envenomed = false;

	@Getter
	private int lastDamage = 0;
	private PoisonInfobox poisonBox = null;
	private PoisonInfobox venomBox = null;

	private BufferedImage poisonSplat = null;
	private BufferedImage venomSplat = null;

	@Override
	public void startUp() throws Exception
	{
		overlayManager.add(poisonOverlay);
	}

	@Override
	public void shutDown() throws Exception
	{
		overlayManager.remove(poisonOverlay);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int poisonValue = client.getVar(VarPlayer.POISON);
		if (poisonValue >= 1000000)
		{
			//Venom Damage starts at 6, and increments in twos;
			//The VarPlayer increments in values of 1, however.
			poisonValue -= 1000000 - 3;
			int venomDamage = poisonValue * 2;
			//Venom Damage caps at 20, but the VarPlayer keeps increasing
			if (venomDamage > 20)
			{
				venomDamage = 20;
			}
			if (venomDamage != this.lastDamage || !this.envenomed)
			{
				this.envenomed = true;
				if (this.poisonBox != null)
				{
					infoBoxManager.removeInfoBox(this.poisonBox);
					this.poisonBox = null;
				}
				if (this.venomBox == null)
				{
					this.venomBox = new PoisonInfobox(this.getVenomSplat(), this, venomDamage, true);
					infoBoxManager.addInfoBox(this.venomBox);
				}
				else
				{
					this.venomBox.setCount(venomDamage);
				}

				this.lastDamage = venomDamage;
			}
		}
		else
		{
			int poisonDamage = (int) Math.ceil(poisonValue / 5.0f);
			if (poisonDamage != this.lastDamage || this.envenomed)
			{
				this.envenomed = false;
				if (poisonDamage > 0)
				{
					if (this.venomBox != null)
					{
						infoBoxManager.removeInfoBox(this.venomBox);
						this.venomBox = null;
					}
					if (this.poisonBox == null)
					{
						this.poisonBox = new PoisonInfobox(this.getPoisonSplat(), this, poisonDamage, false);
						infoBoxManager.addInfoBox(this.poisonBox);
					}
					else
					{
						this.poisonBox.setCount(poisonDamage);
					}
				}
				else
				{
					infoBoxManager.removeInfoBox(this.venomBox);
					infoBoxManager.removeInfoBox(this.poisonBox);
					this.poisonBox = null;
				}
				this.lastDamage = poisonDamage;
			}
		}
	}

	private BufferedImage getPoisonSplat()
	{
		if (this.poisonSplat == null)
		{
			this.poisonSplat = spriteManager.getSprite(SpriteID.HITSPLAT_GREEN_POISON, 0);
		}
		return this.poisonSplat;
	}

	private BufferedImage getVenomSplat()
	{
		if (this.venomSplat == null)
		{
			this.venomSplat = spriteManager.getSprite(SpriteID.HITSPLAT_DARK_GREEN_VENOM, 0);
		}
		return this.venomSplat;
	}
}