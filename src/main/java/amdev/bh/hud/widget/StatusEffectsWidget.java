package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StatusEffectsWidget implements HudWidget {

	@Override
	public String id() {
		return "status_effects";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.status_effects");
	}

	@Override
	public boolean shouldRender(Minecraft client) {
		return client.player != null && !client.player.getActiveEffects().isEmpty();
	}

	@Override
	public int getWidth(Minecraft client) {
		return 180;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 94;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showText = widgetConfig.showText();
		boolean showIcons = widgetConfig.toggle("effects_icons", true);
		if (!showText && !showIcons) {
			return 8;
		}
		return showIcons ? 194 : 176;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showText = widgetConfig.showText();
		boolean showIcons = widgetConfig.toggle("effects_icons", true);
		if (!showText && !showIcons) {
			return 8;
		}
		if (client.player == null) {
			return showIcons ? 20 : 16;
		}
		int effects = client.player.getActiveEffects().size();
		int maxLines = widgetConfig.toggle("compact_mode", true) ? 3 : 6;
		int lines = Math.min(Math.max(1, effects), maxLines);
		if (effects > maxLines) {
			lines++;
		}
		int rowHeight = showIcons ? 18 : 14;
		return (lines * rowHeight) + 4;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null && !context.editorMode()) {
			return;
		}

		List<MobEffectInstance> effects = player == null ? new ArrayList<>() : new ArrayList<>(player.getActiveEffects());
		effects.sort(Comparator.comparingInt(MobEffectInstance::getDuration).reversed());
		boolean showText = widgetConfig.showText();
		boolean showIcons = widgetConfig.toggle("effects_icons", true);
		boolean hideTime = widgetConfig.toggle("effects_hide_time", false);
		boolean alignRight = widgetConfig.toggle("align_right", false);
		int widgetWidth = getWidth(client, context.config(), widgetConfig);
		if (!showText && !showIcons) {
			return;
		}

		if (effects.isEmpty()) {
			if (context.editorMode() && showText) {
				String empty = Component.translatable("widget.better-huds.no_effects").getString();
				int emptyX = alignRight ? x + Math.max(0, widgetWidth - client.font.width(empty)) : x;
				graphics.text(client.font, empty, emptyX, y + 4, 0xFFAAAAAA, false);
			}
			return;
		}

		int maxLines = widgetConfig.toggle("compact_mode", true) ? 3 : 6;
		int rowHeight = showIcons ? 18 : 14;
		int drawn = 0;
		for (MobEffectInstance effect : effects) {
			if (drawn >= maxLines) {
				break;
			}

			int lineY = y + (drawn * rowHeight);
			int iconX = alignRight ? x + Math.max(0, widgetWidth - 16) : x;
			if (showIcons) {
				drawEffectIcon(graphics, client, effect, iconX, lineY);
			}

			if (!showText) {
				drawn++;
				continue;
			}

			String name = effect.getEffect().value().getDisplayName().getString();
			int level = effect.getAmplifier() + 1;
			String duration = hideTime ? "" : MobEffectUtil.formatDuration(effect, 1.0F, 1.0F).getString();
			String suffix = hideTime ? "" : " " + duration;
			String text = level > 1 ? name + " " + level + suffix : name + suffix;
			int color = 0xFF000000 | effect.getEffect().value().getColor();
			if (widgetConfig.toggle("rainbow_text", false)) {
				color = WidgetRenderUtil.rainbowColor(773 + drawn * 13);
			}
			int maxWidth = showIcons ? Math.max(10, widgetWidth - 20) : Math.max(10, widgetWidth);
			if (showIcons && alignRight) {
				maxWidth = Math.max(10, iconX - x - 4);
			}
			String clipped = client.font.plainSubstrByWidth(text, maxWidth);
			int textX;
			if (alignRight) {
				int rightEdge = showIcons ? (iconX - 4) : (x + widgetWidth);
				textX = rightEdge - client.font.width(clipped);
			} else {
				textX = x + (showIcons ? 20 : 0);
			}
			graphics.text(client.font, clipped, textX, lineY + (showIcons ? 4 : 0), color, false);
			drawn++;
		}

		if (effects.size() > maxLines && showText) {
			int remaining = effects.size() - maxLines;
			String text = "+" + remaining + " more";
			int textX;
			if (alignRight) {
				textX = x + Math.max(0, widgetWidth - client.font.width(text));
			} else {
				textX = x + (showIcons ? 20 : 0);
			}
			graphics.text(client.font, text, textX, y + (maxLines * rowHeight), 0xFFAAAAAA, false);
		}
	}

	private static void drawEffectIcon(GuiGraphicsExtractor graphics, Minecraft client, MobEffectInstance effect, int x, int y) {
		int color = 0xFF000000 | effect.getEffect().value().getColor();
		graphics.fill(x, y, x + 16, y + 16, 0xAA000000);
		graphics.fill(x + 1, y + 1, x + 15, y + 15, color);
		String label = effect.getEffect().value().getDisplayName().getString();
		String shortLabel = label.isBlank() ? "?" : label.substring(0, 1).toUpperCase();
		int textX = x + (16 - client.font.width(shortLabel)) / 2;
		graphics.text(client.font, shortLabel, textX, y + 4, 0xFFFFFFFF, false);
	}
}
