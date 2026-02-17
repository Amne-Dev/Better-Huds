package amdev.bh.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import amdev.bh.hud.Anchor;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProfileShareCodec {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ProfileShareCodec() {
	}

	public static String encode(BetterHudsConfig.Profile profile) {
		return GSON.toJson(profile);
	}

	public static BetterHudsConfig.Profile decode(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}

		BetterHudsConfig.Profile direct = parseProfile(raw);
		if (direct != null) {
			return sanitizeProfile(direct);
		}

		try {
			BetterHudsConfig full = GSON.fromJson(raw, BetterHudsConfig.class);
			if (full == null || full.profiles == null || full.profiles.isEmpty()) {
				return null;
			}
			int index = full.activeProfile;
			if (index < 0 || index >= full.profiles.size()) {
				index = 0;
			}
			BetterHudsConfig.Profile profile = full.profiles.get(index);
			return sanitizeProfile(profile);
		} catch (JsonParseException exception) {
			return null;
		}
	}

	private static BetterHudsConfig.Profile parseProfile(String raw) {
		try {
			BetterHudsConfig.Profile profile = GSON.fromJson(raw, BetterHudsConfig.Profile.class);
			if (profile == null) {
				return null;
			}
			return profile;
		} catch (JsonParseException exception) {
			return null;
		}
	}

	private static BetterHudsConfig.Profile sanitizeProfile(BetterHudsConfig.Profile input) {
		BetterHudsConfig.Profile profile = new BetterHudsConfig.Profile();
		profile.name = (input.name == null || input.name.isBlank()) ? "Imported Profile" : input.name.trim();
		profile.widgets = new LinkedHashMap<>();

		Map<String, BetterHudsConfig.WidgetConfig> widgets = input.widgets == null ? Map.of() : input.widgets;
		for (Map.Entry<String, BetterHudsConfig.WidgetConfig> entry : widgets.entrySet()) {
			String id = entry.getKey();
			if (id == null || id.isBlank()) {
				continue;
			}
			BetterHudsConfig.WidgetConfig source = entry.getValue();
			if (source == null) {
				profile.widgets.put(id, new BetterHudsConfig.WidgetConfig(8, 8, Anchor.TOP_LEFT));
				continue;
			}
			BetterHudsConfig.WidgetConfig copy = new BetterHudsConfig.WidgetConfig(source.x, source.y, source.anchor == null ? Anchor.TOP_LEFT : source.anchor);
			copy.enabled = source.enabled;
			copy.scale = source.scale;
			copy.background = source.background;
			copy.showText = source.showText;
			copy.backgroundColor = source.backgroundColor;
			copy.textColor = source.textColor;
			copy.toggles = source.toggles == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.toggles);
			copy.values = source.values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source.values);
			profile.widgets.put(id, copy);
		}
		return profile;
	}
}
