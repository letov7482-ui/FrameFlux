package com.javaonphone.frameflux.mixin;

import com.javaonphone.frameflux.FrameFluxClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "onResolutionChanged", at = @At("TAIL"))
    private void onResize(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client != null) {
            client.onResize(mc.getWindow().getFramebufferWidth(),
                    mc.getWindow().getFramebufferHeight());
        }
    }
}
