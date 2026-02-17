package amdev.bh.api;

import amdev.bh.hud.HudWidget;

public final class BetterHudsApi {
	private BetterHudsApi() {
	}

	@FunctionalInterface
	public interface WidgetRegistrar {
		void register(HudWidget widget);
	}

	public interface WidgetEntrypoint {
		void register(WidgetRegistrar registrar);
	}
}
