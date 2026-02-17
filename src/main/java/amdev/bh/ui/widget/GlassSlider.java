package amdev.bh.ui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.Function;

public class GlassSlider extends AbstractSliderButton {
	private final double min;
	private final double max;
	private final double step;
	private final DoubleConsumer onValueChanged;
	private final Function<Double, Component> messageFactory;
	private double currentValue;

	public GlassSlider(
		int x,
		int y,
		int width,
		int height,
		double value,
		double min,
		double max,
		double step,
		Function<Double, Component> messageFactory,
		DoubleConsumer onValueChanged
	) {
		super(x, y, width, height, Component.empty(), normalize(value, min, max));
		this.min = min;
		this.max = max;
		this.step = Math.max(0.0D, step);
		this.messageFactory = messageFactory;
		this.onValueChanged = onValueChanged;
		this.currentValue = clampAndSnap(value);
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		setMessage(messageFactory.apply(currentValue));
	}

	@Override
	protected void applyValue() {
		double raw = min + (max - min) * value;
		double snapped = clampAndSnap(raw);
		if (Math.abs(snapped - currentValue) < 0.000001D) {
			return;
		}
		currentValue = snapped;
		onValueChanged.accept(currentValue);
		updateMessage();
	}

	public double currentValue() {
		return currentValue;
	}

	private double clampAndSnap(double value) {
		double clamped = Math.max(min, Math.min(max, value));
		if (step <= 0.0D) {
			return clamped;
		}
		double snapped = Math.round(clamped / step) * step;
		return Math.max(min, Math.min(max, snapped));
	}

	private static double normalize(double value, double min, double max) {
		if (max <= min) {
			return 0.0D;
		}
		double clamped = Math.max(min, Math.min(max, value));
		return (clamped - min) / (max - min);
	}
}
