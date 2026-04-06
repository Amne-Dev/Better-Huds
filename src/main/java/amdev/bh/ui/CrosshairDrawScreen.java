package amdev.bh.ui;

import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.HudSystem;
import amdev.bh.hud.widget.CrosshairPatternUtil;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CrosshairDrawScreen extends Screen {
	private final HudSystem hudSystem;
	private final Screen parent;
	private Tool activeTool = Tool.PEN;
	private boolean painting;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int gridX;
	private int gridY;
	private int cellSize;

	public CrosshairDrawScreen(HudSystem hudSystem, Screen parent) {
		super(Component.translatable("screen.better-huds.crosshair_draw.title"));
		this.hudSystem = hudSystem;
		this.parent = parent;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		clearWidgets();
		panelWidth = Math.min(560, width - 20);
		panelHeight = Math.min(420, height - 20);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		int topY = panelY + 36;
		int x = panelX + 12;

		addRenderableWidget(new GlassButton(x, topY, 66, 20, Component.translatable("screen.better-huds.crosshair_draw.pen"), button -> {
			activeTool = Tool.PEN;
			init();
		}, activeTool == Tool.PEN ? 0xFFA6E66A : 0xFF80D8FF));
		x += 72;
		addRenderableWidget(new GlassButton(x, topY, 76, 20, Component.translatable("screen.better-huds.crosshair_draw.eraser"), button -> {
			activeTool = Tool.ERASER;
			init();
		}, activeTool == Tool.ERASER ? 0xFFFFA0A0 : 0xFF80D8FF));
		x += 82;
		addRenderableWidget(new GlassButton(x, topY, 66, 20, Component.translatable("screen.better-huds.crosshair_draw.clear"), button -> {
			CrosshairPatternUtil.clear(cfg());
			CrosshairPatternUtil.setUseDrawnPattern(cfg(), true);
			hudSystem.configManager().save();
		}));
		x += 72;
		addRenderableWidget(new GlassButton(x, topY, 92, 20, Component.translatable("screen.better-huds.crosshair_draw.grid", canvasLabel(CrosshairPatternUtil.gridSize(cfg()))), button -> {
			int next = CrosshairPatternUtil.nextGridSize(CrosshairPatternUtil.gridSize(cfg()));
			CrosshairPatternUtil.setGridSize(cfg(), next);
			hudSystem.configManager().save();
			init();
		}));
		x += 98;
		addRenderableWidget(new GlassButton(x, topY, 102, 20, Component.translatable(
			"screen.better-huds.crosshair_draw.use_drawn",
			CrosshairPatternUtil.useDrawnPattern(cfg()) ? "ON" : "OFF"
		), button -> {
			CrosshairPatternUtil.setUseDrawnPattern(cfg(), !CrosshairPatternUtil.useDrawnPattern(cfg()));
			hudSystem.configManager().save();
			init();
		}));

		addRenderableWidget(new GlassButton(panelX + panelWidth - 90, panelY + 8, 78, 20, Component.translatable("screen.better-huds.back"), button -> onClose()));
		recalculateGridBounds();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return false;
		}
		boolean handled = applyToolAt(event.x(), event.y());
		painting = handled;
		return handled;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (painting && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return applyToolAt(event.x(), event.y());
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		painting = false;
		return super.mouseReleased(event);
	}

	private boolean applyToolAt(double mouseX, double mouseY) {
		int[] cell = cellAt(mouseX, mouseY);
		if (cell == null) {
			return false;
		}
		boolean on = activeTool == Tool.PEN;
		CrosshairPatternUtil.setPixel(cfg(), cell[0], cell[1], on);
		if (on) {
			CrosshairPatternUtil.setUseDrawnPattern(cfg(), true);
		}
		hudSystem.configManager().save();
		return true;
	}

	private int[] cellAt(double mouseX, double mouseY) {
		int grid = CrosshairPatternUtil.gridSize(cfg());
		int size = grid * cellSize;
		if (mouseX < gridX || mouseY < gridY || mouseX >= gridX + size || mouseY >= gridY + size) {
			return null;
		}
		int x = (int) ((mouseX - gridX) / cellSize);
		int y = (int) ((mouseY - gridY) / cellSize);
		if (x < 0 || y < 0 || x >= grid || y >= grid) {
			return null;
		}
		return new int[]{x, y};
	}

	private void recalculateGridBounds() {
		int grid = CrosshairPatternUtil.gridSize(cfg());
		int available = Math.min(panelWidth - 24, panelHeight - 120);
		cellSize = Math.max(4, Math.min(22, Math.max(1, available / grid)));
		int total = grid * cellSize;
		gridX = panelX + (panelWidth - total) / 2;
		gridY = panelY + 72;
	}

	private BetterHudsConfig.WidgetConfig cfg() {
		return hudSystem.config().getOrCreateWidgetConfig("crosshair");
	}

	@Override
	public void onClose() {
		hudSystem.configManager().save();
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
		if (amdev.bh.util.McCompat.shouldDisableUiBlur()) {
			graphics.fill(0, 0, width, height, 0x66000000);
		} else {
			extractTransparentBackground(graphics);
		}
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xCC111111);
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xFF80D8FF);
		graphics.text(font, title, panelX + 14, panelY + 15, 0xFFFFFFFF, false);
		graphics.text(font, Component.translatable("screen.better-huds.crosshair_draw.hint"), panelX + 14, panelY + panelHeight - 16, 0xFFB6E3FF, false);

		renderGrid(graphics, mouseX, mouseY);
		super.extractRenderState(graphics, mouseX, mouseY, tickDelta);
	}

	private void renderGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		BetterHudsConfig.WidgetConfig widgetCfg = cfg();
		int grid = CrosshairPatternUtil.gridSize(widgetCfg);
		int total = grid * cellSize;
		int color = widgetCfg.textColor;
		int gridColorA = 0x553A4A5C;
		int gridColorB = 0x44304152;

		for (int y = 0; y < grid; y++) {
			for (int x = 0; x < grid; x++) {
				int left = gridX + x * cellSize;
				int top = gridY + y * cellSize;
				int back = ((x + y) & 1) == 0 ? gridColorA : gridColorB;
				graphics.fill(left, top, left + cellSize, top + cellSize, back);
			}
		}

		for (int i = 0; i <= grid; i++) {
			int gx = gridX + i * cellSize;
			int gy = gridY + i * cellSize;
			int lineColor = ((grid & 1) == 0 && i == grid / 2) ? 0xAA80D8FF : 0x445A7A95;
			graphics.fill(gx, gridY, gx + 1, gridY + total, lineColor);
			graphics.fill(gridX, gy, gridX + total, gy + 1, lineColor);
		}
		graphics.fill(gridX, gridY, gridX + total, gridY + 1, 0xFF80D8FF);
		graphics.fill(gridX, gridY + total - 1, gridX + total, gridY + total, 0xFF80D8FF);
		graphics.fill(gridX, gridY, gridX + 1, gridY + total, 0xFF80D8FF);
		graphics.fill(gridX + total - 1, gridY, gridX + total, gridY + total, 0xFF80D8FF);
		if ((grid & 1) != 0) {
			int centerCell = grid / 2;
			int centerLeft = gridX + centerCell * cellSize;
			int centerTop = gridY + centerCell * cellSize;
			int centerRight = centerLeft + cellSize;
			int centerBottom = centerTop + cellSize;
			graphics.fill(centerLeft, centerTop, centerRight, centerBottom, 0x1E80D8FF);
			graphics.fill(centerLeft, centerTop, centerRight, centerTop + 1, 0xFF80D8FF);
			graphics.fill(centerLeft, centerBottom - 1, centerRight, centerBottom, 0xFF80D8FF);
			graphics.fill(centerLeft, centerTop, centerLeft + 1, centerBottom, 0xFF80D8FF);
			graphics.fill(centerRight - 1, centerTop, centerRight, centerBottom, 0xFF80D8FF);
		}

		// Draw active pixels last so adjacent pixels are flush with no visible spacing.
		for (int y = 0; y < grid; y++) {
			for (int x = 0; x < grid; x++) {
				if (!CrosshairPatternUtil.pixel(widgetCfg, x, y)) {
					continue;
				}
				int left = gridX + x * cellSize;
				int top = gridY + y * cellSize;
				graphics.fill(left, top, left + cellSize, top + cellSize, color);
			}
		}

		int[] hovered = cellAt(mouseX, mouseY);
		if (hovered != null) {
			int left = gridX + hovered[0] * cellSize;
			int top = gridY + hovered[1] * cellSize;
			graphics.fill(left, top, left + cellSize, top + 1, 0xFFFFFFFF);
			graphics.fill(left, top + cellSize - 1, left + cellSize, top + cellSize, 0xFFFFFFFF);
			graphics.fill(left, top, left + 1, top + cellSize, 0xFFFFFFFF);
			graphics.fill(left + cellSize - 1, top, left + cellSize, top + cellSize, 0xFFFFFFFF);
		}
	}

	private static String canvasLabel(int gridSize) {
		return gridSize + "x" + gridSize;
	}

	private enum Tool {
		PEN,
		ERASER
	}
}
