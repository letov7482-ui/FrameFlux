package com.javaonphone.frameflux.modrinth;

import com.google.gson.*;
import com.javaonphone.frameflux.FrameFluxClient;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ModrinthClient {
    private static final String API = "https://api.modrinth.com/v2";
    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public static List<ModEntry> searchOptimizationMods(String gameVersion) {
        String facets = "[[\"project_type:mod\"],[\"categories:optimization\"],"
                + "[\"versions:" + gameVersion + "\"],[\"categories:fabric\"]]";
        String url = API + "/search?facets="
                + URLEncoder.encode(facets, StandardCharsets.UTF_8)
                + "&limit=50&index=downloads";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "JavaOnPhone/FrameFlux/1.0.0")
                .get()
                .build();

        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                FrameFluxClient.LOGGER.error("[FrameFlux] Modrinth API error: {}", response.code());
                return Collections.emptyList();
            }
            String body = response.body().string();
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray hits = json.getAsJsonArray("hits");

            List<ModEntry> mods = new ArrayList<>();
            for (JsonElement hit : hits) {
                JsonObject obj = hit.getAsJsonObject();
                mods.add(new ModEntry(
                        obj.get("project_id").getAsString(),
                        obj.get("title").getAsString(),
                        obj.get("description").getAsString(),
                        obj.has("icon_url") && !obj.get("icon_url").isJsonNull()
                                ? obj.get("icon_url").getAsString() : "",
                        obj.get("downloads").getAsLong()
                ));
            }
            return mods;
        } catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Modrinth search failed", e);
            return Collections.emptyList();
        }
    }
}
