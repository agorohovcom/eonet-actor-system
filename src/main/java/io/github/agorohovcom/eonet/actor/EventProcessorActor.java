package io.github.agorohovcom.eonet.actor;

import io.github.agorohovcom.eonet.model.EONETEvent;
import io.github.agorohovcom.eonet.model.EventCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventProcessorActor extends MethodHandleActor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessorActor.class);

    private final ActorSystem system;
    private final String nextActor;

    public EventProcessorActor(ActorSystem system, String nextActor) {
        this.system = system;
        this.nextActor = nextActor;
    }

    @Handle
    public void handleEventsFetched(EventsFetched message) {
        List<EONETEvent> events = message.response().events();
        log.info("Processing {} events", events.size());

        Map<String, Long> eventsByCategory = events.stream()
                .flatMap(event -> event.categories().stream())
                .collect(Collectors.groupingBy(
                        EventCategory::title,
                        Collectors.counting()
                ));

        addMissingCategories(eventsByCategory);
        system.sendMessage(nextActor, new StatisticsUpdate(eventsByCategory, events.size()));
    }

    @Handle
    public void handlePollingError(PollingError message) {
        log.error("Processing polling error: {}", message.error());
        system.sendMessage(nextActor, new ProcessingError("Failed to process events: " + message.error()));
    }

    private void addMissingCategories(Map<String, Long> eventsByCategory) {
        String[] allCategories = {
                "Wildfires", "Severe Storms", "Volcanoes",
                "Dust and Haze", "Manmade", "Sea and Lake Ice",
                "Snow", "Water Color"
        };

        for (String category : allCategories) {
            eventsByCategory.putIfAbsent(category, 0L);
        }
    }
}
