package amdev.bh.ui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;

public class GlassButton extends AbstractWidget {
	private final OnPress onPress;
	private final int accentColor;

	public GlassButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		this(x, y, width, height, message, onPress, 0xFF80D8FF);
	}

	public GlassButton(int x, int y, int width, int height, Component message, OnPress onPress, int accentColor) {
		super(x, y, width, height, message);
		this.onPress = onPress;
		this.accentColor = accentColor;
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		int x = getX();
		int y = getY();
		int w = getWidth();
		int h = getHeight();

		boolean hovered = isHoveredOrFocused();
		int background = !active ? 0x33222222 : (hovered ? 0x77405566 : 0x55303B4A);
		int border = !active ? 0x55777777 : (hovered ? accentColor : 0xAA6FAFCF);

		graphics.fill(x, y, x + w, y + h, background);
		graphics.fill(x, y, x + w, y + 1, border);
		graphics.fill(x, y + h - 1, x + w, y + h, border);
		graphics.fill(x, y, x + 1, y + h, border);
		graphics.fill(x + w - 1, y, x + w, y + h, border);
		graphics.fill(x + 2, y + h - 2, x + w - 2, y + h - 1, 0x55FFFFFF);

		Minecraft client = Minecraft.getInstance();
		int textColor = active ? 0xFFFFFFFF : 0xFF9A9A9A;
		String label = client.font.plainSubstrByWidth(getMessage().getString(), Math.max(4, w - 8));
		int tx = x + (w - client.font.width(label)) / 2;
		int ty = y + (h - client.font.lineHeight) / 2;
		graphics.drawString(client.font, label, tx, ty, textColor, false);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		if (!active || !visible) {
			return;
		}
		playClickSound();
		onPress.onPress(this);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!active || !visible) {
			return false;
		}
		int key = keyCode;
		if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_SPACE) {
			playClickSound();
			onPress.onPress(this);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void playClickSound() {
		Object soundManager = Minecraft.getInstance().getSoundManager();
		for (Method method : AbstractWidget.class.getMethods()) {
			if (!method.getName().equals("playButtonClickSound") || method.getParameterCount() != 1) {
				continue;
			}
			try {
				method.invoke(null, soundManager);
				return;
			} catch (Exception ignored) {
				// Try next fallback.
			}
		}
		for (Method method : AbstractWidget.class.getMethods()) {
			if (!method.getName().equals("playDownSound") || method.getParameterCount() != 1) {
				continue;
			}
			try {
				method.invoke(this, soundManager);
				return;
			} catch (Exception ignored) {
				// Try next fallback.
			}
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}

	@FunctionalInterface
	public interface OnPress {
		void onPress(GlassButton button);
	}
}
