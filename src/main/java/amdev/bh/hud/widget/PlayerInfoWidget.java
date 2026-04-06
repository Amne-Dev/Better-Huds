package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PlayerInfoWidget implements HudWidget {
	@Override
	public String id() {
		return "player_info";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.player_info");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 220;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 30;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null || client.level == null) {
			return;
		}

		BlockPos pos = player.blockPosition();
		String facing = player.getDirection().getName().toUpperCase();
		String biome = client.level.getBiome(pos).unwrapKey().map(McCompat::resourceKeyPath).orElse("unknown");
		int light = client.level.getMaxLocalRawBrightness(pos);

		Vec3 velocity = player.getDeltaMovement();
		double horizontalSpeed = velocity.horizontalDistance() * 20.0D;
		double verticalSpeed = velocity.y * 20.0D;
		if (!widgetConfig.showText()) {
			return;
		}

		graphics.text(
			client.font,
			String.format("X%.1f Y%.1f Z%.1f %s  B:%s L:%d", player.getX(), player.getY(), player.getZ(), facing, biome, light),
			x,
			y + 2,
			widgetConfig.textColor,
			false
		);
		graphics.text(client.font, String.format("SPD %.2f VY %.2f  T%s", horizontalSpeed, verticalSpeed, WidgetRenderUtil.formatDurationSeconds(McCompat.levelDayTime(client.level) / 20L)), x, y + 15, widgetConfig.textColor, false);
	}
}
