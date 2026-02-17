package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudSystem;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ProfileRenameScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private final int profileIndex;
	private EditBox nameBox;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public ProfileRenameScreen(HudSystem hudSystem, Screen parent, int profileIndex) {
		super(Component.translatable("screen.better-huds.settings.profile_rename_title"));
		this.hudSystem = hudSystem;
		this.parent = parent;
		this.profileIndex = profileIndex;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		panelWidth = 320;
		panelHeight = 140;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		BetterHudsConfig config = hudSystem.config();
		String current = "";
		if (profileIndex >= 0 && profileIndex < config.profiles.size()) {
			current = config.profiles.get(profileIndex).name;
		}

		nameBox = new EditBox(font, panelX + 16, panelY + 50, panelWidth - 32, 20, Component.translatable("screen.better-huds.settings.profile_name"));
		nameBox.setMaxLength(32);
		nameBox.setValue(current == null ? "" : current);
		addRenderableWidget(nameBox);
		setInitialFocus(nameBox);

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 80, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + panelHeight - 28, 80, 20, Component.translatable("screen.better-huds.item_counter_save"), button -> {
			saveName();
			onClose();
		}));
	}

	private void saveName() {
		BetterHudsConfig config = hudSystem.config();
		if (profileIndex < 0 || profileIndex >= config.profiles.size()) {
			return;
		}
		String next = nameBox.getValue().trim();
		if (next.isBlank()) {
			next = "Profile " + (profileIndex + 1);
		}
		config.profiles.get(profileIndex).name = next;
		hudSystem.configManager().save();
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		renderTransparentBackground(graphics);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.drawString(font, title, panelX + 16, panelY + 16, 0xFFFFFFFF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.profile_name"), panelX + 16, panelY + 38, 0xFFB6E3FF, false);
		super.render(graphics, mouseX, mouseY, tickDelta);
	}
}
