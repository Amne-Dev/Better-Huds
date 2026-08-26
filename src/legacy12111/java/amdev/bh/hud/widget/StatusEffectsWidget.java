package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StatusEffectsWidget implements HudWidget {
	private static final int ICON_SIZE = 18;
	private static final int ICON_GAP = 4;
	private static final int TEXT_WIDTH = 112;
	private static final int ROW_HEIGHT = 22;

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
		if (!showText) {
			return ICON_SIZE;
		}
		return showIcons ? ICON_SIZE + ICON_GAP + TEXT_WIDTH : TEXT_WIDTH;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showText = widgetConfig.showText();
		boolean showIcons = widgetConfig.toggle("effects_icons", true);
		if (!showText && !showIcons) {
			return 8;
		}
		if (client.player == null) {
			return ROW_HEIGHT;
		}
		int effects = client.player.getActiveEffects().size();
		int maxLines = widgetConfig.toggle("compact_mode", true) ? 3 : 6;
		int lines = Math.min(Math.max(1, effects), maxLines);
		if (effects > maxLines) {
			lines++;
		}
		return lines * ROW_HEIGHT;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null) {
			return;
		}

		List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());
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
				graphics.drawString(client.font, empty, emptyX, y + 4, 0xFFAAAAAA, false);
			}
			return;
		}

		int maxLines = widgetConfig.toggle("compact_mode", true) ? 3 : 6;
		int drawn = 0;
		for (MobEffectInstance effect : effects) {
			if (drawn >= maxLines) {
				break;
			}

			int lineY = y + (drawn * ROW_HEIGHT);
			int iconX = alignRight ? x + Math.max(0, widgetWidth - ICON_SIZE) : x;
			if (showIcons) {
				drawEffectIcon(graphics, effect, iconX, lineY + 1);
			}

			if (!showText) {
				drawn++;
				continue;
			}

			String name = effect.getEffect().value().getDisplayName().getString();
			String duration = hideTime ? "" : MobEffectUtil.formatDuration(effect, 1.0F, 20.0F).getString();
			String title = name + " " + amplifierLabel(effect.getAmplifier());
			int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 773 + drawn * 13);
			int textLeft = alignRight ? x : x + (showIcons ? ICON_SIZE + ICON_GAP : 0);
			int textRight = alignRight && showIcons ? iconX - ICON_GAP : x + widgetWidth;
			int maxWidth = Math.max(10, textRight - textLeft);
			String clippedTitle = client.font.plainSubstrByWidth(title, maxWidth);
			int titleX = alignRight ? textRight - client.font.width(clippedTitle) : textLeft;
			int titleY = lineY + (hideTime ? 6 : 1);
			graphics.drawString(client.font, clippedTitle, titleX, titleY, color, true);
			if (!hideTime) {
				String clippedDuration = client.font.plainSubstrByWidth(duration, maxWidth);
				int durationX = alignRight ? textRight - client.font.width(clippedDuration) : textLeft;
				graphics.drawString(client.font, clippedDuration, durationX, lineY + 11, color, true);
			}
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
			graphics.drawString(client.font, text, textX, y + (maxLines * ROW_HEIGHT) + 5, 0xFFAAAAAA, true);
		}
	}

	private static void drawEffectIcon(GuiGraphics graphics, MobEffectInstance effect, int x, int y) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(effect.getEffect()), x, y, ICON_SIZE, ICON_SIZE);
	}

	private static String amplifierLabel(int amplifier) {
		return switch (amplifier + 1) {
			case 1 -> "I";
			case 2 -> "II";
			case 3 -> "III";
			case 4 -> "IV";
			case 5 -> "V";
			case 6 -> "VI";
			case 7 -> "VII";
			case 8 -> "VIII";
			case 9 -> "IX";
			case 10 -> "X";
			default -> Integer.toString(amplifier + 1);
		};
	}
}
