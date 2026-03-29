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

@Getter
public class EffectTracker
{
	private static final long MILLIS_PER_MINUTE = 60_000L;
	private static final int UNINITIALIZED_BOOST = -1;

	private EffectState state = EffectState.INACTIVE;
	private long expiredAtMillis = 0;
	private long lastActiveAtMillis = 0;
	private boolean divineWasActive = false;
	private int lastKnownBoost = UNINITIALIZED_BOOST;
	private boolean drinkDetected = false;

	/**
	 * Records that a divine potion variant was seen active.
	 * Once set, it stays true until cleared by a state transition.
	 */
	public void updateDivine(boolean divineActive)
	{
		if (divineActive)
		{
			this.divineWasActive = true;
		}
	}

	/**
	 * Tracks boost changes to detect drinks.
	 * First observation sets baseline without marking a drink.
	 * A boost increase from a known level marks a drink as detected.
	 */
	public void updateBoost(int currentBoost)
	{
		if (lastKnownBoost == UNINITIALIZED_BOOST)
		{
			if (currentBoost > 0)
			{
				drinkDetected = true;
			}
		}
		else if (currentBoost > lastKnownBoost)
		{
			drinkDetected = true;
		}
		lastKnownBoost = currentBoost;
	}

	/**
	 * Records the timestamp of the last time this effect was seen active.
	 */
	public void markActive(long now)
	{
		lastActiveAtMillis = now;
	}

	/**
	 * Directly transitions to ACTIVE state.
	 * Used for chat-message-based effects that activate immediately.
	 */
	public void activate(long now)
	{
		state = EffectState.ACTIVE;
		lastActiveAtMillis = now;
	}

	/**
	 * Attempts to transition from ACTIVE to EXPIRED_FLASHING.
	 * Returns true if the transition occurred, false if not in ACTIVE state.
	 */
	public boolean tryExpire(long now)
	{
		if (state == EffectState.ACTIVE)
		{
			state = EffectState.EXPIRED_FLASHING;
			expiredAtMillis = now;
			return true;
		}
		return false;
	}

	/**
	 * Sets state based on whether the effect is currently active.
	 * Used for WHILE_ACTIVE blink mode where state directly mirrors the effect.
	 */
	public void setWhileActiveState(boolean active)
	{
		state = active ? EffectState.ACTIVE : EffectState.INACTIVE;
	}

	/**
	 * Processes state transitions for on-expire blink mode.
	 *
	 * @param active               whether the effect is currently active
	 * @param divineActive         whether the divine counterpart varbit is active
	 * @param requireDrinkDetection if true, only flash if a drink was detected (skill boosts);
	 *                              if false, always flash on expire (prayers, varbits)
	 * @param now                  current time in millis
	 */
	public void processOnExpire(boolean active, boolean divineActive,
		boolean requireDrinkDetection, long now)
	{
		switch (state)
		{
			case INACTIVE:
			case EXPIRED_FLASHING:
				if (active)
				{
					state = EffectState.ACTIVE;
				}
				break;

			case ACTIVE:
				if (!active)
				{
					if (requireDrinkDetection && !drinkDetected)
					{
						state = EffectState.INACTIVE;
					}
					else if (divineWasActive && !divineActive)
					{
						state = EffectState.INACTIVE;
						divineWasActive = false;
					}
					else
					{
						state = EffectState.EXPIRED_FLASHING;
						expiredAtMillis = now;
					}
				}
				break;
		}
	}

	/**
	 * Processes state transitions for skill boost effects with an absolute threshold.
	 * Threshold is the minimum boost level the user wants to maintain.
	 * Alert when boost drops to or below this level.
	 *
	 * @param currentBoost current boost level above base
	 * @param threshold    alert when boost drops to this level or below
	 * @param divineActive whether the divine counterpart varbit is active
	 * @param now          current time in millis
	 */
	public void processThreshold(int currentBoost, int threshold,
		boolean divineActive, long now)
	{
		switch (state)
		{
			case INACTIVE:
			case EXPIRED_FLASHING:
				if (currentBoost > threshold)
				{
					state = EffectState.ACTIVE;
				}
				else if (currentBoost > 0)
				{
					state = EffectState.THRESHOLD_FLASHING;
					expiredAtMillis = now;
				}
				break;

			case ACTIVE:
				if (currentBoost <= 0)
				{
					if (divineWasActive && !divineActive)
					{
						state = EffectState.INACTIVE;
						divineWasActive = false;
					}
					else
					{
						state = EffectState.EXPIRED_FLASHING;
						expiredAtMillis = now;
					}
				}
				else if (currentBoost <= threshold)
				{
					state = EffectState.THRESHOLD_FLASHING;
					expiredAtMillis = now;
				}
				break;

			case THRESHOLD_FLASHING:
				if (currentBoost > threshold)
				{
					state = EffectState.ACTIVE;
				}
				else if (currentBoost <= 0)
				{
					state = EffectState.EXPIRED_FLASHING;
					expiredAtMillis = now;
				}
				break;
		}
	}

	/**
	 * Returns whether this tracker is in a flashing state for the given blink mode.
	 */
	public boolean isFlashing(BlinkMode blinkMode)
	{
		switch (blinkMode)
		{
			case ON_EXPIRE:
				return state == EffectState.EXPIRED_FLASHING
					|| state == EffectState.THRESHOLD_FLASHING;
			case WHILE_ACTIVE:
				return state == EffectState.ACTIVE;
			default:
				return false;
		}
	}

	/**
	 * Returns whether the flashing has exceeded the configured timeout.
	 */
	public boolean isTimedOut(int timeoutMinutes, long now)
	{
		return timeoutMinutes > 0 && expiredAtMillis > 0
			&& now - expiredAtMillis > timeoutMinutes * MILLIS_PER_MINUTE;
	}

	public void reset()
	{
		this.state = EffectState.INACTIVE;
		this.expiredAtMillis = 0;
		this.lastActiveAtMillis = 0;
		this.divineWasActive = false;
		this.lastKnownBoost = UNINITIALIZED_BOOST;
		this.drinkDetected = false;
	}
}
