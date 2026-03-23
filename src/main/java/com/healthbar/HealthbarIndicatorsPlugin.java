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
package com.healthbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.healthbar.model.BlinkMode;
import com.healthbar.model.EffectDetectionType;
import com.healthbar.model.EffectTracker;
import com.healthbar.model.TrackedEffect;
import com.healthbar.model.TrackedEffectEntry;
import com.healthbar.ui.HealthbarIndicatorsOverlay;
import com.healthbar.ui.HealthbarIndicatorsPanel;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Healthbar Indicators",
	description = "Flashes indicator icons above your character when tracked effects expire or activate",
	tags = {"health", "indicator", "flashing", "potion", "prayer", "overlay", "timer"}
)
public class HealthbarIndicatorsPlugin extends Plugin
{
	private static final Gson GSON = new Gson();
	private static final Type ENTRY_LIST_TYPE = new TypeToken<List<TrackedEffectEntry>>(){}.getType();
	private static final int NAV_ICON_SIZE = 16;
	private static final int VENOM_IMMUNITY_THRESHOLD = -38;

	@Inject
	private Client client;

	@Inject
	private HealthbarIndicatorsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HealthbarIndicatorsOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ClientThread clientThread;

	private HealthbarIndicatorsPanel panel;
	private NavigationButton navButton;
	private final Map<TrackedEffect, EffectTracker> trackers = new HashMap<>();
	private final Set<TrackedEffect> chatActivatedEffects = new HashSet<>();
	private final List<TrackedEffectEntry> flashingEntriesBuffer = new ArrayList<>();
	private volatile List<TrackedEffectEntry> cachedEntries = Collections.emptyList();

