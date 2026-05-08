package com.wildernessalarmplus;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("wildernessalarmplus")
public interface WildernessAlarmPlusConfig extends Config
{
	@ConfigItem(
		keyName = "flashOnAttackable",
		name = "Red flash (attackable)",
		description = "Flash red when a player who can attack you (within combat bracket) is visible",
		position = 1
	)
	default boolean flashOnAttackable() { return true; }

	@ConfigItem(
		keyName = "flashOnNonAttackable",
		name = "Blue flash (non-attackable)",
		description = "Flash blue when a player outside your combat bracket is visible",
		position = 2
	)
	default boolean flashOnNonAttackable() { return false; }

	@ConfigItem(
		keyName = "ignoreFriends",
		name = "Ignore friends",
		description = "Don't flash for players on your friends list",
		position = 3
	)
	default boolean ignoreFriends() { return true; }

	@ConfigItem(
		keyName = "ignoreClan",
		name = "Ignore clan",
		description = "Don't flash for clan members",
		position = 4
	)
	default boolean ignoreClan() { return true; }

	@ConfigItem(
		keyName = "ignoreFriendsChat",
		name = "Ignore friends chat",
		description = "Don't flash for friends chat members",
		position = 5
	)
	default boolean ignoreFriendsChat() { return true; }

	@Range(min = 1, max = 30)
	@ConfigItem(
		keyName = "flashSpeedTicks",
		name = "Flash period (ticks)",
		description = "How fast the screen pulses (lower = faster). 1 game tick = 600ms",
		position = 6
	)
	default int flashSpeedTicks() { return 2; }

	@Range(min = 5, max = 80)
	@ConfigItem(
		keyName = "maxAlpha",
		name = "Max opacity",
		description = "Peak opacity of the flash overlay (5-80)",
		position = 7
	)
	default int maxAlpha() { return 35; }

	@Alpha
	@ConfigItem(
		keyName = "attackableColor",
		name = "Attackable color",
		description = "Flash color for players inside your combat bracket",
		position = 8
	)
	default Color attackableColor() { return new Color(255, 0, 0, 255); }

	@Alpha
	@ConfigItem(
		keyName = "nonAttackableColor",
		name = "Non-attackable color",
		description = "Flash color for players outside your combat bracket",
		position = 9
	)
	default Color nonAttackableColor() { return new Color(0, 120, 255, 255); }

	@ConfigItem(
		keyName = "debugChat",
		name = "Debug to chat",
		description = "Print plugin version on startup and the players triggering each flash to game chat (use to verify the latest jar is loaded)",
		position = 10
	)
	default boolean debugChat() { return false; }
}
