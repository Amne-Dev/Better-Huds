package amdev.bh.mixin;

import amdev.bh.client.BetterHudsClient;
import amdev.bh.hud.HudSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
	private void betterHuds$cancelVanillaCrosshair(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
		HudSystem hudSystem = BetterHudsClient.hudSystem();
		if (hudSystem == null) {
			return;
		}
		if (hudSystem.shouldReplaceVanillaCrosshair(Minecraft.getInstance())) {
			ci.cancel();
		}
	}
}
