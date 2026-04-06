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
	private final float baseReferenceX;
	private final float baseReferenceY;

	public ResolvedWidget(HudWidget widget, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y, int baseWidth, int baseHeight, float appliedScale, float baseReferenceX, float baseReferenceY) {
		this.widget = widget;
		this.widgetConfig = widgetConfig;
		this.x = x;
		this.y = y;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
		this.appliedScale = appliedScale;
		this.baseReferenceX = baseReferenceX;
		this.baseReferenceY = baseReferenceY;
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

	public float scaledReferenceX() {
		return baseReferenceX * appliedScale;
	}

	public float scaledReferenceY() {
		return baseReferenceY * appliedScale;
	}

	public float screenReferenceX() {
		return x + scaledReferenceX();
	}

	public float screenReferenceY() {
		return y + scaledReferenceY();
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
