package io.github.agorohovcom.eonet.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ActorSystem {
    private static final Logger log = LoggerFactory.getLogger(ActorSystem.class);

    private final Map<String, ActorContext> actors = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final ExecutorService executor;
    private final ScheduledExecutorService cleanupExecutor;

    private final ActorSystemConfig config;

    public ActorSystem() {
        this(new ActorSystemConfig());
    }

    public ActorSystem(ActorSystemConfig config) {
        this.config = config;
        this.executor = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

        startCleanupTask();
    }

    public void createActor(String name, Actor actor) {
        actors.put(name, new ActorContext(actor, ActorType.USER));
    }

    void createSystemActor(String name, Actor actor) {
        actors.put(name, new ActorContext(actor, ActorType.SYSTEM));
    }

    public boolean isSystemActor(String actorName) {
        ActorContext context = actors.get(actorName);
        return context != null && context.actorType.equals(ActorType.SYSTEM);
    }

    public Set<String> getSystemActors() {
        return actors.entrySet().stream()
                .filter(entry -> entry.getValue().actorType.equals(ActorType.SYSTEM))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserActors() {
        return actors.entrySet().stream()
                .filter(entry -> entry.getValue().actorType.equals(ActorType.USER))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public void sendMessage(String actorName, Object message) {
        ActorContext context = actors.get(actorName);
        if (context != null) {
            context.updateAccessTime();
            context.mailbox.offer(message);
            scheduleProcessing(actorName, context);
        }
    }

    public void shutdown() {
        running.set(false);

        // Останавливаем очистку
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(config.getShutdownTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Останавливаем основной executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(config.getShutdownTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Методы для мониторинга состояния
    public Map<String, ActorStats> getActorStats() {
        long now = System.currentTimeMillis();
        return actors.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            ActorContext context = entry.getValue();
                            return new ActorStats(
                                    context.mailbox.size(),
                                    context.processing.get(),
                                    now - context.lastAccessTime,
                                    context.actorType
                            );
                        }
                ));
    }

    public int cleanupNow() {
        cleanupIdleActors();
        return actors.size();
    }

    private void scheduleProcessing(String actorName, ActorContext context) {
        // Если актор уже обрабатывает сообщения, не создаем новую задачу
        if (context.processing.compareAndSet(false, true)) {
            executor.submit(() -> processActorMessages(actorName, context));
        }
    }

    private void processActorMessages(String actorName, ActorContext context) {
        try {
            // Обрабатываем все сообщения в mailbox пока они есть
            while (!context.mailbox.isEmpty()) {
                Object message = context.mailbox.poll();
                if (message != null) {
                    try {
                        context.actor.onMessage(message);
                    } catch (Exception e) {
                        log.error("Error processing message in actor {}: {}", actorName, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            // Освобождаем флаг обработки
            context.processing.set(false);

            // Проверяем, не появились ли новые сообщения пока мы обрабатывали
            if (!context.mailbox.isEmpty()) {
                scheduleProcessing(actorName, context);
            }
        }
    }

    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            if (running.get()) {
                cleanupIdleActors();
            }
        }, config.getCleanupInterval().toMillis(), config.getCleanupInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    private void cleanupIdleActors() {
        long now = System.currentTimeMillis();
        long idleThreshold = now - config.getMaxIdleTime().toMillis();

        log.debug("Starting cleanup check. Total actors: {}, idle threshold: {}ms",
                actors.size(), config.getMaxIdleTime().toMillis());

        List<String> actorsToRemove = actors.entrySet().stream()
                .filter(entry -> entry.getValue().actorType.equals(ActorType.USER))     // не системные
                .filter(entry -> entry.getValue().lastAccessTime < idleThreshold)       // старые
                .filter(entry -> entry.getValue().mailbox.isEmpty())                    // без сообщений в очереди
                .filter(entry -> !entry.getValue().processing.get())                    // не обрабатывают сообщения
                .map(Map.Entry::getKey)
                .toList();

        if (!actorsToRemove.isEmpty()) {
            actorsToRemove.forEach(actors::remove);
            log.info("Cleaned up {} idle actors: {}", actorsToRemove.size(), actorsToRemove);
            log.debug("Remaining actors: {}", actors.size());
        } else {
            log.debug("No idle actors to clean up");
        }
    }

    private static class ActorContext {
        final Actor actor;
        final BlockingQueue<Object> mailbox = new LinkedBlockingQueue<>();
        final AtomicBoolean processing = new AtomicBoolean(false);
        final ActorType actorType;
        volatile long lastAccessTime;

        ActorContext(Actor actor, ActorType actorType) {
            this.actor = actor;
            this.actorType = actorType;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    private enum ActorType {
        SYSTEM, USER
    }

    public static record ActorStats(
            int pendingMessages,
            boolean processing,
            long idleTimeMs,
            ActorType type
    ) {
    }
}
