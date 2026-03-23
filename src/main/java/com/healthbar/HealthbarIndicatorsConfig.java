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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(HealthbarIndicatorsConfig.CONFIG_GROUP)
public interface HealthbarIndicatorsConfig extends Config
{
	String CONFIG_GROUP = "healthbarIndicators";
	String TRACKED_EFFECTS_KEY = "trackedEffects";
	String SETUPS_KEY = "setups";
	String ACTIVE_SETUP_KEY = "activeSetup";

	@ConfigItem(
		keyName = "flashRate",
		name = "Flash Rate (ms)",
		description = "How fast icons blink on/off in milliseconds",
		position = 0
	)
	default int flashRate()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "iconSize",
		name = "Icon Size",
		description = "Size of indicator icons in pixels",
		position = 1
	)
	default int iconSize()
	{
		return 20;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "zoomDamping",
		name = "Zoom Sensitivity %",
		description = "How much icons scale with zoom (0 = no scaling, 100 = full scaling)",
		position = 2
	)
	default int zoomDamping()
	{
		return 50;
	}

	@ConfigItem(
		keyName = "invertZoomScaling",
		name = "Invert Zoom Scaling",
		description = "When enabled, icons grow when zooming out (like the healthbar). When disabled, icons shrink when zooming out.",
		position = 3
	)
	default boolean invertZoomScaling()
	{
		return true;
	}

	@Range(min = -500, max = 500)
	@ConfigItem(
		keyName = "offsetX",
		name = "X Offset",
		description = "Horizontal offset in pixels (negative = left, positive = right)",
		position = 4
	)
	default int offsetX()
	{
		return 0;
	}

	@Range(min = -500, max = 500)
	@ConfigItem(
		keyName = "offsetY",
		name = "Y Offset",
		description = "Vertical offset in pixels (negative = down, positive = up)",
		position = 5
	)
	default int offsetY()
	{
		return 0;
	}

	@ConfigItem(
		keyName = TRACKED_EFFECTS_KEY,
		name = "",
		description = "",
		hidden = true
	)
	default String trackedEffects()
	{
		return "[]";
	}

	@ConfigItem(
		keyName = TRACKED_EFFECTS_KEY,
		name = "",
		description = ""
	)
	void setTrackedEffects(String json);
}
