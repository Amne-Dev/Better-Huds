package amdev.bh.ui;

import amdev.bh.hud.widget.WidgetRenderUtil;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public class ColorPickerScreen extends Screen {
	private static final int DRAW_STEP_IDLE = 3;
	private static final int DRAW_STEP_DRAG = 4;

	private final Screen parent;
	private final IntConsumer onColorChanged;
	private float hue;
	private float saturation;
	private float value;
	private int alpha;
	private EditBox hexBox;
	private boolean updatingText;
	private boolean draggingHue;
	private boolean draggingSv;
	private boolean draggingAlpha;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int wheelCenterX;
	private int wheelCenterY;
	private int wheelOuterRadius;
	private int wheelInnerRadius;
	private int squareX;
	private int squareY;
	private int squareSize;
	private int alphaBarX;
	private int alphaBarY;
	private int alphaBarWidth;
	private int alphaBarHeight;

	public ColorPickerScreen(Screen parent, Component title, int initialColor, IntConsumer onColorChanged) {
		super(title);
		this.parent = parent;
		this.onColorChanged = onColorChanged;
		float[] hsv = WidgetRenderUtil.rgbToHsv(initialColor);
		this.hue = hsv[0];
		this.saturation = hsv[1];
		this.value = hsv[2];
		this.alpha = (initialColor >>> 24) & 0xFF;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		clearWidgets();
		panelWidth = 392;
		panelHeight = 324;
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;

		wheelCenterX = panelX + 96;
		wheelCenterY = panelY + 132;
		wheelOuterRadius = 66;
		wheelInnerRadius = 48;
		squareX = panelX + 180;
		squareY = panelY + 62;
		squareSize = 154;
		alphaBarX = panelX + 180;
		alphaBarY = panelY + 228;
		alphaBarWidth = 154;
		alphaBarHeight = 10;

		hexBox = new EditBox(font, panelX + 180, panelY + 252, 154, 20, Component.translatable("screen.better-huds.color_picker.hex"));
		hexBox.setMaxLength(9);
		hexBox.setFilter(text -> text.matches("#?[0-9a-fA-F]*"));
		hexBox.setResponder(this::onHexInputChanged);
		updateHexField();
		addRenderableWidget(hexBox);

		addRenderableWidget(new GlassButton(panelX + panelWidth - 92, panelY + 10, 78, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		addRenderableWidget(new GlassButton(panelX + 180, panelY + 286, 74, 20, Component.translatable("screen.better-huds.color_picker.reset"), button -> {
			hue = 0.0F;
			saturation = 0.0F;
			value = 1.0F;
			alpha = 255;
			applyCurrentColor();
		}));
	}

	private void onHexInputChanged(String text) {
		if (updatingText) {
			return;
		}
		String normalized = normalizedHex(text);
		if (!(normalized.length() == 6 || normalized.length() == 8)) {
			return;
		}
		for (int i = 0; i < normalized.length(); i++) {
			char c = Character.toUpperCase(normalized.charAt(i));
			boolean digit = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
			if (!digit) {
				return;
			}
		}
		int parsed = WidgetRenderUtil.parseHexColor(normalized, currentColor());
		float[] hsv = WidgetRenderUtil.rgbToHsv(parsed);
		hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
		if (hexDigitsCount(text) == 8) {
			alpha = (parsed >>> 24) & 0xFF;
		}
		onColorChanged.accept(currentColor());
	}

	private int currentColor() {
		return (alpha << 24) | (WidgetRenderUtil.hsvToRgb(hue, saturation, value) & 0x00FFFFFF);
	}

	private void applyCurrentColor() {
		onColorChanged.accept(currentColor());
		updateHexField();
	}

	private void updateHexField() {
		updatingText = true;
		hexBox.setValue(WidgetRenderUtil.shortColor(currentColor()));
		updatingText = false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}

		if (isInsideHueRing(event.x(), event.y())) {
			draggingHue = true;
			updateHueFromMouse(event.x(), event.y());
			return true;
		}
		if (isInsideSvSquare(event.x(), event.y())) {
			draggingSv = true;
			updateSvFromMouse(event.x(), event.y());
			return true;
		}
		if (isInsideAlphaBar(event.x(), event.y())) {
			draggingAlpha = true;
			updateAlphaFromMouse(event.x());
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (draggingHue) {
			updateHueFromMouse(event.x(), event.y());
			return true;
		}
		if (draggingSv) {
			updateSvFromMouse(event.x(), event.y());
			return true;
		}
		if (draggingAlpha) {
			updateAlphaFromMouse(event.x());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		draggingHue = false;
		draggingSv = false;
		draggingAlpha = false;
		return super.mouseReleased(event);
	}

	private boolean isInsideHueRing(double mouseX, double mouseY) {
		double dx = mouseX - wheelCenterX;
		double dy = mouseY - wheelCenterY;
		double distSquared = dx * dx + dy * dy;
		return distSquared <= wheelOuterRadius * wheelOuterRadius && distSquared >= wheelInnerRadius * wheelInnerRadius;
	}

	private boolean isInsideSvSquare(double mouseX, double mouseY) {
		return mouseX >= squareX && mouseX <= squareX + squareSize && mouseY >= squareY && mouseY <= squareY + squareSize;
	}

	private boolean isInsideAlphaBar(double mouseX, double mouseY) {
		return mouseX >= alphaBarX && mouseX <= alphaBarX + alphaBarWidth && mouseY >= alphaBarY && mouseY <= alphaBarY + alphaBarHeight;
	}

	private void updateHueFromMouse(double mouseX, double mouseY) {
		double angle = Math.atan2(mouseY - wheelCenterY, mouseX - wheelCenterX);
		hue = (float) ((angle / (Math.PI * 2.0D)) + 0.5D);
		if (hue < 0.0F) {
			hue += 1.0F;
		}
		if (hue >= 1.0F) {
			hue -= 1.0F;
		}
		applyCurrentColor();
	}

	private void updateSvFromMouse(double mouseX, double mouseY) {
		saturation = (float) ((mouseX - squareX) / squareSize);
		value = 1.0F - (float) ((mouseY - squareY) / squareSize);
		saturation = Math.max(0.0F, Math.min(1.0F, saturation));
		value = Math.max(0.0F, Math.min(1.0F, value));
		applyCurrentColor();
	}

	private void updateAlphaFromMouse(double mouseX) {
		float t = (float) ((mouseX - alphaBarX) / Math.max(1, alphaBarWidth - 1));
		t = Math.max(0.0F, Math.min(1.0F, t));
		alpha = Math.round(t * 255.0F);
		applyCurrentColor();
	}

	@Override
	public void onClose() {
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		renderTransparentBackground(graphics);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.drawString(font, title, panelX + 14, panelY + 16, 0xFFFFFFFF, false);

		renderHueRing(graphics);
		renderSvSquare(graphics);
		renderMarkers(graphics);
		renderAlphaBar(graphics);

		int currentColor = currentColor();
		renderCheckerboard(graphics, panelX + 180, panelY + 206, panelX + 254, panelY + 224, 4);
		graphics.fill(panelX + 180, panelY + 206, panelX + 254, panelY + 224, currentColor);
		renderCheckerboard(graphics, panelX + 258, panelY + 206, panelX + 334, panelY + 224, 4);
		graphics.fill(panelX + 258, panelY + 206, panelX + 334, panelY + 224, 0xFF000000 | (currentColor & 0x00FFFFFF));
		graphics.drawString(font, Component.translatable("screen.better-huds.color_picker.preview"), panelX + 180, panelY + 192, 0xFFB6E3FF, false);

		graphics.drawString(font, Component.translatable("screen.better-huds.color_picker.opacity", Math.round((alpha / 255.0F) * 100.0F)), panelX + 180, panelY + 216, 0xFFB6E3FF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.color_picker.hex"), panelX + 180, panelY + 242, 0xFFB6E3FF, false);
		graphics.drawString(font, Component.translatable("screen.better-huds.color_picker.hint"), panelX + 14, panelY + 304, 0xFFB6E3FF, false);

		super.render(graphics, mouseX, mouseY, tickDelta);
	}

	private void renderHueRing(GuiGraphics graphics) {
		int drawStep = currentDrawStep();
		int outerSq = wheelOuterRadius * wheelOuterRadius;
		int innerSq = wheelInnerRadius * wheelInnerRadius;
		for (int y = wheelCenterY - wheelOuterRadius; y <= wheelCenterY + wheelOuterRadius; y += drawStep) {
			for (int x = wheelCenterX - wheelOuterRadius; x <= wheelCenterX + wheelOuterRadius; x += drawStep) {
				int dx = x - wheelCenterX;
				int dy = y - wheelCenterY;
				int distSq = dx * dx + dy * dy;
				if (distSq > outerSq || distSq < innerSq) {
					continue;
				}

				double angle = Math.atan2(dy, dx);
				float localHue = (float) ((angle / (Math.PI * 2.0D)) + 0.5D);
				int color = WidgetRenderUtil.hsvToRgb(localHue, 1.0F, 1.0F);
				graphics.fill(x, y, x + drawStep, y + drawStep, color);
			}
		}
	}

	private void renderSvSquare(GuiGraphics graphics) {
		int drawStep = currentDrawStep();
		for (int y = 0; y < squareSize; y += drawStep) {
			for (int x = 0; x < squareSize; x += drawStep) {
				float s = x / (float) (squareSize - 1);
				float v = 1.0F - (y / (float) (squareSize - 1));
				int color = WidgetRenderUtil.hsvToRgb(hue, s, v);
				graphics.fill(squareX + x, squareY + y, squareX + x + drawStep, squareY + y + drawStep, color);
			}
		}

		graphics.fill(squareX, squareY, squareX + squareSize, squareY + 1, 0xFF80D8FF);
		graphics.fill(squareX, squareY + squareSize - 1, squareX + squareSize, squareY + squareSize, 0xFF80D8FF);
		graphics.fill(squareX, squareY, squareX + 1, squareY + squareSize, 0xFF80D8FF);
		graphics.fill(squareX + squareSize - 1, squareY, squareX + squareSize, squareY + squareSize, 0xFF80D8FF);
	}

	private void renderMarkers(GuiGraphics graphics) {
		double angle = (hue - 0.5D) * Math.PI * 2.0D;
		int ringRadius = (wheelInnerRadius + wheelOuterRadius) / 2;
		int hueX = wheelCenterX + (int) Math.round(Math.cos(angle) * ringRadius);
		int hueY = wheelCenterY + (int) Math.round(Math.sin(angle) * ringRadius);
		graphics.fill(hueX - 2, hueY - 2, hueX + 2, hueY + 2, 0xFFFFFFFF);
		graphics.fill(hueX - 1, hueY - 1, hueX + 1, hueY + 1, 0xFF000000);

		int svX = squareX + Math.round(saturation * squareSize);
		int svY = squareY + Math.round((1.0F - value) * squareSize);
		graphics.fill(svX - 3, svY - 1, svX + 3, svY + 1, 0xFFFFFFFF);
		graphics.fill(svX - 1, svY - 3, svX + 1, svY + 3, 0xFFFFFFFF);
		graphics.fill(svX - 2, svY, svX + 2, svY + 1, 0xFF000000);
		graphics.fill(svX, svY - 2, svX + 1, svY + 2, 0xFF000000);
	}

	private void renderAlphaBar(GuiGraphics graphics) {
		renderCheckerboard(graphics, alphaBarX, alphaBarY, alphaBarX + alphaBarWidth, alphaBarY + alphaBarHeight, 3);
		int rgb = WidgetRenderUtil.hsvToRgb(hue, saturation, value) & 0x00FFFFFF;
		for (int i = 0; i < alphaBarWidth; i++) {
			float t = i / (float) Math.max(1, alphaBarWidth - 1);
			int a = Math.round(t * 255.0F);
			graphics.fill(alphaBarX + i, alphaBarY, alphaBarX + i + 1, alphaBarY + alphaBarHeight, (a << 24) | rgb);
		}

		graphics.fill(alphaBarX, alphaBarY, alphaBarX + alphaBarWidth, alphaBarY + 1, 0xFF80D8FF);
		graphics.fill(alphaBarX, alphaBarY + alphaBarHeight - 1, alphaBarX + alphaBarWidth, alphaBarY + alphaBarHeight, 0xFF80D8FF);
		graphics.fill(alphaBarX, alphaBarY, alphaBarX + 1, alphaBarY + alphaBarHeight, 0xFF80D8FF);
		graphics.fill(alphaBarX + alphaBarWidth - 1, alphaBarY, alphaBarX + alphaBarWidth, alphaBarY + alphaBarHeight, 0xFF80D8FF);

		int markerX = alphaBarX + Math.round((alpha / 255.0F) * (alphaBarWidth - 1));
		graphics.fill(markerX - 1, alphaBarY - 2, markerX + 1, alphaBarY + alphaBarHeight + 2, 0xFFFFFFFF);
		graphics.fill(markerX, alphaBarY - 1, markerX + 1, alphaBarY + alphaBarHeight + 1, 0xFF000000);
	}

	private void renderCheckerboard(GuiGraphics graphics, int x1, int y1, int x2, int y2, int step) {
		int c1 = 0xFF4A4A4A;
		int c2 = 0xFF2E2E2E;
		for (int y = y1; y < y2; y += step) {
			for (int x = x1; x < x2; x += step) {
				boolean flip = ((x - x1) / step + (y - y1) / step) % 2 == 0;
				graphics.fill(x, y, Math.min(x + step, x2), Math.min(y + step, y2), flip ? c1 : c2);
			}
		}
	}

	private int hexDigitsCount(String text) {
		return normalizedHex(text).length();
	}

	private String normalizedHex(String text) {
		if (text == null) {
			return "";
		}
		String normalized = text.trim();
		if (normalized.startsWith("#")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}

	private int currentDrawStep() {
		return draggingHue || draggingSv ? DRAW_STEP_DRAG : DRAW_STEP_IDLE;
	}
}
