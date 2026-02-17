package amdev.bh.ui;

import amdev.bh.hud.HudSystem;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class HudKeybindsScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public HudKeybindsScreen(HudSystem hudSystem, Screen parent) {
		super(Component.translatable("screen.better-huds.settings.keybinds_title"));
		this.hudSystem = hudSystem;
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		panelWidth = 430;
		panelHeight = 180;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 80, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
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
		graphics.drawString(font, title, panelX + 14, panelY + 14, 0xFFFFFFFF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.keybinds_hint"), panelX + 14, panelY + 30, 0xFFB6E3FF, false);

		List<HudSystem.HudKeybind> keybinds = hudSystem.keybinds();
		int y = panelY + 56;
		for (HudSystem.HudKeybind keybind : keybinds) {
			String label = Component.translatable(keybind.nameKey()).getString();
			String key = keybind.keyMapping() != null ? keybind.keyMapping().getTranslatedKeyMessage().getString() : "?";
			graphics.drawString(font, label, panelX + 16, y, 0xFFFFFFFF, false);
			graphics.drawString(font, key, panelX + panelWidth - 120, y, 0xFFDAFFC2, false);
			y += 20;
		}

		super.render(graphics, mouseX, mouseY, tickDelta);
	}
}
