package io.github.agorohovcom.eonet.actor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ActorSystem {
    private static class ActorContext {
        final Actor actor;
        final BlockingQueue<Object> mailbox = new LinkedBlockingQueue<>();  // TODO maybe Actor?
        final AtomicBoolean processing = new AtomicBoolean(false);
        final ActorType actorType;

        ActorContext(Actor actor, ActorType actorType) {
            this.actor = actor;
            this.actorType = actorType;
        }
    }

    private final Map<String, ActorContext> actors = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final AtomicBoolean running = new AtomicBoolean(true);

    public void createActor(String name, Actor actor) {
        actors.put(name, new ActorContext(actor, ActorType.USER));
    }

    void createSystemActor(String name, Actor actor) {
        actors.put(name, new ActorContext(actor, ActorType.SYSTEM)); // системный
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
            context.mailbox.offer(message);
            scheduleProcessing(actorName, context);
        }
    }

    public void shutdown() {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
                        System.err.println("Error processing message in actor " + actorName + ": " + e.getMessage());
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

    private enum ActorType {
        SYSTEM, USER
    }
}
