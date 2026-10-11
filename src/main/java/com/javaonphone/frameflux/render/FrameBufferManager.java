package com.javaonphone.frameflux.render;

import com.javaonphone.frameflux.FrameFluxClient;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class FrameBufferManager {
    private int colorTexture;
    private int depthTexture;
    private int width, height;
    private boolean depthSupported = true;

    public void initialize(int w, int h) {
        this.width = w;
        this.height = h;

        colorTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Пробуем создать depth-текстуру с 32F, при ошибке — с 16
        try {
            depthTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F,
                    w, h, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            // Проверка ошибок
            int err = GL11.glGetError();
            if (err != GL11.GL_NO_ERROR) {
                FrameFluxClient.LOGGER.warn("[FrameFlux] 32F depth не поддерживается, пробуем 16");
                GL11.glDeleteTextures(depthTexture);
                depthTexture = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT16,
                        w, h, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, (ByteBuffer) null);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }
        } catch (Exception e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Depth-текстура не создалась", e);
            depthSupported = false;
            depthTexture = 0;
        }
    }

    public void captureColor() {
        if (width == 0 || colorTexture == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    public void captureDepth() {
        if (!depthSupported || width == 0 || depthTexture == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    public void onResize(int newW, int newH) {
        dispose();
        initialize(newW, newH);
    }

    private void dispose() {
        if (colorTexture != 0) GL11.glDeleteTextures(colorTexture);
        if (depthTexture != 0) GL11.glDeleteTextures(depthTexture);
        colorTexture = 0;
        depthTexture = 0;
    }

    public int getColorTexture() { return colorTexture; }
    public int getDepthTexture() { return depthTexture; }
    public boolean isDepthSupported() { return depthSupported; }
}
