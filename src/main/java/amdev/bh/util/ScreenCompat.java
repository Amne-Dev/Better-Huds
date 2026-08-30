package amdev.bh.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class ScreenCompat {
	private ScreenCompat() {
	}

	static void setScreen(Minecraft client, Screen screen) {
		client.setScreenAndShow(screen);
	}

	static Screen currentScreen(Minecraft client) {
		try {
			for (Field field : client.getClass().getFields()) {
				if (Screen.class.isAssignableFrom(field.getType())) {
					Object value = field.get(client);
					return value instanceof Screen screen ? screen : null;
				}
			}
			for (Method method : client.gui.getClass().getMethods()) {
				if (method.getParameterCount() == 0 && Screen.class.isAssignableFrom(method.getReturnType())) {
					Object value = method.invoke(client.gui);
					return value instanceof Screen screen ? screen : null;
				}
			}
		} catch (ReflectiveOperationException ignored) {
			// No active screen could be read on this Minecraft version.
		}
		return null;
	}
}
