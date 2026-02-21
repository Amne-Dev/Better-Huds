package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.Anchor;
import amdev.bh.hud.HudRenderContext;
import amdev.bh.hud.HudSystem;
import amdev.bh.hud.HudWidget;
import amdev.bh.hud.widget.CrosshairPatternUtil;
import amdev.bh.ui.widget.GlassButton;
import amdev.bh.ui.widget.GlassSlider;
import amdev.bh.hud.widget.WidgetRenderUtil;
import amdev.bh.util.PoseCompat;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class WidgetSettingsScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private final String widgetId;
	private final List<LabelBinding> labelBindings = new ArrayList<>();
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int controlsX;
	private int controlsWidth;
	private int previewX;
	private int previewY;
	private int previewWidth;
	private int previewHeight;
	private int appearanceSectionY;
	private int behaviorSectionY;
	private int appearanceSectionEndY;
	private int behaviorSectionEndY;
	private final List<ScrollableControlBinding> scrollableControls = new ArrayList<>();
	private int controlsViewportTop;
	private int controlsViewportBottom;
	private int controlsViewportLeft;
	private int controlsViewportRight;
	private int controlsContentTop;
	private int controlsContentBottom;
	private int controlsScroll;
	private int controlsScrollMax;
	private GlassButton rainbowTextButton;
	private final List<ColorChipBinding> colorChipBindings = new ArrayList<>();

	public WidgetSettingsScreen(HudSystem hudSystem, Screen parent, String widgetId) {
		super(Component.translatable("screen.better-huds.settings.widget_title"));
		this.hudSystem = hudSystem;
		this.parent = parent;
		this.widgetId = widgetId;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		clearWidgets();
		labelBindings.clear();
		colorChipBindings.clear();
		scrollableControls.clear();
		rainbowTextButton = null;
		controlsScroll = 0;
		if ("keystrokes".equals(widgetId) || "crosshair".equals(widgetId)) {
			cfg().background = false;
		}

		panelWidth = Math.min(760, width - 20);
		panelHeight = Math.min(430, height - 20);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		controlsX = panelX + 14;
		controlsWidth = 372;
		previewX = controlsX + controlsWidth + 10;
		previewY = panelY + 64;
		previewWidth = panelX + panelWidth - previewX - 14;
		previewHeight = panelHeight - 78;
		appearanceSectionY = panelY + 62;
		behaviorSectionY = panelY + 210;
		controlsViewportLeft = controlsX - 8;
		controlsViewportRight = controlsX + controlsWidth + 4;
		controlsViewportTop = panelY + 42;
		controlsViewportBottom = panelY + panelHeight - 12;
		controlsContentTop = Integer.MAX_VALUE;
		controlsContentBottom = Integer.MIN_VALUE;

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 80, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));

		appearanceSectionEndY = addAppearanceSection(appearanceSectionY);
		behaviorSectionEndY = addBehaviorSection(behaviorSectionY);
		controlsContentTop = Math.min(controlsContentTop, panelY + 30);
		controlsContentBottom = Math.max(controlsContentBottom, Math.max(appearanceSectionEndY, behaviorSectionEndY) + 12);
		recalculateControlsScrollBounds();
		applyControlsScroll();
	}

	private int addAppearanceSection(int y) {
		boolean keystrokes = "keystrokes".equals(widgetId);
		addBoundButton(controlsX, y, 118,
			() -> Component.translatable("screen.better-huds.settings.text", cfg().showText() ? "ON" : "OFF"),
			() -> cfg().showText = !cfg().showText);
		addBoundButton(controlsX + 126, y, 118,
			() -> keystrokes
				? Component.translatable("screen.better-huds.settings.ks_key_background", cfg().toggle("ks_key_background", true) ? "ON" : "OFF")
				: Component.translatable("screen.better-huds.settings.bg", cfg().background ? "ON" : "OFF"),
			() -> {
				if (keystrokes) {
					cfg().setToggle("ks_key_background", !cfg().toggle("ks_key_background", true));
				} else {
					cfg().background = !cfg().background;
				}
			});

		y += 28;
		addColorButton(controlsX, y, 180,
			() -> Component.translatable("screen.better-huds.settings.text_color", WidgetRenderUtil.shortColor(cfg().textColor)),
			() -> {
				if (minecraft != null) {
					minecraft.setScreen(new ColorPickerScreen(this, Component.translatable("screen.better-huds.settings.text_color", ""), cfg().textColor, color -> {
						cfg().textColor = color;
						cfg().touch();
					}));
				}
			},
			() -> cfg().textColor
		);
		addColorButton(controlsX + 188, y, 180,
			() -> keystrokes
				? Component.translatable("screen.better-huds.settings.ks_key_bg_color", WidgetRenderUtil.shortColor(cfg().backgroundColor))
				: Component.translatable("screen.better-huds.settings.bg_color", WidgetRenderUtil.shortColor(cfg().backgroundColor)),
			() -> {
				if (minecraft != null) {
					Component title = keystrokes
						? Component.translatable("screen.better-huds.settings.ks_key_bg_color", "")
						: Component.translatable("screen.better-huds.settings.bg_color", "");
					minecraft.setScreen(new ColorPickerScreen(this, title, cfg().backgroundColor, color -> {
						cfg().backgroundColor = color;
						cfg().touch();
					}));
				}
			},
			() -> cfg().backgroundColor
		);

		if (keystrokes) {
			y += 28;
			addColorButton(controlsX, y, 180,
				() -> Component.translatable("screen.better-huds.settings.ks_pressed_text_color", WidgetRenderUtil.shortColor(cfg().intValue("ks_pressed_text_color", 0xFFFFFFFF))),
				() -> {
					if (minecraft != null) {
						int current = cfg().intValue("ks_pressed_text_color", 0xFFFFFFFF);
						minecraft.setScreen(new ColorPickerScreen(this, Component.translatable("screen.better-huds.settings.ks_pressed_text_color", ""), current, color -> {
							cfg().setIntValue("ks_pressed_text_color", color);
						}));
					}
				},
				() -> cfg().intValue("ks_pressed_text_color", 0xFFFFFFFF)
			);
			addColorButton(controlsX + 188, y, 180,
				() -> Component.translatable("screen.better-huds.settings.ks_pressed_bg_color", WidgetRenderUtil.shortColor(cfg().intValue("ks_pressed_bg_color", cfg().backgroundColor))),
				() -> {
					if (minecraft != null) {
						int current = cfg().intValue("ks_pressed_bg_color", cfg().backgroundColor);
						minecraft.setScreen(new ColorPickerScreen(this, Component.translatable("screen.better-huds.settings.ks_pressed_bg_color", ""), current, color -> {
							cfg().setIntValue("ks_pressed_bg_color", color);
						}));
					}
				},
				() -> cfg().intValue("ks_pressed_bg_color", cfg().backgroundColor)
			);
		}

		y += 28;
		rainbowTextButton = addBoundButton(controlsX, y, 368,
			() -> Component.translatable("screen.better-huds.settings.rainbow_text", cfg().toggle("rainbow_text", false) ? "ON" : "OFF"),
			() -> cfg().setToggle("rainbow_text", !cfg().toggle("rainbow_text", false)));
		y += 28;
		addFloatSliderRow(
			y,
			240,
			0.5D,
			2.5D,
			0.05D,
			() -> (double) cfg().scale,
			next -> cfg().scale = round2((float) next),
			value -> Component.translatable("screen.better-huds.settings.scale", new Object[]{String.format(Locale.ROOT, "%.2f", value)})
		);
		addBoundButton(controlsX + 248, y, 120, () -> Component.translatable("screen.better-huds.settings.reset_style"), this::resetStyle);
		return y + 20;
	}

	private int addBehaviorSection(int yStart) {
		int y = yStart;
		if ("armor".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable(
					"screen.better-huds.settings.armor_orientation",
					cfg().toggle("orientation_vertical", false)
						? Component.translatable("screen.better-huds.settings.mode_vertical").getString()
						: Component.translatable("screen.better-huds.settings.mode_horizontal").getString()
				),
				() -> cfg().setToggle("orientation_vertical", !cfg().toggle("orientation_vertical", false)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.alignment", alignmentLabel()),
				this::toggleAlignment);
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.armor_hand_mode", handModeLabel()),
				this::cycleHandMode);
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.armor_hand_text", cfg().toggle("show_hand_text", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("show_hand_text", !cfg().toggle("show_hand_text", true)));
			return y + 20;
		}
		if ("keystrokes".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_show_mouse", cfg().toggle("ks_show_mouse", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("ks_show_mouse", !cfg().toggle("ks_show_mouse", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_show_cps", cfg().toggle("show_cps", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("show_cps", !cfg().toggle("show_cps", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_compact", cfg().toggle("ks_compact", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("ks_compact", !cfg().toggle("ks_compact", false)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_neon_style", cfg().toggle("ks_neon_style", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("ks_neon_style", !cfg().toggle("ks_neon_style", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_show_modifiers", cfg().toggle("ks_show_modifiers", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("ks_show_modifiers", !cfg().toggle("ks_show_modifiers", false)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ks_symbol_labels", cfg().toggle("ks_symbol_labels", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("ks_symbol_labels", !cfg().toggle("ks_symbol_labels", false)));
			y += 26;
			addIntSliderRow(
				y,
				14,
				34,
				1,
				() -> cfg().intValue("ks_key_size", cfg().toggle("ks_compact", false) ? 18 : 22),
				next -> cfg().setIntValue("ks_key_size", next),
				value -> Component.translatable("screen.better-huds.settings.ks_key_size", Integer.toString(value))
			);
			y += 26;
			addIntSliderRow(
				y,
				0,
				24,
				1,
				() -> cfg().intValue("ks_spacing", cfg().toggle("ks_compact", false) ? 6 : 8),
				next -> cfg().setIntValue("ks_spacing", next),
				value -> Component.translatable("screen.better-huds.settings.ks_spacing", Integer.toString(value))
			);
			y += 26;
			addIntSliderRow(
				y,
				0,
				24,
				1,
				() -> cfg().intValue("ks_padding", cfg().toggle("ks_compact", false) ? 4 : 6),
				next -> cfg().setIntValue("ks_padding", next),
				value -> Component.translatable("screen.better-huds.settings.ks_padding", Integer.toString(value))
			);
			return y + 20;
		}
		if ("crosshair".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_use_drawn", CrosshairPatternUtil.useDrawnPattern(cfg()) ? "ON" : "OFF"),
				() -> CrosshairPatternUtil.setUseDrawnPattern(cfg(), !CrosshairPatternUtil.useDrawnPattern(cfg())));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_invert", cfg().toggle("crosshair_invert", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("crosshair_invert", !cfg().toggle("crosshair_invert", false)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_dot", cfg().toggle("crosshair_dot", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("crosshair_dot", !cfg().toggle("crosshair_dot", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_outline", cfg().toggle("crosshair_outline", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("crosshair_outline", !cfg().toggle("crosshair_outline", true)));
			y += 26;
			addIntSliderRow(
				y,
				0,
				24,
				1,
				() -> cfg().intValue("crosshair_gap", 3),
				next -> cfg().setIntValue("crosshair_gap", next),
				value -> Component.translatable("screen.better-huds.settings.crosshair_gap", Integer.toString(value))
			);
			y += 26;
			addIntSliderRow(
				y,
				1,
				40,
				1,
				() -> cfg().intValue("crosshair_length", 6),
				next -> cfg().setIntValue("crosshair_length", next),
				value -> Component.translatable("screen.better-huds.settings.crosshair_length", Integer.toString(value))
			);
			y += 26;
			addIntSliderRow(
				y,
				1,
				8,
				1,
				() -> cfg().intValue("crosshair_thickness", 2),
				next -> cfg().setIntValue("crosshair_thickness", next),
				value -> Component.translatable("screen.better-huds.settings.crosshair_thickness", Integer.toString(value))
			);
			y += 26;
			addIntSliderRow(
				y,
				1,
				8,
				1,
				() -> cfg().intValue("crosshair_pixel_size", 2),
				next -> cfg().setIntValue("crosshair_pixel_size", next),
				value -> Component.translatable("screen.better-huds.settings.crosshair_pixel_size", Integer.toString(value))
			);
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_draw_editor"),
				() -> {
					if (minecraft != null) {
						minecraft.setScreen(new CrosshairDrawScreen(hudSystem, this));
					}
				},
				false);
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.crosshair_recenter"),
				this::recenterCrosshair);
			return y + 20;
		}
		if ("status_effects".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.effects_compact", cfg().toggle("compact_mode", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("compact_mode", !cfg().toggle("compact_mode", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.alignment", alignmentLabel()),
				this::toggleAlignment);
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.effects_icons", cfg().toggle("effects_icons", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("effects_icons", !cfg().toggle("effects_icons", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.effects_hide_time", cfg().toggle("effects_hide_time", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("effects_hide_time", !cfg().toggle("effects_hide_time", false)));
			return y + 20;
		}
		if ("fps".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.fps_extended", cfg().toggle("fps_extended", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("fps_extended", !cfg().toggle("fps_extended", true)));
			return y + 20;
		}
		if ("ping".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.ping_colorize", cfg().toggle("ping_colorize", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("ping_colorize", !cfg().toggle("ping_colorize", true)));
			return y + 20;
		}
		if ("coordinates".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.coords_decimals", cfg().toggle("coords_decimals", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("coords_decimals", !cfg().toggle("coords_decimals", true)));
			return y + 20;
		}
		if ("speed".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.speed_precise", cfg().toggle("speed_precise", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("speed_precise", !cfg().toggle("speed_precise", false)));
			return y + 20;
		}
		if ("clock".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.clock_real", cfg().toggle("clock_real", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("clock_real", !cfg().toggle("clock_real", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.clock_game", cfg().toggle("clock_game", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("clock_game", !cfg().toggle("clock_game", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.clock_24h", cfg().toggle("clock_24h", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("clock_24h", !cfg().toggle("clock_24h", true)));
			return y + 20;
		}
		if ("biome".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.biome_show_light", cfg().toggle("biome_show_light", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("biome_show_light", !cfg().toggle("biome_show_light", true)));
			return y + 20;
		}
		if ("mini_inventory".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.mini_inv_hotbar", cfg().toggle("mini_show_hotbar", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("mini_show_hotbar", !cfg().toggle("mini_show_hotbar", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable(
					"screen.better-huds.settings.mini_inv_hold_mode",
					cfg().toggle("mini_hold_mode", false)
						? Component.translatable("screen.better-huds.settings.mode_hold").getString()
						: Component.translatable("screen.better-huds.settings.mode_toggle").getString()
				),
				() -> cfg().setToggle("mini_hold_mode", !cfg().toggle("mini_hold_mode", false)));
			return y + 20;
		}
		if ("direction".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.direction_labels", cfg().toggle("direction_labels", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("direction_labels", !cfg().toggle("direction_labels", true)));
			return y + 20;
		}
		if ("sprint_status".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.sprint_show_sneak", cfg().toggle("sprint_show_sneak", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("sprint_show_sneak", !cfg().toggle("sprint_show_sneak", true)));
			return y + 20;
		}
		if ("survival".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.survival_show_counts", cfg().toggle("survival_show_counts", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("survival_show_counts", !cfg().toggle("survival_show_counts", true)));
			return y + 20;
		}
		if ("item_history".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.history_compact", cfg().toggle("history_compact", false) ? "ON" : "OFF"),
				() -> cfg().setToggle("history_compact", !cfg().toggle("history_compact", false)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.alignment", alignmentLabel()),
				this::toggleAlignment);
			return y + 20;
		}
		if ("item_counter".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.counter_show_bar", cfg().toggle("counter_show_bar", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("counter_show_bar", !cfg().toggle("counter_show_bar", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.alignment", alignmentLabel()),
				this::toggleAlignment);
			return y + 20;
		}
		if ("consumables".equals(widgetId)) {
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.consumables_food", cfg().toggle("consumables_food", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("consumables_food", !cfg().toggle("consumables_food", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.consumables_potions", cfg().toggle("consumables_potions", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("consumables_potions", !cfg().toggle("consumables_potions", true)));
			y += 26;
			addBoundButton(controlsX, y, 368,
				() -> Component.translatable("screen.better-huds.settings.consumables_compact", cfg().toggle("consumables_compact", true) ? "ON" : "OFF"),
				() -> cfg().setToggle("consumables_compact", !cfg().toggle("consumables_compact", true)));
			return y + 20;
		}
		return y;
	}

	private GlassButton addBoundButton(int x, int y, int width, Supplier<Component> labelSupplier, Runnable action) {
		return addBoundButton(x, y, width, labelSupplier, action, true);
	}

	private GlassButton addBoundButton(int x, int y, int width, Supplier<Component> labelSupplier, Runnable action, boolean markModified) {
		GlassButton button = new GlassButton(x, y, width, 20, labelSupplier.get(), btn -> {
			action.run();
			if (markModified) {
				cfg().touch();
			}
			hudSystem.configManager().save();
			refreshButtonLabels();
		});
		labelBindings.add(new LabelBinding(button, labelSupplier));
		addRenderableWidget(button);
		registerScrollableControl(button, y);
		return button;
	}

	private GlassButton addColorButton(int x, int y, int width, Supplier<Component> labelSupplier, Runnable action, IntSupplier colorSupplier) {
		GlassButton button = addBoundButton(x, y, width, labelSupplier, action);
		colorChipBindings.add(new ColorChipBinding(button, colorSupplier));
		return button;
	}

	private void refreshButtonLabels() {
		for (LabelBinding binding : labelBindings) {
			binding.button().setMessage(binding.labelSupplier().get());
		}
	}

	private void addIntSliderRow(int y, int min, int max, int step, Supplier<Integer> getter, java.util.function.IntConsumer setter, java.util.function.Function<Integer, Component> messageFactory) {
		GlassSlider slider = new GlassSlider(
			controlsX,
			y,
			368,
			20,
			getter.get(),
			min,
			max,
			step,
			value -> messageFactory.apply((int) Math.round(value)),
			value -> {
				setter.accept((int) Math.round(value));
				cfg().touch();
				hudSystem.configManager().save();
			}
		);
		addRenderableWidget(slider);
		registerScrollableControl(slider, y);
	}

	private void addFloatSliderRow(int y, int width, double min, double max, double step, Supplier<Double> getter, java.util.function.DoubleConsumer setter, java.util.function.Function<Double, Component> messageFactory) {
		GlassSlider slider = new GlassSlider(
			controlsX,
			y,
			width,
			20,
			getter.get(),
			min,
			max,
			step,
			messageFactory,
			value -> {
				setter.accept(value);
				cfg().touch();
				hudSystem.configManager().save();
			}
		);
		addRenderableWidget(slider);
		registerScrollableControl(slider, y);
	}

	private void registerScrollableControl(AbstractWidget widget, int baseY) {
		scrollableControls.add(new ScrollableControlBinding(widget, baseY));
		controlsContentTop = Math.min(controlsContentTop, baseY);
		controlsContentBottom = Math.max(controlsContentBottom, baseY + widget.getHeight());
	}

	private void recalculateControlsScrollBounds() {
		if (controlsContentTop == Integer.MAX_VALUE || controlsContentBottom <= controlsContentTop) {
			controlsScrollMax = 0;
			controlsScroll = 0;
			return;
		}
		int contentHeight = controlsContentBottom - controlsContentTop;
		int viewportHeight = Math.max(1, controlsViewportBottom - controlsViewportTop);
		controlsScrollMax = Math.max(0, contentHeight - viewportHeight);
		controlsScroll = clamp(controlsScroll, 0, controlsScrollMax);
	}

	private void applyControlsScroll() {
		for (ScrollableControlBinding binding : scrollableControls) {
			AbstractWidget widget = binding.widget();
			int y = binding.baseY() - controlsScroll;
			widget.setY(y);
			boolean visible = y >= controlsViewportTop && y + widget.getHeight() <= controlsViewportBottom;
			widget.visible = visible;
			widget.active = visible;
		}
	}

	private BetterHudsConfig.WidgetConfig cfg() {
		return hudSystem.config().getOrCreateWidgetConfig(widgetId);
	}

	private void resetStyle() {
		BetterHudsConfig.WidgetConfig cfg = cfg();
		cfg.textColor = 0xFFFFFFFF;
		cfg.background = true;
		cfg.backgroundColor = 0xFF888888;
		cfg.showText = true;
		cfg.scale = 1.0F;
	}

	private void recenterCrosshair() {
		BetterHudsConfig.WidgetConfig cfg = cfg();
		cfg.anchor = Anchor.CENTER;
		cfg.x = 0;
		cfg.y = 0;
	}

	private float round2(float value) {
		return Math.round(value * 100.0F) / 100.0F;
	}

	private String handModeLabel() {
		boolean showHands = cfg().toggle("show_hands", true);
		boolean separateHands = cfg().toggle("separate_hands", false);
		if (!showHands) {
			return Component.translatable("screen.better-huds.settings.mode_off").getString();
		}
		return separateHands
			? Component.translatable("screen.better-huds.settings.mode_separate").getString()
			: Component.translatable("screen.better-huds.settings.mode_alongside").getString();
	}

	private void cycleHandMode() {
		boolean showHands = cfg().toggle("show_hands", true);
		boolean separateHands = cfg().toggle("separate_hands", false);

		if (!showHands) {
			cfg().setToggle("show_hands", true);
			cfg().setToggle("separate_hands", false);
			return;
		}
		if (!separateHands) {
			cfg().setToggle("show_hands", true);
			cfg().setToggle("separate_hands", true);
			return;
		}
		cfg().setToggle("show_hands", false);
		cfg().setToggle("separate_hands", false);
	}

	private void toggleAlignment() {
		cfg().setToggle("align_right", !cfg().toggle("align_right", false));
	}

	private String alignmentLabel() {
		return cfg().toggle("align_right", false)
			? Component.translatable("screen.better-huds.settings.mode_right").getString()
			: Component.translatable("screen.better-huds.settings.mode_left").getString();
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
		if (amdev.bh.util.McCompat.shouldDisableUiBlur()) {
			graphics.fill(0, 0, width, height, 0x66000000);
		} else {
			renderTransparentBackground(graphics);
		}
		applyControlsScroll();
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.fill(previewX - 8, panelY + 38, previewX - 7, panelY + panelHeight - 8, 0x5577AACC);

		HudWidget widget = hudSystem.widget(widgetId);
		String widgetName = widget != null ? widget.displayName().getString() : widgetId;
		graphics.drawString(font, title, panelX + 14, panelY + 14, 0xFFFFFFFF, false);
		graphics.drawString(font, widgetName, panelX + 160, panelY + 14, 0xFFB6E3FF, false);

		int appearanceCardHeight = Math.max(72, appearanceSectionEndY - appearanceSectionY + 26);
		int behaviorCardHeight = Math.max(72, behaviorSectionEndY - behaviorSectionY + 26);
		drawScrollableSectionCard(graphics, controlsX - 8, appearanceSectionY - 14, controlsWidth + 4, appearanceCardHeight);
		drawScrollableSectionCard(graphics, controlsX - 8, behaviorSectionY - 14, controlsWidth + 4, behaviorCardHeight);
		drawScrollableLeftLabel(graphics, Component.translatable("screen.better-huds.settings.section_appearance"), controlsX, panelY + 48, 0xFFB6E3FF);
		drawScrollableLeftLabel(graphics, Component.translatable("screen.better-huds.settings.section_behavior"), controlsX, panelY + 196, 0xFFB6E3FF);
		graphics.drawString(font, Component.translatable("screen.better-huds.settings.section_preview"), previewX, panelY + 48, 0xFFB6E3FF, false);
		drawScrollableLeftLabel(graphics, Component.translatable("screen.better-huds.settings.widget_hint"), controlsX, panelY + 30, 0xFFB6E3FF);

		renderWidgetPreview(graphics, widget);
		super.render(graphics, mouseX, mouseY, tickDelta);
		for (ColorChipBinding binding : colorChipBindings) {
			if (!binding.button().visible) {
				continue;
			}
			drawColorChip(graphics, binding.button(), binding.colorSupplier().getAsInt());
		}
		drawControlsScrollBar(graphics);
		if (rainbowTextButton != null && rainbowTextButton.isHoveredOrFocused()) {
			drawSimpleTooltip(graphics, Component.translatable("screen.better-huds.settings.rainbow_tooltip"), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isInsideControlsViewport(mouseX, mouseY) || controlsScrollMax <= 0) {
			return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		double amount = verticalAmount != 0.0D ? verticalAmount : horizontalAmount;
		if (amount == 0.0D) {
			return true;
		}
		int next = clamp((int) Math.round(controlsScroll - amount * 18.0D), 0, controlsScrollMax);
		if (next != controlsScroll) {
			controlsScroll = next;
			applyControlsScroll();
		}
		return true;
	}

	private boolean isInsideControlsViewport(double mouseX, double mouseY) {
		return mouseX >= controlsViewportLeft
			&& mouseX <= controlsViewportRight
			&& mouseY >= controlsViewportTop
			&& mouseY <= controlsViewportBottom;
	}

	private void drawScrollableSectionCard(GuiGraphics graphics, int x, int baseY, int width, int height) {
		int y = baseY - controlsScroll;
		if (y + height < controlsViewportTop || y > controlsViewportBottom) {
			return;
		}
		drawSectionCard(graphics, x, y, width, height);
	}

	private void drawScrollableLeftLabel(GuiGraphics graphics, Component text, int x, int baseY, int color) {
		int y = baseY - controlsScroll;
		if (y + font.lineHeight < controlsViewportTop || y > controlsViewportBottom) {
			return;
		}
		graphics.drawString(font, text, x, y, color, false);
	}

	private void drawControlsScrollBar(GuiGraphics graphics) {
		if (controlsScrollMax <= 0) {
			return;
		}
		int viewportHeight = Math.max(1, controlsViewportBottom - controlsViewportTop);
		int contentHeight = Math.max(viewportHeight, controlsContentBottom - controlsContentTop);
		int trackX = controlsViewportRight - 3;
		int trackWidth = 3;
		int thumbHeight = Math.max(18, (int) Math.round((viewportHeight * (double) viewportHeight) / contentHeight));
		thumbHeight = Math.min(viewportHeight, thumbHeight);
		int trackTravel = Math.max(0, viewportHeight - thumbHeight);
		int thumbY = controlsViewportTop;
		if (trackTravel > 0) {
			thumbY += (int) Math.round(trackTravel * (controlsScroll / (double) controlsScrollMax));
		}
		graphics.fill(trackX, controlsViewportTop, trackX + trackWidth, controlsViewportBottom, 0x441A2A38);
		graphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, 0xCC80D8FF);
	}

	private void renderWidgetPreview(GuiGraphics graphics, HudWidget widget) {
		if (minecraft == null || widget == null) {
			return;
		}

		graphics.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, 0x33000000);
		graphics.fill(previewX, previewY, previewX + previewWidth, previewY + 1, 0x5577AACC);
		graphics.fill(previewX, previewY + previewHeight - 1, previewX + previewWidth, previewY + previewHeight, 0x5577AACC);
		graphics.fill(previewX, previewY, previewX + 1, previewY + previewHeight, 0x5577AACC);
		graphics.fill(previewX + previewWidth - 1, previewY, previewX + previewWidth, previewY + previewHeight, 0x5577AACC);

		BetterHudsConfig.WidgetConfig widgetConfig = cfg();
		int baseW = Math.max(8, widget.getWidth(minecraft, hudSystem.config(), widgetConfig));
		int baseH = Math.max(8, widget.getHeight(minecraft, hudSystem.config(), widgetConfig));
		float scale = Math.min((previewWidth - 12) / (float) baseW, (previewHeight - 12) / (float) baseH);
		scale = Math.max(0.6F, Math.min(2.0F, scale));
		int drawX = previewX + (previewWidth - Math.round(baseW * scale)) / 2;
		int drawY = previewY + (previewHeight - Math.round(baseH * scale)) / 2;

		var pose = graphics.pose();
		PoseCompat.push(pose);
		PoseCompat.translate(pose, drawX, drawY);
		PoseCompat.scale(pose, scale, scale);
		boolean showWidgetBackground = widgetConfig.background
			&& !"keystrokes".equals(widgetId)
			&& !"crosshair".equals(widgetId);
		if (showWidgetBackground) {
			graphics.fill(-2, -2, baseW + 2, baseH + 2, widgetConfig.backgroundColor);
			graphics.fill(-2, -2, baseW + 2, -1, 0x88FFFFFF);
			graphics.fill(-2, baseH + 1, baseW + 2, baseH + 2, 0x88FFFFFF);
			graphics.fill(-2, -2, -1, baseH + 2, 0x88FFFFFF);
			graphics.fill(baseW + 1, -2, baseW + 2, baseH + 2, 0x88FFFFFF);
		}
		HudRenderContext context = new HudRenderContext(hudSystem.config(), hudSystem.metrics(), hudSystem.itemHistory(), true, true);
		widget.render(graphics, minecraft, context, widgetConfig, 0, 0);
		PoseCompat.pop(pose);
	}

	private record LabelBinding(GlassButton button, Supplier<Component> labelSupplier) {
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

	private void drawColorChip(GuiGraphics graphics, GlassButton button, int color) {
		int chipSize = Math.min(12, button.getHeight() - 6);
		int x = button.getX() + button.getWidth() - chipSize - 4;
		int y = button.getY() + (button.getHeight() - chipSize) / 2;
		graphics.fill(x - 1, y - 1, x + chipSize + 1, y + chipSize + 1, 0xFF80D8FF);
		graphics.fill(x, y, x + chipSize, y + chipSize, color);
	}

	private void drawSectionCard(GuiGraphics graphics, int x, int y, int w, int h) {
		graphics.fill(x, y, x + w, y + h, 0x3318232E);
		graphics.fill(x, y, x + w, y + 1, 0x5577AACC);
		graphics.fill(x, y + h - 1, x + w, y + h, 0x5577AACC);
		graphics.fill(x, y, x + 1, y + h, 0x5577AACC);
		graphics.fill(x + w - 1, y, x + w, y + h, 0x5577AACC);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private record ScrollableControlBinding(AbstractWidget widget, int baseY) {
	}

	private record ColorChipBinding(GlassButton button, IntSupplier colorSupplier) {
	}
}

