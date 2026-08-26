package amdev.bh.mixin;

import amdev.bh.util.McCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {
	@Inject(method = "getMenuBackgroundBlurriness", at = @At("HEAD"), cancellable = true)
	private void betterHuds$disableMenuBlurForHudScreens(CallbackInfoReturnable<Integer> cir) {
		Minecraft client = Minecraft.getInstance();
		if (client == null || McCompat.currentScreen(client) == null) {
			return;
		}
		if (McCompat.currentScreen(client).getClass().getName().startsWith("amdev.bh.ui.")) {
			cir.setReturnValue(0);
		}
	}
}