	@Override
	protected void startUp()
	{
		reloadTrackedEntries();
		overlayManager.add(overlay);

		panel = new HealthbarIndicatorsPanel(configManager, itemManager, spriteManager);
		panel.setResetCallback(() -> clientThread.invokeLater(this::resetAllTrackers));

		spriteManager.getSpriteAsync(SpriteID.SKILL_HITPOINTS, 0, img ->
		{
			final BufferedImage icon = (img != null)
				? ImageUtil.resizeImage(img, NAV_ICON_SIZE, NAV_ICON_SIZE)
				: new BufferedImage(NAV_ICON_SIZE, NAV_ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

			navButton = NavigationButton.builder()
				.tooltip("Healthbar Indicators")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navButton);
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		if (navButton != null)
		{
			clientToolbar.removeNavigation(navButton);
		}
		trackers.clear();
		chatActivatedEffects.clear();
		overlay.clearSpriteCache();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (HealthbarIndicatorsConfig.CONFIG_GROUP.equals(event.getGroup())
			&& HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY.equals(event.getKey()))
		{
			reloadTrackedEntries();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.HOPPING || state == GameState.LOGIN_SCREEN)
		{
			resetAllTrackers();
			overlay.clearSpriteCache();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		long now = System.currentTimeMillis();
		Skill changedSkill = event.getSkill();

		for (TrackedEffectEntry entry : getTrackedEntries())
		{
			TrackedEffect effect = entry.getEffect();
			if (effect == null || !effect.tracksSkill(changedSkill))
			{
				continue;
			}

			EffectTracker tracker = getOrCreateTracker(effect);
			boolean active = isSkillBoostActive(effect);
			boolean divineActive = updateDivineState(effect, tracker);

			if (active)
			{
				tracker.markActive(now);
			}

			int currentBoost = getCurrentBoostLevel(effect);
			tracker.updateBoost(currentBoost);

			if (entry.getDropThreshold() > 0)
			{
				tracker.processThreshold(currentBoost, entry.getDropThreshold(), divineActive, now);
			}
			else
			{
				tracker.processOnExpire(active, divineActive, true, now);
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		long now = System.currentTimeMillis();

		// Deactivate chat-based effects when their associated varbit goes to 0
		if (!chatActivatedEffects.isEmpty())
		{
			java.util.Iterator<TrackedEffect> it = chatActivatedEffects.iterator();
			while (it.hasNext())
			{
				TrackedEffect effect = it.next();
				if (!isEffectStillActiveByVarbit(effect))
				{
					it.remove();
					EffectTracker tracker = trackers.get(effect);
					if (tracker != null)
					{
						tracker.tryExpire(now);
					}
				}
			}
		}

		for (TrackedEffectEntry entry : getTrackedEntries())
		{
			TrackedEffect effect = entry.getEffect();
			if (effect == null)
			{
				continue;
			}

			if (effect.getDetectionType() == EffectDetectionType.SKILL_BOOST)
			{
				updateDivineStateIfTracked(effect);
				continue;
			}

			boolean active = isEffectCurrentlyActive(effect);
			EffectTracker tracker = getOrCreateTracker(effect);

			if (active)
			{
				tracker.markActive(now);
			}

			if (entry.getBlinkMode() == BlinkMode.WHILE_ACTIVE)
			{
				tracker.setWhileActiveState(active);
			}
			else
			{
				tracker.processOnExpire(active, false, false, now);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		String message = event.getMessage().toLowerCase();
		long now = System.currentTimeMillis();

		for (TrackedEffectEntry entry : getTrackedEntries())
		{
			TrackedEffect effect = entry.getEffect();
			if (effect == null || effect.getDetectionType() != EffectDetectionType.CHAT_MESSAGE)
			{
				continue;
			}

			if (message.contains(effect.getChatPattern()))
			{
				chatActivatedEffects.add(effect);
				getOrCreateTracker(effect).activate(now);
			}
		}
	}

	private EffectTracker getOrCreateTracker(TrackedEffect effect)
	{
		return trackers.computeIfAbsent(effect, e -> new EffectTracker());
	}

	private boolean updateDivineState(TrackedEffect effect, EffectTracker tracker)
	{
		if (effect.getDivineVarbitId() < 0)
		{
			return false;
		}
		boolean divineActive = client.getVarbitValue(effect.getDivineVarbitId()) > 0;
		tracker.updateDivine(divineActive);
		return divineActive;
	}

	private void updateDivineStateIfTracked(TrackedEffect effect)
	{
		if (effect.getDivineVarbitId() >= 0)
		{
			EffectTracker tracker = trackers.get(effect);
			if (tracker != null)
			{
				tracker.updateDivine(client.getVarbitValue(effect.getDivineVarbitId()) > 0);
			}
		}
	}

	private boolean isSkillBoostActive(TrackedEffect effect)
	{
		for (Skill skill : effect.getSkills())
		{
			if (client.getBoostedSkillLevel(skill) > client.getRealSkillLevel(skill))
			{
				return true;
			}
		}
		return false;
	}

	private int getCurrentBoostLevel(TrackedEffect effect)
	{
		Skill[] skills = effect.getSkills();
		int maxBoost = 0;
		for (Skill skill : skills)
		{
			int boost = client.getBoostedSkillLevel(skill) - client.getRealSkillLevel(skill);
			if (boost > maxBoost)
			{
				maxBoost = boost;
			}
		}
		return maxBoost;
	}

	public List<TrackedEffectEntry> getFlashingEntries()
	{
		flashingEntriesBuffer.clear();
		long now = System.currentTimeMillis();

		for (TrackedEffectEntry entry : getTrackedEntries())
		{
			TrackedEffect effect = entry.getEffect();
			if (effect == null)
			{
				continue;
			}

			EffectTracker tracker = trackers.get(effect);
			if (tracker == null)
			{
				continue;
			}

			if (tracker.isTimedOut(entry.getTimeoutMinutes(), now))
			{
				tracker.reset();
				chatActivatedEffects.remove(effect);
				continue;
			}

			if (tracker.isFlashing(entry.getBlinkMode()))
			{
				flashingEntriesBuffer.add(entry);
			}
		}
		return flashingEntriesBuffer;
	}

	private void reloadTrackedEntries()
	{
		String json = configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY
		);
		if (json == null || json.isEmpty())
		{
			json = "[]";
		}

		try
		{
			List<TrackedEffectEntry> list = GSON.fromJson(json, ENTRY_LIST_TYPE);
			cachedEntries = list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
		}
		catch (Exception e)
		{
			log.warn("Failed to parse tracked effects config", e);
			cachedEntries = Collections.emptyList();
		}

		pruneStaleTrackers();
	}

	private void pruneStaleTrackers()
	{
		Set<TrackedEffect> activeEffects = new HashSet<>();
		for (TrackedEffectEntry entry : cachedEntries)
		{
			TrackedEffect effect = entry.getEffect();
			if (effect != null)
			{
				activeEffects.add(effect);
			}
		}
		trackers.keySet().retainAll(activeEffects);
		chatActivatedEffects.retainAll(activeEffects);
	}

	private List<TrackedEffectEntry> getTrackedEntries()
	{
		return cachedEntries;
	}

	private boolean isEffectCurrentlyActive(TrackedEffect effect)
	{
		switch (effect.getDetectionType())
		{
			case VARBIT:
				return client.getVarbitValue(effect.getVarbitId()) > 0;

			case PRAYER:
				return client.isPrayerActive(effect.getPrayer());

			case POISON_IMMUNITY:
				return client.getVarpValue(VarPlayerID.POISON) < 0;

			case VENOM_IMMUNITY:
				return client.getVarpValue(VarPlayerID.POISON) < VENOM_IMMUNITY_THRESHOLD;

			case CHAT_MESSAGE:
				return chatActivatedEffects.contains(effect);

			default:
				return false;
		}
	}

	private boolean isEffectStillActiveByVarbit(TrackedEffect effect)
	{
		int varbitId = effect.getDeactivationVarbitId();
		if (varbitId < 0)
		{
			return true;
		}
		return client.getVarbitValue(varbitId) > 0;
	}

	private void resetAllTrackers()
	{
		for (EffectTracker tracker : trackers.values())
		{
			tracker.reset();
		}
		chatActivatedEffects.clear();
	}

	@Provides
	HealthbarIndicatorsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HealthbarIndicatorsConfig.class);
	}
}
