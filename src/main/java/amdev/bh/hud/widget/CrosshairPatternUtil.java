package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;

public final class CrosshairPatternUtil {
	public static final int GRID_SMALL = 8;
	public static final int GRID_CENTERED = 15;
	public static final int GRID_LARGE = 16;
	public static final int GRID_XL = 30;
	public static final int MAX_GRID = GRID_XL;
	private static final int[] GRID_SIZES = {GRID_SMALL, GRID_CENTERED, GRID_LARGE, GRID_XL};

	private CrosshairPatternUtil() {
	}

	public static boolean useDrawnPattern(BetterHudsConfig.WidgetConfig cfg) {
		return cfg.toggle("crosshair_use_drawn", false);
	}

	public static void setUseDrawnPattern(BetterHudsConfig.WidgetConfig cfg, boolean value) {
		cfg.setToggle("crosshair_use_drawn", value);
	}

	public static int gridSize(BetterHudsConfig.WidgetConfig cfg) {
		return normalizeGridSize(cfg.intValue("crosshair_grid_size", GRID_LARGE));
	}

	public static void setGridSize(BetterHudsConfig.WidgetConfig cfg, int size) {
		cfg.setIntValue("crosshair_grid_size", normalizeGridSize(size));
	}

	public static int nextGridSize(int current) {
		int normalized = normalizeGridSize(current);
		for (int i = 0; i < GRID_SIZES.length; i++) {
			if (GRID_SIZES[i] == normalized) {
				return GRID_SIZES[(i + 1) % GRID_SIZES.length];
			}
		}
		return GRID_LARGE;
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

	private static int normalizeGridSize(int raw) {
		for (int size : GRID_SIZES) {
			if (raw == size) {
				return size;
			}
		}
		if (raw <= GRID_SMALL) {
			return GRID_SMALL;
		}
		if (raw <= GRID_CENTERED) {
			return GRID_CENTERED;
		}
		if (raw <= GRID_LARGE) {
			return GRID_LARGE;
		}
		return GRID_XL;
	}

	private static String pixelKey(int x, int y) {
		return "cross_px_" + x + "_" + y;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
