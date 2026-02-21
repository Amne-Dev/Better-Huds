package amdev.bh.config;

import amdev.bh.hud.Anchor;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BetterHudsConfig {
	private static final Gson GSON = new Gson();
	private static final String DEFAULT_PROFILE_1_RESOURCE = "better-huds/default-profile-1.json";
	public boolean hudEnabled = true;
	public boolean hideWhenF1 = true;
	public boolean hideInChat = false;
	public boolean snapToGrid = true;
	public int gridSize = 4;
	public float globalScale = 1.0F;
	public int chromaSpeed = 420;
	public int activeProfile = 0;
	public boolean editorTipMinimized = false;
	public String itemCounterItemId = "";
	public int itemCounterTarget = 0;
	public List<Profile> profiles = new ArrayList<>();

	public static BetterHudsConfig createDefault() {
		BetterHudsConfig config = new BetterHudsConfig();

		Profile profile1 = new Profile("Profile 1");
		profile1.widgets.put("armor", new WidgetConfig(8, 8));
		profile1.widgets.put("survival", new WidgetConfig(8, 98));
		profile1.widgets.put("keystrokes", new WidgetConfig(8, 144));
		profile1.widgets.put("sprint_status", new WidgetConfig(8, 252));
		profile1.widgets.put("crosshair", new WidgetConfig(0, 0, Anchor.CENTER));
		profile1.widgets.put("held_item", new WidgetConfig(8, 52));
		profile1.widgets.put("fps", new WidgetConfig(8, 274));
		profile1.widgets.put("ping", new WidgetConfig(8, 294));
		profile1.widgets.put("coordinates", new WidgetConfig(8, 314));
		profile1.widgets.put("speed", new WidgetConfig(8, 334));
		profile1.widgets.put("clock", new WidgetConfig(8, 354));
		profile1.widgets.put("biome", new WidgetConfig(8, 374));
		profile1.widgets.put("direction", new WidgetConfig(0, 8, Anchor.TOP_CENTER));
		profile1.widgets.put("consumables", new WidgetConfig(8, 394));
		profile1.widgets.put("item_history", new WidgetConfig(-8, 90, Anchor.TOP_RIGHT));
		profile1.widgets.put("item_counter", new WidgetConfig(-8, 150, Anchor.TOP_RIGHT));
		profile1.widgets.put("status_effects", new WidgetConfig(-8, 8, Anchor.TOP_RIGHT));
		WidgetConfig miniInventory1 = new WidgetConfig(-8, -86, Anchor.BOTTOM_RIGHT);
		miniInventory1.showText = false;
		profile1.widgets.put("mini_inventory", miniInventory1);
		Profile bundledProfile1 = loadBundledProfile(DEFAULT_PROFILE_1_RESOURCE);
		if (bundledProfile1 != null) {
			profile1 = bundledProfile1;
			profile1.name = "Profile 1";
		}

		Profile profile2 = new Profile("Profile 2");
		profile2.widgets.put("armor", new WidgetConfig(-8, 8, Anchor.TOP_RIGHT));
		profile2.widgets.put("survival", new WidgetConfig(-8, 98, Anchor.TOP_RIGHT));
		profile2.widgets.put("keystrokes", new WidgetConfig(8, -118, Anchor.BOTTOM_LEFT));
		profile2.widgets.put("sprint_status", new WidgetConfig(8, -40, Anchor.BOTTOM_LEFT));
		profile2.widgets.put("crosshair", new WidgetConfig(0, 0, Anchor.CENTER));
		profile2.widgets.put("held_item", new WidgetConfig(-8, 52, Anchor.TOP_RIGHT));
		profile2.widgets.put("fps", new WidgetConfig(8, 8));
		profile2.widgets.put("ping", new WidgetConfig(8, 28));
		profile2.widgets.put("coordinates", new WidgetConfig(8, 48));
		profile2.widgets.put("speed", new WidgetConfig(8, 68));
		profile2.widgets.put("clock", new WidgetConfig(8, 88));
		profile2.widgets.put("biome", new WidgetConfig(8, 108));
		profile2.widgets.put("direction", new WidgetConfig(0, 8, Anchor.TOP_CENTER));
		profile2.widgets.put("consumables", new WidgetConfig(8, 128));
		profile2.widgets.put("item_history", new WidgetConfig(8, 118));
		profile2.widgets.put("item_counter", new WidgetConfig(8, 184));
		profile2.widgets.put("status_effects", new WidgetConfig(-8, 158, Anchor.TOP_RIGHT));
		WidgetConfig miniInventory2 = new WidgetConfig(-8, -86, Anchor.BOTTOM_RIGHT);
		miniInventory2.showText = false;
		profile2.widgets.put("mini_inventory", miniInventory2);

		Profile profile3 = new Profile("Profile 3");
		profile3.widgets.put("armor", new WidgetConfig(-128, -62, Anchor.BOTTOM_CENTER));
		profile3.widgets.put("held_item", new WidgetConfig(-110, -84, Anchor.BOTTOM_CENTER));
		profile3.widgets.put("survival", new WidgetConfig(8, -34, Anchor.BOTTOM_LEFT));
		profile3.widgets.put("keystrokes", new WidgetConfig(-8, -118, Anchor.BOTTOM_RIGHT));
		profile3.widgets.put("sprint_status", new WidgetConfig(-8, -40, Anchor.BOTTOM_RIGHT));
		profile3.widgets.put("crosshair", new WidgetConfig(0, 0, Anchor.CENTER));
		profile3.widgets.put("fps", new WidgetConfig(8, 8));
		profile3.widgets.put("ping", new WidgetConfig(8, 28));
		profile3.widgets.put("coordinates", new WidgetConfig(8, 48));
		profile3.widgets.put("speed", new WidgetConfig(8, 68));
		profile3.widgets.put("clock", new WidgetConfig(8, 88));
		profile3.widgets.put("biome", new WidgetConfig(8, 108));
		profile3.widgets.put("direction", new WidgetConfig(0, 8, Anchor.TOP_CENTER));
		profile3.widgets.put("consumables", new WidgetConfig(8, 128));
		profile3.widgets.put("item_history", new WidgetConfig(-8, 8, Anchor.TOP_RIGHT));
		profile3.widgets.put("item_counter", new WidgetConfig(-8, 74, Anchor.TOP_RIGHT));
		profile3.widgets.put("status_effects", new WidgetConfig(8, 8));
		WidgetConfig miniInventory3 = new WidgetConfig(-8, -86, Anchor.BOTTOM_RIGHT);
		miniInventory3.showText = false;
		profile3.widgets.put("mini_inventory", miniInventory3);

		config.profiles.add(profile1);
		config.profiles.add(profile2);
		config.profiles.add(profile3);
		return config;
	}

	public Profile getActiveProfile() {
		if (profiles.isEmpty()) {
			profiles.add(new Profile("Profile 1"));
			activeProfile = 0;
		}
		if (activeProfile < 0 || activeProfile >= profiles.size()) {
			activeProfile = 0;
		}
		return profiles.get(activeProfile);
	}

	public WidgetConfig getOrCreateWidgetConfig(String widgetId) {
		Profile profile = getActiveProfile();
		return profile.widgets.computeIfAbsent(widgetId, BetterHudsConfig::defaultWidgetConfigFor);
	}

	private static WidgetConfig defaultWidgetConfigFor(String widgetId) {
		return switch (widgetId) {
			case "crosshair" -> new WidgetConfig(0, 0, Anchor.CENTER);
			case "direction" -> new WidgetConfig(0, 8, Anchor.TOP_CENTER);
			case "clock" -> new WidgetConfig(8, 354);
			case "biome" -> new WidgetConfig(8, 374);
			case "mini_inventory" -> {
				WidgetConfig widgetConfig = new WidgetConfig(-8, -86, Anchor.BOTTOM_RIGHT);
				widgetConfig.showText = false;
				yield widgetConfig;
			}
			default -> new WidgetConfig(8, 8);
		};
	}

	private static Profile loadBundledProfile(String resourcePath) {
		try (InputStream stream = BetterHudsConfig.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (stream == null) {
				return null;
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				Profile profile = GSON.fromJson(reader, Profile.class);
				if (profile == null) {
					return null;
				}
				if (profile.name == null || profile.name.isBlank()) {
					profile.name = "Profile 1";
				}
				if (profile.widgets == null) {
					profile.widgets = new LinkedHashMap<>();
				}
				for (WidgetConfig widgetConfig : profile.widgets.values()) {
					sanitizeWidget(widgetConfig);
				}
				return profile;
			}
		} catch (IOException | RuntimeException ignored) {
			return null;
		}
	}

	private static void sanitizeWidget(WidgetConfig widgetConfig) {
		if (widgetConfig == null) {
			return;
		}
		if (widgetConfig.anchor == null) {
			widgetConfig.anchor = Anchor.TOP_LEFT;
		}
		if (widgetConfig.toggles == null) {
			widgetConfig.toggles = new LinkedHashMap<>();
		}
		if (widgetConfig.values == null) {
			widgetConfig.values = new LinkedHashMap<>();
		}
		if (widgetConfig.showText == null) {
			widgetConfig.showText = true;
		}
	}

	public int getGridSizeOrDefault() {
		return Math.max(1, gridSize);
	}

	public static class Profile {
		public String name;
		public Map<String, WidgetConfig> widgets = new LinkedHashMap<>();

		public Profile() {
			this("Profile");
		}

		public Profile(String name) {
			this.name = name;
		}
	}

	public static class WidgetConfig {
		public boolean enabled = true;
		public int x;
		public int y;
		public Anchor anchor = Anchor.TOP_LEFT;
		public long lastModifiedAt = 0L;
		public float scale = 1.0F;
		public boolean background = true;
		public Boolean showText = true;
		public int backgroundColor = 0xFF888888;
		public int textColor = 0xFFFFFFFF;
		public Map<String, Boolean> toggles = new LinkedHashMap<>();
		public Map<String, Integer> values = new LinkedHashMap<>();

		public WidgetConfig() {
			this(8, 8, Anchor.TOP_LEFT);
		}

		public WidgetConfig(int x, int y) {
			this(x, y, Anchor.TOP_LEFT);
		}

		public WidgetConfig(int x, int y, Anchor anchor) {
			this.x = x;
			this.y = y;
			this.anchor = anchor;
		}

		public boolean showText() {
			return showText == null || showText;
		}

		public boolean toggle(String key, boolean defaultValue) {
			if (toggles == null) {
				toggles = new LinkedHashMap<>();
			}
			return toggles.getOrDefault(key, defaultValue);
		}

		public void setToggle(String key, boolean value) {
			if (toggles == null) {
				toggles = new LinkedHashMap<>();
			}
			toggles.put(key, value);
			touch();
		}

		public int intValue(String key, int defaultValue) {
			if (values == null) {
				values = new LinkedHashMap<>();
			}
			return values.getOrDefault(key, defaultValue);
		}

		public void setIntValue(String key, int value) {
			if (values == null) {
				values = new LinkedHashMap<>();
			}
			values.put(key, value);
			touch();
		}

		public void touch() {
			lastModifiedAt = System.currentTimeMillis();
		}
	}
}
