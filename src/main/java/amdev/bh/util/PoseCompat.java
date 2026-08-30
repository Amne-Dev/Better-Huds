package amdev.bh.util;

import org.joml.Matrix3x2fStack;

public final class PoseCompat {
	private PoseCompat() {
	}

	public static void push(Matrix3x2fStack poseStack) {
		poseStack.pushMatrix();
	}

	public static void pop(Matrix3x2fStack poseStack) {
		poseStack.popMatrix();
	}

	public static void translate(Matrix3x2fStack poseStack, float x, float y) {
		poseStack.translate(x, y);
	}

	public static void scale(Matrix3x2fStack poseStack, float x, float y) {
		poseStack.scale(x, y);
	}
}
