package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SpeedWidget implements HudWidget {
	@Override
	public String id() {
		return "speed";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.speed");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 120;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean precise = widgetConfig.toggle("speed_precise", false);
		String sample = widgetConfig.showText() ? (precise ? "Speed 99.999 b/s" : "Speed 99.99 b/s") : (precise ? "99.999" : "99.99");
		return Math.max(28, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		if (client.player == null) {
			return;
		}
		double speed = client.player.getDeltaMovement().horizontalDistance() * 20.0D;
		boolean precise = widgetConfig.toggle("speed_precise", false);
		String format = precise ? "%.3f" : "%.2f";
		String value = String.format(format, speed);
		String text = widgetConfig.showText() ? ("Speed " + value + " b/s") : value;
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 173);
		graphics.drawString(client.font, text, drawX, y + 3, color, false);
	}
}
