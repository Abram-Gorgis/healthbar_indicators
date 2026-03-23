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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.healthbar.HealthbarIndicatorsConfig;
import com.healthbar.model.BlinkMode;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
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
import javax.swing.JButton;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class HealthbarIndicatorsPanel extends PluginPanel
{
	private static final Gson GSON = new Gson();
	private static final Type ENTRY_LIST_TYPE = new TypeToken<List<TrackedEffectEntry>>(){}.getType();

	private final ConfigManager configManager;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;
	private final JPanel listPanel;
	private final List<TrackedEffectEntry> entries = new ArrayList<>();
	private Runnable resetCallback;

	public HealthbarIndicatorsPanel(ConfigManager configManager, ItemManager itemManager, SpriteManager spriteManager)
	{
		super(false);
		this.configManager = configManager;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Header with title and add button
		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JLabel titleLabel = new JLabel("Healthbar Indicators");
		titleLabel.setForeground(Color.WHITE);
		headerPanel.add(titleLabel, BorderLayout.WEST);

		JButton addButton = new JButton("+");
		addButton.setPreferredSize(new Dimension(30, 25));
		addButton.setToolTipText("Add a new tracked effect");
		addButton.addActionListener(e -> showAddDialog());
		headerPanel.add(addButton, BorderLayout.EAST);

		add(headerPanel, BorderLayout.NORTH);

		// Scrollable list of tracked effects
		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(listPanel);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, BorderLayout.CENTER);

		// Footer with Stop and Clear buttons
		JPanel footerPanel = new JPanel(new GridBagLayout());
		footerPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		footerPanel.setBorder(new EmptyBorder(6, 8, 8, 8));

		GridBagConstraints fc = new GridBagConstraints();
		fc.fill = GridBagConstraints.HORIZONTAL;
		fc.weightx = 1.0;
		fc.insets = new Insets(0, 2, 0, 2);

		JButton stopButton = new JButton("Stop Indicators");
		stopButton.setToolTipText("Stop all active indicators without removing them");
		stopButton.addActionListener(e ->
		{
			if (resetCallback != null)
			{
				resetCallback.run();
			}
		});
		fc.gridx = 0;
		footerPanel.add(stopButton, fc);

		JButton clearButton = new JButton("Clear All");
		clearButton.setToolTipText("Remove all tracked effects");
		clearButton.addActionListener(e ->
		{
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Remove all tracked effects?",
				"Clear All",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (confirm == JOptionPane.OK_OPTION)
			{
				entries.clear();
				saveEntries();
				rebuildList();
			}
		});
		fc.gridx = 1;
		footerPanel.add(clearButton, fc);

		add(footerPanel, BorderLayout.SOUTH);

		loadEntries();
	}

	private void loadEntries()
	{
		entries.clear();
		String json = configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY
		);
		if (json != null && !json.isEmpty())
		{
			try
			{
				List<TrackedEffectEntry> loaded = GSON.fromJson(json, ENTRY_LIST_TYPE);
				if (loaded != null)
				{
					entries.addAll(loaded);
				}
			}
			catch (Exception e)
			{
				// Corrupted config, start fresh
			}
		}
		rebuildList();
	}

	private void saveEntries()
	{
		String json = GSON.toJson(entries);
		configManager.setConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY,
			json
		);
	}

	private void rebuildList()
	{
		listPanel.removeAll();

		if (entries.isEmpty())
		{
			JLabel emptyLabel = new JLabel("No effects tracked. Click + to add.");
			emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			emptyLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
			listPanel.add(emptyLabel);
		}
		else
		{
			for (int i = 0; i < entries.size(); i++)
			{
				listPanel.add(buildEntryPanel(entries.get(i)));
				listPanel.add(Box.createRigidArea(new Dimension(0, 2)));
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
			// Effect no longer exists, skip
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
		int panelHeight = (effect.supportsThreshold()) ? 105 : 80;
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelHeight));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 2, 0, 2);

		// Icon
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = (effect.supportsThreshold()) ? 4 : 3;
		c.anchor = GridBagConstraints.CENTER;
		JLabel iconLabel = new JLabel();
		loadIcon(effect, iconLabel);
		panel.add(iconLabel, c);

		// Name
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		JLabel nameLabel = new JLabel(effect.getDisplayName());
		nameLabel.setForeground(Color.WHITE);
		panel.add(nameLabel, c);

		// Blink mode dropdown
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		JComboBox<BlinkMode> blinkCombo = new JComboBox<>(BlinkMode.values());
		blinkCombo.setSelectedItem(entry.getBlinkMode());
		blinkCombo.setPreferredSize(new Dimension(150, 20));
		blinkCombo.addActionListener(e ->
		{
			entry.setBlinkMode((BlinkMode) blinkCombo.getSelectedItem());
			saveEntries();
		});
		panel.add(blinkCombo, c);

		// Alert after X levels drained (only for skill boost effects)
		int rowCount = 2;
		if (effect.supportsThreshold())
		{
			c.gridx = 1;
			c.gridy = rowCount;
			c.gridheight = 1;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;

			JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			thresholdPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

			JLabel threshLabel = new JLabel("Min boost:");
			threshLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			threshLabel.setToolTipText("Alert when boost drops to this level or below (0 = only alert when fully gone)");
			thresholdPanel.add(threshLabel);

			JTextField threshField = new JTextField(String.valueOf(entry.getDropThreshold()), 3);
			threshField.setPreferredSize(new Dimension(40, 20));
			addSaveOnChangeListener(threshField, val ->
			{
				entry.setDropThreshold(val);
				saveEntries();
			});
			thresholdPanel.add(threshField);

			panel.add(thresholdPanel, c);
			rowCount++;
		}

		// Timeout row
		c.gridx = 1;
		c.gridy = rowCount;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel timeoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		timeoutPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JLabel timeoutLabel = new JLabel("Timeout (min):");
		timeoutLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		timeoutLabel.setToolTipText("Stop tracking after inactive for this many minutes (0 = never timeout)");
		timeoutPanel.add(timeoutLabel);

		JTextField timeoutField = new JTextField(String.valueOf(entry.getTimeoutMinutes()), 3);
		timeoutField.setPreferredSize(new Dimension(40, 20));
		addSaveOnChangeListener(timeoutField, val ->
		{
			entry.setTimeoutMinutes(val);
			saveEntries();
		});
		timeoutPanel.add(timeoutField);

		panel.add(timeoutPanel, c);
		rowCount++;

		// Remove button
		c.gridx = 2;
		c.gridy = 0;
		c.gridheight = rowCount;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		JButton removeBtn = new JButton("X");
		removeBtn.setPreferredSize(new Dimension(25, 25));
		removeBtn.setMargin(new Insets(0, 0, 0, 0));
		removeBtn.setToolTipText("Remove this effect");
		removeBtn.addActionListener(e ->
		{
			entries.remove(entry);
			saveEntries();
			rebuildList();
		});
		panel.add(removeBtn, c);

		return panel;
	}

	private void loadIcon(TrackedEffect effect, JLabel label)
	{
		if (effect.isItemSprite())
		{
			BufferedImage img = itemManager.getImage(effect.getDefaultSpriteId());
			if (img != null)
			{
				label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, 24, 24)));
				// AsyncBufferedImage may not be loaded yet — register callback to update when ready
				if (img instanceof net.runelite.client.util.AsyncBufferedImage)
				{
					((net.runelite.client.util.AsyncBufferedImage) img).onLoaded(() ->
						SwingUtilities.invokeLater(() ->
							label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, 24, 24)))));
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
						label.setIcon(new ImageIcon(ImageUtil.resizeImage(img, 24, 24))));
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
		JDialog dialog = new JDialog(
			SwingUtilities.getWindowAncestor(this),
			"Add Effect",
			JDialog.DEFAULT_MODALITY_TYPE
		);
		dialog.setLayout(new BorderLayout(0, 5));
		dialog.setMinimumSize(new Dimension(300, 400));

		// Search field
		JTextField searchField = new JTextField();
		searchField.setBorder(BorderFactory.createCompoundBorder(
			new EmptyBorder(8, 8, 4, 8),
			searchField.getBorder()
		));
		dialog.add(searchField, BorderLayout.NORTH);

		// Build the list of available effects (not already tracked)
		List<TrackedEffect> available = getAvailableEffects();
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
				// Category header
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

		// Search filtering
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

		// Add button
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addBtn = new JButton("Add");
		addBtn.addActionListener(e ->
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
				entries.add(new TrackedEffectEntry(selected.name(), selected.getDefaultBlinkMode(), 0, 20));
				saveEntries();
				rebuildList();
				dialog.dispose();
			}
		});
		buttonPanel.add(addBtn);

		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(e -> dialog.dispose());
		buttonPanel.add(cancelBtn);

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

	private List<TrackedEffect> getAvailableEffects()
	{
		Set<String> alreadyTracked = entries.stream()
			.map(TrackedEffectEntry::getEffectName)
			.collect(Collectors.toSet());

		return Arrays.stream(TrackedEffect.values())
			.filter(e -> !alreadyTracked.contains(e.name()))
			.sorted(Comparator.comparing(TrackedEffect::getCategory)
				.thenComparing(TrackedEffect::getDisplayName))
			.collect(Collectors.toList());
	}

	public List<TrackedEffectEntry> getTrackedEntries()
	{
		return entries;
	}

	public void setResetCallback(Runnable callback)
	{
		this.resetCallback = callback;
	}

	public void refresh()
	{
		loadEntries();
	}
}
