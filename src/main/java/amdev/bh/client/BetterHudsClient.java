package amdev.bh.client;

import amdev.bh.BetterHuds;
import amdev.bh.config.ConfigManager;
import amdev.bh.hud.HudSystem;
import amdev.bh.ui.HudEditorScreen;
import amdev.bh.ui.widget.GlassButton;
import amdev.bh.util.McCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class BetterHudsClient implements ClientModInitializer {
	private static HudSystem hudSystem;

	@Override
	public void onInitializeClient() {
		hudSystem = new HudSystem(new ConfigManager());
		hudSystem.initialize();
		registerMainMenuButton();
		BetterHuds.LOGGER.info("Better Huds client initialized");
	}

	private void registerMainMenuButton() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof TitleScreen) || hudSystem == null) {
				return;
			}
			int buttonWidth = 106;
			int x = 8;
			int y = scaledHeight - 28;
			GlassButton editorButton = new GlassButton(
				x,
				y,
				buttonWidth,
				20,
				Component.translatable("screen.better-huds.main_menu_editor"),
				btn -> McCompat.setScreen(client, new HudEditorScreen(hudSystem))
			);
			McCompat.addButtonToScreen(screen, editorButton);
		});
	}

	public static HudSystem hudSystem() {
		return hudSystem;
	}
}
