package io.github.agorohovcom.eonet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EONETResponse(
        String title,
        String description,
        String link,
        List<EONETEvent> events
) {}
