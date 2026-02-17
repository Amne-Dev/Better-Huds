package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.config.ProfileShareCodec;
import amdev.bh.hud.HudSystem;
import amdev.bh.hud.HudWidget;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HudSettingsScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private final List<Card> cards = new ArrayList<>();
	private final List<ProfileBounds> profileBounds = new ArrayList<>();
	private int page;
	private int totalPages = 1;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int sidebarWidth;
	private int contentX;
	private int contentWidth;
	private boolean profileMenuVisible;
	private int profileMenuIndex = -1;
	private int profileMenuX;
	private int profileMenuY;
	private Component noticeMessage = Component.empty();
	private int noticeColor = 0xFFB6E3FF;
	private long noticeUntilMs;

	public HudSettingsScreen(HudSystem hudSystem, Screen parent) {
		super(Component.translatable("screen.better-huds.settings"));
		this.hudSystem = hudSystem;
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		clearWidgets();
		cards.clear();
		profileBounds.clear();
		profileMenuVisible = false;
		profileMenuIndex = -1;

		List<HudWidget> widgets = hudSystem.widgets().stream().filter(widget -> !widget.id().equals("held_item")).toList();
		int columns = 3;
		int gap = 10;
		int cardHeight = 82;
		panelWidth = Math.min(720, width - 20);
		panelHeight = Math.min(height - 20, 390);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		sidebarWidth = 106;
		contentX = panelX + sidebarWidth + 8;
		contentWidth = panelWidth - sidebarWidth - 16;

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 80, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		addRenderableWidget(new GlassButton(contentX + 10, panelY + 8, 80, 20, Component.translatable("screen.better-huds.settings.global"), button -> {
			if (minecraft != null) {
				minecraft.setScreen(new HudGlobalSettingsScreen(hudSystem, this));
			}
		}));
		addRenderableWidget(new GlassButton(contentX + 96, panelY + 8, 92, 20, Component.translatable("screen.better-huds.settings.keybinds"), button -> {
			if (minecraft != null) {
				minecraft.setScreen(new HudKeybindsScreen(hudSystem, this));
			}
		}));

		renderProfileButtons();

		int startY = panelY + 58;
		int rowsPerPage = Math.max(1, (panelHeight - 70) / (cardHeight + gap));
		int pageSize = columns * rowsPerPage;
		totalPages = Math.max(1, (int) Math.ceil(widgets.size() / (double) pageSize));
		page = Math.max(0, Math.min(page, totalPages - 1));
		if (totalPages > 1) {
			addRenderableWidget(new GlassButton(contentX + contentWidth - 58, panelY + 32, 18, 16, Component.literal("<"), button -> {
				page = Math.max(0, page - 1);
				init();
			}));
			addRenderableWidget(new GlassButton(contentX + contentWidth - 36, panelY + 32, 18, 16, Component.literal(">"), button -> {
				page = Math.min(totalPages - 1, page + 1);
				init();
			}));
		}

		int cardWidth = (contentWidth - 20 - ((columns - 1) * gap)) / columns;
		int startIndex = page * pageSize;
		int endIndex = Math.min(widgets.size(), startIndex + pageSize);
		for (int index = startIndex; index < endIndex; index++) {
			HudWidget widget = widgets.get(index);
			int pageIndex = index - startIndex;
			int column = pageIndex % columns;
			int row = pageIndex / columns;
			int x = contentX + 10 + column * (cardWidth + gap);
			int y = startY + row * (cardHeight + gap);
			cards.add(new Card(widget, iconFor(widget.id()), x, y, cardWidth, cardHeight));

			addRenderableWidget(new GlassButton(x + cardWidth - 70, y + 8, 60, 16, Component.literal(hudSystem.config().getOrCreateWidgetConfig(widget.id()).enabled ? "ON" : "OFF"), button -> {
				var cfg = hudSystem.config().getOrCreateWidgetConfig(widget.id());
				cfg.enabled = !cfg.enabled;
				button.setMessage(Component.literal(cfg.enabled ? "ON" : "OFF"));
				hudSystem.configManager().save();
			}));

			addRenderableWidget(new GlassButton(x + 10, y + cardHeight - 24, cardWidth - 20, 16, Component.translatable("screen.better-huds.settings.widget_settings"), button -> {
				if (minecraft != null) {
					minecraft.setScreen(new WidgetSettingsScreen(hudSystem, this, widget.id()));
				}
			}));
		}
	}

	private void renderProfileButtons() {
		BetterHudsConfig config = hudSystem.config();
		int buttonX = panelX + 10;
		int y = panelY + 36;
		int width = sidebarWidth - 20;
		for (int i = 0; i < config.profiles.size(); i++) {
			int index = i;
			BetterHudsConfig.Profile profile = config.profiles.get(i);
			boolean active = config.activeProfile == i;
			int accent = active ? 0xFFA6E66A : 0xFF80D8FF;
			String label = font.plainSubstrByWidth(profile.name == null || profile.name.isBlank() ? ("Profile " + (i + 1)) : profile.name, width - 8);
			profileBounds.add(new ProfileBounds(i, buttonX, y, width, 20));
			addRenderableWidget(new GlassButton(buttonX, y, width, 20, Component.literal(label), button -> {
				config.activeProfile = index;
				hudSystem.configManager().save();
				init();
			}, accent));
			y += 24;
		}

		int footerY = panelY + panelHeight - 52;
		addRenderableWidget(new GlassButton(buttonX, footerY, width, 20, Component.translatable("screen.better-huds.settings.import_profile"), button -> {
			if (minecraft != null) {
				minecraft.setScreen(new ProfileImportScreen(hudSystem, this));
			}
		}));
		addRenderableWidget(new GlassButton(buttonX, footerY + 22, width, 20, Component.translatable("screen.better-huds.settings.add_profile"), button -> {
			createProfileFromActive();
			hudSystem.configManager().save();
			init();
		}));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			ProfileBounds hit = profileAt(event.x(), event.y());
			if (hit != null) {
				profileMenuVisible = true;
				profileMenuIndex = hit.index();
				profileMenuX = hit.x() + hit.width() + 4;
				profileMenuY = hit.y();
				return true;
			}
			profileMenuVisible = false;
			profileMenuIndex = -1;
		}

		if (profileMenuVisible && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int menuWidth = 116;
			int menuHeight = 56;
			int menuX = clamp(profileMenuX, panelX + 2, panelX + panelWidth - menuWidth - 2);
			int menuY = clamp(profileMenuY, panelY + 2, panelY + panelHeight - menuHeight - 2);
			boolean inRename = event.x() >= menuX + 1 && event.x() < menuX + menuWidth - 1 && event.y() >= menuY + 1 && event.y() < menuY + 18;
			boolean inDelete = event.x() >= menuX + 1 && event.x() < menuX + menuWidth - 1 && event.y() >= menuY + 20 && event.y() < menuY + 37;
			boolean inExport = event.x() >= menuX + 1 && event.x() < menuX + menuWidth - 1 && event.y() >= menuY + 39 && event.y() < menuY + 56;
			if (inRename) {
				if (minecraft != null && profileMenuIndex >= 0) {
					minecraft.setScreen(new ProfileRenameScreen(hudSystem, this, profileMenuIndex));
				}
				profileMenuVisible = false;
				return true;
			}
			if (inDelete) {
				deleteProfile(profileMenuIndex);
				profileMenuVisible = false;
				return true;
			}
			if (inExport) {
				exportProfileToClipboard(profileMenuIndex);
				profileMenuVisible = false;
				return true;
			}
			profileMenuVisible = false;
		}

		return super.mouseClicked(event, doubleClick);
	}

	private void createProfileFromActive() {
		BetterHudsConfig config = hudSystem.config();
		BetterHudsConfig.Profile active = config.getActiveProfile();
		BetterHudsConfig.Profile next = new BetterHudsConfig.Profile("Profile " + (config.profiles.size() + 1));
		next.widgets = new LinkedHashMap<>();
		for (var entry : active.widgets.entrySet()) {
			next.widgets.put(entry.getKey(), copyWidgetConfig(entry.getValue()));
		}
		config.profiles.add(next);
		config.activeProfile = config.profiles.size() - 1;
		page = 0;
	}

	private void deleteProfile(int index) {
		BetterHudsConfig config = hudSystem.config();
		if (config.profiles.size() <= 1 || index < 0 || index >= config.profiles.size()) {
			return;
		}
		config.profiles.remove(index);
		if (config.activeProfile >= config.profiles.size()) {
			config.activeProfile = config.profiles.size() - 1;
		}
		if (config.activeProfile > index) {
			config.activeProfile--;
		}
		hudSystem.configManager().save();
		init();
	}

	private void exportProfileToClipboard(int index) {
		BetterHudsConfig config = hudSystem.config();
		if (minecraft == null || index < 0 || index >= config.profiles.size()) {
			setNotice(Component.translatable("screen.better-huds.settings.profile_export_failed"), 0xFFFF8080);
			return;
		}
		String json = ProfileShareCodec.encode(config.profiles.get(index));
		minecraft.keyboardHandler.setClipboard(json);
		setNotice(Component.translatable("screen.better-huds.settings.profile_export_copied"), 0xFFDAFFC2);
	}

	public boolean importProfileFromText(String raw) {
		if (minecraft == null) {
			setNotice(Component.translatable("screen.better-huds.settings.profile_import_failed"), 0xFFFF8080);
			return false;
		}
		BetterHudsConfig.Profile parsed = ProfileShareCodec.decode(raw);
		if (parsed == null) {
			setNotice(Component.translatable("screen.better-huds.settings.profile_import_failed"), 0xFFFF8080);
			return false;
		}

		BetterHudsConfig config = hudSystem.config();
		config.profiles.add(parsed);
		config.activeProfile = config.profiles.size() - 1;
		hudSystem.configManager().save();
		setNotice(Component.translatable("screen.better-huds.settings.profile_imported", parsed.name), 0xFFDAFFC2);
		init();
		return true;
	}

	private void setNotice(Component message, int color) {
		noticeMessage = message;
		noticeColor = color;
		noticeUntilMs = System.currentTimeMillis() + 3500L;
	}

	private ProfileBounds profileAt(double x, double y) {
		for (ProfileBounds bounds : profileBounds) {
			if (x >= bounds.x() && x < bounds.x() + bounds.width() && y >= bounds.y() && y < bounds.y() + bounds.height()) {
				return bounds;
			}
		}
		return null;
	}

	private BetterHudsConfig.WidgetConfig copyWidgetConfig(BetterHudsConfig.WidgetConfig source) {
		BetterHudsConfig.WidgetConfig copy = new BetterHudsConfig.WidgetConfig(source.x, source.y, source.anchor);
		copy.enabled = source.enabled;
		copy.scale = source.scale;
		copy.background = source.background;
		copy.showText = source.showText;
		copy.backgroundColor = source.backgroundColor;
		copy.textColor = source.textColor;
		copy.toggles = source.toggles == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.toggles);
		copy.values = source.values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.values);
		return copy;
	}

	@Override
	public void onClose() {
		hudSystem.configManager().save();
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		renderTransparentBackground(graphics);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);

		graphics.fill(panelX + sidebarWidth, panelY + 1, panelX + sidebarWidth + 1, panelY + panelHeight - 1, 0x5577AACC);
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.profiles"), panelX + 12, panelY + 14, 0xFFB6E3FF, false);

		int titleX = contentX + (contentWidth - font.width(title)) / 2;
		graphics.drawString(font, title, titleX, panelY + 14, 0xFFFFFFFF, false);
		if (totalPages > 1) {
			String pageLabel = (page + 1) + "/" + totalPages;
			graphics.drawString(font, pageLabel, contentX + contentWidth - 102, panelY + 35, 0xFFB6E3FF, false);
		}
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.grid_hint"), contentX + 10, panelY + 32, 0xFFB6E3FF, false);

		Component hoveredInfoTip = null;
		for (Card card : cards) {
			boolean enabled = hudSystem.config().getOrCreateWidgetConfig(card.widget().id()).enabled;
			int border = enabled ? 0xFF80D8FF : 0x66777777;
			graphics.fill(card.x(), card.y(), card.x() + card.width(), card.y() + card.height(), 0x661A1A1A);
			graphics.fill(card.x(), card.y(), card.x() + card.width(), card.y() + 1, border);
			graphics.fill(card.x(), card.y() + card.height() - 1, card.x() + card.width(), card.y() + card.height(), border);
			graphics.fill(card.x(), card.y(), card.x() + 1, card.y() + card.height(), border);
			graphics.fill(card.x() + card.width() - 1, card.y(), card.x() + card.width(), card.y() + card.height(), border);

			graphics.renderItem(card.icon(), card.x() + 8, card.y() + 8);
			String name = font.plainSubstrByWidth(card.widget().displayName().getString(), card.width() - 42);
			graphics.drawString(font, name, card.x() + 30, card.y() + 10, 0xFFFFFFFF, false);
			graphics.drawString(
				font,
				Component.translatable("screen.better-huds.settings.widget_state", enabled ? "ON" : "OFF"),
				card.x() + 30,
				card.y() + 24,
				enabled ? 0xFFDAFFC2 : 0xFFAAAAAA,
				false
			);

			Component keybindTip = keybindTipForWidget(card.widget().id());
			if (keybindTip != null) {
				int infoX = card.x() + card.width() - 16;
				int infoY = card.y() + 30;
				boolean infoHovered = mouseX >= infoX && mouseX < infoX + 10 && mouseY >= infoY && mouseY < infoY + 10;
				int infoBorder = infoHovered ? 0xFFFFFFFF : 0xFF80D8FF;
				graphics.fill(infoX, infoY, infoX + 10, infoY + 10, 0x66203040);
				graphics.fill(infoX, infoY, infoX + 10, infoY + 1, infoBorder);
				graphics.fill(infoX, infoY + 9, infoX + 10, infoY + 10, infoBorder);
				graphics.fill(infoX, infoY, infoX + 1, infoY + 10, infoBorder);
				graphics.fill(infoX + 9, infoY, infoX + 10, infoY + 10, infoBorder);
				graphics.drawString(font, "i", infoX + 3, infoY + 1, 0xFFFFFFFF, false);
				if (infoHovered) {
					hoveredInfoTip = keybindTip;
				}
			}
		}

		if (profileMenuVisible && profileMenuIndex >= 0) {
			int menuWidth = 116;
			int menuHeight = 56;
			int menuX = clamp(profileMenuX, panelX + 2, panelX + panelWidth - menuWidth - 2);
			int menuY = clamp(profileMenuY, panelY + 2, panelY + panelHeight - menuHeight - 2);
			boolean canDelete = hudSystem.config().profiles.size() > 1;
			graphics.fill(menuX, menuY, menuX + menuWidth, menuY + menuHeight, 0xDD101010);
			graphics.fill(menuX, menuY, menuX + menuWidth, menuY + 1, 0xFF80D8FF);
			graphics.fill(menuX, menuY + menuHeight - 1, menuX + menuWidth, menuY + menuHeight, 0xFF80D8FF);
			graphics.fill(menuX, menuY, menuX + 1, menuY + menuHeight, 0xFF80D8FF);
			graphics.fill(menuX + menuWidth - 1, menuY, menuX + menuWidth, menuY + menuHeight, 0xFF80D8FF);
			graphics.drawString(font, Component.translatable("screen.better-huds.settings.profile_menu_rename"), menuX + 8, menuY + 5, 0xFFFFFFFF, false);
			graphics.drawString(font, Component.translatable("screen.better-huds.settings.profile_menu_delete"), menuX + 8, menuY + 23, canDelete ? 0xFFFF8080 : 0xFF777777, false);
			graphics.drawString(font, Component.translatable("screen.better-huds.settings.profile_menu_export"), menuX + 8, menuY + 41, 0xFFB6E3FF, false);
		}

		if (System.currentTimeMillis() < noticeUntilMs) {
			graphics.drawString(font, noticeMessage, contentX + 10, panelY + panelHeight - 14, noticeColor, false);
		}

		super.render(graphics, mouseX, mouseY, tickDelta);
		if (hoveredInfoTip != null) {
			drawSimpleTooltip(graphics, hoveredInfoTip, mouseX, mouseY);
		}
	}

	private ItemStack iconFor(String widgetId) {
		return switch (widgetId) {
			case "armor" -> new ItemStack(Items.DIAMOND_CHESTPLATE);
			case "held_item" -> new ItemStack(Items.DIAMOND_SWORD);
			case "keystrokes" -> new ItemStack(Items.OBSERVER);
			case "sprint_status" -> new ItemStack(Items.LEATHER_BOOTS);
			case "fps" -> new ItemStack(Items.CLOCK);
			case "ping" -> new ItemStack(Items.ENDER_PEARL);
			case "coordinates" -> new ItemStack(Items.MAP);
			case "speed" -> new ItemStack(Items.SUGAR);
			case "clock" -> new ItemStack(Items.CLOCK);
			case "biome" -> new ItemStack(Items.GRASS_BLOCK);
			case "direction" -> new ItemStack(Items.COMPASS);
			case "consumables" -> new ItemStack(Items.COOKED_BEEF);
			case "status_effects" -> new ItemStack(Items.POTION);
			case "survival" -> new ItemStack(Items.GOLDEN_APPLE);
			case "item_history" -> new ItemStack(Items.BUNDLE);
			case "item_counter" -> new ItemStack(Items.CHEST);
			case "crosshair" -> new ItemStack(Items.TARGET);
			case "mini_inventory" -> new ItemStack(Items.BARREL);
			default -> new ItemStack(Items.PAPER);
		};
	}

	private Component keybindTipForWidget(String widgetId) {
		return switch (widgetId) {
			case "item_counter" -> Component.translatable(
				"screen.better-huds.settings.keybind_info",
				Component.translatable("key.better-huds.item_counter_setup")
			);
			case "mini_inventory" -> Component.translatable(
				"screen.better-huds.settings.keybind_info",
				Component.translatable("key.better-huds.mini_inventory")
			);
			default -> null;
		};
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private void drawSimpleTooltip(GuiGraphics graphics, Component text, int mouseX, int mouseY) {
		String line = text.getString();
		int pad = 4;
		int tx = mouseX + 10;
		int ty = mouseY + 10;
		int tw = font.width(line) + pad * 2;
		int th = font.lineHeight + pad * 2;
		if (tx + tw > width - 4) {
			tx = Math.max(4, width - tw - 4);
		}
		if (ty + th > height - 4) {
			ty = Math.max(4, mouseY - th - 8);
		}
		graphics.fill(tx, ty, tx + tw, ty + th, 0xE0101010);
		graphics.fill(tx, ty, tx + tw, ty + 1, 0xFF80D8FF);
		graphics.fill(tx, ty + th - 1, tx + tw, ty + th, 0xFF80D8FF);
		graphics.fill(tx, ty, tx + 1, ty + th, 0xFF80D8FF);
		graphics.fill(tx + tw - 1, ty, tx + tw, ty + th, 0xFF80D8FF);
		graphics.drawString(font, line, tx + pad, ty + pad, 0xFFFFFFFF, false);
	}

	private record Card(HudWidget widget, ItemStack icon, int x, int y, int width, int height) {
	}

	private record ProfileBounds(int index, int x, int y, int width, int height) {
	}
}
