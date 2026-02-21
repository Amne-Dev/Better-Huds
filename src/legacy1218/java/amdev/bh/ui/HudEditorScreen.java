package amdev.bh.ui;

import amdev.bh.util.McCompat;
import amdev.bh.config.BetterHudsConfig;
import amdev.bh.hud.Anchor;
import amdev.bh.hud.HudLayout;
import amdev.bh.hud.HudSystem;
import amdev.bh.hud.ResolvedWidget;
import amdev.bh.ui.widget.GlassButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class HudEditorScreen extends Screen {
	private final HudSystem hudSystem;
	private GlassButton settingsButton;
	private GlassButton tipToggleButton;
	private String draggingWidgetId;
	private String resizingWidgetId;
	private String selectedWidgetId;
	private int dragStartWidgetX;
	private int dragStartWidgetY;
	private double dragDeltaX;
	private double dragDeltaY;
	private long lastLeftClickTimeMs;
	private double lastLeftClickX;
	private double lastLeftClickY;
	private String lastLeftClickWidgetId;

	public HudEditorScreen(HudSystem hudSystem) {
		super(Component.translatable("screen.better-huds.editor"));
		this.hudSystem = hudSystem;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		settingsButton = addRenderableWidget(new GlassButton(
			width / 2 - 94,
			height / 2 + 36,
			188,
			20,
			Component.translatable("screen.better-huds.editor.open_settings"),
			button -> {
				if (minecraft != null) {
					minecraft.setScreen(new HudSettingsScreen(hudSystem, this));
				}
			}
		));
		int tipWidth = tipWidth();
		int tipX = (width - tipWidth) / 2;
		int tipY = tipY();
		tipToggleButton = addRenderableWidget(new GlassButton(
			tipX + tipWidth - 20,
			tipY + 2,
			18,
			16,
			Component.literal(hudSystem.config().editorTipMinimized ? "+" : "-"),
			button -> {
				BetterHudsConfig config = hudSystem.config();
				config.editorTipMinimized = !config.editorTipMinimized;
				hudSystem.configManager().save();
				button.setMessage(Component.literal(config.editorTipMinimized ? "+" : "-"));
			}
		));
	}

	@Override
	public void onClose() {
		hudSystem.configManager().save();
		if (minecraft != null) {
			minecraft.setScreen(null);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		int key = keyCode;
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}

		if (selectedWidgetId != null) {
			BetterHudsConfig config = hudSystem.config();
			BetterHudsConfig.WidgetConfig widgetConfig = config.getOrCreateWidgetConfig(selectedWidgetId);
			boolean ignoreSnap = Screen.hasControlDown();
			int step = ignoreSnap ? 1 : config.getGridSizeOrDefault();
			boolean moved = false;
			if (key == GLFW.GLFW_KEY_LEFT) {
				widgetConfig.x -= step;
				moved = true;
			} else if (key == GLFW.GLFW_KEY_RIGHT) {
				widgetConfig.x += step;
				moved = true;
			} else if (key == GLFW.GLFW_KEY_UP) {
				widgetConfig.y -= step;
				moved = true;
			} else if (key == GLFW.GLFW_KEY_DOWN) {
				widgetConfig.y += step;
				moved = true;
			}

			if (moved) {
				widgetConfig.anchor = Anchor.TOP_LEFT;
				if (config.snapToGrid && !ignoreSnap) {
					int grid = config.getGridSizeOrDefault();
					widgetConfig.x = HudLayout.snap(widgetConfig.x, grid);
					widgetConfig.y = HudLayout.snap(widgetConfig.y, grid);
				}
				int[] clamped = applyEdgeSnapAndClamp(selectedWidgetId, widgetConfig.x, widgetConfig.y, !ignoreSnap);
				widgetConfig.x = clamped[0];
				widgetConfig.y = clamped[1];
				hudSystem.configManager().save();
				return true;
			}
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return false;
		}

		List<ResolvedWidget> widgets = hudSystem.getResolvedWidgets(false);
		for (int i = widgets.size() - 1; i >= 0; i--) {
			ResolvedWidget candidate = widgets.get(i);
			if (!isInResizeHandle(candidate, mouseX, mouseY)) {
				continue;
			}
			resizingWidgetId = candidate.widget().id();
			selectedWidgetId = resizingWidgetId;
			BetterHudsConfig.WidgetConfig widgetConfig = hudSystem.config().getOrCreateWidgetConfig(resizingWidgetId);
			widgetConfig.anchor = Anchor.TOP_LEFT;
			widgetConfig.x = candidate.x();
			widgetConfig.y = candidate.y();
			return true;
		}

		ResolvedWidget hit = null;
		for (int i = widgets.size() - 1; i >= 0; i--) {
			ResolvedWidget candidate = widgets.get(i);
			if (candidate.contains(mouseX, mouseY)) {
				hit = candidate;
				break;
			}
		}

		if (hit == null) {
			return false;
		}

		boolean doubleClick = isWidgetDoubleClick(hit.widget().id(), mouseX, mouseY);
		recordClick(hit.widget().id(), mouseX, mouseY);
		if (doubleClick && minecraft != null) {
			minecraft.setScreen(new WidgetSettingsScreen(hudSystem, this, settingsTargetWidgetId(hit.widget().id())));
			return true;
		}

		draggingWidgetId = hit.widget().id();
		selectedWidgetId = draggingWidgetId;
		BetterHudsConfig.WidgetConfig widgetConfig = hudSystem.config().getOrCreateWidgetConfig(draggingWidgetId);
		widgetConfig.anchor = Anchor.TOP_LEFT;
		widgetConfig.x = hit.x();
		widgetConfig.y = hit.y();
		dragStartWidgetX = widgetConfig.x;
		dragStartWidgetY = widgetConfig.y;
		dragDeltaX = 0.0D;
		dragDeltaY = 0.0D;
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (resizingWidgetId != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return resizeWidget(mouseX, mouseY);
		}

		if (draggingWidgetId == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}

		BetterHudsConfig config = hudSystem.config();
		BetterHudsConfig.WidgetConfig widgetConfig = config.getOrCreateWidgetConfig(draggingWidgetId);
		dragDeltaX += dragX;
		dragDeltaY += dragY;
		int newX = dragStartWidgetX + (int) Math.round(dragDeltaX);
		int newY = dragStartWidgetY + (int) Math.round(dragDeltaY);
		boolean ignoreSnap = Screen.hasControlDown();

		if (config.snapToGrid && !ignoreSnap) {
			int grid = config.getGridSizeOrDefault();
			newX = HudLayout.snap(newX, grid);
			newY = HudLayout.snap(newY, grid);
		}
		int[] snapped = applyEdgeSnapAndClamp(draggingWidgetId, newX, newY, !ignoreSnap);
		newX = snapped[0];
		newY = snapped[1];

		widgetConfig.x = newX;
		widgetConfig.y = newY;
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (draggingWidgetId != null || resizingWidgetId != null) {
			draggingWidgetId = null;
			resizingWidgetId = null;
			hudSystem.configManager().save();
			return true;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (McCompat.shouldDisableUiBlur()) {
			graphics.fill(0, 0, width, height, 0x66000000);
		} else {
			renderTransparentBackground(graphics);
		}
		renderGridGuidelines(graphics);
		hudSystem.renderForEditor(graphics);

		List<ResolvedWidget> widgets = hudSystem.getResolvedWidgets(false);
		for (ResolvedWidget widget : widgets) {
			int x1 = widget.x() - 1;
			int y1 = widget.y() - 1;
			int x2 = widget.x() + widget.scaledWidth() + 1;
			int y2 = widget.y() + widget.scaledHeight() + 1;
			boolean hovered = widget.contains(mouseX, mouseY);
			int border = hovered ? 0xFFFFFFFF : 0xFF80D8FF;
			graphics.fill(x1, y1, x2, y1 + 1, border);
			graphics.fill(x1, y2 - 1, x2, y2, border);
			graphics.fill(x1, y1, x1 + 1, y2, border);
			graphics.fill(x2 - 1, y1, x2, y2, border);

			String name = widget.widget().displayName().getString();
			graphics.drawString(font, name, x1 + 2, y1 - 10, 0xFFFFFFFF, true);

			int[] handle = resizeHandleRect(widget);
			int handleX = handle[0];
			int handleY = handle[1];
			int handleSize = handle[2];
			boolean handleHovered = isInResizeHandle(widget, mouseX, mouseY);
			int handleColor = handleHovered ? 0xFFFFFFFF : 0xFF80D8FF;
			graphics.fill(handleX, handleY, handleX + handleSize, handleY + handleSize, 0xAA000000);
			graphics.fill(handleX, handleY, handleX + handleSize, handleY + 1, handleColor);
			graphics.fill(handleX, handleY + handleSize - 1, handleX + handleSize, handleY + handleSize, handleColor);
			graphics.fill(handleX, handleY, handleX + 1, handleY + handleSize, handleColor);
			graphics.fill(handleX + handleSize - 1, handleY, handleX + handleSize, handleY + handleSize, handleColor);
		}

		if (!hudSystem.config().editorTipMinimized) {
			int tipWidth = tipWidth();
			int tipX = (width - tipWidth) / 2;
			int tipY = tipY();
			int tipHeight = selectedWidgetId != null ? 32 : 20;
			graphics.fill(tipX, tipY, tipX + tipWidth, tipY + tipHeight, 0x88000000);
			graphics.drawString(font, Component.translatable("screen.better-huds.editor.hint_compact"), tipX + 8, tipY + 6, 0xFFB6E3FF, false);
			if (selectedWidgetId != null) {
				graphics.drawString(font, Component.translatable("screen.better-huds.editor.nudge_hint_short"), tipX + 8, tipY + 17, 0xFFB6E3FF, false);
			}
		}
		super.render(graphics, mouseX, mouseY, tickDelta);
	}

	private boolean resizeWidget(double mouseX, double mouseY) {
		BetterHudsConfig config = hudSystem.config();
		BetterHudsConfig.WidgetConfig widgetConfig = config.getOrCreateWidgetConfig(resizingWidgetId);
		ResolvedWidget resolved = resolveWidget(resizingWidgetId);
		if (resolved == null || minecraft == null) {
			return false;
		}

		widgetConfig.anchor = Anchor.TOP_LEFT;
		widgetConfig.x = resolved.x();
		widgetConfig.y = resolved.y();

		int screenW = minecraft.getWindow().getGuiScaledWidth();
		int screenH = minecraft.getWindow().getGuiScaledHeight();
		int desiredW = (int) Math.round(mouseX - resolved.x());
		int desiredH = (int) Math.round(mouseY - resolved.y());
		int maxW = Math.max(8, screenW - resolved.x());
		int maxH = Math.max(8, screenH - resolved.y());
		desiredW = clamp(desiredW, 8, maxW);
		desiredH = clamp(desiredH, 8, maxH);

		boolean ignoreSnap = Screen.hasControlDown();
		if (config.snapToGrid && !ignoreSnap) {
			int grid = config.getGridSizeOrDefault();
			desiredW = Math.max(8, HudLayout.snap(desiredW, grid));
			desiredH = Math.max(8, HudLayout.snap(desiredH, grid));
		}

		float appliedW = desiredW / (float) Math.max(1, resolved.baseWidth());
		float appliedH = desiredH / (float) Math.max(1, resolved.baseHeight());
		float targetApplied = Math.max(0.25F, Math.max(appliedW, appliedH));
		float globalScale = Math.max(0.25F, config.globalScale);
		widgetConfig.scale = round2(clamp(targetApplied / globalScale, 0.25F, 4.0F));
		return true;
	}

	private boolean isWidgetDoubleClick(String widgetId, double mouseX, double mouseY) {
		if (widgetId == null || lastLeftClickWidgetId == null) {
			return false;
		}
		long elapsed = System.currentTimeMillis() - lastLeftClickTimeMs;
		if (elapsed > 250L || !widgetId.equals(lastLeftClickWidgetId)) {
			return false;
		}
		return Math.abs(mouseX - lastLeftClickX) <= 4.0D && Math.abs(mouseY - lastLeftClickY) <= 4.0D;
	}

	private void recordClick(String widgetId, double mouseX, double mouseY) {
		lastLeftClickWidgetId = widgetId;
		lastLeftClickTimeMs = System.currentTimeMillis();
		lastLeftClickX = mouseX;
		lastLeftClickY = mouseY;
	}

	private int[] applyEdgeSnapAndClamp(String widgetId, int x, int y, boolean snapEdges) {
		ResolvedWidget resolved = resolveWidget(widgetId);
		int width = resolved != null ? resolved.scaledWidth() : 24;
		int height = resolved != null ? resolved.scaledHeight() : 24;
		int screenW = minecraft != null ? minecraft.getWindow().getGuiScaledWidth() : 0;
		int screenH = minecraft != null ? minecraft.getWindow().getGuiScaledHeight() : 0;
		if (screenW <= 0 || screenH <= 0) {
			return new int[]{x, y};
		}
		int edgeThreshold = 8;

		if (snapEdges) {
			if (Math.abs(x) <= edgeThreshold) {
				x = 0;
			}
			if (Math.abs(y) <= edgeThreshold) {
				y = 0;
			}
			if (Math.abs((x + width) - screenW) <= edgeThreshold) {
				x = screenW - width;
			}
			if (Math.abs((y + height) - screenH) <= edgeThreshold) {
				y = screenH - height;
			}
			if (Math.abs((x + width / 2) - (screenW / 2)) <= edgeThreshold) {
				x = (screenW - width) / 2;
			}
			if (Math.abs((y + height / 2) - (screenH / 2)) <= edgeThreshold) {
				y = (screenH - height) / 2;
			}
		}

		x = Math.max(0, Math.min(Math.max(0, screenW - width), x));
		y = Math.max(0, Math.min(Math.max(0, screenH - height), y));
		return new int[]{x, y};
	}

	private void renderGridGuidelines(GuiGraphics graphics) {
		BetterHudsConfig config = hudSystem.config();
		int step = Math.max(8, config.getGridSizeOrDefault() * 2);
		for (int x = step; x < width; x += step) {
			graphics.fill(x, 0, x + 1, height, 0x1C80D8FF);
		}
		for (int y = step; y < height; y += step) {
			graphics.fill(0, y, width, y + 1, 0x1C80D8FF);
		}

		int centerX = width / 2;
		int centerY = height / 2;
		int verticalColor = 0x4480D8FF;
		int horizontalColor = 0x4480D8FF;
		ResolvedWidget selected = selectedWidgetId == null ? null : resolveWidget(selectedWidgetId);
		if (selected != null) {
			int selectedCenterX = selected.x() + selected.scaledWidth() / 2;
			int selectedCenterY = selected.y() + selected.scaledHeight() / 2;
			if (Math.abs(selectedCenterX - centerX) <= 8) {
				verticalColor = 0xCC80D8FF;
			}
			if (Math.abs(selectedCenterY - centerY) <= 8) {
				horizontalColor = 0xCC80D8FF;
			}
		}
		graphics.fill(centerX, 0, centerX + 1, height, verticalColor);
		graphics.fill(0, centerY, width, centerY + 1, horizontalColor);
	}

	private boolean isInResizeHandle(ResolvedWidget widget, double mouseX, double mouseY) {
		int[] handle = resizeHandleRect(widget);
		int x = handle[0];
		int y = handle[1];
		int size = handle[2];
		return mouseX >= x && mouseY >= y && mouseX < x + size && mouseY < y + size;
	}

	private int[] resizeHandleRect(ResolvedWidget widget) {
		int size = 6;
		int x = widget.x() + widget.scaledWidth() + 1;
		int y = widget.y() + widget.scaledHeight() + 1;
		if (x + size > width) {
			x = widget.x() + widget.scaledWidth() - size;
		}
		if (y + size > height) {
			y = widget.y() + widget.scaledHeight() - size;
		}
		x = Math.max(0, Math.min(Math.max(0, width - size), x));
		y = Math.max(0, Math.min(Math.max(0, height - size), y));
		return new int[]{x, y, size};
	}

	private int tipWidth() {
		return Math.min(300, width - 60);
	}

	private int tipY() {
		return Math.max(4, height / 2 - 44);
	}

	private ResolvedWidget resolveWidget(String widgetId) {
		for (ResolvedWidget resolved : hudSystem.getResolvedWidgets(true)) {
			if (resolved.widget().id().equals(widgetId)) {
				return resolved;
			}
		}
		return null;
	}

	private String settingsTargetWidgetId(String widgetId) {
		if ("held_item".equals(widgetId)) {
			return "armor";
		}
		return widgetId;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float round2(float value) {
		return Math.round(value * 100.0F) / 100.0F;
	}
}
