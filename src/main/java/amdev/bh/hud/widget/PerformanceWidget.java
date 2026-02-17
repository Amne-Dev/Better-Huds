package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public class PerformanceWidget implements HudWidget {
	@Override
	public String id() {
		return "performance";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.performance");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 210;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 30;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		int fps = Math.max(0, client.getFps());
		int avgFps = context.metrics().averageFps();
		int onePercentLow = context.metrics().onePercentLowFps();
		int ping = resolvePing(client);
		float tps = client.level != null ? client.level.tickRateManager().tickrate() : 0.0F;

		long usedMemMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L);
		long maxMemMb = Runtime.getRuntime().maxMemory() / (1024L * 1024L);
		double gpuUtil = client.getGpuUtilization() * 100.0D;
		if (!widgetConfig.showText()) {
			int barWidth = 204;
			graphics.fill(x, y + 2, x + barWidth, y + 6, 0x33000000);
			graphics.fill(x, y + 10, x + barWidth, y + 14, 0x33000000);
			graphics.fill(x, y + 18, x + barWidth, y + 22, 0x33000000);
			graphics.fill(x, y + 2, x + Math.round(barWidth * Math.min(1.0F, fps / 240.0F)), y + 6, 0xFF32D74B);
			graphics.fill(x, y + 10, x + Math.round(barWidth * Math.min(1.0F, usedMemMb / (float) Math.max(1L, maxMemMb))), y + 14, 0xFFFFD60A);
			graphics.fill(x, y + 18, x + Math.round(barWidth * Math.min(1.0F, (float) (gpuUtil / 100.0D))), y + 22, 0xFF64D2FF);
			return;
		}

		graphics.drawString(client.font, String.format("FPS %d A%d L%d  P%d  T%.1f", fps, avgFps, onePercentLow, ping, tps), x, y + 2, widgetConfig.textColor, false);
		graphics.drawString(client.font, String.format("M%d/%d GPU%.0f%% S%s", usedMemMb, maxMemMb, gpuUtil, WidgetRenderUtil.formatDurationSeconds(context.metrics().sessionSeconds())), x, y + 15, widgetConfig.textColor, false);
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
