package com.wildernessalarmplus;

import com.google.inject.Provides;
import java.awt.Color;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Wilderness Alarm+",
	description = "Flashes the screen when nearby players are inside (red) or outside (blue) your wilderness combat bracket",
	tags = {"wilderness", "pvp", "alarm", "combat", "bracket", "pk", "alert"}
)
public class WildernessAlarmPlusPlugin extends Plugin
{
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

		for (Player p : client.getTopLevelWorldView().players())
		{
			if (p == null || p == local || shouldIgnore(p))
			{
				continue;
			}
			if (!isInWilderness(p.getWorldLocation()))
			{
				continue;
			}
			int cb = p.getCombatLevel();
			if (cb >= low && cb <= high)
			{
				sawAttackable = true;
			}
			else
			{
				sawNonAttackable = true;
			}
			if (sawAttackable && sawNonAttackable)
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
		return computeWildernessLevel(wp) > 0;
	}

	private static int computeWildernessLevel(WorldPoint wp)
	{
		if (wp == null)
		{
			return 0;
		}
		int y = wp.getY();
		if (y >= 9920)
		{
			return ((y - 9920) / 8) + 1;
		}
		if (y >= 3523)
		{
			return ((y - 3520) / 8) + 1;
		}
		return 0;
	}
}
