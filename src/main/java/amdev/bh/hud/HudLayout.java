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

		Anchor anchor = widgetConfig.anchor == null ? Anchor.TOP_LEFT : widgetConfig.anchor;
		float anchorX = getAnchorBaseX(anchor, screenWidth, scaledWidthF, scaledReferenceX);
		float anchorY = getAnchorBaseY(anchor, screenHeight, scaledHeightF, scaledReferenceY);

		int x = Math.round(anchorX) + widgetConfig.x;
		int y = Math.round(anchorY) + widgetConfig.y;

		x = Math.max(0, Math.min(Math.max(0, screenWidth - scaledWidth), x));
		y = Math.max(0, Math.min(Math.max(0, screenHeight - scaledHeight), y));

		return new ResolvedWidget(widget, widgetConfig, x, y, widgetWidth, widgetHeight, appliedScale, referenceX, referenceY);
	}

	public static float getAnchorBaseX(Anchor anchor, int screenWidth, float scaledWidthF, float scaledReferenceX) {
		return switch (anchor) {
			case TOP_CENTER, CENTER, BOTTOM_CENTER -> (screenWidth / 2.0F) - scaledReferenceX;
			case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> screenWidth - scaledWidthF;
			default -> 0.0F;
		};
	}

	public static float getAnchorBaseY(Anchor anchor, int screenHeight, float scaledHeightF, float scaledReferenceY) {
		return switch (anchor) {
			case CENTER_LEFT, CENTER, CENTER_RIGHT -> (screenHeight / 2.0F) - scaledReferenceY;
			case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenHeight - scaledHeightF;
			default -> 0.0F;
		};
	}

	public static Anchor getClosestAnchor(int screenX, int screenY, int scaledWidth, int scaledHeight, int screenWidth, int screenHeight) {
		float centerX = screenX + (scaledWidth / 2.0F);
		float centerY = screenY + (scaledHeight / 2.0F);

		float colSplit1 = screenWidth / 3.0F;
		float colSplit2 = (screenWidth * 2.0F) / 3.0F;
		float rowSplit1 = screenHeight / 3.0F;
		float rowSplit2 = (screenHeight * 2.0F) / 3.0F;

		int col = centerX < colSplit1 ? 0 : (centerX > colSplit2 ? 2 : 1);
		int row = centerY < rowSplit1 ? 0 : (centerY > rowSplit2 ? 2 : 1);

		if (row == 0) {
			return col == 0 ? Anchor.TOP_LEFT : (col == 1 ? Anchor.TOP_CENTER : Anchor.TOP_RIGHT);
		} else if (row == 1) {
			return col == 0 ? Anchor.CENTER_LEFT : (col == 1 ? Anchor.CENTER : Anchor.CENTER_RIGHT);
		} else {
			return col == 0 ? Anchor.BOTTOM_LEFT : (col == 1 ? Anchor.BOTTOM_CENTER : Anchor.BOTTOM_RIGHT);
		}
	}

	public static int snap(int value, int gridSize) {
		if (gridSize <= 1) {
			return value;
		}
		return Math.round((float) value / gridSize) * gridSize;
	}
}
