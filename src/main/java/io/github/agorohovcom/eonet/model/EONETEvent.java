package io.github.agorohovcom.eonet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EONETEvent(
        String id,
        String title,
        String description,
        String link,
        List<EventCategory> categories,
        List<Source> sources,
        Instant closed
) {
}

record Source(String id, String url) {}