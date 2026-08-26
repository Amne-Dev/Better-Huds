package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumablesWidget implements HudWidget {
	@Override
	public String id() {
		return "consumables";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.consumables");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 110;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 30;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		int maxIcons = widgetConfig.toggle("consumables_compact", true) ? 8 : 14;
		int icons = Math.max(1, Math.min(maxIcons, collectEntries(client, widgetConfig, maxIcons).size()));
		return (icons * 18) - 2;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return 18;
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config) {
		if (client.player == null) {
			return false;
		}
		int maxIcons = config.getOrCreateWidgetConfig(id()).toggle("consumables_compact", true) ? 8 : 14;
		return !collectEntries(client, config.getOrCreateWidgetConfig(id()), maxIcons).isEmpty();
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		int maxIcons = widgetConfig.toggle("consumables_compact", true) ? 8 : 14;
		List<Entry> entries = collectEntries(client, widgetConfig, maxIcons);
		int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 307);
		if (entries.isEmpty()) {
			if (context.editorMode() && widgetConfig.showText()) {
				graphics.drawString(client.font, Component.translatable("widget.better-huds.consumables_empty"), x, y + 3, textColor, false);
			}
			return;
		}

		for (int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			int iconX = x + (i * 18);
			graphics.renderItem(new ItemStack(entry.item()), iconX, y);
			if (widgetConfig.showText()) {
				String count = Integer.toString(entry.count());
				int countX = iconX + (16 - client.font.width(count)) / 2;
				graphics.drawString(client.font, count, countX, y - 6, textColor, true);
			}
		}
	}

	private static List<Entry> collectEntries(Minecraft client, BetterHudsConfig.WidgetConfig widgetConfig, int maxIcons) {
		Player player = client.player;
		if (player == null) {
			return List.of();
		}

		boolean includeFood = widgetConfig.toggle("consumables_food", true);
		boolean includePotions = widgetConfig.toggle("consumables_potions", true);
		if (!includeFood && !includePotions) {
			return List.of();
		}

		Inventory inventory = player.getInventory();
		Map<Item, Integer> counts = new HashMap<>();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			Item item = stack.getItem();
			if (item == Items.AIR) {
				continue;
			}
			if (!isTracked(stack, includeFood, includePotions)) {
				continue;
			}
			counts.put(item, counts.getOrDefault(item, 0) + stack.getCount());
		}

		List<Entry> entries = new ArrayList<>();
		for (Map.Entry<Item, Integer> countEntry : counts.entrySet()) {
			entries.add(new Entry(countEntry.getKey(), countEntry.getValue()));
		}
		entries.sort(Comparator.comparingInt(Entry::count).reversed().thenComparing(entry -> entry.item().toString()));
		if (entries.size() > maxIcons) {
			return new ArrayList<>(entries.subList(0, maxIcons));
		}
		return entries;
	}

	private static boolean isTracked(ItemStack stack, boolean includeFood, boolean includePotions) {
		boolean isFood = stack.has(DataComponents.FOOD);
		boolean isPotion = stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
		return (includeFood && isFood) || (includePotions && isPotion);
	}

	private record Entry(Item item, int count) {
	}
}
