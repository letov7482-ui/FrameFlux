package com.javaonphone.frameflux.modrinth;

import com.google.gson.*;
import com.javaonphone.frameflux.FrameFluxClient;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ModrinthClient {
    private static final String USER_AGENT = "JavaOnPhone/FrameFlux/1.0.0";
    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static List<ModEntry> searchOptimizationMods(String gameVersion) {
        // ПРОСТОЙ ЗАПРОС: без facets, без фильтров
        // Modrinth всегда отвечает на query=optimization
        String url = "https://api.modrinth.com/v2/search"
                + "?query=optimization"
                + "&limit=50"
                + "&index=downloads";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .get()
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                FrameFluxClient.LOGGER.error("[FrameFlux] Modrinth HTTP {}", response.code());
                return Collections.emptyList();
            }

            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray hits = json.getAsJsonArray("hits");

            List<ModEntry> all = new ArrayList<>();
            for (JsonElement hit : hits) {
                JsonObject obj = hit.getAsJsonObject();
                String title = obj.get("title").getAsString();
                String desc = obj.get("description").getAsString().toLowerCase();
                String t = title.toLowerCase();

                // Клиентская фильтрация: только FPS-моды
                if (t.contains("sodium") || t.contains("lithium") || t.contains("fps")
                        || t.contains("optimiz") || t.contains("performance")
                        || t.contains("culling") || t.contains("memory")) {

                    String icon = "";
                    if (obj.has("icon_url") && !obj.get("icon_url").isJsonNull()) {
                        icon = obj.get("icon_url").getAsString();
                    }

                    all.add(new ModEntry(
                            obj.get("project_id").getAsString(),
                            title,
                            obj.get("description").getAsString(),
                            icon,
                            obj.get("downloads").getAsLong()
                    ));
                }
            }

            FrameFluxClient.LOGGER.info("[FrameFlux] Modrinth returned {} FPS mods", all.size());
            return all;

        } catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Modrinth network error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
