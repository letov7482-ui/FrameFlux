package com.javaonphone.frameflux.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public class FrameBufferManager {
    private int colorTexture;
    private int depthTexture;
    private int width, height;

    public void initialize(int w, int h) {
        this.width = w;
        this.height = h;

        colorTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        depthTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F,
                w, h, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    public void captureColor() {
        if (width == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
    }

    public void captureDepth() {
        if (width == 0) return;
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
}
