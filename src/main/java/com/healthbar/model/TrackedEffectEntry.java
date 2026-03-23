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

import lombok.Data;

/**
 * Represents a user-configured tracked effect entry.
 * Serialized to/from JSON for config storage.
 */
@Data
public class TrackedEffectEntry
{
	private String effectName; // TrackedEffect enum name
	private BlinkMode blinkMode;
	private int dropThreshold; // For skill boost effects: alert when boost drops to this level or below (0 = only alert when fully gone)
	private int timeoutMinutes; // Per-effect activity timeout in minutes (0 = never timeout)

	public TrackedEffectEntry()
	{
	}

	public TrackedEffectEntry(String effectName, BlinkMode blinkMode, int dropThreshold, int timeoutMinutes)
	{
		this.effectName = effectName;
		this.blinkMode = blinkMode;
		this.dropThreshold = dropThreshold;
		this.timeoutMinutes = timeoutMinutes;
	}

	public TrackedEffect getEffect()
	{
		try
		{
			return TrackedEffect.valueOf(effectName);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}
}
