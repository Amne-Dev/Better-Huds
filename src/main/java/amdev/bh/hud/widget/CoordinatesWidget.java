package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CoordinatesWidget implements HudWidget {
	@Override
	public String id() {
		return "coordinates";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.coordinates");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 180;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean decimals = widgetConfig.toggle("coords_decimals", true);
		String sample;
		if (widgetConfig.showText()) {
			sample = decimals ? "X -9999.9  Y 999.9  Z -9999.9" : "X -9999  Y 999  Z -9999";
		} else {
			sample = decimals ? "-9999.9 999.9 -9999.9" : "-9999 999 -9999";
		}
		return Math.max(28, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		boolean decimals = widgetConfig.toggle("coords_decimals", true);
		String text;
		if (client.player == null) {
			if (!context.editorMode()) {
				return;
			}
			text = widgetConfig.showText()
				? (decimals ? "X 123.4  Y 64.0  Z -987.6" : "X 123  Y 64  Z -988")
				: (decimals ? "123.4 64.0 -987.6" : "123 64 -988");
		} else if (widgetConfig.showText()) {
			text = decimals
				? String.format("X %.1f  Y %.1f  Z %.1f", client.player.getX(), client.player.getY(), client.player.getZ())
				: String.format("X %.0f  Y %.0f  Z %.0f", client.player.getX(), client.player.getY(), client.player.getZ());
		} else {
			text = decimals
				? String.format("%.1f %.1f %.1f", client.player.getX(), client.player.getY(), client.player.getZ())
				: String.format("%.0f %.0f %.0f", client.player.getX(), client.player.getY(), client.player.getZ());
		}
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 131);
		graphics.text(client.font, text, drawX, y + 3, color, false);
	}
}
