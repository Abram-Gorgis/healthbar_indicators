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

import com.healthbar.model.EffectTracker;
import com.healthbar.model.TrackedEffect;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;

/**
 * Owns the runtime lifecycle of chat-activated effects with calculated durations.
 * All tracker transitions happen on the RuneLite client thread.
 */
@Slf4j
public final class TimedEffectManager
{
	private final Client client;
	private final ClientThread clientThread;
	private final ScheduledExecutorService executor;
	private final Clock clock;
	private final Map<TrackedEffect, ActiveTimedEffect> activeEffects =
		new EnumMap<>(TrackedEffect.class);

	private long nextGeneration;
	private boolean suspended;

	public TimedEffectManager(Client client, ClientThread clientThread,
		ScheduledExecutorService executor)
	{
		this(client, clientThread, executor, Clock.systemUTC());
	}

	public TimedEffectManager(Client client, ClientThread clientThread,
		ScheduledExecutorService executor, Clock clock)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.executor = executor;
		this.clock = clock;
	}

	/**
	 * Starts or replaces a timed effect. Recasting always creates a new deadline.
	 */
	public void activate(TrackedEffect effect, Duration duration, EffectTracker tracker)
	{
		if (duration == null || duration.isZero() || duration.isNegative())
		{
			log.warn("Ignoring timed effect {} with invalid duration {}", effect, duration);
			return;
		}

		remove(effect);

		Instant now = clock.instant();
		Instant expiresAt = now.plus(duration);
		tracker.activate(now.toEpochMilli());

		ActiveTimedEffect activeEffect = new ActiveTimedEffect(effect, tracker, expiresAt);
		activeEffects.put(effect, activeEffect);
		if (!suspended)
		{
			schedule(activeEffect);
		}
	}

	/**
	 * Updates scheduling only for states that enter or leave a real game session.
	 * LOADING is intentionally ignored because it also occurs during region changes.
	 */
	public void onGameStateChanged(GameState gameState)
	{
		if (isDisconnectedState(gameState))
		{
			suspend();
		}
		else if (gameState == GameState.LOGGED_IN)
		{
			resume();
		}
	}

	public boolean isActive(TrackedEffect effect)
	{
		return activeEffects.containsKey(effect);
	}

	public Instant getExpiration(TrackedEffect effect)
	{
		ActiveTimedEffect activeEffect = activeEffects.get(effect);
		return activeEffect == null ? null : activeEffect.expiresAt;
	}

	public boolean isSuspended()
	{
		return suspended;
	}

	public void remove(TrackedEffect effect)
	{
		ActiveTimedEffect activeEffect = activeEffects.remove(effect);
		if (activeEffect != null)
		{
			invalidate(activeEffect);
		}
	}

	public void retainAll(Set<TrackedEffect> retainedEffects)
	{
		Iterator<Map.Entry<TrackedEffect, ActiveTimedEffect>> iterator =
			activeEffects.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<TrackedEffect, ActiveTimedEffect> entry = iterator.next();
			if (!retainedEffects.contains(entry.getKey()))
			{
				invalidate(entry.getValue());
				iterator.remove();
			}
		}
	}

	public void clear()
	{
		for (ActiveTimedEffect activeEffect : activeEffects.values())
		{
			invalidate(activeEffect);
		}
		activeEffects.clear();
		suspended = false;
	}

	private void suspend()
	{
		if (suspended)
		{
			return;
		}

		suspended = true;
		for (ActiveTimedEffect activeEffect : activeEffects.values())
		{
			invalidate(activeEffect);
		}
	}

	private void resume()
	{
		if (!suspended)
		{
			return;
		}

		suspended = false;
		Instant now = clock.instant();
		Iterator<ActiveTimedEffect> iterator = activeEffects.values().iterator();
		while (iterator.hasNext())
		{
			ActiveTimedEffect activeEffect = iterator.next();
			if (!activeEffect.expiresAt.isAfter(now))
			{
				activeEffect.tracker.reset();
				iterator.remove();
			}
			else
			{
				schedule(activeEffect);
			}
		}
	}

	private void schedule(ActiveTimedEffect activeEffect)
	{
		long generation = ++nextGeneration;
		activeEffect.generation = generation;
		long delayMillis = Math.max(0,
			Duration.between(clock.instant(), activeEffect.expiresAt).toMillis());

		activeEffect.scheduledFuture = executor.schedule(
			() -> clientThread.invokeLater(
				() -> expire(activeEffect.effect, generation)),
			delayMillis,
			TimeUnit.MILLISECONDS);
	}

	private void expire(TrackedEffect effect, long generation)
	{
		ActiveTimedEffect activeEffect = activeEffects.get(effect);
		if (activeEffect == null || activeEffect.generation != generation)
		{
			return;
		}

		if (suspended || isDisconnectedState(client.getGameState()))
		{
			suspend();
			return;
		}

		activeEffects.remove(effect);
		activeEffect.scheduledFuture = null;
		activeEffect.tracker.tryExpire(activeEffect.expiresAt.toEpochMilli());
	}

	private void invalidate(ActiveTimedEffect activeEffect)
	{
		activeEffect.generation = ++nextGeneration;
		if (activeEffect.scheduledFuture != null)
		{
			activeEffect.scheduledFuture.cancel(false);
			activeEffect.scheduledFuture = null;
		}
	}

	private static boolean isDisconnectedState(GameState gameState)
	{
		return gameState == GameState.HOPPING
			|| gameState == GameState.LOGIN_SCREEN
			|| gameState == GameState.LOGIN_SCREEN_AUTHENTICATOR
			|| gameState == GameState.LOGGING_IN
			|| gameState == GameState.CONNECTION_LOST;
	}

	private static final class ActiveTimedEffect
	{
		private final TrackedEffect effect;
		private final EffectTracker tracker;
		private final Instant expiresAt;
		private long generation;
		private ScheduledFuture<?> scheduledFuture;

		private ActiveTimedEffect(TrackedEffect effect, EffectTracker tracker, Instant expiresAt)
		{
			this.effect = effect;
			this.tracker = tracker;
			this.expiresAt = expiresAt;
		}
	}
}
