package io.github.agorohovcom.eonet.actor;

import java.util.Map;

public record StatisticsUpdate(
        Map<String, Long> eventsByCategory,
        int totalEvents
) {
}
