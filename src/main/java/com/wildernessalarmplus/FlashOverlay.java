package com.wildernessalarmplus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class FlashOverlay extends Overlay
{
	private final Client client;
	private final WildernessAlarmPlusPlugin plugin;
	private final WildernessAlarmPlusConfig config;

	@Inject
	public FlashOverlay(Client client, WildernessAlarmPlusPlugin plugin, WildernessAlarmPlusConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Color base = plugin.getActiveFlashColor();
		if (base == null)
		{
			return null;
		}

		int periodMs = Math.max(1, config.flashSpeedTicks()) * 600;
		double phase = (System.currentTimeMillis() % periodMs) / (double) periodMs;
		double pulse = 0.5 - 0.5 * Math.cos(phase * 2.0 * Math.PI);

		int maxAlpha = (int) Math.round(config.maxAlpha() * 2.55);
		int alpha = (int) Math.round(pulse * maxAlpha);
		if (alpha <= 0)
		{
			return null;
		}

		Rectangle bounds = client.getCanvas().getBounds();
		graphics.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
		graphics.fillRect(0, 0, bounds.width, bounds.height);
		return null;
	}
}
