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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class IndicatorSetupTest
{
	private static final Gson GSON = new Gson();

	@Test
	public void testDefaultConstructor()
	{
		IndicatorSetup setup = new IndicatorSetup();
		assertEquals("", setup.getName());
		assertTrue(setup.getEntries().isEmpty());
	}

	@Test
	public void testNamedConstructor()
	{
		List<TrackedEffectEntry> entries = Arrays.asList(
			new TrackedEffectEntry(TrackedEffect.SUPER_COMBAT.name(), BlinkMode.ON_EXPIRE, 0, 20),
			new TrackedEffectEntry(TrackedEffect.STAMINA.name(), BlinkMode.ON_EXPIRE, 0, 20)
		);
		IndicatorSetup setup = new IndicatorSetup("Bossing", entries);

		assertEquals("Bossing", setup.getName());
		assertEquals(2, setup.getEntries().size());
	}

	@Test
	public void testConstructorDefensiveCopy()
	{
		List<TrackedEffectEntry> entries = new ArrayList<>();
		entries.add(new TrackedEffectEntry(TrackedEffect.STAMINA.name(), BlinkMode.ON_EXPIRE, 0, 20));
		IndicatorSetup setup = new IndicatorSetup("Test", entries);

		// Modifying original list should not affect setup
		entries.add(new TrackedEffectEntry(TrackedEffect.ANTIFIRE.name(), BlinkMode.ON_EXPIRE, 0, 20));
		assertEquals(1, setup.getEntries().size());
	}

	@Test
	public void testJsonRoundTrip()
	{
		List<TrackedEffectEntry> entries = Arrays.asList(
			new TrackedEffectEntry(TrackedEffect.SUPER_COMBAT.name(), BlinkMode.ON_EXPIRE, 9, 20),
			new TrackedEffectEntry(TrackedEffect.PROTECT_FROM_MELEE.name(), BlinkMode.ON_EXPIRE, 0, 0)
		);
		IndicatorSetup original = new IndicatorSetup("PvM", entries);

		String json = GSON.toJson(original);
		IndicatorSetup deserialized = GSON.fromJson(json, IndicatorSetup.class);

		assertEquals(original.getName(), deserialized.getName());
		assertEquals(original.getEntries().size(), deserialized.getEntries().size());
		assertEquals(original.getEntries().get(0).getEffectName(), deserialized.getEntries().get(0).getEffectName());
		assertEquals(original.getEntries().get(0).getDropThreshold(), deserialized.getEntries().get(0).getDropThreshold());
	}

	@Test
	public void testJsonRoundTripList()
	{
		List<IndicatorSetup> setups = Arrays.asList(
			new IndicatorSetup("Bossing", Arrays.asList(
				new TrackedEffectEntry(TrackedEffect.SUPER_COMBAT.name(), BlinkMode.ON_EXPIRE, 0, 20)
			)),
			new IndicatorSetup("Slayer", Arrays.asList(
				new TrackedEffectEntry(TrackedEffect.STAMINA.name(), BlinkMode.ON_EXPIRE, 0, 20)
			))
		);

		String json = GSON.toJson(setups);
		Type listType = new TypeToken<List<IndicatorSetup>>(){}.getType();
		List<IndicatorSetup> deserialized = GSON.fromJson(json, listType);

		assertEquals(2, deserialized.size());
		assertEquals("Bossing", deserialized.get(0).getName());
		assertEquals("Slayer", deserialized.get(1).getName());
	}

	@Test
	public void testEntriesAreMutable()
	{
		IndicatorSetup setup = new IndicatorSetup("Test", new ArrayList<>());
		setup.getEntries().add(new TrackedEffectEntry(TrackedEffect.STAMINA.name(), BlinkMode.ON_EXPIRE, 0, 20));
		assertEquals(1, setup.getEntries().size());

		setup.getEntries().clear();
		assertTrue(setup.getEntries().isEmpty());
	}
}
