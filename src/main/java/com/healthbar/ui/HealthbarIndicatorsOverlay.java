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
package com.healthbar.ui;

import com.healthbar.HealthbarIndicatorsConfig;
import com.healthbar.HealthbarIndicatorsPlugin;
import com.healthbar.model.TrackedEffect;
import com.healthbar.model.TrackedEffectEntry;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class HealthbarIndicatorsOverlay extends Overlay
{
	private static final int HEIGHT_ABOVE_PLAYER = 60;
	private static final int ICON_SPACING = 2;
	private static final int MIN_FLASH_RATE = 100;

	private final Client client;
	private final HealthbarIndicatorsPlugin plugin;
	private final HealthbarIndicatorsConfig config;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	private final Map<TrackedEffect, BufferedImage> spriteCache = new HashMap<>();

	@Inject
	public HealthbarIndicatorsOverlay(Client client, HealthbarIndicatorsPlugin plugin,
		HealthbarIndicatorsConfig config, ItemManager itemManager, SpriteManager spriteManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		List<TrackedEffectEntry> flashingEntries = plugin.getFlashingEntries();
		if (flashingEntries.isEmpty())
		{
			return null;
		}

		net.runelite.api.Point screenPos = getPlayerScreenPosition();
		if (screenPos == null)
		{
			return null;
		}

		if (!isBlinkOn())
		{
			return null;
		}

		drawIcons(graphics, flashingEntries, screenPos);
		return null;
	}

	private net.runelite.api.Point getPlayerScreenPosition()
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}

		LocalPoint lp = localPlayer.getLocalLocation();
		if (lp == null)
		{
			return null;
		}

		return net.runelite.api.Perspective.localToCanvas(
			client, lp, client.getPlane(), localPlayer.getLogicalHeight() + HEIGHT_ABOVE_PLAYER);
	}

	private boolean isBlinkOn()
	{
		int flashRate = Math.max(config.flashRate(), MIN_FLASH_RATE);
		return (System.currentTimeMillis() / flashRate) % 2 == 0;
	}

	private void drawIcons(Graphics2D graphics, List<TrackedEffectEntry> entries,
		net.runelite.api.Point screenPos)
	{
		int iconSize = config.iconSize();
		int totalWidth = entries.size() * (iconSize + ICON_SPACING) - ICON_SPACING;
		int startX = screenPos.getX() - totalWidth / 2 + config.offsetX();
		int startY = screenPos.getY() - iconSize - config.offsetY();

		int drawn = 0;
		for (TrackedEffectEntry entry : entries)
		{
			TrackedEffect effect = entry.getEffect();
			if (effect == null)
			{
				continue;
			}

			BufferedImage sprite = getSprite(effect);
			if (sprite == null)
			{
				continue;
			}

			int x = startX + (drawn * (iconSize + ICON_SPACING));
			graphics.drawImage(sprite, x, startY, iconSize, iconSize, null);
			drawn++;
		}
	}

	private BufferedImage getSprite(TrackedEffect effect)
	{
		BufferedImage cached = spriteCache.get(effect);
		if (cached != null)
		{
			return cached;
		}

		BufferedImage loaded = loadSprite(effect);
		if (loaded != null)
		{
			spriteCache.put(effect, loaded);
		}
		return loaded;
	}

	private BufferedImage loadSprite(TrackedEffect effect)
	{
		if (effect.isItemSprite())
		{
			return itemManager.getImage(effect.getDefaultSpriteId());
		}
		else
		{
			return spriteManager.getSprite(effect.getDefaultSpriteId(), 0);
		}
	}

	public void clearSpriteCache()
	{
		spriteCache.clear();
	}
}
