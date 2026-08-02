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

import com.healthbar.model.EffectState;
import com.healthbar.model.EffectTracker;
import com.healthbar.model.TrackedEffect;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimedEffectManagerTest
{
	@Mock
	private Client client;

	@Mock
	private ClientThread clientThread;

	@Mock
	private ScheduledExecutorService executor;

	private final List<ScheduledCall> scheduledCalls = new ArrayList<>();
	private final List<Runnable> clientCallbacks = new ArrayList<>();
	private MutableClock clock;
	private TimedEffectManager manager;
	private EffectTracker tracker;

	@Before
	public void setUp()
	{
		clock = new MutableClock(Instant.ofEpochMilli(1_000));
		manager = new TimedEffectManager(client, clientThread, executor, clock);
		tracker = new EffectTracker();
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);

		when(executor.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS)))
			.thenAnswer(invocation ->
			{
				ScheduledFuture<?> future = mock(ScheduledFuture.class);
				scheduledCalls.add(new ScheduledCall(
					invocation.getArgument(0), invocation.getArgument(1), future));
				return future;
			});

		org.mockito.Mockito.doAnswer(invocation ->
		{
			clientCallbacks.add(invocation.getArgument(0));
			return null;
		}).when(clientThread).invokeLater(any(Runnable.class));
	}

	@Test
	public void testActivationSchedulesAndExpiresOnClientThread()
	{
		Instant expiration = manager.activate(
			TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(5), tracker);

		assertEquals(Instant.ofEpochMilli(6_000), expiration);
		assertEquals(5_000, scheduledCalls.get(0).delayMillis);
		assertEquals(EffectState.ACTIVE, tracker.getState());
		assertTrue(manager.isActive(TrackedEffect.WARD_OF_ARCEUUS));

		clock.advance(Duration.ofSeconds(5));
		fireScheduledCall(0);
		assertEquals("Scheduler thread must not mutate the tracker",
			EffectState.ACTIVE, tracker.getState());

		runClientCallback(0);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
		assertEquals(6_000, tracker.getExpiredAtMillis());
		assertFalse(manager.isActive(TrackedEffect.WARD_OF_ARCEUUS));
	}

	@Test
	public void testRecastCancelsAndInvalidatesPreviousExpiration()
	{
		manager.activate(TrackedEffect.MARK_OF_DARKNESS, Duration.ofSeconds(5), tracker);
		ScheduledFuture<?> firstFuture = scheduledCalls.get(0).future;

		clock.advance(Duration.ofSeconds(2));
		Instant secondExpiration = manager.activate(
			TrackedEffect.MARK_OF_DARKNESS, Duration.ofSeconds(5), tracker);
		assertEquals(Instant.ofEpochMilli(8_000), secondExpiration);
		verify(firstFuture).cancel(false);

		fireScheduledCall(0);
		runClientCallback(0);
		assertEquals("The canceled cast must not expire the replacement",
			EffectState.ACTIVE, tracker.getState());
		assertEquals(secondExpiration,
			manager.getExpiration(TrackedEffect.MARK_OF_DARKNESS));

		clock.advance(Duration.ofSeconds(5));
		fireScheduledCall(1);
		runClientCallback(1);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
		assertEquals(8_000, tracker.getExpiredAtMillis());
	}

	@Test
	public void testLogoutAndLoginBeforeDeadlineReschedulesRemainingTime()
	{
		manager.activate(TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(10), tracker);
		ScheduledFuture<?> firstFuture = scheduledCalls.get(0).future;

		clock.advance(Duration.ofSeconds(3));
		manager.onGameStateChanged(GameState.HOPPING);
		assertTrue(manager.isSuspended());
		verify(firstFuture).cancel(false);

		clock.advance(Duration.ofSeconds(2));
		manager.onGameStateChanged(GameState.LOGGED_IN);
		assertFalse(manager.isSuspended());
		assertEquals(5_000, scheduledCalls.get(1).delayMillis);
		assertEquals(EffectState.ACTIVE, tracker.getState());

		clock.advance(Duration.ofSeconds(5));
		fireScheduledCall(1);
		runClientCallback(0);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
	}

	@Test
	public void testEffectExpiredWhileLoggedOutIsDiscarded()
	{
		manager.activate(TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(5), tracker);

		clock.advance(Duration.ofSeconds(1));
		manager.onGameStateChanged(GameState.LOGIN_SCREEN);
		clock.advance(Duration.ofSeconds(5));
		manager.onGameStateChanged(GameState.LOGGED_IN);

		assertEquals(EffectState.INACTIVE, tracker.getState());
		assertFalse(manager.isActive(TrackedEffect.WARD_OF_ARCEUUS));
		assertNull(manager.getExpiration(TrackedEffect.WARD_OF_ARCEUUS));
		assertEquals("An offline-expired effect must not be rescheduled", 1,
			scheduledCalls.size());
	}

	@Test
	public void testLoadingAndRepeatedLoggedInEventsDoNotReschedule()
	{
		manager.activate(TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(5), tracker);
		ScheduledFuture<?> firstFuture = scheduledCalls.get(0).future;

		manager.onGameStateChanged(GameState.LOADING);
		manager.onGameStateChanged(GameState.LOGGED_IN);
		manager.onGameStateChanged(GameState.LOGGED_IN);

		assertFalse(manager.isSuspended());
		assertEquals(1, scheduledCalls.size());
		org.mockito.Mockito.verifyNoInteractions(firstFuture);
	}

	@Test
	public void testQueuedExpirationIsInvalidatedByLogout()
	{
		manager.activate(TrackedEffect.MARK_OF_DARKNESS, Duration.ofSeconds(5), tracker);
		clock.advance(Duration.ofSeconds(5));
		fireScheduledCall(0);

		manager.onGameStateChanged(GameState.HOPPING);
		runClientCallback(0);

		assertEquals(EffectState.ACTIVE, tracker.getState());
		assertTrue(manager.isActive(TrackedEffect.MARK_OF_DARKNESS));
	}

	@Test
	public void testCallbackDetectsDisconnectBeforeStateEventArrives()
	{
		manager.activate(TrackedEffect.MARK_OF_DARKNESS, Duration.ofSeconds(5), tracker);
		clock.advance(Duration.ofSeconds(5));
		when(client.getGameState()).thenReturn(GameState.CONNECTION_LOST);

		fireScheduledCall(0);
		runClientCallback(0);

		assertTrue(manager.isSuspended());
		assertEquals(EffectState.ACTIVE, tracker.getState());

		manager.onGameStateChanged(GameState.LOGGED_IN);
		assertEquals(EffectState.INACTIVE, tracker.getState());
		assertFalse(manager.isActive(TrackedEffect.MARK_OF_DARKNESS));
	}

	@Test
	public void testRemoveCancelsAndInvalidatesCallback()
	{
		manager.activate(TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(5), tracker);
		ScheduledFuture<?> future = scheduledCalls.get(0).future;

		manager.remove(TrackedEffect.WARD_OF_ARCEUUS);
		verify(future).cancel(false);
		fireScheduledCall(0);
		runClientCallback(0);

		assertEquals(EffectState.ACTIVE, tracker.getState());
		assertFalse(manager.isActive(TrackedEffect.WARD_OF_ARCEUUS));
	}

	@Test
	public void testClearResetsSuspendedLifecycle()
	{
		manager.activate(TrackedEffect.WARD_OF_ARCEUUS, Duration.ofSeconds(5), tracker);
		manager.onGameStateChanged(GameState.LOGIN_SCREEN);
		assertTrue(manager.isSuspended());

		manager.clear();

		assertFalse(manager.isSuspended());
		assertFalse(manager.isActive(TrackedEffect.WARD_OF_ARCEUUS));
	}

	private void fireScheduledCall(int index)
	{
		scheduledCalls.get(index).runnable.run();
	}

	private void runClientCallback(int index)
	{
		clientCallbacks.get(index).run();
	}

	private static final class ScheduledCall
	{
		private final Runnable runnable;
		private final long delayMillis;
		private final ScheduledFuture<?> future;

		private ScheduledCall(Runnable runnable, long delayMillis, ScheduledFuture<?> future)
		{
			this.runnable = runnable;
			this.delayMillis = delayMillis;
			this.future = future;
		}
	}

	private static final class MutableClock extends Clock
	{
		private Instant current;

		private MutableClock(Instant current)
		{
			this.current = current;
		}

		private void advance(Duration duration)
		{
			current = current.plus(duration);
		}

		@Override
		public ZoneId getZone()
		{
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone)
		{
			return this;
		}

		@Override
		public Instant instant()
		{
			return current;
		}
	}
}
