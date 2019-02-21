package net.runelite.client.plugins.inventorysetups.ui;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.inventorysetups.InventorySetup;
import net.runelite.client.plugins.inventorysetups.InventorySetupPlugin;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.loottracker.GameItem;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

@Slf4j
public class InventorySetupPluginPanel extends PluginPanel
{
	private static ImageIcon ADD_ICON;
	private static ImageIcon ADD_HOVER_ICON;
	private static ImageIcon REMOVE_ICON;
	private static ImageIcon REMOVE_HOVER_ICON;

	private final JPanel noSetupsPanel;
	private final JPanel invEqPanel;

	private final InventorySetupInventoryPanel invPanel;
	private final InventorySetupEquipmentPanel eqpPanel;

	private final JComboBox<String> setupComboBox;

	private final JLabel removeMarker;

	private final InventorySetupPlugin plugin;

	private final ItemManager itemManager;

	static
	{
		final BufferedImage addIcon = ImageUtil.getResourceStreamFromClass(InventorySetupPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));

		final BufferedImage removeIcon = ImageUtil.getResourceStreamFromClass(InventorySetupPlugin.class, "remove_icon.png");
		REMOVE_ICON = new ImageIcon(removeIcon);
		REMOVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(removeIcon, 0.53f));
	}

	public InventorySetupPluginPanel(InventorySetupPlugin plugin, ItemManager itemManager)
	{
		super(false);
		this.plugin = plugin;
		this.itemManager = itemManager;
		this.removeMarker = new JLabel(REMOVE_ICON);
		this.invPanel = new InventorySetupInventoryPanel(itemManager, plugin);
		this.eqpPanel = new InventorySetupEquipmentPanel(itemManager, plugin);
		this.noSetupsPanel = new JPanel();
		this.invEqPanel = new JPanel();
		this.setupComboBox = new JComboBox<>();

		// setup the title
		final JLabel addMarker = new JLabel(ADD_ICON);
		final JLabel title = new JLabel();
		title.setText("Inventory Setups");
		title.setForeground(Color.WHITE);

		// setup the add marker (+ sign in the top right)
		addMarker.setToolTipText("Add a new inventory setup");
		addMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e) { plugin.addInventorySetup(); }

			@Override
			public void mouseEntered(MouseEvent e) {
				addMarker.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				addMarker.setIcon(ADD_ICON);
			}
		});

		// setup the remove marker (X sign in the top right)
		removeMarker.setToolTipText("Remove the current inventory setup");
		removeMarker.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				final String name = (String)setupComboBox.getSelectedItem();
				plugin.removeInventorySetup(name, true);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (removeMarker.isEnabled())
				{
					removeMarker.setIcon(REMOVE_HOVER_ICON);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) { removeMarker.setIcon(REMOVE_ICON); }
		});

		// setup the combo box for selection switching
		// add empty to indicate the empty position
		setupComboBox.addItem("");
		setupComboBox.setSelectedIndex(0);
		setupComboBox.addItemListener(e ->
		{
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				String selection = (String)e.getItem();

				// empty selection
				if (selection.isEmpty())
				{
					removeMarker.setEnabled(false);
					noSetupsPanel.setVisible(true);
					invEqPanel.setVisible(false);
				}
				else
				{
					removeMarker.setEnabled(true);
					noSetupsPanel.setVisible(false);
					invEqPanel.setVisible(true);
				}

				plugin.changeCurrentInventorySetup(selection);
			}
		});

		// the panel on the top right that holds the add and delete buttons
		final JPanel markersPanel = new JPanel();
		markersPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		markersPanel.add(removeMarker);
		markersPanel.add(addMarker);

		// the top panel that has the title and the buttons
		final JPanel titleAndMarkersPanel = new JPanel();
		titleAndMarkersPanel.setLayout(new BorderLayout());
		titleAndMarkersPanel.add(title, BorderLayout.WEST);
		titleAndMarkersPanel.add(markersPanel, BorderLayout.EAST);

		// the panel that stays at the top and doesn't scroll
		// contains the title, buttons, and the combo box
		final JPanel northAnchoredPanel = new JPanel();
		northAnchoredPanel.setLayout(new BoxLayout(northAnchoredPanel, BoxLayout.Y_AXIS));
		northAnchoredPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		northAnchoredPanel.add(titleAndMarkersPanel);
		northAnchoredPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		northAnchoredPanel.add(setupComboBox);

		// the panel that holds the inventory and equipment panels
		final BoxLayout invEqLayout = new BoxLayout(invEqPanel, BoxLayout.Y_AXIS);
		invEqPanel.setLayout(invEqLayout);
		invEqPanel.add(invPanel);
		invEqPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		invEqPanel.add(eqpPanel);

		// setup the error panel. It's wrapped around a normal panel
		// so it doesn't stretch to fill the parent panel
		final PluginErrorPanel errorPanel = new PluginErrorPanel();
		errorPanel.setContent("Inventory Setups", "Select or create an inventory setup.");
		noSetupsPanel.add(errorPanel);

		// the panel that holds the inventory panels, and the error panel
		final JPanel contentPanel = new JPanel();
		final BoxLayout contentLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		contentPanel.setLayout(contentLayout);
		contentPanel.add(invEqPanel);
		contentPanel.add(noSetupsPanel);

		// wrapper for the main content panel to keep it from stretching
		final JPanel contentWrapper = new JPanel(new BorderLayout());
		contentWrapper.add(Box.createGlue(), BorderLayout.CENTER);
		contentWrapper.add(contentPanel, BorderLayout.NORTH);
		final JScrollPane contentWrapperPane = new JScrollPane(contentWrapper);
		contentWrapperPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(northAnchoredPanel, BorderLayout.NORTH);
		add(contentWrapperPane, BorderLayout.CENTER);

		// show the no setups panel on startup
		showNoSetupsPanel();

	}

	public void showNoSetupsPanel()
	{
		setupComboBox.setSelectedIndex(0);
		removeMarker.setEnabled(false);
		noSetupsPanel.setVisible(true);
		invEqPanel.setVisible(false);
	}

	public void setCurrentInventorySetup(final InventorySetup inventorySetup)
	{
		if (inventorySetup == null) {
			return;
		}

		if( inventorySetup.getEquipment() == null || inventorySetup.getInventory() == null)
		{
			return;
		}

		for (GameItem item : inventorySetup.getEquipment())
		{
			log.debug("Equipment: " + String.valueOf(item.getId()));
		}

		log.debug("-------------");

		clientThread.invokeLater(() -> invPanel.setInventorySetupSlots(inventorySetup.getInventory()));
		clientThread.invokeLater(() -> eqpPanel.setEquipmentSetupSlots(inventorySetup.getEquipment()));

		if (plugin.getHighlightDifference())
		{
			final ItemContainer inv = plugin.getCurrentPlayerContainer(InventoryID.INVENTORY);
			final ItemContainer eqp = plugin.getCurrentPlayerContainer(InventoryID.EQUIPMENT);
			highlightDifferences(inv, inventorySetup, InventoryID.INVENTORY);
			highlightDifferences(eqp, inventorySetup, InventoryID.EQUIPMENT);
		}
		else
		{
			invPanel.resetInventorySlotsColor();
			eqpPanel.resetEquipmentSlotsColor();
		}

		validate();
		repaint();
	}

	public void addInventorySetup(final String name, final InventorySetup setup, boolean setToCurrent)
	{
		setupComboBox.addItem(name);
		setupComboBox.setSelectedItem(name);
		removeMarker.setEnabled(true);
		noSetupsPanel.setVisible(false);
		invEqPanel.setVisible(true);

		// set this inventory setup to be the current one
		if (setToCurrent)
		{
			log.debug("4");
			setCurrentInventorySetup(setup);
		}
	}

	public void removeInventorySetup(final String name)
	{
		setupComboBox.removeItem(name);
		showNoSetupsPanel();

		invPanel.resetInventorySlotsColor();
		eqpPanel.resetEquipmentSlotsColor();

		validate();
		repaint();
	}

	public void highlightDifferences(final ItemContainer container,
	                                 final InventorySetup setupToCheck,
	                                 final InventoryID type)
	{
		if (type == InventoryID.INVENTORY)
		{
			invPanel.highlightDifferentSlots(container, setupToCheck);
		}
		else if (type == InventoryID.EQUIPMENT)
		{
			eqpPanel.highlightDifferences(container, setupToCheck);
		}
	}

	public final String getSelectedInventorySetup()
	{
		return (String)setupComboBox.getSelectedItem();
	}


}
