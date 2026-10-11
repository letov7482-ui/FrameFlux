package com.javaonphone.frameflux.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javaonphone.frameflux.FrameFluxClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class FrameFluxConfig {
    public boolean frameGenEnabled = true;
    public int multiplier = 2;
    public boolean hudEnabled = true;
    public boolean autoApplySodium = false;
    public int minRenderDistance = 4;

    public static FrameFluxConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("frameflux.json");
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                FrameFluxConfig c = new Gson().fromJson(r, FrameFluxConfig.class);
                if (c != null) return c;
            } catch (IOException e) {
                FrameFluxClient.LOGGER.error("[FrameFlux] Config load failed", e);
            }
        }
        FrameFluxConfig c = new FrameFluxConfig();
        c.save();
        return c;
    }

    public void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("frameflux.json");
        try (Writer w = Files.newBufferedWriter(path)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(this, w);
        } catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Config save failed", e);
        }
    }
                  }
