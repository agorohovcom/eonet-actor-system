package io.github.agorohovcom.eonet.actor;

public class PollingSchedulerActor implements Actor {
    private final ActorSystem system;
    private final String pollerActor;

    private boolean running = false;
    private int scheduledPolls = 0;

    public PollingSchedulerActor(ActorSystem system, String pollerActor) {
        this.system = system;
        this.pollerActor = pollerActor;
    }

    @Override
    public void onMessage(Object message) {
        if (message instanceof StartPolling) {
            startPeriodicPolling();
        } else if (message instanceof StopPolling) {
            stopPeriodicPolling();
        }
    }

    private void startPeriodicPolling() {
        if (!running) {
            running = true;
            scheduledPolls = 0;
            scheduleNextPoll();
        }
    }

    private void stopPeriodicPolling() {
        running = false;
    }

    private void scheduleNextPoll() {
        if (!running) return;

        scheduledPolls++;

        // Запускаем в отдельном потоке, чтобы не блокировать акторную систему
        new Thread(() -> {
            try {
                Thread.sleep(10000); // 10 секунд между опросами
                if (running) {
                    system.sendMessage(pollerActor, new StartPolling());
                    scheduleNextPoll();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
