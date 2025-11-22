package io.github.agorohovcom.eonet;

import io.github.agorohovcom.eonet.actor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("EONET Console Tracker starting...");

        ActorSystem system = new ActorSystem();

        // Создаем цепочку акторов
        system.createActor("dashboard", new DashboardActor());
        system.createActor("processor", new EventProcessorActor(system, "dashboard"));
        system.createActor("poller", new EONETPollerActor(system, "processor"));
        system.createActor("scheduler", new PollingSchedulerActor(system, "poller"));

        // Запускаем периодический опрос
        log.info("Starting periodic polling (every 10 seconds)...");
        system.sendMessage("scheduler", new StartPolling());

        // Ждем 1 минуту, затем останавливаем
        Thread.sleep(60000); // 1 минута

        // Останавливаем систему
        system.sendMessage("scheduler", new StopPolling());
        Thread.sleep(2000); // Даем время на завершение
        system.shutdown();

        log.info("EONET Console Tracker finished");
    }
}