package amdev.bh.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

final class ScreenCompat {
	private ScreenCompat() {
	}

	static void setScreen(Minecraft client, Screen screen) {
		client.setScreen(screen);
	}

	static Screen currentScreen(Minecraft client) {
		return client.screen;
	}
}
