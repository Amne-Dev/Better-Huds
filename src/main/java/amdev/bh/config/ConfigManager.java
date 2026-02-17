package amdev.bh.config;

import amdev.bh.BetterHuds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("better-huds.json");
	private BetterHudsConfig config;

	public BetterHudsConfig load() {
		if (config != null) {
			return config;
		}

		if (!Files.exists(configPath)) {
			config = BetterHudsConfig.createDefault();
			save();
			return config;
		}

		try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
			config = GSON.fromJson(reader, BetterHudsConfig.class);
		} catch (IOException | JsonParseException exception) {
			BetterHuds.LOGGER.error("Failed to read config at {}", configPath, exception);
		}

		if (config == null) {
			config = BetterHudsConfig.createDefault();
			save();
		}

		return config;
	}

	public BetterHudsConfig config() {
		return load();
	}

	public void save() {
		if (config == null) {
			return;
		}

		try {
			Path parent = configPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException exception) {
			BetterHuds.LOGGER.error("Failed to write config at {}", configPath, exception);
		}
	}
}
