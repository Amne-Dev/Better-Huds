package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.util.McCompat;
import amdev.bh.util.PoseCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
		int size = keySize(widgetConfig, compact);
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 24);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 24);
		return (pad * 2) + (size * 3) + (gap * 2);
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean compact = widgetConfig.toggle("ks_compact", false);
		int size = keySize(widgetConfig, compact);
		int spaceHeight = spaceHeight(size);
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 24);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 24);
		boolean showModifiers = widgetConfig.toggle("ks_show_modifiers", false);
		boolean showMouse = widgetConfig.toggle("ks_show_mouse", true);
		int mouseHeight = showMouse ? size : 0;
		int height = (pad * 2) + size + gap + size + gap + spaceHeight;
		if (showModifiers) {
			height += gap + size;
		}
		if (showMouse) {
			height += gap + mouseHeight;
		}
		return height + 7;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Options options = client.options;
		boolean showText = widgetConfig.showText();
		boolean showCps = widgetConfig.toggle("show_cps", true);
		boolean showMouse = widgetConfig.toggle("ks_show_mouse", true);
		boolean showModifiers = widgetConfig.toggle("ks_show_modifiers", false);
		boolean symbolLabels = widgetConfig.toggle("ks_symbol_labels", false);
		boolean neon = widgetConfig.toggle("ks_neon_style", true);
		boolean compact = widgetConfig.toggle("ks_compact", false);
		boolean keyBgEnabled = widgetConfig.toggle("ks_key_background", true);
		int keyBgColor = widgetConfig.backgroundColor;
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 601);
		int pressedTextColor = widgetConfig.intValue("ks_pressed_text_color", 0xFFFFFFFF);
		int pressedBgColor = widgetConfig.intValue("ks_pressed_bg_color", keyBgColor);
		int size = keySize(widgetConfig, compact);
		int spaceHeight = spaceHeight(size);
		int gap = clamp(widgetConfig.intValue("ks_spacing", compact ? 6 : 8), 0, 24);
		int pad = clamp(widgetConfig.intValue("ks_padding", compact ? 4 : 6), 0, 24);
		int wide = (size * 3) + (gap * 2);
		int rowY = y + pad;
		int leftX = x + pad;
		String wLabel = showText ? (symbolLabels ? "\u25B2" : "W") : "";
		String aLabel = showText ? (symbolLabels ? "\u25C0" : "A") : "";
		String sLabel = showText ? (symbolLabels ? "\u25BC" : "S") : "";
		String dLabel = showText ? (symbolLabels ? "\u25B6" : "D") : "";

		int centerX = leftX + size + gap;
		drawKey(graphics, client, centerX, rowY, size, size, wLabel, null, false, keyDown(options.keyUp), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		rowY += size + gap;

		drawKey(graphics, client, leftX, rowY, size, size, aLabel, null, false, keyDown(options.keyLeft), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		drawKey(graphics, client, centerX, rowY, size, size, sLabel, null, false, keyDown(options.keyDown), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		drawKey(graphics, client, leftX + (2 * (size + gap)), rowY, size, size, dLabel, null, false, keyDown(options.keyRight), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		rowY += size + gap;

		boolean spaceLine = showText && symbolLabels;
		String spaceLabel = showText ? (symbolLabels ? "" : "SPACE") : "";
		drawKey(graphics, client, leftX, rowY, wide, spaceHeight, spaceLabel, null, spaceLine, keyDown(options.keyJump), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		rowY += spaceHeight;

		int halfWidth = (wide - gap) / 2;
		if (showModifiers) {
			rowY += gap;
			drawKey(graphics, client, leftX, rowY, halfWidth, size, showText ? "SHIFT" : "", null, false, keyDown(options.keyShift), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
			drawKey(graphics, client, leftX + halfWidth + gap, rowY, halfWidth, size, showText ? "CTRL" : "", null, false, McCompat.isControlDown(client), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
			rowY += size;
		}

		if (!showMouse) {
			return;
		}
		rowY += gap;
		int mouseHeight = size;
		String lSub = showText && showCps ? context.metrics().leftCps() + " CPS" : null;
		String rSub = showText && showCps ? context.metrics().rightCps() + " CPS" : null;
		drawKey(graphics, client, leftX, rowY, halfWidth, mouseHeight, showText ? "LMB" : "", lSub, false, keyDown(options.keyAttack), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
		drawKey(graphics, client, leftX + halfWidth + gap, rowY, halfWidth, mouseHeight, showText ? "RMB" : "", rSub, false, keyDown(options.keyUse), neon, keyBgEnabled, keyBgColor, pressedBgColor, textColor, pressedTextColor);
	}

	private static boolean keyDown(net.minecraft.client.KeyMapping mapping) {
		return mapping != null && mapping.isDown();
	}

	private static void drawKey(
		GuiGraphicsExtractor graphics,
		Minecraft client,
		int x,
		int y,
		int width,
		int height,
		String label,
		String subLabel,
		boolean drawSpaceLine,
		boolean pressed,
		boolean neon,
		boolean keyBgEnabled,
		int keyBgColor,
		int pressedBgColor,
		int textColor,
		int pressedTextColor
	) {
		int border;
		int outerFill = 0x00000000;
		int innerFill = 0x00000000;
		int accent = 0x00000000;
		boolean drawGloss = false;
		if (!keyBgEnabled) {
			border = neon ? (pressed ? 0xFFE0E0E0 : 0xAA8A8A8A) : 0x00000000;
		} else if (neon) {
			border = pressed ? 0xFF86F5FF : 0xAA6D7884;
			if (pressed) {
				outerFill = tint(pressedBgColor, 1.14F, 0xCE);
				innerFill = tint(pressedBgColor, 1.28F, 0xD8);
			} else {
				outerFill = tint(keyBgColor, 0.84F, 0x80);
				innerFill = tint(keyBgColor, 0.98F, 0x94);
			}
			accent = pressed ? 0xD0A1FCFF : 0x6A7BD7ED;
			drawGloss = true;
		} else {
			border = 0x00000000;
			outerFill = pressed ? tint(pressedBgColor, 1.0F, 0xD0) : tint(keyBgColor, 1.0F, 0xB8);
		}
		int text = pressed ? pressedTextColor : textColor;
		String safeLabel = label == null ? "" : label;
		String safeSubLabel = subLabel == null ? "" : subLabel;

		int frame = (border >>> 24) != 0 ? 1 : 0;
		if (frame == 1) {
			graphics.fill(x, y, x + width, y + height, border);
		}
		if (outerFill != 0) {
			graphics.fill(x + frame, y + frame, x + width - frame, y + height - frame, outerFill);
		}
		if (innerFill != 0) {
			graphics.fill(x + frame + 1, y + frame + 1, x + width - frame - 1, y + height - frame - 1, innerFill);
		}
		if ((accent >>> 24) != 0) {
			graphics.fill(x + frame + 1, y + frame + 1, x + frame + 3, y + height - frame - 1, accent);
		}
		if (drawGloss) {
			graphics.fill(x + frame + 1, y + frame + 1, x + width - frame - 1, y + frame + 2, pressed ? 0xAAFFFFFF : 0x55FFFFFF);
			graphics.fill(x + frame + 1, y + height - frame - 2, x + width - frame - 1, y + height - frame - 1, 0x55000000);
		}

		if (drawSpaceLine) {
			int lineWidth = Math.max(12, Math.round(width * 0.48F));
			int lineHeight = Math.max(2, height / 9);
			int lineX = x + (width - lineWidth) / 2;
			int lineY = y + (height - lineHeight) / 2;
			graphics.fill(lineX, lineY, lineX + lineWidth, lineY + lineHeight, text);
			return;
		}

		if (safeSubLabel.isEmpty()) {
			String clipped = client.font.plainSubstrByWidth(safeLabel, Math.max(4, width - 4));
			int tx = x + (width - client.font.width(clipped)) / 2;
			int ty = y + (height - client.font.lineHeight) / 2;
			graphics.text(client.font, clipped, tx, ty, text, false);
			return;
		}

		String top = client.font.plainSubstrByWidth(safeLabel, Math.max(4, width - 4));
		String bottom = client.font.plainSubstrByWidth(safeSubLabel, Math.max(4, width - 4));
		int topX = x + (width - client.font.width(top)) / 2;
		int topY = y + 2;
		float subScale = 0.72F;
		int bottomTextWidth = Math.round(client.font.width(bottom) * subScale);
		int bottomX = x + (width - bottomTextWidth) / 2;
		int bottomY = y + height - Math.round(client.font.lineHeight * subScale) - 4;
		graphics.text(client.font, top, topX, topY, text, false);
		var pose = graphics.pose();
		PoseCompat.push(pose);
		PoseCompat.translate(pose, bottomX, bottomY);
		PoseCompat.scale(pose, subScale, subScale);
		graphics.text(client.font, bottom, 0, 0, text, false);
		PoseCompat.pop(pose);
	}

	private static int keySize(BetterHudsConfig.WidgetConfig widgetConfig, boolean compact) {
		int fallback = compact ? 18 : 22;
		return clamp(widgetConfig.intValue("ks_key_size", fallback), 14, 34);
	}

	private static int spaceHeight(int size) {
		return Math.max(8, Math.round(size * 0.55F));
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
