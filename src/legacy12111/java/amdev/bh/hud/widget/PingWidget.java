package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class PingWidget implements HudWidget {
	@Override
	public String id() {
		return "ping";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.ping");
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
		String sample = widgetConfig.showText() ? "Ping 999ms" : "999ms";
		return Math.max(28, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		int ping = resolvePing(client);
		String text = widgetConfig.showText() ? ("Ping " + ping + "ms") : (ping + "ms");
		int baseColor = widgetConfig.textColor;
		if (widgetConfig.toggle("ping_colorize", true) && !widgetConfig.toggle("rainbow_text", false)) {
			if (ping >= 200) {
				baseColor = 0xFFFF453A;
			} else if (ping >= 120) {
				baseColor = 0xFFFF9F0A;
			} else {
				baseColor = 0xFF32D74B;
			}
		}
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, baseColor, 211);
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		graphics.drawString(client.font, text, drawX, y + 3, color, false);
	}

	private static int resolvePing(Minecraft client) {
		if (client.player == null) {
			return 0;
		}
		ClientPacketListener connection = client.getConnection();
		if (connection == null) {
			return 0;
		}
		PlayerInfo info = connection.getPlayerInfo(client.player.getUUID());
		return info != null ? info.getLatency() : 0;
	}
}
