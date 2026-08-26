package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SprintStatusWidget implements HudWidget {
	@Override
	public String id() {
		return "sprint_status";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.sprint_status");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 130;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showSneak = widgetConfig.toggle("sprint_show_sneak", true);
		String sample;
		if (widgetConfig.showText()) {
			sample = showSneak ? "Sprint ON  Sneak ON" : "Sprint ON";
		} else {
			sample = showSneak ? "SP SN" : "SP";
		}
		return Math.max(28, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		if (client.player == null) {
			return;
		}
		boolean sprint = client.player.isSprinting();
		boolean sneak = client.player.isCrouching();
		boolean showSneak = widgetConfig.toggle("sprint_show_sneak", true);
		String text;
		if (widgetConfig.showText()) {
			text = showSneak
				? ("Sprint " + (sprint ? "ON" : "OFF") + "  Sneak " + (sneak ? "ON" : "OFF"))
				: ("Sprint " + (sprint ? "ON" : "OFF"));
		} else {
			text = showSneak
				? ((sprint ? "SP" : "sp") + " " + (sneak ? "SN" : "sn"))
				: (sprint ? "SP" : "sp");
		}
		int baseColor = sprint ? 0xFF32D74B : widgetConfig.textColor;
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, baseColor, 461);
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		graphics.drawString(client.font, text, drawX, y + 3, color, false);
	}
}
