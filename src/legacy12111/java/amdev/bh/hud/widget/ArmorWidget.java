package amdev.bh.hud.widget;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorWidget implements HudWidget {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};

	@Override
	public String id() {
		return "armor";
	}

	@Override
	public Component displayName() {
		return Component.translatable("widget.better-huds.armor");
	}

	@Override
	public int getWidth(Minecraft client) {
		return 190;
	}

	@Override
	public int getHeight(Minecraft client) {
		return 96;
	}

	@Override
	public int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean vertical = widgetConfig.toggle("orientation_vertical", false);
		boolean showHands = widgetConfig.toggle("show_hands", true);
		boolean separateHands = widgetConfig.toggle("separate_hands", false);
		boolean handsInline = showHands && !separateHands;
		boolean showHandText = HeldItemWidget.showHandText(config, widgetConfig);

		if (vertical) {
			int armorWidth = widgetConfig.showText() ? 52 : 18;
			if (handsInline) {
				return Math.max(armorWidth, HeldItemWidget.sectionWidth(showHandText));
			}
			return armorWidth;
		}

		int armorWidth = 82;
		if (handsInline) {
			return armorWidth + 8 + HeldItemWidget.sectionWidth(showHandText);
		}
		return armorWidth;
	}

	@Override
	public int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		boolean vertical = widgetConfig.toggle("orientation_vertical", false);
		boolean showHands = widgetConfig.toggle("show_hands", true);
		boolean separateHands = widgetConfig.toggle("separate_hands", false);
		boolean handsInline = showHands && !separateHands;
		int armorHeight = vertical ? 78 : (widgetConfig.showText() ? 34 : 18);
		if (handsInline) {
			if (vertical) {
				return armorHeight + 4 + HeldItemWidget.sectionHeight();
			}
			return Math.max(armorHeight, HeldItemWidget.sectionHeight());
		}
		return armorHeight;
	}

	@Override
	public void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y) {
		Player player = client.player;
		if (player == null) {
			return;
		}

		boolean showText = widgetConfig.showText();
		boolean vertical = widgetConfig.toggle("orientation_vertical", false);
		boolean showHands = widgetConfig.toggle("show_hands", true);
		boolean separateHands = widgetConfig.toggle("separate_hands", false);
		boolean alignRight = widgetConfig.toggle("align_right", false);
		boolean handsInline = showHands && !separateHands;
		int widgetWidth = getWidth(client, context.config(), widgetConfig);
		int armorWidth = vertical ? (showText ? 52 : 18) : 82;
		int armorX = x;
		int handsWidth = HeldItemWidget.sectionWidth(HeldItemWidget.showHandText(context.config(), widgetConfig));
		int handsX = x;
		int handsY = y;
		if (handsInline) {
			if (vertical) {
				int baseWidth = Math.max(armorWidth, handsWidth);
				armorX = alignRight ? x + (baseWidth - armorWidth) : x;
				handsX = alignRight ? x + (baseWidth - handsWidth) : x;
				handsY = y + 78 + 4;
			} else if (alignRight) {
				handsX = x;
				handsY = y;
				armorX = x + handsWidth + 8;
			} else {
				armorX = x;
				handsX = x + 90;
				handsY = y;
			}
		} else {
			armorX = alignRight ? x + Math.max(0, widgetWidth - armorWidth) : x;
		}
		boolean hasAnyArmor = false;
		for (int i = 0; i < SLOTS.length; i++) {
			ItemStack stack = player.getItemBySlot(SLOTS[i]);
			int drawX = vertical ? armorX : armorX + (i * 20);
			int drawY = vertical ? y + (i * 20) : y;

			if (stack.isEmpty()) {
				graphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x22000000);
				continue;
			}

			hasAnyArmor = true;
			graphics.renderItem(stack, drawX, drawY);
			graphics.renderItemDecorations(client.font, stack, drawX, drawY);

			if (showText && stack.isDamageableItem()) {
				int max = Math.max(1, stack.getMaxDamage());
				int remaining = Math.max(0, max - stack.getDamageValue());
				float ratio = remaining / (float) max;
				String durabilityText = Math.round(ratio * 100.0F) + "%";
				int textColor = WidgetRenderUtil.durabilityColor(ratio);
				if (vertical) {
					graphics.drawString(client.font, durabilityText, drawX + 20, drawY + 4, textColor, false);
				} else {
					graphics.drawString(client.font, durabilityText, drawX - 1, drawY + 20, textColor, false);
				}
			}
		}

		if (showText && !hasAnyArmor) {
			String empty = Component.translatable("widget.better-huds.no_armor").getString();
			int textColor = WidgetRenderUtil.widgetTextColor(widgetConfig, widgetConfig.textColor, 347);
			int textX;
			if (alignRight) {
				textX = x + Math.max(0, widgetWidth - client.font.width(empty));
			} else {
				textX = x;
			}
			graphics.drawString(client.font, empty, textX, y + 5, textColor, false);
		}

		if (handsInline) {
			HeldItemWidget.renderHandsAligned(graphics, client, context.config(), widgetConfig, handsX, handsY, handsWidth, alignRight, player);
		}
	}
}
