package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudSystem;
import amdev.bh.util.McCompat;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ItemCounterSetupScreen extends Screen {
	private final HudSystem hudSystem;
	private final List<ItemSuggestion> suggestions = new ArrayList<>();
	private String selectedItemId;
	private EditBox amountBox;
	private EditBox searchBox;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public ItemCounterSetupScreen(HudSystem hudSystem, String selectedItemId) {
		super(Component.translatable("screen.better-huds.item_counter_setup"));
		this.hudSystem = hudSystem;
		this.selectedItemId = selectedItemId == null ? "" : selectedItemId;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		panelWidth = 430;
		panelHeight = 292;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		BetterHudsConfig config = hudSystem.config();
		int currentTarget = Math.max(1, config.itemCounterTarget <= 0 ? 64 : config.itemCounterTarget);

		searchBox = new EditBox(font, panelX + 20, panelY + 100, 240, 20, Component.translatable("screen.better-huds.item_counter_search"));
		searchBox.setMaxLength(80);
		searchBox.setResponder(this::onSearchChanged);
		if (selectedItemId != null && !selectedItemId.isBlank()) {
			searchBox.setValue(selectedItemId);
		}
		addRenderableWidget(searchBox);

		amountBox = new EditBox(font, panelX + 280, panelY + 100, 120, 20, Component.translatable("screen.better-huds.item_counter_amount"));
		amountBox.setValue(Integer.toString(currentTarget));
		amountBox.setFilter(text -> text.matches("[0-9]*"));
		addRenderableWidget(amountBox);

		addRenderableWidget(new GlassButton(panelX + panelWidth - 88, panelY + 8, 74, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		addRenderableWidget(new GlassButton(panelX + 20, panelY + 132, 120, 20, Component.translatable("screen.better-huds.item_counter_use_held"), button -> {
			String heldId = detectHeldItemId();
			if (heldId.isBlank()) {
				return;
			}
			selectedItemId = heldId;
			searchBox.setValue(heldId);
			refreshSuggestions();
		}));
		addRenderableWidget(new GlassButton(panelX + 20, panelY + 258, 100, 20, Component.translatable("screen.better-huds.item_counter_clear"), button -> {
			BetterHudsConfig cfg = hudSystem.config();
			cfg.itemCounterItemId = "";
			cfg.itemCounterTarget = 0;
			hudSystem.configManager().save();
			onClose();
		}, 0xFFFF9AA2));
		addRenderableWidget(new GlassButton(panelX + panelWidth - 120, panelY + 258, 100, 20, Component.translatable("screen.better-huds.item_counter_save"), button -> {
			saveSelection();
			onClose();
		}));
		refreshSuggestions();
	}

	private void saveSelection() {
		if (selectedItemId.isBlank()) {
			return;
		}
		int target = parsePositive(amountBox.getValue(), 1);
		BetterHudsConfig cfg = hudSystem.config();
		cfg.itemCounterItemId = selectedItemId;
		cfg.itemCounterTarget = target;
		hudSystem.configManager().save();
	}

	private int parsePositive(String raw, int fallback) {
		try {
			int value = Integer.parseInt(raw.trim());
			return Math.max(1, value);
		} catch (Exception ignored) {
			return fallback;
		}
	}

	private void onSearchChanged(String ignored) {
		refreshSuggestions();
	}

	private void refreshSuggestions() {
		suggestions.clear();
		if (searchBox == null) {
			return;
		}
		String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
		if (query.isBlank()) {
			return;
		}

		List<ItemSuggestion> prefix = new ArrayList<>();
		List<ItemSuggestion> contains = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM) {
			if (item == Items.AIR) {
				continue;
			}
			String id = BuiltInRegistries.ITEM.getKey(item).toString();
			String name = McCompat.itemDisplayName(item);
			String idLower = id.toLowerCase(Locale.ROOT);
			String nameLower = name.toLowerCase(Locale.ROOT);
			if (idLower.startsWith(query) || nameLower.startsWith(query)) {
				prefix.add(new ItemSuggestion(id, name));
			} else if (idLower.contains(query) || nameLower.contains(query)) {
				contains.add(new ItemSuggestion(id, name));
			}
		}

		Comparator<ItemSuggestion> byId = Comparator.comparing(ItemSuggestion::itemId);
		prefix.sort(byId);
		contains.sort(byId);
		for (ItemSuggestion suggestion : prefix) {
			if (suggestions.size() >= 7) {
				break;
			}
			suggestions.add(suggestion);
		}
		for (ItemSuggestion suggestion : contains) {
			if (suggestions.size() >= 7) {
				break;
			}
			suggestions.add(suggestion);
		}
	}

	private String detectHeldItemId() {
		if (minecraft == null || minecraft.player == null) {
			return "";
		}
		ItemStack main = minecraft.player.getMainHandItem();
		if (!main.isEmpty() && main.getItem() != Items.AIR) {
			return BuiltInRegistries.ITEM.getKey(main.getItem()).toString();
		}
		ItemStack off = minecraft.player.getOffhandItem();
		Item item = off.getItem();
		if (item == null || item == Items.AIR) {
			return "";
		}
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private Item resolveSelectedItem() {
		return McCompat.findItemById(selectedItemId);
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreen(null);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return clickSuggestion(mouseX, mouseY);
	}

	private boolean clickSuggestion(double mouseX, double mouseY) {
		int listX = panelX + 20;
		int listY = panelY + 166;
		int listW = panelWidth - 40;
		int rowHeight = 16;
		for (int i = 0; i < suggestions.size(); i++) {
			int rowY = listY + i * rowHeight;
			if (mouseX < listX || mouseX >= listX + listW || mouseY < rowY || mouseY >= rowY + rowHeight) {
				continue;
			}
			ItemSuggestion selected = suggestions.get(i);
			selectedItemId = selected.itemId();
			searchBox.setValue(selected.itemId());
			refreshSuggestions();
			return true;
		}
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (McCompat.shouldDisableUiBlur()) {
			graphics.fill(0, 0, width, height, 0x66000000);
		} else {
			renderTransparentBackground(graphics);
		}
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.drawString(font, title, panelX + 16, panelY + 15, 0xFFFFFFFF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_hint"), panelX + 16, panelY + 31, 0xFFB6E3FF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_search"), panelX + 20, panelY + 88, 0xFFECECEC, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_amount"), panelX + 280, panelY + 88, 0xFFECECEC, false);

		Item item = resolveSelectedItem();
		graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_selected"), panelX + 20, panelY + 66, 0xFFECECEC, false);
		if (item != Items.AIR) {
			graphics.renderItem(new ItemStack(item), panelX + 152, panelY + 60);
			graphics.drawString(font, font.plainSubstrByWidth(McCompat.itemDisplayName(item), 240), panelX + 176, panelY + 64, 0xFFFFFFFF, false);
		} else {
			graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_none"), panelX + 152, panelY + 64, 0xFFAAAAAA, false);
		}

		int listX = panelX + 20;
		int listY = panelY + 166;
		int listW = panelWidth - 40;
		int rowHeight = 16;
		if (!suggestions.isEmpty()) {
			graphics.drawString(font, Component.translatable("screen.better-huds.item_counter_suggestions"), listX, listY - 12, 0xFFB6E3FF, false);
		}
		for (int i = 0; i < suggestions.size(); i++) {
			ItemSuggestion suggestion = suggestions.get(i);
			int rowY = listY + i * rowHeight;
			boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + rowHeight;
			graphics.fill(listX, rowY, listX + listW, rowY + rowHeight, hovered ? 0x66304555 : 0x33222222);
			graphics.drawString(font, font.plainSubstrByWidth(suggestion.itemId(), 185), listX + 4, rowY + 4, 0xFFFFFFFF, false);
			graphics.drawString(font, font.plainSubstrByWidth(suggestion.displayName(), 145), listX + 210, rowY + 4, 0xFFB6E3FF, false);
		}

		super.render(graphics, mouseX, mouseY, tickDelta);
	}

	private record ItemSuggestion(String itemId, String displayName) {
	}
}

