package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class DirectionWidget implements HudWidget {
	@Override
	public String id() {
		return "direction";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.direction");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 182;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 22;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		if (client.player == null && !context.editorMode()) {
			return;
		}
		float yaw = normalizeYaw(client.player == null ? 180.0F : client.player.getYRot());
		int width = getWidth(client) - 2;
		int centerX = x + width / 2;
		int lineY = y + 14;
		boolean showLabels = widgetConfig.toggle("direction_labels", true);
		int baseColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 263);
		graphics.fill(x + 4, lineY, x + width - 4, lineY + 1, 0x66FFFFFF);
		graphics.fill(centerX, y + 3, centerX + 1, y + 19, 0xFFFFFFFF);

		drawMarker(graphics, client, centerX, y, yaw, 180.0F, showLabels ? "N" : "", 0xFFFF453A, baseColor);
		drawMarker(graphics, client, centerX, y, yaw, 270.0F, showLabels ? "E" : "", baseColor, baseColor);
		drawMarker(graphics, client, centerX, y, yaw, 0.0F, showLabels ? "S" : "", baseColor, baseColor);
		drawMarker(graphics, client, centerX, y, yaw, 90.0F, showLabels ? "W" : "", baseColor, baseColor);

		if (widgetConfig.showText() && showLabels) {
			String text = Component.translatable("widget.better-huds.direction_compass").getString();
			int drawX = x + Math.max(0, (getWidth(client) - client.font.width(text)) / 2);
			graphics.text(client.font, text, drawX, y + 2, baseColor, false);
		}
	}

	private static float normalizeYaw(float yaw) {
		float normalized = yaw % 360.0F;
		if (normalized < 0.0F) {
			normalized += 360.0F;
		}
		return normalized;
	}

	private static void drawMarker(GuiGraphicsExtractor graphics, Minecraft client, int centerX, int baseY, float yaw, float markerYaw, String label, int mainColor, int secondaryColor) {
		float diff = wrapDegrees(markerYaw - yaw);
		float pixelsPerDegree = 1.0F;
		int markerX = centerX + Math.round(diff * pixelsPerDegree);
		if (Math.abs(markerX - centerX) > 90) {
			return;
		}
		graphics.fill(markerX, baseY + 12, markerX + 1, baseY + 18, secondaryColor);
		graphics.text(client.font, label, markerX - 2, baseY + 4, mainColor, false);
	}

	private static float wrapDegrees(float value) {
		float wrapped = value % 360.0F;
		if (wrapped >= 180.0F) {
			wrapped -= 360.0F;
		}
		if (wrapped < -180.0F) {
			wrapped += 360.0F;
		}
		return wrapped;
	}
}
