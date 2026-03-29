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
import net.runelite.api.Prayer;
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

	MAGIC_IMBUE("Magic Imbue", EffectDetectionType.VARBIT,
		Varbits.MAGIC_IMBUE,
		ItemID.MAGIC_POTION4, true, BlinkMode.ON_EXPIRE, "Buffs"),

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

	THRALL_ACTIVE("Thrall Active", EffectDetectionType.CHAT_MESSAGE,
		"resurrect a",
		SpriteID.SPELL_RESURRECT_LESSER_GHOST, false, BlinkMode.ON_EXPIRE, "Buffs", -1, Varbits.RESURRECT_THRALL),

	THRALL_COOLDOWN("Thrall Cooldown", EffectDetectionType.VARBIT,
		Varbits.RESURRECT_THRALL_COOLDOWN,
		SpriteID.SPELL_RESURRECT_LESSER_GHOST, false, BlinkMode.WHILE_ACTIVE, "Buffs"),

	// ===========================
	// Standard Prayers
	// ===========================
	THICK_SKIN("Thick Skin", EffectDetectionType.PRAYER,
		Prayer.THICK_SKIN,
		SpriteID.PRAYER_THICK_SKIN, false, BlinkMode.ON_EXPIRE, "Prayers"),

	BURST_OF_STRENGTH("Burst of Strength", EffectDetectionType.PRAYER,
		Prayer.BURST_OF_STRENGTH,
		SpriteID.PRAYER_BURST_OF_STRENGTH, false, BlinkMode.ON_EXPIRE, "Prayers"),

	CLARITY_OF_THOUGHT("Clarity of Thought", EffectDetectionType.PRAYER,
		Prayer.CLARITY_OF_THOUGHT,
		SpriteID.PRAYER_CLARITY_OF_THOUGHT, false, BlinkMode.ON_EXPIRE, "Prayers"),

	SHARP_EYE("Sharp Eye", EffectDetectionType.PRAYER,
		Prayer.SHARP_EYE,
		SpriteID.PRAYER_SHARP_EYE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	MYSTIC_WILL("Mystic Will", EffectDetectionType.PRAYER,
		Prayer.MYSTIC_WILL,
		SpriteID.PRAYER_MYSTIC_WILL, false, BlinkMode.ON_EXPIRE, "Prayers"),

	ROCK_SKIN("Rock Skin", EffectDetectionType.PRAYER,
		Prayer.ROCK_SKIN,
		SpriteID.PRAYER_ROCK_SKIN, false, BlinkMode.ON_EXPIRE, "Prayers"),

	SUPERHUMAN_STRENGTH("Superhuman Strength", EffectDetectionType.PRAYER,
		Prayer.SUPERHUMAN_STRENGTH,
		SpriteID.PRAYER_SUPERHUMAN_STRENGTH, false, BlinkMode.ON_EXPIRE, "Prayers"),

	IMPROVED_REFLEXES("Improved Reflexes", EffectDetectionType.PRAYER,
		Prayer.IMPROVED_REFLEXES,
		SpriteID.PRAYER_IMPROVED_REFLEXES, false, BlinkMode.ON_EXPIRE, "Prayers"),

	RAPID_RESTORE("Rapid Restore", EffectDetectionType.PRAYER,
		Prayer.RAPID_RESTORE,
		SpriteID.PRAYER_RAPID_RESTORE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	RAPID_HEAL("Rapid Heal", EffectDetectionType.PRAYER,
		Prayer.RAPID_HEAL,
		SpriteID.PRAYER_RAPID_HEAL, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PROTECT_ITEM("Protect Item", EffectDetectionType.PRAYER,
		Prayer.PROTECT_ITEM,
		SpriteID.PRAYER_PROTECT_ITEM, false, BlinkMode.ON_EXPIRE, "Prayers"),

	HAWK_EYE("Hawk Eye", EffectDetectionType.PRAYER,
		Prayer.HAWK_EYE,
		SpriteID.PRAYER_HAWK_EYE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	MYSTIC_LORE("Mystic Lore", EffectDetectionType.PRAYER,
		Prayer.MYSTIC_LORE,
		SpriteID.PRAYER_MYSTIC_LORE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	STEEL_SKIN("Steel Skin", EffectDetectionType.PRAYER,
		Prayer.STEEL_SKIN,
		SpriteID.PRAYER_STEEL_SKIN, false, BlinkMode.ON_EXPIRE, "Prayers"),

	ULTIMATE_STRENGTH("Ultimate Strength", EffectDetectionType.PRAYER,
		Prayer.ULTIMATE_STRENGTH,
		SpriteID.PRAYER_ULTIMATE_STRENGTH, false, BlinkMode.ON_EXPIRE, "Prayers"),

	INCREDIBLE_REFLEXES("Incredible Reflexes", EffectDetectionType.PRAYER,
		Prayer.INCREDIBLE_REFLEXES,
		SpriteID.PRAYER_INCREDIBLE_REFLEXES, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PROTECT_FROM_MAGIC("Protect from Magic", EffectDetectionType.PRAYER,
		Prayer.PROTECT_FROM_MAGIC,
		SpriteID.PRAYER_PROTECT_FROM_MAGIC, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PROTECT_FROM_MISSILES("Protect from Missiles", EffectDetectionType.PRAYER,
		Prayer.PROTECT_FROM_MISSILES,
		SpriteID.PRAYER_PROTECT_FROM_MISSILES, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PROTECT_FROM_MELEE("Protect from Melee", EffectDetectionType.PRAYER,
		Prayer.PROTECT_FROM_MELEE,
		SpriteID.PRAYER_PROTECT_FROM_MELEE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	EAGLE_EYE("Eagle Eye", EffectDetectionType.PRAYER,
		Prayer.EAGLE_EYE,
		SpriteID.PRAYER_EAGLE_EYE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	MYSTIC_MIGHT("Mystic Might", EffectDetectionType.PRAYER,
		Prayer.MYSTIC_MIGHT,
		SpriteID.PRAYER_MYSTIC_MIGHT, false, BlinkMode.ON_EXPIRE, "Prayers"),

	RETRIBUTION("Retribution", EffectDetectionType.PRAYER,
		Prayer.RETRIBUTION,
		SpriteID.PRAYER_RETRIBUTION, false, BlinkMode.ON_EXPIRE, "Prayers"),

	REDEMPTION("Redemption", EffectDetectionType.PRAYER,
		Prayer.REDEMPTION,
		SpriteID.PRAYER_REDEMPTION, false, BlinkMode.ON_EXPIRE, "Prayers"),

	SMITE("Smite", EffectDetectionType.PRAYER,
		Prayer.SMITE,
		SpriteID.PRAYER_SMITE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	CHIVALRY("Chivalry", EffectDetectionType.PRAYER,
		Prayer.CHIVALRY,
		SpriteID.PRAYER_CHIVALRY, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PIETY("Piety", EffectDetectionType.PRAYER,
		Prayer.PIETY,
		SpriteID.PRAYER_PIETY, false, BlinkMode.ON_EXPIRE, "Prayers"),

	PRESERVE("Preserve", EffectDetectionType.PRAYER,
		Prayer.PRESERVE,
		SpriteID.PRAYER_PRESERVE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	RIGOUR("Rigour", EffectDetectionType.PRAYER,
		Prayer.RIGOUR,
		SpriteID.PRAYER_RIGOUR, false, BlinkMode.ON_EXPIRE, "Prayers"),

	AUGURY("Augury", EffectDetectionType.PRAYER,
		Prayer.AUGURY,
		SpriteID.PRAYER_AUGURY, false, BlinkMode.ON_EXPIRE, "Prayers"),

	DEADEYE("Deadeye", EffectDetectionType.PRAYER,
		Prayer.DEADEYE,
		SpriteID.PRAYER_DEADEYE, false, BlinkMode.ON_EXPIRE, "Prayers"),

	MYSTIC_VIGOUR("Mystic Vigour", EffectDetectionType.PRAYER,
		Prayer.MYSTIC_VIGOUR,
		SpriteID.PRAYER_MYSTIC_VIGOUR, false, BlinkMode.ON_EXPIRE, "Prayers"),

	// ===========================
	// Ruinous Powers Prayers
	// ===========================
	RP_REJUVENATION("Rejuvenation", EffectDetectionType.PRAYER,
		Prayer.RP_REJUVENATION,
		SpriteID.PRAYER_RP_REJUVENATION, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_ANCIENT_STRENGTH("Ancient Strength", EffectDetectionType.PRAYER,
		Prayer.RP_ANCIENT_STRENGTH,
		SpriteID.PRAYER_RP_ANCIENT_STRENGTH, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_ANCIENT_SIGHT("Ancient Sight", EffectDetectionType.PRAYER,
		Prayer.RP_ANCIENT_SIGHT,
		SpriteID.PRAYER_RP_ANCIENT_SIGHT, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_ANCIENT_WILL("Ancient Will", EffectDetectionType.PRAYER,
		Prayer.RP_ANCIENT_WILL,
		SpriteID.PRAYER_RP_ANCIENT_WILL, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_PROTECT_ITEM("Ruinous Protect Item", EffectDetectionType.PRAYER,
		Prayer.RP_PROTECT_ITEM,
		SpriteID.PRAYER_RP_PROTECT_ITEM, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_DAMPEN_MAGIC("Dampen Magic", EffectDetectionType.PRAYER,
		Prayer.RP_DAMPEN_MAGIC,
		SpriteID.PRAYER_RP_DAMPEN_MAGIC, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_DAMPEN_RANGED("Dampen Ranged", EffectDetectionType.PRAYER,
		Prayer.RP_DAMPEN_RANGED,
		SpriteID.PRAYER_RP_DAMPEN_RANGED, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_DAMPEN_MELEE("Dampen Melee", EffectDetectionType.PRAYER,
		Prayer.RP_DAMPEN_MELEE,
		SpriteID.PRAYER_RP_DAMPEN_MELEE, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_TRINITAS("Trinitas", EffectDetectionType.PRAYER,
		Prayer.RP_TRINITAS,
		SpriteID.PRAYER_RP_TRINITAS, false, BlinkMode.ON_EXPIRE, "Ruinous Powers"),

	RP_BERSERKER("Berserker", EffectDetectionType.PRAYER,
		Prayer.RP_BERSERKER,
		SpriteID.PRAYER_RP_BERSERKER, false, BlinkMode.ON_EXPIRE, "Ruinous Powers");

	private final String displayName;
	private final EffectDetectionType detectionType;
	private final Object detectionKey; // Skill[], int (varbit ID), or Prayer
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

	public Prayer getPrayer()
	{
		if (detectionType != EffectDetectionType.PRAYER)
		{
			throw new IllegalStateException("getPrayer() called on " + detectionType + " effect: " + name());
		}
		return (Prayer) detectionKey;
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
