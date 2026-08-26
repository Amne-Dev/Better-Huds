package amdev.bh.hud;

import amdev.bh.config.BetterHudsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface HudWidget {
	String id();

	Component displayName();

	default boolean shouldRender(Minecraft client) {
		return client.player != null;
	}

	default boolean shouldRender(Minecraft client, BetterHudsConfig config) {
		return shouldRender(client);
	}

	default boolean shouldRender(Minecraft client, BetterHudsConfig config, HudRenderContext context) {
		return shouldRender(client, config);
	}

	int getWidth(Minecraft client);

	int getHeight(Minecraft client);

	default int getWidth(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return getWidth(client);
	}

	default int getHeight(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return getHeight(client);
	}

	default float centerReferenceX(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return getWidth(client, config, widgetConfig) / 2.0F;
	}

	default float centerReferenceY(Minecraft client, BetterHudsConfig config, BetterHudsConfig.WidgetConfig widgetConfig) {
		return getHeight(client, config, widgetConfig) / 2.0F;
	}

	void render(GuiGraphics graphics, Minecraft client, HudRenderContext context, BetterHudsConfig.WidgetConfig widgetConfig, int x, int y);
}
