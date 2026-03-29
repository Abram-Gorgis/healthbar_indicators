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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class EffectTrackerTest
{
	private EffectTracker tracker;

	@Before
	public void setUp()
	{
		tracker = new EffectTracker();
	}

	// =====================================================
	// Initial state
	// =====================================================

	@Test
	public void testInitialStateIsInactive()
	{
		assertEquals(EffectState.INACTIVE, tracker.getState());
		assertFalse(tracker.isFlashing(BlinkMode.ON_EXPIRE));
		assertFalse(tracker.isFlashing(BlinkMode.WHILE_ACTIVE));
	}

	// =====================================================
	// processOnExpire
	// =====================================================

	@Test
	public void testProcessOnExpireInactiveToActive()
	{
		tracker.processOnExpire(true, false, false, 1000);
		assertEquals(EffectState.ACTIVE, tracker.getState());
	}

	@Test
	public void testProcessOnExpireActiveToFlashing()
	{
		tracker.processOnExpire(true, false, false, 1000);
		tracker.processOnExpire(false, false, false, 2000);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
		assertTrue(tracker.isFlashing(BlinkMode.ON_EXPIRE));
	}

	@Test
	public void testProcessOnExpireFlashingBackToActive()
	{
		tracker.processOnExpire(true, false, false, 1000);
		tracker.processOnExpire(false, false, false, 2000);
		tracker.processOnExpire(true, false, false, 3000);
		assertEquals(EffectState.ACTIVE, tracker.getState());
		assertFalse(tracker.isFlashing(BlinkMode.ON_EXPIRE));
	}

	@Test
	public void testProcessOnExpireInactiveStaysInactiveWhenNotActive()
	{
		tracker.processOnExpire(false, false, false, 1000);
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	@Test
	public void testProcessOnExpireWithDrinkDetectionRequired()
	{
		// No drink detected — should go inactive, not flashing
		tracker.processOnExpire(true, false, true, 1000);
		tracker.processOnExpire(false, false, true, 2000);
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	@Test
	public void testProcessOnExpireWithDrinkDetected()
	{
		tracker.updateBoost(0);
		tracker.updateBoost(10); // drink detected
		tracker.processOnExpire(true, false, true, 1000);
		tracker.processOnExpire(false, false, true, 2000);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
	}

	@Test
	public void testProcessOnExpireDivineGuardrail()
	{
		tracker.updateDivine(true);
		tracker.processOnExpire(true, false, false, 1000);
		// Divine was active, now it's not — should go inactive
		tracker.processOnExpire(false, false, false, 2000);
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	// =====================================================
	// processThreshold
	// =====================================================

	@Test
	public void testThresholdInactiveToActiveAboveThreshold()
	{
		tracker.processThreshold(12, 9, false, 1000);
		assertEquals(EffectState.ACTIVE, tracker.getState());
	}

	@Test
	public void testThresholdActiveToFlashingAtThreshold()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(9, 9, false, 2000);
		assertEquals(EffectState.THRESHOLD_FLASHING, tracker.getState());
		assertTrue(tracker.isFlashing(BlinkMode.ON_EXPIRE));
	}

	@Test
	public void testThresholdActiveToFlashingBelowThreshold()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(8, 9, false, 2000);
		assertEquals(EffectState.THRESHOLD_FLASHING, tracker.getState());
	}

	@Test
	public void testThresholdFlashingBackToActiveOnRedrink()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(9, 9, false, 2000);
		tracker.processThreshold(12, 9, false, 3000);
		assertEquals(EffectState.ACTIVE, tracker.getState());
	}

	@Test
	public void testThresholdFlashingToExpiredAtZero()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(9, 9, false, 2000);
		tracker.processThreshold(0, 9, false, 3000);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
	}

	@Test
	public void testThresholdActiveToExpiredAtZero()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(0, 9, false, 2000);
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
	}

	@Test
	public void testThresholdDivineGuardrailAtZero()
	{
		tracker.updateDivine(true);
		tracker.processThreshold(12, 9, false, 1000);
		// Divine was active, boost drops to 0 — should go inactive
		tracker.processThreshold(0, 9, false, 2000);
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	@Test
	public void testThresholdStaysActiveAboveThreshold()
	{
		tracker.processThreshold(12, 9, false, 1000);
		tracker.processThreshold(10, 9, false, 2000);
		assertEquals(EffectState.ACTIVE, tracker.getState());
	}

	@Test
	public void testThresholdLoginBelowThresholdWithBoost()
	{
		// Login at +8, threshold is 9 — below threshold but has boost
		tracker.processThreshold(8, 9, false, 1000);
		assertEquals(EffectState.THRESHOLD_FLASHING, tracker.getState());
	}

	@Test
	public void testThresholdLoginAtZeroStaysInactive()
	{
		tracker.processThreshold(0, 9, false, 1000);
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	// =====================================================
	// setWhileActiveState
	// =====================================================

	@Test
	public void testWhileActiveFlashesWhenActive()
	{
		tracker.setWhileActiveState(true);
		assertEquals(EffectState.ACTIVE, tracker.getState());
		assertTrue(tracker.isFlashing(BlinkMode.WHILE_ACTIVE));
	}

	@Test
	public void testWhileActiveStopsWhenInactive()
	{
		tracker.setWhileActiveState(true);
		tracker.setWhileActiveState(false);
		assertEquals(EffectState.INACTIVE, tracker.getState());
		assertFalse(tracker.isFlashing(BlinkMode.WHILE_ACTIVE));
	}

	// =====================================================
	// tryExpire
	// =====================================================

	@Test
	public void testTryExpireFromActive()
	{
		tracker.activate(1000);
		assertTrue(tracker.tryExpire(2000));
		assertEquals(EffectState.EXPIRED_FLASHING, tracker.getState());
	}

	@Test
	public void testTryExpireFromInactive()
	{
		assertFalse(tracker.tryExpire(1000));
		assertEquals(EffectState.INACTIVE, tracker.getState());
	}

	// =====================================================
	// Drink detection (updateBoost)
	// =====================================================

	@Test
	public void testFirstBoostObservationZeroDoesNotDetectDrink()
	{
		tracker.updateBoost(0);
		assertFalse(tracker.isDrinkDetected());
	}

	@Test
	public void testFirstBoostObservationPositiveDetectsDrink()
	{
		tracker.updateBoost(12);
		assertTrue(tracker.isDrinkDetected());
	}

	@Test
	public void testBoostIncreaseDetectsDrink()
	{
		tracker.updateBoost(5);
		tracker.updateBoost(12);
		assertTrue(tracker.isDrinkDetected());
	}

	@Test
	public void testBoostDecreaseDoesNotDetectDrink()
	{
		tracker.updateBoost(0);
		tracker.updateBoost(0);
		assertFalse(tracker.isDrinkDetected());
	}

	// =====================================================
	// Timeout
	// =====================================================

	@Test
	public void testIsTimedOutBeforeTimeout()
	{
		tracker.activate(1000);
		tracker.tryExpire(2000);
		assertFalse(tracker.isTimedOut(1, 50_000));
	}

	@Test
	public void testIsTimedOutAfterTimeout()
	{
		tracker.activate(1000);
		tracker.tryExpire(2000);
		assertTrue(tracker.isTimedOut(1, 63_000));
	}

	@Test
	public void testZeroTimeoutNeverTimesOut()
	{
		tracker.activate(1000);
		tracker.tryExpire(2000);
		assertFalse(tracker.isTimedOut(0, 999_999_999));
	}

	// =====================================================
	// Reset
	// =====================================================

	@Test
	public void testResetClearsAllState()
	{
		tracker.activate(1000);
		tracker.updateDivine(true);
		tracker.updateBoost(10);
		tracker.tryExpire(2000);

		tracker.reset();

		assertEquals(EffectState.INACTIVE, tracker.getState());
		assertEquals(0, tracker.getExpiredAtMillis());
		assertEquals(0, tracker.getLastActiveAtMillis());
		assertFalse(tracker.isDivineWasActive());
		assertFalse(tracker.isDrinkDetected());
	}
}
