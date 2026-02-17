package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudSystem;
import amdev.bh.hud.HudWidget;
import amdev.bh.hud.widget.WidgetRenderUtil;
import amdev.bh.ui.widget.GlassButton;
import amdev.bh.ui.widget.GlassSlider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class HudGlobalSettingsScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private final List<LabelBinding> labelBindings = new ArrayList<>();
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public HudGlobalSettingsScreen(HudSystem hudSystem, Screen parent) {
		super(Component.translatable("screen.better-huds.settings.global_title"));
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
		labelBindings.clear();

		panelWidth = Math.min(420, width - 20);
		panelHeight = 216;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 80, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));

		int y = panelY + 42;
		addBoundButton(panelX + 14, y, 120,
			() -> Component.translatable("screen.better-huds.settings.all_enabled", allEnabled() ? "ON" : "OFF"),
			this::toggleAllEnabled);
		addBoundButton(panelX + 142, y, 120,
			() -> Component.translatable("screen.better-huds.settings.all_text", allTextVisible() ? "ON" : "OFF"),
			this::toggleAllText);
		addBoundButton(panelX + 270, y, 120,
			() -> Component.translatable("screen.better-huds.settings.all_bg", allBackgroundEnabled() ? "ON" : "OFF"),
			this::toggleAllBackground);

		y += 28;
		addBoundButton(panelX + 14, y, 184,
			() -> Component.translatable("screen.better-huds.settings.all_text_color", WidgetRenderUtil.shortColor(referenceTextColor())),
			() -> {
				if (minecraft != null) {
					minecraft.setScreen(new ColorPickerScreen(this, Component.translatable("screen.better-huds.settings.all_text_color", ""), referenceTextColor(), color -> {
						applyAllTextColor(color);
					}));
				}
			});
		addBoundButton(panelX + 206, y, 184,
			() -> Component.translatable("screen.better-huds.settings.all_bg_color", WidgetRenderUtil.shortColor(referenceBackgroundColor())),
			() -> {
				if (minecraft != null) {
					minecraft.setScreen(new ColorPickerScreen(this, Component.translatable("screen.better-huds.settings.all_bg_color", ""), referenceBackgroundColor(), color -> {
						applyAllBackgroundColor(color);
					}));
				}
			});

		y += 28;
		addRenderableWidget(new GlassSlider(
			panelX + 14,
			y,
			panelWidth - 28,
			20,
			hudSystem.config().chromaSpeed,
			20,
			600,
			5,
			value -> Component.translatable("screen.better-huds.settings.global_chroma_speed", Integer.toString((int) Math.round(value))),
			value -> {
				hudSystem.config().chromaSpeed = (int) Math.round(value);
				WidgetRenderUtil.setChromaSpeed(hudSystem.config().chromaSpeed);
				hudSystem.configManager().save();
			}
		));
	}

	private void addBoundButton(int x, int y, int width, Supplier<Component> labelSupplier, Runnable action) {
		GlassButton button = new GlassButton(x, y, width, 20, labelSupplier.get(), btn -> {
				action.run();
				hudSystem.configManager().save();
				refreshButtonLabels();
			});
		labelBindings.add(new LabelBinding(button, labelSupplier));
		addRenderableWidget(button);
	}

	private void refreshButtonLabels() {
		for (LabelBinding binding : labelBindings) {
			binding.button().setMessage(binding.labelSupplier().get());
		}
	}

	private BetterHudsConfig.WidgetConfig getCfg(String widgetId) {
		return hudSystem.config().getOrCreateWidgetConfig(widgetId);
	}

	private boolean allEnabled() {
		for (HudWidget widget : hudSystem.widgets()) {
			if (!getCfg(widget.id()).enabled) {
				return false;
			}
		}
		return true;
	}

	private boolean allTextVisible() {
		for (HudWidget widget : hudSystem.widgets()) {
			if (!getCfg(widget.id()).showText()) {
				return false;
			}
		}
		return true;
	}

	private boolean allBackgroundEnabled() {
		for (HudWidget widget : hudSystem.widgets()) {
			if (!getCfg(widget.id()).background) {
				return false;
			}
		}
		return true;
	}

	private int referenceTextColor() {
		List<HudWidget> widgets = hudSystem.widgets();
		if (widgets.isEmpty()) {
			return WidgetRenderUtil.TEXT_PALETTE[0];
		}
		return getCfg(widgets.get(0).id()).textColor;
	}

	private int referenceBackgroundColor() {
		List<HudWidget> widgets = hudSystem.widgets();
		if (widgets.isEmpty()) {
			return WidgetRenderUtil.BACKGROUND_PALETTE[0];
		}
		return getCfg(widgets.get(0).id()).backgroundColor;
	}

	private void toggleAllEnabled() {
		boolean enable = !allEnabled();
		for (HudWidget widget : hudSystem.widgets()) {
			getCfg(widget.id()).enabled = enable;
		}
	}

	private void toggleAllText() {
		boolean show = !allTextVisible();
		for (HudWidget widget : hudSystem.widgets()) {
			getCfg(widget.id()).showText = show;
		}
	}

	private void toggleAllBackground() {
		boolean show = !allBackgroundEnabled();
		for (HudWidget widget : hudSystem.widgets()) {
			getCfg(widget.id()).background = show;
		}
	}

	private void applyAllTextColor(int next) {
		for (HudWidget widget : hudSystem.widgets()) {
			getCfg(widget.id()).textColor = next;
		}
	}

	private void applyAllBackgroundColor(int next) {
		for (HudWidget widget : hudSystem.widgets()) {
			getCfg(widget.id()).backgroundColor = next;
		}
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
		graphics.drawString(font, title, panelX + 14, panelY + 14, 0xFFFFFFFF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.global_hint"), panelX + 14, panelY + 30, 0xFFB6E3FF, false);
		super.render(graphics, mouseX, mouseY, tickDelta);
	}

	private record LabelBinding(GlassButton button, Supplier<Component> labelSupplier) {
	}
}
