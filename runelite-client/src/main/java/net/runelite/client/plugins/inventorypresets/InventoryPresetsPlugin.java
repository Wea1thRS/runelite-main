package net.runelite.client.plugins.inventorypresets;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.events.ConfigChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PluginDescriptor(
		name = "Inventory Presets",
		description = "",
		tags = {""}
)
public class InventoryPresetsPlugin extends Plugin
{
	private static final String PLUGIN_NAME = "Inventory Presets";
	private static final String CONFIG_GROUP = "inventorypresets";
	private static final String CONFIG_KEY = "presets";
	private static final String ICON_FILE = "panel_icon.png";
	private static final String DEFAULT_PRESET_NAME = "Preset";
	private static final Dimension DEFAULT_SIZE = new Dimension(2, 2);

	@Getter
	private final List<InventoryPreset> inventoryPresets = new ArrayList<>();

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientToolbar clientToolbar;

	private InventoryPresetPluginPanel pluginPanel;
	private NavigationButton navigationButton;

	@Getter(AccessLevel.PACKAGE)
	private InventoryPresets currentPreset;

	@Getter
	private boolean creatingPreset = false;

	@Override
	protected void startUp() throws Exception
	{
		loadConfig(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).forEach(inventoryPresets::add);

		pluginPanel = injector.getInstance(InventoryPresetPluginPanel.class);
		pluginPanel.rebuild();

		final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), ICON_FILE);

		navigationButton = NavigationButton.builder()
				.tooltip(PLUGIN_NAME)
				.icon(icon)
				.priority(5)
				.panel(pluginPanel)
				.build();

		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navigationButton);
		creatingPreset = false;
		pluginPanel = null;
		currentPreset = null;
		navigationButton = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (inventoryPresets.isEmpty() && event.getGroup().equals(CONFIG_GROUP) && event.getKey().equals(CONFIG_KEY))
		{
			loadConfig(event.getNewValue()).forEach(inventoryPresets::add);
		}
	}

	public void startCreation()
	{
		currentPreset = new InventoryPresets(
				Instant.now().toEpochMilli(),
				DEFAULT_PRESET_NAME + " " + (inventoryPresets.size() + 1),
				true
		);

		creatingPreset = true;
	}

	public void finishCreation(boolean aborted)
	{
		if (!aborted)
		{
			final InventoryPreset inventoryPreset = new InventoryPreset(currentPreset);
			inventoryPresets.add(inventoryPreset);
			pluginPanel.rebuild();
			updateConfig();
		}

		creatingPreset = false;
		currentPreset = null;
		pluginPanel.setCreation(false);
	}

	/* The marker area has been drawn, inform the user and unlock the confirm button */
	public void completeSelection()
	{
		pluginPanel.getCreationPanel().unlockConfirm();
	}

	public void deletePreset(final InventoryPreset preset)
	{
		inventoryPresets.remove(preset);
		pluginPanel.rebuild();
		updateConfig();
	}

	public void updateConfig()
	{
		if (inventoryPresets.isEmpty())
		{
			configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			return;
		}

		final Gson gson = new Gson();
		final String json = gson
				.toJson(inventoryPresets.stream().map(InventoryPreset::getPreset).collect(Collectors.toList()));
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, json);
	}

	private Stream<InventoryPreset> loadConfig(String json)
	{
		if (Strings.isNullOrEmpty(json))
		{
			return Stream.empty();
		}

		final Gson gson = new Gson();
		final List<InventoryPresets> presetData = gson.fromJson(json, new TypeToken<ArrayList<InventoryPresets>>()
		{
		}.getType());

		return presetData.stream().map(InventoryPreset::new);
	}
}

