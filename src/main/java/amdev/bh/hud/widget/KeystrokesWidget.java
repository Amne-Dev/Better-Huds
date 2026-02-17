package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class KeystrokesWidget implements HudWidget {
	@Override
	public String id() {
		return "keystrokes";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.keystrokes");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 74;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 94;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean compact = widgetConfig.toggle("ks_compact", false);
		int size = compact ? 18 : 22;
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 20);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 20);
		return (pad * 2) + (size * 3) + (gap * 2);
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean compact = widgetConfig.toggle("ks_compact", false);
		int size = compact ? 18 : 22;
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 20);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 20);
		int rows = 4;
		boolean showMouse = widgetConfig.toggle("ks_show_mouse", true);
		if (showMouse) {
			rows++;
		}
		int cpsRows = (showMouse && widgetConfig.showText() && widgetConfig.toggle("show_cps", true)) ? 1 : 0;
		return (pad * 2) + (rows * size) + ((rows - 1) * gap) + (cpsRows * (client.font.lineHeight + 2));
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Options options = client.options;
		boolean showText = widgetConfig.showText();
		boolean showCps = widgetConfig.toggle("show_cps", true);
		boolean showMouse = widgetConfig.toggle("ks_show_mouse", true);
		boolean neon = widgetConfig.toggle("ks_neon_style", true);
		boolean compact = widgetConfig.toggle("ks_compact", false);
		boolean keyBgEnabled = widgetConfig.toggle("ks_key_background", true);
		int keyBgColor = widgetConfig.backgroundColor;
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 601);
		int size = compact ? 18 : 22;
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 20);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 20);
		int wide = (size * 3) + (gap * 2);
		int rowY = y + pad;
		int leftX = x + pad;

		int centerX = leftX + size + gap;
		drawKey(graphics, client, centerX, rowY, size, size, showText ? "W" : "", options.keyUp.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		rowY += size + gap;

		drawKey(graphics, client, leftX, rowY, size, size, showText ? "A" : "", options.keyLeft.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		drawKey(graphics, client, centerX, rowY, size, size, showText ? "S" : "", options.keyDown.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		drawKey(graphics, client, leftX + (2 * (size + gap)), rowY, size, size, showText ? "D" : "", options.keyRight.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		rowY += size + gap;

		drawKey(graphics, client, leftX, rowY, wide, size, showText ? "SPACE" : "", options.keyJump.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		rowY += size + gap;

		int halfWidth = (wide - gap) / 2;
		drawKey(graphics, client, leftX, rowY, halfWidth, size, showText ? "SHIFT" : "", options.keyShift.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		drawKey(graphics, client, leftX + halfWidth + gap, rowY, halfWidth, size, showText ? "CTRL" : "", client.hasControlDown(), neon, keyBgEnabled, keyBgColor, textColor);
		rowY += size + gap;

		if (!showMouse) {
			return;
		}
		drawKey(graphics, client, leftX, rowY, halfWidth, size, showText ? "LMB" : "", options.keyAttack.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		drawKey(graphics, client, leftX + halfWidth + gap, rowY, halfWidth, size, showText ? "RMB" : "", options.keyUse.isDown(), neon, keyBgEnabled, keyBgColor, textColor);
		rowY += size;
		if (showText && showCps) {
			int cpsColor = neon ? 0xFF90EAF8 : textColor;
			String leftCps = context.metrics().leftCps() + " CPS";
			String rightCps = context.metrics().rightCps() + " CPS";
			int leftCpsX = leftX + (halfWidth - client.font.width(leftCps)) / 2;
			int rightCpsX = leftX + halfWidth + gap + (halfWidth - client.font.width(rightCps)) / 2;
			graphics.drawString(client.font, leftCps, leftCpsX, rowY + 2, cpsColor, false);
			graphics.drawString(client.font, rightCps, rightCpsX, rowY + 2, cpsColor, false);
		}
	}

	private static void drawKey(GuiGraphics graphics, Minecraft client, int x, int y, int width, int height, String label, boolean pressed, boolean neon, boolean keyBgEnabled, int keyBgColor, int textColor) {
		int border = pressed ? (neon ? 0xFF7AF9E3 : 0xFFFFFFFF) : (neon ? 0xAA4E7D92 : 0x66FFFFFF);
		int fill;
		if (!keyBgEnabled) {
			fill = pressed ? (neon ? 0x44358993 : 0x33FFFFFF) : 0x00000000;
		} else if (pressed) {
			fill = tint(keyBgColor, neon ? 1.25F : 1.15F, 0xC0);
		} else {
			fill = tint(keyBgColor, 0.92F, 0x8E);
		}
		int topLight = pressed ? (neon ? 0xAA99FFF4 : 0xAAFFFFFF) : 0x55FFFFFF;
		int text = pressed ? 0xFFFFFFFF : textColor;

		graphics.fill(x, y, x + width, y + height, border);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
		graphics.fill(x + 1, y + 1, x + width - 1, y + 2, topLight);
		graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0x44000000);

		String clipped = client.font.plainSubstrByWidth(label, width - 4);
		int tx = x + (width - client.font.width(clipped)) / 2;
		int ty = y + (height - client.font.lineHeight) / 2;
		graphics.drawString(client.font, clipped, tx, ty, text, false);
	}

	private static int tint(int color, float brightness, int alpha) {
		int r = Math.min(255, Math.max(0, Math.round(((color >> 16) & 0xFF) * brightness)));
		int g = Math.min(255, Math.max(0, Math.round(((color >> 8) & 0xFF) * brightness)));
		int b = Math.min(255, Math.max(0, Math.round((color & 0xFF) * brightness)));
		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
