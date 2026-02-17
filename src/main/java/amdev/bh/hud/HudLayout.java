package amdev.bh.hud;

import amdev.bh.config.BetterHudsConfig;

public final class HudLayout {
	private HudLayout() {
	}

	public static ResolvedWidget resolve(HudWidget widget, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig, int screenWidth, int screenHeight, int widgetWidth, int widgetHeight) {
		float appliedScale = Math.max(0.25F, config.globalScale * Math.max(0.25F, widgetConfig.scale));
		if (widgetWidth > 0 && widgetHeight > 0 && screenWidth > 0 && screenHeight > 0) {
			float fitW = (screenWidth - 4) / (float) widgetWidth;
			float fitH = (screenHeight - 4) / (float) widgetHeight;
			float fit = Math.max(0.1F, Math.min(fitW, fitH));
			appliedScale = Math.min(appliedScale, fit);
		}
		int scaledWidth = Math.max(1, Math.round(widgetWidth * appliedScale));
		int scaledHeight = Math.max(1, Math.round(widgetHeight * appliedScale));

		int anchorX;
		int anchorY;

		Anchor anchor = widgetConfig.anchor == null ? Anchor.TOP_LEFT : widgetConfig.anchor;
		switch (anchor) {
			case TOP_LEFT -> {
				anchorX = 0;
				anchorY = 0;
			}
			case TOP_CENTER -> {
				anchorX = (screenWidth - scaledWidth) / 2;
				anchorY = 0;
			}
			case TOP_RIGHT -> {
				anchorX = screenWidth - scaledWidth;
				anchorY = 0;
			}
			case CENTER_LEFT -> {
				anchorX = 0;
				anchorY = (screenHeight - scaledHeight) / 2;
			}
			case CENTER -> {
				anchorX = (screenWidth - scaledWidth) / 2;
				anchorY = (screenHeight - scaledHeight) / 2;
			}
			case CENTER_RIGHT -> {
				anchorX = screenWidth - scaledWidth;
				anchorY = (screenHeight - scaledHeight) / 2;
			}
			case BOTTOM_LEFT -> {
				anchorX = 0;
				anchorY = screenHeight - scaledHeight;
			}
			case BOTTOM_CENTER -> {
				anchorX = (screenWidth - scaledWidth) / 2;
				anchorY = screenHeight - scaledHeight;
			}
			case BOTTOM_RIGHT -> {
				anchorX = screenWidth - scaledWidth;
				anchorY = screenHeight - scaledHeight;
			}
			default -> {
				anchorX = 0;
				anchorY = 0;
			}
		}

		int x = anchorX + widgetConfig.x;
		int y = anchorY + widgetConfig.y;

		if (config.snapToGrid) {
			int grid = config.getGridSizeOrDefault();
			x = snap(x, grid);
			y = snap(y, grid);
		}

		x = Math.max(0, Math.min(Math.max(0, screenWidth - scaledWidth), x));
		y = Math.max(0, Math.min(Math.max(0, screenHeight - scaledHeight), y));

		return new ResolvedWidget(widget, widgetConfig, x, y, widgetWidth, widgetHeight, appliedScale);
	}

	public static int snap(int value, int gridSize) {
		if (gridSize <= 1) {
			return value;
		}
		return Math.round((float) value / gridSize) * gridSize;
	}
}
