package amdev.bh.util;

import java.lang.reflect.Method;

public final class PoseCompat {
	private PoseCompat() {
	}

	public static void push(Object poseStack) {
		if (!invoke(poseStack, "pushMatrix") && !invoke(poseStack, "pushPose")) {
			// no-op
		}
	}

	public static void pop(Object poseStack) {
		if (!invoke(poseStack, "popMatrix") && !invoke(poseStack, "popPose")) {
			// no-op
		}
	}

	public static void translate(Object poseStack, float x, float y) {
		if (invoke(poseStack, "translate", (double) x, (double) y)) {
			return;
		}
		if (invoke(poseStack, "translate", x, y)) {
			return;
		}
		if (invoke(poseStack, "translate", (double) x, (double) y, 0.0D)) {
			return;
		}
		invoke(poseStack, "translate", x, y, 0.0F);
	}

	public static void scale(Object poseStack, float x, float y) {
		if (invoke(poseStack, "scale", x, y)) {
			return;
		}
		if (invoke(poseStack, "scale", x, y, 1.0F)) {
			return;
		}
		if (invoke(poseStack, "scale", (double) x, (double) y)) {
			return;
		}
		invoke(poseStack, "scale", (double) x, (double) y, 1.0D);
	}

	private static boolean invoke(Object target, String name, Object... args) {
		if (target == null) {
			return false;
		}
		Method[] methods = target.getClass().getMethods();
		for (Method method : methods) {
			if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
				continue;
			}
			try {
				method.invoke(target, args);
				return true;
			} catch (Exception ignored) {
				// Try another overload.
			}
		}
		return false;
	}
}
