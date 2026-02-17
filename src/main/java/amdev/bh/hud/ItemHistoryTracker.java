package amdev.bh.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import amdev.bh.util.McCompat;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemHistoryTracker {
	private static final int MAX_EVENTS = 20;
	private static final long EVENT_TTL_MS = 30_000L;

	private final Map<String, Integer> previousCounts = new HashMap<>();
	private final Deque<ItemHistoryEvent> events = new ArrayDeque<>();
	private boolean initialized;

	public void tick(Minecraft client) {
		Player player = client.player;
		if (player == null) {
			initialized = false;
			previousCounts.clear();
			events.clear();
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
				events.addFirst(new ItemHistoryEvent(key, itemDisplayName(key), delta, now));
			}
		}

		while (events.size() > MAX_EVENTS) {
			events.removeLast();
		}
		while (!events.isEmpty() && now - events.getLast().timestampMs() > EVENT_TTL_MS) {
			events.removeLast();
		}

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
			return itemId;
		}
		return item.getName().getString();
	}

	public Deque<ItemHistoryEvent> events() {
		return events;
	}

	public record ItemHistoryEvent(String itemId, String displayName, int delta, long timestampMs) {
	}
}
