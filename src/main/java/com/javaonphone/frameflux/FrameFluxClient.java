package com.javaonphone.frameflux;

import com.javaonphone.frameflux.config.FrameFluxConfig;
import com.javaonphone.frameflux.hud.HudIndicator;
import com.javaonphone.frameflux.key.KeyBindings;
import com.javaonphone.frameflux.render.FrameBufferManager;
import com.javaonphone.frameflux.render.FrameInterpolator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameFluxClient implements ClientModInitializer {
    public static final String MOD_ID = "frameflux";
    public static final Logger LOGGER = LoggerFactory.getLogger("FrameFlux");

    private static FrameFluxClient INSTANCE;
    private FrameBufferManager bufferManager;
    private FrameInterpolator interpolator;
    private FrameFluxConfig config;
    private boolean frameGenEnabled = false;
    private int frameCounter = 0;
    private int multiplier = 2;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        config = FrameFluxConfig.load();
        bufferManager = new FrameBufferManager();
        interpolator = new FrameInterpolator();

        frameGenEnabled = config.frameGenEnabled;
        multiplier = config.multiplier;

        KeyBindings.register();
        HudRenderCallback.EVENT.register(new HudIndicator());

        // Инициализируем буферы под текущее разрешение окна
        var window = net.minecraft.client.MinecraftClient.getInstance().getWindow();
        bufferManager.initialize(window.getFramebufferWidth(), window.getFramebufferHeight());

        LOGGER.info("[FrameFlux] Initialized. FrameGen: {}, Depth: {}",
                frameGenEnabled, bufferManager.isDepthSupported());
    }

    public static FrameFluxClient getInstance() { return INSTANCE; }

    public boolean shouldSkipRender() {
        if (!frameGenEnabled) return false;
        // На Android без depth — не пропускаем кадры, иначе будет мигание
        if (!bufferManager.isDepthSupported()) return false;
        frameCounter++;
        return frameCounter % multiplier != 0;
    }

    public void renderInterpolatedFrame(float tickDelta) {
        interpolator.reproject(bufferManager.getColorTexture(),
                bufferManager.getDepthTexture(), tickDelta);
    }

    public void captureFrame() {
        bufferManager.captureColor();
        bufferManager.captureDepth();
    }

    public void onResize(int w, int h) {
        bufferManager.onResize(w, h);
    }

    public FrameFluxConfig getConfig() { return config; }
    public boolean isFrameGenEnabled() { return frameGenEnabled; }

    public void setFrameGenEnabled(boolean v) {
        frameGenEnabled = v;
        config.frameGenEnabled = v;
        config.save();
    }

    public int getMultiplier() { return multiplier; }

    public void setMultiplier(int m) {
        multiplier = m;
        config.multiplier = m;
        config.save();
    }
}
