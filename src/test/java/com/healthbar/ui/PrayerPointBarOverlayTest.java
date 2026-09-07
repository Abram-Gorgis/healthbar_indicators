package com.healthbar.ui;

import com.healthbar.HealthbarIndicatorsConfig;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PrayerPointBarOverlayTest
{
	private Client client;
	private Player player;
	private HealthbarIndicatorsConfig config;
	private PrayerPointBarOverlay overlay;

	@Before
	public void setUp()
	{
		client = mock(Client.class);
		player = mock(Player.class);
		config = mock(HealthbarIndicatorsConfig.class);
		when(config.showPrayerPointBar()).thenReturn(true);
		when(config.prayerBarOnlyWithHealthBar()).thenReturn(true);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getHealthRatio()).thenReturn(30);
		when(player.getHealthScale()).thenReturn(30);
		when(client.getRealSkillLevel(Skill.PRAYER)).thenReturn(100);
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(50);
		overlay = spy(new PrayerPointBarOverlay(client, config));
		doReturn(new Point(50, 50)).when(overlay).getAnchor(player);
	}

	private BufferedImage render()
	{
		return render(0);
	}

	private BufferedImage render(long now)
	{
		BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try { overlay.render(graphics, now); }
		finally { graphics.dispose(); }
		return image;
	}

	private void assertHidden()
	{
		BufferedImage image = render();
		for (int y = 0; y < 100; y++)
			for (int x = 0; x < 100; x++)
				assertEquals(0, image.getRGB(x, y));
	}

	@Test
	public void anchorDelegatesToActorWithoutImageCenteringOffset()
	{
		overlay = new PrayerPointBarOverlay(client, config);
		when(player.getLocalLocation()).thenReturn(new LocalPoint(6400, 6400));
		when(player.getWorldView()).thenReturn(mock(WorldView.class));
		when(player.getLogicalHeight()).thenReturn(180);
		when(player.getCanvasImageLocation(any(BufferedImage.class), eq(195)))
			.thenAnswer(invocation -> {
				BufferedImage marker = invocation.getArgument(0);
				assertEquals(1, marker.getWidth());
				assertEquals(1, marker.getHeight());
				return new Point(50, 50);
			});
		assertEquals(new Point(50, 50), overlay.getAnchor(player));
		verify(client, never()).getPlane();
		when(player.getLogicalHeight()).thenReturn(160);
		when(player.getCanvasImageLocation(any(BufferedImage.class), eq(175)))
			.thenReturn(new Point(55, 45));
		assertEquals(new Point(55, 45), overlay.getAnchor(player));
	}

	@Test
	public void actorProjectionMovesImmediatelyWithConstantPixelSpacing()
	{
		overlay = new PrayerPointBarOverlay(client, config);
		when(player.getLocalLocation()).thenReturn(new LocalPoint(6400, 6400));
		when(player.getWorldView()).thenReturn(mock(WorldView.class));
		when(player.getCanvasImageLocation(any(BufferedImage.class), anyInt()))
			.thenReturn(new Point(50, 50), new Point(60, 30));
		BufferedImage first = render();
		assertEquals(0, first.getRGB(34, 52));
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), first.getRGB(35, 52));
		BufferedImage moved = render();
		assertEquals(0, moved.getRGB(44, 32));
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), moved.getRGB(45, 32));
		assertEquals(0, moved.getRGB(45, 31));
		assertEquals(0, moved.getRGB(35, 52));
	}

	@Test
	public void realAnchorSkipsMissingActorLocationWorldAndProjection()
	{
		overlay = new PrayerPointBarOverlay(client, config);
		assertHidden();
		verify(player, never()).getCanvasImageLocation(any(BufferedImage.class), anyInt());
		when(player.getLocalLocation()).thenReturn(new LocalPoint(6400, 6400));
		assertHidden();
		verify(player, never()).getCanvasImageLocation(any(BufferedImage.class), anyInt());
		when(player.getWorldView()).thenReturn(mock(WorldView.class));
		assertHidden();
		verify(player).getCanvasImageLocation(any(BufferedImage.class), anyInt());
	}

	@Test
	public void rendersHalfFullBelowAnchorAndUnderInterfaces()
	{
		assertEquals(OverlayLayer.UNDER_WIDGETS, overlay.getLayer());
		BufferedImage image = render();
		assertEquals(0, image.getRGB(34, 54));
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), image.getRGB(35, 54));
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), image.getRGB(49, 56));
		int emptyAlpha = image.getRGB(50, 54) >>> 24;
		assertTrue(emptyAlpha > 0 && emptyAlpha < 255);
		assertEquals(0, image.getRGB(35, 51));
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), image.getRGB(35, 52));
		assertNotEquals(0, image.getRGB(64, 54));
		assertEquals(0, image.getRGB(65, 54));
		assertEquals(0, image.getRGB(35, 57));
	}

	@Test
	public void toggleAndUnavailableHealthHideBar()
	{
		when(config.showPrayerPointBar()).thenReturn(false);
		assertHidden();
		when(config.showPrayerPointBar()).thenReturn(true);
		when(player.getHealthRatio()).thenReturn(-1);
		assertHidden();
		when(config.prayerBarOnlyWithHealthBar()).thenReturn(false);
		assertNotEquals(0, render().getRGB(35, 54));
	}

	@Test
	public void emptyHealthIsNotMissingHealth()
	{
		when(player.getHealthRatio()).thenReturn(0);
		assertNotEquals(0, render().getRGB(35, 54));
	}

	@Test
	public void clampsEmptyAndOverboostedPrayer()
	{
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(-1);
		BufferedImage empty = render();
		assertEquals(empty.getRGB(35, 54), empty.getRGB(64, 54));
		assertTrue((empty.getRGB(35, 54) >>> 24) < 255);
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(120);
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), render().getRGB(64, 54));
	}

	@Test
	public void lowPrayerFlashesOnlyDepletedPortionAndStopsAtThreshold()
	{
		when(config.prayerBarWarningThreshold()).thenReturn(10);
		when(config.flashRate()).thenReturn(500);
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(9);
		BufferedImage red = render(0);
		BufferedImage gray = render(500);
		assertEquals(PrayerPointBarOverlay.FILL.getRGB(), red.getRGB(35, 54));
		assertEquals(red.getRGB(35, 54), gray.getRGB(35, 54));
		assertNotEquals(red.getRGB(64, 54), gray.getRGB(64, 54));
		assertTrue(((red.getRGB(64, 54) >> 16) & 255) > 240);
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(10);
		assertEquals(gray.getRGB(64, 54), render(0).getRGB(64, 54));
		when(client.getBoostedSkillLevel(Skill.PRAYER)).thenReturn(0);
		assertEquals(render(0).getRGB(35, 54), render(0).getRGB(64, 54));
		when(config.prayerBarWarningThreshold()).thenReturn(0);
		assertEquals(gray.getRGB(64, 54), render(0).getRGB(64, 54));
	}

	@Test
	public void skipsInvalidMaximumAndOffscreenPlayer()
	{
		when(client.getRealSkillLevel(Skill.PRAYER)).thenReturn(0);
		assertHidden();
		when(client.getRealSkillLevel(Skill.PRAYER)).thenReturn(100);
		doReturn(null).when(overlay).getAnchor(player);
		assertHidden();
	}

	@Test
	public void skipsLogoutMissingPlayerAndDeath()
	{
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		assertHidden();
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(null);
		assertHidden();
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.isDead()).thenReturn(true);
		assertHidden();
	}
}
