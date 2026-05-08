# Wilderness Alarm+

A RuneLite plugin that flashes the screen when other players are visible in the wilderness, color-coded by whether they can attack you.

## What it does

While you are in the wilderness, every game tick the plugin:

1. Computes your **combat bracket**: `[yourCombat - wildyLevel, yourCombat + wildyLevel]`. This is the OSRS rule for who can target whom in the wilderness.
2. Looks at every loaded player nearby.
3. Flashes the screen:
   - **Red** if any player is inside your bracket (i.e. they can attack you)
   - **Blue** if any player is outside your bracket (i.e. they can't, but they are still loaded around you)

Both flashes are toggled independently. Red takes precedence if both kinds of players are visible.

Outside the wilderness the overlay is silent.

## Configuration

| Setting | Default | Description |
|---|---|---|
| Red flash (attackable) | on | Flash when an in-bracket player is visible |
| Blue flash (non-attackable) | off | Flash when an out-of-bracket player is visible |
| Ignore friends | on | Skip players on your friends list |
| Ignore clan | on | Skip clan members |
| Ignore friends chat | on | Skip FC members |
| Flash period (ticks) | 2 | Pulse speed; 1 tick = 600ms |
| Max opacity | 35 | Peak alpha of the overlay (5-80) |
| Attackable color | red | Color for the in-bracket flash |
| Non-attackable color | blue | Color for the out-of-bracket flash |

## License

BSD 2-Clause. See [LICENSE](LICENSE).
