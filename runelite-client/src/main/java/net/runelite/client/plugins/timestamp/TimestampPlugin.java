/*
 * Copyright (c) 2017, Magic fTail
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
package net.runelite.client.plugins.timestamp;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.Varbits;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.SetMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ChatColorConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Hashtable;

@PluginDescriptor(
		name = "Timestamp",
		description = "Add timestamps to messages"
)
public class TimestampPlugin extends Plugin
{
	private static final String WELCOME_MESSAGE = "Welcome to Old School RuneScape.";

	private Color timestampColour;
	private Hashtable<Integer, String> timestamps = new Hashtable<>();

	@Inject
	private Client client;

	@Inject
	private ChatColorConfig chatColorConfig;

	@Inject
	private ClientThread clientThread;

	@Subscribe
	public void onSetMessage(SetMessage setMessage)
	{
		if (setMessage.getValue().equals(WELCOME_MESSAGE))
		{
			timestamps.clear();
		}
	}

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		int[] intStack = client.getIntStack();
		int inStackSize = client.getIntStackSize();

		String[] stringStack = client.getStringStack();
		int stringStackSize = client.getStringStackSize();
		int messageNumber = intStack[inStackSize - 1];

		switch (event.getEventName())
		{
			case "getMessageInfo":
				final ZonedDateTime time = ZonedDateTime.now();

				final String dateFormat = time.get(ChronoField.HOUR_OF_DAY) + ":" +
						String.format("%02d", time.get(ChronoField.MINUTE_OF_HOUR));

				if (timestampColour == null)
				{
					clientThread.invokeLater(this::updateColour);
				}

				if (timestamps.size() < messageNumber)
				{
					timestamps.put(messageNumber, dateFormat);
				}
				break;

			case "addTimestamp":
				String timestamp = timestamps.get(messageNumber);

				if (timestamp == null)
				{
					return;
				}

				timestamp = "[" + timestamp + "]";

				if (timestampColour != null)
				{
					timestamp = ColorUtil.wrapWithColorTag(timestamp, timestampColour);
				}
				
	}

	private void updateColour()
	{
		boolean isChatboxTransparent = client.isResized() && client.getVar(Varbits.TRANSPARENT_CHATBOX) == 1;

		timestampColour = isChatboxTransparent ? chatColorConfig.transparentTimestamp() : chatColorConfig.opaqueTimestamp();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (timestampColour == null)
		{
			return;
		}

		clientThread.invokeLater(this::updateColour);
	}

	@Override
	protected void startUp() throws Exception
	{
		if (timestampColour == null)
		{
			return;
		}

		clientThread.invokeLater(this::updateColour);
	}
}