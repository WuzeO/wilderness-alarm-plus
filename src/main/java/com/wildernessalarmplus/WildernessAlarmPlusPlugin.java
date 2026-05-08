package com.wildernessalarmplus;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Wilderness Alarm+",
	description = "Flashes the screen when nearby players are inside (red) or outside (blue) your wilderness combat bracket",
	tags = {"wilderness", "pvp", "alarm", "combat", "bracket", "pk", "alert"}
)
public class WildernessAlarmPlusPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(WildernessAlarmPlusPlugin.class);
	private static final String VERSION = "1.0.5";
	private static final int VARBIT_IN_WILDERNESS = 5963;

	@Inject private Client client;
	@Inject private WildernessAlarmPlusConfig config;
	@Inject private OverlayManager overlayManager;
	@Inject private FlashOverlay overlay;

	private volatile Color activeColor;

	@Provides
	WildernessAlarmPlusConfig provideConfig(ConfigManager cm)
	{
		return cm.getConfig(WildernessAlarmPlusConfig.class);
	}

	@Override
	protected void startUp()
	{
		activeColor = null;
		overlayManager.add(overlay);
		log.info("[Wilderness Alarm+] v{} startUp; debugChat={}", VERSION, config.debugChat());
		announceLoaded();
	}

	private void announceLoaded()
	{
		if (!config.debugChat())
		{
			return;
		}
		try
		{
			client.addChatMessage(
				ChatMessageType.GAMEMESSAGE,
				"",
				"[Wilderness Alarm+] v" + VERSION + " loaded.",
				""
			);
		}
		catch (Throwable ignored)
		{
			// client may not be ready at startup; harmless
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		activeColor = null;
	}

	public Color getActiveFlashColor()
	{
		return activeColor;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player local = client.getLocalPlayer();
		if (local == null || client.getVarbitValue(VARBIT_IN_WILDERNESS) != 1)
		{
			activeColor = null;
			return;
		}

		int wildyLevel = computeWildernessLevel(local.getWorldLocation());
		if (wildyLevel <= 0)
		{
			activeColor = null;
			return;
		}

		int myCombat = local.getCombatLevel();
		int low = myCombat - wildyLevel;
		int high = myCombat + wildyLevel;

		boolean sawAttackable = false;
		boolean sawNonAttackable = false;
		List<String> debugAttackable = config.debugChat() ? new ArrayList<>() : null;
		List<String> debugNonAttackable = config.debugChat() ? new ArrayList<>() : null;

		for (Player p : client.getTopLevelWorldView().players())
		{
			if (p == null || p == local || shouldIgnore(p))
			{
				continue;
			}
			WorldPoint wp = p.getWorldLocation();
			if (!isInWilderness(wp))
			{
				continue;
			}
			int cb = p.getCombatLevel();
			boolean inBracket = cb >= low && cb <= high;
			if (inBracket)
			{
				sawAttackable = true;
			}
			else
			{
				sawNonAttackable = true;
			}
			if (debugAttackable != null && wp != null)
			{
				String entry = (p.getName() == null ? "?" : p.getName())
					+ " cb=" + cb + " @(" + wp.getX() + "," + wp.getY() + ")";
				if (inBracket)
				{
					debugAttackable.add(entry);
				}
				else
				{
					debugNonAttackable.add(entry);
				}
			}
			// no early break when debug enabled — we want a full list
			if (debugAttackable == null && sawAttackable && sawNonAttackable)
			{
				break;
			}
		}

		if (sawAttackable && config.flashOnAttackable())
		{
			activeColor = config.attackableColor();
		}
		else if (sawNonAttackable && config.flashOnNonAttackable())
		{
			activeColor = config.nonAttackableColor();
		}
		else
		{
			activeColor = null;
		}

		if (debugAttackable != null)
		{
			WorldPoint mwp = local.getWorldLocation();
			String msg = "[WA+ v" + VERSION + "] me@(" + (mwp == null ? "?" : mwp.getX() + "," + mwp.getY())
				+ ") wildy=" + wildyLevel
				+ " bracket=" + low + ".." + high
				+ " atk=" + debugAttackable
				+ " non=" + debugNonAttackable;
			log.info(msg);
			if (!debugAttackable.isEmpty() || !debugNonAttackable.isEmpty())
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, "");
			}
		}
	}

	private boolean shouldIgnore(Player p)
	{
		if (config.ignoreFriends() && p.isFriend())
		{
			return true;
		}
		if (config.ignoreClan() && p.isClanMember())
		{
			return true;
		}
		if (config.ignoreFriendsChat() && p.isFriendsChatMember())
		{
			return true;
		}
		return false;
	}

	private static boolean isInWilderness(WorldPoint wp)
	{
		if (computeWildernessLevel(wp) <= 0)
		{
			return false;
		}
		// Exclude known safe zones inside the wildy bounding box (e.g. Ferox Enclave).
		return !SafeZones.isInSafeZone(wp);
	}

	// Wilderness rectangle bounds (surface and underground share X range; underground Y is offset by 6400)
	private static final int WILDY_MIN_X = 2944;
	private static final int WILDY_MAX_X = 3392;
	private static final int WILDY_MIN_Y_SURFACE = 3523;
	private static final int WILDY_MAX_Y_SURFACE = 3967;
	private static final int WILDY_MIN_Y_CAVES = 9920;
	private static final int WILDY_MAX_Y_CAVES = 10367;

	private static int computeWildernessLevel(WorldPoint wp)
	{
		if (wp == null)
		{
			return 0;
		}
		int x = wp.getX();
		int y = wp.getY();
		if (x < WILDY_MIN_X || x > WILDY_MAX_X)
		{
			return 0;
		}
		if (y >= WILDY_MIN_Y_CAVES && y <= WILDY_MAX_Y_CAVES)
		{
			return ((y - WILDY_MIN_Y_CAVES) / 8) + 1;
		}
		if (y >= WILDY_MIN_Y_SURFACE && y <= WILDY_MAX_Y_SURFACE)
		{
			return ((y - 3520) / 8) + 1;
		}
		return 0;
	}
}
