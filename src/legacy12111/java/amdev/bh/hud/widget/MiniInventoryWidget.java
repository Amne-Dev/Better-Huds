package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MiniInventoryWidget implements HudWidget {
	@Override
	public String id() {
		return "mini_inventory";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.mini_inventory");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 162;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 72;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean showHotbar = widgetConfig.toggle("mini_show_hotbar", true);
		int rows = showHotbar ? 4 : 3;
		int title = widgetConfig.showText() ? 10 : 0;
		return title + (rows * 18);
	}

	@Override
	public boolean shouldRender(Minecraft client, BetterHudsConfig config, HudRenderContext context) {
		return context.editorMode() || context.miniInventoryVisible();
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		boolean showHotbar = widgetConfig.toggle("mini_show_hotbar", true);
		boolean showTitle = widgetConfig.showText();
		int drawY = y;
		if (showTitle) {
			int color = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 863);
			graphics.drawString(client.font, Component.translatable("widget.better-huds.mini_inventory_title"), x + 1, y, color, false);
			drawY += 10;
		}

		Inventory inventory = client.player != null ? client.player.getInventory() : null;
		int rows = showHotbar ? 4 : 3;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < 9; col++) {
				int slotX = x + (col * 18);
				int slotY = drawY + (row * 18);
				boolean hotbarRow = showHotbar && row == 3;
				boolean selected = hotbarRow && inventory != null && McCompat.selectedHotbarSlot(inventory) == col;
				drawSlotFrame(graphics, slotX, slotY, selected);
				if (inventory == null) {
					continue;
				}

				int slot = slotIndex(row, col, showHotbar);
				if (slot < 0 || slot >= inventory.getContainerSize()) {
					continue;
				}
				ItemStack stack = inventory.getItem(slot);
				if (stack.isEmpty()) {
					continue;
				}
				graphics.renderItem(stack, slotX + 1, slotY + 1);
				graphics.renderItemDecorations(client.font, stack, slotX + 1, slotY + 1);
			}
		}
	}

	private static int slotIndex(int row, int col, boolean showHotbar) {
		if (showHotbar && row == 3) {
			return col;
		}
		return 9 + (row * 9) + col;
	}

	private static void drawSlotFrame(GuiGraphics graphics, int x, int y, boolean selected) {
		int border = selected ? 0xFF80D8FF : 0x66464646;
		graphics.fill(x, y, x + 18, y + 18, 0x66121212);
		graphics.fill(x, y, x + 18, y + 1, border);
		graphics.fill(x, y + 17, x + 18, y + 18, border);
		graphics.fill(x, y, x + 1, y + 18, border);
		graphics.fill(x + 17, y, x + 18, y + 18, border);
	}
}
