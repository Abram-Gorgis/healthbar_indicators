package com.healthbar.ui;

import com.healthbar.HealthbarIndicatorsConfig;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

/** Current prayer points, independent of tracked effects and their blink cycle. */
public class PrayerPointBarOverlay extends Overlay
{
	// Standard small health-bar footprint in canvas pixels.
	static final int WIDTH = 30;
	static final int HEIGHT = 5;
	static final Color FILL = new Color(120, 205, 255);
	static final Color EMPTY = new Color(85, 85, 85, 140);
	static final Color WARNING = new Color(255, 45, 45, 220);
	// A one-pixel image has no integer centering offset in the actor helper.
	// It is only used to obtain a point; it is never drawn.
	private static final BufferedImage ANCHOR_MARKER =
		new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private final Client client;
	private final HealthbarIndicatorsConfig config;

	@Inject
	public PrayerPointBarOverlay(Client client, HealthbarIndicatorsConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		return render(graphics, System.currentTimeMillis());
	}

	Dimension render(Graphics2D graphics, long now)
	{
		if (!config.showPrayerPointBar() || client.getGameState() != GameState.LOGGED_IN)
		{
			return null;
		}
		Player player = client.getLocalPlayer();
		if (player == null || player.isDead())
		{
			return null;
		}
		if (config.prayerBarOnlyWithHealthBar()
			&& (player.getHealthRatio() < 0 || player.getHealthScale() <= 0))
		{
			return null;
		}
		int maximum = client.getRealSkillLevel(Skill.PRAYER);
		if (maximum <= 0)
		{
			return null;
		}
		Point anchor = getAnchor(player);
		if (anchor == null)
		{
			return null;
		}
		int current = Math.max(0, client.getBoostedSkillLevel(Skill.PRAYER));
		int filled = (int) Math.round(WIDTH * Math.min(1.0, current / (double) maximum));
		int threshold = config.prayerBarWarningThreshold();
		boolean warning = threshold > 0 && current < threshold
			&& (now / Math.max(100, config.flashRate())) % 2 == 0;
		int x = anchor.getX() - WIDTH / 2;
		// Native small bar is approximately y-3 through y+1; join its lower edge.
		int y = anchor.getY() + 2;
		Graphics2D barGraphics = (Graphics2D) graphics.create();
		try
		{
			barGraphics.setColor(warning ? WARNING : EMPTY);
			barGraphics.fillRect(x + filled, y, WIDTH - filled, HEIGHT);
			barGraphics.setColor(FILL);
			barGraphics.fillRect(x, y, filled, HEIGHT);
		}
		finally
		{
			barGraphics.dispose();
		}
		return null;
	}

	Point getAnchor(Player player)
	{
		if (player.getLocalLocation() == null || player.getWorldView() == null)
		{
			return null;
		}
		// Delegate actor ground/footprint, world-view and animation-height handling
		// to RuneLite. The generic LocalPoint projection does not include these.
		// Keep the bar spacing in screen pixels after projection, not world units.
		return player.getCanvasImageLocation(ANCHOR_MARKER, player.getLogicalHeight() + 15);
	}
}
