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

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.Varbits;

@Getter
public enum TrackedEffect
{
	// ===========================
	// Potions (SKILL_BOOST)
	// ===========================
	SUPER_COMBAT("Super Combat", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE},
		ItemID.SUPER_COMBAT_POTION4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_SUPER_COMBAT),

	SUPER_ATTACK("Super Attack", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.ATTACK},
		ItemID.SUPER_ATTACK4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_SUPER_ATTACK),

	SUPER_STRENGTH("Super Strength", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.STRENGTH},
		ItemID.SUPER_STRENGTH4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_SUPER_STRENGTH),

	SUPER_DEFENCE("Super Defence", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.DEFENCE},
		ItemID.SUPER_DEFENCE4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_SUPER_DEFENCE),

	RANGING_POTION("Ranging Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.RANGED},
		ItemID.RANGING_POTION4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_RANGING),

	MAGIC_POTION("Magic Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.MAGIC},
		ItemID.MAGIC_POTION4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_MAGIC),

	ATTACK_POTION("Attack Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.ATTACK},
		ItemID.ATTACK_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	STRENGTH_POTION("Strength Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.STRENGTH},
		ItemID.STRENGTH_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DEFENCE_POTION("Defence Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.DEFENCE},
		ItemID.DEFENCE_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	RUBY_HARVEST("Ruby Harvest (+ Mix)", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.ATTACK},
		ItemID.RUBY_HARVEST, true, BlinkMode.ON_EXPIRE, "Potions"),

	SAPPHIRE_GLACIALIS("Sapphire Glacialis (+ Mix)", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.DEFENCE},
		ItemID.SAPPHIRE_GLACIALIS, true, BlinkMode.ON_EXPIRE, "Potions"),

	BLACK_WARLOCK("Black Warlock (+ Mix)", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.STRENGTH},
		ItemID.BLACK_WARLOCK, true, BlinkMode.ON_EXPIRE, "Potions"),

	COMBAT_POTION("Combat Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.ATTACK, Skill.STRENGTH},
		ItemID.COMBAT_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	BASTION_POTION("Bastion Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.RANGED, Skill.DEFENCE},
		ItemID.BASTION_POTION4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_BASTION),

	BATTLEMAGE_POTION("Battlemage Potion", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.MAGIC, Skill.DEFENCE},
		ItemID.BATTLEMAGE_POTION4, true, BlinkMode.ON_EXPIRE, "Potions", Varbits.DIVINE_BATTLEMAGE),

	SARADOMIN_BREW("Saradomin Brew", EffectDetectionType.SKILL_BOOST,
		new Skill[]{Skill.HITPOINTS, Skill.DEFENCE},
		ItemID.SARADOMIN_BREW4, true, BlinkMode.ON_EXPIRE, "Potions"),

	// ===========================
	// Buffs (VARBIT)
	// ===========================
	STAMINA("Stamina", EffectDetectionType.VARBIT,
		Varbits.RUN_SLOWED_DEPLETION_ACTIVE,
		ItemID.STAMINA_POTION4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	ANTIFIRE("Antifire", EffectDetectionType.VARBIT,
		Varbits.ANTIFIRE,
		ItemID.ANTIFIRE_POTION4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	SUPER_ANTIFIRE("Super Antifire", EffectDetectionType.VARBIT,
		Varbits.SUPER_ANTIFIRE,
		ItemID.SUPER_ANTIFIRE_POTION4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	ANTIPOISON("Antipoison", EffectDetectionType.POISON_IMMUNITY,
		0,
		ItemID.ANTIPOISON4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	ANTIVENOM("Antivenom", EffectDetectionType.VENOM_IMMUNITY,
		0,
		ItemID.ANTIVENOM4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	DIVINE_SUPER_COMBAT("Divine Super Combat", EffectDetectionType.VARBIT,
		Varbits.DIVINE_SUPER_COMBAT,
		ItemID.DIVINE_SUPER_COMBAT_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_SUPER_ATTACK("Divine Super Attack", EffectDetectionType.VARBIT,
		Varbits.DIVINE_SUPER_ATTACK,
		ItemID.DIVINE_SUPER_ATTACK_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_SUPER_STRENGTH("Divine Super Strength", EffectDetectionType.VARBIT,
		Varbits.DIVINE_SUPER_STRENGTH,
		ItemID.DIVINE_SUPER_STRENGTH_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_SUPER_DEFENCE("Divine Super Defence", EffectDetectionType.VARBIT,
		Varbits.DIVINE_SUPER_DEFENCE,
		ItemID.DIVINE_SUPER_DEFENCE_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_RANGING("Divine Ranging", EffectDetectionType.VARBIT,
		Varbits.DIVINE_RANGING,
		ItemID.DIVINE_RANGING_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_MAGIC("Divine Magic", EffectDetectionType.VARBIT,
		Varbits.DIVINE_MAGIC,
		ItemID.DIVINE_MAGIC_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_BASTION("Divine Bastion", EffectDetectionType.VARBIT,
		Varbits.DIVINE_BASTION,
		ItemID.DIVINE_BASTION_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	DIVINE_BATTLEMAGE("Divine Battlemage", EffectDetectionType.VARBIT,
		Varbits.DIVINE_BATTLEMAGE,
		ItemID.DIVINE_BATTLEMAGE_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	IMBUED_HEART("Imbued Heart", EffectDetectionType.VARBIT,
		Varbits.IMBUED_HEART_COOLDOWN,
		ItemID.IMBUED_HEART, true, BlinkMode.ON_EXPIRE, "Buffs"),

	SATURATED_HEART("Saturated Heart", EffectDetectionType.VARBIT,
		Varbits.IMBUED_HEART_COOLDOWN,
		ItemID.SATURATED_HEART, true, BlinkMode.ON_EXPIRE, "Buffs"),

	MAGIC_IMBUE("Magic Imbue", EffectDetectionType.VARBIT,
		Varbits.MAGIC_IMBUE,
		SpriteID.SPELL_MAGIC_IMBUE, false, BlinkMode.ON_EXPIRE, "Buffs"),

	VENGEANCE("Vengeance", EffectDetectionType.VARBIT,
		Varbits.VENGEANCE_ACTIVE,
		SpriteID.SPELL_VENGEANCE, false, BlinkMode.ON_EXPIRE, "Buffs"),

	MOONLIGHT_POTION("Moonlight Potion", EffectDetectionType.VARBIT,
		Varbits.MOONLIGHT_POTION,
		ItemID.MOONLIGHT_POTION4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	NMZ_ABSORPTION("NMZ Absorption", EffectDetectionType.VARBIT,
		Varbits.NMZ_ABSORPTION,
		ItemID.ABSORPTION_4, true, BlinkMode.ON_EXPIRE, "Buffs"),

	PRAYER_REGENERATION("Prayer Regeneration", EffectDetectionType.VARBIT,
		Varbits.BUFF_PRAYER_REGENERATION,
		ItemID.PRAYER_REGENERATION_POTION4, true, BlinkMode.ON_EXPIRE, "Potions"),

	COX_OVERLOAD("CoX Overload", EffectDetectionType.VARBIT,
		Varbits.COX_OVERLOAD_REFRESHES_REMAINING,
		ItemID.OVERLOAD_4, true, BlinkMode.ON_EXPIRE, "Potions"),

	RING_OF_ENDURANCE("Ring of Endurance", EffectDetectionType.VARBIT,
		Varbits.RING_OF_ENDURANCE_EFFECT,
		ItemID.RING_OF_ENDURANCE, true, BlinkMode.ON_EXPIRE, "Buffs"),

	// ===========================
	// Debuffs (VARBIT) - blink while active
	// ===========================
	TELEBLOCK("Teleblock", EffectDetectionType.VARBIT,
		Varbits.TELEBLOCK,
		SpriteID.SPELL_TELE_BLOCK, false, BlinkMode.WHILE_ACTIVE, "Debuffs"),

	IN_WILDERNESS("In Wilderness", EffectDetectionType.VARBIT,
		Varbits.IN_WILDERNESS,
		SpriteID.SPELL_TELEPORT_TO_BOUNTY_TARGET, false, BlinkMode.WHILE_ACTIVE, "Debuffs"),

	SHADOW_VEIL("Shadow Veil", EffectDetectionType.VARBIT,
		Varbits.SHADOW_VEIL,
		SpriteID.SPELL_SHADOW_VEIL, false, BlinkMode.ON_EXPIRE, "Buffs"),

	DEATH_CHARGE("Death Charge", EffectDetectionType.VARBIT,
		Varbits.DEATH_CHARGE,
		SpriteID.SPELL_DEATH_CHARGE, false, BlinkMode.ON_EXPIRE, "Buffs"),

	MARK_OF_DARKNESS("Mark of Darkness", EffectDetectionType.TIMED_CHAT_MESSAGE,
		null,
		SpriteID.SPELL_MARK_OF_DARKNESS, false, BlinkMode.ON_EXPIRE, "Buffs"),

	WARD_OF_ARCEUUS("Ward of Arceuus", EffectDetectionType.TIMED_CHAT_MESSAGE,
		null,
		SpriteID.SPELL_WARD_OF_ARCEUUS, false, BlinkMode.ON_EXPIRE, "Buffs"),

	THRALL_ACTIVE("Thrall Active", EffectDetectionType.CHAT_MESSAGE,
		"resurrect a",
		SpriteID.SPELL_RESURRECT_LESSER_GHOST, false, BlinkMode.ON_EXPIRE, "Buffs", -1, Varbits.RESURRECT_THRALL),

	THRALL_COOLDOWN("Thrall Cooldown", EffectDetectionType.VARBIT,
		Varbits.RESURRECT_THRALL_COOLDOWN,
		SpriteID.SPELL_RESURRECT_LESSER_GHOST, false, BlinkMode.WHILE_ACTIVE, "Buffs");

	private final String displayName;
	private final EffectDetectionType detectionType;
	private final Object detectionKey; // Skill[], int (varbit ID), or chat pattern
	private final int defaultSpriteId; // ItemID or SpriteID constant
	private final boolean isItemSprite; // true = ItemManager, false = SpriteManager
	private final BlinkMode defaultBlinkMode;
	private final String category;
	private final int divineVarbitId; // Varbit ID of the divine counterpart (-1 = none)
	private final int deactivationVarbitId; // Varbit to check for chat-based effect deactivation (-1 = none)

	TrackedEffect(String displayName, EffectDetectionType detectionType, Object detectionKey,
		int defaultSpriteId, boolean isItemSprite, BlinkMode defaultBlinkMode, String category)
	{
		this(displayName, detectionType, detectionKey, defaultSpriteId, isItemSprite, defaultBlinkMode, category, -1, -1);
	}

	TrackedEffect(String displayName, EffectDetectionType detectionType, Object detectionKey,
		int defaultSpriteId, boolean isItemSprite, BlinkMode defaultBlinkMode, String category, int divineVarbitId)
	{
		this(displayName, detectionType, detectionKey, defaultSpriteId, isItemSprite, defaultBlinkMode, category, divineVarbitId, -1);
	}

	TrackedEffect(String displayName, EffectDetectionType detectionType, Object detectionKey,
		int defaultSpriteId, boolean isItemSprite, BlinkMode defaultBlinkMode, String category,
		int divineVarbitId, int deactivationVarbitId)
	{
		this.displayName = displayName;
		this.detectionType = detectionType;
		this.detectionKey = detectionKey;
		this.defaultSpriteId = defaultSpriteId;
		this.isItemSprite = isItemSprite;
		this.defaultBlinkMode = defaultBlinkMode;
		this.category = category;
		this.divineVarbitId = divineVarbitId;
		this.deactivationVarbitId = deactivationVarbitId;
	}

	public Skill[] getSkills()
	{
		if (detectionType != EffectDetectionType.SKILL_BOOST)
		{
			throw new IllegalStateException("getSkills() called on " + detectionType + " effect: " + name());
		}
		return (Skill[]) detectionKey;
	}

	public int getVarbitId()
	{
		if (detectionType != EffectDetectionType.VARBIT)
		{
			throw new IllegalStateException("getVarbitId() called on " + detectionType + " effect: " + name());
		}
		return (int) detectionKey;
	}


	public String getChatPattern()
	{
		if (detectionType != EffectDetectionType.CHAT_MESSAGE)
		{
			throw new IllegalStateException("getChatPattern() called on " + detectionType + " effect: " + name());
		}
		return (String) detectionKey;
	}

	public boolean supportsThreshold()
	{
		return detectionType == EffectDetectionType.SKILL_BOOST;
	}

	public boolean tracksSkill(Skill skill)
	{
		if (detectionType != EffectDetectionType.SKILL_BOOST)
		{
			return false;
		}
		for (Skill s : (Skill[]) detectionKey)
		{
			if (s == skill)
			{
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString()
	{
		return displayName;
	}
}
