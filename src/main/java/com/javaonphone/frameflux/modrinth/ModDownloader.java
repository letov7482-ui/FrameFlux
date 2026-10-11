package com.javaonphone.frameflux.modrinth;

import com.javaonphone.frameflux.FrameFluxClient;
import net.fabricmc.loader.api.FabricLoader;
import okhttp3.*;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class ModDownloader {
    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public static void download(String url, String fileName) {
        Path modsDir = FabricLoader.getInstance().getGameDir().resolve("mods");
        try { Files.createDirectories(modsDir); }
        catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Cannot create mods dir", e);
            return;
        }

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "JavaOnPhone/FrameFlux/1.0.0")
                .get().build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            Path target = modsDir.resolve(fileName);
            try (InputStream in = response.body().byteStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            FrameFluxClient.LOGGER.info("[FrameFlux] Downloaded {}", fileName);
        } catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Download failed", e);
        }
    }
}
