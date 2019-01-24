/*
 * Copyright (c) 2019, DennisDeV <https://github.com/DevDennis>
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
package net.runelite.mixins;

import net.runelite.api.mixins.Copy;
import net.runelite.api.mixins.Inject;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Replace;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSClient;
import net.runelite.rs.api.RSItemComposition;
import org.apache.commons.text.similarity.FuzzyScore;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

@Mixin(RSClient.class)
public abstract class GrandExchangeMixin implements RSClient
{
	private static final int MAX_RESULT_COUNT = 250;

	@Shadow("clientInstance")
	private static RSClient client;

	@Inject
	private static boolean geFuzzySearchEnabled;

	@Copy("searchGrandExchangeItems")
	static void rs$searchGrandExchangeItems(String input, boolean tradableOnly)
	{
		throw new RuntimeException();
	}

	@Replace("searchGrandExchangeItems")
	public static void rl$searchGrandExchangeItems(String input, boolean tradableOnly)
	{
		if (!geFuzzySearchEnabled)
		{
			rs$searchGrandExchangeItems(input, tradableOnly);
			return;
		}
		Set<Integer> scoreIds = new TreeSet<Integer>(Collections.reverseOrder());

		FuzzyScore fuzzy = new FuzzyScore(Locale.ENGLISH);

		for (int id = 0; id < client.getItemCount(); id++)
		{
			RSItemComposition itemComposition = client.getItemDefinition(id);
			if ((!tradableOnly || itemComposition.isTradeable()) && itemComposition.getNote() == -1)
			{
				int score = fuzzy.fuzzyScore(itemComposition.getName(), input);
				if (score > 0)
				{
					scoreIds.add(score << 16 | id);
				}
			}
		}

		int resultCount = Math.min(scoreIds.size(), MAX_RESULT_COUNT);

		short[] ids = new short[resultCount];
		Iterator<Integer> scoreIdIter = scoreIds.iterator();
		int i = 0;
		while (scoreIdIter.hasNext() && i < resultCount)
		{
			ids[i] = (short) (scoreIdIter.next() & 0xFFFF);
			i++;
		}

		client.setGeSearchResultIds(ids);
		client.setGeSearchResultIndex(0);
		client.setGeSearchResultCount(resultCount);
	}

	@Inject
	public boolean isGeFuzzySearchEnabled()
	{
		return geFuzzySearchEnabled;
	}

	@Inject
	public void setGeFuzzySearchEnabled(boolean enabled)
	{
		geFuzzySearchEnabled = enabled;
	}
}
