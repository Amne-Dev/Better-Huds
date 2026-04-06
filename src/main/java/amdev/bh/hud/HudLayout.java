package amdev.bh.hud;

import amdev.bh.config.BetterHudsConfig;
import net.minecraft.client.Minecraft;

public final class HudLayout {
	private HudLayout() {
	}

	public static ResolvedWidget resolve(Minecraft client, HudWidget widget, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig, int screenWidth, int screenHeight, int widgetWidth, int widgetHeight) {
		float appliedScale = Math.max(0.25F, config.globalScale * Math.max(0.25F, widgetConfig.scale));
		if (widgetWidth > 0 && widgetHeight > 0 && screenWidth > 0 && screenHeight > 0) {
			float fitW = (screenWidth - 4) / (float) widgetWidth;
			float fitH = (screenHeight - 4) / (float) widgetHeight;
			float fit = Math.max(0.1F, Math.min(fitW, fitH));
			appliedScale = Math.min(appliedScale, fit);
		}
		float referenceX = widget.centerReferenceX(client, config, widgetConfig);
		float referenceY = widget.centerReferenceY(client, config, widgetConfig);
		float scaledWidthF = widgetWidth * appliedScale;
		float scaledHeightF = widgetHeight * appliedScale;
		float scaledReferenceX = referenceX * appliedScale;
		float scaledReferenceY = referenceY * appliedScale;
		int scaledWidth = Math.max(1, Math.round(widgetWidth * appliedScale));
		int scaledHeight = Math.max(1, Math.round(widgetHeight * appliedScale));

		float anchorX;
		float anchorY;

		Anchor anchor = widgetConfig.anchor == null ? Anchor.TOP_LEFT : widgetConfig.anchor;
		switch (anchor) {
			case TOP_LEFT -> {
				anchorX = 0;
				anchorY = 0;
			}
			case TOP_CENTER -> {
				anchorX = (screenWidth / 2.0F) - scaledReferenceX;
				anchorY = 0;
			}
			case TOP_RIGHT -> {
				anchorX = screenWidth - scaledWidthF;
				anchorY = 0;
			}
			case CENTER_LEFT -> {
				anchorX = 0;
				anchorY = (screenHeight / 2.0F) - scaledReferenceY;
			}
			case CENTER -> {
				anchorX = (screenWidth / 2.0F) - scaledReferenceX;
				anchorY = (screenHeight / 2.0F) - scaledReferenceY;
			}
			case CENTER_RIGHT -> {
				anchorX = screenWidth - scaledWidthF;
				anchorY = (screenHeight / 2.0F) - scaledReferenceY;
			}
			case BOTTOM_LEFT -> {
				anchorX = 0;
				anchorY = screenHeight - scaledHeightF;
			}
			case BOTTOM_CENTER -> {
				anchorX = (screenWidth / 2.0F) - scaledReferenceX;
				anchorY = screenHeight - scaledHeightF;
			}
			case BOTTOM_RIGHT -> {
				anchorX = screenWidth - scaledWidthF;
				anchorY = screenHeight - scaledHeightF;
			}
			default -> {
				anchorX = 0;
				anchorY = 0;
			}
		}

		int x = Math.round(anchorX) + widgetConfig.x;
		int y = Math.round(anchorY) + widgetConfig.y;

		if (config.snapToGrid) {
			int grid = config.getGridSizeOrDefault();
			x = snap(x, grid);
			y = snap(y, grid);
		}

		x = Math.max(0, Math.min(Math.max(0, screenWidth - scaledWidth), x));
		y = Math.max(0, Math.min(Math.max(0, screenHeight - scaledHeight), y));

		return new ResolvedWidget(widget, widgetConfig, x, y, widgetWidth, widgetHeight, appliedScale, referenceX, referenceY);
	}

	public static int snap(int value, int gridSize) {
		if (gridSize <= 1) {
			return value;
		}
		return Math.round((float) value / gridSize) * gridSize;
	}
}
