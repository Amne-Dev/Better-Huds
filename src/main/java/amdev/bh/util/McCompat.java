package amdev.bh.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McCompat {
	private static final Pattern MC_121_PATTERN = Pattern.compile("^1\\.21(?:\\.(\\d+))?.*");
	private static final String BETTER_HUDS_KEY_CATEGORY = "key.categories.better-huds";
	private static Boolean disableUiBlurCache;

	private McCompat() {
	}

	public static Item findItemById(String rawItemId) {
		if (rawItemId == null || rawItemId.isBlank()) {
			return Items.AIR;
		}
		String normalized = normalizeItemId(rawItemId);
		Item direct = findExact(normalized);
		if (direct != Items.AIR) {
			return direct;
		}
		if (!normalized.contains(":")) {
			return findExact("minecraft:" + normalized);
		}
		return Items.AIR;
	}

	public static String resourceKeyPath(Object resourceKey) {
		if (resourceKey == null) {
			return "unknown";
		}

		Object id = invokeNoArgs(resourceKey, "identifier");
		if (id == null) {
			id = invokeNoArgs(resourceKey, "location");
		}

		String raw = id == null ? resourceKey.toString() : id.toString();
		if (raw == null || raw.isBlank()) {
			return "unknown";
		}

		int bracketStart = raw.indexOf('[');
		int bracketEnd = raw.indexOf(']');
		if (bracketStart >= 0 && bracketEnd > bracketStart) {
			raw = raw.substring(bracketStart + 1, bracketEnd);
		}

		int colon = raw.indexOf(':');
		if (colon >= 0 && colon + 1 < raw.length()) {
			return raw.substring(colon + 1);
		}
		return raw;
	}

	public static String itemDisplayName(Item item) {
		if (item == null || item == Items.AIR) {
			return "air";
		}
		Component fromGetName = invokeComponent(item, "getName");
		if (fromGetName != null) {
			return fromGetName.getString();
		}
		Component fromDescription = invokeComponent(item, "getDescription");
		if (fromDescription != null) {
			return fromDescription.getString();
		}
		return BuiltInRegistries.ITEM.getKey(item).toString();
	}

	public static int selectedHotbarSlot(Inventory inventory) {
		if (inventory == null) {
			return 0;
		}
		Object selected = invokeNoArgs(inventory, "getSelectedSlot");
		if (selected instanceof Integer value) {
			return value;
		}
		selected = readField(inventory, "selected");
		if (selected instanceof Integer value) {
			return value;
		}
		selected = readField(inventory, "selectedSlot");
		if (selected instanceof Integer value) {
			return value;
		}
		return 0;
	}

	public static boolean optionsKeyDown(Object options, String... fieldNames) {
		if (options == null || fieldNames == null) {
			return false;
		}
		for (String fieldName : fieldNames) {
			Object keyMapping = readField(options, fieldName);
			if (keyMapping == null) {
				continue;
			}
			Object down = invokeNoArgs(keyMapping, "isDown");
			if (down instanceof Boolean value && value) {
				return true;
			}
		}
		return false;
	}

	public static boolean isControlDown(Minecraft client) {
		Object fromClient = invokeNoArgs(client, "hasControlDown");
		if (fromClient instanceof Boolean value) {
			return value;
		}
		try {
			Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
			Method method = screenClass.getMethod("hasControlDown");
			Object result = method.invoke(null);
			if (result instanceof Boolean value) {
				return value;
			}
		} catch (Exception ignored) {
			// Fallback false.
		}
		return false;
	}

	public static KeyMapping createKeyMapping(String key, int defaultKey) {
		Object keyObj = null;
		Throwable lastError = null;
		try {
			keyObj = InputConstants.Type.KEYSYM.getOrCreate(defaultKey);
		} catch (Exception ignored) {
			// Key object overload may not exist.
		}

		for (var constructor : KeyMapping.class.getDeclaredConstructors()) {
			Class<?>[] params = constructor.getParameterTypes();
			Object[] args = new Object[params.length];
			boolean valid = true;
			for (int i = 0; i < params.length; i++) {
				Class<?> param = params[i];
				if (param == String.class) {
					// First string is translation key, second (if present) is category id.
					args[i] = i == 0 ? key : BETTER_HUDS_KEY_CATEGORY;
					continue;
				}
				if (param == int.class || param == Integer.class) {
					// Use bound key for first integer parameter, neutral defaults for any extra ints.
					args[i] = i <= 2 ? defaultKey : 0;
					continue;
				}
				if (param == InputConstants.Type.class) {
					args[i] = InputConstants.Type.KEYSYM;
					continue;
				}
				if (keyObj != null && param.isInstance(keyObj)) {
					args[i] = keyObj;
					continue;
				}
				Object category = resolveKeyCategoryArg(param);
				if (category != null) {
					args[i] = category;
					continue;
				}
				valid = false;
				break;
			}
			if (!valid) {
				continue;
			}
			try {
				constructor.setAccessible(true);
				return (KeyMapping) constructor.newInstance(args);
			} catch (Exception exception) {
				lastError = exception;
				// Try other overloads.
			}
		}
		throw new IllegalStateException("No compatible KeyMapping constructor found", lastError);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static boolean addButtonToScreen(Object screen, Object button) {
		if (screen == null || button == null) {
			return false;
		}
		try {
			Class<?> screensClass = Class.forName("net.fabricmc.fabric.api.client.screen.v1.Screens");
			Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
			Method getButtons = screensClass.getMethod("getButtons", screenClass);
			Object buttons = getButtons.invoke(null, screen);
			if (buttons instanceof List list) {
				list.add(button);
				return true;
			}
		} catch (Exception ignored) {
			// Fall through to direct screen methods.
		}

		Class<?> current = screen.getClass();
		while (current != null) {
			for (Method method : current.getDeclaredMethods()) {
				if (method.getParameterCount() != 1) {
					continue;
				}
				String name = method.getName();
				if (!name.equals("addRenderableWidget") && !name.equals("addDrawableChild") && !name.equals("addWidget")) {
					continue;
				}
				Class<?> param = method.getParameterTypes()[0];
				if (!param.isAssignableFrom(button.getClass())) {
					continue;
				}
				try {
					method.setAccessible(true);
					method.invoke(screen, button);
					return true;
				} catch (Exception ignored) {
					// Try next candidate.
				}
			}
			current = current.getSuperclass();
		}
		return false;
	}

	public static KeyMapping registerKeyBinding(KeyMapping mapping) {
		if (mapping == null) {
			return null;
		}
		try {
			Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
			Method register = helperClass.getMethod("registerKeyBinding", KeyMapping.class);
			Object result = register.invoke(null, mapping);
			if (result instanceof KeyMapping registered) {
				return registered;
			}
		} catch (Exception ignored) {
			// Fallback to unregistered mapping instance.
		}
		return mapping;
	}

	public static boolean setEditBoxFilter(EditBox editBox, Predicate<String> predicate) {
		if (editBox == null || predicate == null) {
			return false;
		}
		if (invokeEditBoxPredicateMethod(editBox, "setFilter", predicate)) {
			return true;
		}
		if (invokeEditBoxPredicateMethod(editBox, "setTextPredicate", predicate)) {
			return true;
		}
		return invokeEditBoxPredicateMethod(editBox, "setValidator", predicate);
	}

	public static long levelDayTime(Object level) {
		if (level == null) {
			return 0L;
		}
		Object value = invokeNoArgs(level, "getDayTime");
		if (value instanceof Number number) {
			return number.longValue();
		}
		value = invokeNoArgs(level, "getTimeOfDay");
		if (value instanceof Number number) {
			return number.longValue();
		}
		value = invokeNoArgs(level, "dayTime");
		if (value instanceof Number number) {
			return number.longValue();
		}
		value = readField(level, "dayTime");
		if (value instanceof Number number) {
			return number.longValue();
		}
		return 0L;
	}

	public static void displayClientMessage(Object player, Component component, boolean actionBar) {
		if (player == null || component == null) {
			return;
		}
		try {
			Method method = player.getClass().getMethod("displayClientMessage", Component.class, boolean.class);
			method.invoke(player, component, actionBar);
			return;
		} catch (Exception ignored) {
			// Try other method names.
		}
		try {
			Method method = player.getClass().getMethod("sendSystemMessage", Component.class);
			method.invoke(player, component);
			return;
		} catch (Exception ignored) {
			// Try fallback.
		}
		try {
			Method method = player.getClass().getMethod("sendMessage", Component.class);
			method.invoke(player, component);
		} catch (Exception ignored) {
			// No available message method.
		}
	}

	public static void enableInvertBlend() {
		invokeStaticGl("_enableBlend");
		invokeStaticGl("_blendFuncSeparate", 775, 0, 1, 0); // ONE_MINUS_DST_COLOR, ZERO, ONE, ZERO
	}

	public static void resetDefaultBlend() {
		invokeStaticGl("_blendFuncSeparate", 770, 771, 1, 0); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA, ONE, ZERO
		invokeStaticGl("_disableBlend");
	}

	public static boolean shouldDisableUiBlur() {
		if (disableUiBlurCache != null) {
			return disableUiBlurCache;
		}
		String version = detectMinecraftVersion();
		Matcher matcher = MC_121_PATTERN.matcher(version);
		if (!matcher.matches()) {
			disableUiBlurCache = false;
			return false;
		}
		int patch = 0;
		String patchText = matcher.group(1);
		if (patchText != null && !patchText.isBlank()) {
			try {
				patch = Integer.parseInt(patchText);
			} catch (NumberFormatException ignored) {
				patch = 0;
			}
		}
		disableUiBlurCache = patch <= 8;
		return disableUiBlurCache;
	}

	private static Item findExact(String normalized) {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item == null || item == Items.AIR) {
				continue;
			}
			String key = BuiltInRegistries.ITEM.getKey(item).toString();
			if (normalized.equalsIgnoreCase(key)) {
				return item;
			}
		}
		return Items.AIR;
	}

	private static String normalizeItemId(String rawItemId) {
		return rawItemId.trim().toLowerCase(Locale.ROOT);
	}

	private static Object invokeNoArgs(Object target, String methodName) {
		try {
			Method method = target.getClass().getMethod(methodName);
			return method.invoke(target);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static Component invokeComponent(Object target, String methodName) {
		Object value = invokeNoArgs(target, methodName);
		if (value instanceof Component component) {
			return component;
		}
		return null;
	}

	private static Object readField(Object target, String fieldName) {
		try {
			Field field = target.getClass().getField(fieldName);
			return field.get(target);
		} catch (Exception ignored) {
			try {
				Field declared = target.getClass().getDeclaredField(fieldName);
				declared.setAccessible(true);
				return declared.get(target);
			} catch (Exception ignoredAgain) {
				return null;
			}
		}
	}

	private static boolean invokeEditBoxPredicateMethod(EditBox editBox, String methodName, Predicate<String> predicate) {
		try {
			Method method = editBox.getClass().getMethod(methodName, Predicate.class);
			method.invoke(editBox, predicate);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	private static Object resolveKeyCategoryArg(Class<?> categoryType) {
		if (categoryType == String.class) {
			return BETTER_HUDS_KEY_CATEGORY;
		}
		if (categoryType.isEnum()) {
			Object[] constants = categoryType.getEnumConstants();
			if (constants == null) {
				return null;
			}
			for (Object constant : constants) {
				if (constant != null && "MISC".equalsIgnoreCase(constant.toString())) {
					return constant;
				}
			}
			return constants.length > 0 ? constants[0] : null;
		}
		Object first = null;
		for (Field field : categoryType.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) || !categoryType.isAssignableFrom(field.getType())) {
				continue;
			}
			try {
				field.setAccessible(true);
				Object value = field.get(null);
				if (value == null) {
					continue;
				}
				if (first == null) {
					first = value;
				}
				String text = value.toString();
				if (text != null && text.toLowerCase(Locale.ROOT).contains("misc")) {
					return value;
				}
			} catch (Exception ignored) {
				// Try next field.
			}
		}
		return first;
	}

	private static boolean invokeStaticGl(String methodName, Object... args) {
		return invokeStatic("com.mojang.blaze3d.opengl.GlStateManager", methodName, args)
			|| invokeStatic("com.mojang.blaze3d.platform.GlStateManager", methodName, args);
	}

	private static boolean invokeStatic(String className, String methodName, Object... args) {
		try {
			Class<?> clazz = Class.forName(className);
			for (Method method : clazz.getMethods()) {
				if (!method.getName().equals(methodName)
					|| method.getParameterCount() != args.length
					|| !Modifier.isStatic(method.getModifiers())) {
					continue;
				}
				try {
					method.invoke(null, args);
					return true;
				} catch (Exception ignored) {
					// Try another overload.
				}
			}
		} catch (Exception ignored) {
			// Class not present on this MC version.
		}
		return false;
	}

	private static String detectMinecraftVersion() {
		try {
			Class<?> sharedConstants = Class.forName("net.minecraft.SharedConstants");
			Method currentVersion = sharedConstants.getMethod("getCurrentVersion");
			Object gameVersion = currentVersion.invoke(null);
			if (gameVersion != null) {
				Object name = invokeNoArgs(gameVersion, "getName");
				if (name instanceof String version && !version.isBlank()) {
					return version;
				}
			}
		} catch (Exception ignored) {
			// Try fallback below.
		}
		Minecraft client = Minecraft.getInstance();
		if (client != null) {
			Object version = invokeNoArgs(client, "getLaunchedVersion");
			if (version instanceof String text && !text.isBlank()) {
				return text;
			}
		}
		return "";
	}
}
