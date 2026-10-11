package com.javaonphone.frameflux.mixin;

import com.javaonphone.frameflux.FrameFluxClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderStart(float tickDelta, long startTime,
                                boolean tick, CallbackInfo ci) {
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client == null) return;
        if (!client.shouldSkipRender()) return;

        client.renderInterpolatedFrame(tickDelta);
        ci.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(float tickDelta, long startTime,
                              boolean tick, CallbackInfo ci) {
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client == null) return;
        client.captureFrame();
    }
}
