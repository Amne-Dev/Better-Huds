package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

public class CrosshairWidget implements HudWidget {
	@Override
	public String id() {
		return "crosshair";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.crosshair");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 24;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 24;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		int size = size(widgetConfig);
		return Math.max(8, size);
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		int size = size(widgetConfig);
		return Math.max(8, size);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		boolean invert = widgetConfig.toggle("crosshair_invert", false);
		if (invert) {
			GlStateManager._enableBlend();
			GlStateManager._blendFuncSeparate(
				GL11.GL_ONE_MINUS_DST_COLOR,
				GL11.GL_ZERO,
				GL11.GL_ONE,
				GL11.GL_ZERO
			);
		}
		try {
			if (CrosshairPatternUtil.useDrawnPattern(widgetConfig)) {
				renderDrawnPattern(graphics, widgetConfig, x, y, invert);
				return;
			}

			int thickness = clamp(widgetConfig.intValue("crosshair_thickness", 2), 1, 8);
			int gap = clamp(widgetConfig.intValue("crosshair_gap", 3), 0, 24);
			int length = clamp(widgetConfig.intValue("crosshair_length", 6), 1, 40);
			boolean dot = widgetConfig.toggle("crosshair_dot", true);
			boolean outline = !invert && widgetConfig.toggle("crosshair_outline", true);
			int baseText = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 647);
			int color = invert ? 0xFFFFFFFF : baseText;
			int outlineColor = widgetConfig.backgroundColor == 0 ? 0xB0000000 : widgetConfig.backgroundColor;
			int centerX = x + length + gap;
			int centerY = y + length + gap;

			// Left arm
			drawRect(graphics, centerX - gap - length, centerY, centerX - gap, centerY + thickness, color, outline, outlineColor);
			// Right arm
			drawRect(graphics, centerX + gap + thickness, centerY, centerX + gap + thickness + length, centerY + thickness, color, outline, outlineColor);
			// Top arm
			drawRect(graphics, centerX, centerY - gap - length, centerX + thickness, centerY - gap, color, outline, outlineColor);
			// Bottom arm
			drawRect(graphics, centerX, centerY + gap + thickness, centerX + thickness, centerY + gap + thickness + length, color, outline, outlineColor);

			if (dot) {
				drawRect(graphics, centerX, centerY, centerX + thickness, centerY + thickness, color, outline, outlineColor);
			}
		} finally {
			if (invert) {
				GlStateManager._blendFuncSeparate(
					GL11.GL_SRC_ALPHA,
					GL11.GL_ONE_MINUS_SRC_ALPHA,
					GL11.GL_ONE,
					GL11.GL_ZERO
				);
				GlStateManager._disableBlend();
			}
		}
	}

	private static int size(BetterHudsConfig.WidgetConfig widgetConfig) {
		if (CrosshairPatternUtil.useDrawnPattern(widgetConfig)) {
			return CrosshairPatternUtil.gridSize(widgetConfig) * CrosshairPatternUtil.pixelSize(widgetConfig);
		}
		int thickness = clamp(widgetConfig.intValue("crosshair_thickness", 2), 1, 8);
		int gap = clamp(widgetConfig.intValue("crosshair_gap", 3), 0, 24);
		int length = clamp(widgetConfig.intValue("crosshair_length", 6), 1, 40);
		return (length + gap) * 2 + thickness;
	}

	private static void renderDrawnPattern(GuiGraphics graphics, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y, boolean invert) {
		int grid = CrosshairPatternUtil.gridSize(widgetConfig);
		int pixelSize = CrosshairPatternUtil.pixelSize(widgetConfig);
		boolean outline = !invert && widgetConfig.toggle("crosshair_outline", true);
		int baseText = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 647);
		int color = invert ? 0xFFFFFFFF : baseText;
		int outlineColor = widgetConfig.backgroundColor == 0 ? 0xB0000000 : widgetConfig.backgroundColor;
		for (int py = 0; py < grid; py++) {
			for (int px = 0; px < grid; px++) {
				if (!CrosshairPatternUtil.pixel(widgetConfig, px, py)) {
					continue;
				}
				int left = x + (px * pixelSize);
				int top = y + (py * pixelSize);
				graphics.fill(left, top, left + pixelSize, top + pixelSize, color);
				if (!outline) {
					continue;
				}
				if (!CrosshairPatternUtil.pixel(widgetConfig, px - 1, py)) {
					graphics.fill(left - 1, top - 1, left, top + pixelSize + 1, outlineColor);
				}
				if (!CrosshairPatternUtil.pixel(widgetConfig, px + 1, py)) {
					graphics.fill(left + pixelSize, top - 1, left + pixelSize + 1, top + pixelSize + 1, outlineColor);
				}
				if (!CrosshairPatternUtil.pixel(widgetConfig, px, py - 1)) {
					graphics.fill(left - 1, top - 1, left + pixelSize + 1, top, outlineColor);
				}
				if (!CrosshairPatternUtil.pixel(widgetConfig, px, py + 1)) {
					graphics.fill(left - 1, top + pixelSize, left + pixelSize + 1, top + pixelSize + 1, outlineColor);
				}
			}
		}
	}

	private static void drawRect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color, boolean outline, int outlineColor) {
		if (outline) {
			graphics.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, outlineColor);
		}
		graphics.fill(x1, y1, x2, y2, color);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
