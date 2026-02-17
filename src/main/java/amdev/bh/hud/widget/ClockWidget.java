package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockWidget implements HudWidget {
	private static final DateTimeFormatter TIME_24H = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final DateTimeFormatter TIME_12H = DateTimeFormatter.ofPattern("hh:mm:ss a");

	@Override
	public String id() {
		return "clock";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.clock");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 176;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		String sample = widgetConfig.showText() ? "RT 12:34:56 PM  MC 23:59" : "12:34:56 23:59";
		return Math.max(40, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		boolean showReal = widgetConfig.toggle("clock_real", true);
		boolean showGame = widgetConfig.toggle("clock_game", true);
		if (!showReal && !showGame) {
			showReal = true;
		}

		StringBuilder text = new StringBuilder();
		boolean use24h = widgetConfig.toggle("clock_24h", true);
		if (showReal) {
			LocalTime now = LocalTime.now();
			String real = use24h ? now.format(TIME_24H) : now.format(TIME_12H);
			text.append(widgetConfig.showText() ? "RT " : "").append(real);
		}
		if (showGame) {
			if (!text.isEmpty()) {
				text.append(widgetConfig.showText() ? "  " : " ");
			}
			String game = formatGameTime(client);
			if (widgetConfig.showText()) {
				text.append("MC ").append(game);
			} else {
				text.append(game);
			}
		}

		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 809);
		String out = text.toString();
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(out)) / 2);
		graphics.drawString(client.font, out, drawX, y + 3, color, false);
	}

	private static String formatGameTime(Minecraft client) {
		if (client.level == null) {
			return "--:--";
		}
		long dayTime = client.level.getDayTime() % 24000L;
		int hours = (int) ((dayTime / 1000L + 6L) % 24L);
		int minutes = (int) Math.round((dayTime % 1000L) * 60.0D / 1000.0D);
		if (minutes >= 60) {
			minutes = 0;
			hours = (hours + 1) % 24;
		}
		return String.format("%02d:%02d", hours, minutes);
	}
}
