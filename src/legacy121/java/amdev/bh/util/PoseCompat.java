package amdev.bh.util;

import com.mojang.blaze3d.vertex.PoseStack;

public final class PoseCompat {
	private PoseCompat() {
	}

	public static void push(PoseStack poseStack) {
		poseStack.pushPose();
	}

	public static void pop(PoseStack poseStack) {
		poseStack.popPose();
	}

	public static void translate(PoseStack poseStack, float x, float y) {
		poseStack.translate(x, y, 0.0F);
	}

	public static void scale(PoseStack poseStack, float x, float y) {
		poseStack.scale(x, y, 1.0F);
	}
}
