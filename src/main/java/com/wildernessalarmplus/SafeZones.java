package com.wildernessalarmplus;

import net.runelite.api.coords.WorldPoint;

/**
 * Polygons of named "safe" areas that geographically fall inside the wilderness rectangle
 * but where players cannot engage in PvP. Polygon for Ferox Enclave was lifted (with the
 * same vertex set) from the existing wilderness-player-alarm plugin's SafeZoneHelper.
 */
final class SafeZones
{
	private SafeZones() {}

	// Ferox Enclave outline. Vertices form a closed loop; last edge implicit (last->first).
	private static final int[][] FEROX_ENCLAVE = {
		{3125, 3639}, {3138, 3639}, {3138, 3647}, {3156, 3647},
		{3156, 3636}, {3154, 3636}, {3154, 3626}, {3151, 3622},
		{3144, 3620}, {3142, 3618}, {3138, 3618}, {3138, 3617},
		{3125, 3617}, {3125, 3627}, {3123, 3627}, {3123, 3633},
		{3125, 3633}
	};

	static boolean isInSafeZone(WorldPoint wp)
	{
		if (wp == null)
		{
			return false;
		}
		// All currently-tracked safe zones live on plane 0 (surface).
		if (wp.getPlane() != 0)
		{
			return false;
		}
		return pointInPolygon(wp.getX(), wp.getY(), FEROX_ENCLAVE);
	}

	/**
	 * Standard ray-casting point-in-polygon test.
	 * Casts a horizontal ray from (x,y) to +infinity and counts edge crossings.
	 */
	private static boolean pointInPolygon(int x, int y, int[][] poly)
	{
		boolean inside = false;
		int n = poly.length;
		for (int i = 0, j = n - 1; i < n; j = i++)
		{
			int xi = poly[i][0], yi = poly[i][1];
			int xj = poly[j][0], yj = poly[j][1];
			boolean crosses = ((yi > y) != (yj > y))
				&& (x < (long) (xj - xi) * (y - yi) / (yj - yi) + xi);
			if (crosses)
			{
				inside = !inside;
			}
		}
		return inside;
	}
}
