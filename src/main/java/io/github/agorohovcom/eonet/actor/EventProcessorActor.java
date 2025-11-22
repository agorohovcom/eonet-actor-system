package io.github.agorohovcom.eonet.actor;

import io.github.agorohovcom.eonet.model.EONETEvent;
import io.github.agorohovcom.eonet.model.EventCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventProcessorActor implements Actor {
    private static final Logger log = LoggerFactory.getLogger(EventProcessorActor.class);

    private final ActorSystem system;
    private final String nextActor;

    public EventProcessorActor(ActorSystem system, String nextActor) {
        this.system = system;
        this.nextActor = nextActor;
    }

    @Override
    public void onMessage(Object message) {
        if (message instanceof EventsFetched fetched) {
            List<EONETEvent> events = fetched.response().events();
            log.info("Processing {} events", events.size());

            // Группируем события по категориям
            Map<String, Long> eventsByCategory = events.stream()
                    .flatMap(event -> event.categories().stream())
                    .collect(Collectors.groupingBy(
                            EventCategory::title,
                            Collectors.counting()
                    ));

            // Добавляем категории с нулевым количеством для полноты
            addMissingCategories(eventsByCategory);

            // Отправляем статистику следующему актору
            system.sendMessage(nextActor, new StatisticsUpdate(eventsByCategory, events.size()));
        }
    }

    private void addMissingCategories(Map<String, Long> eventsByCategory) {
        // Все возможные категории EONET
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
