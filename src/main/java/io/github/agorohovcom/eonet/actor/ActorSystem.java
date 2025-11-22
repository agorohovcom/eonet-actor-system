package io.github.agorohovcom.eonet.actor;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActorSystem {
    private final Map<String, Actor> actors = new ConcurrentHashMap<>();
    private final BlockingQueue<ActorMessage> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ActorSystem() {
        executor.submit(this::processMessages);
    }

    public void createActor(String name, Actor actor) {
        actors.put(name, actor);
    }

    public void sendMessage(String actorName, Object message) {
        messageQueue.offer(new ActorMessage(actorName, message));
    }

    private void processMessages() {
        while (running.get()) {
            try {
                ActorMessage msg = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                if (msg != null) {
                    Actor actor = actors.get(msg.actorName());
                    if (actor != null) {
                        actor.onMessage(msg.message());
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        running.set(false);
        executor.shutdown();
    }

    private record ActorMessage(String actorName, Object message) {}
}
