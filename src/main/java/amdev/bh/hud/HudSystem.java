package amdev.bh.hud;

import amdev.bh.BetterHuds;
import amdev.bh.config.BetterHudsConfig;
import amdev.bh.config.ConfigManager;
import amdev.bh.api.BetterHudsApi;
import amdev.bh.hud.widget.ArmorWidget;
import amdev.bh.hud.widget.BiomeWidget;
import amdev.bh.hud.widget.ClockWidget;
import amdev.bh.hud.widget.CoordinatesWidget;
import amdev.bh.hud.widget.ConsumablesWidget;
import amdev.bh.hud.widget.CrosshairWidget;
import amdev.bh.hud.widget.DirectionWidget;
import amdev.bh.hud.widget.FpsWidget;
import amdev.bh.hud.widget.HeldItemWidget;
import amdev.bh.hud.widget.ItemCounterWidget;
import amdev.bh.hud.widget.ItemHistoryWidget;
import amdev.bh.hud.widget.KeystrokesWidget;
import amdev.bh.hud.widget.MiniInventoryWidget;
import amdev.bh.hud.widget.PingWidget;
import amdev.bh.hud.widget.SprintStatusWidget;
import amdev.bh.hud.widget.StatusEffectsWidget;
import amdev.bh.hud.widget.SpeedWidget;
import amdev.bh.hud.widget.SurvivalWidget;
import amdev.bh.hud.widget.WidgetRenderUtil;
import amdev.bh.ui.HudEditorScreen;
import amdev.bh.ui.ItemCounterSetupScreen;
import amdev.bh.util.McCompat;
import amdev.bh.util.PoseCompat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class HudSystem {
	private final ConfigManager configManager;
	private final HudWidgetRegistry registry = new HudWidgetRegistry();
	private final MetricsTracker metrics = new MetricsTracker();
	private final ItemHistoryTracker itemHistory = new ItemHistoryTracker();
	private KeyMapping openEditorKey;
	private KeyMapping toggleHudKey;
	private KeyMapping setupItemCounterKey;
	private KeyMapping toggleMiniInventoryKey;
	private boolean miniInventoryToggleState;

	public HudSystem(ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void initialize() {
		configManager.load();
		registerWidgets();
		registerKeybinds();

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
		registerHudElements();
	}

	private void registerHudElements() {
		Identifier hudElementId = Identifier.fromNamespaceAndPath(BetterHuds.MOD_ID, "hud");
		HudElementRegistry.addLast(hudElementId, (graphics, tickCounter) -> renderHud(graphics));
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanillaElement -> (graphics, tickCounter) -> {
			if (!shouldReplaceVanillaCrosshair(Minecraft.getInstance())) {
				vanillaElement.extractRenderState(graphics, tickCounter);
			}
		});
	}

	private void registerWidgets() {
		registry.register(new ArmorWidget());
		registry.register(new HeldItemWidget());
		registry.register(new SurvivalWidget());
		registry.register(new KeystrokesWidget());
		registry.register(new SprintStatusWidget());
		registry.register(new FpsWidget());
		registry.register(new PingWidget());
		registry.register(new CoordinatesWidget());
		registry.register(new SpeedWidget());
		registry.register(new ClockWidget());
		registry.register(new BiomeWidget());
		registry.register(new DirectionWidget());
		registry.register(new CrosshairWidget());
		registry.register(new ConsumablesWidget());
		registry.register(new ItemHistoryWidget());
		registry.register(new ItemCounterWidget());
		registry.register(new StatusEffectsWidget());
		registry.register(new MiniInventoryWidget());
		registerExternalWidgets();
	}

	private void registerExternalWidgets() {
		for (BetterHudsApi.WidgetEntrypoint entrypoint : FabricLoader.getInstance().getEntrypoints("better-huds", BetterHudsApi.WidgetEntrypoint.class)) {
			try {
				entrypoint.register(widget -> {
					if (widget == null || widget.id() == null || widget.id().isBlank()) {
						return;
					}
					registry.register(widget);
				});
			} catch (Exception exception) {
				BetterHuds.LOGGER.error("Failed to register external Better Huds widgets for {}", entrypoint.getClass().getName(), exception);
			}
		}
	}

	private void registerKeybinds() {
		openEditorKey = McCompat.registerKeyBinding(McCompat.createKeyMapping("key.better-huds.open_editor", GLFW.GLFW_KEY_RIGHT_SHIFT));
		toggleHudKey = McCompat.registerKeyBinding(McCompat.createKeyMapping("key.better-huds.toggle_hud", GLFW.GLFW_KEY_H));
		setupItemCounterKey = McCompat.registerKeyBinding(McCompat.createKeyMapping("key.better-huds.item_counter_setup", GLFW.GLFW_KEY_O));
		toggleMiniInventoryKey = McCompat.registerKeyBinding(McCompat.createKeyMapping("key.better-huds.mini_inventory", GLFW.GLFW_KEY_V));
	}

	private void onClientTick(Minecraft client) {
		metrics.tick(client);
		itemHistory.tick(client);
		WidgetRenderUtil.setChromaSpeed(config().chromaSpeed);
		if (client.player == null) {
			return;
		}

		while (toggleHudKey.consumeClick()) {
			BetterHudsConfig config = config();
			config.hudEnabled = !config.hudEnabled;
			configManager.save();
			McCompat.displayClientMessage(client.player, Component.translatable(config.hudEnabled ? "message.better-huds.hud_enabled" : "message.better-huds.hud_disabled"), true);
		}

		while (openEditorKey.consumeClick()) {
			client.setScreen(new HudEditorScreen(this));
		}

		while (setupItemCounterKey.consumeClick()) {
			client.setScreen(new ItemCounterSetupScreen(this, detectHeldItemId(client)));
		}

		BetterHudsConfig.WidgetConfig miniCfg = config().getOrCreateWidgetConfig("mini_inventory");
		if (miniCfg.toggle("mini_hold_mode", false)) {
			miniInventoryToggleState = toggleMiniInventoryKey != null && toggleMiniInventoryKey.isDown();
		} else {
			while (toggleMiniInventoryKey.consumeClick()) {
				miniInventoryToggleState = !miniInventoryToggleState;
			}
		}
	}

	public void renderHud(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return;
		}

		BetterHudsConfig config = config();
		if (!config.hudEnabled) {
			return;
		}
		if (config.hideWhenF1 && client.options.hideGui) {
			return;
		}
		if (config.hideInChat && client.screen != null && client.screen.getClass().getSimpleName().equals("ChatScreen")) {
			return;
		}
		WidgetRenderUtil.setChromaSpeed(config.chromaSpeed);

		HudRenderContext context = new HudRenderContext(config, metrics, itemHistory, false, isMiniInventoryVisible());
		for (ResolvedWidget resolved : getResolvedWidgets(false)) {
			renderResolvedWidget(graphics, client, context, resolved, 0x00000000);
		}
	}

	public void renderForEditor(GuiGraphicsExtractor graphics) {
		Minecraft client = Minecraft.getInstance();
		WidgetRenderUtil.setChromaSpeed(config().chromaSpeed);
		HudRenderContext context = new HudRenderContext(config(), metrics, itemHistory, true, true);
		for (ResolvedWidget resolved : getResolvedWidgets(false)) {
			renderResolvedWidget(graphics, client, context, resolved, 0x00000000);
		}
	}

	private void renderResolvedWidget(GuiGraphicsExtractor graphics, Minecraft client, HudRenderContext context, ResolvedWidget resolved, int forcedBackground) {
		if (!resolved.widgetConfig().enabled) {
			return;
		}
		if (!context.editorMode() && !resolved.widget().shouldRender(client, context.config(), context)) {
			return;
		}

		var pose = graphics.pose();
		PoseCompat.push(pose);
		PoseCompat.translate(pose, resolved.x(), resolved.y());
		PoseCompat.scale(pose, resolved.appliedScale(), resolved.appliedScale());

		int backgroundColor = forcedBackground != 0 ? forcedBackground : resolved.widgetConfig().backgroundColor;
		int borderColor = context.editorMode() ? 0xFF80D8FF : 0x88FFFFFF;
		boolean allowWidgetBackground = !resolved.widget().id().equals("keystrokes")
			&& !resolved.widget().id().equals("crosshair");
		if (allowWidgetBackground && (resolved.widgetConfig().background || context.editorMode())) {
			int pad = 2;
			int left = -pad;
			int top = -pad;
			int right = resolved.baseWidth() + pad;
			int bottom = resolved.baseHeight() + pad;
			graphics.fill(left, top, right, bottom, backgroundColor);
			graphics.fill(left, top, right, top + 1, borderColor);
			graphics.fill(left, bottom - 1, right, bottom, borderColor);
			graphics.fill(left, top, left + 1, bottom, borderColor);
			graphics.fill(right - 1, top, right, bottom, borderColor);
		}
		resolved.widget().render(graphics, client, context, resolved.widgetConfig(), 0, 0);
		PoseCompat.pop(pose);
	}

	public List<ResolvedWidget> getResolvedWidgets(boolean includeDisabled) {
		Minecraft client = Minecraft.getInstance();
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		BetterHudsConfig config = config();

		List<ResolvedWidget> resolvedWidgets = new ArrayList<>();
		for (HudWidget widget : registry.all()) {
			BetterHudsConfig.WidgetConfig widgetConfig = config.getOrCreateWidgetConfig(widget.id());
			if (!includeDisabled && !widgetConfig.enabled) {
				continue;
			}
			if ("held_item".equals(widget.id()) && !HeldItemWidget.isSeparate(config)) {
				continue;
			}

			int width = Math.max(8, widget.getWidth(client, config, widgetConfig));
			int height = Math.max(8, widget.getHeight(client, config, widgetConfig));
			resolvedWidgets.add(HudLayout.resolve(client, widget, config, widgetConfig, screenWidth, screenHeight, width, height));
		}

		return resolvedWidgets;
	}

	public BetterHudsConfig config() {
		return configManager.config();
	}

	public ConfigManager configManager() {
		return configManager;
	}

	public MetricsTracker metrics() {
		return metrics;
	}

	public ItemHistoryTracker itemHistory() {
		return itemHistory;
	}

	public List<HudWidget> widgets() {
		return new ArrayList<>(registry.all());
	}

	public boolean shouldReplaceVanillaCrosshair(Minecraft client) {
		if (client == null || client.player == null) {
			return false;
		}
		BetterHudsConfig cfg = config();
		if (!cfg.hudEnabled) {
			return false;
		}
		if (cfg.hideWhenF1 && client.options.hideGui) {
			return false;
		}
		BetterHudsConfig.WidgetConfig crosshairCfg = cfg.getOrCreateWidgetConfig("crosshair");
		return crosshairCfg.enabled;
	}

	public HudWidget widget(String id) {
		return registry.get(id);
	}

	public record HudKeybind(String nameKey, KeyMapping keyMapping) {
	}

	public List<HudKeybind> keybinds() {
		List<HudKeybind> entries = new ArrayList<>();
		entries.add(new HudKeybind("key.better-huds.open_editor", openEditorKey));
		entries.add(new HudKeybind("key.better-huds.toggle_hud", toggleHudKey));
		entries.add(new HudKeybind("key.better-huds.item_counter_setup", setupItemCounterKey));
		entries.add(new HudKeybind("key.better-huds.mini_inventory", toggleMiniInventoryKey));
		return entries;
	}

	public boolean isMiniInventoryVisible() {
		if (minecraftBlockedForMiniInventory()) {
			return false;
		}
		BetterHudsConfig.WidgetConfig miniCfg = config().getOrCreateWidgetConfig("mini_inventory");
		return miniCfg.enabled && miniInventoryToggleState;
	}

	private boolean minecraftBlockedForMiniInventory() {
		Minecraft client = Minecraft.getInstance();
		return client == null || client.player == null || (client.screen != null && !(client.screen instanceof HudEditorScreen));
	}

	private String detectHeldItemId(Minecraft client) {
		if (client.player == null) {
			return "";
		}
		ItemStack mainHand = client.player.getMainHandItem();
		if (!mainHand.isEmpty() && mainHand.getItem() != Items.AIR) {
			Item item = mainHand.getItem();
			return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
		}
		ItemStack offHand = client.player.getOffhandItem();
		if (offHand.isEmpty() || offHand.getItem() == Items.AIR) {
			return "";
		}
		Item item = offHand.getItem();
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
	}
}
