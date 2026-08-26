package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.hud.ItemHistoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Iterator;

public class ItemHistoryWidget implements HudWidget {
	private static final long FADE_START_MS = 1_400L;
	private static final long FADE_END_MS = 4_200L;

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
		return 220;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 62;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		if (widgetConfig.toggle("history_compact", false)) {
			return widgetConfig.showText() ? 48 : 62;
		}
		return widgetConfig.showText() ? 62 : 84;
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config, HudRenderContext context) {
		return client.player != null && !context.itemHistory().groupedEvents().isEmpty();
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Iterator<ItemHistoryTracker.ItemHistoryEvent> iterator = context.itemHistory().groupedEvents().iterator();
		int widgetWidth = getWidth(client, context.config(), widgetConfig);
		boolean alignRight = widgetConfig.toggle("align_right", false);
		int baseTextColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 547);
		if (!iterator.hasNext()) {
			if (context.editorMode() && widgetConfig.showText()) {
				String empty = Component.translatable("widget.better-huds.item_history_empty").getString();
				int emptyX = alignRight ? x + Math.max(0, widgetWidth - client.font.width(empty)) : x;
				graphics.drawString(client.font, empty, emptyX, y + 3, baseTextColor, false);
			}
			return;
		}

		int row = 0;
		int maxRows = widgetConfig.toggle("history_compact", false) ? (widgetConfig.showText() ? 3 : 5) : (widgetConfig.showText() ? 4 : 6);
		long now = System.currentTimeMillis();
		while (iterator.hasNext() && row < maxRows) {
			ItemHistoryTracker.ItemHistoryEvent event = iterator.next();
			int delta = event.delta();
			int baseColor = delta >= 0 ? 0xFF32D74B : 0xFFFF453A;
			if (widgetConfig.toggle("rainbow_text", false)) {
				baseColor = WidgetRenderUtil.rainbowColor(547 + (row * 19));
			}
			int color = withAgeFade(baseColor, now - event.timestampMs());
			if ((color >>> 24) <= 2) {
				continue;
			}
			String text;
			if (widgetConfig.showText()) {
				String prefix = (delta >= 0 ? "+" : "") + delta + " ";
				int maxNameWidth = Math.max(10, widgetWidth - client.font.width(prefix) - 2);
				String itemName = client.font.plainSubstrByWidth(event.displayName(), maxNameWidth);
				text = prefix + itemName;
			} else {
				text = (delta >= 0 ? "+" : "") + delta;
			}
			int drawX = alignRight ? x + Math.max(0, widgetWidth - client.font.width(text)) : x;
			graphics.drawString(client.font, text, drawX, y + 3 + row * 14, color, false);
			row++;
		}
	}

	private static int withAgeFade(int color, long ageMs) {
		if (ageMs <= FADE_START_MS) {
			return color;
		}
		float fadeRange = Math.max(1.0F, FADE_END_MS - FADE_START_MS);
		float alpha = 1.0F - Math.min(1.0F, (ageMs - FADE_START_MS) / fadeRange);
		int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
		return (alphaByte << 24) | (color & 0x00FFFFFF);
	}
}
