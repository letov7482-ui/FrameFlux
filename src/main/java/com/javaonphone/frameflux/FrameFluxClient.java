package com.javaonphone.frameflux;

import com.javaonphone.frameflux.config.FrameFluxConfig;
import com.javaonphone.frameflux.hud.HudIndicator;
import com.javaonphone.frameflux.key.KeyBindings;
import com.javaonphone.frameflux.render.FrameBufferManager;
import com.javaonphone.frameflux.render.FrameInterpolator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
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
    private boolean buffersInitialized = false;
    private int frameCounter = 0;
    private int multiplier = 2;

    @Override
    public void onInitializeClient() {
        try {
            INSTANCE = this;
            config = FrameFluxConfig.load();
            bufferManager = new FrameBufferManager();
            interpolator = new FrameInterpolator();

            frameGenEnabled = config.frameGenEnabled;
            multiplier = config.multiplier;

            KeyBindings.register();
            HudRenderCallback.EVENT.register(new HudIndicator());

            // Инициализация буферов ОТЛОЖЕНА до первого тика — рендер ещё не готов
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                if (!buffersInitialized && client.getWindow() != null) {
                    try {
                        bufferManager.initialize(
                                client.getWindow().getFramebufferWidth(),
                                client.getWindow().getFramebufferHeight());
                        buffersInitialized = true;
                        LOGGER.info("[FrameFlux] Buffers initialized. Depth: {}",
                                bufferManager.isDepthSupported());
                    } catch (Exception e) {
                        LOGGER.error("[FrameFlux] Buffer init failed", e);
                        buffersInitialized = true; // чтобы не спамить
                    }
                }
            });

            LOGGER.info("[FrameFlux] Initialized. FrameGen: {}, Depth: pending",
                    frameGenEnabled);
        } catch (Throwable t) {
            LOGGER.error("[FrameFlux] CRITICAL: mod failed to initialize", t);
        }
    }

    public static FrameFluxClient getInstance() { return INSTANCE; }

    public boolean shouldSkipRender() {
        if (!frameGenEnabled || !buffersInitialized) return false;
        if (bufferManager == null || !bufferManager.isDepthSupported()) return false;
        frameCounter++;
        return frameCounter % multiplier != 0;
    }

    public void renderInterpolatedFrame(float tickDelta) {
        if (interpolator == null || bufferManager == null) return;
        interpolator.reproject(bufferManager.getColorTexture(),
                bufferManager.getDepthTexture(), tickDelta);
    }

    public void captureFrame() {
        if (bufferManager == null) return;
        bufferManager.captureColor();
        bufferManager.captureDepth();
    }

    public void onResize(int w, int h) {
        if (bufferManager == null) return;
        bufferManager.onResize(w, h);
    }

    public FrameFluxConfig getConfig() { return config; }
    public boolean isFrameGenEnabled() { return frameGenEnabled; }

    public void setFrameGenEnabled(boolean v) {
        frameGenEnabled = v;
        if (config != null) {
            config.frameGenEnabled = v;
            config.save();
        }
    }

    public int getMultiplier() { return multiplier; }

    public void setMultiplier(int m) {
        multiplier = m;
        if (config != null) {
            config.multiplier = m;
            config.save();
        }
    }
}
