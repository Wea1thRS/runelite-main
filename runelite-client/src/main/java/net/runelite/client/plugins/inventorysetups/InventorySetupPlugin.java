package net.runelite.client.plugins.inventorysetups;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.inventorysetups.ui.InventorySetupPluginPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

@PluginDescriptor(
		name = "Inventory Setups",
		description = "Save inventory setups",
		tags = { "items", "inventory", "setups"},
		enabledByDefault = false
)

@Slf4j
public class InventorySetupPlugin extends Plugin
{

	private static final String CONFIG_GROUP = "inventorysetups";
	private static final String CONFIG_KEY = "setups";

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private InventorySetupConfig config;

	@Inject
	private InventorySetupBankOverlay overlay;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientThread clientThread;

	private InventorySetupPluginPanel panel;

	private HashMap<String, InventorySetup> inventorySetups;

	private NavigationButton navButton;

	private boolean highlightDifference;

	@Override
	public void startUp()
	{
		overlayManager.add(overlay);

		panel = new InventorySetupPluginPanel(this, itemManager, clientThread);

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "inventorysetups_icon.png");

		navButton = NavigationButton.builder()
				.tooltip("Inventory Setups")
				.icon(icon)
				.priority(9)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		// load all the inventory setups from the config file
		clientThread.invokeLater(() ->
		{
			if (client.getGameState() != GameState.LOGIN_SCREEN)
			{
				return false;
			}

			loadConfig();
			panel.showNoSetupsPanel();
			return true;
		});

	}

	public void addInventorySetup()
	{
		final String name = JOptionPane.showInputDialog(panel,
				"Enter the name of this setup.",
				"Add New Setup",
				JOptionPane.PLAIN_MESSAGE);

		// cancel button was clicked
		if (name == null)
		{
			return;
		}

		if (name.isEmpty())
		{
			JOptionPane.showMessageDialog(panel,
					"Invalid Setup Name",
					"Names must not be empty.",
					JOptionPane.PLAIN_MESSAGE);
			return;
		}

		if (inventorySetups.containsKey(name))
		{
			String builder = "The setup " + name + " already exists. " +
					"Would you like to replace it with the current setup?";
			int confirm = JOptionPane.showConfirmDialog(panel,
					builder,
					"Warning",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (confirm == JOptionPane.CANCEL_OPTION)
			{
				return;
			}

			// delete the old setup, no need to ask for confirmation
			// because the user confirmed above
			removeInventorySetup(name, false);
		}

		clientThread.invoke(() ->
		{
			ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
			ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

			final InventorySetup invSetup = new InventorySetup(inventory, equipment, itemManager);
			SwingUtilities.invokeLater(() ->
			{
				inventorySetups.put(name, invSetup);
				panel.addInventorySetup(name);
				panel.setCurrentInventorySetup(name);

				updateConfig();
			});
		});
	}

	public void removeInventorySetup(final String name, boolean askForConfirmation)
	{
		if (inventorySetups.containsKey(name))
		{
			int confirm = JOptionPane.YES_OPTION;

			if (askForConfirmation) {
				confirm = JOptionPane.showConfirmDialog(panel,
						"Are you sure you want to remove this setup?",
						"Warning",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE);
			}

			if (confirm == JOptionPane.YES_OPTION)
			{
				inventorySetups.remove(name);
				panel.removeInventorySetup(name);
			}

			updateConfig();
		}
	}

	public final InventorySetup getInventorySetup(final String name)
	{
		return inventorySetups.get(name);
	}

	@Provides
	InventorySetupConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InventorySetupConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			// only allow highlighting if the config is enabled and the player is logged in
			highlightDifference = config.getHighlightDifferences() && client.getGameState() == GameState.LOGGED_IN;
			final String setupName = panel.getSelectedInventorySetup();
			if (!setupName.isEmpty())
			{
				panel.setCurrentInventorySetup(setupName);
			}
		}
	}

	private void updateConfig()
	{
		if (inventorySetups.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			return;
		}

		final Gson gson = new Gson();
		final String json = gson.toJson(inventorySetups);
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	private void loadConfig()
	{
		// serialize the internal data structure from the json in the configuration
		final String json = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		if (json == null || json.isEmpty())
		{
			inventorySetups = new HashMap<>();
		}
		else
		{
			// TODO add last resort?, serialize exception just make empty map
			final Gson gson = new Gson();
			Type type = new TypeToken<HashMap<String, InventorySetup>>(){}.getType();
			inventorySetups = gson.fromJson(json, type);
		}

		for (final String key : inventorySetups.keySet())
		{
			panel.addInventorySetup(key);
		}

		highlightDifference = false;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		// empty entry, no need to compare anything
		final String selectedInventorySetup = panel.getSelectedInventorySetup();
		if (selectedInventorySetup.isEmpty())
		{
			return;
		}

		// check to see that the container is the equipment or inventory
		ItemContainer container = event.getItemContainer();
		if (container == client.getItemContainer(InventoryID.INVENTORY))
		{
			final InventorySetup setup = inventorySetups.get(selectedInventorySetup);
			panel.highlightDifferences(container, setup, InventoryID.INVENTORY);
		}
		else if (container == client.getItemContainer(InventoryID.EQUIPMENT))
		{
			final InventorySetup setup = inventorySetups.get(selectedInventorySetup);
			panel.highlightDifferences(container, setup, InventoryID.EQUIPMENT);
		}

	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			highlightDifference = config.getHighlightDifferences();
		}
		else
		{
			highlightDifference = false;
		}

		// reset the current inventory setup
		final String setupName = panel.getSelectedInventorySetup();
		if (!setupName.isEmpty())
		{
			panel.setCurrentInventorySetup(setupName);
		}
	}

	public final ItemContainer getCurrentPlayerContainer(final InventoryID type)
	{
		return client.getItemContainer(type);
	}

	public final InventorySetupConfig getConfig()
	{
		return config;
	}

	public boolean getHighlightDifference()
	{
		return highlightDifference;
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
	}

	final int[] getCurrentInventorySetupIds()
	{
		InventorySetup setup = inventorySetups.get(panel.getSelectedInventorySetup());
		if (setup == null)
		{
			return null;
		}
		ArrayList<InventorySetupItem> items = new ArrayList<>();
		items.addAll(setup.getEquipment());
		items.addAll(setup.getInventory());

		ArrayList<Integer> itemIds = new ArrayList<>();
		for (InventorySetupItem item : items)
		{
			itemIds.add(item.getId());
		}
		return itemIds.stream().mapToInt(i -> i).toArray();
	}
}
