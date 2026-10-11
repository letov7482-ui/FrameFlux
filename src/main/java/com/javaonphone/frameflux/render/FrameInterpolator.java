package com.javaonphone.frameflux.render;

import com.javaonphone.frameflux.FrameFluxClient;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class FrameInterpolator {
    private final Matrix4f previousView = new Matrix4f();
    private boolean firstFrame = true;

    public void reproject(int colorTex, int depthTex, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Получаем матрицу камеры
        Quaternionf rotation = new Quaternionf(client.gameRenderer.getCamera().getRotation());
        Matrix4f currentView = new Matrix4f().rotation(rotation);

        if (firstFrame) {
            previousView.set(currentView);
            firstFrame = false;
            return;
        }

        // Вычисляем дельту между кадрами
        Matrix4f delta = new Matrix4f(currentView).mul(previousView).invert();

        // Если depth не поддерживается — просто копируем цвет
        if (depthTex == 0) {
            // Просто показываем предыдущий кадр
            previousView.set(currentView);
            return;
        }

        // TODO: настоящая репроекция через шейдер
        // Пока просто сохраняем матрицу
        previousView.set(currentView);
    }

    public void reset() {
        firstFrame = true;
    }
}
