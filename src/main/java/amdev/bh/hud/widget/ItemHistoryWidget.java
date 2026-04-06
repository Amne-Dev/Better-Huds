package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.hud.ItemHistoryTracker;
import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ItemHistoryWidget implements HudWidget {
	private static final int ICON_SIZE = 16;
	private static final int ROW_HEIGHT = 18;
	private static final int ROW_GAP = 2;

	@Override
	public String id() {
		return "item_history";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.item_history");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 246;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 78;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		int rows = maxRows(widgetConfig);
		return Math.max(20, rows * ROW_HEIGHT + Math.max(0, rows - 1) * ROW_GAP);
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config, HudRenderContext context) {
		return client.player != null && !context.itemHistory().groupedEvents().isEmpty();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		List<ItemHistoryTracker.ItemHistoryEvent> events = context.editorMode() && context.itemHistory().groupedEvents().isEmpty()
			? previewEvents()
			: context.itemHistory().groupedEvents();
		int widgetWidth = getWidth(client, context.config(), widgetConfig);
		boolean alignRight = widgetConfig.toggle("align_right", false);
		int baseTextColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 547);
		if (events.isEmpty()) {
			if (context.editorMode() && widgetConfig.showText()) {
				String empty = Component.translatable("widget.better-huds.item_history_empty").getString();
				int emptyX = alignRight ? x + Math.max(0, widgetWidth - client.font.width(empty)) : x;
				graphics.text(client.font, empty, emptyX, y + 3, baseTextColor, false);
			}
			return;
		}

		int row = 0;
		int maxRows = maxRows(widgetConfig);
		long now = System.currentTimeMillis();
		for (ItemHistoryTracker.ItemHistoryEvent event : events) {
			if (row >= maxRows) {
				break;
			}
			int delta = event.delta();
			int baseColor = delta >= 0 ? 0xFF32D74B : 0xFFFF453A;
			if (widgetConfig.toggle("rainbow_text", false)) {
				baseColor = WidgetRenderUtil.rainbowColor(547 + (row * 19));
			}
			int color = withAgeFade(baseColor, now - event.timestampMs());
			if ((color >>> 24) <= 2) {
				continue;
			}
			int rowY = y + row * (ROW_HEIGHT + ROW_GAP);
			ItemStack iconStack = iconStack(event.itemId());
			int deltaWidth;
			int drawX;
			if (widgetConfig.showText()) {
				String deltaText = (delta >= 0 ? "+" : "") + delta;
				deltaWidth = client.font.width(deltaText);
				int availableNameWidth = Math.max(10, widgetWidth - ICON_SIZE - 6 - deltaWidth - 6);
				int nameColor = withAgeFade(baseTextColor, now - event.timestampMs());
				if (alignRight) {
					int rightEdge = x + widgetWidth;
					int iconX = rightEdge - ICON_SIZE;
					int deltaX = iconX - 4 - deltaWidth;
					int nameRight = deltaX - 6;
					String clippedName = client.font.plainSubstrByWidth(event.displayName(), Math.max(10, nameRight - x));
					int nameX = nameRight - client.font.width(clippedName);
					drawIcon(graphics, client, iconStack, iconX, rowY);
					graphics.text(client.font, deltaText, deltaX, rowY + 4, color, false);
					graphics.text(client.font, clippedName, nameX, rowY + 4, nameColor, false);
				} else {
					int iconX = x;
					int nameX = iconX + ICON_SIZE + 4;
					String clippedName = client.font.plainSubstrByWidth(event.displayName(), availableNameWidth);
					int deltaX = x + widgetWidth - deltaWidth;
					drawIcon(graphics, client, iconStack, iconX, rowY);
					graphics.text(client.font, clippedName, nameX, rowY + 4, nameColor, false);
					graphics.text(client.font, deltaText, deltaX, rowY + 4, color, false);
				}
			} else {
				String deltaText = (delta >= 0 ? "+" : "") + delta;
				deltaWidth = client.font.width(deltaText);
				if (alignRight) {
					int iconX = x + widgetWidth - ICON_SIZE;
					drawX = iconX - 4 - deltaWidth;
					drawIcon(graphics, client, iconStack, iconX, rowY);
				} else {
					int iconX = x;
					drawX = iconX + ICON_SIZE + 4;
					drawIcon(graphics, client, iconStack, iconX, rowY);
				}
				graphics.text(client.font, deltaText, drawX, rowY + 4, color, false);
			}
			row++;
		}
	}

	private static int maxRows(BetterHudsConfig.WidgetConfig widgetConfig) {
		if (widgetConfig.toggle("history_compact", false)) {
			return widgetConfig.showText() ? 3 : 4;
		}
		return widgetConfig.showText() ? 4 : 5;
	}

	private static ItemStack iconStack(String itemId) {
		Item item = McCompat.findItemById(itemId);
		if (item == null || item == Items.AIR) {
			return new ItemStack(Items.CHEST);
		}
		return new ItemStack(item);
	}

	private static void drawIcon(GuiGraphicsExtractor graphics, Minecraft client, ItemStack stack, int x, int y) {
		graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, 0x33000000);
		graphics.item(stack, x, y);
		graphics.itemDecorations(client.font, stack, x, y);
	}

	private static List<ItemHistoryTracker.ItemHistoryEvent> previewEvents() {
		List<ItemHistoryTracker.ItemHistoryEvent> preview = new ArrayList<>();
		long now = System.currentTimeMillis();
		preview.add(new ItemHistoryTracker.ItemHistoryEvent("minecraft:stone", "Stone", 32, now));
		preview.add(new ItemHistoryTracker.ItemHistoryEvent("minecraft:oak_log", "Oak Log", -12, now - 700L));
		preview.add(new ItemHistoryTracker.ItemHistoryEvent("minecraft:torch", "Torch", 16, now - 1400L));
		return preview;
	}

	private static int withAgeFade(int color, long ageMs) {
		if (ageMs <= ItemHistoryTracker.ITEM_IDLE_BEFORE_FADE_MS) {
			return color;
		}
		float fadeRange = Math.max(1.0F, ItemHistoryTracker.ITEM_FADE_DURATION_MS);
		float alpha = 1.0F - Math.min(1.0F, (ageMs - ItemHistoryTracker.ITEM_IDLE_BEFORE_FADE_MS) / fadeRange);
		int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
		return (alphaByte << 24) | (color & 0x00FFFFFF);
	}
}
