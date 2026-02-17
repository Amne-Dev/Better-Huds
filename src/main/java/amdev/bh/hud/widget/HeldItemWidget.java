package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null || !isSeparate(context.config())) {
			return;
		}

		renderHands(graphics, client, context.config(), widgetConfig, x, y, player);
	}

	public static void renderHands(GuiGraphics graphics, Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y, Player player) {
		boolean showText = showHandText(config, widgetConfig);
		int width = sectionWidth(showText);
		renderHandsAligned(graphics, client, config, widgetConfig, x, y, width, false, player);
	}

	public static void renderHandsAligned(
		GuiGraphics graphics,
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

	private static void drawHandLine(
		GuiGraphics graphics,
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
				graphics.drawString(client.font, label, x, y + 5, textColor, false);
				iconX = x + 22;
			} else {
				String labelText = client.font.plainSubstrByWidth(label.getString(), Math.max(10, lineWidth - 24));
				int labelX = iconX - 4 - client.font.width(labelText);
				graphics.drawString(client.font, labelText, labelX, y + 5, textColor, false);
			}
		}

		if (stack.isEmpty()) {
			if (showText) {
				String empty = Component.translatable("widget.better-huds.empty").getString();
				if (!alignRight) {
					graphics.drawString(client.font, empty, iconX + 20, y + 5, 0xFFAAAAAA, false);
				} else {
					int emptyX = Math.max(x, iconX - 8 - client.font.width(empty));
					graphics.drawString(client.font, empty, emptyX, y + 5, 0xFFAAAAAA, false);
				}
			}
			return;
		}

		graphics.renderItem(stack, iconX, y);
		graphics.renderItemDecorations(client.font, stack, iconX, y);
		if (!showText) {
			return;
		}

		if (!alignRight) {
			String itemName = client.font.plainSubstrByWidth(stack.getHoverName().getString(), 82);
			graphics.drawString(client.font, itemName, iconX + 20, y + 1, textColor, false);

			if (stack.isDamageableItem()) {
				int max = Math.max(1, stack.getMaxDamage());
				int remaining = Math.max(0, max - stack.getDamageValue());
				float ratio = remaining / (float) max;
				String detail = Math.round(ratio * 100.0F) + "%";
				graphics.drawString(client.font, detail, iconX + 20, y + 10, WidgetRenderUtil.durabilityColor(ratio), false);
			} else if (stack.getCount() > 1) {
				graphics.drawString(client.font, "x" + stack.getCount(), iconX + 20, y + 10, textColor, false);
			}
			return;
		}

		int textRight = iconX - 8;
		int available = Math.max(12, textRight - x);
		String itemName = client.font.plainSubstrByWidth(stack.getHoverName().getString(), available);
		int itemX = textRight - client.font.width(itemName);
		graphics.drawString(client.font, itemName, itemX, y + 1, textColor, false);

		if (stack.isDamageableItem()) {
			int max = Math.max(1, stack.getMaxDamage());
			int remaining = Math.max(0, max - stack.getDamageValue());
			float ratio = remaining / (float) max;
			String detail = Math.round(ratio * 100.0F) + "%";
			int detailX = textRight - client.font.width(detail);
			graphics.drawString(client.font, detail, detailX, y + 10, WidgetRenderUtil.durabilityColor(ratio), false);
		} else if (stack.getCount() > 1) {
			String count = "x" + stack.getCount();
			int countX = textRight - client.font.width(count);
			graphics.drawString(client.font, count, countX, y + 10, textColor, false);
		}
	}
}
