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
package com.healthbar.timing;

import com.healthbar.model.TrackedEffect;
import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.client.util.RSTimeUnit;

public final class TimedChatEffectRegistry
{
	private static final Map<TrackedEffect, TimedChatEffect> EFFECTS;

	static
	{
		Map<TrackedEffect, TimedChatEffect> effects = new EnumMap<>(TrackedEffect.class);
		register(effects, new MarkOfDarknessEffect());
		register(effects, new WardOfArceuusEffect());
		EFFECTS = Collections.unmodifiableMap(effects);
	}

	private TimedChatEffectRegistry()
	{
	}

	public static TimedChatEffect get(TrackedEffect effect)
	{
		return EFFECTS.get(effect);
	}

	private static void register(Map<TrackedEffect, TimedChatEffect> effects, TimedChatEffect effect)
	{
		effects.put(effect.getEffect(), effect);
	}

	public interface TimedChatEffect
	{
		TrackedEffect getEffect();

		boolean matches(String normalizedMessage);

		Duration calculateDuration(Client client);
	}

	private static final class MarkOfDarknessEffect implements TimedChatEffect
	{
		private static final String ACTIVATION_MESSAGE = "mark of darkness upon yourself";

		@Override
		public TrackedEffect getEffect()
		{
			return TrackedEffect.MARK_OF_DARKNESS;
		}

		@Override
		public boolean matches(String normalizedMessage)
		{
			return normalizedMessage.contains(ACTIVATION_MESSAGE);
		}

		@Override
		public Duration calculateDuration(Client client)
		{
			Duration duration = Duration.of(
				(long) client.getRealSkillLevel(Skill.MAGIC) * 3,
				RSTimeUnit.GAME_TICKS);
			if (isWeaponEquipped(client, ItemID.PURGING_STAFF))
			{
				duration = duration.multipliedBy(5);
			}
			return duration;
		}
	}

	private static final class WardOfArceuusEffect implements TimedChatEffect
	{
		private static final String ACTIVATION_MESSAGE =
			"defence against arceuus magic has been strengthened";

		@Override
		public TrackedEffect getEffect()
		{
			return TrackedEffect.WARD_OF_ARCEUUS;
		}

		@Override
		public boolean matches(String normalizedMessage)
		{
			return normalizedMessage.contains(ACTIVATION_MESSAGE);
		}

		@Override
		public Duration calculateDuration(Client client)
		{
			return Duration.of(client.getRealSkillLevel(Skill.MAGIC), RSTimeUnit.GAME_TICKS);
		}
	}

	private static boolean isWeaponEquipped(Client client, int itemId)
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return false;
		}
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		return weapon != null && weapon.getId() == itemId;
	}
}
