package net.runelite.client.plugins.inventorypresets;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;
import net.runelite.client.util.ImageUtil;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class InventoryPresetCreationPanel extends JPanel
{
	private static final ImageIcon CONFIRM_ICON;
	private static final ImageIcon CONFIRM_HOVER_ICON;
	private static final ImageIcon CONFIRM_LOCKED_ICON;
	private static final ImageIcon EQUIPMENT_ICON;
	private static final ImageIcon EQUIPMENT_HOVER_ICON;
	private static final ImageIcon INVENTORY_ICON;
	private static final ImageIcon INVENTORY_HOVER_ICON;
	private static final ImageIcon CANCEL_ICON;
	private static final ImageIcon CANCEL_HOVER_ICON;

	private final JShadowedLabel instructionsLabel = new JShadowedLabel();
	private final JLabel confirmLabel = new JLabel();
	private boolean lockedConfirm = true;

	static
	{
		EQUIPMENT_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(InventoryPresetsPlugin.class, "confirm_icon.png"));
		INVENTORY_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(InventoryPresetsPlugin.class, "confirm_icon.png"));
		EQUIPMENT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(ImageUtil.bufferedImageFromImage(EQUIPMENT_ICON.getImage()), 0.6f));
		INVENTORY_HOVER_ICON =new ImageIcon(ImageUtil.alphaOffset(ImageUtil.bufferedImageFromImage(INVENTORY_ICON.getImage()), 0.6f));
		CONFIRM_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(InventoryPresetsPlugin.class, "confirm_icon.png"));
		CANCEL_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(InventoryPresetsPlugin.class, "cancel_icon.png"));

		final BufferedImage confirmIcon = ImageUtil.bufferedImageFromImage(CONFIRM_ICON.getImage());
		CONFIRM_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(confirmIcon, 0.54f));
		CONFIRM_LOCKED_ICON = new ImageIcon(ImageUtil.grayscaleImage(confirmIcon));
		CANCEL_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(ImageUtil.bufferedImageFromImage(CANCEL_ICON.getImage()), 0.6f));
	}

	InventoryPresetCreationPanel(InventoryPresetsPlugin plugin)
	{
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(8, 8, 8, 8));
		setLayout(new BorderLayout());

		instructionsLabel.setFont(FontManager.getRunescapeSmallFont());
		instructionsLabel.setForeground(Color.WHITE);

		JPanel actionsContainer = new JPanel(new GridLayout(1, 2, 8, 0));
		actionsContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		confirmLabel.setIcon(CONFIRM_LOCKED_ICON);
		confirmLabel.setToolTipText("Confirm and save");
		confirmLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				/* If the confirm button is not locked */
				if (!lockedConfirm)
				{
					plugin.finishCreation(false);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				confirmLabel.setIcon(lockedConfirm ? CONFIRM_LOCKED_ICON : CONFIRM_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				confirmLabel.setIcon(lockedConfirm ? CONFIRM_LOCKED_ICON : CONFIRM_ICON);
			}
		});

		JLabel cancelLabel = new JLabel(CANCEL_ICON);
		cancelLabel.setToolTipText("Cancel");
		cancelLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.finishCreation(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancelLabel.setIcon(CANCEL_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancelLabel.setIcon(CANCEL_ICON);
			}
		});

		actionsContainer.add(confirmLabel);
		actionsContainer.add(cancelLabel);

		add(instructionsLabel, BorderLayout.CENTER);
		add(actionsContainer, BorderLayout.EAST);
	}

	/* Unlocks the confirm button */
	public void unlockConfirm()
	{
		this.confirmLabel.setIcon(CONFIRM_ICON);
		this.lockedConfirm = false;
		instructionsLabel.setText("Confirm or cancel to finish.");
	}

	/* Locks the confirm button */
	public void lockConfirm()
	{
		this.confirmLabel.setIcon(CONFIRM_LOCKED_ICON);
		this.lockedConfirm = true;
		instructionsLabel.setText("Confirm to save preset.");
	}
}
