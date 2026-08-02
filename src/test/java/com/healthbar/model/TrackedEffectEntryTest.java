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
package com.healthbar.model;

import com.healthbar.timing.TimedChatEffectRegistry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import net.runelite.api.ItemID;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;
import org.junit.Test;

public class TrackedEffectEntryTest
{
	@Test
	public void testGetEffectResolvesValidName()
	{
		TrackedEffectEntry entry = new TrackedEffectEntry(
			TrackedEffect.SUPER_COMBAT.name(), BlinkMode.ON_EXPIRE, 0, 20);
		assertNotNull(entry.getEffect());
		assertEquals(TrackedEffect.SUPER_COMBAT, entry.getEffect());
	}

	@Test
	public void testGetEffectReturnsNullForInvalidName()
	{
		TrackedEffectEntry entry = new TrackedEffectEntry(
			"NONEXISTENT_EFFECT", BlinkMode.ON_EXPIRE, 0, 20);
		assertNull(entry.getEffect());
	}

	@Test
	public void testGetEffectCachesResult()
	{
		TrackedEffectEntry entry = new TrackedEffectEntry(
			TrackedEffect.STAMINA.name(), BlinkMode.ON_EXPIRE, 0, 20);
		TrackedEffect first = entry.getEffect();
		TrackedEffect second = entry.getEffect();
		assertSame("Should return same cached instance", first, second);
	}

	@Test
	public void testGetEffectCachesNullForInvalidName()
	{
		TrackedEffectEntry entry = new TrackedEffectEntry(
			"INVALID", BlinkMode.ON_EXPIRE, 0, 20);
		assertNull(entry.getEffect());
		// Second call should also return null without throwing
		assertNull(entry.getEffect());
	}

	@Test
	public void testDefaultConstructor()
	{
		TrackedEffectEntry entry = new TrackedEffectEntry();
		assertNull(entry.getEffectName());
		assertNull(entry.getBlinkMode());
		assertEquals(0, entry.getDropThreshold());
		assertEquals(0, entry.getTimeoutMinutes());
	}

	@Test
	public void testNewEffectsUseExpectedDetectionAndSprites()
	{
		assertEquals(Varbits.IMBUED_HEART_COOLDOWN, TrackedEffect.SATURATED_HEART.getVarbitId());
		assertEquals(ItemID.SATURATED_HEART, TrackedEffect.SATURATED_HEART.getDefaultSpriteId());
		assertEquals(SpriteID.SPELL_MARK_OF_DARKNESS, TrackedEffect.MARK_OF_DARKNESS.getDefaultSpriteId());
		assertEquals(SpriteID.SPELL_WARD_OF_ARCEUUS, TrackedEffect.WARD_OF_ARCEUUS.getDefaultSpriteId());
		assertNotNull(TimedChatEffectRegistry.get(TrackedEffect.MARK_OF_DARKNESS));
		assertNotNull(TimedChatEffectRegistry.get(TrackedEffect.WARD_OF_ARCEUUS));
	}
}
