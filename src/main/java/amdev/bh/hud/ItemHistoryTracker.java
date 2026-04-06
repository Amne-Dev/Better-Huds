package amdev.bh.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import amdev.bh.util.McCompat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ItemHistoryTracker {
	private static final int MAX_EVENTS = 20;
	public static final long ITEM_IDLE_BEFORE_FADE_MS = 5_000L;
	public static final long ITEM_FADE_DURATION_MS = 3_000L;
	private final Map<String, Integer> previousCounts = new HashMap<>();
	private final Map<String, ItemHistoryEvent> activeItems = new LinkedHashMap<>();
	private boolean initialized;

	public void tick(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			initialized = false;
			previousCounts.clear();
			activeItems.clear();
			return;
		}

		Map<String, Integer> currentCounts = collectCounts(player.getInventory());
		if (!initialized) {
			previousCounts.clear();
			previousCounts.putAll(currentCounts);
			initialized = true;
			return;
		}

		Set<String> keys = new HashSet<>(previousCounts.keySet());
		keys.addAll(currentCounts.keySet());
		long now = System.currentTimeMillis();
		for (String key : keys) {
			int previous = previousCounts.getOrDefault(key, 0);
			int current = currentCounts.getOrDefault(key, 0);
			int delta = current - previous;
			if (delta != 0) {
				updateActiveItem(key, delta, now);
			}
		}
		pruneExpired(now);
		trimToMaxEntries();

		previousCounts.clear();
		previousCounts.putAll(currentCounts);
	}

	private static Map<String, Integer> collectCounts(Inventory inventory) {
		Map<String, Integer> counts = new HashMap<>();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}

			Item item = stack.getItem();
			if (item == Items.AIR) {
				continue;
			}
			String key = BuiltInRegistries.ITEM.getKey(item).toString();
			counts.put(key, counts.getOrDefault(key, 0) + stack.getCount());
		}
		return counts;
	}

	private static String itemDisplayName(String itemId) {
		Item item = McCompat.findItemById(itemId);
		if (item == null || item == Items.AIR) {
			return prettyItemName(itemId);
		}
		String displayName = McCompat.itemDisplayName(item);
		if (displayName == null || displayName.isBlank() || displayName.contains(":")) {
			return prettyItemName(itemId);
		}
		return displayName;
	}

	public List<ItemHistoryEvent> groupedEvents() {
		List<ItemHistoryEvent> grouped = new ArrayList<>(activeItems.values());
		grouped.sort((left, right) -> Long.compare(right.timestampMs(), left.timestampMs()));
		return grouped;
	}

	private static String prettyItemName(String itemId) {
		String raw = itemId == null ? "" : itemId;
		int colon = raw.indexOf(':');
		String path = colon >= 0 ? raw.substring(colon + 1) : raw;
		if (path.isBlank()) {
			return "Unknown";
		}
		String[] parts = path.split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isBlank()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
			if (part.length() > 1) {
				builder.append(part.substring(1));
			}
		}
		return builder.isEmpty() ? itemId : builder.toString();
	}

	private void updateActiveItem(String itemId, int delta, long now) {
		String key = normalizeItemKey(itemId);
		ItemHistoryEvent existing = activeItems.remove(key);
		String displayName = existing == null ? itemDisplayName(itemId) : displayNameFor(existing, itemId);
		int mergedDelta = delta + (existing == null ? 0 : existing.delta());
		if (mergedDelta == 0) {
			return;
		}
		activeItems.put(key, new ItemHistoryEvent(itemId, displayName, mergedDelta, now));
	}

	private void pruneExpired(long now) {
		List<String> expiredKeys = new ArrayList<>();
		for (Map.Entry<String, ItemHistoryEvent> entry : activeItems.entrySet()) {
			if (now - entry.getValue().timestampMs() > ITEM_IDLE_BEFORE_FADE_MS + ITEM_FADE_DURATION_MS) {
				expiredKeys.add(entry.getKey());
			}
		}
		for (String key : expiredKeys) {
			activeItems.remove(key);
		}
	}

	private void trimToMaxEntries() {
		if (activeItems.size() <= MAX_EVENTS) {
			return;
		}
		List<Map.Entry<String, ItemHistoryEvent>> ordered = new ArrayList<>(activeItems.entrySet());
		ordered.sort((left, right) -> Long.compare(right.getValue().timestampMs(), left.getValue().timestampMs()));
		activeItems.clear();
		for (int i = 0; i < Math.min(MAX_EVENTS, ordered.size()); i++) {
			Map.Entry<String, ItemHistoryEvent> entry = ordered.get(i);
			activeItems.put(entry.getKey(), entry.getValue());
		}
	}

	private static String normalizeItemKey(String itemId) {
		if (itemId == null) {
			return "";
		}
		return itemId.trim().toLowerCase(Locale.ROOT);
	}

	private static String displayNameFor(ItemHistoryEvent existing, String itemId) {
		String existingName = existing.displayName();
		if (existingName != null && !existingName.isBlank() && !existingName.contains(":")) {
			return existingName;
		}
		return itemDisplayName(itemId);
	}

	public record ItemHistoryEvent(String itemId, String displayName, int delta, long timestampMs) {
	}
}
