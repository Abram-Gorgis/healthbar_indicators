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
import com.healthbar.model.BlinkMode;
import com.healthbar.model.EffectState;
import com.healthbar.model.EffectTracker;
import com.healthbar.model.TrackedEffect;
import com.healthbar.model.TrackedEffectEntry;
import com.healthbar.ui.HealthbarIndicatorsOverlay;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthbarIndicatorsPluginUnitTest
{
	private static final Gson GSON = new Gson();

	@Mock
	private Client client;

	@Mock
	private ConfigManager configManager;

	@Mock
	private OverlayManager overlayManager;

	@Mock
	private HealthbarIndicatorsOverlay overlay;

	@Mock
	private ClientThread clientThread;

	private HealthbarIndicatorsPlugin plugin;

	@Before
	public void setUp() throws Exception
	{
		plugin = new HealthbarIndicatorsPlugin();
		setField("client", client);
		setField("configManager", configManager);
		setField("overlayManager", overlayManager);
		setField("overlay", overlay);
		setField("clientThread", clientThread);
	}

	private void setField(String name, Object value) throws Exception
	{
		Field field = HealthbarIndicatorsPlugin.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(plugin, value);
	}

	// =====================================================
	// Helper methods
	// =====================================================

	private void setTrackedEntries(TrackedEffectEntry... entries)
	{
		String json = GSON.toJson(entries);
		when(configManager.getConfiguration(
			HealthbarIndicatorsConfig.CONFIG_GROUP,
			HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY
		)).thenReturn(json);

		ConfigChanged event = new ConfigChanged();
		event.setGroup(HealthbarIndicatorsConfig.CONFIG_GROUP);
		event.setKey(HealthbarIndicatorsConfig.TRACKED_EFFECTS_KEY);
		plugin.onConfigChanged(event);
	}

	private TrackedEffectEntry entry(TrackedEffect effect, BlinkMode mode, int threshold, int timeout)
	{
		return new TrackedEffectEntry(effect.name(), mode, threshold, timeout);
	}

	private StatChanged statChanged(Skill skill)
	{
		return new StatChanged(skill, 0, 0, 0);
	}

	private VarbitChanged varbitChanged()
	{
		return new VarbitChanged();
	}

	private ChatMessage chatMessage(String text)
	{
		return new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", text, "", 0);
	}

	@SuppressWarnings("unchecked")
	private Map<TrackedEffect, EffectTracker> getTrackerMap() {
		try {
			Field field = HealthbarIndicatorsPlugin.class.getDeclaredField("trackers");
			field.setAccessible(true);
			return (Map<TrackedEffect, EffectTracker>) field.get(plugin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void backdateExpiry(EffectTracker tracker, long millis)
	{
		try
		{
			Field field = EffectTracker.class.getDeclaredField("expiredAtMillis");
			field.setAccessible(true);
			field.setLong(tracker, millis);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void simulateBoost(Skill skill, int real, int boosted)
	{
		lenient().when(client.getRealSkillLevel(skill)).thenReturn(real);
		lenient().when(client.getBoostedSkillLevel(skill)).thenReturn(boosted);
	}


	/**
	 * Sets baseline unboosted stats and fires a stat event so drink detection
	 * can distinguish a fresh drink (boost increase) from a pre-login boost.
	 */
	private void establishCombatBaseline()
	{
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
	}

	// =====================================================
	// Regular Potion Tests (SKILL_BOOST, ON_EXPIRE)
	// =====================================================

	@Test
	public void testPotionDrinkAndExpire()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Drink super combat — boosted above real (drink detected: 0 → 19)
		simulateBoost(Skill.ATTACK, 99, 118);
		simulateBoost(Skill.STRENGTH, 99, 118);
		simulateBoost(Skill.DEFENCE, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Should not flash while boost is active", plugin.getFlashingEntries().isEmpty());

		// Boost wears off
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		List<TrackedEffectEntry> flashing = plugin.getFlashingEntries();
		assertEquals("Should flash when boost expires", 1, flashing.size());
		assertEquals(TrackedEffect.SUPER_COMBAT.name(), flashing.get(0).getEffectName());
	}

	@Test
	public void testPotionRedrinkWhileFlashing()
	{
		setTrackedEntries(entry(TrackedEffect.RANGING_POTION, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Baseline then drink
		simulateBoost(Skill.RANGED, 99, 99);
		plugin.onStatChanged(statChanged(Skill.RANGED));
		simulateBoost(Skill.RANGED, 99, 112);
		plugin.onStatChanged(statChanged(Skill.RANGED));

		simulateBoost(Skill.RANGED, 99, 99);
		plugin.onStatChanged(statChanged(Skill.RANGED));
		assertEquals(1, plugin.getFlashingEntries().size());

		// Re-drink while flashing
		simulateBoost(Skill.RANGED, 99, 112);
		plugin.onStatChanged(statChanged(Skill.RANGED));

		assertTrue("Re-drinking should stop flashing", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testNoFalseFlashOnLogin()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// No boost at login — stat at normal level
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Should not flash on login with no boost", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testIrrelevantSkillIgnored()
	{
		setTrackedEntries(entry(TrackedEffect.RANGING_POTION, BlinkMode.ON_EXPIRE, 0, 20));
		lenient().when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Fire a Magic stat change — irrelevant to Ranging
		simulateBoost(Skill.RANGED, 99, 112);
		plugin.onStatChanged(statChanged(Skill.MAGIC));

		assertTrue("Irrelevant skill changes should be ignored", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Threshold Tests (alert when boost drops to threshold or below)
	// =====================================================

	@Test
	public void testThresholdFlashOnDrop()
	{
		// Threshold 9 = alert when boost drops to +9 or below
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Boosted to +12 — above threshold
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertTrue(plugin.getFlashingEntries().isEmpty());

		// Drop to +9 — at threshold, should flash
		simulateBoost(Skill.ATTACK, 99, 108);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Should flash when boost drops to threshold", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testThresholdRedrinkStopsFlashing()
	{
		// Threshold 9 = alert when boost drops to +9 or below
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Start above threshold, drop to threshold
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		simulateBoost(Skill.ATTACK, 99, 108);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertEquals(1, plugin.getFlashingEntries().size());

		// Re-drink back above threshold
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Re-drinking should stop threshold flashing", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testThresholdTransitionsToExpiredAtZero()
	{
		// Threshold 9 = alert when boost drops to +9 or below
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Start above, drop to threshold, then fully expire
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		simulateBoost(Skill.ATTACK, 99, 108);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Should still flash after full expiry", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testThresholdNotReachedDoesNotFlash()
	{
		// Threshold 9 = alert when boost drops to +9 or below
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Boosted to +12
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Drop to +10 — still above threshold of 9
		simulateBoost(Skill.ATTACK, 99, 109);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Should not flash above threshold", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Divine Potion Tests (guardrail)
	// =====================================================

	@Test
	public void testDivineExpireDoesNotFlashRegular()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		establishCombatBaseline();

		// Divine is active
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(1);

		simulateBoost(Skill.ATTACK, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Divine expires — boost drops to 0
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(0);
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Divine expiry should not trigger regular potion flash", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testRegularPotionExpiresNormallyWithoutDivine()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		simulateBoost(Skill.ATTACK, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Regular potion should flash on expire", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testDivineVarbitTrackedViaVarbitChanged()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Boost becomes active via stat change
		simulateBoost(Skill.ATTACK, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Then divine varbit activates via VarbitChanged
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(1);
		plugin.onVarbitChanged(varbitChanged());

		// Now divine expires and boost drops
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(0);
		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Divine guardrail via VarbitChanged should prevent flash", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testDivineWithThresholdDoesNotFlash()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));

		// Divine is active
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(1);

		simulateBoost(Skill.ATTACK, 99, 118);
		simulateBoost(Skill.STRENGTH, 99, 118);
		simulateBoost(Skill.DEFENCE, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Divine expires, boost drops to 0
		when(client.getVarbitValue(Varbits.DIVINE_SUPER_COMBAT)).thenReturn(0);
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Divine guardrail should work with threshold mode", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Prayer Tests (PRAYER detection, ON_EXPIRE)
	// =====================================================

	@Test
	public void testPrayerActivateAndDeactivate()
	{
		setTrackedEntries(entry(TrackedEffect.PROTECT_FROM_MELEE, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());
		assertTrue("Should not flash while prayer is active", plugin.getFlashingEntries().isEmpty());

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());

		assertEquals("Should flash when prayer deactivated", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testPrayerReactivateStopsFlashing()
	{
		setTrackedEntries(entry(TrackedEffect.PIETY, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());
		when(client.isPrayerActive(Prayer.PIETY)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());
		assertEquals(1, plugin.getFlashingEntries().size());

		when(client.isPrayerActive(Prayer.PIETY)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());

		assertTrue("Reactivating prayer should stop flashing", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testPrayerNoFalseFlashOnLogin()
	{
		setTrackedEntries(entry(TrackedEffect.RIGOUR, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.isPrayerActive(Prayer.RIGOUR)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());

		assertTrue("Should not flash prayer on login", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testPrayerWhileActiveMode()
	{
		setTrackedEntries(entry(TrackedEffect.PROTECT_FROM_MISSILES, BlinkMode.WHILE_ACTIVE, 0, 20));

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());
		assertTrue(plugin.getFlashingEntries().isEmpty());

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());
		assertEquals("WHILE_ACTIVE should flash when prayer is on", 1, plugin.getFlashingEntries().size());

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());
		assertTrue("WHILE_ACTIVE should stop when prayer is off", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Thrall Tests (CHAT_MESSAGE + deactivation varbit)
	// =====================================================

	@Test
	public void testThrallActivateViaChatMessage()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		plugin.onChatMessage(chatMessage("You resurrect a lesser ghostly thrall."));

		assertTrue("Should not flash when thrall just summoned", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testThrallDeactivateViaVarbit()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(1);
		plugin.onChatMessage(chatMessage("You resurrect a lesser ghostly thrall."));

		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(0);
		plugin.onVarbitChanged(varbitChanged());

		assertEquals("Should flash when thrall despawns", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testThrallResummonStopsFlashing()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(1);
		plugin.onChatMessage(chatMessage("You resurrect a lesser ghostly thrall."));
		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(0);
		plugin.onVarbitChanged(varbitChanged());
		assertEquals(1, plugin.getFlashingEntries().size());

		lenient().when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(1);
		plugin.onChatMessage(chatMessage("You resurrect a greater skeletal thrall."));

		assertTrue("Re-summoning thrall should stop flashing", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testThrallIgnoresIrrelevantChatMessages()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		plugin.onChatMessage(chatMessage("You drink some of your super combat potion."));

		assertTrue("Irrelevant chat should not activate thrall tracker", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testThrallIgnoresNonGameMessages()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		ChatMessage publicChat = new ChatMessage(null, ChatMessageType.PUBLICCHAT, "", "You resurrect a lesser ghostly thrall.", "", 0);
		plugin.onChatMessage(publicChat);

		assertTrue("Non-game chat messages should be ignored", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Game State Reset Tests
	// =====================================================

	@Test
	public void testHoppingResetsTrackers()
	{
		setTrackedEntries(entry(TrackedEffect.PROTECT_FROM_MELEE, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());
		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());
		assertEquals(1, plugin.getFlashingEntries().size());

		GameStateChanged hop = new GameStateChanged();
		hop.setGameState(GameState.HOPPING);
		plugin.onGameStateChanged(hop);

		assertTrue("Hopping should clear all flashing", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testLoginScreenResetsTrackers()
	{
		setTrackedEntries(entry(TrackedEffect.THRALL_ACTIVE, BlinkMode.ON_EXPIRE, 0, 20));

		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(1);
		plugin.onChatMessage(chatMessage("You resurrect a lesser ghostly thrall."));
		when(client.getVarbitValue(Varbits.RESURRECT_THRALL)).thenReturn(0);
		plugin.onVarbitChanged(varbitChanged());
		assertEquals(1, plugin.getFlashingEntries().size());

		GameStateChanged logout = new GameStateChanged();
		logout.setGameState(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(logout);

		assertTrue("Login screen should clear all flashing", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// Timeout Tests
	// =====================================================

	@Test
	public void testThresholdFlashingTimesOut()
	{
		// Use 1-minute timeout for easy math
		// Threshold 9 = alert when boost drops to +9 or below
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 1));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Boosted to +12
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Drop to +9 — triggers threshold flash
		simulateBoost(Skill.ATTACK, 99, 108);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertEquals("Should be threshold flashing", 1, plugin.getFlashingEntries().size());

		// Simulate time passing — manipulate expiredAtMillis to be >1 min ago
		Map<TrackedEffect, EffectTracker> trackerMap = getTrackerMap();
		EffectTracker tracker = trackerMap.get(TrackedEffect.SUPER_COMBAT);
		backdateExpiry(tracker, System.currentTimeMillis() - 61_000L);

		// Re-drink above threshold — timeout should clear it
		simulateBoost(Skill.ATTACK, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Threshold flashing should time out after timeout period", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testExpiredFlashingTimesOut()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 1));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Drink and expire
		simulateBoost(Skill.ATTACK, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertEquals(1, plugin.getFlashingEntries().size());

		// Backdate expiredAtMillis to >1 min ago
		Map<TrackedEffect, EffectTracker> trackerMap = getTrackerMap();
		EffectTracker tracker = trackerMap.get(TrackedEffect.SUPER_COMBAT);
		backdateExpiry(tracker, System.currentTimeMillis() - 61_000L);

		assertTrue("Expired flashing should time out", plugin.getFlashingEntries().isEmpty());
	}

	// =====================================================
	// World Hop Suppress Tests
	// =====================================================

	@Test
	public void testWorldHopSuppressesPreExistingBoost()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Drink potion before hop
		simulateBoost(Skill.ATTACK, 99, 118);
		simulateBoost(Skill.STRENGTH, 99, 118);
		simulateBoost(Skill.DEFENCE, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Hop — boost persists, but tracker resets (lastKnownBoost = -1)
		GameStateChanged hop = new GameStateChanged();
		hop.setGameState(GameState.HOPPING);
		plugin.onGameStateChanged(hop);

		// Stats load in after hop — first observation sets baseline, no drink detected
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertTrue("Should not flash for pre-hop boost", plugin.getFlashingEntries().isEmpty());

		// Boost drains to 0 — no drink was detected so no flash
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Pre-hop boost expiry should NOT flash", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testNewPotionAfterHopFlashesNormally()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Hop with no active boost
		GameStateChanged hop = new GameStateChanged();
		hop.setGameState(GameState.HOPPING);
		plugin.onGameStateChanged(hop);

		// Stats load at base level after hop (sets baseline)
		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Drink a new potion after hop (boost increases: drink detected)
		simulateBoost(Skill.ATTACK, 99, 118);
		simulateBoost(Skill.STRENGTH, 99, 118);
		simulateBoost(Skill.DEFENCE, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Boost wears off
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("New potion after hop should flash normally", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testLoginWithBoostThenRedrinkFlashesCorrectly()
	{
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Login with existing +12 boost (first observation, baseline)
		simulateBoost(Skill.ATTACK, 99, 111);
		simulateBoost(Skill.STRENGTH, 99, 111);
		simulateBoost(Skill.DEFENCE, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Boost drains to 0 — no drink detected, should not flash
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertTrue("Pre-login boost should not flash", plugin.getFlashingEntries().isEmpty());

		// Now re-drink (boost increases: drink detected!)
		simulateBoost(Skill.ATTACK, 99, 118);
		simulateBoost(Skill.STRENGTH, 99, 118);
		simulateBoost(Skill.DEFENCE, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Boost wears off — should flash because drink was detected
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Re-drink after login should flash on expire", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testFirstLoginSuppressesPreExistingBoost()
	{
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Player logs in with existing boost — first observation is baseline
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20));

		simulateBoost(Skill.ATTACK, 99, 110);
		simulateBoost(Skill.STRENGTH, 99, 110);
		simulateBoost(Skill.DEFENCE, 99, 110);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		// Boost drains to 0 — no drink detected
		simulateBoost(Skill.ATTACK, 99, 99);
		simulateBoost(Skill.STRENGTH, 99, 99);
		simulateBoost(Skill.DEFENCE, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertTrue("Pre-login boost expiry should NOT flash", plugin.getFlashingEntries().isEmpty());
	}

	@Test
	public void testLoginWithBoostThresholdStillAlerts()
	{
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Player logs in with +12 boost, threshold = 9 (alert at +9 or below)
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));

		simulateBoost(Skill.ATTACK, 99, 111);
		simulateBoost(Skill.STRENGTH, 99, 111);
		simulateBoost(Skill.DEFENCE, 99, 111);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		assertTrue("Should not flash above threshold", plugin.getFlashingEntries().isEmpty());

		// Boost drains to +9 — at threshold, should alert
		simulateBoost(Skill.ATTACK, 99, 108);
		simulateBoost(Skill.STRENGTH, 99, 108);
		simulateBoost(Skill.DEFENCE, 99, 108);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Threshold should alert for pre-login boost", 1, plugin.getFlashingEntries().size());
	}

	@Test
	public void testLoginBelowThresholdAlertsImmediately()
	{
		when(client.getVarbitValue(anyInt())).thenReturn(0);

		// Player logs in at +8, threshold = 9 — already below, should alert immediately
		setTrackedEntries(entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 9, 20));

		simulateBoost(Skill.ATTACK, 99, 107);
		simulateBoost(Skill.STRENGTH, 99, 107);
		simulateBoost(Skill.DEFENCE, 99, 107);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		assertEquals("Should alert immediately when login boost is below threshold", 1, plugin.getFlashingEntries().size());
	}

	// =====================================================
	// Multiple Effects Tests
	// =====================================================

	@Test
	public void testMultipleEffectsFlashIndependently()
	{
		setTrackedEntries(
			entry(TrackedEffect.SUPER_COMBAT, BlinkMode.ON_EXPIRE, 0, 20),
			entry(TrackedEffect.PROTECT_FROM_MELEE, BlinkMode.ON_EXPIRE, 0, 20)
		);
		when(client.getVarbitValue(anyInt())).thenReturn(0);
		establishCombatBaseline();

		// Both active
		simulateBoost(Skill.ATTACK, 99, 118);
		plugin.onStatChanged(statChanged(Skill.ATTACK));
		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(true);
		plugin.onVarbitChanged(varbitChanged());
		assertTrue(plugin.getFlashingEntries().isEmpty());

		// Only potion expires
		simulateBoost(Skill.ATTACK, 99, 99);
		plugin.onStatChanged(statChanged(Skill.ATTACK));

		List<TrackedEffectEntry> flashing = plugin.getFlashingEntries();
		assertEquals("Only expired effect should flash", 1, flashing.size());
		assertEquals(TrackedEffect.SUPER_COMBAT.name(), flashing.get(0).getEffectName());

		// Prayer also deactivates
		when(client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)).thenReturn(false);
		plugin.onVarbitChanged(varbitChanged());

		assertEquals("Both should now be flashing", 2, plugin.getFlashingEntries().size());
	}
}
