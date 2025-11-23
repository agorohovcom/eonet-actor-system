package io.github.agorohovcom.eonet.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ActorOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(ActorOrchestrator.class);

    private final ActorSystem system;
    private final ActorSystemConfig config;
    private volatile boolean started = false;

    private ActorOrchestrator(ActorSystemConfig config) {
        this.config = config;
        this.system = new ActorSystem(config);
    }

    public static ActorOrchestrator create() {
        return new ActorOrchestrator(new ActorSystemConfig());
    }

    public static ActorOrchestrator create(ActorSystemConfig config) {
        return new ActorOrchestrator(config);
    }

    public void start() {
        if (started) {
            log.warn("ActorOrchestrator already started");
            return;
        }

        log.info("Starting ActorOrchestrator...");

        try {
            // Создаем системные акторы
            createSystemActors();

            // Запускаем периодический опрос
            system.sendMessage("scheduler", new StartPolling());

            started = true;
            log.info("ActorOrchestrator started successfully. System actors: {}", system.getSystemActors());

            // Ждем указанное время работы
            Thread.sleep(config.getApplicationRunTime().toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.info("ActorOrchestrator interrupted");
        } finally {
            // Гарантированно останавливаем
            stopInternal();
        }
    }

    public void stop() {
        if (!started) {
            log.warn("ActorOrchestrator not started");
            return;
        }

        stopInternal();
    }

    public Duration getApplicationRunTime() {
        return config.getApplicationRunTime();
    }

    public void createUserActor(String name, Actor actor) {
        system.createActor(name, actor);
        log.debug("Created user actor: {}", name);
    }

    public void sendMessage(String actorName, Object message) {
        system.sendMessage(actorName, message);
    }

    public boolean isStarted() {
        return started;
    }

    public ActorSystem getActorSystem() {
        return system;
    }

    private void createSystemActors() {
        system.createSystemActor("dashboard", new DashboardActor(config.getPollingInterval(), config.getApplicationRunTime()));
        system.createSystemActor("processor", new EventProcessorActor(system, "dashboard"));
        system.createSystemActor("poller", new EONETPollerActor(system, "processor"));
        system.createSystemActor("scheduler", new PollingSchedulerActor(system, "poller"));

        log.debug("Created {} system actors", system.getSystemActors().size());
    }

    private void stopInternal() {
        log.info("Stopping ActorOrchestrator...");

        // Останавливаем планировщик
        system.sendMessage("scheduler", new StopPolling());

        // Даем время на завершение текущих операций
        try {
            Thread.sleep(config.getShutdownTimeout().toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Останавливаем систему акторов
        system.shutdown();

        started = false;
        log.info("ActorOrchestrator stopped");
    }
}
