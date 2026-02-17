package amdev.bh.hud;

import amdev.bh.config.BetterHudsConfig;

public class HudRenderContext {
	private final BetterHudsConfig config;
	private final MetricsTracker metrics;
	private final ItemHistoryTracker itemHistory;
	private final boolean editorMode;
	private final boolean miniInventoryVisible;

	public HudRenderContext(BetterHudsConfig config, MetricsTracker metrics, ItemHistoryTracker itemHistory, boolean editorMode, boolean miniInventoryVisible) {
		this.config = config;
		this.metrics = metrics;
		this.itemHistory = itemHistory;
		this.editorMode = editorMode;
		this.miniInventoryVisible = miniInventoryVisible;
	}

	public BetterHudsConfig config() {
		return config;
	}

	public MetricsTracker metrics() {
		return metrics;
	}

	public ItemHistoryTracker itemHistory() {
		return itemHistory;
	}

	public boolean editorMode() {
		return editorMode;
	}

	public boolean miniInventoryVisible() {
		return miniInventoryVisible;
	}
}
