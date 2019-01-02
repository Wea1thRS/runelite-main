/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
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
package net.runelite.client.plugins.pmcycle;

import java.awt.event.KeyEvent;
import java.util.TreeSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.SetMessage;
import net.runelite.api.events.WidgetHiddenChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatboxInputListener;
import net.runelite.client.chat.CommandManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ChatboxInput;
import net.runelite.client.events.PrivateMessageInput;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Private Message Cycle",
	description = "When sending a private message pressing the tab button again will cycle through your previous recipients"
)
public class PMCyclePlugin extends Plugin implements ChatboxInputListener, KeyListener
{
	private static final int HOTKEY = KeyEvent.VK_TAB;
	private static final String PRIVATE_MESSAGE_TITLE_PREFIX = "Enter message to send to ";

	private final TreeSet<String> friends = new TreeSet<>();
	private String target;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private CommandManager commandManager;

	@Inject
	private KeyManager keyManager;

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(this);
		commandManager.register(this);
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
		commandManager.unregister(this);
		friends.clear();
	}

	@Subscribe
	public void onSetMessage(SetMessage message)
	{
		ChatMessageType t = message.getType();
		switch (t)
		{
			case PRIVATE_MESSAGE_SENT:
			case PRIVATE_MESSAGE_RECEIVED:
			case PRIVATE_MESSAGE_RECEIVED_MOD:
				String name = Text.removeTags(message.getName());
				// Remove and readd to update its position in history.
				friends.remove(name);
				friends.add(name);
		}
	}

	@Override
	public boolean onChatboxInput(ChatboxInput c)
	{
		return false;
	}

	@Override
	public boolean onPrivateMessageInput(PrivateMessageInput p)
	{
		boolean check = target != null && !p.getTarget().equals(target);
		if (check)
		{
			log.debug("Custom Private Message.  Target: {} | Message: {}", target, p.getMessage());
			sendPrivmsg(target, p.getMessage());
		}

		target = null;
		return check;
	}

	private void sendPrivmsg(String t, String message)
	{
		clientThread.invokeLater(() -> client.runScript(ScriptID.PRIVMSG, t, message));
	}

	@Subscribe
	protected void onWidgetHiddenChanged(WidgetHiddenChanged l)
	{
		if (l.getWidget() == client.getWidget(WidgetInfo.CHATBOX_CONTAINER) && !l.isHidden())
		{
			// Chatbox no longer hidden, invoke on clientThread to ensure widget text is updated.
			clientThread.invokeLater(this::privateMessageInterfaceOpened);
		}
	}

	private void privateMessageInterfaceOpened()
	{
		if (isTypingPrivateMessage())
		{
			Widget title = client.getWidget(WidgetInfo.CHATBOX_TITLE);
			target = title.getText().replace(PRIVATE_MESSAGE_TITLE_PREFIX, "").trim();
		}
	}

	private boolean isTypingPrivateMessage()
	{
		Widget title = client.getWidget(WidgetInfo.CHATBOX_TITLE);
		if (title == null || title.getText() == null)
		{
			return false;
		}

		return title.getText().startsWith(PRIVATE_MESSAGE_TITLE_PREFIX);

	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == HOTKEY)
		{
			if (target == null || friends.size() == 0)
			{
				return;
			}

			if (isTypingPrivateMessage())
			{
				String newFriend = friends.lower(target);
				if (newFriend != null && !target.equals(newFriend))
				{
					target = newFriend;
				}
				else
				{
					target = friends.last();
				}

				Widget title = client.getWidget(WidgetInfo.CHATBOX_TITLE);
				title.setText(PRIVATE_MESSAGE_TITLE_PREFIX + target);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}
}
