package com.javaonphone.frameflux.modrinth;

public record ModEntry(String projectId, String title, String description,
                       String iconUrl, long downloads) {
}
