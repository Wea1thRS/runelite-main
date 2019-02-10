/*
 * Copyright (c) 2018, TheStonedTurtle <https://github.com/TheStonedTurtle>
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
import java.util.LinkedHashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.VarClientInt;
import net.runelite.api.VarClientStr;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarClientStrChanged;
import net.runelite.api.vars.InputType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
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
public class PMCyclePlugin extends Plugin implements KeyListener
{
	private static final int HOTKEY = KeyEvent.VK_TAB;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private KeyManager keyManager;

	private final LinkedHashSet<String> friends = new LinkedHashSet<>();
	private String target;

	@Override
	protected void startUp()
	{
		keyManager.registerKeyListener(this);
	}

	@Override
	protected void shutDown()
	{
		keyManager.unregisterKeyListener(this);
		friends.clear();
	}

	@Subscribe
	public void onChatMessage(ChatMessage message)
	{
		ChatMessageType t = message.getType();
		switch (t)
		{
			case PRIVATE_MESSAGE_SENT:
			case PRIVATE_MESSAGE_RECEIVED:
			case PRIVATE_MESSAGE_RECEIVED_MOD:
				final String name = Text.removeTags(message.getName());
				// Remove and readd to update its position in history.
				friends.remove(name);
				friends.add(name);
		}
	}

	@Subscribe
	public void onVarClientStrChanged(VarClientStrChanged c)
	{
		if (c.getIndex() == VarClientStr.PRIVATE_MESSAGE_TARGET.getIndex())
		{
			target = client.getVar(VarClientStr.PRIVATE_MESSAGE_TARGET);
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (e.getKeyCode() == HOTKEY && (client.getVar(VarClientInt.INPUT_TYPE) == InputType.PRIVATE_MESSAGE.getType()))
		{
			clientThread.invoke(() ->
			{
				if (target == null || friends.size() == 0)
				{
					return;
				}

				final String oldTarget = target;
				String lastTarget = null;
				target = null;
				for (final String friend : friends)
				{
					if (friend.equals(oldTarget))
					{
						target = lastTarget;
						// Target will be null if we matched to first friend. Keep looping and default to last friend
						if (target != null)
						{
							break;
						}
					}

					lastTarget = friend;
				}

				if (target == null)
				{
					target = lastTarget;
				}

				client.runScript(ScriptID.OPEN_PRIVATE_MESSAGE_INTERFACE, target);
			});
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
