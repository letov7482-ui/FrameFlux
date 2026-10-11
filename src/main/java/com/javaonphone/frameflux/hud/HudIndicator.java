package com.javaonphone.frameflux.hud;

import com.javaonphone.frameflux.FrameFluxClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudIndicator implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext ctx, float tickDelta) {
        FrameFluxClient client = FrameFluxClient.getInstance();
        if (client == null || !client.isFrameGenEnabled()) return;
        if (!client.getConfig().hudEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        String text = "FrameFlux: " + client.getMultiplier() + "x";
        int x = 5;
        int y = mc.getWindow().getScaledHeight() - 15;
        ctx.drawTextWithShadow(mc.textRenderer, text, x, y, 0x55FF55);
    }
}
