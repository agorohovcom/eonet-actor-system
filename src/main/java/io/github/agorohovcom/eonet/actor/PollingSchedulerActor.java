package io.github.agorohovcom.eonet.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PollingSchedulerActor extends MethodHandleActor {
    private static final Logger log = LoggerFactory.getLogger(PollingSchedulerActor.class);

    private final ActorSystem system;
    private final String pollerActor;

    private final ScheduledExecutorService scheduler;

    private final AtomicInteger pollCount = new AtomicInteger(0);
    private volatile boolean running = false;

    public PollingSchedulerActor(ActorSystem system, String pollerActor) {
        this.system = system;
        this.pollerActor = pollerActor;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "polling-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @Handle
    public void handleStartPolling(StartPolling message) {
        if (!running) {
            running = true;
            pollCount.set(0);
            startPeriodicPolling();
            log.info("Started periodic polling every 10 seconds");
        }
    }

    @Handle
    public void handleStopPolling(StopPolling message) {
        if (running) {
            running = false;
            scheduler.shutdown();
            log.info("Stopped periodic polling after {} polls", pollCount.get());
        }
    }

    private void startPeriodicPolling() {
        // Запускаем периодическую задачу
        scheduler.scheduleAtFixedRate(() -> {
            if (running) {
                int count = pollCount.incrementAndGet();
                log.debug("Scheduling poll #{}", count);
                system.sendMessage(pollerActor, new StartPolling());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    protected void unhandled(Object message) {
        log.warn("PollingSchedulerActor received unhandled message: {}", message.getClass().getSimpleName());
    }
}
