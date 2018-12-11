/*
 * Copyright (c) 2018, Rprrr <https://github.com/rprrr>
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
package net.runelite.client.plugins.timestamps;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.SetMessage;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@PluginDescriptor
		(
				name = "Timestamps",
				enabledByDefault = false
		)

@Slf4j
public class TimestampsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TimestampsConfig tsconfig;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Provides
	TimestampsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TimestampsConfig.class);
	}

	@Subscribe
	public void onSetMessage(SetMessage message)
	{
		String timeLog = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
		if (message.getType().equals(ChatMessageType.SERVER))
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag("[", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("] ", Color.WHITE))
					.append(message.getValue())
					.build();
			message.getMessageNode().setValue(response);
			chatMessageManager.update(message.getMessageNode());
			return;
		}
		if (message.getType().equals(ChatMessageType.FILTERED))
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag("[", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("] ", Color.WHITE))
					.append(message.getValue())
					.build();
			message.getMessageNode().setValue(response);
			chatMessageManager.update(message.getMessageNode());
			System.out.println("Filtered");
			return;
		}
		if (message.getType().equals(ChatMessageType.AUTOCHAT))
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag("[", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("] ", Color.WHITE))
					.append(message.getName())
					.build();
			message.getMessageNode().setName(response);
			chatMessageManager.update(message.getMessageNode());
			return;
		}
		if (message.getType().equals(ChatMessageType.CLANCHAT))
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("][", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(message.getSender(), new Color(144, 112, 255)))
					.build();
			message.getMessageNode().setSender(response);
			chatMessageManager.update(message.getMessageNode());
			return;
		}
		if (message.getType().equals(ChatMessageType.PRIVATE_MESSAGE_SENT))
		{
			System.out.println("Pm Sent");
			return;
		}
		if (message.getType().equals(ChatMessageType.PRIVATE_MESSAGE_RECEIVED))
		{
			System.out.println("Sender:" + message.getSender() + " Name:" + message.getName());
			System.out.println("Pm Recieved");
			return;
		}
		if (message.getType().equals(ChatMessageType.PUBLIC))
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag("[", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("] ", Color.WHITE))
					.append(message.getName())
					.build();
			message.getMessageNode().setName(response);
			chatMessageManager.update(message.getMessageNode());
			return;
		}
		if (message.getType().equals(ChatMessageType.CLANCHAT_INFO))
		{
			message.getMessageNode().setName(timeLog + message.getName());
			System.out.println("Clan Chat Info");
			return;
		}
		else
		{
			String response = new ChatMessageBuilder()
					.append(ColorUtil.wrapWithColorTag("[", Color.WHITE))
					.append(ColorUtil.wrapWithColorTag(timeLog, Color.CYAN))
					.append(ColorUtil.wrapWithColorTag("] ", Color.WHITE))
					.append(message.getValue())
					.build();
			message.getMessageNode().setValue(response);
			chatMessageManager.update(message.getMessageNode());
			System.out.println("Other: " + message.getType());
		}
	}
}