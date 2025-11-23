package io.github.agorohovcom.eonet;

import io.github.agorohovcom.eonet.actor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("EONET Console Tracker starting...");

        // Вариант 1: Простой запуск с дефолтным конфигом
//        ActorOrchestrator orchestrator = ActorOrchestrator.create();
//        orchestrator.start();

        // Вариант 2: С кастомным конфигом
        ActorSystemConfig config = new ActorSystemConfig(
                4,             // thread pool size
                Duration.ofSeconds(3),              // shutdown timeout
                Duration.ofSeconds(10),             // polling interval
                Duration.ofMinutes(5),              // application run time
                Duration.ofSeconds(30),             // cleanup interval
                Duration.ofMinutes(1)               // max idle time
        );
        ActorOrchestrator orchestrator = ActorOrchestrator.create(config);
        orchestrator.start();

        log.info("EONET Console Tracker finished");
    }
}