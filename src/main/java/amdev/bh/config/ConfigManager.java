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
	/**
	 * Minimum interval between actual disk writes. Callers save on every slider
	 * drag frame; without throttling this writes the config hundreds of times
	 * per second. The in-memory config is always up to date, so only the disk
	 * write frequency is reduced.
	 */
	private static final long SAVE_THROTTLE_MS = 500L;
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("better-huds.json");
	private BetterHudsConfig config;
	private long lastSaveMs = Long.MIN_VALUE;
	private boolean savePending;

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
			backUpUnreadableConfig();
			save();
		}

		return config;
	}

	private void backUpUnreadableConfig() {
		try {
			Path backupPath = configPath.resolveSibling(configPath.getFileName() + ".broken");
			if (Files.exists(configPath) && !Files.exists(backupPath)) {
				Files.copy(configPath, backupPath);
				BetterHuds.LOGGER.warn("Backed up unreadable Better Huds config to {}", backupPath);
			}
		} catch (IOException exception) {
			BetterHuds.LOGGER.error("Failed to back up unreadable config at {}", configPath, exception);
		}
	}

	public BetterHudsConfig config() {
		return load();
	}

	public void save() {
		if (config == null) {
			return;
		}

		long now = System.currentTimeMillis();
		if (lastSaveMs != Long.MIN_VALUE && now - lastSaveMs < SAVE_THROTTLE_MS) {
			savePending = true;
			return;
		}
		writeToDisk(now);
	}

	/**
	 * Writes the config to disk immediately, bypassing the throttle.
	 */
	private void writeToDisk(long nowMs) {
		savePending = false;
		lastSaveMs = nowMs;
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

	/**
	 * Flushes any change that was skipped by the save throttle. Called from
	 * screen close paths; cheap no-op when nothing is pending.
	 */
	public void flushPendingSave() {
		if (savePending && config != null) {
			writeToDisk(System.currentTimeMillis());
		}
	}
}
