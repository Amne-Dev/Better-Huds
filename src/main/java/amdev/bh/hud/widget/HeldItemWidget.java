package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HeldItemWidget implements HudWidget {
	public static boolean handsEnabled(BetterHudsConfig config) {
		return config.getOrCreateWidgetConfig("armor").toggle("show_hands", true);
	}

	public static boolean isSeparate(BetterHudsConfig config) {
		return handsEnabled(config) && config.getOrCreateWidgetConfig("armor").toggle("separate_hands", false);
	}

	public static boolean showHandText(BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return widgetConfig.showText() && config.getOrCreateWidgetConfig("armor").toggle("show_hand_text", true);
	}

	public static int sectionWidth(boolean showText) {
		return showText ? 170 : 44;
	}

	public static int sectionHeight() {
		return 38;
	}

	@Override
	public String id() {
		return "held_item";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.held_item");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 170;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 38;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return sectionWidth(showHandText(config, widgetConfig));
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return sectionHeight();
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config) {
		return client.player != null && isSeparate(config);
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (!isSeparate(context.config())) {
			return;
		}
		if (player == null) {
			if (context.editorMode()) {
				renderPreviewHands(graphics, client, context.config(), widgetConfig, x, y, sectionWidth(showHandText(context.config(), widgetConfig)), false);
			}
			return;
		}

		renderHands(graphics, client, context.config(), widgetConfig, x, y, player);
	}

	public static void renderHands(GuiGraphicsExtractor graphics, Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y, Player player) {
		boolean showText = showHandText(config, widgetConfig);
		int width = sectionWidth(showText);
		renderHandsAligned(graphics, client, config, widgetConfig, x, y, width, false, player);
	}

	public static void renderHandsAligned(
		GuiGraphicsExtractor graphics,
		Minecraft client,
		BetterHudsConfig config,
		BetterHudsConfig.WidgetConfig widgetConfig,
		int x,
		int y,
		int lineWidth,
		boolean alignRight,
		Player player
	) {
		boolean showText = showHandText(config, widgetConfig);
		drawHandLine(graphics, client, widgetConfig, x, y, lineWidth, alignRight, Component.translatable("widget.better-huds.mainhand"), player.getMainHandItem(), showText);
		drawHandLine(graphics, client, widgetConfig, x, y + 18, lineWidth, alignRight, Component.translatable("widget.better-huds.offhand"), player.getOffhandItem(), showText);
	}

	public static void renderPreviewHands(
		GuiGraphicsExtractor graphics,
		Minecraft client,
		BetterHudsConfig config,
		BetterHudsConfig.WidgetConfig widgetConfig,
		int x,
		int y,
		int lineWidth,
		boolean alignRight
	) {
		boolean showText = showHandText(config, widgetConfig);
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 389);
		drawPreviewLine(graphics, client, x, y, lineWidth, alignRight, Component.translatable("widget.better-huds.mainhand").getString(), showText, textColor, "Sword");
		drawPreviewLine(graphics, client, x, y + 18, lineWidth, alignRight, Component.translatable("widget.better-huds.offhand").getString(), showText, textColor, "Shield");
	}

	private static void drawHandLine(
		GuiGraphicsExtractor graphics,
		Minecraft client,
		BetterHudsConfig.WidgetConfig widgetConfig,
		int x,
		int y,
		int lineWidth,
		boolean alignRight,
		Component label,
		ItemStack stack,
		boolean showText
	) {
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 389);
		int iconX = alignRight ? x + Math.max(0, lineWidth - 16) : x;
		if (showText) {
			if (!alignRight) {
				graphics.text(client.font, label, x, y + 5, textColor, false);
				iconX = x + 22;
			} else {
				String labelText = client.font.plainSubstrByWidth(label.getString(), Math.max(10, lineWidth - 24));
				int labelX = iconX - 4 - client.font.width(labelText);
				graphics.text(client.font, labelText, labelX, y + 5, textColor, false);
			}
		}

		if (stack.isEmpty()) {
			if (showText) {
				String empty = Component.translatable("widget.better-huds.empty").getString();
				if (!alignRight) {
					graphics.text(client.font, empty, iconX + 20, y + 5, 0xFFAAAAAA, false);
				} else {
					int emptyX = Math.max(x, iconX - 8 - client.font.width(empty));
					graphics.text(client.font, empty, emptyX, y + 5, 0xFFAAAAAA, false);
				}
			}
			return;
		}

		graphics.item(stack, iconX, y);
		graphics.itemDecorations(client.font, stack, iconX, y);
		if (!showText) {
			return;
		}

		if (!alignRight) {
			String itemName = client.font.plainSubstrByWidth(stack.getHoverName().getString(), 82);
			graphics.text(client.font, itemName, iconX + 20, y + 1, textColor, false);

			if (stack.isDamageableItem()) {
				int max = Math.max(1, stack.getMaxDamage());
				int remaining = Math.max(0, max - stack.getDamageValue());
				float ratio = remaining / (float) max;
				String detail = Math.round(ratio * 100.0F) + "%";
				graphics.text(client.font, detail, iconX + 20, y + 10, WidgetRenderUtil.durabilityColor(ratio), false);
			} else if (stack.getCount() > 1) {
				graphics.text(client.font, "x" + stack.getCount(), iconX + 20, y + 10, textColor, false);
			}
			return;
		}

		int textRight = iconX - 8;
		int available = Math.max(12, textRight - x);
		String itemName = client.font.plainSubstrByWidth(stack.getHoverName().getString(), available);
		int itemX = textRight - client.font.width(itemName);
		graphics.text(client.font, itemName, itemX, y + 1, textColor, false);

		if (stack.isDamageableItem()) {
			int max = Math.max(1, stack.getMaxDamage());
			int remaining = Math.max(0, max - stack.getDamageValue());
			float ratio = remaining / (float) max;
			String detail = Math.round(ratio * 100.0F) + "%";
			int detailX = textRight - client.font.width(detail);
			graphics.text(client.font, detail, detailX, y + 10, WidgetRenderUtil.durabilityColor(ratio), false);
		} else if (stack.getCount() > 1) {
			String count = "x" + stack.getCount();
			int countX = textRight - client.font.width(count);
			graphics.text(client.font, count, countX, y + 10, textColor, false);
		}
	}

	private static void drawPreviewLine(
		GuiGraphicsExtractor graphics,
		Minecraft client,
		int x,
		int y,
		int lineWidth,
		boolean alignRight,
		String label,
		boolean showText,
		int textColor,
		String itemName
	) {
		int iconX = alignRight ? x + Math.max(0, lineWidth - 16) : x;
		if (showText) {
			if (!alignRight) {
				graphics.text(client.font, label, x, y + 5, textColor, false);
				iconX = x + 22;
			} else {
				String clipped = client.font.plainSubstrByWidth(label, Math.max(10, lineWidth - 24));
				int labelX = iconX - 4 - client.font.width(clipped);
				graphics.text(client.font, clipped, labelX, y + 5, textColor, false);
			}
		}
		graphics.fill(iconX, y, iconX + 16, y + 16, 0xAA000000);
		graphics.fill(iconX + 1, y + 1, iconX + 15, y + 15, 0xFF80D8FF);
		if (!showText) {
			return;
		}
		if (!alignRight) {
			graphics.text(client.font, itemName, iconX + 20, y + 1, textColor, false);
			graphics.text(client.font, "100%", iconX + 20, y + 10, 0xFF32D74B, false);
			return;
		}
		int textRight = iconX - 8;
		int nameX = textRight - client.font.width(itemName);
		graphics.text(client.font, itemName, nameX, y + 1, textColor, false);
		int detailX = textRight - client.font.width("100%");
		graphics.text(client.font, "100%", detailX, y + 10, 0xFF32D74B, false);
	}
}
