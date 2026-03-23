/*
 * Copyright (c) 2026, Abram
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
package com.healthbar.ui;

import com.healthbar.model.BlinkMode;
import com.healthbar.model.IndicatorSetup;
import com.healthbar.model.TrackedEffect;
import com.healthbar.model.TrackedEffectEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

public class EffectListPanel extends JPanel
{
	private static final int ICON_SIZE = 24;
	private static final int ICON_COL = 0;
	private static final int CONTENT_COL = 1;
	private static final int REMOVE_COL = 2;
	private static final int ROW_HEIGHT_WITH_THRESHOLD = 105;
	private static final int ROW_HEIGHT_WITHOUT_THRESHOLD = 80;
	private static final int FIELD_WIDTH = 40;
	private static final int FIELD_HEIGHT = 20;
	private static final int FIELD_COLUMNS = 3;
	private static final int COMBO_WIDTH = 150;
	private static final int SMALL_BTN_SIZE = 25;
	private static final int ADD_BTN_SIZE = 30;
	private static final int ADD_BTN_HEIGHT = 25;
	private static final int DIALOG_MIN_WIDTH = 300;
	private static final int DIALOG_MIN_HEIGHT = 400;
	private static final int ROW_SPACING = 2;

	private final JLabel titleLabel;
	private final JPanel listPanel;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	private final Runnable onBack;
	private final Runnable onSave;
	private final Runnable onReset;

	private IndicatorSetup activeSetup;

	public EffectListPanel(ItemManager itemManager, SpriteManager spriteManager,
		Runnable onBack, Runnable onSave, Runnable onReset)
	{
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.onBack = onBack;
		this.onSave = onSave;
		this.onReset = onReset;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		titleLabel = new JLabel();
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setHorizontalAlignment(JLabel.CENTER);

		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(buildHeader(), BorderLayout.NORTH);
		add(buildScrollableList(), BorderLayout.CENTER);
		add(buildFooter(), BorderLayout.SOUTH);
	}

	private JPanel buildHeader()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(10, 10, 5, 10));

		header.add(ButtonBuilder.create("\u2190 Back")
			.margin(2, 6, 2, 6)
			.tooltip("Back to setups (deactivates indicators)")
			.onClick(e -> onBack.run())
			.build(), BorderLayout.WEST);

		header.add(ButtonBuilder.create("+")
			.size(ADD_BTN_SIZE, ADD_BTN_HEIGHT)
			.tooltip("Add a new tracked effect")
			.onClick(e -> showAddDialog())
			.build(), BorderLayout.EAST);

		header.add(titleLabel, BorderLayout.CENTER);

		return header;
	}

	private JScrollPane buildScrollableList()
	{
		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	private JPanel buildFooter()
	{
		JPanel footer = new JPanel(new GridBagLayout());
		footer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footer.setBorder(new EmptyBorder(6, 8, 8, 8));

		GridBagConstraints fc = new GridBagConstraints();
		fc.fill = GridBagConstraints.HORIZONTAL;
		fc.weightx = 1.0;
		fc.insets = new Insets(0, ROW_SPACING, 0, ROW_SPACING);

		fc.gridx = 0;
		footer.add(ButtonBuilder.create("Stop Indicators")
			.tooltip("Stop all active indicators without removing them")
			.onClick(e -> onReset.run())
			.build(), fc);

		fc.gridx = 1;
		footer.add(ButtonBuilder.create("Clear All")
			.tooltip("Remove all tracked effects from this setup")
			.onClick(e -> clearAllEffects())
			.build(), fc);

		return footer;
	}

	private void clearAllEffects()
	{
		if (activeSetup == null)
		{
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(
			this,
			"Remove all tracked effects from \"" + activeSetup.getName() + "\"?",
			"Clear All",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE
		);
		if (confirm == JOptionPane.OK_OPTION)
		{
			activeSetup.getEntries().clear();
			onSave.run();
			rebuild();
		}
	}

	public void setActiveSetup(IndicatorSetup setup)
	{
		this.activeSetup = setup;
		titleLabel.setText(setup != null ? setup.getName() : "");
	}

	public void setTitle(String title)
	{
		titleLabel.setText(title);
	}

	public void rebuild()
	{
		listPanel.removeAll();

		if (activeSetup == null || activeSetup.getEntries().isEmpty())
		{
			JLabel emptyLabel = new JLabel("No effects tracked. Click + to add.");
			emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			emptyLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
			listPanel.add(emptyLabel);
		}
		else
		{
			for (TrackedEffectEntry entry : activeSetup.getEntries())
			{
				listPanel.add(buildEntryPanel(entry));
				listPanel.add(Box.createRigidArea(new Dimension(0, ROW_SPACING)));
			}
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel buildEntryPanel(TrackedEffectEntry entry)
	{
		TrackedEffect effect = entry.getEffect();
		if (effect == null)
		{
			JPanel errorPanel = new JPanel();
			errorPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			errorPanel.add(new JLabel("Unknown: " + entry.getEffectName()));
			return errorPanel;
		}

		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		panel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			new EmptyBorder(6, 8, 6, 8)
		));
		int panelHeight = effect.supportsThreshold() ? ROW_HEIGHT_WITH_THRESHOLD : ROW_HEIGHT_WITHOUT_THRESHOLD;
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelHeight));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, ROW_SPACING, 0, ROW_SPACING);

		addIconColumn(panel, c, effect);
		addNameRow(panel, c, effect);
		int row = addBlinkModeRow(panel, c, entry);
		row = addThresholdRow(panel, c, entry, effect, row);
		row = addTimeoutRow(panel, c, entry, row);
		addRemoveButton(panel, c, entry, row);

		return panel;
	}

	private void addIconColumn(JPanel panel, GridBagConstraints c, TrackedEffect effect)
	{
		c.gridx = ICON_COL;
		c.gridy = 0;
		c.gridheight = effect.supportsThreshold() ? 4 : 3;
		c.anchor = GridBagConstraints.CENTER;
		JLabel iconLabel = new JLabel();
		loadIcon(effect, iconLabel);
		panel.add(iconLabel, c);
	}

	private void addNameRow(JPanel panel, GridBagConstraints c, TrackedEffect effect)
	{
		c.gridx = CONTENT_COL;
		c.gridy = 0;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		JLabel nameLabel = new JLabel(effect.getDisplayName());
		nameLabel.setForeground(Color.WHITE);
		panel.add(nameLabel, c);
	}

	private int addBlinkModeRow(JPanel panel, GridBagConstraints c, TrackedEffectEntry entry)
	{
		c.gridx = CONTENT_COL;
		c.gridy = 1;
		c.weightx = 1.0;
		JComboBox<BlinkMode> blinkCombo = new JComboBox<>(BlinkMode.values());
		blinkCombo.setSelectedItem(entry.getBlinkMode());
		blinkCombo.setPreferredSize(new Dimension(COMBO_WIDTH, FIELD_HEIGHT));
		blinkCombo.addActionListener(e ->
		{
			entry.setBlinkMode((BlinkMode) blinkCombo.getSelectedItem());
			onSave.run();
		});
		panel.add(blinkCombo, c);
		return 2;
	}

	private int addThresholdRow(JPanel panel, GridBagConstraints c, TrackedEffectEntry entry,
		TrackedEffect effect, int row)
	{
		if (!effect.supportsThreshold())
		{
			return row;
		}

		c.gridx = CONTENT_COL;
		c.gridy = row;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ROW_SPACING, 0));
		thresholdPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel threshLabel = new JLabel("Min boost:");
		threshLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		threshLabel.setToolTipText("Alert when boost drops to this level or below (0 = only alert when fully gone)");
		thresholdPanel.add(threshLabel);

		JTextField threshField = new JTextField(String.valueOf(entry.getDropThreshold()), FIELD_COLUMNS);
		threshField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
		addSaveOnChangeListener(threshField, val ->
		{
			entry.setDropThreshold(val);
			onSave.run();
		});
		thresholdPanel.add(threshField);

		panel.add(thresholdPanel, c);
		return row + 1;
	}

	private int addTimeoutRow(JPanel panel, GridBagConstraints c, TrackedEffectEntry entry, int row)
	{
		c.gridx = CONTENT_COL;
		c.gridy = row;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel timeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, ROW_SPACING, 0));
		timeoutPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel timeoutLabel = new JLabel("Timeout (min):");
		timeoutLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		timeoutLabel.setToolTipText("Stop tracking after inactive for this many minutes (0 = never timeout)");
		timeoutPanel.add(timeoutLabel);

		JTextField timeoutField = new JTextField(String.valueOf(entry.getTimeoutMinutes()), FIELD_COLUMNS);
		timeoutField.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
		addSaveOnChangeListener(timeoutField, val ->
		{
			entry.setTimeoutMinutes(val);
			onSave.run();
		});
		timeoutPanel.add(timeoutField);

		panel.add(timeoutPanel, c);
		return row + 1;
	}

	private void addRemoveButton(JPanel panel, GridBagConstraints c, TrackedEffectEntry entry, int totalRows)
	{
		c.gridx = REMOVE_COL;
		c.gridy = 0;
		c.gridheight = totalRows;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		panel.add(ButtonBuilder.create("X")
			.size(SMALL_BTN_SIZE, SMALL_BTN_SIZE)
			.margin(0, 0, 0, 0)
			.tooltip("Remove this effect")
			.onClick(e ->
			{
				if (activeSetup != null)
				{
					activeSetup.getEntries().remove(entry);
					onSave.run();
					rebuild();
				}
			})
			.build(), c);
	}

	private void loadIcon(TrackedEffect effect, JLabel label)
	{
		if (effect.isItemSprite())
		{
			BufferedImage img = itemManager.getImage(effect.getDefaultSpriteId());
			if (img != null)
			{
				label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, ICON_SIZE, ICON_SIZE)));
				if (img instanceof net.runelite.client.util.AsyncBufferedImage)
				{
					((net.runelite.client.util.AsyncBufferedImage) img).onLoaded(() ->
						SwingUtilities.invokeLater(() ->
							label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, ICON_SIZE, ICON_SIZE)))));
				}
			}
		}
		else
		{
			spriteManager.getSpriteAsync(effect.getDefaultSpriteId(), 0, img ->
			{
				if (img != null)
				{
					SwingUtilities.invokeLater(() ->
						label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, ICON_SIZE, ICON_SIZE))));
				}
			});
		}
	}

	private void addSaveOnChangeListener(JTextField field, java.util.function.IntConsumer onValid)
	{
		field.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e) { update(); }
			@Override
			public void removeUpdate(DocumentEvent e) { update(); }
			@Override
			public void changedUpdate(DocumentEvent e) { update(); }

			private void update()
			{
				try
				{
					int val = Integer.parseInt(field.getText().trim());
					if (val >= 0)
					{
						onValid.accept(val);
					}
				}
				catch (NumberFormatException ignored) { }
			}
		});
	}

	private void showAddDialog()
	{
		if (activeSetup == null)
		{
			return;
		}

		JDialog dialog = new JDialog(
			SwingUtilities.getWindowAncestor(this),
			"Add Effect",
			JDialog.DEFAULT_MODALITY_TYPE
		);
		dialog.setLayout(new BorderLayout(0, 5));
		dialog.setMinimumSize(new Dimension(DIALOG_MIN_WIDTH, DIALOG_MIN_HEIGHT));

		JTextField searchField = new JTextField();
		searchField.setBorder(BorderFactory.createCompoundBorder(
			new EmptyBorder(8, 8, 4, 8),
			searchField.getBorder()
		));
		dialog.add(searchField, BorderLayout.NORTH);

		List<TrackedEffect> available = getAvailableEffects(activeSetup);
		DefaultListModel<String> listModel = new DefaultListModel<>();
		populateListModel(listModel, available, "");

		JList<String> effectList = new JList<>(listModel);
		effectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		effectList.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		effectList.setForeground(Color.WHITE);
		effectList.setCellRenderer((list, value, index, isSelected, cellHasFocus) ->
		{
			JLabel label = new JLabel(value);
			label.setOpaque(true);
			if (value.startsWith("— "))
			{
				label.setBackground(ColorScheme.DARK_GRAY_COLOR);
				label.setForeground(ColorScheme.BRAND_ORANGE);
				label.setBorder(new EmptyBorder(4, 4, 4, 4));
			}
			else
			{
				label.setBackground(isSelected ? ColorScheme.DARK_GRAY_COLOR : ColorScheme.DARKER_GRAY_COLOR);
				label.setForeground(Color.WHITE);
				label.setBorder(new EmptyBorder(2, 12, 2, 4));
			}
			return label;
		});

		JScrollPane listScroll = new JScrollPane(effectList);
		listScroll.setBorder(new EmptyBorder(0, 8, 0, 8));
		dialog.add(listScroll, BorderLayout.CENTER);

		searchField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e) { filter(); }
			@Override
			public void removeUpdate(DocumentEvent e) { filter(); }
			@Override
			public void changedUpdate(DocumentEvent e) { filter(); }

			private void filter()
			{
				String text = searchField.getText().toLowerCase().trim();
				populateListModel(listModel, available, text);
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(ButtonBuilder.create("Add")
			.onClick(e ->
			{
				String selectedName = effectList.getSelectedValue();
				if (selectedName == null || selectedName.startsWith("— "))
				{
					return;
				}
				TrackedEffect selected = available.stream()
					.filter(ef -> ef.getDisplayName().equals(selectedName))
					.findFirst()
					.orElse(null);
				if (selected != null)
				{
					activeSetup.getEntries().add(new TrackedEffectEntry(selected.name(), selected.getDefaultBlinkMode(), 0, 20));
					onSave.run();
					rebuild();
					dialog.dispose();
				}
			})
			.build());

		buttonPanel.add(ButtonBuilder.create("Cancel")
			.onClick(e -> dialog.dispose())
			.build());

		dialog.add(buttonPanel, BorderLayout.SOUTH);

		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
	}

	private void populateListModel(DefaultListModel<String> model, List<TrackedEffect> available, String filter)
	{
		model.clear();
		String currentCategory = null;
		for (TrackedEffect effect : available)
		{
			if (!filter.isEmpty()
				&& !effect.getDisplayName().toLowerCase().contains(filter)
				&& !effect.getCategory().toLowerCase().contains(filter))
			{
				continue;
			}

			if (!effect.getCategory().equals(currentCategory))
			{
				currentCategory = effect.getCategory();
				model.addElement("— " + currentCategory + " —");
			}
			model.addElement(effect.getDisplayName());
		}
	}

	private List<TrackedEffect> getAvailableEffects(IndicatorSetup setup)
	{
		Set<String> alreadyTracked = setup.getEntries().stream()
			.map(TrackedEffectEntry::getEffectName)
			.collect(Collectors.toSet());

		return Arrays.stream(TrackedEffect.values())
			.filter(e -> !alreadyTracked.contains(e.name()))
			.sorted(Comparator.comparing(TrackedEffect::getCategory)
				.thenComparing(TrackedEffect::getDisplayName))
			.collect(Collectors.toList());
	}
}
