package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;

public final class CrosshairPatternUtil {
	public static final int GRID_SMALL = 8;
	public static final int GRID_LARGE = 16;
	public static final int MAX_GRID = GRID_LARGE;

	private CrosshairPatternUtil() {
	}

	public static boolean useDrawnPattern(BetterHudsConfig.WidgetConfig cfg) {
		return cfg.toggle("crosshair_use_drawn", false);
	}

	public static void setUseDrawnPattern(BetterHudsConfig.WidgetConfig cfg, boolean value) {
		cfg.setToggle("crosshair_use_drawn", value);
	}

	public static int gridSize(BetterHudsConfig.WidgetConfig cfg) {
		int raw = cfg.intValue("crosshair_grid_size", GRID_LARGE);
		return raw <= GRID_SMALL ? GRID_SMALL : GRID_LARGE;
	}

	public static void setGridSize(BetterHudsConfig.WidgetConfig cfg, int size) {
		cfg.setIntValue("crosshair_grid_size", size <= GRID_SMALL ? GRID_SMALL : GRID_LARGE);
	}

	public static int pixelSize(BetterHudsConfig.WidgetConfig cfg) {
		return clamp(cfg.intValue("crosshair_pixel_size", 2), 1, 8);
	}

	public static void setPixel(BetterHudsConfig.WidgetConfig cfg, int x, int y, boolean on) {
		if (!inBounds(x, y)) {
			return;
		}
		cfg.setIntValue(pixelKey(x, y), on ? 1 : 0);
	}

	public static boolean pixel(BetterHudsConfig.WidgetConfig cfg, int x, int y) {
		if (!inBounds(x, y)) {
			return false;
		}
		return cfg.intValue(pixelKey(x, y), 0) != 0;
	}

	public static void clear(BetterHudsConfig.WidgetConfig cfg) {
		for (int y = 0; y < MAX_GRID; y++) {
			for (int x = 0; x < MAX_GRID; x++) {
				cfg.setIntValue(pixelKey(x, y), 0);
			}
		}
	}

	public static boolean hasAnyPixel(BetterHudsConfig.WidgetConfig cfg) {
		int grid = gridSize(cfg);
		for (int y = 0; y < grid; y++) {
			for (int x = 0; x < grid; x++) {
				if (pixel(cfg, x, y)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < MAX_GRID && y < MAX_GRID;
	}

	private static String pixelKey(int x, int y) {
		return "cross_px_" + x + "_" + y;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
