package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LightLayer;

import java.util.Locale;

public class BiomeWidget implements HudWidget {
	@Override
	public String id() {
		return "biome";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.biome");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 190;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 16;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showLight = widgetConfig.toggle("biome_show_light", true);
		String sample = widgetConfig.showText()
			? (showLight ? "Biome Cherry Grove  L B15 S15" : "Biome Cherry Grove")
			: (showLight ? "Cherry Grove B15 S15" : "Cherry Grove");
		return Math.max(40, client.font.width(sample) + 6);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		boolean showLight = widgetConfig.toggle("biome_show_light", true);
		if ((client.player == null || client.level == null) && !context.editorMode()) {
			return;
		}
		String biomeName;
		int blockLight;
		int skyLight;
		if (client.player == null || client.level == null) {
			biomeName = "Cherry Grove";
			blockLight = 12;
			skyLight = 15;
		} else {
			BlockPos pos = client.player.blockPosition();
			biomeName = client.level.getBiome(pos)
				.unwrapKey()
				.map(key -> prettyName(McCompat.resourceKeyPath(key)))
				.orElse("Unknown");
			blockLight = client.level.getBrightness(LightLayer.BLOCK, pos);
			skyLight = client.level.getBrightness(LightLayer.SKY, pos);
		}
		String text;
		if (widgetConfig.showText()) {
			text = showLight
				? String.format("Biome %s  L B%d S%d", biomeName, blockLight, skyLight)
				: String.format("Biome %s", biomeName);
		} else {
			text = showLight
				? String.format("%s B%d S%d", biomeName, blockLight, skyLight)
				: biomeName;
		}
		int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 863);
		int drawX = x + Math.max(0, (getWidth(client, context.config(), widgetConfig) - client.font.width(text)) / 2);
		graphics.text(client.font, text, drawX, y + 3, color, false);
	}

	private static String prettyName(String raw) {
		String[] parts = raw.split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isBlank()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.isEmpty() ? raw : builder.toString();
	}
}
