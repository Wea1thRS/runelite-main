/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
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
package net.runelite.client.plugins.motherlode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
public class MotherlodeSession
{
	private static final Duration HOUR = Duration.ofHours(1);

	private int perHour;

	private Instant lastPayDirtMined;
	private int totalMined;

	private Instant recentPayDirtMined;
	private int recentMined;

	@Getter(AccessLevel.PACKAGE)
	private Instant lastGemFound;

	@Getter(AccessLevel.PACKAGE)
	private int diamondsFound;

	@Getter(AccessLevel.PACKAGE)
	private int rubiesFound;

	@Getter(AccessLevel.PACKAGE)
	private int emeraldsFound;

	@Getter(AccessLevel.PACKAGE)
	private int sapphiresFound;

	@Getter(AccessLevel.PACKAGE)
	private HashMap<MotherloadeOre, Integer> collectedOres;

	public void setupCollectedOres()
	{
		collectedOres = new HashMap<>();
		Arrays.stream(MotherloadeOre.values()).forEach(o -> collectedOres.put(o, 0));
	}

	public void incrementGemFound(int gemID)
	{
		lastGemFound = Instant.now();

		switch (gemID)
		{
			case ItemID.UNCUT_DIAMOND:
				diamondsFound++;
				break;

			case ItemID.UNCUT_RUBY:
				rubiesFound++;
				break;

			case ItemID.UNCUT_EMERALD:
				emeraldsFound++;
				break;

			case ItemID.UNCUT_SAPPHIRE:
				sapphiresFound++;
				break;

			default:
				log.error("Invalid gem type specified. The gem count will not be incremented.");
		}
	}

	public void incrementPayDirtMined()
	{
		Instant now = Instant.now();

		lastPayDirtMined = now;
		++totalMined;

		if (recentMined == 0)
		{
			recentPayDirtMined = now;
		}
		++recentMined;

		Duration timeSinceStart = Duration.between(recentPayDirtMined, now);
		if (!timeSinceStart.isZero())
		{
			perHour = (int) ((double) recentMined * (double) HOUR.toMillis() / (double) timeSinceStart.toMillis());
		}
	}

	public void incrementCollectedOre(int itemID, int amount)
	{

		MotherloadeOre ore = MotherloadeOre.getFromItem(itemID);

		if (ore != null)
			collectedOres.put(ore, collectedOres.get(ore) + amount);

	}

	public void resetRecent()
	{
		recentPayDirtMined = null;
		recentMined = 0;
	}

	public int getPerHour()
	{
		return perHour;
	}

	public Instant getLastPayDirtMined()
	{
		return lastPayDirtMined;
	}

	public int getTotalMined()
	{
		return totalMined;
	}

	public Instant getRecentPayDirtMined()
	{
		return recentPayDirtMined;
	}

	public int getRecentMined()
	{
		return recentMined;
	}

	public boolean showOres()
	{
		return collectedOres.values().stream().mapToInt(o -> o.intValue()).sum() > 0;
	}

}
