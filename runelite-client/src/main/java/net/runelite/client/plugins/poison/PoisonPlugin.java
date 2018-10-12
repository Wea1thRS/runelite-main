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
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.SpriteID;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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
	private InfoBoxManager infoBoxManager;

	@Inject
	private SpriteManager spriteManager;

	private int lastPoisonDamage = 0;
	private PoisonInfobox box = null;

	private BufferedImage poisonSplat = null;

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int poisonValue = client.getVar(VarPlayer.POISON);
		int poisonDamage = (int) Math.ceil(poisonValue / 5.0f);
		if (poisonDamage != this.lastPoisonDamage)
		{
			if (poisonDamage > 0)
			{
				if (this.box == null)
				{
					this.box = new PoisonInfobox(this.getPoisonSplat(), this, poisonDamage, false);
					infoBoxManager.addInfoBox(this.box);
				}
				else
				{
					this.box.setCount(poisonDamage);
				}
			}
			else
			{
				infoBoxManager.removeInfoBox(this.box);
				this.box = null;
			}
			this.lastPoisonDamage = poisonDamage;
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
}