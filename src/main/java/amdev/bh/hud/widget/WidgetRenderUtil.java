package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;

public final class WidgetRenderUtil {
	private static volatile int chromaSpeed = 420;

	public static final int[] TEXT_PALETTE = {
		0xFFFFFFFF,
		0xFFB6E3FF,
		0xFFFFE4B5,
		0xFFDAFFC2,
		0xFFFFD7F0,
		0xFFFF9AA2,
		0xFFFFF1A8,
		0xFFA5FFD6
	};
	public static final int[] BACKGROUND_PALETTE = {
		0x00000000,
		0x44000000,
		0x66000000,
		0x88202020,
		0x664A2E00,
		0x663A004A,
		0x663A3A00,
		0x66440026
	};

	private WidgetRenderUtil() {
	}

	public static int lerpColor(float t, int colorA, int colorB) {
		float clamped = Math.max(0.0F, Math.min(1.0F, t));
		int aA = (colorA >>> 24) & 0xFF;
		int rA = (colorA >>> 16) & 0xFF;
		int gA = (colorA >>> 8) & 0xFF;
		int bA = colorA & 0xFF;

		int aB = (colorB >>> 24) & 0xFF;
		int rB = (colorB >>> 16) & 0xFF;
		int gB = (colorB >>> 8) & 0xFF;
		int bB = colorB & 0xFF;

		int a = Math.round(aA + (aB - aA) * clamped);
		int r = Math.round(rA + (rB - rA) * clamped);
		int g = Math.round(gA + (gB - gA) * clamped);
		int b = Math.round(bA + (bB - bA) * clamped);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int durabilityColor(float ratio) {
		float clamped = Math.max(0.0F, Math.min(1.0F, ratio));
		if (clamped < 0.5F) {
			return lerpColor(clamped / 0.5F, 0xFFFF3B30, 0xFFFFD60A);
		}
		return lerpColor((clamped - 0.5F) / 0.5F, 0xFFFFD60A, 0xFF32D74B);
	}

	public static String formatDurationSeconds(long totalSeconds) {
		long seconds = Math.max(0L, totalSeconds);
		long hours = seconds / 3600L;
		long minutes = (seconds % 3600L) / 60L;
		long secs = seconds % 60L;
		if (hours > 0) {
			return String.format("%dh %02dm %02ds", hours, minutes, secs);
		}
		return String.format("%dm %02ds", minutes, secs);
	}

	public static String compactNumber(long value) {
		long abs = Math.abs(value);
		if (abs >= 1_000_000) {
			return String.format("%.1fM", value / 1_000_000.0D);
		}
		if (abs >= 1_000) {
			return String.format("%.1fK", value / 1_000.0D);
		}
		return Long.toString(value);
	}

	public static int nextPaletteColor(int currentColor, int[] palette) {
		if (palette == null || palette.length == 0) {
			return currentColor;
		}

		for (int i = 0; i < palette.length; i++) {
			if (palette[i] == currentColor) {
				return palette[(i + 1) % palette.length];
			}
		}
		return palette[0];
	}

	public static String shortColor(int color) {
		return String.format("#%06X", color & 0xFFFFFF);
	}

	public static int hsvToRgb(float hue, float saturation, float value) {
		float h = normalizeHue(hue);
		float s = clamp01(saturation);
		float v = clamp01(value);

		if (s <= 0.0001F) {
			int gray = Math.round(v * 255.0F);
			return 0xFF000000 | (gray << 16) | (gray << 8) | gray;
		}

		float scaled = h * 6.0F;
		int sector = (int) Math.floor(scaled);
		float fraction = scaled - sector;
		float p = v * (1.0F - s);
		float q = v * (1.0F - s * fraction);
		float t = v * (1.0F - s * (1.0F - fraction));

		float r;
		float g;
		float b;
		switch (sector % 6) {
			case 0 -> {
				r = v;
				g = t;
				b = p;
			}
			case 1 -> {
				r = q;
				g = v;
				b = p;
			}
			case 2 -> {
				r = p;
				g = v;
				b = t;
			}
			case 3 -> {
				r = p;
				g = q;
				b = v;
			}
			case 4 -> {
				r = t;
				g = p;
				b = v;
			}
			default -> {
				r = v;
				g = p;
				b = q;
			}
		}

		int ir = Math.round(r * 255.0F);
		int ig = Math.round(g * 255.0F);
		int ib = Math.round(b * 255.0F);
		return 0xFF000000 | (ir << 16) | (ig << 8) | ib;
	}

	public static float[] rgbToHsv(int color) {
		float r = ((color >>> 16) & 0xFF) / 255.0F;
		float g = ((color >>> 8) & 0xFF) / 255.0F;
		float b = (color & 0xFF) / 255.0F;

		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float delta = max - min;

		float h;
		if (delta < 0.0001F) {
			h = 0.0F;
		} else if (max == r) {
			h = ((g - b) / delta) % 6.0F;
		} else if (max == g) {
			h = ((b - r) / delta) + 2.0F;
		} else {
			h = ((r - g) / delta) + 4.0F;
		}
		h /= 6.0F;
		if (h < 0.0F) {
			h += 1.0F;
		}

		float s = max <= 0.0001F ? 0.0F : delta / max;
		float v = max;
		return new float[]{h, s, v};
	}

	public static int parseHexColor(String raw, int fallback) {
		if (raw == null) {
			return fallback;
		}
		String normalized = raw.trim().toUpperCase();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		if (normalized.length() != 6 && normalized.length() != 8) {
			return fallback;
		}
		for (int i = 0; i < normalized.length(); i++) {
			char c = normalized.charAt(i);
			boolean digit = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
			if (!digit) {
				return fallback;
			}
		}
		if (normalized.length() == 6) {
			int value = Integer.parseInt(normalized, 16);
			return 0xFF000000 | value;
		}
		long value = Long.parseLong(normalized, 16);
		return (int) value;
	}

	public static int widgetTextColor(BetterHudsConfig.WidgetConfig cfg, int defaultColor, int salt) {
		if (cfg != null && cfg.toggle("rainbow_text", false)) {
			return rainbowColor(salt);
		}
		return defaultColor;
	}

	public static int rainbowColor(int salt) {
		double seconds = System.nanoTime() * 1.0E-9D;
		double degreesPerSecond = Math.max(10, chromaSpeed);
		double cycle = (seconds * degreesPerSecond + salt) % 360.0D;
		float hue = (float) (cycle / 360.0D);
		return hsvToRgb(hue, 0.95F, 1.0F);
	}

	public static void setChromaSpeed(int speed) {
		chromaSpeed = Math.max(10, Math.min(800, speed));
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float normalizeHue(float hue) {
		float normalized = hue % 1.0F;
		if (normalized < 0.0F) {
			normalized += 1.0F;
		}
		return normalized;
	}
}
