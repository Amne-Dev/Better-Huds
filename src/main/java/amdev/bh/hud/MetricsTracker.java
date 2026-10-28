package amdev.bh.hud;

import net.minecraft.client.Minecraft;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class MetricsTracker {
	private static final long CPS_WINDOW_MS = 1_000L;
	private static final int FPS_SAMPLES = 120;

	private final long sessionStartMs = System.currentTimeMillis();
	private final Deque<Long> leftClicks = new ArrayDeque<>();
	private final Deque<Long> rightClicks = new ArrayDeque<>();
	private final Deque<Integer> fpsSamples = new ArrayDeque<>();

	private long lastFpsSampleMs;
	private boolean previousLeftDown;
	private boolean previousRightDown;
	private int cachedAverageFps;
	private int cachedOnePercentLow;

	public void tick(Minecraft client) {
		long now = System.currentTimeMillis();

		if (now - lastFpsSampleMs >= 1_000L) {
			fpsSamples.addLast(Math.max(0, client.getFps()));
			while (fpsSamples.size() > FPS_SAMPLES) {
				fpsSamples.removeFirst();
			}
			updateFpsMetrics();
			lastFpsSampleMs = now;
		}

		boolean leftDown = client.options.keyAttack.isDown();
		boolean rightDown = client.options.keyUse.isDown();

		if (leftDown && !previousLeftDown) {
			leftClicks.addLast(now);
		}
		if (rightDown && !previousRightDown) {
			rightClicks.addLast(now);
		}

		previousLeftDown = leftDown;
		previousRightDown = rightDown;

		pruneClicks(leftClicks, now);
		pruneClicks(rightClicks, now);
	}

	private void updateFpsMetrics() {
		if (fpsSamples.isEmpty()) {
			cachedAverageFps = 0;
			cachedOnePercentLow = 0;
			return;
		}
		int total = 0;
		for (int sample : fpsSamples) {
			total += sample;
		}
		cachedAverageFps = Math.round((float) total / fpsSamples.size());

		List<Integer> sorted = new ArrayList<>(fpsSamples);
		Collections.sort(sorted);
		int index = Math.max(0, (int) Math.floor((sorted.size() - 1) * 0.01D));
		cachedOnePercentLow = sorted.get(index);
	}

	private static void pruneClicks(Deque<Long> clicks, long now) {
		while (!clicks.isEmpty() && now - clicks.peekFirst() > CPS_WINDOW_MS) {
			clicks.removeFirst();
		}
	}

	public int leftCps() {
		return leftClicks.size();
	}

	public int rightCps() {
		return rightClicks.size();
	}

	public long sessionSeconds() {
		return Math.max(0L, (System.currentTimeMillis() - sessionStartMs) / 1_000L);
	}

	public int averageFps() {
		return cachedAverageFps;
	}

	public int onePercentLowFps() {
		return cachedOnePercentLow;
	}
}
