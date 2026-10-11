package com.javaonphone.frameflux.render;

import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;

public class FrameInterpolator {
    private final Matrix4f previousView = new Matrix4f();

    public void reproject(int colorTex, int depthTex, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        Matrix4f currentView = new Matrix4f(client.gameRenderer.getCamera().getRotation());
        Matrix4f delta = new Matrix4f(currentView).mul(previousView).invert();

        // Упрощённая версия. Полный шейдер репроекции — следующий шаг.
        previousView.set(currentView);
    }
}
