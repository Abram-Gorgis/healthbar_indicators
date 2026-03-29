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
import com.healthbar.model.IndicatorSetup;
import com.healthbar.model.TrackedEffectEntry;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class HealthbarIndicatorsPanel extends PluginPanel
{
	private static final Type SETUP_LIST_TYPE = new TypeToken<List<IndicatorSetup>>(){}.getType();
	private static final String PAGE_SETUPS = "setups";
	private static final String PAGE_EFFECTS = "effects";

	private final Gson gson;
	private final ConfigManager configManager;

	private final CardLayout cardLayout;
	private final javax.swing.JPanel cardPanel;

	private final SetupListPanel setupListPanel;
	private final EffectListPanel effectListPanel;

	private final List<IndicatorSetup> setups = new ArrayList<>();
	private IndicatorSetup activeSetup;
	private Runnable resetCallback;

	public HealthbarIndicatorsPanel(Gson gson, ConfigManager configManager, ItemManager itemManager, SpriteManager spriteManager)
	{
		super(false);
		this.gson = gson;
		this.configManager = configManager;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		cardLayout = new CardLayout();
		cardPanel = new javax.swing.JPanel(cardLayout);
		cardPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		setupListPanel = new SetupListPanel(
			this::navigateToEffects,
			this::createSetup,
			this::renameSetup,
			this::deleteSetup
		);

		effectListPanel = new EffectListPanel(
			itemManager,
			spriteManager,
			this::navigateToSetups,
			this::saveAndApply,
			() -> { if (resetCallback != null) resetCallback.run(); }
		);

		cardPanel.add(setupListPanel, PAGE_SETUPS);
		cardPanel.add(effectListPanel, PAGE_EFFECTS);
		add(cardPanel, BorderLayout.CENTER);

		loadSetups();
	}

	// =====================================================
	// Navigation
	// =====================================================

	private void navigateToSetups()
	{
		activeSetup = null;
		saveActiveSetupName(null);
		clearTrackedEffects();
		setupListPanel.rebuild(setups);
		cardLayout.show(cardPanel, PAGE_SETUPS);
	}

	private void navigateToEffects(IndicatorSetup setup)
	{
		activeSetup = setup;
		saveActiveSetupName(setup.getName());
		effectListPanel.setActiveSetup(setup);
		applyActiveSetup();
		effectListPanel.rebuild();
		cardLayout.show(cardPanel, PAGE_EFFECTS);
	}

	// =====================================================
	// Setup management
	// =====================================================

	private void loadSetups()
	{
		setups.clear();
		String json = configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.SETUPS_KEY
		);
		if (json != null && !json.isEmpty())
		{
			try
			{
				List<IndicatorSetup> loaded = gson.fromJson(json, SETUP_LIST_TYPE);
				if (loaded != null)
				{
					setups.addAll(loaded);
				}
			}
			catch (Exception e)
			{
				// Corrupted config
			}
		}

		if (setups.isEmpty())
		{
			List<TrackedEffectEntry> existing = loadLegacyEntries();
			if (!existing.isEmpty())
			{
				setups.add(new IndicatorSetup("Default", existing));
				saveSetups();
			}
		}

		setupListPanel.rebuild(setups);

		if (!restoreActiveSetup())
		{
			cardLayout.show(cardPanel, PAGE_SETUPS);
		}
	}

	private boolean restoreActiveSetup()
	{
		String activeName = configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.ACTIVE_SETUP_KEY
		);
		if (activeName == null || activeName.isEmpty())
		{
			return false;
		}

		for (IndicatorSetup setup : setups)
		{
			if (setup.getName().equals(activeName))
			{
				activeSetup = setup;
				effectListPanel.setActiveSetup(setup);
				applyActiveSetup();
				effectListPanel.rebuild();
				cardLayout.show(cardPanel, PAGE_EFFECTS);
				return true;
			}
		}
		return false;
	}

	private void saveActiveSetupName(String name)
	{
		configManager.setConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.ACTIVE_SETUP_KEY,
			name != null ? name : ""
		);
	}

	private List<TrackedEffectEntry> loadLegacyEntries()
	{
		String json = configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY
		);
		if (json != null && !json.isEmpty())
		{
			try
			{
				Type listType = new TypeToken<List<TrackedEffectEntry>>(){}.getType();
				List<TrackedEffectEntry> loaded = gson.fromJson(json, listType);
				if (loaded != null)
				{
					return loaded;
				}
			}
			catch (Exception e)
			{
				// Ignore
			}
		}
		return new ArrayList<>();
	}

	private void saveSetups()
	{
		configManager.setConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.SETUPS_KEY,
			gson.toJson(setups)
		);
	}

	private void saveAndApply()
	{
		saveSetups();
		applyActiveSetup();
	}

	private void applyActiveSetup()
	{
		String json = (activeSetup != null) ? gson.toJson(activeSetup.getEntries()) : "[]";
		configManager.setConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY,
			json
		);
	}

	private void clearTrackedEffects()
	{
		configManager.setConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY,
			"[]"
		);
	}

	private void createSetup()
	{
		String name = JOptionPane.showInputDialog(
			this,
			"Setup name:",
			"New Setup",
			JOptionPane.PLAIN_MESSAGE
		);
		if (name == null || name.trim().isEmpty())
		{
			return;
		}
		name = name.trim();

		for (IndicatorSetup s : setups)
		{
			if (s.getName().equals(name))
			{
				JOptionPane.showMessageDialog(this, "A setup with that name already exists.");
				return;
			}
		}

		IndicatorSetup newSetup = new IndicatorSetup(name, new ArrayList<>());
		setups.add(newSetup);
		saveSetups();
		navigateToEffects(newSetup);
	}

	private void renameSetup(IndicatorSetup setup)
	{
		String newName = JOptionPane.showInputDialog(
			this,
			"New name:",
			setup.getName()
		);
		if (newName == null || newName.trim().isEmpty())
		{
			return;
		}
		newName = newName.trim();

		for (IndicatorSetup s : setups)
		{
			if (s != setup && s.getName().equals(newName))
			{
				JOptionPane.showMessageDialog(this, "A setup with that name already exists.");
				return;
			}
		}

		setup.setName(newName);
		saveSetups();
		setupListPanel.rebuild(setups);

		if (activeSetup == setup)
		{
			saveActiveSetupName(newName);
			effectListPanel.setTitle(newName);
		}
	}

	private void deleteSetup(IndicatorSetup setup)
	{
		int confirm = JOptionPane.showConfirmDialog(
			this,
			"Delete setup \"" + setup.getName() + "\"?",
			"Delete Setup",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.WARNING_MESSAGE
		);
		if (confirm != JOptionPane.OK_OPTION)
		{
			return;
		}

		setups.remove(setup);
		saveSetups();

		if (activeSetup == setup)
		{
			navigateToSetups();
		}
		else
		{
			setupListPanel.rebuild(setups);
		}
	}

	public void setResetCallback(Runnable callback)
	{
		this.resetCallback = callback;
	}

	public void refresh()
	{
		loadSetups();
	}
}
