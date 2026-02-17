package amdev.bh.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;
import java.util.Locale;

public final class McCompat {
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
}
