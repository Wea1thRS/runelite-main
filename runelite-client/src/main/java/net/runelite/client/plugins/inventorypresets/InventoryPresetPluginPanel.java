package net.runelite.client.plugins.inventorypresets;

import com.google.inject.Inject;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class InventoryPresetPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;

	private static final Color DEFAULT_BORDER_COLOR = Color.GREEN;
	private static final Color DEFAULT_FILL_COLOR = new Color(0, 255, 0, 0);

	private static final int DEFAULT_BORDER_THICKNESS = 3;

	private final JLabel addMarker = new JLabel(ADD_ICON);
	private final JLabel title = new JLabel();
	private final PluginErrorPanel noPresetsPanel = new PluginErrorPanel();

	@Inject
	private InventoryPresetsPlugin plugin;

	@Getter
	private Color selectedColor = DEFAULT_BORDER_COLOR;

	@Getter
	private Color selectedFillColor = DEFAULT_FILL_COLOR;

	@Getter
	private int selectedBorderThickness = DEFAULT_BORDER_THICKNESS;

	@Getter
	private InventoryPresetCreationPanel creationPanel;

	static
	{
		final BufferedImage addIcon = ImageUtil.getResourceStreamFromClass(InventoryPresetsPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
	}

	public void init()
	{
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(new EmptyBorder(1, 0, 10, 0));

		title.setText("Inventory Presets");
		title.setForeground(Color.WHITE);

		northPanel.add(title, BorderLayout.WEST);
		northPanel.add(addMarker, BorderLayout.EAST);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel presetView = new JPanel(new GridBagLayout());
		presetView.setBackground(ColorScheme.DARK_GRAY_COLOR);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		for (final InventoryPreset preset : plugin.getInventoryPresets())
		{
			presetView.add(new InventoryPresetPanel(plugin, preset), constraints);
			constraints.gridy++;

			presetView.add(Box.createRigidArea(new Dimension(0, 10)), constraints);
			constraints.gridy++;
		}

		noPresetsPanel.setContent("Inventory Presets", "Save an inventory for later.");
		noPresetsPanel.setVisible(false);

		if (plugin.getInventoryPresets().isEmpty())
		{
			noPresetsPanel.setVisible(true);
			title.setVisible(false);
		}

		presetView.add(noPresetsPanel, constraints);
		constraints.gridy++;

		creationPanel = new InventoryPresetCreationPanel(plugin);
		creationPanel.setVisible(false);

		presetView.add(creationPanel, constraints);
		constraints.gridy++;

		addMarker.setToolTipText("Add new screen marker");
		addMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				setCreation(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addMarker.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addMarker.setIcon(ADD_ICON);
			}
		});

		centerPanel.add(presetView, BorderLayout.CENTER);

		add(northPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
	}

	public void rebuild()
	{
		removeAll();
		repaint();
		revalidate();
		init();
	}

	/* Enables/Disables new marker creation mode */
	public void setCreation(boolean on)
	{
		if (on)
		{
			noPresetsPanel.setVisible(false);
			title.setVisible(true);
		}
		else
		{
			boolean empty = plugin.getInventoryPresets().isEmpty();
			noPresetsPanel.setVisible(empty);
			title.setVisible(!empty);
		}

		creationPanel.setVisible(on);
		addMarker.setVisible(!on);

		if (on)
		{
			creationPanel.lockConfirm();
		}
	}
}
