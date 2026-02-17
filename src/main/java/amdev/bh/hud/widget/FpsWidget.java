package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FpsWidget implements HudWidget {
	@Override
	public String id() {
		return "fps";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.fps");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 140;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean extended = widgetConfig.toggle("fps_extended", true);
		String sample = widgetConfig.showText() ? (extended ? "FPS 999 AVG 999 LOW 999" : "FPS 999") : "999";
		return Math.max(28, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		int fps = Math.max(0, client.getFps());
		int avgFps = context.metrics().averageFps();
		int low = context.metrics().onePercentLowFps();
		boolean extended = widgetConfig.toggle("fps_extended", true);
		String text = widgetConfig.showText()
			? (extended ? String.format("FPS %d AVG %d LOW %d", fps, avgFps, low) : String.format("FPS %d", fps))
			: String.format("%d", fps);
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 101);
		graphics.drawString(client.font, text, drawX, y + 3, color, false);
	}
}
