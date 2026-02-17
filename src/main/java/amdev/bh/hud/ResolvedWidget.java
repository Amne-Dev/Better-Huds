package amdev.bh.hud;

import amdev.bh.config.BetterHudsConfig;

public class ResolvedWidget {
	private final HudWidget widget;
	private final BetterHudsConfig.WidgetConfig widgetConfig;
	private final int x;
	private final int y;
	private final int baseWidth;
	private final int baseHeight;
	private final float appliedScale;

	public ResolvedWidget(HudWidget widget, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y, int baseWidth, int baseHeight, float appliedScale) {
		this.widget = widget;
		this.widgetConfig = widgetConfig;
		this.x = x;
		this.y = y;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
		this.appliedScale = appliedScale;
	}

	public HudWidget widget() {
		return widget;
	}

	public BetterHudsConfig.WidgetConfig widgetConfig() {
		return widgetConfig;
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int baseWidth() {
		return baseWidth;
	}

	public int baseHeight() {
		return baseHeight;
	}

	public float appliedScale() {
		return appliedScale;
	}

	public int scaledWidth() {
		return Math.max(1, Math.round(baseWidth * appliedScale));
	}

	public int scaledHeight() {
		return Math.max(1, Math.round(baseHeight * appliedScale));
	}

	public boolean contains(double mouseX, double mouseY) {
		return mouseX >= x && mouseY >= y && mouseX < x + scaledWidth() && mouseY < y + scaledHeight();
	}
}
