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
    private static final String USER_AGENT = "JavaOnPhone/FrameFlux/1.0.0 (contact: letov7482-ui)";
    private static final OkHttpClient http = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // Публичный метод — пробует несколько эндпоинтов по очереди
    public static List<ModEntry> searchOptimizationMods(String gameVersion) {
        // Сначала пробуем v2 search (самый надёжный)
        List<ModEntry> result = tryV2Search(gameVersion);
        if (!result.isEmpty()) return result;

        // Если v2 вернул пусто — пробуем v3
        result = tryV3Search(gameVersion);
        if (!result.isEmpty()) return result;

        // Если и v3 пусто — пробуем простой поиск по названию
        return trySimpleSearch();
    }

    // === Эндпоинт 1: v2 search с facets ===
    private static List<ModEntry> tryV2Search(String gameVersion) {
        try {
            String facets = "[[\"project_type:mod\"],[\"categories:optimization\"],"
                    + "[\"versions:" + gameVersion + "\"],[\"categories:fabric\"]]";
            String url = "https://api.modrinth.com/v2/search?facets="
                    + URLEncoder.encode(facets, StandardCharsets.UTF_8)
                    + "&limit=50&index=downloads";

            String body = fetch(url);
            if (body == null) return Collections.emptyList();

            return parseHits(body);
        } catch (Exception e) {
            FrameFluxClient.LOGGER.warn("[FrameFlux] v2 search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // === Эндпоинт 2: v3 search с new_filters ===
    private static List<ModEntry> tryV3Search(String gameVersion) {
        try {
            String filters = "project_types=[\"mod\"] AND categories=[\"optimization\"] AND loaders=[\"fabric\"]";
            String url = "https://api.modrinth.com/v3/search?new_filters="
                    + URLEncoder.encode(filters, StandardCharsets.UTF_8)
                    + "&limit=50&index=downloads";

            String body = fetch(url);
            if (body == null) return Collections.emptyList();

            return parseHits(body);
        } catch (Exception e) {
            FrameFluxClient.LOGGER.warn("[FrameFlux] v3 search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // === Эндпоинт 3: простой поиск (без facets) ===
    private static List<ModEntry> trySimpleSearch() {
        try {
            String url = "https://api.modrinth.com/v2/search?query=optimization&limit=20&index=downloads";
            String body = fetch(url);
            if (body == null) return Collections.emptyList();

            List<ModEntry> all = parseHits(body);
            // Фильтруем только FPS-моды вручную
            List<ModEntry> filtered = new ArrayList<>();
            for (ModEntry m : all) {
                String t = m.title().toLowerCase();
                if (t.contains("sodium") || t.contains("lithium") || t.contains("fps")
                        || t.contains("optimiz") || t.contains("performance")) {
                    filtered.add(m);
                }
            }
            return filtered;
        } catch (Exception e) {
            FrameFluxClient.LOGGER.warn("[FrameFlux] simple search failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // === Общий HTTP-запрос ===
    private static String fetch(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .get()
                    .build();

            try (Response response = http.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    FrameFluxClient.LOGGER.error("[FrameFlux] HTTP {} for {}", response.code(), url);
                    return null;
                }
                return response.body().string();
            }
        } catch (IOException e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] Network error: {}", e.getMessage());
            return null;
        }
    }

    // === Парсер JSON-ответа ===
    private static List<ModEntry> parseHits(String jsonBody) {
        try {
            JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();
            JsonArray hits = json.getAsJsonArray("hits");
            if (hits == null) return Collections.emptyList();

            List<ModEntry> mods = new ArrayList<>();
            for (JsonElement hit : hits) {
                JsonObject obj = hit.getAsJsonObject();
                String icon = "";
                if (obj.has("icon_url") && !obj.get("icon_url").isJsonNull()) {
                    icon = obj.get("icon_url").getAsString();
                }
                mods.add(new ModEntry(
                        obj.get("project_id").getAsString(),
                        obj.get("title").getAsString(),
                        obj.get("description").getAsString(),
                        icon,
                        obj.get("downloads").getAsLong()
                ));
            }
            return mods;
        } catch (Exception e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] JSON parse failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // === Загрузка мода по project_id (для кнопки "Скачать") ===
    public static String getDownloadUrl(String projectId) {
        try {
            String url = "https://api.modrinth.com/v2/project/" + projectId + "/version";
            String body = fetch(url);
            if (body == null) return null;

            JsonArray versions = JsonParser.parseString(body).getAsJsonArray();
            if (versions.isEmpty()) return null;

            // Берём первую (самую новую) версию
            JsonObject version = versions.get(0).getAsJsonObject();
            JsonArray files = version.getAsJsonArray("files");
            for (JsonElement f : files) {
                JsonObject file = f.getAsJsonObject();
                if (file.has("primary") && file.get("primary").getAsBoolean()) {
                    return file.get("url").getAsString();
                }
            }
            // Если primary нет — берём первый файл
            return files.get(0).getAsJsonObject().get("url").getAsString();
        } catch (Exception e) {
            FrameFluxClient.LOGGER.error("[FrameFlux] getDownloadUrl failed: {}", e.getMessage());
            return null;
        }
    }
                    }
