package com.javaonphone.frameflux.mixin;

import com.javaonphone.frameflux.FrameFluxClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderStart(RenderTickCounter tickCounter, boolean tick,
                                CallbackInfo ci) {
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client == null) return;
        if (!client.shouldSkipRender()) return;

        client.renderInterpolatedFrame(0.0f);
        ci.cancel();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(RenderTickCounter tickCounter, boolean tick,
                              CallbackInfo ci) {
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client == null) return;
        client.captureFrame();
    }
}
