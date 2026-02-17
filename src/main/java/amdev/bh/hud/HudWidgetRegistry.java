package amdev.bh.hud;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class HudWidgetRegistry {
	private final Map<String, HudWidget> widgets = new LinkedHashMap<>();

	public void register(HudWidget widget) {
		widgets.put(widget.id(), widget);
	}

	public HudWidget get(String id) {
		return widgets.get(id);
	}

	public Collection<HudWidget> all() {
		return widgets.values();
	}
}
