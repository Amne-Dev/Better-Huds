package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemCounterWidget implements HudWidget {
	@Override
	public String id() {
		return "item_counter";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.item_counter");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 220;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 42;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return widgetConfig.toggle("counter_show_bar", true) ? 42 : 24;
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config) {
		return client.player != null && config.itemCounterTarget > 0 && config.itemCounterItemId != null && !config.itemCounterItemId.isBlank();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null) {
			return;
		}

		BetterHudsConfig config = context.config();
		int widgetWidth = getWidth(client, config, widgetConfig);
		boolean alignRight = widgetConfig.toggle("align_right", false);
		int target = Math.max(0, config.itemCounterTarget);
		Item tracked = resolveTrackedItem(config.itemCounterItemId);
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 503);
		if (tracked == null || tracked == Items.AIR || target <= 0) {
			if (context.editorMode() && widgetConfig.showText()) {
				String unset = Component.translatable("widget.better-huds.item_counter_unset").getString();
				int unsetX = alignRight ? x + Math.max(0, widgetWidth - client.font.width(unset)) : x;
				graphics.text(client.font, unset, unsetX, y + 3, textColor, false);
			}
			return;
		}

		int have = countItem(player.getInventory(), tracked);
		float progress = Math.max(0.0F, Math.min(1.0F, have / (float) target));
		int iconX = alignRight ? x + Math.max(0, widgetWidth - 16) : x;
		int textLeft = alignRight ? x : x + 20;
		int textRight = alignRight ? iconX - 4 : x + widgetWidth;
		int textMaxWidth = Math.max(10, textRight - textLeft);

		graphics.item(new ItemStack(tracked), iconX, y);
		if (widgetConfig.showText()) {
			String itemName = client.font.plainSubstrByWidth(McCompat.itemDisplayName(tracked), textMaxWidth);
			String ratio = have + "/" + target;
			if (alignRight) {
				graphics.text(client.font, itemName, textRight - client.font.width(itemName), y + 1, textColor, false);
				graphics.text(client.font, ratio, textRight - client.font.width(ratio), y + 13, WidgetRenderUtil.durabilityColor(progress), false);
			} else {
				graphics.text(client.font, itemName, textLeft, y + 1, textColor, false);
				graphics.text(client.font, ratio, textLeft, y + 13, WidgetRenderUtil.durabilityColor(progress), false);
			}
		} else {
			String ratio = have + "/" + target;
			int ratioX = alignRight ? textRight - client.font.width(ratio) : textLeft;
			graphics.text(client.font, ratio, ratioX, y + 6, textColor, false);
		}

		if (widgetConfig.toggle("counter_show_bar", true)) {
			int barX = alignRight ? x + 4 : x + 20;
			int barY = y + 27;
			int barWidth = 190;
			int filledWidth = Math.round(barWidth * progress);
			graphics.fill(barX, barY, barX + barWidth, barY + 4, 0x33000000);
			if (alignRight) {
				graphics.fill(barX + barWidth - filledWidth, barY, barX + barWidth, barY + 4, WidgetRenderUtil.durabilityColor(progress));
			} else {
				graphics.fill(barX, barY, barX + filledWidth, barY + 4, WidgetRenderUtil.durabilityColor(progress));
			}
		}
	}

	private static Item resolveTrackedItem(String itemId) {
		Item item = McCompat.findItemById(itemId);
		return item == Items.AIR ? null : item;
	}

	private static int countItem(Inventory inventory, Item item) {
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}
}
