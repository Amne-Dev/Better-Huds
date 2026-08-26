package amdev.bh.ui;

import amdev.bh.hud.HudSystem;
import amdev.bh.ui.widget.GlassButton;
import amdev.bh.util.McCompat;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ProfileImportScreen extends Screen {
	private final HudSystem hudSystem;
	private final HudSettingsScreen parent;
	private EditBox inputBox;
	private Component status = Component.empty();
	private int statusColor = 0xFFB6E3FF;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public ProfileImportScreen(HudSystem hudSystem, HudSettingsScreen parent) {
		super(Component.translatable("screen.better-huds.settings.profile_import_title"));
		this.hudSystem = hudSystem;
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		panelWidth = Math.min(640, width - 20);
		panelHeight = 190;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		inputBox = new EditBox(font, panelX + 14, panelY + 58, panelWidth - 28, 20, Component.translatable("screen.better-huds.settings.profile_import_input"));
		inputBox.setMaxLength(32767);
		addRenderableWidget(inputBox);
		setInitialFocus(inputBox);

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 10, 78, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		addRenderableWidget(new GlassButton(panelX + panelWidth - 94, panelY + panelHeight - 30, 82, 20, Component.translatable("screen.better-huds.settings.import_profile"), button -> tryImport()));
	}

	private void tryImport() {
		String raw = inputBox.getValue();
		if (raw == null || raw.isBlank()) {
			status = Component.translatable("screen.better-huds.settings.profile_import_paste_hint");
			statusColor = 0xFFFFA0A0;
			return;
		}
		boolean imported = parent.importProfileFromText(raw);
		if (!imported) {
			status = Component.translatable("screen.better-huds.settings.profile_import_failed");
			statusColor = 0xFFFF8080;
			return;
		}
		if (minecraft != null) {
			McCompat.setScreen(minecraft, parent);
		}
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			McCompat.setScreen(minecraft, parent);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
		if (amdev.bh.util.McCompat.shouldDisableUiBlur()) {
			graphics.fill(0, 0, width, height, 0x66000000);
		} else {
			extractTransparentBackground(graphics);
		}
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.text(font, title, panelX + 14, panelY + 16, 0xFFFFFFFF, false);
		graphics.text(font, Component.translatable("screen.better-huds.settings.profile_import_hint"), panelX + 14, panelY + 38, 0xFFB6E3FF, false);
		graphics.text(font, Component.translatable("screen.better-huds.settings.profile_import_paste_hint"), panelX + 14, panelY + 82, 0xFFB6E3FF, false);
		if (!status.getString().isBlank()) {
			graphics.text(font, status, panelX + 14, panelY + panelHeight - 24, statusColor, false);
		}
		super.extractRenderState(graphics, mouseX, mouseY, tickDelta);
	}
}

