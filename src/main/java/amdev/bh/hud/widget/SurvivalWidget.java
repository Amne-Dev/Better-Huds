package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SurvivalWidget implements HudWidget {
	@Override
	public String id() {
		return "survival";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.survival");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 190;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 30;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		if (!widgetConfig.showText()) {
			return 184;
		}
		int line1 = client.font.width("HP 20.0/20.0 AR20 FD20 SAT20.0");
		if (!widgetConfig.toggle("survival_show_counts", true)) {
			return line1 + 6;
		}
		int line2 = client.font.width("ATK 100% TTM 99 ARR 999 ABS 20.0");
		return Math.max(line1, line2) + 6;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		if (!widgetConfig.showText()) {
			return 24;
		}
		return widgetConfig.toggle("survival_show_counts", true) ? 30 : 16;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null) {
			return;
		}

		FoodData food = player.getFoodData();
		int totems = countItem(player.getInventory(), Items.TOTEM_OF_UNDYING);
		int arrows = countItem(player.getInventory(), Items.ARROW) + countItem(player.getInventory(), Items.SPECTRAL_ARROW) + countItem(player.getInventory(), Items.TIPPED_ARROW);
		float cooldown = player.getAttackStrengthScale(0.0F);
		boolean showCounts = widgetConfig.toggle("survival_show_counts", true);
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 421);
		if (!widgetConfig.showText()) {
			int barWidth = 184;
			graphics.fill(x, y + 2, x + barWidth, y + 6, 0x33000000);
			graphics.fill(x, y + 10, x + barWidth, y + 14, 0x33000000);
			graphics.fill(x, y + 18, x + barWidth, y + 22, 0x33000000);
			graphics.fill(x, y + 2, x + Math.round(barWidth * Math.min(1.0F, player.getHealth() / Math.max(1.0F, player.getMaxHealth()))), y + 6, 0xFFFF3B30);
			graphics.fill(x, y + 10, x + Math.round(barWidth * (food.getFoodLevel() / 20.0F)), y + 14, 0xFFFF9F0A);
			graphics.fill(x, y + 18, x + Math.round(barWidth * cooldown), y + 22, WidgetRenderUtil.durabilityColor(cooldown));
			return;
		}

		String line1 = String.format(
			"HP %.1f/%.1f AR%d FD%d SAT%.1f",
			player.getHealth(),
			player.getMaxHealth(),
			player.getArmorValue(),
			food.getFoodLevel(),
			food.getSaturationLevel()
		);
		int width = getWidth(client, context.config(), widgetConfig);
		int line1X = x + Math.max(0, (width - client.font.width(line1)) / 2);
		graphics.drawString(client.font, line1, line1X, y + 2, textColor, false);

		if (!showCounts) {
			return;
		}
		String line2 = String.format(
			"ATK %d%% TTM %d ARR %d ABS %.1f",
			Math.round(cooldown * 100.0F),
			totems,
			arrows,
			player.getAbsorptionAmount()
		);
		int line2X = x + Math.max(0, (width - client.font.width(line2)) / 2);
		graphics.drawString(client.font, line2, line2X, y + 15, textColor, false);
	}

	private static int countItem(Inventory inventory, net.minecraft.world.item.Item item) {
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
